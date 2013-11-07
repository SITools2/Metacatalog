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
package fr.cnes.sitools.server;

import org.restlet.data.Status;

/**
 * Simple class to store a status and a message for Application lock
 * 
 * @author m.gond
 * 
 */
public class InfoResource {
  /** The status */
  private Status status;
  /** The message */
  private String message;

  /**
   * Constructor with status and message
   * 
   * @param status
   *          the {@link Status}
   * @param message
   *          the message {@link String}
   */
  public InfoResource(Status status, String message) {
    this.status = status;
    this.message = message;
  }

  /**
   * Gets the status value
   * 
   * @return the status
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Sets the value of status
   * 
   * @param status
   *          the status to set
   */
  public void setStatus(Status status) {
    this.status = status;
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
