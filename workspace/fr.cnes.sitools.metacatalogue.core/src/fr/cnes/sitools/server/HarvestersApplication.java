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
package fr.cnes.sitools.server;

import java.util.concurrent.atomic.AtomicBoolean;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.resource.Directory;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Router;

import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.persistence.HarvesterModelStore;
import fr.cnes.sitools.server.administration.HarvesterCollectionResource;
import fr.cnes.sitools.server.administration.HarvesterResource;
import fr.cnes.sitools.server.resources.HarvestResource;
import fr.cnes.sitools.server.resources.HarvestStatusResource;
import fr.cnes.sitools.server.resources.HarvesterMergeResource;
import fr.cnes.sitools.server.resources.JsonFileExpositionResource;
import fr.cnes.sitools.server.resources.MetacatalogueStatusResource;
import fr.cnes.sitools.server.tasks.TaskManager;

/**
 * Application for managing Harvester Model
 * 
 * @author AKKA
 */
public final class HarvestersApplication extends Application implements ILockableApplication {

  /** Store */
  private HarvesterModelStore store = null;
  /** The metacatalog merged core */
  private String metacatalogIndexerUrl;

  /** The metacatalog merged core */
  private String metacatalogIndexerCoreName;
  /** The application lock */
  private final AtomicBoolean locked;
  /** The infoResource of the lock */
  private InfoResource infoResource;

  private TaskManager taskManager;

  /**
   * Constructor with component to activate DataSources
   * 
   * 
   * @param context
   *          RESTlet application context
   * @param store
   *          The {@link HarvesterModelStore}
   */
  public HarvestersApplication(Context context, HarvesterModelStore store) {
    super(context);
    this.store = store;
    metacatalogIndexerUrl = HarvesterSettings.getInstance().getString("METACATALOG_INDEXER_URL");
    metacatalogIndexerCoreName = HarvesterSettings.getInstance().getString("METACATALOG_INDEXER_CORE_NAME");
    this.locked = new AtomicBoolean(false);
    taskManager = new TaskManager();

  }

  @Override
  public Restlet createInboundRoot() {

    Router router = new Router(getContext());
    router.attachDefault(MetacatalogueStatusResource.class);

    router.attach("/merge", HarvesterMergeResource.class);

    router.attach("/admin", HarvesterCollectionResource.class);
    router.attach("/admin/{harvesterId}", HarvesterResource.class);

    router.attach("/admin/{harvesterId}/harvest/start", HarvestResource.class);
    router.attach("/admin/{harvesterId}/harvest/cleanAndStart", HarvestResource.class);
    router.attach("/admin/{harvesterId}/harvest/stop", HarvestResource.class);
    router.attach("/admin/{harvesterId}/harvest/clean", HarvestResource.class);
    router.attach("/admin/{harvesterId}/harvest/status", HarvestStatusResource.class);

    router.attach("/ihm/catalogsTypes", JsonFileExpositionResource.class);
    router.attach("/ihm/havestersClasses", JsonFileExpositionResource.class);

    String logFolder = HarvesterSettings.getInstance().getString("LOG_FOLDER");
    logFolder = "file:///" + HarvesterSettings.getInstance().getRootDirectory() + logFolder + "/";

    Directory directory = new Directory(getContext(), logFolder);
    directory.setDeeplyAccessible(true);
    directory.setListingAllowed(true);
    directory.setModifiable(false);
    router.attach("/logs", directory);

    return router;
  }

  /**
   * Gets the store value
   * 
   * @return the store
   */
  public HarvesterModelStore getStore() {
    return store;
  }

  /**
   * Gets the metacatalogIndexerUrl value
   * 
   * @return the metacatalogIndexerUrl
   */
  public String getMetacatalogIndexerUrl() {
    return metacatalogIndexerUrl;
  }

  /**
   * Sets the value of metacatalogIndexerUrl
   * 
   * @param metacatalogIndexerUrl
   *          the metacatalogIndexerUrl to set
   */
  public void setMetacatalogIndexerUrl(String metacatalogIndexerUrl) {
    this.metacatalogIndexerUrl = metacatalogIndexerUrl;
  }

  /**
   * Gets the metacatalogIndexerCoreName value
   * 
   * @return the metacatalogIndexerCoreName
   */
  public String getMetacatalogIndexerCoreName() {
    return metacatalogIndexerCoreName;
  }

  /**
   * Sets the value of metacatalogIndexerCoreName
   * 
   * @param metacatalogIndexerCoreName
   *          the metacatalogIndexerCoreName to set
   */
  public void setMetacatalogIndexerCoreName(String metacatalogIndexerCoreName) {
    this.metacatalogIndexerCoreName = metacatalogIndexerCoreName;
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.server.ITransactionnalApplication#lock(fr.cnes.sitools.server.harvest.InfoResource)
   */
  @Override
  public void lock(InfoResource infoResource) {
    synchronized (this.locked) {
      if (!this.locked.get()) {
        this.locked.set(true);
        this.setInfoResource(infoResource);
      }
      else {
        throw new ResourceException(infoResource.getStatus(), infoResource.getMessage());
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.server.ITransactionnalApplication#unlock()
   */
  @Override
  public void unlock() {
    synchronized (this.locked) {
      this.locked.set(false);
      this.setInfoResource(null);
    }
  }

  /**
   * Gets the infoResource value
   * 
   * @return the infoResource
   */
  public InfoResource getInfoResource() {
    return infoResource;
  }

  /**
   * Sets the value of infoResource
   * 
   * @param infoResource
   *          the infoResource to set
   */
  public void setInfoResource(InfoResource infoResource) {
    this.infoResource = infoResource;
  }

  @Override
  public boolean isLocked() {
    synchronized (this.locked) {
      return this.locked.get();
    }
  }

  /**
   * Gets the taskManager value
   * 
   * @return the taskManager
   */
  public TaskManager getTaskManager() {
    return taskManager;
  }

}
