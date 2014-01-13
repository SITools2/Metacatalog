package fr.cnes.sitools.metacatalogue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.AbstractSitoolsTestCase;
import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueAccessApplication;
import fr.cnes.sitools.metacatalogue.resources.suggest.SuggestDTO;
import fr.cnes.sitools.util.RIAPUtils;

public class SuggestionTestCase extends AbstractSitoolsTestCase {

  private static final String METACATALOGUE_APP_URL = "/metacatalogue";

  /**
   * Root path for storing files
   */
  public static final String TEST_FILES_REPOSITORY = SitoolsSettings.getInstance().getString("Tests.STORE_DIR");

  /**
   * Restlet Component for server
   */
  private Component component = null;

  private MetacatalogueAccessApplication application;

  /**
   * relative url for dataset management REST API
   * 
   * @return url
   */
  protected String getAttachUrl() {
    return SITOOLS_URL + METACATALOGUE_APP_URL;
  }

  /**
   * absolute url for dataset management REST API
   * 
   * @return url
   */
  protected String getBaseUrl() {
    return super.getBaseUrl() + METACATALOGUE_APP_URL;
  }

  @Before
  @Override
  /**
   * Init and Start a server with GraphApplication
   * 
   * @throws java.lang.Exception
   */
  public void setUp() throws Exception {
    if (this.component == null) {
      SitoolsSettings settings = SitoolsSettings.getInstance();
      this.component = new Component();
      this.component.getServers().add(Protocol.HTTP, getTestPort());
      this.component.getClients().add(Protocol.HTTP);
      this.component.getClients().add(Protocol.FILE);
      this.component.getClients().add(Protocol.CLAP);

      // Context
      Context ctx = this.component.getContext().createChildContext();
      settings.setStoreDIR(TEST_FILES_REPOSITORY);
      settings.setStores(new HashMap<String, Object>());
      ctx.getAttributes().put(ContextAttributes.SETTINGS, settings);

      ctx.getAttributes().put(ContextAttributes.APP_ATTACH_REF, getAttachUrl());

      application = new MetacatalogueAccessApplication(ctx);
      application.getModel().getParametersMap().get("thesaurus")
          .setValue("./test/resources/thesaurus/TechniqueDev3.rdf");
      this.component.getDefaultHost().attach(getAttachUrl(), application);

    }

    if (!this.component.isStarted()) {
      this.component.start();
    }
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

  @Test
  public void testSuggestErrorBadThesaurus() {
    String suggest = "Alb";
    String url = getBaseUrl() + "/suggest?q=" + suggest;

    application.getModel().getParametersMap().remove("thesaurus");

    final Client client = new Client(Protocol.HTTP);
    Request request = new Request(Method.GET, url);
    Response response = null;
    try {
      response = client.handle(request);

      assertNotNull(response);
      assertTrue(response.getStatus().isError());
      assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());

    }
    finally {
      if (response != null) {
        RIAPUtils.exhaust(response);
      }
    }

  }

  @Test
  public void testSuggest() {

    String suggest = "Alb";
    String url = getBaseUrl() + "/suggest?q=" + suggest;

    ClientResource cr = new ClientResource(url);
    SuggestDTO[] res = cr.get(SuggestDTO[].class);

    assertNotNull(res);
    assertEquals(21, res.length);

    for (int i = 0; i < res.length; i++) {
      System.out.println(res[i].getSuggestion());
    }

  }

  @Test
  public void testSuggestErrorNoParam() {

    String url = getBaseUrl() + "/suggest";

    final Client client = new Client(Protocol.HTTP);
    Request request = new Request(Method.GET, url);
    Response response = null;
    try {
      response = client.handle(request);

      assertNotNull(response);
      assertTrue(response.getStatus().isError());
      assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, response.getStatus());

    }
    finally {
      if (response != null) {
        RIAPUtils.exhaust(response);
      }
    }

  }
}