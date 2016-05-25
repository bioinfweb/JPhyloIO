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
package info.bioinfweb.jphyloio.formats.phyloxml.elementreader;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;



public class PhyloXMLStartElementReader implements XMLElementReader<PhyloXMLReaderStreamDataProvider> {
	private QName literalPredicate;
	private QName resourcePredicate;
	private boolean isEdgeMeta;
	private Map<QName, QName> attributeToPredicateMap;
	

	
	public PhyloXMLStartElementReader(QName literalPredicate, QName resourcePredicate, boolean isEdgeMeta, QName... mappings) {
		super();
		this.literalPredicate = literalPredicate;
		this.resourcePredicate = resourcePredicate;
		this.isEdgeMeta = isEdgeMeta;
		
		if (mappings.length % 2 != 0) {
			throw new IllegalArgumentException("Attributes and predicates need to be given in pairs, but an uneven number of arguments was found.");
		}
		else if (mappings.length >= 2) {
			attributeToPredicateMap = new HashMap<QName, QName>();
			for (int i  = 0; i  < mappings.length; i += 2) {
				attributeToPredicateMap.put(mappings[i], mappings[i + 1]);
			}
		}
	}
	
	
	public PhyloXMLStartElementReader(QName literalPredicate, QName resourcePredicate, boolean isEdgeMeta, Map<QName, QName> attributeToPredicateMap) {
		super();
		this.literalPredicate = literalPredicate;
		this.resourcePredicate = resourcePredicate;
		this.isEdgeMeta = isEdgeMeta;
		this.attributeToPredicateMap = attributeToPredicateMap;		
	}


	@Override
	public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException,
			XMLStreamException {
		if (isEdgeMeta) {
			streamDataProvider.setCurrentEventCollection(streamDataProvider.getEdgeInfos().peek().getNestedEvents());
		}
		
		if (resourcePredicate != null) {
			streamDataProvider.getCurrentEventCollection().add(
					new ResourceMetadataEvent(streamDataProvider.getEventReader().getID(null, EventContentType.META_RESOURCE), null, resourcePredicate, null, null));
			
			if ((attributeToPredicateMap != null) && !attributeToPredicateMap.isEmpty()) {
				@SuppressWarnings("unchecked")
				Iterator<Attribute> attributes = event.asStartElement().getAttributes();
				while (attributes.hasNext()) {
					Attribute attribute = attributes.next();
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataEvent(streamDataProvider.getEventReader().getID(null, EventContentType.META_LITERAL), null, 
							new URIOrStringIdentifier(null, attributeToPredicateMap.get(attribute.getName())), LiteralContentSequenceType.SIMPLE));

					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataContentEvent(null, event.asStartElement().getAttributeByName(attribute.getName()).getValue(), null)); //TODO get ObjectValue and OriginalType from translator object or as a parameter
							
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				}
			}
		}
		
		if (literalPredicate != null) {
			streamDataProvider.getCurrentEventCollection().add(
					new LiteralMetadataEvent(streamDataProvider.getEventReader().getID(null, EventContentType.META_LITERAL), null, 
					new URIOrStringIdentifier(null, literalPredicate), LiteralContentSequenceType.SIMPLE));
		}
	}
}
