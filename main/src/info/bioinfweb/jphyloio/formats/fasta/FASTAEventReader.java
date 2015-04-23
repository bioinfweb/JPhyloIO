/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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


import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceCharactersEvent;



/**
 * Event based reader for FASTA alignment files.
 * 
 * @author Ben St&ouml;ver
 */
public class FASTAEventReader extends AbstractBufferedReaderBasedEventReader {
	public static final char NAME_START_CHAR = '>';
	
	
	private String currentSequenceName = null;
	private String firstSequenceName = null;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the FASTA data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public FASTAEventReader(BufferedReader reader, boolean translateMatchToken) throws IOException {
		super(reader, translateMatchToken);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the FASTA file to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public FASTAEventReader(File file, boolean translateMatchToken) throws IOException {
		super(file, translateMatchToken);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the FASTA data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public FASTAEventReader(InputStream stream, boolean translateMatchToken) throws IOException {
		super(stream, translateMatchToken);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the FASTA data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public FASTAEventReader(Reader reader, boolean translateMatchToken) throws IOException {
		super(reader, translateMatchToken);
	}
	
	
	private JPhyloIOEvent readSequenceStart(String exceptionMessage) throws IOException {
		try {
			if (getReader().readChar() == NAME_START_CHAR) {
				currentSequenceName = getReader().readLine().getSequence().toString();
				currentPosition = 0;
				return null;
			}
			else {
				throw new IOException(exceptionMessage);
			}
		}
		catch (EOFException e) {
			return new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_END);
		}
	}
	
	
	@Override
	protected JPhyloIOEvent readNextEvent() throws IOException {
		if (isBeforeFirstAccess()) {
			return new ConcreteJPhyloIOEvent(EventType.DOCUMENT_START);
		}
		else {
			JPhyloIOEvent alignmentEndEvent;
			
			switch (getPreviousEvent().getEventType()) {
				case DOCUMENT_START:
					return new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_START);
					
				case ALIGNMENT_START:
					alignmentEndEvent = readSequenceStart("FASTA file does not start with a \"" + NAME_START_CHAR + "\".");
					if (alignmentEndEvent != null) {
						return alignmentEndEvent;
					}
					firstSequenceName = currentSequenceName;
					lineConsumed = true;  
					// fall through
				case SEQUENCE_CHARACTERS:
					// Check if new name needs to be read:
					int c = getReader().peek();
					if ((c == -1) || (lineConsumed && (c == (int)NAME_START_CHAR))) {
						alignmentEndEvent = readSequenceStart(
								"Inconsistent stream. (The cause might be code outside this class reading from the same stream.)");
						if (alignmentEndEvent != null) {
							return alignmentEndEvent;
						}
						else {
							c = getReader().peek();
							if ((c == -1) || (c == (int)NAME_START_CHAR)) {  // No characters found before the next name. => empty sequence
								return new SequenceCharactersEvent(currentSequenceName, Collections.<String>emptyList());
							}
						}
					}

					// Read new tokens:
					PeekReader.ReadResult lineResult = getReader().readLine(getMaxTokensToRead());
					List<String> tokenList = new ArrayList<String>(lineResult.getSequence().length());
					for (int i = 0; i < lineResult.getSequence().length(); i++) {
						String token = Character.toString(lineResult.getSequence().charAt(i));
						if (isTranslateMatchToken()) {
							if (firstSequenceName.equals(currentSequenceName)) {
								firstSequence.add(token);
							}
							else {
								token = replaceMatchToken(token);
							}
						}
						tokenList.add(token);
						currentPosition++;
					}
					lineConsumed = lineResult.isCompletelyRead();					
					return new SequenceCharactersEvent(currentSequenceName, tokenList);
					
				case ALIGNMENT_END:
					return new ConcreteJPhyloIOEvent(EventType.DOCUMENT_END);

				case DOCUMENT_END:
					return null;  // Calling method will throw a NoSuchElementException.

				default:  // includes META_INFORMATION
					throw new InternalError("Impossible case");
			}
		}
	}
}
