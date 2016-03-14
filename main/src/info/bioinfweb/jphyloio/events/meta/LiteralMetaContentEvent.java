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
package info.bioinfweb.jphyloio.events.meta;


import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import javax.xml.stream.events.XMLEvent;



/**
 * Instances of this class act as a wrapper for {@link XMLEvent}s and are nested within 
 * {@link EventContentType#META_INFORMATION}, if the content of this metainformation is
 * XML.  
 * 
 * @author Ben St&ouml;ver
 */
public class LiteralMetaContentEvent extends ConcreteJPhyloIOEvent {
	private XMLEvent xmlEvent;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param xmlEvent XML event to be wrapped 
	 */
	public LiteralMetaContentEvent(XMLEvent xmlEvent) {
		super(EventContentType.META_XML_CONTENT, EventTopologyType.SOLE);
		this.xmlEvent = xmlEvent;
	}


	/**
	 * Returns the XML event that is wrapped by this {@link JPhyloIOEvent}.
	 * 
	 * @return the XML event representing the next piece of XML
	 */
	public XMLEvent getXMLEvent() {
		return xmlEvent;
	}
}
