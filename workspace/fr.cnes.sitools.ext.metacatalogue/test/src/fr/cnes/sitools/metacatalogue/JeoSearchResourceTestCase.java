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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.AbstractSitoolsTestCase;
import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;
import fr.cnes.sitools.util.RIAPUtils;


//ATTENTION TEST PAS A JOUR AVEC LA NOUVELLE VERSION DU METACATALOGUE .....

/**
 * Tests the search service
 * 
 * Test sur les données spirit
 * 
 * @author m.gond
 */
public class JeoSearchResourceTestCase extends AbstractSitoolsTestCase {
  private static final String METACATALOGUE_APP_URL = "/metacatalogue";

  /**
   * Root path for storing files
   */
  public static final String TEST_FILES_REPOSITORY = SitoolsSettings.getInstance().getString("Tests.STORE_DIR");

  /**
   * Restlet Component for server
   */
  private Component component = null;

  private MetacatalogueApplication application;

  /** The url of the search resource */
  private String searchResourceUrl = "/search";

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

      application = new MetacatalogueApplication(ctx);
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

  /**
   * Test avec des paramètres standard et un resultat vide (count, startIndex, startPage) US : JEO Service opensearch,
   * id : 3166
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  @Test
  public void testEmptyResult() throws IOException, JSONException {
    String url = getServiceUrl() + "?limit=0&pw=0&start=0";
    sendRequestToSearchService(url, 0);
  }

  /**
   * Test avec des paramètres standard (count, startIndex, startPage) US : JEO Service opensearch, id : 3166
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   */
  @Test
  public void testWithParamStandard() throws IOException, JSONException {
    String url = getServiceUrl() + "?limit=1&pw=0&start=0";
    sendRequestToSearchService(url, 1);

  }

  /**
   * Test avec des paramètres lat long radius US : JEO Service opensearch avec paramètres geo:lat geo:lon et geo:radius
   * , id : 3170
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithBoxParam() throws IOException, JSONException {
    String boxCoord = "-57.96936,53.77307,-29.84436,63.50815";

    String url = getServiceUrl() + "?bbox=" + boxCoord;
    sendRequestToSearchService(url, 6);

  }

  /**
   * Test avec des paramètres Lat long radius US : JEO Service opensearch avec paramètre geo:box , id : 3169
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  // @Test
  public void testWithLatLongRadiusParam() throws IOException, JSONException {
    // distance is 0.8 in order to find only one record
    String distance = "0.8";
    String lat = "1";
    String longitude = "41";
    String url = getServiceUrl() + "?lat=" + lat + "&lon=" + longitude + "&r=" + distance;
    sendRequestToSearchService(url, 1);

  }

  /**
   * Test avec des paramètres date US : JJEO Service opensearch avec paramètres time:start et time:end : 3171
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithDateParam() throws IOException, JSONException {
    String dtstart = "2007-12-06T00:00:00";
    String dtend = "2007-12-06T00:00:00";
    String url = getServiceUrl() + "?startDate=" + dtstart + "&completionDate=" + dtend;
    sendRequestToSearchService(url, 1);

  }

  /**
   * Test avec des paramètres search terms US : JEO Service opensearch avec paramètre searchTerms : 3187
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSearchTermsParam() throws IOException, JSONException {
    String searchTerms = "\"Byrd Glacier\"";
    String url = getServiceUrl() + "?q=" + searchTerms;
    sendRequestToSearchService(url, 2);
  }

  /**
   * Test avec des paramètres search terms US : JEO Service opensearch avec paramètre searchTerms : 3187
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSearchTermsParamFuzySearch() throws IOException, JSONException {
    String searchTerms = "Byrd*";
    String url = getServiceUrl() + "?q=" + searchTerms;
    sendRequestToSearchService(url, 3);
  }

  /**
   * Test avec des paramètres specifiques de type date US : JEO Service opensearch avec paramètres spécifiques, 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSpecificParamDate() throws IOException, JSONException {
    String dateParam = "2010-08-01T00:00:00";
    String url = getServiceUrl() + "?startDate=" + dateParam;
    sendRequestToSearchService(url, 4);
  }

  /**
   * Test avec des paramètres specifiques de type date US : JEO Service opensearch avec paramètres spécifiques, 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  @Test
  public void testWithSpecificParamDateInterval() throws IOException, JSONException {
    String dateParam = "2009-02-01T00:00:00/2011-03-01T00:00:00";
    String url = getServiceUrl() + "?startDate=" + dateParam;
    sendRequestToSearchService(url, 11);
  }

  /**
   * Test avec des paramètres specifiques de type Enumeration US : JEO Service opensearch avec paramètres spécifiques,
   * 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  // @Test
  public void testWithSpecificParamEnumeration() throws IOException, JSONException {
    String enumParam = "bon|Moyen";
    String url = getServiceUrl() + "?building_state=" + enumParam;
    sendRequestToSearchService(url, 2);
  }

  /**
   * Test avec des paramètres specifiques de type Text US : JEO Service opensearch avec paramètres spécifiques, 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  // @Test
  public void testWithSpecificParamText() throws IOException, JSONException {
    String textParam = "texte de la note 465456";
    String url = getServiceUrl() + "?notes=" + textParam;
    sendRequestToSearchService(url, 1);
  }

  /**
   * Test avec des paramètres specifiques de type Number US : JEO Service opensearch avec paramètres spécifiques, 3188
   * 
   * @throws IOException
   *           if an error occur while reading the response
   * @throws JSONException
   *           if an error occur while parsing the JSON
   * 
   */
  // @Test
  public void testWithSpecificParamNumber() throws IOException, JSONException {
    String numberParam = "172.5";
    String url = getServiceUrl() + "?ele=" + numberParam;
    sendRequestToSearchService(url, 1);
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
  private void sendRequestToSearchService(String url, int expectedRecord) throws IOException, JSONException {
    ClientResource cr = new ClientResource(url);
    Representation result = cr.get(getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());

    try {
      JsonRepresentation jsonRepr = new JsonRepresentation(result);
      JSONObject json = jsonRepr.getJsonObject();
      assertEquals("FeatureCollection", json.get("type"));
      assertNotNull(json.getJSONArray("features"));
      assertEquals(expectedRecord, json.getJSONArray("features").length());
    }
    finally {
      RIAPUtils.exhaust(result);
    }

  }

}
