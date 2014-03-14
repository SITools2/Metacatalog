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

import java.io.File;
import java.io.FilenameFilter;

import com.izforge.izpack.Pack;
import com.izforge.izpack.event.SimpleInstallerListener;
import com.izforge.izpack.installer.DataValidator.Status;
import com.izforge.izpack.util.AbstractUIProgressHandler;

/**
 * Check if the name of metacatalogue project module already exist in sitools
 * projects modules. If it's the case, the metacatalogue module name is changed
 * 
 * @author b.fiorito
 * 
 */
public class CheckMetacatalogueModuleAction extends SimpleInstallerListener {

	/** PATH OF SITOOLS2 PROJECTS MODULES **/
	private String sitoolsProjectModulesPath;

	private String moduleName;

	public CheckMetacatalogueModuleAction() {
		super();
	}

	@Override
	public void beforePack(Pack pack, Integer i,
			AbstractUIProgressHandler handler) throws Exception {

		boolean findClient = false;
		for (Pack p : getInstalldata().selectedPacks) {
			if (p.id.equalsIgnoreCase("Client")) {
				findClient = true;
			}
		}

		if (!findClient) {
			return;
		}

		if (!"client-metacatalogue".equals(pack.name)) {
			return;
		}

		moduleName = getInstalldata().getVariable("metacatalogModuleName");
		sitoolsProjectModulesPath = getInstalldata().getVariable("sitoolsPath");
		if (sitoolsProjectModulesPath == "") {
			return;
		}

		sitoolsProjectModulesPath += "/data/projects_modules";

		File moduleDir = new File(sitoolsProjectModulesPath);

		if (moduleDir.exists() && moduleDir.isDirectory()) {
			int nbModules = moduleDir.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File file, String name) {
					return name.startsWith("int@");
				}
			}).length;
			String lastModuleName = "int@" + nbModules + ".xml";

			getInstalldata().setVariable("metacatalogModuleName", lastModuleName);
		}

	}

	@Override
	public boolean isFileListener() {
		return true;
	}

}
