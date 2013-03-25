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
package fr.cnes.sitools.server.dto;

public class MetacatalogStatusDTO {

  private Boolean pendingOperation;

  private String pendingOperationMessage;

  /**
   * Gets the pendingOperation value
   * 
   * @return the pendingOperation
   */
  public Boolean getPendingOperation() {
    return pendingOperation;
  }

  /**
   * Sets the value of pendingOperation
   * 
   * @param pendingOperation
   *          the pendingOperation to set
   */
  public void setPendingOperation(Boolean pendingOperation) {
    this.pendingOperation = pendingOperation;
  }

  /**
   * Gets the pendingOperationMessage value
   * 
   * @return the pendingOperationMessage
   */
  public String getPendingOperationMessage() {
    return pendingOperationMessage;
  }

  /**
   * Sets the value of pendingOperationMessage
   * 
   * @param pendingOperationMessage
   *          the pendingOperationMessage to set
   */
  public void setPendingOperationMessage(String pendingOperationMessage) {
    this.pendingOperationMessage = pendingOperationMessage;
  }

}
