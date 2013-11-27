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
package fr.cnes.sitools.metacatalogue.csw.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.restlet.Context;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
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
 * POST method containing a XML body
 * 
 * @author m.gond
 * 
 */
public class CswPostReader extends HarvesterStep {
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

  private Context context;

  /**
   * Create a new {@link CswPostReader} with a given {@link HarvesterSource}
   * 
   * @param conf
   *          the {@link HarvesterModel} to harvest
   */
  public CswPostReader(HarvesterModel conf, Context context) {
    this.source = conf.getSource();
    this.schemaName = conf.getCatalogType();
    this.conf = conf;
   
    this.context = context;
  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {
    logger = context.getLogger();
    Integer nbRecords = null;
    Integer nextRecord = 1;

    String xmlFilePath = HarvesterSettings.getInstance().getResourcePath(this.schemaName, "xml_csw.xml");
    Reference ref = LocalReference.createFileReference(xmlFilePath);

    boolean keepLooping = true;
    do {
      data = new MetadataContainer();

      CswQueryModel queryModel = new CswQueryModel();
      queryModel.setStartPosition(nextRecord);
      queryModel.setMaxRecords(pageSize);

      Representation ftlCsw = new ClientResource(ref).get();
      // Wraps the bean with a FreeMarker representation
      Representation xmlCsw = new TemplateRepresentation(ftlCsw, queryModel, MediaType.APPLICATION_ALL_XML);

      ClientResourceProxy clientResourceProxy = new ClientResourceProxy(source.getUrl(), Method.POST);
      ClientResource clientResource = clientResourceProxy.getClientResource();
      clientResource.setRetryOnError(false);
      clientResource.setRetryAttempts(0);
      clientResource.setRetryDelay(50);

      if (this.conf.getLastHarvest() != null) {
        queryModel.setLastIndexTime(this.conf.getLastHarvest());

      }

      StringRepresentation str;
      try {
        str = new StringRepresentation(xmlCsw.getText());

        str.setMediaType(MediaType.APPLICATION_ALL_XML);

        // logger.log(Level.INFO, "Query CSW service : " + clientResource.getRequest().getResourceRef().toString());
        // logger.log(Level.INFO, "XML request : " + str.getText());

        Representation repr = clientResource.post(str, MediaType.APPLICATION_XML);

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
          HarvestStatus status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);
          status.setNbDocumentsRetrieved(status.getNbDocumentsIndexed() + nbRecords);
        }

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

    next.end();
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

}
