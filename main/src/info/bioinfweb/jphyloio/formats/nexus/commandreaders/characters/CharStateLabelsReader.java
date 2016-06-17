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


import java.io.IOException;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.CharacterDefinitionEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;



/**
 * Nexus reader for the {@code CHARSTATELABELS} command that produces {@link CharacterDefinitionEvent}s.
 * <p>
 * The character state (token) definitions that are also provided by the command are ignored in the current version. Future versions
 * may use this information to include it in token sets. 
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class CharStateLabelsReader extends AbstractNexusCommandEventReader implements NexusConstants, ReadWriteConstants {
	public CharStateLabelsReader(NexusReaderStreamDataProvider streamDataProvider) {
		super(COMMAND_NAME_CHAR_STATE_LABELS, new String[]{BLOCK_NAME_CHARACTERS, BLOCK_NAME_UNALIGNED, BLOCK_NAME_DATA}, streamDataProvider);
	}
	
	
	private boolean consumeStateNames() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		boolean result = reader.peekChar() == CHARACTER_NAME_STATES_SEPARATOR;
		if (result) {  // No character label, only state names.
			reader.skip(1);  // Consume '/'.
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			
			char c = reader.peekChar();
			while ((c != ELEMENT_SEPARATOR) && (c != COMMAND_END)) {
				String stateName = getStreamDataProvider().readNexusWord();  //TODO Use names for token sets in later versions.
				if (stateName.equals("")) {
					throw new JPhyloIOReaderException("The character '" + c + "' is invalid at this position inside a Nexus " + 
							COMMAND_NAME_CHAR_STATE_LABELS + " command.", reader);
				}
				getStreamDataProvider().consumeWhiteSpaceAndComments();
				c = reader.peekChar();
			}
		}
		return result;
	}

	
	@Override
	protected boolean doReadNextEvent() throws IOException {
		//TODO Buffer comment events
		boolean eventCreated = false;
		while (!eventCreated) {
			PeekReader reader = getStreamDataProvider().getDataReader();
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			if (reader.peek() == COMMAND_END) {
				getStreamDataProvider().getDataReader().skip(1);  // Consume ';'.
				setAllDataProcessed(true);
				return false;
			}
			else if ((reader.peek() == KEY_VALUE_SEPARATOR)) {  // This two characters would prevent reading a Nexus word and are illegal in this command.
				throw new JPhyloIOReaderException("The character " + getStreamDataProvider().getDataReader().readChar() + " is not allowed in the Nexus "
						+ COMMAND_NAME_CHAR_STATE_LABELS + " command.", reader);
			}
			else {
				long index = getStreamDataProvider().readPositiveInteger(-1);
				if (index < 0) {
					throw new JPhyloIOReaderException("Invalid character index (starting with '" + reader.peekChar() + "') found in Nexus " + 
							COMMAND_NAME_CHAR_STATE_LABELS + " command.", reader);
				}
				else {
					getStreamDataProvider().consumeWhiteSpaceAndComments();
					eventCreated = !consumeStateNames(); 
					if (eventCreated) {  // Character label not omitted.
						String characterName = getStreamDataProvider().readNexusWord();
						if (!characterName.equals("")) { 
							getStreamDataProvider().getCurrentEventCollection().add(new CharacterDefinitionEvent(DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + 
									getStreamDataProvider().getIDManager().createNewID(), characterName, index - 1));  // Nexus indices start with 1.
							getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.CHARACTER_DEFINITION));
							
							consumeStateNames();
						}
						else {
							throw new JPhyloIOReaderException("The character '" + reader.peekChar() + "' is invalid at this position inside a Nexus " + 
									COMMAND_NAME_CHAR_STATE_LABELS + " command.", reader);
						}
					}
					
					if (reader.peek() == ELEMENT_SEPARATOR) {
						reader.skip(1);  // Consume ','.
					}
				}
			}
		}
		return true;
	}
}
