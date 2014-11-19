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
package fr.cnes.sitools.server.resources;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.Future;
import java.util.logging.Level;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.Response;
import fr.cnes.sitools.metacatalogue.common.Harvester;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.persistence.HarvesterModelStore;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.server.HarvestersApplication;
import fr.cnes.sitools.server.InfoResource;
import fr.cnes.sitools.server.administration.AbstractHarvesterResource;
import fr.cnes.sitools.server.tasks.Task;

public class HarvestResource extends AbstractHarvesterResource {

  /** Parent Application */
  protected volatile HarvestersApplication application = null;

  /** Store */
  protected volatile HarvesterModelStore store = null;

  /** DataSet identifier parameter */
  protected volatile String harvesterId = null;

  @Override
  protected void doInit() {
    super.doInit();

    // Declares the two variants supported
    getVariants().add(new Variant(MediaType.APPLICATION_XML));
    getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    getVariants().add(new Variant(MediaType.APPLICATION_JAVA_OBJECT));
    
    application = (HarvestersApplication) getApplication();
    store = application.getStore();

    harvesterId = getHavesterId();
  }

  /**
   * Actions on PUT
   * 
   * @param representation
   *          could be null.
   * @param variant
   *          MediaType of response
   * @return Representation response
   */
  @Put
  public Representation put(Representation representation, Variant variant) {
    return execute(variant);
  }

  private Representation execute(Variant variant) {

    Response response = null;
    do {
      // on charge le dataset
      HarvesterModel conf = store.get(harvesterId);
      if (conf == null) {
        response = new Response(false, "Cannot find Harvester configuration for id : " + harvesterId);
        break;
      }

      if (this.getReference().toString().endsWith("start")) {
        application.lock(new InfoResource(Status.CLIENT_ERROR_CONFLICT, "Start harvesting operation on " + conf.getId()
            + " catalog"));

        conf.setStatus("ACTIVE");
        store.save(conf);
        // start a new harvesting
        HarvestStatus result;
        try {
          result = this.harvest(conf);
          getResponse().setStatus(Status.SUCCESS_CREATED);
          response = new Response(true, result, HarvestStatus.class, "harvestStatus");
        }
        catch (Exception e) {
          try {
            conf.setStatus("INACTIVE");
            store.save(conf);
            HarvesterSettings.getInstance().getLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
            response = new Response(false, e.getMessage());
            break;
          }
          finally {
            application.unlock();
          }
        }

      }

      if (this.getReference().toString().endsWith("cleanAndStart")) {
        application.lock(new InfoResource(Status.CLIENT_ERROR_CONFLICT, "CleanAndStart harvesting operation on "
            + conf.getId() + " catalog"));

        conf.setStatus("ACTIVE");
        store.save(conf);
        // start a new harvesting
        HarvestStatus result;
        try {
          this.cleanCore(conf);
          conf.setLastHarvest(null);
          store.save(conf);
          result = this.harvest(conf);
          getResponse().setStatus(Status.SUCCESS_CREATED);
          response = new Response(true, result, HarvestStatus.class, "harvestStatus");
        }
        catch (Exception e) {
          try {
            conf.setStatus("INACTIVE");
            store.save(conf);
            HarvesterSettings.getInstance().getLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
            response = new Response(false, e.getMessage());
            break;
          }
          finally {
            application.unlock();
          }
        }

      }

      if (this.getReference().toString().endsWith("stop")) {
        // stop the current harvesting
        // getResponse().setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED, "not implemented yet");
        // response = new Response(false, "not implemented yet");
        Task task = application.getTaskManager().get(harvesterId);
        if (task != null) {
          boolean stopped = application.getTaskManager().cancel(harvesterId);
          if (stopped) {
            task.getStatus().setEndDate(new Date());
            response = new Response(true, task.getStatus(), HarvestStatus.class, "harvestStatus");
            application.unlock();
            break;
          }
          else {
            response = new Response(false, "Harvesting cannot be stopped");
            break;
          }
        }
        response = new Response(false, "No current harvesting process");
        break;
      }

      // if (this.getReference().toString().endsWith("cancel")) {
      // // cancel the current harvesting
      // Map<String, HarvesterStatus> tasks = (Map<String, HarvesterStatus>) application.getContext().getAttributes()
      // .get("TASKS");
      // if (tasks == null) {
      // getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No running tasks");
      // break;
      // }
      // HarvesterStatus status = tasks.get(conf.getId());
      // if (status == null) {
      // getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No running task for id : " + conf.getId());
      // break;
      // }
      // Future<HarvesterResult> future = status.getFuture();
      // boolean canceled = future.cancel(true);
      // if (canceled) {
      // getResponse().setStatus(Status.SUCCESS_OK, "Harvesting stopped");
      // }
      // else {
      // getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "cannot stop harvesting");
      // }
      // break;
      //
      // }

      if (this.getReference().toString().endsWith("clean")) {
        application.lock(new InfoResource(Status.CLIENT_ERROR_CONFLICT, "Clean harvesting operation on " + conf.getId()
            + " catalog"));

        try {
          conf.setStatus("ACTIVE");
          store.save(conf);
          this.cleanCore(conf);
          conf.setLastHarvest(null);
          response = new Response(true, "Core cleaned successfully");
        }
        catch (SolrServerException e) {
          HarvesterSettings.getInstance().getLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
          response = new Response(false, e.getMessage());
          break;
        }
        catch (IOException e) {
          HarvesterSettings.getInstance().getLogger().log(Level.WARNING, e.getLocalizedMessage(), e);
          response = new Response(false, e.getMessage());
          break;
        }
        finally {
          conf.setStatus("INACTIVE");
          store.save(conf);
          application.unlock();
        }
      }

    } while (false);

    return getRepresentation(response, variant);
  }

  private HarvestStatus harvest(HarvesterModel conf) throws Exception {

    HarvestStatus status = new HarvestStatus();
    getContext().getAttributes().put(ContextAttributes.STATUS, status);
    getContext().getAttributes().put("APPLICATION", application);

    Class<Harvester> harvesterClass = (Class<Harvester>) Class.forName(conf.getHarvesterClassName());
    Harvester harvester = harvesterClass.newInstance();

    harvester.initHarvester(conf, getContext());

    HarvesterSettings settings = HarvesterSettings.getInstance();

    String url = settings.getPublicHostDomain() + settings.getString(Consts.HARVESTERS_APP_URL)
        + String.format("/admin/%s/harvest/status", conf.getId());
    status.setUrl(url);

    Future<?> future = application.getTaskService().submit(harvester);

    Task task = new Task(future, status);
    application.getTaskManager().register(conf.getId(), task);

    status.setStatus(HarvestStatus.STATUS_RUNNING);

    return status;

  }

  private void cleanCore(HarvesterModel model) throws SolrServerException, IOException {
    SolrServer server = SolRUtils.getSolRServer(model.getIndexerConf().getUrl());
    if (server == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Cannot connect to core "
          + model.getIndexerConf().getUrl());
    }
    server.deleteByQuery("*:*");
    server.commit();

  }

}
