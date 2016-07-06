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


import info.bioinfweb.commons.io.XMLUtils;
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
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();
		String metaType = streamDataProvider.getNeXMLPrefix(streamDataProvider.getXMLStreamWriter()) + ":" + TYPE_LITERAL_META;
		
		writer.writeStartElement(TAG_META.getLocalPart());
		streamDataProvider.writeLabeledIDAttributes(event, null);
		
		writer.writeAttribute(XMLReadWriteUtils.getXSIPrefix(streamDataProvider.getXMLStreamWriter()), ATTR_XSI_TYPE.getNamespaceURI(), 
				ATTR_XSI_TYPE.getLocalPart(), metaType);
		
		if (event.getPredicate().getURI() != null) {
			QName predicate = event.getPredicate().getURI();			
			writer.writeAttribute(ATTR_PROPERTY.getLocalPart(), obtainPrefix(streamDataProvider, predicate.getNamespaceURI()) + ":" + predicate.getLocalPart());
		}
		else if (event.getPredicate().getStringRepresentation() != null) {
			 //TODO How to use alternative string representation if no QName is present?
		}
		
		if ((event.getOriginalType() != null) && (event.getOriginalType().getURI() != null)) { // Attribute is optional
			writer.writeAttribute(ATTR_DATATYPE.getLocalPart(), obtainPrefix(streamDataProvider, event.getOriginalType().getURI().getNamespaceURI()) 
					+ ":" + event.getOriginalType().getURI().getLocalPart());
		}
		
		if (event.getAlternativeStringValue() != null) { // Attribute is optional
			writer.writeAttribute(ATTR_CONTENT.getLocalPart(), event.getAlternativeStringValue());
		}
		
		streamDataProvider.setCurrentLiteralMetaSequenceType(event.getSequenceType());
		streamDataProvider.setCurrentLiteralMetaDatatype(event.getOriginalType());
	}
	
	
	public static void checkLiteralMeta (NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event) 
			throws XMLStreamException, JPhyloIOWriterException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			QName resourceIdentifier;
			
			streamDataProvider.addToDocumentIDs(event.getID());
			
			if (event.getPredicate() != null) {
				resourceIdentifier = event.getPredicate().getURI();
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getXMLStreamWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
			
			if ((event.getOriginalType() != null) && (event.getOriginalType().getURI() != null)) {
				resourceIdentifier = event.getOriginalType().getURI();
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getXMLStreamWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
		}
	}
	

	public static void handleLiteralContentMeta(NeXMLWriterStreamDataProvider streamDataProvider, ReadWriteParameterMap parameters, LiteralMetadataContentEvent event) throws XMLStreamException, JPhyloIOWriterException {		
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();
		
		switch (streamDataProvider.getCurrentLiteralMetaSequenceType()) {
			case SIMPLE:
				QName datatype = null;
				if (streamDataProvider.getCurrentLiteralMetaDatatype() != null) {
					datatype = streamDataProvider.getCurrentLiteralMetaDatatype().getURI();
				}
				
				ObjectTranslator<?> translator = parameters.getObjectTranslatorFactory().getDefaultTranslator(datatype);
				if ((event.getObjectValue() != null)) {
					if ((translator != null) && translator.hasStringRepresentation()) {
						if (event.getObjectValue() instanceof QName) { //TODO Refactor QName-translator instead?
							QName objectValue = (QName)event.getObjectValue();						
							writer.writeCharacters(writer.getPrefix(objectValue.getNamespaceURI()) + XMLUtils.QNAME_SEPARATOR + objectValue.getLocalPart());
						}
						else {
							try {
								streamDataProvider.getXMLStreamWriter().writeCharacters(translator.javaToRepresentation(event.getObjectValue()));
							}
							catch (ClassCastException e) {
								throw new JPhyloIOWriterException("The original type of the object declared in this event did not match the actual object type. "
										+ "Therefore it could not be parsed.");
							}
						}
					}
					else if (event.getStringValue() != null) {						
						writer.writeCharacters(event.getStringValue());
					}
					else { //TODO What should be written if neither a translator could be found nor a stringValue is present? Check if XML representation is available?
						writer.writeCharacters(event.getObjectValue().toString());
					}
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
							writer.writeStartElement(obtainPrefix(streamDataProvider, element.getName().getNamespaceURI()), element.getName().getLocalPart(), 
									element.getName().getNamespaceURI());
							@SuppressWarnings("unchecked")
							Iterator<Attribute> attributes = element.getAttributes();
							while (attributes.hasNext()) {
								Attribute attribute = attributes.next();
								writer.writeAttribute(obtainPrefix(streamDataProvider, attribute.getName().getNamespaceURI()), attribute.getName().getNamespaceURI(), 
										attribute.getName().getLocalPart(), attribute.getValue());
							}
							break;
						case XMLStreamConstants.END_ELEMENT:
							writer.writeEndElement();
							break;
						case XMLStreamConstants.CHARACTERS:
							writer.writeCharacters(xmlContentEvent.asCharacters().getData());
							break;							
						default:							
							break;
					}
				}
				break;			
		}
		
		streamDataProvider.setLiteralContentIsContinued(event.isContinuedInNextEvent());
	}
	
	
	public static void checkLiteralContentMeta(NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataContentEvent event) throws XMLStreamException {
		QName resourceIdentifier;
		
		if (event.hasXMLEventValue()) {
			if (event.getXMLEvent().getEventType() == XMLStreamConstants.START_ELEMENT) {
				StartElement element = event.getXMLEvent().asStartElement();
				resourceIdentifier = element.getName();
				
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getXMLStreamWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
		}
		
		if ((event.getObjectValue() != null) && (event.getObjectValue() instanceof QName)) {
			QName objectValue = (QName)event.getObjectValue();
			streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getXMLStreamWriter(), objectValue.getPrefix(), 
					objectValue.getNamespaceURI()), objectValue.getNamespaceURI());
		}
	}
	
	
	public static void handleResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws ClassCastException, XMLStreamException, JPhyloIOWriterException {
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();		
		String metaType = streamDataProvider.getNeXMLPrefix(streamDataProvider.getXMLStreamWriter()) + ":" + TYPE_RESOURCE_META;
		
		writer.writeStartElement(TAG_META.getLocalPart());		
		streamDataProvider.writeLabeledIDAttributes(event, event.getAbout());
		
		writer.writeAttribute(XMLReadWriteUtils.getXSIPrefix(streamDataProvider.getXMLStreamWriter()), ATTR_XSI_TYPE.getNamespaceURI(), 
				ATTR_XSI_TYPE.getLocalPart(), metaType);
		
		if (event.getRel().getURI() != null) {
			QName predicate = event.getRel().getURI();			
			writer.writeAttribute(ATTR_REL.getLocalPart(), obtainPrefix(streamDataProvider, predicate.getNamespaceURI()) + ":" + predicate.getLocalPart());		
		}
		else if (event.getRel().getStringRepresentation() != null) {
			 //TODO How to use alternative string representation if no QName is present?
		}
		
		if (event.getHRef() != null) { // Attribute is optional
			writer.writeAttribute(ATTR_HREF.getLocalPart(), event.getHRef().toString());
		}		
	}
	
	
	public static void checkResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			streamDataProvider.addToDocumentIDs(event.getID());
			
			if (event.getRel() != null) {
				QName resourceIdentifier = event.getRel().getURI();
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getXMLStreamWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
		}
	}
	
	
	public static void handleMetaEndEvent(NeXMLWriterStreamDataProvider streamDataProvider, JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (event.getType().getContentType().equals(EventContentType.META_LITERAL)) {
			if (streamDataProvider.isLiteralContentContinued()) {
				throw new InconsistentAdapterDataException("A literal meta end event was encounterd, although the last literal meta content "
						+ "event was marked to be continued in a subsequent event.");
			}
			
			streamDataProvider.setCurrentLiteralMetaSequenceType(null);
		}		
		
		streamDataProvider.getXMLStreamWriter().writeEndElement();
	}
	
	
	public static void handleComment(NeXMLWriterStreamDataProvider streamDataProvider, CommentEvent event) throws ClassCastException, XMLStreamException {
		String comment = event.getContent();
		
		if (!comment.isEmpty()) {
			streamDataProvider.getCommentContent().append(comment);
		}
		
		if (!event.isContinuedInNextEvent()) {
			streamDataProvider.getXMLStreamWriter().writeComment(streamDataProvider.getCommentContent().toString());
			streamDataProvider.getCommentContent().delete(0, streamDataProvider.getCommentContent().length());			
		}
	}
	
	
	private static String obtainPrefix(NeXMLWriterStreamDataProvider streamDataProvider, String namespaceURI) throws XMLStreamException {
		String prefix = streamDataProvider.getXMLStreamWriter().getPrefix(namespaceURI);
		if (prefix == null) {
			prefix = streamDataProvider.getNeXMLPrefix(streamDataProvider.getXMLStreamWriter());
		}
		
		return prefix;
	}
}
