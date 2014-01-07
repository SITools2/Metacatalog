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
package fr.cnes.sitools.metacatalogue.opensearch.extractor;

import java.io.Reader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.geojson.geom.GeometryJSON;
import org.restlet.Context;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.server.ContextAttributes;

/**
 * Extract the geometry from a json geometry string and add a new Field to the Fields collection
 * 
 * @author m.gond
 * 
 */
public class OpensearchGeometryExtractor {
  /**
   * Extract the geometry from a json geometry string and add a new Field to the Fields collection given
   * 
   * @param geometryJson
   *          the json geometry element
   * @param fields
   *          the list of fields
   * @param context
   *          the Context
   * @return the list of fields with the added geometry
   * @throws Exception
   *           if there is an error while extracting the geometry
   */
  public MetadataRecords extractGeometry(String geometryJson, MetadataRecords fields, Context context) throws Exception {

    Geometry geometry = extractGeometry(geometryJson, context);

    if (geometry != null) {

      if (geometry.getCoordinates().length > 2 && !isCounterClockWise(geometry)) {
        geometry = geometry.reverse();
      }

      WKTWriter wktWriter = new WKTWriter();
      String geo = wktWriter.writeFormatted(geometry);
      fields.add(MetacatalogField.GEOGRAPHICAL_EXTENT.getField(), geo);
      // Point point = geometry.getInteriorPoint();
      // String geographie = wktWriter.write(point);
      // fields.add("_interiorPoint", geographie);
    }

    return fields;

  }

  /**
   * Check if the geometry is counterClockWise
   * 
   * @param geometry
   *          the Geometry
   * @return true if the geometry is counterClockWise, false otherwise
   */
  private boolean isCounterClockWise(Geometry geometry) {
    return CGAlgorithms.isCCW(geometry.getCoordinates());
  }

  /**
   * Extract geometry.
   * 
   * @param geometryJson
   *          the String representing the json geometry element
   * @return the geometry
   * @throws Exception
   *           the exception
   */
  private Geometry extractGeometry(String geometryJson, Context context) throws Exception {
    Geometry geo = null;
    try {
      GeometryJSON gjson = new GeometryJSON();
      Reader reader = new StringReader(geometryJson);
      geo = gjson.read(reader);
    }
    catch (Exception e) {
      Logger logger = (Logger) context.getAttributes().get(ContextAttributes.LOGGER);
      logger.log(Level.WARNING, "Failed to convert geoJSON to jts object: " + geometryJson, e);
    }

    if (geo == null) {
      return null;
    }
    else {
      return geo;

    }
  }
}
