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
package fr.cnes.sitools.metacatalogue.izpack.actions;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import com.izforge.izpack.Pack;
import com.izforge.izpack.event.SimpleInstallerListener;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * Create a Jar with the config Solr File and copy it in core_libs solr
 * directory
 * 
 * @author b.fiorito
 * 
 */
public class ConfigSolrJarAction extends SimpleInstallerListener {

	/** PATH OF THE PROPERTIES FILE TO INCLUDE IN JAR */
	private File configSolrFile;
	
	/** THE LOG FILE */
	private File log;

	/** PATH OF THE METACATALOGUE INSTALLATION **/
	public String INSTALL_PATH = "";

	public ConfigSolrJarAction() {
		super();
	}

	/**
	 * Create a jar
	 * 
	 * @param file
	 * @throws IOException
	 */
	public void createConfigSolrJar() throws IOException {
		System.out.println("Creating jar file....");
		JarOutputStream target = new JarOutputStream(new FileOutputStream(
				INSTALL_PATH + "/Solr/solr/core_libs/configSolr.jar"));
		addFileToJar(this.configSolrFile, target);
		target.close();
		System.out.println("JAR successfully created....");
	}

	/**
	 * add a source file to a target jar
	 * 
	 * @param source
	 * @param target
	 * @throws IOException
	 */
	private void addFileToJar(File source, JarOutputStream target)
			throws IOException {
		BufferedInputStream in = null;
		try {
			System.out.println("Trying to add : " + source.getName() + " file to archive.");

			JarEntry entry = new JarEntry(source.getName());
			entry.setTime(source.lastModified());
			target.putNextEntry(entry);
			in = new BufferedInputStream(new FileInputStream(source));

			byte[] buffer = new byte[1024];
			while (true) {
				int count = in.read(buffer);
				if (count == -1)
					break;
				target.write(buffer, 0, count);
			}
			target.closeEntry();
		} finally {
			if (in != null) {
				in.close();
				System.out.println("File succesfully added to archive.");
			}
		}
	}

	/**
	 * Delete the properties file after creating the jar
	 */
	private boolean deletePropertiesFile() {
		if (configSolrFile.exists()) {
			return configSolrFile.delete();
		}
		return false;
	}
	
	private void setOutputLogFile(){
		try {
            PrintStream printStream = new PrintStream(log);
            System.setOut(printStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}

	@Override
	public void afterPacks(AutomatedInstallData arg0, AbstractUIProgressHandler arg1) throws Exception {
		super.afterPacks(arg0, arg1);

		boolean findSolr = false;
		for (Pack p : arg0.selectedPacks) {
			if (p.id.equalsIgnoreCase("Solr")) {
				findSolr = true;
			}
		}
		if (!findSolr){
			return;
		}
		
		INSTALL_PATH = getInstalldata().getVariable(ScriptParser.INSTALL_PATH);

		log = new File(INSTALL_PATH + "/Solr/logCreateJar.txt");
		log.createNewFile();
		this.setOutputLogFile();

		configSolrFile = new File(INSTALL_PATH
				+ "/Solr/metacatalogue-solr.properties");

		this.createConfigSolrJar();
		boolean delete = this.deletePropertiesFile();
		System.out.println("FILE : " + configSolrFile.getName() + " correctly deleted : " + String.valueOf(delete).toUpperCase());
	}

	@Override
	public boolean isFileListener() {
		return true;
	}
}
