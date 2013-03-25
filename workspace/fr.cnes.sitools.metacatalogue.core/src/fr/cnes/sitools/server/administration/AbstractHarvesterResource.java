/*******************************************************************************
 * Copyright 2011, 2012 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.sitools.server.administration;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.jackson.JacksonRepresentation;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.thoughtworks.xstream.XStream;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.common.XStreamFactory;
import fr.cnes.sitools.metacatalogue.common.HarvesterUtils;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.SitoolsDateConverter;
import fr.cnes.sitools.persistence.HarvesterModelStore;
import fr.cnes.sitools.server.HarvestersApplication;

/**
 * Abstract resource for DataSource Objects management
 * 
 * @author jp.boignard (AKKA Technologies)
 * 
 */
public abstract class AbstractHarvesterResource extends ServerResource {

  /** Parent application */
  protected HarvestersApplication application = null;

  /** Store */
  protected HarvesterModelStore store = null;

  /** DataSource identifier parameter */
  protected String harvesterId = null;

  @Override
  protected void doInit() {
    super.doInit();
    // Declares the two variants supported
    getVariants().add(new Variant(MediaType.APPLICATION_XML));
    getVariants().add(new Variant(MediaType.APPLICATION_JSON));

    application = (HarvestersApplication) getApplication();
    store = application.getStore();

    harvesterId = (String) this.getRequest().getAttributes().get("harvesterId");
  }

  /**
   * Get the representation
   * 
   * @param response
   *          the response to use
   * @param media
   *          the media to use
   * @return Representation
   */
  public final Representation getRepresentation(Response response, MediaType media) {
    getLogger().info(media.toString());
    if (media.isCompatible(MediaType.APPLICATION_JAVA_OBJECT)) {
      return new ObjectRepresentation<Response>(response);
    }

    XStream xstream = XStreamFactory.getInstance().getXStream(media);
    configure(xstream, response);
    xstream.alias("harvesterModel", HarvesterModel.class);
    
    xstream.registerConverter(new SitoolsDateConverter());
    
    XstreamRepresentation<Response> rep = new XstreamRepresentation<Response>(media, response);
    rep.setXstream(xstream);
    return rep;
  }

  /**
   * Get object from representation
   * 
   * @param representation
   *          the representation to use
   * @return JDBCDataSource
   * @throws IOException
   */
  public final HarvesterModel getObject(Representation representation) throws IOException {
    HarvesterModel object = null;

    if (MediaType.APPLICATION_XML.isCompatible(representation.getMediaType())) {
      // Parse the XML representation to get the bean
      // Default parsing :
      // object = new XstreamRepresentation<JDBCDataSource>(representation).getObject();
      XstreamRepresentation<HarvesterModel> repXML = new XstreamRepresentation<HarvesterModel>(representation,
          HarvesterModel.class);
      XStream xstream = XStreamFactory.getInstance().getXStreamReader(MediaType.APPLICATION_XML);
      xstream.autodetectAnnotations(false);
      xstream.alias("harvesterModel", HarvesterModel.class);

      repXML.setXstream(xstream);
      object = repXML.getObject();
    }
    else if (MediaType.APPLICATION_JSON.isCompatible(representation.getMediaType())) {
      // Parse the JSON representation to get the bean
      object = new JacksonRepresentation<HarvesterModel>(representation, HarvesterModel.class).getObject();
    }

    return object;
  }

  /**
   * Gets the store value
   * 
   * @return the store
   */
  public final HarvesterModelStore getStore() {
    return store;
  }

  /**
   * Gets the datasourceId value
   * 
   * @return the datasourceId
   */
  public final String getHavesterId() {
    return harvesterId;
  }

  /**
   * Configure the XStream
   * 
   * @param xstream
   *          the XStream to treat
   * @param response
   *          the response used
   */
  public void configure(XStream xstream, Response response) {
    xstream.autodetectAnnotations(false);
    xstream.alias("response", Response.class);

    // Because annotations are apparently missed
    xstream.omitField(Response.class, "itemName");
    xstream.omitField(Response.class, "itemClass");

    // If a class is present inside the response, link the item alias with this class
    if (response.getItemClass() != null) {
      xstream.alias("item", Object.class, response.getItemClass());
    }

    // If the object has a name, associate its name instead of item in the response
    if (response.getItemName() != null) {
      xstream.aliasField(response.getItemName(), Response.class, "item");
    }

  }

  /**
   * Gets representation according to the specified Variant if present. If variant is null (when content negociation =
   * false) sets the variant to the first client accepted mediaType.
   * 
   * @param response
   *          the response to use
   * @param variant
   *          the variant to use
   * @return Representation the final representation of the response
   */
  public Representation getRepresentation(Response response, Variant variant) {
    MediaType defaultMediaType = variant.getMediaType();
    return this.getRepresentation(response, defaultMediaType);
  }

  protected void merge() throws ResourceException {
    try {
      HarvesterUtils.merge(store.getList());
    }
    catch (SolrServerException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }
    catch (IOException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }
  }

}
