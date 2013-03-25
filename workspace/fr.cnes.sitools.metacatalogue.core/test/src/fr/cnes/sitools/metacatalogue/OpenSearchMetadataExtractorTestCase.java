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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.Metadata;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.opensearch.extractor.OpensearchMetadataExtractor;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;

public class OpenSearchMetadataExtractorTestCase {

  private int nbFieldsExpected = 102;

  @Test
  public void testOpenSearchMetadataExtractor() throws ProcessException, IOException {
    Context context = new Context();
    HarvestStatus result = new HarvestStatus();
    context.getAttributes().put("RESULT", result);
    Metadata data = getJsonDataFromFile();
    HarvesterModel model = createHarvesterModelForTest("spirit_for_test");
    HarvesterStep reader = new OpensearchMetadataExtractor(model, context);
    reader.setNext(new assertDataClass());
    reader.execute(data);

  }

  private HarvesterModel createHarvesterModelForTest(String id) {
    HarvesterModel model = new HarvesterModel();
    model.setId(id);
    model.setCatalogType("opensearch");
    return model;
  }

  private Metadata getJsonDataFromFile() throws IOException {
    HarvesterSettings settings = HarvesterSettings.getInstance();
    String filePath = settings.getRootDirectory() + "/" + settings.getString("TEST_RESOURCES_DIRECTORY")
        + "/opensearch/spirit.json";
    BufferedReader reader = new BufferedReader(new FileReader(filePath));
    StringBuffer fileData = new StringBuffer(1000);
    char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = reader.read(buf)) != -1) {
      String readData = String.valueOf(buf, 0, numRead);
      fileData.append(readData);
      buf = new char[1024];
    }
    reader.close();
    Metadata data = new Metadata();
    data.setJsonData(fileData.toString());
    return data;

  }

  private class assertDataClass extends HarvesterStep {

    @Override
    public void execute(Metadata data) throws ProcessException {
      assertNotNull(data);
      assertNotNull(data.getFields());
      assertEquals(nbFieldsExpected, data.getFields().size());
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
