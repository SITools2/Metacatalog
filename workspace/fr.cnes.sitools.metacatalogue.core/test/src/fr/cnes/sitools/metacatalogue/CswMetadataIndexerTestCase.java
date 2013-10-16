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
package fr.cnes.sitools.metacatalogue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.jdom.JDOMException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.Metadata;
import fr.cnes.sitools.metacatalogue.csw.extractor.CswMetadataExtractor;
import fr.cnes.sitools.metacatalogue.csw.indexer.CswMetadataIndexer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.index.solr.SolrMetadataIndexer;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.server.ContextAttributes;

public class CswMetadataIndexerTestCase extends AbstractHarvesterTestCase {

  private int nbFieldsExpected = 1;
  private SolrServer server;

  @Before
  public void setupTest() throws Exception {

    HarvesterSettings settings = (HarvesterSettings) HarvesterSettings.getInstance();
    server = SolRUtils.getEmbeddedSolRServer(settings.getStoreDIR("Tests.SOLR_HOME"), "solr.xml", "geosud");
    server.deleteByQuery("*:*");
    server.commit();

  }

  @After
  public void tearDown() {
    if (server != null) {
      server.shutdown();
    }
  }

  @Test
  public void testOpenSearchMetadataExtractor() throws ProcessException, IOException, SolrServerException,
    JDOMException {

    Context context = initContext();
    context.getAttributes().put(ContextAttributes.INDEXER_SERVER, server);

    String filePath = settings.getRootDirectory() + "/" + settings.getString("Tests.RESOURCES_DIRECTORY")
        + "/csw/geosud.xml";

    Metadata data = getXMLDataFromFile(filePath);

    SolrMetadataIndexer solrIndexer = new SolrMetadataIndexer(context);

    HarvesterModel model = createHarvesterModelForTest("geosud");
    HarvesterStep extractor = new CswMetadataExtractor(model, context);
    HarvesterStep indexer = new CswMetadataIndexer(model, context, solrIndexer);

    extractor.setNext(indexer);
    // execute the process
    extractor.execute(data);
    // simulate the end of the input stream to end the process
    extractor.end();
    // assert that the metadata have been inserted
    assertMetadataInserted();

  }

  private void assertMetadataInserted() throws SolrServerException {
    SolrQuery query = new SolrQuery();
    query.setQuery("*:*");

    QueryResponse rsp = server.query(query);
    assertNotNull(rsp);
    SolrDocumentList listDoc = rsp.getResults();
    assertNotNull(listDoc);
    assertEquals(nbFieldsExpected, listDoc.getNumFound());
  }

  private HarvesterModel createHarvesterModelForTest(String id) {
    HarvesterModel model = new HarvesterModel();
    model.setId(id);
    model.setCatalogType("csw-iso19139-2-Geosud");
    return model;
  }

}
