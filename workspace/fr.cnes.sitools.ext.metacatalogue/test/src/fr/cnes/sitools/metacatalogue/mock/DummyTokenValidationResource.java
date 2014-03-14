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

import org.restlet.data.ChallengeResponse;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;

import fr.cnes.sitools.common.SitoolsResource;
import fr.cnes.sitools.metacatalogue.security.SitoolsChallengeScheme;

/**
 * Mock resource to simulate a Oauth validation server
 * 
 * @author m.gond, tx.chevallier
 * 
 */
public class DummyTokenValidationResource extends SitoolsResource {

  @Override
  public void sitoolsDescribe() {
    setName("DummyTokenValidationResource");
    setDescription("Mock resource to simulate a Oauth validation server");
  }

  @Override
  protected Representation get() throws ResourceException {

    ChallengeResponse challengeResponse = getRequest().getChallengeResponse();
    if (challengeResponse != null) {
      if (challengeResponse.getScheme().equals(SitoolsChallengeScheme.HTTP_BEARER)
          && challengeResponse.getRawValue().equals("test_ok")) {
        return new StringRepresentation("{ \"success\" : \"token is valid\"}");
      }
      else {
        return new StringRepresentation("{ \"error\" : \"token is not valid\"}");
      }

    }
    else {
      return new StringRepresentation("no token provided");
    }

  }

}
