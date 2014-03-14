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
package fr.cnes.sitools.metacatalogue.resources;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.restlet.Context;
import org.restlet.data.Language;
import org.restlet.data.Preference;
import org.restlet.data.Status;

import fr.cnes.sitools.common.SitoolsResource;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueAccessApplication;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginParameter;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;

public abstract class AbstractSearchResource extends SitoolsResource {

  /** The parent application */
  protected MetacatalogueAccessApplication application;

  protected String thesaurusName;

  private String language;

  private List<Language> preferedLanguages;

  protected List<String> thesaurusFacetFields;

  @Override
  protected void doInit() {
    super.doInit();

    application = (MetacatalogueAccessApplication) getApplication();

    ApplicationPluginParameter thesaurusParam = application.getParameter("thesaurus");
    if (thesaurusParam == null || thesaurusParam.getName() == null || thesaurusParam.getName().isEmpty()) {
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "No thesaurus parameter defined");
      return;
    }

    thesaurusName = thesaurusParam.getValue();

    extractLanguage();

    ApplicationPluginParameter thesaurusFacetFieldsParam = application.getParameter("thesaurusFacetFields");
    if (thesaurusFacetFieldsParam == null || thesaurusFacetFieldsParam.getName() == null
        || thesaurusFacetFieldsParam.getName().isEmpty()) {
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "No thesaurusFacetFieldsParam parameter defined");
      return;
    }

    thesaurusFacetFields = getThesaurusFacetFields(thesaurusFacetFieldsParam.getValue());

  }

  private void extractLanguage() {
    preferedLanguages = new ArrayList<Language>();

    preferedLanguages.add(Language.FRENCH);
    preferedLanguages.add(Language.ENGLISH);

    language = getRequest().getResourceRef().getQueryAsForm().getFirstValue("lang", null);
    if (language == null) {
      List<Preference<Language>> acceptedLanguage = getRequest().getClientInfo().getAcceptedLanguages();
      for (Preference<Language> preference : acceptedLanguage) {
        Language lang = preference.getMetadata();
        if (preferedLanguages.contains(lang)) {
          language = lang.getName();
          break;
        }
      }
    }
    else {
      // remove the lang parameter as we don't need it anymore
      getRequest().getResourceRef().getQueryAsForm().removeFirst("lang");
    }
    // check that the language is ok
    Language lang = Language.valueOf(language);
    if (language == null || lang == null || !preferedLanguages.contains(lang)) {
      language = preferedLanguages.get(0).getName();
    }
  }

  protected String getLanguage() {
    return language;
  }

  protected SolrServer getSolrServer(Context context) {
    SolrServer server = (SolrServer) context.getAttributes().get("INDEXER_SERVER");
    return server;
  }

  protected ThesaurusSearcher getThesaurusSearcher() throws IOException {
    return new ThesaurusSearcher(thesaurusName);
  }

  private List<String> getThesaurusFacetFields(String fields) {
    String[] fieldsArray = fields.split(",");
    List<String> fieldsList = new ArrayList<String>();
    for (String field : fieldsArray) {
      fieldsList.add(field.trim());
    }
    return fieldsList;
  }

}
