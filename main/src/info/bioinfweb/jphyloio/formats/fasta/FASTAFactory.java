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
package info.bioinfweb.jphyloio.formats.fasta;


import java.io.InputStream;
import java.io.Reader;

import info.bioinfweb.commons.io.ExtensionFileFilter;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.DefaultFormatInfo;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;
import info.bioinfweb.jphyloio.formats.SingleReaderWriterFactory;



public class FASTAFactory implements SingleReaderWriterFactory, JPhyloIOFormatIDs, FASTAConstants {
	@Override
	public JPhyloIOEventReader getReader(InputStream stream, ReadWriteParameterMap parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public JPhyloIOEventReader getReader(Reader reader, ReadWriteParameterMap parameters) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public JPhyloIOEventWriter getWriter() {
		return new FASTAEventWriter();
	}

	
	@Override
	public boolean hasReader() {
		return true;
	}

	
	@Override
	public boolean hasWriter() {
		return true;
	}


	/**
	 * Returns the FASTA format info including a file filter that specifies the file extensions listed
	 * <a href="https://en.wikipedia.org/wiki/FASTA_format#File_extension">here</a>.
	 * 
	 * @see info.bioinfweb.jphyloio.formats.SingleReaderWriterFactory#getFormatInfo()
	 */
	@Override
	public JPhyloIOFormatInfo getFormatInfo() {
		return new DefaultFormatInfo(FASTA_FORMAT_ID, FASTA_FORMAT_NAME, new ExtensionFileFilter(
				"FASTA format", "fasta", true, "fas", "fa", "fas", "fna", "ffn", "faa", "frn"));
	}
}
