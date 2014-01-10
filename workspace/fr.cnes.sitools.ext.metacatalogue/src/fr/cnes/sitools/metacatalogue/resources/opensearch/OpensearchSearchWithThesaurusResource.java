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
package fr.cnes.sitools.metacatalogue.resources.opensearch;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.thesaurus.Concept;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

public class OpensearchSearchWithThesaurusResource extends AbstractOpensearchSearchResource {
  @Override
  public void sitoolsDescribe() {
    setName("OpensearchSearchResource");
    setDescription("Opensearch search resource, expose the result as GeoJSON");
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
    Representation repr = null;

    Form query = getRequest().getResourceRef().getQueryAsForm();
    SolrServer server = getSolrServer(getContext());

    // GET THE CONCEPT ASSOCIATED TO THE SEARCH TERMS PARAMETER
    String searchTermParam = query.getFirstValue(OpenSearchQuery.SEARCH_TERMS.getParamName(), null);
    ThesaurusSearcher searcher = null;
    try {
      searcher = getThesaurusSearcher();
      if (searchTermParam != null) {
        List<Concept> concepts = searcher.search(searchTermParam, getLanguage());
        if (concepts.size() > 0) {
          searchTermParam = concepts.get(0).getProperties().get("altLabel").toString();
          query.removeFirst(OpenSearchQuery.SEARCH_TERMS.getParamName());
          query.set(OpenSearchQuery.SEARCH_TERMS.getParamName(), searchTermParam);
        }
        else {
          throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot find concept for prefLabel : '"
              + searchTermParam + "' for language : '" + getLanguage() + "'");
        }
      }
    }
    catch (IOException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot read thesaurus", e);
    }
    
    repr = opensearchQuery(repr, query, server, searcher);

    return repr;
  }

  @Override
  public void describeGet(MethodInfo info) {
    info.setDocumentation("Method to query the metacatalog with opensearch parameters and get geojson as a return");
    info.setIdentifier("opensearch_query");
    // ParameterInfo pic = new ParameterInfo("datasetId", true, "xs:string", ParameterStyle.TEMPLATE,
    // "Identifier of the dataset");
    // info.getRequest().getParameters().add(pic);
    addStandardResponseInfo(info);
    addStandardInternalServerErrorInfo(info);
  }

}
