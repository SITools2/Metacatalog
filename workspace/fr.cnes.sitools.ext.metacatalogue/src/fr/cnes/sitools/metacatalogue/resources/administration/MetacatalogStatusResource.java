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
package fr.cnes.sitools.metacatalogue.resources.administration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;

import fr.cnes.sitools.common.SitoolsResource;
import fr.cnes.sitools.common.model.Response;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueAdminApplication;
import fr.cnes.sitools.metacatalogue.dto.MetacatalogApplicationDTO;
import fr.cnes.sitools.util.ClientResourceProxy;

/**
 * A resource to get the status of the metacatalog, and some simple information about it
 * 
 * @author m.gond
 * 
 */
public class MetacatalogStatusResource extends SitoolsResource {

  @Override
  public void sitoolsDescribe() {
    setName("MetacatalogStatusResource");
    setDescription("A resource to get the status of the metacatalog, and some simple information about it");
  }

  @Get
  public Representation get(Variant variant) {
    Response response;
    MetacatalogueAdminApplication application = (MetacatalogueAdminApplication) getApplication();
    MetacatalogApplicationDTO appDTO = new MetacatalogApplicationDTO();

    appDTO.setDescription(getParameter("description", application));
    appDTO.setContact(getParameter("contact", application));
    appDTO.setNameCoreMetacatalogue(getParameter("metacatalogSolrName", application));
    appDTO.setUrlCoreMetacatalogue(getParameter("metacatalogSolrCore", application));
    appDTO.setUrlHarvester(getParameter("metacatalogServer", application));

    addHarvesterStatusInformation(appDTO, application);

    response = new Response(true, appDTO, MetacatalogApplicationDTO.class, "metacatalogStatus");

    return getRepresentation(response, variant);

  }

  @Override
  public void describeGet(MethodInfo info) {
    info.setDocumentation("Method to get the status of the metacatalog server");
    info.setIdentifier("status");
    addStandardGetRequestInfo(info);
    addStandardResponseInfo(info);
    addStandardInternalServerErrorInfo(info);
  }

  private String getParameter(String paramName, MetacatalogueAdminApplication application) {
    if (application.getParameter(paramName) != null) {
      return application.getParameter(paramName).getValue();
    }
    return null;
  }

  private void addHarvesterStatusInformation(MetacatalogApplicationDTO appDTO, MetacatalogueAdminApplication application) {

    String urlHarvester = getParameter("metacatalogServer", application);
    if (urlHarvester == null) {
      return;
    }

    ClientResourceProxy clientResourceProxy = new ClientResourceProxy(urlHarvester, Method.GET);
    ClientResource clientResource = clientResourceProxy.getClientResource();
    clientResource.setRetryDelay(500);
    clientResource.setRetryOnError(false);
    
    ArrayList<Preference<MediaType>> objectMediaType = new ArrayList<Preference<MediaType>>();
    objectMediaType.add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
    clientResource.getRequest().getClientInfo().setAcceptedMediaTypes(objectMediaType);

    Representation representation = clientResource.handle();

    if (!clientResource.getStatus().isError()) {

      try {
        JSONObject jsonObject = new JSONObject(representation.getText());

        boolean success = jsonObject.getBoolean("success");
        if (success) {

          JSONObject metacatalogStatus = jsonObject.getJSONObject("metacatalogStatus");
          boolean pendingOperation = metacatalogStatus.getBoolean("pendingOperation");
          appDTO.setPendingOperation(pendingOperation);
          if (pendingOperation) {
            appDTO.setPendingOperationMessage(metacatalogStatus.getString("pendingOperationMessage"));
          }
        }
      }
      catch (IOException e) {
        getLogger().log(Level.WARNING, "Error reading the metacatalog status", e);
      }
      catch (JSONException e) {
        getLogger().log(Level.WARNING, "Error parsing the metacatalog status JSON", e);
      }
    }

  }
}
