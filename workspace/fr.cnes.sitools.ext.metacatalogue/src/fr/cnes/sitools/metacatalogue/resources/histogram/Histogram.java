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
package fr.cnes.sitools.metacatalogue.resources.histogram;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import fr.cnes.sitools.metacatalogue.utils.MetacatalogField;

/**
 * Creates a distribution of records by date with a number of bins.
 * 
 * @author Jean-Christophe Malapert <jean-christophe.malapert@cnes.fr>
 */
public final class Histogram {
  /** The name of the date field to use */
  private static final String DATE_FIELD = MetacatalogField.START_DATE.getField();
  /**
   * number of bins in the histogram.
   */
  private int binNumber;
  /**
   * starting date in the histogram.
   */
  private String startDate;
  /**
   * Stopping date in the histogram.
   */
  private String stopDate;
  /**
   * The solr url
   */
  private String solr;

  /**
   * Constructs a histogram.
   * 
   * @param binNumber
   *          number of bins in the histogram
   * @param solr
   *          the solr url
   * @throws ParseException
   * @throws IOException
   */
  public Histogram(final int binNumber, final String solr) throws ParseException, IOException {
    this.binNumber = binNumber;
    this.solr = solr;
    parseStatsDate();
  }

  /**
   * Converts a String as a Calendar object and returns the calendar object.
   * 
   * @param dateStr
   *          date to transform
   * @return a calendar object
   * @throws ParseException
   */
  public static Calendar convertToCalendar(final String dateStr) throws ParseException {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    Date date = formatter.parse(dateStr);
    Calendar cal = Calendar.getInstance();
    cal.setTime(date);
    return cal;
  }

  /**
   * Returns the number of days between two dates.
   * 
   * @param startDate
   *          the starting date
   * @param endDate
   *          the stopping date
   * @return the number of days between two dates
   */
  public static long daysBetween(Calendar startDate, Calendar endDate) {
    Calendar date = (Calendar) startDate.clone();
    long daysBetween = 0;
    while (date.before(endDate)) {
      date.add(Calendar.DAY_OF_MONTH, 1);
      daysBetween++;
    }
    return daysBetween;
  }

  /**
   * Computes the number of days by bin.
   * 
   * @return the number of days by bin
   * @throws ParseException
   */
  public long daysByBin() throws ParseException {
    Calendar startCalendar = convertToCalendar(startDate);
    Calendar stopCalendar = convertToCalendar(stopDate);
    return (long) Math.ceil((double) daysBetween(startCalendar, stopCalendar) / (double) this.binNumber);
  }

  /**
   * Returns the JSON distribution.
   * 
   * @return JSON distribution
   * @throws ParseException
   */
  public Representation getHistogram() throws ParseException {
    long daysByBin = daysByBin();
    String query = this.solr + "/select?q=*:*&rows=0&facet=true&facet.date=" + DATE_FIELD + "&facet.date.start="
        + this.startDate + "&facet.date.end=" + this.stopDate + "&facet.date.gap=%2B" + daysByBin + "DAY&wt=json";
    ClientResource client = new ClientResource(query);
    return client.get();
  }

  /**
   * Parses the stats module from Solr.
   * 
   * @throws IOException
   * @throws ParseException
   */
  private void parseStatsDate() throws IOException, ParseException {
    ClientResource client = new ClientResource(this.solr + "/select?q=*:*&rows=0&stats=true&stats.field=" + DATE_FIELD
        + "&wt=json");
    Representation rep = client.get();

    // read the suggest JSON
    ObjectMapper mapper = new ObjectMapper();
    // (note: can also use more specific type, like ArrayNode or ObjectNode!)
    JsonNode rootNode = mapper.readValue(rep.getStream(), JsonNode.class); // src can be a File, URL,

    JsonNode stats = rootNode.get("stats");
    JsonNode statsFields = stats.get("stats_fields");
    JsonNode date = statsFields.get(DATE_FIELD);
    this.startDate = date.get("min").getTextValue();
    this.stopDate = date.get("max").getTextValue();
  }

  /**
   * Test
   */
  public static void main(String[] args) {
    try {
      Histogram histo = new Histogram(10, "http://localhost:8983/solr/metacatalogue");
      System.out.println(histo.getHistogram().getText());
    }
    catch (IOException ex) {
      Engine.getLogger(Histogram.class.getName()).log(Level.SEVERE, null, ex);
    }
    catch (ParseException ex) {
      Engine.getLogger(Histogram.class.getName()).log(Level.SEVERE, null, ex);
    }

  }
}
