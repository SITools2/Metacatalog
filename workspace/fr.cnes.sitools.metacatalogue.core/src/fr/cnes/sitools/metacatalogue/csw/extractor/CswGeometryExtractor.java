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
package fr.cnes.sitools.metacatalogue.csw.extractor;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.fao.geonet.csw.common.util.Xml;
import org.geotools.geojson.geom.GeometryJSON;
import org.geotools.geometry.jts.JTS;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.xml.Parser;
import org.jdom.Element;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.xml.sax.SAXException;

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTWriter;

import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.metacatalogue.utils.XSLTUtils;

/**
 * Geometry extractor for CSW, ISO-19139 format
 * 
 * @author m.gond
 * 
 */
public class CswGeometryExtractor {

  public MetadataRecords extractGeometry(Element metadata, MetadataRecords fields, String schemaName) throws Exception {

    String resourcesFolder = HarvesterSettings.getInstance().getResourcePath(schemaName, "extract-gml.xsl");
    File sFileGmlXSL = new File(resourcesFolder);
    // File sFileGmlXSL = new File("resources/csw-iso19139/extract-gml.xsl");

    // extract the geometry from the XML
    Geometry geometry = extractGeometry(metadata, sFileGmlXSL);

    if (geometry != null) {

      if (!isCounterClockWise(geometry)) {
        geometry = geometry.reverse();
      }

      WKTWriter wktWriter = new WKTWriter();
      String geo = wktWriter.write(geometry);
      fields.add(MetacatalogField._GEOMETRY.getField(), geo);

      // create a GeoJSON representation of the geometry to be stored
      GeometryJSON gjson = new GeometryJSON();
      String geojson = gjson.toString(geometry);
      if (geojson != null) {
        fields.add(MetacatalogField._GEOMETRY_GEOJSON.getField(), geojson);
      }
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
   * @param elt
   *          the element
   * @return the geometry
   * @throws Exception
   *           the exception
   */
  @SuppressWarnings("unchecked")
  private Geometry extractGeometry(Element metadata, File fileXSLT) throws Exception {
    Element transform;
    try {
      XSLTUtils utils = XSLTUtils.getInstance();
      InputStream stream = utils.transform(fileXSLT, metadata);
      transform = Xml.loadStream(stream);
    }
    catch (Exception e1) {
      throw new Exception(e1);
    }
    if (transform.getChildren().size() == 0) {
      return null;
    }

    List<Polygon> allPolygons = new ArrayList<Polygon>();

    for (Element geom : (List<Element>) transform.getChildren()) {
      String srs = geom.getAttributeValue("srsName");
      CoordinateReferenceSystem sourceCRS = DefaultGeographicCRS.WGS84;
      String gml = Xml.getString(geom);

      try {
        if (srs != null && !(srs.equals(""))) {
          sourceCRS = CRS.decode(srs);
        }
        Parser parser = new Parser(new GMLConfiguration());
        MultiPolygon jts = parseGml(parser, gml);

        // if we have an srs and its not WGS84 then transform to WGS84
        if (!CRS.equalsIgnoreMetadata(sourceCRS, DefaultGeographicCRS.WGS84)) {
          MathTransform tform = CRS.findMathTransform(sourceCRS, DefaultGeographicCRS.WGS84);
          jts = (MultiPolygon) JTS.transform(jts, tform);
        }

        for (int i = 0; i < jts.getNumGeometries(); i++) {
          allPolygons.add((Polygon) jts.getGeometryN(i));
        }
      }
      catch (Exception e) {
        System.out.println("Failed to convert gml to jts object: " + gml);
        e.printStackTrace();
      }
    }

    if (allPolygons.isEmpty()) {
      return null;
    }
    else {
      try {
        Polygon[] array = new Polygon[allPolygons.size()];
        GeometryFactory geometryFactory = allPolygons.get(0).getFactory();
        return geometryFactory.createMultiPolygon(allPolygons.toArray(array));

      }
      catch (Exception e) {
        System.out.println("Failed to create a MultiPolygon from: " + allPolygons);
        e.printStackTrace();
        return null;
      }
    }
  }

  /**
   * Parses the GML.
   * 
   * @param parser
   *          the parser
   * @param gml
   *          the GML
   * @return the multi polygon
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws SAXException
   *           the sAX exception
   * @throws ParserConfigurationException
   *           the parser configuration exception
   */
  @SuppressWarnings("rawtypes")
  private MultiPolygon parseGml(Parser parser, String gml) throws IOException, SAXException,
    ParserConfigurationException {
    Object value = parser.parse(new StringReader(gml));
    if (value instanceof Map) {
      Map map = (Map) value;
      List<Polygon> geoms = new ArrayList<Polygon>();
      for (Object entry : map.values()) {
        addGeometryEntryToList(geoms, entry);
      }
      if (geoms.isEmpty()) {
        return null;
      }
      else if (geoms.size() > 1) {
        GeometryFactory factory = geoms.get(0).getFactory();
        return factory.createMultiPolygon(geoms.toArray(new Polygon[0]));
      }
      else {
        return toMultiPolygon(geoms.get(0));
      }

    }
    else if (value == null) {
      return null;
    }
    else {
      return toMultiPolygon((Geometry) value);
    }
  }

  /**
   * Adds the to list.
   * 
   * @param geoms
   *          the geometries
   * @param entry
   *          the entry
   */
  @SuppressWarnings("rawtypes")
  private void addGeometryEntryToList(List<Polygon> geoms, Object entry) {
    if (entry instanceof Polygon) {
      geoms.add((Polygon) entry);
    }
    else if (entry instanceof Collection) {
      Collection collection = (Collection) entry;
      for (Object object : collection) {
        geoms.add((Polygon) object);
      }
    }
  }

  /**
   * To multi polygon.
   * 
   * @param geometry
   *          the geometry
   * @return the multi polygon
   */
  private MultiPolygon toMultiPolygon(Geometry geometry) {
    if (geometry instanceof Polygon) {
      Polygon polygon = (Polygon) geometry;
      return geometry.getFactory().createMultiPolygon(new Polygon[] { polygon });
    }
    else if (geometry instanceof MultiPolygon) {
      return (MultiPolygon) geometry;
    }
    String message = geometry.getClass() + " cannot be converted to a polygon. Check Metadata";
    System.out.println(message);
    throw new IllegalArgumentException(message);
  }

}
