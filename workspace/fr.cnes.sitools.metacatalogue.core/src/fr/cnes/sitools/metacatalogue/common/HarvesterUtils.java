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
package fr.cnes.sitools.metacatalogue.common;

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.CoreAdminRequest;

import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;

public final class HarvesterUtils {

  private HarvesterUtils() {
  }

  public static void merge(Collection<HarvesterModel> models) throws SolrServerException, IOException {
    HarvesterSettings settings = HarvesterSettings.getInstance();
    Logger logger = settings.getLogger();

    String metacatalogueIndexerUrl = HarvesterSettings.getInstance().getString("METACATALOG_INDEXER_URL");
    String metacatalogueIndexerCoreName = HarvesterSettings.getInstance().getString("METACATALOG_INDEXER_CORE_NAME");

    String mergeServerUrl = metacatalogueIndexerUrl + "/" + metacatalogueIndexerCoreName;
    SolrServer serverMetacatalogue = SolRUtils.getSolRServer(mergeServerUrl);

    String serverUrl = metacatalogueIndexerUrl;
    SolrServer server = SolRUtils.getSolRServerWithoutCheck(serverUrl);

    serverMetacatalogue.deleteByQuery("*:*");
    serverMetacatalogue.commit();
    for (HarvesterModel harvesterModel : models) {
      try {
        String[] cores = { harvesterModel.getId() };
        CoreAdminRequest.mergeIndexes(metacatalogueIndexerCoreName, new String[0], cores, server);
        logger.log(Level.INFO, "Merge index : " + harvesterModel.getId());
      }
      catch (SolrServerException e) {
        logger.log(Level.INFO, "Cannot merge index : " + harvesterModel.getId());
      }
      catch (IOException e) {
        logger.log(Level.INFO, "Cannot merge index : " + harvesterModel.getId());
      }
    }
    serverMetacatalogue.commit();

  }

}
