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
package info.bioinfweb.jphyloio.test.tests.nexml;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.nexml.model.Annotation;
import org.nexml.model.Document;
import org.nexml.model.DocumentFactory;
import org.nexml.model.OTU;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class ReadingCustomXMLTest {
	public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException {
		Document document = DocumentFactory.parse(new File("data/NeXML/MetaCustomXML.xml"));
		
		OTU otu = document.getOTUsList().get(0).getAllOTUs().get(0);
		for (Annotation annotation : otu.getAllAnnotations()) {
			System.out.println(annotation.getAllAnnotations().size());
			
			Element element = (Element)annotation.getValue();
			System.out.println(element.getNamespaceURI());
			System.out.println(element.getLocalName());
			System.out.println(element.getNodeValue());
			System.out.println("'" + element.getTextContent() + "'");
			System.out.println(element.hasChildNodes());
			
			NodeList childNodes = element.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				System.out.println("Child " + i + ":");
				System.out.println("  " + child.getLocalName());
				System.out.println("  " + child.getNodeName());
				System.out.println("  " + child.getNodeValue());
				System.out.println("  '" + child.getTextContent() + "'");
				System.out.println("  " + child.getNodeType());
			}
		}
	}
}
