/*******************************************************************************
 * Copyright 2011, 2012 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.util.List;

/**
 * Modèle qui permet de représenter un élément du tableau filters de description
 * des paramètres de recherche sur un dataset
 * 
 * L’élément filters est un tableau qui contient la liste des descriptions des
 * paramètres spécifiques. Chaque description contient les éléments suivants :
 * 
 * @author m.gond
 */
public class Filter {

  /**
   * le nom de la clé correspondant au paramètre tel que défini dans le modèle
   * d’url (cf. Interfaces - §2.2.2). Cet élément est obligatoire.
   */
  private String id;
  /**
   * le nom de la clé tel qu’il doit être affiché à l’utilisateur. Ce nom doit
   * être rédigé en langue anglaise. Cet élément est obligatoire.
   */
  private String title;
  /**
   * le type du paramètre. Les types supportés sont : date, enumeration, text,
   * number. Cet élément est obligatoire.
   */
  private FilterType type;
  /**
   * la taille en nombre de caractères que doit avoir la boite de saisie textuel
   * pour ce paramètre sur jeobrowser. Cet élément ne s’applique qu’aux types
   * text et number. Cet élément est optionnel.
   */
  private Integer size;
  /**
   * l’opérateur de comparaison applicable à ce paramètre. Les valeurs possibles
   * sont1 eq, lt, gt, bt. Cet élément ne s’applique qu’au type number. Cet
   * élément est optionnel.
   */
  private String operator;
  /**
   * le nombre total de résultats du jeu de données. Cet élément ne s’applique
   * qu’au type enumeration. Cet élément est optionnel
   */
  private Integer population;
  /**
   * la liste des énumérations possible pour le paramètre. Cet élément ne
   * s’applique qu’au type enumeration. Cet élément est optionnel
   */
  private List<EnumSon> son;
  /** la valeur par défaut du paramètre. Cet élément est optionnel. */
  private String value;
  /**
   * cet élément ne s’applique qu’au type enumeration. C’est un booléen qui
   * indique si plusieurs élements de l’énumération peuvent être séléctionné par
   * l’utilisateur (unique:false) ou bien si la sélection est exclusive, c’est à
   * dire qu’un seul élément peut être séléctionné par l’utilisateur
   * (unique:false). Cet élément est optionnel et vaut false par défaut si il
   * n’est pas spécifié.
   */
  private Boolean unique;

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
   * Gets the type value
   * 
   * @return the type
   */
  public FilterType getType() {
    return type;
  }

  /**
   * Sets the value of type
   * 
   * @param type
   *          the type to set
   */
  public void setType(FilterType type) {
    this.type = type;
  }

  /**
   * Gets the size value
   * 
   * @return the size
   */
  public Integer getSize() {
    return size;
  }

  /**
   * Sets the value of size
   * 
   * @param size
   *          the size to set
   */
  public void setSize(Integer size) {
    this.size = size;
  }

  /**
   * Gets the operator value
   * 
   * @return the operator
   */
  public String getOperator() {
    return operator;
  }

  /**
   * Sets the value of operator
   * 
   * @param operator
   *          the operator to set
   */
  public void setOperator(String operator) {
    this.operator = operator;
  }

  /**
   * Gets the population value
   * 
   * @return the population
   */
  public Integer getPopulation() {
    return population;
  }

  /**
   * Sets the value of population
   * 
   * @param population
   *          the population to set
   */
  public void setPopulation(Integer population) {
    this.population = population;
  }

  /**
   * Gets the son value
   * 
   * @return the son
   */
  public List<EnumSon> getSon() {
    return son;
  }

  /**
   * Sets the value of son
   * 
   * @param son
   *          the son to set
   */
  public void setSon(List<EnumSon> son) {
    this.son = son;
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

  /**
   * Gets the unique value
   * 
   * @return the unique
   */
  public Boolean getUnique() {
    return unique;
  }

  /**
   * Sets the value of unique
   * 
   * @param unique
   *          the unique to set
   */
  public void setUnique(Boolean unique) {
    this.unique = unique;
  }

}
