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
package fr.cnes.sitools.metacatalogue.common;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Element;

import fr.cnes.sitools.metacatalogue.model.MetadataRecords;

/**
 * Metadata object, containing the XML and Java object of multiple metadata
 * 
 * @author m.gond
 * 
 */
public class MetadataContainer {

  /** The XML data of the metadatas */
  private Element xmlData;

  /** The JSON data of the metadatas */
  private String jsonData;

  /** The list of {@link MetadataRecords} containing the value of the metadatas */
  private List<MetadataRecords> metadataRecords;

  /**
   * Default constructor, initialise a new {@link List} of {@link MetadataRecords}
   */
  public MetadataContainer() {
    super();
    metadataRecords = new ArrayList<MetadataRecords>();
  }

  /**
   * Gets the xmlData value
   * 
   * @return the xmlData
   */
  public Element getXmlData() {
    return xmlData;
  }

  /**
   * Sets the value of xmlData
   * 
   * @param xmlData
   *          the xmlData to set
   */
  public void setXmlData(Element xmlData) {
    this.xmlData = xmlData;
  }

  /**
   * Gets the MetadataRecords value
   * 
   * @return the fields
   */
  public List<MetadataRecords> getMetadataRecords() {
    return metadataRecords;
  }

  /**
   * Sets the value of fields
   * 
   * @param metadataRecords
   *          the fields to set
   */
  public void setMetadataRecords(List<MetadataRecords> metadataRecords) {
    this.metadataRecords = metadataRecords;
  }

  public String getJsonData() {
    return jsonData;
  }

  public void setJsonData(String jsonData) {
    this.jsonData = jsonData;
  }

}
