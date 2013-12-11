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
package fr.cnes.sitools.metacatalogue.index.solr;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.common.util.DateUtil;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.index.MetadataIndexer;
import fr.cnes.sitools.metacatalogue.model.Field;
import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.server.ContextAttributes;

/**
 * Metadata index on Solr search engine
 * 
 * @author m.gond
 * 
 */
public class SolrMetadataIndexer implements MetadataIndexer {

  /** The SolrServer */
  protected SolrServer server;
  /** The list of Document to index */
  protected Collection<SolrInputDocument> documentsToIndex;
  /** The context */
  protected Context context;

  /**
   * Constructor with a server url
   * 
   * 
   * @param context
   * 
   */
  public SolrMetadataIndexer(Context context) {
    documentsToIndex = new ArrayList<SolrInputDocument>();
    server = (SolrServer) context.getAttributes().get(ContextAttributes.INDEXER_SERVER);
    this.context = context;
  }

  @Override
  public void indexMetadata() throws Exception {
    if (!documentsToIndex.isEmpty()) {
      // throw new Exception("No data to index");

      server.add(documentsToIndex);
      documentsToIndex.clear();
    }
  }

  @Override
  public void commit() throws Exception {
    server.commit();
  }

  @Override
  public void addFieldsToIndex(MetadataRecords fields) throws Exception {
    SolrInputDocument inputDocument = buildSolrInput(fields);
    documentsToIndex.add(inputDocument);
  }

  @Override
  public void addListFieldsToIndex(List<MetadataRecords> fieldList) throws Exception {
    for (MetadataRecords fields : fieldList) {
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
   *          the {@link MetadataRecords} object containing the document field
   * @return the solr input document
   * @throws Exception
   *           if there is an error
   */
  protected SolrInputDocument buildSolrInput(MetadataRecords fields) throws Exception {
    SolrInputDocument document = new SolrInputDocument();
    String text;
    SolrInputField solrField;
    MetacatalogField indexField;
    for (Field field : fields.getList()) {
      String name = field.getName();
      indexField = MetacatalogField.getField(name);
      if (indexField == null) {
        context.getLogger().info("Unknown field " + name + " add it to the index as a String object");
        indexField = MetacatalogField._ANY;
      }
      if (field.getValue() != null) {
        text = field.getValue().toString();
        solrField = document.getField(indexField.getField());
        if (StringUtils.isNotBlank(text) && (solrField == null || !text.equals(solrField.getFirstValue()))) {
          if (indexField.isDate()) {
            Date date = parseDate(text.toUpperCase());
            if (date != null) {
              document.addField(name, date);
            }
          }
          else if (indexField.isBoolean()) {
            Boolean bool = Boolean.parseBoolean(text);
            if (bool != null) {
              document.addField(name, bool);
            }
          }
          else {
            document.addField(name, indexField.valueToString(text));
          }
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
    
    if (HarvesterSettings.getInstance().get("DATE_FORMATS")!=null){
      List<String> fmts = new ArrayList<String>();
      String[] formats = HarvesterSettings.getInstance().get("DATE_FORMATS").toString().split(",");
      for ( int i = 0 ; i < formats.length ; i++){
        fmts.add(formats[i]);
      }
      result = DateUtil.parseDate(sDate, fmts);
      
    } else {
      result = DateUtil.parseDate(sDate);  
    }
    return result;
  }

  @Override
  public int getCurrentNumberOfFieldsToIndex() {
    return documentsToIndex.size();
  }

  /**
   * For tests
   * 
   * @param server
   *          the SolrServer
   */
  public void setSolrServer(SolrServer server) {
    this.server = server;
  }

}
