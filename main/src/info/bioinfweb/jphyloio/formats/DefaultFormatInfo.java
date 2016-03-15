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
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.factory.JPhyloIOContentExtensionFileFilter;
import info.bioinfweb.jphyloio.factory.SingleReaderWriterFactory;



/**
 * Default implementation of {@link JPhyloIOFormatInfo}.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class DefaultFormatInfo implements JPhyloIOFormatInfo {
	private SingleReaderWriterFactory factory;
	private String formatID;
	private String formatName;
	private ReadWriteParameterMap filterParamaters;
	private String filterDescription;
	private String filterDefaultExtension;
	private String[] filterExtensions;
	

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param formatID the ID of the format this information object is about
	 * @param formatName the name of the format this information object is about
	 * @param fileFilter the filter for the format this information object is about
	 * @throws NullPointerException if any of the arguments is {@code null}
	 */
	public DefaultFormatInfo(SingleReaderWriterFactory factory, String formatID, String formatName, 
			ReadWriteParameterMap filterParamaters,	String filterDescription,	String filterDefaultExtension, String... filterExtensions) {
		
		super();
		if (factory == null) {
			throw new NullPointerException("factory must not be null.");
		}
		else if (formatID == null) {
			throw new NullPointerException("formatID must not be null.");
		}
		else if (formatName == null) {
			throw new NullPointerException("formatName must not be null.");
		}
		else if (filterDescription == null) {
			throw new NullPointerException("filterDescription must not be null.");
		}
		else if (filterDefaultExtension == null) {
			throw new NullPointerException("filterDefaultExtension must not be null.");
		}
		else {
			for (int i = 0; i < filterExtensions.length; i++) {
				if (filterExtensions[i] == null) {
					throw new NullPointerException("The filter extension with the index " + i + " is null.");
				}
			}
			
			this.formatID = formatID;
			this.formatName = formatName;
			if (filterParamaters == null) {
				this.filterParamaters = new ReadWriteParameterMap();
			}
			else {
				this.filterParamaters = filterParamaters;
			}
			this.filterDescription = filterDescription;
			this.filterDefaultExtension = filterDefaultExtension;
			this.filterExtensions = filterExtensions;
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
	public ContentExtensionFileFilter createFileFilter() {
		return new JPhyloIOContentExtensionFileFilter(factory, filterParamaters, filterDescription, filterDefaultExtension, true, 
				ContentExtensionFileFilter.TestStrategy.CONTENT, false, filterExtensions);
	}
}
