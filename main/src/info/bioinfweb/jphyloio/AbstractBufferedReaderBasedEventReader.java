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


import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;



/**
 * Abstract implementation of {@link AbstractEventReader} for all readers that
 * read their data from a {@link BufferedReader}.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractBufferedReaderBasedEventReader extends AbstractEventReader {
	public static final int DEFAULT_MAX_COMMENT_LENGTH = 1024 * 1024;
	
	public static class KeyValueInformation {
		private String key;
		private String originalKey;
		private String value;
		
		public KeyValueInformation(String prefix, String key, String value) {
			super();
			this.key = prefix + key;
			this.originalKey = key;
			this.value = value;
		}
		
		public String getKey() {
			return key;
		}

		public String getOriginalKey() {
			return originalKey;
		}

		public String getValue() {
			return value;
		}
	}
	
	
	private int maxCommentLength = DEFAULT_MAX_COMMENT_LENGTH;
	private PeekReader reader;
	protected boolean lineConsumed = true;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the document data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 */
	public AbstractBufferedReaderBasedEventReader(PeekReader reader, boolean translateMatchToken) {
		super(translateMatchToken);
		this.reader = reader;
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the document data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 */
	public AbstractBufferedReaderBasedEventReader(Reader reader, boolean translateMatchToken) throws IOException {
		super(translateMatchToken);
		if (!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}
		this.reader = new PeekReader(reader);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param stream the stream providing the document data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 */
	public AbstractBufferedReaderBasedEventReader(InputStream stream, boolean translateMatchToken) throws IOException {
		this(new InputStreamReader(stream), translateMatchToken);
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the document file to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 */
	public AbstractBufferedReaderBasedEventReader(File file, boolean translateMatchToken) throws IOException{
		this(new FileReader(file), translateMatchToken);
	}


	/**
	 * Returns the maximum length a comment may have, before it is split into separate events.
	 * <p>
	 * The default value is {@link #DEFAULT_MAX_COMMENT_LENGTH}.
	 * 
	 * @return the maximum allowed number of characters for a single comment
	 */
	public int getMaxCommentLength() {
		return maxCommentLength;
	}


	/**
	 * Allows the specify the maximum length a comment may have, before it is split into separate events.
	 * 
	 * @param maxCommentLength the maximum allowed number of characters for a single comment that shall be 
	 *        used from now on
	 */
	public void setMaxCommentLength(int maxCommentLength) {
		this.maxCommentLength = maxCommentLength;
	}


	protected List<String> createTokenList(CharSequence sequence) {
		List<String> result = new ArrayList<String>(sequence.length());
		for (int i = 0; i < sequence.length(); i++) {
			char c = sequence.charAt(i);
			if (!Character.isWhitespace(c)) {  // E.g. Phylip and MEGA allow white spaces in between sequences
				result.add(Character.toString(c));
			}
		}
		return result;
	}
	
	
	protected void consumeWhiteSpaceAndComments(char commentStart, char commentEnd) throws IOException {
		int c = getReader().peek();
		while ((c != -1) && (Character.isWhitespace(c) || ((char)c == commentStart))) {
			if (((char)c == commentStart)) {
				getReader().skip(1);  // Consume comment start.
				readComment(commentStart, commentEnd);
			}
			else {
				getReader().skip(1);  // Consume white space.
			}
			c = getReader().peek();
		}
	}
	
	
	/**
	 * Reads a single comment from the reader. Only the first one of subsequent comments (e.g. 
	 * {@code [comment 1][comment 2]}) would be read. Nested comments are included in the
	 * parsed comment.
	 * <p>
	 * This method assumes that the comment start symbol has already been consumed. 
	 * 
	 * @throws IOException
	 */
	protected void readComment(char commentStart, char commentEnd) throws IOException {
		StringBuilder content = new StringBuilder();
		int nestedComments = 0;
		try {
			char c = getReader().readChar();
			int length = 0;
			while (!((nestedComments == 0) && (c == commentEnd))) {
				if (c == commentStart) {
					nestedComments++;
				}
				else if (c == commentEnd) {
					nestedComments--;
				}
				content.append(c);
				length++;
				if (length >= getMaxCommentLength()) {
					c = getReader().peekChar();
					getUpcomingEvents().add(new CommentEvent(content.toString(), (c == -1) || (c == commentEnd)));
					content.delete(0, content.length());
					length = 0;
				}
				c = getReader().readChar();
			}
			if (content.length() > 0) {
				getUpcomingEvents().add(new CommentEvent(content.toString(), true));
			}
		}
		catch (EOFException e) {
			throw new JPhyloIOReaderException("Unexpected end of file inside a comment.", getReader());
		}
	}
	
	
	private JPhyloIOEvent eventFromCharacters(String currentSequenceName, CharSequence content) throws Exception {
		List<String> characters = createTokenList(content);
		if (characters.isEmpty()) {  // The rest of the line was consisting only of spaces
			return null;
		}
		else {
			return getSequenceTokensEventManager().createEvent(currentSequenceName, characters);
		}
	}
	
	
	protected JPhyloIOEvent readCharacters(String currentSequenceName) throws Exception {
		PeekReader.ReadResult readResult = getReader().readLine(getMaxTokensToRead());
		lineConsumed = readResult.isCompletelyRead();
		return eventFromCharacters(currentSequenceName, readResult.getSequence());
	}
	
	
	/**
	 * Reads characters from the stream and adds an according sequence tokens event to the queue. Additionally comment events
	 * are added to the queue, if comments are found.
	 * 
	 * @param currentSequenceName
	 * @param commentStart
	 * @param commentEnd
	 * @return the sequence tokens event that was added to the event queue
	 * @throws Exception
	 */
	protected JPhyloIOEvent readCharacters(String currentSequenceName, char commentStart, char commentEnd) throws Exception {
		final Pattern pattern = Pattern.compile(".*(\\n|\\r|\\" + commentStart + ")");
		PeekReader.ReadResult readResult = getReader().readRegExp(getMaxTokensToRead() /*- content.length()*/, pattern, false);  // In greedy mode the start of a nested comment could be consumed.
		char lastChar = StringUtils.lastChar(readResult.getSequence());
		
		JPhyloIOEvent result = eventFromCharacters(currentSequenceName, StringUtils.cutEnd(readResult.getSequence(), 1));
		if (result != null) {
			getUpcomingEvents().add(result);
		}
		if (lastChar == commentStart) {
			readComment(commentStart, commentEnd);
		}
		else if (StringUtils.isNewLineChar(lastChar)) {
		  // Consume rest of line break:
			int nextChar = getReader().peek();
			if ((nextChar != -1) && (lastChar == '\r') && ((char)nextChar == '\n')) {
				getReader().skip(1);
			}
			lineConsumed = true;
			getUpcomingEvents().add(new PartEndEvent(EventContentType.SEQUENCE, false));
		}
		else {  // Maximum length was reached.
			lineConsumed = false;
		}
		return result;
	}

	
	protected String readToken(char commandEnd, char commentStart, char commentEnd, char keyValueSeparator) throws IOException {
		PeekReader reader = getReader();
		StringBuilder result = new StringBuilder();
		char c = reader.peekChar();
		while (!Character.isWhitespace(c) && (c != commandEnd) && (c != keyValueSeparator)) {
			if ((char)c == commentStart) {
				reader.skip(1);  // Consume comment start.
				readComment(commentStart, commentEnd);
			}
			else {
				result.append(c);
				reader.skip(1);
			}
			c = reader.peekChar();
		}
		return result.toString();
	}
	
	
	protected KeyValueInformation readKeyValueInformation(String keyPrefix, char commandEnd, char commentStart, 
			char commentEnd, char keyValueSeparator, char valueDelimiter) throws IOException {
		
		PeekReader reader = getReader();
		
		// Read key:
		String key = readToken(commandEnd, commentStart, commentEnd, keyValueSeparator).toLowerCase();
		consumeWhiteSpaceAndComments(commentStart, commentEnd);
		
		// Read value:
		String value = "";
		if (reader.peekChar() == keyValueSeparator) {
			reader.skip(1);  // Consume '='.
			consumeWhiteSpaceAndComments(commentStart, commentEnd);
			
			if (reader.peekChar() == valueDelimiter) {
				reader.skip(1);  // Consume '"'.
				value = reader.readUntil(Character.toString(valueDelimiter)).getSequence().toString();
			}
			else {
				value = readToken(commandEnd, commentStart, commentEnd, keyValueSeparator);
			}
			consumeWhiteSpaceAndComments(commentStart, commentEnd);
		}
		return new KeyValueInformation(keyPrefix, key, value);
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
}
