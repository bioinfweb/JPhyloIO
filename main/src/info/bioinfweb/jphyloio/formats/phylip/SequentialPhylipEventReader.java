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
package info.bioinfweb.jphyloio.formats.phylip;


import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceCharactersEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;



public class SequentialPhylipEventReader extends AbstractPhylipEventReader {
	private int charactersRead = Integer.MAX_VALUE;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 */
	public SequentialPhylipEventReader(BufferedReader reader, boolean relaxedPhylip) throws IOException {
		super(reader, relaxedPhylip);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the Phylip file to be read 
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 */
	public SequentialPhylipEventReader(File file, boolean relaxedPhylip) throws IOException {
		super(file, relaxedPhylip);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the Phylip data to be read 
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 */
	public SequentialPhylipEventReader(InputStream stream, boolean relaxedPhylip) throws IOException {
		super(stream, relaxedPhylip);
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Phylip data to be read 
	 * @param relaxedPhylip Specify {@code true} here, if data in relaxed Phylip format (sequence names not limited to 10
	 *        characters, no spaces in sequence names allowed, spaces between sequence names and sequence characters necessary)
	 *        shall be parsed, or {@code false} if the expected data is in classic Phylip.
	 */
	public SequentialPhylipEventReader(Reader reader, boolean relaxedPhylip) throws IOException {
		super(reader, relaxedPhylip);
	}
	
	
	@Override
	protected JPhyloIOEvent readNextEvent() throws Exception {
		if (isBeforeFirstAccess()) {
			return new ConcreteJPhyloIOEvent(EventType.DOCUMENT_START);
		}
		else {
			switch (getPreviousEvent().getEventType()) {
				case DOCUMENT_START:
					readMatrixDimensions();
					return createAlignmentStartEvent(getSequenceCount(), getCharacterCount());
					
				case ALIGNMENT_START:
					if (getSequenceCount() == 0) {  // Empty alignment:
						return new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_END);
					}
				case SEQUENCE_CHARACTERS:
					if (lineConsumed) {  // Keep current name if current line was not completely consumed yet.
						while (getReader().isNewLineNext()) {  // Ignore empty lines.
							getReader().consumeNewLine();
						}
						
						if (charactersRead >= getCharacterCount()) {  // Read next name:
							currentSequenceName = readSequenceName();
							charactersRead = 0;
						}
						
						if (getReader().peek() == -1) {  // End of file was reached
							// if (currentSequenceIndex < sequenceCount) {}  //TODO Should an exception be thrown here, if the specified number of sequences has not been found yet? => Probably not, because parsing files with a wrong number of specified sequences would still make sense, unless this is not a accidental stream break.
							return new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_END);
						}
					}
					
					// Read characters:
					JPhyloIOEvent event = readCharacters(currentSequenceName);
					if (EventType.SEQUENCE_CHARACTERS.equals(event.getEventType())) {
						charactersRead += ((SequenceCharactersEvent)event).getCharacterValues().size();
					}
					return event;
					
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
