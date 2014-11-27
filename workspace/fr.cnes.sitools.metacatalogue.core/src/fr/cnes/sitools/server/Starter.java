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
package fr.cnes.sitools.server;

import java.io.File;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.routing.VirtualHost;

import fr.cnes.sitools.mail.MailAdministration;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.persistence.HarvesterModelStore;
import fr.cnes.sitools.persistence.HarvesterModelStoreXmlImpl;
import fr.cnes.sitools.proxy.ProxySettings;
import fr.cnes.sitools.util.HarvesterSettingsAttributes;

/**
 * Server Starting class.
 * 
 * @author AKKA Technologies
 */
public final class Starter {

  /** The Default port number */
  private static final int DEFAULT_PORT_NUMBER = 8182;

  /** Component server */
  private static Component server = null;

  /**
   * Private constructor for utility classes
   */
  private Starter() {
  }

  /**
   * Run the server as a stand-alone component.
   * 
   * @param args
   *          The optional arguments.
   */
  public static void main(String[] args) {
    try {
      HarvesterSettings settings = HarvesterSettings.getInstance();

      startWithProxy(settings);

    }
    catch (Exception e) {
      System.err.println("ERROR starting SITools2/Metacatalogue server.");
      System.err.println(e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Builds and starts the server component.
   * 
   * @param settings
   *          the settings
   * @throws Exception
   *           always possible
   */
  public static void startWithProxy(HarvesterSettings settings) throws Exception {
    // ===========================================================================
    // PROXY CONFIGURATION
    ProxySettings.init();

    // ===========================================================================
    // Builds and starts the server

    // TODO args : String hostname, int port, String publicHostName
    start(null, 0, null);
  }

  /**
   * Builds and starts the server component.
   * 
   * @param settings
   *          the settings
   * @throws Exception
   *           always possible
   */
  public static void start(String hostname, int port, String publicHostName) throws Exception {

    HarvesterSettings settings = HarvesterSettings.getInstance();

    HarvesterModelStore storeHarvesterModel = new HarvesterModelStoreXmlImpl(new File(
        settings.getStoreDIR("APP_HARVESTER_MODEL_STORE_DIR")));

    Component component = new Component();

    // =============================================================
    // Create a virtual host
    Context vhostContext = component.getContext().createChildContext();
    VirtualHost host = Starter.initVirtualHost(vhostContext, settings);

    String hostPort = (port != 0) ? String.valueOf(port) : settings.get(HarvesterSettingsAttributes.HOST_PORT)
        .toString();

    int portNumber;
    try {
      portNumber = Integer.parseInt(hostPort);
    }
    catch (Exception e) {
      portNumber = DEFAULT_PORT_NUMBER;
    }

    // racine de l'url publique (listings de repertoires)
    if (publicHostName != null) {
      settings.setPublicHostDomain(publicHostName);
    }

    component.getServers().add(Protocol.HTTP, portNumber);
    component.getClients().add(Protocol.FILE);
    component.getClients().add(Protocol.HTTP);

    Context appContext = component.getContext().createChildContext();
    appContext.getAttributes().put(HarvesterSettingsAttributes.SETTINGS, settings);
    HarvestersApplication harvesterAdministration = new HarvestersApplication(appContext, storeHarvesterModel);

    String url = settings.getString("HARVESTERS_APP_URL");
    host.attach(url, harvesterAdministration);
    component.getInternalRouter().attach(url, harvesterAdministration);
    
    
    /* add mail application */
    /* we do not use the sitools email to avoid dependency between servers */
    String mailUrl = settings.getString("MAIL_ADMIN_URL");
    MailAdministration mailAdministration = new MailAdministration(appContext, component);
    host.attach(mailUrl, mailAdministration);
    component.getInternalRouter().attach(settings.getString("MAIL_ADMIN_URL"), mailAdministration);


    component.getHosts().add(host);
    component.start();

    server = component;
  }

  /**
   * STOP server
   */
  public static void stop() {
    if (server != null) {
      try {
        server.stop();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    server = null;
  }

  /**
   * Initialize the virtual host
   * 
   * @param vhostContext
   *          the context with which to create the host
   * @param settings
   *          the SitoolsSettings object
   * @return VirtualHost the VirtualHost created
   */
  public static VirtualHost initVirtualHost(Context vhostContext, HarvesterSettings settings) {
    VirtualHost host = new VirtualHost(vhostContext);
    host.setName("SitoolsMetacatalogueVirtualHost");
    host.setHostDomain(settings.getString("HOST_DOMAIN"));
    return host;
  }
}
