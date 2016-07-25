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
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PropertyOwner;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;



public class PhyloXMLPropertyMetadataReceiver extends PhyloXMLMetaDataReceiver {
	private boolean isProperty = false;
	private QName currentPredicate;
	
	private QName ref;
	private QName datatype;
	private String appliesTo;
	private String unit;
	private String idRef;
	private String value;
	

	public PhyloXMLPropertyMetadataReceiver(PhyloXMLWriterStreamDataProvider streamDataProvider,
			ReadWriteParameterMap parameterMap, PropertyOwner propertyOwner) {
		super(streamDataProvider, parameterMap, propertyOwner);
	}

	@Override
	protected void handleLiteralMetaStart(LiteralMetadataEvent event) throws IOException, XMLStreamException {		
		if (isProperty) {
			currentPredicate = event.getPredicate().getURI();
			
			if (!currentPredicate.equals(PREDICATE_PROPERTY_ATTR_APPLIES_TO) && !currentPredicate.equals(PREDICATE_PROPERTY_ATTR_UNIT) 
					&& !currentPredicate.equals(PREDICATE_PROPERTY_ATTR_ID_REF)) {
				ref = event.getPredicate().getURI();
				
				if (event.getOriginalType() != null) {
					datatype = event.getOriginalType().getURI();
				}
				else {
					throw new InconsistentAdapterDataException("A property element must specify a datatype.");
				}
			}
		}
	}

	@Override
	protected void handleLiteralContentMeta(LiteralMetadataContentEvent event) throws IOException, XMLStreamException {
		if (isProperty) {
			if (currentPredicate.equals(PREDICATE_PROPERTY_ATTR_APPLIES_TO)) {
				appliesTo = event.getStringValue();
			}
			else if (currentPredicate.equals(PREDICATE_PROPERTY_ATTR_UNIT)) {
				unit = event.getStringValue();
			}
			else if (currentPredicate.equals(PREDICATE_PROPERTY_ATTR_ID_REF)) {
				idRef = event.getStringValue();
			}
			else {
				value = event.getStringValue();
			}
		}
	}

	@Override
	protected void handleResourceMetaStart(ResourceMetadataEvent event) throws IOException, XMLStreamException {
		if ((event.getRel().getURI() != null) && event.getRel().getURI().equals(PREDICATE_PROPERTY)) {
			isProperty = true;
		}
	}

	@Override
	protected void handleMetaEndEvent(JPhyloIOEvent event) throws IOException, XMLStreamException {
		if (event.getType().getContentType().equals(EventContentType.META_RESOURCE) && isProperty) {
			if ((ref != null) && (datatype != null)) {
				getStreamDataProvider().getWriter().writeStartElement(TAG_PROPERTY.getLocalPart());	
				
				getStreamDataProvider().getWriter().writeAttribute(ATTR_REF.getLocalPart(), XMLReadWriteUtils.getNamespacePrefix(getStreamDataProvider().getWriter(), 
						ref.getPrefix(), ref.getNamespaceURI()) + ":" + ref.getLocalPart());		
				
				if (unit != null) {
					getStreamDataProvider().getWriter().writeAttribute(ATTR_UNIT.getLocalPart(), unit);
				}
				
				getStreamDataProvider().getWriter().writeAttribute(ATTR_DATATYPE.getLocalPart(), XMLReadWriteUtils.XSD_DEFAULT_PRE 
						+ ":" + datatype.getLocalPart());			
				
				if (appliesTo != null) {
					getStreamDataProvider().getWriter().writeAttribute(ATTR_APPLIES_TO.getLocalPart(), appliesTo);
				}
				else {
					getStreamDataProvider().getWriter().writeAttribute(ATTR_APPLIES_TO.getLocalPart(), getPropertyOwner().toString().toLowerCase());
				}
				
				if (idRef != null) {
					getStreamDataProvider().getWriter().writeAttribute(ATTR_ID_REF.getLocalPart(), idRef);
				}
				
				if (value != null) {
					getStreamDataProvider().getWriter().writeCharacters(value);
				}
				
				getStreamDataProvider().getWriter().writeEndElement();		
			}
		}
	}
}
