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
package fr.cnes.sitools.metacatalogue.resources.suggest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;

import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.resources.AbstractOpensearchQueryResource;
import fr.cnes.sitools.thesaurus.Concept;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

public class OpensearchSuggestResource extends AbstractOpensearchQueryResource {

  @Override
  public void sitoolsDescribe() {
    setName("OpensearchSuggestResource");
    setDescription("Get suggestions");
  }

  @Get
  public List<SuggestDTO> suggest(Variant variant) {
    String query = getRequest().getResourceRef().getQueryAsForm().getFirstValue("q");
    if (query == null || query.isEmpty()) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No suggestion parameter");
      return null;
    }

    try {
      ThesaurusSearcher searcher = new ThesaurusSearcher(thesaurusName);
      List<SuggestDTO> suggests = new ArrayList<SuggestDTO>();

      List<Concept> concepts = searcher.searchNarrowersBroader(query + "*");
      for (Concept concept : concepts) {
        SuggestDTO suggestDTO = new SuggestDTO();
        suggestDTO.setSuggestion(concept.getProperties().get("prefLabelNarrower").toString());
        suggests.add(suggestDTO);
      }

      // get suggestion number in the metacatalogue then
      if (suggests.size() > 0) {
        SolrServer server = SolRUtils.getSolRServer(solrCoreUrl);
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setRequestHandler("/terms");
        solrQuery.setTerms(true);
        solrQuery.setTermsLimit(-1);
        solrQuery.addTermsField("concepts");

        QueryResponse rsp;
        try {
          rsp = server.query(solrQuery);
          TermsResponse termsResponse = rsp.getTermsResponse();
          List<TermsResponse.Term> terms = termsResponse.getTerms("concepts");
          Map<String, Long> map = createMapFromTerms(terms);
          for (SuggestDTO suggest : suggests) {
            Long nb = map.get(suggest.getSuggestion());
            if (nb == null) {  
              suggest.setNb(0);
            }
            else {
              suggest.setNb(nb);
            }
          }
        }
        catch (SolrServerException e) {
          getLogger().warning("Cannot access Solr server at url : " + solrCoreUrl + " not suggestion number returned");
        }
      }

      return suggests;
    }
    catch (IOException e) {
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Cannot read Thesaurs : " + thesaurusName);
      return null;
    }

  }

  private Map<String, Long> createMapFromTerms(List<Term> terms) {
    Map<String, Long> map = new HashMap<String, Long>();
    for (Term term : terms) {
      map.put(term.getTerm(), term.getFrequency());
    }
    return map;
  }
}
