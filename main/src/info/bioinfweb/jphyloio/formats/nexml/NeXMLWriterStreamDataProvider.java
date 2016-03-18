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
package info.bioinfweb.jphyloio.formats.nexml;

import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class NeXMLWriterStreamDataProvider implements NeXMLConstants {
	private NeXMLEventWriter eventWriter;

	
	public NeXMLWriterStreamDataProvider(NeXMLEventWriter eventWriter) {
		super();
		this.eventWriter = eventWriter;
	}


	public NeXMLEventWriter getEventWriter() {
		return eventWriter;
	}
	
	
	public void writeLabeledIDAttributes(LabeledIDEvent event) throws XMLStreamException {
		getEventWriter().getWriter().writeAttribute(ATTR_ID.getLocalPart(), event.getID());  //TODO Add ID to set to ensure all IDs are unique. (Probably a task that should use resources to be added to the super class.)
		getEventWriter().getWriter().writeAttribute(ATTR_ABOUT.getLocalPart(), "#" + event.getID());
		if (event.hasLabel()) {
			getEventWriter().getWriter().writeAttribute(ATTR_LABEL.getLocalPart(), event.getLabel());
		}
	}
	
	
	public void writeLinkedOTUOrOTUsAttributes(LinkedOTUOrOTUsEvent event, QName linkAttribute, boolean forceOTULink) throws XMLStreamException {
		writeLabeledIDAttributes(event);
		if (event.isOTUOrOTUsLinked()) {
			getEventWriter().getWriter().writeAttribute(linkAttribute.getLocalPart(), event.getOTUOrOTUsID());
		}
		else if (forceOTULink) {
			//TODO Link UNDEFINED taxon, if an OTU shall be linked.
		}
		//TODO Linking OTUs is never optional, therefore one OTU (usually the one containing the UDEFINED taxon) should be linked.
	}
}
