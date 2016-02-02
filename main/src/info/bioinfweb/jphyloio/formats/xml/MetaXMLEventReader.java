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


import info.bioinfweb.jphyloio.events.MetaXMLEvent;

import java.util.NoSuchElementException;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;



/**
 * Adapter class that allows reading a sequence of {@link MetaXMLEvent}s using a {@link XMLStreamReader}.
 * 
 * @author Ben St&ouml;ver
 */
public class MetaXMLEventReader implements XMLEventReader {
	private AbstractXMLEventReader jPhyloIOEventReader;
	private boolean endReached = false;
	
	
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
	public String getElementText() throws XMLStreamException {
		// TODO Delegate to underlying XMLStreamReader and make sure reading behind the end of the metadata is not possible.
		return null;
	}

	
	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public boolean hasNext() {
		// TODO Delegate to underlying XMLStreamReader and make sure reading behind the end of the metadata is not possible.
		return false;
	}
	

	@Override
	public XMLEvent nextEvent() throws XMLStreamException {
		if (!endReached) {
			// TODO Delegate to underlying XMLStreamReader and make sure reading behind the end of the metadata is not possible.
			return null;
		}
		else {
			throw new NoSuchElementException("The end of this XML metadata stream was already reached.");
		}
	}
	

	@Override
	public XMLEvent nextTag() throws XMLStreamException {
		// TODO Delegate to underlying XMLStreamReader and make sure reading behind the end of the metadata is not possible.
		return null;
	}
	

	@Override
	public XMLEvent peek() throws XMLStreamException {
		if (!endReached) {
			// TODO Delegate to underlying XMLStreamReader and make sure reading behind the end of the metadata is not possible.
			return null;
		}
		else {
			return null;
		}
	}
}
