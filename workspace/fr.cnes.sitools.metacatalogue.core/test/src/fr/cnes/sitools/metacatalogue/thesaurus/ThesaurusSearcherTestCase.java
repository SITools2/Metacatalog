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
package fr.cnes.sitools.metacatalogue.thesaurus;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import fr.cnes.sitools.metacatalogue.AbstractHarvesterServerTestCase;
import fr.cnes.sitools.thesaurus.Concept;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

public class ThesaurusSearcherTestCase extends AbstractHarvesterServerTestCase {

  @Test
  public void testThesaurusSearchFromPrefLabel() throws IOException {
    String thesaurusName = "thesaurus/TechniqueDev3.rdf";
    String prefLabel = "Albedo de surface";

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(thesaurusName);
    List<Concept> concepts = searcher.search(prefLabel);
    assertNotNull(concepts);
    assertEquals(1, concepts.size());
    
    System.out.println(concepts.get(0).toString());
  }
  
  
  @Test
  public void testThesaurusSearchNarrowersBroader() throws IOException {
    String thesaurusName = "thesaurus/TechniqueDev3.rdf";
    String prefLabel = "Albe*";
    
    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(thesaurusName);
    List<Concept> concepts = searcher.searchNarrowersBroader(prefLabel);
    assertNotNull(concepts);
    assertEquals(21, concepts.size());
    
    
  }
  
  

}
