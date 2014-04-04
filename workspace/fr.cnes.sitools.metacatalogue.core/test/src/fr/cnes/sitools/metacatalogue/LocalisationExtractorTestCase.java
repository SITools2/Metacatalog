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
package fr.cnes.sitools.metacatalogue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.JDOMException;
import org.junit.Test;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.csw.extractor.LocalisationExtractor;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Field;
import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.proxy.ProxySettings;

public class LocalisationExtractorTestCase extends AbstractHarvesterTestCase {

  private String polygon = "POLYGON ((0.63474993908497 43.060580878688, 1.7157488382218 43.060580878688, 1.7157488382218 43.759319204217, 0.63474993908497 43.759319204217, 0.63474993908497 43.060580878688))";

  private String polygonToulouse = "POLYGON ((1.410316 43.618928, 1.472801 43.618928, 1.472801 43.57268, 1.410316 43.57268, 1.410316 43.618928))";

  @Test
  public void test() throws IOException, JDOMException, ProcessException {
    ProxySettings.init();
    // HarvesterSettings settings = HarvesterSettings.getInstance();

    Context context = initContext();

    HarvesterModel conf = createHarvesterModelForTest("kalideos");

    MetadataContainer data = new MetadataContainer();
    List<MetadataRecords> documents = new ArrayList<MetadataRecords>();

    MetadataRecords fields = new MetadataRecords();
    fields.add(MetacatalogField.GEOGRAPHICAL_EXTENT.getField(), polygon);
    fields.add(MetacatalogField._RESOLUTION_DOMAIN.getField(), "HR");

    documents.add(fields);

    data.setMetadataRecords(documents);

    LocalisationExtractor localisationExtractor = new LocalisationExtractor(conf, context);
    localisationExtractor.setNext(new assertLocalisationFields());

    // execute the process
    localisationExtractor.execute(data);
    // simulate the end of the input stream to end the process
    localisationExtractor.end();

  }

  @Test
  public void testWithCities() throws IOException, JDOMException, ProcessException {
    ProxySettings.init();
    // HarvesterSettings settings = HarvesterSettings.getInstance();

    Context context = initContext();

    HarvesterModel conf = createHarvesterModelForTest("kalideos");

    MetadataContainer data = new MetadataContainer();
    List<MetadataRecords> documents = new ArrayList<MetadataRecords>();

    MetadataRecords fields = new MetadataRecords();
    fields.add(MetacatalogField.GEOGRAPHICAL_EXTENT.getField(), polygonToulouse);
    fields.add(MetacatalogField._RESOLUTION_DOMAIN.getField(), "VHR");

    documents.add(fields);

    data.setMetadataRecords(documents);

    LocalisationExtractor localisationExtractor = new LocalisationExtractor(conf, context);
    localisationExtractor.setNext(new assertLocalisationFieldsToulouse());

    // execute the process
    localisationExtractor.execute(data);
    // simulate the end of the input stream to end the process
    localisationExtractor.end();

  }

  private HarvesterModel createHarvesterModelForTest(String id) {
    HarvesterModel model = new HarvesterModel();
    model.setId(id);
    model.setCatalogType("opensearch");
    return model;
  }

  private class assertLocalisationFields extends HarvesterStep {

    @Override
    public void execute(MetadataContainer data) throws ProcessException {
      assertNotNull(data);
      assertNotNull(data.getMetadataRecords());
      assertEquals(1, data.getMetadataRecords().size());

      MetadataRecords fields = data.getMetadataRecords().get(0);
      Map<String, List<String>> localisations = extractLocalisations(fields);

      // assert Country
      List<String> countries = localisations.get(MetacatalogField.COUNTRY.getField());
      assertNotNull(countries);
      assertTrue(countries.contains("France"));

      // assert Regions
      List<String> regions = localisations.get(MetacatalogField.REGION.getField());
      assertNotNull(regions);
      assertTrue(regions.contains("Midi-Pyrenees"));

      // assert Departments
      List<String> departements = localisations.get(MetacatalogField.DEPARTMENT.getField());
      assertNotNull(departements);
      assertTrue(departements.contains("Ariege"));
      assertTrue(departements.contains("Gers"));
      assertTrue(departements.contains("Haute-Garonne"));
      assertTrue(departements.contains("Hautes-Pyrenees"));
      assertTrue(departements.contains("Tarn"));

      // Assert city
      List<String> cities = localisations.get(MetacatalogField.CITY.getField());
      assertNull(cities);

    }

    @Override
    public void end() {
    }

    @Override
    public CheckStepsInformation check() {
      return new CheckStepsInformation(false);
    }

  }

  private class assertLocalisationFieldsToulouse extends HarvesterStep {

    @Override
    public void execute(MetadataContainer data) throws ProcessException {
      assertNotNull(data);
      assertNotNull(data.getMetadataRecords());
      assertEquals(1, data.getMetadataRecords().size());

      MetadataRecords fields = data.getMetadataRecords().get(0);
      Map<String, List<String>> localisations = extractLocalisations(fields);

      // assert Country
      List<String> countries = localisations.get(MetacatalogField.COUNTRY.getField());
      assertNotNull(countries);
      assertTrue(countries.contains("France"));

      // assert Regions
      List<String> regions = localisations.get(MetacatalogField.REGION.getField());
      assertNotNull(regions);
      assertTrue(regions.contains("Midi-Pyrenees"));

      // assert Departments
      List<String> departements = localisations.get(MetacatalogField.DEPARTMENT.getField());
      assertNotNull(departements);
      assertTrue(departements.contains("Haute-Garonne"));

      // Assert city
      List<String> cities = localisations.get(MetacatalogField.CITY.getField());
      assertNotNull(cities);
      assertTrue(cities.contains("Toulouse"));

    }

    @Override
    public void end() {
    }

    @Override
    public CheckStepsInformation check() {
      return new CheckStepsInformation(false);
    }

  }

  private Map<String, List<String>> extractLocalisations(MetadataRecords fields) {
    Map<String, List<String>> locs = new HashMap<String, List<String>>();

    for (Field doc : fields.getList()) {
      if (doc.getName().equals(MetacatalogField.COUNTRY.getField())) {
        addValueToMap(locs, doc);
      }
      if (doc.getName().equals(MetacatalogField.REGION.getField())) {
        addValueToMap(locs, doc);
      }
      if (doc.getName().equals(MetacatalogField.DEPARTMENT.getField())) {
        addValueToMap(locs, doc);
      }
      if (doc.getName().equals(MetacatalogField.CITY.getField())) {
        addValueToMap(locs, doc);
      }

    }

    return locs;
  }

  private void addValueToMap(Map<String, List<String>> locs, Field doc) {
    List<String> values = locs.get(doc.getName());
    if (values == null) {
      values = new ArrayList<String>();
      locs.put(doc.getName(), values);
    }
    values.add(doc.getValue().toString());
  }

}
