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
package fr.cnes.sitools.util;

/**
 * Commons attributes for SitoolsApplications
 * 
 * @author jp.boignard (AKKA Technologies)
 * 
 */
public interface HarvesterSettingsAttributes {

  /** ----------------------------------- */
  /** Context attributes for applications */

  /** Proxy host */
  String PROXY_HOST = "PROXY_HOST";

  /** Proxy port */
  String PROXY_PORT = "PROXY_PORT";

  /** Proxy user name */
  String PROXY_USER = "PROXY_USER";

  /** Proxy user password */
  String PROXY_PASSWORD = "PROXY_PASSWORD";

  /** Proxy host */
  String NONPROXY_HOSTS = "NONPROXY_HOSTS";
  
  /** The Host port number */
  String HOST_PORT = "HOST_PORT";
  
  /** The Host domain */
  String HOST_DOMAIN = "HOST_DOMAIN";
  
  /** The public host domain */
  String PUBLIC_HOST_DOMAIN = "PUBLIC_HOST_DOMAIN";
  
  /** The root directory */
  String ROOT_DIRECTORY = "ROOT_DIRECTORY";
  
  

}
