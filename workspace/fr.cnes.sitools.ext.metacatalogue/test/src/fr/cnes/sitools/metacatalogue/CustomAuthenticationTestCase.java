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
package fr.cnes.sitools.metacatalogue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Group;
import org.restlet.security.User;

import fr.cnes.sitools.AbstractSitoolsTestCase;
import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.common.store.SitoolsStore;
import fr.cnes.sitools.metacatalogue.mock.OAuthApplication;
import fr.cnes.sitools.metacatalogue.security.OAuthHelper;
import fr.cnes.sitools.registry.AppRegistryApplication;
import fr.cnes.sitools.registry.AppRegistryStoreXML;
import fr.cnes.sitools.registry.model.AppRegistry;
import fr.cnes.sitools.role.RoleStoreXML;
import fr.cnes.sitools.role.model.Role;
import fr.cnes.sitools.security.authentication.SitoolsRealm;
import fr.cnes.sitools.server.Consts;

/**
 * Test the authorization mecanism of Sitools
 * 
 * 
 * @author m.gond
 */
public class CustomAuthenticationTestCase extends AbstractSitoolsTestCase {

  /**
   * Restlet Component for server
   */
  private Component component = null;

  /**
   * relative url for dataset management REST API
   * 
   * @return url
   */
  protected String getAttachUrl() {
    return SITOOLS_URL + "/myapp";
  }

  /**
   * absolute url  
   * @return url
   */
  protected String getBaseUrl() {
    return super.getBaseUrl() + "/myapp";
  }
  
  private static SitoolsStore<AppRegistry> store = null;
  
  private static SitoolsStore<Role> roleStore = null;
  
  private AppRegistryApplication appManager;
  
  

  @Before
  @Override
  /**
   * Init and Start a server with GraphApplication
   * 
   * @throws java.lang.Exception
   */
  public void setUp() throws Exception {
    
    SitoolsSettings settings = SitoolsSettings.getInstance();
    
    if (this.component == null) {
      
      this.component = new Component();
      this.component.getServers().add(Protocol.HTTP, getTestPort());
      this.component.getClients().add(Protocol.HTTP);
      this.component.getClients().add(Protocol.FILE);
      this.component.getClients().add(Protocol.CLAP);
      this.component.getClients().add(Protocol.RIAP);
      
      Context appContext = this.component.getContext().createChildContext();
      appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
      
      if (store == null) {
        File storeDirectory = new File(getTestRepository());
        cleanDirectory(storeDirectory);
        store = new AppRegistryStoreXML(storeDirectory, appContext);
      }
      
      
      // Context
      String appReference = SITOOLS_URL + "/applications";
      appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
      appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, appReference);
      appContext.getAttributes().put(ContextAttributes.APP_STORE, store);

      appManager = new AppRegistryApplication(appContext);
      appManager.setHost(this.component.getDefaultHost());

      this.component.getDefaultHost().attach(appReference, appManager);
      
      // attach the application 
      
      appContext = this.component.getContext().createChildContext();
      appContext.getAttributes().put(ContextAttributes.CUSTOM_CHALLENGE_AUTHENTICATOR, new ChallengeAuthenticator(appContext, ChallengeScheme.HTTP_OAUTH, null));
      
      appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, getAttachUrl());
      
      appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
      
      OAuthApplication oauthApp = new OAuthApplication(appContext);
      
      appManager.attachApplication(oauthApp);
      
      component.getInternalRouter().attach(getAttachUrl(), oauthApp);
      
    }
    

    if (!this.component.isStarted()) {
      this.component.start();
    }
    
    
  }
  
  
  @Test
  public void testNoTokenProvided() throws IOException{
    
    ClientResource clientResource = new ClientResource(getBaseUrl());
    
    // case 1 : no token
    
    Representation rep = clientResource.get();
    
    assertTrue(clientResource.getStatus().isSuccess());
    
    assertEquals("KO", rep.getText());
    
  }
  
  @Test
  public void testTokenProvided() throws IOException {

    // case 2 : token ok
    
    String token = "text";
    
    Client client = new Client(this.component.getContext(), Protocol.HTTP);
    
    Request req  = new Request(Method.GET, getBaseUrl());
      
    ChallengeScheme scheme = new ChallengeScheme("HTTP_Bearer", "Bearer");
    
    OAuthHelper helper = new OAuthHelper(scheme, true, true);
    Engine.getInstance().getRegisteredAuthenticators().add(helper); 
    
    ChallengeResponse challengeResponse = new ChallengeResponse(scheme);
        challengeResponse.setRawValue(token);
        
        req.setChallengeResponse(challengeResponse);
        
    Response resp = client.handle(req);

    Representation representation = resp.getEntity();
    
    assertTrue(resp.getStatus().isSuccess());

    assertEquals("OK", representation.getText());

    

  }
  
  
  
  protected String getTestRepository() {
    return super.getTestRepository() + "/applications";
  }
  

  @After
  @Override
  /**
   * Stop server
   * @throws java.lang.Exception
   */
  public void tearDown() throws Exception {
    super.tearDown();
    this.component.stop();
    this.component = null;
  }
  
}
