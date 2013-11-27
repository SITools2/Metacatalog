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
package fr.cnes.sitools.metacatalogue.csw.extractor;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Context;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Localisation;
import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.ETagReader;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.model.HarvesterModel;

public class LocalisationExtractor extends HarvesterStep {

  private Context context;

  private Cache<String, Localisation> cache;

  public LocalisationExtractor(HarvesterModel conf, Context context) {
    this.context = context;
    cache = CacheBuilder.newBuilder().maximumSize(1000) // Taille Max
        .expireAfterWrite(20, TimeUnit.MINUTES) // TTL
        .build();
  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {
    List<MetadataRecords> metadatas = data.getMetadataRecords();
    for (MetadataRecords doc : metadatas) {
      String geometry = doc.get(MetacatalogField._GEOMETRY.getField()).toString();
      if (geometry != null) {
        Localisation localisation = cache.getIfPresent(geometry);
        if (localisation == null) {
          String etagReaderUrl = HarvesterSettings.getInstance().getString("ETAG_URL");
          ETagReader reader = new ETagReader(etagReaderUrl, geometry);
          try {
            reader.read();

            localisation = reader.getLocalisation();
            cache.put(geometry, localisation);
          }
          catch (IOException e) {
            context.getLogger().log(Level.WARNING, "CANNOT READ URL FROM ETAG", e);
          }
        }
        else {
        }

        // countries
        addValuesToMetadata(doc, localisation.getCountries(), "country");

        // regions
        addValuesToMetadata(doc, localisation.getRegions(), "region");

        // departments
        addValuesToMetadata(doc, localisation.getDepartments(), "department");

        // city
        addValuesToMetadata(doc, localisation.getCities(), "city");

      }
    }

    if (next != null) {
      this.next.execute(data);
    }
  }

  private void addValuesToMetadata(MetadataRecords doc, List<String> values, String key) {
    if (values != null) {
      for (String value : values) {
        doc.add(key, value);
      }
    }
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
