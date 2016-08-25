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


import static info.bioinfweb.commons.testing.XMLAssert.*;
import static org.junit.Assert.*;

import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
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
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NetworkDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NoAnnotationsTree;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.PhyloXMLEdgeAndNodeMetadataTreeAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.PhyloXMLSpecificMetadataTreeAdapter;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
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
	
	
	private void writeDocument(StoreDocumentDataAdapter document, ReadWriteParameterMap parameters, File file) throws IOException {
		PhyloXMLEventWriter writer = new PhyloXMLEventWriter();
		
		if (parameters == null) {
			parameters = new ReadWriteParameterMap();
		}
		
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
		
		writeDocument(document, null, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNamespaceCount(3, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), 
					true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), 
					true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			
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
	public void assertDocumentWithMetadataStrategyNone() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		// Add document metadata
		document.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null));
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		document.getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		// Add treegroup
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), new ArrayList<JPhyloIOEvent>());
		PhyloXMLEdgeAndNodeMetadataTreeAdapter tree = new PhyloXMLEdgeAndNodeMetadataTreeAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID");
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_METADATA_TREATMENT, PhyloXMLMetadataTreatment.NONE);		
		
		writeDocument(document, parameters, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(7, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			assertNamespace(new QName(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
	public void assertDocumentWithMetadataStrategyOnlyLeafs() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		// Add document metadata
		document.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null));
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		document.getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		// Add treegroup
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), new ArrayList<JPhyloIOEvent>());
		PhyloXMLEdgeAndNodeMetadataTreeAdapter tree = new PhyloXMLEdgeAndNodeMetadataTreeAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID");
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_METADATA_TREATMENT, PhyloXMLMetadataTreatment.ONLY_LEAFS);
		writeDocument(document, parameters, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(7, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			assertNamespace(new QName(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix1 = assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix2 = assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix3 = assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_INT.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_NODE, element);
			assertCharactersEvent("100", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_NODE, element);
			assertCharactersEvent("200", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "1.1", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PARENT_BRANCH, element);
			assertCharactersEvent("edge meta", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_INT.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PARENT_BRANCH, element);
			assertCharactersEvent("100", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_ANY_URI.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("www.test.de", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("myValue", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);			
			
			assertEndElement(TAG_PHYLOGENY, reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);
			
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
	public void assertDocumentWithMetadataStrategySequential() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		// Add document metadata
		document.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null));
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		document.getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		// Add treegroup
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), new ArrayList<JPhyloIOEvent>());
		PhyloXMLEdgeAndNodeMetadataTreeAdapter tree = new PhyloXMLEdgeAndNodeMetadataTreeAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID");
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_METADATA_TREATMENT, PhyloXMLMetadataTreatment.SEQUENTIAL);
		writeDocument(document, parameters, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(7, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			assertNamespace(new QName(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix1 = assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix2 = assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix3 = assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_INT.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_NODE, element);
			assertCharactersEvent("100", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_NODE, element);
			assertCharactersEvent("200", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "1.1", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PARENT_BRANCH, element);
			assertCharactersEvent("edge meta", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_INT.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PARENT_BRANCH, element);
			assertCharactersEvent("100", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_ANY_URI.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("www.test.de", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_ANY_URI.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("www.test2.de", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("myValue", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);			
			
			assertEndElement(TAG_PHYLOGENY, reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);
			
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
	public void assertDocumentWithMetadataStrategyTopWithChildren() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		// Add document metadata
		document.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null));
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		document.getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		// Add treegroup
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), new ArrayList<JPhyloIOEvent>());
		PhyloXMLEdgeAndNodeMetadataTreeAdapter tree = new PhyloXMLEdgeAndNodeMetadataTreeAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID");
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_METADATA_TREATMENT, PhyloXMLMetadataTreatment.TOP_LEVEL_WITH_CHILDREN);
		writeDocument(document, parameters, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(7, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			assertNamespace(new QName(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix1 = assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix2 = assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_ANY_URI.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("www.test2.de", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//		assertAttributeCount(1, element);
//		assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//				XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);
			
			assertEndElement(TAG_PHYLOGENY, reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//		assertAttributeCount(1, element);
//		assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//				XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);
			
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
	public void assertDocumentWithMetadataStrategyTopWithoutChildren() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		// Add document metadata
		document.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null));
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		document.getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		// Add treegroup
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), new ArrayList<JPhyloIOEvent>());
		PhyloXMLEdgeAndNodeMetadataTreeAdapter tree = new PhyloXMLEdgeAndNodeMetadataTreeAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID");
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_METADATA_TREATMENT, PhyloXMLMetadataTreatment.TOP_LEVEL_WITHOUT_CHILDREN);
		writeDocument(document, parameters, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(7, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			assertNamespace(new QName(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix1 = assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix2 = assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix3 = assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_INT.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_NODE, element);
			assertCharactersEvent("100", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_NODE, element);
			assertCharactersEvent("200", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "1.1", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PARENT_BRANCH, element);
			assertCharactersEvent("edge meta", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix3 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_INT.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PARENT_BRANCH, element);
			assertCharactersEvent("100", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_ANY_URI.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("www.test.de", reader);
			assertEndElement(TAG_PROPERTY, reader);			
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);			
			
			assertEndElement(TAG_PHYLOGENY, reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);
			
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
	public void assertSingleTreeDocumentWithPhyloXMLSpecificMetadata() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file:
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
		
		trees.getTreesAndNetworks().add(new PhyloXMLSpecificMetadataTreeAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, null, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNamespaceCount(7, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			assertNamespace(new QName(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			assertNamespace(new QName(PHYLOXML_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix1 = assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix2 = assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);			
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			element = assertStartElement(TAG_PHYLOGENY, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ROOTED, "true", element);
			assertAttribute(ATTR_BRANCH_LENGTH_UNIT, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_DOUBLE.getLocalPart(), element);
			
			element = assertStartElement(TAG_ID, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_ID_PROVIDER, "NCBI", element);
			assertCharactersEvent("phylogeny1", reader);
			assertEndElement(TAG_ID, reader);
			
			assertShortElement(TAG_DESCRIPTION, "example tree", reader);
			
			element = assertStartElement(TAG_CONFIDENCE, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_TYPE, "bootstrap", element);
			assertCharactersEvent("0.6", reader);
			assertEndElement(TAG_CONFIDENCE, reader);
			
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
			
			assertStartElement(TAG_TAXONOMY, reader);			
			assertShortElement(TAG_SCI_NAME, "Mus musculus", reader);
			
			element = assertStartElement(TAG_URI, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_DESC, "Some URI", element);
			assertCharactersEvent("http://www.some-uri.com", reader);
			assertEndElement(TAG_URI, reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "XMLPredicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);
			
			assertEndElement(TAG_TAXONOMY, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_NODE, element);
			assertAttribute(ATTR_ID_REF, "someID", element);
			assertCharactersEvent("myValue", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "1.1", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
			assertShortElement(TAG_BRANCH_WIDTH, "7.3", reader);
			
			assertStartElement(TAG_BRANCH_COLOR, reader);
			assertShortElement(TAG_RED, "45", reader);
			assertShortElement(TAG_GREEN, "210", reader);
			assertShortElement(TAG_BLUE, "78", reader);
			assertEndElement(TAG_BRANCH_COLOR, reader);
			
			assertEndElement(TAG_CLADE, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "0.9", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix2), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "XMLPredicate", element);
			assertCharactersEvent("test characters", reader);
			assertEndElement(new QName("http://test.com/", "customTest", prefix2), reader);
			
			assertEndElement(TAG_CLADE, reader);			
			
			assertEndElement(TAG_CLADE, reader);
			
			element = assertStartElement(TAG_CLADE, reader);
			assertAttributeCount(2, element);
			assertAttribute(ATTR_ID_SOURCE, element);
			assertAttribute(ATTR_BRANCH_LENGTH, "2.0", element);
			assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
			assertEndElement(TAG_CLADE, reader);
			
			assertEndElement(TAG_CLADE, reader);
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("myValue", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			assertEndElement(TAG_PHYLOGENY, reader);
			
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
	public void assertSingleTreeDocumentWithPhyloXMLSpecificMetadataWrongOrder() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file:
		try {
			idIndex = 1;
			StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
			StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
					EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
			
			NoAnnotationsTree tree = new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, 
					"nodeEdgeID"); // Does not contain any meta data, but it is possible to add tree meta data manually
			
			// Add meta events with PhyloXML-specific predicates to tree			
			tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent("bootstrap", "bootstrap"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent(0.6, "0.6"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_DESCRIPTION), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent("example tree", "example tree"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
			
			trees.getTreesAndNetworks().add(tree);
			document.getTreesNetworks().add(trees);
			
			writeDocument(document, null, file);
			fail("Exception not thrown");
		}
		catch (InconsistentAdapterDataException e) {
			assertEquals(e.getMessage(), "Metaevents with PhyloXML-specific predicates must be given in the correct order. Attributes can only be written once.");
		}
		finally {
			file.delete();
		}
	}
	
	
	@Test
	public void assertSingleTreeDocumentWithPhyloXMLSpecificMetadataWrongContent() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file:
		try {
			idIndex = 1;
			StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
			StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
					EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
			
			NoAnnotationsTree tree = new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, 
					"nodeEdgeID"); // Does not contain any meta data, but it is possible to add tree meta data manually
			
			// Add meta events with PhyloXML-specific predicates to tree
			tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent("bootstrap", "bootstrap"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_COLOR), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent(new Color(34, 38, 210), "color"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
			
			trees.getTreesAndNetworks().add(tree);
			document.getTreesNetworks().add(trees);
			
			writeDocument(document, null, file);
			fail("Exception not thrown");
		}
		catch (InconsistentAdapterDataException e) {
			assertTrue(e.getMessage().matches("The meta event \"\\S+\" with the PhyloXML-specific predicate \"\\S+\" was not nested correctly."));			
		}
		finally {
			file.delete();
		}
	}
	
	
	@Test
	public void assertSingleTreeDocumentWithPhyloXMLSpecificMetadataMultipleChildren() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file:
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
		
		NoAnnotationsTree tree = new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, 
				"nodeEdgeID"); // Does not contain any meta data, but it is possible to add tree meta data manually
		
		// Add meta events with PhyloXML-specific predicates to tree			
		tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null));

		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		tree.getAnnotations().add(new LiteralMetadataContentEvent("bootstrap", "bootstrap"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
				LiteralContentSequenceType.SIMPLE));
		tree.getAnnotations().add(new LiteralMetadataContentEvent(0.6, "0.6"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null));

		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		tree.getAnnotations().add(new LiteralMetadataContentEvent("jack-knife", "jack-knife"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
				LiteralContentSequenceType.SIMPLE));
		tree.getAnnotations().add(new LiteralMetadataContentEvent(0.9, "0.9"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, null, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNamespaceCount(4, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			assertNamespace(new QName(PHYLOXML_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);			
			
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
			
			element = assertStartElement(TAG_CONFIDENCE, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_TYPE, "bootstrap", element);
			assertCharactersEvent("0.6", reader);
			assertEndElement(TAG_CONFIDENCE, reader);
			
			element = assertStartElement(TAG_CONFIDENCE, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_TYPE, "jack-knife", element);
			assertCharactersEvent("0.9", reader);
			assertEndElement(TAG_CONFIDENCE, reader);
			
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
	public void assertSingleTreeDocumentWithMetadataInvalidDatatype() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file:
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
		
		NoAnnotationsTree tree = new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, 
				"nodeEdgeID"); // Does not contain any meta data, but it is possible to add tree meta data manually
			
		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, DATA_TYPE_RANK), 
				LiteralContentSequenceType.SIMPLE));
		tree.getAnnotations().add(new LiteralMetadataContentEvent("family", "family"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, null, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNamespaceCount(4, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			String prefix = assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_STRING.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("family", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			assertEndElement(TAG_PHYLOGENY, reader);
			
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
	public void assertDocumentWithMetadataValidDatatypeNoTranslator() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();	
		
		// Add treegroup
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), new ArrayList<JPhyloIOEvent>());
		NoAnnotationsTree tree = new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID");
		
		Duration duration = null;
		try {
			duration = DatatypeFactory.newInstance().newDuration(true, 5, 2, 10, 0, 0, 0);			
		}
		catch (DatatypeConfigurationException e) {}
		
		tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DURATION), 
				LiteralContentSequenceType.SIMPLE));	
		tree.getAnnotations().add(new LiteralMetadataContentEvent(duration, "P5Y2M10D"));
		tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		trees.getTreesAndNetworks().add(tree);
		document.getTreesNetworks().add(trees);
		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_METADATA_TREATMENT, PhyloXMLMetadataTreatment.ONLY_LEAFS);
		writeDocument(document, parameters, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(4, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			String prefix1 = assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			
			element = assertStartElement(TAG_PROPERTY, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_REF, prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertAttribute(ATTR_DATATYPE, XMLReadWriteUtils.XSD_DEFAULT_PRE + XMLUtils.QNAME_SEPARATOR + W3CXSConstants.DATA_TYPE_DURATION.getLocalPart(), element);
			assertAttribute(ATTR_APPLIES_TO, APPLIES_TO_PHYLOGENY, element);
			assertCharactersEvent("P5Y2M10D", reader);
			assertEndElement(TAG_PROPERTY, reader);
			
			assertEndElement(TAG_PHYLOGENY, reader);
			
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
	public void assertSingleTreeDocumentWithPhyloXMLSpecificMetadataMultipleAttributes() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file:
		try {
			idIndex = 1;
			StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
			StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
					EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
			
			NoAnnotationsTree tree = new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, 
					"nodeEdgeID"); // Does not contain any meta data, but it is possible to add tree meta data manually
			
			// Add meta events with PhyloXML-specific predicates to tree			
			tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent("bootstrap", "bootstrap"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
			
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent("jack-knife", "jack-knife"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent(0.6, "0.6"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
			
			trees.getTreesAndNetworks().add(tree);
			document.getTreesNetworks().add(trees);
			
			writeDocument(document, null, file);			
			fail("Exception not thrown");
		}
		catch (InconsistentAdapterDataException e) {
			assertEquals(e.getMessage(), "Metaevents with PhyloXML-specific predicates must be given in the correct order. Attributes can only be written once.");
		}
		finally {			
			file.delete();
		}
	}
	
	
	@Test
	public void assertSingleTreeDocumentWithPhyloXMLSpecificMetadataMultipleValues() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file:
		try {
			idIndex = 1;
			StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
			StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
					EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
			
			NoAnnotationsTree tree = new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, 
					"nodeEdgeID"); // Does not contain any meta data, but it is possible to add tree meta data manually
			
			// Add meta events with PhyloXML-specific predicates to tree			
			tree.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent("bootstrap", "bootstrap"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent(0.6, "0.6"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
			
			tree.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
					LiteralContentSequenceType.SIMPLE));
			tree.getAnnotations().add(new LiteralMetadataContentEvent(0.9, "0.9"));
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	
			tree.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
			
			trees.getTreesAndNetworks().add(tree);
			document.getTreesNetworks().add(trees);
			
			writeDocument(document, null, file);			
			fail("Exception not thrown");
		}
		catch (InconsistentAdapterDataException e) {
			assertEquals(e.getMessage(), "Metaevents with PhyloXML-specific predicates must be given in the correct order. Attributes can only be written once.");
		}
		finally {			
			file.delete();
		}
	}
	
	
	@Test
	public void assertEmptyDocument() throws IOException, XMLStreamException {
		// Write file
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		
		writeDocument(document, parameters, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		
		try {
			assertStartDocument(reader);
			
			StartElement element = assertStartElement(TAG_ROOT, reader);
			
			assertNamespaceCount(3, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			assertStartElement(TAG_PHYLOGENY, reader);
			assertEndElement(TAG_PHYLOGENY, reader);
			
			assertEndElement(TAG_ROOT, reader);
			
			assertEndDocument(reader);
			
			assertEquals(1, logger.getMessageList().size());
			assertEquals(ApplicationLoggerMessageType.WARNING, logger.getMessageList().get(0).getType());
			assertEquals("The document did not contain any data that could be written to the file.", logger.getMessageList().get(0).getMessage());
		}
		finally {
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
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		
		writeDocument(document, parameters, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		
		try {
			assertStartDocument(reader);
			
			StartElement element = assertStartElement(TAG_ROOT, reader);
			
			assertNamespaceCount(3, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));

			
			assertStartElement(TAG_PHYLOGENY, reader);
			assertEndElement(TAG_PHYLOGENY, reader);
			
			assertEndElement(TAG_ROOT, reader);
			
			assertEndDocument(reader);		
			
			assertEquals(1, logger.getMessageList().size());
			assertEquals(ApplicationLoggerMessageType.WARNING, logger.getMessageList().get(0).getType());
			assertEquals("The document did not contain any data that could be written to the file.", logger.getMessageList().get(0).getMessage());
		}
		finally {
			file.delete();
		}
	}
	
	
	@Test
	public void assertDocumentWithOnlyMetadata() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		// Add document metadata
		document.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null));
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		document.getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		writeDocument(document, null, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(6, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			assertNamespace(new QName(ReadWriteConstants.JPHYLOIO_PREDICATE_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), false, element);
			assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix = assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			element = assertStartElement(new QName("http://test.com/", "customTest", prefix), reader);
//			assertAttributeCount(1, element);
//			assertAttribute(new QName(XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getNamespaceURI(), XMLReadWriteUtils.ATTRIBUTE_RDF_PROPERTY.getLocalPart(), 
//					XMLReadWriteUtils.RDF_DEFAULT_PRE), prefix1 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertEndElement(new QName("http://test.com/", "customTest", prefix), reader);
			
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
	public void assertTreeAndNetworkDocument() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex(), null, null), null);
		
		trees.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID"));
		trees.getTreesAndNetworks().add(new NetworkDataAdapter(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, null, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNamespaceCount(3, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			
			assertTrue(reader.hasNext());
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+ <http://bioinfweb.info/JPhyloIO/>. "));
			
			// Validate tree
			validateSingleTree(reader);
			
			// Validate network
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
			assertAttributeCount(1, element);
			assertAttribute(ATTR_ID_SOURCE, element);
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
			
			element = assertStartElement(TAG_CLADE_RELATION, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID_REF_0, "nodeEdgeIDnB", element);
			assertAttribute(ATTR_ID_REF_1, "nodeEdgeIDnC", element);
			assertAttribute(ATTR_DISTANCE, "1.4", element);
			assertAttribute(ATTR_TYPE, TYPE_NETWORK_EDGE, element);
			assertEndElement(TAG_CLADE_RELATION, reader);
			
			assertEndElement(TAG_PHYLOGENY, reader);
			
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
		
		writeDocument(document, null, file);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			
			assertNamespaceCount(3, element);
			assertDefaultNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			
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
	}
}