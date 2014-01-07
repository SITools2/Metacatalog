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
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.restlet.Context;
import org.restlet.engine.util.DateUtils;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.metacatalogue.utils.ProductCategoryExtractorUtil;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.server.Consts;

public class FacetsInformationExtractor extends HarvesterStep {

  public FacetsInformationExtractor(HarvesterModel conf, Context context) {
  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {

    HarvesterSettings settings = HarvesterSettings.getInstance();

    ProductCategoryExtractorUtil extractor = new ProductCategoryExtractorUtil(settings.getString(Consts.THESAURUS_PATH));

    // modified
    List<String> frmt = HarvesterSettings.getInstance().getDateFormats();

    List<MetadataRecords> metadatas = data.getMetadataRecords();
    for (MetadataRecords doc : metadatas) {
      // PRODUCT CATEGORIES
      Object productObj = doc.get(MetacatalogField.PRODUCT.getField());
      if (productObj != null) {
        String product = productObj.toString();
        try {
          doc.add(MetacatalogField._PRODUCT_CATEGORY.getField(), extractor.extractCategory(product));
        }
        catch (IOException e) {
          throw new ProcessException("Cannot extract category for product : " + product, e);
        }
      }

      // YEAR AND MONTH
      Object dateObj = doc.get(MetacatalogField.START_DATE.getField());
      if (dateObj != null) {
        Date date = DateUtils.parse(dateObj.toString(), frmt);
        doc.add(MetacatalogField._YEAR.getField(), DateUtils.format(date, "yyyy"));
        doc.add(MetacatalogField._MONTH.getField(), DateUtils.format(date, "MM"));
        doc.add(MetacatalogField._DAY.getField(), DateUtils.format(date, "dd"));
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
