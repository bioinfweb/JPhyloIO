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


import info.bioinfweb.jphyloio.AbstractEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;

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

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.semanticweb.owlapi.io.XMLUtils;



/**
 * Implements shared functionality for reading XML formats.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public abstract class AbstractXMLEventReader<P extends XMLReaderStreamDataProvider<? extends AbstractXMLEventReader<P>>>
		extends AbstractEventReader<P> implements JPhyloIOXMLEventReader {
	
	public static final String INTERNAL_USE_NAMESPACE = "http://bioinfweb.info/xmlns/JPhyloIO/internalUse";	
	public static final QName TAG_PARENT_OF_ROOT = new QName(INTERNAL_USE_NAMESPACE, "root");
	
	
	private Map<XMLElementReaderKey, XMLElementReader<P>> elementReaderMap = new HashMap<XMLElementReaderKey, XMLElementReader<P>>();
	private XMLEventReader xmlReader;
	private Stack<QName> encounteredTags = new Stack<QName>();
	
	private NamespaceContext namespaceContext = null;
	
	
	public AbstractXMLEventReader(File file, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		this(new FileReader(file), parameters);
	}

	
	public AbstractXMLEventReader(InputStream stream, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
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
	
	
	protected boolean isAllowDefaultNamespace() {
		return getParameters().getBoolean(ReadWriteParameterMap.KEY_ALLOW_DEFAULT_NAMESPACE, true);
	}
	
	
	protected void putElementReader(XMLElementReaderKey key, XMLElementReader<P> reader) {
		elementReaderMap.put(key, reader);
		if (isAllowDefaultNamespace()) {
			QName parentTag = null;
			if (key.getParentTag() != null) {
				parentTag = new QName(key.getParentTag().getLocalPart());
			}
			QName tag = null;
			if (key.getTagName() != null) {
				tag = new QName(key.getTagName().getLocalPart());
			}
			elementReaderMap.put(new XMLElementReaderKey(parentTag, tag, key.getXmlEventType()), reader);
		}
	}


	protected abstract void fillMap();
	
	
	@Override
	protected void readNextEvent() throws IOException {
		try {
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
						StartElement element = xmlEvent.asStartElement();
						elementTag = element.getName();						
						namespaceContext = element.getNamespaceContext();
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
					getStreamDataProvider().setParentName(parentTag.getLocalPart());
					getStreamDataProvider().setElementName(elementTag.getLocalPart());
				}
				
				XMLElementReader<P> elementReader = getElementReader(parentTag, elementTag, xmlEvent.getEventType());
				if (elementReader != null) {
					elementReader.readEvent(getStreamDataProvider(), xmlEvent);
				}

				if (xmlEvent.isStartElement()) {  // Should be done after elementReader.readEvent().
					getEncounteredTags().push(elementTag);
				}
			}
		}
		catch (XMLStreamException e) {
			throw new JPhyloIOReaderException("The underlying XML reader threw an exception, when trying to read the next event.", e);
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
	
	
	public QName qNameFromCURIE(String curie, StartElement element) throws JPhyloIOReaderException {
		String prefix = null;
		String localPart = null;
		String namespaceURI = null;
		QName qName = null;
		
		if (curie != null) {
			if (curie.contains(":")) {
				prefix = curie.substring(0, curie.indexOf(':'));
				localPart = curie.substring(curie.indexOf(':') + 1);				

				if (!XMLUtils.isNCName(prefix)) {
					prefix = null;
				}
				
				namespaceURI =  element.getNamespaceContext().getNamespaceURI(prefix);
				if (namespaceURI == null) { // prefix value was not defined
					if (prefix.equals(XMLReadWriteUtils.XSD_DEFAULT_PRE)) {
						namespaceURI = XMLConstants.W3C_XML_SCHEMA_NS_URI;
					}
					else {
						prefix = null;
					}					
				}				
			}
			
			if (prefix == null) {
				qName = new QName(element.getNamespaceContext().getNamespaceURI(""), curie); // if no prefix was specified or it was invalid, the default namespace is used
			}
			else {
				qName = new QName(namespaceURI, localPart, prefix);
			}
		}
		
		return qName;
	}
	
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected P createStreamDataProvider() {
		return (P)new XMLReaderStreamDataProvider(this);
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

	/**
	 * Returns the currently valid namespace context object or {@code null}, if the root element was not yet read.
	 * 
	 * @return the currently valid namespace context or {@code null}
	 */
	@Override
	public NamespaceContext getNamespaceContext() {
		return namespaceContext;
	}


	@Override
	public void close() throws IOException {
		super.close();
		try {
			getXMLReader().close();
		}
		catch (XMLStreamException e) {
			throw new JPhyloIOReaderException("The underlying XML reader throw an exception, when trying to close it.", e);
		}
	}
}
