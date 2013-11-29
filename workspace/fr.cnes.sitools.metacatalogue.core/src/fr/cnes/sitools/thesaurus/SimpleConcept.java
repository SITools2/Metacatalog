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
package fr.cnes.sitools.thesaurus;

public class SimpleConcept {

  private String altEn;

  private String prefEn;

  private String prefFr;

  /**
   * Gets the altEn value
   * 
   * @return the altEn
   */
  public String getAltEn() {
    return altEn;
  }

  /**
   * Sets the value of altEn
   * 
   * @param altEn
   *          the altEn to set
   */
  public void setAltEn(String altEn) {
    this.altEn = altEn;
  }

  /**
   * Gets the prefEn value
   * 
   * @return the prefEn
   */
  public String getPrefEn() {
    return prefEn;
  }

  /**
   * Sets the value of prefEn
   * 
   * @param prefEn
   *          the prefEn to set
   */
  public void setPrefEn(String prefEn) {
    this.prefEn = prefEn;
  }

  /**
   * Gets the prefFr value
   * 
   * @return the prefFr
   */
  public String getPrefFr() {
    return prefFr;
  }

  /**
   * Sets the value of prefFr
   * 
   * @param prefFr
   *          the prefFr to set
   */
  public void setPrefFr(String prefFr) {
    this.prefFr = prefFr;
  }

  public static SimpleConcept fromConcept(Concept concept) {
    SimpleConcept simpleConcept = new SimpleConcept();
    simpleConcept.setAltEn(getConceptValue(concept, "altLabelEn"));
    simpleConcept.setPrefEn(getConceptValue(concept, "prefLabelEn"));
    simpleConcept.setPrefFr(getConceptValue(concept, "prefLabelFr"));
    return simpleConcept;
  }

  private static String getConceptValue(Concept concept, String conceptName) {
    Object conceptValue = concept.getProperties().get(conceptName);
    if (conceptValue != null) {
      return conceptValue.toString();
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "SimpleConcept [altEn=" + altEn + ", prefEn=" + prefEn + ", prefFr=" + prefFr + "]";
  }

}
