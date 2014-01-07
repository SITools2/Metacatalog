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
package fr.cnes.sitools.metacatalogue.csw;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.solr.client.solrj.SolrServer;
import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.Harvester;
import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.csw.extractor.CswMetadataExtractor;
import fr.cnes.sitools.metacatalogue.csw.extractor.LocalisationExtractor;
import fr.cnes.sitools.metacatalogue.csw.extractor.FacetsInformationExtractor;
import fr.cnes.sitools.metacatalogue.csw.extractor.ResolutionExtractor;
import fr.cnes.sitools.metacatalogue.csw.indexer.CswMetadataIndexer;
import fr.cnes.sitools.metacatalogue.csw.reader.CswPostReader;
import fr.cnes.sitools.metacatalogue.csw.validator.CswMetadataValidator;
import fr.cnes.sitools.metacatalogue.exceptions.CheckProcessException;
import fr.cnes.sitools.metacatalogue.index.MetadataIndexer;
import fr.cnes.sitools.metacatalogue.index.solr.SolRUtils;
import fr.cnes.sitools.metacatalogue.index.solr.SolrMetadataIndexer;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.server.ContextAttributes;

public class CswPostHarvester extends Harvester {

  private HarvesterStep step1;

  private Context context;

  @Override
  public void initHarvester(HarvesterModel harvestConf, Context context) throws CheckProcessException {
    super.initHarvester(harvestConf, context);
    
    HarvesterStep step2, step3, step4, step5, step6, step7;
    
    SolrServer solrServer = SolRUtils.getSolRServer(harvestConf.getIndexerConf().getUrl());
    context.getAttributes().put(ContextAttributes.INDEXER_SERVER, solrServer);
    MetadataIndexer indexer = new SolrMetadataIndexer(context);
    

    step1 = new CswPostReader(harvestConf, context);
    step2 = new CswMetadataExtractor(harvestConf, context);
    step3 = new ResolutionExtractor(harvestConf, context);
    step4 = new LocalisationExtractor(harvestConf, context);
    step5 = new FacetsInformationExtractor(harvestConf, context);
    step6 = new CswMetadataValidator(harvestConf, context);
    step7 = new CswMetadataIndexer(harvestConf, context, indexer);

    step1.setNext(step2);
    step2.setNext(step3);
    step3.setNext(step4);
    step4.setNext(step5);
    step5.setNext(step6);
    step6.setNext(step7);
    

    CheckStepsInformation ok = step1.check();
    if (!ok.isOk()) {
      throw new CheckProcessException(ok.getMessage());
    }
    this.context = context;
  }

  @Override
  public void harvest() throws Exception {
    HarvestStatus status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);
    try {
      step1.execute(null);
      status.setResult(HarvestStatus.RESULT_SUCCESS);
    }
    catch (Exception e) {
      status.setResult(HarvestStatus.RESULT_ERROR);
      Logger logger = (Logger) context.getAttributes().get(ContextAttributes.LOGGER);
      logger.log(Level.WARNING, e.getMessage(), e);
      throw e;
    }
  }

}
