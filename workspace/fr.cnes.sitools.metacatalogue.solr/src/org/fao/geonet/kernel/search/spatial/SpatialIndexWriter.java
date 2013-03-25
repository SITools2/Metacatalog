//==============================================================================
//===	Copyright (C) 2001-2008 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.search.spatial;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.data.Transaction;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.xml.Parser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.identity.FeatureId;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.STRtree;

import fr.cnes.sitools.metacatalogue.search.solr.spatial.PostgisSpatial;

/**
 * This class is responsible for extracting geographic information from metadata and writing that information to a
 * storage mechanism.
 * 
 * @author jeichar
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SpatialIndexWriter {

  /** The Constant MAX_WRITES_IN_TRANSACTION. */
  private static final int MAX_WRITES_IN_TRANSACTION = 5000;

  /** The _parser. */
  private final Parser _parser;

  /** The _transaction. */
  private final Transaction _transaction;

  /** The _lock. */
  private final Lock _lock;

  /** The _feature store. */
  private final FeatureStore<SimpleFeatureType, SimpleFeature> _featureStore;

  /** The _index. */
  private STRtree _index;

  /** The _writes. */
  private static int _writes;

  /**
   * Instantiates a new spatial index writer.
   * 
   * @param datastore
   *          the datastore
   * @param parser
   *          the parser
   * @param transaction
   *          the transaction
   * @param lock
   *          the lock
   * @throws Exception
   *           the exception
   */
  public SpatialIndexWriter(DataStore datastore, Parser parser, Transaction transaction, Lock lock) throws Exception {
    // Note: The Configuration takes a long time to create so it is worth
    // re-using the same Configuration
    _lock = lock;
    _parser = parser;
    _parser.setStrict(false);
    _parser.setValidating(false);
    _transaction = transaction;

    _featureStore = createFeatureStore(datastore);
    _featureStore.setTransaction(_transaction);

  }

  /**
   * Index.
   * 
   * @param uuid
   *          the metadata uuid
   * @param geometry
   *          the metadata geometry
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void index(String uuid, Geometry geometry, String sourceCore) throws IOException {
    _lock.lock();
    try {
      _index = null;
      if (geometry != null) {
        FeatureCollection features = FeatureCollections.newCollection();

        SimpleFeatureType schema = _featureStore.getSchema();
        Object[] data = new Object[schema.getAttributeCount()];
        List<AttributeDescriptor> attributes = schema.getAttributeDescriptors();
        int index = 0;
        for (AttributeDescriptor attributeDescriptor : attributes) {
          if (attributeDescriptor == schema.getGeometryDescriptor()) {
            data[index++] = geometry;
          }
          if (attributeDescriptor.getLocalName().equals(PostgisSpatial.SPATIAL_INDEX_COLUMN_UUID)) {
            data[index++] = uuid;
          }
          if (attributeDescriptor.getLocalName().equals(PostgisSpatial.SPATIAL_INDEX_COLUMN_CORE)) {
            data[index++] = sourceCore;
          }
        }

        features.add(SimpleFeatureBuilder.build(schema, data, SimpleFeatureBuilder.createDefaultFeatureId()));

        _featureStore.addFeatures(features);

        _writes++;

        if (_writes > MAX_WRITES_IN_TRANSACTION) {
          _transaction.commit();
          _writes = 0;
        }
      }
    }
    finally {
      _lock.unlock();
    }
  }

  // /**
  // * Index.
  // *
  // * @param uuid
  // * the metadata uuid
  // * @param geometry
  // * the metadata geometry
  // * @throws IOException
  // * Signals that an I/O exception has occurred.
  // */
  // public void modify(String uuid, Geometry geometry) throws IOException {
  // _lock.lock();
  // try {
  // _index = null;
  // if (geometry != null) {
  // SimpleFeatureType schema = _featureStore.getSchema();
  //
  // FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
  // Filter filter = factory.equals(factory.property(PostgisSpatial.SPATIAL_INDEX_COLUMN_UUID),
  // factory.literal(uuid));
  //
  //
  //
  // _featureStore.modifyFeatures(schema.getGeometryDescriptor(), geometry, filter);
  //
  // _writes++;
  //
  // if (_writes > MAX_WRITES_IN_TRANSACTION) {
  // _transaction.commit();
  // _writes = 0;
  // }
  // }
  // }
  // finally {
  // _lock.unlock();
  // }
  // }

  /**
   * Close.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void close() throws IOException {
    _lock.lock();
    try {
      if (_writes > 0) {
        _transaction.commit();
        _writes = 0;
      }
      _transaction.close();
      _index = null;
      _featureStore.setTransaction(Transaction.AUTO_COMMIT);
    }
    finally {
      _lock.unlock();
    }
  }

  /**
   * Gets the feature source.
   * 
   * @return the feature source
   */
  public FeatureSource getFeatureSource() {
    return _featureStore;
  }

  /**
   * Delete.
   * 
   * @param id
   *          the id
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void delete(String id) throws IOException {
    _lock.lock();
    try {
      FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
      Filter filter = factory.equals(factory.property(PostgisSpatial.SPATIAL_INDEX_COLUMN_UUID), factory.literal(id));

      _index = null;

      _featureStore.removeFeatures(filter);
      _writes++;
    }
    finally {
      _lock.unlock();
    }
  }

  /**
   * Delete.
   * 
   * @param id
   *          the id
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void deleteByCore(String coreName) throws IOException {
    _lock.lock();
    try {
      FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
      Filter filter = factory.equals(factory.property(PostgisSpatial.SPATIAL_INDEX_COLUMN_CORE),
          factory.literal(coreName));

      _index = null;

      _featureStore.removeFeatures(filter);
      _writes++;
    }
    finally {
      _lock.unlock();
    }
  }

  /**
   * Delete.
   * 
   * @param id
   *          the id
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void delete(String id, String coreName) throws IOException {
    _lock.lock();
    try {
      FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
      Filter filterId = factory.equals(factory.property(PostgisSpatial.SPATIAL_INDEX_COLUMN_UUID), factory.literal(id));
      Filter filterCore = factory.equals(factory.property(PostgisSpatial.SPATIAL_INDEX_COLUMN_CORE),
          factory.literal(coreName));

      Filter filterAnd = factory.and(filterId, filterCore);

      _index = null;

      _featureStore.removeFeatures(filterAnd);
      _writes++;
    }
    finally {
      _lock.unlock();
    }
  }

  public boolean featureExists(String id) throws IOException {
    _lock.lock();
    try {
      FilterFactory2 factory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
      Filter filter = factory.equals(factory.property(PostgisSpatial.SPATIAL_INDEX_COLUMN_UUID), factory.literal(id));

      FeatureCollection<SimpleFeatureType, SimpleFeature> features = _featureStore.getFeatures(filter);
      boolean exists = !features.isEmpty();
      return exists;
    }
    finally {
      _lock.unlock();
    }
  }

  /**
   * Commit.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public void commit() throws IOException {
    _lock.lock();
    try {
      if (_writes > 0) {
        _writes = 0;
        _transaction.commit();
        _index = null;
        // populateIndex();
      }
      // TODO voir si le populateIndex exécuté à chaque commit est utile quand il y a beaucoup de données
      populateIndex();
    }
    finally {
      _lock.unlock();
    }
  }

  /**
   * Gets the index.
   * 
   * @return the index
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public SpatialIndex getIndex() throws IOException {
    _lock.lock();
    try {
      if (_index == null) {
        populateIndex();
      }
      return _index;
    }
    finally {
      _lock.unlock();
    }
  }

  /**
   * Deletes the old index and sets up an empty index file.
   * 
   * @throws Exception
   *           the exception
   */
  public void reset() throws Exception {
    _lock.lock();
    try {
      _featureStore.setTransaction(Transaction.AUTO_COMMIT);
      _index = null;
      _featureStore.removeFeatures(Filter.INCLUDE);
      _featureStore.setTransaction(_transaction);
    }
    finally {
      _lock.unlock();
    }
  }

  /**
   * Populate index.
   * 
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  private void populateIndex() throws IOException {
    _index = new STRtree();
    FeatureIterator<SimpleFeature> features = _featureStore.getFeatures().features();
    try {
      while (features.hasNext()) {
        SimpleFeature feature = features.next();
        Pair<FeatureId, Object> data = Pair.read(feature.getIdentifier(),
            feature.getAttribute(PostgisSpatial.SPATIAL_INDEX_COLUMN_UUID));
        _index.insert(((Geometry) feature.getDefaultGeometry()).getEnvelopeInternal(), data);
      }
    }
    finally {
      features.close();
    }
  }

  /**
   * Creates the feature store.
   * 
   * @param datastore
   *          the datastore
   * @return the feature store
   * @throws Exception
   *           the exception
   */
  private FeatureStore createFeatureStore(DataStore datastore) throws Exception {

    return (FeatureStore) datastore.getFeatureSource(PostgisSpatial.SPATIAL_INDEX_TABLE);
  }

}
