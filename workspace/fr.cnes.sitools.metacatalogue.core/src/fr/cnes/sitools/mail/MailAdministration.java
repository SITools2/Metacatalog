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
package fr.cnes.sitools.mail;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;

import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.server.administration.HarvesterCollectionResource;

/**
 * Application for managing mails (sending / receiving)
 * 
 * Aims at : 
 * 0. Sending mail service according to sitools.properties
 * 
 * 1. Administrator must be able to configure the mail settings (protocol, server) with
 * the administration GUI, and enable/disable this service. SMTP/SMTPS : the key file SSL must be present on the server.
 * 
 * 2. Administrator must be able to configure and manage the mail income for an applicative 
 * mail account (POP/POPS) : the key file SSL must be present on the server.
 * 
 * @author jp.boignard (AKKA Technologies)
 * 
 * @see org.restlet.test.engine.JavaMailTestCase.java
 * 
 */
public final class MailAdministration extends Application {

  /** Store for keys */
  private String trustStore = "certificats/sitools.keystore";

  /** Component */
  private Component component = null;

  /** Client list concerning mail management */
  private Map<String, Client> mailClients = new ConcurrentHashMap<String, Client>();
  
  /** mail server */
  private String mailServer = null;
  
  /** mail admin */
  private String mailAdmin = null;
  
  /** admin identifier */
  private String adminIdentifier = null;
  
  /** admin secret */
  private String adminSecret = null;

  
  /**
   * Constructor
   * 
   * @param context
   *          RESTlet context
   * @param server
   *          the server component
   */
  public MailAdministration(Context context, Component server) {
    super(context);
    this.component = server;
    
    setMailServer(HarvesterSettings.getInstance().getString("mail.send.server"));
    setMailAdmin(HarvesterSettings.getInstance().getString("mail.send.admin"));
    setAdminIdentifier(HarvesterSettings.getInstance().getString("mail.send.identifier"));
    setAdminSecret(HarvesterSettings.getInstance().getString("mail.send.secret"));
    
    try {
      final File keyStoreFile = new File(trustStore);
      if (keyStoreFile.exists()) {
        System.setProperty("javax.net.ssl.trustStore", keyStoreFile.getCanonicalPath());
      }
    }
    catch (IOException e) {
      getLogger().warning("Setting javax.net.ssl.trustStore failed.");
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, null, e);
    }

     
    try {
      // Loading mail configuration for administrator account
      setupMailClient(Protocol.SMTP, HarvesterSettings.getInstance().getString("mail.send.server"),
          Boolean.parseBoolean(HarvesterSettings.getInstance().getString("mail.send.debug")),
          Boolean.parseBoolean(HarvesterSettings.getInstance().getString("mail.send.tls")));
    }
    catch (Exception e) {
      getLogger().warning("MailAdministration initialization failed. Check your mail settings in sitools.properties and restart server.");
      getLogger().log(Level.INFO, null, e);
    }
    
    // register client protocol according to the configuration
  }


  @Override
  public Restlet createInboundRoot() {

    Router router = new Router(getContext());

    // GET mails / POST mail
    //router.attachDefault(MailResource.class);
    router.attach("/send", MailResource.class);

    return router;
  }

  /**
   * Enregistrement d'un client pour le serveur / protocole mail en question avec / sans SSL
   * 
   * @param protocol
   *          protocol used
   * @param server
   *          the server used
   * @param debug
   *          debug mode on/off
   * @param startTls
   *          starts TLS
   * @throws Exception
   *           if setup fails
   */
  public void setupMailClient(Protocol protocol, String server, boolean debug, boolean startTls) throws Exception {
    Client client = mailClients.get(server);
    if (client != null) {
      mailClients.remove(server);
      client.stop();
      component.getClients().remove(client);
    }

    client = new Client(this.getContext(), protocol);
    client.getContext().getParameters().add("debug", Boolean.toString(debug));
    client.getContext().getParameters().add("startTls", Boolean.toString(startTls).toLowerCase());
    client.getContext().getParameters().add("representationMessageClass", SitoolsRepresentationMessage.class.getName());
    mailClients.put(server, client);
    component.getClients().add(client);
  }


  public String getMailServer() {
    return mailServer;
  }


  public void setMailServer(String mailServer) {
    this.mailServer = mailServer;
  }


  public String getMailAdmin() {
    return mailAdmin;
  }


  public void setMailAdmin(String mailAdmin) {
    this.mailAdmin = mailAdmin;
  }


  public String getAdminIdentifier() {
    return adminIdentifier;
  }


  public void setAdminIdentifier(String adminIdentifier) {
    this.adminIdentifier = adminIdentifier;
  }


  public String getAdminSecret() {
    return adminSecret;
  }


  public void setAdminSecret(String adminSecret) {
    this.adminSecret = adminSecret;
  }


}
