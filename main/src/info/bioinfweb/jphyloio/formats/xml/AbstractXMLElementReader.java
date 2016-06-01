/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.xml;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;



public abstract class AbstractXMLElementReader<P extends XMLReaderStreamDataProvider<? extends AbstractXMLEventReader<P>>>
		implements XMLElementReader<P> {
	
	
	protected void readAttributes(P streamDataProvider, StartElement element, QName... mappings) {
		if (mappings.length % 2 != 0) {
			throw new IllegalArgumentException("Attributes and predicates need to be given in pairs, but an uneven number of arguments was found.");
		}
		else if (mappings.length >= 2) {
			Map<QName, QName> attributeToPredicateMap = new HashMap<QName, QName>();
			for (int i  = 0; i  < mappings.length; i += 2) {
				attributeToPredicateMap.put(mappings[i], mappings[i + 1]);
			}
			readAttributes(streamDataProvider, element, attributeToPredicateMap);
		}
	}
	
	
	protected void readAttributes(P streamDataProvider, StartElement element, Map<QName, QName> attributeToPredicateMap) {
		if ((attributeToPredicateMap != null) && !attributeToPredicateMap.isEmpty()) {
			@SuppressWarnings("unchecked")
			Iterator<Attribute> attributes = element.getAttributes();
			while (attributes.hasNext()) {
				Attribute attribute = attributes.next();
				if (attributeToPredicateMap.containsKey(attribute.getName())) { //allows to ignore certain attributes
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
							new URIOrStringIdentifier(null, attributeToPredicateMap.get(attribute.getName())), LiteralContentSequenceType.SIMPLE));
	
					streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(null, element.getAttributeByName(attribute.getName()).getValue(), null));
							
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				}
			}
		}
	}
}