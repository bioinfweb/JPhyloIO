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


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import info.bioinfweb.commons.io.ExtensionFileFilter;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.DefaultFormatInfo;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;
import info.bioinfweb.jphyloio.formats.SingleReaderWriterFactory;



/**
 * Reader and writer factory for the MEGA format.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class MEGAFactory implements SingleReaderWriterFactory, JPhyloIOFormatIDs, MEGAConstants {
	@Override
	public boolean checkFormat(InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		return checkFormat(new InputStreamReader(stream), parameters);
	}


	@Override
	public boolean checkFormat(Reader reader, ReadWriteParameterMap parameters) throws IOException {
		for (int i = 0; i < FIRST_LINE.length(); i++) {
			int c = reader.read();
			if ((c == -1) || (Character.toUpperCase((char)c) != FIRST_LINE.charAt(i))) {
				return false;
			}
		}
		return true;
	}


	@Override
	public JPhyloIOEventReader getReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		return new MEGAEventReader(stream, parameters);
	}


	@Override
	public JPhyloIOEventReader getReader(Reader reader, ReadWriteParameterMap parameters) throws IOException {
		return new MEGAEventReader(reader, parameters);
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
