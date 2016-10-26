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
				while (reader.hasNext()) {
					int eventType = reader.next();
					switch (eventType) {
						case XMLStreamConstants.START_ELEMENT:
							System.out.println("Element name: :" + reader.getLocalName());
							System.out.println("Attribute name: " + reader.getAttributeLocalName(0));
							System.out.println("Attribute type: " + reader.getAttributeType(0));  // If an attribute is present the default type is CDATA
							break;
						default:
							break;
					}
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
