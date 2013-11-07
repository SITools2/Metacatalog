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
package fr.cnes.sitools.metacatalogue.common;

import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;

/**
 * Abstract class to implement to have a new step in the harvesting process
 * 
 * @author m.gond
 * 
 */
public abstract class HarvesterStep {
  /** The next HarvesterStep */
  protected HarvesterStep next;

  /**
   * Set the next HarvesterStep to the current HarvesterStep
   * 
   * @param next
   *          the HarvesterStep
   */
  public void setNext(HarvesterStep next) {
    this.next = next;
  }

  /**
   * Execute the harvester step
   * 
   * @param data
   *          the list of metadata as a parameter
   * @throws ProcessException
   *           when an error occured that will stop the process
   */
  public abstract void execute(Metadata data) throws ProcessException;

  /**
   * Call this method to tell the Step that the previous step is over
   * 
   * @throws ProcessException
   *           when an error occured that will stop the process
   */
  public abstract void end() throws ProcessException;

  /**
   * Check that the current step can be executed or not
   * 
   * @return true if the current step can be executed, false otherwise
   */
  public abstract CheckStepsInformation check();

}
