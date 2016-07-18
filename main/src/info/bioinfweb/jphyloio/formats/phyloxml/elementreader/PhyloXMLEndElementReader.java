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


import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.AbstractXMLElementReader;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;



public class PhyloXMLEndElementReader extends AbstractXMLElementReader<PhyloXMLReaderStreamDataProvider> {
	private boolean createLiteralEnd;
	private boolean createResourceEnd;
	private boolean isEdgeMeta;
	
	
	public PhyloXMLEndElementReader(boolean createLiteralEnd, boolean createResourceEnd, boolean isEdgeMeta) {
		super();
		this.createLiteralEnd = createLiteralEnd;
		this.createResourceEnd = createResourceEnd;
		this.isEdgeMeta = isEdgeMeta;
	}


	@Override
	public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException,
			XMLStreamException {
		if (createLiteralEnd) {
			streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		}
		
		if (createResourceEnd) {
			streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		}
		
		if (isEdgeMeta && streamDataProvider.hasSpecialEventCollection()) {
			streamDataProvider.resetCurrentEventCollection();
		}
	}	
}
