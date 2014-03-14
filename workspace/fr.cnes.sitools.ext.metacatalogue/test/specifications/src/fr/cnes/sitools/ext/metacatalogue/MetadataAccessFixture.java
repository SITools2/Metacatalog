package fr.cnes.sitools.ext.metacatalogue;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.apache.solr.client.solrj.SolrServer;
import org.concordion.integration.junit4.ConcordionRunner;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.AbstractSitoolsTestCase;
import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueAccessApplication;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.util.RIAPUtils;

//ATTENTION TEST PAS A JOUR AVEC LA NOUVELLE VERSION DU METACATALOGUE .....

/**
 * Tests the search service
 * 
 * Test sur les données spirit
 * 
 * @author m.gond
 */
@RunWith(ConcordionRunner.class)
public class MetadataAccessFixture extends AbstractSitoolsTestCase {
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

  /** The url of the search resource */
  private String searchResourceUrl = "/mdweb/search";

  private SolrServer server;

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

      server = SolRUtils.getEmbeddedSolRServer("./test/data/solr", "solr.xml", "kalideos_mock");

      ctx.getAttributes().put(ContextAttributes.APP_ATTACH_REF, getAttachUrl());
      ctx.getAttributes().put("INDEXER_SERVER", server);

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
    server.shutdown();
    this.component.stop();
    this.component = null;
  }

  /**
   * Test avec des paramètres standard et un resultat vide (count, startIndex, startPage) US : JEO Service opensearch,
   * id : 3166
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  public boolean queryMetacatalogue(String query) throws IOException, JSONException {
    String url = getServiceUrl() + "?" + query;
    return (sendRequestToSearchService(url) > 0);
  }

  public String getServiceUrl() {
    return getBaseUrl() + searchResourceUrl;
  }

  /**
   * Send a request to the specified URL and assert that the number is records in the expectedRecrod
   * 
   * @param url
   *          the url
   * @param expectedRecord
   *          the number of record expected
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  private int sendRequestToSearchService(String url) throws IOException, JSONException {
    ClientResource cr = new ClientResource(url);
    Representation result = cr.get(getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());

    try {
      JsonRepresentation jsonRepr = new JsonRepresentation(result);
      JSONObject json = jsonRepr.getJsonObject();
      assertEquals("FeatureCollection", json.get("type"));
      assertNotNull(json.getJSONArray("features"));
      return json.getJSONArray("features").length();
    }
    finally {
      RIAPUtils.exhaust(result);
    }

  }

}
