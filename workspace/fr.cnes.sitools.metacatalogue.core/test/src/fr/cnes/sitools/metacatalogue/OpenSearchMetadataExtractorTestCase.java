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
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.opensearch.extractor.OpensearchMetadataExtractor;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.model.HarvesterModel;

public class OpenSearchMetadataExtractorTestCase extends AbstractHarvesterTestCase {

  private int nbFieldsExpected = 10;

  @Test
  public void testOpenSearchMetadataExtractor() throws ProcessException, IOException {
    Context context = initContext();
    String filePath = settings.getRootDirectory() + "/" + settings.getString("Tests.RESOURCES_DIRECTORY")
        + "/opensearch/kalideos.json";
    MetadataContainer data = getJsonDataFromFile(filePath);

    HarvesterModel model = createHarvesterModelForTest("kalideos");
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

  private class assertDataClass extends HarvesterStep {

    @Override
    public void execute(MetadataContainer data) throws ProcessException {
      assertNotNull(data);
      assertNotNull(data.getMetadataRecords());
      assertEquals(nbFieldsExpected, data.getMetadataRecords().size());
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
