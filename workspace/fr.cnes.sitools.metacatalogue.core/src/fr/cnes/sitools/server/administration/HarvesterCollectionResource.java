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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.model.HarvesterModel;

/**
 * Class for JDBC data source collection management
 * 
 * @author jp.boignard (AKKA Technologies)
 * 
 */
public final class HarvesterCollectionResource extends AbstractHarvesterResource {

  /**
   * Create a new DataSource
   * 
   * @param representation
   *          input
   * @param variant
   *          client preferred media type
   * @return Representation
   */
  @Post
  public Representation post(Representation representation, Variant variant) {
    if (representation == null) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "DATASOURCE_REPRESENTATION_REQUIRED");
    }
    try {
      HarvesterModel harvesterInput = getObject(representation);
      harvesterInput.setStatus("INACTIVE");

      // Business service
      getStore().save(harvesterInput);

      HarvesterModel datasourceOutput = getStore().get(harvesterInput.getId());

      // Response
      Response response = new Response(true, datasourceOutput, HarvesterModel.class, "harvesterModel");
      return getRepresentation(response, variant);

    }
    catch (ResourceException e) {
      getLogger().log(Level.INFO, null, e);
      throw e;
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, null, e);
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }
  }

  /**
   * get all DataSets
   * 
   * @param variant
   *          client preferred media type
   * @return Representation
   */
  @Get
  public Representation get(Variant variant) {
    try {
      if (getHavesterId() != null) {
        HarvesterModel harvesterModel = getStore().get(getHavesterId());
        Response response = new Response(true, harvesterModel, HarvesterModel.class, "harvesterModel");
        return getRepresentation(response, variant);
      }
      else {
        Collection<HarvesterModel> harvesterModels = getStore().getList();

        List<HarvesterModel> harvesterModelsReturn = new ArrayList<HarvesterModel>();
        harvesterModelsReturn.addAll(harvesterModels);
        // int total = datasources.size();
        // datasources = getStore().getPage(datasources);
        Response response = new Response(true, harvesterModelsReturn, HarvesterModel.class, "harvesterModel");
        response.setTotal(harvesterModels.size());
        return getRepresentation(response, variant);
      }
    }
    catch (ResourceException e) {
      getLogger().log(Level.INFO, null, e);
      throw e;
    }
    catch (Exception e) {
      getLogger().log(Level.SEVERE, null, e);
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }
  }

}
