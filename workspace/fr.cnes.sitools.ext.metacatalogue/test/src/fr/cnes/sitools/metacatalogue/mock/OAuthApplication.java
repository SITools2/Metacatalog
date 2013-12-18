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
package fr.cnes.sitools.metacatalogue.mock;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.ChallengeAuthenticator;

import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.common.application.SitoolsApplication;
import fr.cnes.sitools.dataset.ActivationDataSetResource;
import fr.cnes.sitools.dataset.DataSetCollectionResource;
import fr.cnes.sitools.dataset.DataSetDictionaryMappingCollectionResource;
import fr.cnes.sitools.dataset.DataSetDictionaryMappingResource;
import fr.cnes.sitools.dataset.DataSetNotificationResource;
import fr.cnes.sitools.dataset.DataSetResource;
import fr.cnes.sitools.dataset.RefreshDataSetResource;
import fr.cnes.sitools.metacatalogue.security.OAuthVerifier;
import fr.cnes.sitools.metacatalogue.security.SitoolsChallengeScheme;
import fr.cnes.sitools.notification.business.NotifierFilter;
import fr.cnes.sitools.util.RIAPUtils;

public class OAuthApplication extends SitoolsApplication {

  
  
  public OAuthApplication(Context context) {
    super(context);
    ChallengeAuthenticator oauthAuthenticator = new ChallengeAuthenticator(context, true, SitoolsChallengeScheme.HTTP_BEARER, null, new OAuthVerifier(RIAPUtils.getRiapBase() + this.getAttachementRef() + "/dummyTokenValidation"));
    Authorizer authorizer = Authorizer.ALWAYS;
    context.getAttributes().put(ContextAttributes.CUSTOM_CHALLENGE_AUTHENTICATOR, oauthAuthenticator);
    context.getAttributes().put(ContextAttributes.CUSTOM_AUTHORIZER, authorizer);
  }

  @Override
  public void sitoolsDescribe() {


  }
  
  
  @Override
  public Restlet createInboundRoot() {

    Router router = new Router(getContext());

    router.attachDefault(OAuthResource.class);
    
    router.attach("/dummyTokenValidation", DummyTokenValidationResource.class);

    return router;

  }
  
  
  

}
