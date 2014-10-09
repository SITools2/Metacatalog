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
package fr.cnes.sitools.metacatalogue.thesaurus;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.restlet.Context;

import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.thesaurus.ThesaurusIndexer;
import fr.cnes.sitools.thesaurus.ThesaurusIndexer.IndexerStatus;

public class ThesaurusTestUtils {
  
  
  
  public void cleanThesaurus(SolrServer server) throws SolrServerException, IOException {
    server.deleteByQuery("*:*");
    server.commit();

    assertNone(server);
  }

  public void indexThesaurus(SolrServer server) throws InterruptedException, SolrServerException {

    Context context = new Context();
    context.getAttributes().put(ContextAttributes.INDEXER_SERVER, server);

    ThesaurusIndexer indexer = new ThesaurusIndexer();
    IndexerStatus status = indexer.index(context);

    // assertEquals(IndexerStatus.BUSY, status);
    Thread.sleep(2000);
    status = indexer.waitForCompletion(context);
    assertEquals(IndexerStatus.IDLE, status);

    assertConceptInserted(server);

  }

  private void assertNbRecords(SolrServer server, int nbRecords) throws SolrServerException {
    SolrQuery solrQuery = new SolrQuery();
    solrQuery.setQuery("*:*");
    QueryResponse rsp = server.query(solrQuery);
    SolrDocumentList listDoc = rsp.getResults();

    assertEquals(nbRecords, listDoc.getNumFound());

  }

  private void assertConceptInserted(SolrServer server) throws SolrServerException {
    assertNbRecords(server, 431);
  }

  private void assertNone(SolrServer server) throws SolrServerException {
    assertNbRecords(server, 0);
  }
}
