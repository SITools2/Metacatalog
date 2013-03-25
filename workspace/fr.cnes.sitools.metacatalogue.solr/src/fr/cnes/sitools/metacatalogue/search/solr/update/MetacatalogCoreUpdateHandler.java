package fr.cnes.sitools.metacatalogue.search.solr.update;

import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.SolrCore;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.CommitUpdateCommand;
import org.apache.solr.update.DirectUpdateHandler2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.sitools.metacatalogue.search.solr.spatial.OpenwisGeometryTool;

/**
 * The Class OpenwisUpdateHandler.
 * <P>
 * Explanation goes here.
 * <P>
 */
public class MetacatalogCoreUpdateHandler extends DirectUpdateHandler2 {

  /** The Constant FIELD_UUID. */
  protected static final String FIELD_UUID = "_uuid";

  /** The Constant FIELD_GEOMETRY. */
  private static final String FIELD_GEOMETRY = "_geometry";

  /** The logger. */
  protected static Logger logger = LoggerFactory.getLogger(MetacatalogCoreUpdateHandler.class);

  /** The openwis geometry tool. */
  protected OpenwisGeometryTool geometryTool;
  /** The name of the core */
  protected String solrCoreName;

  /**
   * Instantiates a new openwis update handler.
   * 
   * @param core
   *          the core
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   */
  public MetacatalogCoreUpdateHandler(SolrCore core) throws IOException {
    super(core);
    this.solrCoreName = core.getName();
    try {
      geometryTool = OpenwisGeometryTool.initialize();
    }
    catch (SecurityException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    catch (NoSuchMethodException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    finally {
      geometryTool = OpenwisGeometryTool.getInstance();
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.solr.update.DirectUpdateHandler2#addDoc(org.apache.solr.update.AddUpdateCommand)
   */
  @Override
  public int addDoc(AddUpdateCommand cmd) throws IOException {
    logger.info("AddDoc: {}", cmd);
    int result = super.addDoc(cmd);
    
    if (result == 1) {
      try {
        // update the spatial index
        SolrInputDocument solrDoc = cmd.getSolrInputDocument();
        String uuid = (String) solrDoc.getFieldValue(FIELD_UUID);
        String geometry = (String) solrDoc.getFieldValue(FIELD_GEOMETRY);

        // Update the spatial index
        if (geometry != null && geometry.length() > 0) {
          geometryTool.addMetadata(uuid, geometry, solrCoreName);
        }
        else {
          logger.warn("No Spatial data for {}", uuid);
        }
      }
      catch (Exception e) {
        reinitializeDataStore();
        throw new IOException(e);
      }
    }
    return result;
  }

  /**
   * Attempts to re-initialize the datastore in case of update failure.
   */
  protected void reinitializeDataStore() {
    try {
      geometryTool.cleanDataStore();
    }
    catch (Exception e) {
      logger.error("Unable to re-initialize datastore", e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.solr.update.DirectUpdateHandler2#commit(org.apache.solr.update.CommitUpdateCommand)
   */
  @Override
  public void commit(CommitUpdateCommand cmd) throws IOException {
    logger.info("Commit", cmd);
    super.commit(cmd);
    try {
      geometryTool.commit();
    }
    catch (Exception e) {
      reinitializeDataStore();
      throw new IOException(e);
    }
  }

  /**
   * {@inheritDoc}
   * 
   * @see org.apache.solr.update.DirectUpdateHandler2#close()
   */
  @Override
  public void close() throws IOException {
    logger.info("Close");
    super.close();
    try {
      geometryTool.close();
    }
    catch (Exception e) {
      reinitializeDataStore();
      throw new IOException(e);
    }
  }
}
