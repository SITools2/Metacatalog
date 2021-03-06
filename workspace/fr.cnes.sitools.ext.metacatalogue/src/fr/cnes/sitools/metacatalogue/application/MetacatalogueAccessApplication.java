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
package fr.cnes.sitools.metacatalogue.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.client.solrj.SolrServer;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.ExtendedResourceInfo;
import org.restlet.ext.wadl.ResourceInfo;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Extractor;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;
import org.restlet.security.Authorizer;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.Verifier;

import fr.cnes.sitools.common.application.ContextAttributes;
import fr.cnes.sitools.common.exception.SitoolsException;
import fr.cnes.sitools.common.model.Category;
import fr.cnes.sitools.common.validator.ConstraintViolation;
import fr.cnes.sitools.common.validator.ConstraintViolationLevel;
import fr.cnes.sitools.common.validator.Validator;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.resources.histogram.HistogramResource;
import fr.cnes.sitools.metacatalogue.resources.mdweb.MdWebSearchResource;
import fr.cnes.sitools.metacatalogue.resources.opensearch.OpensearchDescribeResource;
import fr.cnes.sitools.metacatalogue.resources.opensearch.OpensearchDescriptionServiceResource;
import fr.cnes.sitools.metacatalogue.resources.opensearch.OpensearchSearchResource;
import fr.cnes.sitools.metacatalogue.resources.opensearch.OpensearchSearchWithThesaurusResource;
import fr.cnes.sitools.metacatalogue.resources.suggest.OpensearchSuggestResource;
import fr.cnes.sitools.metacatalogue.security.OAuthVerifier;
import fr.cnes.sitools.metacatalogue.security.SitoolsChallengeScheme;
import fr.cnes.sitools.plugins.applications.business.AbstractApplicationPlugin;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginModel;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginParameter;
import fr.cnes.sitools.proxy.RedirectorProxy;
import fr.cnes.sitools.server.Consts;

/**
 * Metacatalog application for Opensearch exposition
 * 
 * @author m.gond
 * 
 */
public class MetacatalogueAccessApplication extends AbstractApplicationPlugin {

  /**
   * Default constructor
   * 
   * @param context
   *          context
   */
  public MetacatalogueAccessApplication(Context context) {
    super(context);
    constructor();
  }

  /** Default constructor */
  public MetacatalogueAccessApplication() {
    super();
    constructor();
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.plugins.applications.business.AbstractApplicationPlugin#start()
   */
  @Override
  public synchronized void start() throws Exception {
    Context context = getContext();
    // get the solr server from the context or put it inside if not
    if (!context.getAttributes().containsKey("INDEXER_SERVER")) {
      String solrCoreUrl = getSolrCoreUrl();
      SolrServer server = SolRUtils.getSolRServer(solrCoreUrl);
      if (server == null) {
        throw new SitoolsException("Solr core : " + solrCoreUrl + " not reachable");
      }
      context.getAttributes().put("INDEXER_SERVER", server);
    }
    super.start();

  }

  /**
   * Constructor with context and model of the application configuration. This is the constructor called when the
   * application is started
   * 
   * @param context
   *          Restlet context
   * @param model
   *          model
   */
  public MetacatalogueAccessApplication(Context context, ApplicationPluginModel model) {
    super(context, model);

    ApplicationPluginParameter appSSO = getParameter("SSO_Token_validator_URL");
    if (appSSO != null && appSSO.getName() != null) {
      ApplicationPluginParameter ssoCache = getParameter("ssoCache");
      boolean withCache = Boolean.parseBoolean(ssoCache.getValue());
      Verifier verifier;
      if (withCache) {
        ApplicationPluginParameter cacheSizeParam = getParameter("cacheSize");
        int cacheSize = Integer.parseInt(cacheSizeParam.getValue());

        ApplicationPluginParameter cacheExpireTimeParam = getParameter("expireCacheTime");
        int cacheExpireTime = Integer.parseInt(cacheExpireTimeParam.getValue());
        verifier = new OAuthVerifier(appSSO.getValue(), withCache, cacheSize, cacheExpireTime);
      }
      else {
        verifier = new OAuthVerifier(appSSO.getValue());
      }
      // define custom SSO authorizer
      ChallengeAuthenticator oauthAuthenticator = new ChallengeAuthenticator(getContext(), true,
          SitoolsChallengeScheme.HTTP_BEARER, null, verifier);
      Authorizer authorizer = Authorizer.ALWAYS;

      getContext().getAttributes().put(ContextAttributes.CUSTOM_CHALLENGE_AUTHENTICATOR, oauthAuthenticator);
      getContext().getAttributes().put(ContextAttributes.CUSTOM_AUTHORIZER, authorizer);
    }

  }

  @Override
  public void sitoolsDescribe() {
    setName("MetacatalogueAccessApplication");
    setDescription("The metacatalogue application to access/search data");
    this.setAuthor("AKKA Technologies");
    this.setOwner("CNES");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.restlet.Application#createInboundRoot()
   */
  @Override
  public Restlet createInboundRoot() {

    Router router = new Router(getContext());

    // Opensearch API exposition
    router.attach("/search", OpensearchSearchResource.class);
    router.attach("/searchThesaurus", OpensearchSearchWithThesaurusResource.class);
    router.attach("/opensearch.xml", OpensearchDescriptionServiceResource.class);
    router.attach("/describe", OpensearchDescribeResource.class);
    router.attach("/suggest", OpensearchSuggestResource.class);

    // MDWEB API exposition
    router.attach("/mdweb/search", MdWebSearchResource.class);
    
    String urlSolrSearch = getSolrCoreUrl() + "/select?{rq}";
    router.attach("/select", new Redirector(getContext(), urlSolrSearch, Redirector.MODE_SERVER_OUTBOUND));

    router.attach("/histo/{numbins}", HistogramResource.class);

    attachParameterizedResources(router);

    return router;
  }

  /**
   * Gets the solrCore url for the metacatalogue
   * 
   * @return the solrCore url for the metacatalogue
   */
  public String getSolrCoreUrl() {
    ApplicationPluginParameter solrCoreUrlParameter = this.getParameter("metacatalogSolrCore");
    if (solrCoreUrlParameter == null || solrCoreUrlParameter.getValue() == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No solr core url defined, cannot perform search");
    }

    ApplicationPluginParameter solrCoreNameParameter = this.getParameter("metacatalogSolrName");
    if (solrCoreNameParameter == null || solrCoreNameParameter.getValue() == null) {
      throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "No solr core name defined, cannot perform search");
    }
    return solrCoreUrlParameter.getValue() + "/" + solrCoreNameParameter.getValue();

  }

  /** the common part of constructor */
  public void constructor() {
    this.getModel().setClassAuthor("AKKA Technologies");
    this.getModel().setClassVersion("0.5");
    this.getModel().setClassOwner("CNES");

    setCategory(Category.USER);

    // --------------------------
    // Parameters
    // --------------------------

    // Metacatalog SolR core URL
    ApplicationPluginParameter paramMetacatalogSolrCore = new ApplicationPluginParameter();
    paramMetacatalogSolrCore.setName("metacatalogSolrCore");
    paramMetacatalogSolrCore.setDescription("The url of the metacatalogue SolR core");
    this.addParameter(paramMetacatalogSolrCore);

    // Metacatalog SolR name
    ApplicationPluginParameter paramMetacatalogSolrName = new ApplicationPluginParameter();
    paramMetacatalogSolrName.setName("metacatalogSolrName");
    paramMetacatalogSolrName.setDescription("The name of the metacatalogue SolR core");
    this.addParameter(paramMetacatalogSolrName);

    ApplicationPluginParameter param = new ApplicationPluginParameter();
    param.setName("shortName");
    param.setDescription("Contains a brief human-readable title that identifies this search engine");
    param.setValue("SITools2 search");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("description");
    param.setDescription("Contains a human-readable text description of the search engine.");
    param.setValue("SITools2 connector providing an open search capability");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("contact");
    param
        .setDescription("Contains an email address at which the maintener of the description document can be reached.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("tags");
    param
        .setDescription("Contains a set of words that are used as keywords to identify and categorize this search content. Tags must be a single word and are delimited by the space character.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("longName");
    param.setDescription("Contains an extended human-readable title that identifies the search engine.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("imagePng");
    param
        .setDescription("Contains a URL that identifies the location of an image that can be used in association with this search content.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("imageIcon");
    param
        .setDescription("Contains a URL that identifies the location of an image that can be used in association with this search content.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("attribution");
    param
        .setDescription("Contains a list of all sources or entities that should be credited for the content contained in the search feed.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("language");
    param
        .setDescription("Contains a string that indicates that the search engine supports search results in the specified language.");
    param.setValue("fr");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("syndicationRight");
    param
        .setDescription("Contains a value that indicates the degree to which the search result provided by this search engine can be queried, displayed, and redistributed.");
    param.setValueType("xs:enum[open, closed]");
    param.setValue("open");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("maxTopTerms");
    param
        .setDescription("The maximum number of terms to see in the enum describe list (don't display any term if there are more terms)");
    param.setValueType("xs:integer");
    param.setValue("30");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("thesaurus");
    param.setDescription("The location of the thesaurus on the current server");
    param.setValueType("xs:string");
    param
        .setValue("D:/CNES-ULISSE-2.0-GIT/extensions/metacatalogue/workspace/fr.cnes.sitools.metacatalogue.core/thesaurus/TechniqueDev3.rdf");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("SSO_Token_validator_URL");
    param.setDescription("The url of the service to validate SSO token");
    param.setValueType("xs:string");
    param.setValue("https://ids-psc.kalimsat.eu/oauth/TokenValidation.php");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("ssoCache");
    param.setDescription("true to cache token for a period of time, false otherwise");
    param.setValueType("xs:boolean");
    param.setValue("true");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("cacheSize");
    param.setDescription("The size in number of the SSO cache");
    param.setValueType("xs:integer");
    param.setValue("100");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("expireCacheTime");
    param.setDescription("The time in minutes to keep an entry in the SSO cache");
    param.setValueType("xs:integer");
    param.setValue("120");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("thesaurusFacetFields");
    param
        .setDescription("The list of fields to get the values from the thesaurus when displaying the facets (used for traduction)");
    param.setValueType("xs:list");
    param.setValue("product,_product_category,instrument,platform,_resolution_domain,processingLevel");
    this.addParameter(param);
  }

  @Override
  public Validator<AbstractApplicationPlugin> getValidator() {
    return new Validator<AbstractApplicationPlugin>() {

      @Override
      public Set<ConstraintViolation> validate(AbstractApplicationPlugin item) {
        Set<ConstraintViolation> constraintList = new HashSet<ConstraintViolation>();
        Map<String, ApplicationPluginParameter> params = item.getModel().getParametersMap();
        ApplicationPluginParameter shortName = params.get("shortName");
        if (shortName.getValue() == null || shortName.getValue().isEmpty()) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("shortName");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("Short name cannot be empty");
          constraintList.add(constraint);
        }
        else if (!shortName.getValue().isEmpty()
            && (shortName.getValue().length() > 16 || shortName.getValue().contains("<") || shortName.getValue()
                .contains(">"))) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("shortName");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint
              .setMessage("The value must contain 16 of fewer characters of plain text. The value must not contain HTML or other markup");
          constraintList.add(constraint);
        }
        ApplicationPluginParameter description = params.get("description");
        if (description.getValue() == null || description.getValue().isEmpty()) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("description");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("description cannot be empty");
          constraintList.add(constraint);
        }
        if (!description.getValue().isEmpty()
            && (description.getValue().length() > 1024 || description.getValue().contains("<") || description
                .getValue().contains(">"))) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("description");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint
              .setMessage("The value must contain 1024 of fewer characters of plain text. The value must not contain HTML or other markup");
          constraintList.add(constraint);
        }
        ApplicationPluginParameter contact = params.get("contact");
        if (contact != null && !contact.getValue().isEmpty()
            && !(contact.getValue().contains("@") && contact.getValue().contains("."))) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("contact");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("The value must be an email address");
          constraintList.add(constraint);
        }
        ApplicationPluginParameter tags = params.get("tags");
        if (tags != null && tags.getValue().length() > 256 || tags.getValue().contains("<")
            || tags.getValue().contains(">")) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("tags");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint
              .setMessage("The value must contain 256 of fewer characters of plain text. The value must not contain HTML or other markup");
          constraintList.add(constraint);
        }
        ApplicationPluginParameter longName = params.get("longName");
        if (longName != null && longName.getValue().length() > 48 || longName.getValue().contains("<")
            || longName.getValue().contains(">")) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("longName");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint
              .setMessage("The value must contain 48 of fewer characters of plain text. The value must not contain HTML or other markup");
          constraintList.add(constraint);
        }
        ApplicationPluginParameter imagePng = params.get("imagePng");
        try {
          if (imagePng != null && !imagePng.getValue().isEmpty()) {
            URL url = new URL(imagePng.getValue());
          }
        }
        catch (MalformedURLException ex) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("imagePng");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage(ex.getMessage());
          constraintList.add(constraint);
        }
        ApplicationPluginParameter imageIcon = params.get("imageIcon");
        try {
          if (imageIcon != null && !imageIcon.getValue().isEmpty()) {
            URL url = new URL(imageIcon.getValue());
          }
        }
        catch (MalformedURLException ex) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("imageIcon");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage(ex.getMessage());
          constraintList.add(constraint);
        }
        ApplicationPluginParameter syndicationRight = params.get("syndicationRight");
        if (syndicationRight != null && !syndicationRight.getValue().equals("open")
            && !syndicationRight.getValue().equals("closed") && !syndicationRight.getValue().equals("private")
            && !syndicationRight.getValue().equals("limited")) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("syndicationRight");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint
              .setMessage("syndicationRight must take one of the following values : open, private, limited, closed");
          constraintList.add(constraint);
        }

        ApplicationPluginParameter thesaurus = params.get("thesaurus");
        if (thesaurus == null || (thesaurus != null && thesaurus.getValue().isEmpty())) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("thesaurus");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("thesaurus cannot be empty");
          constraintList.add(constraint);
        }

        ApplicationPluginParameter ssoCache = params.get("ssoCache");
        if (ssoCache == null || (ssoCache != null && ssoCache.getValue().isEmpty())) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("ssoCache");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("ssoCache cannot be empty");
          constraintList.add(constraint);
        }

        ApplicationPluginParameter ssoUrl = params.get("SSO_Token_validator_URL");
        if (ssoCache == null || (ssoCache != null && ssoCache.getValue().isEmpty())) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("SSO_Token_validator_URL");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("SSO_Token_validator_URL cannot be empty");
          constraintList.add(constraint);
        }
        return constraintList;
      }
    };
  }
  
  
  

}
