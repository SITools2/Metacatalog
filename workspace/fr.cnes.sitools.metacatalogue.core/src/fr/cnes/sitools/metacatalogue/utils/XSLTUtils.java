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
package fr.cnes.sitools.metacatalogue.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.FeatureKeys;

import org.jdom.Element;
import org.jdom.transform.JDOMSource;

/**
 * XSLT utils class to transform
 * 
 * @author m.gond
 * 
 */
public class XSLTUtils {

  private static XSLTUtils instance = null;

  private static TransformerFactory transFact;

  private XSLTUtils() {

  }

  private static void init() throws TransformerConfigurationException {
    instance = new XSLTUtils();
    Properties props = System.getProperties();
    props.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");

    transFact = TransformerFactoryFactory.getTransformerFactory();

    transFact.setAttribute(FeatureKeys.VERSION_WARNING, false);
    transFact.setAttribute(FeatureKeys.LINE_NUMBERING, true);
    transFact.setAttribute(FeatureKeys.PRE_EVALUATE_DOC_FUNCTION, true);
    transFact.setAttribute(FeatureKeys.RECOVERY_POLICY, Configuration.RECOVER_SILENTLY);

  }

  public static XSLTUtils getInstance() throws TransformerConfigurationException {
    if (instance == null) {
      init();
    }
    return instance;
  }

  public InputStream transform(File xsltFile, InputStream xmlFile) throws TransformerException {
    StreamSource srcSheet = new StreamSource(xsltFile);
    Transformer t = transFact.newTransformer(srcSheet);
    StringWriter swOut = new StringWriter();
    StreamResult result = new StreamResult(swOut);
    t.transform(new StreamSource(xmlFile), result);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(swOut.getBuffer().toString().getBytes());
    return inputStream;

  }

  public InputStream transform(File xsltFile, Element element) throws TransformerException {
    StreamSource srcSheet = new StreamSource(xsltFile);
    Transformer t = transFact.newTransformer(srcSheet);
    StringWriter swOut = new StringWriter();
    StreamResult result = new StreamResult(swOut);
    t.transform(new JDOMSource(element), result);
    ByteArrayInputStream inputStream = new ByteArrayInputStream(swOut.getBuffer().toString().getBytes());
    return inputStream;

  }

}
