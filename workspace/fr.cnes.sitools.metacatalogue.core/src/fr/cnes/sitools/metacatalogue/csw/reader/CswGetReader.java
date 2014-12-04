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
package fr.cnes.sitools.metacatalogue.csw.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.HarvesterSource;
import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.util.ClientResourceProxy;

/**
 * {@link HarvesterStep} that is in charge of reading some CSW metadata. It will query the getRecords operation with a
 * GET method
 * 
 * @author m.gond
 * 
 */
public class CswGetReader extends HarvesterStep {
  /** The reader page size */
  private int pageSize = 20;
  /** The source to harvest */
  private HarvesterSource source;
  /** The name of the schema */
  private String schemaName;
  /** Current harvester configuration */
  private HarvesterModel conf;

  /** The logger */
  private Logger logger;
  /** The context */
  private Context context;

  /**
   * Create a new {@link CswGetReader} with a given {@link HarvesterSource}
   * 
   * @param conf
   *          the {@link HarvesterModel} to harvest
   */
  public CswGetReader(HarvesterModel conf, Context context) {
    this.schemaName = conf.getCatalogType();
    this.conf = conf;
    this.context = context;

  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {

    // get url to harvest from getcapabilities
    this.source = getSource(conf);

    logger = getLogger(context);

    Integer nbRecords = null;
    Integer nextRecord = 1;

    boolean keepLooping = true;
    do {
      data = new MetadataContainer();

      ClientResourceProxy clientResourceProxy = new ClientResourceProxy(source.getUrl(), Method.GET);
      ClientResource clientResource = clientResourceProxy.getClientResource();
      clientResource.setRetryOnError(false);
      clientResource.setRetryAttempts(0);
      clientResource.setRetryDelay(50);

      addCswQueryParams(clientResource.getRequest(), nextRecord, pageSize);
      if (this.conf.getLastHarvest() != null) {
        try {
          addCswQueryConstraintParams(clientResource.getRequest(), this.conf.getLastHarvest());
        }
        catch (IOException e) {
          throw new ProcessException("Error while creating the filter XML", e);
        }
      }
      logger.log(Level.INFO, "Query CSW service : " + clientResource.getRequest().getResourceRef().toString());

      Representation repr = clientResource.get(MediaType.APPLICATION_XML);

      try {
        InputStream in = repr.getStream();
        Element root = Xml.loadStream(in);

        // get the search results and the number of records
        Element searchResults = root.getChild("SearchResults",
            Namespace.getNamespace("http://www.opengis.net/cat/csw/2.0.2"));

        if (searchResults == null) {
          // if there is an error we log it and exit the loop otherwise it will loop infinitely
          logger.warning("Cannot find <csw:searchResult tag, there has been an error :\n" + Xml.getString(root));
          break;
        }

        if (nbRecords == null) {
          nbRecords = Integer.parseInt(searchResults.getAttributeValue("numberOfRecordsMatched"));
          logger.info("Number of records found = " + nbRecords);

        }

        int numberOfRecordsReturned = Integer.parseInt(searchResults.getAttributeValue("numberOfRecordsReturned"));
        HarvestStatus status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);
        status.setNbDocumentsRetrieved(status.getNbDocumentsRetrieved() + numberOfRecordsReturned);

        nextRecord = Integer.parseInt(searchResults.getAttributeValue("nextRecord"));

        // if nextRecord is 0, we asked for the last page so we stop looping
        keepLooping = (nextRecord != 0);

        data.setXmlData(searchResults);
        next.execute(data);
      }
      catch (IOException e) {
        logger.log(Level.WARNING, e.getLocalizedMessage(), e);
      }
      catch (JDOMException e) {
        logger.log(Level.WARNING, e.getLocalizedMessage(), e);
      }

    } while (keepLooping);

    this.end();
  }

  /**
   * getSource
   * 
   * @param conf
   *          the harvester model
   * @return HarvesterSource
   * @throws ProcessException
   */
  private HarvesterSource getSource(HarvesterModel conf) throws ProcessException {

    HarvesterSource returnedSource = new HarvesterSource();

    String capabilitiesUrl = conf.getSource().getUrl();

    Reference ref = new Reference(capabilitiesUrl);
    ClientResourceProxy client = new ClientResourceProxy(ref, Method.GET);
    ClientResource clientResource = client.getClientResource();

    Representation repr = clientResource.get(MediaType.APPLICATION_XML);

    try {

      InputStream in = repr.getStream();
      Element root = Xml.loadStream(in);

      Element operationsMetadata = root.getChild("OperationsMetadata",
          Namespace.getNamespace("http://www.opengis.net/ows"));
      List operationElementsList = operationsMetadata.getChildren("Operation",
          Namespace.getNamespace("http://www.opengis.net/ows"));

      Element recordOperationElement = getElementFromValueInList(operationElementsList, "name", "GetRecords");
      Element dcpElement = recordOperationElement.getChild("DCP", Namespace.getNamespace("http://www.opengis.net/ows"));

      List httpElementsList = dcpElement.getChildren("HTTP", Namespace.getNamespace("http://www.opengis.net/ows"));
      Element httpGetElement = getChildElementFromList(httpElementsList, "Get",
          Namespace.getNamespace("http://www.opengis.net/ows"));

      String url = httpGetElement.getAttributeValue("href", Namespace.getNamespace("http://www.w3.org/1999/xlink"));

      // set source parameters
      returnedSource.setUrl(url);
      returnedSource.setName(conf.getSource().getName());
      returnedSource.setType(conf.getSource().getType());

    }
    catch (Exception e) {
      e.printStackTrace();
      throw new ProcessException("Unable to retrieve url to harvest from GetCapabilities");
    }

    return returnedSource;

  }

  /**
   * getChildElementFromList
   * 
   * @param inputList
   *          the list to parse
   * @param childName
   * @param namespace
   * @return
   */
  private Element getChildElementFromList(List inputList, String childName, Namespace namespace) {

    Element returnedElement = null;

    for (int i = 0; i < inputList.size(); i++) {
      Object obj = (Object) inputList.get(i);
      if (obj instanceof Element) {
        Element element = (Element) obj;
        Element childElement = element.getChild(childName, namespace);
        returnedElement = childElement;
      }
    }

    return returnedElement;
  }

  /**
   * getElementFromValueInList
   * 
   * @param inputList
   * @param attributeName
   * @param attributeValue
   * @return
   */
  private Element getElementFromValueInList(List inputList, String attributeName, String attributeValue) {

    Element returnedElement = null;

    for (int i = 0; i < inputList.size(); i++) {
      Object object = (Object) inputList.get(i);
      if (object instanceof Element) {
        Element element = (Element) object;
        if (element.getAttributeValue(attributeName).equals(attributeValue)) {
          returnedElement = element;
        }
      }
    }

    return returnedElement;

  }

  @Override
  public void end() throws ProcessException {
    if (next != null) {
      this.next.end();
    }
  }

  @Override
  public CheckStepsInformation check() {
    if (next != null) {
      CheckStepsInformation ok = this.next.check();
      if (!ok.isOk()) {
        return ok;
      }
    }
    return new CheckStepsInformation(true);
  }

  private void addCswQueryParams(Request request, int start, int maxRows) {
    Reference reference = request.getResourceRef();
    reference.addQueryParameter("version", "2.0.2");
    reference.addQueryParameter("SERVICE", "CSW");
    reference.addQueryParameter("REQUEST", "getRecords");
    reference.addQueryParameter("resultType", "results");
    reference.addQueryParameter("outputSchema", "http://www.isotc211.org/2005/gmd");
    reference.addQueryParameter("typeNames", "csw:Record");

    reference.addQueryParameter("startPosition", new Integer(start).toString());
    reference.addQueryParameter("maxRecords", new Integer(maxRows).toString());

  }

  private void addCswQueryConstraintParams(Request request, Date lastHarvest) throws IOException {
    Reference reference = request.getResourceRef();
    reference.addQueryParameter("CONSTRAINTLANGUAGE", "Filter");
    reference.addQueryParameter("CONSTRAINT_LANGUAGE_VERSION", "1.1.0");

    Reference ref = LocalReference.createFileReference(HarvesterSettings.getInstance().getResourcePath(schemaName,
        "xml_get_filter.ftl"));

    Map<String, Object> filterObject = new HashMap<String, Object>();
    filterObject.put("date", lastHarvest);

    Representation ftlFilter = new ClientResource(ref).get();

    // Wraps the bean with a FreeMarker representation
    Representation xmlCsw = new TemplateRepresentation(ftlFilter, filterObject, MediaType.APPLICATION_ALL_XML);

    reference.addQueryParameter("CONSTRAINT", xmlCsw.getText());
  }

}
