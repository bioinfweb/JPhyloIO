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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.sets;


import java.io.IOException;
import java.util.regex.Pattern;

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
	
	
	public CharSetReader(NexusStreamDataProvider nexusDocument) {
		super("CharSet", new String[]{"SETS"}, nexusDocument);
	}

	
	private boolean checkFormatName(PeekReader reader, String name) {
		return reader.peekString(name.length()).toUpperCase().equals(name);
	}
	
	
	private JPhyloIOEvent readNameAndFormat() throws IOException {
		PeekReader reader = getNexusDocument().getDataReader();
		
		// Read name:
		getNexusDocument().consumeWhiteSpaceAndComments();
		name = reader.readRegExp(UNTIL_WHITESPACE_COMMENT_COMMAND_EQUAL_PATTERN, false).getSequence().toString();
		if (name.length() == 0) {  // Can only happen if end of file was reached. (Otherwise at least ';' or '[' must be in name.)
			throw new IOException("Unexpected end of file");  //TODO Replace by ParseException with line and column information.
		}
		char end = StringUtils.lastChar(name);
		name = StringUtils.cutEnd(name, 1);
		
		if (end == COMMENT_START) {
			getNexusDocument().readComment();
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
		else if (end != CHAR_SET_NAME_SEPARATOR) {
			// Determine format:
			getNexusDocument().consumeWhiteSpaceAndComments();
			isVectorFormat = false;
			if (checkFormatName(reader, FORMAT_NAME_STANDARD)) {
				reader.skip(FORMAT_NAME_STANDARD.length());
			}
			else if (checkFormatName(reader, FORMAT_NAME_VECTOR)) {
				isVectorFormat = true;
				reader.skip(FORMAT_NAME_VECTOR.length());
			}
			
			// Consume '=':
			getNexusDocument().consumeWhiteSpaceAndComments();
			char c = reader.readChar();
			if (c == COMMAND_END) {
				setAllDataProcessed(true);
				return new CharacterSetEvent(name, 0, 0);  // Empty character sets are not valid in Nexus but are anyway supported here.
			}
			else if (c != CHAR_SET_NAME_SEPARATOR) {
				throw new IOException("Unexpected token '" + c + "' found in CharSet command.");  //TODO Replace by parse exception.
			}
		}		
		return null;
	}
	
	
	private JPhyloIOEvent readVectorFormat(boolean isFirstCall) throws IOException {
		char c = getNexusDocument().getDataReader().readChar();
		long currentStartColumn = -1;
		while (c != COMMAND_END) {
			if (!Character.isWhitespace(c)) {
				switch (c) {
					case COMMENT_START:
						getNexusDocument().readComment();
						break;
					case CHAR_SET_CONTAINED:
						if (currentStartColumn == -1) {
							currentStartColumn = currentColumn;
						}
						currentColumn++;
						break;
					case CHAR_SET_NOT_CONTAINED:
						currentColumn++;
						if (currentStartColumn != -1) {
							return new CharacterSetEvent(name, currentStartColumn, currentColumn - 1);
						}
						break;
					default:
						throw new IOException("Invalid CharSet vector symbol '" + c + "' found.");  //TODO Replace by ParseException with line and column information.
				}
			}
			c = getNexusDocument().getDataReader().readChar();
		}
		
		setAllDataProcessed(true);
		if (currentStartColumn != -1) {  // No terminal '0' was found.
			return new CharacterSetEvent(name, currentStartColumn, currentColumn);
		}
		else if (isFirstCall) {  // A sequence of only '0' was found.
			return new CharacterSetEvent(name, 0, 0);
		}
		else {
			return null;
		}
	}
	
	
	private int parseInteger() throws IOException {  //TODO Move method to PeekReader
		String number = getNexusDocument().getDataReader().readRegExp(INTEGER_PATTERN, true).getSequence().toString();
		if (number.length() > 0) {
			return Integer.parseInt(number);
		}
		else {
			return -1;
		}
	}
	
	
	private JPhyloIOEvent readStandardFormat(boolean isFirstCall) throws IOException {
		PeekReader reader = getNexusDocument().getDataReader();
		
		int start = parseInteger();
		if (start == -1) {  // Command end, comment or white space was already checked before calling this method. 
			throw new IOException("Unexpected token '" + reader.peekChar() + "' found in CharSet command.");  //TODO Replace by parse exception.
		}
		else {
			getNexusDocument().consumeWhiteSpaceAndComments();
			int end = start;  // Definitions like "1-2 4 6-7" are allowed. 
			if (reader.peekChar() == CHAR_SET_TO) {
				reader.skip(1);  // Consume '-'
				getNexusDocument().consumeWhiteSpaceAndComments();
				end = parseInteger();
				if (end == -1) {
					throw new IOException("Unexpected end of file in character set definition."); 	//TODO Replace by ParseException
				}
			}
			
			return new CharacterSetEvent(name, start, end + 1);
		}
	}
	
	
	private JPhyloIOEvent endParsing(boolean isFirstCall) {
		setAllDataProcessed(true);
		if (isFirstCall) {
			return new CharacterSetEvent(name, 0, 0);  // Empty character sets are not valid in Nexus but are anyway supported here.
		}
		else {
			return null;
		}
	}
	
	
	@Override
	protected JPhyloIOEvent doReadNextEvent() throws Exception {
		PeekReader reader = getNexusDocument().getDataReader();
		
		// Read set name:
		boolean isFirstCall = (name == null);  // Save for later use
		if (isFirstCall) {
			JPhyloIOEvent result = readNameAndFormat();
			if (result != null) {
				return result;
			}
		}

		// Read position information:
		getNexusDocument().consumeWhiteSpaceAndComments();
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
				return readVectorFormat(isFirstCall);
			}
			else {
				return readStandardFormat(isFirstCall);
			}
		}
	}
}
