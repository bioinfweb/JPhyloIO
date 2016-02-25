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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.all;


import java.io.EOFException;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;



/**
 * Reads the {@code TITLE} command of a Nexus block and stores the title in the shared information map of the
 * stream data provider under the key {@link NexusStreamDataProvider#INFO_KEY_BLOCK_TITLE}.
 * <p>
 * Note that the {@code TITLE} command is not part of the initial Nexus definition, but was used by Mesquite
 * as a custom command to allow references between blocks using the {@code LINK} command.
 * <p>
 * This reader is valid for all blocks, therefore {@link #getValidBlocks()} returns an empty collection.
 * <p>
 * See the documentation of {@link LinkReader} for a usage example the the {@code TITLE} and {@code LINK}
 * commands.  
 * 
 * @author Ben St&ouml;ver
 * @see LinkReader
 */
public class TitleReader extends AbstractNexusCommandEventReader implements NexusConstants, ReadWriteConstants {
	public TitleReader(NexusStreamDataProvider nexusDocument) {
		super(COMMAND_NAME_TITLE, new String[0], nexusDocument);  // Empty array makes this reader valid for all blocks.
	}

	
	@Override
	protected boolean doReadNextEvent() throws Exception {
		PeekReader reader = getStreamDataProvider().getDataReader();
		try {
			getStreamDataProvider().consumeWhiteSpaceAndComments();  //TODO Store comments until start event was fired by substituting the event queue.
			getStreamDataProvider().getSharedInformationMap().put(NexusStreamDataProvider.INFO_KEY_BLOCK_TITLE, 
					getStreamDataProvider().readNexusWord());
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			if (reader.readChar() == COMMAND_END) {
				setAllDataProcessed(true);
				return false;
			}
			else {
				throw new JPhyloIOReaderException("Expected end of Nexus TITLE command.", reader);
			}
		}
		catch (EOFException e) {
			throw new JPhyloIOReaderException("Unexpected end of file in " + getCommandName() + " command.", reader);
		}
	}
}