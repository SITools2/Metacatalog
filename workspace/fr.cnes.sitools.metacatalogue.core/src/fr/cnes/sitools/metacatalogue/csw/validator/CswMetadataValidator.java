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
package fr.cnes.sitools.metacatalogue.csw.validator;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.Metadata;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Fields;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.server.ContextAttributes;

public class CswMetadataValidator extends HarvesterStep {

  private Logger logger;

  private Context context;

  public CswMetadataValidator(HarvesterModel conf, Context context) {
    
    this.context = context;
  }

  @Override
  public void execute(Metadata data) throws ProcessException {
    logger = context.getLogger();
    List<Fields> fields = data.getFields();

    HarvestStatus status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);

    int nbDocInvalid = 0;
    for (Iterator<Fields> iterator = fields.iterator(); iterator.hasNext();) {
      Fields doc = iterator.next();
      if (doc.get(MetacatalogField._GEOMETRY.getField()) == null) {
        logger.info("No geometry defined for record : " + doc.get(MetacatalogField.ID.getField())
            + " not inserted in the metacatalog");
        nbDocInvalid++;

        iterator.remove();
      }
    }
    status.setNbDocumentsInvalid(status.getNbDocumentsInvalid() + nbDocInvalid);

    next.execute(data);
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
