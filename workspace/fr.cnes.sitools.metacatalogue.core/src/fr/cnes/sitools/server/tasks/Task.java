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
package fr.cnes.sitools.server.tasks;

import java.util.concurrent.Future;

import fr.cnes.sitools.metacatalogue.model.HarvestStatus;

public class Task {

  private Future<?> future;

  private HarvestStatus status;

  public Task(Future<?> future, HarvestStatus status) {
    super();
    this.future = future;
    this.status = status;
  }

  /**
   * Gets the future value
   * 
   * @return the future
   */
  public Future<?> getFuture() {
    return future;
  }

  /**
   * Gets the status value
   * 
   * @return the status
   */
  public HarvestStatus getStatus() {
    return status;
  }

  /**
   * Sets the value of status
   * 
   * @param status
   *          the status to set
   */
  public void setStatus(HarvestStatus status) {
    this.status = status;
  }

}
