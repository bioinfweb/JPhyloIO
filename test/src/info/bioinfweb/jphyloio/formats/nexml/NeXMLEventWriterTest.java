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
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeNetworkGroupDataAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;



public class NeXMLEventWriterTest {
	private StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
	private List<List<String>> sequences = new ArrayList<List<String>>();
	private long idIndex = 0;
	
	
	public long getIDIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}


	protected void createTestDocument() {
//		for (JPhyloIOEvent event : createMetaData("document")) {
//			document.getAnnotations().add(event);
//		}
		String taxaID = ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + getIDIndex();
		document.getOTUListsMap().put(taxaID, createOTUList(taxaID));
		document.getMatrices().add(createMatrix(taxaID));
		document.getTreesNetworks().add(createTrees(taxaID));
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
////		metaData.add(new ResourceMetadataEvent("meta" + getIdIndex(), "ResourceMeta", new QName("http://meta.net/", "relations"), example, about));
////		
////		metaData.add(new LiteralMetadataEvent("meta" + getIdIndex(), "LiteralMeta", new QName("http://meta.net/", "predicate"), "literal value", LiteralContentSequenceType.SIMPLE));
////		
////		metaData.add(new CommentEvent("This is a ", true));
////		metaData.add(new CommentEvent("divided comment.", false));
////		
////		metaData.add(new LiteralMetadataContentEvent(NeXMLConstants.TYPE_STRING, "This is a long ", true));
////		metaData.add(new LiteralMetadataContentEvent(NeXMLConstants.TYPE_STRING, "literal text", false));
////		
////		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
////		
////		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
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
	
	
	protected StoreOTUListDataAdapter createOTUList(String id) {
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, id, "taxa label"), null);
		
		for (int i = 0; i < 5; i++) {
			String otuID = ReadWriteConstants.DEFAULT_OTU_ID_PREFIX + getIDIndex();
			otuList.getOtus().getObjectMap().put(otuID, createOTU(otuID));
		}
		
		return otuList;
	}
	
	
	protected StoreObjectData<LabeledIDEvent> createOTU(String otuID) {		
		StoreObjectData<LabeledIDEvent> otu = new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
				otuID, "taxon"), null);
		
		return otu;
	}
	
	
	protected StoreMatrixDataAdapter createMatrix(String otusID) {
		String matrixID = ReadWriteConstants.DEFAULT_MATRIX_ID_PREFIX + getIDIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "matrix", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(null, startEvent, false);
		
		Iterator<String> iterator = document.getOTUList(otusID).getIDIterator();
		List<String> tokens = StringUtils.charSequenceToStringList("AGTCTTGCGCTTAGCAGTCGAC");
		sequences.add(tokens);
		
		while (iterator.hasNext()) {
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIDIndex();
			matrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID,
					tokens, document.getOTUList(otusID).getObjectStartEvent(iterator.next()).getID()));
//			matrix.getMatrix().getObjectMap().put(sequenceID, createSingleTokens(sequenceID, 
//					document.getOTUList(otusID).getObjectStartEvent(iterator.next()).getID()));
		}
		
		String undefinedOTUSequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIDIndex();
		matrix.getMatrix().getObjectMap().put(undefinedOTUSequenceID, createSequence(undefinedOTUSequenceID, tokens, null));
		
		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + getIDIndex();
		matrix.getTokenSets().getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, CharacterStateSetType.DNA));
		
		String charSetID = ReadWriteConstants.DEFAULT_CHAR_SET_ID_PREFIX + getIDIndex();
		matrix.getCharacterSets().getObjectMap().put(charSetID, createCharSet(charSetID, true));
		charSetID = ReadWriteConstants.DEFAULT_CHAR_SET_ID_PREFIX + getIDIndex();
		matrix.getCharacterSets().getObjectMap().put(charSetID, createCharSet(charSetID, false));
		
		return matrix;
	}
	
	
	protected StoreObjectData<LinkedLabeledIDEvent> createSequence(String id, List<String> tokens, String otuID) {		
		StoreObjectData<LinkedLabeledIDEvent> sequence = new StoreObjectData<LinkedLabeledIDEvent>(new LinkedLabeledIDEvent(EventContentType.OTU, 
				id, "sequence", otuID), null);
		
		sequence.getObjectContent().add(new SequenceTokensEvent(tokens));
		
//		for (JPhyloIOEvent event : createMetaData(id)) {
//			sequence.getObjectContent().add(event);
//		}
		
		return sequence;
	}
	
	
	protected StoreObjectData<LinkedLabeledIDEvent> createSingleTokens(String id, String otuID) {		
		StoreObjectData<LinkedLabeledIDEvent> singleTokens = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, id, "token definition", otuID), null);
		
		singleTokens.getObjectContent().add(new SingleSequenceTokenEvent(null, "A"));
		singleTokens.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
		
		singleTokens.getObjectContent().add(new SingleSequenceTokenEvent(null, "T"));
		
		singleTokens.getObjectContent().add(new LiteralMetadataEvent("meta" + getIDIndex(), "LiteralMeta", new URIOrStringIdentifier("literal value", new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.SIMPLE));
		singleTokens.getObjectContent().add(new LiteralMetadataContentEvent(new URIOrStringIdentifier(null, new QName("string")), "My literal value", true));
		singleTokens.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));		
		
		singleTokens.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
		
		singleTokens.getObjectContent().add(new SingleSequenceTokenEvent(null, "G"));
		singleTokens.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
		
		singleTokens.getObjectContent().add(new SingleSequenceTokenEvent(null, "G"));
		singleTokens.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN));
		
		return singleTokens;
	}
	
	
	protected StoreObjectData<TokenSetDefinitionEvent> createTokenSet(String id, CharacterStateSetType type) {		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DNA, id, "token set label"), new ArrayList<JPhyloIOEvent>());
		
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
		
//		Set<String> ambiguityCodeB = new HashSet<String>();
//		ambiguityCodeB.add("C");
//		ambiguityCodeB.add("G");
//		ambiguityCodeB.add("T");
//
//		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent(ReadWriteConstants.DEFAULT_TOKEN_DEFINITION_ID_PREFIX + getIdIndex(), null, "B", CharacterSymbolMeaning.CHARACTER_STATE, 
//				CharacterSymbolType.UNCERTAIN, ambiguityCodeB));
//		tokenSet.getObjectContent().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION));
		
		return tokenSet;
	}
	
	
	protected StoreObjectData<LinkedLabeledIDEvent> createCharSet(String id, boolean multipleIntervals) {	
		StoreObjectData<LinkedLabeledIDEvent> charSet = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.CHARACTER_SET, id, "char set label", null), new ArrayList<JPhyloIOEvent>());		
		
		if (multipleIntervals) {
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 5));
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(10, sequences.get(0).size() - 1));
		}
		else {
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(4, 9));
		}
		
		return charSet;
	}
	
	
	protected TestTreeNetworkGroupDataAdapter createTrees(String prefix) {
		String treeID = ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIDIndex();
		TestTreeNetworkGroupDataAdapter trees = new TestTreeNetworkGroupDataAdapter(treeID, null, "nodeEdgeID");
		trees.setLinkedOTUsID(prefix);
		return trees;
	}
	
	
	@Test
	public void test_writeDocument() throws Exception {
		createTestDocument();
		NeXMLEventWriter writer = new NeXMLEventWriter();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		writer.writeDocument(document, new File("data/testOutput/NeXMLTest.xml"), parameters);
	}
}