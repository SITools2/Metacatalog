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

import java.util.List;

import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.metacatalogue.utils.ResolutionConverter;
import fr.cnes.sitools.model.HarvesterModel;

public class ResolutionExtractor extends HarvesterStep {

  private Context context;

  public ResolutionExtractor(HarvesterModel conf, Context context) {
    this.context = context;
  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {
    ResolutionConverter converter = new ResolutionConverter();
    List<MetadataRecords> metadatas = data.getMetadataRecords();
    for (MetadataRecords doc : metadatas) {
      Object resolutionObj = doc.get(MetacatalogField.ACQUISITION_SETUP_RESOLUTION.getField());
      if (resolutionObj != null) {
        double resolution = Double.parseDouble(resolutionObj.toString());
        doc.add(MetacatalogField._RESOLUTION_DOMAIN.getField(), converter.getResolution(resolution));
      }
    }

    if (next != null) {
      this.next.execute(data);
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
