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
package fr.cnes.sitools.metacatalogue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.jdom.JDOMException;
import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.Metadata;
import fr.cnes.sitools.metacatalogue.csw.extractor.CswMetadataExtractor;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Field;
import fr.cnes.sitools.metacatalogue.model.Fields;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.model.HarvesterModel;

public class CSWMetadataExtractorTest extends AbstractHarvesterTestCase {

  @Test
  public void test() throws IOException, JDOMException, ProcessException {
    String filePath = settings.getRootDirectory() + "/" + settings.getString("Tests.RESOURCES_DIRECTORY")
        + "/csw/geosud.xml";

    Metadata data = getXMLDataFromFile(filePath);

    Context context = initContext();

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
