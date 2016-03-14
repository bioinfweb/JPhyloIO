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
package info.bioinfweb.jphyloio.factory;


import info.bioinfweb.commons.io.ContentExtensionFileFilter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;

import java.io.InputStream;
import java.io.InputStreamReader;



public abstract class AbstractSingleReaderWriterFactory implements SingleReaderWriterFactory {
	@Override
	public boolean checkFormat(InputStream stream, ReadWriteParameterMap parameters) throws Exception {
		return checkFormat(new InputStreamReader(stream), parameters);
	}
	
	
	protected JPhyloIOContentExtensionFileFilter createFileFilter(ReadWriteParameterMap defaultParamaters, 
			String description,	String defaultExtension, String... extensions) {
		
		return new JPhyloIOContentExtensionFileFilter(this, defaultParamaters, description, defaultExtension, true, 
				ContentExtensionFileFilter.TestStrategy.CONTENT, false, extensions);
	}
}
