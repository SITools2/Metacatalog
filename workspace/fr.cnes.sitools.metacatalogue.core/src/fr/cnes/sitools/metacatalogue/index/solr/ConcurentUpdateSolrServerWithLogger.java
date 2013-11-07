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
package fr.cnes.sitools.metacatalogue.index.solr;

import java.util.logging.Level;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer;
import org.restlet.Context;

public class ConcurentUpdateSolrServerWithLogger extends ConcurrentUpdateSolrServer {

  /** serialVersionUID */
  private static final long serialVersionUID = 1L;

  private Context context;

  public ConcurentUpdateSolrServerWithLogger(String solrServerUrl, HttpClient client, int queueSize, int threadCount) {
    super(solrServerUrl, client, queueSize, threadCount);
  }

  public ConcurentUpdateSolrServerWithLogger(String solrServerUrl, int queueSize, int threadCount) {
    super(solrServerUrl, queueSize, threadCount);
  }

  public ConcurentUpdateSolrServerWithLogger(String solrServerUrl, int queueSize, int threadCount, Context context) {
    super(solrServerUrl, queueSize, threadCount);
    this.context = context;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrServer# handleError(java.lang.Throwable)
   */
  @Override
  public void handleError(Throwable ex) {
    super.handleError(ex);
    this.context.getLogger().log(Level.WARNING, ex.getLocalizedMessage(), ex);
  }

}
