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
package info.bioinfweb.jphyloio.formats.xml.elementreaders;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;

import java.awt.Color;
import java.util.LinkedHashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.events.StartElement;



public abstract class AbstractXMLElementReader<P extends XMLReaderStreamDataProvider<? extends AbstractXMLEventReader<P>>>
		implements XMLElementReader<P> {
	
	
	protected void readAttributes(P streamDataProvider, StartElement element, String idPrefix, QName... mappings) {
		if (mappings.length % 2 != 0) {
			throw new IllegalArgumentException("Attributes and predicates need to be given in pairs, but an uneven number of arguments was found.");
		}
		else if (mappings.length >= 2) {
			LinkedHashMap<QName, QName> attributeToPredicateMap = new LinkedHashMap<QName, QName>();
			for (int i  = 0; i  < mappings.length; i += 2) {
				attributeToPredicateMap.put(mappings[i], mappings[i + 1]);
			}
			
			readAttributes(streamDataProvider, element, idPrefix, attributeToPredicateMap);
		}
	}
	
	
	protected void readAttributes(P streamDataProvider, StartElement element, String idPrefix, LinkedHashMap<QName, QName> attributeToPredicateMap) {
		if ((attributeToPredicateMap != null) && !attributeToPredicateMap.isEmpty()) {
			String metaIDPrefix = idPrefix + ReadWriteConstants.DEFAULT_META_ID_PREFIX;
			
			for (QName attribute : attributeToPredicateMap.keySet()) {
				if (element.getAttributeByName(attribute) != null) {
					String attributeValue = element.getAttributeByName(attribute).getValue();
					Object objectValue = null;					

					if (!attributeValue.isEmpty() && (attributeValue.charAt(0) == '#')) {
						try {
							objectValue = Color.decode(attributeValue);
						}
						catch (IllegalArgumentException f) {}
					}
					else if (attributeValue.equals(Boolean.toString(false))) {
						objectValue = false;
					}
					else if (attributeValue.equals(Boolean.toString(true))) {
						objectValue = true;
					}
					else {
						try {
							objectValue = Double.parseDouble(attributeValue);
						}
						catch (IllegalArgumentException f) {}
					}
					
					if (objectValue == null) {
						objectValue = attributeValue;
					}
					
					streamDataProvider.getCurrentEventCollection().add(
							new LiteralMetadataEvent(metaIDPrefix + streamDataProvider.getIDManager().createNewID(), null, 
							new URIOrStringIdentifier(null, attributeToPredicateMap.get(attribute)), LiteralContentSequenceType.SIMPLE));
					
					if ((attributeValue != null) && !attributeValue.isEmpty()) {
						streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(objectValue, attributeValue));
					}
							
					streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));					
				}
			}
		}
	}
}