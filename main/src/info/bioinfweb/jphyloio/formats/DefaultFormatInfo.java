/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.formats;


import info.bioinfweb.commons.io.ExtensionFileFilter;



/**
 * Default implementation of {@link JPhyloIOFormatInfo}.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class DefaultFormatInfo implements JPhyloIOFormatInfo {
	private String formatID;
	private String formatName;
	private ExtensionFileFilter fileFilter;
	

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param formatID the ID of the format this information object is about
	 * @param formatName the name of the format this information object is about
	 * @param fileFilter the filter for the format this information object is about
	 * @throws NullPointerException if any of the arguments is {@code null}
	 */
	public DefaultFormatInfo(String formatID, String formatName,	ExtensionFileFilter fileFilter) {
		super();
		if (formatID == null) {
			throw new NullPointerException("formatID must not be null.");
		}
		else if (formatName == null) {
			throw new NullPointerException("formatName must not be null.");
		}
		else if (fileFilter == null) {
			throw new NullPointerException("fileFilter must not be null.");
		}
		else {
			this.formatID = formatID;
			this.formatName = formatName;
			this.fileFilter = fileFilter;
		}
	}


	@Override
	public String getFormatID() {
		return formatID;
	}

	
	@Override
	public String getFormatName() {
		return formatName;
	}

	
	@Override
	public ExtensionFileFilter getFileFilter() {
		return fileFilter;
	}
}
