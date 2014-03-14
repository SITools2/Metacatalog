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
package fr.cnes.sitools.thesaurus;

import org.apache.solr.client.solrj.SolrResponse;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.restlet.Context;

import fr.cnes.sitools.server.ContextAttributes;

public class ThesaurusIndexer {

  public enum IndexerStatus {
    BUSY, IDLE, ERROR
  }

  public IndexerStatus index(Context context) {

    SolrServer server = (SolrServer) context.getAttributes().get(ContextAttributes.INDEXER_SERVER);

    try {
      ModifiableSolrParams params = new ModifiableSolrParams();
      params.set("qt", "/dataimport");
      params.set("command", "full-import");
      params.set("clean", "true");
      params.set("commit", "true");
      server.query(params);

      return requestStatus(server);

    }
    catch (SolrServerException e) {
      e.printStackTrace();
      return IndexerStatus.ERROR;
    }
  }

  public IndexerStatus waitForCompletion(Context context) {
    SolrServer server = (SolrServer) context.getAttributes().get(ContextAttributes.INDEXER_SERVER);
    IndexerStatus status = IndexerStatus.ERROR;
    try {
      do {
        status = requestStatus(server);
      } while (status == IndexerStatus.BUSY);
    }
    catch (SolrServerException e) {
      return IndexerStatus.ERROR;
    }
    return status;
  }

  private String getStatus(SolrResponse response) {
    Object statusObj = response.getResponse().get("status");
    if (statusObj != null) {
      String status = statusObj.toString();
      return status;
    }
    else {
      return null;
    }
  }

  private IndexerStatus requestStatus(SolrServer server) throws SolrServerException {
    ModifiableSolrParams params = new ModifiableSolrParams();
    params.set("qt", "/dataimport");
    params.set("command", "status");
    SolrResponse response = server.query(params);
    String statusStr = getStatus(response);
    return parseStatus(statusStr);
  }

  private IndexerStatus parseStatus(String status) {
    if (status == null) {
      return IndexerStatus.ERROR;
    }
    if (status.equals("busy")) {
      return IndexerStatus.BUSY;
    }
    if (status.equals("idle")) {
      return IndexerStatus.IDLE;
    }
    return IndexerStatus.ERROR;
  }

}
