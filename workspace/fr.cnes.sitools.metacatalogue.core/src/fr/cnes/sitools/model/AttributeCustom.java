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
package fr.cnes.sitools.model;

import java.util.List;


/**
 * Model to store custom attributes to add to the structure
 * 
 * @author m.gond
 * 
 */
public class AttributeCustom {
  /** The name of the attribute */
  private String name;
  /** The xpath or the jsonPath of the attribute */
  private String path;
  /** The static value */
  private String value;
  /** The converter class */
  private String converterClass;
  /** The list of parameters for the converter */
  private List<Property> converterParameters;

  /**
   * Gets the name value
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of name
   * 
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the path value
   * 
   * @return the path
   */
  public String getPath() {
    return path;
  }

  /**
   * Sets the value of path
   * 
   * @param path
   *          the path to set
   */
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Gets the value value
   * 
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of value
   * 
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

  /**
   * Gets the converterClass value
   * 
   * @return the converterClass
   */
  public String getConverterClass() {
    return converterClass;
  }

  /**
   * Sets the value of converterClass
   * 
   * @param converterClass
   *          the converterClass to set
   */
  public void setConverterClass(String converterClass) {
    this.converterClass = converterClass;
  }

  /**
   * Gets the converterParameters value
   * 
   * @return the converterParameters
   */
  public List<Property> getConverterParameters() {
    return converterParameters;
  }

  /**
   * Sets the value of converterParameters
   * 
   * @param converterParameters
   *          the converterParameters to set
   */
  public void setConverterParameters(List<Property> converterParameters) {
    this.converterParameters = converterParameters;
  }

}
