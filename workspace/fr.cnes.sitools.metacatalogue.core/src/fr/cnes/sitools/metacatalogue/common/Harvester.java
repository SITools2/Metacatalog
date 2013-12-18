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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.engine.util.DateUtils;

import fr.cnes.sitools.metacatalogue.exceptions.CheckProcessException;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.server.HarvestersApplication;

/**
 * Interface for a Harvesting process
 * 
 * @author m.gond
 * 
 */
public abstract class Harvester implements Runnable {

  protected Context context;

  protected HarvesterModel harvestConf;

  protected HarvestersApplication application;

  /**
   * Launch a new Harvest process
   * 
   * @param harvestConf
   *          the configuration of the harvest
   * @throws CheckProcessException
   *           if there is an error while checking the harvesting process
   * @throws ProcessException
   *           if there is an error during the harvesting process
   */
  public void initHarvester(HarvesterModel harvestConf, Context context) throws CheckProcessException {
    this.context = context;
    this.harvestConf = harvestConf;
    this.application = (HarvestersApplication) context.getAttributes().get("APPLICATION");
  }

  @Override
  public void run() {
    HarvestStatus status = null;
    try {
      status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);
      status.setStartDate(new Date());
      Logger logger = initNewLogger(harvestConf, status.getStartDate());
      context.getAttributes().put(ContextAttributes.LOGGER, logger);
      status.setLoggerFileName(getLoggerName(status.getStartDate(), harvestConf));

      try {
        harvest();
        if (harvestConf.isAutomaticMerge()) {
          HarvesterUtils.merge(application.getStore().getList());
        }
        harvestConf.setLastHarvest(new Date());
        status.setResult(HarvestStatus.RESULT_SUCCESS);
        application.getStore().save(harvestConf);
      }
      catch (Exception e) {
        status.setErrorCause(e.getMessage());
        status.setResult(HarvestStatus.RESULT_ERROR);
        e.printStackTrace();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {
      harvestConf.setStatus("INACTIVE");
      status.setStatus(HarvestStatus.STATUS_READY);
      harvestConf.setLastRunResult(status);
      status.setEndDate(new Date());
      application.getStore().save(harvestConf);
      application.getTaskManager().unregister(harvestConf.getId());
      application.unlock();
    }

  }

  private Logger initNewLogger(HarvesterModel conf, Date date) throws SecurityException, IOException {

    // create a logger for the Task
    Logger logger = Logger.getLogger(conf.getId());

    logger.setLevel(Level.INFO);
    String logFolder = getLoggerDir();
    // create a fileHandler to log into a file

    File f = new File(logFolder);
    if (!f.exists()) {
      f.mkdirs();
      f.setReadable(true);
      f.setWritable(true);
    }

    String fileHandlerName = logFolder + "/" + getLoggerName(date, conf);

    FileHandler fl = new FileHandler(fileHandlerName, true);
    fl.setFormatter(new MetacatalogueLogFormatter());
    logger.addHandler(fl);

    return logger;
  }

  private String getLoggerDir() {
    String logFolder = HarvesterSettings.getInstance().getString("LOG_FOLDER");
    logFolder = HarvesterSettings.getInstance().getRootDirectory() + logFolder;

    return logFolder;
  }

  private String getLoggerName(Date date, HarvesterModel conf) {
    String fileHandlerName = conf.getId() + "_"
        + DateUtils.format(date, fr.cnes.sitools.util.DateUtils.LOG_FILE_NAME_DATE_FORMAT) + ".log";
    return fileHandlerName;

  }

  public abstract void harvest() throws Exception;

}
