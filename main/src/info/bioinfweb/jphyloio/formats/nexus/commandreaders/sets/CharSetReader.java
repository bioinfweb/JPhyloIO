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


import java.io.IOException;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.events.CharacterSetEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;



/**
 * Parser for the {@code CharSet} command in the {@code Sets} block.
 * 
 * @author Ben St&ouml;ver
 */
public class CharSetReader extends AbstractNexusCommandEventReader implements NexusConstants {
	private String name = null;
	private boolean isVectorFormat = false;
	private long currentColumn = 0;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param streamDataProvider the provider of shared Nexus data and streams
	 */
	public CharSetReader(NexusStreamDataProvider streamDataProvider) {
		super("CharSet", new String[]{BLOCK_NAME_SETS}, streamDataProvider);
	}

	
	private boolean checkFormatName(PeekReader reader, String name) {
		return reader.peekString(name.length()).toUpperCase().equals(name);
	}
	
	
	private JPhyloIOEvent readNameAndFormat() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		
		// Read name:
		getStreamDataProvider().consumeWhiteSpaceAndComments();
		name = getStreamDataProvider().readNexusWord();
		if (name.length() == 0) {  // Can only happen if end of file was reached. (Otherwise at least ';' or '[' must be in name.)
			throw new IOException("Unexpected end of file");  //TODO Replace by ParseException with line and column information.
		}
		char end = reader.readChar();  //StringUtils.lastChar(name);
		
		if (end == COMMENT_START) {
			getStreamDataProvider().readComment();
		}
		else if (end == COMMAND_END) {  // character set definition incomplete
			setAllDataProcessed(true);
			if (name.length() > 1) {
				return new CharacterSetEvent(StringUtils.cutEnd(name, 1), 0, 0);  // Empty character sets are not valid in Nexus but are anyway supported here.
			}
			else {
				throw new IOException("Empty CharSet command. At least a set name must be specified.");  //TODO Replace by ParseException with line and column information.
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
				return new CharacterSetEvent(name, 0, 0);  // Empty character sets are not valid in Nexus but are anyway supported here.
			}
			else if (c != KEY_VALUE_SEPARATOR) {
				throw new IOException("Unexpected token '" + c + "' found in CharSet command.");  //TODO Replace by parse exception.
			}
		}		
		return null;
	}
	
	
	private void readVectorFormat(boolean isFirstCall) throws IOException {
		char c = getStreamDataProvider().getDataReader().readChar();
		long currentStartColumn = -1;
		while (c != COMMAND_END) {
			if (!Character.isWhitespace(c)) {
				switch (c) {
					case COMMENT_START:
						if (currentStartColumn != -1) {
							getStreamDataProvider().getUpcomingEvents().add(new CharacterSetEvent(name, currentStartColumn, currentColumn));
						}
						getStreamDataProvider().readComment();
						return;
					case CHAR_SET_CONTAINED:
						if (currentStartColumn == -1) {
							currentStartColumn = currentColumn;
						}
						currentColumn++;
						break;
					case CHAR_SET_NOT_CONTAINED:
						currentColumn++;
						if (currentStartColumn != -1) {
							getStreamDataProvider().getUpcomingEvents().add(new CharacterSetEvent(name, currentStartColumn, currentColumn - 1));
							return;
						}
						break;
					default:
						throw new IOException("Invalid CharSet vector symbol '" + c + "' found.");  //TODO Replace by ParseException with line and column information.
				}
			}
			c = getStreamDataProvider().getDataReader().readChar();
		}
		
		setAllDataProcessed(true);
		if (currentStartColumn != -1) {  // No terminal '0' was found.
			getStreamDataProvider().getUpcomingEvents().add(new CharacterSetEvent(name, currentStartColumn, currentColumn));
		}
		else if (isFirstCall) {  // A sequence of only '0' was found.
			getStreamDataProvider().getUpcomingEvents().add(new CharacterSetEvent(name, 0, 0));
		}
	}
	
	
	private int parseInteger() throws IOException {  //TODO Move method to PeekReader
		String number = getStreamDataProvider().getDataReader().readRegExp(INTEGER_PATTERN, true).getSequence().toString();
		if (number.length() > 0) {
			return Integer.parseInt(number);
		}
		else {
			return -1;
		}
	}
	
	
	private void readStandardFormat(boolean isFirstCall) throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		
		int start = parseInteger();
		if (start == -1) {  // Command end, comment or white space was already checked before calling this method. 
			throw new IOException("Unexpected token '" + reader.peekChar() + "' found in CharSet command.");  //TODO Replace by parse exception.
		}
		else {
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			int end = start;  // Definitions like "1-2 4 6-7" are allowed. 
			if (reader.peekChar() == CHAR_SET_TO) {
				reader.skip(1);  // Consume '-'
				getStreamDataProvider().consumeWhiteSpaceAndComments();
				end = parseInteger();
				if (end == -1) {
					throw new IOException("Unexpected end of file in character set definition."); 	//TODO Replace by ParseException
				}
			}
			
			getStreamDataProvider().getUpcomingEvents().add(new CharacterSetEvent(name, start, end + 1));
		}
	}
	
	
	private boolean endParsing(boolean isFirstCall) {
		setAllDataProcessed(true);
		if (isFirstCall) {
			getStreamDataProvider().getUpcomingEvents().add(new CharacterSetEvent(name, 0, 0));  // Empty character sets are not valid in Nexus but are anyway supported here.
			return true;
		}
		else {
			return false;
		}
	}
	
	
	@Override
	protected boolean doReadNextEvent() throws Exception {
		int initialEventCount = getStreamDataProvider().getUpcomingEvents().size();
		
		PeekReader reader = getStreamDataProvider().getDataReader();
		
		// Read set name:
		boolean isFirstCall = (name == null);  // Save for later use
		if (isFirstCall) {
			JPhyloIOEvent result = readNameAndFormat();
			if (result != null) {
				getStreamDataProvider().getUpcomingEvents().add(result);
				return true;
			}
		}

		// Read position information:
		getStreamDataProvider().consumeWhiteSpaceAndComments();
		int nextChar = reader.peek();
		if (nextChar == -1) {
			throw new IOException("Unexpected end of file");  // At least ';' end "END Sets" would be still to come.  //TODO Replace by ParseException with line and column information.
		}
		else if ((char)nextChar == COMMAND_END) {
			reader.skip(1);  // Consume ';'.
			return endParsing(isFirstCall);
		}
		else {
			if (isVectorFormat) {
				readVectorFormat(isFirstCall);
			}
			else {
				readStandardFormat(isFirstCall);
			}
			getStreamDataProvider().consumeWhiteSpaceAndComments();  // Consume upcoming comments to have the fired before the next character set event.
			return initialEventCount < getStreamDataProvider().getUpcomingEvents().size();
		}
	}
}
