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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.characters;


import java.io.EOFException;
import java.io.IOException;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;



/**
 * Parser for the {@code Format} command in a {@code Characters}, {@code Unaligned} or {@code Data} block.
 * 
 * @author Ben St&ouml;ver
 */
public class FormatReader extends AbstractNexusCommandEventReader implements NexusConstants {
	public static final String KEY_PREFIX = "info.bioinfweb.jphyloio.formats.nexus.format.";
	
	public static final String INFO_KEY_TOKENS_FORMAT = "info.bioinfweb.jphyloio.nexus.tokens";
	public static final String INFO_KEY_INTERLEAVE = "info.bioinfweb.jphyloio.nexus.interleave";
	public static final String INFO_KEY_LABELS = "info.bioinfweb.jphyloio.nexus.labels";
	public static final String INFO_KEY_TRANSPOSE = "info.bioinfweb.jphyloio.nexus.transpose";
	
	
	public FormatReader(NexusStreamDataProvider nexusDocument) {
		super("Format", new String[]{BLOCK_NAME_CHARACTERS, BLOCK_NAME_UNALIGNED, BLOCK_NAME_DATA}, nexusDocument);
	}
	
	
	private String readToken() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		StringBuilder result = new StringBuilder();
		char c = reader.peekChar();
		while (!Character.isWhitespace(c) && (c != COMMAND_END) && (c != KEY_VALUE_SEPARATOR)) {
			if ((char)c == COMMENT_START) {
				reader.skip(1);  // Consume comment start.
				getStreamDataProvider().readComment();
			}
			else {
				result.append(c);
				reader.skip(1);
			}
			c = reader.peekChar();
		}
		return result.toString();
	}
	
	
	private void processSubcommand(String key, final String value) {
		if (FORMAT_SUBCOMMAND_TOKENS.equals(key) || 
				(FORMAT_SUBCOMMAND_DATA_TYPE.equals(key) && FORMAT_VALUE_CONTINUOUS_DATA_TYPE.equals(value.toUpperCase()))) {
			
			getStreamDataProvider().getSharedInformationMap().put(INFO_KEY_TOKENS_FORMAT, true);
		}
		else if (FORMAT_SUBCOMMAND_INTERLEAVE.equals(key)) {
			getStreamDataProvider().getSharedInformationMap().put(INFO_KEY_INTERLEAVE, true);
		}
		else if (FORMAT_SUBCOMMAND_NO_LABELS.equals(key)) {
			getStreamDataProvider().getSharedInformationMap().put(INFO_KEY_LABELS, false);
		}
		else if (FORMAT_SUBCOMMAND_TRANSPOSE.equals(key)) {
			getStreamDataProvider().getSharedInformationMap().put(INFO_KEY_TRANSPOSE, true);
		}
	}
	
	
	@Override
	protected JPhyloIOEvent doReadNextEvent() throws Exception {
		PeekReader reader = getStreamDataProvider().getDataReader();
		try {
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			if (reader.peekChar() != COMMAND_END) {
				// Read key:
				String key = readToken().toUpperCase();
				getStreamDataProvider().consumeWhiteSpaceAndComments();
				
				// Read value:
				String value = "";
				if (reader.peekChar() == KEY_VALUE_SEPARATOR) {
					reader.skip(1);  // Consume '='.
					getStreamDataProvider().consumeWhiteSpaceAndComments();
					
					if (reader.peekChar() == VALUE_DELIMITER) {
						reader.skip(1);  // Consume '"'.
						value = reader.readUntil(Character.toString(VALUE_DELIMITER)).getSequence().toString();
					}
					else {
						value = readToken();
					}
					getStreamDataProvider().consumeWhiteSpaceAndComments();
				}
				processSubcommand(key, value);
				return new MetaInformationEvent(KEY_PREFIX + key, value);  //TODO For some subcommands (e.g. character names) special events would be useful.
			}
			else {
				reader.skip(1); // Consume ';'.
				setAllDataProcessed(true);
				return null;
			}
		}
		catch (EOFException e) {
			throw new IOException("Unexpected and of file in " + getCommandName() + " command.");  //TODO Replace by ParseException
		}
	}
}