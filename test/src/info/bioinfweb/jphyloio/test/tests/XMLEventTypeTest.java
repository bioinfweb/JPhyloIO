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

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.NotationDeclaration;
import javax.xml.stream.events.XMLEvent;



public class XMLEventTypeTest {
	
	
	public static void main(String[] args) {
		try {
			XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(new File(
					//"data/other/XMLEventTypeTest.xml"
					"data/other/entity.xml"
					)));
			
			try {
				while (reader.hasNext()) {
					XMLEvent event = reader.nextEvent();

					System.out.println(event.getEventType());
					if (event.getEventType() == XMLStreamConstants.CDATA) {
						System.out.println("CDATA found!");
					}
					else if (event.getEventType() == XMLStreamConstants.CHARACTERS) {
						System.out.println("Characters with the content \"" + event.asCharacters().getData() + "\" found!");
					}
					else if (event.getEventType() == XMLStreamConstants.DTD) {
						DTD dtd = (DTD)event;
						System.out.println("DTD: \"" + dtd.getDocumentTypeDeclaration() + "\"");
					}
					else if (event.getEventType() == XMLStreamConstants.ENTITY_DECLARATION) {
						NotationDeclaration decl = (NotationDeclaration)event;
						System.out.println("Entity declaration: name='" + decl.getName() + "' publicID='" + decl.getPublicId() + "' systemID='" + decl.getSystemId() + "'");
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
