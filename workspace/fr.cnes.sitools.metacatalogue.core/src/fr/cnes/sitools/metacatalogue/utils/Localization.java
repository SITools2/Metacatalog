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
package fr.cnes.sitools.metacatalogue.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class Localization.
 */
public class Localization {

  /**
   * Enum for localization type.
   * 
   * @author m.gond
   */
  public enum Type {

    /** The continent. */
    CONTINENT,
    /** The country. */
    COUNTRY,
    /** The region. */
    REGION,
    /** The department. */
    DEPARTMENT,
    /** The city. */
    CITY
  }

  /** The name. */
  private String name;

  /** The type. */
  private Type type;

  /** The percentage covered by the localization. */
  private double pcover;

  private List<Localization> children;

  public Localization(String name, Type type) {
    super();
    this.name = name;
    this.type = type;
    children = new ArrayList<Localization>();
  }

  public Localization(String name, Type type, double pcover) {
    this(name, type);
    this.pcover = pcover;

  }

  /**
   * Gets the name value.
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of name.
   * 
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the type value.
   * 
   * @return the type
   */
  public Type getType() {
    return type;
  }

  /**
   * Sets the value of type.
   * 
   * @param type
   *          the type to set
   */
  public void setType(Type type) {
    this.type = type;
  }

  /**
   * Gets the pcover value.
   * 
   * @return the pcover
   */
  public double getPcover() {
    return pcover;
  }

  /**
   * Sets the value of pcover.
   * 
   * @param pcover
   *          the pcover to set
   */
  public void setPcover(double pcover) {
    this.pcover = pcover;
  }

  /**
   * Gets the children value
   * 
   * @return the children
   */
  public List<Localization> getChildren() {
    return children;
  }

  /**
   * Sets the value of children
   * 
   * @param children
   *          the children to set
   */
  public void setChildren(List<Localization> children) {
    this.children = children;
  }

}
