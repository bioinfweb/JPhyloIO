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
package info.bioinfweb.jphyloio.formats.nexml;


import static info.bioinfweb.commons.testing.XMLAssert.*;
import static org.junit.Assert.*;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.commons.bio.SequenceUtils;
import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreMatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreOTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreObjectData;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreTreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreTreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.CharacterDefinitionEvent;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SetElementEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.EdgeAndNodeMetaDataTreeAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NetworkDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NoAnnotationsTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.junit.Test;



public class NeXMLEventWriterTest implements ReadWriteConstants, NeXMLConstants {
	private StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
	private ReadWriteParameterMap parameters = new ReadWriteParameterMap();
	private long idIndex = 0;
	
	
	public long obtainCurrentIDIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}
	
	
	@Test
	public void testWritingSimpleDocument() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest.xml");
		boolean writeMetadata = false;
		boolean writeSets = true;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID, writeMetadata, writeSets));
		
		// Add matrix to document data adapter
		StoreMatrixDataAdapter matrix = createDNASequenceMatrix(true, writeMetadata, writeSets, otuListID);
		document.getMatrices().add(matrix);
		
		// Add tree group to document data adapter
		document.getTreesNetworks().add(createTrees(otuListID, writeMetadata, true, writeSets));
		
		// Add metadata to document data adapter
		if (writeMetadata) {
			document.getAnnotations().addAll(createMetaData(null));
		}
			
		// Write file:
		NeXMLEventWriter writer = new NeXMLEventWriter();
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL_METADATA, TokenDefinitionLabelHandling.DISCARDED);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(5, element);
			assertDefaultNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
			assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/Formats/NeXML/Predicates/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			
			element = assertStartElement(TAG_OTUS, reader);
			assertAttributeCount(3, element);
			String otusID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "taxonlist", element);
			
			String[] otuIDs = new String[6];
			for (int i = 0; i < 5; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				otuIDs[i] = assertAttribute(ATTR_ID, element);
				assertAttribute(ATTR_ABOUT, element);
				assertAttribute(ATTR_LABEL, "taxon" + i, element);
				assertEndElement(TAG_OTU, reader);
			}
			
			element = assertStartElement(TAG_OTU, reader);
			assertAttributeCount(3, element);
			otuIDs[5] = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "UNDEFINED OTU generated by JPhyloIO", element);
			assertEndElement(TAG_OTU, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, otuIDs[1] + " " + otuIDs[2] + " " + otuIDs[3] + " ", element);
			assertEndElement(TAG_SET, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, otuIDs[1] + " " + otuIDs[2] + " " + otuIDs[3] + " " + otuIDs[4] + " ", element);
			assertEndElement(TAG_SET, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, otuIDs[0] + " " + otuIDs[1] + " " + otuIDs[2] + " " + otuIDs[3] + " " + otuIDs[4] + " ", element);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_OTUS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:DnaSeqs", element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			String tokenSetID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenC = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "C", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenG = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "G", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenA = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "A", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenT = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "T", element);
			assertEndElement(TAG_STATE, reader);
			
			assertUncertainStateSet("B", null, reader, tokenC, tokenG, tokenT);
			assertUncertainStateSet("D", null, reader, tokenA, tokenG, tokenT);
			assertUncertainStateSet("H", null, reader, tokenA, tokenC, tokenT);
			assertUncertainStateSet("K", null, reader, tokenG, tokenT);
			assertUncertainStateSet("M", null, reader, tokenA, tokenC);
			assertUncertainStateSet("N", null, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("R", null, reader, tokenA, tokenG);
			assertUncertainStateSet("S", null, reader, tokenC, tokenG);
			assertUncertainStateSet("V", null, reader, tokenA, tokenC, tokenG);
			assertUncertainStateSet("W", null, reader, tokenA, tokenT);
			assertUncertainStateSet("X", null, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("Y", null, reader, tokenC, tokenT);
			assertUncertainStateSet("-", "gap", reader);
			assertUncertainStateSet("?", "missing data", reader, tokenC, tokenG, tokenA, tokenT);
			
			assertEndElement(TAG_STATES, reader);
			
			String char0 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			String char3 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			String char4 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			String char5 = assertCharacterDefinition(null, tokenSetID, null, null, false, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "character set", element);
			assertAttribute(ATTR_CHAR_SET_LINKED_IDS, char0 + " " + char3 + " " + char4 + " " + char5 + " ", element);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			String sequence1 = assertRow(null, otuIDs[0], "AGTGC", writeMetadata, reader);
			assertRow(null, otuIDs[1], "A-TCT", writeMetadata, reader);
			String sequence3 = assertRow(null, otuIDs[2], "AGTGT", writeMetadata, reader);
			assertRow(null, otuIDs[3], "CGC?C", writeMetadata, reader);
			String sequence5 = assertRow(null, otuIDs[4], "CATCGT", writeMetadata, reader);
			assertRow("sequence", otuIDs[5], "AGTCTA", writeMetadata, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);			
			assertAttribute(ATTR_SEQUENCE_SET_LINKED_IDS, sequence1 + " " + sequence3 + " " + sequence5 + " ", element);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_TREES, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "treesAndNetworks", element);
			assertAttribute(ATTR_OTUS, otuListID, element);
			
			element = assertStartElement(TAG_TREE, reader);
			assertAttributeCount(4, element);
			String tree1 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tree", element);
			assertAttribute(ATTR_XSI_TYPE, "nex:FloatTree", element);
			
			String node1 = assertNode(false, reader);
			String node2 = assertNode(true, reader);
			String node3 = assertNode(false, reader);
			String node4 = assertNode(false, reader);
			String node5 = assertNode(false, reader);
			
			element = assertStartElement(TAG_ROOTEDGE, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "Root edge", element);
			assertAttribute(ATTR_TARGET, node2, element);
			assertAttribute(ATTR_LENGTH, "1.5", element);
			assertEndElement(TAG_ROOTEDGE, reader);
			
			assertEdge("Internal edge", node2, node1, 1.0, reader);
			assertEdge("Leaf edge A", node1, node3, 1.1, reader);
			assertEdge("Leaf edge B", node1, node4, 0.9, reader);
			assertEdge("Leaf edge C", node2, node5, 2.0, reader);
			
			assertEndElement(TAG_TREE, reader);
			
			element = assertStartElement(TAG_NETWORK, reader);
			assertAttributeCount(4, element);
			String tree2 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "network", element);
			assertAttribute(ATTR_XSI_TYPE, "nex:FloatNetwork", element);
			
			node1 = assertNode(false, reader);
			node2 = assertNode(true, reader);
			node3 = assertNode(false, reader);
			node4 = assertNode(false, reader);
			node5 = assertNode(false, reader);
			
			element = assertStartElement(TAG_ROOTEDGE, reader);
			assertAttributeCount(5, element);
			String rootEdgeID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "Root edge", element);
			assertAttribute(ATTR_TARGET, node2, element);
			assertAttribute(ATTR_LENGTH, "1.5", element);
			assertEndElement(TAG_ROOTEDGE, reader);
			
			String edgeID = assertEdge("Internal edge", node2, node1, 1.0, reader);
			assertEdge("Leaf edge A", node1, node3, 1.1, reader);
			assertEdge("Leaf edge B", node1, node4, Double.NaN, reader);
			assertEdge("Leaf edge C", node2, node5, 2.0, reader);
			assertEdge("network edge", node4, node5, 1.4, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);			
			assertAttribute(ATTR_NODE_EDGE_SET_LINKED_NODE_IDS, node3 + " " + node4 + " ", element);
			assertAttribute(ATTR_NODE_EDGE_SET_LINKED_EDGE_IDS, edgeID + " ", element);
			assertAttribute(ATTR_NODE_EDGE_SET_LINKED_ROOTEDGE_IDS, rootEdgeID + " ", element);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_NETWORK, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);			
			assertAttribute(ATTR_TREE_SET_LINKED_TREE_IDS, tree1 + " ", element);
			assertAttribute(ATTR_TREE_SET_LINKED_NETWORK_IDS, tree2 + " ", element);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_TREES, reader);
			
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
	public void testWritingSimpleDocumentWithMetadata() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest.xml");
		boolean writeMetadata = true;
		boolean writeSets = true;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID, writeMetadata, writeSets));
		
		// Add matrix to document data adapter
		StoreMatrixDataAdapter matrix = createDNASequenceMatrix(false, writeMetadata, writeSets, otuListID);
		document.getMatrices().add(matrix);
		
		// Add tree group to document data adapter
		document.getTreesNetworks().add(createTrees(otuListID, writeMetadata, true, writeSets));
		
		// Add metadata to document data adapter
		if (writeMetadata) {
			document.getAnnotations().addAll(createMetaData(null));
		}
			
		// Write file:
		NeXMLEventWriter writer = new NeXMLEventWriter();
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL_METADATA, TokenDefinitionLabelHandling.DISCARDED);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, new URL("http://www.exampleApplication.com"));
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(12, element);
			assertDefaultNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);			
			assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/General/Predicates/", XMLConstants.XMLNS_ATTRIBUTE, 
					ReadWriteConstants.JPHYLOIO_PREDICATE_PREFIX), true, element);
			assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/General/Attributes/", XMLConstants.XMLNS_ATTRIBUTE, 
					ReadWriteConstants.JPHYLOIO_ATTRIBUTES_PREFIX), true, element);
			assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/Formats/NeXML/Predicates/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix2 = assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix3 = assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/Formats/Newick/DataTypes/", XMLConstants.XMLNS_ATTRIBUTE), false, element);		
			assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);			
			assertNamespace(new QName("www.another-test.net", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix6 = assertNamespace(new QName("http://meta.net/", XMLConstants.XMLNS_ATTRIBUTE), false, element);		
			
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
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:ResourceMeta", element);
			assertAttribute(ATTR_REL, prefix6 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);	
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix6 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			
			assertCommentEvent("This is a divided comment.", reader);			
			assertCharactersEvent("This is a long literal text", reader);
			
			assertEndElement(TAG_META, reader);
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:ResourceMeta", element);
			assertAttribute(ATTR_REL, prefix6 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			assertAttribute(ATTR_HREF, "somePath/#fragment", element);
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);	
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix6 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			
			assertStartElement(new QName("http://test.com/", "customTest", "pre"), reader);
			assertEndElement(new QName("http://test.com/", "customTest", "pre"), reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix6 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertAttribute(ATTR_DATATYPE, "xsd:QName", element);
			
			assertCharactersEvent("pre1:test2", reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "my string key", element);
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, "jpp:hasLiteralMetadata", element);
			assertAttribute(ATTR_DATATYPE, "xsd:integer", element);
			
			assertCharactersEvent("25", reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_OTUS, reader);
			assertAttributeCount(3, element);
			String otusID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "taxonlist", element);
			
			assertLiteralMeta(reader);
			
			String[] otuIDs = new String[6];
			for (int i = 0; i < 5; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				otuIDs[i] = assertAttribute(ATTR_ID, element);
				assertAttribute(ATTR_ABOUT, element);
				assertAttribute(ATTR_LABEL, "taxon" + i, element);
				
				assertLiteralMeta(reader);
				
				assertEndElement(TAG_OTU, reader);
			}			
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, otuIDs[1] + " " + otuIDs[2] + " " + otuIDs[3] + " ", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_SET, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, otuIDs[4] + " " + otuIDs[1] + " " + otuIDs[2] + " " + otuIDs[3] + " ", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_SET, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, otuIDs[4] + " " + otuIDs[0] + " " + otuIDs[1] + " " + otuIDs[2] + " " + otuIDs[3] + " ", element); //TODO ensure set elements are in correct order
			assertLiteralMeta(reader);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_OTUS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:DnaSeqs", element);
			
			assertLiteralMeta(reader);
			
			assertStartElement(TAG_FORMAT, reader);
			
			assertLiteralMeta(reader);
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			String tokenSetID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			assertLiteralMeta(reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenC = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "C", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenG = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "G", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenA = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "A", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenT = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "T", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_STATE, reader);
			
			assertUncertainStateSet("B", null, reader, tokenC, tokenG, tokenT); //TODO assert meta under state sets
			assertUncertainStateSet("D", null, reader, tokenA, tokenG, tokenT);
			assertUncertainStateSet("H", null, reader, tokenA, tokenC, tokenT);
			assertUncertainStateSet("K", null, reader, tokenG, tokenT);
			assertUncertainStateSet("M", null, reader, tokenA, tokenC);
			assertUncertainStateSet("N", null, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("R", null, reader, tokenA, tokenG);
			assertUncertainStateSet("S", null, reader, tokenC, tokenG);
			assertUncertainStateSet("V", null, reader, tokenA, tokenC, tokenG);
			assertUncertainStateSet("W", null, reader, tokenA, tokenT);
			assertUncertainStateSet("X", null, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("Y", null, reader, tokenC, tokenT);
			assertUncertainStateSet("-", "gap", reader);
			assertUncertainStateSet("?", "missing data", reader, tokenC, tokenG, tokenA, tokenT);
			
			assertEndElement(TAG_STATES, reader);
			
			String char0 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			String char3 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			String char4 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			String char5 = assertCharacterDefinition(null, tokenSetID, null, null, false, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "character set", element);
			assertAttribute(ATTR_CHAR_SET_LINKED_IDS, char0 + " " + char3 + " " + char4 + " " + char5 + " ", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertLiteralMeta(reader);
			String sequence1 = assertRow(null, otuIDs[0], "AGTGC", writeMetadata, reader);
			assertRow(null, otuIDs[1], "A-TCT", writeMetadata, reader);
			String sequence3 = assertRow(null, otuIDs[2], "AGTGT", writeMetadata, reader);
			assertRow(null, otuIDs[3], "CGC?C", writeMetadata, reader);
			String sequence5 = assertRow(null, otuIDs[4], "CATCGT", writeMetadata, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);			
			assertAttribute(ATTR_SEQUENCE_SET_LINKED_IDS, sequence1 + " " + sequence3 + " " + sequence5 + " ", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_TREES, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "treesAndNetworks", element);
			assertAttribute(ATTR_OTUS, otuListID, element);
			assertLiteralMeta(reader);
			
			element = assertStartElement(TAG_TREE, reader);
			assertAttributeCount(4, element);
			String tree1 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tree", element);
			assertAttribute(ATTR_XSI_TYPE, "nex:FloatTree", element);
			assertLiteralMeta(reader);
			
			element = assertStartElement(TAG_NODE, reader);
			assertAttributeCount(3, element);			
			String node1 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, element);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix2 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, "xsd:int", element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "a1", element);
			
			assertCharactersEvent("100", reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix2 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "a2", element);
			
			assertCharactersEvent("ab 'c", reader);
			
			assertEndElement(TAG_META, reader);
			
			assertEndElement(TAG_NODE, reader);			
			
			String node2 = assertNode(true, reader);
			String node3 = assertNode(false, reader);
			String node4 = assertNode(false, reader);
			String node5 = assertNode(false, reader);
			
			element = assertStartElement(TAG_ROOTEDGE, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "Root edge", element);
			assertAttribute(ATTR_TARGET, node2, element);
			assertAttribute(ATTR_LENGTH, "1.5", element);
			assertEndElement(TAG_ROOTEDGE, reader);
			
			assertEdge("Internal edge", node2, node1, 1.0, reader);
			
			element = assertStartElement(TAG_EDGE, reader);
			assertAttributeCount(6, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "Leaf edge A", element);
			assertAttribute(ATTR_SOURCE, node1, element);
			assertAttribute(ATTR_TARGET, node3, element);			
			assertAttribute(ATTR_LENGTH, Double.toString(1.1), element);			
	
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix2 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "splitString", element);
			
			assertCharactersEvent("ABCDEF", reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix2 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, prefix3 + XMLUtils.QNAME_SEPARATOR + "Array", element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "array", element);
			
			assertCharactersEvent("[100, abc]", reader);
			
			assertEndElement(TAG_META, reader);
			
			assertEndElement(TAG_EDGE, reader);

			assertEdge("Leaf edge B", node1, node4, 0.9, reader);
			assertEdge("Leaf edge C", node2, node5, 2.0, reader);
			
			assertEndElement(TAG_TREE, reader);
			
			element = assertStartElement(TAG_NETWORK, reader);
			assertAttributeCount(4, element);
			String tree2 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "network", element);
			assertAttribute(ATTR_XSI_TYPE, "nex:FloatNetwork", element);
			assertLiteralMeta(reader);
			
			node1 = assertNode(false, reader);
			node2 = assertNode(true, reader);
			node3 = assertNode(false, reader);
			node4 = assertNode(false, reader);
			node5 = assertNode(false, reader);
			
			element = assertStartElement(TAG_ROOTEDGE, reader);
			assertAttributeCount(5, element);
			String rootEdgeID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "Root edge", element);
			assertAttribute(ATTR_TARGET, node2, element);
			assertAttribute(ATTR_LENGTH, "1.5", element);
			assertEndElement(TAG_ROOTEDGE, reader);
			
			String edgeID = assertEdge("Internal edge", node2, node1, 1.0, reader);
			assertEdge("Leaf edge A", node1, node3, 1.1, reader);
			assertEdge("Leaf edge B", node1, node4, Double.NaN, reader);
			assertEdge("Leaf edge C", node2, node5, 2.0, reader);
			assertEdge("network edge", node4, node5, 1.4, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);			
			assertAttribute(ATTR_NODE_EDGE_SET_LINKED_NODE_IDS, node3 + " " + node4 + " ", element);
			assertAttribute(ATTR_NODE_EDGE_SET_LINKED_EDGE_IDS, edgeID + " ", element);
			assertAttribute(ATTR_NODE_EDGE_SET_LINKED_ROOTEDGE_IDS, rootEdgeID + " ", element);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_NETWORK, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);			
			assertAttribute(ATTR_TREE_SET_LINKED_TREE_IDS, tree1 + " ", element);
			assertAttribute(ATTR_TREE_SET_LINKED_NETWORK_IDS, tree2 + " ", element);
			assertLiteralMeta(reader);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_TREES, reader);
			
			assertEndElement(TAG_ROOT, reader);
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
	
	
	private void assertUncertainStateSet(String symbol, String label, XMLEventReader reader, String... memberID) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_UNCERTAIN, reader);	
		
		if (label != null) {
			assertAttributeCount(4, element);
			assertAttribute(ATTR_LABEL, label, element);
		}
		else {
			assertAttributeCount(3, element);
		}
		
		assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_ABOUT, element);
		assertAttribute(ATTR_SYMBOL, symbol, element);
		
		for (int i = 0; i < memberID.length; i++) {
			element = assertStartElement(TAG_MEMBER, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_STATE_SET_LINKED_IDS, memberID[i], element);
			assertEndElement(TAG_MEMBER, reader);
		}
		
		assertEndElement(TAG_UNCERTAIN, reader);
	}
	
	
	private String assertCharacterDefinition(String label, String states, String codonPosition, String tokens, boolean writeMetadata, XMLEventReader reader) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_CHAR, reader);
		int count = 3;
		
		String id = assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_ABOUT, element);
		assertAttribute(ATTR_STATES, states, element);
		
		if (label != null) {
			assertAttribute(ATTR_LABEL, label, element);
			count++;
		}
		if (codonPosition != null) {
			assertAttribute(ATTR_CODON_POSITION, codonPosition, element);
			count++;
		}
		if (tokens != null) {
			assertAttribute(ATTR_TOKENS, tokens, element);
			count++;
		}
		
		assertAttributeCount(count, element);
		
		if (writeMetadata) {
			assertLiteralMeta(reader);
		}
		assertEndElement(TAG_CHAR, reader);
		
		return id;
	}
	
	
	private String assertRow(String expectedSequenceLabel, String otuID, String expectedSequence, boolean writeMetadata, XMLEventReader reader) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_ROW, reader);		
		String id = assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_ABOUT, element);
		assertAttribute(ATTR_SINGLE_OTU_LINK, otuID, element);
		
		if (expectedSequenceLabel != null) {
			assertAttributeCount(4, element);
			assertAttribute(ATTR_LABEL, expectedSequenceLabel, element);
		}	
		else {
			assertAttributeCount(3, element);
		}
		
		if (writeMetadata) {
			assertLiteralMeta(reader);
		}
		
		assertStartElement(TAG_SEQ, reader);
		assertCharactersEvent(expectedSequence, reader);
		assertEndElement(TAG_SEQ, reader);
		
		assertEndElement(TAG_ROW, reader);
		
		return id;
	}
	
	
	private String assertNode(boolean root, XMLEventReader reader) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_NODE, reader);
		
		if (root) {
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ROOT, "true", element);
		}
		else {
			assertAttributeCount(3, element);
		}
		
		String id = assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_ABOUT, element);
		assertAttribute(ATTR_LABEL, element);
		
		assertEndElement(TAG_NODE, reader);
		
		return id;
	}
	
	
	private String assertEdge(String label, String expectedSourceID, String expectedtargetID, double expectedLength, XMLEventReader reader) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_EDGE, reader);		
		String id = assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_ABOUT, element);
		assertAttribute(ATTR_LABEL, label, element);
		assertAttribute(ATTR_SOURCE, expectedSourceID, element);
		assertAttribute(ATTR_TARGET, expectedtargetID, element);
		
		if (!Double.isNaN(expectedLength)) {
			assertAttributeCount(6, element);
			assertAttribute(ATTR_LENGTH, Double.toString(expectedLength), element);			
		}
		else {
			assertAttributeCount(5, element);
		}		
		
		assertEndElement(TAG_EDGE, reader);
		
		return id;
	}
	
	
	private void addLiteralMeta(List<JPhyloIOEvent> annotations, QName predicate) {
		if (predicate == null) {
			predicate = new QName("http://meta.net/", "predicate");
		}
		
		annotations.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), "simple literal meta", 
				new URIOrStringIdentifier(null, predicate), new URIOrStringIdentifier(null, 
				new QName(W3CXSConstants.DATA_TYPE_INTEGER.getNamespaceURI(), W3CXSConstants.DATA_TYPE_INTEGER.getLocalPart())), 
				LiteralContentSequenceType.SIMPLE));
		annotations.add(new LiteralMetadataContentEvent(25, "25"));		
		annotations.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	}
	
	
	private void assertLiteralMeta(XMLEventReader reader) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_META, reader);
		assertAttributeCount(5, element);
		assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_LABEL, "simple literal meta", element);		
		assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
		assertAttribute(ATTR_PROPERTY, "p:predicate", element);
		assertAttribute(ATTR_DATATYPE, "xsd:integer", element);
		
		assertCharactersEvent("25", reader);
		
		assertEndElement(TAG_META, reader);
	}
	
	
	private void addResourceMeta(List<JPhyloIOEvent> annotations, URIOrStringIdentifier resourcePredicate) {
		annotations.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
			resourcePredicate, null, null));		
		addLiteralMeta(annotations, null);
		annotations.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
	}
	
	
	private List<JPhyloIOEvent> createMetaData(String about) {
		List<JPhyloIOEvent> metaData = new ArrayList<JPhyloIOEvent>();
		URI example = null;
		
		try {
			example = new URI("somePath/#fragment");
		} 
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		metaData.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), null, about));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.SIMPLE));
		
		metaData.add(new CommentEvent("This is a ", true));
		metaData.add(new CommentEvent("divided comment.", false));
		
		metaData.add(new LiteralMetadataContentEvent("This is a long ", true));
		metaData.add(new LiteralMetadataContentEvent("literal text", false));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));		
		
		metaData.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), example, about));
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));
		
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, new QName(W3CXSConstants.DATA_TYPE_QNAME.getNamespaceURI(), W3CXSConstants.DATA_TYPE_QNAME.getLocalPart())), 
				LiteralContentSequenceType.SIMPLE));

		metaData.add(new LiteralMetadataContentEvent(new QName("www.another-test.net", "test2", "pre"), null));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier("my string key", null), new URIOrStringIdentifier(null, 
				new QName(W3CXSConstants.DATA_TYPE_INTEGER.getNamespaceURI(), W3CXSConstants.DATA_TYPE_INTEGER.getLocalPart())), 
				LiteralContentSequenceType.SIMPLE));
		metaData.add(new LiteralMetadataContentEvent(25, "25"));		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		return metaData;
	}
	
	
	private StoreOTUListDataAdapter createOTUList(String id, boolean writeMetaData, boolean writeSets) {
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, id, "taxonlist"), null);
		
		if (writeMetaData) {
			addLiteralMeta(otuList.getAnnotations(), null);
		}
		
		String[] otuIDs = new String[5];
		for (int i = 0; i < 5; i++) {
			String otuID = DEFAULT_OTU_ID_PREFIX + obtainCurrentIDIndex();
			otuIDs[i] = otuID;
			otuList.getOtus().getObjectMap().put(otuID, new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
					otuID, "taxon" + i), null));
			
			if (writeMetaData) {
				addLiteralMeta(otuList.getOtus().getObjectContent(otuID), null);
			}
		}
		
		if (writeSets) {
			String otuSetID = DEFAULT_OTU_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> otuSet = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetID, null, id));
			
			if (writeMetaData) {
				addLiteralMeta(otuSet.getObjectContent(), null);
			}
			
			otuSet.getObjectContent().add(new SetElementEvent(otuIDs[1], EventContentType.OTU));
			otuSet.getObjectContent().add(new SetElementEvent(otuIDs[2], EventContentType.OTU));
			otuSet.getObjectContent().add(new SetElementEvent(otuIDs[3], EventContentType.OTU));
			
			otuList.getOTUSets(parameters).getObjectMap().put(otuSetID, otuSet);
		
			// Add OTU set referencing another set
			String otuSetReferencingSetID = DEFAULT_OTU_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> otuSet2 = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetReferencingSetID, null, id));
			otuSet2.getObjectContent().add(new SetElementEvent(otuSetID, EventContentType.OTU_SET));		
			otuSet2.getObjectContent().add(new SetElementEvent(otuIDs[4], EventContentType.OTU));
			
			if (writeMetaData) {
				addLiteralMeta(otuSet2.getObjectContent(), null);
			}
			
			otuList.getOTUSets(parameters).getObjectMap().put(otuSetReferencingSetID, otuSet2);
			
			// Add OTU set referencing another set
			String otuSetReferencingSetID2 = DEFAULT_OTU_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> otuSet3 = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetReferencingSetID2, null, id));
			otuSet3.getObjectContent().add(new SetElementEvent(otuSetReferencingSetID, EventContentType.OTU_SET));		
			otuSet3.getObjectContent().add(new SetElementEvent(otuIDs[0], EventContentType.OTU));
			
			if (writeMetaData) {
				addLiteralMeta(otuSet3.getObjectContent(), null);
			}
			
			otuList.getOTUSets(parameters).getObjectMap().put(otuSetReferencingSetID2, otuSet3);
		}
		
		return otuList;
	}
	
	
	private StoreMatrixDataAdapter createDNASequenceMatrix(boolean addLinklessSeq, boolean writeMetadata, boolean writeSets, String otuListID) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otuListID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		if (writeMetadata) {
			addResourceMeta(matrix.getAnnotations(), new URIOrStringIdentifier(null, PREDICATE_FORMAT));
			addResourceMeta(matrix.getAnnotations(), new URIOrStringIdentifier(null, PREDICATE_MATRIX));
			addLiteralMeta(matrix.getAnnotations(), null);
		}
		
		// Add character definitions
		String charDefinitionID;
		for (long i = 0; i < 5; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getCharacterDefinitions(parameters).getObjectMap().put(charDefinitionID, createCharacterDefinition(charDefinitionID, i, writeMetadata));
		}		
		
		if (writeSets) {
			// Add token set of type DNA to matrix data adapter
			String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, CharacterStateSetType.DNA, writeMetadata));
			
			// Add char sets to matrix data adapter
			String charSetID = DEFAULT_CHAR_SET_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getCharacterSets(parameters).getObjectMap().put(charSetID, createCharSet(writeMetadata));
		}
			
		List<List<String>> sequences = new ArrayList<>();
		sequences.add(StringUtils.charSequenceToStringList("AGTGC"));
		sequences.add(StringUtils.charSequenceToStringList("A-TCT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTGT"));
		sequences.add(StringUtils.charSequenceToStringList("CGC?C"));
		sequences.add(StringUtils.charSequenceToStringList("CATCGT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTCTA"));		
		
		// Add sequences
		Iterator<String> iterator = document.getOTUList(parameters, otuListID).getIDIterator(parameters);
		int otuCount = 0;
		String[] sequenceIDs = new String[5];
		while (iterator.hasNext()) {
			String sequenceID = DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			sequenceIDs[otuCount] = sequenceID;
			matrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID, null,
					sequences.get(otuCount), document.getOTUList(parameters, otuListID).getObjectStartEvent(parameters, iterator.next()).getID(), writeMetadata));
			otuCount++;			
		}
		
		if (addLinklessSeq) {  // Add sequence that does not specify a linked OTU
			String undefinedOTUSequenceID = DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getMatrix().getObjectMap().put(undefinedOTUSequenceID, createSequence(undefinedOTUSequenceID, "sequence", sequences.get(5), null, writeMetadata));
		}
		
		if (writeSets) {
			String rowSetID = DEFAULT_SEQUENCE_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> rowSet = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.SEQUENCE_SET, rowSetID, null, otuListID));
			
			if (writeMetadata) {
				addLiteralMeta(rowSet.getObjectContent(), null);
			}
			
			rowSet.getObjectContent().add(new SetElementEvent(sequenceIDs[0], EventContentType.SEQUENCE));
			rowSet.getObjectContent().add(new SetElementEvent(sequenceIDs[2], EventContentType.SEQUENCE));
			rowSet.getObjectContent().add(new SetElementEvent(sequenceIDs[4], EventContentType.SEQUENCE));
			
			matrix.getSequenceSets(parameters).getObjectMap().put(rowSetID, rowSet);
		}
		
		return matrix;
	}
	
	
	private StoreObjectData<CharacterDefinitionEvent> createCharacterDefinition(String charDefinitionID, long index, boolean writeMetadata) {
		StoreObjectData<CharacterDefinitionEvent> characterDefinition = new StoreObjectData<CharacterDefinitionEvent>(
				new CharacterDefinitionEvent(charDefinitionID, "column definition", index), new ArrayList<JPhyloIOEvent>());
		
		addLiteralMeta(characterDefinition.getObjectContent(), PREDICATE_CHAR_ATTR_TOKENS);
		addLiteralMeta(characterDefinition.getObjectContent(), PREDICATE_CHAR_ATTR_CODON_POSITION);
		
		if (writeMetadata) {
			addLiteralMeta(characterDefinition.getObjectContent(), null);
		}
		
		return characterDefinition;
	}
	
	
	private StoreObjectData<LinkedLabeledIDEvent> createSequence(String id, String label, List<String> tokens, String otuID, boolean writeMetadata) {		
		StoreObjectData<LinkedLabeledIDEvent> sequence = new StoreObjectData<LinkedLabeledIDEvent>(new LinkedLabeledIDEvent(EventContentType.OTU, 
				id, label, otuID), null);
		
		sequence.getObjectContent().add(new SequenceTokensEvent(tokens));
		
		if (writeMetadata) {
			addLiteralMeta(sequence.getObjectContent(), null);
		}
		
		return sequence;
	}
	
	
	private StoreMatrixDataAdapter createCellsMatrix(ReadWriteParameterMap parameters, String otusID) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		// Add single tokens
		Iterator<String> iterator = document.getOTUList(parameters, otusID).getIDIterator(parameters);
		while (iterator.hasNext()) {
			String otuID = document.getOTUList(parameters, otusID).getObjectStartEvent(parameters, iterator.next()).getID();
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			
			StoreObjectData<LinkedLabeledIDEvent> singleTokens = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, sequenceID, "single token", otuID), null);
			
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "A", true);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "T", true);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "G", true);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "G", true);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "C", true);
			
			matrix.getMatrix().getObjectMap().put(sequenceID, singleTokens);
		}
		
		return matrix;
	}
	
	
	private StoreMatrixDataAdapter createContinuousCellsMatrix(String otusID) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "continuous data", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		// Add single tokens
		Iterator<String> iterator = document.getOTUList(parameters, otusID).getIDIterator(parameters);
		while (iterator.hasNext()) {
			String otuID = document.getOTUList(parameters, otusID).getObjectStartEvent(parameters, iterator.next()).getID();
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			
			StoreObjectData<LinkedLabeledIDEvent> singleTokens = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, sequenceID, "single token", otuID), null);
			
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "0.64566", true);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "0.66673", true);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "0.34454", true);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "5.98678", true);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, "-5.43334", true);
			
			matrix.getMatrix().getObjectMap().put(sequenceID, singleTokens);
		}
		
		return matrix;
	}
	
	
	private void addSingleSequenceToken(List<JPhyloIOEvent> content, String label, String token, boolean writeMetadata) {
		content.add(new SingleSequenceTokenEvent(label, token));
		
		if (writeMetadata) {
			addLiteralMeta(content, null);
		}
		
		content.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
	}
	
	
	private StoreObjectData<TokenSetDefinitionEvent> createTokenSet(String id, CharacterStateSetType type, boolean writeMetadata) {
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(type, id, "tokenSet"), new ArrayList<JPhyloIOEvent>());

		if (writeMetadata) {
			addLiteralMeta(tokenSet.getObjectContent(), null);
		}
		
		// Add single token definitions
		switch (type) {
			case DNA:
				for (int i = 0; i < SequenceUtils.DNA_CHARS.length(); i++) {
					tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
							Character.toString(SequenceUtils.DNA_CHARS.charAt(i)), CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE));
					
					if (writeMetadata) {
						addLiteralMeta(tokenSet.getObjectContent(), null);
					}
					
					tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
				}
				
//				for (Character nucleotide: SequenceUtils.getNucleotideCharacters()) {
//					if (SequenceUtils.isNucleotideAmbuguityCode(nucleotide)) {						
//						char[] constituentArray = SequenceUtils.nucleotideConstituents(nucleotide);
//						if (constituentArray.length > 1) {
//							Set<String> constituents = new HashSet<String>();
//							for (int i = 0; i < constituentArray.length; i++) {
//								constituents.add(Character.toString(constituentArray[i]));
//							}
//							
//							tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
//									Character.toString(nucleotide), CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, constituents));
//							tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));							
//						}
//					}
//				}
				break;
			case RNA:
				for (int i = 0; i < SequenceUtils.RNA_CHARS.length(); i++) {
					tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
							Character.toString(SequenceUtils.RNA_CHARS.charAt(i)), CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE));
					tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
				}
				
				for (Character nucleotide: SequenceUtils.getNucleotideCharacters()) {
					if (SequenceUtils.isNucleotideAmbuguityCode(nucleotide)) {
						char[] constituentArray = SequenceUtils.nucleotideConstituents(nucleotide);
						if (constituentArray.length > 1) {
							Set<String> constituents = new HashSet<String>();
							for (int i = 0; i < constituentArray.length; i++) {
								char constituent = constituentArray[i];
								if (constituent == 'T') {
									constituents.add("U");
								}
								else {
									constituents.add(Character.toString(constituentArray[i]));
								}
							}
							
							tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
									Character.toString(nucleotide), CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, constituents));
							tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));							
						}
					}
				}
				break;
			default:
				break;
		}
		
		// Add validity interval for token set
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 10));
		
		return tokenSet;
	}
	
	
	private StoreObjectData<LinkedLabeledIDEvent> createCharSet(boolean writeMetadata) {
		String characterSetID = DEFAULT_CHAR_SET_ID_PREFIX + obtainCurrentIDIndex();
		StoreObjectData<LinkedLabeledIDEvent> charSet = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.CHARACTER_SET, characterSetID, "character set", null), new ArrayList<JPhyloIOEvent>());		

		if (writeMetadata) {
			addLiteralMeta(charSet.getObjectContent(), null);
		}
		
		charSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 1));
		charSet.getObjectContent().add(new CharacterSetIntervalEvent(3, 6));
		
		return charSet;
	}
	
	
	private StoreTreeNetworkGroupDataAdapter createTrees(String otuListID, boolean writeMetadata, boolean writeTree, boolean writeSet) {		
		String treeGroupID = DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + obtainCurrentIDIndex();
		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
				treeGroupID, "treesAndNetworks", otuListID), new ArrayList<JPhyloIOEvent>());
		
		if (writeMetadata) {
			addLiteralMeta(trees.getAnnotations(), null);
		}
		
		StoreTreeNetworkDataAdapter tree1 = createTreeOrNetwork(otuListID, writeTree, writeMetadata);
		trees.getTreesAndNetworks().add(tree1);
		
		if (writeSet) {
			StoreTreeNetworkDataAdapter tree2 = createTreeOrNetwork(otuListID, !writeTree, writeMetadata);
			trees.getTreesAndNetworks().add(tree2);
			
			String treeNetworkSetID = DEFAULT_TREE_NETWORK_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> treeNetworkSet = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_SET, treeNetworkSetID, null, otuListID));
			
			if (writeMetadata) {
				addLiteralMeta(treeNetworkSet.getObjectContent(), null);
			}
			
			treeNetworkSet.getObjectContent().add(new SetElementEvent(tree1.getStartEvent(parameters).getID(), 
					tree1.isTree(parameters) ? EventContentType.TREE : EventContentType.NETWORK));
			treeNetworkSet.getObjectContent().add(new SetElementEvent(tree2.getStartEvent(parameters).getID(), 
					tree2.isTree(parameters) ? EventContentType.TREE : EventContentType.NETWORK));
			
			trees.getTreeSets(parameters).getObjectMap().put(treeNetworkSetID, treeNetworkSet);
		}
		
		return trees;
	}
	
	
	private StoreTreeNetworkDataAdapter createTreeOrNetwork(String otuListID, boolean writeTree, boolean writeMetadata) {
		StoreTreeNetworkDataAdapter treeOrNetwork = null;
		String treeOrNetworkID;
		
		if (writeTree) {
			treeOrNetworkID = DEFAULT_TREE_ID_PREFIX + obtainCurrentIDIndex();
			
			if (writeMetadata) {
				treeOrNetwork = new EdgeAndNodeMetaDataTreeAdapter(treeOrNetworkID, "tree", otuListID + obtainCurrentIDIndex());

				addLiteralMeta(treeOrNetwork.getAnnotations(), null);				 
			}
			else {
				treeOrNetwork = new NoAnnotationsTree(treeOrNetworkID, "tree", otuListID + obtainCurrentIDIndex());
			}
		}
		else {
			treeOrNetworkID = DEFAULT_NETWORK_ID_PREFIX + obtainCurrentIDIndex();
			treeOrNetwork = new NetworkDataAdapter(treeOrNetworkID, "network", otuListID + obtainCurrentIDIndex());
			
			if (writeMetadata) {
				addLiteralMeta(treeOrNetwork.getAnnotations(), null);
			}
			
		}
		
		return treeOrNetwork;
	}
}