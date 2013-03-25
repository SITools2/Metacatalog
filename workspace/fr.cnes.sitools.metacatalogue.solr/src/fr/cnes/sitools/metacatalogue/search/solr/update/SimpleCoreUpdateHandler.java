/*******************************************************************************
 * Copyright 2011 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.sitools.metacatalogue.search.solr.update;

import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.solr.core.SolrCore;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.update.DeleteUpdateCommand;
import org.apache.solr.util.RefCounted;

public class SimpleCoreUpdateHandler extends MetacatalogCoreUpdateHandler {

  public SimpleCoreUpdateHandler(SolrCore core) throws IOException {
    super(core);    
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.solr.update.DirectUpdateHandler2#delete(org.apache.solr.update.DeleteUpdateCommand)
   */
  @Override
  public void delete(DeleteUpdateCommand cmd) throws IOException {
    logger.info("delete: {}", cmd);
    super.delete(cmd);
  
    // delete into the spatial index
    try {
      String uuid = cmd.id;
      geometryTool.removeMetadata(uuid);
    }
    catch (Exception e) {
      reinitializeDataStore();
      throw new IOException(e);
    }
    
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.solr.update.DirectUpdateHandler2#deleteByQuery(org.apache.solr.update.DeleteUpdateCommand)
   */
  @Override
  @SuppressWarnings("rawtypes")
  public void deleteByQuery(DeleteUpdateCommand cmd) throws IOException {
    logger.info("Delete by query: {}", cmd);
    try {
      // Get Query
      Query q = QueryParsing.parseQuery(cmd.query, schema);
  
      if (MatchAllDocsQuery.class.equals(q.getClass())) {
        geometryTool.removeMetadataByCore(solrCoreName);
        super.deleteByQuery(cmd);
      }
      else {
        // Retrieve document to delete
        Future[] waitSearcher = new Future[1];
        RefCounted<SolrIndexSearcher> searcher = core.getSearcher(true, true, waitSearcher);
        SolrIndexSearcher indexSearcher = searcher.get();
        DocList docList = indexSearcher.getDocList(q, (Query) null, null, 0, Integer.MAX_VALUE);
  
        // Delete all document
        int docId;
        String uuid;
        Document luceneDoc;
        for (DocIterator iterator = docList.iterator(); iterator.hasNext();) {
          docId = iterator.nextDoc();
          luceneDoc = indexSearcher.doc(docId);
          uuid = luceneDoc.get(FIELD_UUID);
          geometryTool.removeMetadata(uuid, solrCoreName);
        }
      }
    }
    catch (Exception e) {
      reinitializeDataStore();
      throw new IOException(e);
    }
    super.deleteByQuery(cmd);
  }

}
