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
package info.bioinfweb.jphyloio.formats.xml;


import static info.bioinfweb.commons.testing.XMLAssert.*;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLEventReader;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.junit.Assert;
import org.junit.Test;



public class AbstractMetaXMLReaderTest {
	@Test
	public void testMetaXMLEventReaderInNeXML() throws XMLStreamException, IOException {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/XML/NeXMLCustomXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLEventReader customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);			
			assertCharactersEvent("\n\t\t" + "characters" + "\n\t\t", customXMLReader);
			StartElement element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertAttributeCount(1, element);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", element);
			assertCharactersEvent("\n\t\t\t" + "some more" + "\n\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertStartElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\tcharacters and even more\n\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			Assert.assertFalse(customXMLReader.hasNext());
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();				
			}
			
			customXMLReader = reader.createMetaXMLEventReader();			
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			String pre = assertNamespace(new QName("http://example.com/", ""), false, element);
			assertNamespaceCount(1, element);
			element = assertStartElement(new QName("http://example.com/", "nestedTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.com/", "attribute", pre), "false", element);
			assertAttributeCount(1, element);
			assertStartElement(new QName("http://example.com/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "secondNested", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.com/", "nestedTag", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			Assert.assertFalse(customXMLReader.hasNext());
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			String ex = assertNamespace(new QName("http://example.com/", ""), false, element);
			assertNamespaceCount(1, element);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "customTag", ex), customXMLReader);
			assertEndDocument(customXMLReader);
			Assert.assertFalse(customXMLReader.hasNext());
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);			
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", ex), customXMLReader);
			assertEndDocument(customXMLReader);
			Assert.assertFalse(customXMLReader.hasNext());

			while (reader.hasNextEvent()) {
				event = reader.next();
			}
			
			Assert.assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testMetaXMLEventReaderInPhyloXML() throws IOException, XMLStreamException {
		PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/XML/PhyloXMLCustomXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {				
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLEventReader customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			StartElement element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", element);
			assertAttributeCount(1, element);
			assertCharactersEvent("\n\t\t\t\t\t" + "some more" + "\n\t\t\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertStartElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t" + "characters and even more" + "\n\t\t\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			Assert.assertFalse(customXMLReader.hasNext());
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			String prefix = assertNamespace(new QName("http://example.com/", "", "pre"), true, element);
			assertNamespaceCount(1, element);
			element = assertStartElement(new QName("http://example.com/", "nestedTag", prefix), customXMLReader);
			assertAttribute(new QName("http://example.com/", "attribute", "ex"), "false", element);
			assertAttributeCount(1, element);
			assertStartElement(new QName("http://example.com/", "secondNested", prefix), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "secondNested", prefix), customXMLReader);
			assertEndElement(new QName("http://example.com/", "nestedTag", prefix), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			Assert.assertFalse(customXMLReader.hasNext());
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			prefix = assertNamespace(new QName("http://example.com/", "", "ex"), true, element);
			assertNamespaceCount(1, element);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "customTag", prefix), customXMLReader);
			assertEndDocument(customXMLReader);
			Assert.assertFalse(customXMLReader.hasNext());
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
					
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", prefix), customXMLReader);
			assertEndDocument(customXMLReader);
			Assert.assertFalse(customXMLReader.hasNext());
			
			while (reader.hasNextEvent()) {
				event = reader.next();
			}
			
			Assert.assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testMetaXMLStreamReaderInNeXML() throws IOException, XMLStreamException {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/XML/NeXMLCustomXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLStreamReader customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertCharactersEvent("\n\t\t" + "characters" + "\n\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", 0, customXMLReader);
			assertAttributeCount(1, customXMLReader);
			assertCharactersEvent("\n\t\t\t" + "some more" + "\n\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t" + "characters and even more" + "\n\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			String prefix = assertNamespace(new QName("http://example.com/", "", "pre"), true, 0, customXMLReader);
			assertNamespaceCount(1, customXMLReader);
			assertStartElement(new QName("http://example.com/", "nestedTag", prefix), customXMLReader);
			assertAttribute(new QName("http://example.com/", "attribute", prefix), "false", 0, customXMLReader);
			assertAttributeCount(1, customXMLReader);
			assertShortElement(new QName("http://example.com/", "secondNested", prefix), "nested content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "nestedTag", prefix), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			assertNamespace(new QName("http://example.com/", "", "ex"), true, 0, customXMLReader);
			assertNamespaceCount(1, customXMLReader);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertShortElement(new QName("http://example.org/", "customTag", "ex"), "some content", customXMLReader);
			assertEndDocument(customXMLReader);
			
			while (reader.hasNextEvent()) {
				event = reader.next();
			}
			
			Assert.assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}				
	}
	
	
	@Test
	public void testMetaXMLStreamReaderInPhyloXML() throws IOException, XMLStreamException {
		PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/XML/PhyloXMLCustomXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLStreamReader customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", 0, customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t" + "some more" + "\n\t\t\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t" + "characters and even more" + "\n\t\t\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			String prefix = assertNamespace(new QName("http://example.com/", "", "pre"), true, 0, customXMLReader);
			assertNamespaceCount(1, customXMLReader);
			assertStartElement(new QName("http://example.com/", "nestedTag", prefix), customXMLReader);
			assertAttribute(new QName("http://example.com/", "attribute", prefix), "false", 0, customXMLReader);
			assertStartElement(new QName("http://example.com/", "secondNested", prefix), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "secondNested", prefix), customXMLReader);
			assertEndElement(new QName("http://example.com/", "nestedTag", prefix), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			prefix = assertNamespace(new QName("http://example.com/", "", "ex"), true, 0, customXMLReader);
			assertNamespaceCount(1, customXMLReader);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "customTag", prefix), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", prefix), customXMLReader);
			assertEndDocument(customXMLReader);
			
			while (reader.hasNextEvent()) {
				event = reader.next();
			}
			
			Assert.assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}

	
	@Test
	public void testMultipleEventReaders() throws IOException, XMLStreamException {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/XML/NeXMLCustomXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLEventReader customXMLReader = reader.createMetaXMLEventReader();
			MetaXMLEventReader customXMLReader2 = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			
			assertCharactersEvent("\n\t\t" + "characters" + "\n\t\t", customXMLReader);
			StartElement element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertAttributeCount(1, element);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", element);
			assertCharactersEvent("\n\t\t\t" + "some more" + "\n\t\t\t", customXMLReader2);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertStartElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader2);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader2);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\tcharacters and even more\n\t\t", customXMLReader2);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader2);
			assertEndDocument(customXMLReader2);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();				
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			customXMLReader2 = reader.createMetaXMLEventReader();
			XMLEventReader customXMLReader3 = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);  // Readers each generate their own start and end document events
			assertStartDocument(customXMLReader2);
			element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader2);
			String pre = assertNamespace(new QName("http://example.com/", ""), false, element);
			assertNamespaceCount(1, element);
			element = assertStartElement(new QName("http://example.com/", "nestedTag", "ex"), customXMLReader3);
			assertAttribute(new QName("http://example.com/", "attribute", pre), "false", element);
			assertAttributeCount(1, element);
			assertStartElement(new QName("http://example.com/", "secondNested", "ex"), customXMLReader2);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "secondNested", "ex"), customXMLReader2);
			assertEndElement(new QName("http://example.com/", "nestedTag", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader3);
			assertEndDocument(customXMLReader3);
			assertEndDocument(customXMLReader);
			assertEndDocument(customXMLReader2);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			String ex = assertNamespace(new QName("http://example.com/", ""), false, element);
			assertNamespaceCount(1, element);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "customTag", ex), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);			
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", ex), customXMLReader);
			assertEndDocument(customXMLReader);		

			while (reader.hasNextEvent()) {
				event = reader.next();
			}
			
			Assert.assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testMultipleStreamReaders() throws IOException, XMLStreamException {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/XML/NeXMLCustomXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLStreamReader customXMLReader = reader.createMetaXMLStreamReader();
			MetaXMLStreamReader customXMLReader2 = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);  // Readers each generate their own start and end document events
			assertStartDocument(customXMLReader2);
			assertCharactersEvent("\n\t\t" + "characters" + "\n\t\t", customXMLReader2);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader2);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", 0, customXMLReader2);
			assertAttributeCount(1, customXMLReader2);
			assertCharactersEvent("\n\t\t\t" + "some more" + "\n\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLReader2);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t" + "characters and even more" + "\n\t\t", customXMLReader2);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader2);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			customXMLReader2 = reader.createMetaXMLStreamReader();
			MetaXMLStreamReader customXMLReader3 = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			String prefix = assertNamespace(new QName("http://example.com/", "", "pre"), true, 0, customXMLReader);
			assertNamespaceCount(1, customXMLReader);
			assertStartElement(new QName("http://example.com/", "nestedTag", prefix), customXMLReader2);
			assertAttribute(new QName("http://example.com/", "attribute", prefix), "false", 0, customXMLReader2);
			assertAttributeCount(1, customXMLReader2);
			assertShortElement(new QName("http://example.com/", "secondNested", prefix), "nested content", customXMLReader3);
			assertEndElement(new QName("http://example.com/", "nestedTag", prefix), customXMLReader3);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader3);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			assertNamespace(new QName("http://example.com/", "", "ex"), true, 0, customXMLReader);
			assertNamespaceCount(1, customXMLReader);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertShortElement(new QName("http://example.org/", "customTag", "ex"), "some content", customXMLReader);
			assertEndDocument(customXMLReader);
			
			while (reader.hasNextEvent()) {
				event = reader.next();
			}
			
			Assert.assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}				
	}
	
	
	@Test
	public void testDifferentReaderTypes() throws Exception {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/XML/NeXMLCustomXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLStreamReader customXMLStreamReader = reader.createMetaXMLStreamReader();
			
			// Read custom XML with XML stream reader
			assertStartDocument(customXMLStreamReader);  // Readers each generate their own start and end document events
			assertCharactersEvent("\n\t\t" + "characters" + "\n\t\t", customXMLStreamReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLStreamReader);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", 0, customXMLStreamReader);
			assertAttributeCount(1, customXMLStreamReader);
			assertCharactersEvent("\n\t\t\t" + "some more" + "\n\t\t\t", customXMLStreamReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLStreamReader);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLStreamReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLStreamReader);
			assertCharactersEvent("\n\t\t\t" + "characters and even more" + "\n\t\t", customXMLStreamReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLStreamReader);
			assertEndDocument(customXMLStreamReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			// Read custom XML with JPhyloIO event reader
			StartElement element = assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/", "customTag", "ex"), null, false, reader).asStartElement();
			String prefix = assertNamespace(new QName("http://example.com/", "", "pre"), true, element);
			assertNamespaceCount(1, element);
			element = assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.com/", "nestedTag", "ex"), null, false, reader).asStartElement();
			assertAttribute(new QName("http://example.com/", "attribute", prefix), "false", element);
			assertAttributeCount(1, element);
			assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.com/", "secondNested", prefix), null, false, reader);
			assertXMLContentEvent("nested content", XMLStreamConstants.CHARACTERS, null, "nested content", false, reader);
			assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.com/", "secondNested", prefix), null, false, reader);
			assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.com/", "nestedTag", prefix), null, false, reader);
			assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/", "customTag", prefix), null, false, reader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			MetaXMLEventReader customXMLEventReader = reader.createMetaXMLEventReader();
			
			// Read custom XML with XML event reader
			assertStartDocument(customXMLEventReader);
			element = assertStartElement(new QName("http://example.com/", "customTag", "ex"), customXMLEventReader);
			String ex = assertNamespace(new QName("http://example.com/", ""), false, element);
			assertNamespaceCount(1, element);
			assertCharactersEvent("some content", customXMLEventReader);
			assertEndElement(new QName("http://example.com/", "customTag", ex), customXMLEventReader);
			assertEndDocument(customXMLEventReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.LITERAL_META, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLEventReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLEventReader);
			element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLEventReader);			
			assertCharactersEvent("some content", customXMLEventReader);
			assertEndElement(new QName("http://example.org/", "customTag", ex), customXMLEventReader);
			assertEndDocument(customXMLEventReader);		

			while (reader.hasNextEvent()) {
				event = reader.next();
			}
			
			Assert.assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}				
	}
}