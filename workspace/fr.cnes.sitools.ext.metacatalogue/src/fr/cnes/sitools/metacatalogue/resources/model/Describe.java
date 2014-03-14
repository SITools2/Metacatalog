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
package fr.cnes.sitools.metacatalogue.resources.model;

import java.util.List;

/**
 * Model to store description of specific search parameters on a JeoBrowser
 * search service
 * 
 * 
 * @author m.gond (AKKA Technologies)
 */
public class Describe {
  /** List of filters in the description */
  private List<Filter> filters;
  /** List of errors */
  private ErrorDescription error;

  /**
   * Sets the value of filters
   * 
   * @param filters
   *          the filters to set
   */
  public void setFilters(List<Filter> filters) {
    this.filters = filters;
  }

  /**
   * Gets the filters value
   * 
   * @return the filters
   */
  public List<Filter> getFilters() {
    return filters;
  }

  /**
   * Sets the value of error
   * 
   * @param error
   *          the error to set
   */
  public void setError(ErrorDescription error) {
    this.error = error;
  }

  /**
   * Gets the error value
   * 
   * @return the error
   */
  public ErrorDescription getError() {
    return error;
  }
}
