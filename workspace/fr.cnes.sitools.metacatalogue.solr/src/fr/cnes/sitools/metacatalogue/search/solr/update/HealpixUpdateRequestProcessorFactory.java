package fr.cnes.sitools.metacatalogue.search.solr.update;

import java.io.IOException;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.sitools.metacatalogue.search.solr.spatial.HealpixGeometryTool;

/**
 * Update request processor factory to add healpix indexes for each entry
 * <P>
 * For each entry it calculates the healpix numbers and add it to the entry
 * <P>
 */
public class HealpixUpdateRequestProcessorFactory extends UpdateRequestProcessorFactory {
  @Override
  public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
    return new HeadpixUpdateRequestProcessor(next);
  }
}

class HeadpixUpdateRequestProcessor extends UpdateRequestProcessor {

  /** The Constant FIELD_UUID. */
  protected static final String FIELD_UUID = "_uuid";

  /** The Constant FIELD_GEOMETRY. */
  private static final String FIELD_GEOMETRY = "_geometry";

  /** The logger. */
  protected static Logger logger = LoggerFactory.getLogger(HeadpixUpdateRequestProcessor.class);

  /** The openwis geometry tool. */
  protected HealpixGeometryTool geometryTool;
  /** The name of the core */
  protected String solrCoreName;

  public HeadpixUpdateRequestProcessor(UpdateRequestProcessor next) {
    super(next);
    geometryTool = HealpixGeometryTool.initialize();
  }

  @Override
  public void processAdd(AddUpdateCommand cmd) throws IOException {
    logger.info("processAdd: {}", cmd);
    SolrInputDocument solrDoc = cmd.getSolrInputDocument();

    try {
      // update the spatial index
      String uuid = (String) solrDoc.getFieldValue(FIELD_UUID);
      String geometry = (String) solrDoc.getFieldValue(FIELD_GEOMETRY);

      // Update the spatial index
      if (geometry != null && geometry.length() > 0) {
        geometryTool.addMetadata(uuid, geometry, solrDoc);
      }
      else {
        logger.warn("No Spatial data for {}", uuid);
      }
    }
    catch (Exception e) {
      throw new IOException(e);
    }
    // pass it up the chain
    super.processAdd(cmd);
  }

}
