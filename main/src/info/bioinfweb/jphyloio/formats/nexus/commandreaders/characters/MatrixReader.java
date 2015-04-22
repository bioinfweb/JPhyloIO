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
import java.util.ArrayList;
import java.util.List;

import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceCharactersEvent;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;



/**
 * Parser for the {@code Matrix} command in a {@code Characters}, {@code Unaligned} or {@code Data} block.
 * 
 * @author Ben St&ouml;ver
 */
public class MatrixReader extends AbstractNexusCommandEventReader implements NexusConstants {
	private String currentSequenceLabel = null;
	
	
	public MatrixReader(NexusStreamDataProvider nexusDocument) {
		super("Matrix", new String[]{BLOCK_NAME_CHARACTERS, BLOCK_NAME_UNALIGNED, BLOCK_NAME_DATA}, nexusDocument);
	}

	
	private String readDelimitedToken(char start, char end) throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		reader.skip(1);  // Consume "start".
		char c = reader.peekChar();
		StringBuilder result = new StringBuilder();
		while (c != MATRIX_POLYMORPHIC_TOKEN_END) {
			if (c == COMMENT_START) {
				reader.skip(1);  // Consume '['.
				getStreamDataProvider().readComment();
			}
			else {
				result.append(c);
				reader.skip(1);  // Consume c.
			}
			c = reader.peekChar();
		}
		reader.skip(1);  // Consume "end".
		return result.toString();
	}
	
	
	private String readToken(boolean longTokens) throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		if (!longTokens) {
			return Character.toString(reader.readChar());
		}
		else {
			char c = reader.peekChar();
			if (c == MATRIX_POLYMORPHIC_TOKEN_START) {
				return readDelimitedToken(MATRIX_POLYMORPHIC_TOKEN_START, MATRIX_POLYMORPHIC_TOKEN_END);
			}
			else if (c == MATRIX_UNCERTAINS_TOKEN_START) {
				return readDelimitedToken(MATRIX_UNCERTAINS_TOKEN_START, MATRIX_UNCERTAINS_TOKEN_END);
			}
			else {
				return readName();
			}
		}
	}
	
	
	private String readName() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		char c = reader.peekChar();
		StringBuilder result = new StringBuilder();
		while (!Character.isWhitespace(c) && (c != COMMAND_END) && (c != COMMENT_START)) {
			result.append(c);
			reader.skip(1);  // Consume c.
			c = reader.peekChar();
		}
		return result.toString();
	}
	
	
	@Override
	protected JPhyloIOEvent doReadNextEvent() throws Exception {
		ParameterMap map = getStreamDataProvider().getSharedInformationMap();
		if (map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false)) {
			throw new InternalError("Transposed Nexus matrices are currently not supported by JPhyloIO.");
		}
		else if (!map.getBoolean(FormatReader.INFO_KEY_LABELS, true)) {
			throw new InternalError("Nexus matrices without labels are currently not supported by JPhyloIO.");
		}
		else {
			boolean longTokens = map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false);
			//boolean interleaved = map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false);
			PeekReader reader = getStreamDataProvider().getDataReader();
			try {
				char c = reader.peekChar();
				if (c == COMMAND_END) {
					reader.skip(1);  // Consume ';'.
					setAllDataProcessed(true);
					return null;
				}
				else {
					// Read name:
					if (currentSequenceLabel == null) {
						getStreamDataProvider().consumeWhiteSpaceAndComments();
						currentSequenceLabel = readName();
					}
					
					// Read tokens:
					List<String> tokens = new ArrayList<String>();
					c = reader.peekChar();
					SequenceCharactersEvent result = null;
					while ((c != COMMAND_END) && (tokens.size() < getStreamDataProvider().getNexusReader().getMaxTokensToRead()) && 
							(currentSequenceLabel != null)) {
						
						if (StringUtils.isNewLineChar(c)) {
							reader.consumeNewLine();  //TODO Can sequences in non-interleaved matrices span over multiple lines? (Could be checked by the number of characters, but not in an UNALIGNED block.)
							if (!tokens.isEmpty()) {  //TODO What about events for empty sequences?
								result = new SequenceCharactersEvent(currentSequenceLabel, tokens);
							}
							currentSequenceLabel = null;  // Read new label next time.
						}
						else if (c == COMMENT_START) {
							reader.skip(1);  // Consume '['.
							getStreamDataProvider().readComment();
						}
						else if (Character.isWhitespace(c)) {  // consumeWhitespaceAndComments() cannot be used here, because line breaks are relevant.
							reader.skip(1);  // Consume white space.
						}
						else {
							String token = readToken(longTokens);
							if (!"".equals(token)) {
								tokens.add(token);
							}
						}
						c = reader.peekChar();
					}
					
					// Return event:
					if (!tokens.isEmpty() && (currentSequenceLabel != null)) {  // Max number of tokens was reached.
						result = new SequenceCharactersEvent(currentSequenceLabel, tokens);
					}
					if (c == COMMAND_END) {
						setAllDataProcessed(true);
						reader.skip(1);  // Consume ';'.
					}
					return result;
				}
			}
			catch (EOFException e) {
				throw new IOException("Unexpected end of file in " + getCommandName() + " command.");  //TODO Replace by ParseException
			}
		}
	}
}