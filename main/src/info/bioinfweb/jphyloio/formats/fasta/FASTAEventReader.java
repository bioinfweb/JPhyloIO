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


import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.io.PeekReader.ReadResult;
import info.bioinfweb.jphyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;



/**
 * Event based reader for FASTA alignment files.
 * <p>
 * This reader supports comments in lines following the name definition line. Additionally indices preceding 
 * lines containing sequence data are allowed but are ignored by this reader.
 * <p>
 * <b>Example:</b>
 * <pre>
 *   >Sequence 1
 *   ;Some comment
 *   ;Another comment
 *    0 ACGT
 *    5 TAGC
 *   10 TTAGT
 *   >Sequence 2  
 *   ACGT-ACC-TAGT
 * </pre>
 * <p>
 * This reader does not process the sequence names according to any conventions. The full string following '>' 
 * will be returned in a each {@link SequenceTokensEvent} and can be parsed later on by the application.
 * 
 * @author Ben St&ouml;ver
 */
public class FASTAEventReader extends AbstractBufferedReaderBasedEventReader implements FASTAConstants {
	private String currentSequenceName = null;
	
	
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
			  //TODO Optionally an additional OTU event with an ID could be generated here.
				getUpcomingEvents().add(new LinkedOTUEvent(EventContentType.SEQUENCE, 
						DEFAULT_SEQUENCE_ID_PREFIX + getIDManager().createNewID(),	currentSequenceName, null));  // This event may remain in the queue in addition to the upcoming characters event, since it will not consume much memory.
				while (getReader().peekChar() == COMMENT_START_CHAR) {
					getReader().read();  // Consume ';'.
					ReadResult readResult;
					do {
						readResult = getReader().readLine(getMaxCommentLength());
						getUpcomingEvents().add(new CommentEvent(readResult.getSequence().toString(), !readResult.isCompletelyRead()));
					} while (!readResult.isCompletelyRead());
				}
				return null;
			}
			else {
				throw new JPhyloIOReaderException(exceptionMessage, getReader());
			}
		}
		catch (EOFException e) {
			return new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END);
		}
	}
	
	
	/**
	 * Consumes whitespace and token indices and returns whether the file contains any additional content
	 * 
	 * @return {@code true} if additional content is to be read or {@code false} otherwise
	 * @throws IOException
	 */
	private boolean consumeTokenIndex() throws IOException {
		try {
			char c = getReader().peekChar();
			while (Character.isWhitespace(c) || Character.isDigit(c)) {
				getReader().skip(1);
				c = getReader().peekChar();
			}
			return true;
		}
		catch (EOFException e) {
			return false;
		}
	}
	
	
	@Override
	protected void readNextEvent() throws IOException {
		if (isBeforeFirstAccess()) {
			getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
		}
		else {
			JPhyloIOEvent alignmentEndEvent;
			
			switch (getPreviousEvent().getType().getContentType()) {
				case DOCUMENT:
					if (getPreviousEvent().getType().getTopologyType().equals(EventTopologyType.START)) {
						getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.START));
						break;
					}
					else {
						return;  // Calling method will throw a NoSuchElementException.
					}
					
				case ALIGNMENT:
					if (getPreviousEvent().getType().getTopologyType().equals(EventTopologyType.START)) {
						alignmentEndEvent = readSequenceStart("FASTA file does not start with a \"" + NAME_START_CHAR + "\".");
						if (alignmentEndEvent != null) {
							getUpcomingEvents().add(alignmentEndEvent);
							break;
						}
						else {
							lineConsumed = true;
						}
					}
					else {
						getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
						break;
					}
					// fall through for if case
				case SEQUENCE:
				case SEQUENCE_TOKENS:
				case COMMENT:
					// Check if new name needs to be read:
					int c = getReader().peek();
					if ((c == -1) || (lineConsumed && (c == (int)NAME_START_CHAR))) {
						getUpcomingEvents().add(new PartEndEvent(EventContentType.SEQUENCE, true));
						alignmentEndEvent = readSequenceStart(
								"Inconsistent stream. (The cause might be code outside this class reading from the same stream.)");
						if (alignmentEndEvent != null) {
							getUpcomingEvents().add(alignmentEndEvent);
						}
						if (!getUpcomingEvents().isEmpty()) {
							break;  // Return token or sequence end event from above or waiting comment event from readSequenceStart(). 
						}
					}

					// Read new tokens:
					if (lineConsumed) {
						if (!consumeTokenIndex()) {  // The last line of the file contained only white spaces or token indices.
							getUpcomingEvents().add(new PartEndEvent(EventContentType.SEQUENCE, true));
							getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
							break;
						}
					}
					PeekReader.ReadResult lineResult = getReader().readLine(getMaxTokensToRead());
					List<String> tokenList = new ArrayList<String>(lineResult.getSequence().length());
					for (int i = 0; i < lineResult.getSequence().length(); i++) {
						tokenList.add(Character.toString(lineResult.getSequence().charAt(i)));
					}
					lineConsumed = lineResult.isCompletelyRead();
					getUpcomingEvents().add(getSequenceTokensEventManager().createEvent(currentSequenceName, tokenList));
					break;
					
				default:  // includes META_INFORMATION
					throw new InternalError("Impossible case");
			}
		}
	}
}
