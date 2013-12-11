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

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Util class to get the settings of the application
 * 
 * @author m.gond
 * 
 */
public final class HarvesterSettings {
  /** The instance */
  private static HarvesterSettings instance = null;
  /** The bundle name */
  private static final String BUNDLE_NAME = "metacatalogue";
  /** Application logger */
  private Logger log;

  /** The resource bundler */
  private ResourceBundle resourceBundle;

  private String storeDIR;

  private String publicHostDomain;

  /** Private constuctor */
  private HarvesterSettings() {
  }

  /**
   * Init method
   */
  private void init() {
    resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
    log = Logger.getLogger(BUNDLE_NAME);
    setStoreDIR(getString("STORE_DIR"));
    setPublicHostDomain(this.getString("PUBLIC_HOST_DOMAIN"));
  }

  /**
   * Get instance method
   * 
   * @return the instance of {@link HarvesterSettings}
   */
  public static HarvesterSettings getInstance() {
    if (instance == null) {
      instance = new HarvesterSettings();
      instance.init();
    }
    return instance;
  }

  /**
   * Get the parameter with the given name
   * 
   * @param name
   *          the name of the parameter
   * @return the parameter value, or null if not found
   */
  public Object get(String name) {
    try {
      return this.resourceBundle.getObject(name);
    } catch(MissingResourceException mre) {
      return null;
    }
  }

  /**
   * Get the parameter with the given name
   * 
   * @param name
   *          the name of the parameter
   * @return the parameter value, or null if not found
   */
  public String getString(String name) {
    try {
      return this.resourceBundle.getString(name);
    } catch(MissingResourceException mre) {
      return null;
    }
  }

  /**
   * Get the {@link ResourceBundle}
   * 
   * @return the {@link ResourceBundle}
   */
  public ResourceBundle getBundle() {
    return this.resourceBundle;
  }

  /**
   * Gets the log value
   * 
   * @return the log
   */
  public Logger getLogger() {
    return log;
  }

  public String getResourcePath(String schemaName, String resourceName) {
    String resourcesDir = getString("RESOURCES_DIRECTORY");
    return getRootDirectory() + resourcesDir + "/" + schemaName + "/" + resourceName;
  }

  /**
   * Gets Store Directory for a specific ID
   * 
   * @param storeID
   *          property key
   * @return String
   */
  public String getStoreDIR(String storeID) {
    return getRootDirectory() + storeDIR + getString(storeID);
  }

  /**
   * Get the directory of the stores
   * 
   * @return the directory of the stores
   */
  public String getStoreDIR() {
    return storeDIR;
  }

  /**
   * Get the directory of the stores
   * 
   * @param storeDIRParameter
   *          the store dir
   * 
   */
  public void setStoreDIR(String storeDIRParameter) {
    storeDIR = storeDIRParameter;
  }

  /**
   * Gets the rootDirectory value
   * 
   * @return the rootDirectory
   */
  public String getRootDirectory() {
    return getString("ROOT_DIRECTORY");
  }

  /**
   * Gets the publicHostDomain value
   * 
   * @return the publicHostDomain
   */
  public String getPublicHostDomain() {
    return publicHostDomain;
  }

  /**
   * Sets the value of publicHostDomain
   * 
   * @param publicHostDomain
   *          the publicHostDomain to set
   */
  public void setPublicHostDomain(String publicHostDomain) {
    this.publicHostDomain = publicHostDomain;
  }

}
