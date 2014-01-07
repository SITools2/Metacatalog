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
package fr.cnes.sitools.metacatalogue.representation;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.util.DateUtils;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

/**
 * Produce a GeoJson representation from a DatabaseRequest, a geometry column and a list of converters
 * 
 * @author m.gond
 */
public class GeoJsonMDEORepresentation extends JsonRepresentation {
  /** The name of the geometry column in the solr index */
  private static final String GEOMETRY_COLUMN = MetacatalogField._GEOMETRY_GEOJSON.getField();
  /** The name of the geometry column in the solr index */
  private static final String UUID_COLUMN = MetacatalogField.IDENTIFIER.getField();
  /** The date format */
  private static final String FORMAT_ISO_8601_WITHOUT_TIME_ZONE = "yyyy-MM-dd'T'HH:mm:ss";
  /** The WMS proxy uri attachment */
  private static final String WMS_PROXY_URI = "/wms";
  /** The Download proxy uri attachment */
  private static final String DOWNLOAD_PROXY_URI = "/download";

  /** the parameters */
  private SolrDocumentList listDocuments;
  /** True if the user is authenticated, false otherwise */
  private boolean authenticatedUser;
  /** The application base url */
  private String applicationBaseUrl;

  private List<FacetField> facets;
  private NamedList<List<PivotField>> pivotFacets;

  private ThesaurusSearcher searcher;
  private Map<String, String> conceptsMap;
  private List<RangeFacet> rangesFacets;
  private List<String> thesaurusFacetFields;

  /**
   * Constructor with a DatabaseRequestParameters, a geometryColName and a converterChained
   * 
   * @param authenticatedUser
   *          true if the user is authenticated, false otherwise
   * @param applicationBaseUrl
   *          the current application base url
   * @param thesaurusFacetFields
   *          TODO
   * @param listDocuments
   *          the listDocuments
   * @param searcher
   */
  public GeoJsonMDEORepresentation(QueryResponse queryResponse, boolean authenticatedUser, String applicationBaseUrl,
      Map<String, String> conceptsMap, List<String> thesaurusFacetFields) {
    super(MediaType.APPLICATION_JSON);
    this.listDocuments = queryResponse.getResults();
    this.facets = queryResponse.getFacetFields();
    this.pivotFacets = queryResponse.getFacetPivot();
    this.rangesFacets = queryResponse.getFacetRanges();
    this.authenticatedUser = authenticatedUser;
    this.applicationBaseUrl = applicationBaseUrl;
    this.conceptsMap = conceptsMap;
    this.thesaurusFacetFields = thesaurusFacetFields;
  }

  @Override
  public void write(Writer writer) throws IOException {
    try {
      writer.write("{");
      writer.write("\"type\":\"FeatureCollection\",");
      // start features
      writer.write("\"totalResults\":" + listDocuments.getNumFound() + ",");
      writer.write("\"facet_counts\":");
      writer.write("{");
      boolean commaFacet = false;
      if (this.facets != null && !this.facets.isEmpty()) {
        commaFacet = true;
        writer.write("\"facet_fields\":" + getFacetFields());
      }
      if (this.pivotFacets != null && this.pivotFacets.size() > 0) {
        if (commaFacet) {
          writer.write(",");
        }
        commaFacet = true;
        writer.write("\"facet_pivot\":" + getFacetPivots());
      }
      if (this.rangesFacets != null && !this.rangesFacets.isEmpty()) {
        if (commaFacet) {
          writer.write(",");
        }
        writer.write("\"facet_ranges\":" + getFacetRanges());
      }
      writer.write("}");
      writer.write(",");
      writer.write("\"features\":[");
      try {
        boolean first = true;
        for (SolrDocument solrDocument : listDocuments) {

          // creates a geometry and a properties string
          String geometry = new String();
          String uuid = (String) solrDocument.getFieldValue(UUID_COLUMN);

          boolean publicServices = false;

          Collection<String> fieldNames = solrDocument.getFieldNames();
          JSONObject propertiesObject = new JSONObject();

          publicServices = getPublicServices(solrDocument);

          for (String fieldName : fieldNames) {
            Object fieldValue = solrDocument.get(fieldName);
            if (fieldName.equals(GEOMETRY_COLUMN)) {
              geometry += fieldValue;
            }
            else {
              if (fieldValue != null && !"".equals(fieldValue)) {

                if (fieldValue instanceof Date) {
                  fieldValue = DateUtils.format((Date) fieldValue, FORMAT_ISO_8601_WITHOUT_TIME_ZONE);
                }

                MetacatalogField metafield = MetacatalogField.getField(fieldName);
                if (metafield != null) {
                  switch (metafield) {
                    case WMS:
                    case ARCHIVE:
                    case MIME_TYPE:
                      if (publicServices || (!publicServices && authenticatedUser)) {
                        propertiesObject.put(fieldName, fieldValue);
                      }
                      break;
                    default:
                      if (!metafield.isMetacatalogIntern()) {
                        propertiesObject.put(fieldName, fieldValue);
                      }
                      break;

                  }
                }
                else if (fieldName.startsWith("properties.")) {
                  String name = fieldName.substring("properties.".length());
                  propertiesObject.put(name, fieldValue);
                }
              }
            }
          }
          if (!geometry.isEmpty()) {
            if (!first) {
              writer.write(",");
            }
            else {
              first = false;
            }

            // start feature
            writer.write("{");
            writer.write("\"type\":\"Feature\",");
            // start geometry
            writer.write("\"geometry\":");
            writer.write(geometry);
            // end geometry
            writer.write(",");
            // start properties
            // writer.write("\"properties\":{");
            // writer.write(properties);
            // // end properties
            // writer.write("}");

            writer.write("\"properties\":");
            writer.write(propertiesObject.toString());

            // end feature
            writer.write("}");
          }

        }
        // end features
        writer.write("]");
      }
      // catch (SitoolsException e) {
      // writer.write("],");
      // writer.write("\"error\":{");
      // writer.write("\"code\":");
      // writer.write("\"message\":" + e.getLocalizedMessage());
      // writer.write("}");
      //
      // }
      finally {
        writer.write("}");
        if (writer != null) {
          writer.flush();
        }
      }
    }
    catch (JSONException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }

  }

  // private JSONObject parseJSON(String jsonString) throws JSONException {
  // JSONTokener tokener = new JSONTokener(jsonString);
  // JSONObject root = new JSONObject(tokener);
  // return root;
  //
  // }

  private boolean getPublicServices(SolrDocument solrDocument) {
    Object obj = solrDocument.getFirstValue(MetacatalogField._PUBLIC_SERVICES.getField());
    if (obj != null && obj instanceof Boolean) {
      return (Boolean) obj;
    }
    return false;
  }

  private String getFacetPivots() throws JSONException {

    JSONObject pivotFacetsJSON = new JSONObject();

    for (Entry<String, List<PivotField>> field : this.pivotFacets) {
      pivotFacetsJSON.put(field.getKey(), getFacetPivots(field.getValue()));
    }
    return pivotFacetsJSON.toString();
  }

  private JSONArray getFacetPivots(List<PivotField> pivotFields) throws JSONException {
    if (pivotFields == null) {
      return null;
    }
    JSONArray array = new JSONArray();
    for (PivotField field : pivotFields) {
      JSONObject out = new JSONObject();
      out.put("field", field.getField());
      out.put("value", getValue(field.getField(), field.getValue().toString()));
      out.put("count", field.getCount());
      if (field.getPivot() != null) {
        out.put("pivot", getFacetPivots(field.getPivot()));
      }
      array.put(out);
    }
    return array;
  }

  private String getFacetFields() throws JSONException {
    JSONObject facetsJSON = new JSONObject();
    if (this.facets != null) {
      for (FacetField field : this.facets) {
        JSONArray array = new JSONArray();
        for (Count count : field.getValues()) {
          array.put(getValue(field.getName(), count.getName()));
          array.put(count.getCount());
        }
        facetsJSON.put(field.getName(), array);
      }
    }

    return facetsJSON.toString();
  }

  private String getFacetRanges() throws JSONException {
    JSONObject facetsJSON = new JSONObject();
    if (this.rangesFacets != null) {
      for (RangeFacet facet : this.rangesFacets) {
        JSONObject facetObject = new JSONObject();
        JSONArray counts = new JSONArray();
        if (facet instanceof RangeFacet.Date) {
          RangeFacet.Date facetDate = (RangeFacet.Date) facet;
          for (RangeFacet.Count count : facetDate.getCounts()) {
            counts.put(count.getValue());
            counts.put(count.getCount());
          }
        }
        if (facet instanceof RangeFacet.Numeric) {
          RangeFacet.Numeric facetDate = (RangeFacet.Numeric) facet;
          for (RangeFacet.Count count : facetDate.getCounts()) {
            counts.put(count.getValue());
            counts.put(count.getCount());
          }
        }
        facetObject.put("counts", counts);
        facetObject.put("start", facet.getStart());
        facetObject.put("end", facet.getEnd());
        facetObject.put("gap", facet.getGap());
        facetObject.put("after", facet.getAfter());
        facetObject.put("before", facet.getBefore());
        facetObject.put("between", facet.getBetween());

        facetsJSON.put(facet.getName(), facetObject);
      }
    }

    return facetsJSON.toString();
  }

  private String getValue(String name, String value) {
    if (this.thesaurusFacetFields.contains(name)) {
      return getValueFromThesaurus(value);
    }
    else {
      return value;
    }

  }

  private String getValueFromThesaurus(String value) {
    String concept = conceptsMap.get(value.toLowerCase());
    if (concept != null) {
      return concept;
    }
    return value;
  }
}
