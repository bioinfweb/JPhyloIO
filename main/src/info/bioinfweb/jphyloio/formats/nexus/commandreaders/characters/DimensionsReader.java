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

import info.bioinfweb.jphyloio.AbstractBufferedReaderBasedEventReader.KeyValueInformation;
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.AbstractKeyValueCommandReader;



public class DimensionsReader extends AbstractKeyValueCommandReader implements NexusConstants {
	public static final String KEY_PREFIX = "info.bioinfweb.jphyloio.formats.nexus.dimensions.";
	
	public static final String INFO_KEY_NTAX = "info.bioinfweb.jphyloio.nexus.ntax";
	public static final String INFO_KEY_CHAR = "info.bioinfweb.jphyloio.nexus.ntax";
	
	
	public DimensionsReader(NexusStreamDataProvider nexusDocument) {
		super("Dimensions", new String[]{BLOCK_NAME_CHARACTERS, BLOCK_NAME_UNALIGNED, BLOCK_NAME_DATA}, nexusDocument, KEY_PREFIX);
		//TODO In the UNALIGNED block NCHAR is invalid.
	}


	@Override
	protected boolean processSubcommand(KeyValueInformation info) throws IOException {
		long longValue = Long.MIN_VALUE;
		try {
			longValue = Long.parseLong(info.getValue());
		}
		catch (NumberFormatException e) {}  // Nothing to do.
		
		String key = info.getOriginalKey().toUpperCase();
		if (longValue > 0) {
			if (DIMENSIONS_SUBCOMMAND_NTAX.equals(key)) {
				getStreamDataProvider().getSharedInformationMap().put(INFO_KEY_NTAX, longValue);
			}
			else if (DIMENSIONS_SUBCOMMAND_NCHAR.equals(key)) {
				getStreamDataProvider().getSharedInformationMap().put(INFO_KEY_CHAR, longValue);
			}
			
			getStreamDataProvider().getUpcomingEvents().add(new MetaInformationEvent(info.getKey(), null, info.getValue(), longValue));  //TODO Does a type need to specified, if an object value is provided?
		}
		else if (DIMENSIONS_SUBCOMMAND_NTAX.equals(key) || DIMENSIONS_SUBCOMMAND_NCHAR.equals(key)) {
			throw new JPhyloIOReaderException("\"" + info.getValue() + "\" is not a valid positive integer. Only positive integer "
					+ "values are valid for NTAX or NCHAR in the Nexus DIMENSIONS command.", getStreamDataProvider().getDataReader());  //TODO Is the position of the reader too far behind?
		}
		else {  // Possible unknown subcommand
			getStreamDataProvider().getUpcomingEvents().add(new MetaInformationEvent(info.getKey(), null, info.getValue()));
		}
		getStreamDataProvider().getUpcomingEvents().add(
				new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
		
		return true;  // An event is added to the queue in every case.
	}


	@Override
	protected boolean addStoredEvents() {
		return false;  // This reader does not store any events.
	}
}
