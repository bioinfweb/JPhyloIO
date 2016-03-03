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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.fasta.FASTAFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;



/**
 * Factory to create instances of <i>JPhyloIO</i> event reader and writers as well as {@link JPhyloIOFormatInfo} 
 * instances.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class JPhyloIOReaderWriterFactory implements JPhyloIOFormatIDs {
	private static JPhyloIOReaderWriterFactory firstInstance = null;
	
	
	private Map<String, SingleReaderWriterFactory> formatMap = new TreeMap<String, SingleReaderWriterFactory>();
	
	
	private JPhyloIOReaderWriterFactory() {
		super();
		fillMap();
	}
	
	
	public static JPhyloIOReaderWriterFactory getInstance() {
		if (firstInstance == null) {
			firstInstance = new JPhyloIOReaderWriterFactory();
		}
		return firstInstance;
	}
	
	
	private void fillMap() {
		formatMap.put(FASTA_FORMAT_ID, new FASTAFactory());
	}
	
	
	public JPhyloIOFormatInfo getFormatInfo(String formatID) {
		SingleReaderWriterFactory factory = formatMap.get(formatID);
		if (factory == null) {
			return null;
		}
		else {
			return factory.getFormatInfo();
		}
	}
	
	
	public JPhyloIOEventReader getReader(String formatID, InputStream stream, ReadWriteParameterMap parameters) throws IOException {
		SingleReaderWriterFactory factory = formatMap.get(formatID);
		if (factory == null) {
			return null;
		}
		else {
			return factory.getReader(stream, parameters);
		}
	}
	
	
	public JPhyloIOEventReader getReader(String formatID, Reader reader, ReadWriteParameterMap parameters) throws IOException {
		SingleReaderWriterFactory factory = formatMap.get(formatID);
		if (factory == null) {
			return null;
		}
		else {
			return factory.getReader(reader, parameters);
		}
	}
	
	
	public JPhyloIOEventReader getReader(InputStream stream) throws IOException {
		//TODO Implement
		return null;
	}

	
	public JPhyloIOEventWriter getWriter(String formatID) {
		SingleReaderWriterFactory factory = formatMap.get(formatID);
		if (factory == null) {
			return null;
		}
		else {
			return factory.getWriter();
		}
	}
}
