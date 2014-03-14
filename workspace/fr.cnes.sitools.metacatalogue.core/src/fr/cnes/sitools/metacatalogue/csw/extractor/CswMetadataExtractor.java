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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.fao.geonet.csw.common.util.Xml;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.restlet.Context;
import org.restlet.engine.util.DateUtils;

import fr.cnes.sitools.metacatalogue.common.Converter;
import fr.cnes.sitools.metacatalogue.common.HarvesterStep;
import fr.cnes.sitools.metacatalogue.common.MetadataContainer;
import fr.cnes.sitools.metacatalogue.exceptions.ProcessException;
import fr.cnes.sitools.metacatalogue.model.Error;
import fr.cnes.sitools.metacatalogue.model.Field;
import fr.cnes.sitools.metacatalogue.model.HarvestStatus;
import fr.cnes.sitools.metacatalogue.model.MetadataRecords;
import fr.cnes.sitools.metacatalogue.utils.CheckStepsInformation;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;
import fr.cnes.sitools.metacatalogue.utils.XSLTUtils;
import fr.cnes.sitools.model.AttributeCustom;
import fr.cnes.sitools.model.HarvesterModel;
import fr.cnes.sitools.model.Property;
import fr.cnes.sitools.server.ContextAttributes;

public class CswMetadataExtractor extends HarvesterStep {

  private String schemaName;

  private Logger logger;

  private HarvesterModel conf;

  private Context context;

  public CswMetadataExtractor(HarvesterModel conf, Context context) {
    this.schemaName = conf.getCatalogType();
    this.context = context;
    this.conf = conf;
  }

  @Override
  public void execute(MetadataContainer data) throws ProcessException {
    logger = getLogger(context);
    Element metadata = data.getXmlData();

    String resourcesFolder = HarvesterSettings.getInstance().getResourcePath(schemaName, "solr-fields.xsl");
    File sFileXSL = new File(resourcesFolder);

    List<Element> children = metadata.getChildren();

    List<MetadataRecords> listMetadataRecords = new ArrayList<MetadataRecords>();

    for (Element child : children) {

      try {
        XSLTUtils utils = XSLTUtils.getInstance();
        InputStream stream = utils.transform(sFileXSL, child);

        // BufferedReader stdIn = new BufferedReader(new InputStreamReader(stream));
        // String userInput;
        //
        // while ((userInput = stdIn.readLine()) != null) {
        // System.out.println(userInput);
        // }

        Element doc = Xml.loadStream(stream);

        List<Field> fields = getFields(doc);
        List<Error> errors = getErrors(doc);

        CswGeometryExtractor extractor = new CswGeometryExtractor();

        MetadataRecords mdRecords = new MetadataRecords(fields, errors);

        mdRecords = extractor.extractGeometry(child, mdRecords, this.schemaName);
        if (mdRecords != null) {
          // add the custom attributes
          addCustomAttributes(child, mdRecords, conf.getAttributes());

          // public services
          addField(mdRecords, String.valueOf(conf.isPublicServices()), MetacatalogField._PUBLIC_SERVICES.getField());

          HarvestStatus status = (HarvestStatus) context.getAttributes().get(ContextAttributes.STATUS);

          // modified
          List<String> frmt = HarvesterSettings.getInstance().getDateFormats();
          String modified = DateUtils.format(status.getStartDate(), frmt);

          addField(mdRecords, modified, MetacatalogField.MODIFIED.getField());

          listMetadataRecords.add(mdRecords);

        }

      }
      catch (TransformerConfigurationException e) {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
      catch (TransformerException e) {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
      catch (Exception e) {
        logger.log(Level.WARNING, e.getMessage(), e);
      }
    }

    if (data.getMetadataRecords() == null) {
      data.setMetadataRecords(listMetadataRecords);
    }
    else {
      data.getMetadataRecords().addAll(listMetadataRecords);
    }
    next.execute(data);

  }

  private List<Field> getFields(Element doc) {

    List<Field> fields = new ArrayList();
    List<Element> xmlFields = doc.getChildren("field");
    String name;
    Object value;
    for (Element child : xmlFields) {
      name = child.getAttributeValue("name");
      value = child.getText();
      Field field = new Field(name, value);
      fields.add(field);
    }
    return fields;

  }

  private List<Error> getErrors(Element doc) {

    List<Error> errors = new ArrayList();
    List<Element> xmlFields = doc.getChildren("error");
    String name;
    String level;
    Object value;
    for (Element child : xmlFields) {
      name = child.getAttributeValue("name");
      level = child.getAttributeValue("level");
      value = child.getText();
      Error error = new Error(name, level, value);
      errors.add(error);
    }
    return errors;

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

  private void addField(MetadataRecords fields, String value, String fieldName) {
    fields.add(fieldName, value);
  }

  private void addField(MetadataRecords fields, Date value, String fieldName) {
    fields.add(fieldName, value);
  }

  private void addCustomAttributes(Element xml, MetadataRecords fields, List<AttributeCustom> attributes) {
    if (attributes != null) {
      for (AttributeCustom attributeCustom : attributes) {
        if (attributeCustom.getValue() != null) {
          fields.add(attributeCustom.getName(), attributeCustom.getValue());
        }
        else {
          try {
            XPath xpa = XPath.newInstance(attributeCustom.getPath());
            xpa.addNamespace(Namespace.getNamespace("gmd", "http://www.isotc211.org/2005/gmd"));
            xpa.addNamespace(Namespace.getNamespace("gco", "http://www.isotc211.org/2005/gco"));

            Object object = xpa.valueOf(xml);
            if ("".equals(object)) {
              logger.info("ERROR #################################### XPATH");
            }
            if (attributeCustom.getConverterClass() != null) {
              object = applyConverter(object, attributeCustom.getConverterClass(),
                  attributeCustom.getConverterParameters());
            }
            fields.add(attributeCustom.getName(), object);
          }
          catch (JDOMException e) {
            logger.log(Level.WARNING, "XPATH error", e);
          }
        }
      }
    }
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

}
