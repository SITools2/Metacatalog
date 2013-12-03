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
package fr.cnes.sitools.thesaurus;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ThesaurusSearcher {

  public String SEARCH_BY_PREFLABEL_QUERY;

  public String SEARCH_NARROWERS_BROADERS;

  public String SEARCH_BY_ALTLABEL_QUERY;

  public String GET_ALL_CONCEPTS_QUERY;

  private Model model;

  public ThesaurusSearcher(String thesaurusName) throws IOException {
    model = ModelFactory.createDefaultModel();
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(thesaurusName);
      model.read(inputStream, "", "RDF/XML");

      initializeSparqlRequests();
    }
    finally {
      inputStream.close();
    }

  }

  private void initializeSparqlRequests() {
    ResourceBundle bundle = ResourceBundle.getBundle(this.getClass().getPackage().getName() + ".sparqlRequests");
    for (Field field : this.getClass().getFields()) {
      try {
        field.setAccessible(true);
        field.set(this, bundle.getString(field.getName()));
      }
      catch (IllegalArgumentException e) {
        Logger.getLogger("initializeSparqlRequests").severe(e.getMessage());
      }
      catch (IllegalAccessException e) {
        Logger.getLogger("initializeSparqlRequests").severe(e.getMessage());
      }
    }

  }

  public List<Concept> search(String prefLabel, String language) {
    String queryString = SEARCH_BY_PREFLABEL_QUERY.replace("{prefLabel}", prefLabel);
    queryString = queryString.replace("{lang}", language);
    List<Concept> concepts = executeQuery(queryString);
    return concepts;
  }

  public List<Concept> searchNarrowersBroader(String prefLabel, String language) {
    String queryString = SEARCH_NARROWERS_BROADERS.replace("{prefLabel}", prefLabel);
    queryString = queryString.replace("{lang}", language);
    List<Concept> concepts = executeQuery(queryString);
    return concepts;
  }

  public List<Concept> executeQuery(String queryString) {
    List<Concept> concepts = new ArrayList<Concept>();
    Query query = QueryFactory.create(queryString);
    QueryExecution qexec = QueryExecutionFactory.create(query, model);
    ResultSet results = qexec.execSelect();
    for (; results.hasNext();) {
      QuerySolution soln = results.next();
      Iterator<String> it = soln.varNames();
      Concept concept = new Concept();
      for (; it.hasNext();) {
        String varName = it.next();
        concept.getProperties().put(varName, soln.getLiteral(varName).getString());
      }
      concepts.add(concept);
    }
    return concepts;
  }

  public boolean conceptExists(String altLabelExists) {
    String queryString = SEARCH_BY_ALTLABEL_QUERY.replace("{altLabel}", altLabelExists);
    List<Concept> concepts = executeQuery(queryString);
    return (concepts != null && concepts.size() >= 1);
  }

  public List<Concept> getAllConcepts(String language) {
    String queryString = GET_ALL_CONCEPTS_QUERY.replace("{lang}", language);
    return executeQuery(queryString);
  }

  public Map<String, String> getAllConceptsAsMap(String language) {
    Map<String, String> out = new HashMap<String, String>();
    List<Concept> concepts = getAllConcepts(language);
    if (concepts != null) {
      for (Concept concept : concepts) {
        Object altLabelEn = concept.getProperties().get("altLabelEn");
        Object prefLabel = concept.getProperties().get("prefLabel");
        if (altLabelEn != null && prefLabel != null) {
          out.put(altLabelEn.toString().toLowerCase(), prefLabel.toString());
        }
      }
    }
    return out;
  }

}
