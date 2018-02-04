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


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;



public class NamespaceContextTest {
	public static void main(String[] args) throws FileNotFoundException, XMLStreamException, FactoryConfigurationError {
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileInputStream("data/other/Namespace.xml"));
		try {
			List<NamespaceContext> contexts = new ArrayList<NamespaceContext>();
			
			while (reader.hasNext()) {
				XMLEvent event = reader.nextEvent();
				if (event.isStartElement()) {
					contexts.add(event.asStartElement().getNamespaceContext());
				}
			}
			
			for (NamespaceContext context : contexts) {
				System.out.println("ns1: " + context.getNamespaceURI("ns1"));
				System.out.println("ns2: " + context.getNamespaceURI("ns2"));
				System.out.println("ns3: " + context.getNamespaceURI("ns3"));
				System.out.println();
			}
		}
		finally {
			reader.close();
		}
	}
}
