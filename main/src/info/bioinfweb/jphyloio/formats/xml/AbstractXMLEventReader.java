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


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.jphyloio.AbstractEventReader;
import info.bioinfweb.jphyloio.StreamDataProvider;
import info.bioinfweb.jphyloio.formats.nexml.AbstractNeXMLElementReader;



/**
 * Implements shared functionality for reading XML formats.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractXMLEventReader extends AbstractEventReader {	
	public static final String NAMESPACE_BIOINFWEB = "http://bioinfweb.info/JPhyloIO/technical";	
	public static final QName TAG_ROOT = new QName(NAMESPACE_BIOINFWEB, "root");
	
	private Map<XMLElementReaderKey, XMLElementReader> elementReaderMap = createMap();
	private XMLEventReader xmlReader;
	private Stack<QName> encounteredTags = new Stack<QName>();
	
	
	public AbstractXMLEventReader(boolean translateMatchToken, File file) throws IOException, XMLStreamException {
		this(translateMatchToken, new FileReader(file));
	}

	
	public AbstractXMLEventReader(boolean translateMatchToken, InputStream stream) throws IOException, XMLStreamException {
		this(translateMatchToken, new InputStreamReader(stream));
	}

	
	public AbstractXMLEventReader(boolean translateMatchToken, XMLEventReader xmlReader) {
		super(translateMatchToken);
		this.xmlReader = xmlReader;
	}
	
	
	public AbstractXMLEventReader(boolean translateMatchToken, int maxTokensToRead, XMLEventReader xmlReader) {
		super(translateMatchToken, maxTokensToRead);
		this.xmlReader = xmlReader;
	}

	
	public AbstractXMLEventReader(boolean translateMatchToken, Reader reader) throws IOException, XMLStreamException {
		super(translateMatchToken);
		if (!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}
		this.xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);		
	}
	
	protected abstract Map<XMLElementReaderKey, XMLElementReader> createMap();
	
	
	@Override
	protected void readNextEvent() throws Exception {
		while (getXMLReader().hasNext() && getUpcomingEvents().isEmpty()) {
			XMLEvent xmlEvent = getXMLReader().nextEvent();
			QName parentTag = null;
			
			QName elementTag = null;
			switch (xmlEvent.getEventType()) {
				case XMLStreamConstants.START_DOCUMENT:
					elementTag = null;
					break;
				case XMLStreamConstants.END_DOCUMENT:
					elementTag = null;
					break;
				case XMLStreamConstants.START_ELEMENT:
					elementTag = xmlEvent.asStartElement().getName();
					break;
				case XMLStreamConstants.END_ELEMENT:
					getEncounteredTags().pop();
					elementTag = xmlEvent.asEndElement().getName();
					break;
				default: 
					break;  // Nothing to do.
			}

			if (!getEncounteredTags().isEmpty()) {
				parentTag = getEncounteredTags().peek();
			}
			else {
				parentTag = TAG_ROOT;
			}		
			
			if (xmlEvent.isStartElement()) {
				getEncounteredTags().push(xmlEvent.asStartElement().getName());
			}

			XMLElementReader elementReader = getElementReader(parentTag, elementTag, xmlEvent.getEventType());
			if (elementReader != null) {
				elementReader.readEvent(getStreamDataProvider(), xmlEvent);
			}			
		}
	}
	
	
	protected XMLElementReader getElementReader(QName parentTag, QName elementTag, int eventType) {
		XMLElementReader result = elementReaderMap.get(new XMLElementReaderKey(parentTag, elementTag, eventType));
		if (result == null) {
			result = elementReaderMap.get(new XMLElementReaderKey(null, elementTag, eventType));
			if (result == null) {
				result = elementReaderMap.get(new XMLElementReaderKey(parentTag, null, eventType));
				if (result == null) {
					result = elementReaderMap.get(new XMLElementReaderKey(null, null, eventType));
				}
			}
		}
		return result;
	}
	
	
	@Override
	protected XMLStreamDataProvider createStreamDataProvider() {
		return new XMLStreamDataProvider(this);
	}
	
	
	@Override
	protected XMLStreamDataProvider getStreamDataProvider() {
		return (XMLStreamDataProvider)super.getStreamDataProvider(); //TODO can I do that like this?
	}


	protected XMLEventReader getXMLReader() {
		return xmlReader;
	}
	

	protected Stack<QName> getEncounteredTags() {
		return encounteredTags;
	}
	
	
	@Override
	public void close() throws Exception {
		super.close();
		getXMLReader().close();
	}	
}
