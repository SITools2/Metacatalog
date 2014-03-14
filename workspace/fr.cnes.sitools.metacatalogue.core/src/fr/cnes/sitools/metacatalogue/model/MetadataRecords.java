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
package fr.cnes.sitools.metacatalogue.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Stores a List of {@link Field}
 * 
 * @author m.gond
 * 
 */
public class MetadataRecords {
  /** List of Field */
  private List<Field> fields;
  private List<Error> errors;

  /**
   * Constuctor
   */
  public MetadataRecords() {
    fields = new ArrayList<Field>();
    errors = new ArrayList<Error>();
  }
  
  public MetadataRecords(List<Field> _fields, List<Error> _errors) {
    fields = _fields;
    errors = _errors;
  }

  /**
   * Get the first Field value found or null if the field doesn't exist
   * 
   * @param name
   *          the name of the field to get
   * @return the first Field value found or null if the field doesn't exist
   */
  public Object get(String name) {
    Field field = findFirstField(name);
    if (field != null) {
      return field.getValue();
    }
    else {
      return null;
    }
  }
  
  
  public Object getError(String name) {
    Error error = findFirstError(name);
    if (error != null) {
      return error.getValue();
    }
    else {
      return null;
    }
  }


  /**
   * Add a new Field with the given name and value
   * 
   * @param name
   *          the name
   * @param value
   *          the value
   */
  public void add(String name, Object value) {
    fields.add(new Field(name, value));
  }

  /**
   * Remove the first occurence of a Field, return true if the field has been removed, false otherwise
   * 
   * @param name
   *          the name of the field to remove
   * @return true if the first occurence of the field has been removed, false otherwise
   */
  public boolean remove(String name) {
    Field field = findFirstField(name);
    if (field != null) {
      return fields.remove(field);
    }
    else {
      return false;
    }
  }

  /**
   * Get the underlying List of Field
   * 
   * @return the List of Field
   */
  public List<Field> getList() {
    return fields;
  }
  
  
  public List<Field> getFields() {
    return fields;
  }
    
  public List<Error> getErrors() {
    return errors;
  }

  
  /**
   * Util method to find the first Field in the List with the specified name
   * 
   * @param name
   *          the name of the field
   * @return the first Field found or null if it does not exist
   */
  public Field findFirstField(String name) {
    Field fieldResult = null;
    Iterator<Field> it = this.fields.iterator();
    while (it.hasNext() && fieldResult == null) {
      Field field = it.next();
      if (name.equals(field.getName())) {
        fieldResult = field;
      }
    }
    return fieldResult;
  }
  
  public Error findFirstError(String name) {
    Error errorFound = null;
    Iterator<Error> it = this.errors.iterator();
    while (it.hasNext() && errorFound == null) {
      Error error = it.next();
      if (name.equals(error.getName())) {
        errorFound = error;
      }
    }
    return errorFound;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String out = "Fields (" + fields.size() + ") : [\n";
    for (Field field : fields) {
      out += field.getName() + " : " + field.getValue().toString() + "\n";
    }
    out += "]";
    return out;
  }



}
