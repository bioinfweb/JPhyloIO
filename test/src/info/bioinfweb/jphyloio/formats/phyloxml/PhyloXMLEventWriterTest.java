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


import static org.junit.Assert.assertEquals;
import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.commons.testing.XMLTestTools;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreTreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.MultipleRootEdgesTree;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NoAnnotationsTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.junit.Test;



public class PhyloXMLEventWriterTest implements PhyloXMLConstants {
	private long idIndex = 1;
	
	
	public long getIdIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}
	
	
	private void writeDocument(StoreDocumentDataAdapter document, File file) throws IOException {
		PhyloXMLEventWriter writer = new PhyloXMLEventWriter();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();		
		writer.writeDocument(document, file, parameters);
	}
	
	
	@Test
	public void assertSingleTreeDocument() throws IOException, XMLStreamException {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();		
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIdIndex(), null, null), null);
		
		trees.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			StartElement element;
			
			XMLTestTools.assertStartDocument(reader);
			
			element = XMLTestTools.assertStartElement(TAG_ROOT, reader);
			XMLTestTools.assertNameSpaceCount(2, element);
			XMLTestTools.assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			XMLTestTools.assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			
			validateSingleTree("tree2", reader);
			
			XMLTestTools.assertEndElement(TAG_ROOT, reader);			
			
			XMLTestTools.assertEndDocument(reader);
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void assertMultiplyRootedTreeDocument()  throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		
		// Write file
		idIndex = 1;
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIdIndex(), null, null), null);		
		trees.getTreesAndNetworks().add(new MultipleRootEdgesTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		PhyloXMLEventWriter writer = new PhyloXMLEventWriter();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			StartElement element;
			
			XMLTestTools.assertStartDocument(reader);
			
			element = XMLTestTools.assertStartElement(TAG_ROOT, reader);
			XMLTestTools.assertNameSpaceCount(2, element);
			XMLTestTools.assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			XMLTestTools.assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			
			validateSingleTree("tree2", reader);
			
			XMLTestTools.assertEndElement(TAG_ROOT, reader);			
			
			XMLTestTools.assertEndDocument(reader);
			
			assertEquals(1, logger.getMessageList().size());
			assertEquals(ApplicationLoggerMessageType.WARNING, logger.getMessageList().get(0).getType());
			assertEquals("A tree definition contains more than one root edge, which is not supported "
					+ "by the PhyloXML format. Only the first root edge will be considered.", logger.getMessageList().get(0).getMessage());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
//	@Test
//	public void assertSingleTreeDocumentWithMetadata() throws IOException, XMLStreamException, FactoryConfigurationError {
//		File file = new File("data/testOutput/PhyloXMLTestMeta.xml");
//		
//		// Write file
//		idIndex = 1;
//		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
//		document.getAnnotations().add(new LiteralMetadataEvent("meta0", null, new URIOrStringIdentifier(null, new QName("meta")), "meta", LiteralContentSequenceType.SIMPLE));
//		document.getAnnotations().add(new LiteralMetadataContentEvent(new URIOrStringIdentifier(null, new QName("string")), "myValue", "myValue"));
//		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
//		
//		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(null, 
//				new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + getIdIndex(), null, null));		
//		trees.getTreesAndNetworks().add(new EdgeAndNodeMetaDataTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
//		document.getTreesNetworks().add(trees);
//		
//		writeDocument(document, file);
//		
//		// Validate file:
//		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
//		try {
//			StartElement element;
//			
//			XMLTestTools.assertStartDocument(reader);
//			
//			element = XMLTestTools.assertStartElement(TAG_ROOT, reader);
//			XMLTestTools.assertNameSpaceCount(2, element);
//			XMLTestTools.assertNamespace(new QName(NAMESPACE_URI, XMLConstants.XMLNS_ATTRIBUTE), element);
//			XMLTestTools.assertNamespace(new QName(NAMESPACE_XSI, XMLConstants.XMLNS_ATTRIBUTE, "xsi"), element);
//			
//			element = XMLTestTools.assertStartElement(TAG_PHYLOGENY, reader);
//			XMLTestTools.assertAttributeCount(2, element);
//			XMLTestTools.assertAttribute(ATTR_ROOTED, "true", element);
//			XMLTestTools.assertAttribute(ATTR_BRANCH_LENGTH_UNIT, "xs:double", element);
//			
//			XMLTestTools.assertShortElement(TAG_ID, "tree2", reader);
//			
//			XMLTestTools.assertStartElement(TAG_CLADE, reader);
//			XMLTestTools.assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
//			XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
//			XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
//			
//			XMLTestTools.assertStartElement(TAG_CLADE, reader);
//			XMLTestTools.assertShortElement(TAG_NAME, "Node '_1", reader);
//			XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
//			XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
//			
//			XMLTestTools.assertStartElement(TAG_CLADE, reader);
//			XMLTestTools.assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
//			XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
//			XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
//			XMLTestTools.assertEndElement(TAG_CLADE, reader);
//			
//			XMLTestTools.assertStartElement(TAG_CLADE, reader);
//			XMLTestTools.assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
//			XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
//			XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
//			XMLTestTools.assertEndElement(TAG_CLADE, reader);
//			
//			XMLTestTools.assertEndElement(TAG_CLADE, reader);
//			
//			XMLTestTools.assertStartElement(TAG_CLADE, reader);
//			XMLTestTools.assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
//			XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
//			XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
//			XMLTestTools.assertEndElement(TAG_CLADE, reader);
//			
//			XMLTestTools.assertEndElement(TAG_CLADE, reader);
//			XMLTestTools.assertEndElement(TAG_PHYLOGENY, reader);			
//			
//			//TODO assert property attributes XMLTestTools.assertShortElement(TAG_PROPERTY, "myValue", reader);
//			
//			XMLTestTools.assertEndElement(TAG_ROOT, reader);			
//			
//			XMLTestTools.assertEndDocument(reader);
//		}
//		finally {
//			reader.close();
//			file.delete();
//		}
//	}
	
	
	@Test
	public void assertEmptyDocument() throws IOException, XMLStreamException {
		// Write file
		File file = new File("data/testOutput/PhyloXMLTest.xml");
		StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			XMLTestTools.assertStartDocument(reader);
			
			StartElement element = XMLTestTools.assertStartElement(TAG_ROOT, reader);
			XMLTestTools.assertNameSpaceCount(2, element);
			XMLTestTools.assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			XMLTestTools.assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);		
			
			XMLTestTools.assertEndElement(TAG_ROOT, reader);			
			
			XMLTestTools.assertEndDocument(reader);
		}
		finally {
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
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIdIndex(), null, null), null);		
		
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			XMLTestTools.assertStartDocument(reader);
			
			StartElement element = XMLTestTools.assertStartElement(TAG_ROOT, reader);
			XMLTestTools.assertNameSpaceCount(2, element);
			XMLTestTools.assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			XMLTestTools.assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			
			XMLTestTools.assertEndElement(TAG_ROOT, reader);			
			
			XMLTestTools.assertEndDocument(reader);
		}
		finally {
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
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIdIndex(), null, null), null);
		
		trees.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		trees.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		document.getTreesNetworks().add(trees);
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			StartElement element;
			
			XMLTestTools.assertStartDocument(reader);
			
			element = XMLTestTools.assertStartElement(TAG_ROOT, reader);
			XMLTestTools.assertNameSpaceCount(2, element);
			XMLTestTools.assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			XMLTestTools.assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			
			validateSingleTree("tree2", reader);
			
			validateSingleTree("tree3", reader);
			
			XMLTestTools.assertEndElement(TAG_ROOT, reader);			
			
			XMLTestTools.assertEndDocument(reader);
		}
		finally {
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
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIdIndex(), null, null), null);
		trees1.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));		
		
		StoreTreeNetworkGroupDataAdapter trees2 = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(
				EventContentType.TREE_NETWORK_GROUP, ReadWriteConstants.DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIdIndex(), null, null), null);
		trees1.getTreesAndNetworks().add(new NoAnnotationsTree(ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex(), null, "nodeEdgeID"));
		
		document.getTreesNetworks().add(trees1);
		document.getTreesNetworks().add(trees2);
		
		writeDocument(document, file);
		
		// Validate file:
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(new FileReader(file));
		try {
			StartElement element;
			
			XMLTestTools.assertStartDocument(reader);
			
			element = XMLTestTools.assertStartElement(TAG_ROOT, reader);
			XMLTestTools.assertNameSpaceCount(2, element);
			XMLTestTools.assertNamespace(new QName(PHYLOXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			XMLTestTools.assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, 
					XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			
			validateSingleTree("tree2", reader);
			
			validateSingleTree("tree4", reader);
			
			XMLTestTools.assertEndElement(TAG_ROOT, reader);			
			
			XMLTestTools.assertEndDocument(reader);
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	private void validateSingleTree(String treeID, XMLEventReader reader) throws IOException, XMLStreamException {
		StartElement element = XMLTestTools.assertStartElement(TAG_PHYLOGENY, reader);
		XMLTestTools.assertAttributeCount(2, element);
		XMLTestTools.assertAttribute(ATTR_ROOTED, "true", element);
		XMLTestTools.assertAttribute(ATTR_BRANCH_LENGTH_UNIT, "xs:double", element);
		
		XMLTestTools.assertShortElement(TAG_ID, treeID, reader);
		
		XMLTestTools.assertStartElement(TAG_CLADE, reader);
		XMLTestTools.assertShortElement(TAG_NAME, "Node nodeEdgeIDnRoot", reader);
		XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "1.5", reader);
		XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnRoot", reader);
		
		XMLTestTools.assertStartElement(TAG_CLADE, reader);
		XMLTestTools.assertShortElement(TAG_NAME, "Node '_1", reader);
		XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "1.0", reader);
		XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDn1", reader);
		
		XMLTestTools.assertStartElement(TAG_CLADE, reader);
		XMLTestTools.assertShortElement(TAG_NAME, "Node nodeEdgeIDnA", reader);
		XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "1.1", reader);
		XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnA", reader);			
		XMLTestTools.assertEndElement(TAG_CLADE, reader);
		
		XMLTestTools.assertStartElement(TAG_CLADE, reader);
		XMLTestTools.assertShortElement(TAG_NAME, "Node nodeEdgeIDnB", reader);
		XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "0.9", reader);
		XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnB", reader);
		XMLTestTools.assertEndElement(TAG_CLADE, reader);
		
		XMLTestTools.assertEndElement(TAG_CLADE, reader);
		
		XMLTestTools.assertStartElement(TAG_CLADE, reader);
		XMLTestTools.assertShortElement(TAG_NAME, "Node nodeEdgeIDnC", reader);
		XMLTestTools.assertShortElement(TAG_BRANCH_LENGTH, "2.0", reader);
		XMLTestTools.assertShortElement(TAG_NODE_ID, "nodeEdgeIDnC", reader);
		XMLTestTools.assertEndElement(TAG_CLADE, reader);
		
		XMLTestTools.assertEndElement(TAG_CLADE, reader);
		XMLTestTools.assertEndElement(TAG_PHYLOGENY, reader);
	}
}
