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
package fr.cnes.sitools.metacatalogue.resources.proxyservices;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

/**
 * 
 */
public class SecureOutputRepresentation extends OutputRepresentation {

  CloseableHttpResponse response;

  public SecureOutputRepresentation(MediaType mediaType) {
    super(mediaType);
  }

  public SecureOutputRepresentation(MediaType _mediaType, CloseableHttpResponse _response) {
    super(_mediaType);
    response = _response;
  }

  @Override
  public void write(OutputStream os) throws IOException {

    Writer out = new OutputStreamWriter(os);

    InputStream is = response.getEntity().getContent();

    BufferedInputStream buff = new BufferedInputStream(is);
    int i;
    do {
      i = buff.read();
      out.write((char) i);
    } while (i != -1);

  }

}
