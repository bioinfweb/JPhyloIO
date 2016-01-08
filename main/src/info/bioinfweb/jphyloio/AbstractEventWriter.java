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
package info.bioinfweb.jphyloio;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;



/**
 * Implements shared functionality for classes implementing {@link JPhyloIOEventWriter}.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractEventWriter implements JPhyloIOEventWriter {
	private Writer writer;
	private boolean longTokens = false;
	

	/**
	 * Creates a new instance of this class writing to a writer instance.
	 * 
	 * @param writer the underlying writer to write the data to
	 * @param longTokens Specify {@code true} here, if this instance allows token representation that are longer 
	 *        than one character or {@code false} otherwise.
	 */
	public AbstractEventWriter(Writer writer, boolean longTokens) {
		super();
		this.writer = writer;
		this.longTokens = longTokens;
	}

	
	/**
	 * Creates a new instance of this class writing to an output stream.
	 * 
	 * @param stream the underlying output stream to write the data to
	 * @param longTokens Specify {@code true} here, if this instance allows token representation that are longer 
	 *        than one character or {@code false} otherwise.
	 */
	public AbstractEventWriter(OutputStream stream, boolean longTokens) {
		this(new OutputStreamWriter(stream), longTokens);
	}

	
	/**
	 * Creates a new instance of this class writing to a file.
	 * 
	 * @param stream the file to write the data to
	 * @param longTokens Specify {@code true} here, if this instance allows token representation that are longer 
	 *        than one character or {@code false} otherwise.
	 * @throws IOException if the file exists but is a directory rather than a regular file, does not exist 
	 *         but cannot be created, or cannot be opened for any other reason 
	 */
	public AbstractEventWriter(File file, boolean longTokens) throws IOException {
		this(new FileWriter(file), longTokens);
	}

	
	protected Writer getUnderlyingWriter() {
		return writer;
	}


	/**
	 * Determines whether this instance allows token representation that are longer than one character.
	 * <p>
	 * In many formats tokens will be separated by white space in this case.  
	 * 
	 * @return {@code true} if longer tokens are allowed, {@code false} otherwise
	 */
	public boolean allowsLongTokens() {
		return longTokens;
	}


	/* (non-Javadoc)
	 * @see java.io.Flushable#flush()
	 */
	@Override
	public void flush() throws IOException {
		writer.flush();
	}

	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		writer.close();
	}
}
