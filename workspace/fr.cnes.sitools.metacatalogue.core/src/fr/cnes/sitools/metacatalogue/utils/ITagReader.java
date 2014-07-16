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
package fr.cnes.sitools.metacatalogue.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.itag.ItagLocalization;
import fr.cnes.sitools.util.ClientResourceProxy;

/**
 * Utility class to Read response from the Itag service.
 * 
 * @author m.gond
 * @version
 */
public class ITagReader {

  /** The url to query. */
  private String url;

  /** The itag loc. */
  private ItagLocalization itagLoc;

  /**
   * Constructor with etagUrl and geometry as WKT.
   * 
   * @param etagUrl
   *          the url to query
   * @param geometry
   *          the geometry to query
   */
  public ITagReader(String etagUrl, String geometry) {
    url = etagUrl.replace("{footprint}", geometry);
    itagLoc = new ItagLocalization();

  }

  /**
   * Constructor with etagUrl and geometry as WKT.
   * 
   * @param etagUrl
   *          the url to query
   * @param geometry
   *          the geometry to query
   * @param withCities
   *          true to query all cities, false otherwise
   */
  public ITagReader(String etagUrl, String geometry, boolean withCities) {
    this(etagUrl, geometry);
    if (withCities) {
      Reference reference = new Reference(url);
      reference.addQueryParameter("cities", "all");
      url = reference.toString();
    }
  }

  /**
   * Perform the call and read/parse the result.
   * 
   * @throws ProcessException
   *           if there is an error during the call to the Etag service
   * @throws IOException
   *           if there is an error while reading/parsing the JSON result
   */
  public void read() throws ProcessException, IOException {
    Reference ref = new Reference(url);
    ClientResourceProxy client = new ClientResourceProxy(ref, Method.GET);
    ClientResource clientResource = client.getClientResource();
    Representation repr = null;
    try {
      repr = clientResource.get(MediaType.APPLICATION_JSON);
      if (clientResource.getStatus().isError()) {
        throw new ProcessException("Cannot read ETAG", clientResource.getStatus().getThrowable());
      }
      // read the suggest JSON
      ObjectMapper mapper = new ObjectMapper();
      // (note: can also use more specific type, like ArrayNode or ObjectNode!)
      JsonNode object = mapper.readValue(repr.getStream(), JsonNode.class);

      JsonNode features = object.get("features");
      if (features.size() == 1) {
        JsonNode feature = features.get(0);
        JsonNode properties = feature.get("properties");
        JsonNode political = properties.get("political");
        if (political != null && political.size() > 0) {
          JsonNode jsonContinents = political.get("continents");
          processContinent(jsonContinents);
        }
      }
      else {
        throw new ProcessException("Invalid JSON response from Etag");
      }
    }
    finally {
      if (repr != null) {
        repr.exhaust();
      }
    }

  }

  /**
   * Process continent.
   * 
   * @param continents
   *          the continents
   */
  private void processContinent(JsonNode continents) {
    Iterator<Entry<String, JsonNode>> continentsFields = continents.getFields();
    if (continentsFields != null) {
      while (continentsFields.hasNext()) {
        Entry<String, JsonNode> continent = continentsFields.next();
        String continentName = continent.getKey();
        Localization continentLoc = new Localization(continentName, Localization.Type.CONTINENT);
        itagLoc.getContinents().add(continentLoc);

        processCountries(continent.getValue(), continentLoc);
      }
    }
  }

  /**
   * Process countries.
   * 
   * @param countriesRoot
   *          the countries root
   * @param continent
   *          the continent
   */
  private void processCountries(JsonNode countriesRoot, Localization continent) {

    JsonNode countries = countriesRoot.get("countries");
    if (countries != null) {
      for (JsonNode country : countries) {
        try {
          Localization countriesLoc = new Localization(country.get("name").getTextValue(), Localization.Type.COUNTRY,
              country.get("pcover").getDoubleValue());
          continent.getChildren().add(countriesLoc);
          if (countriesLoc.getName().equals("France")) {
            processRegions(country.get("regions"), countriesLoc);
          }
          else {
            processCities(country.get("cities"), countriesLoc);
          }
        }
        catch (Exception e) {
          Engine.getLogger(this.getClass().getName()).log(Level.WARNING, e.getMessage(), e);
          continue;
        }
      }
    }
  }

  /**
   * Process regions.
   * 
   * @param countries
   *          the countries
   * @param countriesLoc
   *          the countries loc
   */
  private void processRegions(JsonNode countries, Localization countriesLoc) {
    Iterator<Entry<String, JsonNode>> regionsFields = countries.getFields();
    if (regionsFields != null) {
      while (regionsFields.hasNext()) {
        Entry<String, JsonNode> region = regionsFields.next();
        Localization regionLoc = new Localization(region.getKey(), Localization.Type.REGION);
        countriesLoc.getChildren().add(regionLoc);

        processDepartments(region.getValue(), regionLoc);
      }
    }
  }

  /**
   * Process departments.
   * 
   * @param departmentsRoot
   *          the departments root
   * @param regionLoc
   *          the region loc
   */
  private void processDepartments(JsonNode departmentsRoot, Localization regionLoc) {
    JsonNode departments = departmentsRoot.get("states");
    if (departments != null) {
      for (JsonNode department : departments) {
        try {
          Localization departementLoc = new Localization(department.get("name").getTextValue(),
              Localization.Type.DEPARTMENT, department.get("pcover").getDoubleValue());
          regionLoc.getChildren().add(departementLoc);
          if (department.get("cities") != null) {
            processCities(department.get("cities"), departementLoc);
          }
        }
        catch (Exception e) {
          Engine.getLogger(this.getClass().getName()).log(Level.WARNING, e.getMessage(), e);
          continue;
        }
      }
    }
  }

  /**
   * Process cities.
   * 
   * @param citiesRoot
   *          the cities root
   * @param parentLoc
   *          the parent loc
   */
  private void processCities(JsonNode citiesRoot, Localization parentLoc) {
    if (citiesRoot != null) {
      for (JsonNode city : citiesRoot) {
        try {
          Localization cityLoc = new Localization(city.getTextValue(), Localization.Type.CITY);
          parentLoc.getChildren().add(cityLoc);
        }
        catch (Exception e) {
          Engine.getLogger(this.getClass().getName()).log(Level.WARNING, e.getMessage(), e);
          continue;
        }
      }
    }

  }

  /**
   * Gets the localisation.
   * 
   * @return the localisation
   */
  public ItagLocalization getLocalisation() {
    return itagLoc;
  }

}
