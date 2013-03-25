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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.model.HarvesterModel;

/**
 * Resource for managing single HarvesterModel (GET UPDATE DELETE)
 * 
 * @author jp.boignard (AKKA Technologies)
 * 
 */
public final class HarvesterResource extends AbstractHarvesterResource {

  /**
   * get all DataSources
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

  /**
   * Update / Validate existing DataSource
   * 
   * @param representation
   *          the representation sent
   * @param variant
   *          client preferred media type
   * @return Representation
   */
  @Put
  public Representation put(Representation representation, Variant variant) {
    HarvesterModel harvesterOutput = null;
    try {
      HarvesterModel harvesterInput = null;
      if (representation != null) {
        harvesterInput = getObject(representation);
        // get the last harvest date and store it in the input
        HarvesterModel harvesterFromStore = getStore().get(getHavesterId());
        harvesterInput.setLastHarvest(harvesterFromStore.getLastHarvest());
        harvesterInput.setStatus(harvesterFromStore.getStatus());

        // Business service
        getStore().save(harvesterInput);
        harvesterOutput = getStore().get(getHavesterId());
      }

      if (harvesterOutput != null) {
        Response response = new Response(true, harvesterOutput, HarvesterModel.class, "harvesterModel");
        return getRepresentation(response, variant);
      }
      else {
        Response response = new Response(false, "Can not validate harvesterModel");
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

  /**
   * Delete data source
   * 
   * @param variant
   *          client preferred media type
   * @return Representation
   */
  @Delete
  public Representation delete(Variant variant) {
    try {
      Response response;
      HarvesterModel harvesterOutput = getStore().get(getHavesterId());
      if (harvesterOutput == null) {
        response = new Response(false, "Cannot find harvester model with id : " + getHavesterId());
      }
      else {
        // Business service
        getStore().delete(harvesterOutput);
        response = new Response(true, "harvester.delete.success");
      }
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

}
