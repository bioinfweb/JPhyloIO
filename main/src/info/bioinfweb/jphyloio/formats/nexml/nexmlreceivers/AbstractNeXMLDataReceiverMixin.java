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
package info.bioinfweb.jphyloio.formats.nexml.nexmlreceivers;


import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;



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
	
	
	private static void writeLiteralMeta(NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event, Class<? extends Object> objectType) throws XMLStreamException, JPhyloIOWriterException {
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();
		
		writer.writeStartElement(TAG_META.getLocalPart());
		
		streamDataProvider.writeLabeledIDAttributes(event, null); //TODO are there any cases where an about-attribute should be written?
		
		if (event.getPredicate() != null) {
			writer.writeAttribute(ATTR_PROPERTY.getLocalPart(), event.getPredicate().getLocalPart()); //TODO make sure predicate is valid CURIE
		}
		else {
			throw new InternalError("Literal meta should have a predicate that is a QName.");
		}
		
		if (objectType != null) {
			writer.writeAttribute(ATTR_DATATYPE.getLocalPart(), XSD_PRE + ":" + NeXMLWriterStreamDataProvider.getXsdTypeForClass().get(objectType).getLocalPart());
		}
		
		writer.writeAttribute(ATTR_XSI_TYPE.getLocalPart(), TYPE_LITERAL_META);
		
		if (streamDataProvider.getCommentContent().length() != 0) {
			streamDataProvider.getXMLStreamWriter().writeComment(streamDataProvider.getCommentContent().toString());
			streamDataProvider.getCommentContent().delete(0, streamDataProvider.getCommentContent().length());
		}
	}
	
	
	public static void checkLiteralMeta (NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event) 
			throws XMLStreamException, JPhyloIOWriterException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			streamDataProvider.addToDocumentIDs(event.getID());
			
			String nameSpace = event.getPredicate().getNamespaceURI();
			if (streamDataProvider.getXMLStreamWriter().getPrefix(nameSpace) == null) {
				String prefix = "defaultNamespacePrefix";
				String nameSpacePrefix;
				int index = 0;
				
				do {
					nameSpacePrefix = prefix + index;
					index++;
				} while (!streamDataProvider.getNamespacePrefixes().add(nameSpacePrefix));
				
				streamDataProvider.getXMLStreamWriter().setPrefix(nameSpacePrefix, nameSpace);
				streamDataProvider.getNameSpaces().add(nameSpace);
			}
		}
	}

	
	public static void handleLiteralMeta(NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event) throws XMLStreamException, JPhyloIOWriterException {		
		if (!event.getSequenceType().equals(LiteralContentSequenceType.SIMPLE)) { //TODO what about type OTHER?
			writeLiteralMeta(streamDataProvider, event, null);
		}
		else {
			streamDataProvider.setLiteralWithoutXMLContent(event);
		}
	}


	public static void handleLiteralContentMeta(NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataContentEvent event) throws XMLStreamException, JPhyloIOWriterException {		
		if (streamDataProvider.getLiteralWithoutXMLContent() != null) {
			writeLiteralMeta(streamDataProvider, streamDataProvider.getLiteralWithoutXMLContent(), event.getObjectValue().getClass()); //TODO should getOriginalType() be used here?
			streamDataProvider.setLiteralWithoutXMLContent(null);
		}
		
		streamDataProvider.setLiteralContentIsContinued(event.isContinuedInNextEvent());
				
		if (event.getObjectValue() != null) {
			streamDataProvider.getXMLStreamWriter().writeCharacters(event.getObjectValue().toString());
		}
		else if (event.getStringValue() != null) {
			streamDataProvider.getXMLStreamWriter().writeCharacters(event.getStringValue()); //TODO should the alternative String value be written somewhere to?
		}
		else {
			streamDataProvider.getXMLStreamWriter().writeCharacters("");
		}
	}
	
	
	public static void checkResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			streamDataProvider.addToDocumentIDs(event.getID());
			streamDataProvider.getNameSpaces().add(event.getRel().getNamespaceURI());
		}
	}


	public static void handleResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws ClassCastException, XMLStreamException, JPhyloIOWriterException {
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();
					
		writer.writeStartElement(TAG_META.getLocalPart());		
		
		streamDataProvider.writeLabeledIDAttributes(event, event.getAbout());
		
		if (event.getRel() != null) {
			writer.writeAttribute(ATTR_REL.getLocalPart(), event.getRel().getLocalPart());		
		}
		else {
			throw new InternalError("Resource meta should have a predicate that is a QName.");
		}
		
		if (event.getHRef() != null) {
			writer.writeAttribute(ATTR_HREF.getLocalPart(), event.getHRef().toString());
		}
		
		writer.writeAttribute(ATTR_XSI_TYPE.getLocalPart(), TYPE_RESOURCE_META);
	}
	
	
	public static void handleMetaEndEvent(NeXMLWriterStreamDataProvider streamDataProvider, JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (event.getType().getContentType().equals(EventContentType.META_LITERAL) && (streamDataProvider.getLiteralWithoutXMLContent() != null)) {
			writeLiteralMeta(streamDataProvider, streamDataProvider.getLiteralWithoutXMLContent(), null);
			streamDataProvider.setLiteralWithoutXMLContent(null);
			
			if (streamDataProvider.isLiteralContentIsContinued()) { //TODO do the same for comments?
				throw new InconsistentAdapterDataException("A literal meta end event was encounterd, although the last literal meta content "
						+ "event was marked to be continued in a subsequent event.");
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
			if (streamDataProvider.getLiteralWithoutXMLContent() == null) {
				streamDataProvider.getXMLStreamWriter().writeComment(streamDataProvider.getCommentContent().toString());
				streamDataProvider.getCommentContent().delete(0, streamDataProvider.getCommentContent().length());
			}
		}
	}
}
