/*******************************************************************************
 * Copyright 2011 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.sitools.metacatalogue.index.solr;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.DateUtil;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.index.MetadataIndexer;
import fr.cnes.sitools.metacatalogue.model.Field;
import fr.cnes.sitools.metacatalogue.model.Fields;
import fr.cnes.sitools.metacatalogue.utils.old.InspireIndexField;

/**
 * Metadata index on Solr search engine
 * 
 * @author m.gond
 * 
 */
public class SolrInspireMetadataIndexer implements MetadataIndexer {
  /** The SolrServer */
  private SolrServer server;
  /** The list of Document to index */
  private Collection<SolrInputDocument> documentsToIndex;
  /** The logger */
  private Logger logger;

  /**
   * Constructor with a server url
   * 
   * @param urlServer
   *          the url of the server
   * 
   */
  public SolrInspireMetadataIndexer(String urlServer, Context context) {
    documentsToIndex = new ArrayList<SolrInputDocument>();
    server = SolRUtils.getSolRServer(urlServer);
    this.logger = context.getLogger();
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.metacatalogue.index.MetadataIndexer#indexMetadata()
   */
  @Override
  public void indexMetadata() throws Exception {
    if (documentsToIndex.isEmpty()) {
      throw new Exception("No data to index");
    }
    server.add(documentsToIndex);
    server.commit();
    documentsToIndex.clear();
  }

  @Override
  public void commit() throws Exception {
    server.commit();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * fr.cnes.sitools.metacatalogue.index.MetadataIndexer#addDocumentToIndex(fr.cnes.sitools.metacatalogue.model.Fields)
   */
  @Override
  public void addFieldsToIndex(Fields fields) throws Exception {
    SolrInputDocument inputDocument = buildSolrInput(fields);
    documentsToIndex.add(inputDocument);
  }

  /*
   * (non-Javadoc)
   * 
   * @see fr.cnes.sitools.metacatalogue.index.MetadataIndexer#addDocumentToIndex(java.util.List)
   */
  @Override
  public void addListFieldsToIndex(List<Fields> fieldList) throws Exception {
    for (Fields fields : fieldList) {
      this.addFieldsToIndex(fields);
    }
  }

  @Override
  public boolean checkIndexerAvailable() {
    return server != null;
  }

  /**
   * Builds the solr input.
   * 
   * @param fields
   *          the {@link Fields} object containing the document field
   * @return the solr input document
   * @throws Exception
   *           if there is an error
   */
  private SolrInputDocument buildSolrInput(Fields fields) throws Exception {
    SolrInputDocument document = new SolrInputDocument();
    String text;
    SolrInputField solrField;
    InspireIndexField indexField;
    for (Field field : fields.getList()) {
      String name = field.getName();
      indexField = InspireIndexField.getField(name);
      if (indexField == null) {
        logger.info("Unknown field " + name + " add it to the index as a String object");
        indexField = InspireIndexField.ANY;
      }
      text = field.getValue().toString();
      solrField = document.getField(indexField.getField());
      if (StringUtils.isNotBlank(text) && (solrField == null || !text.equals(solrField.getFirstValue()))) {
        if (indexField.isDate()) {
          Date date = parseDate(text.toUpperCase());
          if (date != null) {
            document.addField(name, date);
          }
        }
        else {
          document.addField(name, indexField.valueToString(text));
        }
      }
    }
    return document;
  }

  /**
   * Parse a date from its string
   * 
   * @param sDate
   *          the date string
   * @return the Date corresponding to the sDate
   * @throws ParseException
   *           if there is an error while parsing the date
   */
  private Date parseDate(String sDate) throws ParseException {
    Date result = null;
    result = DateUtil.parseDate(sDate);
    return result;
  }

  @Override
  public int getCurrentNumberOfFieldsToIndex() {
    return documentsToIndex.size();
  }

}
