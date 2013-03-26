/*******************************************************************************
 * Copyright 2011 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import fr.cnes.sitools.common.SitoolsResource;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginParameter;

/**
 * Abstract class for opensearch services
 * 
 * @author m.gond
 * 
 */
public abstract class AbstractOpenSearchServiceResource extends SitoolsResource {

  /** The default Max number of terms to look for */
  protected static final int DEFAULT_MAX_TOP_TERMS = 30;
  /** The metacatalogue Application */
  protected MetacatalogueApplication application;
  /** The solr core url */
  protected String solrCoreUrl;
  /** The actual Max number of terms to look for */
  protected int maxTopTerms;

  @Override
  public void doInit() {
    super.doInit();
    application = (MetacatalogueApplication) getApplication();
    solrCoreUrl = application.getSolrCoreUrl();

    ApplicationPluginParameter maxTopTermsParam = application.getParameter("maxTopTerms");
    if (maxTopTermsParam == null || maxTopTermsParam.getValue() == null) {
      maxTopTerms = DEFAULT_MAX_TOP_TERMS;
    }
    else {
      try {
        maxTopTerms = new Integer(maxTopTermsParam.getValue());
      }
      catch (Exception e) {
        maxTopTerms = DEFAULT_MAX_TOP_TERMS;
      }
    }

  }

  /**
   * Check if the the given field name is to be added to the description
   * 
   * @param fieldName
   *          the name of the field
   * @return true if the field corresponding to the following name has to be added to the description, false otherwise
   */
  public boolean addToDescription(String fieldName) {
    MetacatalogField metaField = MetacatalogField.getField(fieldName);
    boolean addToDescription = false;
    if (metaField != null) {
      addToDescription = !metaField.isInspire()
          && !metaField.isMetacatalogIntern()
          && !(MetacatalogField.CHARACTERISATION_AXIS_TEMPORAL_AXIS_MIN.equals(metaField) || MetacatalogField.CHARACTERISATION_AXIS_TEMPORAL_AXIS_MAX
              .equals(metaField));
    }
    return addToDescription;
  }
}