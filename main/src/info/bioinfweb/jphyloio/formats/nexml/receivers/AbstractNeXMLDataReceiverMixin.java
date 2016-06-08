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
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;

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
	
	
	private static void writeLiteralMeta(NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event, Object object) throws XMLStreamException, JPhyloIOWriterException {
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();
		String metaType = streamDataProvider.getNexPrefix() + ":" + TYPE_LITERAL_META;
		
		writer.writeStartElement(TAG_META.getLocalPart());
		
		streamDataProvider.writeLabeledIDAttributes(event, null);
		
		if (event.getPredicate() != null) {
			if (event.getPredicate().getURI() != null) { //TODO predicate is always required to be written
				writer.writeAttribute(ATTR_PROPERTY.getLocalPart(), event.getPredicate().getURI().getLocalPart());
			}
		}
		else {
			throw new InternalError("Literal meta needs to have a predicate.");
		}
		
		if (object != null) { //TODO use object translator
//			writer.writeAttribute(ATTR_DATATYPE.getLocalPart(), XSD_PRE + ":" + NeXMLWriterStreamDataProvider.getXsdTypeForClass().get(object.getClass()).getLocalPart());
		}
		
		writer.writeAttribute(XMLReadWriteUtils.getXSIPrefix(streamDataProvider.getXMLStreamWriter()), ATTR_XSI_TYPE.getNamespaceURI(), 
				ATTR_XSI_TYPE.getLocalPart(), metaType);
		
		if (streamDataProvider.getCommentContent().length() != 0) {
			streamDataProvider.getXMLStreamWriter().writeComment(streamDataProvider.getCommentContent().toString());
			streamDataProvider.getCommentContent().delete(0, streamDataProvider.getCommentContent().length());
		}
	}
	
	
	public static void checkLiteralMeta (NeXMLWriterStreamDataProvider streamDataProvider, LiteralMetadataEvent event) 
			throws XMLStreamException, JPhyloIOWriterException { //TODO also check datatype of content event
		if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
			streamDataProvider.addToDocumentIDs(event.getID());
			
			if (event.getPredicate().getURI() != null) {
				streamDataProvider.setNamespacePrefix(event.getPredicate().getURI().getPrefix(), event.getPredicate().getURI().getNamespaceURI());
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
			writeLiteralMeta(streamDataProvider, streamDataProvider.getLiteralWithoutXMLContent(),	event.getObjectValue());
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
			
			if (event.getRel() != null) {
				streamDataProvider.setNamespacePrefix(event.getRel().getURI().getPrefix(), event.getRel().getURI().getNamespaceURI());
			}
		}
	}


	public static void handleResourceMeta(NeXMLWriterStreamDataProvider streamDataProvider, ResourceMetadataEvent event) throws ClassCastException, XMLStreamException, JPhyloIOWriterException {
		XMLStreamWriter writer = streamDataProvider.getXMLStreamWriter();		
		String metaType = streamDataProvider.getNexPrefix() + ":" + TYPE_RESOURCE_META;
		
		writer.writeStartElement(TAG_META.getLocalPart());
		
		streamDataProvider.writeLabeledIDAttributes(event, event.getAbout());
		
		if (event.getRel() != null) {
			writer.writeAttribute(ATTR_REL.getLocalPart(), event.getRel().getURI().getLocalPart());		
		}
		else {
			throw new InternalError("Resource meta should have a predicate that is a QName.");
		}
		
		if (event.getHRef() != null) {
			writer.writeAttribute(ATTR_HREF.getLocalPart(), event.getHRef().toString());
		}
		
		writer.writeAttribute(XMLReadWriteUtils.getXSIPrefix(streamDataProvider.getXMLStreamWriter()), ATTR_XSI_TYPE.getNamespaceURI(), 
				ATTR_XSI_TYPE.getLocalPart(), metaType);
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
