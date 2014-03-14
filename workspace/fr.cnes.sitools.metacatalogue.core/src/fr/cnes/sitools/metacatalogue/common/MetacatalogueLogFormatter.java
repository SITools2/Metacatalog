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
package fr.cnes.sitools.metacatalogue.common;

import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * The Class MetacatalogueLogFormatter. Log only the message, not the class informations
 * 
 * @author m.gond
 */
public class MetacatalogueLogFormatter extends SimpleFormatter {

  /**
   * format a LogRecord
   * 
   * @param record
   *          the LogRecord to log
   * @return the formatted string
   */
  public String format(LogRecord record) {
    return record.getLevel() + " : " + record.getMessage() + "\r\n";
  }
}
