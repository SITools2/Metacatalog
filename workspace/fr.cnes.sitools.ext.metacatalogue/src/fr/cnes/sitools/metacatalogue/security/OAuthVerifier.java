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

public class OAuthVerifier implements Verifier {

  String url;

  public OAuthVerifier(String _url) {
    super();
    ChallengeScheme scheme = SitoolsChallengeScheme.HTTP_BEARER;
    OAuthHelper helper = new OAuthHelper(scheme, true, true);
    Engine.getInstance().getRegisteredAuthenticators().add(helper); 
    url = _url;
  }

  @Override
  public int verify(Request request, Response response) {

    try {

      Reference ref = new Reference(url);
      
      Client client = new Client(ref.getSchemeProtocol());
      
      Request req  = new Request(Method.GET, url);
        
      ChallengeResponse challengeResponse = request.getChallengeResponse();
      
      req.setChallengeResponse(challengeResponse);
          
      Response resp = client.handle(req);

      JSONObject object = new JSONObject(new JSONTokener(resp.getEntity().getReader()));

      if (object.get("success") != null) {
        request.getClientInfo().setAuthenticated(true);
        return Verifier.RESULT_VALID;
        
      }
      else {
        request.getClientInfo().setAuthenticated(false);
        return Verifier.RESULT_MISSING;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      return 0;
    }

  }

}
