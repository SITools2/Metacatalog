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
package fr.cnes.sitools.metacatalogue.model;

import java.util.List;

public class Localisation {

  /** The countries */
  private List<String> countries;
  /** The continents */
  private List<String> continents;
  /** The regions */
  private List<String> regions;
  /** The departments */
  private List<String> departments;
  /** The cities */
  private List<String> cities;

  /**
   * Gets the countries value
   * 
   * @return the countries
   */
  public List<String> getCountries() {
    return countries;
  }

  /**
   * Sets the value of countries
   * 
   * @param countries
   *          the countries to set
   */
  public void setCountries(List<String> countries) {
    this.countries = countries;
  }

  /**
   * Gets the continents value
   * 
   * @return the continents
   */
  public List<String> getContinents() {
    return continents;
  }

  /**
   * Sets the value of continents
   * 
   * @param continents
   *          the continents to set
   */
  public void setContinents(List<String> continents) {
    this.continents = continents;
  }

  /**
   * Gets the regions value
   * 
   * @return the regions
   */
  public List<String> getRegions() {
    return regions;
  }

  /**
   * Sets the value of regions
   * 
   * @param regions
   *          the regions to set
   */
  public void setRegions(List<String> regions) {
    this.regions = regions;
  }

  /**
   * Gets the departments value
   * 
   * @return the departments
   */
  public List<String> getDepartments() {
    return departments;
  }

  /**
   * Sets the value of departements
   * 
   * @param departments
   *          the departements to set
   */
  public void setDepartments(List<String> departments) {
    this.departments = departments;
  }

  /**
   * Gets the cities value
   * 
   * @return the cities
   */
  public List<String> getCities() {
    return cities;
  }

  /**
   * Sets the value of cities
   * 
   * @param cities
   *          the cities to set
   */
  public void setCities(List<String> cities) {
    this.cities = cities;
  }

}
