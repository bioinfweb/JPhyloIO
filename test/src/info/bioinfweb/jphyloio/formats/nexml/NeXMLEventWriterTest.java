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


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.bio.CharacterSymbolType;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreMatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreOTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreObjectData;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeNetworkGroupDataAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.junit.Test;



public class NeXMLEventWriterTest {
	private StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
	private long idIndex = 0;
	
	
	public long getIDIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}


	@Test
	public void testWriteSimpleDocument() throws Exception {
		createSimpleTestDocument();
		NeXMLEventWriter writer = new NeXMLEventWriter();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		writer.writeDocument(document, new File("data/testOutput/NeXMLTest.xml"), parameters);
	}
	
	
	protected void createSimpleTestDocument() {
		// Add OTU list to document data adapter
		String otuListID = ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + getIDIndex();
		document.getOTUListsMap().put(otuListID, createOTUList(otuListID));
		
//		StoreMatrixDataAdapter matrix = createSequenceMatrix(otuListID);
		StoreMatrixDataAdapter matrix = createContinuousCellsMatrix(otuListID);
		
		// Add token set of type DNA to matrix data adapter
//		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + getIDIndex();
//		matrix.getTokenSets().getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, CharacterStateSetType.DNA, 10));
			
		// Add char sets to matrix data adapter
		String charSetID = ReadWriteConstants.DEFAULT_CHAR_SET_ID_PREFIX + getIDIndex();
		matrix.getCharacterSets().getObjectMap().put(charSetID, createCharSet(charSetID));
		
		// Add sequence matrix to matrix data adapter
		document.getMatrices().add(matrix);
		
		// Add tree group to document data adapter
		document.getTreesNetworks().add(createTrees(otuListID));
	}	
	
	
	protected StoreOTUListDataAdapter createOTUList(String id) {
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, id, "taxonlist"), null);
		
		for (int i = 0; i < 5; i++) {
			String otuID = ReadWriteConstants.DEFAULT_OTU_ID_PREFIX + getIDIndex();
			otuList.getOtus().getObjectMap().put(otuID, new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
					otuID, "taxon"), null));
		}
		
		return otuList;
	}
	
	
	protected StoreMatrixDataAdapter createSequenceMatrix(String otusID) {
		String matrixID = ReadWriteConstants.DEFAULT_MATRIX_ID_PREFIX + getIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(null, startEvent, false);
		
		List<List<String>> sequences = new ArrayList<>();
		sequences.add(StringUtils.charSequenceToStringList("AGTGC"));
		sequences.add(StringUtils.charSequenceToStringList("AGTCT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTGT"));
		sequences.add(StringUtils.charSequenceToStringList("CGCTC"));
		sequences.add(StringUtils.charSequenceToStringList("CATCGT"));
		sequences.add(StringUtils.charSequenceToStringList("AGTCTAGTCT"));		
		
		// Add sequences
		Iterator<String> iterator = document.getOTUList(otusID).getIDIterator();
		int otuCount = 0;
		while (iterator.hasNext()) {
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIDIndex();
			matrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID,
					sequences.get(otuCount), document.getOTUList(otusID).getObjectStartEvent(iterator.next()).getID()));
			otuCount++;			
		}
		
		// Add sequence that does not specify a linked OTU
		String undefinedOTUSequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIDIndex();
		matrix.getMatrix().getObjectMap().put(undefinedOTUSequenceID, createSequence(undefinedOTUSequenceID, sequences.get(5), null));
		
		return matrix;
	}
	
	
	private StoreObjectData<LinkedLabeledIDEvent> createSequence(String id, List<String> tokens, String otuID) {		
		StoreObjectData<LinkedLabeledIDEvent> sequence = new StoreObjectData<LinkedLabeledIDEvent>(new LinkedLabeledIDEvent(EventContentType.OTU, 
				id, "sequence", otuID), null);
		
		sequence.getObjectContent().add(new SequenceTokensEvent(tokens));
		
		return sequence;
	}
	
	
	protected StoreMatrixDataAdapter createCellsMatrix(String otusID) {
		String matrixID = ReadWriteConstants.DEFAULT_MATRIX_ID_PREFIX + getIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "alignment", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(null, startEvent, false);
		
		// Add single tokens
		Iterator<String> iterator = document.getOTUList(otusID).getIDIterator();
		while (iterator.hasNext()) {
			String otuID = document.getOTUList(otusID).getObjectStartEvent(iterator.next()).getID();
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
		String matrixID = ReadWriteConstants.DEFAULT_MATRIX_ID_PREFIX + getIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "continuous data", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(null, startEvent, false);
		
		// Add single tokens
		Iterator<String> iterator = document.getOTUList(otusID).getIDIterator();
		while (iterator.hasNext()) {
			String otuID = document.getOTUList(otusID).getObjectStartEvent(iterator.next()).getID();
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
		singleTokenAdapter.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
	}
	
	
	protected StoreObjectData<TokenSetDefinitionEvent> createTokenSet(String id, CharacterStateSetType type, long alignmentLength) {
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DNA, id, "tokenSet"), new ArrayList<JPhyloIOEvent>());

		// Add single token definitions
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Adenin", "A", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Guanin", "G", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Cytosin", "C", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "Thymine", "T", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		// Add single token definition for ambiguity code
		Set<String> ambiguityCodeB = new HashSet<String>();
		ambiguityCodeB.add("C");
		ambiguityCodeB.add("G");
		ambiguityCodeB.add("T");

		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIDIndex(), "ambiguity code", "B", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.UNCERTAIN, ambiguityCodeB));
		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
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
	
	
	protected TestTreeNetworkGroupDataAdapter createTrees(String prefix) {
		String treeID = ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex();
		TestTreeNetworkGroupDataAdapter trees = new TestTreeNetworkGroupDataAdapter(treeID, null, "nodeEdgeID");
		trees.setLinkedOTUsID(prefix);
		return trees;
	}
	
	
//	protected List<JPhyloIOEvent> createMetaData(String about) {
//		List<JPhyloIOEvent> metaData = new ArrayList<JPhyloIOEvent>();
//		URI example = null;
//		
//		try {
//			example = new URI("somePath/#fragment");
//		} 
//		catch (URISyntaxException e) {
//			e.printStackTrace();
//		}
//		
//		metaData.add(new ResourceMetadataEvent("meta" + getIDIndex(), "ResourceMeta", new QName("http://meta.net/", "relations"), example, about));
//		
//		metaData.add(new LiteralMetadataEvent("meta" + getIDIndex(), "LiteralMeta", new QName("http://meta.net/", "predicate"), "literal value", LiteralContentSequenceType.SIMPLE));
//		
//		metaData.add(new CommentEvent("This is a ", true));
//		metaData.add(new CommentEvent("divided comment.", false));
//		
//		metaData.add(new LiteralMetadataContentEvent(null, "This is a long ", true));
//		metaData.add(new LiteralMetadataContentEvent(null, "literal text", false));
//		
//		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
//		
//		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
//		
//		
//		metaData.add(new ResourceMetadataEvent("meta" + getIdIndex(), "ResourceMeta", new QName("http://meta.net/", "relations"), 
//				example, null));
//		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
//		
//		metaData.add(new LiteralMetadataEvent("meta" + getIdIndex(), "LiteralMeta", new UriOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), "literal value", LiteralContentSequenceType.SIMPLE));
//		metaData.add(new LiteralMetadataContentEvent(new UriOrStringIdentifier(null, new QName(NeXMLConstants.TYPE_STRING)), "My literal value", true));
//		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));		
//		
//		return metaData;
//	}
}