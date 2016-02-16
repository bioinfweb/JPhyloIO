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
package info.bioinfweb.jphyloio.formats.nexus.blockhandlers;


import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandEventReader;



public class CharactersDataUnalignedBlockHandler extends AbstractNexusBlockHandler 
		implements NexusBlockHandler, ReadWriteConstants, NexusConstants {
	
	public CharactersDataUnalignedBlockHandler() {
		super(new String[]{BLOCK_NAME_CHARACTERS, BLOCK_NAME_DATA, BLOCK_NAME_UNALIGNED});
	}


	@Override
	public void handleBegin(NexusStreamDataProvider streamDataProvider) {}

	
	@Override
	public void handleEnd(NexusStreamDataProvider streamDataProvider) {
		streamDataProvider.getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.ALIGNMENT, EventTopologyType.END));
	}


	@Override
	public void beforeCommand(NexusStreamDataProvider streamDataProvider,	String commandName, NexusCommandEventReader commandReader) {
		ParameterMap map = streamDataProvider.getSharedInformationMap();
		if (!map.getBoolean(NexusStreamDataProvider.INFO_KEY_BLOCK_START_EVENT_FIRED, false) 
				&& (commandName.equals(COMMAND_NAME_DIMENSIONS) || commandName.equals(COMMAND_NAME_FORMAT) 
						|| commandName.equals(COMMAND_NAME_MATRIX))) {  // Fire start event, as soon as one of these commands is encountered.
			
			streamDataProvider.getUpcomingEvents().add(new LinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, 
					DEFAULT_MATRIX_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
					map.getString(NexusStreamDataProvider.INFO_KEY_BLOCK_TITLE),
					streamDataProvider.getBlockLinks().get(BLOCK_NAME_TAXA)));
			map.put(NexusStreamDataProvider.INFO_KEY_BLOCK_START_EVENT_FIRED, true);
		}
	}
}
