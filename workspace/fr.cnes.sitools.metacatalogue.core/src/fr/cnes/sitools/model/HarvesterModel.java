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
package fr.cnes.sitools.model;

import java.util.Date;
import java.util.List;

import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.persistence.Persistent;

/**
 * Harvester Model object
 * 
 * @author m.gond
 * 
 */
public class HarvesterModel implements Persistent {

  /** serialVersionUID */
  private static final long serialVersionUID = 1L;
  /** The harvester Id, also its name */
  private String id;
  /** The type of catalog */
  private String catalogType;
  /** The source to harvest */
  private HarvesterSource source;
  /** The harvester description */
  private String description;
  /** The className of the harvester */
  private String harvesterClassName;
  /** The list of properties */
  private List<Property> properties;
  /** The configuration of the indexer engine */
  private IndexerModel indexerConf;
  /** The date of last harvesting */
  private Date lastHarvest;
  /** The list of attributes to add */
  private List<AttributeCustom> attributes;
  /** true to merge automatically after an harvest, false otherwise */
  private boolean automaticMerge;
  /** true if the services are public, false otherwise */
  private boolean publicServices;
  /** a string to represent the harvester status */
  private String status;
  /** The status of the last harvest run */
  private HarvestStatus lastRunResult;

  /**
   * Gets the id value
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of id
   * 
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the name value
   * 
   * @return the name
   */
  public String getCatalogType() {
    return catalogType;
  }

  /**
   * Sets the value of name
   * 
   * @param catalogType
   *          the catalogType to set
   */
  public void setCatalogType(String catalogType) {
    this.catalogType = catalogType;
  }

  /**
   * Gets the source value
   * 
   * @return the source
   */
  public HarvesterSource getSource() {
    return source;
  }

  /**
   * Sets the value of source
   * 
   * @param source
   *          the source to set
   */
  public void setSource(HarvesterSource source) {
    this.source = source;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return description;
  }

  /**
   * @param description
   *          the description to set
   */
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the harvesterClassName value
   * 
   * @return the harvesterClassName
   */
  public String getHarvesterClassName() {
    return harvesterClassName;
  }

  /**
   * Sets the value of harvesterClassName
   * 
   * @param harvesterClassName
   *          the harvesterClassName to set
   */
  public void setHarvesterClassName(String harvesterClassName) {
    this.harvesterClassName = harvesterClassName;
  }

  /**
   * Gets the properties value
   * 
   * @return the properties
   */
  public List<Property> getProperties() {
    return properties;
  }

  /**
   * Sets the value of properties
   * 
   * @param properties
   *          the properties to set
   */
  public void setProperties(List<Property> properties) {
    this.properties = properties;
  }

  /**
   * Gets the indexerConf value
   * 
   * @return the indexerConf
   */
  public IndexerModel getIndexerConf() {
    return indexerConf;
  }

  /**
   * Sets the value of indexerConf
   * 
   * @param indexerConf
   *          the indexerConf to set
   */
  public void setIndexerConf(IndexerModel indexerConf) {
    this.indexerConf = indexerConf;
  }

  /**
   * Gets the lastHarvest value
   * 
   * @return the lastHarvest
   */
  public Date getLastHarvest() {
    return lastHarvest;
  }

  /**
   * Sets the value of lastHarvest
   * 
   * @param lastHarvest
   *          the lastHarvest to set
   */
  public void setLastHarvest(Date lastHarvest) {
    this.lastHarvest = lastHarvest;
  }

  /**
   * Gets the attributes value
   * 
   * @return the attributes
   */
  public List<AttributeCustom> getAttributes() {
    return attributes;
  }

  /**
   * Sets the value of attributes
   * 
   * @param attributes
   *          the attributes to set
   */
  public void setAttributes(List<AttributeCustom> attributes) {
    this.attributes = attributes;
  }

  /**
   * Gets the automaticMerge value
   * 
   * @return the automaticMerge
   */
  public boolean isAutomaticMerge() {
    return automaticMerge;
  }

  /**
   * Sets the value of automaticMerge
   * 
   * @param automaticMerge
   *          the automaticMerge to set
   */
  public void setAutomaticMerge(boolean automaticMerge) {
    this.automaticMerge = automaticMerge;
  }

  /**
   * Gets the publicServices value
   * 
   * @return the publicServices
   */
  public boolean isPublicServices() {
    return publicServices;
  }

  /**
   * Sets the value of publicServices
   * 
   * @param publicServices
   *          the publicServices to set
   */
  public void setPublicServices(boolean publicServices) {
    this.publicServices = publicServices;
  }

  /**
   * Gets the status value
   * 
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the value of status
   * 
   * @param status
   *          the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Gets the lastRunResult value
   * 
   * @return the lastRunResult
   */
  public HarvestStatus getLastRunResult() {
    return lastRunResult;
  }

  /**
   * Sets the value of lastRunResult
   * 
   * @param lastRunResult
   *          the lastRunResult to set
   */
  public void setLastRunResult(HarvestStatus lastRunResult) {
    this.lastRunResult = lastRunResult;
  }

}
