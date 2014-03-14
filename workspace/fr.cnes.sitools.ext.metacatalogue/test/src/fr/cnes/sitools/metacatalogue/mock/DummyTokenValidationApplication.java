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
package fr.cnes.sitools.metacatalogue.mock;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

import fr.cnes.sitools.common.application.SitoolsApplication;

/**
 * Mock application to simulate a Oauth validation server
 * 
 * @author m.gond, tx.chevallier
 * 
 */
public class DummyTokenValidationApplication extends SitoolsApplication {
  /**
   * Constructor with a Context
   * 
   * @param context
   *          the Context
   */
  public DummyTokenValidationApplication(Context context) {
    super(context);
  }

  @Override
  public void sitoolsDescribe() {
    setName("DummyTokenValidationApplication");
    setDescription("Mock application to simulate a Oauth validation server");
  }

  @Override
  public Restlet createInboundRoot() {
    Router router = new Router(getContext());
    router.attachDefault(DummyTokenValidationResource.class);
    return router;

  }

}
