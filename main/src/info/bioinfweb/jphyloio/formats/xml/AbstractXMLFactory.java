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


import java.io.Reader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.factory.AbstractSingleReaderWriterFactory;



public abstract class AbstractXMLFactory extends AbstractSingleReaderWriterFactory {
	protected abstract boolean checkRootTag(StartElement startElement);
	
	
	@Override
	public boolean checkFormat(Reader reader, ReadWriteParameterMap parameters)	{
		try {
			XMLEventReader xmlReader = XMLInputFactory.newInstance().createXMLEventReader(reader);
			
			if (!(xmlReader.nextEvent().getEventType() == XMLStreamConstants.START_DOCUMENT)) {
				return false;
			}
			
			return checkRootTag(xmlReader.nextEvent().asStartElement());
		}
		catch (XMLStreamException e) {
			return false;
		}
	}
}
