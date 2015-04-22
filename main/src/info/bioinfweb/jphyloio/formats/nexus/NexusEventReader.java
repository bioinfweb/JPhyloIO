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
package info.bioinfweb.jphyloio.formats.nexus;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.io.PeekReader.ReadResult;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.AbstractBufferedReaderBasedEventReader;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.DefaultCommandReader;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandEventReader;



/**
 * Event based reader for Nexus files.
 * 
 * @author Ben St&ouml;ver
 */
public class NexusEventReader extends AbstractBufferedReaderBasedEventReader implements NexusConstants {
	private NexusCommandReaderFactory factory;
	private boolean metaEventsForUnknownCommands = false;
	private String currentBlockName = null;
	private NexusCommandEventReader currentCommandReader = null;
	private NexusStreamDataProvider streamDataProvider;
	private boolean documentEndReached = false;
	
	
	private void initStreamDataProvider() {
		streamDataProvider = new NexusStreamDataProvider(this, getReader());
	}
	
	
	public NexusEventReader(File file, NexusCommandReaderFactory factory) throws IOException {
		super(file);
		this.factory = factory;
		initStreamDataProvider();
		
	}
	

	public NexusEventReader(InputStream stream, NexusCommandReaderFactory factory) throws IOException {
		super(stream);
		this.factory = factory;
		initStreamDataProvider();
	}
	

	public NexusEventReader(PeekReader reader, NexusCommandReaderFactory factory) {
		super(reader);
		this.factory = factory;
		initStreamDataProvider();
	}

	
	public NexusEventReader(Reader reader, NexusCommandReaderFactory factory) throws IOException {
		super(reader);
		this.factory = factory;
		initStreamDataProvider();
	}

	
	public boolean isMetaEventsForUnknownCommands() {
		return metaEventsForUnknownCommands;
	}


	public void setMetaEventsForUnknownCommands(boolean metaEventsForUnknownCommands) {
		this.metaEventsForUnknownCommands = metaEventsForUnknownCommands;
	}


	public NexusStreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
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
	
	
	/**
	 * Calls {@link #readComment(char, char)} with according parameters and
	 * ensures visibility for {@link NexusStreamDataProvider}.
	 * 
	 * @throws IOException if an I/O error occurs during the read operation
	 */
	protected void readComment() throws IOException {
		readComment(COMMENT_START, COMMENT_END);
	}
	
	
	protected JPhyloIOEvent pollUpcommingEvent() {
		return upcommingEvents.poll();
	}
	
	
	private void checkStart() throws IOException {
		if (!FIRST_LINE.equals(getReader().readString(FIRST_LINE.length()).toUpperCase())) { 
			throw new IOException("All Nexus files must start with \"" + FIRST_LINE + "\".");
		}
	}
	
	
	private JPhyloIOEvent createBlockStartEvent() {
		if (BLOCK_NAME_CHARACTERS.equals(currentBlockName) || BLOCK_NAME_DATA.equals(currentBlockName) || 
				BLOCK_NAME_UNALIGNED.equals(currentBlockName)) {
			
			return new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_START);
		}
		else {
			return null;
		}
	}
	
	
	private JPhyloIOEvent createBlockEndEvent() {
		if (BLOCK_NAME_CHARACTERS.equals(currentBlockName) || BLOCK_NAME_DATA.equals(currentBlockName) || 
				BLOCK_NAME_UNALIGNED.equals(currentBlockName)) {
			
			return new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_END);
		}
		else {
			return null;
		}
	}
	
	
	private JPhyloIOEvent readNextCommand() throws Exception {
		JPhyloIOEvent result = null;
		while ((result == null) && (getReader().peek() != -1)) {  // Read commands until an event is produced.  
			consumeWhiteSpaceAndComments();
			String commandName = getReader().readRegExp(UNTIL_WHITESPACE_COMMENT_COMMAND_PATTERN, false).getSequence().toString();
			char end = StringUtils.lastChar(commandName);
			commandName = StringUtils.cutEnd(commandName, 1).toUpperCase();
			
			if (end == COMMENT_START) {
				readComment();
			}
			
			if (BEGIN_COMMAND.equals(commandName)) {
				if (currentBlockName == null) {
					consumeWhiteSpaceAndComments();
					currentBlockName = getReader().readRegExp(UNTIL_WHITESPACE_COMMENT_COMMAND_PATTERN, false).getSequence().toString().toUpperCase();
					end = StringUtils.lastChar(currentBlockName);
					currentBlockName = StringUtils.cutEnd(currentBlockName, 1);
					result = createBlockStartEvent();
					if (end != COMMAND_END) {
						consumeWhiteSpaceAndComments();
						if (getReader().peekChar() == COMMAND_END) {
							getReader().skip(1);  // Consume ';'.
						}
						else {
							throw new IOException("Invalid character '" + getReader().peekChar() + "' in " + BEGIN_COMMAND + " command.");
						}
					}
				}
				else {
					throw new IOException("Nested blocks are not allowed in Nexus.");  //TODO Throw other exception
				}
			}
			else if (END_COMMAND.equals(commandName) || ALTERNATIVE_END_COMMAND.equals(commandName)) {
				result = createBlockEndEvent();  // Must be called before currentBlockName is set to null.
				currentBlockName = null;
			}
			else if (end == COMMAND_END) {
				//TODO Fire according event. (else case should be used here, but reader would have to be moved backwards to make ';' available again.)
				result = new MetaInformationEvent(commandName, "");  //TODO Fire different event?
			}
			else {
				currentCommandReader = factory.createReader(currentBlockName, commandName, streamDataProvider);
				if (currentCommandReader == null) {
					if (isMetaEventsForUnknownCommands()) {  // Create meta event from command content.
						currentCommandReader = new DefaultCommandReader(commandName, streamDataProvider);
					}
					else if (end != COMMAND_END) {  // Consume content of command without loading large commands into a CharacterSequence as a whole.
						ReadResult readResult;
						do {
							readResult = getReader().readUntil(getMaxTokensToRead(), Character.toString(COMMAND_END));
						} while (!readResult.isCompletelyRead());
					}
				}
				if (currentCommandReader != null) {
					result = currentCommandReader.readNextEvent();
				}
			}
			
			if ((result == null) && !upcommingEvents.isEmpty()) {
				return upcommingEvents.poll();
			}
		}
		return result;
	}
	
	
	@Override
	protected JPhyloIOEvent readNextEvent() throws Exception {
		if (documentEndReached) { 
			return null;
		}
		else {
			if (isBeforeFirstAccess()) {
				checkStart();
				consumeWhiteSpaceAndComments();
				return new ConcreteJPhyloIOEvent(EventType.DOCUMENT_START);
			}
			else if (!upcommingEvents.isEmpty()) {
				return upcommingEvents.poll();
			}
			else {
				JPhyloIOEvent event = null;
				if (currentCommandReader == null) {
					event = readNextCommand();
				}
				else {
					event = currentCommandReader.readNextEvent();
					if (event == null) {
						event = readNextCommand();
					}
				}
				if (event == null) {
					documentEndReached = true;
					event = new ConcreteJPhyloIOEvent(EventType.DOCUMENT_END);
				}
				return event;
			}
		}
	}
}
