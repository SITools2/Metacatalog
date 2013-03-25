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
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.Metadata;
import fr.cnes.sitools.metacatalogue.csw.extractor.CswMetadataExtractor;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Field;
import fr.cnes.sitools.metacatalogue.model.Fields;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.model.HarvesterModel;

public class CSWMetadataExtractorTest {

  private String filePath = "resources/geosud_csw_data.xml";

  @Test
  public void test() throws IOException, JDOMException, ProcessException {

    File file = new File(filePath);
    FileInputStream fis = new FileInputStream(file);

    Element root = Xml.loadStream(fis);
    Metadata data = new Metadata();

    // get the search results and the number of records
    Element searchResults = root.getChild("SearchResults",
        Namespace.getNamespace("http://www.opengis.net/cat/csw/2.0.2"));

    data.setXmlData(searchResults);

    Context context = new Context();
    HarvestStatus status = new HarvestStatus();
    context.getAttributes().put("STATUS", status);
    HarvesterModel conf = createHarvesterModelForTest("geosud_test");

    CswMetadataExtractor metadataExtractor = new CswMetadataExtractor(conf, context);
    metadataExtractor.setNext(new assertSolrXML());

    metadataExtractor.execute(data);
  }

  private HarvesterModel createHarvesterModelForTest(String id) {
    HarvesterModel model = new HarvesterModel();
    model.setId(id);
    model.setCatalogType("csw-iso19139-2-Geosud");
    return model;
  }

  private class assertSolrXML extends HarvesterStep {

    @Override
    public void execute(Metadata data) throws ProcessException {
      assertNotNull(data);
      assertNotNull(data.getFields());
      assertEquals(1, data.getFields().size());

      Fields fields = data.getFields().get(0);

      for (Field field : fields.getList()) {
        String name = field.getName();
        Object value = field.getValue();
        System.out.println("NAME : " + name + " value : " + value);
      }

      assertNotNull(fields.get("quicklook"));
      String quicklook = (String) fields.get("quicklook");
      assertFalse(quicklook.isEmpty());
    }

    @Override
    public void end() {
    }

    @Override
    public CheckStepsInformation check() {
      return new CheckStepsInformation(false);
    }

  }

}
