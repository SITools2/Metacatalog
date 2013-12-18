package fr.cnes.sitools.metacatalogue.security;

import org.restlet.data.ChallengeScheme;
import org.restlet.engine.security.AuthenticatorHelper;

public class OAuthHelper extends AuthenticatorHelper {

	public OAuthHelper(ChallengeScheme challengeScheme, boolean clientSide,
			boolean serverSide) {
		super(challengeScheme, clientSide, serverSide);
	}

}
