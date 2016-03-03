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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatInfo;
import info.bioinfweb.jphyloio.formats.fasta.FASTAFactory;
import info.bioinfweb.jphyloio.formats.mega.MEGAFactory;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLFactory;
import info.bioinfweb.jphyloio.formats.nexus.NexusFactory;
import info.bioinfweb.jphyloio.formats.pde.PDEFactory;
import info.bioinfweb.jphyloio.formats.phylip.PhylipFactory;
import info.bioinfweb.jphyloio.formats.phylip.SequentialPhylipFactory;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLFactory;
import info.bioinfweb.jphyloio.formats.xtg.XTGFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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
	
	
	private void addFactory(SingleReaderWriterFactory factory) {
		formatMap.put(factory.getFormatInfo().getFormatID(), factory);
	}
	
	
	private void fillMap() {
		addFactory(new NeXMLFactory());
		addFactory(new NexusFactory());
		addFactory(new PhyloXMLFactory());
		addFactory(new XTGFactory());
		addFactory(new PDEFactory());
		addFactory(new FASTAFactory());
		addFactory(new MEGAFactory());
		addFactory(new PhylipFactory());
		addFactory(new SequentialPhylipFactory());
		//TODO Add Newick factory
	}
	
	
	/**
	 * Tries to determine the format of the contents of the specified file by examining at its beginning (e.g. the root 
	 * tag in XML formats). The format is determined by subsequent calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(Reader, ReadWriteParameterMap)} until a matching factory is found.
	 * <p>
	 * The parameter map is only necessary for formats that are so variable that parameter values are needed to determine 
	 * how a valid input would look like. That is currently not the case for any format supported in <i>JPhyloIO</i>, but
	 * may be necessary for third parts format-specific factories used in this instance. (Refer to the documentation of 
	 * third party format-specific factories for details.) In most cases the convenience method {@link #guessFormat(File)}
	 * will be sufficient.
	 * 
	 * @param reader the reader providing the contents
	 * @param parameters the parameter map containing parameters for the <i>JPhyloIO</i> event reader that would 
	 *        be used with {@code reader} 
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 */
	public String guessFormat(File file, ReadWriteParameterMap parameters) throws Exception {
		String result;
		FileReader reader = new FileReader(file);
		try {
			result = guessFormat(reader, parameters);
		}
		finally {
			reader.close();
		}
		return result;
	}
	
	
	/**
	 * Tries to determine the format of the contents of the specified file by examining at its beginning (e.g. the root 
	 * tag in XML formats). The format is determined by subsequent calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(Reader, ReadWriteParameterMap)} until a matching factory is found.
	 * <p>
	 * It uses an empty parameter map that is passed to the internal calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(Reader, ReadWriteParameterMap)}. Use 
	 * {@link #guessFormat(File, ReadWriteParameterMap)} if parameters are necessary to determine the format correctly.
	 * 
	 * @param reader the reader providing the contents
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 */
	public String guessFormat(File file) throws Exception {
		return guessFormat(file, new ReadWriteParameterMap());
	}
	
	
	/**
	 * Tries to determine the format of the contents of the specified reader by examining at its beginning (e.g. the root 
	 * tag in XML formats). The format is determined by subsequent calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(Reader, ReadWriteParameterMap)} until a matching factory is found.
	 * <p>
	 * The parameter map is only necessary for formats that are so variable that parameter values are needed to determine 
	 * how a valid input would look like. That is currently not the case for any format supported in <i>JPhyloIO</i>, but
	 * may be necessary for third parts format-specific factories used in this instance. (Refer to the documentation of 
	 * third party format-specific factories for details.) In most cases the convenience method {@link #guessFormat(Reader)}
	 * will be sufficient.
	 * 
	 * @param reader the reader providing the contents
	 * @param parameters the parameter map containing parameters for the <i>JPhyloIO</i> event reader that would 
	 *        be used with {@code reader} 
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 */
	public String guessFormat(Reader reader, ReadWriteParameterMap parameters) throws Exception {
		for (SingleReaderWriterFactory factory : formatMap.values()) {
			if (factory.checkFormat(reader, parameters)) {
				return factory.getFormatInfo().getFormatID();
			}
		}
		return null;
	}
	
	
	/**
	 * Tries to determine the format of the contents of the specified reader by examining at its beginning (e.g. the root 
	 * tag in XML formats). The format is determined by subsequent calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(Reader, ReadWriteParameterMap)} until a matching factory is found.
	 * <p>
	 * It uses an empty parameter map that is passed to the internal calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(Reader, ReadWriteParameterMap)}. Use 
	 * {@link #guessFormat(Reader, ReadWriteParameterMap)} if parameters are necessary to determine the format correctly.
	 * 
	 * @param reader the reader providing the contents
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 */
	public String guessFormat(Reader reader) throws Exception {
		return guessFormat(reader, new ReadWriteParameterMap());
	}
	
	
	/**
	 * Tries to determine the format of the contents of the specified input stream by examining at its beginning (e.g. 
	 * the root tag in XML formats). The format is determined by subsequent calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(InputStream, ReadWriteParameterMap)} until a matching factory is found.
	 * <p>
	 * The parameter map is only necessary for formats that are so variable that parameter values are needed to determine 
	 * how a valid input would look like. That is currently not the case for any format supported in <i>JPhyloIO</i>, but
	 * may be necessary for third parts format-specific factories used in this instance. (Refer to the documentation of 
	 * third party format-specific factories for details.) In most cases the convenience method 
	 * {@link #guessFormat(InputStream)} will be sufficient.
	 * 
	 * @param reader the reader providing the contents
	 * @param parameters the parameter map containing parameters for the <i>JPhyloIO</i> event reader that would 
	 *        be used with {@code reader} 
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 */
	public String guessFormat(InputStream stream, ReadWriteParameterMap parameters) throws Exception {
		for (SingleReaderWriterFactory factory : formatMap.values()) {
			if (factory.checkFormat(stream, parameters)) {
				return factory.getFormatInfo().getFormatID();
			}
		}
		return null;
	}

	
	/**
	 * Tries to determine the format of the contents of the specified reader by examining at its beginning (e.g. the root 
	 * tag in XML formats). The format is determined by subsequent calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(InputStream, ReadWriteParameterMap)} until a matching factory is found.
	 * <p>
	 * It uses an empty parameter map that is passed to the internal calls of 
	 * {@link SingleReaderWriterFactory#checkFormat(InputStream, ReadWriteParameterMap)}. Use 
	 * {@link #guessFormat(InputStream, ReadWriteParameterMap)} if parameters are necessary to determine the format 
	 * correctly.
	 * 
	 * @param reader the reader providing the contents
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 */
	public String guessFormat(InputStream stream) throws Exception {
		return guessFormat(stream, new ReadWriteParameterMap());
	}
	
	
	/**
	 * Returns an information object for the specified format.
	 * 
	 * @param formatID the unique format ID specifying the format
	 * @return the information object
	 */
	public JPhyloIOFormatInfo getFormatInfo(String formatID) {
		SingleReaderWriterFactory factory = formatMap.get(formatID);
		if (factory == null) {
			return null;
		}
		else {
			return factory.getFormatInfo();
		}
	}
	
	
	public JPhyloIOEventReader getReader(String formatID, InputStream stream, ReadWriteParameterMap parameters) throws Exception {
		SingleReaderWriterFactory factory = formatMap.get(formatID);
		if (factory == null) {
			return null;
		}
		else {
			return factory.getReader(stream, parameters);
		}
	}
	
	
	public JPhyloIOEventReader getReader(String formatID, File file, ReadWriteParameterMap parameters) throws Exception {
		return getReader(formatID, new FileReader(file), parameters);
	}
	
	
	public JPhyloIOEventReader getReader(String formatID, Reader reader, ReadWriteParameterMap parameters) throws Exception {
		SingleReaderWriterFactory factory = formatMap.get(formatID);
		if (factory == null) {
			return null;
		}
		else {
			return factory.getReader(reader, parameters);
		}
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
