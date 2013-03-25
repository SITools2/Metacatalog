package fr.cnes.sitools.metacatalogue.search.solr.test;

import healpix.essentials.HealpixMapDouble;
import healpix.essentials.RangeSet;
import healpix.essentials.RangeSet.ValueIterator;
import healpix.essentials.Scheme;

import java.awt.Color;
import java.io.File;

import fr.cnes.sitools.SearchGeometryEngine.CoordSystem;
import fr.cnes.sitools.SearchGeometryEngine.GeometryIndex;
import fr.cnes.sitools.SearchGeometryEngine.Point;
import fr.cnes.sitools.SearchGeometryEngine.Polygon;
import fr.cnes.sitools.SearchGeometryEngine.RingIndex;
import fr.cnes.sitools.astro.graph.CircleDecorator;
import fr.cnes.sitools.astro.graph.CoordinateDecorator;
import fr.cnes.sitools.astro.graph.GenericProjection;
import fr.cnes.sitools.astro.graph.Graph;
import fr.cnes.sitools.astro.graph.HealpixFootprint;
import fr.cnes.sitools.astro.graph.HealpixGridDecorator.CoordinateTransformation;
import fr.cnes.sitools.astro.graph.ImageBackGroundDecorator;
import fr.cnes.sitools.astro.graph.Utility;

/*******************************************************************************
 * Copyright 2011 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 * 
 * This file is part of SITools2.
 * 
 * SITools2 is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * SITools2 is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with SITools2. If not, see
 * <http://www.gnu.org/licenses/>.
 ******************************************************************************/

public class Graphtest {

  private static final int MAX_ORDER = 6;

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    // Graph graph = new GenericProjection(Graph.ProjectionType.ECQ);
    // graph = new CoordinateDecorator(graph, Color.BLUE, 0.5f);
    // graph = new HealpixGridDecorator(graph, Scheme.RING, 5);
    // ((HealpixGridDecorator)graph).setDebug(true);
    //
    //
    // Utility.createJFrame(graph, 900, 500);

    // String box = "-9.44425,38.8259,18.68075,51.97396";
    // String box = "-124.58084,-24.1079,100.41916,77.33633";

    String box = "-10.59092,70.59523,-7.0753,71.36316";

    String[] boxCoord = box.split(",");

    double long1 = new Double(boxCoord[0]);
    double lat1 = new Double(boxCoord[1]);
    double long2 = new Double(boxCoord[2]);
    double lat2 = new Double(boxCoord[3]);

    CoordSystem coordSystem = CoordSystem.GEOCENTRIC;

    Point point1 = new Point(long1, lat1, coordSystem);
    Point point2 = new Point(long2, lat2, coordSystem);

    // TODO mettre point1 en premier et point2 en second lors du changement de version de la librairies de JCM
    Polygon shape = new Polygon(point1, point2);

    System.out.println(shape.toString());

    RingIndex healpixIndex = (RingIndex) GeometryIndex.createIndex(shape, Scheme.RING);

    if (healpixIndex.getOrder() > MAX_ORDER) {
      healpixIndex.setOrder(MAX_ORDER);
    }
    System.out.println("ORDER : " + healpixIndex.getOrder());
    
    healpixIndex.setOrder(3);
    int nbOrder = healpixIndex.getOrder();

    // faire iterator
    // RangeSet.ValueIterator valueIter = ((RangeSet) index).valueIterator();

    RangeSet healpixResult = ((RangeSet) healpixIndex.getIndex());
    System.out.println("Order : " + nbOrder);
    System.out.println(healpixIndex.getIndex().toString());

    int orderPix = nbOrder;
    Graph graph = new GenericProjection(Graph.ProjectionType.ECQ);
    // graph = new HealpixGridDecorator(graph, Scheme.RING, order);
    // ((HealpixGridDecorator)graph).setDebug(true);
    graph = new ImageBackGroundDecorator(graph, new File("Equirectangular-projection.jpg"));

    graph = new HealpixFootprint(graph, Scheme.RING, orderPix, 0.5f);
    HealpixMapDouble map = new HealpixMapDouble((long) Math.pow(2, orderPix), Scheme.RING);
    // map.fill(0.0);

    ValueIterator iterator = healpixResult.valueIterator();
    while (iterator.hasNext()) {
      long pix = iterator.next();
      map.setPixel(pix, 1.0);
    }

    // map.setPixel(7321, 1.0);
    // map.setPixel(7564, 1.0);
    // map.setPixel(7565, 1.0);
    // map.setPixel(7812, 1.0);
    // map.setPixel(7813, 1.0);
    // // map.setPixel(905, 1.0);
    // map.setPixel(991, 1.0);
    // map.setPixel(992, 1.0);
    ((HealpixFootprint) graph).importHealpixMap(map, CoordinateTransformation.NATIVE);
    ((HealpixFootprint) graph).setAlpha(0.2f);
    ((HealpixFootprint) graph).setColor(Color.GREEN);

    graph = new CircleDecorator(graph, 0.0, 0.0, 1, Scheme.RING, 10);
    ((CircleDecorator) graph).setColor(Color.yellow);

    graph = new CoordinateDecorator(graph, Color.BLUE, 0.5f);

    Utility.createJFrame(graph, 900, 500);

  }

}
