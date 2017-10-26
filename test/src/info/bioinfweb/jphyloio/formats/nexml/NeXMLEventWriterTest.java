/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.nexml;


import static info.bioinfweb.commons.testing.XMLAssert.assertAttribute;
import static info.bioinfweb.commons.testing.XMLAssert.assertAttributeCount;
import static info.bioinfweb.commons.testing.XMLAssert.assertCharactersEvent;
import static info.bioinfweb.commons.testing.XMLAssert.assertCommentEvent;
import static info.bioinfweb.commons.testing.XMLAssert.assertDefaultNamespace;
import static info.bioinfweb.commons.testing.XMLAssert.assertEndDocument;
import static info.bioinfweb.commons.testing.XMLAssert.assertEndElement;
import static info.bioinfweb.commons.testing.XMLAssert.assertNamespace;
import static info.bioinfweb.commons.testing.XMLAssert.assertNamespaceCount;
import static info.bioinfweb.commons.testing.XMLAssert.assertStartDocument;
import static info.bioinfweb.commons.testing.XMLAssert.assertStartElement;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.commons.bio.SequenceUtils;
import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreMatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreOTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreObjectData;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreTreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreTreeNetworkGroupDataAdapter;
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
import info.bioinfweb.jphyloio.exception.InconsistentAdapterDataException;
import info.bioinfweb.jphyloio.formats.xml.XMLReadWriteUtils;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.EdgeAndNodeMetaDataTreeAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NetworkMetadataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.PhyloXMLNetworkDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NoAnnotationsNetwork;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NoAnnotationsTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
		File file = new File("data/testOutput/NeXMLTest_SimpleDocument.xml");
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
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
		parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
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
			String nexPrefix = assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_NAMESPACE_PREFIX), true, element);
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

			assertOTUSet(reader, writeMetadata, otuIDs[1], otuIDs[2], otuIDs[3]);			
			assertOTUSet(reader, writeMetadata, otuIDs[1], otuIDs[2], otuIDs[3], otuIDs[4]);			
			assertOTUSet(reader, writeMetadata, otuIDs[0], otuIDs[1], otuIDs[2], otuIDs[3], otuIDs[4]);
			
			assertEndElement(TAG_OTUS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "DnaSeqs", element);
			
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
			
			assertUncertainStateSet("B", null, writeMetadata, reader, tokenC, tokenG, tokenT);
			assertUncertainStateSet("D", null, writeMetadata, reader, tokenA, tokenG, tokenT);
			assertUncertainStateSet("H", null, writeMetadata, reader, tokenA, tokenC, tokenT);
			assertUncertainStateSet("K", null, writeMetadata, reader, tokenG, tokenT);
			assertUncertainStateSet("M", null, writeMetadata, reader, tokenA, tokenC);
			assertUncertainStateSet("N", null, writeMetadata, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("R", null, writeMetadata, reader, tokenA, tokenG);
			assertUncertainStateSet("S", null, writeMetadata, reader, tokenC, tokenG);
			assertUncertainStateSet("V", null, writeMetadata, reader, tokenA, tokenC, tokenG);
			assertUncertainStateSet("W", null, writeMetadata, reader, tokenA, tokenT);
			assertUncertainStateSet("X", null, writeMetadata, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("Y", null, writeMetadata, reader, tokenC, tokenT);
			String tokenGap = assertState("-", "gap", writeMetadata, reader);
			assertUncertainStateSet("?", "missing data", false, reader, tokenC, tokenG, tokenA, tokenT, tokenGap);
			
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
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "FloatTree", element);
			
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
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "FloatNetwork", element);
			
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
		File file = new File("data/testOutput/NeXMLTest_SimpleDocumentWithMetadata.xml");
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
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, new URL("http://www.exampleApplication.com"));
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL_METADATA, TokenDefinitionLabelHandling.BOTH);
		parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
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
			String nexPrefix = assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_NAMESPACE_PREFIX), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);			
			assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/General/Predicates/", XMLConstants.XMLNS_ATTRIBUTE, 
					ReadWriteConstants.JPHYLOIO_PREDICATE_PREFIX), true, element);
			assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/General/Attributes/", XMLConstants.XMLNS_ATTRIBUTE, 
					ReadWriteConstants.JPHYLOIO_ATTRIBUTES_PREFIX), true, element);
			String prefix1 = assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/Formats/NeXML/Predicates/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix2 = assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix3 = assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/Formats/Newick/DataTypes/", XMLConstants.XMLNS_ATTRIBUTE), false, element);		
			String prefix4 = assertNamespace(new QName("http://test.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);			
			String prefix5 = assertNamespace(new QName("www.another-test.net", XMLConstants.XMLNS_ATTRIBUTE), false, element);
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
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "ResourceMeta", element);
			assertAttribute(ATTR_REL, prefix6 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);	
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix6 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			
			assertCommentEvent("This is a divided comment.", reader);			
			assertCharactersEvent("This is a long literal text", reader);
			
			assertEndElement(TAG_META, reader);
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "ResourceMeta", element);
			assertAttribute(ATTR_REL, prefix6 + XMLUtils.QNAME_SEPARATOR + "relations", element);
			assertAttribute(ATTR_HREF, "somePath/#fragment", element);
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);	
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix6 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			
			assertStartElement(new QName("http://test.com/", "customTest", prefix4), reader);
			assertCharactersEvent("split text", reader);
			assertEndElement(new QName("http://test.com/", "customTest", prefix4), reader);
			
			assertStartElement(new QName("http://test.com/", "topLevelTest", prefix4), reader);
			assertStartElement(new QName("http://test.com/", "nestedTest", prefix4), reader);
			assertCharactersEvent("text", reader);
			assertEndElement(new QName("http://test.com/", "nestedTest", prefix4), reader);
			assertEndElement(new QName("http://test.com/", "topLevelTest", prefix4), reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix6 + XMLUtils.QNAME_SEPARATOR + "predicate", element);
			assertAttribute(ATTR_DATATYPE, "xsd:QName", element);
			
			assertCharactersEvent(prefix5 + XMLUtils.QNAME_SEPARATOR + "test2", reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "my string key", element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
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
			
			assertOTUSet(reader, writeMetadata, otuIDs[1], otuIDs[2], otuIDs[3]);			
			assertOTUSet(reader, writeMetadata, otuIDs[1], otuIDs[2], otuIDs[3], otuIDs[4]);			
			assertOTUSet(reader, writeMetadata, otuIDs[0], otuIDs[1], otuIDs[2], otuIDs[3], otuIDs[4]);
			
			assertEndElement(TAG_OTUS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "DnaSeqs", element);
			
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
			assertTokenDefinitionMeta("C", PREDICATE_ORIGINAL_TOKEN_NAME, prefix1, reader);			
			assertLiteralMeta(reader);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenG = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "G", element);
			assertTokenDefinitionMeta("G", PREDICATE_ORIGINAL_TOKEN_NAME, prefix1, reader);	
			assertLiteralMeta(reader);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenA = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "A", element);
			assertTokenDefinitionMeta("A", PREDICATE_ORIGINAL_TOKEN_NAME, prefix1, reader);	
			assertLiteralMeta(reader);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenT = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "T", element);
			assertTokenDefinitionMeta("T", PREDICATE_ORIGINAL_TOKEN_NAME, prefix1, reader);	
			assertLiteralMeta(reader);
			assertEndElement(TAG_STATE, reader);
			
			assertUncertainStateSet("B", null, writeMetadata, reader, tokenC, tokenG, tokenT);
			assertUncertainStateSet("D", null, writeMetadata, reader, tokenA, tokenG, tokenT);
			assertUncertainStateSet("H", null, writeMetadata, reader, tokenA, tokenC, tokenT);
			assertUncertainStateSet("K", null, writeMetadata, reader, tokenG, tokenT);
			assertUncertainStateSet("M", null, writeMetadata, reader, tokenA, tokenC);
			assertUncertainStateSet("N", null, writeMetadata, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("R", null, writeMetadata, reader, tokenA, tokenG);
			assertUncertainStateSet("S", null, writeMetadata, reader, tokenC, tokenG);
			assertUncertainStateSet("V", null, writeMetadata, reader, tokenA, tokenC, tokenG);
			assertUncertainStateSet("W", null, writeMetadata, reader, tokenA, tokenT);
			assertUncertainStateSet("X", null, writeMetadata, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("Y", null, writeMetadata, reader, tokenC, tokenT);
			String tokenGap = assertState("-", "gap", writeMetadata, reader);
			assertUncertainStateSet("?", "missing data", writeMetadata, reader, tokenC, tokenG, tokenA, tokenT, tokenGap);
			
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
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "FloatTree", element);
			assertLiteralMeta(reader);
			
			element = assertStartElement(TAG_NODE, reader);
			assertAttributeCount(3, element);			
			String node1 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, element);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix2 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, "xsd:int", element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "a1", element);
			
			assertCharactersEvent("100", reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
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
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix2 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "splitString", element);
			
			assertCharactersEvent("ABCDEF", reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
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
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "FloatNetwork", element);
			assertLiteralMeta(reader);
			
			element = assertStartElement(TAG_NODE, reader);
			assertAttributeCount(3, element);			
			node1 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, element);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix2 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ATTR_DATATYPE, "xsd:int", element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "a1", element);
			
			assertCharactersEvent("100", reader);
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix2 + XMLUtils.QNAME_SEPARATOR + "somePredicate", element);
			assertAttribute(ReadWriteConstants.ATTRIBUTE_STRING_KEY, "a2", element);
			
			assertCharactersEvent("ab 'c", reader);
			
			assertEndElement(TAG_META, reader);
			
			assertEndElement(TAG_NODE, reader);			
			
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
//			file.delete();
		}
	}
	
	
	@Test
	public void testWritingCustomXMLDoNotManageNamespaces() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest_CustomXMLDoNotManageNamespaces.xml");
		XMLEventFactory factory = XMLEventFactory.newInstance();
		boolean writeMetadata = false;
		boolean writeSets = false;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID, writeMetadata, writeSets));
		
		// Add custom XML to document adapter
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://example.org/", "hasCustomXML", "ex")), "customXML",
				new URIOrStringIdentifier(null, new QName(XMLReadWriteUtils.NAMESPACE_RDF, "Literal")), LiteralContentSequenceType.XML));
		
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters("characters"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createStartElement("ex", "http://example.com/", "customTag"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createNamespace("ex", "http://example.com/"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters("some more"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createStartElement("ex", "http://example.com/", "nestedTag"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createStartElement("ex", "http://example.com/", "secondNested"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createAttribute(new QName("http://example.com/", "attribute", "ex"), "true"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters("nested content"), true));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createEndElement("ex", "http://example.com/", "secondNested"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createEndElement("ex", "http://example.com/", "nestedTag"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters("characters"), true));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters(" and even more"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createEndElement("ex", "http://example.com/", "customTag"), false));
		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
			
		// Write file:
		NeXMLEventWriter writer = new NeXMLEventWriter();
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, new URL("http://www.exampleApplication.com"));
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL_METADATA, TokenDefinitionLabelHandling.BOTH);
		parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, false);  // This is also the default case
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(6, element);
			assertDefaultNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			String nexPrefix = assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_NAMESPACE_PREFIX), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);			
			String prefixRDF = assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			String prefix1 = assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);	
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix1 + XMLUtils.QNAME_SEPARATOR + "hasCustomXML", element);
			assertAttribute(ATTR_DATATYPE, prefixRDF + XMLUtils.QNAME_SEPARATOR + "Literal", element);
			assertAttribute(ATTR_CONTENT, "customXML", element);
			
			assertCharactersEvent("characters", reader);
			StartElement customTag = assertStartElement(new QName("http://example.com/", "customTag"), reader);
			String prefixEx = assertNamespace(new QName("http://example.com/", XMLConstants.XMLNS_ATTRIBUTE), false, customTag);
			assertCharactersEvent("some more", reader);
			assertStartElement(new QName("http://example.com/", "nestedTag", prefixEx), reader);
			customTag = assertStartElement(new QName("http://example.com/", "secondNested", prefixEx), reader);
			assertAttribute(new QName("http://example.com/", "attribute", prefixEx), "true", customTag);
			assertCharactersEvent("nested content", reader);
			assertEndElement(new QName("http://example.com/", "secondNested", prefixEx), reader);
			assertEndElement(new QName("http://example.com/", "nestedTag", prefixEx), reader);
			assertCharactersEvent("characters and even more", reader);
			assertEndElement(new QName("http://example.com/", "customTag", prefixEx), reader);		
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_OTUS, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "taxonlist", element);			
			
			for (int i = 0; i < 5; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				assertAttribute(ATTR_ID, element);
				assertAttribute(ATTR_ABOUT, element);
				assertAttribute(ATTR_LABEL, "taxon" + i, element);				
				assertEndElement(TAG_OTU, reader);
			}
			
			assertEndElement(TAG_OTUS, reader);
			
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
	public void testWritingCustomXMLManageNamespaces() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest_CustomXMLManageNamespaces.xml");
		XMLEventFactory factory = XMLEventFactory.newInstance();
		boolean writeMetadata = false;
		boolean writeSets = false;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID, writeMetadata, writeSets));
		
		// Add custom XML to document adapter
		document.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://example.org/", "hasCustomXML", "ex")), "customXML",
				new URIOrStringIdentifier(null, new QName(XMLReadWriteUtils.NAMESPACE_RDF, "Literal")), LiteralContentSequenceType.XML));
		
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters("characters"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createStartElement("nex", "http://new-example.com/", "customTag"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createNamespace("ex", "http://example.com/"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters("some more"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createStartElement("ex", "http://example.com/", "nestedTag"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createStartElement("ex", "http://example.com/", "secondNested"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createAttribute(new QName("http://example.com/", "attribute", "ex"), "true"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters("nested content"), true));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createEndElement("ex", "http://example.com/", "secondNested"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createEndElement("ex", "http://example.com/", "nestedTag"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters("characters"), true));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createCharacters(" and even more"), false));
		document.getAnnotations().add(new LiteralMetadataContentEvent(factory.createEndElement("nex", "http://new-example.com/", "customTag"), false));
		
		document.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
			
		// Write file:
		NeXMLEventWriter writer = new NeXMLEventWriter();
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, new URL("http://www.exampleApplication.com"));
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL_METADATA, TokenDefinitionLabelHandling.BOTH);
		parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(8, element);
			assertDefaultNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			String nexPrefix = assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_NAMESPACE_PREFIX), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);			
			String prefixRDF = assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), true, element);
			String prefix1 = assertNamespace(new QName("http://example.org/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix2 = assertNamespace(new QName("http://example.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			String prefix3 = assertNamespace(new QName("http://new-example.com/", XMLConstants.XMLNS_ATTRIBUTE), false, element);
			
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
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);	
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, prefix1 + XMLUtils.QNAME_SEPARATOR + "hasCustomXML", element);
			assertAttribute(ATTR_DATATYPE, prefixRDF + XMLUtils.QNAME_SEPARATOR + "Literal", element);
			assertAttribute(ATTR_CONTENT, "customXML", element);
			
			assertCharactersEvent("characters", reader);
			assertStartElement(new QName("http://new-example.com/", "customTag", prefix3), reader);			
			assertCharactersEvent("some more", reader);
			assertStartElement(new QName("http://example.com/", "nestedTag", prefix2), reader);
			StartElement customTag = assertStartElement(new QName("http://example.com/", "secondNested", prefix2), reader);
			assertAttribute(new QName("http://example.com/", "attribute", prefix2), "true", customTag);
			assertCharactersEvent("nested content", reader);
			assertEndElement(new QName("http://example.com/", "secondNested", prefix2), reader);
			assertEndElement(new QName("http://example.com/", "nestedTag", prefix2), reader);
			assertCharactersEvent("characters and even more", reader);
			assertEndElement(new QName("http://new-example.com/", "customTag", prefix3), reader);		
			
			assertEndElement(TAG_META, reader);
			
			element = assertStartElement(TAG_OTUS, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "taxonlist", element);			
			
			for (int i = 0; i < 5; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				assertAttribute(ATTR_ID, element);
				assertAttribute(ATTR_ABOUT, element);
				assertAttribute(ATTR_LABEL, "taxon" + i, element);				
				assertEndElement(TAG_OTU, reader);
			}
			
			assertEndElement(TAG_OTUS, reader);
			
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
	public void testWriteDocumentLinkedElementMissing() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest_DocumentLinkedElementMissing.xml");
		boolean writeMetadata = false;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID, writeMetadata, false));
		
		// Add matrix to document data adapter		
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otuListID), 
				false, null);		
		
		// Add character definitions
		String charDefinitionID;
		for (long i = 0; i < 5; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getCharacterDefinitions(parameters).getObjectMap().put(charDefinitionID, createCharacterDefinition(charDefinitionID, i, writeMetadata));
		}
		
		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DNA, tokenSetID, "tokenSet"), new ArrayList<JPhyloIOEvent>());	
		
		// Add single token definitions
		for (int i = 0; i < SequenceUtils.DNA_CHARS.length(); i++) {
			tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
					Character.toString(SequenceUtils.DNA_CHARS.charAt(i)), CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE));			
			tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		}
		
		Set<String> constituents = new HashSet<String>();
		constituents.add("A");
		constituents.add("G");
		constituents.add("S");
		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"B", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, constituents));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
				
		
		// Add validity interval for token set
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 10));
		
		matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, tokenSet);		
			
		List<List<String>> sequences = new ArrayList<>();
		sequences.add(StringUtils.charSequenceToStringList("AGTGC"));
		sequences.add(StringUtils.charSequenceToStringList("A-TCT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTGT"));
		sequences.add(StringUtils.charSequenceToStringList("CGC?C"));
		sequences.add(StringUtils.charSequenceToStringList("CATCGT"));
		
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
		
		document.getMatrices().add(matrix);

		// Write file:
		try {
			NeXMLEventWriter writer = new NeXMLEventWriter();
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
			parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
			
			writer.writeDocument(document, file, parameters);
			fail("Exception not thrown");
		}
		catch (InconsistentAdapterDataException e) {
			assertEquals(e.getMessage(), "The token \"S\" was referenced in a token definition but not defined before. This may error may be solved by providing tokens in the correct order (atomic states before uncertain states that reference them as constituents).");
		}
		finally {			
			file.delete();
		}
	}
	
	
	@Test
	public void testWriteDocumentIDNotUnique() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest_DocumentIDNotUnique.xml");
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();		
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, otuListID, "taxonlist"), null);
		
		String otuID = DEFAULT_OTU_ID_PREFIX + obtainCurrentIDIndex();
		otuList.getOtus().getObjectMap().put(otuID, new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
				otuID, "taxon0"), null));
		otuList.getOtus().getObjectMap().put(otuListID, new StoreObjectData<LabeledIDEvent>(
				new LabeledIDEvent(EventContentType.OTU, otuListID, "taxon0"), null));
		
		document.getOTUListsMap().put(otuListID, otuList);

		// Write file:
		try {
			NeXMLEventWriter writer = new NeXMLEventWriter();
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
			parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
			
			writer.writeDocument(document, file, parameters);
			fail("Exception not thrown");		
		}
		catch (InconsistentAdapterDataException e){
			assertEquals(e.getMessage(), "The encountered ID " + otuListID + " already exists in the document. IDs have to be unique.");
		}
		finally {			
			file.delete();
		}
	}
	
	
	@Test
	public void testWriteDocumentTokenSetsOverlap() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest_DocumentTokenSetsOverlap.xml");
		boolean writeMetadata = false;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID, writeMetadata, false));
		
		// Add matrix to document data adapter		
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otuListID), 
				false, null);		
		
		// Add character definitions
		String charDefinitionID;
		for (long i = 0; i < 5; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getCharacterDefinitions(parameters).getObjectMap().put(charDefinitionID, createCharacterDefinition(charDefinitionID, i, writeMetadata));
		}
		
		// Add token set
		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, tokenSetID, "tokenSet"), new ArrayList<JPhyloIOEvent>());	
		
		// Add single token definitions		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"blue", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, null));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"green", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, null));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));				
		
		// Add validity interval for token set
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 5));
		
		matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, tokenSet);	
		
		// Add second token set
		tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet2 = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, tokenSetID, "tokenSet"), new ArrayList<JPhyloIOEvent>());	
		
		// Add single token definitions		
		tokenSet2.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"red", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, null));
		tokenSet2.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet2.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"yellow", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, null));
		tokenSet2.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));				
		
		// Add validity interval for token set
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(3, 10));
		
		matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, tokenSet2);		
			
		List<List<String>> sequences = new ArrayList<>();
		sequences.add(StringUtils.charSequenceToStringList("AGTGC"));
		sequences.add(StringUtils.charSequenceToStringList("A-TCT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTGT"));
		sequences.add(StringUtils.charSequenceToStringList("CGC?C"));
		sequences.add(StringUtils.charSequenceToStringList("CATCGT"));
		
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
		
		document.getMatrices().add(matrix);

		// Write file:
		try {
			NeXMLEventWriter writer = new NeXMLEventWriter();
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
			parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
			
			writer.writeDocument(document, file, parameters);
			fail("Exception not thrown");
		}
		catch (InconsistentAdapterDataException e) {
			assertEquals(e.getMessage(), "More than one token set was assigned to the alignment column 3.");
		}
		finally {			
			file.delete();
		}
	}
	
	
	@Test
	public void testWriteDocumentTokenSetForContinuousData() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest_DocumentTokenSetForContinuousData.xml");
		boolean writeMetadata = false;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID, writeMetadata, false));
		
		// Add matrix to document data adapter		
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otuListID), 
				true, null);		
		
		// Add character definitions
		String charDefinitionID;
		for (long i = 0; i < 5; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getCharacterDefinitions(parameters).getObjectMap().put(charDefinitionID, createCharacterDefinition(charDefinitionID, i, writeMetadata));
		}
		
		// Add token set
		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, tokenSetID, "tokenSet"), new ArrayList<JPhyloIOEvent>());	
		
		// Add single token definitions		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"blue", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, null));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"green", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, null));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		// Add validity interval for token set
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 6));
		
		matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, tokenSet);			
			
		List<List<String>> sequences = new ArrayList<>();
		String[] sequence1 = {"0.5", "0.565", "0.4545", "1.4545", "3.427"};
		sequences.add(Arrays.asList(sequence1));
		String[] sequence2 = {"0.5", "6", "9.566", "1.4545", "3.427"};
		sequences.add(Arrays.asList(sequence2));
		String[] sequence3 = {"0.5", "47.544", "0.4545", "1.4545", "3.427"};
		sequences.add(Arrays.asList(sequence3));
		String[] sequence4 = {"8.45", "0.565", "0.4545", "1.4545", "3.427"};
		sequences.add(Arrays.asList(sequence4));
		String[] sequence5 = {"0.5", "4.667", "0.4545", "1.4545", "6"};
		sequences.add(Arrays.asList(sequence5));
		
		// Add sequences
		Iterator<String> iterator = document.getOTUList(parameters, otuListID).getIDIterator(parameters);
		int otuCount = 0;
		while (iterator.hasNext()) {
			String sequenceID = DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID, null,
					sequences.get(otuCount), document.getOTUList(parameters, otuListID).getObjectStartEvent(parameters, iterator.next()).getID(), writeMetadata));
			otuCount++;			
		}
		
		document.getMatrices().add(matrix);

		// Write file:
		try {
			NeXMLEventWriter writer = new NeXMLEventWriter();
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
			parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
			parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
			
			writer.writeDocument(document, file, parameters);
			fail("Exception not thrown");
		}
		catch (InconsistentAdapterDataException e) {
			assertEquals(e.getMessage(), "A continuous data token set can not specify single token definitions.");
		}
		finally {			
			file.delete();
		}
	}
	
	
	@Test
	public void testWritingStandardData() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest_StandardData.xml");
		boolean writeMetadata = false;
		boolean writeSets = false;
		
		// Add OTU lists to document data adapter
		String otuListID1 = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID1, createOTUList(otuListID1, writeMetadata, writeSets));
		String otuListID2 = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID2, createOTUList(otuListID2, writeMetadata, writeSets));
		
		// Add standard sequences matrix to document data adapter
		String sequenceMatrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		StoreMatrixDataAdapter sequenceMatrix = new StoreMatrixDataAdapter(new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, sequenceMatrixID, 
				"standardSequences", otuListID1), true, null);		
		
		// Add character definitions
		String charDefinitionID;
		for (long i = 0; i < 3; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + obtainCurrentIDIndex();
			sequenceMatrix.getCharacterDefinitions(parameters).getObjectMap().put(charDefinitionID, 
					createCharacterDefinition(charDefinitionID, i, writeMetadata));
		}
			
		List<List<String>> sequences = new ArrayList<>();
		String[] sequence1 = {"blue", "green", "red"};
		sequences.add(Arrays.asList(sequence1));
		String[] sequence2 = {"red", "green", "blue"};
		sequences.add(Arrays.asList(sequence2));
		String[] sequence3 = {"red", "red", "red"};
		sequences.add(Arrays.asList(sequence3));
		String[] sequence4 = {"red", "blueOrRed", "blue"};
		sequences.add(Arrays.asList(sequence4));
		String[] sequence5 = {"blue", "red", "blue"};
		sequences.add(Arrays.asList(sequence5));		
		
		// Add sequences
		Iterator<String> iterator = document.getOTUList(parameters, otuListID1).getIDIterator(parameters);
		int otuCount = 0;
		String[] sequenceIDs = new String[5];
		while (iterator.hasNext()) {
			String sequenceID = DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			sequenceIDs[otuCount] = sequenceID;
			sequenceMatrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID, null,
					sequences.get(otuCount), document.getOTUList(parameters, otuListID1).getObjectStartEvent(parameters, iterator.next()).getID(), writeMetadata));
			otuCount++;			
		}

		document.getMatrices().add(sequenceMatrix);
		
		// Add standard cells matrix to document data adapter
		String cellsMatrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		StoreMatrixDataAdapter cellsMatrix = new StoreMatrixDataAdapter(new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, cellsMatrixID, 
				"standardCells", otuListID2), true, null);
		
		// Add character definitions
		for (long i = 0; i < 3; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + obtainCurrentIDIndex();
			cellsMatrix.getCharacterDefinitions(parameters).getObjectMap().put(charDefinitionID, 
					createCharacterDefinition(charDefinitionID, i, writeMetadata));
		}		
		
		// Add token set
		String generatedTokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, generatedTokenSetID, "tokenSet"), new ArrayList<JPhyloIOEvent>());	
		
		// Add single token definitions		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"red", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE, null));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"blue", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE, null));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		// Add validity interval for token set
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 1));
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(2, 3));
		
		// Add second token set
		String tokenSetID2 = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet2 = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, tokenSetID2, "tokenSet"), new ArrayList<JPhyloIOEvent>());	
		
		// Add single token definitions		
		tokenSet2.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), "LabelBlue", 
				"blue", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE, null));
		tokenSet2.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet2.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), "LabelGreen", 
				"green", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE, null));
		tokenSet2.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet2.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"green/blue", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, Arrays.asList(new String[]{"green", "blue"})));
		tokenSet2.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet2.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
				"blueOrGreen", CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.POLYMORPHIC, Arrays.asList(new String[]{"green", "blue"})));
		tokenSet2.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		// Add validity interval for second token set
		tokenSet2.getObjectContent().add(new CharacterSetIntervalEvent(1, 2));
		
		cellsMatrix.getTokenSets(parameters).getObjectMap().put(generatedTokenSetID, tokenSet);	
		cellsMatrix.getTokenSets(parameters).getObjectMap().put(tokenSetID2, tokenSet2);		
		
		// Add single tokens
		iterator = document.getOTUList(parameters, otuListID2).getIDIterator(parameters);
		otuCount = 0;
		while (iterator.hasNext()) {
			String otuID = document.getOTUList(parameters, otuListID2).getObjectStartEvent(parameters, iterator.next()).getID();
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			
			StoreObjectData<LinkedLabeledIDEvent> singleTokens = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, sequenceID, "single token", otuID), null);
			
			addSingleSequenceToken(singleTokens.getObjectContent(), "token0", sequences.get(otuCount).get(0), writeMetadata);
			addSingleSequenceToken(singleTokens.getObjectContent(), "token1", sequences.get(otuCount).get(1), writeMetadata);
			addSingleSequenceToken(singleTokens.getObjectContent(), null, sequences.get(otuCount).get(2), writeMetadata);
			
			cellsMatrix.getMatrix().getObjectMap().put(sequenceID, singleTokens);
			
			otuCount++;
		}

		document.getMatrices().add(cellsMatrix);
		
		// Add tree groups to document data adapter
		document.getTreesNetworks().add(createTrees(otuListID1, writeMetadata, true, false));
		document.getTreesNetworks().add(createTrees(null, writeMetadata, false, false));
			
		// Write file:
		NeXMLEventWriter writer = new NeXMLEventWriter();
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL, true);
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL_METADATA, TokenDefinitionLabelHandling.DISCARDED);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
		parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
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
			String nexPrefix = assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_NAMESPACE_PREFIX), true, element);
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
			String otusID1 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "taxonlist", element);
			
			String[] otuIDs1 = new String[5];
			for (int i = 0; i < 5; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				otuIDs1[i] = assertAttribute(ATTR_ID, element);
				assertAttribute(ATTR_ABOUT, element);
				assertAttribute(ATTR_LABEL, "taxon" + i, element);
				assertEndElement(TAG_OTU, reader);
			}
			
			assertEndElement(TAG_OTUS, reader);
			
			element = assertStartElement(TAG_OTUS, reader);
			assertAttributeCount(3, element);
			String otusID2 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "taxonlist", element);
			
			String[] otuIDs2 = new String[5];
			for (int i = 0; i < 5; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				otuIDs2[i] = assertAttribute(ATTR_ID, element);
				assertAttribute(ATTR_ABOUT, element);
				assertAttribute(ATTR_LABEL, "taxon" + i, element);
				assertEndElement(TAG_OTU, reader);
			}
			
			assertEndElement(TAG_OTUS, reader);
			
			element = assertStartElement(TAG_OTUS, reader);
			assertAttributeCount(3, element);
			String undefinedOTUListID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, UNDEFINED_OTUS_LABEL, element);
			
			element = assertStartElement(TAG_OTU, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, UNDEFINED_OTU_LABEL, element);
			assertEndElement(TAG_OTU, reader);
						
			assertEndElement(TAG_OTUS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "standardSequences", element);
			assertAttribute(ATTR_OTUS, otusID1, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + TYPE_STANDARD_SEQ, element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(2, element);
			String tokenSetID = assertAttribute(ATTR_ID, DEFAULT_TOKEN_DEFINITION_SET_ID_PREFIX + "0", element);
			assertAttribute(ATTR_ABOUT, element);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "red", element);
			String token1 = assertAttribute(ATTR_SYMBOL, "0", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "green", element);
			String token2 = assertAttribute(ATTR_SYMBOL, "1", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "blue", element);
			String token3 = assertAttribute(ATTR_SYMBOL, "2", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "blueOrRed", element);
			String token4 = assertAttribute(ATTR_SYMBOL, "3", element);
			assertEndElement(TAG_STATE, reader);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", writeMetadata, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertRow(null, otuIDs1[0], token3 + " " + token2 + " " + token1 + " ", writeMetadata, reader);
			assertRow(null, otuIDs1[1], token1 + " " + token2 + " " + token3 + " ", writeMetadata, reader);
			assertRow(null, otuIDs1[2], token1 + " " + token1 + " " + token1 + " ", writeMetadata, reader);
			assertRow(null, otuIDs1[3], token1 + " " + token4 + " " + token3 + " ", writeMetadata, reader);
			assertRow(null, otuIDs1[4], token3 + " " + token1 + " " + token3 + " ", writeMetadata, reader);
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "standardCells", element);
			assertAttribute(ATTR_OTUS, otusID2, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + TYPE_STANDARD_CELLS, element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			tokenSetID2 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			assertAttribute(ATTR_ABOUT, element);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "red", element);
			token1 = assertAttribute(ATTR_SYMBOL, "0", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "blue", element);
			token2 = assertAttribute(ATTR_SYMBOL, "1", element);
			assertEndElement(TAG_STATE, reader);
			
			assertEndElement(TAG_STATES, reader);
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			String tokenSetID3 = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			assertAttribute(ATTR_ABOUT, element);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			String blueID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "blue", element);
			token1 = assertAttribute(ATTR_SYMBOL, "0", element);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);	
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, "p:originalLabel", element);			
			assertCharactersEvent("LabelBlue", reader);			
			assertEndElement(TAG_META, reader);
			
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			String greenID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "green", element);
			token2 = assertAttribute(ATTR_SYMBOL, "1", element);
			
			element = assertStartElement(TAG_META, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);	
			assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
			assertAttribute(ATTR_PROPERTY, "p:originalLabel", element);			
			assertCharactersEvent("LabelGreen", reader);			
			assertEndElement(TAG_META, reader);
			
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_UNCERTAIN, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "green/blue", element);
			token4 = assertAttribute(ATTR_SYMBOL, "2", element);
			
			element = assertStartElement(TAG_MEMBER, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_SINGLE_STATE_LINK, greenID, element);
			assertEndElement(TAG_MEMBER, reader);
			
			element = assertStartElement(TAG_MEMBER, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_SINGLE_STATE_LINK, blueID, element);
			assertEndElement(TAG_MEMBER, reader);
			
			assertEndElement(TAG_UNCERTAIN, reader);
			
			element = assertStartElement(TAG_POLYMORPHIC, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "blueOrGreen", element);
			token4 = assertAttribute(ATTR_SYMBOL, "3", element);
			
			element = assertStartElement(TAG_MEMBER, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_SINGLE_STATE_LINK, greenID, element);
			assertEndElement(TAG_MEMBER, reader);
			
			element = assertStartElement(TAG_MEMBER, reader);
			assertAttributeCount(1, element);
			assertAttribute(ATTR_SINGLE_STATE_LINK, blueID, element);
			assertEndElement(TAG_MEMBER, reader);
			
			assertEndElement(TAG_POLYMORPHIC, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "red", element);
			assertAttribute(ATTR_SYMBOL, "4", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "blueOrRed", element);
			assertAttribute(ATTR_SYMBOL, "5", element);
			assertEndElement(TAG_STATE, reader);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition("column definition", tokenSetID2, "25", "25", writeMetadata, reader);
			assertCharacterDefinition("column definition", tokenSetID3, "25", "25", writeMetadata, reader);
			assertCharacterDefinition("column definition", tokenSetID2, "25", "25", writeMetadata, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			
			assertCellsRow("single token", otuIDs2[0], reader, "1", "token0", "1", "token1", "0", null);
			assertCellsRow("single token", otuIDs2[1], reader, "0", "token0", "1", "token1", "1", null);
			assertCellsRow("single token", otuIDs2[2], reader, "0", "token0", "4", "token1", "0", null);
			assertCellsRow("single token", otuIDs2[3], reader, "0", "token0", "5", "token1", "1", null);
			assertCellsRow("single token", otuIDs2[4], reader, "1", "token0", "4", "token1", "1", null);
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_TREES, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "treesAndNetworks", element);
			assertAttribute(ATTR_OTUS, otuListID1, element);
			
			element = assertStartElement(TAG_TREE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tree", element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "FloatTree", element);
			
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
			
			assertEndElement(TAG_TREES, reader);
			
			element = assertStartElement(TAG_TREES, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "treesAndNetworks", element);
			assertAttribute(ATTR_OTUS, undefinedOTUListID, element);
			
			element = assertStartElement(TAG_NETWORK, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "network", element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "FloatNetwork", element);
			
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
	public void testWritingMultipleCharactersElements() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest_MultipleCharactersElements.xml");
		boolean writeMetadata = false;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();		
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, otuListID, "taxonlist"), null);
		
		for (int i = 0; i < 2; i++) {
			String otuID = DEFAULT_OTU_ID_PREFIX + obtainCurrentIDIndex();
			otuList.getOtus().getObjectMap().put(otuID, new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
					otuID, "taxon" + i), null));		
		}
		
		document.getOTUListsMap().put(otuListID, otuList);		
		
		// Add DNA cells characters element
		String[] dnaTokens1 = {"G", "C", "-"};
		String[] dnaTokens2 = {"A", "?", "T"};
		StoreMatrixDataAdapter dnaCellsMatrix = createCellsMatrix(parameters, otuListID, CharacterStateSetType.DNA, dnaTokens1, dnaTokens2);		
		document.getMatrices().add(dnaCellsMatrix);
		
		// Add RNA sequences characters element
		StoreMatrixDataAdapter rnaSequenceMatrix = createSequenceMatrix(parameters, otuListID, CharacterStateSetType.RNA, "AACU-", "UU?CG");
		document.getMatrices().add(rnaSequenceMatrix);
		
		// Add RNA cells characters element
		String[] rnaTokens1 = {"G", "C", "-"};
		String[] rnaTokens2 = {"A", "?", "U"};
		StoreMatrixDataAdapter rnaCellsMatrix = createCellsMatrix(parameters, otuListID, CharacterStateSetType.RNA, rnaTokens1, rnaTokens2);
		document.getMatrices().add(rnaCellsMatrix);
		
		// Add continuous data sequences characters element
		StoreMatrixDataAdapter continuousSequenceMatrix = createSequenceMatrix(parameters, otuListID, CharacterStateSetType.CONTINUOUS, 
				"0.66673 0.34454 5.98678", "-5.43334 0.8232 3.5678");
		document.getMatrices().add(continuousSequenceMatrix);
		
		// Add continuous data cells characters element
		String[] continuousTokens1 = {"0.66673", "0.34454", "5.98678"};
		String[] continuousTokens2 = {"-5.43334", "0.8932", "3.5"};
		StoreMatrixDataAdapter continuousCellsMatrix = createCellsMatrix(parameters, otuListID, CharacterStateSetType.CONTINUOUS, 
				continuousTokens1, continuousTokens2);
		document.getMatrices().add(continuousCellsMatrix);
		
		// Add amino acid data sequences characters element
		StoreMatrixDataAdapter aaSequenceMatrix = createSequenceMatrix(parameters, otuListID, CharacterStateSetType.AMINO_ACID, "Tyr Val Glu - Phe *", "KL?MN");
		document.getMatrices().add(aaSequenceMatrix);
		
		// Add amino acid cells characters element
		String[] aaTokens1 = {"-", "Q", "*"};
		String[] aaTokens2 = {"W", "?", "A"};
		StoreMatrixDataAdapter aaCellsMatrix = createCellsMatrix(parameters, otuListID, CharacterStateSetType.AMINO_ACID, aaTokens1, aaTokens2);
		document.getMatrices().add(aaCellsMatrix);
		
		// Add nucleotide data sequences characters element
		StoreMatrixDataAdapter nucleotideSequenceMatrix = createSequenceMatrix(parameters, otuListID, CharacterStateSetType.NUCLEOTIDE, "A - G T U", "A ? T G U");
		document.getMatrices().add(nucleotideSequenceMatrix);
		
		// Add nucleotide data cells characters element
		String[] nucleotideTokens1 = {"T", "C", "-"};
		String[] nucleotideTokens2 = {"A", "?", "U"};
		StoreMatrixDataAdapter nucleotideCellsMatrix = createCellsMatrix(parameters, otuListID, CharacterStateSetType.NUCLEOTIDE, nucleotideTokens1, nucleotideTokens2);
		document.getMatrices().add(nucleotideCellsMatrix);
			
		// Write file:
		NeXMLEventWriter writer = new NeXMLEventWriter();
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL, true);
		parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_DEFINITION_LABEL_METADATA, TokenDefinitionLabelHandling.DISCARDED);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_NAME, "exampleApplication");
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_VERSION, 1.0);
		parameters.put(ReadWriteParameterMap.KEY_APPLICATION_URL, "http://www.exampleApplication.com");
		parameters.put(ReadWriteParameterMap.KEY_CUSTOM_XML_NAMESPACE_HANDLING, true);
		writer.writeDocument(document, file, parameters);
		
		
		// Test output:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNamespaceCount(4, element);
			assertDefaultNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			String nexPrefix = assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_NAMESPACE_PREFIX), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), true, element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), true, element);
			
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
			
			String[] otuIDs = new String[2];
			for (int i = 0; i < 2; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				otuIDs[i] = assertAttribute(ATTR_ID, element);
				assertAttribute(ATTR_ABOUT, element);
				assertAttribute(ATTR_LABEL, "taxon" + i, element);
				assertEndElement(TAG_OTU, reader);
			}
			
			assertEndElement(TAG_OTUS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "DnaCells", element);
			
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
			
			assertUncertainStateSet("B", null, writeMetadata, reader, tokenC, tokenG, tokenT);
			assertUncertainStateSet("D", null, writeMetadata, reader, tokenA, tokenG, tokenT);
			assertUncertainStateSet("H", null, writeMetadata, reader, tokenA, tokenC, tokenT);
			assertUncertainStateSet("K", null, writeMetadata, reader, tokenG, tokenT);
			assertUncertainStateSet("M", null, writeMetadata, reader, tokenA, tokenC);
			assertUncertainStateSet("N", null, writeMetadata, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("R", null, writeMetadata, reader, tokenA, tokenG);
			assertUncertainStateSet("S", null, writeMetadata, reader, tokenC, tokenG);
			assertUncertainStateSet("V", null, writeMetadata, reader, tokenA, tokenC, tokenG);
			assertUncertainStateSet("W", null, writeMetadata, reader, tokenA, tokenT);
			assertUncertainStateSet("X", null, writeMetadata, reader, tokenA, tokenT, tokenC, tokenG);
			assertUncertainStateSet("Y", null, writeMetadata, reader, tokenC, tokenT);
			String tokenGap = assertState("-", "gap", writeMetadata, reader);
			assertUncertainStateSet("?", "missing data", false, reader, tokenC, tokenG, tokenA, tokenT, tokenGap);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
					
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertCellsRow(null, otuIDs[0], reader, "G", "single token", "C", "single token", "-", "single token");
			assertCellsRow(null, otuIDs[1], reader, "A", "single token", "?", "single token", "T", "single token");
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);

			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "RnaSeqs", element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			tokenSetID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			tokenC = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "C", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			tokenG = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "G", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			tokenA = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "A", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			String tokenU = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "U", element);
			assertEndElement(TAG_STATE, reader);
			
			assertUncertainStateSet("B", null, writeMetadata, reader, tokenC, tokenG, tokenU);
			assertUncertainStateSet("D", null, writeMetadata, reader, tokenA, tokenG, tokenU);
			assertUncertainStateSet("H", null, writeMetadata, reader, tokenA, tokenC, tokenU);
			assertUncertainStateSet("K", null, writeMetadata, reader, tokenG, tokenU);
			assertUncertainStateSet("M", null, writeMetadata, reader, tokenA, tokenC);
			assertUncertainStateSet("N", null, writeMetadata, reader, tokenA, tokenU, tokenC, tokenG);
			assertUncertainStateSet("R", null, writeMetadata, reader, tokenA, tokenG);
			assertUncertainStateSet("S", null, writeMetadata, reader, tokenC, tokenG);
			assertUncertainStateSet("V", null, writeMetadata, reader, tokenA, tokenC, tokenG);
			assertUncertainStateSet("W", null, writeMetadata, reader, tokenA, tokenU);
			assertUncertainStateSet("X", null, writeMetadata, reader, tokenA, tokenU, tokenC, tokenG);
			assertUncertainStateSet("Y", null, writeMetadata, reader, tokenC, tokenU);
			tokenGap = assertState("-", "gap", writeMetadata, reader);
			assertUncertainStateSet("?", "missing data", false, reader, tokenC, tokenG, tokenA, tokenU, tokenGap);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertRow(null, otuIDs[0], "AACU-", writeMetadata, reader);
			assertRow(null, otuIDs[1], "UU?CG", writeMetadata, reader);
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "RnaCells", element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			tokenSetID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			tokenC = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "C", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			tokenG = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "G", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			tokenA = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "A", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(3, element);
			tokenU = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_SYMBOL, "U", element);
			assertEndElement(TAG_STATE, reader);
			
			assertUncertainStateSet("B", null, writeMetadata, reader, tokenC, tokenG, tokenU);
			assertUncertainStateSet("D", null, writeMetadata, reader, tokenA, tokenG, tokenU);
			assertUncertainStateSet("H", null, writeMetadata, reader, tokenA, tokenC, tokenU);
			assertUncertainStateSet("K", null, writeMetadata, reader, tokenG, tokenU);
			assertUncertainStateSet("M", null, writeMetadata, reader, tokenA, tokenC);
			assertUncertainStateSet("N", null, writeMetadata, reader, tokenA, tokenU, tokenC, tokenG);
			assertUncertainStateSet("R", null, writeMetadata, reader, tokenA, tokenG);
			assertUncertainStateSet("S", null, writeMetadata, reader, tokenC, tokenG);
			assertUncertainStateSet("V", null, writeMetadata, reader, tokenA, tokenC, tokenG);
			assertUncertainStateSet("W", null, writeMetadata, reader, tokenA, tokenU);
			assertUncertainStateSet("X", null, writeMetadata, reader, tokenA, tokenU, tokenC, tokenG);
			assertUncertainStateSet("Y", null, writeMetadata, reader, tokenC, tokenU);
			tokenGap = assertState("-", "gap", writeMetadata, reader);
			assertUncertainStateSet("?", "missing data", false, reader, tokenC, tokenG, tokenA, tokenU, tokenGap);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
					
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertCellsRow(null, otuIDs[0], reader, "G", "single token", "C", "single token", "-", "single token");
			assertCellsRow(null, otuIDs[1], reader, "A", "single token", "?", "single token", "U", "single token");
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "ContinuousSeqs", element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			assertCharacterDefinition(null, null, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, null, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, null, null, null, writeMetadata, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertRow(null, otuIDs[0], "0.66673 0.34454 5.98678 ", writeMetadata, reader);
			assertRow(null, otuIDs[1], "-5.43334 0.8232 3.5678 ", writeMetadata, reader);
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "ContinuousCells", element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			assertCharacterDefinition(null, null, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, null, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, null, null, null, writeMetadata, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertCellsRow(null, otuIDs[0], reader, "0.66673", "single token", "0.34454", "single token", "5.98678", "single token");
			assertCellsRow(null, otuIDs[1], reader, "-5.43334", "single token", "0.8932", "single token", "3.5", "single token");
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "ProteinSeqs", element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			tokenSetID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			
			String[] tokenIDs = new String[22];
			tokenIDs[0] = assertState("A", writeMetadata, reader);
			tokenIDs[1] = assertState("C", writeMetadata, reader);
			tokenIDs[2] = assertState("D", writeMetadata, reader);
			tokenIDs[3] = assertState("E", writeMetadata, reader);
			tokenIDs[4] = assertState("F", writeMetadata, reader);
			tokenIDs[5] = assertState("G", writeMetadata, reader);
			tokenIDs[6] = assertState("H", writeMetadata, reader);
			tokenIDs[7] = assertState("I", writeMetadata, reader);
			tokenIDs[8] = assertState("K", writeMetadata, reader);
			tokenIDs[9] = assertState("L", writeMetadata, reader);
			tokenIDs[10] = assertState("M", writeMetadata, reader);
			tokenIDs[11] = assertState("N", writeMetadata, reader);
			tokenIDs[12] = assertState("O", writeMetadata, reader);
			tokenIDs[13] = assertState("P", writeMetadata, reader);
			tokenIDs[14] = assertState("Q", writeMetadata, reader);
			tokenIDs[15] = assertState("R", writeMetadata, reader);
			tokenIDs[16] = assertState("S", writeMetadata, reader);
			tokenIDs[17] = assertState("T", writeMetadata, reader);
			tokenIDs[18] = assertState("U", writeMetadata, reader);
			tokenIDs[19] = assertState("V", writeMetadata, reader);
			tokenIDs[20] = assertState("W", writeMetadata, reader);
			tokenIDs[21] = assertState("Y", writeMetadata, reader);
			String tokenStop = assertState("*", writeMetadata, reader);
			
			assertUncertainStateSet("B", null, writeMetadata, reader, tokenIDs[2], tokenIDs[11]);
			assertUncertainStateSet("X", null, false, reader, tokenIDs);
			assertUncertainStateSet("Z", null, writeMetadata, reader, tokenIDs[14], tokenIDs[3]);			
			tokenGap = assertState("-", "gap", writeMetadata, reader);
			assertUncertainStateSet("?", "missing data", false, reader, tokenIDs[0], tokenIDs[1], tokenIDs[2], tokenIDs[3], tokenIDs[4], 
					tokenIDs[5], tokenIDs[6], tokenIDs[7], tokenIDs[8], tokenIDs[9], tokenIDs[10], tokenIDs[11], tokenIDs[12], tokenIDs[13], 
					tokenIDs[14], tokenIDs[15], tokenIDs[16], tokenIDs[17], tokenIDs[18], tokenIDs[19], tokenIDs[20], tokenIDs[21], tokenGap, 
					tokenStop);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertRow(null, otuIDs[0], "YVE-F*", writeMetadata, reader);
			assertRow(null, otuIDs[1], "KL?MN", writeMetadata, reader);
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + "ProteinCells", element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			tokenSetID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			
			tokenIDs = new String[22];
			tokenIDs[0] = assertState("A", writeMetadata, reader);
			tokenIDs[1] = assertState("C", writeMetadata, reader);
			tokenIDs[2] = assertState("D", writeMetadata, reader);
			tokenIDs[3] = assertState("E", writeMetadata, reader);
			tokenIDs[4] = assertState("F", writeMetadata, reader);
			tokenIDs[5] = assertState("G", writeMetadata, reader);
			tokenIDs[6] = assertState("H", writeMetadata, reader);
			tokenIDs[7] = assertState("I", writeMetadata, reader);
			tokenIDs[8] = assertState("K", writeMetadata, reader);
			tokenIDs[9] = assertState("L", writeMetadata, reader);
			tokenIDs[10] = assertState("M", writeMetadata, reader);
			tokenIDs[11] = assertState("N", writeMetadata, reader);
			tokenIDs[12] = assertState("O", writeMetadata, reader);
			tokenIDs[13] = assertState("P", writeMetadata, reader);
			tokenIDs[14] = assertState("Q", writeMetadata, reader);
			tokenIDs[15] =	assertState("R", writeMetadata, reader);
			tokenIDs[16] = assertState("S", writeMetadata, reader);
			tokenIDs[17] = assertState("T", writeMetadata, reader);
			tokenIDs[18] = assertState("U", writeMetadata, reader);
			tokenIDs[19] = assertState("V", writeMetadata, reader);
			tokenIDs[20] = assertState("W", writeMetadata, reader);
			tokenIDs[21] = assertState("Y", writeMetadata, reader);
			tokenStop = assertState("*", writeMetadata, reader);
			
			assertUncertainStateSet("B", null, writeMetadata, reader, tokenIDs[2], tokenIDs[11]);
			assertUncertainStateSet("X", null, false, reader, tokenIDs);
			assertUncertainStateSet("Z", null, writeMetadata, reader, tokenIDs[14], tokenIDs[3]);			
			tokenGap = assertState("-", "gap", writeMetadata, reader);
			assertUncertainStateSet("?", "missing data", false, reader, tokenIDs[0], tokenIDs[1], tokenIDs[2], tokenIDs[3], tokenIDs[4], 
					tokenIDs[5], tokenIDs[6], tokenIDs[7], tokenIDs[8], tokenIDs[9], tokenIDs[10], tokenIDs[11], tokenIDs[12], tokenIDs[13], 
					tokenIDs[14], tokenIDs[15], tokenIDs[16], tokenIDs[17], tokenIDs[18], tokenIDs[19], tokenIDs[20], tokenIDs[21], tokenGap, 
					tokenStop);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertCellsRow(null, otuIDs[0], reader, "-", "single token", "Q", "single token", "*", "single token");
			assertCellsRow(null, otuIDs[1], reader, "W", "single token", "?", "single token", "A", "single token");
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + TYPE_STANDARD_SEQ, element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			tokenSetID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "A", element);
			String token1 = assertAttribute(ATTR_SYMBOL, "0", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "T", element);
			String token2 = assertAttribute(ATTR_SYMBOL, "1", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "U", element);
			String token3 = assertAttribute(ATTR_SYMBOL, "2", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "G", element);
			String token4 = assertAttribute(ATTR_SYMBOL, "3", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "-", element);
			String token5 = assertAttribute(ATTR_SYMBOL, "4", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "?", element);
			String token6 = assertAttribute(ATTR_SYMBOL, "5", element);
			assertEndElement(TAG_STATE, reader);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			assertRow(null, otuIDs[0], token1 + " " + token5 + " " + token4 + " " + token2 + " " + token3 + " ", writeMetadata, reader);
			assertRow(null, otuIDs[1], token1 + " " + token6 + " " + token2 + " " + token4 + " " + token3 + " ", writeMetadata, reader);
					
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
			
			element = assertStartElement(TAG_CHARACTERS, reader);
			assertAttributeCount(5, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "alignment", element);
			assertAttribute(ATTR_OTUS, otusID, element);
			assertAttribute(ATTR_XSI_TYPE, nexPrefix + XMLUtils.QNAME_SEPARATOR + TYPE_STANDARD_CELLS, element);
			
			assertStartElement(TAG_FORMAT, reader);		
			
			element = assertStartElement(TAG_STATES, reader);
			assertAttributeCount(3, element);
			tokenSetID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "tokenSet", element);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "A", element);
			token1 = assertAttribute(ATTR_SYMBOL, "0", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "C", element);
			token2 = assertAttribute(ATTR_SYMBOL, "1", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "T", element);
			token2 = assertAttribute(ATTR_SYMBOL, "2", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "U", element);
			token3 = assertAttribute(ATTR_SYMBOL, "3", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "-", element);
			token5 = assertAttribute(ATTR_SYMBOL, "4", element);
			assertEndElement(TAG_STATE, reader);
			
			element = assertStartElement(TAG_STATE, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "?", element);
			token6 = assertAttribute(ATTR_SYMBOL, "5", element);
			assertEndElement(TAG_STATE, reader);
			
			assertEndElement(TAG_STATES, reader);
			
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			assertCharacterDefinition(null, tokenSetID, null, null, writeMetadata, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
			assertStartElement(TAG_MATRIX, reader);
			
			assertCellsRow(null, otuIDs[0], reader, "2", "single token", "1", "single token", "4", "single token");
			assertCellsRow(null, otuIDs[1], reader, "0", "single token", "5", "single token", "3", "single token");
			
			assertEndElement(TAG_MATRIX, reader);
			
			assertEndElement(TAG_CHARACTERS, reader);
						
			assertEndElement(TAG_ROOT, reader);
			
			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
			file.delete();
		}
	}
	
	
	private void assertOTUSet(XMLEventReader reader, boolean assertMetadata, String... expectedOTUIDs) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_SET, reader);
		assertAttributeCount(3, element);
		assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_ABOUT, element);
		String otuIDs = assertAttribute(ATTR_OTU_SET_LINKED_IDS, element);
		
		if (assertMetadata) {
			assertLiteralMeta(reader);
		}
		
		assertEndElement(TAG_SET, reader);
		
		List<String> otuIDList = Arrays.asList(otuIDs.split(" "));
		assertEquals(expectedOTUIDs.length, otuIDList.size());
		for (String otuID : expectedOTUIDs) {
			assertTrue(otuIDList.contains(otuID));
		}
	}
	
	
	private String assertState(String symbol, boolean assertMetadata, XMLEventReader reader) throws XMLStreamException {
		return assertState(symbol, null, assertMetadata, reader);
	}
	
	
	private String assertState(String symbol, String label, boolean assertMetadata, XMLEventReader reader) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_STATE, reader);
		if (label != null) {
			assertAttributeCount(4, element);
			assertAttribute(ATTR_LABEL, label, element);
		}
		else {
			assertAttributeCount(3, element);
		}
		String tokenID = assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_ABOUT, "#" + tokenID, element);
		assertAttribute(ATTR_SYMBOL, symbol, element);
		
		if (symbol.equals("-") && assertMetadata) {			
			assertTokenDefinitionMeta("gap", PREDICATE_ORIGINAL_LABEL, "p1", reader);
			assertTokenDefinitionMeta(symbol, PREDICATE_ORIGINAL_TOKEN_NAME, "p1", reader);
		}
		else if (symbol.equals("?") && assertMetadata) {			
			assertTokenDefinitionMeta("missing data", PREDICATE_ORIGINAL_LABEL, "p1", reader);
			assertTokenDefinitionMeta(symbol, PREDICATE_ORIGINAL_TOKEN_NAME, "p1", reader);
		}
		else if (assertMetadata) {
			assertTokenDefinitionMeta(symbol, PREDICATE_ORIGINAL_TOKEN_NAME, "p1", reader);
		}
		
		assertEndElement(TAG_STATE, reader);
		
		return tokenID;
	}
	
	
	private void assertUncertainStateSet(String symbol, String label, boolean assertMetadata, XMLEventReader reader, String... memberID) throws XMLStreamException {
		Set<String> constituents = new HashSet<String>();
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
		
		if (symbol.equals("-") && assertMetadata) {			
			assertTokenDefinitionMeta("gap", PREDICATE_ORIGINAL_LABEL, "p1", reader);
			assertTokenDefinitionMeta(symbol, PREDICATE_ORIGINAL_TOKEN_NAME, "p1", reader);
		}
		else if (symbol.equals("?") && assertMetadata) {			
			assertTokenDefinitionMeta("missing data", PREDICATE_ORIGINAL_LABEL, "p1", reader);
			assertTokenDefinitionMeta(symbol, PREDICATE_ORIGINAL_TOKEN_NAME, "p1", reader);
		}
		else if (assertMetadata) {
			assertTokenDefinitionMeta(symbol, PREDICATE_ORIGINAL_TOKEN_NAME, "p1", reader);
		}
		
		for (int i = 0; i < memberID.length; i++) {
			element = assertStartElement(TAG_MEMBER, reader);
			assertAttributeCount(1, element);
			constituents.add(assertAttribute(ATTR_STATE_SET_LINKED_IDS, element));			
			assertEndElement(TAG_MEMBER, reader);
		}
		
		if (assertMetadata && !symbol.equals("?") && !symbol.equals("-")) {
			assertLiteralMeta(reader);
		}
		
		assertEndElement(TAG_UNCERTAIN, reader);
		
		assertEquals(memberID.length, constituents.size());
		for (String constituent : memberID) {
			assertTrue(constituents.contains(constituent));
		}
	}
	
	
	private void assertTokenDefinitionMeta(String content, QName predicate, String predicatePrefix, XMLEventReader reader) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_META, reader);
		assertAttributeCount(3, element);
		assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_XSI_TYPE, "nex:LiteralMeta", element);
		assertAttribute(ATTR_PROPERTY, predicatePrefix + XMLUtils.QNAME_SEPARATOR + predicate.getLocalPart(), element);
		
		assertCharactersEvent(content, reader);
		
		assertEndElement(TAG_META, reader);
	}
	
	
	private String assertCharacterDefinition(String label, String states, String codonPosition, String tokens, boolean writeMetadata, XMLEventReader reader) throws XMLStreamException {
		StartElement element = assertStartElement(TAG_CHAR, reader);
		int count = 2;
		
		String id = assertAttribute(ATTR_ID, element);
		assertAttribute(ATTR_ABOUT, element);
		
		if (states != null) {
			assertAttribute(ATTR_STATES, states, element);
			count++;
		}
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
	
	
	private String assertCellsRow(String expectedSequenceLabel, String otuID, XMLEventReader reader, String...expectedSequence) throws XMLStreamException {
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
		
		for (int i = 0; i < expectedSequence.length; i += 2) {
			element = assertStartElement(TAG_CELL, reader);
			
			if (expectedSequence[i+1] != null) {
				assertAttributeCount(1, element);
				assertAttribute(ATTR_LABEL, expectedSequence[i+1], element);
			}
			else {
				assertAttributeCount(0, element);
			}

			assertCharactersEvent(expectedSequence[i], reader);
			
			assertEndElement(TAG_CELL, reader);
		}
		
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
		annotations.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
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
		annotations.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));
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
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));		
		
		metaData.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), example, about));
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));
		
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("test", "http://test.com/", "customTest"), false));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createCharacters("split"), true));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createCharacters(" text"), false));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("test", "http://test.com/", "customTest"), false));
		
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("test", "http://test.com/", "topLevelTest"), false));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("test", "http://test.com/", "nestedTest"), false));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createCharacters("text"), false));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("test", "http://test.com/", "nestedTest"), false));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("test", "http://test.com/", "topLevelTest"), false));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate", "pre")), new URIOrStringIdentifier(null, new QName(W3CXSConstants.DATA_TYPE_QNAME.getNamespaceURI(), W3CXSConstants.DATA_TYPE_QNAME.getLocalPart())), 
				LiteralContentSequenceType.SIMPLE));

		metaData.add(new LiteralMetadataContentEvent(new QName("www.another-test.net", "test2", "pre"), null));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier("my string key", null), new URIOrStringIdentifier(null, 
				new QName(W3CXSConstants.DATA_TYPE_INTEGER.getNamespaceURI(), W3CXSConstants.DATA_TYPE_INTEGER.getLocalPart())), 
				LiteralContentSequenceType.SIMPLE));
		metaData.add(new LiteralMetadataContentEvent(25, "25"));		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		
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
	
	
	private StoreMatrixDataAdapter createSequenceMatrix(ReadWriteParameterMap parameters, String otusID, CharacterStateSetType tokenSetType, String... tokens) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		List<List<String>> sequences = new ArrayList<>();	
		for (int i = 0; i < tokens.length; i++) {
			if (!tokens[i].contains(" ")) {
				sequences.add(StringUtils.charSequenceToStringList(tokens[i]));
			}
			else {
				sequences.add(new ArrayList<String>());
				for (int j = 0; j < tokens[i].split(" ").length; j++) {
					sequences.get(i).add(tokens[i].split(" ")[j]);
				}				
			}
		}
		
		// Add sequences
		Iterator<String> iterator = document.getOTUList(parameters, otusID).getIDIterator(parameters);
		int otuCount = 0;
		while (iterator.hasNext()) {
			String sequenceID = DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID, null,
					sequences.get(otuCount), document.getOTUList(parameters, otusID).getObjectStartEvent(parameters, iterator.next()).getID(), false));
			otuCount++;
		}
		
		// Add token set
		String tokenSetID = DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();
		matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, tokenSetType, 
				AbstractEventWriter.determineMaxSequenceLength(matrix, parameters), false));

		return matrix;
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
			matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, CharacterStateSetType.DNA, 6, writeMetadata));
			
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
	
	
	private StoreMatrixDataAdapter createCellsMatrix(ReadWriteParameterMap parameters, String otusID, CharacterStateSetType tokenSetType, String[]... tokens) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		// Add single tokens
		Iterator<String> iterator = document.getOTUList(parameters, otusID).getIDIterator(parameters);
		int i = 0;
		while (iterator.hasNext()) {
			String otuID = document.getOTUList(parameters, otusID).getObjectStartEvent(parameters, iterator.next()).getID();
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			
			StoreObjectData<LinkedLabeledIDEvent> singleTokens = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, sequenceID, null, otuID), null);
			
			for (int j = 0; j < tokens[i].length; j++) {
				addSingleSequenceToken(singleTokens.getObjectContent(), "single token", tokens[i][j], false);
			}
			
			matrix.getMatrix().getObjectMap().put(sequenceID, singleTokens);
			i++;
		}
		
		String tokenSetID = DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();
		matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, tokenSetType, 
				AbstractEventWriter.determineMaxSequenceLength(matrix, parameters), false));
		
		return matrix;
	}
	
	
	private void addSingleSequenceToken(List<JPhyloIOEvent> content, String label, String token, boolean writeMetadata) {
		content.add(new SingleSequenceTokenEvent(label, token));
		
		if (writeMetadata) {
			addLiteralMeta(content, null);
		}
		
		content.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
	}
	
	
	private StoreObjectData<TokenSetDefinitionEvent> createTokenSet(String id, CharacterStateSetType type, long alignmentLength, 
			boolean writeMetadata) {
		
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
				
				for (Character nucleotide: SequenceUtils.getNucleotideCharacters()) {
					if (SequenceUtils.isNucleotideAmbuguityCode(nucleotide)) {
						char[] constituentArray = SequenceUtils.nucleotideConstituents(nucleotide);
						if (constituentArray.length > 1) {
							Set<String> constituents = new HashSet<String>();
							for (int i = 0; i < constituentArray.length; i++) {
								constituents.add(Character.toString(constituentArray[i]));
							}
							
							tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
									Character.toString(nucleotide), CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.UNCERTAIN, constituents));
							
							if (writeMetadata) {
								addLiteralMeta(tokenSet.getObjectContent(), null);
							}
							
							tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
						}
					}
				}
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
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(0, alignmentLength));
		
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
			
			if (writeMetadata) {
				treeOrNetwork = new NetworkMetadataAdapter(treeOrNetworkID, "network", otuListID + obtainCurrentIDIndex());

				addLiteralMeta(treeOrNetwork.getAnnotations(), null);
			}
			else {
				treeOrNetwork = new NoAnnotationsNetwork(treeOrNetworkID, "network", otuListID + obtainCurrentIDIndex());
			}			
		}
		
		return treeOrNetwork;
	}
}