/*******************************************************************************
 * Copyright 2010-2014 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.util.DateUtils;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

/**
 * Produce a GeoJson representation from a DatabaseRequest, a geometry column and a list of converters
 * 
 * @author m.gond
 */
public class GeoJsonMDEORepresentation extends WriterRepresentation {
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

    JsonFactory jfactory = new JsonFactory();
    JsonGenerator jGenerator = null;

    try {
      jGenerator = jfactory.createJsonGenerator(writer);
      ObjectMapper mapper = new ObjectMapper();
      jGenerator.writeStartObject();
      jGenerator.writeStringField("type", "FeatureCollection");
      jGenerator.writeNumberField("totalResults", listDocuments.getNumFound());
      // Facets
      jGenerator.writeObjectFieldStart("facet_counts");
      if (this.facets != null && !this.facets.isEmpty()) {
        writeFacetFields(jGenerator);
      }
      if (this.pivotFacets != null && this.pivotFacets.size() > 0) {
        writeFacetPivots(jGenerator);
      }
      if (this.rangesFacets != null && !this.rangesFacets.isEmpty()) {
        writeFacetRanges(jGenerator);
      }
      jGenerator.writeEndObject();
      // End Facets
      // Features
      jGenerator.writeArrayFieldStart("features");
      try {
        for (SolrDocument solrDocument : listDocuments) {

          // creates a geometry and a properties string
          String uuid = (String) solrDocument.getFieldValue(UUID_COLUMN);

          boolean publicServices = false;

          Collection<String> fieldNames = solrDocument.getFieldNames();

          Object geometry = solrDocument.get(GEOMETRY_COLUMN);

          if (geometry != null) {
            // feature
            jGenerator.writeStartObject();
            jGenerator.writeStringField("type", "Feature");
            // id
            jGenerator.writeFieldName("id");
            mapper.writeValue(jGenerator, solrDocument.get("identifier"));                        
            // geometry
            jGenerator.writeFieldName("geometry");
            jGenerator.writeRawValue(geometry.toString());
            // end geometry
            // properties
            jGenerator.writeObjectFieldStart("properties");
            publicServices = getPublicServices(solrDocument);

            for (String fieldName : fieldNames) {
              Object fieldValue = solrDocument.get(fieldName);
              if (!fieldName.equals(GEOMETRY_COLUMN)) {
                if (fieldValue != null && !"".equals(fieldValue)) {

                  if (fieldValue instanceof Date) {
                    fieldValue = DateUtils.format((Date) fieldValue, FORMAT_ISO_8601_WITHOUT_TIME_ZONE);
                  }

                  MetacatalogField metafield = MetacatalogField.getField(fieldName);
                  if (metafield != null) {
                    switch (metafield) {
                      case WMS:
                      case ARCHIVE:
                      case IDENTIFIER:
                      case MIME_TYPE:
                        if (publicServices || (!publicServices && authenticatedUser)) {
                          jGenerator.writeFieldName(fieldName);
                          mapper.writeValue(jGenerator, fieldValue);
                        }
                        break;
                      default:
                        if (!metafield.isMetacatalogIntern()) {
                          jGenerator.writeFieldName(fieldName);
                          mapper.writeValue(jGenerator, fieldValue);
                        }
                        break;

                    }
                  }
                  else if (fieldName.startsWith("properties.")) {
                    String name = fieldName.substring("properties.".length());
                    jGenerator.writeFieldName(name);
                    mapper.writeValue(jGenerator, fieldValue);
                  }
                }
              }
            }
            jGenerator.writeEndObject();
            // end properties
            jGenerator.writeEndObject();
            // end feature
          }
        }

      }
      finally {
        jGenerator.writeEndArray();
        // end features
        jGenerator.writeEndObject();
        jGenerator.flush();
        // end global object
        writer.flush();
      }
    }
    catch (Exception e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }

  }

  private boolean getPublicServices(SolrDocument solrDocument) {
    Object obj = solrDocument.getFirstValue(MetacatalogField._PUBLIC_SERVICES.getField());
    if (obj != null && obj instanceof Boolean) {
      return (Boolean) obj;
    }
    return false;
  }

  private void writeFacetPivots(JsonGenerator jGenerator) throws JsonGenerationException, IOException {

    jGenerator.writeObjectFieldStart("facet_pivot");

    for (Entry<String, List<PivotField>> field : this.pivotFacets) {
      jGenerator.writeFieldName(field.getKey());
      writeFacetPivots(jGenerator, field.getValue());
    }
    jGenerator.writeEndObject();
  }

  private void writeFacetPivots(JsonGenerator jGenerator, List<PivotField> pivotFields) throws JsonGenerationException,
    IOException {
    if (pivotFields == null) {
      return;
    }
    jGenerator.writeStartArray();
    for (PivotField field : pivotFields) {
      jGenerator.writeStartObject();
      jGenerator.writeStringField("field", field.getField());
      jGenerator.writeStringField("value", getValue(field.getField(), field.getValue().toString()));
      jGenerator.writeNumberField("count", field.getCount());
      if (field.getPivot() != null) {
        jGenerator.writeFieldName("pivot");
        writeFacetPivots(jGenerator, field.getPivot());
      }
      jGenerator.writeEndObject();
    }
    jGenerator.writeEndArray();
  }

  private void writeFacetFields(JsonGenerator jGenerator) throws JsonGenerationException, IOException {

    jGenerator.writeObjectFieldStart("facet_fields");
    if (this.facets != null) {
      for (FacetField field : this.facets) {
        jGenerator.writeArrayFieldStart(field.getName());
        for (Count count : field.getValues()) {
          jGenerator.writeString(getValue(field.getName(), count.getName()));
          jGenerator.writeNumber(count.getCount());
        }
        jGenerator.writeEndArray();
      }
    }
    jGenerator.writeEndObject();
  }

  private void writeFacetRanges(JsonGenerator jGenerator) throws JsonGenerationException, IOException {

    jGenerator.writeObjectFieldStart("facet_ranges");
    if (this.rangesFacets != null) {
      for (RangeFacet facet : this.rangesFacets) {
        jGenerator.writeObjectFieldStart(facet.getName());
        jGenerator.writeArrayFieldStart("counts");
        if (facet instanceof RangeFacet.Date) {
          RangeFacet.Date facetDate = (RangeFacet.Date) facet;
          for (RangeFacet.Count count : facetDate.getCounts()) {
            jGenerator.writeString(count.getValue());
            jGenerator.writeNumber(count.getCount());
          }
          jGenerator.writeEndArray();
          jGenerator.writeStringField("start", formatDate(facetDate.getStart()));
          jGenerator.writeStringField("end", formatDate(facetDate.getEnd()));
          jGenerator.writeStringField("gap", facetDate.getGap());
          writeNumber(jGenerator, "after", facetDate.getAfter());
          writeNumber(jGenerator, "before", facetDate.getBefore());
          writeNumber(jGenerator, "between", facetDate.getBetween());
        }
        if (facet instanceof RangeFacet.Numeric) {
          RangeFacet.Numeric facetNumeric = (RangeFacet.Numeric) facet;
          for (RangeFacet.Count count : facetNumeric.getCounts()) {
            jGenerator.writeString(count.getValue());
            jGenerator.writeNumber(count.getCount());
          }
          jGenerator.writeEndArray();
          writeNumber(jGenerator, "start", facetNumeric.getStart());
          writeNumber(jGenerator, "end", facetNumeric.getEnd());
          writeNumber(jGenerator, "gap", facetNumeric.getGap());
          writeNumber(jGenerator, "after", facetNumeric.getAfter());
          writeNumber(jGenerator, "before", facetNumeric.getBefore());
          writeNumber(jGenerator, "between", facetNumeric.getBetween());
        }

        jGenerator.writeEndObject();
      }
    }
    jGenerator.writeEndObject();
  }

  private void writeNumber(JsonGenerator jGenerator, String fieldName, Number numberValue)
    throws JsonGenerationException, IOException {
    if (numberValue != null) {
      jGenerator.writeFieldName(fieldName);
      jGenerator.writeNumber(numberValue.toString());
    }
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

  private String formatDate(Date date) {
    return DateUtils.format(date, FORMAT_ISO_8601_WITHOUT_TIME_ZONE);
  }
}
