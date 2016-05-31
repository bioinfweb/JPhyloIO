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
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandEventReader;



public class TreesBlockHandler extends AbstractNexusBlockHandler implements NexusBlockHandler, NexusConstants, ReadWriteConstants {
	public TreesBlockHandler() {
		super(new String[]{BLOCK_NAME_TREES});
	}


	@Override
	public void handleBegin(NexusReaderStreamDataProvider streamDataProvider) {
		streamDataProvider.getTreesTranslationTable().clear();  // Usually not necessary. Just to make sure, that no other classes left any data in it.
		streamDataProvider.getSharedInformationMap().put(NexusReaderStreamDataProvider.INFO_KEY_CURRENT_BLOCK_ID, 
				DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + streamDataProvider.getIDManager().createNewID());
	}

	
	@Override
	public void handleEnd(NexusReaderStreamDataProvider streamDataProvider) {
		streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE_NETWORK_GROUP));
		streamDataProvider.getTreesTranslationTable().clear();  // Clear for another possible upcoming TREES block.
	}


	@Override
	public void beforeCommand(NexusReaderStreamDataProvider streamDataProvider,	String commandName, NexusCommandEventReader commandReader) {
		ParameterMap map = streamDataProvider.getSharedInformationMap();
		if (!map.getBoolean(NexusReaderStreamDataProvider.INFO_KEY_BLOCK_START_EVENT_FIRED, false) 
				&& (commandName.equals(COMMAND_NAME_TREE))) {  // Fire start event, as soon as one of these commands is encountered.
			
			streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
					map.getString(NexusReaderStreamDataProvider.INFO_KEY_CURRENT_BLOCK_ID),	
					map.getString(NexusReaderStreamDataProvider.INFO_KEY_BLOCK_TITLE), streamDataProvider.getBlockLinks().get(BLOCK_NAME_TAXA)));  //TODO The default OTUs ID for TREES blocks without LINK commands does not seem to be returned here.
			map.put(NexusReaderStreamDataProvider.INFO_KEY_BLOCK_START_EVENT_FIRED, true);
		}
	}
}
