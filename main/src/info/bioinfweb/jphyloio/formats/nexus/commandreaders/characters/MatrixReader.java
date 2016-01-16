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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.characters;


import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.events.BasicOTUEvent;
import info.bioinfweb.jphyloio.events.SequenceEndEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
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
	private long currentSequencePosition = 0;
	
	
	public MatrixReader(NexusStreamDataProvider nexusDocument) {
		super("Matrix", new String[]{BLOCK_NAME_CHARACTERS, BLOCK_NAME_UNALIGNED, BLOCK_NAME_DATA}, nexusDocument);
	}

	
	private String readDelimitedToken(char start, char end) throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		reader.skip(1);  // Consume "start".
		char c = reader.peekChar();
		StringBuilder result = new StringBuilder();
		while (c != end) {
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
		return start + result.toString() + end;
	}
	
	
	private String readToken(boolean longTokens) throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		char c = reader.peekChar();
		if (!longTokens && (c != MATRIX_POLYMORPHIC_TOKEN_START) && (c != MATRIX_UNCERTAINS_TOKEN_START)) {
			return Character.toString(reader.readChar());
		}
		else {
			if (c == MATRIX_POLYMORPHIC_TOKEN_START) {
				return readDelimitedToken(MATRIX_POLYMORPHIC_TOKEN_START, MATRIX_POLYMORPHIC_TOKEN_END);
			}
			else if (c == MATRIX_UNCERTAINS_TOKEN_START) {
				return readDelimitedToken(MATRIX_UNCERTAINS_TOKEN_START, MATRIX_UNCERTAINS_TOKEN_END);
			}
			else {
				return getStreamDataProvider().readNexusWord();  // Also parses ' delimited tokens although they are formally not allowed in Nexus. 
			}
		}
	}
	
	
	@Override
	protected boolean doReadNextEvent() throws Exception {
		ParameterMap map = getStreamDataProvider().getSharedInformationMap();
		if (map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false)) {
			throw new InternalError("Transposed Nexus matrices are currently not supported by JPhyloIO.");
		}
		else if (!map.getBoolean(FormatReader.INFO_KEY_LABELS, true)) {
			throw new InternalError("Nexus matrices without labels are currently not supported by JPhyloIO.");
		}
		else {
			boolean longTokens = map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false);
			boolean interleaved = map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false);
			PeekReader reader = getStreamDataProvider().getDataReader();
			try {
				char c = reader.peekChar();
				if (c == COMMAND_END) {
					reader.skip(1);  // Consume ';'.
					setAllDataProcessed(true);
					return false;
				}
				else {
					// Read name:
					if (currentSequenceLabel == null) {
						getStreamDataProvider().consumeWhiteSpaceAndComments();
						if (!getStreamDataProvider().getUpcomingEvents().isEmpty()) {
							return true;  // Immediately return comment in front of sequence name.
						}
						currentSequenceLabel = getStreamDataProvider().readNexusWord();
					  //TODO Link possible taxon with sequence start event.
						getStreamDataProvider().getUpcomingEvents().add(new BasicOTUEvent(EventContentType.SEQUENCE, currentSequenceLabel, null));
						currentSequencePosition = getStreamDataProvider().getSequenceTokensEventManager().getCurrentBlockStartPosition();  // Should always be 0, except in interleaved reading.
					}
					
					// Read tokens:
					List<String> tokens = new ArrayList<String>();
					c = reader.peekChar();
					boolean result = false;
					boolean tokenListComplete = false;
					while ((c != COMMAND_END) && (tokens.size() < getStreamDataProvider().getNexusReader().getMaxTokensToRead()) && 
							!tokenListComplete) {
						
						
						if ((c == MATRIX_UNALIGNED_SEQUENCE_SEPARATOR) || (interleaved && StringUtils.isNewLineChar(c)) ||
								(currentSequencePosition >= map.getLong(DimensionsReader.INFO_KEY_CHAR, Long.MAX_VALUE))) {  // Position from SequenceTokensEventManager cannot be used here, because it is not valid until all read tokens have been passed to createEvent(). 
							
							if (c == MATRIX_UNALIGNED_SEQUENCE_SEPARATOR) {  // If ',' should be allowed as a token in CHARACTERS and DATA blocks, an additional check whether the current block is UNALIGNED would be needed here.
								reader.skip(1);  // Consume ','.
							}
							reader.consumeNewLine();
							if (!tokens.isEmpty()) {  //TODO What about events for empty sequences?
								getStreamDataProvider().getUpcomingEvents().add(
										getStreamDataProvider().getSequenceTokensEventManager().createEvent(currentSequenceLabel, tokens));
								result = true;
							}
							currentSequenceLabel = null;  // Read new label next time.
							tokenListComplete = true;
							getStreamDataProvider().getUpcomingEvents().add(new SequenceEndEvent(false));  //TODO Set boolean according to NTAX or ','/';'.
						}
						else if (c == COMMENT_START) {
							if (!tokens.isEmpty()) {
								getStreamDataProvider().getUpcomingEvents().add(  // Make sure to add token event before comment event.
										getStreamDataProvider().getSequenceTokensEventManager().createEvent(currentSequenceLabel, tokens));
							}
							
							reader.skip(1);  // Consume '['.
							getStreamDataProvider().readComment();
							return true;  // Return comment that was just read.
						}
						else if (Character.isWhitespace(c)) {  // consumeWhitespaceAndComments() cannot be used here, because line breaks are relevant.
							reader.skip(1);  // Consume white space.
						}
						else {
							String token = readToken(longTokens);
							if (!"".equals(token)) {
								tokens.add(token);
								currentSequencePosition++;
							}
						}
						c = reader.peekChar();
					}
					
					// Return event:
					if (!tokens.isEmpty() && (currentSequenceLabel != null)) {  // Max number of tokens was reached.
						getStreamDataProvider().getUpcomingEvents().add(
								getStreamDataProvider().getSequenceTokensEventManager().createEvent(currentSequenceLabel, tokens));
						result = true;
					}
					if (c == COMMAND_END) {
						setAllDataProcessed(true);
						reader.skip(1);  // Consume ';'.
						getStreamDataProvider().getUpcomingEvents().add(new SequenceEndEvent(true));
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
