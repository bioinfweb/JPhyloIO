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
package info.bioinfweb.jphyloio.test.tests;


import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;



public class XMLStreamExceptionTest {
	public static void main(String[] args) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader("data/XML/Invalid.xml"));
		try {
			XMLEvent event = (XMLEvent)reader.next();
			while (reader.hasNext()) {
				if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
					System.out.println(event.asCharacters().getData().length());
				}
				else {
					System.out.println(event);
				}
				event = (XMLEvent)reader.next();
			}
		}
		finally {
			reader.close();
		}
		// => The next() method throws all XMLStreamExceptions as NoSuchElementExceptions.
	}
}
