/*******************************************************************************
 * Copyright 2011 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.sitools.metacatalogue.search.solr.spatial;

import healpix.essentials.RangeSet;
import healpix.essentials.RangeSet.ValueIterator;
import healpix.essentials.Scheme;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

import fr.cnes.sitools.SearchGeometryEngine.CoordSystem;
import fr.cnes.sitools.SearchGeometryEngine.GeometryIndex;
import fr.cnes.sitools.SearchGeometryEngine.Index;
import fr.cnes.sitools.SearchGeometryEngine.Point;
import fr.cnes.sitools.SearchGeometryEngine.Polygon;
import fr.cnes.sitools.SearchGeometryEngine.RingIndex;
import fr.cnes.sitools.SearchGeometryEngine.Shape;

public class HealpixGeometryTool {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(HealpixGeometryTool.class);

  /** The _lock. */
  private final Lock lock;
  /** Min healpix order */
  // TODO a mettre dans le fichier de configuration
  public static final int MIN_HEALIPX_ORDER = 1;

  /** Max healpix order */
  // TODO a mettre dans le fichier de configuration
  public static final int MAX_HEALIPX_ORDER = 6;

  /** Max healpix order */
  // TODO a mettre dans le fichier de configuration
  public static final int HEALPIX_BEST_ORDER_PRECISION = 4;

  // // TODO a mettre dans le fichier de configuration
  // public static final int HEALIPX_ORDER_PALIER_1 = 4;
  //
  // /** Max healpix order */
  // // TODO a mettre dans le fichier de configuration
  // public static final int HEALIPX_ORDER_PALIER_2 = 7;
  //
  // // TODO a mettre dans le fichier de configuration
  // public static final int HEALIPX_ORDER_PALIER_3 = 10;
  //
  // public static int[] healpixOrders = { HEALIPX_ORDER_PALIER_3, HEALIPX_ORDER_PALIER_2, HEALIPX_ORDER_PALIER_1 };

  /** The instance. */
  private static HealpixGeometryTool instance;

  public static HealpixGeometryTool getInstance() {
    return instance;
  }

  public static HealpixGeometryTool initialize() {
    instance = new HealpixGeometryTool();
    return instance;
  }

  public HealpixGeometryTool() {
    super();
    lock = new ReentrantLock();
  }

  /**
   * Adds the metadata.
   * 
   * @param uuid
   *          the metadata uuid
   * @param wktGeometry
   *          the wkt geometry
   * @throws Exception
   *           the exception
   */
  public void addMetadata(String uuid, String wktGeometry, SolrInputDocument doc) throws Exception {
    logger.info("\tAdd spatial metadata: {}", uuid);
    if (uuid != null && wktGeometry != null)
      lock.lock();
    try {
      // Extract Geometry
      WKTReader reader = new WKTReader();
      Geometry geometry = reader.read(wktGeometry);
      addGeometry(geometry, doc);
    }
    finally {
      lock.unlock();
    }
  }

  public void addGeometry(Geometry geometry, SolrInputDocument doc) throws Exception {
    int nbGeometry = geometry.getNumGeometries();
    for (int i = 0; i < nbGeometry; i++) {
      Geometry currentGeo = geometry.getGeometryN(i);
      Shape shape = getShape(currentGeo);
      if (shape != null) {
        addHealpixIndexes(shape, Scheme.RING, doc);

      }
      else {
        logger.info("No geometry found");
      }

    }

  }

  public Shape getShape(Geometry geometry) {
    Shape shape;
    if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
      List<Point> polygonPoints = new ArrayList<Point>();
      Coordinate[] coordinates = geometry.getCoordinates();
      for (int i = coordinates.length - 1; i > 0; i--) {
        Coordinate coordinate = coordinates[i];
        Point point = new Point(coordinate.x, coordinate.y, CoordSystem.GEOCENTRIC);
        polygonPoints.add(point);
      }
      shape = new Polygon(polygonPoints);
    }
    else if (geometry instanceof com.vividsolutions.jts.geom.Point) {
      Coordinate[] coordinates = geometry.getCoordinates();
      Coordinate coordinate = coordinates[0];
      shape = new Point(coordinate.x, coordinate.y, CoordSystem.GEOCENTRIC);
    }
    else {
      shape = null;
    }

    return shape;
  }

  private void addHealpixIndexes(Shape shape, Scheme scheme, SolrInputDocument doc) throws Exception {
    if (Scheme.RING == scheme) {
      Index index = GeometryIndex.createIndex(shape, scheme);

      RingIndex ringIndex = (RingIndex) index;
      int minOrder = MIN_HEALIPX_ORDER;
      for (int order = minOrder; order <= MAX_HEALIPX_ORDER; order++) {
        ringIndex.setOrder(order);
        RangeSet healpixNumbers = (RangeSet) index.getIndex();
        ValueIterator iterator = healpixNumbers.valueIterator();
        while (iterator.hasNext()) {
          long pix = iterator.next();
          doc.addField("healpix-order-" + order, pix);
        }
      }
    }
    else {
      logger.error("Scheme not supported : " + scheme);
    }

  }

  // public void addHealpixIndexes(Shape shape, Scheme scheme, SolrInputDocument doc) throws Exception {
  // if (Scheme.RING == scheme) {
  // Index index = GeometryIndex.createIndex(shape, scheme);
  //
  // RingIndex ringIndex = (RingIndex) index;
  //
  // int bestOrder = ringIndex.getOrder() + HEALPIX_BEST_ORDER_PRECISION;
  //
  // for (int i = 0; i < healpixOrders.length; i++) {
  // int order = healpixOrders[i];
  // if (i == healpixOrders.length - 1 || bestOrder > healpixOrders[i + 1]) {
  // ringIndex.setOrder(order);
  // RangeSet healpixNumbers = (RangeSet) index.getIndex();
  // ValueIterator iterator = healpixNumbers.valueIterator();
  // while (iterator.hasNext()) {
  // long pix = iterator.next();
  // doc.addField("healpix-order-" + order, pix);
  // }
  // }
  // }
  // }
  // else {
  // logger.error("Scheme not supported : " + scheme);
  // }
  // }

  // public int getIndexOrder(int bestOrder) {
  //
  // if (bestOrder >= HEALIPX_ORDER_PALIER_1) {
  // return HEALIPX_ORDER_PALIER_1;
  // }
  // else if (bestOrder <= HEALIPX_ORDER_PALIER_2) {
  // return HEALIPX_ORDER_PALIER_2;
  // }
  // else {
  // return HEALIPX_ORDER_PALIER_3;
  // }
  //
  // }

}
