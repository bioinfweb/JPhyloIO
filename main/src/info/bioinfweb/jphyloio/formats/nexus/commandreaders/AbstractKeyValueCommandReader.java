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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders;


import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;

import java.io.EOFException;
import java.io.IOException;



public abstract class AbstractKeyValueCommandReader extends AbstractNexusCommandEventReader implements NexusConstants {
	private String keyPrefix;
	
	
	public AbstractKeyValueCommandReader(String commandName, String[] validBlocks, NexusStreamDataProvider nexusDocument,
			String keyPrefix) {
		
		super(commandName, validBlocks, nexusDocument);
		this.keyPrefix = keyPrefix;
	}


	protected String getKeyPrefix() {
		return keyPrefix;
	}


	protected abstract void processSubcommand(MetaInformationEvent event, String key, String value) throws IOException;
	
	
	@Override
	protected boolean doReadNextEvent() throws Exception {
		PeekReader reader = getStreamDataProvider().getDataReader();
		try {
			getStreamDataProvider().consumeWhiteSpaceAndComments();
			if (reader.peekChar() != COMMAND_END) {
				MetaInformationEvent event = getStreamDataProvider().readKeyValueMetaInformation(getKeyPrefix());
				processSubcommand(event, event.getKey().substring(getKeyPrefix().length()).toUpperCase(),  // Remove key prefix for comparison
						event.getStringValue().toUpperCase());
				return true;
			}
			else {
				reader.skip(1); // Consume ';'.
				setAllDataProcessed(true);
				return false;
			}
		}
		catch (EOFException e) {
			throw new IOException("Unexpected end of file in " + getCommandName() + " command.");  //TODO Replace by ParseException
		}
	}
}
