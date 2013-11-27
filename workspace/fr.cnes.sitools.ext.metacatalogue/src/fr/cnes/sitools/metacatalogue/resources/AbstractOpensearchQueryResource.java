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
package fr.cnes.sitools.metacatalogue.resources;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.SitoolsResource;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginParameter;

public abstract class AbstractOpensearchQueryResource extends SitoolsResource {

  /** The parent application */
  protected MetacatalogueApplication application;

  /** The url of the solr core to query */
  protected String solrCoreUrl;

  protected String thesaurusName;

  @Override
  protected void doInit() {
    super.doInit();

    application = (MetacatalogueApplication) getApplication();

    ApplicationPluginParameter solrCoreUrlParameter = application.getParameter("metacatalogSolrCore");
    if (solrCoreUrlParameter == null || solrCoreUrlParameter.getValue() == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No solr core url defined, cannot perform search");
    }

    ApplicationPluginParameter solrCoreNameParameter = application.getParameter("metacatalogSolrName");
    if (solrCoreNameParameter == null || solrCoreNameParameter.getValue() == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No solr core name defined, cannot perform search");
    }
    solrCoreUrl = solrCoreUrlParameter.getValue() + "/" + solrCoreNameParameter.getValue();

    ApplicationPluginParameter thesaurusParam = application.getParameter("thesaurus");
    if (thesaurusParam == null || thesaurusParam.getName() == null || thesaurusParam.getName().isEmpty()) {
      getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "No thesaurus parameter defined");
      return;
    }

    thesaurusName = thesaurusParam.getValue();

  }

}
