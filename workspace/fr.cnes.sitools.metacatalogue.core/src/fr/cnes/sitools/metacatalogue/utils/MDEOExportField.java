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
import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.solr.common.util.DateUtil;

/**
 * enum to store every field of the Modèle de données EO (CNES)
 * 
 * @author m.gond
 * 
 */
public enum MDEOExportField {

  /** The identifier. */
  IDENTIFIER("identifier", MetacatalogField.ID),

  /** The Constant ANY. */
  // ANY("any", MetacatalogField.ANY),

  /** The Title . */
  TITLE("title", MetacatalogField.TITLE),

  /** The description. */
  DESCRIPTION("description", MetacatalogField.DESCRIPTION),

  /** The Project. */
  PROJECT("project", MetacatalogField.PROJECT),

  /** The product. */
  PRODUCT("product", MetacatalogField.PRODUCT),

  /** The platform, nom du satellite */
  PLATFORM("platform", MetacatalogField.PLATFORM),

  /** The instrument. Nom de l'instrument */
  INSTRUMENT("instrument", MetacatalogField.INSTRUMENT),

  /** startDate, Date de début d'acquisition au format ISO 8601, i.e. YYYY-MM-DDTHH:MM:SS */
  START_DATE("startDate", MetacatalogField.START_DATE, Date.class),

  /** completionDate, Date de fin d'acquisition au format ISO 8601, i.e. YYYY-MM-DDTHH:MM:SS */
  COMPLETIONDATE("completionDate", MetacatalogField.COMPLETION_DATE, Date.class),

  /** resolution, Résolution spatiale exprimée en mètres, */
  RESOLUTION("resolution", MetacatalogField.RESOLUTION),

  /** MODIFIED, Date de dernière modification de la métadonnée au format ISO 8601, i.e.YYYY-MM-DDTHH:MM:SS */
  MODIFIED("modified", MetacatalogField.MODIFICATION_DATE, Date.class),

  /**
   * Services, Structure de données qui permet de décrire les services de téléchargement et de visualisation pleine
   * résolution du produit.
   */
  // SERVICES("services", MetacatalogField.SERVICES),

  SERVICES_BROWSE_TITLE("services.browse.title", MetacatalogField.SERVICES_BROWSE_TITLE),

  SERVICES_BROWSE_LAYER_TYPE("services.browse.layer.type", MetacatalogField.SERVICES_BROWSE_LAYER_TYPE),

  SERVICES_BROWSE_LAYER_URL("services.browse.layer.url", MetacatalogField.SERVICES_BROWSE_LAYER_URL),

  SERVICES_BROWSE_LAYER_LAYERS("services.browse.layer.layers", MetacatalogField.SERVICES_BROWSE_LAYER_LAYERS),

  SERVICES_BROWSE_LAYER_VERSION("services.browse.layer.version", MetacatalogField.SERVICES_BROWSE_LAYER_VERSION),

  SERVICES_BROWSE_LAYER_BBOX("services.browse.layer.version.bbox", MetacatalogField.SERVICES_BROWSE_LAYER_BBOX),

  SERVICES_BROWSE_LAYER_SRS("services.browse.layer.version.srs", MetacatalogField.SERVICES_BROWSE_LAYER_SRS),

  SERVICES_DOWNLOAD_URL("services.download.url", MetacatalogField.ARCHIVE),

  SERVICES_DOWNLOAD_MIME_TYPE("services.download.mimeType", MetacatalogField.MIME_TYPE),

  SERVICES_METADATA_URL("services.metadata.url", MetacatalogField.SERVICES_METADATA_URL),

  /** QUICKLOOK, url vers le quicklook du produit */
  QUICKLOOK("quicklook", MetacatalogField.QUICKLOOK),

  /** THUMBNAIL, url vers l'imagette du produit */
  THUMBNAIL("thumbnail", MetacatalogField.THUMBNAIL),

  /** KEYWORDS, Liste de mots clés exprimés sous la forme d'un tableau de chaine de caractères, */
  KEYWORDS("keywords", MetacatalogField.KEYWORDS),

  /** GEOMETRY AS GEOJSON, */
  GEOMETRY_GEOJSON("geometry", MetacatalogField.CHARACTERISATION_SPATIAL_AXIS);

  /** SPECIFIC, The GEOMETRY WKT STRING. */
  // _GEOMETRY("_geometry", MetacatalogField._GEOMETRY),

  /** SPECIFIC, The UUID used . */
  // _UUID("_uuid", MetacatalogField._UUID);

  /** The field. */
  private final String field;

  /** The clazz. */
  @SuppressWarnings("rawtypes")
  private final Class clazz;
  /** The corresponding field in the metacatalog */
  private MetacatalogField metacatalogField;

  /**
   * Instantiates a new index field.
   * 
   * @param field
   *          the field
   * @param metacatalogField
   *          the metacatalogField
   * @param clazz
   *          the clazz
   */
  @SuppressWarnings("rawtypes")
  private MDEOExportField(String field, MetacatalogField metacatalogField, Class clazz) {
    this.field = field;
    this.metacatalogField = metacatalogField;
    this.clazz = clazz;
  }

  /**
   * Instantiates a new index field.
   * 
   * @param field
   *          the field
   */
  private MDEOExportField(String field, MetacatalogField metacatalogField) {
    this(field, metacatalogField, String.class);
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
  public static MDEOExportField getField(String fieldName) {
    MDEOExportField result = null;
    for (MDEOExportField field : values()) {
      if (field.getField().equals(fieldName)) {
        result = field;
        break;
      }
    }
    return result;
  }

  /**
   * Gets the field.
   * 
   * @param metacatalogField
   *          the inspire field
   * @return the field
   */
  public static MDEOExportField getField(MetacatalogField metacatalogField) {
    MDEOExportField result = null;
    for (MDEOExportField field : values()) {
      if (field.getMetacatalogField().equals(metacatalogField)) {
        result = field;
        break;
      }
    }
    return result;
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
   * Gets the metacatalogField value
   * 
   * @return the metacatalogField
   */
  public MetacatalogField getMetacatalogField() {
    return metacatalogField;
  }

}
