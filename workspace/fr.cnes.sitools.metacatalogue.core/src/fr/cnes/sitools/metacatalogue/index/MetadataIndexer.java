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
package fr.cnes.sitools.metacatalogue.index;

import java.util.List;

import fr.cnes.sitools.metacatalogue.model.Fields;

/**
 * Interface that define a Metadata Indexer
 * 
 * @author m.gond
 * 
 */
public interface MetadataIndexer {
  /**
   * Index the metadata that have been previously added
   * 
   * @throws Exception
   *           if there are some errors related to the Index engine or if no data where previously added
   */
  void indexMetadata() throws Exception;

  /**
   * Add a new {@link Fields} to index
   * 
   * @param fields
   *          a {@link Fields} object to index
   * @throws Exception
   *           if there are some errors
   */
  void addFieldsToIndex(Fields fields) throws Exception;

  /**
   * Add a new {@link List} of {@link Fields} to index
   * 
   * @param fieldList
   *          a {@link List} of {@link Fields} object to index
   * @throws Exception
   *           if there are some errors
   */
  void addListFieldsToIndex(List<Fields> fieldList) throws Exception;

  /**
   * Get the current number of fields in the cache to index
   * 
   * @return the current number of fields in the cache to index
   */
  int getCurrentNumberOfFieldsToIndex();

  /**
   * Check that the indexer engine is available
   * 
   * @return true if the indexer is available, false otherwise
   */
  boolean checkIndexerAvailable();

  /**
   * Commit the changes on the server
   * 
   * @throws Exception
   *           if there are errors during the commit phase
   */
  void commit() throws Exception;
}