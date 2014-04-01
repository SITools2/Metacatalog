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
package fr.cnes.sitools.metacatalogue.persistance;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.thoughtworks.xstream.XStream;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.common.SitoolsXStreamRepresentation;
import fr.cnes.sitools.common.XStreamFactory;
import fr.cnes.sitools.metacatalogue.AbstractHarvesterTestCase;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.IndexerModel;
import fr.cnes.sitools.persistence.HarvesterModelStore;
import fr.cnes.sitools.persistence.HarvesterModelStoreXmlImpl;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.server.HarvestersApplication;

/**
 * Test case of converters.
 * 
 * @author AKKA Technologies
 */
public abstract class AbstractHarvesterModelTestCase extends AbstractHarvesterTestCase {

  /**
   * static xml store instance for the test
   */
  private static HarvesterModelStore store = null;

  /**
   * Restlet Component for server
   */
  private Component component = null;

  /**
   * absolute url for dataset management REST API
   * 
   * @return url
   */
  protected String getAttachUrl() {
    return HarvesterSettings.getInstance().getString("HARVESTERS_APP_URL");
  }

  /**
   * absolute url for sitools REST API
   * 
   * @return url
   */
  protected String getBaseUrl() {
    return super.getBaseUrl() + "/admin";
  }

  @Before
  /**
   * Init and Start a server with GraphApplication
   * 
   * @throws java.lang.Exception
   */
  public void setUp() throws Exception {

    if (this.component == null) {
      this.component = new Component();
      this.component.getServers().add(Protocol.HTTP, getTestPort());
      this.component.getClients().add(Protocol.HTTP);
      this.component.getClients().add(Protocol.FILE);

      // Context
      Context ctx = this.component.getContext().createChildContext();

      if (store == null) {
        File storeDirectory = new File(settings.getStoreDIR(Consts.APP_HARVESTER_MODEL_STORE_DIR));
        store = new HarvesterModelStoreXmlImpl(storeDirectory);
        Collection<HarvesterModel> coll = store.getList();
        for (HarvesterModel harvesterModel : coll) {
          store.delete(harvesterModel);
        }

      }

      this.component.getDefaultHost().attach(getAttachUrl(), new HarvestersApplication(ctx, store));
    }

    if (!this.component.isStarted()) {
      this.component.start();
    }
  }

  @After
  /**
   * Stop server
   * @throws java.lang.Exception
   */
  public void tearDown() throws Exception {
    this.component.stop();
    this.component = null;
  }

  /**
   * Test CRUD Graph with JSon format exchanges.
   * 
   * @throws IOException
   */
  @Test
  public void testCRUD() throws IOException {
    assertNone();
    String id = "harvester_test";
    // create a new converter
    HarvesterModel harvesterModel = createHarvesterObject(id);
    // add it to the server
    create(harvesterModel);
    // retrieve the converterChained
    retrieve(id);

    update(harvesterModel);
    // delete the first converter
    delete(harvesterModel);
    // delete the converterChained
    assertNone();
  }

  /**
   * Create a ConverterModelDTO object with the specified description and identifier
   * 
   * @param description
   *          the description
   * @param id
   *          the ConverterModelDTO identifier
   * @return the created ConverterModelDTO
   */
  public HarvesterModel createHarvesterObject(String id) {
    HarvesterModel model = new HarvesterModel();
    model.setId(id);
    model.setCatalogType("opensearch");
    model.setHarvesterClassName("fake.class");
    IndexerModel indexer = new IndexerModel();
    indexer.setUrl("http://fake_path/test/indexer");
    model.setIndexerConf(indexer);
    return model;

  }

  /**
   * Add a converter to a Dataset
   * 
   * @param item
   *          ConverterModelDTO
   * @throws IOException
   */
  public void create(HarvesterModel item) throws IOException {
    Representation rep = getRepresentation(item, getMediaTest());

    ClientResource cr = new ClientResource(getBaseUrl());
    Representation result = cr.post(rep, getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());
    Response response = getResponse(getMediaTest(), result, HarvesterModel.class);
    assertTrue(response.getSuccess());
    HarvesterModel harvesterOut = (HarvesterModel) response.getItem();
    assertEquals(item.getId(), harvesterOut.getId());
    assertEquals(item.getHarvesterClassName(), harvesterOut.getHarvesterClassName());
    result.exhaust();
  }

  /**
   * Invoke GET, Gets the converterChainedModel details
   * 
   * @return the converterChainedModel details
   * @throws IOException
   */
  public HarvesterModel retrieve(String id) throws IOException {

    String url = getBaseUrl() + "/" + id;
    ClientResource cr = new ClientResource(url);

    Representation result = cr.get(getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());
    Response response = getResponse(getMediaTest(), result, HarvesterModel.class);
    assertTrue(response.getSuccess());
    HarvesterModel harvesterOut = (HarvesterModel) response.getItem();
    assertEquals(id, harvesterOut.getId());
    result.exhaust();
    return harvesterOut;

  }

  /**
   * Update the given ConverterModelDTO in the given converterChained
   * 
   * @param item
   *          the ConverterModelDTO
   * @throws IOException
   * 
   */
  public void update(HarvesterModel item) throws IOException {
    Representation rep = getRepresentation(item, getMediaTest());
    String url = getBaseUrl() + "/" + item.getId();

    ClientResource cr = new ClientResource(url);
    Representation result = cr.put(rep, getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());
    Response response = getResponse(getMediaTest(), result, HarvesterModel.class);
    assertTrue(response.getSuccess());
    HarvesterModel harvesterOut = (HarvesterModel) response.getItem();
    assertEquals(item.getId(), harvesterOut.getId());
    result.exhaust();

  }

  /**
   * Delete a converterModel from a converterChained
   * 
   * @param item
   *          ConverterModelDTO to delete
   * @throws IOException
   */
  public void delete(HarvesterModel item) throws IOException {
    String url = getBaseUrl() + "/" + item.getId();

    ClientResource cr = new ClientResource(url);
    Representation result = cr.delete(getMediaTest());
    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());
    Response response = getResponse(getMediaTest(), result, HarvesterModel.class);
    assertTrue(response.getSuccess());
    result.exhaust();

  }

  /**
   * Invokes GET and asserts result response is an empty array.
   * 
   * @throws IOException
   */
  public void assertNone() throws IOException {
    String url = getBaseUrl();
    ClientResource cr = new ClientResource(url);

    Representation result = cr.get(getMediaTest());

    assertNotNull(result);

    assertTrue(cr.getStatus().isSuccess());

    Response response = getResponse(getMediaTest(), result, HarvesterModel.class);
    assertTrue(response.getSuccess());
    assertNull(response.getItem());
    result.exhaust();

  }

  // ------------------------------------------------------------
  // RESPONSE REPRESENTATION WRAPPING

  /**
   * REST API Response wrapper for single item expected.
   * 
   * @param media
   *          MediaType expected
   * @param representation
   *          service response representation
   * @param dataClass
   *          class expected in the item property of the Response object
   * @return Response the response.
   */
  public static Response getResponse(MediaType media, Representation representation, Class<?> dataClass) {
    return getResponse(media, representation, dataClass, false);
  }

  /**
   * REST API Response Representation wrapper for single or multiple items expexted
   * 
   * @param media
   *          MediaType expected
   * @param representation
   *          service response representation
   * @param dataClass
   *          class expected for items of the Response object
   * @param isArray
   *          if true wrap the data property else wrap the item property
   * @return Response
   */
  public static Response getResponse(MediaType media, Representation representation, Class<?> dataClass, boolean isArray) {
    try {
      if (!media.isCompatible(getMediaTest()) && !media.isCompatible(MediaType.APPLICATION_XML)) {
        Engine.getLogger(AbstractHarvesterModelTestCase.class.getName()).warning("Only JSON or XML supported in tests");
        return null;
      }

      XStream xstream = XStreamFactory.getInstance().getXStreamReader(media);
      xstream.alias("response", Response.class);
      if (dataClass == HarvesterModel.class) {
        xstream.alias("harvesterModel", HarvesterModel.class);
        xstream.alias("indexerConf", IndexerModel.class);
      }

      if (isArray) {
        if (dataClass == HarvesterModel.class) {
          xstream.addImplicitCollection(HarvesterModel.class, "data", dataClass);
        }
        if (media.equals(MediaType.APPLICATION_JSON)) {
          xstream.addImplicitCollection(Response.class, "data", dataClass);
        }
      }
      else {
        xstream.alias("item", dataClass);
        xstream.alias("item", Object.class, dataClass);

        if (media.equals(MediaType.APPLICATION_JSON)) {
          // if (dataClass == HarvesterModel.class) {
          // xstream.addImplicitCollection(HarvesterModel.class, "converters", ConverterModelDTO.class);
          // }
          // xstream.addImplicitCollection(ConverterModelDTO.class, "parameters", ConverterParameter.class);
        }

        if (dataClass == HarvesterModel.class) {
          xstream.aliasField("harvesterModel", Response.class, "item");
        }

      }
      xstream.aliasField("data", Response.class, "data");

      SitoolsXStreamRepresentation<Response> rep = new SitoolsXStreamRepresentation<Response>(representation);
      rep.setXstream(xstream);

      if (media.isCompatible(getMediaTest())) {
        Response response = rep.getObject("response");

        return response;
      }
      else {
        Engine.getLogger(AbstractHarvesterModelTestCase.class.getName()).warning("Only JSON or XML supported in tests");
        return null; // TODO complete test with ObjectRepresentation
      }
    }
    finally {
      try {
        representation.exhaust();
      }
      catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
  }

  /**
   * Builds XML or JSON Representation of Project for Create and Update methods.
   * 
   * @param item
   *          Project
   * @param media
   *          APPLICATION_XML or APPLICATION_JSON
   * @return XML or JSON Representation
   */
  public static Representation getRepresentation(HarvesterModel item, MediaType media) {
    if (media.equals(MediaType.APPLICATION_JSON)) {
      return new JacksonRepresentation<HarvesterModel>(item);
    }
    else if (media.equals(MediaType.APPLICATION_XML)) {
      XStream xstream = XStreamFactory.getInstance().getXStream(media, false);
      XstreamRepresentation<HarvesterModel> rep = new XstreamRepresentation<HarvesterModel>(media, item);
      configure(xstream);
      rep.setXstream(xstream);
      return rep;
    }
    else {
      Engine.getLogger(AbstractHarvesterModelTestCase.class.getName()).warning("Only JSON or XML supported in tests");
      return null; // TODO complete test with ObjectRepresentation
    }
  }

  /**
   * Builds XML or JSON Representation of Project for Create and Update methods.
   * 
   * @param item
   *          Project
   * @param media
   *          APPLICATION_XML or APPLICATION_JSON
   * @return XML or JSON Representation
   */
  public static Representation getRepresentationDTO(HarvesterModel item, MediaType media) {
    if (media.equals(MediaType.APPLICATION_JSON)) {
      return new JacksonRepresentation<HarvesterModel>(item);
    }
    else if (media.equals(MediaType.APPLICATION_XML)) {
      XStream xstream = XStreamFactory.getInstance().getXStream(media, false);
      XstreamRepresentation<HarvesterModel> rep = new XstreamRepresentation<HarvesterModel>(media, item);
      configure(xstream);
      rep.setXstream(xstream);
      return rep;
    }
    else {
      Engine.getLogger(AbstractHarvesterModelTestCase.class.getName()).warning("Only JSON or XML supported in tests");
      return null; // TODO complete test with ObjectRepresentation
    }
  }

  /**
   * Configures XStream mapping of Response object with ConverterModelDTO content.
   * 
   * @param xstream
   *          XStream
   */
  private static void configure(XStream xstream) {
    xstream.autodetectAnnotations(false);
    xstream.alias("response", Response.class);

    // Parce que les annotations ne sont apparemment prises en compte
    xstream.omitField(Response.class, "itemName");
    xstream.omitField(Response.class, "itemClass");
  }

}
