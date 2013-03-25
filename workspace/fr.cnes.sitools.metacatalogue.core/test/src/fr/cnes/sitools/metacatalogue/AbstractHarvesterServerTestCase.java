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
package fr.cnes.sitools.metacatalogue;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.engine.Engine;

import fr.cnes.sitools.metacatalogue.utils.FileCopyUtils;
import fr.cnes.sitools.metacatalogue.utils.FileUtils;
import fr.cnes.sitools.metacatalogue.utils.HarvesterSettings;
import fr.cnes.sitools.server.Consts;
import fr.cnes.sitools.server.Starter;

/**
 * Classe de base pour les tests unitaires des Serveurs/Applications Restlet et de la persistance des données
 * 
 * On suppose qu'un serveur est lancé au démarrage de la suite de tests avec une configuration donnée.
 * 
 * @author AKKA Technologies
 * @see org.restlet.test.RestletTestCase
 */
public abstract class AbstractHarvesterServerTestCase {

  /**
   * Class logger
   */
  public static final Logger LOGGER = Logger.getLogger(AbstractHarvesterServerTestCase.class.getName());

  /**
   * BASE URL of global Sitools application
   */
  public static final String HARVESTERS_URL = HarvesterSettings.getInstance().getString(Consts.HARVESTERS_APP_URL);

  /**
   * Root path for storing files
   */
  public static final String TEST_FILES_REPOSITORY = HarvesterSettings.getInstance().getString("Tests.STORE_DIR");

  /**
   * Root path for storing files
   */
  public static final String TEST_FILES_REFERENCE_REPOSITORY = HarvesterSettings.getInstance().getString(
      "Tests.REFERENCE_STORE_DIR");

  /**
   * Default test port for all tests
   */
  public static final int DEFAULT_TEST_PORT = 1340;

  /**
   * Settings for the test
   */
  protected static HarvesterSettings settings = null;

  /**
   * MEDIA type to be set in concrete subclasses of test case
   */
  protected static MediaType mediaTest = MediaType.APPLICATION_XML;

  /**
   * system property name for test port
   */
  protected static final String PROPERTY_TEST_PORT = "sitools.test.port";

  /**
   * server port for the each test instance.
   */
  protected final int portTest = getTestPort();

  /**
   * Port for test defined in this order : 1. System property sitools.test.port 2. default test port (1340)
   * 
   * @return test port
   */
  protected static int getTestPort() {
    if (System.getProperties().containsKey(PROPERTY_TEST_PORT)) {
      return Integer.parseInt(System.getProperty(PROPERTY_TEST_PORT));
    }
    return DEFAULT_TEST_PORT;
  }

  /**
   * Try to remove files from directory
   * 
   * @param dir
   *          directory to be cleaned
   */
  public static void cleanDirectory(File dir) {
    if (dir == null) {
      LOGGER.warning("Null directory");
      return;
    }

    LOGGER.info("Clean XML files in directory " + dir.getAbsolutePath());
    try {
      FileUtils.cleanDirectory(dir, new String[] { "xml" }, false);
    }
    catch (IOException e) {
      Logger.getLogger(AbstractHarvesterServerTestCase.class.getName()).warning(
          "Unable to clean " + dir.getPath() + "\n cause:" + e.getMessage());
    }
  }

  /**
   * Supprime tous les fichiers du repertoire.
   * 
   * @param dir
   *          File directory to clean up
   */
  public static void cleanDirectoryAll(File dir) {
    if (dir == null) {
      LOGGER.warning("Null directory");
      return;
    }

    LOGGER.info("Clean directory " + dir.getAbsolutePath());
    try {
      FileUtils.cleanDirectory(dir, new String[] {}, false);
    }
    catch (IOException e) {
      Logger.getLogger(AbstractHarvesterServerTestCase.class.getName()).warning(
          "Unable to clean " + dir.getPath() + "\n cause:" + e.getMessage());
    }
  }

  /**
   * Copie les fichiers du repertoire source vers le repertoire cible
   * 
   * @param source
   *          String directory path
   * @param cible
   *          String directory path
   */
  public static void setUpDataDirectory(String source, String cible) {
    cleanDirectoryAll(new File(cible));
    LOGGER.info("Copy files from:" + source + " cible:" + cible);
    File cibleFile = new File(cible);
    if (!cibleFile.exists()) {
      cibleFile.mkdirs();
    }
    FileCopyUtils.copyAFolderExclude(source, cible, ".svn");
  }

  /**
   * Absolute path location for data files
   * 
   * @return path
   */
  protected String getTestRepository() {
    return TEST_FILES_REPOSITORY;
  }

  /**
   * absolute url for sitools REST API
   * 
   * @return url
   */
  protected String getBaseUrl() {
    return "http://localhost:" + getTestPort() + HARVESTERS_URL;
  }

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
   * Executed once before all test methods
   */
  @BeforeClass
  public static void before() {
    setup();
    start();
  }

  /** Setup tests variables before starting server */
  protected static void setup() {
    Engine.clearThreadLocalVariables();

    settings = HarvesterSettings.getInstance();

    String source = settings.getRootDirectory() + TEST_FILES_REFERENCE_REPOSITORY;
    String cible = settings.getRootDirectory() + TEST_FILES_REPOSITORY;

    LOGGER.info("COPY SOURCE:" + source + " CIBLE:" + cible);
    
    setUpDataDirectory(source, cible);
    settings.setStoreDIR(TEST_FILES_REPOSITORY);

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
   * Gets the mediaTest value
   * 
   * @return the mediaTest
   */
  public static MediaType getMediaTest() {
    return mediaTest;
  }

  /**
   * Sets the value of mediaTest
   * 
   * @param mediaTest
   *          the mediaTest to set
   */
  public static void setMediaTest(MediaType mediaTest) {
    AbstractHarvesterServerTestCase.mediaTest = mediaTest;
  }

}
