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
package fr.cnes.sitools.metacatalogue.resources.suggest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;

import fr.cnes.sitools.common.SitoolsResource;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginParameter;
import fr.cnes.sitools.thesaurus.Concept;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

public class OpensearchSuggestResource extends SitoolsResource {

  private String thesaurusName;

  @Override
  public void sitoolsDescribe() {
    setName("OpensearchSuggestResource");
    setDescription("Get suggestions");
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.common.SitoolsResource#doInit()
   */
  @Override
  protected void doInit() {
    super.doInit();

    MetacatalogueApplication application = (MetacatalogueApplication) getApplication();
    ApplicationPluginParameter thesaurusParam = application.getParameter("thesaurus");
    if (thesaurusParam == null || thesaurusParam.getName() == null || thesaurusParam.getName().isEmpty()) {
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "No thesaurus parameter defined");
      return;
    }

    thesaurusName = thesaurusParam.getValue();

  }

  @Get
  public List<SuggestDTO> suggest(Variant variant) {
    String query = getRequest().getResourceRef().getQueryAsForm().getFirstValue("q");
    if (query == null || query.isEmpty()) {
      getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No suggestion parameter");
      return null;
    }

    try {
      ThesaurusSearcher searcher = new ThesaurusSearcher(thesaurusName);
      List<SuggestDTO> suggest = new ArrayList<SuggestDTO>();

      List<Concept> concepts = searcher.searchNarrowersBroader(query + "*");
      for (Concept concept : concepts) {
        SuggestDTO suggestDTO = new SuggestDTO();
        suggestDTO.setSuggestion(concept.getProperties().get("prefLabelNarrower").toString());
        suggest.add(suggestDTO);
      }
      return suggest;
    }
    catch (IOException e) {
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Cannot read Thesaurs : " + thesaurusName);
      return null;
    }

  }

}
