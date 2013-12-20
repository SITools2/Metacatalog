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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.wadl.ApplicationInfo;
import org.restlet.ext.wadl.DocumentationInfo;
import org.restlet.ext.wadl.ExtendedResourceInfo;
import org.restlet.ext.wadl.ResourceInfo;
import org.restlet.routing.Extractor;
import org.restlet.routing.Redirector;
import org.restlet.routing.Router;

import fr.cnes.sitools.common.model.Category;
import fr.cnes.sitools.common.validator.ConstraintViolation;
import fr.cnes.sitools.common.validator.Validator;
import fr.cnes.sitools.metacatalogue.resources.administration.MetacatalogStatusResource;
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
public class MetacatalogueAdminApplication extends AbstractApplicationPlugin {

  /**
   * Default constructor
   * 
   * @param context
   *          context
   */
  public MetacatalogueAdminApplication(Context context) {
    super(context);
    constructor();
  }

  /** Default constructor */
  public MetacatalogueAdminApplication() {
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
  public MetacatalogueAdminApplication(Context arg0, ApplicationPluginModel model) {
    super(arg0, model);
  }

  @Override
  public void sitoolsDescribe() {
    setName("MetacatalogueAdminApplication");
    setDescription("The metacatalogue application to administrate harversters");
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

    attachRedirector(router, "/logs/{harvesterId}", urlHarvester, parameters);

    // ADMIN CRUD
    attachRedirector(router, "/ihm/catalogsTypes", urlHarvester, null);
    attachRedirector(router, "/ihm/havestersClasses", urlHarvester, null);

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

  /** the common part of constructor */
  public void constructor() {
    this.getModel().setClassAuthor("AKKA Technologies");
    this.getModel().setClassVersion("0.3");
    this.getModel().setClassOwner("CNES");

    setCategory(Category.ADMIN);

    // Metacatalog server URL
    ApplicationPluginParameter paramMetacatalogSrv = new ApplicationPluginParameter();
    paramMetacatalogSrv.setName("metacatalogServer");
    paramMetacatalogSrv.setDescription("The url of the metacatalogue server");
    this.addParameter(paramMetacatalogSrv);

  }

  @Override
  public Validator<AbstractApplicationPlugin> getValidator() {
    return new Validator<AbstractApplicationPlugin>() {

      @Override
      public Set<ConstraintViolation> validate(AbstractApplicationPlugin item) {
        Set<ConstraintViolation> constraintList = new HashSet<ConstraintViolation>();
        return constraintList;
      }
    };
  }

}
