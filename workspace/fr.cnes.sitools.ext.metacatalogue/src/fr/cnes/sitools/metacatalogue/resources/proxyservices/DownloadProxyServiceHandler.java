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
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;

public class DownloadProxyServiceHandler extends AbstractProxyServiceHandler {

  /** The solr core url */
  protected String solrCoreUrl;

  private static final String DOWNLOAD_URL_FIELD = MetacatalogField.ARCHIVE.getField();

  /**
   * @param context
   */
  public DownloadProxyServiceHandler(Context context) {
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
    return getServiceUrl(urn, solrCoreUrl, DOWNLOAD_URL_FIELD);
  }

}
