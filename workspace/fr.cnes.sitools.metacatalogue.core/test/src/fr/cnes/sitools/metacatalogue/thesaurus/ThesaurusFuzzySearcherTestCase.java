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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.AbstractHarvesterTestCase;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.thesaurus.Concept;
import fr.cnes.sitools.thesaurus.fuzzy.ThesaurusFuzzySearcher;

public class ThesaurusFuzzySearcherTestCase extends AbstractHarvesterTestCase {

  @Test
  public void testThesaurusFuzzySearcher() throws SolrServerException, IOException, InterruptedException {

    HarvesterSettings settings = (HarvesterSettings) HarvesterSettings.getInstance();
    SolrServer server = SolRUtils.getEmbeddedSolRServer(settings.getStoreDIR("Tests.SOLR_HOME"), "solr.xml",
        "thesaurus");

    ThesaurusTestUtils utils = new ThesaurusTestUtils();
    utils.cleanThesaurus(server);
    utils.indexThesaurus(server);

    Context context = new Context();
    context.getAttributes().put(ContextAttributes.INDEXER_SERVER, server);

    ThesaurusFuzzySearcher searcher = new ThesaurusFuzzySearcher();
    List<Concept> concepts = searcher.search("Alb*", context);

    assertNotNull(concepts);
    assertEquals(1, concepts.size());

    System.out.println(concepts.get(0).toString());

  }

}
