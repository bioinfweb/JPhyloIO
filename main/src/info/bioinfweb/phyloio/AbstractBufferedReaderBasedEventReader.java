/*
 * PhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
 * <http://bioinfweb.info/PhyloIO>
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
package info.bioinfweb.phyloio;


import info.bioinfweb.commons.io.PeekReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;



/**
 * Abstract implementation of {@link AbstractEventReader} for all readers that
 * read their data from a {@link BufferedReader}.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractBufferedReaderBasedEventReader extends AbstractEventReader {
	private PeekReader reader;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the document data to be read 
	 */
	public AbstractBufferedReaderBasedEventReader(PeekReader reader) {
		super();
		this.reader = reader;
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the document data to be read 
	 */
	public AbstractBufferedReaderBasedEventReader(Reader reader) throws IOException {
		super();
		if (!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}
		this.reader = new PeekReader(reader);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the document data to be read 
	 */
	public AbstractBufferedReaderBasedEventReader(InputStream stream) throws IOException {
		this(new InputStreamReader(stream));
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the document file to be read 
	 */
	public AbstractBufferedReaderBasedEventReader(File file) throws IOException{
		this(new FileReader(file));
	}


	/**
	 * Returns the reader providing the document contents.
	 * 
	 * @return the reader to read the document data from
	 */
	protected PeekReader getReader() {
		return reader;
	}


	@Override
	public void close() throws Exception {
		super.close();
		reader.close();
	}


	@Override
	public void reset() throws Exception {
		super.reset();
		reader.reset();
	}
}
