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
package info.bioinfweb.jphyloio.formats.xml.elementreaders;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;



@SuppressWarnings("rawtypes")
public class XMLStartElementReader extends AbstractXMLElementReader {
	private QName literalPredicate;
	private QName resourcePredicate;
	private URIOrStringIdentifier datatype;
	private boolean isEdgeMeta;
	private Map<QName, QName> attributeToPredicateMap;
	

	
	public XMLStartElementReader(QName literalPredicate, QName resourcePredicate, URIOrStringIdentifier datatype, boolean isEdgeMeta, QName... mappings) {
		super();
		this.literalPredicate = literalPredicate;
		this.resourcePredicate = resourcePredicate;
		this.datatype = datatype;
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


	@SuppressWarnings("unchecked")
	@Override
	public void readEvent(XMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
		
		if (isEdgeMeta) {
			streamDataProvider.setCurrentEventCollection(((NodeEdgeInfo)streamDataProvider.getSourceNode().peek()).getNestedEdgeEvents());
		}
		
		if (resourcePredicate != null) {
			streamDataProvider.getCurrentEventCollection().add(
					new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
							new URIOrStringIdentifier(null, resourcePredicate), null, null));
			
			if ((attributeToPredicateMap != null) && !attributeToPredicateMap.isEmpty()) {
				Iterator<Attribute> attributes = event.asStartElement().getAttributes();
				while (attributes.hasNext()) {
					Attribute attribute = attributes.next();
					String attributeValue = event.asStartElement().getAttributeByName(attribute.getName()).getValue();					
					
					if (attributeToPredicateMap.get(attribute.getName()) != null) {
						streamDataProvider.getCurrentEventCollection().add(
								new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
								new URIOrStringIdentifier(null, attributeToPredicateMap.get(attribute.getName())), null, LiteralContentSequenceType.SIMPLE));
	
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(attributeValue, attributeValue));
								
						streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
					}
					else {
						throw new JPhyloIOReaderException("No predicate was found for the attribute \"" + attribute.getName().getLocalPart() + "\".", 
								event.getLocation());
					}
				}
			}
		}
		
		if (literalPredicate != null) {
			streamDataProvider.getCurrentEventCollection().add(
					new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
					new URIOrStringIdentifier(null, literalPredicate), datatype, LiteralContentSequenceType.SIMPLE));
		}
	}
}
