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
import java.util.Map;

import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.nexus.NexusConstants;
import info.bioinfweb.jphyloio.formats.nexus.NexusReaderStreamDataProvider;



/**
 * Parser for the {@code CHARSET} command in the {@code SETS} block.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class CharSetReader extends AbstractNexusSetReader implements NexusConstants, ReadWriteConstants {
	// No default SETS block is ever specified using BlockTitleToIDMap.putDefaultBlockID() since there is no JPhyloIO event that models
	// a sets block.

	public CharSetReader(NexusReaderStreamDataProvider streamDataProvider) {
		super(EventContentType.CHARACTER_SET, COMMAND_NAME_CHAR_SET, new String[]{BLOCK_NAME_SETS}, streamDataProvider);
	}
	

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreateStartEvent(LinkedLabeledIDEvent event) {
		((Map<String, String>)getStreamDataProvider().getMap(NexusReaderStreamDataProvider.INFO_SET_NAME_TO_ID_MAP)).put(
				event.getLabel(), event.getID());
	}


	@Override
	protected long getElementCount() {
		Long result = getStreamDataProvider().getMatrixWidthsMap().get(getStreamDataProvider().getMatrixLink());  //TODO Catch any null values and throw according exceptions?
		if (result == null) {
			return -1;
		}
		else {
			return result;
		}
	}


	@Override
	protected String getLinkedID() {
		return getStreamDataProvider().getMatrixLink();  // Will link null if no CHARACTERS, DATA or UNALIGNED block was defined before.  //TODO Should an exception be thrown instead?  //TODO Sets for UNALIGNED blocks are not allowed according to the Nexus paper. (Is it a problem to support them anyway?)
	}
	

	@Override
	protected void createEventsForInterval(long start, long end) {
		getStreamDataProvider().getCurrentEventCollection().add(new CharacterSetIntervalEvent(start, end));
	}

	
	@SuppressWarnings("unchecked")
	@Override
	protected void createEventsForName(String name) throws IOException {
		//TODO Separate methods will be needed, for translating to an index and to an set ID. Possibly these methods should just return the according translation and not create any events?
		
		Long index = ((Map<String, Long>)getStreamDataProvider().getMap(NexusReaderStreamDataProvider.INFO_CHARACTER_NAME_TO_INDEX_MAP)).get(name);
		if (index != null) {
			
		}
		else {
			String setID = ((Map<String, String>)getStreamDataProvider().getMap(NexusReaderStreamDataProvider.INFO_SET_NAME_TO_ID_MAP)).get(name);
			if (setID != null) {
				
			}
			else {
				throw new JPhyloIOReaderException("", getStreamDataProvider().getDataReader());
			}
		}
	}
}
