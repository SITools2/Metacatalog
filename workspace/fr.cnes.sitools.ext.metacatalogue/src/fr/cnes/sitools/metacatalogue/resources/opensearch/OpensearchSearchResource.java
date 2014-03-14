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
package fr.cnes.sitools.metacatalogue.resources.opensearch;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServer;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

public class OpensearchSearchResource extends AbstractOpensearchSearchResource {
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

    ThesaurusSearcher searcher = null;

    try {
      searcher = getThesaurusSearcher();
    }
    catch (IOException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot read thesaurus", e);
    }

    repr = opensearchQuery(repr, query, server, searcher);

    return repr;
  }

}
