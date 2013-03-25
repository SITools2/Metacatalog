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
package fr.cnes.sitools.metacatalogue.representation.dto;

public class Services {

  private Browse browse;

  private Download download;

  private Metadata metadata;

  /**
   * 
   */
  public Services() {
    super();
    browse = new Browse();
    download = new Download();
    metadata = new Metadata();
  }

  /**
   * Gets the browse value
   * 
   * @return the browse
   */
  public Browse getBrowse() {
    return browse;
  }

  /**
   * Sets the value of browse
   * 
   * @param browse
   *          the browse to set
   */
  public void setBrowse(Browse browse) {
    this.browse = browse;
  }

  /**
   * Gets the download value
   * 
   * @return the download
   */
  public Download getDownload() {
    return download;
  }

  /**
   * Sets the value of download
   * 
   * @param download
   *          the download to set
   */
  public void setDownload(Download download) {
    this.download = download;
  }

  /**
   * Gets the metadata value
   * 
   * @return the metadata
   */
  public Metadata getMetadata() {
    return metadata;
  }

  /**
   * Sets the value of metadata
   * 
   * @param metadata
   *          the metadata to set
   */
  public void setMetadata(Metadata metadata) {
    this.metadata = metadata;
  }

}
