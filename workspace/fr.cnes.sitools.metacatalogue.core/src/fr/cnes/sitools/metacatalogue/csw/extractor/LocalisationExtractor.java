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
import fr.cnes.sitools.metacatalogue.utils.ITagReader;
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
    String itagReaderUrl = HarvesterSettings.getInstance().getString("ITAG_URL");
    for (MetadataRecords doc : metadatas) {
      String resolution = getResolution(doc);
      String geometry = getGeometry(doc);
      if (geometry != null) {
        Localisation localisation = cache.getIfPresent(geometry);
        if (localisation == null) {
          ITagReader reader = new ITagReader(itagReaderUrl, geometry, "VHR".equals(resolution));
          try {
            reader.read();

            localisation = reader.getLocalisation();
            cache.put(geometry, localisation);
          }
          catch (IOException e) {
            getLogger(context).log(Level.WARNING, "CANNOT READ URL FROM ETAG", e);
          }
        }

        // countries
        addValuesToMetadata(doc, localisation.getCountries(), MetacatalogField.COUNTRY.getField());

        // regions
        addValuesToMetadata(doc, localisation.getRegions(), MetacatalogField.REGION.getField());
        
        
        // departments
        addValuesToMetadata(doc, localisation.getDepartments(), MetacatalogField.DEPARTMENT.getField());

        // city
        addValuesToMetadata(doc, localisation.getCities(), MetacatalogField.CITY.getField());

      }
    }

    if (next != null) {
      this.next.execute(data);
    }
  }

  /**
   * Get the resolution string for the MetadataRecords given
   * 
   * @param doc
   *          the {@link MetadataRecords}
   * @return the resolution string or null if not found
   */
  private String getResolution(MetadataRecords doc) {
    Object resolutionObj = doc.get(MetacatalogField._RESOLUTION_DOMAIN.getField());
    if (resolutionObj != null) {
      return resolutionObj.toString();

    }
    return null;
  }

  /**
   * Get the geometry string for the MetadataRecords given
   * 
   * @param doc
   *          the {@link MetadataRecords}
   * @return the geometry string or null if not found
   */
  private String getGeometry(MetadataRecords doc) {
    Object geometryObj = doc.get(MetacatalogField.FOOTPRINT.getField());
    if (geometryObj != null) {
      return geometryObj.toString();

    }
    return null;
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
