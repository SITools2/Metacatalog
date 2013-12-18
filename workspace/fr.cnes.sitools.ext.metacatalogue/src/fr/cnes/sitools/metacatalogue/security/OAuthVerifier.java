package fr.cnes.sitools.metacatalogue.security;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.security.Verifier;

/**
 * The Class OAuthVerifier.
 * 
 * @author m.gond,tx.chevallier
 */
public class OAuthVerifier implements Verifier {

  /** The url of the Oauth validation service */
  private String url;

  /**
   * Instantiates a new Oauth verifier.
   * 
   * @param url
   *          the url of the Oauth validation service
   */
  public OAuthVerifier(String url) {
    super();
    // Register a new OauthHelper
    ChallengeScheme scheme = SitoolsChallengeScheme.HTTP_BEARER;
    OAuthHelper helper = new OAuthHelper(scheme, true, true);
    Engine.getInstance().getRegisteredAuthenticators().add(helper);

    this.url = url;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.restlet.security.Verifier#verify(org.restlet.Request, org.restlet.Response)
   */
  @Override
  public int verify(Request request, Response response) {

    ChallengeResponse challengeResponse = request.getChallengeResponse();
    if (challengeResponse == null || !challengeResponse.getScheme().equals(SitoolsChallengeScheme.HTTP_BEARER)) {
      return Verifier.RESULT_MISSING;
    }
    try {

      Reference ref = new Reference(url);

      Client client = new Client(ref.getSchemeProtocol());
      Request req = new Request(Method.GET, url);

      req.setChallengeResponse(challengeResponse);
      Response resp = client.handle(req);
      if (resp.getStatus().isSuccess()) {

        JSONObject object = new JSONObject(new JSONTokener(resp.getEntity().getReader()));

        if (object.has("success") && object.get("success") != null) {
          return Verifier.RESULT_VALID;

        }
        else {
          return Verifier.RESULT_MISSING;
        }
      }
      else {
        return Verifier.RESULT_MISSING;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      return Verifier.RESULT_MISSING;
    }

  }

}
