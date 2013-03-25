package fr.cnes.sitools.model;

import java.util.Date;

import org.restlet.engine.util.DateUtils;

import com.thoughtworks.xstream.converters.basic.DateConverter;

/**
 * Date converte for tests
 * 
 * @author m.marseille
 */
public final class SitoolsDateConverter extends DateConverter {

  @Override
  public Object fromString(String arg0) {
    return DateUtils.parse(arg0, fr.cnes.sitools.util.DateUtils.SITOOLS_DATE_FORMAT);
  }

  @Override
  public String toString(Object obj) {
    if ((obj != null) && (obj instanceof Date)) {
      return DateUtils.format((Date) obj, fr.cnes.sitools.util.DateUtils.SITOOLS_DATE_FORMAT.get(0));
    }
    return super.toString(obj);
  }

}
