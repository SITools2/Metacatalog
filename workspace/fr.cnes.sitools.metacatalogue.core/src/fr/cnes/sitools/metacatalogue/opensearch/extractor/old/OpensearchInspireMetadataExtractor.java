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
package fr.cnes.sitools.metacatalogue.opensearch.extractor.old;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.minidev.json.JSONObject;

import org.restlet.Context;

import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

import fr.cnes.sitools.metacatalogue.common.Converter;
import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.Metadata;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Fields;
import fr.cnes.sitools.metacatalogue.opensearch.extractor.OpensearchGeometryExtractor;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.old.InspireIndexField;
import fr.cnes.sitools.model.AttributeCustom;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.Property;

public class OpensearchInspireMetadataExtractor extends HarvesterStep {

  private String schemaName;

  private Logger logger;

  private HarvesterModel conf;

  private Context context;

  public OpensearchInspireMetadataExtractor(HarvesterModel conf, Context context) {
    this.schemaName = conf.getCatalogType();
    this.conf = conf;
    logger = context.getLogger();
    this.context = context;
  }

  @Override
  public void execute(Metadata data) throws ProcessException {

    String metadata = data.getJsonData();

    List<JSONObject> features = JsonPath.read(metadata, "$.features");

    List<Fields> listFields = new ArrayList<Fields>();

    for (JSONObject jsonObject : features) {

      String jsonString = jsonObject.toJSONString();
      Fields fields = new Fields();
      addField(fields, "$.properties.identifier", jsonString, InspireIndexField.RESOURCE_UNIQUE_IDENTIFIER.getField());
      addField(fields, "$.properties.identifier", jsonString, InspireIndexField.UUID.getField());
      addField(fields, "$.properties.title", jsonString, InspireIndexField.RESOURCE_TITLE.getField());
      addField(fields, "$.properties.description", jsonString, InspireIndexField.RESOURCE_ABSTRACT.getField());
      addField(fields, "dataset", InspireIndexField.RESOURCE_TYPE.getField());

      addField(fields, "$.properties.project", jsonString, InspireIndexField.KEYWORD_VALUE.getField());
      addField(fields, "$.properties.product", jsonString, InspireIndexField.KEYWORD_VALUE.getField());
      addField(fields, "$.properties.platform", jsonString, InspireIndexField.KEYWORD_VALUE.getField());
      addField(fields, "$.properties.instrument", jsonString, InspireIndexField.KEYWORD_VALUE.getField());

      addField(fields, new Date().toString(), InspireIndexField.DATE_DE_PUBLICATION.getField());

      addField(fields, "$.properties.startDate", jsonString, InspireIndexField.TEMPORAL_EXTENT.getField());
      addField(fields, "$.properties.completionDate", jsonString, InspireIndexField.TEMPORAL_EXTENT.getField());

      addField(fields, "$.properties.resolution", jsonString, InspireIndexField.SPATIAL_RESOLUTION.getField());
      addField(fields, "$.properties.modified", jsonString, InspireIndexField.METADATA_DATE.getField());

      // geometry
      addField(fields, "$.geometry", jsonString, "geometry");

      OpensearchGeometryExtractor extractor = new OpensearchGeometryExtractor();

      try {
        String geometry = JsonPath.read(jsonString, "$.geometry").toString();
        fields = extractor.extractGeometry(geometry, fields, context);
      }
      catch (Exception e) {
        logger.log(Level.WARNING, e.getMessage(), e);
      }

      // add the custom attributes
      addCustomAttributes(jsonString, fields, conf.getAttributes());

      System.out.println(fields.toString());
      listFields.add(fields);

    }

    if (data.getFields() == null) {
      data.setFields(listFields);
    }
    else {
      data.getFields().addAll(listFields);
    }
    next.execute(data);

  }

  private void addCustomAttributes(String json, Fields fields, List<AttributeCustom> attributes) {
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

  private void addField(Fields fields, String jsonPath, String json, String fieldName) {
    try {
      Object object = JsonPath.read(json, jsonPath);
      fields.add(fieldName, object);
    }
    catch (InvalidPathException e) {
      logger.log(Level.WARNING, "Invalid path : " + jsonPath, e);
    }
  }

  private void addField(Fields fields, String value, String fieldName) {
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
