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
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.NoAnnotationsTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
import javax.xml.stream.events.Attribute;
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
	public void assertSimpleDocument() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest.xml");
		boolean writeMetadata = false;
		boolean writeSets = true;
		
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + obtainCurrentIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID, writeMetadata, writeSets));
		
		// Add matrix to document data adapter
		StoreMatrixDataAdapter matrix = createDNASequenceMatrix(writeSets, false, writeMetadata, otuListID);
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
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
			element = assertStartElement(TAG_ROOT, reader);
			assertNameSpaceCount(5, element);
			assertDefaultNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
			assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_PRE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), element);
			assertNamespace(new QName("http://bioinfweb.info/xmlns/JPhyloIO/Formats/NeXML/Predicates/", XMLConstants.XMLNS_ATTRIBUTE, "p"), element);
			
			assertAttributeCount(2, element);
			assertAttribute(ATTR_VERSION, "0.9", element);
			
			String generator = assertAttribute(ATTR_GENERATOR, element);
			assertTrue(generator, generator.matches(
					"exampleApplication 1.0 JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+"));
			
			assertTrue(reader.hasNext());		
			XMLEvent event = reader.nextEvent();			
			assertEquals(XMLStreamConstants.COMMENT, event.getEventType());
			assertTrue(((Comment)event).getText().matches(
					" This file was generated by exampleApplication 1.0 using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+. <http://bioinfweb.info/JPhyloIO/>"));
			
			element = assertStartElement(TAG_OTUS, reader);
			assertAttributeCount(3, element);
			String otusID = assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "taxonlist", element);
			
			for (int i = 0; i < 5; i++) {
				element = assertStartElement(TAG_OTU, reader);
				assertAttributeCount(3, element);
				assertNotNull(element.getAttributeByName(ATTR_ID));
				assertNotNull(element.getAttributeByName(ATTR_ABOUT));
				assertAttribute(ATTR_LABEL, "taxon", element);
				assertEndElement(TAG_OTU, reader);
			}
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, "otu1 otu2 otu3 ", element);
			assertEndElement(TAG_SET, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, "otu1 otu2 otu3 otu4 ", element);
			assertEndElement(TAG_SET, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(3, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_OTU_SET_LINKED_IDS, "otu0 otu1 otu2 otu3 otu4 ", element);
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
			
			String char0 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", reader);
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", reader);
			assertCharacterDefinition("column definition", tokenSetID, "25", "25", reader);
			String char3 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", reader);
			String char4 = assertCharacterDefinition("column definition", tokenSetID, "25", "25", reader);
			String char5 = assertCharacterDefinition(null, tokenSetID, null, null, reader);
			
			element = assertStartElement(TAG_SET, reader);
			assertAttributeCount(4, element);
			assertAttribute(ATTR_ID, element);
			assertAttribute(ATTR_ABOUT, element);
			assertAttribute(ATTR_LABEL, "character set", element);
			assertAttribute(ATTR_CHAR_SET_LINKED_IDS, char0 + " " + char3 + " " + char4 + " " + char5 + " ", element);
			assertEndElement(TAG_SET, reader);
			
			assertEndElement(TAG_FORMAT, reader);
			
//			assertEndElement(TAG_ROOT, reader);
//			
//			assertEndDocument(reader);
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
	
	
	private String assertCharacterDefinition(String label, String states, String codonPosition, String tokens, XMLEventReader reader) throws XMLStreamException {
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
		assertEndElement(TAG_CHAR, reader);
		
		return id;
	}
	
	
	private void addLiteralMetadata(List<JPhyloIOEvent> annotations, QName predicate) {
		if (predicate == null) {
			predicate = new QName("http://meta.net/", "predicate");
		}
		
		annotations.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
				new URIOrStringIdentifier(null, predicate), new URIOrStringIdentifier(null, 
				new QName(W3CXSConstants.DATA_TYPE_INTEGER.getNamespaceURI(), W3CXSConstants.DATA_TYPE_INTEGER.getLocalPart())), 
				LiteralContentSequenceType.SIMPLE));
		annotations.add(new LiteralMetadataContentEvent(25, "25"));		
		annotations.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	}
	
	
	private void addResourceMetadata(List<JPhyloIOEvent> annotations, URIOrStringIdentifier resourcePredicate) {
		annotations.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + obtainCurrentIDIndex(), null, 
			resourcePredicate, null, null));		
		addLiteralMetadata(annotations, null);
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
			addLiteralMetadata(otuList.getAnnotations(), null);
		}
		
		for (int i = 0; i < 5; i++) {
			String otuID = DEFAULT_OTU_ID_PREFIX + obtainCurrentIDIndex();
			otuList.getOtus().getObjectMap().put(otuID, new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
					otuID, "taxon"), null));
			
			if (writeMetaData) {
				addLiteralMetadata(otuList.getOtus().getObjectContent(otuID), null);
			}
		}
		
		if (writeSets) {
			String otuSetID = DEFAULT_OTU_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> otuSet = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetID, null, id));
			
			if (writeMetaData) {
				addLiteralMetadata(otuSet.getObjectContent(), null);
			}
			
			otuSet.getObjectContent().add(new SetElementEvent("otu1", EventContentType.OTU));
			otuSet.getObjectContent().add(new SetElementEvent("otu2", EventContentType.OTU));
			otuSet.getObjectContent().add(new SetElementEvent("otu3", EventContentType.OTU)); //TODO use real OTU IDs
			
			otuList.getOTUSets(parameters).getObjectMap().put(otuSetID, otuSet);
		
			// Add OTU set referencing another set
			String otuSetReferencingSetID = DEFAULT_OTU_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> otuSet2 = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetReferencingSetID, null, id));
			otuSet2.getObjectContent().add(new SetElementEvent(otuSetID, EventContentType.OTU_SET));		
			otuSet2.getObjectContent().add(new SetElementEvent("otu4", EventContentType.OTU)); //TODO use real OTU IDs
			
			otuList.getOTUSets(parameters).getObjectMap().put(otuSetReferencingSetID, otuSet2);
			
			// Add OTU set referencing another set
			String otuSetReferencingSetID2 = DEFAULT_OTU_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> otuSet3 = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetReferencingSetID2, null, id));
			otuSet3.getObjectContent().add(new SetElementEvent(otuSetReferencingSetID, EventContentType.OTU_SET));		
			otuSet3.getObjectContent().add(new SetElementEvent("otu0", EventContentType.OTU)); //TODO use real OTU IDs
			
			otuList.getOTUSets(parameters).getObjectMap().put(otuSetReferencingSetID2, otuSet3);
		}
		
		return otuList;
	}
	
	
	private StoreMatrixDataAdapter createDNASequenceMatrix(boolean createTokenSet, boolean addLinklessSeq, boolean writeMetadata, String otuListID) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + obtainCurrentIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otuListID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		if (writeMetadata) {
			addResourceMetadata(matrix.getAnnotations(), new URIOrStringIdentifier(null, PREDICATE_FORMAT));
			addResourceMetadata(matrix.getAnnotations(), new URIOrStringIdentifier(null, PREDICATE_MATRIX));
			addLiteralMetadata(matrix.getAnnotations(), null);
		}
		
		// Add character definitions
		String charDefinitionID;
		for (long i = 0; i < 5; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getCharacterDefinitions(parameters).getObjectMap().put(charDefinitionID, createCharacterDefinition(charDefinitionID, i, writeMetadata));
		}		
		
		// Add token set of type DNA to matrix data adapter
		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + obtainCurrentIDIndex();
		matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, CharacterStateSetType.DNA, writeMetadata));
		
		// Add char sets to matrix data adapter
		String charSetID = DEFAULT_CHAR_SET_ID_PREFIX + obtainCurrentIDIndex();
		matrix.getCharacterSets(parameters).getObjectMap().put(charSetID, createCharSet(writeMetadata));
		
		List<List<String>> sequences = new ArrayList<>();
		sequences.add(StringUtils.charSequenceToStringList("AGTGC"));
		sequences.add(StringUtils.charSequenceToStringList("AGTCT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTGT"));
		sequences.add(StringUtils.charSequenceToStringList("CGCTC"));
		sequences.add(StringUtils.charSequenceToStringList("CATCGT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTCTAGTCT"));		
		
		// Add sequences
		Iterator<String> iterator = document.getOTUList(parameters, otuListID).getIDIterator(parameters);
		int otuCount = 0;
		while (iterator.hasNext()) {
			String sequenceID = DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID,
					sequences.get(otuCount), document.getOTUList(parameters, otuListID).getObjectStartEvent(parameters, iterator.next()).getID()));
			otuCount++;			
		}
		
		if (addLinklessSeq) {  // Add sequence that does not specify a linked OTU
			String undefinedOTUSequenceID = DEFAULT_SEQUENCE_ID_PREFIX + obtainCurrentIDIndex();
			matrix.getMatrix().getObjectMap().put(undefinedOTUSequenceID, createSequence(undefinedOTUSequenceID, sequences.get(5), null));
		}
		
		return matrix;
	}
	
	
	private StoreObjectData<CharacterDefinitionEvent> createCharacterDefinition(String charDefinitionID, long index, boolean writeMetadata) {
		StoreObjectData<CharacterDefinitionEvent> characterDefinition = new StoreObjectData<CharacterDefinitionEvent>(
				new CharacterDefinitionEvent(charDefinitionID, "column definition", index), new ArrayList<JPhyloIOEvent>());
		
		addLiteralMetadata(characterDefinition.getObjectContent(), PREDICATE_CHAR_ATTR_TOKENS);
		addLiteralMetadata(characterDefinition.getObjectContent(), PREDICATE_CHAR_ATTR_CODON_POSITION);
		
		if (writeMetadata) {
			addLiteralMetadata(characterDefinition.getObjectContent(), null);
		}
		
		return characterDefinition;
	}
	
	
	private StoreObjectData<LinkedLabeledIDEvent> createSequence(String id, List<String> tokens, String otuID) {		
		StoreObjectData<LinkedLabeledIDEvent> sequence = new StoreObjectData<LinkedLabeledIDEvent>(new LinkedLabeledIDEvent(EventContentType.OTU, 
				id, "sequence", otuID), null);
		
		sequence.getObjectContent().add(new SequenceTokensEvent(tokens));
		
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
			addLiteralMetadata(content, null);
		}
		
		content.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
	}
	
	
	private StoreObjectData<TokenSetDefinitionEvent> createTokenSet(String id, CharacterStateSetType type, boolean writeMetadata) {
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(type, id, "tokenSet"), new ArrayList<JPhyloIOEvent>());

		if (writeMetadata) {
			addLiteralMetadata(tokenSet.getObjectContent(), null);
		}
		
		// Add single token definitions
		switch (type) {
			case DNA:
				for (int i = 0; i < SequenceUtils.DNA_CHARS.length(); i++) {
					tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + obtainCurrentIDIndex(), null, 
							Character.toString(SequenceUtils.DNA_CHARS.charAt(i)), CharacterSymbolMeaning.CHARACTER_STATE, CharacterSymbolType.ATOMIC_STATE));
					
					if (writeMetadata) {
						addLiteralMetadata(tokenSet.getObjectContent(), null);
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
			addLiteralMetadata(charSet.getObjectContent(), null);
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
			addLiteralMetadata(trees.getAnnotations(), null);
		}
		
		StoreTreeNetworkDataAdapter tree1 = createTreeOrNetwork(otuListID, writeTree, writeMetadata);
		trees.getTreesAndNetworks().add(tree1);
		
		if (writeSet) {
			StoreTreeNetworkDataAdapter tree2 = createTreeOrNetwork(otuListID, writeTree, writeMetadata); //TODO also add network
			trees.getTreesAndNetworks().add(tree2);
			
			String treeNetworkSetID = DEFAULT_TREE_NETWORK_SET_ID_PREFIX + obtainCurrentIDIndex();
			StoreObjectData<LinkedLabeledIDEvent> treeNetworkSet = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_SET, treeNetworkSetID, null, otuListID));
			
			if (writeMetadata) {
				addLiteralMetadata(treeNetworkSet.getObjectContent(), null);
			}
			
			treeNetworkSet.getObjectContent().add(new SetElementEvent(tree1.getStartEvent(parameters).getID(), 
					writeTree ? EventContentType.TREE : EventContentType.NETWORK));
			treeNetworkSet.getObjectContent().add(new SetElementEvent(tree2.getStartEvent(parameters).getID(), 
					writeTree ? EventContentType.TREE : EventContentType.NETWORK));		
			
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

				addLiteralMetadata(treeOrNetwork.getAnnotations(), null);				 
			}
			else {
				treeOrNetwork = new NoAnnotationsTree(treeOrNetworkID, "tree", otuListID + obtainCurrentIDIndex());
			}
		}
		else {
			// TODO add network adapter
		}
		
		return treeOrNetwork;
	}
}