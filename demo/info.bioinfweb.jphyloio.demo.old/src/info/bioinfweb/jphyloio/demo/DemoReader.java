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
package info.bioinfweb.jphyloio.demo;


import java.util.List;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;



public class DemoReader {
	private static String currentSequenceName = null;
	
	public static void readNeXML(DemoModel collection, NeXMLEventReader reader) throws Exception {		
		while (reader.hasNextEvent()) {
			JPhyloIOEvent event = reader.next();
			addSequenceToElementCollection(collection, event);
		}
	}
	
	
	public static void readFasta(DemoModel collection, JPhyloIOEventReader reader) throws Exception {
		reader.setMaxTokensToRead(10);
		if (reader.hasNextEvent() && reader.next().getType().equals(new EventType(EventContentType.DOCUMENT, EventTopologyType.START))) {
			if (reader.hasNextEvent() && reader.next().getType().equals(new EventType(EventContentType.ALIGNMENT, EventTopologyType.START))) {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
					if (!event.getType().equals(new EventType(EventContentType.ALIGNMENT, EventTopologyType.END))) {							
						addSequenceToElementCollection(collection, event);
					}
				}
			}
		}		
	}
	
	
	public static void addSequenceToElementCollection(DemoModel collection, JPhyloIOEvent event) {
		if (event.getType().getContentType().equals(EventContentType.SEQUENCE) && event.getType().getTopologyType().equals(EventTopologyType.START)) {
			LinkedOTUOrOTUsEvent sequenceStartEvent = event.asLinkedOTUOrOTUsEvent();
			currentSequenceName = sequenceStartEvent.getLabel();
		}
		else if (event.getType().getContentType().equals(EventContentType.SEQUENCE_TOKENS)) {
			SequenceTokensEvent tokensEvent = event.asSequenceTokensEvent();
			List<String> sequence = collection.getSequence(currentSequenceName);
			
			for (int i = 0; i < tokensEvent.getCharacterValues().size(); i++) {
				sequence.add(tokensEvent.getCharacterValues().get(i));
			}
			collection.getAlignmentData().put(currentSequenceName, sequence);
		}
	}
}
