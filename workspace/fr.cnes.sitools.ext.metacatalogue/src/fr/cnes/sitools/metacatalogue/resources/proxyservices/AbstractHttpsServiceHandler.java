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

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Redirector;

import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;

public abstract class AbstractHttpsServiceHandler extends Restlet {

  /**
   * Default constructor
   */
  public AbstractHttpsServiceHandler() {
    super();
  }

  /**
   * Constructor with {@link Context}
   * 
   * @param context
   *          the {@link Context}
   */
  public AbstractHttpsServiceHandler(Context context) {
    super(context);
  }

  @Override
  public void handle(Request request, Response response) {
    MetacatalogueApplication application = (MetacatalogueApplication) getApplication();

    String serviceUrl = createServiceUrl(request, response);

    Redirector redirector;
    if (request.getClientInfo().isAuthenticated()) {
      String username = application.getServicesUserName();
      String password = application.getServicesPassword();
      redirector = new RedirectorProxyAuthenticated(getContext(), serviceUrl, Redirector.MODE_SERVER_OUTBOUND,
          username, password);
    }
    else {
      redirector = new RedirectorHttps(getContext(), serviceUrl, Redirector.MODE_SERVER_OUTBOUND);
    }

    redirector.handle(request, response);
  }

  /**
   * Gets the service url corresponding to the following serviceFieldName for the data identified by the given urn
   * 
   * @param urn
   *          the urn of the data to find
   * @param solrCoreUrl
   *          the url of the solrCore
   * @param serviceFieldName
   *          the name of the field in the solrIndex
   * @return the url of the service
   */
  protected String getServiceUrl(String urn, String solrCoreUrl, String serviceFieldName) {

    SolrServer server = SolRUtils.getSolRServer(solrCoreUrl);
    if (server == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Solr core : " + solrCoreUrl + " not reachable");
    }

    SolrQuery query = new SolrQuery(MetacatalogField.IDENTIFIER.getField() + ":\"" + urn + "\"");

    try {
      QueryResponse rsp = server.query(query);
      SolrDocumentList listDoc = rsp.getResults();
      if (listDoc == null || listDoc.getNumFound() != 1) {
        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No value or multiple value found for the given urn "
            + ((listDoc == null) ? 0 : listDoc.getNumFound()));
      }
      SolrDocument document = listDoc.get(0);
      String downUrl = (String) document.getFieldValue(serviceFieldName);
      if (downUrl == null || downUrl.isEmpty()) {
        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No download urlfound for the given urn ");
      }
      return downUrl;
    }
    catch (SolrServerException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error while querying solr index", e);
    }
  }

  protected abstract String createServiceUrl(Request request, Response response);

}