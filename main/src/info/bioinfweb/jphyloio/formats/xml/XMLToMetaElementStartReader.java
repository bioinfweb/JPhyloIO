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
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



@SuppressWarnings("rawtypes")
public class XMLToMetaElementStartReader extends AbstractXMLElementReader {
	@SuppressWarnings("unchecked")
	@Override
	public void readEvent(XMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
		StartElement element = event.asStartElement();
		
		if (streamDataProvider.getNestedMetaNames().isEmpty()) {
			streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), 
					null, new URIOrStringIdentifier(null, element.getName()), null, LiteralContentSequenceType.XML));
		}
		
		streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event, false));
		
		streamDataProvider.getNestedMetaNames().add(element.getName().getLocalPart());
	}
}