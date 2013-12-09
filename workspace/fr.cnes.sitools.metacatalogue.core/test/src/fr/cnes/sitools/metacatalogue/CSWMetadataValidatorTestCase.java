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

import org.jdom.Element;
import org.jdom.JDOMException;
import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.csw.extractor.CswMetadataExtractor;
import fr.cnes.sitools.metacatalogue.csw.extractor.LocalisationExtractor;
import fr.cnes.sitools.metacatalogue.csw.extractor.ResolutionExtractor;
import fr.cnes.sitools.metacatalogue.csw.validator.CswMetadataValidator;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.model.HarvesterModel;


/**
 * CSWMetadataValidatorTestCase
 * 
 * @author tx.chevallier
 * @project fr.cnes.sitools.metacatalogue.core
 * @version 
 */
public class CSWMetadataValidatorTestCase extends AbstractHarvesterTestCase {

  private int nbFieldsExpected;
  
  /**
   * test
   * @throws IOException
   * @throws JDOMException
   * @throws ProcessException
   */
  @Test
  public void test() throws IOException, JDOMException, ProcessException {
    
    String filePath = settings.getRootDirectory() + "/" + settings.getString("Tests.RESOURCES_DIRECTORY")
        + "/csw/geosud-new.xml";

    MetadataContainer data = getXMLDataFromFile(filePath);
    Element xmldata = data.getXmlData();
    
    nbFieldsExpected = Integer.parseInt(xmldata.getAttributeValue("numberOfRecordsReturned"));

    Context context = initContext();

    HarvesterModel conf = createHarvesterModelForTest("geosud_test");

    CswMetadataExtractor extractor = new CswMetadataExtractor(conf, context);
    ResolutionExtractor resolution = new ResolutionExtractor(conf, context);
    CswMetadataValidator validator = new CswMetadataValidator(conf, context);
    
    extractor.setNext(resolution);
    resolution.setNext(validator);
    validator.setNext(new assertDataClass());
    
    extractor.execute(data);
    
  }

  private HarvesterModel createHarvesterModelForTest(String id) {
    HarvesterModel model = new HarvesterModel();
    model.setId(id);
    model.setCatalogType("csw-iso19139-2-Geosud");
    return model;
  }


  /**
   * assertDataClass
   * @project fr.cnes.sitools.metacatalogue.core
   * @version 
   *
   */
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
