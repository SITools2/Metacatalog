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
package fr.cnes.sitools.metacatalogue.thesaurus;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;

import fr.cnes.sitools.metacatalogue.AbstractHarvesterTestCase;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;

public class ThesaurusIndexerTestCase extends AbstractHarvesterTestCase {

  @Test
  public void testThesaurusIndexer() throws SolrServerException, IOException, InterruptedException {

    HarvesterSettings settings = (HarvesterSettings) HarvesterSettings.getInstance();
    SolrServer server = SolRUtils.getEmbeddedSolRServer(settings.getStoreDIR("Tests.SOLR_HOME"), "solr.xml",
        "thesaurus");
    
    ThesaurusTestUtils utils = new ThesaurusTestUtils();
    utils.cleanThesaurus(server);
    utils.indexThesaurus(server);

  }

}
