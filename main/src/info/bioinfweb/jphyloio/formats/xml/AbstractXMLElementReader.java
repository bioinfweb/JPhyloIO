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
import info.bioinfweb.jphyloio.events.meta.UriOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public abstract class AbstractXMLElementReader<P extends XMLReaderStreamDataProvider<? extends AbstractXMLEventReader<P>>>
		implements XMLElementReader<P> {
	
	protected void readAttributes(P streamDataProvider, StartElement element) {
		String key = streamDataProvider.getEventReader().getFormatID() + "." + streamDataProvider.getParentName() + "." + element.getName().getLocalPart();
		@SuppressWarnings("unchecked")
		Iterator<Attribute> attributes = element.getAttributes();
		while (attributes.hasNext()) {
			Attribute attribute = attributes.next();
			
			streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
					null, new UriOrStringIdentifier(null, new QName(key + "." + attribute.getName())), null, LiteralContentSequenceType.SIMPLE));
			streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(new UriOrStringIdentifier("string", new QName("string")), 
					attribute.getValue(), null));
			streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		}
	}
	
	
	protected String readCharacterData(P streamDataProvider, StartElement element) throws XMLStreamException {
		XMLEvent nextEvent = streamDataProvider.getEventReader().getXMLReader().peek();
		String value = null;
		
		if (nextEvent.getEventType() == XMLStreamConstants.CHARACTERS) {
			String characterData = nextEvent.asCharacters().getData();
			if (!characterData.matches("\\s+")) {
				value = characterData;
			}
		}
		
		return value;
	}
}
