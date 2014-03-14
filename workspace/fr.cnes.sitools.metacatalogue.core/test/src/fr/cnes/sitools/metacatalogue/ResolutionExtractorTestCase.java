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

import org.junit.Test;

import fr.cnes.sitools.metacatalogue.utils.ResolutionConverter;

public class ResolutionExtractorTestCase {

  @Test
  public void testResolutionExtractor() {
    ResolutionConverter resolutionExtractor = new ResolutionConverter();

    assertEquals("VHR", resolutionExtractor.getResolution(1.0));
    assertEquals("HR", resolutionExtractor.getResolution(20));
    assertEquals("MR", resolutionExtractor.getResolution(50));
    assertEquals("LR", resolutionExtractor.getResolution(250));
    assertEquals(null, resolutionExtractor.getResolution(-1));

  }

}
