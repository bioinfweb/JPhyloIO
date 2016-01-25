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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.trees;


import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;

import java.io.EOFException;



public class TranslateReader extends AbstractNexusCommandEventReader implements NexusConstants, ReadWriteConstants {
	public TranslateReader(NexusStreamDataProvider nexusDocument) {
		super("Translate", new String[]{BLOCK_NAME_TREES}, nexusDocument);
	}

	
	@Override
	protected boolean doReadNextEvent() throws Exception {
		PeekReader reader = getStreamDataProvider().getDataReader();
		getStreamDataProvider().consumeWhiteSpaceAndComments();
		try {
			while (reader.peekChar() != COMMAND_END) {
				String key = getStreamDataProvider().readNexusWord();
				getStreamDataProvider().consumeWhiteSpaceAndComments();
				String value = getStreamDataProvider().readNexusWord();
				getStreamDataProvider().consumeWhiteSpaceAndComments();
				getStreamDataProvider().getTreesTranslationTable().add(key, value);  //TODO Make sure the table is emptied at the end (or beginning) of each trees block.
				
				if (reader.peekChar() == ELEMENT_SEPARATOR) {
					reader.read();  // Skip ELEMENT_SEPARATOR.
				}
			}
			
			reader.read();  // Skip COMMAND_END.
			setAllDataProcessed(true);
			return false;  // This reader does not produce any events.
		}
		catch (EOFException e) {
			throw new JPhyloIOReaderException("Unexpected end of file in " + getCommandName() + " command.", reader);
		}
	}
}
