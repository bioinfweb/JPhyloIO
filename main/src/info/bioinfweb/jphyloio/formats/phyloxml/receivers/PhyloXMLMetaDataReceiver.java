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
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PropertyOwner;
import info.bioinfweb.jphyloio.formats.xml.receivers.AbstractXMLDataReceiver;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class PhyloXMLMetaDataReceiver extends AbstractXMLDataReceiver<PhyloXMLWriterStreamDataProvider> implements PhyloXMLConstants {	
	private PropertyOwner propertyOwner;
	private LiteralContentSequenceType metaContentType;
	private URIOrStringIdentifier originalType;
	private String alternativeStringRepresentation;


	public PhyloXMLMetaDataReceiver(PhyloXMLWriterStreamDataProvider streamDataProvider,
			ReadWriteParameterMap parameterMap, PropertyOwner propertyOwner) {
		super(streamDataProvider, parameterMap);
		this.propertyOwner = propertyOwner;
	}


	public LiteralContentSequenceType getMetaContentType() {
		return metaContentType;
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		if (!((getParentEvent() instanceof LiteralMetadataEvent) || (getParentEvent() instanceof ResourceMetadataEvent))) {
			metaContentType = event.getSequenceType();
			originalType = event.getOriginalType();
			alternativeStringRepresentation = event.getAlternativeStringValue();
			
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
						writeCustomXMLTag(event.getXMLEvent());
					}
				default:
					break;
			}
		}
	}
	
	
	private void writePropertyTag(String dataType, PropertyOwner appliesTo, String property) throws XMLStreamException {
		getStreamDataProvider().getWriter().writeStartElement(TAG_PROPERTY.getLocalPart());
		
		if ((dataType == null) || dataType.isEmpty()) {
			dataType = "string";
		}
		
		getStreamDataProvider().getWriter().writeAttribute(ATTR_DATATYPE.getLocalPart(), ATTR_DATATYPE.getPrefix() + ":" + dataType); //TODO make sure this is a valid XSD type, prefix in front of value?
		getStreamDataProvider().getWriter().writeAttribute(ATTR_APPLIES_TO.getLocalPart(), appliesTo.toString().toLowerCase()); //TODO XTG attribute constants zum Vergleich ansehen
		getStreamDataProvider().getWriter().writeCharacters(property);		
		getStreamDataProvider().getWriter().writeEndElement();
	}
	
	
	private void writeConfidenceTag(String type, double value) throws XMLStreamException {
		if ((type == null) || type.isEmpty()) {
			type = TAG_CONFIDENCE.getLocalPart();
		}
		else {
			type = type.replaceAll("\\s*", " ");
			type = type.trim();
		}
		
		getStreamDataProvider().getWriter().writeStartElement(TAG_CONFIDENCE.getLocalPart());
		getStreamDataProvider().getWriter().writeAttribute(ATTR_TYPE.getLocalPart(), type);
		getStreamDataProvider().getWriter().writeCharacters(Double.toString(value));
		getStreamDataProvider().getWriter().writeEndElement();
	}
	
	
	protected void writeCustomXMLTag(XMLEvent event) throws XMLStreamException {
		switch (event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				StartElement element = event.asStartElement();
				QName tag = element.getName();
				getStreamDataProvider().getWriter().writeStartElement(tag.getPrefix(), tag.getLocalPart(), tag.getNamespaceURI());
			// TODO write RDF attributes (datatype, property)
				break;
			case XMLStreamConstants.END_ELEMENT:
				getStreamDataProvider().getWriter().writeEndElement();
				break;
			case XMLStreamConstants.CHARACTERS:
				getStreamDataProvider().getWriter().writeCharacters(event.asCharacters().getData());
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
			getStreamDataProvider().getCommentContent().append(comment);
		}
		
		if (!event.isContinuedInNextEvent()) {
			getStreamDataProvider().getWriter().writeComment(getStreamDataProvider().getCommentContent().toString());
			getStreamDataProvider().getCommentContent().delete(0, getStreamDataProvider().getCommentContent().length());			
		}
	}
	

	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		// TODO Auto-generated method stub
	}
}
