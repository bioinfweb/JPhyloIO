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


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.push.JPhyloIOEventListener;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;



/**
 * Adapter class that allows reading a sequence of {@link LiteralMetadataContentEvent}s using an {@link XMLStreamReader}.
 * 
 * Since it is registered which events are read from the event stream, it is possible to read only a part of the 
 * custom XML tree with this reader, while the rest is read using the original {@link JPhyloIOEventReader}.
 * 
 * @author Ben St&ouml;ver
 */
public class MetaXMLEventReader implements XMLEventReader {
	private AbstractXMLEventReader jPhyloIOEventReader;
	private boolean endReached = false;
	private boolean startDocumentFired;
	private boolean endDocumentFired;
	
	
	private class MetaEventListener implements JPhyloIOEventListener { //TODO make public which events were fired (e.g. to use this info in LibrAlign)?
		@Override
		public void processEvent(JPhyloIOEventReader source, JPhyloIOEvent event) throws IOException {
			if (source.peek().getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.END))) {
				setEndReached();
			}
		}
	}
	
	
	public MetaXMLEventReader(AbstractXMLEventReader jPhyloIOEventReader) {
		super();
		this.jPhyloIOEventReader = jPhyloIOEventReader;
	}
	
	
	protected void setEndReached() {
		endReached = true;
	}


	@Override
	public Object next() throws NoSuchElementException {
		try {
			return nextEvent();
		}
		catch (XMLStreamException e) {
			throw new NoSuchElementException(e.getLocalizedMessage());
		}
	}

	
	@Override
	public void remove() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("A passed event stream cannot be modified.");
	}


	/**
	 * This method has no effect in this reader. Both the <i>JPhyloIO</i> and the XML event stream will
	 * still be open, if they were before calling this method. Freeing resources of this reader is not
	 * necessary, since it just delegates to another reader.
	 */
	@Override
	public void close() throws XMLStreamException {}
	
	
	@Override
	public String getElementText() throws XMLStreamException { //TODO based on nextEvent() of this class an abstract implementation XML Event Reader can maybe be used here
		StringBuffer content = new StringBuffer();
		
		//TODO check if start event was the last event
		
		while (hasNext() && (peek().getEventType() == XMLStreamConstants.CHARACTERS)) {
			content.append(nextEvent().asCharacters().getData());
		}
				
		return content.toString();
	}

	
	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return jPhyloIOEventReader.getXMLReader().getProperty(name); //TODO return null, because this implementation does not have any specific properties?
	}
	

	@Override
	public boolean hasNext() {
		boolean result = false;
		
		try {
			result = (peek() != null);
		} 
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	

	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		XMLEvent result = null;
		if (!endReached) {
			if (jPhyloIOEventReader.getPreviousEvent().getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START)) && !startDocumentFired) {
				result = XMLEventFactory.newInstance().createStartDocument();
				startDocumentFired = true;
			}
			else {
				LiteralMetadataContentEvent contentEvent;
				
				try {
					contentEvent = jPhyloIOEventReader.next().asLiteralMetadataContentEvent();  // Any other event type is not allowed between a literal meta start and end
					
					if (contentEvent.hasXMLEventValue()) {
						result = contentEvent.getXMLEvent();
					}
					else {
						//TODO throw according exception
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		else if (!endDocumentFired) {
			result = XMLEventFactory.newInstance().createEndDocument();
			endDocumentFired = true;
		}
		else {
			throw new NoSuchElementException("The end of this XML metadata stream was already reached.");
		}		
		
		return result;
	}
	

	@Override
	public XMLEvent nextTag() throws XMLStreamException { //TODO consider content events with only whitespace content?
		if (peek().getEventType() == XMLStreamConstants.START_ELEMENT) {  // No content events containing only whitespace are generated by any JPhyloIOEventReader
			return nextEvent();
		}
		else {
			throw new XMLStreamException("An element that could not be skipped was encountered before the next start element.");
		}
	}
	

	@Override
	public XMLEvent peek() throws XMLStreamException {
		XMLEvent result = null;
		
		if (!endReached) {
			JPhyloIOEvent nextEvent;
			
			try {
				nextEvent = jPhyloIOEventReader.peek();
				
				if (nextEvent.getType().getContentType().equals(EventContentType.META_LITERAL_CONTENT)) {
					if (nextEvent.asLiteralMetadataContentEvent().hasXMLEventValue()) {
						result = nextEvent.asLiteralMetadataContentEvent().getXMLEvent();
					}
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}			
		}
		
		return result;
	}
}
