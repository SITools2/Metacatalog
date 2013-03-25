package fr.cnes.sitools.metacatalogue.utils.old;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.lang.ObjectUtils;
import org.apache.solr.common.util.DateUtil;

/**
 * Names of fields in the index.
 */
public enum InspireIndexField {

  /** The title of the Resource. */
  RESOURCE_TITLE("Resource title"),

  /** The Constant ANY. */
  ANY("any"),

  /** The ABSTRACT . */
  RESOURCE_ABSTRACT("Resource abstract"),

  /** The Constant RESOURCE_TYPE. */
  RESOURCE_TYPE("Resource Type"),

  /** The RESOURCE_LOCATOR. */
  RESOURCE_LOCATOR("Resource locator"),

  /** SPEC_INSPIRE, The UNIQUE_RESOURCE_IDENTIFIER. */
  UNIQUE_RESOURCE_IDENTIFIER("Unique resource identifier"),

  /** SPEC_CNES, The RESOURCE_UNIQUE_IDENTIFIER. */
  RESOURCE_UNIQUE_IDENTIFIER("Resource unique identifier"),

  /** The UNIQUE_RESOURCE_IDENTIFIER. */
  RESOURCE_LANGUAGE("Resource language"),

  /** MANDATORY, The TOPIC_CATEGORY, */
  TOPIC_CATEGORY("Topic category"),

  /** MANDATORY, The KEYWORD_VALUE, */
  KEYWORD_VALUE("Keyword value"),

  /** MANDATORY, The GEOGRAPHIC_BOUNDING_BOX, */
  GEOGRAPHIC_BOUNDING_BOX("Geographic bounding box"),

  /** Date de l'insertion des données dans le métacatalogue, */
  DATE_DE_PUBLICATION("Date de publication", true, Date.class),

  /** CONDITIONAL, The TEMPORAL_EXTENT, */
  TEMPORAL_EXTENT("Temporal extent", true, Date.class),

  /** CONDITIONAL, The DATE_OF_PUBLICATION, */
  DATE_OF_PUBLICATION("Date of publication", true, Date.class),

  /** CONDITIONAL, The DATE_OF_PUBLICATION, */
  DATE_OF_LAST_REVISION("Date of last revision", true, Date.class),

  /** CONDITIONAL, The DATE_OF_CREATION, */
  DATE_OF_CREATION("Date of creation", true, Date.class),

  /** MANDATORY, The LINEAGE, */
  LINEAGE("Lineage"),

  /** MANDATORY, The SPATIAL_RESOLUTION, */
  SPATIAL_RESOLUTION("Spatial resolution"),

  /** MANDATORY, The DEGREE, */
  DEGREE("Degree"),

  /** MANDATORY, The SPECIFICATION_TITLE, */
  SPECIFICATION_TITLE("Specification_title"),

  /** MANDATORY, The SPECIFICATION_PUBLICATION_DATE, */
  SPECIFICATION_PUBLICATION_DATE("Specification_publication_date", true, Date.class),

  /** MANDATORY, The LIMITATIONS_ON_PUBLIC_ACCESS, */
  LIMITATIONS_ON_PUBLIC_ACCESS("Limitations on public access"),

  /** MANDATORY, The CONDITION_APPLYING_TO_ACCESS_AND_USE, */
  CONDITION_APPLYING_TO_ACCESS_AND_USE("Condition applying to access and use"),

  /** MANDATORY, The RESPONSIBLE_PARTY, */
  RESPONSIBLE_PARTY("Responsible party"),

  /** MANDATORY, The RESPONSIBLE_PARTY_ROLE, */
  RESPONSIBLE_PARTY_ROLE("Responsible party role"),

  /** MANDATORY, The METADATA_POINT_OF_CONTACT, */
  METADATA_POINT_OF_CONTACT("Metadata point of contact"),

  /** MANDATORY, The METADATA_DATE, */
  METADATA_DATE("Metadata date", true, Date.class),

  /** MANDATORY, The METADATA LANGUAGE, */
  METADATA_LANGUAGE("Metadata language"),

  /** SPEC_CNES, The ORGANISATION, */
  ORGANISATION("Organisation"),

  /** SPEC_CNES, The EMAIL, */
  EMAIL("Email"),

  /** SPEC_CNES, The ROLE, */
  ROLE("role"),

  /** SPECIFIC, The RESOURCE_UNIQUE_IDENTIFIER. */
  UUID("uuid"),
 
  /** SPECIFIC, The GEOMETRY WKT STRING. */
  GEOMETRY("_geometry");

  /** The field. */
  private final String field;

  /** The range. */
  private final boolean range;

  /** The clazz. */
  @SuppressWarnings("rawtypes")
  private final Class clazz;

  /**
   * Instantiates a new index field.
   * 
   * @param field
   *          the field
   * @param range
   *          the range
   * @param clazz
   *          the clazz
   */
  @SuppressWarnings("rawtypes")
  private InspireIndexField(String field, boolean range, Class clazz) {
    this.field = field;
    this.range = range;
    this.clazz = clazz;
  }

  /**
   * Instantiates a new index field.
   * 
   * @param field
   *          the field
   */
  private InspireIndexField(String field) {
    this.field = field;
    range = false;
    clazz = String.class;
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
  public static InspireIndexField getField(String fieldName) {
    InspireIndexField result = null;
    for (InspireIndexField field : values()) {
      if (field.getField().equals(fieldName)) {
        result = field;
        break;
      }
    }
    return result;
  }

  /**
   * Checks if is range.
   * 
   * @return true, if is range
   */
  public boolean isRange() {
    return range;
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

}