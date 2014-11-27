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
package fr.cnes.sitools.metacatalogue.common;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.util.DateUtils;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.mail.model.Mail;
import fr.cnes.sitools.metacatalogue.exceptions.CheckProcessException;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.server.HarvestersApplication;
import fr.cnes.sitools.util.RIAPUtils;

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

  protected String errorMessage = null;

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
        if (status.getNbDocumentsInvalid() == 0) {
          status.setResult(HarvestStatus.RESULT_SUCCESS);
          sendStatusMail(harvestConf.getDescription(), HarvestStatus.RESULT_SUCCESS);
        }
        else {
          status.setResult(HarvestStatus.RESULT_PARTIAL_SUCCESS);
          sendStatusMail(harvestConf.getDescription(), HarvestStatus.RESULT_PARTIAL_SUCCESS);
        }
        application.getStore().save(harvestConf);
      }
      catch (Exception e) {
        status.setErrorCause(e.getMessage());
        status.setResult(HarvestStatus.RESULT_ERROR);
        sendStatusMail(harvestConf.getDescription(), HarvestStatus.RESULT_ERROR, exceptionAsString(e));
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

  /**
   * initNewLogger
   * 
   * @param conf
   * @param date
   * @return
   * @throws SecurityException
   * @throws IOException
   */
  private Logger initNewLogger(HarvesterModel conf, Date date) throws SecurityException, IOException {

    // create a logger for the Task
    Logger logger = Engine.getLogger(conf.getId());

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

  /**
   * getLoggerDir
   * 
   * @return String
   */
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

  /**
   * sendStatusMail
   * 
   * @param description
   *          the description of the harvester
   * @param resultError
   *          the result of the error
   * @param message
   *          the message
   */
  private void sendStatusMail(String description, String resultError, String message) {

    this.errorMessage = message;

    sendStatusMail(description, resultError);

  }

  /**
   * sendStatusMail
   * 
   * @param description
   *          the description of the harvesting process
   * @param resultSuccess
   *          the status code
   */
  private void sendStatusMail(String description, String resultSuccess) {

    Mail input = new Mail();

    String sender = HarvesterSettings.getInstance().getString("mail.send.noreply");
    String recipient = HarvesterSettings.getInstance().getString("mail.send.admin");

    String[] toList = new String[] { recipient };
    input.setFrom(sender);
    input.setToList(Arrays.asList(toList));
    input.setSubject("[Metacatalogue] : " + description);

    String body = description + " : " + resultSuccess;

    if (errorMessage != null) {
      body += "<br/><br/>" + this.errorMessage;
    }

    input.setBody(body);

    org.restlet.Response sendMailResponse = null;

    try {

      // riap request to MailAdministration application
      Request req = new Request(Method.POST, RIAPUtils.getRiapBase()
          + HarvesterSettings.getInstance().getString("MAIL_ADMIN_URL") + "/send",
          new ObjectRepresentation<Mail>(input));

      Restlet dispatcher = context.getClientDispatcher();
      sendMailResponse = dispatcher.handle(req);

    }
    catch (Exception e) {
      application.getLogger().warning("Failed to post message to user");
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
    }
    if (sendMailResponse.getStatus().isError()) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Server Error sending email to user.");
    }

  }

  /**
   * exceptionAsString
   * 
   * @param e
   *          the exception
   * @return String
   */
  public String exceptionAsString(Exception e) {

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);

    return sw.toString();
  }

  /**
   * harvest
   * @throws Exception the exception
   */
  public abstract void harvest() throws Exception;

}
