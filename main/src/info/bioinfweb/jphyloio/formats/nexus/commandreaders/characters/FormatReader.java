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
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.events.CharacterSetEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
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
	
	public static final Pattern MIXED_DATA_TYPE_VALUE_PATTERN = Pattern.compile(FORMAT_VALUE_MIXED_DATA_TYPE + "\\((.+)\\)", 
			Pattern.DOTALL + Pattern.CASE_INSENSITIVE);
	public static final Pattern MIXED_DATA_TYPE_SINGLE_SET_PATTERN = Pattern.compile("(.+)\\:([0-9]+)\\-([0-9]+)");
	public static final String DATA_TYPE_CHARACTER_SET_NAME_PREFIX = "DataTypeCharSet";
	
	
	private boolean continuousData = false;
	
	
	public FormatReader(NexusStreamDataProvider nexusDocument) {
		super("Format", new String[]{BLOCK_NAME_CHARACTERS, BLOCK_NAME_UNALIGNED, BLOCK_NAME_DATA}, nexusDocument);
	}
	
	
	private TokenSetDefinitionEvent.SetType getTokenSetType(String parsedName) {
		if (parsedName.equals(FORMAT_VALUE_STANDARD_DATA_TYPE)) {
			return TokenSetDefinitionEvent.SetType.DISCRETE;
		}
		else if (parsedName.equals(FORMAT_VALUE_NUCLEOTIDE_DATA_TYPE)) {
			return TokenSetDefinitionEvent.SetType.NUCLEOTIDE;
		}
		else if (parsedName.equals(FORMAT_VALUE_DNA_DATA_TYPE)) {
			return TokenSetDefinitionEvent.SetType.DNA;
		}
		else if (parsedName.equals(FORMAT_VALUE_RNA_DATA_TYPE)) {
			return TokenSetDefinitionEvent.SetType.RNA;
		}
		else if (parsedName.equals(FORMAT_VALUE_PROTEIN_DATA_TYPE)) {
			return TokenSetDefinitionEvent.SetType.AMINO_ACID;
		}
		else if (parsedName.equals(FORMAT_VALUE_CONTINUOUS_DATA_TYPE)) {
			continuousData = true;
			return TokenSetDefinitionEvent.SetType.CONTINUOUS;
		}
		else {
			return TokenSetDefinitionEvent.SetType.UNKNOWN;
		}
	}
	
	
	private void parseMixedDataType(String content) {
		String[] parts = content.split("\\,");
		List<JPhyloIOEvent> events = new ArrayList<JPhyloIOEvent>(parts.length * 2);
		for (int i = 0; i < parts.length; i++) {
			Matcher matcher = MIXED_DATA_TYPE_SINGLE_SET_PATTERN.matcher(parts[i]);
			if (matcher.matches()) {
				try {
					events.add(new CharacterSetEvent(DATA_TYPE_CHARACTER_SET_NAME_PREFIX + (i + 1), 
							Long.parseLong(matcher.group(2)),	Long.parseLong(matcher.group(3)) + 1));
				}
				catch (NumberFormatException e) {
					return;  // Abort parsing and treat the whole string as a regular data type name.  //TODO Give warning or throw exception?
				}
				events.add(new TokenSetDefinitionEvent(getTokenSetType(matcher.group(1).toUpperCase()), matcher.group(1)));
			}
			else {
				return;  // Abort parsing and treat the whole string as a regular data type name.  //TODO Give warning or throw exception?
			}
		}
		getStreamDataProvider().getUpcomingEvents().addAll(events);  // Events are added to the queue when its sure that there was no parsing error.
	}
	
	
	private JPhyloIOEvent processSubcommand(MetaInformationEvent event) throws IOException {
		String key = event.getKey().substring(KEY_PREFIX.length()).toUpperCase();  // Remove key prefix for comparison
		String value = event.getValue().toUpperCase();
		JPhyloIOEvent result = event;
		
		if (FORMAT_SUBCOMMAND_TOKENS.equals(key) || 
				(FORMAT_SUBCOMMAND_DATA_TYPE.equals(key) && FORMAT_VALUE_CONTINUOUS_DATA_TYPE.equals(value))) {
			
			getStreamDataProvider().getSharedInformationMap().put(INFO_KEY_TOKENS_FORMAT, true);
		}
		if (FORMAT_SUBCOMMAND_DATA_TYPE.equals(key)) {
			Matcher matcher = MIXED_DATA_TYPE_VALUE_PATTERN.matcher(event.getValue());  // Does case insensitive matching.
			if (matcher.matches()) {  // Parse MrBayes extension
				parseMixedDataType(matcher.group(1));
						//event.getValue().substring(FORMAT_VALUE_MIXED_DATA_TYPE.length() + 1, event.getValue().length() - 1));
				if (!getStreamDataProvider().getUpcomingEvents().isEmpty()) {  // Otherwise the previously constructed meta event for datatype will be returned.
					result = getStreamDataProvider().getUpcomingEvents().poll();
				}
			}
			else {
				result = new TokenSetDefinitionEvent(getTokenSetType(value), event.getValue());
			}
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
		else if (FORMAT_SUBCOMMAND_MATCH_CHAR.equals(key)) {
			getStreamDataProvider().getNexusReader().setMatchToken(event.getValue());
			result = new SingleTokenDefinitionEvent(event.getValue(), SingleTokenDefinitionEvent.Meaning.MATCH);
		}
		else if (FORMAT_SUBCOMMAND_GAP_CHAR.equals(key)) {
			result = new SingleTokenDefinitionEvent(event.getValue(), SingleTokenDefinitionEvent.Meaning.GAP);
		}
		else if (FORMAT_SUBCOMMAND_MISSING_CHAR.equals(key)) {
			result = new SingleTokenDefinitionEvent(event.getValue(), SingleTokenDefinitionEvent.Meaning.MISSING);
		}
		else if (FORMAT_SUBCOMMAND_SYMBOLS.equals(key)) {
			if (continuousData) {
				throw new IOException("The subcommand " + FORMAT_SUBCOMMAND_SYMBOLS + " of " + getCommandName() + " is not allowed if " +
						FORMAT_SUBCOMMAND_DATA_TYPE + "=" + FORMAT_VALUE_CONTINUOUS_DATA_TYPE + " was specified.");  //TODO Replace by ParseException
			}
			else {
				for (int i = 0; i < event.getValue().length(); i++) {
					char c = event.getValue().charAt(i);
					if (!Character.isWhitespace(c)) {
						getStreamDataProvider().getUpcomingEvents().add(new SingleTokenDefinitionEvent(
								Character.toString(c), SingleTokenDefinitionEvent.Meaning.CHARACTER_STATE));
					}
				} 
				if (!getStreamDataProvider().getUpcomingEvents().isEmpty()) {  // Otherwise the previously constructed meta event will be returned, indicating an empty symbols subcommand.
					result = getStreamDataProvider().getUpcomingEvents().poll();
				}
			}
		}
		return result;
	}
	
	
	@Override
	protected JPhyloIOEvent doReadNextEvent() throws Exception {
		PeekReader reader = getStreamDataProvider().getDataReader();
		try {
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			if (reader.peekChar() != COMMAND_END) {
				MetaInformationEvent event = getStreamDataProvider().readKeyValueMetaInformation(KEY_PREFIX);
				return processSubcommand(event);
			}
			else {
				reader.skip(1); // Consume ';'.
				setAllDataProcessed(true);
				return null;
			}
		}
		catch (EOFException e) {
			throw new IOException("Unexpected end of file in " + getCommandName() + " command.");  //TODO Replace by ParseException
		}
	}
}
