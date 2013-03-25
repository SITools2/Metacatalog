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
package fr.cnes.sitools.metacatalogue.dto;

public class MetacatalogApplicationDTO {

  private String description;

  private String contact;

  private String urlHarvester;

  private String urlCoreMetacatalogue;

  private String nameCoreMetacatalogue;

  private Boolean pendingOperation;

  private String pendingOperationMessage;

  /**
   * Gets the description value
   * 
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * Sets the value of description
   * 
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the contact value
   * 
   * @return the contact
   */
  public String getContact() {
    return contact;
  }

  /**
   * Sets the value of contact
   * 
   * @param contact
   *          the contact to set
   */
  public void setContact(String contact) {
    this.contact = contact;
  }

  /**
   * Gets the urlHarvester value
   * 
   * @return the urlHarvester
   */
  public String getUrlHarvester() {
    return urlHarvester;
  }

  /**
   * Sets the value of urlHarvester
   * 
   * @param urlHarvester
   *          the urlHarvester to set
   */
  public void setUrlHarvester(String urlHarvester) {
    this.urlHarvester = urlHarvester;
  }

  /**
   * Gets the urlCoreMetacatalogue value
   * 
   * @return the urlCoreMetacatalogue
   */
  public String getUrlCoreMetacatalogue() {
    return urlCoreMetacatalogue;
  }

  /**
   * Sets the value of urlCoreMetacatalogue
   * 
   * @param urlCoreMetacatalogue
   *          the urlCoreMetacatalogue to set
   */
  public void setUrlCoreMetacatalogue(String urlCoreMetacatalogue) {
    this.urlCoreMetacatalogue = urlCoreMetacatalogue;
  }

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
   * Gets the nameCoreMetacatalogue value
   * 
   * @return the nameCoreMetacatalogue
   */
  public String getNameCoreMetacatalogue() {
    return nameCoreMetacatalogue;
  }

  /**
   * Sets the value of nameCoreMetacatalogue
   * 
   * @param nameCoreMetacatalogue
   *          the nameCoreMetacatalogue to set
   */
  public void setNameCoreMetacatalogue(String nameCoreMetacatalogue) {
    this.nameCoreMetacatalogue = nameCoreMetacatalogue;
  }

  /**
   * Gets the pendingOperation value
   * 
   * @return the pendingOperation
   */
  public boolean isPendingOperation() {
    return pendingOperation;
  }

  /**
   * Sets the value of pendingOperation
   * 
   * @param pendingOperation
   *          the pendingOperation to set
   */
  public void setPendingOperation(boolean pendingOperation) {
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
