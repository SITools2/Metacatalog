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

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import net.sf.saxon.functions.Collection;

import org.restlet.Context;

import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Field;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.server.ContextAttributes;
import fr.cnes.sitools.thesaurus.ThesaurusSearcher;
import fr.cnes.sitools.metacatalogue.model.Error;

public class CswMetadataValidator extends HarvesterStep {

  private Logger logger;

  private Context context;

  public CswMetadataValidator(HarvesterModel conf, Context context) {

    this.context = context;
  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {
    logger = context.getLogger();
    List<MetadataRecords> metadataRecords = data.getMetadataRecords();

    HarvestStatus status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);

    int nbDocInvalid = 0;

    List<MetacatalogField> mandatoryFields = MetacatalogField.getMandatoryFields();
    List<MetacatalogField> thesaurusFields = MetacatalogField.getThesaurusFields();

    HarvesterSettings settings = HarvesterSettings.getInstance();
    ThesaurusSearcher searcher = getThesaurusSearcher(settings.getString(Consts.THESAURUS_PATH));

    /* iterate on errors */
    for (Iterator<MetadataRecords> iterator = metadataRecords.iterator(); iterator.hasNext();) {

      MetadataRecords doc = iterator.next();

      List<Error> errors = doc.getErrors();

      /* warning */
      for (Error error : errors) {
        if (error != null && error.getLevel() != null) {
          if (error.getLevel().equals("warning")) {
            logger.info("WARNING : " + error.getValue());
          }
        }
      }

      /* critical errors (mandatory fields) : record is not harvested */
      boolean fail = false;
      for (MetacatalogField field : mandatoryFields) {
        Error error = doc.findFirstError(field.getField());
        /* check xpath */
        if (error != null) {
          logger.info("" + error.getValue());
          fail = true;
        }
        /* check content */
        if (doc.get(field.getField()) == null) {
          logger.info(field.getField() + " not defined for record : " + doc.get(MetacatalogField.IDENTIFIER.getField())
              + " not inserted in the metacatalog");
          fail = true;
        }

        if (field.isDate()) {

          List<String> fmts = new ArrayList<String>();
          fmts.add("yyyy-MM-DD'T'HH:mm:ss");
          fmts.add("yyyy-MM-DD'T'HH:mm:ss(Z)");
          fmts.add("yyyy-MM-DD'T'HH:mm:ss+HH:mm");
          fmts.add("yyyy-MM-DD'T'HH:mm:ss-HH:mm");
          try {
            org.apache.solr.common.util.DateUtil.parseDate((String) doc.get(field.getField()), fmts);
          }
          catch (ParseException e) {
            logger.info(field.getField() + " - incorrect date format : "
                + doc.get(MetacatalogField.IDENTIFIER.getField()) + " not inserted in the metacatalog");
            fail = true;
          }

        }
      }
      if (fail) {
        iterator.remove();
        nbDocInvalid++;
      }

    }

    for (Iterator<MetadataRecords> iterator = metadataRecords.iterator(); iterator.hasNext();) {
      MetadataRecords doc = iterator.next();
      if (doc.get(MetacatalogField.FOOTPRINT.getField()) == null) {
        logger.info("No geometry defined for record : " + doc.get(MetacatalogField.IDENTIFIER.getField())
            + " not inserted in the metacatalog");
        nbDocInvalid++;
        iterator.remove();
      }
    }

    /* iterate on concepts fields */
    for (Iterator<MetadataRecords> iterator = metadataRecords.iterator(); iterator.hasNext();) {

      MetadataRecords doc = iterator.next();

      boolean fail = false;

      for (MetacatalogField field : thesaurusFields) {
        Field value = doc.findFirstField(field.getField());
        if (value != null) {
          if (searcher.conceptExists(value.getValue().toString())) {
            doc.add(MetacatalogField._CONCEPTS.getField(), value.getValue());
          }
          else {
            logger.info("Concept " + value.getValue().toString() + " not found in the thesaurus for field : "
                + field.getField() + ". Document " + doc.get(MetacatalogField.IDENTIFIER.getField())
                + " not inserted in the metacatalog");
            fail = true;

          }
        }
        else {
          logger.info("Concept empty for field : " + field.getField() + ". Document "
              + doc.get(MetacatalogField.IDENTIFIER.getField()) + " not inserted in the metacatalog");
          fail = true;
        }
      }

      if (fail) {
        logger.info("-------------------------------------------------------------------------");
        iterator.remove();
        nbDocInvalid++;
      }

    }

    status.setNbDocumentsInvalid(status.getNbDocumentsInvalid() + nbDocInvalid);

    next.execute(data);
  }

  private ThesaurusSearcher getThesaurusSearcher(String thesaurusPath) throws ProcessException {
    try {
      return new ThesaurusSearcher(thesaurusPath);
    }
    catch (IOException e) {
      throw new ProcessException("Cannot find thesaurus : " + thesaurusPath, e);
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
    CheckStepsInformation info = new CheckStepsInformation(true);
    if (next != null) {
      info = next.check();
    }
    if (!info.isOk()) {
      return info;
    }
    String thesaurusPath = HarvesterSettings.getInstance().getString(Consts.THESAURUS_PATH);
    try {
      getThesaurusSearcher(thesaurusPath);
    }
    catch (ProcessException e) {
      info.setOk(false);
      info.setMessage("Cannot find thesaurus : " + thesaurusPath);
    }
    return info;

  }

}
