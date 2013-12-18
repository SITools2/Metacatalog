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
package fr.cnes.sitools.metacatalogue.mock;

import org.restlet.Context;
import org.restlet.data.ChallengeScheme;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

public class OAuthChallengeAuthenticator extends ChallengeAuthenticator {

  public OAuthChallengeAuthenticator(Context context, boolean optional, ChallengeScheme challengeScheme, String realm) {
    super(context, optional, challengeScheme, realm);
  }

  public OAuthChallengeAuthenticator(Context context, boolean optional, ChallengeScheme challengeScheme, String realm,
      Verifier verifier) {
    super(context, optional, challengeScheme, realm, verifier);
  }

  public OAuthChallengeAuthenticator(Context context, ChallengeScheme challengeScheme, String realm) {
    super(context, challengeScheme, realm);
  }

}
