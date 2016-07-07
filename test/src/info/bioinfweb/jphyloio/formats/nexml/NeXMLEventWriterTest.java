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
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreMatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreOTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreObjectData;
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
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeNetworkGroupDataAdapter;

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

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.junit.Test;



public class NeXMLEventWriterTest implements ReadWriteConstants, NeXMLConstants {
	private StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
	private ReadWriteParameterMap parameters = new ReadWriteParameterMap();
	private long idIndex = 0;
	
	
	public long getIDIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}
	
	
	@Test
	public void assertSimpleDocument() throws IOException, XMLStreamException, FactoryConfigurationError {
		File file = new File("data/testOutput/NeXMLTest.xml");
		
		// Write file:
		createSimpleDocument();
		NeXMLEventWriter writer = new NeXMLEventWriter();
		parameters.put(ReadWriteParameterMap.KEY_NEXML_STANDARD_DATA_LABEL_METADATA, TokenDefinitionLabelHandling.DISCARDED);
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		FileReader fileReader = new FileReader(file);
		XMLEventReader reader = XMLInputFactory.newInstance().createXMLEventReader(fileReader);
		try {
			StartElement element;
			
			assertStartDocument(reader);
			
//			element = assertStartElement(TAG_ROOT, reader);
//			assertNameSpaceCount(5, element);
//			assertDefaultNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE), element);
//			assertNamespace(new QName(NEXML_NAMESPACE, XMLConstants.XMLNS_ATTRIBUTE, NEXML_DEFAULT_PRE), element);
//			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSI_DEFAULT_PRE), element);
//			assertNamespace(new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.XSD_DEFAULT_PRE), element);
//			assertNamespace(new QName(XMLReadWriteUtils.NAMESPACE_RDF, XMLConstants.XMLNS_ATTRIBUTE, XMLReadWriteUtils.RDF_DEFAULT_PRE), element);			
//			
//			
//			assertEndElement(TAG_ROOT, reader);
//			
//			assertEndDocument(reader);
		}
		finally {
			fileReader.close();
			reader.close();
//			file.delete();
		}
	}
	
	
	private void createSimpleDocument() {
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + getIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID));
		
		document.getAnnotations().addAll(createMetaData(null));
		
		StoreMatrixDataAdapter matrix = createSequenceMatrix(parameters, otuListID);
		
		// Add character definitions to matrix data adapter
		String charDefinitionID;
		for (long i = 0; i < 5; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + getIDIndex();
			matrix.getCharacterDefinitions(parameters).getObjectMap().put(charDefinitionID, createCharacterDefinition(charDefinitionID, i));
		}
		
		// Add token set of type DNA to matrix data adapter
		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + getIDIndex();
		matrix.getTokenSets(parameters).getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, CharacterStateSetType.DNA, 10));
			
		// Add char sets to matrix data adapter
		String charSetID = DEFAULT_CHAR_SET_ID_PREFIX + getIDIndex();
		matrix.getCharacterSets(parameters).getObjectMap().put(charSetID, createCharSet(charSetID));
		
		// Add metadata to matrix data adapter
		matrix.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_FORMAT), null, null));		
		matrix.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.SIMPLE));		
		matrix.getAnnotations().add(new LiteralMetadataContentEvent("some text (format)", false));
		matrix.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));		
		matrix.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		matrix.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_MATRIX), null, null));		
		matrix.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.SIMPLE));		
		matrix.getAnnotations().add(new LiteralMetadataContentEvent("some text (matrix)", false));
		matrix.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		matrix.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_HAS_LITERAL_METADATA), null, null));		
		matrix.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.SIMPLE));		
		matrix.getAnnotations().add(new LiteralMetadataContentEvent("some more text (matrix)", false));
		matrix.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));		
		matrix.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		matrix.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		matrix.getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_HAS_LITERAL_METADATA), null, null));		
		matrix.getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.SIMPLE));		
		matrix.getAnnotations().add(new LiteralMetadataContentEvent("some text (characters)", false));
		matrix.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));		
		matrix.getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		// Add sequence matrix to matrix data adapter
		document.getMatrices().add(matrix);
		
		// Add tree group to document data adapter
		document.getTreesNetworks().add(createTrees(otuListID));
	}	
	
	
	private void createSimpleTestDocument() {
		// Add OTU list to document data adapter
		String otuListID = DEFAULT_OTU_LIST_ID_PREFIX + getIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID));
		
		StoreMatrixDataAdapter matrix = createSequenceMatrix(parameters, otuListID);
//		StoreMatrixDataAdapter matrix = createContinuousCellsMatrix(otuListID);
		
		// Add character definitions to matrix data adapter
		String charDefinitionID;
		for (long i = 0; i < 3; i++) {
			charDefinitionID = DEFAULT_CHARACTER_DEFINITION_ID_PREFIX + getIDIndex();
			matrix.getCharacterDefinitions(null).getObjectMap().put(charDefinitionID, createCharacterDefinition(charDefinitionID, i));
		}
		
		// Add token set of type DNA to matrix data adapter
		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + getIDIndex();
		matrix.getTokenSets(null).getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, CharacterStateSetType.DNA, 10));
			
		// Add char sets to matrix data adapter
		String charSetID = DEFAULT_CHAR_SET_ID_PREFIX + getIDIndex();
		matrix.getCharacterSets(null).getObjectMap().put(charSetID, createCharSet(charSetID));
		
		// Add sequence sets to matrix data adapter
		String sequenceSetID = DEFAULT_SEQUENCE_SET_ID_PREFIX + getIDIndex();
		StoreObjectData<LinkedLabeledIDEvent> sequenceSet = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.SEQUENCE_SET, sequenceSetID, null, "matrix")); //TODO use real matrix ID
		sequenceSet.getObjectContent().add(new SetElementEvent("sequence1", EventContentType.SEQUENCE));
		sequenceSet.getObjectContent().add(new SetElementEvent("sequence2", EventContentType.SEQUENCE));
		sequenceSet.getObjectContent().add(new SetElementEvent("sequence3", EventContentType.SEQUENCE)); //TODO use real sequence IDs
		
		matrix.getSequenceSets(null).getObjectMap().put(sequenceSetID, sequenceSet);
		
		// Add sequence matrix to matrix data adapter
		document.getMatrices().add(matrix);
		
		// Add tree group to document data adapter
		document.getTreesNetworks().add(createTrees(otuListID));
	}	
	
	
	private StoreOTUListDataAdapter createOTUList(String id) {
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, id, "taxonlist"), null);
		
		for (int i = 0; i < 5; i++) {
			String otuID = DEFAULT_OTU_ID_PREFIX + getIDIndex();
			otuList.getOtus().getObjectMap().put(otuID, new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
					otuID, "taxon"), null));
		}
		
		return otuList;
	}
	
	
	private void addOTUSets(StoreOTUListDataAdapter otuList, String otuListID) {
		// Add OTU set
		String otuSetID = DEFAULT_OTU_SET_ID_PREFIX + getIDIndex();
		StoreObjectData<LinkedLabeledIDEvent> otuSet = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetID, null, otuListID));
		otuSet.getObjectContent().add(new SetElementEvent("otu1", EventContentType.OTU));
		otuSet.getObjectContent().add(new SetElementEvent("otu2", EventContentType.OTU));
		otuSet.getObjectContent().add(new SetElementEvent("otu3", EventContentType.OTU)); //TODO use real OTU IDs
		
		otuList.getOTUSets(null).getObjectMap().put(otuSetID, otuSet);  // Specifying null here may become a problem in the future.
	
		// Add OTU set referencing another set
		String otuSetReferencingSetID = DEFAULT_OTU_SET_ID_PREFIX + getIDIndex();
		otuSet = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetReferencingSetID, null, otuListID));
		otuSet.getObjectContent().add(new SetElementEvent(otuSetID, EventContentType.OTU_SET));		
		otuSet.getObjectContent().add(new SetElementEvent("otu4", EventContentType.OTU)); //TODO use real OTU IDs
		
		otuList.getOTUSets(parameters).getObjectMap().put(otuSetReferencingSetID, otuSet);
		
	// Add OTU set referencing another set
		String otuSetReferencingSetID2 = DEFAULT_OTU_SET_ID_PREFIX + getIDIndex();
		otuSet = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.OTU_SET, otuSetReferencingSetID2, null, otuListID));
		otuSet.getObjectContent().add(new SetElementEvent(otuSetReferencingSetID, EventContentType.OTU_SET));		
		otuSet.getObjectContent().add(new SetElementEvent("otu0", EventContentType.OTU)); //TODO use real OTU IDs
		
		otuList.getOTUSets(parameters).getObjectMap().put(otuSetReferencingSetID2, otuSet);
	}
	
	
	private StoreObjectData<CharacterDefinitionEvent> createCharacterDefinition(String charDefinitionID, long index) {
		StoreObjectData<CharacterDefinitionEvent> characterDefinition = new StoreObjectData<CharacterDefinitionEvent>(
				new CharacterDefinitionEvent(charDefinitionID, "char definition", index), new ArrayList<JPhyloIOEvent>());
		
		characterDefinition.getObjectContent().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CHAR_ATTR_TOKENS), null, null));		
		characterDefinition.getObjectContent().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ATTR_TOKENS), LiteralContentSequenceType.SIMPLE));
		characterDefinition.getObjectContent().add(new LiteralMetadataContentEvent(5, "5"));
		characterDefinition.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));		
		characterDefinition.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		characterDefinition.getObjectContent().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CHAR_ATTR_CODON_POSITION), null, null));		
		characterDefinition.getObjectContent().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ATTR_CODON_POSITION), LiteralContentSequenceType.SIMPLE));
		characterDefinition.getObjectContent().add(new LiteralMetadataContentEvent(2, "2"));
		characterDefinition.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));		
		characterDefinition.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		return characterDefinition;
	}
	
	
	private StoreMatrixDataAdapter createSequenceMatrix(ReadWriteParameterMap parameters, String otusID) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + getIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		List<List<String>> sequences = new ArrayList<>();
		sequences.add(StringUtils.charSequenceToStringList("AGTGC"));
		sequences.add(StringUtils.charSequenceToStringList("AGTCT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTGT"));
		sequences.add(StringUtils.charSequenceToStringList("CGCTC"));
		sequences.add(StringUtils.charSequenceToStringList("CATCGT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTCTAGTCT"));		
		
		// Add sequences
		Iterator<String> iterator = document.getOTUList(parameters, otusID).getIDIterator(parameters);
		int otuCount = 0;
		while (iterator.hasNext()) {
			String sequenceID = DEFAULT_SEQUENCE_ID_PREFIX + getIDIndex();
			matrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID,
					sequences.get(otuCount), document.getOTUList(parameters, otusID).getObjectStartEvent(parameters, iterator.next()).getID()));
			otuCount++;			
		}
		
		// Add sequence that does not specify a linked OTU
		String undefinedOTUSequenceID = DEFAULT_SEQUENCE_ID_PREFIX + getIDIndex();
		matrix.getMatrix().getObjectMap().put(undefinedOTUSequenceID, createSequence(undefinedOTUSequenceID, sequences.get(5), null));
		
		return matrix;
	}
	
	
	private StoreObjectData<LinkedLabeledIDEvent> createSequence(String id, List<String> tokens, String otuID) {		
		StoreObjectData<LinkedLabeledIDEvent> sequence = new StoreObjectData<LinkedLabeledIDEvent>(new LinkedLabeledIDEvent(EventContentType.OTU, 
				id, "sequence", otuID), null);
		
		sequence.getObjectContent().add(new SequenceTokensEvent(tokens));
		
		return sequence;
	}
	
	
	protected StoreMatrixDataAdapter createCellsMatrix(ReadWriteParameterMap parameters, String otusID) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + getIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		// Add single tokens
		Iterator<String> iterator = document.getOTUList(parameters, otusID).getIDIterator(parameters);
		while (iterator.hasNext()) {
			String otuID = document.getOTUList(parameters, otusID).getObjectStartEvent(parameters, iterator.next()).getID();
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIDIndex();
			
			StoreObjectData<LinkedLabeledIDEvent> singleTokens = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, sequenceID, "single token", otuID), null);
			
			addSingleSequenceToken(singleTokens, null, "A");
			addSingleSequenceToken(singleTokens, null, "T");
			addSingleSequenceToken(singleTokens, null, "G");
			addSingleSequenceToken(singleTokens, null, "G");
			addSingleSequenceToken(singleTokens, null, "C");
			
			matrix.getMatrix().getObjectMap().put(sequenceID, singleTokens);
		}
		
		return matrix;
	}
	
	
	protected StoreMatrixDataAdapter createContinuousCellsMatrix(String otusID) {
		String matrixID = DEFAULT_MATRIX_ID_PREFIX + getIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "continuous data", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(startEvent, false, null);
		
		// Add single tokens
		Iterator<String> iterator = document.getOTUList(parameters, otusID).getIDIterator(parameters);
		while (iterator.hasNext()) {
			String otuID = document.getOTUList(parameters, otusID).getObjectStartEvent(parameters, iterator.next()).getID();
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIDIndex();
			
			StoreObjectData<LinkedLabeledIDEvent> singleTokens = new StoreObjectData<LinkedLabeledIDEvent>(
					new LinkedLabeledIDEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, sequenceID, "single token", otuID), null);
			
			addSingleSequenceToken(singleTokens, null, "0.64566");
			addSingleSequenceToken(singleTokens, null, "0.66673");
			addSingleSequenceToken(singleTokens, null, "0.34454");
			addSingleSequenceToken(singleTokens, null, "5.98678");
			addSingleSequenceToken(singleTokens, null, "-5.43334");
			
			matrix.getMatrix().getObjectMap().put(sequenceID, singleTokens);
		}
		
		return matrix;
	}
	
	
	private void addSingleSequenceToken(StoreObjectData<LinkedLabeledIDEvent> singleTokenAdapter, String label, String token) {
		singleTokenAdapter.getObjectContent().add(new SingleSequenceTokenEvent(label, token));
		
//		singleTokenAdapter.getObjectContent().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
//				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.SIMPLE));		
//		singleTokenAdapter.getObjectContent().add(new LiteralMetadataContentEvent("Some text", false));		
//		singleTokenAdapter.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		singleTokenAdapter.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
	}
	
	
	protected StoreObjectData<TokenSetDefinitionEvent> createTokenSet(String id, CharacterStateSetType type, long alignmentLength) {
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, id, "tokenSet"), new ArrayList<JPhyloIOEvent>());

		// Add single token definitions
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Wurst", "W", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
//		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Adenin", "A", CharacterSymbolMeaning.CHARACTER_STATE, 
//				CharacterSymbolType.ATOMIC_STATE));
//		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
//		
//		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Guanin", "G", CharacterSymbolMeaning.CHARACTER_STATE, 
//				CharacterSymbolType.ATOMIC_STATE));
//		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
//		
//		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Cytosin", "C", CharacterSymbolMeaning.CHARACTER_STATE, 
//				CharacterSymbolType.ATOMIC_STATE));
//		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
//		
//		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Thymine", "T", CharacterSymbolMeaning.CHARACTER_STATE, 
//				CharacterSymbolType.ATOMIC_STATE));
//		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
//		
//		// Add single token definition for ambiguity code
//		Set<String> ambiguityCodeB = new HashSet<String>();
//		ambiguityCodeB.add("C");
//		ambiguityCodeB.add("G");
//		ambiguityCodeB.add("T");
//
//		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "ambiguity code", "B", CharacterSymbolMeaning.CHARACTER_STATE, 
//				CharacterSymbolType.UNCERTAIN, ambiguityCodeB));
//		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		// Add validity interval for token set
		tokenSet.getObjectContent().add(new CharacterSetIntervalEvent(0, alignmentLength));
		
		return tokenSet;
	}
	
	
	protected StoreObjectData<LinkedLabeledIDEvent> createCharSet(String id) {	
		StoreObjectData<LinkedLabeledIDEvent> charSet = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.CHARACTER_SET, id, "charSet", null), new ArrayList<JPhyloIOEvent>());		

		charSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 2));
		charSet.getObjectContent().add(new CharacterSetIntervalEvent(3, 4));
		charSet.getObjectContent().add(new CharacterSetIntervalEvent(4, 5));
		
		return charSet;
	}
	
	
	protected TreeNetworkGroupDataAdapter createTrees(String otuListID) {		
		String treesID = DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + getIDIndex();
		TestTreeNetworkGroupDataAdapter trees = new TestTreeNetworkGroupDataAdapter(treesID, null, otuListID);
		
//		String treeID = DEFAULT_TREE_ID_PREFIX + getIDIndex();
//		StoreTreeNetworkGroupDataAdapter trees = new StoreTreeNetworkGroupDataAdapter(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, treesID, 
//				null, otuListID), null);
//		trees.getTreesAndNetworks().add(new NoAnnotationsTree(treeID, null, otuListID));
		
		return trees;
	}
	
	
	protected List<JPhyloIOEvent> createMetaData(String about) {
		List<JPhyloIOEvent> metaData = new ArrayList<JPhyloIOEvent>();
		URI example = null;
		
		try {
			example = new URI("somePath/#fragment");
		} 
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		metaData.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), null, about));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.SIMPLE));
		
		metaData.add(new CommentEvent("This is a ", true));
		metaData.add(new CommentEvent("divided comment.", false));
		
		metaData.add(new LiteralMetadataContentEvent("This is a long ", true));
		metaData.add(new LiteralMetadataContentEvent("literal text", false));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));		
		
		metaData.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), example, about));
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));
		
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		metaData.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		metaData.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, new QName(W3CXSConstants.DATA_TYPE_QNAME.getNamespaceURI(), W3CXSConstants.DATA_TYPE_QNAME.getLocalPart())), 
				LiteralContentSequenceType.SIMPLE));

		metaData.add(new LiteralMetadataContentEvent(new QName("www.another-test.net", "test2", "pre"), null));
		
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		return metaData;
	}
}