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
package info.bioinfweb.jphyloio.formats.nexml.receivers;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * Provides a set of static methods to be used as static imports when needed
 * across multiple NeXML data receivers. Since not all of these classes allow direct access to a 
 * NeXMLWriterStreamDataProvider, an instance of it is a parameter of the static methods instead of an 
 * instance of AbstractNeXMLDataReceiver.
 * 
 * @author Sarah Wiechers
 * @since 0.0.0
 */
public class AbstractNeXMLDataReceiverMixin implements NeXMLConstants {
	
	
	public static void handleLiteralMeta(NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event) throws XMLStreamException, JPhyloIOWriterException {		
		streamDataProvider.setCurrentLiteralMetaStartEvent(event); // Must be buffered because data type and alternative string representation are only given in the following content events
		streamDataProvider.setCurrentLiteralMetaType(event.getSequenceType());
	}
	
	
	private static void writeLiteralMeta(NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event, QName datatype, String alternativeStringRepresentation) throws XMLStreamException, JPhyloIOWriterException {
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();
		String metaType = streamDataProvider.getNexPrefix() + ":" + TYPE_LITERAL_META;
		
		// Write literal meta start tag with attributes
		writer.writeStartElement(TAG_META.getLocalPart());
		streamDataProvider.writeLabeledIDAttributes(event, null);
		
		writer.writeAttribute(XMLReadWriteUtils.getXSIPrefix(streamDataProvider.getXMLStreamWriter()), ATTR_XSI_TYPE.getNamespaceURI(), 
				ATTR_XSI_TYPE.getLocalPart(), metaType);
		
		if (event.getPredicate().getURI() != null) { //TODO How to use alternative string representation if no QName is present?
			QName predicate = event.getPredicate().getURI();
			writer.writeAttribute(ATTR_PROPERTY.getLocalPart(), writer.getPrefix(predicate.getNamespaceURI()) + ":" + predicate.getLocalPart()); //TODO move to separate method and use for custom XML
		}
		
		if (datatype != null) { // Attribute is optional
			writer.writeAttribute(ATTR_DATATYPE.getLocalPart(), writer.getPrefix(datatype.getNamespaceURI()) + ":" + datatype.getLocalPart());
		}
		
		if (alternativeStringRepresentation != null) { // Attribute is optional
			writer.writeAttribute(ATTR_CONTENT.getLocalPart(), alternativeStringRepresentation);
		}
		
		// Write nested comments
		if (streamDataProvider.getCommentContent().length() != 0) {
			streamDataProvider.getXMLStreamWriter().writeComment(streamDataProvider.getCommentContent().toString());
			streamDataProvider.getCommentContent().delete(0, streamDataProvider.getCommentContent().length());
		}
	}
	
	
	public static void checkLiteralMeta (NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event) 
			throws XMLStreamException, JPhyloIOWriterException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			streamDataProvider.addToDocumentIDs(event.getID());
			
			if (event.getPredicate() != null) {
				streamDataProvider.setNamespacePrefix(event.getPredicate().getURI().getPrefix(), event.getPredicate().getURI().getNamespaceURI());
			}
		}
	}
	

	public static void handleLiteralContentMeta(NeXMLWriterStreamDataProvider streamDataProvider, ReadWriteParameterMap parameters, LiteralMetadataContentEvent event) throws XMLStreamException, JPhyloIOWriterException {		
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter(); 
		QName datatype = null;
		
		if (event.getOriginalType() != null) {
			datatype = event.getOriginalType().getURI(); //TODO determine datatype from object value if original type is null -> Map from java class to W3CXSConstant?
		}
		
		streamDataProvider.setLiteralContentIsContinued(event.isContinuedInNextEvent());
		 
		if (streamDataProvider.getCurrentLiteralMetaStartEvent() != null) {
			writeLiteralMeta(streamDataProvider, streamDataProvider.getCurrentLiteralMetaStartEvent(), datatype, event.getAlternativeStringValue());
			streamDataProvider.setCurrentLiteralMetaStartEvent(null);
		}
		
		switch (streamDataProvider.getCurrentLiteralMetaType()) {
			case SIMPLE:
				ObjectTranslator<?> translator = parameters.getObjectTranslatorFactory().getDefaultTranslator(datatype);
				if ((event.getObjectValue() != null) && (translator != null) && translator.hasStringRepresentation() 
						&& translator.getObjectClass().isInstance(event.getObjectValue().getClass())) {
					
					streamDataProvider.getXMLStreamWriter().writeCharacters(translator.javaToRepresentation(event.getObjectValue()));  //TODO Wrap possible ClassCastException
				}
				else if (event.getStringValue() != null) {
					writer.writeCharacters(event.getStringValue());
				}
				break;
			case XML:
				if (event.hasXMLEventValue()) {
					XMLEvent xmlContentEvent = event.getXMLEvent();
					
					switch (xmlContentEvent.getEventType()) {
						case XMLStreamConstants.START_ELEMENT:
							StartElement element = xmlContentEvent.asStartElement();
							writer.writeStartElement(element.getName().getPrefix(), element.getName().getLocalPart(), element.getName().getNamespaceURI());
							@SuppressWarnings("unchecked")
							Iterator<Attribute> attributes = element.getAttributes();
							while (attributes.hasNext()) {
								Attribute attribute = attributes.next();
								writer.writeAttribute(attribute.getName().getPrefix(), attribute.getName().getNamespaceURI(), attribute.getName().getLocalPart(), attribute.getValue());
							}
							break;
						case XMLStreamConstants.END_ELEMENT:
							writer.writeEndElement();
							break;
						case XMLStreamConstants.CHARACTERS:
							writer.writeCharacters(xmlContentEvent.asCharacters().getData());
							break;
						default: //TODO handle start and end document?
							break;
					}
				}
				break;
			default:
				break;
		}
	}
	
	
	public static void checkLiteralContentMeta(NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataContentEvent event) throws XMLStreamException {
		if (event.hasXMLEventValue()) {
			if (event.getXMLEvent().getEventType() == XMLStreamConstants.START_ELEMENT) {
				StartElement element = event.getXMLEvent().asStartElement();
				streamDataProvider.setNamespacePrefix(element.getName().getPrefix(), element.getName().getNamespaceURI());
			}
		}
		
		if ((event.getOriginalType() != null) && (event.getOriginalType().getURI() != null)) {
			streamDataProvider.setNamespacePrefix(event.getOriginalType().getURI().getPrefix(), event.getOriginalType().getURI().getNamespaceURI());
		}
	}
	
	
	public static void handleResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws ClassCastException, XMLStreamException, JPhyloIOWriterException {
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();		
		String metaType = streamDataProvider.getNexPrefix() + ":" + TYPE_RESOURCE_META;
		
		writer.writeStartElement(TAG_META.getLocalPart());		
		streamDataProvider.writeLabeledIDAttributes(event, event.getAbout());
		
		writer.writeAttribute(XMLReadWriteUtils.getXSIPrefix(streamDataProvider.getXMLStreamWriter()), ATTR_XSI_TYPE.getNamespaceURI(), 
				ATTR_XSI_TYPE.getLocalPart(), metaType);
		
		if (event.getRel().getURI() != null) { //TODO How to use alternative string representation if no QName is present?
			QName predicate = event.getRel().getURI();			
			writer.writeAttribute(ATTR_REL.getLocalPart(), writer.getPrefix(predicate.getNamespaceURI()) + ":" + predicate.getLocalPart());		
		}
		
		if (event.getHRef() != null) { // Attribute is optional
			writer.writeAttribute(ATTR_HREF.getLocalPart(), event.getHRef().toString());
		}		
	}
	
	
	public static void checkResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			streamDataProvider.addToDocumentIDs(event.getID());
			
			if (event.getRel() != null) {
				streamDataProvider.setNamespacePrefix(event.getRel().getURI().getPrefix(), event.getRel().getURI().getNamespaceURI());
			}
		}
	}
	
	
	public static void handleMetaEndEvent(NeXMLWriterStreamDataProvider streamDataProvider, JPhyloIOEvent event) throws IOException, XMLStreamException {
		// Write literal meta start if no content event was present
		if (event.getType().getContentType().equals(EventContentType.META_LITERAL)) {
			streamDataProvider.setCurrentLiteralMetaType(null);
			
			if (streamDataProvider.getCurrentLiteralMetaStartEvent() != null) {
				writeLiteralMeta(streamDataProvider, streamDataProvider.getCurrentLiteralMetaStartEvent(), null, null);
				streamDataProvider.setCurrentLiteralMetaStartEvent(null);
				
				if (streamDataProvider.isLiteralContentContinued()) { //TODO do the same for comments?
					throw new InconsistentAdapterDataException("A literal meta end event was encounterd, although the last literal meta content "
							+ "event was marked to be continued in a subsequent event.");
				}
			}
		}		
		
		streamDataProvider.getXMLStreamWriter().writeEndElement();
	}
	
	
	public static void handleComment(NeXMLWriterStreamDataProvider streamDataProvider, CommentEvent event) throws ClassCastException, XMLStreamException {
		String comment = event.getContent();
		
		if (!comment.isEmpty()) {
			streamDataProvider.getCommentContent().append(comment);
		}
		
		if (!event.isContinuedInNextEvent()) {
			if (streamDataProvider.getCurrentLiteralMetaStartEvent() == null) {
				streamDataProvider.getXMLStreamWriter().writeComment(streamDataProvider.getCommentContent().toString());
				streamDataProvider.getCommentContent().delete(0, streamDataProvider.getCommentContent().length());
			}
		}
	}
}
