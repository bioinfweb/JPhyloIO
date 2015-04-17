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
package info.bioinfweb.jphyloio;


import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceCharactersEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;



/**
 * Abstract implementation of {@link AbstractEventReader} for all readers that
 * read their data from a {@link BufferedReader}.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractBufferedReaderBasedEventReader extends AbstractEventReader {
	private PeekReader reader;
	protected boolean lineConsumed = true;
	protected Queue<JPhyloIOEvent> upcommingEvents = new LinkedList<JPhyloIOEvent>();
	
	
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


	protected List<String> createTokenList(CharSequence sequence) {
		List<String> result = new ArrayList<String>(sequence.length());
		for (int i = 0; i < sequence.length(); i++) {
			if (!Character.isWhitespace(sequence.charAt(i))) {  // E.g. Phylip and MEGA allow white spaces in between sequences
				result.add(Character.toString(sequence.charAt(i)));
			}
		}
		return result;
	}
	
	
	/**
	 * Reads a single comment from the reader. Only the first one of subsequent comments (e.g. 
	 * {@code [comment 1][comment 2]}) would be parsed. 
	 * <p>
	 * This method assumes that the comment start symbol has already been consumed. 
	 * 
	 * @throws IOException
	 */
	protected void readComment(char commentStart, char commentEnd) throws IOException {
		StringBuilder content = new StringBuilder();
		int nestedComments = 0;
		int c = getReader().read();
		while ((c != -1) && !((nestedComments == 0) && ((char)c == commentEnd))) {
			if ((char)c == commentStart) {
				nestedComments++;
			}
			else if ((char)c == commentEnd) {
				nestedComments--;
			}
			content.append((char)c);
			c = getReader().read();
		}
		
		upcommingEvents.add(new CommentEvent(content.toString()));
	}
	
	
	private JPhyloIOEvent eventFromCharacters(String currentSequenceName, CharSequence content) throws Exception {
		List<String> characters = createTokenList(content);
		if (characters.isEmpty()) {  // The rest of the line was consisting only of spaces
			return readNextEvent();  // Continue parsing to create the next event
		}
		else {
			return new SequenceCharactersEvent(currentSequenceName, characters);
		}
	}
	
	
	protected JPhyloIOEvent readCharacters(String currentSequenceName) throws Exception {
		PeekReader.ReadResult readResult = getReader().readLine(getMaxTokensToRead());
		lineConsumed = readResult.isCompletelyRead();
		return eventFromCharacters(currentSequenceName, readResult.getSequence());
	}
	
	
	protected JPhyloIOEvent readCharacters(String currentSequenceName, char commentStart, char commentEnd) throws Exception {
		final Pattern pattern = Pattern.compile(".+(\\n|\\r|\\" + commentStart + ")");
		StringBuffer content = new StringBuffer(getMaxTokensToRead());
		char lastChar = commentStart;
		while (lastChar == commentStart) {
			PeekReader.ReadResult readResult = getReader().readRegExp(getMaxTokensToRead() - content.length(), pattern, false);  // In greedy mode the start of a nested comment could be consumed.
			lastChar = readResult.getSequence().charAt(readResult.getSequence().length() - 1); 
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
			}
			else {  // Maximum length was reached.
				lineConsumed = false;
			}
			content.append(readResult.getSequence().subSequence(0, readResult.getSequence().length() - 1));
		}
		return eventFromCharacters(currentSequenceName, content);
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
