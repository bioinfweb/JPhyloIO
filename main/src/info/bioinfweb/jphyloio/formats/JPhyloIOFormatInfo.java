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


import info.bioinfweb.commons.io.ContentExtensionFileFilter;



/**
 * Classes implementing this interface provide information about a phylogenetic file format that is supported by <i>JPhyloIO</i>.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public interface JPhyloIOFormatInfo {
	/**
	 * Returns the unique ID that identifies this format. IDs of formats supported by <i>JPhyloIO</i> are enumerated by 
	 * {@link JPhyloIOFormatIDs}.
	 * <p>
	 * Third party developers that create readers or writers for additional formats must make sure to use a globally unique
	 * format ID. It is strongly recommended to use owned reverse domain names for this (e.g. 
	 * <code>org.example.additionalformat</code>).
	 * 
	 * @return the ID of the modeled format
	 */
	public String getFormatID();
	
	/**
	 * Returns the human readable name of the modeled format. 
	 * 
	 * @return the name of the format
	 */
	public String getFormatName();
	
	/**
	 * Creates a new file filter instance that is associated with the modeled format. It will check valid file extensions and 
	 * possibly the contents of the file, if the extension is not unique.
	 * 
	 * @return a new file filter instance
	 */
	public ContentExtensionFileFilter createFileFilter();
}
