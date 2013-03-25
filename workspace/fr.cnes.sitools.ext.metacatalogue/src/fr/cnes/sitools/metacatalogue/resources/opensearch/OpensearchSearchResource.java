/*******************************************************************************
 * Copyright 2011 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 * 
 * This file is part of SITools2.
 * 
 * SITools2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * SITools2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.cnes.sitools.metacatalogue.resources.opensearch;

import java.text.ParseException;
import java.util.Date;
import java.util.logging.Level;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.SitoolsResource;
import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.representation.GeoJsonMDEORepresentation;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginParameter;
import fr.cnes.sitools.util.DateUtils;

public class OpensearchSearchResource extends SitoolsResource {

  private MetacatalogueApplication application;

  private String solrCoreUrl;

  private String geometryQueryType;

  private static String SOLR_DATE_FORMAT = DateUtils.FORMAT_ISO_8601_WITHOUT_TIME_ZONE + "'Z'";

  @Override
  public void sitoolsDescribe() {
    setName("OpensearchSearchResource");
    setDescription("Opensearch search resource, expose the result as GeoJSON");
  }

  @Override
  protected void doInit() {
    super.doInit();

    application = (MetacatalogueApplication) getApplication();

    ApplicationPluginParameter solrCoreUrlParameter = application.getParameter("metacatalogSolrCore");
    if (solrCoreUrlParameter == null || solrCoreUrlParameter.getValue() == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No solr core url defined, cannot perform search");
    }

    ApplicationPluginParameter solrCoreNameParameter = application.getParameter("metacatalogSolrName");
    if (solrCoreNameParameter == null || solrCoreNameParameter.getValue() == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No solr core name defined, cannot perform search");
    }
    solrCoreUrl = solrCoreUrlParameter.getValue() + "/" + solrCoreNameParameter.getValue();

    ApplicationPluginParameter geometryQueryTypeParameter = application.getParameter("geometryQueryType");
    if (geometryQueryTypeParameter == null || geometryQueryTypeParameter.getValue() == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No geometry query type defined");
    }
    geometryQueryType = geometryQueryTypeParameter.getValue();
  }

  /**
   * Actions on PUT
   * 
   * @param variant
   *          MediaType of response
   * @return Representation response
   */
  @Get
  public Representation get(Variant variant) {
    Representation repr = null;

    Form query = getRequest().getResourceRef().getQueryAsForm();
    SolrServer server = SolRUtils.getSolRServer(solrCoreUrl);
    if (server == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Solr core : " + solrCoreUrl + " not reachable");
    }

    SolrQuery solrQuery = new SolrQuery();

    Integer count = getIntegerParameter(query, OpenSearchQuery.COUNT);
    Integer startIndex = getIntegerParameter(query, OpenSearchQuery.START_INDEX);
    Integer startPage = getIntegerParameter(query, OpenSearchQuery.START_PAGE);

    int start = count * (startPage - 1) + startIndex - 1; // index in solr start at 0, not a 1
    if (start < 0) {
      start = 0;
    }
    int rows = count;

    solrQuery.setStart(start);
    solrQuery.setRows(rows);
    solrQuery.add("df", "searchTerms");

    addGeometryCriteria(solrQuery, query);
    try {
      setQuery(solrQuery, query);

      getLogger().log(Level.INFO, "SOLR query : " + solrQuery.toString());

      QueryResponse rsp = server.query(solrQuery);
      SolrDocumentList listDoc = rsp.getResults();

      boolean isAuthenticated = getClientInfo().isAuthenticated();
      SitoolsSettings settings = getSettings();
      String applicationBaseUrl = settings.getPublicHostDomain() + application.getAttachementRef();

      repr = new GeoJsonMDEORepresentation(listDoc, isAuthenticated, applicationBaseUrl);
    }
    catch (SolrServerException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error while querying solr index", e);
    }
    catch (Exception e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error while querying solr index", e);
    }

    return repr;
  }

  @Override
  public void describeGet(MethodInfo info) {
    info.setDocumentation("Method to query the metacatalog with opensearch parameters and get geojson as a return");
    info.setIdentifier("opensearch_query");
    // ParameterInfo pic = new ParameterInfo("datasetId", true, "xs:string", ParameterStyle.TEMPLATE,
    // "Identifier of the dataset");
    // info.getRequest().getParameters().add(pic);
    addStandardResponseInfo(info);
    addStandardInternalServerErrorInfo(info);
  }

  private void setQuery(SolrQuery solrQuery, Form query) throws Exception {
    String queryStr = "";
    boolean first = true;
    for (Parameter parameter : query) {
      String pieceOfQuery = null;

      if (!isStandardParameter(parameter.getName())) {
        pieceOfQuery = parameter.getName() + ":" + parameter.getSecond();
      }
      else if (parameter.getName().equals(OpenSearchQuery.SEARCH_TERMS.getParamName())) {
        pieceOfQuery = parameter.getSecond();
      }
      else if (parameter.getName().equals(OpenSearchQuery.MODIFIED.getParamName())) {
        String dateStr = getDateParam(parameter, DATE_QUERY_TYPE.GT);
        if (dateStr != null) {
          pieceOfQuery = MetacatalogField.MODIFICATION_DATE.getField() + ":" + dateStr;
        }
      }
      else if (parameter.getName().equals(OpenSearchQuery.TIME_START.getParamName())) {
        String dateStr = getDateParam(parameter, DATE_QUERY_TYPE.GT);
        if (dateStr != null) {
          pieceOfQuery = MetacatalogField.CHARACTERISATION_AXIS_TEMPORAL_AXIS_MIN.getField() + ":" + dateStr;
        }
      }
      else if (parameter.getName().equals(OpenSearchQuery.TIME_END.getParamName())) {
        String dateStr = getDateParam(parameter, DATE_QUERY_TYPE.LT);
        if (dateStr != null) {
          pieceOfQuery = MetacatalogField.CHARACTERISATION_AXIS_TEMPORAL_AXIS_MAX.getField() + ":" + dateStr;
        }
      }
      if (pieceOfQuery != null) {
        if (!first) {
          queryStr += " AND ";
        }
        first = false;
        queryStr += pieceOfQuery;

      }
    }

    if (queryStr.isEmpty()) {
      queryStr = "*";
    }
    solrQuery.setQuery(queryStr);
  }

  // private String getGeometryCriteriaHealpix(Form query) throws Exception {
  //
  // String box = query.getFirstValue(OpenSearchQuery.GEO_BOX.getParamName());
  // if (box != null && !box.isEmpty()) {
  // String[] boxCoord = box.split(",");
  //
  // double long1 = new Double(boxCoord[0]);
  // double lat1 = new Double(boxCoord[1]);
  // double long2 = new Double(boxCoord[2]);
  // double lat2 = new Double(boxCoord[3]);
  //
  // CoordSystem coordSystem = CoordSystem.GEOCENTRIC;
  //
  // Point point1 = new Point(long1, lat1, coordSystem);
  // Point point2 = new Point(long2, lat2, coordSystem);
  //
  // // TODO mettre point1 en premier et point2 en second lors du changement de version de la librairies de JCM
  // Polygon shape = new Polygon(point2, point1);
  //
  // System.out.println(shape.toString());
  //
  // RingIndex healpixIndex = (RingIndex) GeometryIndex.createIndex(shape, Scheme.RING);
  // if (healpixIndex.getOrder() >= MAX_ORDER) {
  // healpixIndex.setOrder(MAX_ORDER);
  // }
  //
  // int nbOrder = healpixIndex.getOrder();
  //
  // // faire iterator
  // // RangeSet.ValueIterator valueIter = ((RangeSet) index).valueIterator();
  //
  // RangeSet healpixResult = ((RangeSet) healpixIndex.getIndex());
  //
  // String constraint = "(";
  //
  // boolean first = true;
  // for (int i = 0; i < healpixResult.size(); i++) {
  // if (first) {
  // first = false;
  // }
  // else {
  // constraint += " OR ";
  // }
  // constraint += String.format("healpix-order-%s:", nbOrder);
  // constraint += String.format("[%s TO %s]", healpixResult.ivbegin(i), healpixResult.ivend(i));
  //
  // }
  //
  // constraint = constraint.concat(")");
  // System.out.println("Constraint healpix : " + constraint);
  // return constraint;
  // }
  // else {
  // return null;
  // }
  //
  // }

  // private void addGeometryCriteria(SolrQuery solrQuery, Form query) {
  // String box = query.getFirstValue(OpenSearchQuery.GEO_BOX.getParamName());
  // if (box != null && !box.isEmpty()) {
  // String[] boxCoord = box.split(",");
  //
  // double minX = new Double(boxCoord[0]);
  // double minY = new Double(boxCoord[1]);
  // double maxX = new Double(boxCoord[2]);
  // double maxY = new Double(boxCoord[3]);
  //
  // String geomStr = getGeometryQueryString(minX, minY, maxX, maxY);
  //
  // String xmlQuery = "<query>";
  // xmlQuery += "<relation>intersection</relation>";
  // xmlQuery += "<geometry>";
  // xmlQuery += geomStr;
  // xmlQuery += "</geometry>";
  // xmlQuery += "</query>";
  //
  // solrQuery.add("openwisRequest", xmlQuery);
  // solrQuery.add("defType", "OpenwisSearch");
  // }
  // }

  private void addGeometryCriteria(SolrQuery solrQuery, Form query) {
    String box = query.getFirstValue(OpenSearchQuery.GEO_BOX.getParamName());
    if (box != null && !box.isEmpty()) {
      solrQuery.add("geoRequest.type", geometryQueryType);
      solrQuery.add("geoRequest", box);
      solrQuery.add("defType", "GeometrySearch");
    }
  }

  private String getGeometryQueryString(double minX, double minY, double maxX, double maxY) {
    String point1 = minX + " " + minY;
    String point2 = maxX + " " + minY;
    String point3 = maxX + " " + maxY;
    String point4 = minX + " " + maxY;

    String geomStr = "POLYGON ((" + point1 + "," + point2 + "," + point3 + "," + point4 + "," + point1 + "))";
    return geomStr;
  }

  private Integer getIntegerParameter(Form query, OpenSearchQuery param) {
    String paramStr = query.getFirstValue(param.getParamName());
    Integer value = null;
    if (paramStr != null && !"".equals(paramStr)) {
      value = Integer.parseInt(paramStr);
    }
    else {
      value = (Integer) param.getDefaultValue();
    }
    return value;
  }

  private boolean isStandardParameter(String name) {
    // name.equals dc added to test with SITools2 poster (ExtJs adds a _dc parameter at the end of the query
    return (name.equals("_dc") || OpenSearchQuery.getFieldFromParamName(name) != null);
  }

  /**
   * Get the Solr parameter from a Date parameter
   * 
   * @param parameter
   * @param from
   * @return
   */
  private String getDateParam(Parameter parameter, DATE_QUERY_TYPE type) {
    String dateStr = parameter.getSecond();
    if (dateStr != null && !"".equals(dateStr)) {

      if (dateStr.contains("/")) {
        String[] dates = dateStr.split("/");
        String date1 = dates[0];
        String date2 = dates[1];
        try {
          date1 = formatSolrDate(date1);
          date2 = formatSolrDate(date2);
          return formatSolrDateInterval(date1, date2);
        }
        catch (ParseException e) {
          getLogger().warning("Cannot parse date range : " + dateStr);
          return null;
        }
      }
      else {
        try {
          dateStr = formatSolrDate(dateStr);
          switch (type) {
            case EQ:
              return dateStr;
            case GT:
              return formatSolrDateInterval(dateStr, "*");
            case LT:
              return formatSolrDateInterval("*", dateStr);
            default:
              getLogger().warning("Unsuppported operator : " + dateStr);
              return null;
          }
        }
        catch (ParseException e) {
          getLogger().warning("Cannot parse date : " + dateStr);
          return null;
        }
      }
    }
    else {
      return null;
    }

  }

  public String formatSolrDateInterval(String date1, String date2) {
    return "[" + date1 + " TO " + date2 + "]";
  }

  public String formatSolrDate(String dateStr) throws ParseException {
    Date date;
    date = DateUtils.parse(dateStr, DateUtils.FORMAT_ISO_8601_WITHOUT_TIME_ZONE);
    dateStr = DateUtils.format(date, SOLR_DATE_FORMAT);
    dateStr = escapeDate(dateStr);
    return dateStr;
  }

  private String escapeDate(String dateStr) {
    return dateStr.replace(":", "\\:");
  }

  private enum DATE_QUERY_TYPE {
    /** ALL dates greater than the given one */
    GT,
    /** ALL dates lower than the given one */
    LT,
    /** ALL dates equals to the given one */
    EQ
  }

}
