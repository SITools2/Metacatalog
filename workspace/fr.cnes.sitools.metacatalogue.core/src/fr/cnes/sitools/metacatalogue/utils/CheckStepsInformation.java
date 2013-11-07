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

public class CheckStepsInformation {

  private boolean ok;

  private String message;

  public CheckStepsInformation() {
    super();
  }
  
  public CheckStepsInformation(boolean ok) {
    super();
    this.ok = ok;
  }

  public CheckStepsInformation(boolean ok, String message) {
    super();
    this.ok = ok;
    this.message = message;
  }

  /**
   * Gets the ok value
   * 
   * @return the ok
   */
  public boolean isOk() {
    return ok;
  }

  /**
   * Sets the value of ok
   * 
   * @param ok
   *          the ok to set
   */
  public void setOk(boolean ok) {
    this.ok = ok;
  }

  /**
   * Gets the message value
   * 
   * @return the message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Sets the value of message
   * 
   * @param message
   *          the message to set
   */
  public void setMessage(String message) {
    this.message = message;
  }

}
