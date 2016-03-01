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
package info.bioinfweb.jphyloio.formats.mega;


import info.bioinfweb.commons.io.ExtensionFileFilter;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.formats.DefaultFormatInfo;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;
import info.bioinfweb.jphyloio.formats.SingleReaderWriterFactory;



public class MEGAFactory implements SingleReaderWriterFactory, JPhyloIOFormatIDs, MEGAConstants {
	@Override
	public JPhyloIOEventReader getReader() {
		// TODO Implement when parameter problem is solved.
		return null;
	}
	

	@Override
	public JPhyloIOEventWriter getWriter() {
		return null;
	}

	
	@Override
	public boolean hasReader() {
		return true;
	}

	
	@Override
	public boolean hasWriter() {
		return false;
	}


	/**
	 * Returns the FASTA format info including a file filter that specifies the file extensions listed
	 * <a href="https://en.wikipedia.org/wiki/FASTA_format#File_extension">here</a>.
	 * 
	 * @see info.bioinfweb.jphyloio.formats.SingleReaderWriterFactory#getFormatInfo()
	 */
	@Override
	public JPhyloIOFormatInfo getFormatInfo() {
		return new DefaultFormatInfo(FASTA_FORMAT_ID, MEGA_FORMAT_NAME, new ExtensionFileFilter(
				"MEGA format", "meg", true, "mega"));
	}
}
