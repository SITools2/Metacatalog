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

import fr.cnes.sitools.AbstractSitoolsTestCase;
import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.common.store.SitoolsStore;
import fr.cnes.sitools.metacatalogue.mock.DummyOAuthApplication;
import fr.cnes.sitools.metacatalogue.mock.DummyTokenValidationApplication;
import fr.cnes.sitools.metacatalogue.security.OAuthHelper;
import fr.cnes.sitools.registry.AppRegistryApplication;
import fr.cnes.sitools.registry.AppRegistryStoreXML;
import fr.cnes.sitools.registry.model.AppRegistry;

/**
 * Test the authorization mecanism of Sitools
 * 
 * 
 * @author m.gond
 */
public class CustomAuthenticationTestCase extends AbstractSitoolsTestCase {
  /**
   * The application store
   */
  private static SitoolsStore<AppRegistry> store = null;

  /**
   * Restlet Component for server
   */
  private Component component = null;
  /**
   * The application registry manager
   */
  private AppRegistryApplication appManager;

  /**
   * relative url for dataset management REST API
   * 
   * @return url
   */
  protected String getAttachUrl() {
    return SITOOLS_URL + "/myapp";
  }

  /**
   * relative url for dataset management REST API
   * 
   * @return url
   */
  protected String getAttachOauthValidatorUrl() {
    return SITOOLS_URL + "/dummyOauthValidator";
  }

  /**
   * absolute url
   * 
   * @return url
   */
  protected String getBaseUrl() {
    return super.getBaseUrl() + "/myapp";
  }

  /**
   * absolute url
   * 
   * @return url
   */
  protected String getBaseOauthValidatorUrl() {
    return super.getBaseUrl() + "/dummyOauthValidator";
  }

  /**
   * Get the test repository for applications
   * 
   * @return the test repository for applications
   */
  protected String getTestRepository() {
    return super.getTestRepository() + "/applications";
  }

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

      // attach the application for fake token validation
      appContext = this.component.getContext().createChildContext();
      appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
      appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, getAttachOauthValidatorUrl());
      component.getDefaultHost().attach(getAttachOauthValidatorUrl(), new DummyTokenValidationApplication(appContext));

      // attach fake business application to be queried with token
      appContext = this.component.getContext().createChildContext();
      appContext.getAttributes().put(ContextAttributes.APP_ATTACH_REF, getAttachUrl());
      appContext.getAttributes().put(ContextAttributes.SETTINGS, settings);
      DummyOAuthApplication oauthApp = new DummyOAuthApplication(appContext, getBaseOauthValidatorUrl());

      appManager.attachApplication(oauthApp);
      component.getInternalRouter().attach(getAttachUrl(), oauthApp);

    }

    if (!this.component.isStarted()) {
      this.component.start();
    }

  }

  /**
   * Test with no token provided, expect KO as a result
   * 
   * @throws IOException
   *           if there is an error while reading the result
   */
  @Test
  public void testNoTokenProvided() throws IOException {

    ClientResource clientResource = new ClientResource(getBaseUrl());

    // case 1 : no token

    Representation rep = clientResource.get();

    assertTrue(clientResource.getStatus().isSuccess());

    assertEquals("KO", rep.getText());

  }

  /**
   * Test with a token provided, expect OK as a result
   * 
   * @throws IOException
   *           if there is an error while reading the result
   */
  @Test
  public void testTokenProvided() throws IOException {

    // case 2 : token ok

    String token = "test_ok";

    Client client = new Client(this.component.getContext(), Protocol.HTTP);

    Request req = new Request(Method.GET, getBaseUrl());

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

  /**
   * Test with an invalid token provided, expect OK as a result
   * 
   * @throws IOException
   *           if there is an error while reading the result
   */
  @Test
  public void testInvalidTokenProvided() throws IOException {

    // case 2 : token ok

    String token = "test_invalid";

    Client client = new Client(this.component.getContext(), Protocol.HTTP);

    Request req = new Request(Method.GET, getBaseUrl());

    ChallengeScheme scheme = new ChallengeScheme("HTTP_Bearer", "Bearer");

    OAuthHelper helper = new OAuthHelper(scheme, true, true);
    Engine.getInstance().getRegisteredAuthenticators().add(helper);

    ChallengeResponse challengeResponse = new ChallengeResponse(scheme);
    challengeResponse.setRawValue(token);

    req.setChallengeResponse(challengeResponse);

    Response resp = client.handle(req);

    Representation representation = resp.getEntity();

    assertTrue(resp.getStatus().isSuccess());

    assertEquals("KO", representation.getText());

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
