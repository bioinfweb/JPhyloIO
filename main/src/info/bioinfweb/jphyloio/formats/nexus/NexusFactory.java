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
package info.bioinfweb.jphyloio.formats.nexus;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import info.bioinfweb.commons.io.ExtensionFileFilter;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.AbstractStartStringSingleFactory;
import info.bioinfweb.jphyloio.formats.DefaultFormatInfo;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;



/**
 * Reader and writer factory for the Nexus format.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class NexusFactory extends AbstractStartStringSingleFactory implements NexusConstants, JPhyloIOFormatIDs {
	public NexusFactory() {
		super(FIRST_LINE);
	}


	@Override
	public JPhyloIOEventReader getReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		return new NexusEventReader(stream, parameters);
	}

	
	@Override
	public JPhyloIOEventReader getReader(Reader reader,	ReadWriteParameterMap parameters) throws IOException {
		return new NexusEventReader(reader, parameters);
	}
	

	@Override
	public JPhyloIOEventWriter getWriter() {
		return new NexusEventWriter();
	}

	
	@Override
	public boolean hasReader() {
		return true;
	}
	

	@Override
	public boolean hasWriter() {
		return true;
	}

	
	@Override
	public JPhyloIOFormatInfo getFormatInfo() {
		return new DefaultFormatInfo(NEXUS_FORMAT_ID, NEXUS_FORMAT_NAME, new ExtensionFileFilter(
				"Nexus format", "nex", true, "nexus", "tre", "tree", "con"));  //TODO Should the tree extension better be removed?
				//TODO Should a file filter be provided that also opens files (uses checkFormat())?
	}
}
