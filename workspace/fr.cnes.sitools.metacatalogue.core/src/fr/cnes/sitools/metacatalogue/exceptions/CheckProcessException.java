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
package fr.cnes.sitools.metacatalogue.exceptions;

/**
 * Exception that will be throw during the process initialisation, meaning that there was an error in the checking of
 * the process steps
 * 
 * @author m.gond
 * 
 */
public class CheckProcessException extends Exception {

  /** serialVersionUID */
  private static final long serialVersionUID = 1L;

  /**
   * Constructs a new CheckProcessException with null as its detail message.
   */
  public CheckProcessException() {
    super();
  }

  /**
   * Constructs a new CheckProcessException with the specified detail message. The cause is not initialized, and may
   * subsequently be initialized by a call to Throwable.initCause(java.lang.Throwable). Parameters:
   * 
   * @param message
   *          the detail message. The detail message is saved for later retrieval by the Throwable.getMessage() method.
   */
  public CheckProcessException(String message) {
    super(message);
  }

}
