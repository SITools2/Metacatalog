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
package fr.cnes.sitools.model;

/**
 * A property model class
 * 
 * @author m.gond
 * 
 */
public class Property {
  /** The key of the property */
  private String key;
  /** The value of the property */
  private String value;

  /**
   * The default property constructor
   */
  public Property() {
    super();
  }

  /**
   * The property constuctor with key and value parameter
   * 
   * @param key
   *          the key of the property
   * @param value
   *          the value of the property
   */
  public Property(String key, String value) {
    super();
    this.key = key;
    this.value = value;
  }

  /**
   * Gets the key value
   * 
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the value of key
   * 
   * @param key
   *          the key to set
   */
  public void setKey(String key) {
    this.key = key;
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

}
