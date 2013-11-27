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
package fr.cnes.sitools.metacatalogue.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Localisation;
import fr.cnes.sitools.util.ClientResourceProxy;

/**
 * Utility class to Read response from the Etag service
 * 
 * @author m.gond
 * 
 * @version
 * 
 */
public class ETagReader {
  /** The url to query */
  private String url;
  /** The countries */
  private List<String> countries;
  /** The continents */
  private List<String> continents;
  /** The regions */
  private List<String> regions;
  /** The departments */
  private List<String> departements;
  /** The cities */
  private List<String> cities;

  /**
   * Constructor with etagUrl and geometry as WKT
   * 
   * @param etagUrl
   *          the url to query
   * @param geometry
   *          the geometry to query
   */
  public ETagReader(String etagUrl, String geometry) {
    url = etagUrl.replace("{footprint}", geometry);
  }

  /**
   * Perform the call and read/parse the result
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

      JSONTokener token = new JSONTokener(repr.getReader());
      JSONObject object = new JSONObject(token);
      JSONArray features = object.getJSONArray("features");
      if (features.length() == 1) {
        JSONObject feature = features.getJSONObject(0);
        JSONObject properties = feature.getJSONObject("properties");
        JSONObject political = properties.getJSONObject("political");

        continents = parseJSONString(political, "continents");
        countries = parseJSONString(political, "countries");
        regions = parseJSONString(political, "regions");
        departements = parseJSONString(political, "departements");
        cities = parseJSONString(political, "cities");
      }
      else {
        throw new ProcessException("Invalid JSON response from Etag");
      }
    }
    catch (JSONException e) {
      return;
    }
    finally {
      if (repr != null) {
        repr.exhaust();
      }
    }

  }

  /**
   * Get the list of continents
   * 
   * @return the list of continents
   */
  public List<String> getContinents() {
    return continents;

  }

  /**
   * Get the list of countries
   * 
   * @return the list of countries
   */
  public List<String> getCountries() {
    return countries;

  }

  /**
   * Get the list of regions
   * 
   * @return the list of regions
   */
  public List<String> getRegions() {
    return regions;
  }

  /**
   * Get the list of departements
   * 
   * @return the list of departements
   */
  public List<String> getDepartments() {
    return departements;
  }

  /**
   * Get the list of cities
   * 
   * @return the list of cities
   */
  public List<String> getCities() {
    return cities;

  }

  public Localisation getLocalisation() {
    Localisation location = new Localisation();
    location.setContinents(continents);
    location.setCountries(countries);
    location.setRegions(regions);
    location.setDepartments(departements);
    location.setCities(cities);
    return location;
  }

  /**
   * Get the list of values from the given object with the given key
   * 
   * @param object
   *          the JSONObject to parse
   * @param key
   *          the key to search for
   * @return the list of values or null if the key is not found
   */
  private List<String> parseJSONString(JSONObject object, String key) {

    try {
      String values = object.getString(key);
      return parseString(values);
    }
    catch (JSONException e) {
      return null;
    }

  }

  /**
   * Split the given string by ; and return a List
   * 
   * @param string
   *          the string to split
   * @return the list of values
   */
  private List<String> parseString(String string) {
    return Arrays.asList(string.split(";"));
  }

}
