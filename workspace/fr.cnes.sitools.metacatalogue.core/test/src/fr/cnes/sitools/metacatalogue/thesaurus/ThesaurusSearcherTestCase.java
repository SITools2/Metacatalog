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
package fr.cnes.sitools.metacatalogue.thesaurus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.cnes.sitools.metacatalogue.AbstractHarvesterTestCase;
import fr.cnes.sitools.thesaurus.Concept;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

public class ThesaurusSearcherTestCase extends AbstractHarvesterTestCase {

  private static String THESAURUS_NAME = "thesaurus/TechniqueDev3.rdf";

  @Test
  public void testThesaurusSearchFromPrefLabelFr() throws IOException {
    String prefLabel = "Réflectance de surface";

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(THESAURUS_NAME);
    List<Concept> concepts = searcher.search(prefLabel, "fr");
    assertNotNull(concepts);
    assertEquals(1, concepts.size());
  }

  @Test
  public void testThesaurusSearchFromPrefLabelEn() throws IOException {
    String prefLabel = "Reflectance";

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(THESAURUS_NAME);
    List<Concept> concepts = searcher.search(prefLabel, "en");
    assertNotNull(concepts);
    assertEquals(1, concepts.size());
  }

  @Test
  public void testThesaurusSearchNarrowersBroaderFr() throws IOException {
    String prefLabel = "Albe*";

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(THESAURUS_NAME);
    List<Concept> concepts = searcher.searchNarrowersBroader(prefLabel, "fr");
    assertNotNull(concepts);
    assertEquals(23, concepts.size());
    assertConceptExists(concepts, "Albedo de surface");

  }

  @Test
  public void testThesaurusSearchNarrowersBroaderEn() throws IOException {
    String prefLabel = "Albe*";

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(THESAURUS_NAME);
    List<Concept> concepts = searcher.searchNarrowersBroader(prefLabel, "en");
    assertNotNull(concepts);
    assertEquals(23, concepts.size());

    assertConceptExists(concepts, "Albedo");
  }

  private void assertConceptExists(List<Concept> concepts, String assertResult) {
    boolean found = false;
    for (Concept concept : concepts) {
      if (concept.getProperties().get("prefLabelNarrower").toString().equals(assertResult)) {
        found = true;
        return;
      }
    }
    assertTrue("CANNOT FIND prefLabelNarrower : " + assertResult, found);
  }

  @Test
  public void testThesaurusSearchConceptByAltLabelEn() throws IOException {
    String altLabelExists = "ALBEDO";
    String altLabelDontExists = "ALBEDODESURFACE";

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(THESAURUS_NAME);
    assertTrue(searcher.conceptExists(altLabelExists));
    assertFalse(searcher.conceptExists(altLabelDontExists));
  }

  @Test
  public void testThesaurusGetAllConcepts() throws IOException {

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(THESAURUS_NAME);
    List<Concept> concepts = searcher.getAllConcepts("fr");
    assertNotNull(concepts);
    assertEquals(209, concepts.size());
  }

  @Test
  public void testThesaurusGetAllConceptsMap() throws IOException {

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(THESAURUS_NAME);
    Map<String, String> map = searcher.getAllConceptsAsMap("fr");
    assertNotNull(map);
    // Il y a des altLabels en double... du coup on a 160 concepts au lieu des 158...
    assertEquals(187, map.keySet().size());
  }
  
  @Test
  public void testThesaurusGetBroader() throws IOException {
    
    String altLabelEn = "ALBEDO";

    // get a concept from its prefLabel
    ThesaurusSearcher searcher = new ThesaurusSearcher(THESAURUS_NAME);
    Concept concept = searcher.getBroader(altLabelEn);
    assertNotNull(concept);
    assertEquals("biogeophysic product", concept.getProperties().get("altLabelBroader"));

  }


}
