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


import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.formats.xml.receivers.AbstractXMLDataReceiver;

import java.io.IOException;
import java.util.Collections;
import java.util.Stack;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * This writer can be used by applications already implementing <i>SAX</i>-writing of metadata as an adapter to a 
 * {@link JPhyloIOEventWriter}. Instances of this class can be created directly inside a metadata writing method an an 
 * application's data adapter implementation, by passing the current event receiver to the constructor.
 * <p>
 * This writer does not manage namespaces of custom XML elements. The application needs to ensure that all used prefixes 
 * (either in any elements, attributes or character data) are properly declared within the custom <i>XML</i> 
 * (e.g. by adding {@link Namespace} events). Any methods of this writer changing the namespace mapping or obtaining information 
 * about it refer to the underlying writer. Note that if the default namespace is changed (either by setting a new default namespace or a completely 
 * new namespace context object) while this reader is on the top level of custom <i>XML</i>, it is possible that the literal meta 
 * end tag is written with the wrong default namespace (it would be written with a prefix then).
 * <p>
 * No end elements can be written by this writer, if the according start element was not written with this instance as well.
 * 
 * @author Sarah Wiechers 
 * @author Ben St&ouml;ver
 */
public class MetaXMLStreamWriter<P extends XMLWriterStreamDataProvider<? extends AbstractXMLEventWriter<P>>> implements XMLStreamWriter {
	private AbstractXMLDataReceiver<P> receiver;
	private XMLEventFactory factory = XMLEventFactory.newInstance();
	private Stack<StartElement> encounteredStartElements = new Stack<StartElement>();

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param receiver the event receiver to write the <i>JPhyloIO</i> events created by this writer to
	 */
	public MetaXMLStreamWriter(AbstractXMLDataReceiver<P> receiver) {
		super();
		this.receiver = receiver;
	}


	/**
	 * This method has no effect in this writer. Freeing resources of this writer is not
	 * necessary, since it just delegates to another writer.
	 */
	@Override
	public void close() throws XMLStreamException {}


	/**
	 * This method has no effect in this writer, since the underlying {@link JPhyloIOEventWriter} 
	 * does not implement a flush() method.
	 */
	@Override
	public void flush() throws XMLStreamException {}


	@Override
	public NamespaceContext getNamespaceContext() {
		return receiver.getStreamDataProvider().getWriter().getNamespaceContext();
	}


	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		return receiver.getStreamDataProvider().getWriter().getPrefix(uri);
	}


	@Override
	public Object getProperty(String name) throws IllegalArgumentException {
		return receiver.getStreamDataProvider().getWriter().getProperty(name);  // It is possible that implementation specific objects have properties that can be changed by the application in a way that our code does not work anymore
	}


	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		receiver.getStreamDataProvider().getWriter().setDefaultNamespace(uri);  // It is possible that this method is called on the top level, which would lead to the literal end tag to be written with the wrong default namespace (it would be written with a prefix then)
	}


	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		receiver.getStreamDataProvider().getWriter().setNamespaceContext(context);  // It is possible that this method is called on the top level, which would lead to the literal end tag to be written with the wrong default namespace (it would be written with a prefix then)
	}


	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		receiver.getStreamDataProvider().getWriter().setPrefix(prefix, uri);
	}


	@Override
	public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {		
		addContentEvent(factory.createAttribute(new QName(namespaceURI, localName, prefix), value));
	}


	@Override
	public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
		addContentEvent(factory.createAttribute(new QName(namespaceURI, localName), value));
	}


	@Override
	public void writeAttribute(String localName, String value) throws XMLStreamException {
		addContentEvent(factory.createAttribute(localName, value));
	}


	@Override
	public void writeCData(String data) throws XMLStreamException {
		addContentEvent(factory.createCData(data));
	}


	@Override
	public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
		addContentEvent(factory.createCharacters(new String(text, start, len)));
	}


	@Override
	public void writeCharacters(String text) throws XMLStreamException {
		addContentEvent(factory.createCharacters(text));		
	}


	@Override
	public void writeComment(String data) throws XMLStreamException {
		addContentEvent(factory.createComment(data));		
	}


	@Override
	public void writeDTD(String dtd) throws XMLStreamException {
		addContentEvent(factory.createDTD(dtd));  // Will not be written by most receivers writing custom XML
	}


	@Override
	public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
		addContentEvent(factory.createNamespace(namespaceURI));
	}


	/** 
	 * It depends on the implementation of XMLStreamWriter returned by {@link XMLOutputFactory#createXMLStreamWriter(java.io.Writer)} 
	 * if a self-closing tag or a separate start and end tag is written by this method.
	 */
	@Override
	public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		addContentEvent(factory.createStartElement(prefix, namespaceURI, localName));
		addContentEvent(factory.createEndElement(prefix, namespaceURI, localName));
	}


	/** 
	 * It depends on the implementation of XMLStreamWriter returned by {@link XMLOutputFactory#createXMLStreamWriter(java.io.Writer)} 
	 * if a self-closing tag or a separate start and end tag is written by this method.
	 */
	@Override
	public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
		addContentEvent(factory.createStartElement(new QName(namespaceURI, localName), Collections.emptyIterator(), Collections.emptyIterator()));
		addContentEvent(factory.createEndElement(new QName(namespaceURI, localName), Collections.emptyIterator()));
	}


	/** 
	 * It depends on the implementation of XMLStreamWriter returned by {@link XMLOutputFactory#createXMLStreamWriter(java.io.Writer)} 
	 * if a self-closing tag or a separate start and end tag is written by this method.
	 */
	@Override
	public void writeEmptyElement(String localName) throws XMLStreamException {
		addContentEvent(factory.createStartElement(new QName(localName), Collections.emptyIterator(), Collections.emptyIterator()));
		addContentEvent(factory.createEndElement(new QName(localName), Collections.emptyIterator()));
	}


	@Override
	public void writeEndDocument() throws XMLStreamException {
		addContentEvent(factory.createEndDocument());  // Will be ignored by most receivers writing custom XML
	}


	@Override
	public void writeEndElement() throws XMLStreamException {
		if (!encounteredStartElements.isEmpty()) {
			addContentEvent(factory.createEndElement(encounteredStartElements.pop().getName(), Collections.emptyIterator()));
		}
		else {
			throw new XMLStreamException("One more end element than start element was found.");
		}
	}


	@Override
	public void writeEntityRef(String name) throws XMLStreamException { //TODO Why is it possible to give an entity declaration here? -> To report unparsed entities
		addContentEvent(factory.createEntityReference(name, null));  // Most receivers writing entity references do not need an EntityDeclaration, since these can not be written at this position of the document
	}


	@Override
	public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
		addContentEvent(factory.createNamespace(prefix, namespaceURI));		
	}


	@Override
	public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
		addContentEvent(factory.createProcessingInstruction(target, data));
	}


	@Override
	public void writeProcessingInstruction(String target) throws XMLStreamException {
		addContentEvent(factory.createProcessingInstruction(target, null));
	}


	@Override
	public void writeStartDocument() throws XMLStreamException {
		addContentEvent(factory.createStartDocument());  // Will be ignored by most receivers writing custom XML	
	}


	@Override
	public void writeStartDocument(String encoding, String version) throws XMLStreamException {
		addContentEvent(factory.createStartDocument(encoding, version));  // Will be ignored by most receivers writing custom XML
	}


	@Override
	public void writeStartDocument(String version) throws XMLStreamException {
		addContentEvent(factory.createStartDocument(null, version));  // Will be ignored by most receivers writing custom XML
	}


	@Override
	public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
		StartElement element = factory.createStartElement(new QName(namespaceURI, localName, prefix), Collections.emptyIterator(), Collections.emptyIterator());
		encounteredStartElements.push(element);
		addContentEvent(element);
	}


	@Override
	public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
		StartElement element = factory.createStartElement(new QName(namespaceURI, localName), Collections.emptyIterator(), Collections.emptyIterator());
		encounteredStartElements.push(element);
		addContentEvent(element);
	}


	@Override
	public void writeStartElement(String localName) throws XMLStreamException {
		StartElement element = factory.createStartElement(new QName(localName), Collections.emptyIterator(), Collections.emptyIterator());
		encounteredStartElements.push(element);
		addContentEvent(element);
	}
	
	
	private void addContentEvent(XMLEvent event) throws XMLStreamException {
		try {
			receiver.add(new LiteralMetadataContentEvent(event, false));
		}
		catch (IOException e) {
			if (e.getCause() != null) {
				throw new XMLStreamException(e.getCause());  // XMLStreamException was already able to do this in Java 6
			}
			else {
				throw new XMLStreamException("The created content event could not be added to the data receiver.");
			}
		}
	}
}