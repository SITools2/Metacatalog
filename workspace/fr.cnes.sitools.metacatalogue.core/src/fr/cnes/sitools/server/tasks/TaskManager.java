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
package fr.cnes.sitools.server.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class TaskManager {

  private Map<String, Task> tasks;

  public TaskManager() {
    tasks = new HashMap<String, Task>();
  }

  public void register(String id, Task task) {
    tasks.put(id, task);
  }

  public void unregister(String id) {
    tasks.remove(id);
  }

  public Task get(String id) {
    return tasks.get(id);
  }

  public boolean cancel(String id) {
    Task task = get(id);
    if (task != null) {
      Future<?> future = task.getFuture();
      boolean result = future.cancel(true);
      if (result) {
        unregister(id);
      }
      return result;
    }
    return false;
  }

}
