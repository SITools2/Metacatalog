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
package fr.cnes.sitools.metacatalogue.resources.proxyservices;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.routing.Redirector;
import org.restlet.util.Series;

import fr.cnes.sitools.proxy.ProxySettings;

public class RedirectorHttps extends Redirector {

  private String user;
  private String password;
  private boolean withproxy = false;

  /**
   * Constructor
   * 
   * @param context
   *          restlet context
   * @param targetPattern
   *          target pattern
   * @param mode
   *          mode
   */
  public RedirectorHttps(Context context, String targetPattern, int mode) {
    this(context, targetPattern, mode, null, null);

  }

  /**
   * Constructor
   * 
   * @param context
   *          restlet context
   * @param targetPattern
   *          target pattern
   * @param mode
   *          mode
   */
  public RedirectorHttps(Context context, String targetPattern, int mode, String user, String password) {
    super(context, targetPattern, mode);
    this.user = user;
    this.password = password;
  }

  /**
   * Constructor
   * 
   * @param context
   *          restlet context
   * @param targetTemplate
   *          targetTemplate
   */
  public RedirectorHttps(Context context, String targetTemplate, String user, String password) {
    super(context, targetTemplate);
    this.user = user;
    this.password = password;
  }

  @Override
  public void handle(Request request, Response response) {

    if ((ProxySettings.getProxyAuthentication() != null) && request.getProxyChallengeResponse() == null) {
      withproxy = true;
      request.setProxyChallengeResponse(ProxySettings.getProxyAuthentication());
    }

    try {
      super.handle(request, response);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Redirects a given call on the server-side to a next Restlet with a given target reference. In the default
   * implementation, the request HTTP headers, stored in the request's attributes, are removed before dispatching. After
   * dispatching, the response HTTP headers are also removed to prevent conflicts with the main call.
   * 
   * @param next
   *          The next Restlet to forward the call to.
   * @param targetRef
   *          The target reference with URI variables resolved.
   * @param request
   *          The request to handle.
   * @param response
   *          The response to update.
   */
  @Override
  protected void serverRedirect(Restlet next, Reference targetRef, Request request, Response response) {

    if (next == null) {
      getLogger().warning("No next Restlet provided for server redirection to " + targetRef);
    }
    else {

      // Save the base URI if it exists as we might need it for
      // redirections
      Reference resourceRef = request.getResourceRef();
      Reference baseRef = resourceRef.getBaseRef();

      // Reset the protocol and let the dispatcher handle the protocol
      request.setProtocol(null);

      // Update the request to cleanly go to the target URI
      // request.setResourceRef(targetRef);
      // request.getAttributes().remove(HeaderConstants.ATTRIBUTE_HEADERS);

      try {

        Map map = request.getAttributes();

        CloseableHttpResponse closeableResponse = getCloseableResponse(targetRef.toString(), request.getCookies());

        updateResponseWithOriginalCookies(response, closeableResponse);
        updateResponseWithStatusCode(response, closeableResponse);

        SecureOutputRepresentation representation = new SecureOutputRepresentation(
            getOriginalMediaType(closeableResponse), closeableResponse);
        response.setEntity(representation);

      }
      catch (Exception e) {
        e.printStackTrace();
      }

    }
  }

  /**
   * 
   */
  private void updateResponseWithStatusCode(Response response, CloseableHttpResponse closeableResponse) {

    int statusCode = closeableResponse.getStatusLine().getStatusCode();
    Status status = new Status(statusCode);
    response.setStatus(status);

  }

  /**
   * 
   */
  private MediaType getOriginalMediaType(CloseableHttpResponse closeableResponse) {

    Header[] contentHeaders = closeableResponse.getHeaders("Content-type");
    int index = contentHeaders[0].getValue().indexOf(";");
    MediaType mediaType = new MediaType(contentHeaders[0].getValue().substring(0, index));

    return mediaType;

  }

  /**
   * CloseableHttpResponse
   * 
   * @return
   * @throws ClientProtocolException
   * @throws IOException
   */
  public CloseableHttpResponse getCloseableResponse(String url, Series<Cookie> cookies) throws ClientProtocolException,
    IOException {

    HttpClientBuilder httpclientBuilder = HttpClients.custom();

    if (withproxy) {
      CredentialsProvider credsProvider = new BasicCredentialsProvider();
      credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(ProxySettings.getProxyUser(),
          ProxySettings.getProxyPassword()));
      httpclientBuilder.setDefaultCredentialsProvider(credsProvider).build();
    }
    CloseableHttpClient httpclient = httpclientBuilder.build();

    HttpClientContext context = HttpClientContext.create();
    CookieStore cookieStore = new BasicCookieStore();

    Iterator<Cookie> iter = cookies.iterator();

    while (iter.hasNext()) {
      Cookie restCookie = iter.next();
      BasicClientCookie cookie = new BasicClientCookie(restCookie.getName(), restCookie.getValue());
      // cookie.setDomain(restCookie.getDomain());
      cookie.setDomain(getDomainName(url));
      cookie.setPath(restCookie.getPath());
      cookie.setSecure(true);
      // cookie.setExpiryDate(restCookie);
      cookieStore.addCookie(cookie);
    }

    context.setCookieStore(cookieStore);

    HttpGet httpget = new HttpGet(url);

    Builder configBuilder = RequestConfig.custom();

    if (withproxy) {
      HttpHost proxy = new HttpHost(ProxySettings.getProxyHost(), Integer.parseInt(ProxySettings.getProxyPort()),
          "http");
      configBuilder.setProxy(proxy).build();
    }

    RequestConfig config = configBuilder.build();
    httpget.setConfig(config);

    return httpclient.execute(httpget, context);

  }

  /**
   * 
   */
  private void updateResponseWithOriginalCookies(Response response, CloseableHttpResponse closeableResponse) {

    Header[] cookieHeaders = closeableResponse.getHeaders("Set-Cookie");

    Series<CookieSetting> cookieSettings = response.getCookieSettings();

    for (int i = 0; i < cookieHeaders.length; i++) {

      String rawCookie = cookieHeaders[i].getValue();
      String[] rawCookieParams = rawCookie.split(";");

      // j = 0
      String[] rawCookieNameAndValue = splitOnFirst(rawCookieParams[0], "=");
      String cookieName = rawCookieNameAndValue[0].trim();
      String cookieValue = rawCookieNameAndValue[1].trim();
      CookieSetting cS = new CookieSetting(1, cookieName, cookieValue);

      // j > 1
      for (int j = 1; j < rawCookieParams.length; j++) {

        String param = rawCookieParams[j].trim();

        if (param.indexOf("=") >= 0) {

          String[] params = splitOnFirst(param, "=");
          String paramName = params[0];
          String paramValue = params[1];

          if (paramName.equalsIgnoreCase("expires")) {
            // set expirydate ?? / max age ??
          }
          else if (paramName.equalsIgnoreCase("domain")) {
            cS.setDomain(paramValue);
          }
          else if (paramName.equalsIgnoreCase("path")) {
            cS.setPath(paramValue);
          }
          else if (paramName.equalsIgnoreCase("comment")) {
            cS.setPath(paramValue);
          }
        }
      }

      cookieSettings.add(cS);

    }

  }

  /**
   * splitOnFirst
   * 
   * @param str
   * @param c
   * @return
   */
  public static String[] splitOnFirst(String str, String c) {
    int idx = str.indexOf(c);
    String head = str.substring(0, idx);
    String tail = str.substring(idx + 1);
    return new String[] { head, tail };
  }

  /**
   * getDomainName
   * 
   * @param url
   * @return
   * @throws MalformedURLException
   */
  public static String getDomainName(String url) throws MalformedURLException {
    if (!url.startsWith("http") && !url.startsWith("https")) {
      url = "http://" + url;
    }
    URL netUrl = new URL(url);
    String host = netUrl.getHost();
    if (host.startsWith("www")) {
      host = host.substring("www".length() + 1);
    }
    return host;
  }

}
