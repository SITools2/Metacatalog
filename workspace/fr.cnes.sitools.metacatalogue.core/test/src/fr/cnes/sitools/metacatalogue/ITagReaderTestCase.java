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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.itag.ItagLocalization;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.metacatalogue.utils.ITagReader;
import fr.cnes.sitools.metacatalogue.utils.Localization;

public class ITagReaderTestCase extends AbstractHarvesterTestCase {

  private String polygon = "POLYGON ((0.63474993908497 43.060580878688, 1.7157488382218 43.060580878688, 1.7157488382218 43.759319204217, 0.63474993908497 43.759319204217, 0.63474993908497 43.060580878688))";

  @Test
  public void testETag() throws ProcessException, IOException {
    HarvesterSettings settings = (HarvesterSettings) HarvesterSettings.getInstance();

    String itagUrl = "file://" + getTestResourcePath(settings, "itag", "itag.json");

    ITagReader reader = new ITagReader(itagUrl, polygon);
    reader.read();

    ItagLocalization loc = reader.getLocalisation();

    // assert Continents
    List<Localization> continents = loc.getContinents();
    assertNotNull(continents);
    assertEquals(1, continents.size());
    assertEquals("Europe", continents.get(0).getName());

    for (Localization continent : continents) {

      // assert Country
      List<Localization> countries = continent.getChildren();
      assertNotNull(countries);
      assertEquals(1, countries.size());
      assertEquals("France", countries.get(0).getName());

      for (Localization country : countries) {

        // assert Regions
        List<Localization> regions = country.getChildren();
        assertNotNull(regions);
        assertEquals(2, regions.size());
        assertEquals("Midi-Pyrenees", regions.get(0).getName());
        assertEquals("Languedoc-Roussillon", regions.get(1).getName());

        for (Localization region : regions) {

          List<Localization> departements = region.getChildren();
          assertNotNull(departements);

          if ("Midi-Pyrenees".equals(region.getName())) {
            // assert Departments
            assertEquals(5, departements.size());
            assertEquals("Haute-Garonne", departements.get(0).getName());
            assertEquals("Gers", departements.get(1).getName());
            assertEquals("Ariege", departements.get(2).getName());
            assertEquals("Tarn", departements.get(3).getName());
            assertEquals("Hautes-Pyrenees", departements.get(4).getName());
          }
          else {
            assertEquals("Aude", departements.get(0).getName());
          }

          for (Localization departement : departements) {
            // assert Departments
            List<Localization> cities = departement.getChildren();
            assertTrue(cities.isEmpty());
          }
        }
      }
    }

  }

}
