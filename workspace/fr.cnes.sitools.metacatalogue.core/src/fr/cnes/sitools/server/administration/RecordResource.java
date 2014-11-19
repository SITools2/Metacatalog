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
package fr.cnes.sitools.server.administration;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.persistence.HarvesterModelStore;
import fr.cnes.sitools.server.HarvestersApplication;

/**
 * RecordResource
 * 
 * @author tx.chevallier
 * @version 0.1
 */
public class RecordResource extends AbstractHarvesterResource {

  /** Parent application */
  protected HarvestersApplication application = null;

  /** DataSource identifier parameter */
  protected String harvesterId = null;

  /** Store */
  protected volatile HarvesterModelStore store = null;

  /** record id */
  protected String recordId = null;

  @Override
  protected void doInit() {

    super.doInit();

    getVariants().add(new Variant(MediaType.APPLICATION_XML));
    getVariants().add(new Variant(MediaType.APPLICATION_JSON));

    application = (HarvestersApplication) getApplication();
    store = application.getStore();

    harvesterId = (String) this.getRequest().getAttributes().get("harvesterId");

    recordId = (String) this.getRequest().getAttributes().get("recordId");

  }

  /**
   * Delete record
   * 
   * @param variant
   *          client preferred media type
   * @return Representation
   */
  @Delete
  public Representation delete(Variant variant) {
    
    Response response = null;
    
    HarvesterModel model = getStore().get(getHavesterId());
    SolrServer server = SolRUtils.getSolRServer(model.getIndexerConf().getUrl());
    if (server == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot connect to core "
          + model.getIndexerConf().getUrl());
    }
    try {
      UpdateResponse solrResponse = server.deleteById(recordId);
      server.commit();
      if (solrResponse.getStatus() == 0) {
        response = new Response(true, "record deleted");
      } 
      else {
        response = new Response(false, "record not deleted");
      }
    }
    catch (SolrServerException e) {
      response = new Response(false, e.getMessage());
      e.printStackTrace();
    }
    catch (IOException e) {
      response = new Response(false, e.getMessage());
      e.printStackTrace();
    }

    return getRepresentation(response, variant);

  }

}
