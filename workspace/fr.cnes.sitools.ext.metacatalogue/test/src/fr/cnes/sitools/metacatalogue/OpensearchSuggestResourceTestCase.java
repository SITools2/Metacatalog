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
package fr.cnes.sitools.metacatalogue;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;

import fr.cnes.sitools.AbstractSitoolsTestCase;
import fr.cnes.sitools.common.SitoolsSettings;
import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.metacatalogue.application.MetacatalogueApplication;

public class OpensearchSuggestResourceTestCase extends AbstractSitoolsTestCase {

  private static final String METACATALOGUE_APP_URL = "/metacatalogue";

  /**
   * Root path for storing files
   */
  public static final String TEST_FILES_REPOSITORY = SitoolsSettings.getInstance().getString("Tests.STORE_DIR");

  /**
   * Restlet Component for server
   */
  private Component component = null;

  /**
   * relative url for dataset management REST API
   * 
   * @return url
   */
  protected String getAttachUrl() {
    return SITOOLS_URL + METACATALOGUE_APP_URL;
  }

  /**
   * absolute url for dataset management REST API
   * 
   * @return url
   */
  protected String getBaseUrl() {
    return super.getBaseUrl() + METACATALOGUE_APP_URL;
  }

  @Before
  @Override
  /**
   * Init and Start a server with GraphApplication
   * 
   * @throws java.lang.Exception
   */
  public void setUp() throws Exception {
    if (this.component == null) {
      SitoolsSettings settings = SitoolsSettings.getInstance();
      this.component = new Component();
      this.component.getServers().add(Protocol.HTTP, getTestPort());
      this.component.getClients().add(Protocol.HTTP);
      this.component.getClients().add(Protocol.FILE);
      this.component.getClients().add(Protocol.CLAP);

      // Context
      Context ctx = this.component.getContext().createChildContext();
      settings.setStoreDIR(TEST_FILES_REPOSITORY);
      settings.setStores(new HashMap<String, Object>());
      ctx.getAttributes().put(ContextAttributes.SETTINGS, settings);

      ctx.getAttributes().put(ContextAttributes.APP_ATTACH_REF, getAttachUrl());
      MetacatalogueApplication app = new MetacatalogueApplication(ctx);
      this.component.getDefaultHost().attach(getAttachUrl(), app);
    }

    if (!this.component.isStarted()) {
      this.component.start();
    }
    
  }
  
  @Test
  public void suggestTest() {
    // pre requis:
    // Coeur SolR de fuzzy query Concept (peut être cachée deriere une API de recherche de concept) => A tester
    // API de recherche de broaders/narrowers dans le thesaurus => A tester    
  }
  
  

}
