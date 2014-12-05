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
package fr.cnes.sitools.mail;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Message;
import javax.mail.Session;


import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.thoughtworks.xstream.XStream;

import fr.cnes.sitools.common.XStreamFactory;
import fr.cnes.sitools.mail.model.Mail;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.proxy.ProxySettings;
import fr.cnes.sitools.util.HarvesterSettingsAttributes;
import fr.cnes.sitools.util.Util;

public class MailResource extends ServerResource {
  
  /** parent application */
  private MailAdministration application = null;

  @Override
  public void doInit() {
    super.doInit();
    application = (MailAdministration) getApplication();
  }
  
  
  /**
   * Envoi Mail
   * 
   * @param representation
   *          representation XML / JSON of a Mail object
   * @param variant
   *          XML / JSON for response
   * @return a representation of the mail sent
   */
  @Post
  public Representation sendMail(Representation representation, Variant variant) {

    HarvesterSettings settings = (HarvesterSettings)getContext().getAttributes().get(HarvesterSettingsAttributes.SETTINGS);

    fr.cnes.sitools.common.Response sendMailResponse = null;
    
    String mailServer = application.getMailServer();
    String mailAdmin = application.getMailAdmin();
    String adminIdentifier = application.getAdminIdentifier();
    String adminSecret = application.getAdminSecret();
    
    try {
    
      getApplication().getLogger().info("sending mail from default email address");
      
      Mail input = getObject(representation, variant);
      
//      Mail input = new Mail();
//      String[] toList = new String[] {"thierry.chevallier@akka.eu"};
//      input.setFrom(mailAdmin);
//      input.setToList(Arrays.asList(toList));
//      input.setSubject("subject");
//      input.setBody("test mail metacatalogue");

      Representation rep = new ObjectRepresentation<Mail>(input);
      
      Request request = new Request(Method.POST, mailServer, rep);

      if (ProxySettings.isWithProxy()) {
        ChallengeResponse challenge = ProxySettings.getProxyAuthentication();
        request.setProxyChallengeResponse(challenge);
      }

      if (Util.isNotEmpty(adminIdentifier)) {
        request.setChallengeResponse(new ChallengeResponse(ChallengeScheme.SMTP_PLAIN, adminIdentifier, adminSecret));
      }

      /*TODO send mail */
       Restlet dispatcher = getContext().getClientDispatcher();
       
       
      
       Response response = dispatcher.handle(request);

      if (response.getStatus().isSuccess()) {
        sendMailResponse = new fr.cnes.sitools.common.Response(true, "mail.send.success");
      }
      else {
        sendMailResponse = new fr.cnes.sitools.common.Response(false, "mail.send.failed");
      }
      
      
    }
    catch (Exception e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "mail.sendmail.error : ", e);
    }
    
    
    
    
    
    return null;
    
  }
  
  
  
  /**
   * Get the mail from representation
   * 
   * @param representation
   *          Request entity
   * @param variant
   *          the variant used
   * @return a mail corresponding to the representation
   * @throws IOException 
   */
  public Mail getObject(Representation representation, Variant variant) throws IOException {
    
    Mail input = null;
    
    if (MediaType.APPLICATION_XML.isCompatible(representation.getMediaType())) {
      // Parse the XML representation to get the input bean
      XstreamRepresentation<Mail> repXML = new XstreamRepresentation<Mail>(representation);
      XStream xstream = XStreamFactory.getInstance().getXStreamReader(MediaType.APPLICATION_XML);
      xstream.autodetectAnnotations(false);
      xstream.alias("mail", Mail.class);
      repXML.setXstream(xstream);
      input = repXML.getObject();
    }
    else if (MediaType.APPLICATION_JSON.isCompatible(representation.getMediaType())) {
      // Parse the JSON representation to get the mail bean
      input = new JacksonRepresentation<Mail>(representation, Mail.class).getObject();
    }
    else if (representation instanceof ObjectRepresentation<?>) {
      try {
        Object object = ((ObjectRepresentation<?>) representation).getObject();
        if (object instanceof Mail) {
          input = (Mail) object;
        }
      }
      catch (IOException e) {
        getLogger().log(Level.INFO, null, e);
      }
      if (input == null) {
        throw new RuntimeException("Only Mail object accepted for MailResource.POST( ObjectRepresentation)");
      }
    }
    return input;
  }


}
