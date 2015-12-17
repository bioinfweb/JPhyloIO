/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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
package info.bioinfweb.jphyloio.formats.nexml;


import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;



public abstract class NeXMLTagReader {
	public JPhyloIOEvent readEvent(NeXMLEventReader reader) throws Exception {
		if (reader.getXMLReader().hasNext()) {
			XMLEvent xmlEvent = reader.getXMLReader().nextEvent();
			if (xmlEvent.isEndElement()) {
				if (reader.getEncounteredTags().peek().equals(NeXMLConstants.TAG_NEXML)) {
					reader.getEncounteredTags().pop();
					return new ConcreteJPhyloIOEvent(EventType.DOCUMENT_END);
				}
				else if (reader.getEncounteredTags().peek().equals(NeXMLConstants.TAG_CHARACTERS)) {
					reader.getEncounteredTags().pop();
					return new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_END);
				}
				reader.getEncounteredTags().pop();
			}
			else {
				return readEventCore(reader, xmlEvent);
			}
		}
		return null;
	}
	
	
	protected abstract JPhyloIOEvent readEventCore(NeXMLEventReader reader, XMLEvent event) throws Exception;
}
