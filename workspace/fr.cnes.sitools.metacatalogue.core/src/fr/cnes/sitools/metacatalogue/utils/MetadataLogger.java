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
package fr.cnes.sitools.metacatalogue.utils;

import java.util.logging.Logger;

import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.Metadata;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Field;
import fr.cnes.sitools.metacatalogue.model.Fields;
import fr.cnes.sitools.model.HarvesterModel;

public class MetadataLogger extends HarvesterStep {

  /** The logger */
  private Logger logger;
  /** The Context */
  private Context context;

  public MetadataLogger(HarvesterModel conf, Context context) {
    this.context = context;

  }

  @Override
  public void execute(Metadata data) throws ProcessException {
    logger = context.getLogger();

    for (Fields fields : data.getFields()) {
      for (Field field : fields.getList()) {
        String name = field.getName();
        String value = "";
        if (field.getValue() != null) {
          value = field.getValue().toString();
        }
        logger.info(name + " : " + value);

      }
    }
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
