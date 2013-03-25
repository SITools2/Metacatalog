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
package fr.cnes.sitools.metacatalogue.persistance;

import static org.junit.Assert.*;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.HarvesterSource;
import fr.cnes.sitools.model.IndexerModel;
import fr.cnes.sitools.persistence.HarvesterModelStore;
import fr.cnes.sitools.persistence.HarvesterModelStoreXmlImpl;
import fr.cnes.sitools.proxy.ProxySettings;

public class HarvesterStoreTest {

  /**
   * @param args
   * @throws ParseException
   */
  @Test
  public void test() {
    HarvesterSettings settings = HarvesterSettings.getInstance();

    // STEP 0 Discover the catalog configurations
    ProxySettings.init();

    HarvesterModelStore storeDataStorage = new HarvesterModelStoreXmlImpl(new File(
        settings.getStoreDIR("APP_HARVESTER_MODEL_STORE_DIR")));

    HarvesterModel conf = new HarvesterModel();
    conf.setId("10000000");
    conf.setCatalogType("csw-iso19139");

    HarvesterSource source = new HarvesterSource();
    // source.setUrl("http://gpp3-wxs.ign.fr/inspire/csw");
    // source.setUrl("http://geonetcab.mdweb-project.org/WS/cswiso/default");
    source.setUrl("http://demo.mdweb-project.org/WS/csw/default");

    conf.setSource(source);

    IndexerModel indexerConf = new IndexerModel();
    indexerConf.setUrl("http://localhost:8983/solr/core_test_metadata");
    conf.setIndexerConf(indexerConf);

    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    Date date;
    try {
      date = (Date) formatter.parse("01/01/2012");
      conf.setLastHarvest(date);
    }
    catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    storeDataStorage.save(conf);

    HarvesterModel confBack = storeDataStorage.get(conf.getId());
    assertNotNull(confBack);
    assertEquals(1, storeDataStorage.getList().size());

    assertEquals(conf.getId(), confBack.getId());

//    storeDataStorage.delete(conf);
//
//    assertEquals(0, storeDataStorage.getList().size());
//    assertNull(storeDataStorage.get(conf.getId()));

  }
}
