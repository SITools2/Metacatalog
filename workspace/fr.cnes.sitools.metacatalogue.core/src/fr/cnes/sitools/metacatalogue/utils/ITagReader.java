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
import fr.cnes.sitools.metacatalogue.model.itag.ItagLocalization;
import fr.cnes.sitools.util.ClientResourceProxy;

/**
 * Utility class to Read response from the Itag service
 * 
 * @author m.gond
 * 
 * @version
 * 
 */
public class ITagReader {
  /** The url to query */
  private String url;
  private ItagLocalization itagLoc;

  /**
   * Constructor with etagUrl and geometry as WKT
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
   * Constructor with etagUrl and geometry as WKT
   * 
   * @param etagUrl
   *          the url to query
   * @param geometry
   *          the geometry to query
   * @param withCities
   *          true to query all cities, false otherwise
   * 
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
        if (political != null) {
          JSONObject jsonContinents = political.getJSONObject("continents");
          processContinent(jsonContinents);
        }
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

  private void processContinent(JSONObject continents) {

    Iterator keys = continents.keys();
    while (keys.hasNext()) {
      String continentName = keys.next().toString();
      Localization continentLoc = new Localization(continentName, Localization.Type.CONTINENT);
      itagLoc.getContinents().add(continentLoc);

      try {
        JSONObject continentJsonObject = continents.getJSONObject(continentName);
        processCountries(continentJsonObject, continentLoc);
      }
      catch (JSONException e) {
        continue;
      }
    }
  }

  private void processCountries(JSONObject countriesRoot, Localization continent) {

    try {
      JSONArray countries = countriesRoot.getJSONArray("countries");
      for (int i = 0; i < countries.length(); i++) {
        try {
          JSONObject country = countries.getJSONObject(i);
          Localization countriesLoc = new Localization(country.getString("name"), Localization.Type.COUNTRY,
              country.getDouble("pcover"));
          continent.getChildren().add(countriesLoc);
          processRegions(country.getJSONObject("regions"), countriesLoc);
          // processCities(country, countriesLoc);
        }
        catch (Exception e) {
          continue;
        }
      }
    }
    catch (JSONException e) {
      return;
    }

  }

  // private void processCities(JSONObject citiesRoot, Localization countriesLoc) {
  // try {
  // JSONArray cities = citiesRoot.getJSONArray("cities");
  // }
  // catch (JSONException e) {
  // }
  //
  // }

  private void processRegions(JSONObject jsonObject, Localization countriesLoc) {
    Iterator keys = jsonObject.keys();
    while (keys.hasNext()) {
      String regionName = keys.next().toString();
      Localization regionLoc = new Localization(regionName, Localization.Type.REGION);
      countriesLoc.getChildren().add(regionLoc);

      try {
        JSONObject regionJSONObject = jsonObject.getJSONObject(regionName);
        processDepartments(regionJSONObject, regionLoc);
      }
      catch (JSONException e) {
        continue;
      }
    }
  }

  private void processDepartments(JSONObject departmentsRoot, Localization regionLoc) {
    try {
      JSONArray departments = departmentsRoot.getJSONArray("departements");
      for (int i = 0; i < departments.length(); i++) {
        try {
          JSONObject department = departments.getJSONObject(i);
          Localization countriesLoc = new Localization(department.getString("name"), Localization.Type.DEPARTMENT,
              department.getDouble("pcover"));
          regionLoc.getChildren().add(countriesLoc);
        }
        catch (Exception e) {
          continue;
        }
        // processCities(department.getJSONObject("cities"), countriesLoc);
      }
    }
    catch (JSONException e) {
      return;
    }
  }

  public ItagLocalization getLocalisation() {
    return itagLoc;
  }

}
