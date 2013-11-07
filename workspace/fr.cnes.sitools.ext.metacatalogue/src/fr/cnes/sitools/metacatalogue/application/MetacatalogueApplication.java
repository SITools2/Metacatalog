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
package fr.cnes.sitools.metacatalogue.application;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.ExtendedResourceInfo;
import org.restlet.ext.wadl.ResourceInfo;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Extractor;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

import fr.cnes.sitools.common.model.Category;
import fr.cnes.sitools.common.validator.ConstraintViolation;
import fr.cnes.sitools.common.validator.ConstraintViolationLevel;
import fr.cnes.sitools.common.validator.Validator;
import fr.cnes.sitools.metacatalogue.resources.administration.MetacatalogStatusResource;
import fr.cnes.sitools.metacatalogue.resources.histogram.HistogramResource;
import fr.cnes.sitools.metacatalogue.resources.opensearch.OpensearchDescribeResource;
import fr.cnes.sitools.metacatalogue.resources.opensearch.OpensearchDescriptionServiceResource;
import fr.cnes.sitools.metacatalogue.resources.opensearch.OpensearchSearchResource;
import fr.cnes.sitools.metacatalogue.resources.proxyservices.DownloadProxyServiceHandler;
import fr.cnes.sitools.metacatalogue.resources.proxyservices.WmsProxyServiceHandler;
import fr.cnes.sitools.plugins.applications.business.AbstractApplicationPlugin;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginModel;
import fr.cnes.sitools.plugins.applications.model.ApplicationPluginParameter;
import fr.cnes.sitools.proxy.RedirectorProxy;

/**
 * Metacatalog application for Opensearch exposition
 * 
 * @author m.gond
 * 
 */
public class MetacatalogueApplication extends AbstractApplicationPlugin {

  /**
   * Default constructor
   * 
   * @param context
   *          context
   */
  public MetacatalogueApplication(Context context) {
    super(context);
    constructor();
  }

  /** Default constructor */
  public MetacatalogueApplication() {
    super();
    constructor();
  }

  /**
   * Constructor with context and model of the application configuration.
   * 
   * @param arg0
   *          Restlet context
   * @param model
   *          model
   */
  public MetacatalogueApplication(Context arg0, ApplicationPluginModel model) {
    super(arg0, model);
  }

  @Override
  public void sitoolsDescribe() {
    setName("MetacatalogueApplication");
    setDescription("The metacatalogue application");
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

    router.attachDefault(MetacatalogStatusResource.class);

    String urlHarvester = this.getParameter("metacatalogServer").getValue();
    List<String> parameters = new ArrayList<String>();
    parameters.add("harvesterId");

    // merge
    attachRedirector(router, "/merge", urlHarvester, null);

    // ADMIN CRUD
    attachRedirector(router, "/admin", urlHarvester, null);
    attachRedirector(router, "/admin/{harvesterId}", urlHarvester, parameters);

    // Harvesting administration
    attachRedirector(router, "/admin/{harvesterId}/harvest/start", urlHarvester, parameters);
    attachRedirector(router, "/admin/{harvesterId}/harvest/cleanAndStart", urlHarvester, parameters);
    attachRedirector(router, "/admin/{harvesterId}/harvest/clean", urlHarvester, parameters);
    attachRedirector(router, "/admin/{harvesterId}/harvest/stop", urlHarvester, parameters);
    attachRedirector(router, "/admin/{harvesterId}/harvest/status", urlHarvester, parameters);

    // Opensearch API exposition
    router.attach("/search", OpensearchSearchResource.class);
    router.attach("/opensearch.xml", OpensearchDescriptionServiceResource.class);
    router.attach("/describe", OpensearchDescribeResource.class);

    // Services redirector exposition

    router.attach("/download/{urn}", new DownloadProxyServiceHandler(getContext()));
    router.attach("/wms/{urn}", new WmsProxyServiceHandler(getContext()));

    // ADMIN CRUD
    attachRedirector(router, "/ihm/catalogsTypes", urlHarvester, null);
    attachRedirector(router, "/ihm/havestersClasses", urlHarvester, null);

    router.attach("/histo/{numbins}", HistogramResource.class);

    attachParameterizedResources(router);

    return router;
  }

  /**
   * Attach a new redirector to the given Router. It is attached to the urlAttach, with the following redirectorUrl and
   * a List of parameters to apply to the redirectorUrl
   * 
   * @param router
   *          the Router to attach the {@link Redirector}
   * @param urlAttach
   *          the url to attach the {@link Redirector}
   * @param baseRedirectorUrl
   *          the url to redirect to
   * @param parameters
   *          the {@link List} of parameters
   */
  private void attachRedirector(Router router, String urlAttach, String baseRedirectorUrl, List<String> parameters) {
    Restlet restlet = getRedirector(baseRedirectorUrl + urlAttach, parameters, urlAttach);
    router.attach(urlAttach, restlet);
  }

  private Restlet getRedirector(String targetUri, List<String> parameters, final String urlAttach) {
    Restlet restlet;
    Restlet redirector = new RedirectorProxy(getContext(), targetUri, Redirector.MODE_SERVER_OUTBOUND) {
      @Override
      public ResourceInfo getResourceInfo(ApplicationInfo applicationInfo) {
        ResourceInfo resourceInfo = new ResourceInfo();
        ExtendedResourceInfo.describe(applicationInfo, resourceInfo, this, urlAttach);
        describe(resourceInfo);

        if (getName() != null && !"".equals(getName())) {
          DocumentationInfo doc = null;
          if (resourceInfo.getDocumentations().isEmpty()) {
            doc = new DocumentationInfo();
            resourceInfo.getDocumentations().add(doc);
          }
          else {
            doc = resourceInfo.getDocumentations().get(0);
          }

          doc.setTitle(getName());

        }
        return resourceInfo;
      }

      /**
       * WADL describe method
       * 
       * @param resource
       *          the ResourceInfo
       */
      private void describe(ResourceInfo resource) {
        setName("TODO");
        setDescription("TODO");
      }
    };
    if (parameters != null && !parameters.isEmpty()) {
      Extractor extractor = new Extractor(getContext(), redirector);
      for (String param : parameters) {
        extractor.extractFromQuery(param, param, true);
      }
      restlet = extractor;
    }
    else {
      restlet = redirector;
    }
    return restlet;
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

  /**
   * Gets the services username to use for authentication
   * 
   * @return the services username to use for authentication
   */
  public String getServicesUserName() {
    ApplicationPluginParameter servicesUserName = this.getParameter("servicesUserName");
    if (servicesUserName != null) {
      return servicesUserName.getValue();
    }
    else {
      return null;
    }

  }

  /**
   * Gets the services password to use for authentication
   * 
   * @return the services password to use for authentication
   */
  public String getServicesPassword() {
    ApplicationPluginParameter servicesPassword = this.getParameter("servicesPassword");
    if (servicesPassword != null) {
      return servicesPassword.getValue();
    }
    else {
      return null;
    }

  }

  /** the common part of constructor */
  public void constructor() {
    this.getModel().setClassAuthor("AKKA Technologies");
    this.getModel().setClassVersion("0.2");
    this.getModel().setClassOwner("CNES");

    setCategory(Category.USER);

    // Metacatalog server URL
    ApplicationPluginParameter paramMetacatalogSrv = new ApplicationPluginParameter();
    paramMetacatalogSrv.setName("metacatalogServer");
    paramMetacatalogSrv.setDescription("The url of the metacatalogue server");
    this.addParameter(paramMetacatalogSrv);

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

    // // Service user name
    ApplicationPluginParameter servicesUserName = new ApplicationPluginParameter();
    servicesUserName.setName("servicesUserName");
    servicesUserName.setDescription("The username to use for the services authentification");
    this.addParameter(servicesUserName);

    // // Service password
    ApplicationPluginParameter servicesPassword = new ApplicationPluginParameter();
    servicesPassword.setName("servicesPassword");
    servicesPassword.setDescription("The password to use for the services authentification");
    this.addParameter(servicesPassword);

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
    param.setDescription("Contains an email address at which the maintener of the description document can be reached.");
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
    param.setDescription("Contains a URL that identifies the location of an image that can be used in association with this search content.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("imageIcon");
    param.setDescription("Contains a URL that identifies the location of an image that can be used in association with this search content.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("attribution");
    param.setDescription("Contains a list of all sources or entities that should be credited for the content contained in the search feed.");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("language");
    param.setDescription("Contains a string that indicates that the search engine supports search results in the specified language.");
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
    param.setName("geometryQueryType");
    param.setDescription("The type of geometry query to use");
    param.setValueType("xs:enum[HEALPIX, POSTGIS, BOTH]");
    param.setValue("POSTGIS");
    this.addParameter(param);

    param = new ApplicationPluginParameter();
    param.setName("maxTopTerms");
    param.setDescription("The maximum number of terms to see in the enum describe list (don't display any term if there are more terms)");
    param.setValueType("xs:integer");
    param.setValue("30");
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
            && (shortName.getValue().length() > 16 || shortName.getValue().contains("<") || shortName.getValue().contains(">"))) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("shortName");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("The value must contain 16 of fewer characters of plain text. The value must not contain HTML or other markup");
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
            && (description.getValue().length() > 1024 || description.getValue().contains("<") || description.getValue().contains(">"))) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("description");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("The value must contain 1024 of fewer characters of plain text. The value must not contain HTML or other markup");
          constraintList.add(constraint);
        }
        ApplicationPluginParameter contact = params.get("contact");
        if (contact != null && !contact.getValue().isEmpty() && !(contact.getValue().contains("@") && contact.getValue().contains("."))) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("contact");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("The value must be an email address");
          constraintList.add(constraint);
        }
        ApplicationPluginParameter tags = params.get("tags");
        if (tags != null && tags.getValue().length() > 256 || tags.getValue().contains("<") || tags.getValue().contains(">")) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("tags");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("The value must contain 256 of fewer characters of plain text. The value must not contain HTML or other markup");
          constraintList.add(constraint);
        }
        ApplicationPluginParameter longName = params.get("longName");
        if (longName != null && longName.getValue().length() > 48 || longName.getValue().contains("<") || longName.getValue().contains(">")) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("longName");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("The value must contain 48 of fewer characters of plain text. The value must not contain HTML or other markup");
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
        if (syndicationRight != null && !syndicationRight.getValue().equals("open") && !syndicationRight.getValue().equals("closed")
            && !syndicationRight.getValue().equals("private") && !syndicationRight.getValue().equals("limited")) {
          ConstraintViolation constraint = new ConstraintViolation();
          constraint.setValueName("syndicationRight");
          constraint.setLevel(ConstraintViolationLevel.CRITICAL);
          constraint.setMessage("syndicationRight must take one of the following values : open, private, limited, closed");
          constraintList.add(constraint);
        }
        // ApplicationPluginParameter solrCore = params.get("solrCore");
        // if(solrCore.getValue().isEmpty()) {
        // ConstraintViolation constraint = new ConstraintViolation();
        // constraint.setValueName("solrCore");
        // constraint.setLevel(ConstraintViolationLevel.CRITICAL);
        // constraint.setMessage("A SOLR core must be set");
        // constraintList.add(constraint);
        // }
        // ApplicationPluginParameter referencesystem = params.get("referenceSystem");
        // if (!referencesystem.getValue().equals("ICRS") && !referencesystem.getValue().equals("geographic")) {
        // ConstraintViolation constraint = new ConstraintViolation();
        // constraint.setValueName("referenceSystem");
        // constraint.setLevel(ConstraintViolationLevel.CRITICAL);
        // constraint.setMessage("referenceSystem must take one of the following values : ICRS, geographic");
        // constraintList.add(constraint);
        // }
        return constraintList;
      }
    };
  }

}
