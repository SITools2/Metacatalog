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
package fr.cnes.sitools.thesaurus;

import java.util.HashMap;
import java.util.Map;

public class Concept {

  private Map<String, Object> properties;

  public Concept() {
    super();
    properties = new HashMap<String, Object>();
  }

  /**
   * Gets the properties value
   * 
   * @return the properties
   */
  public Map<String, Object> getProperties() {
    return properties;
  }

  /**
   * Sets the value of properties
   * 
   * @param properties
   *          the properties to set
   */
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Concept [properties=" + properties + "]";
  }

}
