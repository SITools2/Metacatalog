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
package fr.cnes.sitools.metacatalogue.representation.dto;

public class Layer {
  private String type = null;

  private String url = null;

  private String layers = null;

  private String version = null;

  private String bbox = null;

  private String srs = null;

  /**
   * Gets the type value
   * 
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the value of type
   * 
   * @param type
   *          the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Gets the url value
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the value of url
   * 
   * @param url
   *          the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets the layers value
   * 
   * @return the layers
   */
  public String getLayers() {
    return layers;
  }

  /**
   * Sets the value of layers
   * 
   * @param layers
   *          the layers to set
   */
  public void setLayers(String layers) {
    this.layers = layers;
  }

  /**
   * Gets the version value
   * 
   * @return the version
   */
  public String getVersion() {
    return version;
  }

  /**
   * Sets the value of version
   * 
   * @param version
   *          the version to set
   */
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * Gets the bbox value
   * 
   * @return the bbox
   */
  public String getBbox() {
    return bbox;
  }

  /**
   * Sets the value of bbox
   * 
   * @param bbox
   *          the bbox to set
   */
  public void setBbox(String bbox) {
    this.bbox = bbox;
  }

  /**
   * Gets the srs value
   * 
   * @return the srs
   */
  public String getSrs() {
    return srs;
  }

  /**
   * Sets the value of srs
   * 
   * @param srs
   *          the srs to set
   */
  public void setSrs(String srs) {
    this.srs = srs;
  }

}
