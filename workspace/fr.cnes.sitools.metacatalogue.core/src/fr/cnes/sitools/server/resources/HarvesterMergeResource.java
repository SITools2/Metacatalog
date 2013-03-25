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

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.server.InfoResource;
import fr.cnes.sitools.server.administration.AbstractHarvesterResource;

/**
 * Resource to merge all the catalog harvest into a single one
 * 
 * @author m.gond
 * 
 */
public class HarvesterMergeResource extends AbstractHarvesterResource {

  @Override
  protected void doInit() {
    super.doInit();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.restlet.resource.ServerResource#put(org.restlet.representation.Representation,
   * org.restlet.representation.Variant)
   */
  @Override
  protected Representation put(Representation representation, Variant variant) {
    application.lock(new InfoResource(Status.CLIENT_ERROR_CONFLICT, "Merge operation on the metacatalog"));
    try {
      merge();
    }
    finally {
      application.unlock();
    }
    Response response = new Response(true, "merge successfull");
    return getRepresentation(response, variant);
  }
}
