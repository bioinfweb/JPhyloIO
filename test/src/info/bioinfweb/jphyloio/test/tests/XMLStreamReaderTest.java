/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
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


import java.io.File;
import java.io.FileReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;



public class XMLStreamReaderTest {
	public static void main(String[] args) {
		try {
			File file = new File("data/XML/attributeTypeTest.xml");
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(file));
			
			try {
				int eventType = reader.getEventType();
				
				while (reader.hasNext()) {
					switch (eventType) {
						case XMLStreamConstants.START_DOCUMENT:
							System.out.println("Document encoding: " + reader.getEncoding());
							System.out.println("Document version: " + reader.getVersion());
							System.out.println("Document character encoding scheme: " + reader.getCharacterEncodingScheme());
							break;
						case XMLStreamConstants.START_ELEMENT:
							System.out.println("Element name: " + reader.getLocalName());
							System.out.println("Attribute name: " + reader.getAttributeLocalName(0));
							System.out.println("Attribute type: " + reader.getAttributeType(0));  // If an attribute is present the default type is CDATA
							break;
						case XMLStreamConstants.CHARACTERS:
							System.out.println("Characters: " + reader.getText());
							System.out.println("Character array length: " + reader.getTextCharacters().length);
							System.out.println("Character text length: " + reader.getTextLength());
							System.out.println("Character offset: " + reader.getTextStart());
							break;
						default:
							break;
					}
					
					eventType = reader.next();
					System.out.println();
				}
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
