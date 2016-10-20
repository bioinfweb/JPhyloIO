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


import info.bioinfweb.jphyloio.ReadWriteConstants;
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

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
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
		XMLStreamWriter writer = streamDataProvider.getWriter();
		String metaType = streamDataProvider.getNeXMLPrefix(streamDataProvider.getWriter()) + ":" + TYPE_LITERAL_META;
		QName predicate;
		
		writer.writeStartElement(TAG_META.getLocalPart());
		streamDataProvider.writeLabeledIDAttributes(event, null);
		
		writer.writeAttribute(XMLReadWriteUtils.getXSIPrefix(streamDataProvider.getWriter()), ATTR_XSI_TYPE.getNamespaceURI(), 
				ATTR_XSI_TYPE.getLocalPart(), metaType);
		
		if (event.getPredicate().getURI() != null) { //TODO add entry to readWriteParameterMap to allow not writing metadata without QName predicate at all
			predicate = event.getPredicate().getURI();			
		}
		else {
			predicate = ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA;
		}
		
		if (event.getPredicate().getStringRepresentation() != null) {  // URIORSTringIdentifier checks if either string representation or URI are present, both can not be null
			writer.writeAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY.getNamespaceURI(), ReadWriteConstants.ATTRIBUTE_STRING_KEY.getLocalPart(), event.getPredicate().getStringRepresentation());
		}
		
		writer.writeAttribute(ATTR_PROPERTY.getLocalPart(), obtainPrefix(streamDataProvider, predicate.getNamespaceURI()) + ":" + predicate.getLocalPart());
		
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
			
			if (event.getPredicate().getURI() != null) {
				resourceIdentifier = event.getPredicate().getURI();
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
			else {
				resourceIdentifier = ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA;
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
			
			if (event.getPredicate().getStringRepresentation() != null)  {				
				resourceIdentifier = ReadWriteConstants.ATTRIBUTE_STRING_KEY;
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
			
			if ((event.getOriginalType() != null) && (event.getOriginalType().getURI() != null)) {
				resourceIdentifier = event.getOriginalType().getURI();
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
		}
	}
	

	public static void handleLiteralContentMeta(NeXMLWriterStreamDataProvider streamDataProvider, ReadWriteParameterMap parameters, 
			LiteralMetadataContentEvent event) throws XMLStreamException, ClassCastException, IOException {
		
		XMLStreamWriter writer = streamDataProvider.getWriter();
		
		switch (streamDataProvider.getCurrentLiteralMetaSequenceType()) {
			case SIMPLE:
				QName datatype = null;
				if (streamDataProvider.getCurrentLiteralMetaDatatype() != null) {
					datatype = streamDataProvider.getCurrentLiteralMetaDatatype().getURI();
				}
				
				ObjectTranslator<?> translator = parameters.getObjectTranslatorFactory()
						.getDefaultTranslatorWithPossiblyInvalidNamespace(datatype);
				if ((event.getObjectValue() != null)) {
					if ((translator != null) && translator.hasStringRepresentation()) {
						if (translator.hasStringRepresentation()) {
							try {
								writer.writeCharacters(translator.javaToRepresentation(event.getObjectValue(), streamDataProvider));
							}
							catch (ClassCastException e) {
								throw new JPhyloIOWriterException("The original type of the object declared in this event did not match the actual object type. "
										+ "Therefore it could not be parsed.");
							}
						}
						else {
							translator.writeXMLRepresentation(writer, event.getObjectValue(), null);
						}
					}
					else if (event.getStringValue() != null) {		
						writer.writeCharacters(event.getStringValue());
					}
					else {
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
							writer.writeStartElement(element.getName().getNamespaceURI(), element.getName().getLocalPart());  // Writer obtains the correct prefix from its namespace context
							
							// Write attributes
							@SuppressWarnings("unchecked")
							Iterator<Attribute> attributes = element.getAttributes();
							while (attributes.hasNext()) {
								Attribute attribute = attributes.next();
								writer.writeAttribute(attribute.getName().getNamespaceURI(), attribute.getName().getLocalPart(), attribute.getValue());
							}
							
							break;
						case XMLStreamConstants.END_ELEMENT:
							writer.writeEndElement();
							break;
						case XMLStreamConstants.CHARACTERS:
						case XMLStreamConstants.SPACE:
							writer.writeCharacters(xmlContentEvent.asCharacters().getData());
							break;
						case XMLStreamConstants.CDATA:
							writer.writeCData(xmlContentEvent.asCharacters().getData()); //TODO multiple events with continued content should be buffered and written to a single CDATA element
							break;
						case XMLStreamConstants.ATTRIBUTE:
							Attribute contentAttribute = ((Attribute)xmlContentEvent);
							writer.writeAttribute(contentAttribute.getName().getPrefix(), contentAttribute.getName().getNamespaceURI(), 
									contentAttribute.getName().getLocalPart(), contentAttribute.getValue());
							break;
						case XMLStreamConstants.NAMESPACE:
							Namespace contentNamespace = ((Namespace)xmlContentEvent);
							writer.writeNamespace(contentNamespace.getPrefix(), contentNamespace.getNamespaceURI());
							break;
						case XMLStreamConstants.PROCESSING_INSTRUCTION:
							ProcessingInstruction contentProcessingInstruction = ((ProcessingInstruction)xmlContentEvent);
							writer.writeProcessingInstruction(contentProcessingInstruction.getTarget(), contentProcessingInstruction.getData());
							break;
						case XMLStreamConstants.COMMENT:
							writer.writeComment(((Comment)xmlContentEvent).getText());
							break;
						case XMLStreamConstants.DTD:
							//TODO give substring of dtd in warning (128)
						case XMLStreamConstants.NOTATION_DECLARATION:
						case XMLStreamConstants.ENTITY_DECLARATION:
							parameters.getLogger().addWarning("");
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
				
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
				
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributesIterator = element.getAttributes();
				while (attributesIterator.hasNext()) {
					Attribute attribute = attributesIterator.next();
					resourceIdentifier = attribute.getName();
					
					streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
							resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
				}
				
				@SuppressWarnings("unchecked")
				Iterator<Namespace> namespaceIterator = element.getNamespaces();
				while (namespaceIterator.hasNext()) {
					Namespace namespace = namespaceIterator.next();
					
					streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), namespace.getPrefix(), 
							namespace.getNamespaceURI()), namespace.getNamespaceURI());
				}
			}
		}
		
		if ((event.getObjectValue() != null) && (event.getObjectValue() instanceof QName)) {
			QName objectValue = (QName)event.getObjectValue();
			streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), objectValue.getPrefix(), 
					objectValue.getNamespaceURI()), objectValue.getNamespaceURI());
		}
	}
	
	
	public static void handleResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws ClassCastException, XMLStreamException, JPhyloIOWriterException {
		XMLStreamWriter writer = streamDataProvider.getWriter();		
		String metaType = streamDataProvider.getNeXMLPrefix(streamDataProvider.getWriter()) + ":" + TYPE_RESOURCE_META;
		QName predicate;
		
		writer.writeStartElement(TAG_META.getLocalPart());		
		streamDataProvider.writeLabeledIDAttributes(event, event.getAbout());
		
		writer.writeAttribute(XMLReadWriteUtils.getXSIPrefix(streamDataProvider.getWriter()), ATTR_XSI_TYPE.getNamespaceURI(), 
				ATTR_XSI_TYPE.getLocalPart(), metaType);
		
		if (event.getRel().getURI() != null) {  //TODO add entry to readWriteParameterMap to allow not writing metadata without QName predicate at all
			predicate = event.getRel().getURI();		
		}
		else {
			predicate = ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA;
		}
		
		if (event.getRel().getStringRepresentation() != null)  {  // URIORSTringIdentifier checks if either string representation or URI are present, both can not be null
			writer.writeAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY.getNamespaceURI(), ReadWriteConstants.ATTRIBUTE_STRING_KEY.getLocalPart(), 
					event.getRel().getStringRepresentation());
		}
		
		writer.writeAttribute(ATTR_REL.getLocalPart(), obtainPrefix(streamDataProvider, predicate.getNamespaceURI()) + ":" + predicate.getLocalPart());
		
		if (event.getHRef() != null) { // Attribute is optional
			writer.writeAttribute(ATTR_HREF.getLocalPart(), event.getHRef().toString());
		}		
	}
	
	
	public static void checkResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			QName resourceIdentifier;
			streamDataProvider.addToDocumentIDs(event.getID());
			
			if (event.getRel().getURI() != null) {
				resourceIdentifier = event.getRel().getURI();
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
			else {
				resourceIdentifier = ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA;
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());				
			}
			
			if (event.getRel().getStringRepresentation() != null) {
				resourceIdentifier = ReadWriteConstants.ATTRIBUTE_STRING_KEY;
				streamDataProvider.setNamespacePrefix(XMLReadWriteUtils.getNamespacePrefix(streamDataProvider.getWriter(), resourceIdentifier.getPrefix(), 
						resourceIdentifier.getNamespaceURI()), resourceIdentifier.getNamespaceURI());
			}
		}
	}
	
	
	public static void handleMetaEndEvent(NeXMLWriterStreamDataProvider streamDataProvider, JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (event.getType().getContentType().equals(EventContentType.META_LITERAL)) {
			if (streamDataProvider.isLiteralContentContinued()) {
				throw new InconsistentAdapterDataException("A literal meta end event was encountered, although the last literal meta content "
						+ "event was marked to be continued in a subsequent event.");
			}
			
			streamDataProvider.setCurrentLiteralMetaSequenceType(null);
		}		
		
		streamDataProvider.getWriter().writeEndElement();
	}
	
	
	public static void handleComment(NeXMLWriterStreamDataProvider streamDataProvider, CommentEvent event) throws ClassCastException, XMLStreamException {
		String comment = event.getContent();
		
		if (!comment.isEmpty()) {
			streamDataProvider.getCommentContent().append(comment);
		}
		
		if (!event.isContinuedInNextEvent()) {
			streamDataProvider.getWriter().writeComment(streamDataProvider.getCommentContent().toString());
			streamDataProvider.getCommentContent().delete(0, streamDataProvider.getCommentContent().length());			
		}
	}
	
	
	private static String obtainPrefix(NeXMLWriterStreamDataProvider streamDataProvider, String namespaceURI) throws XMLStreamException, JPhyloIOWriterException {
		String prefix = streamDataProvider.getWriter().getPrefix(namespaceURI);
		
		if (prefix == null) {
			throw new JPhyloIOWriterException("The namespace \"" + namespaceURI + "\" is not bound to a prefix.");
		}
		
		return prefix;
	}
}
