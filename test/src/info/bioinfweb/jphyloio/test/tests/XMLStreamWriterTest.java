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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;



public class XMLStreamWriterTest {
	public static void main(String[] args) {
		try {
			File file = new File("data/testOutput/StreamWriterOutputTest.xml");
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileWriter(file));
			
			try {
				writer.writeStartDocument();
				writer.writeCharacters("some characters"); // Should not be allowed here, can maybe be written to express DTDs and entity declarations
				writer.writeProcessingInstruction("startTag", "some data");
				writer.writeStartElement("startTag");
				writer.writeEntityRef("anEntity");
//				writer.writeAttribute("anAttribute", "aValue"); // throws an exception, because attribute is not associated to any element
				writer.writeDTD("doc type x");  // Writer does not check if DTD is given in the right position
				writer.writeCData("This is the first part of a CDATA element.");  // Writer splits these and writes two CDATA elements
				writer.writeCData("This is the second part of a CDATA element.");
				writer.writeEndElement();
				writer.writeEndDocument();
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
