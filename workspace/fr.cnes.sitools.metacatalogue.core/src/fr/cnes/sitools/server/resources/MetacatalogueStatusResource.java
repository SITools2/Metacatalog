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
package fr.cnes.sitools.server.resources;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.server.HarvestersApplication;
import fr.cnes.sitools.server.administration.AbstractHarvesterResource;
import fr.cnes.sitools.server.dto.MetacatalogStatusDTO;

public class MetacatalogueStatusResource extends AbstractHarvesterResource {

  @Get
  public Representation get(Variant variant) {

    HarvestersApplication application = (HarvestersApplication) getApplication();
    MetacatalogStatusDTO status = new MetacatalogStatusDTO();

    boolean isLocked = application.isLocked();
    status.setPendingOperation(isLocked);
    if (isLocked && application.getInfoResource() != null) {
      status.setPendingOperationMessage(application.getInfoResource().getMessage());
    }

    Response response = new Response(true, status, MetacatalogStatusDTO.class, "metacatalogStatus");

    return getRepresentation(response, variant);
  }

}
