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
package fr.cnes.sitools.metacatalogue.resources.model;

import java.util.Arrays;
import java.util.List;

/**
 * An enumeration for SolrDataType
 * 
 * @author m.gond (AKKA Technologies) from Original DataType class of Jean-Christophe Malapert (CNES)
 * 
 */
public enum SolrDataType {
  /** Numeric */
  NUMBER(Arrays.asList("sinteger", "slong", "sfloat", "sdouble"), FilterType.number),
  /** Date field */
  DATE(Arrays.asList("date"), FilterType.date),
  /** Text field */
  TEXT(Arrays.asList("text", "text_ws", "string", "integer", "long", "float", "double", "boolean", "text_general"),
      FilterType.text);

  /** The list of types */
  private final List<String> solrTypes;

  /** The filterType associated */
  private final FilterType filterType;

  /**
   * Private constructor of datatypes
   * 
   * @param solrTypes
   *          the list of solr types
   * @param filterType
   *          the associated filterType
   */
  private SolrDataType(List<String> solrTypes, FilterType filterType) {
    this.solrTypes = solrTypes;
    this.filterType = filterType;
  }

  /**
   * Get all types for a given Type
   * 
   * @return all type for a given Type
   */
  public List<String> getSolrTypes() {
    return this.solrTypes;
  }

  /**
   * Register a new SolrDataType for a given type
   * 
   * @param solrDataTypeName
   *          the name of the SolrDataType
   * @param dataType
   *          the type to register
   */
  public static void registerNewSolrDataType(String solrDataTypeName, SolrDataType dataType) {
    dataType.getSolrTypes().add(solrDataTypeName);
  }

  /**
   * Get a {@link SolrDataType} from a solr data type name or null if not found
   * 
   * @param solrDataTypeName
   *          the name of the solr type to search
   * @return the {@link SolrDataType} associated to the the given solr data type
   */
  public static SolrDataType getDataTypeFromSolrDataTypeName(String solrDataTypeName) {
    for (SolrDataType solrDataType : SolrDataType.values()) {
      for (String solrIndex : solrDataType.getSolrTypes()) {
        if (solrIndex.equals(solrDataTypeName)) {
          return solrDataType;
        }
      }
    }
    return null;
  }

  /**
   * Gets the filterType value
   * 
   * @return the filterType
   */
  public FilterType getFilterType() {
    return filterType;
  }
}