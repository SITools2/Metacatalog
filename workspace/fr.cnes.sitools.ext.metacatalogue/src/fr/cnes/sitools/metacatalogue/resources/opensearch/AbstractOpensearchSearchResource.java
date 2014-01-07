/*******************************************************************************
 * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import com.google.common.base.Joiner;

import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.metacatalogue.representation.GeoJsonMDEORepresentation;
import fr.cnes.sitools.metacatalogue.resources.AbstractSearchResource;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;
import fr.cnes.sitools.util.DateUtils;

public abstract class AbstractOpensearchSearchResource extends AbstractSearchResource {

  protected enum DATE_QUERY_TYPE {
    /** ALL dates greater than the given one */
    GT,
    /** ALL dates lower than the given one */
    LT,
    /** ALL dates equals to the given one */
    EQ
  }

  /** The default date format */
  protected static String SOLR_DATE_FORMAT = DateUtils.FORMAT_ISO_8601_WITHOUT_TIME_ZONE + "'Z'";

  @Override
  public void sitoolsDescribe() {
    setName("OpensearchSearchResource");
    setDescription("Opensearch search resource, expose the result as GeoJSON");
  }

  protected Representation opensearchQuery(Representation repr, Form query, SolrServer server,
      ThesaurusSearcher searcher) {
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

    // addGeometryCriteria(solrQuery, query);
    try {
      setQuery(solrQuery, query);
      setFacet(solrQuery);

      getLogger().log(Level.INFO, "SOLR query : " + solrQuery.toString());

      QueryResponse rsp = server.query(solrQuery);

      boolean isAuthenticated = getClientInfo().isAuthenticated();
      SitoolsSettings settings = getSettings();
      String applicationBaseUrl = settings.getPublicHostDomain() + application.getAttachementRef();

      repr = new GeoJsonMDEORepresentation(rsp, isAuthenticated, applicationBaseUrl,
          searcher.getAllConceptsAsMap(getLanguage()), thesaurusFacetFields);
    }
    catch (SolrServerException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error while querying solr index", e);
    }
    catch (Exception e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error while querying solr index", e);
    }
    return repr;
  }

  private void setFacet(SolrQuery solrQuery) {

    solrQuery.addFacetField(MetacatalogField._RESOLUTION_DOMAIN.getField());
    // Date dateStart;
    // try {
    // dateStart = DateUtils.parse("1980-01-01T00:00:00.0");
    // solrQuery.addDateRangeFacet("characterisationAxis.temporalAxis.min", dateStart, new Date(), "+1YEAR");
    // }
    // catch (ParseException e) {
    // e.printStackTrace();
    // }

    List<String> plateformIntrument = new ArrayList<String>();
    plateformIntrument.add(MetacatalogField.PLATFORM.getField());
    plateformIntrument.add(MetacatalogField.INSTRUMENT.getField());

    List<String> location = new ArrayList<String>();
    location.add(MetacatalogField.COUNTRY.getField());
    location.add(MetacatalogField.REGION.getField());
    location.add(MetacatalogField.DEPARTMENT.getField());
    location.add(MetacatalogField.CITY.getField());

    solrQuery.add("facet.pivot", Joiner.on(",").join(plateformIntrument));
    solrQuery.add("facet.pivot", Joiner.on(",").join(location));
    solrQuery.addFacetField(MetacatalogField.PROCESSING_LEVEL.getField());
    solrQuery.addFacetField(MetacatalogField.PRODUCT.getField());
    solrQuery.add("facet.pivot.mincount", "0");
    solrQuery.setFacetLimit(10);
    solrQuery.setFacetMinCount(1);

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
          pieceOfQuery = MetacatalogField.MODIFIED.getField() + ":" + dateStr;
        }
      }
      else if (parameter.getName().equals(OpenSearchQuery.TIME_START.getParamName())) {
        String dateStr = getDateParam(parameter, DATE_QUERY_TYPE.GT);
        if (dateStr != null) {
          pieceOfQuery = MetacatalogField.START_DATE.getField() + ":" + dateStr;
        }
      }
      else if (parameter.getName().equals(OpenSearchQuery.TIME_END.getParamName())) {
        String dateStr = getDateParam(parameter, DATE_QUERY_TYPE.LT);
        if (dateStr != null) {
          pieceOfQuery = MetacatalogField.COMPLETION_DATE.getField() + ":" + dateStr;
        }
      }
      else if (parameter.getName().equals(OpenSearchQuery.GEO_BOX.getParamName())) {
        String bbox = parameter.getValue();
        if (bbox != null && !bbox.isEmpty()) {
          pieceOfQuery = MetacatalogField.GEOGRAPHICAL_EXTENT.getField() + ":" + getGeometryCriteria(bbox);
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

  private String getGeometryCriteria(String box) {
    String wkt = parseBbox(box);
    return String.format("\"Intersects(%s)\"", wkt);
  }

  private String parseBbox(String box) {

    String[] boxCoord = box.split(",");

    double minX = new Double(boxCoord[0]);
    double minY = new Double(boxCoord[1]);
    double maxX = new Double(boxCoord[2]);
    double maxY = new Double(boxCoord[3]);

    return getGeometryQueryString(minX, minY, maxX, maxY);
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
    return (name.equals("_dc") || name.equals("lang") || OpenSearchQuery.getFieldFromParamName(name) != null);
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

}
