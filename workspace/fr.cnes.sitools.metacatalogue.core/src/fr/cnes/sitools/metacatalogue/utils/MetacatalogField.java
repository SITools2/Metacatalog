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
package fr.cnes.sitools.metacatalogue.utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.solr.common.util.DateUtil;

public enum MetacatalogField {

  /**
   * ##################### Champs Internes au métacatalogue #########################
   */

  /** SPECIFIC, where or not the services are public . */
  _PUBLIC_SERVICES("_publicServices", Boolean.class),

  /** GEOMETRY AS GEOJSON, */
  _GEOMETRY_GEOJSON("_geometry_geojson"),

  /** The Constant ANY. */
  _ANY("_any"),

  /** The Constant RESOLUTION DOMAIN. */
  _RESOLUTION_DOMAIN("_resolution_domain"),

  /** list of AltLabels concepts used for suggestions */
  _CONCEPTS("_concepts"),

  /**
   * ##################### Modèle de données du métacatalogue #########################
   */

  FOOTPRINT("footprint"),

  IDENTIFIER("identifier"),

  LANGUAGE("language"),

  TITLE("title"),

  DESCRIPTION("description"),

  LINEAGE("lineage"),

  PROCESSING_LEVEL("processingLevel"),

  AUTHORITY("authority"),

  START_DATE("startDate", Date.class),

  COMPLETION_DATE("completionDate", Date.class),

  INSTRUMENT("instrument"),

  PLATFORM("platform"),

  RESOLUTION("resolution"),

  ARCHIVE("archive"),

  MIME_TYPE("mimeType"),

  WMS("wms"),

  KEYWORDS("keywords"),

  /** QUICKLOOK, url vers le quicklook du produit */
  QUICKLOOK("quicklook"),

  /** THUMBNAIL, url vers l'imagette du produit */
  THUMBNAIL("thumbnail"),

  /** The Project. */
  PROJECT("project"),

  /** The product. */
  PRODUCT("product"),

  /** COUNTRY */
  COUNTRY("country"),

  /** REGION */
  REGION("region"),

  /** DEPARTMENT */
  DEPARTMENT("department"),

  /** CITY */
  CITY("city"),

  CREATED("created", Date.class),

  MODIFIED("modified", Date.class);

  /** The field. */
  private final String field;

  /** The clazz. */
  @SuppressWarnings("rawtypes")
  private final Class clazz;

  /**
   * Instantiates a new index field.
   * 
   * @param field
   *          the field
   * 
   * @param clazz
   *          the clazz
   */
  @SuppressWarnings("rawtypes")
  private MetacatalogField(String field, Class clazz) {
    this.field = field;
    this.clazz = clazz;
  }

  /**
   * Instantiates a new index field.
   * 
   * @param field
   *          the field
   */
  private MetacatalogField(String field) {
    this(field, String.class);
  }

  /**
   * Value from string.
   * 
   * @param value
   *          the value
   * @return the object
   * @throws ParseException
   *           the parse exception
   */
  public Object valueFromString(String value) throws ParseException {
    Object result;
    if (String.class.equals(clazz)) {
      result = value;
    }
    else if (Date.class.equals(clazz)) {
      result = DateUtil.parseDate(value);
    }
    else if (Double.class.equals(clazz)) {
      result = Double.valueOf(value);
    }
    else if (Integer.class.equals(clazz)) {
      result = Integer.valueOf(value);
    }
    else {
      // By default return the object
      result = value;
    }
    return result;
  }

  /**
   * Value to string.
   * 
   * @param value
   *          the value
   * @return the string
   */
  public String valueToString(Object value) {
    String result;
    if (String.class.equals(clazz)) {
      result = (String) value;
    }
    else if (Date.class.equals(clazz)) {
      result = DateUtil.getThreadLocalDateFormat().format((Date) value);
    }
    else if (Double.class.equals(clazz)) {
      result = String.valueOf(value);
    }
    else {
      // By default return the object
      result = ObjectUtils.toString(value);
    }
    return result;

  }

  /**
   * Gets the field.
   * 
   * @param fieldName
   *          the field name
   * @return the field
   */
  public static MetacatalogField getField(String fieldName) {
    MetacatalogField result = null;
    for (MetacatalogField field : values()) {
      if (field.getField().equals(fieldName)) {
        result = field;
        break;
      }
    }
    return result;
  }

  /**
   * Get the list of mandatory fields from the metacatalogue.properties
   * 
   * @return List<MetacatalogField>
   */
  public static List<MetacatalogField> getMandatoryFields() {

    List<MetacatalogField> list = new ArrayList<MetacatalogField>();
    String mandatoryFields = (String) HarvesterSettings.getInstance().get("MANDATORY_FIELDS");

    for (String fieldName : mandatoryFields.split(",")) {
      if (getField(fieldName) != null) {
        list.add(getField(fieldName));
      }
    }

    return list;

  }

  /**
   * Get the list of mandatory fields from the metacatalogue.properties
   * 
   * @return List<MetacatalogField>
   */
  public static List<MetacatalogField> getThesaurusFields() {

    List<MetacatalogField> list = new ArrayList<MetacatalogField>();
    String thesaurusFields = (String) HarvesterSettings.getInstance().get("THESAURUS_FIELDS");

    for (String fieldName : thesaurusFields.split(",")) {
      if (getField(fieldName) != null) {
        list.add(getField(fieldName));
      }
    }

    return list;

  }

  public boolean isMetacatalogIntern() {
    return this.equals(MetacatalogField.FOOTPRINT) || this.getField().startsWith("_");
  }

  /**
   * Gets the field.
   * 
   * @return the field
   */
  public String getField() {
    return field;
  }

  /**
   * Checks if is date.
   * 
   * @return true, if is date
   */
  public boolean isDate() {
    return Date.class.equals(clazz);
  }

  /**
   * Checks if is boolean.
   * 
   * @return true, if is date
   */
  public boolean isBoolean() {
    return Boolean.class.equals(clazz);
  }

}
