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


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.exception.JPhyloIOWriterException;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PropertyOwner;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.formats.xml.receivers.AbstractXMLDataReceiver;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslator;

import java.io.IOException;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class PhyloXMLMetaDataReceiver extends AbstractXMLDataReceiver<PhyloXMLWriterStreamDataProvider> implements PhyloXMLConstants {	
	private PropertyOwner propertyOwner;
	private boolean hasSimpleContent;
	private URIOrStringIdentifier literalPredicate;
	private URIOrStringIdentifier originalType;
	private String alternativeStringRepresentation;
	
	private boolean writeMeta;
	private ResourceMetadataEvent parentMeta;


	//TODO ignore all metadata with PhyloXML-specific predicates
	public PhyloXMLMetaDataReceiver(PhyloXMLWriterStreamDataProvider streamDataProvider,
			ReadWriteParameterMap parameterMap, PropertyOwner propertyOwner) {
		super(streamDataProvider, parameterMap);
		this.propertyOwner = propertyOwner;
	}


	protected boolean hasSimpleContent() {
		return hasSimpleContent;
	}


	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {
		switch (getParameterMap().getPhyloXMLMetadataTreatment()) {
			case NONE:
				writeMeta = false;
				break;
			case ONLY_LEAFS:
				writeMeta = !(getParentEvent() instanceof ResourceMetadataEvent);
				break;
			case SEQUENTIAL:
				writeMeta = true;
				break;
			case TOP_LEVEL_WITH_CHILDREN:
			case TOP_LEVEL_WITHOUT_CHILDREN:
				break;
		}
		
		if (writeMeta) {
			hasSimpleContent = event.getSequenceType().equals(LiteralContentSequenceType.SIMPLE);
			literalPredicate = event.getPredicate();
			originalType = event.getOriginalType();
			alternativeStringRepresentation = event.getAlternativeStringValue();
		}
	}
	

	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if (event.hasValue()) {
			if (hasSimpleContent()) {
				if ((originalType != null) && (originalType.getURI() != null) 
						&& originalType.getURI().getNamespaceURI().equals(XMLConstants.W3C_XML_SCHEMA_NS_URI)) { //TODO exception or warning if datatype is not an XSD type?
					//TODO also check if this original type is allowed in PhyloXML?
					XMLStreamWriter writer = getStreamDataProvider().getWriter();
					ObjectTranslator<?> translator = getParameterMap().getObjectTranslatorFactory().getDefaultTranslator(originalType.getURI());
					String value = null;
					
					if ((event.getObjectValue() != null)) {
						if ((translator != null) && translator.hasStringRepresentation()) {
							if (event.getObjectValue() instanceof QName) { //TODO Refactor QName translator
								QName objectValue = (QName)event.getObjectValue();
								value = writer.getPrefix(objectValue.getNamespaceURI()) + XMLUtils.QNAME_SEPARATOR + objectValue.getLocalPart();
							}
							else {
								try {
									value = translator.javaToRepresentation(event.getObjectValue());
								}
								catch (ClassCastException e) {
									throw new JPhyloIOWriterException("The original type of the object declared in this event did not match the actual object type. "
											+ "Therefore it could not be parsed.");
								}
							}
						}
						else if (alternativeStringRepresentation != null) { //TODO use alternative string here?
							value = alternativeStringRepresentation;
						}
						else if (event.getStringValue() != null) {		
							value = event.getStringValue();
						}
						else {
							value = event.getObjectValue().toString();
						}
					}
					else if (alternativeStringRepresentation != null) {
						value = alternativeStringRepresentation;
					}
					else {					
						value = event.getStringValue();
					}
					
					writePropertyTag(originalType, value);
				}
			}
			else if (event.hasXMLEventValue()) {				
				writeCustomXMLTag(event.getXMLEvent());							
			}
		}
	}
	
	
	private void writePropertyTag(URIOrStringIdentifier datatype, String value) throws XMLStreamException, JPhyloIOWriterException {			
		getStreamDataProvider().getWriter().writeStartElement(TAG_PROPERTY.getLocalPart());
		
		if (literalPredicate.getURI() != null) { //TODO handle case that only string key is present
			getStreamDataProvider().getWriter().writeAttribute(ATTR_REF.getLocalPart(), 
					XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), literalPredicate.getURI().getPrefix(), 
					literalPredicate.getURI().getNamespaceURI()) + ":" + literalPredicate.getURI().getLocalPart());
		}
		
		getStreamDataProvider().getWriter().writeAttribute(ATTR_DATATYPE.getLocalPart(), XMLReadWriteUtils.XSD_DEFAULT_PRE 
				+ ":" + datatype.getURI().getLocalPart());
		
		getStreamDataProvider().getWriter().writeAttribute(ATTR_APPLIES_TO.getLocalPart(), propertyOwner.toString().toLowerCase()); //TODO XTG attribute constants zum Vergleich ansehen
		
		//TODO Also write ID_REF attribute?
		
		getStreamDataProvider().getWriter().writeCharacters(value);
		
		getStreamDataProvider().getWriter().writeEndElement();		
	}
	
	
	protected void writeCustomXMLTag(XMLEvent event) throws XMLStreamException {
		switch (event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				StartElement element = event.asStartElement();
				QName tag = element.getName();
				getStreamDataProvider().getWriter().writeStartElement(tag.getPrefix(), tag.getLocalPart(), tag.getNamespaceURI());
				
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributes = element.getAttributes();
				while (attributes.hasNext()) {
					Attribute attribute = attributes.next();
					QName attributeName = attribute.getName();
					getStreamDataProvider().getWriter().writeAttribute(attributeName.getPrefix(), attributeName.getNamespaceURI(), attributeName.getLocalPart(),
							attribute.getValue());					
				}
				
				if (literalPredicate.getURI() != null) {
					getStreamDataProvider().getWriter().writeAttribute(XMLReadWriteUtils.getRDFPrefix(getStreamDataProvider().getWriter()), 
							XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
							XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), literalPredicate.getURI().getPrefix(), 
							literalPredicate.getURI().getNamespaceURI()) + ":" + literalPredicate.getURI().getLocalPart());
				} //TODO handle case that only string key is present (attribute similar to NeXML?)
				
				if ((originalType != null) && (originalType.getURI() != null)) {
					getStreamDataProvider().getWriter().writeAttribute(XMLReadWriteUtils.getRDFPrefix(getStreamDataProvider().getWriter()), 
							XMLReadWriteUtils.ATTRIBUTE_RDF_DATATYPE.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
							XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), originalType.getURI().getPrefix(), 
							originalType.getURI().getNamespaceURI()) + ":" + originalType.getURI().getLocalPart());
				}
				
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
		switch (getParameterMap().getPhyloXMLMetadataTreatment()) {
		case NONE:
			writeMeta = false;
			break;
		case ONLY_LEAFS:
			
			break;
		case SEQUENTIAL:
			writeMeta = true;
			break;
		case TOP_LEVEL_WITH_CHILDREN:
			writeMeta = !(getParentEvent() instanceof ResourceMetadataEvent);
			break;
		case TOP_LEVEL_WITHOUT_CHILDREN:
			writeMeta = !(getParentEvent() instanceof ResourceMetadataEvent);
			break;
	}
		
		if (!(getParentEvent() instanceof ResourceMetadataEvent)) {  // Only write resource meta on highest level
			if (event.getHRef() != null) {
				writePropertyTag(new URIOrStringIdentifier("anyURI", null), event.getHRef().toString());
			}
		}
	}
	

	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (event.getType().getContentType().equals(EventContentType.META_LITERAL)) {
			if (getStreamDataProvider().isLiteralContentContinued()) {
				throw new InconsistentAdapterDataException("A literal meta end event was encounterd, although the last literal meta content "
						+ "event was marked to be continued in a subsequent event.");
			}
		}		
	}
}
