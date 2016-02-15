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
package info.bioinfweb.jphyloio.formats.xml;


import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.xtg.XTGEventReader;

import javax.xml.stream.events.XMLEvent;



@SuppressWarnings("rawtypes")
public class UnknownMetaElementEndReader extends AbstractXMLElementReader {
	@SuppressWarnings("unchecked")
	@Override
	public void readEvent(XMLStreamDataProvider streamDataProvider, XMLEvent event) throws Exception {
		String attributeKey = streamDataProvider.getFormat() + "." + streamDataProvider.getParentName() + "." + streamDataProvider.getMetaWithAttributes().getName().getLocalPart();
		streamDataProvider.getEventReader().readAttributes(streamDataProvider.getMetaWithAttributes(), attributeKey);
		streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_INFORMATION));
	}
}
