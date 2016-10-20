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
package info.bioinfweb.jphyloio.formats.xml;


import java.io.IOException;

import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.formats.xml.receivers.AbstractXMLDataReceiver;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;



public class MetaXMLEventWriter implements XMLEventWriter {
	AbstractXMLDataReceiver receiver; //TODO add generics?
	
	
	public MetaXMLEventWriter(AbstractXMLDataReceiver receiver) {
		super();
		this.receiver = receiver;
	}
	

	@Override
	public void add(XMLEvent event) throws XMLStreamException { //TODO ignore document start and end, attributes of start doc are not processed (not possible at this time)
		try {
			receiver.add(new LiteralMetadataContentEvent(event, false)); // does not need to buffer, allowed to have separate charactere vents
		} 
		catch (IOException e) {			
			throw new XMLStreamException("The current event could not be added to the data receiver.");
		}
	}
	

	@Override
	public void add(XMLEventReader xmlReader) throws XMLStreamException { //TODO use add() from above
		while (xmlReader.hasNext()) {
			XMLEvent event = xmlReader.nextEvent();
			
			try {
				if (xmlReader.hasNext()) {
					receiver.add(new LiteralMetadataContentEvent(event, true));
				}
				else {
					receiver.add(new LiteralMetadataContentEvent(event, false));
				}
			}
			catch (IOException e) {			
				throw new XMLStreamException("The current event could not be added to the data receiver.");
			}
		}
	}
	

	@Override
	public void close() throws XMLStreamException {}  // siehe reader, application need tow rite literal start and end events on its own beacuse necessary info sucha s predicate are nota vailable here

	
	@Override
	public void flush() throws XMLStreamException {} //kann keien ahben, solange writer keine hat (geht im moemnt nicht)

	
	@Override
	public NamespaceContext getNamespaceContext() {
		// TODO Auto-generated method stub		
		return null;
	}
	

	@Override
	public String getPrefix(String arg0) throws XMLStreamException {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void setDefaultNamespace(String arg0) throws XMLStreamException {
		// TODO Auto-generated method stub
	}
	

	@Override
	public void setNamespaceContext(NamespaceContext arg0) throws XMLStreamException {
		// TODO Auto-generated method stub
	}
	

	@Override
	public void setPrefix(String arg0, String arg1) throws XMLStreamException {
		// TODO Auto-generated method stub		
	}
}
