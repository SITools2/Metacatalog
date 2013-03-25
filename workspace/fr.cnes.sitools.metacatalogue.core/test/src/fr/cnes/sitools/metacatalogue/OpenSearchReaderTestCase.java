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

import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.Metadata;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.opensearch.reader.OpensearchReader;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.HarvesterSource;

public class OpenSearchReaderTestCase {

  /** The source url */
  private String sourceUrl = "http://sitools.akka.eu:8182/sitools/datastorage/user/echange-cnes/spirit/spirit.json";

  @Test
  public void testOpenSearchReader() throws ProcessException {
    Context context = new Context();
    HarvestStatus result = new HarvestStatus();
    context.getAttributes().put("RESULT", result);
    Metadata data = null;
    HarvesterModel model = createHarvesterModelForTest("spirit_for_test");
    HarvesterStep reader = new OpensearchReader(model, context);
    reader.setNext(new assertDataClass());
    reader.execute(data);

  }

  private HarvesterModel createHarvesterModelForTest(String id) {
    HarvesterModel model = new HarvesterModel();
    model.setId(id);
    model.setCatalogType("opensearch");
    HarvesterSource source = new HarvesterSource();
    source.setUrl(sourceUrl);
    model.setSource(source);
    return model;
  }

  private class assertDataClass extends HarvesterStep {

    @Override
    public void execute(Metadata data) throws ProcessException {
      assertNotNull(data);
      assertNotNull(data.getJsonData());
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
