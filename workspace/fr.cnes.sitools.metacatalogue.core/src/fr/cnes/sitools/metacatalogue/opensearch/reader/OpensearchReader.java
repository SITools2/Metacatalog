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
package fr.cnes.sitools.metacatalogue.opensearch.reader;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minidev.json.JSONObject;

import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.engine.util.DateUtils;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.jayway.jsonpath.JsonPath;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.HarvesterSource;
import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.util.ClientResourceProxy;

public class OpensearchReader extends HarvesterStep {

  private static final String START_INDEX_PARAM_NAME = "{startIndex?}";

  private static final String COUNT_PARAM_NAME = "{count?}";

  private static final String MODIFIED_KEY_NAME = "modified";

  /** The page size */
  private Integer pageSize = 100;
  /** The source to harvest */
  private HarvesterSource source;

  /** Current harvester configuration */
  private HarvesterModel conf;

  /** The logger */
  private Logger logger;
  /** The Context */
  private Context context;

  public OpensearchReader(HarvesterModel conf, Context context) {
    this.source = conf.getSource();
    this.conf = conf;
    this.context = context;

  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {
    logger = getLogger(context);
    Integer totalResultsRead = 0;
    Integer nextPage = 1;
    Integer nbFeaturesReturned = 0;

    Reference sourceRef = new Reference(source.getUrl());
    addOpensearchQueryParams(sourceRef, nextPage, pageSize, this.conf.getLastHarvest());

    do {
      data = new MetadataContainer();

      ClientResourceProxy clientResourceProxy = new ClientResourceProxy(sourceRef, Method.GET);
      ClientResource clientResource = clientResourceProxy.getClientResource();
      clientResource.setRetryOnError(false);
      clientResource.setRetryAttempts(0);
      clientResource.setRetryDelay(50);

      logger.log(Level.INFO, "Query opensearch service : " + clientResource.getRequest().getResourceRef().toString());

      Representation repr = clientResource.get(MediaType.APPLICATION_JSON);

      try {
        String json = repr.getText();
        List<Object> features = JsonPath.read(json, "$.features");

        if (features == null) {
          // if there is an error we log it and exit the loop otherwise it will loop infinitely
          logger.warning("Cannot find features node, there has been an error :\n");
          break;
        }

        nbFeaturesReturned = features.size();
        totalResultsRead += nbFeaturesReturned;

        data.setJsonData(json);

        if (nbFeaturesReturned != null && nbFeaturesReturned > 0) {
          String url = extractNextUrl(json);
          if (url != null) {
            sourceRef = new Reference(url);
          }
          else {
            throw new ProcessException("Cannot find next url / aborting harvesting");
          }
        }

        // if (nbRecords == null) {
        // nbRecords = Integer.parseInt(searchResults.getAttributeValue("numberOfRecordsMatched"));
        // logger.info("Number of records found = " + nbRecords);
        // HarvesterResult result = (HarvesterResult) context.getAttributes().get(ContextAttributes.RESULT);
        // result.setNbDocumentsRetrieved(result.getNbDocumentsIndexed() + nbRecords);
        // }
        //
        // nextRecord = Integer.parseInt(searchResults.getAttributeValue("nextRecord"));
        //
        // data.setXmlData(searchResults);
        HarvestStatus status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);
        status.setNbDocumentsRetrieved(status.getNbDocumentsRetrieved() + features.size());

        if (next != null) {
          next.execute(data);
        }
        nextPage += nbFeaturesReturned;

      }
      catch (IOException e) {
        logger.log(Level.WARNING, e.getLocalizedMessage(), e);
        throw new ProcessException(e);
      }

    } while (totalResultsRead != null && nbFeaturesReturned != null && nbFeaturesReturned != 0);

    this.end();

  }

  @Override
  public void end() throws ProcessException {
    if (next != null) {
      this.next.end();
    }
  }

  @Override
  public CheckStepsInformation check() {
    if (next != null) {
      CheckStepsInformation ok = this.next.check();
      if (!ok.isOk()) {
        return ok;
      }
    }
    return new CheckStepsInformation(true);
  }

  /**
   * Adds the opensearch query params.
   * 
   * @param reference
   *          the reference
   * @param start
   *          the start
   * @param maxRows
   *          the max rows
   * @param lastHarvest
   *          the last harvest
   */
  private void addOpensearchQueryParams(Reference reference, int start, int maxRows, Date lastHarvest) {
    // reference.addQueryParameter("format", "json");
    Form form = reference.getQueryAsForm();

    for (Iterator<Parameter> iterator = form.iterator(); iterator.hasNext();) {
      Parameter parameter = iterator.next();
      if (START_INDEX_PARAM_NAME.equals(parameter.getValue())) {
        parameter.setValue(new Integer(start).toString());
      }
      else if (COUNT_PARAM_NAME.equals(parameter.getValue())) {
        parameter.setValue(new Integer(maxRows).toString());
      }
      else if (lastHarvest != null && MODIFIED_KEY_NAME.equals(parameter.getName())) {
        String dateFormated = DateUtils.format(lastHarvest, DateUtils.FORMAT_RFC_3339);
        parameter.setValue(dateFormated);
      }
      else if (isTemplate(parameter)) {
        iterator.remove();
      }
    }
    reference.setQuery(form.getQueryString());
  }

  /**
   * check that the given parameter is a template
   * 
   * @param param
   *          the param
   * @return true if the given parameter is a template, false otherwise
   */
  private boolean isTemplate(Parameter param) {
    String paramValue = param.getValue();
    return paramValue.startsWith("{") && paramValue.endsWith("}");
  }

  /**
   * Extract next url.
   * 
   * @param json
   *          the json
   * @return the string
   */
  private String extractNextUrl(String json) {
    List<JSONObject> links = JsonPath.read(json, "$.links");
    if (links == null) {
      return null;
    }
    String url = null;
    for (JSONObject link : links) {
      if ("next".equals(link.get("rel"))) {
        url = (String) link.get("href");
      }
    }
    return url;
  }

}
