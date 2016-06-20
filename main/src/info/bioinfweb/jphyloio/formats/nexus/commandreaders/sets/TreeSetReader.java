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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.sets;


import java.io.IOException;

import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusReaderStreamDataProvider;



public class TreeSetReader extends AbstractNexusSetReader implements NexusConstants, ReadWriteConstants {
	// No default SETS block is ever specified using BlockTitleToIDMap.putDefaultBlockID() since there is no JPhyloIO event that models
	// a sets block.

	public TreeSetReader(NexusReaderStreamDataProvider streamDataProvider) {
		super(EventContentType.TREE_NETWORK_SET, COMMAND_NAME_TREE_SET, new String[]{BLOCK_NAME_SETS}, streamDataProvider);
	}

	
	@Override
	protected String getLinkedID() {
		return getStreamDataProvider().getCurrentLinkedBlockID(BLOCK_NAME_TREES);
	}


	@Override
	protected long getElementCount() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	protected void createEventsForInterval(long start, long end) throws IOException {
		// TODO Auto-generated method stub
		
	}


	@Override
	protected long elementIndexByName(String name) {
		// TODO Auto-generated method stub
		return 0;
	}	
}
