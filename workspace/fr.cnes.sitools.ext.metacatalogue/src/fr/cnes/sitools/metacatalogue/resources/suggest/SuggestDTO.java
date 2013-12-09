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
package fr.cnes.sitools.metacatalogue.resources.suggest;

public class SuggestDTO {

  private String suggestion;

  private String suggestionAltLabel;

  private long nb;

  /**
   * Gets the suggestion value
   * 
   * @return the suggestion
   */
  public String getSuggestion() {
    return suggestion;
  }

  /**
   * Sets the value of suggestion
   * 
   * @param suggestion
   *          the suggestion to set
   */
  public void setSuggestion(String suggestion) {
    this.suggestion = suggestion;
  }

  /**
   * Gets the nb value
   * 
   * @return the nb
   */
  public long getNb() {
    return nb;
  }

  /**
   * Sets the value of nb
   * 
   * @param nb
   *          the nb to set
   */
  public void setNb(long nb) {
    this.nb = nb;
  }

  /**
   * Gets the suggestionAltLabel value
   * 
   * @return the suggestionAltLabel
   */
  public String getSuggestionAltLabel() {
    return suggestionAltLabel;
  }

  /**
   * Sets the value of suggestionAltLabel
   * 
   * @param suggestionAltLabel
   *          the suggestionAltLabel to set
   */
  public void setSuggestionAltLabel(String suggestionAltLabel) {
    this.suggestionAltLabel = suggestionAltLabel;
  }

}
