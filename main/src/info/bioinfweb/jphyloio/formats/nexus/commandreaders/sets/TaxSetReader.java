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
import java.util.List;
import java.util.Map;

import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.SetElementEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusReaderStreamDataProvider;



public class TaxSetReader extends AbstractNexusSetReader implements NexusConstants, ReadWriteConstants {
	// No default SETS block is ever specified using BlockTitleToIDMap.putDefaultBlockID() since there is no JPhyloIO event that models
	// a sets block.

	public TaxSetReader(NexusReaderStreamDataProvider streamDataProvider) {
		super(EventContentType.OTU_SET, COMMAND_NAME_TAXON_SET, new String[]{BLOCK_NAME_SETS}, streamDataProvider);
	}

	
	@Override
	protected String getLinkedID() {
		return getStreamDataProvider().getCurrentLinkedBlockID(BLOCK_NAME_TAXA);
	}

	
	@Override
	protected long getElementCount() throws IOException {
		return getStreamDataProvider().getTaxaList(getLinkedID()).size();
	}

	
	@Override
	protected void createEventsForInterval(long start, long end) throws IOException {
		if (end > Integer.MAX_VALUE) {
			throw new JPhyloIOReaderException("This reader implementation does not support taxon sets in Nexus files that contain more than "
					+ Integer.MAX_VALUE + " elements.", getStreamDataProvider().getDataReader());
		}
		else {
			List<String> taxa = getStreamDataProvider().getTaxaList(getLinkedID());
			Map<String, String> namesToIDMap = getStreamDataProvider().getTaxaToIDMap(getLinkedID());
			for (int i = (int)start; i < end; i++) {
				getStreamDataProvider().getCurrentEventCollection().add(new SetElementEvent(namesToIDMap.get(taxa.get(i)), EventContentType.OTU));
				//TODO If map returns null, the error message from the constructor will be thrown, which may not be sufficient for the end user.
			}
		}
	}

	
	@Override
	protected long elementIndexByName(String name) {
		return getStreamDataProvider().getTaxaList(getLinkedID()).indexOf(name);  //TODO Increase performance by using an ID to index map?
	}
}
