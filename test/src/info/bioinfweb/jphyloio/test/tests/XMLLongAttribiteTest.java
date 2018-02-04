/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2018  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.test.tests;


import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



public class XMLLongAttribiteTest {
	public static void main(String[] args) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader("data/XML/LongAttribute.xml"));
		try {
			XMLEvent event = reader.nextEvent();
			while ((event.getEventType() != XMLStreamConstants.START_ELEMENT) || !event.asStartElement().getName().getLocalPart().equals("test2")) {
				if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
					System.out.println(event.asCharacters().getData().length());
				}
				else {
					System.out.println(event);
				}
				event = reader.nextEvent();
			}
			
			StartElement startElement = event.asStartElement();
			System.out.println(startElement.getAttributeByName(new QName("attr")).getValue().length());
		}
		finally {
			reader.close();
		}
	}
}
