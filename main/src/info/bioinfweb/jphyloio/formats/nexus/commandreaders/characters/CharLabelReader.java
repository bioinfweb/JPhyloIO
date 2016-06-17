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



public class CharLabelReader extends AbstractNexusCommandEventReader implements NexusConstants, ReadWriteConstants {
	private long index = 0;
	
	
	public CharLabelReader(NexusReaderStreamDataProvider streamDataProvider) {
		super(COMMAND_NAME_CHAR_LABELS, new String[]{BLOCK_NAME_CHARACTERS, BLOCK_NAME_UNALIGNED, BLOCK_NAME_DATA}, streamDataProvider);
	}

	
	@Override
	protected boolean doReadNextEvent() throws IOException {
		PeekReader reader = getStreamDataProvider().getDataReader();
		if (reader.peek() == COMMAND_END) {
			getStreamDataProvider().getDataReader().skip(1);  // Consume ';'.
			setAllDataProcessed(true);
			return false;
		}
		else if ((reader.peek() == KEY_VALUE_SEPARATOR) || (reader.peek() == ELEMENT_SEPARATOR)) {  // This two characters would prevent reading a Nexus word and are illegal in this command.
			throw new JPhyloIOReaderException("The character " + getStreamDataProvider().getDataReader().readChar() + " is not allowed in the Nexus "
					+ COMMAND_NAME_CHAR_LABELS + " command.", reader);
		}
		else {
			String characterName = getStreamDataProvider().readNexusWord();
			if (!characterName.equals("")) { 
				getStreamDataProvider().getCurrentEventCollection().add(new CharacterDefinitionEvent(DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + 
						getStreamDataProvider().getIDManager().createNewID(), characterName, index));
				getStreamDataProvider().getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.CHARACTER_DEFINITION));
				index++;  // Labels are always listed consecutively in Nexus.
			}
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			return true;
		}
	}
}
