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


import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import info.bioinfweb.commons.SystemUtils;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.EventWriteResult;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;



/**
 * Writer to write <i>JPhyloIO</i> events to a FASTA file.
 * 
 * @author Ben St&ouml;ver
 */
public class FASTAEventWriter extends AbstractEventWriter implements FASTAConstants {
	private static final int DEFAULT_LINE_LENGTH = 80;
	
	
	private int lineLength = DEFAULT_LINE_LENGTH;
	private boolean alignmentEndReached = false;
	private String currentSequenceName = null;
	private int charsPerLineWritten = 0;
	private boolean commentContinuationExpected = false;
	
	
	/**
	 * Creates a new instance of this class writing to a writer instance.
	 * 
	 * @param writer the underlying writer to write the data to
	 * @param longTokens Specify {@code true} here, if this instance allows token representation that are longer 
	 *        than one character or {@code false} otherwise.
	 */
	public FASTAEventWriter(File file, boolean longTokens) throws IOException {
		super(file, longTokens);
	}

	
	/**
	 * Creates a new instance of this class writing to an output stream.
	 * 
	 * @param stream the underlying output stream to write the data to
	 * @param longTokens Specify {@code true} here, if this instance allows token representation that are longer 
	 *        than one character or {@code false} otherwise.
	 */
	public FASTAEventWriter(OutputStream stream, boolean longTokens) {
		super(stream, longTokens);
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
	public FASTAEventWriter(Writer writer, boolean longTokens) {
		super(writer, longTokens);
	}
	
	
	private void writeNewLine() throws IOException {
		getUnderlyingWriter().write(SystemUtils.LINE_SEPARATOR);
		charsPerLineWritten = 0;
	}
	
	
	private void writeTokenEvent(SequenceTokensEvent event) throws IOException, IllegalArgumentException {
		if (!event.getSequenceName().equals(currentSequenceName)) {
			if (charsPerLineWritten > 0) {
				writeNewLine();
			}
			currentSequenceName = event.getSequenceName();
			getUnderlyingWriter().write(NAME_START_CHAR + currentSequenceName + SystemUtils.LINE_SEPARATOR);
		}
		
		for (String token : event.getCharacterValues()) {
			if (allowsLongTokens()) {
				token += " "; 
			}
			else if (token.length() > 1) {
				throw new IllegalArgumentException("The specified string representation of one or more of token(s) is longer "
						+ "than one character, although this reader is set to not allow longer tokens.");
			}
			if (charsPerLineWritten + token.length() > lineLength) {
				writeNewLine();
			}
			getUnderlyingWriter().write(token);
			charsPerLineWritten += token.length();
		}
	}
	
	
	private void writeCommentEvent(CommentEvent event) throws IOException {
		if (!commentContinuationExpected) {
			if (charsPerLineWritten > 0) {
				writeNewLine();
			}
			getUnderlyingWriter().write(COMMENT_START_CHAR);
		}
		
		getUnderlyingWriter().write(event.getContent());  // Single write calls are used, to avoid string copying if content is very large.
		
		commentContinuationExpected = event.isContinuedInNextEvent();
		if (!commentContinuationExpected) {
			getUnderlyingWriter().write(SystemUtils.LINE_SEPARATOR);
		}
	}
	
	
	/**
	 * Writes the specified event to the stream.
	 * <p>
	 * Note that the underlying writer will be closed, as soon as an event of type {@link EventType#DOCUMENT_END}
	 * is written here.
	 * 
	 * @param event the event to written to the current position of the stream
	 * @return {@code true} if the event was written or {@code false} if the event was stored to be written later
	 *         because the target format does not support this element at the current position.
	 * @throws IOException if an IO error occurs during the write operation
	 * @throws IllegalStateException if an event was specified, than cannot be written at the current position
	 *         (e.g. another event after a {@link EventType#DOCUMENT_END} event)
	 * @throws IllegalArgumentException if a token representation longer than one character if contained in a
	 *         specified {@link SequenceTokensEvent}, although {@link #allowsLongTokens()} was set to {@code false} 
	 */
	@Override
	public EventWriteResult writeEvent(JPhyloIOEvent event) throws IOException, IllegalStateException, IllegalArgumentException {
		if ((event.getEventType().equals(EventType.BLOCK_END) && 
				(event.asBlockEndEvent().getStartEventType().equals(EventType.DOCUMENT))) || !alignmentEndReached) {
			
			if (commentContinuationExpected && !event.getEventType().equals(EventType.COMMENT)) {
				commentContinuationExpected = false;
				writeNewLine();  // Terminate unfinished comment.
			}
			
			switch (event.getEventType()) {
				case SEQUENCE_CHARACTERS:
					writeTokenEvent(event.asSequenceTokensEvent());
					break;
				case COMMENT:
					writeCommentEvent(event.asCommentEvent());
					break;

				case BLOCK_END:
					switch (event.asBlockEndEvent().getStartEventType()) {
						case ALIGNMENT:
							alignmentEndReached = true;
							break;
						case DOCUMENT:
							close();
							break;
					}
				case DOCUMENT:
				case ALIGNMENT:
					break;  // Return EventWriteResult.WRITTEN. (This class would allow omitting DOCUMENT_START and ALIGNMENT_START.)
				default:
					return EventWriteResult.NOT_WRITTEN;  // Unsupported event.
			}
			return EventWriteResult.WRITTEN;
		}
		else {
			throw new IllegalStateException("FASTA files do not support multiple alignments.");
		}
	}
}
