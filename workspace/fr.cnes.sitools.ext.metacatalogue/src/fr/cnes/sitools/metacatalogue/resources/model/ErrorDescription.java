/*******************************************************************************
 * Copyright 2011, 2012 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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


/**
 * Error model class
 * 
 * @author m.gond
 */
public class ErrorDescription {
  /**
   * un code d’erreur défini par l’administrateur. La nomenclature est libre.
   * Cet élément est optionnel.
   */
  private String code;
  /**
   * un message d’erreur explicite rédigé en langue anglaise. Cet élément est
   * obligatoire.
   */
  private String message;

  /**
   * Gets the code value
   * 
   * @return the code
   */
  public String getCode() {
    return code;
  }

  /**
   * Sets the value of code
   * 
   * @param code
   *          the code to set
   */
  public void setCode(String code) {
    this.code = code;
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
