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
package fr.cnes.sitools.metacatalogue.model;

import java.util.Date;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class HarvestStatus.
 */
public class HarvestStatus {

  /** The Constant RESULT_SUCCESS. */
  public static final String RESULT_SUCCESS = "SUCCESS";

  /** The Constant RESULT_STOPPED_USER. */
  public static final String RESULT_STOPPED_USER = "STOPPED_USER";

  /** The Constant RESULT_ERROR. */
  public static final String RESULT_ERROR = "ERROR";

  /** The Constant STATUS_READY. */
  public static final String STATUS_READY = "INACTIVE";

  /** The Constant STATUS_RUNNING. */
  public static final String STATUS_RUNNING = "ACTIVE";

  /** The nb documents retrieved. */
  private int nbDocumentsRetrieved = 0;

  /** The nb documents indexed. */
  private int nbDocumentsIndexed = 0;

  /** The nb documents invalid. */
  private int nbDocumentsInvalid = 0;

  /** The result. */
  private String result;

  /** The error cause. */
  private String errorCause;

  /** The url. */
  private String url;

  /** The status. */
  private String status;

  /** The properties. */
  private Map<String, Object> properties;

  /** The start date. */
  private Date startDate;

  /** The end date. */
  private Date endDate;

  /** The file name of the loggerUsed */
  private String loggerFileName;

  /**
   * Gets the nbDocumentsRetrieved value.
   * 
   * @return the nbDocumentsRetrieved
   */
  public int getNbDocumentsRetrieved() {
    return nbDocumentsRetrieved;
  }

  /**
   * Sets the value of nbDocumentsRetrieved.
   * 
   * @param nbDocumentsRetrieved
   *          the nbDocumentsRetrieved to set
   */
  public void setNbDocumentsRetrieved(int nbDocumentsRetrieved) {
    this.nbDocumentsRetrieved = nbDocumentsRetrieved;
  }

  /**
   * Gets the nbDocumentsIndexed value.
   * 
   * @return the nbDocumentsIndexed
   */
  public int getNbDocumentsIndexed() {
    return nbDocumentsIndexed;
  }

  /**
   * Sets the value of nbDocumentsIndexed.
   * 
   * @param nbDocumentsIndexed
   *          the nbDocumentsIndexed to set
   */
  public void setNbDocumentsIndexed(int nbDocumentsIndexed) {
    this.nbDocumentsIndexed = nbDocumentsIndexed;
  }

  /**
   * Gets the nbDocumentsInvalid value.
   * 
   * @return the nbDocumentsInvalid
   */
  public int getNbDocumentsInvalid() {
    return nbDocumentsInvalid;
  }

  /**
   * Sets the value of nbDocumentsInvalid.
   * 
   * @param nbDocumentsInvalid
   *          the nbDocumentsInvalid to set
   */
  public void setNbDocumentsInvalid(int nbDocumentsInvalid) {
    this.nbDocumentsInvalid = nbDocumentsInvalid;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "HarvesterResult [nbDocumentsRetrieved=" + nbDocumentsRetrieved + ", success=" + isSuccess()
        + ", nbDocumentsIndexed=" + nbDocumentsIndexed + ", nbDocumentsInvalid=" + nbDocumentsInvalid + "]";
  }

  /**
   * Gets the result value.
   * 
   * @return the result
   */
  public String getResult() {
    return result;
  }

  /**
   * Sets the value of result.
   * 
   * @param result
   *          the result to set
   */
  public void setResult(String result) {
    this.result = result;
  }

  /**
   * true if the result is a success, false otherwise.
   * 
   * @return true if the result is a success, false otherwise
   */
  public boolean isSuccess() {
    return RESULT_SUCCESS.equals(result);
  }

  /**
   * Gets the errorCause value.
   * 
   * @return the errorCause
   */
  public String getErrorCause() {
    return errorCause;
  }

  /**
   * Sets the value of errorCause.
   * 
   * @param errorCause
   *          the errorCause to set
   */
  public void setErrorCause(String errorCause) {
    this.errorCause = errorCause;
  }

  /**
   * Gets the url value.
   * 
   * @return the url
   */
  public String getUrl() {
    return url;
  }

  /**
   * Sets the value of url.
   * 
   * @param url
   *          the url to set
   */
  public void setUrl(String url) {
    this.url = url;
  }

  /**
   * Gets the status value.
   * 
   * @return the status
   */
  public String getStatus() {
    return status;
  }

  /**
   * Sets the value of status.
   * 
   * @param status
   *          the status to set
   */
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Gets the properties value.
   * 
   * @return the properties
   */
  public Map<String, Object> getProperties() {
    return properties;
  }

  /**
   * Sets the value of properties.
   * 
   * @param properties
   *          the properties to set
   */
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  /**
   * Gets the startDate value.
   * 
   * @return the startDate
   */
  public Date getStartDate() {
    return startDate;
  }

  /**
   * Sets the value of startDate.
   * 
   * @param startDate
   *          the startDate to set
   */
  public void setStartDate(Date startDate) {
    this.startDate = startDate;
  }

  /**
   * Gets the endDate value.
   * 
   * @return the endDate
   */
  public Date getEndDate() {
    return endDate;
  }

  /**
   * Sets the value of endDate.
   * 
   * @param endDate
   *          the endDate to set
   */
  public void setEndDate(Date endDate) {
    this.endDate = endDate;
  }

  /**
   * Gets the loggerFileName value
   * 
   * @return the loggerFileName
   */
  public String getLoggerFileName() {
    return loggerFileName;
  }

  /**
   * Sets the value of loggerFileName
   * 
   * @param loggerFileName
   *          the loggerFileName to set
   */
  public void setLoggerFileName(String loggerFileName) {
    this.loggerFileName = loggerFileName;
  }

}
