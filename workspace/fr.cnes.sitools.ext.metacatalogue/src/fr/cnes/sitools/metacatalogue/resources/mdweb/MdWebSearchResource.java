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
package fr.cnes.sitools.metacatalogue.resources.mdweb;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.metacatalogue.representation.GeoJsonMDEORepresentation;
import fr.cnes.sitools.metacatalogue.resources.AbstractSearchResource;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

public class MdWebSearchResource extends AbstractSearchResource {

  @Override
  public void sitoolsDescribe() {
    setName("MdWebSearchResource");
    setDescription("Specific search API for MDWeb client");
  }

  @Override
  public void describeGet(MethodInfo info) {
    info.setDocumentation("Method to query the metacatalog with SolR parameters and get geojson as a return");
    info.setIdentifier("mdweb_query");
    // ParameterInfo pic = new ParameterInfo("datasetId", true, "xs:string", ParameterStyle.TEMPLATE,
    // "Identifier of the dataset");
    // info.getRequest().getParameters().add(pic);
    addStandardResponseInfo(info);
    addStandardInternalServerErrorInfo(info);
  }

  /**
   * Actions on PUT
   * 
   * @param variant
   *          MediaType of response
   * @return Representation response
   */
  @Get
  public Representation get(Variant variant) {
    NamedList<String> list = new NamedList<String>();

    Form query = getRequest().getResourceRef().getQueryAsForm();
    for (Parameter parameter : query) {
      list.add(parameter.getName(), parameter.getValue());
    }

    SolrServer server = getSolrServer();

    ThesaurusSearcher searcher = null;

    try {
      searcher = getThesaurusSearcher();
    }
    catch (IOException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot read thesaurus", e);
    }
    //
    // repr = opensearchQuery(repr, query, server, searcher);

    SolrParams solrQuery = SolrQuery.toSolrParams(list);

    try {
      getLogger().info("Query : " + solrQuery.toString());
      QueryResponse rsp = server.query(solrQuery);
      
      boolean isAuthenticated = getClientInfo().isAuthenticated();
      SitoolsSettings settings = getSettings();
      String applicationBaseUrl = settings.getPublicHostDomain() + application.getAttachementRef();
      
      return new GeoJsonMDEORepresentation(rsp, isAuthenticated, applicationBaseUrl,
          searcher.getAllConceptsAsMap(getLanguage()), thesaurusFacetFields);
    }
    catch (SolrServerException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error while querying solr index", e);
    }
  }

}
