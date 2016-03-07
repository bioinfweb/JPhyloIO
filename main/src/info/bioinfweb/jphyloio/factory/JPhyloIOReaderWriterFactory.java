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
import info.bioinfweb.jphyloio.formats.newick.NewickFactory;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLFactory;
import info.bioinfweb.jphyloio.formats.nexus.NexusFactory;
import info.bioinfweb.jphyloio.formats.pde.PDEFactory;
import info.bioinfweb.jphyloio.formats.phylip.PhylipFactory;
import info.bioinfweb.jphyloio.formats.phylip.SequentialPhylipFactory;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLFactory;
import info.bioinfweb.jphyloio.formats.xtg.XTGFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;



/**
 * Factory to create instances of <i>JPhyloIO</i> event reader and writers as well as {@link JPhyloIOFormatInfo} 
 * instances.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class JPhyloIOReaderWriterFactory implements JPhyloIOFormatIDs {
	//TODO Do any other methods need to be synchronized?
	
	public static final int DEFAULT_READ_AHAED_LIMIT = 4 * 1024;  // XML files may contain long comments before the root tag.
	
	
	private final ReadWriteLock readAheahLimitLock = new ReentrantReadWriteLock();	
	
	private Map<String, SingleReaderWriterFactory> formatMap = new TreeMap<String, SingleReaderWriterFactory>();
	private int readAheahLimit = DEFAULT_READ_AHAED_LIMIT;
	
	
	public JPhyloIOReaderWriterFactory() {
		super();
		fillMap();
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
		addFactory(new NewickFactory());  // Should be tested in guess*() methods in the end, since the test is insecure.
		addFactory(new SequentialPhylipFactory());  // Does not have to be tested by guess*() methods at all, since PhylipFactory would have returned true before.
	}
	
	
	/**
	 * Returns the maximal number of bytes this factory will read to determine the format of an input in the 
	 * {@code #guess*()} methods.
	 * <p>
	 * This method is thread save a secured with an {@link ReadWriteLock} together with {@link #setReadAheahLimit(int)}.
	 * 
	 * @return the current read ahead limit
	 */
	public int getReadAheahLimit() {
		readAheahLimitLock.readLock().lock();
		try {
			return readAheahLimit;
		}
		finally {
			readAheahLimitLock.readLock().unlock();
		}
	}


	/**
	 * Allows to specify the maximal number of bytes this factory will read to determine the format of an input
	 * in the {@code #guess*()} methods.
	 * <p>
	 * This method is thread save a secured with an {@link ReadWriteLock} together with {@link #getReadAheahLimit()}.
	 * 
	 * @param readAheahLimit the new read ahead limit
	 */
	public void setReadAheahLimit(int readAheahLimit) {
		readAheahLimitLock.writeLock().lock();
		try {
			this.readAheahLimit = readAheahLimit;
		}
		finally {
			readAheahLimitLock.writeLock().unlock();
		}
	}
	//TODO Isn't setting an integer an atomic operation and synchronizing is unnecessary?


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
	 * <p>
	 * Note that in contrast to {@link #guessReader(File, ReadWriteParameterMap)}, this method does not support
	 * GZIPed inputs.
	 * 
	 * @param reader the reader providing the contents
	 * @param parameters the parameter map containing parameters for the <i>JPhyloIO</i> event reader that would 
	 *        be used with {@code reader} 
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 * @see #guessReader(File, ReadWriteParameterMap)
	 */
	public String guessFormat(File file, ReadWriteParameterMap parameters) throws Exception {
		String result;
		FileInputStream stream = new FileInputStream(file);
		try {
			result = guessFormat(new FileReader(file), parameters);
		}
		finally {
			stream.close();
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
	 * <p>
	 * Note that in contrast to {@link #guessReader(File, ReadWriteParameterMap)}, this method does not support
	 * GZIPed inputs.
	 * 
	 * @param reader the reader providing the contents
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 * @see #guessReader(File, ReadWriteParameterMap)
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
	 * <p>
	 * Note that in contrast to {@link #guessReader(InputStream, ReadWriteParameterMap)}, this method does not support
	 * GZIPed inputs.
	 * 
	 * @param reader the reader providing the contents
	 * @param parameters the parameter map containing parameters for the <i>JPhyloIO</i> event reader that would 
	 *        be used with {@code reader} 
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 * @see #guessReader(InputStream, ReadWriteParameterMap)
	 */
	public String guessFormat(Reader reader, ReadWriteParameterMap parameters) throws Exception {
		BufferedReader bufferedReader = new BufferedReader(reader);
		bufferedReader.mark(getReadAheahLimit());
		for (SingleReaderWriterFactory factory : formatMap.values()) {
			boolean formatFound = factory.checkFormat(bufferedReader, parameters);
			bufferedReader.reset();
			if (formatFound) {
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
	 * <p>
	 * Note that in contrast to {@link #guessReader(InputStream, ReadWriteParameterMap)}, this method does not support
	 * GZIPed inputs.
	 * 
	 * @param reader the reader providing the contents
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 * @see #guessReader(InputStream, ReadWriteParameterMap)
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
	 * <p>
	 * Note that in contrast to {@link #guessReader(InputStream, ReadWriteParameterMap)}, this method does not support
	 * GZIPed inputs.
	 * 
	 * @param reader the reader providing the contents
	 * @param parameters the parameter map containing parameters for the <i>JPhyloIO</i> event reader that would 
	 *        be used with {@code reader} 
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 * @see #guessReader(InputStream, ReadWriteParameterMap)
	 */
	public String guessFormat(InputStream stream, ReadWriteParameterMap parameters) throws Exception {
		return guessFormatFromBufferedStream(new BufferedInputStream(stream), parameters);
	}
	
	
	private String guessFormatFromBufferedStream(BufferedInputStream stream, ReadWriteParameterMap parameters) throws Exception {
		stream.mark(getReadAheahLimit());
		for (SingleReaderWriterFactory factory : formatMap.values()) {
			boolean formatFound = factory.checkFormat(stream, parameters);
			stream.reset();
			if (formatFound) {
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
	 * <p>
	 * Note that in contrast to {@link #guessReader(InputStream, ReadWriteParameterMap)}, this method does not support
	 * GZIPed inputs.
	 * 
	 * @param reader the reader providing the contents
	 * @return the ID of the determined format or {@code null} if no supported format seems to be matching the contents
	 * @throws Exception if the underlying format specific factory throws an exception when testing the stream (e.g. 
	 *         because of an IO error)
	 * @see JPhyloIOFormatIDs
	 * @see #guessReader(InputStream, ReadWriteParameterMap)
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
	
	
	/**
	 * Tries to determine the format of the contents of the specified input stream as described in the documentation of
	 * {@link #guessFormat(InputStream)}, resets the input stream and than creates a reader for the according format
	 * from that stream. Additionally this method checks, if the input data is GZIPed and then returns a reader instance
	 * that unpacks the stream data while reading.
	 * <p>
	 * In other words, this method is able to return a functional reader for input streams in all formats supported by 
	 * this factory, as well as GZIPed streams providing data in any of these formats.
	 * <p>
	 * Note that bytes of {@code stream} will be consumed by this method to determine the format, even if no according 
	 * reader is found. If an event reader is returned, no events of this reader will have been consumed.
	 * <p>
	 * Note that there is no version of this method accepting a {@link Reader} instead of an {@link InputStream},
	 * because uncompressing data from a reader is not directly possible.
	 * 
	 * @param stream the stream to read the data from
	 * @param parameters the parameter map optionally containing parameters for the returned reader
	 * @return the new reader instance or {@code null} if no reader fitting the format of the stream could be found
	 * @throws Exception if an exception occurs while determining the format from the stream or creating the returned
	 *         reader instance (Depending on the type of reader that is returned, this will mostly be 
	 *         {@link IOException}s.)
	 * @see #guessFormat(InputStream)
	 * @see #getReader(String, InputStream, ReadWriteParameterMap)
	 */
	public JPhyloIOEventReader guessReader(InputStream stream, ReadWriteParameterMap parameters) throws Exception {
		// Buffer stream for testing:
		BufferedInputStream bufferedStream = new BufferedInputStream(stream);
		bufferedStream.mark(getReadAheahLimit());
		
	  // Try if the input is GZIPed:
		try {
			bufferedStream = new BufferedInputStream(new GZIPInputStream(bufferedStream));
		}
		catch (ZipException e) {
			bufferedStream.reset();  // Reset bytes that have been read by GZIPInputStream. (If this code is called, bufferedStream was not set in the try block.)
		}
		
		// Return reader:
		String format = guessFormatFromBufferedStream(bufferedStream, parameters);  // bufferedStream is already reset in the called method.
		if (format == null) {
			return null;
		}
		else {
			return getReader(format, bufferedStream, parameters);
		}
		//TODO Does the any of the created streams in here need to be closed, if the underlying stream is closed later in application code? (Usually the top-most stream would be closed, which is not known by the application.)
	}
	
	
	/**
	 * Tries to determine the format of the contents of the specified file as described in the documentation of
	 * {@link #guessFormat(File)} and than creates a reader for the according format from that stream. Additionally this 
	 * method checks, if the input data is GZIPed and then returns a reader instance that unpacks the stream data while 
	 * reading.
	 * <p>
	 * In other words, this method is able to return a functional reader for files in all formats supported by 
	 * this factory, as well as GZIPed files in any of these formats.
	 * <p>
	 * Note that there is no version of this method accepting a {@link Reader} instead of an {@link InputStream},
	 * because uncompressing data from a reader is not directly possible.
	 * 
	 * @param stream the stream to read the data from
	 * @param parameters the parameter map optionally containing parameters for the returned reader
	 * @return the new reader instance or {@code null} if no reader fitting the format of the stream could be found
	 * @throws Exception if an exception occurs while determining the format from the stream or creating the returned
	 *         reader instance (Depending on the type of reader that is returned, this will mostly be 
	 *         {@link IOException}s.)
	 * @see #guessFormat(File)
	 * @see #getReader(String, File, ReadWriteParameterMap)
	 */
	public JPhyloIOEventReader guessReader(File file, ReadWriteParameterMap parameters) throws Exception {
		JPhyloIOEventReader result = null;
		FileInputStream stream = new FileInputStream(file);
		try {
			result = guessReader(stream, parameters);
		}
		finally {
			if (result == null) {  // Otherwise stream must be closed, by calling close() of the returned reader.
				stream.close();
			}
		}
		return result;
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
		JPhyloIOEventReader result = null;
		FileInputStream stream = new FileInputStream(file);
		try {
			result = getReader(formatID, new FileReader(file), parameters);
		}
		finally {
			if (result == null) {  // Otherwise stream must be closed, by calling close() of the returned reader.
				stream.close();
			}
		}
		return result;
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
