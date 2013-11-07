//==============================================================================
//===
//===   CopyFiles
//===
//==============================================================================
//===	Lifted from the net  - 
//=== http://forum.java.sun.com/thread.jspa?threadID=328293&messageID=1334818
//==============================================================================

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
package org.fao.geonet.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
 
public class FileCopyMgr {

		private static void copy(File source, File target) throws IOException { 
      FileChannel sourceChannel = new FileInputStream(source).getChannel();
      FileChannel targetChannel = new FileOutputStream(target).getChannel();
      sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
      sourceChannel.close();
      targetChannel.close();
		}
 
		public static void copyFiles(String strPath, String dstPath) 
														throws IOException {

			File src = new File(strPath);
			File dest = new File(dstPath);
			copyFiles(src, dest);
		}

		public static void removeDirectoryOrFile(File dir) throws IOException {
			if (dir.isDirectory()) {
				File list[] = dir.listFiles();
				for (int i = 0; i < list.length; i++) {
					list[i].delete(); 
				}
			}
			dir.delete();
		}

		public static void copyFiles(File src, File dest) 
														throws IOException {
			if (src.isDirectory()) {
				if(dest.exists()!=true)
					dest.mkdirs();
				String list[] = src.list();

				for (int i = 0; i < list.length; i++) {
					String dest1 = dest.getAbsolutePath() + File.separator + list[i];
					String src1 = src.getAbsolutePath() + File.separator + list[i];
					copyFiles(src1 , dest1);
				}
			} else {
				boolean ready = dest.createNewFile(); 
				copy(src,dest); 
			}
		}
}
//==============================================================================
