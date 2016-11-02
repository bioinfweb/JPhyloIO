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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.junit.Assert;
import org.junit.Test;



public class CustomXMLReaderTest {
	/*
	 * grundsätzliches lesen des customXMl mit allen verfügbaren teilen
	 * lesen mit mehreren readern gleichzeitig/abwechselnd
	 * aus dokument und element metadaten nacheinander (damit beim erzeugen neuer reader nichts hängen bleibt aus vorherigen vorgängen)
	 * assert attributes and namespaces
	 */	
	@Test
	public void testMetaXMLEventReaderInNeXML() throws XMLStreamException, IOException {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/CustomXMLTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
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
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);			
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();				
			}
			
			customXMLReader = reader.createMetaXMLEventReader();			
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			String pre = assertNamespace(new QName("http://example.com/", ""), false, element);
			element = assertStartElement(new QName("http://example.com/", "nestedTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.com/", "attribute", pre), "false", element);
			assertStartElement(new QName("http://example.com/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "secondNested", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.com/", "nestedTag", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			element = assertStartElement(new QName("http://example.com/", "customTag", "ex"), customXMLReader);
			String ex = assertNamespace(new QName("http://example.com/", ""), false, element);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.com/", "customTag", ex), customXMLReader);
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
	public void testMetaXMLEventReaderInPhyloXML() throws IOException, XMLStreamException {
		PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/customXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {				
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLEventReader customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t" + "some more" + "\n\t\t\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertStartElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t" + "characters and even more" + "\n\t\t\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertStartElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
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
	public void testMetaXMLStreamReaderInNeXML() throws IOException, XMLStreamException {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/CustomXMLTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLStreamReader customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertCharactersEvent("\n\t\t" + "characters" + "\n\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", 0, customXMLReader);
			assertCharactersEvent("\n\t\t\t" + "some more" + "\n\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t", customXMLReader);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLReader);
			assertCharactersEvent("\n\t\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t" + "characters and even more" + "\n\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t", customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "false", 0, customXMLReader);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
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
		PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/customXMLReaderTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLStreamReader customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", 0, customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t" + "some more" + "\n\t\t\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t\t", customXMLReader);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t\t" + "characters and even more" + "\n\t\t\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "false", 0, customXMLReader);
			assertStartElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
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
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/CustomXMLTest.xml"), new ReadWriteParameterMap());
		try {
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLEventReader customXMLReader = reader.createMetaXMLEventReader();
			MetaXMLEventReader customXMLReader2 = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			
			assertCharactersEvent("\n\t\t" + "characters" + "\n\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t" + "some more" + "\n\t\t\t", customXMLReader2);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader2);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader2);
			assertCharactersEvent("\n\t\t\t" + "characters and even more" + "\n\t\t", customXMLReader2);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader2);
			assertCharactersEvent("\n\t", customXMLReader);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();				
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			customXMLReader2 = reader.createMetaXMLEventReader();
			XMLEventReader customXMLReader3 = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);  //TODO synchronize creating start document events between readers
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader2);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader3);
			assertStartElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader);
			assertCharactersEvent("nested content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "secondNested", "ex"), customXMLReader2);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader2);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertCharactersEvent("some content", customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
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
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/CustomXMLTest.xml"), new ReadWriteParameterMap());
		try {			
			// Skip format specific content
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLStreamReader customXMLReader = reader.createMetaXMLStreamReader();
			MetaXMLStreamReader customXMLReader2 = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);
			assertCharactersEvent("\n\t\t" + "characters" + "\n\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader2);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "true", 0, customXMLReader2);
			assertCharactersEvent("\n\t\t\t" + "some more" + "\n\t\t\t", customXMLReader);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t\t", customXMLReader2);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLReader);
			assertCharactersEvent("\n\t\t\t", customXMLReader);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertCharactersEvent("\n\t\t\t" + "characters and even more" + "\n\t\t", customXMLReader2);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader2);
			assertCharactersEvent("\n\t", customXMLReader2);
			assertEndDocument(customXMLReader);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}
			
			customXMLReader = reader.createMetaXMLStreamReader();
			customXMLReader2 = reader.createMetaXMLStreamReader();
			MetaXMLStreamReader customXMLReader3 = reader.createMetaXMLStreamReader();
			
			assertStartDocument(customXMLReader);  //TODO synchronize creating start document events between readers
			assertStartElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader2);
			assertStartElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader3);
			assertAttribute(new QName("http://example.org/", "attribute", "ex"), "false", 0, customXMLReader3);
			assertShortElement(new QName("http://example.org/", "secondNested", "ex"), "nested content", customXMLReader3);
			assertEndElement(new QName("http://example.org/", "nestedTag", "ex"), customXMLReader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), customXMLReader);
			assertEndDocument(customXMLReader2);
			
			// Skip format specific content
			event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
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
}
