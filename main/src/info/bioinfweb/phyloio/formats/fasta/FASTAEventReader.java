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
package info.bioinfweb.phyloio.formats.fasta;


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.phyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.phyloio.events.ConcretePhyloIOEvent;
import info.bioinfweb.phyloio.events.EventType;
import info.bioinfweb.phyloio.events.PhyloIOEvent;
import info.bioinfweb.phyloio.events.SequenceStartEvent;
import info.bioinfweb.phyloio.events.TokensEvent;



/**
 * Event based reader for FASTA alignment files.
 * 
 * @author Ben St&ouml;ver
 */
public class FASTAEventReader extends AbstractBufferedReaderBasedEventReader {
	public static final char NAME_START_CHAR = '>';
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the FASTA data to be read 
	 */
	public FASTAEventReader(BufferedReader reader) throws IOException {
		super(reader);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the FASTA file to be read 
	 */
	public FASTAEventReader(File file) throws IOException {
		super(file);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the FASTA data to be read 
	 */
	public FASTAEventReader(InputStream stream) throws IOException {
		super(stream);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the FASTA data to be read 
	 */
	public FASTAEventReader(Reader reader) throws IOException {
		super(reader);
	}
	
	
	private PhyloIOEvent readSequenceStart(String exceptionMessage) throws IOException {
		try {
			if (getReader().readChar() == NAME_START_CHAR) {
				return new SequenceStartEvent(getReader().readLine().getLine().toString());
			}
			else {
				throw new IOException("FASTA file does not start with a \"" + NAME_START_CHAR + "\".");
			}
		}
		catch (EOFException e) {
			return new ConcretePhyloIOEvent(EventType.ALIGNMENT_END);
		}
	}
	
	
	@Override
	protected PhyloIOEvent readNextEvent() throws IOException {
		if (isBeforeFirstAccess()) {
			return new ConcretePhyloIOEvent(EventType.DOCUMENT_START);
		}
		else {
			boolean lineConsumed = true;
			
			switch (getPreviousEvent().getEventType()) {
				case DOCUMENT_START:
					return new ConcretePhyloIOEvent(EventType.ALIGNMENT_START);
					
				case ALIGNMENT_START:
					return readSequenceStart("FASTA file does not start with a \"" + NAME_START_CHAR + "\".");
					
				case SEQUENCE_START:
					lineConsumed = true;  // fall through
				case SEQUENCE_TOKENS:
					int c = getReader().peek();
					if ((c == -1) || (lineConsumed && (c == (int)NAME_START_CHAR))) {
						return new ConcretePhyloIOEvent(EventType.SEQUENCE_END);
					}
					else {
						PeekReader.ReadLineResult lineResult = getReader().readLine(getMaxTokensToRead());
						List<String> tokenList = new ArrayList<String>(lineResult.getLine().length());
						for (int i = 0; i < lineResult.getLine().length(); i++) {
							tokenList.add(Character.toString(lineResult.getLine().charAt(i)));
						}
						lineConsumed = lineResult.isLineCompletelyRead();
						
						return new TokensEvent(tokenList);
					}
					
				case SEQUENCE_END:
					return readSequenceStart("Inconsistent stream. (The cause might code outside this class reading from the same stream.)");  // Should have been guaranteed in the last call of this method.
					
				case ALIGNMENT_END:
					return new ConcretePhyloIOEvent(EventType.DOCUMENT_END);

				case DOCUMENT_END:
					return null;  // Calling method will throw a NoSuchElementException.

				default:  // includes META_INFORMATION
					throw new InternalError("Impossible case");
			}
		}
	}
}
