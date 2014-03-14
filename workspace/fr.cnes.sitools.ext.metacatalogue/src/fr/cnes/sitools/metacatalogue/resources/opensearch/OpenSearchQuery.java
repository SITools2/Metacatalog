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
package fr.cnes.sitools.metacatalogue.resources.opensearch;

public enum OpenSearchQuery {

  /** Search terms. */
  SEARCH_TERMS("searchTerms", "q"),

  /** count */
  COUNT("count", "limit", 100),

  /** startIndex */
  START_INDEX("startIndex", "start", 1),

  /** startPage */
  START_PAGE("startPage", "pw", 1),

  /** language */
  LANGUAGE("language", "language", "fre"),

  /** the geo box param */
  GEO_BOX("geo:box", "bbox"),

  /** the start time param */
  TIME_START("time:start", "startDate"),
  
  /** the start time param */
  MODIFIED("ptsc:modificationDate", "modified"),

  /** the end time param */
  TIME_END("time:end", "completionDate"),
  
  /** the end time param */
  FORMAT("format", "format", "json");

  /** The field. */
  private final String field;

  /** The default value. */
  private final Object defaultValue;

  /** The parameter name in the query */
  private String paramName;

  /**
   * Instantiates a new index field.
   * 
   * @param field
   *          the field
   * @param paramName
   *          the param name
   * @param defaultValue
   *          the defaultValue
   */
  private OpenSearchQuery(String field, String paramName, Object defaultValue) {
    this.field = field;
    this.defaultValue = defaultValue;
    this.paramName = paramName;
  }

  /**
   * Instantiates a new index field.
   * 
   * @param field
   *          the field
   * @param paramName
   *          the paramName
   */
  private OpenSearchQuery(String field, String paramName) {
    this(field, paramName, null);
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
  public Object getDefaultValue() {
    return defaultValue;
  }

  /**
   * Gets the paramName value
   * 
   * @return the paramName
   */
  public String getParamName() {
    return paramName;
  }

  /**
   * Gets the field.
   * 
   * @param paramName
   *          the field name
   * @return the field
   */
  public static OpenSearchQuery getFieldFromParamName(String paramName) {
    OpenSearchQuery result = null;
    for (OpenSearchQuery field : values()) {
      if (field.getParamName().equals(paramName)) {
        result = field;
        break;
      }
    }
    return result;
  }

}
