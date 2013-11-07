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
package fr.cnes.sitools.server.resources;

import java.io.File;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;

public class JsonFileExpositionResource extends ServerResource {

  @Get
  public Representation get(Variant variant) {

    String fileName = null;
    if (this.getReference().getLastSegment().endsWith("catalogsTypes")) {
      fileName = "catalogsTypes.json";
    }
    else if (this.getReference().getLastSegment().endsWith("havestersClasses")) {
      fileName = "harvestersClasses.json";
    }
    

    HarvesterSettings settings = HarvesterSettings.getInstance();

    String resourcesDir = settings.getString("RESOURCES_DIRECTORY");
    String filePath = settings.getRootDirectory() + resourcesDir + "/" + fileName;
    File file = new File(filePath);

    if (!file.exists()) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "File " + fileName + " cannot be found");
    }

    return new FileRepresentation(file, MediaType.APPLICATION_JSON);

  }
}
