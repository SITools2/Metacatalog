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
package fr.cnes.sitools.metacatalogue.resources.proxyservices;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;

import fr.cnes.sitools.proxy.ProxySettings;

public class RedirectorProxyAuthenticated extends Redirector {

  private String user;

  private String password;

  /**
   * Constructor
   * 
   * @param context
   *          restlet context
   * @param targetPattern
   *          target pattern
   * @param mode
   *          mode
   */
  public RedirectorProxyAuthenticated(Context context, String targetPattern, int mode) {
    this(context, targetPattern, mode, null, null);

  }

  /**
   * Constructor
   * 
   * @param context
   *          restlet context
   * @param targetPattern
   *          target pattern
   * @param mode
   *          mode
   */
  public RedirectorProxyAuthenticated(Context context, String targetPattern, int mode, String user, String password) {
    super(context, targetPattern, mode);
    this.user = user;
    this.password = password;
  }

  /**
   * Constructor
   * 
   * @param context
   *          restlet context
   * @param targetTemplate
   *          targetTemplate
   */
  public RedirectorProxyAuthenticated(Context context, String targetTemplate, String user, String password) {
    super(context, targetTemplate);
    this.user = user;
    this.password = password;
  }

  @Override
  public void handle(Request request, Response response) {

    if ((ProxySettings.getProxyAuthentication() != null) && request.getProxyChallengeResponse() == null) {
      request.setProxyChallengeResponse(ProxySettings.getProxyAuthentication());
    }

    if (user != null && password != null) {
      ChallengeResponse chal = new ChallengeResponse(ChallengeScheme.HTTP_BASIC, user, password);
      request.setChallengeResponse(chal);
    }

    try {
      super.handle(request, response);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Redirects a given call on the server-side to a next Restlet with a given target reference. In the default
   * implementation, the request HTTP headers, stored in the request's attributes, are removed before dispatching. After
   * dispatching, the response HTTP headers are also removed to prevent conflicts with the main call.
   * 
   * @param next
   *          The next Restlet to forward the call to.
   * @param targetRef
   *          The target reference with URI variables resolved.
   * @param request
   *          The request to handle.
   * @param response
   *          The response to update.
   */
  @Override
  protected void serverRedirect(Restlet next, Reference targetRef, Request request, Response response) {
    if (next == null) {
      getLogger().warning("No next Restlet provided for server redirection to " + targetRef);
    }
    else {
      // Save the base URI if it exists as we might need it for
      // redirections
      Reference resourceRef = request.getResourceRef();
      Reference baseRef = resourceRef.getBaseRef();

      // Reset the protocol and let the dispatcher handle the protocol
      request.setProtocol(null);

      // Update the request to cleanly go to the target URI
      request.setResourceRef(targetRef);
      request.getAttributes().remove(HeaderConstants.ATTRIBUTE_HEADERS);
      next.handle(request, response);

      // SITOOLS-METACATALOG PATCH to send a forbidden status instead of unauthorized
      if (response.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
        response.setStatus(Status.CLIENT_ERROR_FORBIDDEN);
      }
      // END OF SITOOLS-METACATALOG PATCH

      // Allow for response rewriting and clean the headers
      response.setEntity(rewrite(response.getEntity()));
      response.getAttributes().remove(HeaderConstants.ATTRIBUTE_HEADERS);
      request.setResourceRef(resourceRef);

      // In case of redirection, we may have to rewrite the redirect URI
      if (response.getLocationRef() != null) {
        Template rt = new Template(this.targetTemplate);
        rt.setLogger(getLogger());
        int matched = rt.parse(response.getLocationRef().toString(), request);

        if (matched > 0) {
          String remainingPart = (String) request.getAttributes().get("rr");

          if (remainingPart != null) {
            response.setLocationRef(baseRef.toString() + remainingPart);
          }
        }
      }
    }
  }

}
