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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.sets;


import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.exception.UnsupportedFormatFeatureException;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;



/**
 * General reader implementation to read the Nexus commands that define different kinds of sets.
 * <p>
 * It supports reading sets in Nexus standard and vector format.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public abstract class AbstractNexusSetReader extends AbstractNexusCommandEventReader implements NexusConstants, ReadWriteConstants {
	//TODO Implement support for Nexus name references (to both objects and other sets). Calling the createEventsForName() method will be necessary.
	//TODO Document in JavaDoc if any feature of the standard formats is not supported, when version 1.0 is released (e.g. "REMINDER").
	
	private EventContentType setType;
	private String name = null;
	private boolean isVectorFormat = false;
	private long currentColumn = 0;
	
	
	public AbstractNexusSetReader(EventContentType setType, String commandName, String[] validBlocks, NexusReaderStreamDataProvider nexusDocument) {
		super(commandName, validBlocks, nexusDocument);
		this.setType = setType;
	}
	
	
	protected abstract String getLinkedID();
	
	/**
	 * Returns the number of elements which can potentially be contained in this set (e.g. the number of declared taxa or the number
	 * of alignment columns).
	 * 
	 * @return the number of elements or -1 if the number is currently undefined (e.g. if no block is linked to the current 
	 *         {@code SETS} block
	 */
	protected abstract long getElementCount();
	
	protected abstract void createEventsForInterval(long start, long end);
	
	protected abstract void createEventsForName(String name);  //TODO Can the mapping to IDs already be done in this class, so that IDs can be passed here?
	
	
	private boolean checkFormatName(PeekReader reader, String name) {
		return reader.peekString(name.length()).toUpperCase().equals(name);
	}
	

	private void addStartEvent(String name) {
		getStreamDataProvider().getCurrentEventCollection().add(new LinkedLabeledIDEvent(setType, 
				DEFAULT_GENERAL_ID_PREFIX + getStreamDataProvider().getIDManager().createNewID(), name, 
				getLinkedID()));
	}
	
	
	private boolean readNameAndFormat() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		
		// Read name:
		getStreamDataProvider().consumeWhiteSpaceAndComments();
		name = getStreamDataProvider().readNexusWord();
		if (name.length() == 0) {  // Can only happen if end of file was reached. (Otherwise at least ';' or '[' must be in name.)
			throw new JPhyloIOReaderException("Unexpected end of file in a Nexus CHARSET command.", reader);
		}
		char end = reader.readChar();  //StringUtils.lastChar(name);
		
		if (end == COMMENT_START) {
			getStreamDataProvider().readComment();
		}
		else if (end == COMMAND_END) {  // character set definition incomplete
			setAllDataProcessed(true);
			if (name.length() > 1) {
				addStartEvent(StringUtils.cutEnd(name, 1));
				getStreamDataProvider().getCurrentEventCollection().add(new PartEndEvent(setType, true));  // Empty character sets are not valid in Nexus but are anyway supported here.
				return true;
			}
			else {
				throw new JPhyloIOReaderException("Empty Nexus CHARSET command. At least a set name must be specified.", reader);
			}
		}
		else if (end != KEY_VALUE_SEPARATOR) {
			// Determine format:
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			isVectorFormat = false;
			if (checkFormatName(reader, FORMAT_NAME_STANDARD)) {
				reader.skip(FORMAT_NAME_STANDARD.length());
			}
			else if (checkFormatName(reader, FORMAT_NAME_VECTOR)) {
				isVectorFormat = true;
				reader.skip(FORMAT_NAME_VECTOR.length());
			}
			
			// Consume '=':
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			char c = reader.readChar();
			if (c == COMMAND_END) {
				setAllDataProcessed(true);
				addStartEvent(name);
				getStreamDataProvider().getCurrentEventCollection().add(new PartEndEvent(setType, true));  // Empty character sets are not valid in Nexus but are anyway supported here.
				return true;
			}
			else if (c != KEY_VALUE_SEPARATOR) {
				throw new JPhyloIOReaderException("Unexpected token '" + c + "' found in CharSet command.", reader);
			}
		}
		
		addStartEvent(name);
		return false;
	}

	
	private JPhyloIOReaderException createUnknownElementCountException(PeekReader reader) {
		return new JPhyloIOReaderException("A set was referencing the maximum index (using '.'), which could not be determined. "
				+ "A possible cause can be that no block containing the set elements (e.g. TAXA or CHARACTERS) was previously defined.", reader);
	}
	
	
	private void consumeWhiteSpaceAndComments(Collection<JPhyloIOEvent> buffer) throws IOException {
		getStreamDataProvider().setCurrentEventCollection(buffer);  // Whitespace and comments need to be consumed here to be able to test whether SET_REGULAR_INTERVAL_SYMBOL is next. Events cannot be fired directly, because if no SET_REGULAR_INTERVAL_SYMBOL follows, the comments should be fired after the interval event below.
		try {
			getStreamDataProvider().consumeWhiteSpaceAndComments();
		}
		finally {
			getStreamDataProvider().resetCurrentEventCollection();
		}
	}
	
	
	private void readStandardFormat() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		
		//TODO Add support for "ALL", "x\3", "5-." and "REMAINING" (at least exception in the latter case).
		//     Probably a boolean map should be used for this, which would also allow unordered interval definitions.
		//TODO Support direct listing of names (some set definitions can also contain Nexus names)
		
		long nexusStart;
		long nexusEnd;
		Collection<JPhyloIOEvent> savedCommentEvents = new ArrayList<JPhyloIOEvent>();
		long finalIndex = getElementCount();
				//TODO The current implementation works only for character sets!
		
		try {
			if (reader.isNext(SET_KEY_WORD_ALL)) {
				reader.skip(SET_KEY_WORD_ALL.length());
				if (finalIndex < 0) {
					throw createUnknownElementCountException(reader);
				}
				else {
					nexusStart = 1;
					nexusEnd = finalIndex;
					//TODO Consume remaining characters until command end (but process comments), since additional intervals would be valid but unnecessary.
				}
			}
			else if (reader.isNext(SET_KEY_WORD_REMAINING)) {
				throw new UnsupportedFormatFeatureException("The encountered keyword " + SET_KEY_WORD_REMAINING + 
						" is currently not supported in JPhyloIO.", reader);
			}
			else {
				nexusStart = getStreamDataProvider().readPositiveInteger(-1);  // '.' is only allowed for the end index according to the Nexus paper.
				if (nexusStart == -1) {  // '.' was found.
					throw new JPhyloIOReaderException("The token '.' may not be the first element of a set interval definition in Nexus.", reader);
				}
				else if (nexusStart == -2) {  // non-integer token found
					//TODO Read possible named reference here.
					throw new UnsupportedFormatFeatureException("Named set references are currently not supported.", reader);
				}
				else {
					consumeWhiteSpaceAndComments(savedCommentEvents);
					nexusEnd = nexusStart;  // Definitions like "1-2 4 6-7" are allowed.
					if (reader.peekChar() == SET_TO_SYMBOL) {
						reader.skip(1);  // Consume '-'
						consumeWhiteSpaceAndComments(savedCommentEvents);
						nexusEnd = getStreamDataProvider().readPositiveInteger(finalIndex);
						if (nexusEnd == -2) {
							throw createUnknownElementCountException(reader);
						}
						else if (nexusEnd < 0) {
							throw new JPhyloIOReaderException("Unexpected end of file in Nexus character set definition.", reader);  //TODO This is not always EOF? (Illegal characters would also be possible?)
						}
						
						consumeWhiteSpaceAndComments(savedCommentEvents);
					}
				}
			}
			
			if (reader.peekChar() == SET_REGULAR_INTERVAL_SYMBOL) {  //TODO Throw exception, if this construct is used together with a reference to another set. (A special UnsupportedFeatureException could be implemented for such cases.)
				reader.skip(1);  // Consume '\'
				consumeWhiteSpaceAndComments(savedCommentEvents);
				long interval = getStreamDataProvider().readPositiveInteger(-1);
				if (nexusEnd == -2) {
					throw createUnknownElementCountException(reader);
				}
				else if (interval < 0) {
					throw new JPhyloIOReaderException("Unexpected token found in Nexus set definition.", reader);  //TODO More concrete message?
				}
				for (long i = nexusStart - 1; i < nexusEnd; i += interval) {
					createEventsForInterval(i, i + 1);
				}
			}
			else {
				createEventsForInterval(nexusStart - 1, nexusEnd);
			}
			getStreamDataProvider().getCurrentEventCollection().addAll(savedCommentEvents);
		}
		catch (EOFException e) {  // Would the thrown by peekChar().
			throw new JPhyloIOReaderException("Unexpected end of file inside a set definition.", reader, e);
		}
	}
	
	
	private void readVectorFormat() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		Collection<JPhyloIOEvent> queue = getStreamDataProvider().getCurrentEventCollection();
		
		char c = reader.readChar();
		long currentStartColumn = -1;
		while (c != COMMAND_END) {
			if (!Character.isWhitespace(c)) {
				switch (c) {
					case COMMENT_START:
						if (currentStartColumn != -1) {
							createEventsForInterval(currentStartColumn, currentColumn);
						}
						getStreamDataProvider().readComment();
						return;
					case SET_VECTOR_CONTAINED:
						if (currentStartColumn == -1) {
							currentStartColumn = currentColumn;
						}
						currentColumn++;
						break;
					case SET_VECTOR_NOT_CONTAINED:
						currentColumn++;
						if (currentStartColumn != -1) {
							createEventsForInterval(currentStartColumn, currentColumn - 1);
							return;
						}
						break;
					default:
						throw new JPhyloIOReaderException("Invalid CHARSET vector symbol '" + c + "' found.", reader);
				}
			}
			c = reader.readChar();
		}
		
		setAllDataProcessed(true);
		if (currentStartColumn != -1) {  // No terminal '0' was found.
			createEventsForInterval(currentStartColumn, currentColumn);
		}
		queue.add(new PartEndEvent(setType, true));
	}
	
	
	@Override
	protected boolean doReadNextEvent() throws IOException {
		int initialEventCount = getStreamDataProvider().getCurrentEventCollection().size();
		PeekReader reader = getStreamDataProvider().getDataReader();
		
		// Read set name:
		boolean isFirstCall = (name == null);  // Save for later use
		if (isFirstCall) {
			if (readNameAndFormat()) {
				return true;
			}
		}

		// Read position information:
		getStreamDataProvider().consumeWhiteSpaceAndComments();
		int nextChar = reader.peek();
		if (nextChar == -1) {
			throw new JPhyloIOReaderException("Unexpected end of file in Nexus CHARSET command.", reader);  // At least ';' end "END Sets" would be still to come.
		}
		else if ((char)nextChar == COMMAND_END) {
			reader.skip(1);  // Consume ';'.
			setAllDataProcessed(true);
			getStreamDataProvider().getCurrentEventCollection().add(new PartEndEvent(EventContentType.CHARACTER_SET, true));
			return isFirstCall;
		}
		else {
			if (isVectorFormat) {
				readVectorFormat();
			}
			else {
				readStandardFormat();
			}
			getStreamDataProvider().consumeWhiteSpaceAndComments();  // Consume upcoming comments to have the fired before the next character set event.
			return initialEventCount < getStreamDataProvider().getCurrentEventCollection().size();
		}
	}
}
