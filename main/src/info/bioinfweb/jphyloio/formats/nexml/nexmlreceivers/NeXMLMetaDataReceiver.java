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


import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;


/**
 * This receiver should be used if no other elements than meta are expected under an element.
 * 
 * @author Sarah Wiechers
 */
public class NeXMLMetaDataReceiver extends AbstractNeXMLDataReceiver {	


	public NeXMLMetaDataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap,
			NeXMLWriterStreamDataProvider streamDataProvider) {
		super(writer, parameterMap, streamDataProvider);
	}


	private void writeResourceMeta(ResourceMetadataEvent event) throws XMLStreamException {
		getWriter().writeStartElement(TAG_META.getLocalPart());
		
		getStreamDataProvider().writeLabeledIDAttributes(event);
		
		getWriter().writeAttribute(ATTR_REL.getLocalPart(), event.getRel().getLocalPart());		
		
		if (event.getHRef() != null) {
			getWriter().writeAttribute(ATTR_HREF.getLocalPart(), event.getHRef().toString());
		}
		
		getWriter().writeAttribute(ATTR_XSI_TYPE.getLocalPart(), TYPE_RESOURCE_META);
	}
	
	
	private void writeLiteralMeta(LiteralMetadataEvent event, Class<? extends Object> objectType) throws XMLStreamException {
		getWriter().writeStartElement(TAG_META.getLocalPart());
		
		getStreamDataProvider().writeLabeledIDAttributes(event);
		
		if (event.getPredicate() != null) {
			getWriter().writeAttribute(ATTR_PROPERTY.getLocalPart(), event.getPredicate().getLocalPart());		
		}
		else {
			throw new InternalError("Literal meta should have a predicate that is a QName.");
		}
		
		getWriter().writeAttribute(ATTR_DATATYPE.getLocalPart(), NeXMLWriterStreamDataProvider.getXsdTypeForClass().get(objectType).getLocalPart());	//TODO write prefix?	
		
		getWriter().writeAttribute(ATTR_XSI_TYPE.getLocalPart(), TYPE_LITERAL_META);
	}
	
	
	private void writeLiteralMetaContent(LiteralMetadataContentEvent event) throws XMLStreamException {
		getWriter().writeCharacters(event.getStringValue());
	}
	
	
	protected void writeComment(CommentEvent event) throws XMLStreamException {
		String comment = event.getContent();
		if (!comment.isEmpty()) {
			getWriter().writeComment(comment);			
		}
	}
	

	@Override
	public boolean doAdd(JPhyloIOEvent event) throws IOException, XMLStreamException {
		switch (event.getType().getContentType()) {
			case META_RESOURCE:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					writeResourceMeta(event.asResourceMetadataEvent());
				}
				else {
					getWriter().writeEndElement();
				}
				break;
			case META_LITERAL:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					LiteralMetadataEvent literalMetadataEvent = event.asLiteralMetadataEvent();
					if (literalMetadataEvent.getSequenceType().equals(LiteralContentSequenceType.XML)) {
						writeLiteralMeta(literalMetadataEvent, null);
					}
					else {
						getStreamDataProvider().setLiteralWithoutXMLContent(literalMetadataEvent);
					}
				}
				else {
					getWriter().writeEndElement();
				}
				break;
			case META_LITERAL_CONTENT:
				LiteralMetadataContentEvent contentEvent = event.asLiteralMetadataContentEvent();
				if (getStreamDataProvider().getLiteralWithoutXMLContent() != null) {
					writeLiteralMeta(getStreamDataProvider().getLiteralWithoutXMLContent(), contentEvent.getObjectValue().getClass());							
				}
				writeLiteralMetaContent(contentEvent);				
				break;
			case COMMENT:
				writeComment(event.asCommentEvent());
				break;
			default:
				break;
		}
		return true;
	}
}
