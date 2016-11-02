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


import static info.bioinfweb.commons.testing.XMLAssert.assertAttribute;
import static info.bioinfweb.commons.testing.XMLAssert.assertAttributeCount;
import static info.bioinfweb.commons.testing.XMLAssert.assertCharactersEvent;
import static info.bioinfweb.commons.testing.XMLAssert.assertDefaultNamespace;
import static info.bioinfweb.commons.testing.XMLAssert.assertEndDocument;
import static info.bioinfweb.commons.testing.XMLAssert.assertEndElement;
import static info.bioinfweb.commons.testing.XMLAssert.assertNamespace;
import static info.bioinfweb.commons.testing.XMLAssert.assertNamespaceCount;
import static info.bioinfweb.commons.testing.XMLAssert.assertShortElement;
import static info.bioinfweb.commons.testing.XMLAssert.assertStartDocument;
import static info.bioinfweb.commons.testing.XMLAssert.assertStartElement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreOTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreObjectData;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreTreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreTreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLConstants;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventWriter;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLWriterStreamDataProvider;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLEventWriter;
import info.bioinfweb.jphyloio.formats.xml.receivers.AbstractXMLDataReceiver;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NoAnnotationsTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;



/**
 * customXML mit neuem NS der im StartElement als QName verwendet wird, danach Verwendung dieses neuen NS im character content(z.B. als QName) 
 * 	- möglichst unter Benutzung des NSContext vom Writer -, prefix von definiertem NS sollte im Konflikt zu einem bereits 
 * verwendeten stehen (z.B. "nex") und vom JPhyloIOWriter verändert werden
 * 
 * wird das Überschreiben von prefixes (per NS event oder setPrefix()) wieder rückgängig gemacht nach dem zugehörigen end element im custom XML?
 * 
 * use different instances of writers
 * 
 * test stream and event writer
 * 
 */

public class MetaXMLEventWriterTest implements ReadWriteConstants, NeXMLConstants, PhyloXMLConstants {


	@Test
	public void testMetaXMLWriterInNeXML() throws Exception {
		File file = new File("data/testOutput/NeXMLCustomXMLTest.xml");
		
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter() {
			@Override
			public void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver) throws IOException {
				MetaXMLEventWriter<NeXMLWriterStreamDataProvider> customXMLEventWriter = new MetaXMLEventWriter(((AbstractXMLDataReceiver)receiver));
				
				receiver.add(new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + 0, null, 
						new URIOrStringIdentifier(null, new QName("http://test.com/", "testPredicate", "test")), LiteralContentSequenceType.XML));
				
				try {
					XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(new File("data/XML/shortTestXML.xml")));
					customXMLEventWriter.add(reader);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
			}
		};
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX;		
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, otuListID, "taxonlist"), null) {
			@Override
			public void writeContentData(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, String id)
					throws IOException, IllegalArgumentException {
				
				if (id.equals(DEFAULT_OTU_ID_PREFIX + "2")) {
					MetaXMLEventWriter<NeXMLWriterStreamDataProvider> customXMLEventWriter = new MetaXMLEventWriter(((AbstractXMLDataReceiver)receiver));
					XMLEventFactory factory = XMLEventFactory.newInstance();
					
					receiver.add(new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + 1, null, 
							new URIOrStringIdentifier(null, new QName("http://test.com/", "testPredicate", "test")), LiteralContentSequenceType.XML));
					
					try {
						customXMLEventWriter.add(factory.createStartDocument());
						customXMLEventWriter.add(factory.createStartElement(new QName("http://example.org/", "test_tag", "p"), Collections.emptyIterator(), Collections.emptyIterator()));
						customXMLEventWriter.add(factory.createNamespace("p", "http://example.org/"));				
						customXMLEventWriter.add(factory.createCharacters("some content"));
						customXMLEventWriter.add(factory.createEndElement("p", "http://example.org/", "test_tag"));
					}
					catch (XMLStreamException e) {
						e.printStackTrace();
					}	
					
					receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				}
				else {
					super.writeContentData(parameters, receiver, id);
				}
			}
			

			@Override
			public void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver) throws IOException {
				MetaXMLEventWriter<NeXMLWriterStreamDataProvider> customXMLEventWriter = new MetaXMLEventWriter(((AbstractXMLDataReceiver)receiver));
				XMLEventFactory factory = XMLEventFactory.newInstance();
				
				receiver.add(new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + 2, null, 
						new URIOrStringIdentifier(null, new QName("http://test.com/", "testPredicate", "test")), LiteralContentSequenceType.XML));
				
				try {
					customXMLEventWriter.add(factory.createStartDocument());
					customXMLEventWriter.add(factory.createStartElement(new QName("http://example.com/", "some_tag", "p"), Collections.emptyIterator(), Collections.emptyIterator()));
					customXMLEventWriter.add(factory.createNamespace("p", "http://example.com/"));
					customXMLEventWriter.add(factory.createAttribute(new QName("http://example.com/", "attribute_one", "p"), "something"));
					customXMLEventWriter.add(factory.createAttribute(new QName("http://example.com/", "attribute_two", "p"), "20"));
					customXMLEventWriter.add(factory.createStartElement(new QName("http://example.com/", "another_tag", "p"), Collections.emptyIterator(), Collections.emptyIterator()));
					customXMLEventWriter.add(factory.createCharacters("some content"));
					customXMLEventWriter.add(factory.createEndElement("p", "http://example.com/", "another_tag"));
					customXMLEventWriter.add(factory.createEndElement("p", "http://example.com/", "some_tag"));
				}
				catch (XMLStreamException e) {
					e.printStackTrace();
				}	
				
				receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
			}
		};
		
		for (int i = 0; i < 5; i++) {
			String otuID = DEFAULT_OTU_ID_PREFIX + i;
			otuList.getOtus().getObjectMap().put(otuID, new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
					otuID, "taxon" + i), null));			
		}
		
		document.getOTUListsMap().put(otuListID, otuList);
		
		// Write file:
		NeXMLEventWriter writer = new NeXMLEventWriter();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
		parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, false);
		writer.writeDocument(document, file, parameters);
		
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		
		try {
			// Validate written file
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(NeXMLConstants.TAG_ROOT, reader);
			assertNamespaceCount(5, element);
			assertDefaultNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			String nexPrefix = assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_NAMESPACE_PREFIX), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);			
			String prefix = assertNamespace(new QName("http://test.com/", "test", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
			assertAttributeCount(2, element);
			assertAttribute(ATTR_VERSION, "0.9", element);
			
			String generator = assertAttribute(ATTR_GENERATOR, element);
			assertTrue(generator, generator.matches(
					"exampleApplication 1.0 using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+"));
			
			assertTrue(reader.hasNext());		
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by exampleApplication 1.0 <http://www.exampleApplication.com> using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(3, element);
			assertAttribute(NeXMLConstants.ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix + XMLUtils.QNAME_SEPARATOR + "testPredicate", element);
			
			element = assertStartElement(new QName("http://example.org/", "customTag", "ex"), reader);
			assertNamespace(new QName("http://example.org/", "", "ex"), true, element);
			assertNamespace(new QName("http://example.com/", "", "pre"), true, element);
			assertNamespaceCount(2, element);
			assertCharactersEvent("\n\t", reader);
			element = assertStartElement(new QName("http://example.com/", "nestedTag", "pre"), reader);
			assertAttribute(new QName("http://example.com/", "attribute", "pre"), "false", element);
			assertAttributeCount(1, element);
			assertCharactersEvent("\n\t\t", reader);
			assertStartElement(new QName("http://example.com/", "secondNested", "pre"), reader);
			assertCharactersEvent("nested content", reader);
			assertEndElement(new QName("http://example.com/", "secondNested", "pre"), reader);
			assertCharactersEvent("\n\t", reader);
			assertEndElement(new QName("http://example.com/", "nestedTag", "pre"), reader);
			assertCharactersEvent("\n", reader);
			assertEndElement(new QName("http://example.org/", "customTag", "ex"), reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_OTUS, reader);
			assertAttributeCount(3, element);
			assertAttribute(NeXMLConstants.ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "taxonlist", element);			
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(3, element);
			assertAttribute(NeXMLConstants.ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix + XMLUtils.QNAME_SEPARATOR + "testPredicate", element);
			
			element = assertStartElement(new QName("http://example.com/", "some_tag", "p"), reader);
			assertNamespace(new QName("http://example.com/", "", "p"), true, element);
			assertNamespaceCount(1, element);
			assertAttribute(new QName("http://example.com/", "attribute_one", "p"), "something", element);
			assertAttribute(new QName("http://example.com/", "attribute_two", "p"), "20", element);
			assertAttributeCount(2, element);
			assertStartElement(new QName("http://example.com/", "another_tag", "p"), reader);
			assertCharactersEvent("some content", reader);
			assertEndElement(new QName("http://example.com/", "another_tag", "p"), reader);
			assertEndElement(new QName("http://example.com/", "some_tag", "p"), reader);
			
			assertEndElement(TAG_META, reader);
			
			String[] otuIDs = new String[6];
			for (int i = 0; i < 5; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				otuIDs[i] = assertAttribute(NeXMLConstants.ATTR_ID, element);
				assertAttribute(ATTR_ABOUT, element);
				assertAttribute(ATTR_LABEL, "taxon" + i, element);
				
				if (i == 2) {
					element = assertStartElement(TAG_META, reader);
					assertAttributeCount(3, element);
					assertAttribute(NeXMLConstants.ATTR_ID, element);
					assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
					assertAttribute(ATTR_PROPERTY, prefix + XMLUtils.QNAME_SEPARATOR + "testPredicate", element);
					
					element = assertStartElement(new QName("http://example.org/", "test_tag", "p"), reader);
					assertNamespace(new QName("http://example.org/", "", "p"), true, element);
					assertNamespaceCount(1, element);					
					assertCharactersEvent("some content", reader);
					assertEndElement(new QName("http://example.org/", "test_tag", "p"), reader);
					
					assertEndElement(TAG_META, reader);
				}
				
				assertEndElement(TAG_OTU, reader);
			}
			
			assertEndElement(TAG_OTUS, reader);			
			
			assertEndElement(NeXMLConstants.TAG_ROOT, reader);
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void testMetaXMLWriterInPhyloXML() throws Exception {
		File file = new File("data/testOutput/PhyloXMLCustomXMLTest.xml");
		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
		
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter() {
			@Override
			public void writeMetadata(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver) throws IOException {
				MetaXMLEventWriter<NeXMLWriterStreamDataProvider> customXMLEventWriter = new MetaXMLEventWriter(((AbstractXMLDataReceiver)receiver));
				XMLEventFactory factory = XMLEventFactory.newInstance();
				
				receiver.add(new ResourceMetadataEvent(DEFAULT_META_ID_PREFIX + 0, null, new URIOrStringIdentifier(null, PREDICATE_HAS_CUSTOM_XML), null, null));
				
				receiver.add(new LiteralMetadataEvent(DEFAULT_META_ID_PREFIX + 1, null, new URIOrStringIdentifier(null, new QName("http://test.com/", "testPredicate", "phy")), 
						LiteralContentSequenceType.XML));
				
				try {
					customXMLEventWriter.add(factory.createStartDocument());
					customXMLEventWriter.add(factory.createStartElement(new QName("http://example.com/", "some_tag", "p"), Collections.emptyIterator(), Collections.emptyIterator()));
					customXMLEventWriter.add(factory.createNamespace("p", "http://example.com/"));
					customXMLEventWriter.add(factory.createAttribute(new QName("http://example.com/", "attribute_one", "p"), "something"));
					customXMLEventWriter.add(factory.createAttribute(new QName("http://example.com/", "attribute_two", "p"), "20"));
					customXMLEventWriter.add(factory.createStartElement(new QName("http://example.com/", "another_tag", "p"), Collections.emptyIterator(), Collections.emptyIterator()));
					customXMLEventWriter.add(factory.createCharacters("some content"));
					customXMLEventWriter.add(factory.createEndElement("p", "http://example.com/", "another_tag"));
					customXMLEventWriter.add(factory.createEndElement("p", "http://example.com/", "some_tag"));
				}
				catch (XMLStreamException e) {
					e.printStackTrace();
				}
				
				receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
			}
		};
		
		String treeGroupID = ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + 0;
		StoreTreeNetworkGroupDataAdapter treeGroupAdapter = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				treeGroupID, null, null), null);
		StoreTreeNetworkDataAdapter treeAdapter = new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX, null, "nodeEdgeID");
		
		treeGroupAdapter.getTreesAndNetworks().add(treeAdapter);
		document.getTreesNetworks().add(treeGroupAdapter);
		
		// Write file:
		PhyloXMLEventWriter writer = new PhyloXMLEventWriter();
		
		writer.writeDocument(document, file, parameters);
		
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		
		try {
			// Validate written file
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(PhyloXMLConstants.TAG_ROOT, reader);
			assertNamespaceCount(6, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
			assertNamespace(new QName(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix = assertNamespace(new QName("http://example.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			element = assertStartElement(TAG_PHYLOGENY, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ROOTED, "true", element);
			assertAttribute(ATTR_BRANCH_LENGTH_UNIT, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_DOUBLE.getLocalPart(), element);
			
			assertShortElement(TAG_ID, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "1.5", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "1.0", element);
			assertShortElement(TAG_NAME, "Node '_1", reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "1.1", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			assertEndElement(TAG_CLADE, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "0.9", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
			assertEndElement(TAG_CLADE, reader);
			
			assertEndElement(TAG_CLADE, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "2.0", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
			assertEndElement(TAG_CLADE, reader);
			
			assertEndElement(TAG_CLADE, reader);
			assertEndElement(TAG_PHYLOGENY, reader);
					
			element = assertStartElement(new QName("http://example.com/", "some_tag", prefix), reader);
			assertAttribute(new QName("http://example.com/", "attribute_one", prefix), "something", element);
			assertAttribute(new QName("http://example.com/", "attribute_two", prefix), "20", element);
			assertAttributeCount(2, element);
			assertStartElement(new QName("http://example.com/", "another_tag", prefix), reader);
			assertCharactersEvent("some content", reader);
			assertEndElement(new QName("http://example.com/", "another_tag", prefix), reader);
			assertEndElement(new QName("http://example.com/", "some_tag", prefix), reader);
			
			assertEndElement(PhyloXMLConstants.TAG_ROOT, reader);			
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
}