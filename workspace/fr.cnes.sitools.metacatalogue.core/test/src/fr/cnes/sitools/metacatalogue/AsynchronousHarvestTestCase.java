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
import java.util.logging.Logger;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.thoughtworks.xstream.XStream;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.common.SitoolsXStreamRepresentation;
import fr.cnes.sitools.common.XStreamFactory;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.model.SitoolsDateConverter;

public class AsynchronousHarvestTestCase extends AbstractHarvesterServerTestCase {

  private static final String HARVESTER_ID = "mock";

  /** Max number of tries before stopping */
  private int maxTries = 5;

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.SitoolsServerTestCase#setUp()
   */
  @Override
  public void setUp() throws Exception {
    super.setUp();
    setMediaTest(MediaType.APPLICATION_JSON);
  }

  @Test
  public void test() throws Exception {

    String statusUrl = startHarvester(HARVESTER_ID);

    waitWhileHarvesterFinish(statusUrl);

    assertStatusFinish(statusUrl);

  }

  @Test
  public void testStop() throws Exception {

    startHarvester(HARVESTER_ID);

    stop(HARVESTER_ID);
  }

  private String startHarvester(String id) throws Exception {
    String url = getBaseUrl() + String.format("/admin/%s/harvest/start", id);

    ClientResource cr = new ClientResource(url);

    Representation result = cr.put(getMediaTest());

    // System.out.println(result.getText());

    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());

    Response response = getResponse(getMediaTest(), result, HarvestStatus.class);

    assertTrue(response.getSuccess());
    assertNotNull(response.getItem());
    HarvestStatus status = (HarvestStatus) response.getItem();

    assertNotNull(status.getUrl());
    assertEquals(HarvestStatus.STATUS_RUNNING, status.getStatus());

    return status.getUrl();
  }

  private void waitWhileHarvesterFinish(String statusUrl) throws InterruptedException {
    int i = 0;
    String status = null;

    do {
      Thread.sleep(1000);

      HarvestStatus harvesterStatus = getHarvestStatus(statusUrl);
      status = harvesterStatus.getStatus();

      i++;

    } while (i < maxTries && HarvestStatus.STATUS_RUNNING.equals(status));

  }

  private void stop(String id) {
    String url = getBaseUrl() + String.format("/admin/%s/harvest/stop", id);

    ClientResource cr = new ClientResource(url);

    Representation result = cr.put(getMediaTest());

    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());

    Response response = getResponse(getMediaTest(), result, HarvestStatus.class);

    assertTrue(response.getSuccess());
    assertNotNull(response.getItem());
    HarvestStatus status = (HarvestStatus) response.getItem();

    assertNotNull(status.getUrl());
    assertEquals(HarvestStatus.STATUS_READY, status.getStatus());
  }

  private void assertStatusFinish(String statusUrl) {
    HarvestStatus harvesterStatus = getHarvestStatus(statusUrl);
    assertEquals(HarvestStatus.STATUS_READY, harvesterStatus.getStatus());
  }

  private HarvestStatus getHarvestStatus(String statusUrl) {

    ClientResource cr = new ClientResource(statusUrl);

    Representation result = cr.get(getMediaTest());

    assertNotNull(result);
    assertTrue(cr.getStatus().isSuccess());

    Response response = getResponse(getMediaTest(), result, HarvestStatus.class, true);

    assertTrue(response.getSuccess());
    assertNotNull(response.getData());
    HarvestStatus status = (HarvestStatus) response.getData().get(0);

    return status;
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
      if (!media.isCompatible(MediaType.APPLICATION_JSON) && !media.isCompatible(MediaType.APPLICATION_XML)) {
        Logger.getLogger(AsynchronousHarvestTestCase.class.getName()).warning("Only JSON or XML supported in tests");
        return null;
      }

      XStream xstream = XStreamFactory.getInstance().getXStreamReader(media);
      xstream.autodetectAnnotations(false);

      xstream.alias("response", Response.class);
      xstream.registerConverter(new SitoolsDateConverter());

      if (isArray) {
        xstream.alias("item", dataClass);
        xstream.alias("item", Object.class, dataClass);
        // xstream.omitField(Response.class, "data");
        if (media.isCompatible(MediaType.APPLICATION_JSON)) {
          xstream.addImplicitCollection(Response.class, "data", dataClass);
        }

      }
      else {
        xstream.alias("item", dataClass);
        xstream.alias("item", Object.class, dataClass);

        xstream.aliasField("harvestStatus", Response.class, "item");

      }
      xstream.aliasField("data", Response.class, "data");

      SitoolsXStreamRepresentation<Response> rep = new SitoolsXStreamRepresentation<Response>(representation);
      rep.setXstream(xstream);

      if (media.isCompatible(getMediaTest())) {
        Response response = rep.getObject("response");

        return response;
      }
      else {
        Logger.getLogger(AsynchronousHarvestTestCase.class.getName()).warning("Only JSON or XML supported in tests");
        return null; // TODO complete test with ObjectRepresentation
      }
    }
    finally {
      try {
        if (representation != null) {
          representation.exhaust();
        }
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
