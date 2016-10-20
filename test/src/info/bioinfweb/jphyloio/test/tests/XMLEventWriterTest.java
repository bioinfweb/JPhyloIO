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

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;



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
				writer.add(factory.createNamespace("p", "http://example.org/p"));
				writer.add(factory.createCharacters("abc"));
				writer.add(factory.createEndDocument());
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
