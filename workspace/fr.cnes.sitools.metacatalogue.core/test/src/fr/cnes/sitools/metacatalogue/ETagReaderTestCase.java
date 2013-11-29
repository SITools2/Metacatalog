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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.List;

import org.json.JSONException;
import org.junit.Test;

import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.utils.ETagReader;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;

public class ETagReaderTestCase extends AbstractHarvesterTestCase {

  private String polygon = "POLYGON ((0.63474993908497 43.060580878688, 1.7157488382218 43.060580878688, 1.7157488382218 43.759319204217, 0.63474993908497 43.759319204217, 0.63474993908497 43.060580878688))";

  private String polygonToulouse = "POLYGON ((43.618928 1.410316, 43.618928 1.472801, 43.57268 1.472801, 43.57268 1.410316, 43.618928 1.410316))";

  @Test
  public void testETag() throws ProcessException, JSONException, IOException {
    HarvesterSettings settings = (HarvesterSettings) HarvesterSettings.getInstance();

    String etagUrl = "file://" + getTestResourcePath(settings, "etag", "etag.json");

    ETagReader reader = new ETagReader(etagUrl, polygon);
    reader.read();

    // assert Continents
    List<String> countinents = reader.getContinents();
    assertNotNull(countinents);
    assertTrue(countinents.contains("Europe"));

    // assert Country
    List<String> countries = reader.getCountries();
    assertNotNull(countries);
    assertTrue(countries.contains("France"));

    // assert Regions
    List<String> regions = reader.getRegions();
    assertNotNull(regions);
    assertTrue(regions.contains("Languedoc-Roussillon"));
    assertTrue(regions.contains("Midi-Pyrenees"));

    // assert Departments
    List<String> departements = reader.getDepartments();
    assertNotNull(departements);
    assertTrue(departements.contains("Ariege"));
    assertTrue(departements.contains("Aude"));
    assertTrue(departements.contains("Gers"));
    assertTrue(departements.contains("Haute-Garonne"));
    assertTrue(departements.contains("Hautes-Pyrenees"));
    assertTrue(departements.contains("Tarn"));

    // assert Departments
    List<String> cities = reader.getCities();
    assertNull(cities);

  }

}
