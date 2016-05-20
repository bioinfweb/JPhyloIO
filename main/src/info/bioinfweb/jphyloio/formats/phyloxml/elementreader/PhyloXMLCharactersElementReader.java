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


import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.xml.XMLElementReader;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;



public class PhyloXMLCharactersElementReader implements XMLElementReader<PhyloXMLReaderStreamDataProvider> {
	private QName datatype; //TODO use URIOrStringIdentifier?	
	
	
	public PhyloXMLCharactersElementReader(QName datatype) {
		super();		
		this.datatype = datatype;
	}


	@Override
	public void readEvent(PhyloXMLReaderStreamDataProvider streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
		//TODO CAVE: Multiple character events may occur, resulting in multiple (unterminated) content events.
		boolean isContinued = streamDataProvider.getEventReader().peek().getType().equals(XMLStreamConstants.CHARACTERS);
		streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(
				new URIOrStringIdentifier(null, datatype), event.asCharacters().getData(), isContinued));
	}
}
