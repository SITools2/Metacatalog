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
package fr.cnes.sitools.metacatalogue.resources.opensearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.request.LukeRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.response.LukeResponse;
import org.apache.solr.client.solrj.response.LukeResponse.FieldInfo;
import org.apache.solr.client.solrj.response.TermsResponse;
import org.apache.solr.client.solrj.response.TermsResponse.Term;
import org.apache.solr.common.luke.FieldFlag;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.wadl.MethodInfo;
import org.restlet.ext.xstream.XstreamRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

import com.thoughtworks.xstream.XStream;

import fr.cnes.sitools.common.XStreamFactory;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.resources.AbstractOpenSearchServiceResource;
import fr.cnes.sitools.metacatalogue.resources.model.Describe;
import fr.cnes.sitools.metacatalogue.resources.model.EnumSon;
import fr.cnes.sitools.metacatalogue.resources.model.ErrorDescription;
import fr.cnes.sitools.metacatalogue.resources.model.Filter;
import fr.cnes.sitools.metacatalogue.resources.model.FilterType;
import fr.cnes.sitools.metacatalogue.resources.model.SolrDataType;

public class OpensearchDescribeResource extends AbstractOpenSearchServiceResource {

  @Override
  public void sitoolsDescribe() {
    setName("OpensearchDescriptionServiceResource");
    setDescription("Describe the opensearch service as an XML file");
  }

  @Get
  @Override
  public Representation get() {
    Describe describe = createDescribe();
    Representation representation = getRepresentation(describe, MediaType.APPLICATION_JSON);
    return representation;
  }

  @Override
  public void describeGet(MethodInfo info) {
    info.setDocumentation("Gets the description of the dataset corresponding to the metacatalog");
    info.setIdentifier("opensearch_describe");
    addStandardResponseInfo(info);
  }

  /**
   * Create a description for the metacatalogue containing all the request parameters, their types and enumeration
   * 
   * @return a description for the metacatalogue
   */
  private Describe createDescribe() {
    Describe describe = new Describe();
    SolrServer server = SolRUtils.getSolRServer(solrCoreUrl);
    if (server == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Solr core : " + solrCoreUrl + " not reachable");
    }

    LukeRequest request = new LukeRequest();
    // request.setNumTerms(maxTopTerms);

    try {
      List<Filter> filters = new ArrayList<Filter>();
      LukeResponse response = request.process(server);
      int numDocs = response.getNumDocs();

      Map<String, LukeResponse.FieldInfo> fields = response.getFieldInfo();

      for (Entry<String, LukeResponse.FieldInfo> field : fields.entrySet()) {
        LukeResponse.FieldInfo fieldInfo = field.getValue();
        String fieldName = fieldInfo.getName();

        boolean indexed = false;

        EnumSet<FieldFlag> flags = FieldInfo.parseFlags(fieldInfo.getSchema());
        indexed = (flags != null && flags.contains(FieldFlag.INDEXED));

        if (indexed && addToDescription(fieldName)) {

          // make a terms query to get the top terms
          SolrQuery query = new SolrQuery();
          query.setRequestHandler("/terms");
          query.setTerms(true);
          query.addTermsField(fieldName);
          query.setTermsLimit(maxTopTerms);
          query.setTermsMinCount(1);

          QueryRequest termsRequest = new QueryRequest(query);
          TermsResponse termsResponse = termsRequest.process(server).getTermsResponse();

          List<Term> terms = termsResponse.getTerms(fieldName);

          Filter filter = new Filter();
          filter.setId(fieldName);
          filter.setTitle(fieldName);
          if (canBeCategorised(terms)) {
            filter.setType(FilterType.enumeration);
            filter.setPopulation(numDocs);
            filter.setSon(createSons(terms));
          }
          else {
            filter.setType(getFilterType(fieldInfo.getType()));
          }
          filters.add(filter);
        }
      }
      describe.setFilters(filters);
      return describe;
    }
    catch (SolrServerException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
    }
    catch (IOException e) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
    }
  }

  private List<EnumSon> createSons(List<Term> terms) {
    List<EnumSon> sons = new ArrayList<EnumSon>();
    for (Term term : terms) {
      EnumSon son = new EnumSon();
      son.setId(term.getTerm());
      son.setTitle(term.getTerm());
      son.setValue(term.getTerm());
      son.setPopulation(term.getFrequency());
      sons.add(son);
    }

    return sons;
  }

  private boolean canBeCategorised(List<Term> terms) {
    return (terms != null && terms.size() > 0 && terms.size() < maxTopTerms);
  }

  private FilterType getFilterType(String type) {
    SolrDataType dataType = SolrDataType.getDataTypeFromSolrDataTypeName(type);
    if (dataType != null) {
      return dataType.getFilterType();
    }
    else {
      return FilterType.text;
    }
  }

  /**
   * Encode a Describe into a Representation according to the given media type.
   * 
   * @param describe
   *          the Describe object to serialize
   * @param media
   *          the media
   * @return Representation
   */
  public Representation getRepresentation(Describe describe, MediaType media) {

    XStream xstream = XStreamFactory.getInstance().getXStream(media);
    xstream.autodetectAnnotations(false);
    xstream.alias("describe", Describe.class);
    xstream.alias("error", ErrorDescription.class);
    XstreamRepresentation<Describe> rep = new XstreamRepresentation<Describe>(media, describe);
    rep.setXstream(xstream);
    return rep;
  }

  @Override
  public boolean addToDescription(String fieldName) {
    return super.addToDescription(fieldName) && !"searchTerms".equals(fieldName);
  }

}
