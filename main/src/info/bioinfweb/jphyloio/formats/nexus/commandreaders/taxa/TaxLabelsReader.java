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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.taxa;


import java.io.EOFException;
import java.io.IOException;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractNexusCommandEventReader;



public class TaxLabelsReader extends AbstractNexusCommandEventReader implements NexusConstants {
	public static final String INFO_KEY_TAXA_LIST = "info.bioinfweb.jphyloio.nexus.taxalist";
	public static final String INFO_KEY_TAXA_MAP = "info.bioinfweb.jphyloio.nexus.taxamap";
	
	
	private boolean beforeStart = true;
	
	
	public TaxLabelsReader(NexusStreamDataProvider nexusDocument) {
		super("TaxLabels", new String[]{BLOCK_NAME_TAXA}, nexusDocument);
	}

	
	@Override
	protected boolean doReadNextEvent() throws Exception {
		PeekReader reader = getStreamDataProvider().getDataReader();
		try {
			if (beforeStart) {
				beforeStart = false;
				getStreamDataProvider().getUpcomingEvents().add(new LabeledIDEvent(EventContentType.OTU_LIST, 
						JPhyloIOEventReader.DEFAULT_OTU_LIST_ID_PREFIX + getStreamDataProvider().getIDManager().createNewID(), null));
				return true;
			}
			else {
				getStreamDataProvider().consumeWhiteSpaceAndComments();
				char c = reader.peekChar();
				if (c == COMMAND_END) {
					getStreamDataProvider().getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.OTU_LIST, EventTopologyType.END));
					
					reader.skip(1);  // Consume ';'.
					setAllDataProcessed(true);
					return false;
				}
				else {
					String taxon = getStreamDataProvider().readNexusWord();
					String id = JPhyloIOEventReader.DEFAULT_OTU_ID_PREFIX + getStreamDataProvider().getIDManager().createNewID();
					
					getStreamDataProvider().getTaxaList().add(taxon);
					getStreamDataProvider().getTaxaToIDMap().put(taxon, id);
					
					getStreamDataProvider().getUpcomingEvents().add(new LabeledIDEvent(EventContentType.OTU, id, taxon));
					getStreamDataProvider().getUpcomingEvents().add(new ConcreteJPhyloIOEvent(EventContentType.OTU, EventTopologyType.END));
					return true;
				}
			}
		}
		catch (EOFException e) {
			throw new JPhyloIOReaderException("Unexpected end of file in " + getCommandName() + " command.", reader);
		}
	}
}
