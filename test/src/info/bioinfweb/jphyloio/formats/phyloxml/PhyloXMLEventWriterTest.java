/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.phyloxml;


import static info.bioinfweb.commons.testing.XMLAssert.assertAttribute;
import static info.bioinfweb.commons.testing.XMLAssert.assertAttributeCount;
import static info.bioinfweb.commons.testing.XMLAssert.assertEndDocument;
import static info.bioinfweb.commons.testing.XMLAssert.assertEndElement;
import static info.bioinfweb.commons.testing.XMLAssert.assertNameSpaceCount;
import static info.bioinfweb.commons.testing.XMLAssert.assertNamespace;
import static info.bioinfweb.commons.testing.XMLAssert.assertShortElement;
import static info.bioinfweb.commons.testing.XMLAssert.assertStartDocument;
import static info.bioinfweb.commons.testing.XMLAssert.assertStartElement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreTreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.EdgeAndNodeMetaDataTreeAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NoAnnotationsTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;



public class PhyloXMLEventWriterTest implements PhyloXMLConstants {
	private long idIndex = 1;
	
	
	public long getIDIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}
	
	
	private void writeDocument(StoreDocumentDataAdapter document, File file) throws IOException {
		PhyloXMLEventWriter writer = new PhyloXMLEventWriter();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_METADATA_TREATMENT, PhyloXMLMetadataTreatment.SEQUENTIAL);
		writer.writeDocument(document, file, parameters);
	}
	
	
	@Test
	public void assertSingleTreeDocument() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file:
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
		
		trees.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNameSpaceCount(3, element);
			assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			validateSingleTree(reader);
			
			assertEndElement(TAG_ROOT, reader);			
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void assertSingleTreeDocumentWithMetadata() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTestMeta.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		// Add metadata
		URI href = null;
		URI href2 = null;
		try {
			href = new URI("www.test.de");
			href2 = new URI("www.test2.de");
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		document.getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		document.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), href2, null));
		
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		// Add treegroup
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), new ArrayList<JPhyloIOEvent>());
		EdgeAndNodeMetaDataTreeAdapter tree = new EdgeAndNodeMetaDataTreeAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID");
		
		// Add tree metadata		
		tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), href, null));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), href2, null));		
		
		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		tree.getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		tree.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		tree.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		
		// Add metaevents with PhyloXML-specific predicates
		tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_ID), null, null));		
		
		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_ID_ATTR_PROVIDER), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		tree.getAnnotations().add(new LiteralMetadataContentEvent("NCBI", "NCBI"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING),
				LiteralContentSequenceType.SIMPLE));		
		tree.getAnnotations().add(new LiteralMetadataContentEvent("phylogeny1", "phylogeny1"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNameSpaceCount(6, element);
			assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), element);
			//TODO assert remaining ns
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			element = assertStartElement(TAG_PHYLOGENY, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ROOTED, "true", element);
			assertAttribute(ATTR_BRANCH_LENGTH_UNIT, "xsd:double", element);
			
			assertShortElement(TAG_ID, reader);
			
			assertStartElement(TAG_CLADE, reader);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
			assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
			assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
			
			assertStartElement(TAG_CLADE, reader);
			assertShortElement(TAG_NAME, "Node '_1", reader);
			assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
			assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
			
			assertStartElement(TAG_CLADE, reader);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
			assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
			assertEndElement(TAG_CLADE, reader);
			
			assertStartElement(TAG_CLADE, reader);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
			assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
			assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
			assertEndElement(TAG_CLADE, reader);
			
			assertEndElement(TAG_CLADE, reader);
			
			assertStartElement(TAG_CLADE, reader);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
			assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
			assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
			assertEndElement(TAG_CLADE, reader);
			
			assertEndElement(TAG_CLADE, reader);
			assertEndElement(TAG_PHYLOGENY, reader);
			
			assertEndElement(TAG_ROOT, reader);			
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close(); //TODO do this also in other tests
			reader.close();
//			file.delete();
		}
	}
	
	
	@Test
	public void assertEmptyDocument() throws IOException, XMLStreamException {
		// Write file
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		writeDocument(document, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			assertStartDocument(reader);
			
			StartElement element = assertStartElement(TAG_ROOT, reader);
			
			assertNameSpaceCount(3, element);
			assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			assertEndElement(TAG_ROOT, reader);			
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void assertNoTreesDocument() throws IOException, XMLStreamException {		
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);		
		
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			assertStartDocument(reader);
			
			StartElement element = assertStartElement(TAG_ROOT, reader);
			
			assertNameSpaceCount(3, element);
			assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			assertEndElement(TAG_ROOT, reader);			
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void assertMultipleTreesDocument() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
		
		trees.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID"));
		trees.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNameSpaceCount(3, element);
			assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			validateSingleTree(reader);			
			validateSingleTree(reader);
			
			assertEndElement(TAG_ROOT, reader);			
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void assertMultipleTreegroupsDocument() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		StoreTreeNetworkGroupDataAdapter trees1 = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
		trees1.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID"));		
		
		StoreTreeNetworkGroupDataAdapter trees2 = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
		trees1.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID"));
		
		document.getTreesNetworks().add(trees1);
		document.getTreesNetworks().add(trees2);
		
		writeDocument(document, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNameSpaceCount(3, element);
			assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			validateSingleTree(reader);			
			validateSingleTree(reader);
			
			assertEndElement(TAG_ROOT, reader);			
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
	
	private void validateSingleTree(XMLEventReader reader) throws IOException, XMLStreamException {
		StartElement element = assertStartElement(TAG_PHYLOGENY, reader);
		assertAttributeCount(2, element);
		assertAttribute(ATTR_ROOTED, "true", element);
		assertAttribute(ATTR_BRANCH_LENGTH_UNIT, "xsd:double", element);
		
		assertShortElement(TAG_ID, reader);
		
		assertStartElement(TAG_CLADE, reader);
		assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
		assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
		assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
		
		assertStartElement(TAG_CLADE, reader);
		assertShortElement(TAG_NAME, "Node '_1", reader);
		assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
		assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
		
		assertStartElement(TAG_CLADE, reader);
		assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
		assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
		assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
		assertEndElement(TAG_CLADE, reader);
		
		assertStartElement(TAG_CLADE, reader);
		assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
		assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
		assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
		assertEndElement(TAG_CLADE, reader);
		
		assertEndElement(TAG_CLADE, reader);
		
		assertStartElement(TAG_CLADE, reader);
		assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
		assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
		assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
		assertEndElement(TAG_CLADE, reader);
		
		assertEndElement(TAG_CLADE, reader);
		assertEndElement(TAG_PHYLOGENY, reader);
	}
}
