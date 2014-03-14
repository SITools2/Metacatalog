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
package fr.cnes.sitools.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DateUtils {

  /** AWS date format (ISO 8601). Pattern: "yyyy-MM-dd'T'HH:mm:ss". */
  public static final List<String> FORMAT_ISO_8601_WITHOUT_TIME_ZONE = unmodifiableList("yyyy-MM-dd'T'HH:mm:ss");

  /**
   * Default date format for date exchange between the server and the client in all the Sitools2 application
   */
  public static final List<String> SITOOLS_DATE_FORMAT = unmodifiableList("yyyy-MM-dd'T'HH:mm:ss.SSS");

  /**
   * Date format to use 
   */
  public static final List<String> LOG_FILE_NAME_DATE_FORMAT = unmodifiableList("yyyy-MM-dd_HH-mm-ss");
  
  
  /**
   * Helper method to help initialize this class by providing unmodifiable lists based on arrays.
   * 
   * @param <T>
   *          Any valid java object
   * @param array
   *          to be converted into an unmodifiable list
   * @return unmodifiable list based on the provided array
   */
  private static <T> List<T> unmodifiableList(final T... array) {
    return Collections.unmodifiableList(Arrays.asList(array));
  }

  /**
   * Private constructor to ensure that the class acts as a true utility class i.e. it isn't instantiable and
   * extensible.
   */
  private DateUtils() {

  }

}
