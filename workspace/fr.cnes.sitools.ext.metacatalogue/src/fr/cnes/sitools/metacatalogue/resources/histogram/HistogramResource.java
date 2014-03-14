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
package fr.cnes.sitools.metacatalogue.resources.histogram;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.json.JSONException;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.wadl.ParameterInfo;
import org.restlet.ext.wadl.ParameterStyle;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.metacatalogue.resources.AbstractOpenSearchServiceResource;

/**
 * Provides a distribution of number of records by date range.
 * 
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public class HistogramResource extends AbstractOpenSearchServiceResource {

  private Histogram histo;

  @Override
  public void doInit() {
    super.doInit();
    try {
      String numBinsStr = String.valueOf(this.getRequestAttributes().get("numbins"));
      if (numBinsStr == null || "".equals(numBinsStr) || "null".equals(numBinsStr)) {
        throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "numbins parameter must be specified");
      }
      int numBins = Integer.valueOf(numBinsStr);
      histo = new Histogram(numBins, this.solrCoreUrl);
    }
    catch (ParseException ex) {
      Engine.getLogger(HistogramResource.class.getName()).log(Level.SEVERE, null, ex);
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
    }
    catch (IOException ex) {
      Engine.getLogger(HistogramResource.class.getName()).log(Level.SEVERE, null, ex);
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
    }
    catch (JSONException ex) {
      Engine.getLogger(HistogramResource.class.getName()).log(Level.SEVERE, null, ex);
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
    }
  }

  @Get
  @Override
  public Representation get() {
    try {
      return this.histo.getHistogram();
    }
    catch (ParseException ex) {
      Engine.getLogger(HistogramResource.class.getName()).log(Level.SEVERE, null, ex);
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, ex.getMessage());
    }
  }

  @Override
  protected void describeGet(MethodInfo info) {
    info.setDocumentation("Gets the distribution of date corresponding to the whole metacatalog");
    info.setIdentifier("histo");
    addStandardGetRequestInfo(info);
    List<ParameterInfo> parametersInfo = new ArrayList<ParameterInfo>();
    parametersInfo.add(new ParameterInfo("numbins", true, "Integer", ParameterStyle.TEMPLATE, "Number of bins in the distribution"));
    info.getRequest().setParameters(parametersInfo);
    addStandardResponseInfo(info);
    addStandardInternalServerErrorInfo(info);
  }

  @Override
  public void sitoolsDescribe() {
    setName("HistogramServiceResource");
    setDescription("Represents a distribution of date");
  }

}
