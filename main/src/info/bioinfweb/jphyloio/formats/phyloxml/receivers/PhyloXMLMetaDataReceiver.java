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
package info.bioinfweb.jphyloio.formats.phyloxml.receivers;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.BasicEventReceiver;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PropertyOwner;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class PhyloXMLMetaDataReceiver extends BasicEventReceiver<XMLStreamWriter> implements PhyloXMLConstants {
	private NeXMLWriterStreamDataProvider streamDataProvider;
	
	private PropertyOwner propertyOwner;
	private LiteralContentSequenceType metaContentType;
	
	
	public PhyloXMLMetaDataReceiver(XMLStreamWriter writer, ReadWriteParameterMap parameterMap, PhyloXMLWriterStreamDataProvider streamDataProvider, PropertyOwner propertyOwner) {
		super(writer, parameterMap);
		this.propertyOwner = propertyOwner;
	}
	

	public NeXMLWriterStreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (!((getParentEvent() instanceof LiteralMetadataEvent) || (getParentEvent() instanceof ResourceMetadataEvent))) {
			metaContentType = event.getSequenceType();
			//TODO buffer original type and alternative string representation
			
			//TODO meta literal nur nutzen, um zu bestimmen als was die folgenden Inhalte geschrieben werden?
			//TODO translate predicate to existing PhyloXML tags
		}
	}
	

	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if (event.hasValue()) {
			switch (metaContentType) {
				case SIMPLE:
					if ((event.getObjectValue() != null) && event.getObjectValue() instanceof Double) {
						writeConfidenceTag(null, ((Double)event.getObjectValue())); //TODO use original type
					}
					else {
						if (event.getStringValue() != null) {
							String dataType = "string"; //TODO use original type
							writePropertyTag(dataType, propertyOwner, event.getStringValue());
						}
					}					
					break;
				case XML:
					if (event.hasXMLEventValue()) {
						writeOtherTag(event.getXMLEvent());
					}
				default:
					break;
			}
		}
	}
	
	
	private void writePropertyTag(String dataType, PropertyOwner appliesTo, String property) throws XMLStreamException {
		getWriter().writeStartElement(TAG_PROPERTY.getLocalPart());
		
		if ((dataType == null) || dataType.isEmpty()) {
			dataType = "string";
		}
		
		getWriter().writeAttribute(ATTR_DATATYPE.getLocalPart(), ATTR_DATATYPE.getPrefix() + ":" + dataType); //TODO make sure this is a valid XSD type, prefix in front of value?
		getWriter().writeAttribute(ATTR_APPLIES_TO.getLocalPart(), appliesTo.toString().toLowerCase()); //TODO XTG attribute constants zum Vergleich ansehen
		getWriter().writeCharacters(property);		
		getWriter().writeEndElement();
	}
	
	
	private void writeConfidenceTag(String type, double value) throws XMLStreamException {
		if ((type == null) || type.isEmpty()) {
			type = TAG_CONFIDENCE.getLocalPart();
		}
		else {
			type = type.replaceAll("\\s*", " ");
			type = type.trim();
		}
		
		getWriter().writeStartElement(TAG_CONFIDENCE.getLocalPart());
		getWriter().writeAttribute(ATTR_TYPE.getLocalPart(), type);
		getWriter().writeCharacters(Double.toString(value));
		getWriter().writeEndElement();
	}
	
	
	private void writeOtherTag(XMLEvent event) throws XMLStreamException {
		switch (event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				StartElement element = event.asStartElement();
				QName tag = element.getName();
				getWriter().writeStartElement(tag.getPrefix(), tag.getLocalPart(), tag.getNamespaceURI());
			// TODO write RDF attributes (datatype, property)
				break;
			case XMLStreamConstants.END_ELEMENT:
				getWriter().writeEndElement();
				break;
			case XMLStreamConstants.CHARACTERS:
				getWriter().writeCharacters(event.asCharacters().getData());
				break;
			default:
				break;
		}
	}
	

	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if (!((getParentEvent() instanceof LiteralMetadataEvent) || (getParentEvent() instanceof ResourceMetadataEvent))) {
			writePropertyTag("anyURI", propertyOwner, event.getHRef().toString());
		}
	}
	

	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {
		String comment = event.getContent();
		
		if (!comment.isEmpty()) {
			streamDataProvider.getCommentContent().append(comment);
		}
		
		if (!event.isContinuedInNextEvent()) {
			streamDataProvider.getWriter().writeComment(streamDataProvider.getCommentContent().toString());
			streamDataProvider.getCommentContent().delete(0, streamDataProvider.getCommentContent().length());			
		}
	}
	

	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		// TODO Auto-generated method stub
	}
}
