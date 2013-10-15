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
package fr.cnes.sitools.metacatalogue.resources.model;

/**
 * L’élément son est un tableau qui contient la liste des énumérations possibles pour un paramètre de type enumeration.
 * Chaque énumération contient les éléments suivants :
 * 
 * 
 * @author m.gond
 */
public class EnumSon {
  /**
   * un identifiant unique pour cette liste d’énumération. Cet élément est obligatoire
   */
  private String id;
  /**
   * le nom de l’énumération tel qu’il doit être affiché à l’utilisateur. Ce nom doit être rédigé en langue anglaise.
   * Cet élément est obligatoire.
   */
  private String title;
  /**
   * le nombre de résultats du jeu de données correspondant à la valeur de cette énumération pour le paramètre idoine.
   * Cet élément est optionnel
   */
  private long population;
  /**
   * la valeur envoyée au serveur qui correspond à cette énumération. Ce élément est obligatoire.
   */
  private String value;

  /**
   * Gets the id value
   * 
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * Sets the value of id
   * 
   * @param id
   *          the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Gets the title value
   * 
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * Sets the value of title
   * 
   * @param title
   *          the title to set
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * Gets the population value
   * 
   * @return the population
   */
  public long getPopulation() {
    return population;
  }

  /**
   * Sets the value of population
   * 
   * @param population
   *          the population to set
   */
  public void setPopulation(long population) {
    this.population = population;
  }

  /**
   * Gets the value value
   * 
   * @return the value
   */
  public String getValue() {
    return value;
  }

  /**
   * Sets the value of value
   * 
   * @param value
   *          the value to set
   */
  public void setValue(String value) {
    this.value = value;
  }

}
