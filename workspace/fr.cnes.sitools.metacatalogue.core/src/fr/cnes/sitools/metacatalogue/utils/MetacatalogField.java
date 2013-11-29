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
  /** SPECIFIC, The GEOMETRY WKT STRING. */
  _GEOMETRY("_geometry"),

  /** SPECIFIC, The UUID used . */
  _UUID("_uuid"),

  /** SPECIFIC, where or not the services are public . */
  _PUBLIC_SERVICES("_publicServices", Boolean.class),

  /** GEOMETRY AS GEOJSON, */
  _GEOMETRY_GEOJSON("_geometry_geojson"),

  /** The Constant ANY. */
  _ANY("_any"),

  /** The Constant RESOLUTION DOMAIN. */
  _RESOLUTION_DOMAIN("_resolution_domain"),

  /**
   * ##################### Modèle de données du métacatalogue #########################
   */

  ID("id"),

  LANGUAGE("language"),

  MODIFICATION_DATE("modificationDate", Date.class),

  TITLE("title"),

  DESCRIPTION("description"),

  RESOURCE_TYPE("resourceType"),

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

  KEYWORDS("keywords"),

  FOOTPRINT("footprint"),

  /** QUICKLOOK, url vers le quicklook du produit */
  QUICKLOOK("quicklook"),

  /** THUMBNAIL, url vers l'imagette du produit */
  THUMBNAIL("thumbnail"),

  /** The Project. */
  PROJECT("project"),

  /** The product. */
  PRODUCT("product"),

  /** ACQUISITION_SETUP */
  ACQUISITION_SETUP("acquisitionSetup"),

  /** COUNTRY */
  COUNTRY("country"),

  /** REGION */
  REGION("region"),

  /** DEPARTMENT */
  DEPARTMENT("department"),

  /** CITY */
  CITY("city"),

  // POINT_OF_CONTACT_EMAIL_ADDRESS("pointOfContact.emailAddress"),
  //
  // POINT_OF_CONTACT_ROLE("pointOfContact.role"),

  CHARACTERISATION_SPATIAL_AXIS("characterisation.spatialAxis"),
 
  // CHARACTERISATION_WAVELENGTH("characterisation.wavelength"),
  //
  // ACQUISITION_SETUP_FACILITY_ORGANISATION("acquisitionSetup.facility.organisationName"),
  //
  // ACQUISITION_SETUP_FACILITY_EMAIL_ADDRESS("acquisitionSetup.facility.emailAddress"),
  //
  // ACQUISITION_SETUP_FACILITY_ROLE("acquisitionSetup.facility.role"),

  // ACQUISITION_SETUP_PHYSICAL_PARAMETERS("acquisitionSetup.physicalParameters"),
  //
  // ACQUISITION_SETUP_ILLUMINATION_ELEVATION("acquisitionSetup.illuminationElevation"),
  //
  // ACQUISITION_SETUP_CLOUD_COVER("acquisitionSetup.cloudCover"),
  //
  // CONFORMITY_SPECIFICATION_TITLE("conformity.specification.title"),
  //
  // CONFORMITY_SPECIFICATION_DATE("conformity.specification.date", Date.class),
  //
  // CONFORMITY_SPECIFICATION_DATETYPE("conformity.specification.dateType"),
  //
  // CONFORMITY_EXPLANATION("conformity.explanation"),
  //
  // CONFORMITY_PASS("conformity.pass"),
  //
  // DISTRIBUTION_ACCESS_LIMITATION_PUBLIC_ACCESS("distributionAccess.limitationPublicAccess"),
  //
  // DISTRIBUTION_ACCESS_CONDITION_FOR_ACCESS_AND_USE("distributionAccess.conditionForAccessAndUse"),

  // DISTRIBUTION_ACCESS_FILESIZE("distributionAccess.filesize"),

  // DISTRIBUTION_ACCESS_VERSION("distributionAccess.version"),

   SERVICES_BROWSE_TITLE("services.browse.title"),
  
   SERVICES_BROWSE_LAYER_TYPE("services.browse.layer.type"),
  
   SERVICES_BROWSE_LAYER_URL("services.browse.layer.url"),
  
   SERVICES_BROWSE_LAYER_LAYERS("services.browse.layer.layers"),
  
   SERVICES_BROWSE_LAYER_VERSION("services.browse.layer.version"),
  
   SERVICES_BROWSE_LAYER_BBOX("services.browse.layer.bbox"),
  
   SERVICES_BROWSE_LAYER_SRS("services.browse.layer.srs"),

   SERVICES_BROWSE_VERSION("services.wms.version"),

   SERVICES_BROWSE_CREDITS("services.wms.credits"),

   SERVICES_METADATA_URL("services.metadata.url");
  //
  // TOPIC_CATEGORY("topicCategory"),

  // PROPERTIES_KEY("properties.key"),

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
    
    for ( String fieldName : mandatoryFields.split(",") ){
      if (getField(fieldName)!=null){
        list.add(getField(fieldName));
      }
    }
    
    return list;

  }

  public boolean isInspire() {
    return field.startsWith("INSPIRE.");
  }

  public boolean isMetacatalogIntern() {
    return this.equals(MetacatalogField._UUID) || this.equals(MetacatalogField._GEOMETRY)
        || this.field.startsWith("healpix-order-");
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
