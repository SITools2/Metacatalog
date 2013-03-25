package fr.cnes.sitools.metacatalogue.model;

public class Field {

  private String name;

  private Object value;

  public Field(String name, Object value) {
    super();
    this.name = name;
    this.value = value;
  }

  /**
   * Gets the name value
   * 
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of name
   * 
   * @param name
   *          the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the value value
   * 
   * @return the value
   */
  public Object getValue() {
    return value;
  }

  /**
   * Sets the value of value
   * 
   * @param value
   *          the value to set
   */
  public void setValue(Object value) {
    this.value = value;
  }

}