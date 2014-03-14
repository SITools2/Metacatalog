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
package fr.cnes.sitools.metacatalogue.mock;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.common.application.SitoolsApplication;
import fr.cnes.sitools.metacatalogue.security.OAuthVerifier;
import fr.cnes.sitools.metacatalogue.security.SitoolsChallengeScheme;

/**
 * The Class DummyOAuthApplication. Mock application to simulate an application where the authorizations are delegated
 * to a Oauth SSO server
 * 
 * @author m.gond, tx.chevallier
 */
public class DummyOAuthApplication extends SitoolsApplication {

  /**
   * Instantiates a new dummy oAuth application.
   * 
   * @param context
   *          the context
   * @param oauthValidationUrl
   *          the oauth validation url
   */
  public DummyOAuthApplication(Context context, String oauthValidationUrl) {
    super(context);
    Verifier verifier = new OAuthVerifier(oauthValidationUrl);

    ChallengeAuthenticator oauthAuthenticator = new ChallengeAuthenticator(context, true,
        SitoolsChallengeScheme.HTTP_BEARER, null, verifier);
    Authorizer authorizer = Authorizer.ALWAYS;
    context.getAttributes().put(ContextAttributes.CUSTOM_CHALLENGE_AUTHENTICATOR, oauthAuthenticator);
    context.getAttributes().put(ContextAttributes.CUSTOM_AUTHORIZER, authorizer);
  }

  @Override
  public void sitoolsDescribe() {
    setName("DummyOAuthApplication");
    setDescription("Mock application to simulate an application where the authorizations are delegated to a Oauth SSO server");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.restlet.Application#createInboundRoot()
   */
  @Override
  public Restlet createInboundRoot() {

    Router router = new Router(getContext());

    router.attachDefault(DummyOAuthResource.class);

    return router;

  }

}
