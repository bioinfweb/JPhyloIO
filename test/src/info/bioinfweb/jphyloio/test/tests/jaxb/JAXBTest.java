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
package info.bioinfweb.jphyloio.test.tests.jaxb;


import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;


import static org.junit.Assert.* ;



public class JAXBTest {
	@Test
	public void testReadingCustomXMLObject() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError, JAXBException {
		XMLEventReader reader = XMLInputFactory.newFactory().createXMLEventReader(new FileInputStream("data/NeXML/MetaJAXBXMLValue.xml"));
		
		while(!reader.peek().isStartElement() || !reader.peek().asStartElement().getName().getLocalPart().equals("customObject")) {
			reader.nextEvent();
		}
		
		JAXBContext jc = JAXBContext.newInstance(CustomObject.class);
		Unmarshaller unmarshaller = jc.createUnmarshaller();
		JAXBElement<CustomObject> jb = unmarshaller.unmarshal(reader, CustomObject.class);
		reader.close();
		
		CustomObject object = jb.getValue();
		
		assertEquals("someID", object.getID());
		assertEquals("some string", object.getStringProperty());
		assertEquals(18.5, object.getNumericProperty(), 0.00000001);
	}
	
	
//	@Test
//	public void testReadingCustomSimpleObject() throws FileNotFoundException, XMLStreamException, FactoryConfigurationError, JAXBException {
//		XMLEventReader reader = XMLInputFactory.newFactory().createXMLEventReader(new FileInputStream("data/NeXML/MetaJAXBSimpleValue.xml"));
//		
//		while(!reader.peek().isStartElement() || !reader.peek().asStartElement().getName().getLocalPart().equals("customObject")) {
//			reader.nextEvent();
//		}
//		
//		JAXBContext jc = JAXBContext.newInstance(CustomObject.class);
//		Unmarshaller unmarshaller = jc.createUnmarshaller();
//		JAXBElement<CustomObject> jb = unmarshaller.unmarshal(reader, CustomObject.class);
//		reader.close();
//		
//		CustomObject object = jb.getValue();
//		
//		assertEquals("someID", object.getID());
//		assertEquals("some string", object.getStringProperty());
//		assertEquals(18.5, object.getNumericProperty(), 0.00000001);
//	}
}
