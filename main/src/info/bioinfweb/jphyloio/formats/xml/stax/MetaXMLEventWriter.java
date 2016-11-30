/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.formats.xml.stax;


import info.bioinfweb.jphyloio.JPhyloIOEventWriter;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.BasicEventReceiver;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.formats.xml.XMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.xml.receivers.AbstractXMLDataReceiver;

import java.io.IOException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.XMLEvent;



/**
 * This writer can be used by applications already implementing iterator-based <i>StAX</i>-writing of metadata as an adapter 
 * to a {@link JPhyloIOEventWriter}. Instances of this class can be created directly inside a metadata writing method an an 
 * application's data adapter implementation, by passing the current event receiver to the constructor.
 * <p>
 * Attributes of a start document event are not processed, since it is not possible to write them at this position of the document.
 * <p>
 * This writer does not have the necessary information (e.g. predicate) to be able to write literal meta start and end events, 
 * therefore this has to be done by the application itself.
 * <p>
 * This writer does not manage namespaces of custom XML elements. The application needs to ensure that all used prefixes 
 * (either in any elements, attributes or character data) are properly declared within the custom <i>XML</i> 
 * (e.g. by adding {@link Namespace} events). Any methods of this writer changing the namespace mapping or obtaining information 
 * about it refer to the underlying writer. Note that if the default namespace is changed (either by setting a new default namespace or a completely 
 * new namespace context object) while this reader is on the top level of custom <i>XML</i>, it is possible that the literal meta 
 * end tag is written with the wrong default namespace (it would be written with a prefix then).
 * 
 * @author Sarah Wiechers
 * @author Ben St&ouml;ver
 */
public class MetaXMLEventWriter extends AbstractMetaXMLWriter implements XMLEventWriter {
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param receiver the event receiver to write the <i>JPhyloIO</i> events created by this writer to
	 */
	public MetaXMLEventWriter(JPhyloIOEventReceiver receiver, XMLStreamWriter underlyingXMLWriter) {
		super(receiver, underlyingXMLWriter);
	}
	

	@Override
	public void add(XMLEvent event) throws XMLStreamException {
		try {
			if ((event.getEventType() != XMLStreamConstants.START_DOCUMENT) && (event.getEventType() != XMLStreamConstants.END_DOCUMENT)) {
				getReceiver().add(new LiteralMetadataContentEvent(event, false));  // It is not necessary to buffer these events to find out if the content is continued, since content events containing characters are allowed to occur separately
			}
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
	

	@Override
	public void add(XMLEventReader xmlReader) throws XMLStreamException {
		while (xmlReader.hasNext()) {			
			add(xmlReader.nextEvent());
		}
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

	
	private XMLStreamWriter getUnderlyingXMLStreamWriter() {
		if (getReceiver() instanceof BasicEventReceiver) {
			BasicEventReceiver<?> basicReceiver = (BasicEventReceiver<?>)getReceiver(); 
			if (basicReceiver.getStreamDataProvider() instanceof XMLWriterStreamDataProvider) {
				return (XMLStreamWriter)((XMLWriterStreamDataProvider<?>)basicReceiver.getStreamDataProvider()).getEventWriter();
				// Casting the receiver to AbstractXMLDataReceiver instead does not work, since not all receivers provided by XML writers inherit from this class.
				//TODO This implementation might become problematic with third party JPhyloIO event writers that do not use this class hierarchy for the receiver or streamDataProvider. Maybe an architecture change providing a different way to access the XMLStreamWriter will some day be necessary to improve this.
			}
		}
		return null;
	}
	
	
	/**
	 * Returns the current namespace context of the underlying <i>XML</i> event writer, if
	 * 
	 * @see javax.xml.stream.XMLEventWriter#getNamespaceContext()
	 */
	@Override
	public NamespaceContext getNamespaceContext() {
		XMLStreamWriter writer = getUnderlyingXMLStreamWriter();
		if (writer != null) {
			return writer.getNamespaceContext();
		}
		else {
			return null;  //TODO Return empty context here. 
		}
	}
	

	@Override
	public String getPrefix(String uri) throws XMLStreamException {
		XMLStreamWriter writer = getUnderlyingXMLStreamWriter();
		if (writer != null) {
			return writer.getPrefix(uri);
		}
		else {
			return null;  //TODO Call getPrefix() from own namespace context
		}
	}
	

	@Override
	public void setDefaultNamespace(String uri) throws XMLStreamException {
		XMLStreamWriter writer = getUnderlyingXMLStreamWriter();
		if (writer != null) {
			writer.setDefaultNamespace(uri);  // It is possible that this method is called on the top level, which would lead to the literal end tag to be written with the wrong default namespace (it would be written with a prefix then)
		}
	}
	

	@Override
	public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
		XMLStreamWriter writer = getUnderlyingXMLStreamWriter();
		if (writer != null) {
			writer.setNamespaceContext(context);  // It is possible that this method is called on the top level, which would lead to the literal end tag to be written with the wrong default namespace (it would be written with a prefix then)
		}
	}
	

	@Override
	public void setPrefix(String prefix, String uri) throws XMLStreamException {
		XMLStreamWriter writer = getUnderlyingXMLStreamWriter();
		if (writer != null) {
			writer.setPrefix(prefix, uri);
		}
	}
}
