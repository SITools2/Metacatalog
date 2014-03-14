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

import java.io.IOException;

import org.junit.Test;

import fr.cnes.sitools.metacatalogue.utils.ProductCategoryExtractorUtil;

public class ProductCategoryExtractorTestCase {

  private static String THESAURUS_NAME = "thesaurus/TechniqueDev3.rdf";

  @Test
  public void testALBEDO() throws IOException {
    String product = "ALBEDO";

    ProductCategoryExtractorUtil extractor = new ProductCategoryExtractorUtil(THESAURUS_NAME);
    String category = extractor.extractCategory(product);

    assertEquals("biogeophysic product", category);
  }
  
  @Test
  public void testLANDCOVER() throws IOException {
    String product = "LANDCOVER";

    ProductCategoryExtractorUtil extractor = new ProductCategoryExtractorUtil(THESAURUS_NAME);
    String category = extractor.extractCategory(product);

    assertEquals("thematic product", category);
  }

}
