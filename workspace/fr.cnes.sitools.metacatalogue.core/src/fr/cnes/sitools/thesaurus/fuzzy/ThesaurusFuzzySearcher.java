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
package fr.cnes.sitools.thesaurus.fuzzy;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.restlet.Context;

import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.thesaurus.Concept;

public class ThesaurusFuzzySearcher {

  public List<Concept> search(String query, Context context) {

    List<Concept> concepts = new ArrayList<Concept>();
    SolrServer server = (SolrServer) context.getAttributes().get(ContextAttributes.INDEXER_SERVER);

    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setQuery(query);

    try {
      QueryResponse rsp = server.query(solrQuery);
      SolrDocumentList listDoc = rsp.getResults();
      for (SolrDocument solrDocument : listDoc) {
        concepts.add(parseConcepts(solrDocument));
      }
    }
    catch (SolrServerException e) {
      e.printStackTrace();
      return null;
    }

    return concepts;
  }

  private Concept parseConcepts(SolrDocument solrDocument) {
    Concept concept = new Concept();
    concept.getProperties().putAll(solrDocument);
    return concept;
  }

//  private List<String> toList(Collection<Object> fieldValues) {
//
//    if (fieldValues != null) {
//      List<String> fields = new ArrayList<String>();
//      for (Object object : fieldValues) {
//        fields.add(toString(object));
//      }
//      return fields;
//    }
//
//    return null;
//  }
//
//  private String toString(Object fieldValue) {
//    if (fieldValue != null) {
//      return fieldValue.toString();
//    }
//    return null;
//  }

}
