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
package info.bioinfweb.jphyloio.formats.nexus;


import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Queue;

import info.bioinfweb.commons.LongIDManager;
import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.io.PeekReader.ReadResult;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.UnknownCommandEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.DefaultCommandReader;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandEventReader;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.characters.FormatReader;
import info.bioinfweb.jphyloio.tools.SequenceTokensEventManager;



/**
 * Event based reader for Nexus files.
 * 
 * @author Ben St&ouml;ver
 */
public class NexusEventReader extends AbstractBufferedReaderBasedEventReader implements NexusConstants {
	private NexusCommandReaderFactory factory;
	private boolean createUnknownCommandEvents = false;
	private String currentBlockName = null;
	private NexusCommandEventReader currentCommandReader = null;
	private NexusStreamDataProvider streamDataProvider;
	private boolean documentEndReached = false;
	
	
	private void initStreamDataProvider() {
		streamDataProvider = new NexusStreamDataProvider(this, getReader());
	}
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param file the Nexus file to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param factory the factory to create instances of Nexus command readers that shall be used for parsing the document
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public NexusEventReader(File file, boolean translateMatchToken, NexusCommandReaderFactory factory) throws IOException {
		super(file, translateMatchToken);
		this.factory = factory;
		initStreamDataProvider();
	}
	

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the stream providing the Nexus data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param translateMatchToken
	 * @param factory the factory to create instances of Nexus command readers that shall be used for parsing the document
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public NexusEventReader(InputStream stream, boolean translateMatchToken, NexusCommandReaderFactory factory) throws IOException {
		super(stream, translateMatchToken);
		this.factory = factory;
		initStreamDataProvider();
	}
	

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the Nexus data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param factory the factory to create instances of Nexus command readers that shall be used for parsing the document
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public NexusEventReader(PeekReader reader, boolean translateMatchToken, NexusCommandReaderFactory factory) {
		super(reader, translateMatchToken);
		this.factory = factory;
		initStreamDataProvider();
	}

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param reader the reader providing the FASTA data to be read 
	 * @param translateMatchToken Specify {@code true} here to automatically replace the match character or token (usually '.') 
	 *        by the according token from the first sequence or {@code false} if the match token shall remain in the returned
	 *        sequences. (Note that the first sequence of an alignment needs to be stored in memory by this instance in order
	 *        to replace the match token.)
	 * @param factory the factory to create instances of Nexus command readers that shall be used for parsing the document
	 * @throws IOException if an I/O exception occurs while parsing the first event
	 */
	public NexusEventReader(Reader reader, boolean translateMatchToken, NexusCommandReaderFactory factory) throws IOException {
		super(reader, translateMatchToken);
		this.factory = factory;
		initStreamDataProvider();
	}

	
	/**
	 * Specifies whether {@link UnknownCommandEvent}s will be fired for all Nexus commands with no according reader
	 * stored in the factory. The key will be the name of the command and the value will be its contents.
	 * <p>
	 * Note that e.g. {@link FormatReader} will fire additional Nexus specific meta events, even if this property
	 * is set to {@code false}.  
	 * 
	 * @return {@code true} if unknown command events will be fired, {@code false} otherwise
	 */
	public boolean getCreateUnknownCommandEvents() {
		return createUnknownCommandEvents;
	}


	/**
	 * Use this method to switch firing unknown command events for unknown Nexus commands on and off.
	 * 
	 * @param createUnknownCommandEvents Specify {@code true} here, to receive unknown command events from now on or 
	 *        {@code false} to let the reader ignore unknown Nexus commands.
	 */
	public void setCreateUnknownCommandEvents(boolean createUnknownCommandEvents) {
		this.createUnknownCommandEvents = createUnknownCommandEvents;
	}


	/**
	 * Returns the stream and data provider used by this reader to share information between the different
	 * underlying implementations if {@link NexusCommandEventReader}.
	 * <p>
	 * The returned object should only be used by the underlying command readers. Do not read data from the provided
	 * streams directly to avoid unexpected behavior. 
	 * 
	 * @return the shared stream and data provider
	 */
	@Override
	public NexusStreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
	}

	
	@Override
	protected LongIDManager getIDManager() {  // Enable package visibility
		return super.getIDManager();
	}

	
	@Override
	protected SequenceTokensEventManager getSequenceTokensEventManager() {  // Enable package visibility
		return super.getSequenceTokensEventManager();
	}
	

	/**
	 * Calls {@link #consumeWhiteSpaceAndComments(char, char)} with according parameters and
	 * ensures visibility for {@link NexusStreamDataProvider}.
	 * 
	 * @throws IOException if an I/O error occurs during the read operation
	 */
	protected void consumeWhiteSpaceAndComments() throws IOException {
		consumeWhiteSpaceAndComments(COMMENT_START, COMMENT_END);
	}
	
	
	protected String readNexusWord() throws IOException {
		try {
			StringBuilder result = new StringBuilder();
			char c = getReader().peekChar();
			if (c == WORD_DELIMITER) {
				getReader().skip(1);  // Consume '.
				c = getReader().peekChar();
				while (true) {
					if (c == WORD_DELIMITER) {
						getReader().skip(1);  // Consume ' (either the first of two inside a word or the terminal delimiter).
						if (getReader().peekChar() != WORD_DELIMITER) {  // Otherwise go on parsing and add one ' to the result.
							break;
						}
					}
					result.append(c);
					getReader().skip(1);  // Consume last character.
					c = getReader().peekChar();
				}
			}
			else {
				while (!Character.isWhitespace(c) && (c != COMMENT_START) && (c != COMMAND_END) && (c != KEY_VALUE_SEPARATOR) 
						&& (c != ELEMENT_SEPARATOR)) {  //TODO Add more special characters
					
					result.append(c);
					getReader().skip(1);
					c = getReader().peekChar();
				}
			}
			return result.toString();
		}
		catch (EOFException e) {
			throw new JPhyloIOReaderException("Unexpected end of file inside a Nexus name.", getReader(), e);
		}
	}
	
	
	/**
	 * Calls {@link #readComment(char, char)} with according parameters and
	 * ensures visibility for {@link NexusStreamDataProvider}.
	 * 
	 * @throws IOException if an I/O error occurs during the read operation
	 */
	protected void readComment() throws IOException {
		readComment(COMMENT_START, COMMENT_END);
	}
	
	
	protected KeyValueInformation readKeyValueMetaInformation(String keyPrefix) throws IOException {
		return readKeyValueInformation(keyPrefix, COMMAND_END, COMMENT_START, COMMENT_END, KEY_VALUE_SEPARATOR, VALUE_DELIMITER);
	}
	
	
	/**
	 * Returns the queue of upcoming events to be used by implementations of {@link NexusCommandEventReader}.
	 * <p>
	 * This reimplementation just delegates to the superclass method. It is made to make this method visible 
	 * inside this package.
	 * 
	 * @return the queue of upcoming events
	 */
	protected Queue<JPhyloIOEvent> getUpcomingEvents() {
		return super.getUpcomingEvents();
	}
	
	
	private void checkStart() throws IOException {
		if (!FIRST_LINE.equals(getReader().readString(FIRST_LINE.length()).toUpperCase())) { 
			throw new JPhyloIOReaderException("All Nexus files must start with \"" + FIRST_LINE + "\".", 0, 0, 0);
		}
	}
	
	
	private void addBlockStartEndEvent(EventTopologyType topologyType) {
		if (BLOCK_NAME_CHARACTERS.equals(currentBlockName) || BLOCK_NAME_DATA.equals(currentBlockName) || 
				BLOCK_NAME_UNALIGNED.equals(currentBlockName)) {
			
			if (topologyType.equals(EventTopologyType.START)) {
				getUpcomingEvents().add(new LabeledIDEvent(EventContentType.ALIGNMENT, 
						DEFAULT_MATRIX_ID_PREFIX + getIDManager().createNewID(), null));
			}
			else {
				getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
			}
		}
	}
	
	
	private boolean readNextCommand() throws Exception {
		consumeWhiteSpaceAndComments();  // Needs to be done before the loop once, if the rest of the file only consists of whitespace and comments.
		while (getUpcomingEvents().isEmpty() && (getReader().peek() != -1)) {  // Read commands until an event is produced.  
			String commandName = getReader().readRegExp(UNTIL_WHITESPACE_COMMENT_COMMAND_PATTERN, false).getSequence().toString();
			char lastChar = StringUtils.lastChar(commandName);
			commandName = StringUtils.cutEnd(commandName, 1).toUpperCase();
			
			if (lastChar == COMMENT_START) {
				readComment();
			}
			
			if (BEGIN_COMMAND.equals(commandName)) {
				if (currentBlockName == null) {
					consumeWhiteSpaceAndComments();
					currentBlockName = getReader().readRegExp(
							UNTIL_WHITESPACE_COMMENT_COMMAND_PATTERN, false).getSequence().toString().toUpperCase();
					lastChar = StringUtils.lastChar(currentBlockName);
					currentBlockName = StringUtils.cutEnd(currentBlockName, 1);
					addBlockStartEndEvent(EventTopologyType.START);
					if (lastChar != COMMAND_END) {
						consumeWhiteSpaceAndComments();
						if (getReader().peekChar() == COMMAND_END) {
							getReader().skip(1);  // Consume ';'.
						}
						else {
							throw new JPhyloIOReaderException("Invalid character '" + getReader().peekChar() + "' in " + BEGIN_COMMAND + 
									" command.", getReader());
						}
					}
				}
				else {
					throw new JPhyloIOReaderException("Nested blocks are not allowed in Nexus.", getReader());
				}
			}
			else if (END_COMMAND.equals(commandName) || ALTERNATIVE_END_COMMAND.equals(commandName)) {
				addBlockStartEndEvent(EventTopologyType.END);  // Must be called before currentBlockName is set to null.
				currentBlockName = null;
				
				consumeWhiteSpaceAndComments();
				if ((getReader().peek() != -1) && (getReader().peekChar() == COMMAND_END)) {
					getReader().read();  // Skip COMMAND_END. (Necessary if ';' is not located directly behind the "END".)
				}
			}
			else if (lastChar == COMMAND_END) {
				//TODO Fire according event. (else case should be used here, but reader would have to be moved backwards to make ';' available again.)
				getUpcomingEvents().add(new MetaInformationEvent(commandName, null, ""));
				getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
			}
			else {
				currentCommandReader = factory.createReader(currentBlockName, commandName, streamDataProvider);
				if (currentCommandReader == null) {
					if (getCreateUnknownCommandEvents()) {  // Create unknown command event from command content.
						currentCommandReader = new DefaultCommandReader(commandName, streamDataProvider);
					}
					else if (lastChar != COMMAND_END) {  // Consume content of command without loading large commands into a CharacterSequence as a whole.
						ReadResult readResult;
						do {
							readResult = getReader().readUntil(getMaxTokensToRead(), Character.toString(COMMAND_END));
						} while (!readResult.isCompletelyRead());
					}
				}
				if (currentCommandReader != null) {
					return currentCommandReader.readNextEvent();
				}
			}
			consumeWhiteSpaceAndComments();
		}
		return getReader().peek() != -1;
	}
	
	
	@Override
	protected void readNextEvent() throws Exception {
		if (!documentEndReached) { 
			if (isBeforeFirstAccess()) {
				checkStart();
				getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
				consumeWhiteSpaceAndComments();
			}
			else {
				boolean eventCreated = false;
				if (currentCommandReader == null) {
					eventCreated = readNextCommand();
				}
				else {
					eventCreated = currentCommandReader.readNextEvent();
					if (!eventCreated) {
						do {
							eventCreated = readNextCommand();
							consumeWhiteSpaceAndComments();
						} while (!eventCreated && (getReader().peek() != -1));
					}
				}
				if (!eventCreated) {
					documentEndReached = true;
					getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.END));
				}
			}
		}
	}
}
