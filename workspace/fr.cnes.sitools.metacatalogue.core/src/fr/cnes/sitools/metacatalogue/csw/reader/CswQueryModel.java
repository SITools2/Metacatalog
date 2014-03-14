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
package fr.cnes.sitools.metacatalogue.csw.reader;

import java.util.Date;

/**
 * Model class to store informations to fill the FTL file for CSW filter
 * 
 * @author m.gond
 * 
 */
public class CswQueryModel {
  /** Last index time */
  private Date lastIndexTime;
  /** Pagination information, start index */
  private int startPosition;
  /** Pagination information, number of records neeeded */
  private int maxRecords;

  /**
   * Gets the lastIndexTime value
   * 
   * @return the lastIndexTime
   */
  public Date getLastIndexTime() {
    return lastIndexTime;
  }

  /**
   * Sets the value of lastIndexTime
   * 
   * @param lastIndexTime
   *          the lastIndexTime to set
   */
  public void setLastIndexTime(Date lastIndexTime) {
    this.lastIndexTime = lastIndexTime;
  }

  /**
   * Gets the startPosition value
   * 
   * @return the startPosition
   */
  public int getStartPosition() {
    return startPosition;
  }

  /**
   * Sets the value of startPosition
   * 
   * @param startPosition
   *          the startPosition to set
   */
  public void setStartPosition(int startPosition) {
    this.startPosition = startPosition;
  }

  /**
   * Gets the maxRecords value
   * 
   * @return the maxRecords
   */
  public int getMaxRecords() {
    return maxRecords;
  }

  /**
   * Sets the value of maxRecords
   * 
   * @param maxRecords
   *          the maxRecords to set
   */
  public void setMaxRecords(int maxRecords) {
    this.maxRecords = maxRecords;
  }

}
