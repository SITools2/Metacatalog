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
package fr.cnes.sitools.metacatalogue.resources.proxyservices;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;

public class WmsHttpsServiceHandler extends AbstractHttpsServiceHandler {

  /** The solr core url */
  protected String solrCoreUrl;

  static final String WMS_URL_FIELD = MetacatalogField.SERVICES_BROWSE_LAYER_URL.getField();

  /**
   * Constuctor with {@link Context}
   * 
   * @param context
   *          the {@link Context}
   */
  public WmsHttpsServiceHandler(Context context) {
    super(context);

  }

  @Override
  protected String createServiceUrl(Request request, Response response) {
    MetacatalogueApplication application = (MetacatalogueApplication) getApplication();

    solrCoreUrl = application.getSolrCoreUrl();
    
    String urn = (String) request.getAttributes().get("urn");
    if (urn == null || urn.isEmpty()) {
      throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "urn parameter is mandatory");
    }

    //String serviceUrl = "https://portal.ingeoclouds.eu/sitools/datasets?media=json";
    String serviceUrl = getServiceUrl(urn, solrCoreUrl, WMS_URL_FIELD);

    // we add {rq} at the end of the url to get all query parameters from the original request to the other
    // it may already have a ? in the url so we add a parameter with null value. Then it had either ? or & and the {rq}
    // value
    Reference ref = new Reference(serviceUrl);
    ref.addQueryParameter("{rq}", null);
//    // the reference encodes the special character so we decode them
    serviceUrl = Reference.decode(ref.toString());
    
    return serviceUrl;
  }

}
