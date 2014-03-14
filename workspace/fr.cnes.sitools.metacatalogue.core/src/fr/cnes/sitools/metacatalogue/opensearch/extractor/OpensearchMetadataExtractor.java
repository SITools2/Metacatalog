/*******************************************************************************
 * Copyright 2010-2014 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.sitools.metacatalogue.opensearch.extractor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minidev.json.JSONObject;

import org.restlet.Context;
import org.restlet.engine.util.DateUtils;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

import fr.cnes.sitools.metacatalogue.common.Converter;
import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Error;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.model.AttributeCustom;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.Property;
import fr.cnes.sitools.server.ContextAttributes;

public class OpensearchMetadataExtractor extends HarvesterStep {

  private Logger logger;

  private HarvesterModel conf;

  private Context context;

  public OpensearchMetadataExtractor(HarvesterModel conf, Context context) {
    this.conf = conf;
    this.context = context;

  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {
    logger = getLogger(context);
    String metadata = data.getJsonData();

    List<JSONObject> features = JsonPath.read(metadata, "$.features");

    List<MetadataRecords> listFields = new ArrayList<MetadataRecords>();

    for (JSONObject jsonObject : features) {

      String jsonString = jsonObject.toJSONString();
      MetadataRecords record = new MetadataRecords();
      addField(record, "$.properties.identifier", jsonString, MetacatalogField.IDENTIFIER);
      addField(record, "$.properties.title", jsonString, MetacatalogField.TITLE);
      addField(record, "$.properties.description", jsonString, MetacatalogField.DESCRIPTION);

      addField(record, "$.properties.project", jsonString, MetacatalogField.PROJECT);
      addField(record, "$.properties.product", jsonString, MetacatalogField.PRODUCT);

      addField(record, "$.properties.platform", jsonString, MetacatalogField.PLATFORM);
      addField(record, "$.properties.instrument", jsonString, MetacatalogField.INSTRUMENT);

      addField(record, "$.properties.authority", jsonString, MetacatalogField.AUTHORITY);
      addField(record, "$.properties.processingLevel", jsonString, MetacatalogField.PROCESSING_LEVEL);

      // addField(fields, new Date().toString(), MetacatalogField.MODIFICATION_DATE);

      addField(record, "$.properties.startDate", jsonString, MetacatalogField.START_DATE);
      addField(record, "$.properties.completionDate", jsonString, MetacatalogField.COMPLETION_DATE);

      addField(record, "$.properties.resolution", jsonString, MetacatalogField.RESOLUTION);

      addField(record, "$.properties.wms", jsonString, MetacatalogField.WMS);

      addField(record, "$.properties.services.download.url", jsonString, MetacatalogField.ARCHIVE);
      addField(record, "$.properties.services.download.mimeType", jsonString, MetacatalogField.MIME_TYPE);

      // addField(fields, "$.properties.services.metadata.url", jsonString,
      // MetacatalogField.SERVICES_METADATA_URL);

      addField(record, "$.properties.quicklook", jsonString, MetacatalogField.QUICKLOOK);
      addField(record, "$.properties.thumbnail", jsonString, MetacatalogField.THUMBNAIL);

      // geometry
      addField(record, "$.geometry", jsonString, MetacatalogField._GEOMETRY_GEOJSON);

      // public services
      addField(record, String.valueOf(conf.isPublicServices()), MetacatalogField._PUBLIC_SERVICES.getField());

      HarvestStatus status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);

      // modified
      List<String> frmt = HarvesterSettings.getInstance().getDateFormats();
      String modified = DateUtils.format(status.getStartDate(), frmt);

      addField(record, modified, MetacatalogField.MODIFIED.getField());

      OpensearchGeometryExtractor extractor = new OpensearchGeometryExtractor();

      try {
        String geometry = JsonPath.read(jsonString, "$.geometry").toString();
        record = extractor.extractGeometry(geometry, record, context);
      }
      catch (Exception e) {
        logger.log(Level.WARNING, e.getMessage(), e);
        throw new ProcessException(e);
      }

      // add the custom attributes
      addCustomAttributes(jsonString, record, conf.getAttributes());

      listFields.add(record);

    }

    if (data.getMetadataRecords() == null) {
      data.setMetadataRecords(listFields);
    }
    else {
      data.getMetadataRecords().addAll(listFields);
    }

    if (next != null) {
      next.execute(data);
    }

  }

  private void addCustomAttributes(String json, MetadataRecords fields, List<AttributeCustom> attributes) {
    if (attributes != null) {
      for (AttributeCustom attributeCustom : attributes) {
        if (attributeCustom.getValue() != null) {
          fields.add(attributeCustom.getName(), attributeCustom.getValue());
        }
        else {
          Object object = JsonPath.read(json, attributeCustom.getPath());
          if (attributeCustom.getConverterClass() != null) {
            object = applyConverter(object, attributeCustom.getConverterClass(),
                attributeCustom.getConverterParameters());
          }
          fields.add(attributeCustom.getName(), object);
        }
      }
    }
  }

  private void addField(MetadataRecords fields, String jsonPath, String json, MetacatalogField metacatalogField) {
    try {
      Object object = JsonPath.read(json, jsonPath);
      fields.add(metacatalogField.getField(), object);
    }
    catch (InvalidPathException e) {
      logger.log(Level.WARNING, "Invalid path : " + jsonPath, e);
      if (metacatalogField.isMandatory()) {
        Object objId = fields.get(MetacatalogField.IDENTIFIER.getField());
        String id = "";
        if (objId != null) {
          id = objId.toString();
        }
        Error error = new Error(metacatalogField.getField(), metacatalogField.getField() + " not found for record "
            + id + " with this JSONPath expression \"" + jsonPath + "\"");
        fields.getErrors().add(error);
      }
    }
  }

  private void addField(MetadataRecords fields, String value, String fieldName) {
    fields.add(fieldName, value);
  }

  private void addField(MetadataRecords fields, Date value, String fieldName) {
    fields.add(fieldName, value);
  }

  private Object applyConverter(Object object, String converterClass, List<Property> converterParameters) {
    try {
      Class<Converter> convClass = (Class<Converter>) Class.forName(converterClass);
      Converter conv = convClass.newInstance();
      object = conv.convert(object, converterParameters);
    }
    catch (ClassNotFoundException e) {
      logger.log(Level.WARNING, "Cannot find converter class : " + converterClass, e);
    }
    catch (InstantiationException e) {
      logger.log(Level.WARNING, "Cannot instanciate converter class : " + converterClass, e);
    }
    catch (IllegalAccessException e) {
      logger.log(Level.WARNING, "Cannot access converter class : " + converterClass, e);
    }
    return object;
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
