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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import com.izforge.izpack.event.SimpleInstallerListener;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.InstallerException;
import com.izforge.izpack.installer.ScriptParser;
import com.izforge.izpack.util.AbstractUIProgressHandler;

import fr.cnes.sitools.metacatalogue.izpack.model.JDBCConnectionModel;

public class InstallDatabaseAction extends SimpleInstallerListener {

	/**
	 * the script sql postgres
	 */
	private File postgreSQLFile;

	/**
	 * The model of the connection model
	 */
	private JDBCConnectionModel jdbcModel;

	/**
	 * Metacatalog installation path
	 */
	private String INSTALL_PATH;

	public InstallDatabaseAction() throws Exception {
		super();
	}
	
	@Override
	public void afterPacks(AutomatedInstallData arg0, AbstractUIProgressHandler arg1) throws Exception {
		super.afterPacks(arg0, arg1);

		boolean installDBSelected = Boolean.parseBoolean(getInstalldata().getVariable("dbInstallSelected"));
		
		
		if (!installDBSelected){
			return;
		}
		
		prepareInstallation();
		installUserDatabase();
		installDatabase(jdbcModel, postgreSQLFile);
	}
	
	/**
	 * Map the ProcessingClient parameters to the class properties.
	 * 
	 * @param aid
	 *            the data
	 */
	private void prepareInstallation() {
		
		INSTALL_PATH = getInstalldata().getVariable(ScriptParser.INSTALL_PATH);
		jdbcModel = new JDBCConnectionModel(getInstalldata());

		postgreSQLFile = new File(INSTALL_PATH
				+ "/database/create-postgis-spatialindex.sql");
	}

	/**
	 * Install the user databases
	 * 
	 * @param idata
	 *            the data
	 * @throws Exception
	 *             if something is wrong
	 */
	public void installUserDatabase() throws Exception {
		installDatabase(jdbcModel, postgreSQLFile);
	}

	/**
	 * Install database
	 * 
	 * @param jdbcModel
	 *            the JDBC model
	 * @param fileList
	 *            the file list
	 * @throws Exception
	 *             when occurs
	 */
	private void installDatabase(JDBCConnectionModel jdbcModel, File file)
			throws Exception {
		Connection cnx = null;
		Statement stat = null;
		PrintStream out = System.out;
		try {
			do {
				out.println("Test jdbc data source connection ...");

				Class.forName(jdbcModel.getDbDriverClassName());
				out.println("Load driver class : OK");

				out.println("Get connection ");

				cnx = DriverManager.getConnection(jdbcModel.getDbUrl(),
						jdbcModel.getDbUser(), jdbcModel.getDbPassword());

				String ligne;
				String request;

				String fileName = postgreSQLFile.getPath();

				out.println("File :  " + fileName);

				InputStream ips = new FileInputStream(fileName);
				InputStreamReader ipsr = new InputStreamReader(ips);
				BufferedReader br = new BufferedReader(ipsr);
				request = "";

				StringBuilder stringBuilder = new StringBuilder();
				String ls = System.getProperty("line.separator");
				while ((ligne = br.readLine()) != null) {
					stringBuilder.append(ligne);
					stringBuilder.append(ls);
				}
				request = stringBuilder.toString();
				br.close();

				out.flush();
				stringBuilder = null;

				try {

					// stat = cnx.prepareStatement(request);
					cnx.setAutoCommit(false);
					stat = cnx.createStatement();
					stat.execute(request);
					// stat.execute();
					cnx.commit();
					stat.close();

				} catch (Exception e) {
					throw new InstallerException(
							"Warning there was an error while installing the databases :\n "
									+ e.getLocalizedMessage(), e);
				}
				out.println("Execute statement on connection : OK");

			} while (false);
		} catch (Exception e) {
			throw e;
		} finally {
			if (stat != null) {
				try {
					stat.close();
				} catch (SQLException e) {
					throw e;
				}
			}
			if (cnx != null) {
				try {
					cnx.close();
				} catch (SQLException e) {
					throw e;
				}
			}
		}
	}
}
