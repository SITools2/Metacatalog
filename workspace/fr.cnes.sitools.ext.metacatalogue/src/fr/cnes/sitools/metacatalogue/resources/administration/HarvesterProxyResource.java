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
package fr.cnes.sitools.metacatalogue.resources.administration;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.SitoolsResource;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueAdminApplication;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginParameter;

public class HarvesterProxyResource extends SitoolsResource {

  protected String harvesterServerUrl;

  protected String harvesterId;

  protected MetacatalogueAdminApplication application;

  @Override
  public void sitoolsDescribe() {

  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.common.SitoolsResource#doInit()
   */
  @Override
  protected void doInit() {
    super.doInit();

    application = (MetacatalogueAdminApplication) getApplication();

    ApplicationPluginParameter harvesterServerUrlParam = application.getParameter("metacatalogServer");
    if (harvesterServerUrlParam == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No metacatalog server defined");
    }

    harvesterServerUrl = harvesterServerUrlParam.getValue();

    harvesterId = (String) this.getRequest().getAttributes().get("harvesterId");

  }

  private Representation handle(Variant variant) {

    String urlAttach = application.getModel().getUrlAttach();

    String endPath = this.getReference().toString();
    endPath = endPath.substring(endPath.indexOf(urlAttach) + urlAttach.length(), endPath.length());

    String url = harvesterServerUrl + endPath;

    Reference reference = new Reference(url);
    reference.setQuery(getRequest().getResourceRef().getQuery());

    Request request = getRequest();
    request.setResourceRef(reference);

    Response response = new Response(request);
    ClientResource clientResource = new ClientResource(request, response);

    Representation repr = clientResource.handle();
    this.getResponse().setStatus(clientResource.getResponse().getStatus());    

    return repr;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.restlet.resource.ServerResource#delete(org.restlet.representation.Variant)
   */
  @Delete
  protected Representation delete(Variant variant) throws ResourceException {
    return handle(variant);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.restlet.resource.ServerResource#get(org.restlet.representation.Variant)
   */
  @Get
  protected Representation get(Variant variant) throws ResourceException {
    return handle(variant);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.restlet.resource.ServerResource#post(org.restlet.representation.Representation,
   * org.restlet.representation.Variant)
   */
  @Post
  protected Representation post(Representation entity, Variant variant) throws ResourceException {
    return handle(variant);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.restlet.resource.ServerResource#put(org.restlet.representation.Representation,
   * org.restlet.representation.Variant)
   */
  @Put
  protected Representation put(Representation representation, Variant variant) throws ResourceException {
    return handle(variant);
  }

}
