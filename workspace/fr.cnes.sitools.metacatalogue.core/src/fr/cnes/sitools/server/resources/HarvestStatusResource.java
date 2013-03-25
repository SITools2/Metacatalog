/*******************************************************************************
 * Copyright 2011 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.sitools.server.resources;

import java.util.ArrayList;
import java.util.List;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.server.administration.AbstractHarvesterResource;
import fr.cnes.sitools.server.tasks.Task;

public class HarvestStatusResource extends AbstractHarvesterResource {

  @Get
  public Representation get(Variant variant) {

    Response response = null;
    do {
      // on charge le dataset
      HarvesterModel conf = store.get(harvesterId);
      if (conf == null) {
        response = new Response(false, "Cannot find Harvester configuration for id : " + harvesterId);
        break;
      }

      Task task = application.getTaskManager().get(harvesterId);

      HarvestStatus status;
      if (task == null) {
        status = new HarvestStatus();
        status.setStatus(HarvestStatus.STATUS_READY);
      }
      else {
        status = task.getStatus();
        status.setStatus(HarvestStatus.STATUS_RUNNING);
      }

      List<HarvestStatus> statuses = new ArrayList<HarvestStatus>();

      statuses.add(status);
      if (conf.getLastRunResult() != null) {
        statuses.add(conf.getLastRunResult());
      }

      response = new Response(true, statuses, HarvestStatus.class, "harvestStatus");
      break;
    } while (false);

    return getRepresentation(response, variant);

  }
}
