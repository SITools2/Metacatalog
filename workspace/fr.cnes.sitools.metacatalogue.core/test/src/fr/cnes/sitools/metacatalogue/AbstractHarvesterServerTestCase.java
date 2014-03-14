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
package fr.cnes.sitools.metacatalogue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.engine.Engine;

import fr.cnes.sitools.server.Starter;

/**
 * Classe de base pour les tests unitaires des Serveurs/Applications Restlet et de la persistance des données
 * 
 * On suppose qu'un serveur est lancé au démarrage de la suite de tests avec une configuration donnée.
 * 
 * @author AKKA Technologies
 * @see org.restlet.test.RestletTestCase
 */
public abstract class AbstractHarvesterServerTestCase extends AbstractHarvesterTestCase {

  /**
   * absolute url for sitools REST API
   * 
   * @return url
   */
  protected static String getHostUrl() {
    return "http://localhost:" + getTestPort();
  }

  /**
   * Executed before each test method.
   * 
   * @throws Exception
   *           if failed
   */
  @Before
  public void setUp() throws Exception {
    //
  }

  /**
   * Executed after each test method.
   * 
   * @throws Exception
   *           if failed
   */
  @After
  public void tearDown() throws Exception {
    //
  }

  /**
   * Trace global parameters for the test.
   */
  @Test
  public void testConfig() {
    LOGGER.info(this.getClass().getName() + " TEST BASE URL = " + getBaseUrl());
    LOGGER.info(this.getClass().getName() + " TEST REPOSITORY = " + getTestRepository());
    LOGGER.info(this.getClass().getName() + " TEST PORT = " + getTestPort());

    // asert assertTrue("Check data directory presence", (new
    // File(getTestRepository()).exists()));
  }

  /**
   * Executed once before all test methods
   */
  @BeforeClass
  public static void before() {
    setup();
    start();
  }

  /** Starts server */
  protected static void start() {
    try {
      // String[] args = new String[0];
      // ProxySettings.init(args, settings);
      Starter.start("localhost", getTestPort(), "http://localhost:" + getTestPort());
    }
    catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  /**
   * Executed once after all test methods
   */
  @AfterClass
  public static void afterClass() {
    Starter.stop();
    Engine.clearThreadLocalVariables();

  }

}
