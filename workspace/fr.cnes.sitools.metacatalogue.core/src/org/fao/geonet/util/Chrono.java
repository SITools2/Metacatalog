//=============================================================================
//===   Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===   United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===   and United Nations Environment Programme (UNEP)
//===
//===   This program is free software; you can redistribute it and/or modify
//===   it under the terms of the GNU General Public License as published by
//===   the Free Software Foundation; either version 2 of the License, or (at
//===   your option) any later version.
//===
//===   This program is distributed in the hope that it will be useful, but
//===   WITHOUT ANY WARRANTY; without even the implied warranty of
//===   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===   General Public License for more details.
//===
//===   You should have received a copy of the GNU General Public License
//===   along with this program; if not, write to the Free Software
//===   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===   Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===   Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

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
package org.fao.geonet.util;

//==============================================================================

public class Chrono
{
	//-----------------------------------------------------------------------------
	//---
	//--- Constructor
	//---
	//-----------------------------------------------------------------------------

	public Chrono()
	{
		start = System.currentTimeMillis();
	}

	//-----------------------------------------------------------------------------
	//---
	//--- API methods
	//---
	//-----------------------------------------------------------------------------

	public int getMillis()
	{
		long end = System.currentTimeMillis();

		return (int)(end - start);
	}

	//-----------------------------------------------------------------------------

	public float getSeconds()
	{
		return (getMillis() / 1000f);
	}

	//-----------------------------------------------------------------------------

	public void dump(String message)
	{
		System.out.println(message +" ("+ getSeconds() +" secs)");
	}

	//-----------------------------------------------------------------------------
	//---
	//--- Variables
	//---
	//-----------------------------------------------------------------------------

	private long start;
}

//==============================================================================

