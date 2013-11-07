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
package fr.cnes.sitools.metacatalogue.resources.opensearch;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.LukeResponse.FieldInfo;
import org.apache.solr.common.luke.FieldFlag;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.resources.AbstractOpenSearchServiceResource;

public class OpensearchDescriptionServiceResource extends AbstractOpenSearchServiceResource {

  @Override
  public void sitoolsDescribe() {
    setName("OpensearchDescriptionServiceResource");
    setDescription("Describe the opensearch service as an XML file");
  }

  @Get
  @Override
  public Representation get() {

    Map<String, Object> params = new HashMap<String, Object>();
    fillOpensearchParameters(params);

    Representation metadataFtl = new ClientResource(LocalReference.createClapReference(getClass().getPackage()) + "/openSearchDescription.ftl").get();
    TemplateRepresentation tpl = new TemplateRepresentation(metadataFtl, params, MediaType.TEXT_XML);
    return tpl;
  }

  @Override
  public void describeGet(MethodInfo info) {
    info.setDocumentation("Gets the description of the opensearch service as an XML file");
    info.setIdentifier("opensearch_description_service");
    addStandardResponseInfo(info);
    addStandardInternalServerErrorInfo(info);
  }

  private void fillOpensearchParameters(Map<String, Object> params) {
    params.put("shortName", application.getParameter("shortName").getValue());
    params.put("description", application.getParameter("description").getValue());

    params.put("templateURL", buildTemplateURL());
    String rootUrl = getSitoolsSetting("Starter.PUBLIC_HOST_DOMAIN") + application.getModel().getUrlAttach();
    params.put("rootURL", rootUrl);

    // params.put("describe", getSitoolsSetting("Starter.PUBLIC_HOST_DOMAIN") + getPluginModel().getUrlAttach()
    // + "/describe");
    if (!application.getParameter("contact").getValue().isEmpty()) {
      params.put("contact", application.getParameter("contact").getValue());
    }
    if (!isParameterEmpty("tags")) {
      params.put("tags", application.getParameter("tags").getValue());
    }
    if (!isParameterEmpty("longName")) {
      params.put("longName", application.getParameter("longName").getValue());
    }
    if (!isParameterEmpty("imagePng")) {
      params.put("imagePng", application.getParameter("imagePng").getValue());
    }
    if (!isParameterEmpty("imageIcon")) {
      params.put("imageIcon", application.getParameter("imageIcon").getValue());
    }
    if (!isParameterEmpty("syndicationRight")) {
      params.put("syndicationRight", application.getParameter("syndicationRight").getValue());
    }
    // params.put("referenceSystem", application.getParameter("referenceSystem").getValue());

  }

  public boolean isParameterEmpty(String parameterName) {
    return application.getParameter(parameterName) == null || application.getParameter(parameterName).getValue() == null
        || application.getParameter(parameterName).getValue().isEmpty();
  }

  private String buildTemplateURL() {

    SolrServer server = SolRUtils.getSolRServer(solrCoreUrl);
    if (server == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Solr core : " + solrCoreUrl + " not reachable");
    }

    LukeRequest request = new LukeRequest();
    request.setNumTerms(maxTopTerms);

    try {
      String description = "/search?q={searchTerms}&amp;format=json";

      description += addQueryParameter(OpenSearchQuery.START_PAGE, false);
      description += addQueryParameter(OpenSearchQuery.START_INDEX, false);
      description += addQueryParameter(OpenSearchQuery.COUNT, false);
      description += addQueryParameter(OpenSearchQuery.GEO_BOX, false);

      description += addQueryParameter(OpenSearchQuery.LANGUAGE, false);
      description += addQueryParameter(OpenSearchQuery.TIME_START, false);
      description += addQueryParameter(OpenSearchQuery.TIME_END, false);
      description += addQueryParameter(OpenSearchQuery.MODIFIED, false);

      LukeResponse response = request.process(server);
      Map<String, LukeResponse.FieldInfo> fields = response.getFieldInfo();
      for (Entry<String, LukeResponse.FieldInfo> field : fields.entrySet()) {
        LukeResponse.FieldInfo fieldInfo = field.getValue();
        String fieldName = fieldInfo.getName();

        boolean indexed = false;
        EnumSet<FieldFlag> flags = FieldInfo.parseFlags(fieldInfo.getSchema());
        if (flags != null && flags.contains(FieldFlag.INDEXED)) {
          indexed = true;
        }
        if (indexed && addToDescription(fieldName)) {
          description += "&amp;" + fieldName + "={ptsc:" + fieldName + "?}";
        }
      }

      return description;
    }
    catch (SolrServerException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
    }
    catch (IOException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
    }

  }

  public String addQueryParameter(OpenSearchQuery param, boolean mandatory) {
    return addQueryParameter(param.getParamName(), param.getField(), mandatory);
  }

  public String addQueryParameter(String paramName, String opensearchParamName, boolean mandatory) {
    String param = "&amp;" + paramName + "={" + opensearchParamName;
    if (!mandatory) {
      param += "?";
    }
    param += "}";
    return param;
  }
}
