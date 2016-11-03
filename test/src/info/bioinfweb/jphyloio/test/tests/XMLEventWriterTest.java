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
import java.io.FileWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartDocument;



public class XMLEventWriterTest {
	public static void main(String[] args) {
		try {
			File file = new File("data/testOutput/EventWriterOutputTest.xml");
			XMLEventWriter writer = XMLOutputFactory.newInstance().createXMLEventWriter(new FileWriter(file));
			XMLEventFactory factory = XMLEventFactory.newInstance();
			
			try {
				writer.add(factory.createStartDocument());
				writer.setDefaultNamespace("http://example.org/main");				
				writer.add(factory.createStartElement("", null, "tag"));
				writer.add(factory.createStartElement("", null, "nestedTag"));
//				writer.setPrefix("p", "http://example.org/p");
//				writer.setDefaultNamespace("http://example.org/default");
				writer.add(factory.createNamespace("http://example.org/test"));
				writer.add(factory.createNamespace("p", "http://example.org/p"));
				writer.add(factory.createCharacters("abc"));
				writer.add(factory.createEndElement("", null, "nestedTag"));
				writer.add(factory.createEndElement("", null, "tag"));
				writer.add(factory.createEndDocument());
				
				
				Attribute attribute = factory.createAttribute(new QName("attr"), "testValue");
				System.out.println(attribute.getDTDType());
				
				StartDocument startDocument = factory.createStartDocument();
				System.out.println(startDocument.getVersion());
				System.out.println(startDocument.getCharacterEncodingScheme());
				System.out.println(startDocument.getSystemId());
			}
			finally {
				writer.close();
//				file.delete();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
