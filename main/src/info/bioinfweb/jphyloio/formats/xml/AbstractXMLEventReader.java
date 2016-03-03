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
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import info.bioinfweb.jphyloio.AbstractEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.type.EventContentType;



/**
 * Implements shared functionality for reading XML formats.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractXMLEventReader<P extends XMLStreamDataProvider<? extends AbstractXMLEventReader<P>>>
		extends AbstractEventReader<P> {
	
	public static final String INTERNAL_USE_NAMESPACE = "http://bioinfweb.info/xmlns/JPhyloIO/internalUse";	
	public static final QName TAG_PARENT_OF_ROOT = new QName(INTERNAL_USE_NAMESPACE, "root");
	
	private Map<XMLElementReaderKey, XMLElementReader<P>> elementReaderMap = new HashMap<XMLElementReaderKey, XMLElementReader<P>>();
	private XMLEventReader xmlReader;
	private Stack<QName> encounteredTags = new Stack<QName>();
	
	
	public AbstractXMLEventReader(File file, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		this(new FileReader(file), parameters);
	}

	
	public AbstractXMLEventReader(InputStream stream, ReadWriteParameterMap parameters) 
			throws IOException, XMLStreamException {
		
		this(new InputStreamReader(stream), parameters);
	}

	
	public AbstractXMLEventReader(XMLEventReader xmlReader, ReadWriteParameterMap parameters) {
		super(parameters, parameters.getMatchToken());
		this.xmlReader = xmlReader;
		fillMap();
	}

	
	public AbstractXMLEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(parameters, parameters.getMatchToken());
		if (!(reader instanceof BufferedReader)) {
			reader = new BufferedReader(reader);
		}
		this.xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);
		fillMap();
	}
	
	
	protected abstract void fillMap();
	
	
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
				parentTag = TAG_PARENT_OF_ROOT;
			}		
			
			if (xmlEvent.isStartElement()) {
				QName elementName = xmlEvent.asStartElement().getName();
				getEncounteredTags().push(elementName);
				getStreamDataProvider().setParentName(parentTag.getLocalPart());
				getStreamDataProvider().setElementName(elementName.getLocalPart());
			}
			
			XMLElementReader<P> elementReader = getElementReader(parentTag, elementTag, xmlEvent.getEventType());
			if (elementReader != null) {
				elementReader.readEvent(getStreamDataProvider(), xmlEvent);
			}			
		}
	}
	
	
	protected XMLElementReader<P> getElementReader(QName parentTag, QName elementTag, int eventType) {
		XMLElementReader<P> result = elementReaderMap.get(new XMLElementReaderKey(parentTag, elementTag, eventType));
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
	
	
	protected String getID(String id, EventContentType type) {
		String result = id;
		if ((result == null) || result.equals("")) {
			if (type.equals(EventContentType.ALIGNMENT)) {
				result = DEFAULT_MATRIX_ID_PREFIX;
			}
			else if (type.equals(EventContentType.TOKEN_SET_DEFINITION)) {
				result = DEFAULT_TOKEN_SET_ID_PREFIX;
			}
			else if (type.equals(EventContentType.NODE)) {
				result = DEFAULT_NODE_ID_PREFIX;
			}
			else if (type.equals(EventContentType.EDGE)) {
				result = DEFAULT_EDGE_ID_PREFIX;
			}
			else if (type.equals(EventContentType.TREE)) {
				result = DEFAULT_TREE_ID_PREFIX;
			}
		}
		result += getStreamDataProvider().getIDManager().createNewID();
		return result;
	}
	
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected P createStreamDataProvider() {
		return (P)new XMLStreamDataProvider(this);
	}
	
	
	protected XMLEventReader getXMLReader() {
		return xmlReader;
	}
	

	protected Stack<QName> getEncounteredTags() {
		return encounteredTags;
	}
	
	
	protected Map<XMLElementReaderKey, XMLElementReader<P>> getElementReaderMap() {
		return elementReaderMap;
	}


	@Override
	public void close() throws Exception {
		super.close();
		getXMLReader().close();
	}
}
