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
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeDataAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;



public class NeXMLStoreDataAdapterTest {
	private StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
	private List<List<String>> sequences = new ArrayList<List<String>>();
	private long idIndex = 0;
	
	
	public long getIdIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}


	protected void createTestDocument() {
		for (JPhyloIOEvent event : createMetaData("document")) {
			document.getAnnotations().add(event);
		}
		String taxaID = ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + getIdIndex();
		document.getOTUListsMap().put(taxaID, createOTUList(taxaID));
		document.getMatrices().add(createMatrix(taxaID));
		document.getTreesNetworks().add(createTree(taxaID));
	}
	
	
	protected List<JPhyloIOEvent> createMetaData(String about) {
		List<JPhyloIOEvent> metaData = new ArrayList<JPhyloIOEvent>();
		
		metaData.add(new ResourceMetadataEvent("meta" + getIdIndex(), "ResourceMeta", new QName("relations"), null, about));
		metaData.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		return metaData;
	}
	
	
	protected StoreOTUListDataAdapter createOTUList(String id) {
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, id, "taxa label"), 
				createMetaData(id));
		
		for (int i = 0; i < 5; i++) {
			String otuID = ReadWriteConstants.DEFAULT_OTU_ID_PREFIX + getIdIndex();
			otuList.getObjectMap().put(otuID, createOTU(otuID));
		}		
		
		return otuList;
	}
	
	
	protected StoreObjectData<LabeledIDEvent> createOTU(String otuID) {		
		StoreObjectData<LabeledIDEvent> otu = new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
				otuID, "taxon"), null);		
		return otu;
	}
	
	
	protected StoreMatrixDataAdapter createMatrix(String otusID) {
		String matrixID = ReadWriteConstants.DEFAULT_MATRIX_ID_PREFIX + getIdIndex();
		LinkedLabeledIDEvent startEvent = new LinkedLabeledIDEvent(EventContentType.ALIGNMENT, matrixID, "matrix", otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(createMetaData(matrixID), startEvent, false);
		
		Iterator<String> iterator = document.getOTUList(otusID).getIDIterator();
		List<String> tokens = StringUtils.charSequenceToStringList("AGTCTTGCGCTTAGCAGTCGAC");
		sequences.add(tokens);
		
		while (iterator.hasNext()) {
			String sequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIdIndex();
			matrix.getMatrix().getObjectMap().put(sequenceID, createSequence(sequenceID,
					tokens, document.getOTUList(otusID).getObjectStartEvent(iterator.next()).getID()));
		}
		
		String undefinedOTUSequenceID = ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIdIndex();
		matrix.getMatrix().getObjectMap().put(undefinedOTUSequenceID, createSequence(undefinedOTUSequenceID, tokens, null));
		
//		matrix.getMatrix().getObjectMap().put(ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + sequenceIndex, createSingleTokens(sequenceIndex, 
//				null));
		
		String tokenSetID = ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + getIdIndex();
		matrix.getTokenSets().getObjectMap().put(tokenSetID, createTokenSet(tokenSetID, CharacterStateSetType.DNA));
		
		
		String charSetID = ReadWriteConstants.DEFAULT_CHAR_SET_ID_PREFIX + getIdIndex();
		matrix.getCharacterSets().getObjectMap().put(charSetID, createCharSet(charSetID, true));
		charSetID = ReadWriteConstants.DEFAULT_CHAR_SET_ID_PREFIX + getIdIndex();
		matrix.getCharacterSets().getObjectMap().put(charSetID, createCharSet(charSetID, false));
		
		
		return matrix;
	}
	
	
	protected StoreObjectData<LinkedLabeledIDEvent> createSequence(String id, List<String> tokens, String otuID) {		
		StoreObjectData<LinkedLabeledIDEvent> sequence = new StoreObjectData<LinkedLabeledIDEvent>(new LinkedLabeledIDEvent(EventContentType.OTU, 
				id, "sequence", otuID), null);
		sequence.getObjectContent().add(new SequenceTokensEvent(tokens));
		return sequence;
	}
	
	
	protected StoreObjectData<TokenSetDefinitionEvent> createTokenSet(String id, CharacterStateSetType type) {		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DNA, id, "token set label"), new ArrayList<JPhyloIOEvent>());
		
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent("token1", "Adenin", "A", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent("token1", "Guanin", "G", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent("token1", "Cytosin", "C", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		tokenSet.getObjectContent().add(new SingleTokenDefinitionEvent("token1", "Thymin", "T", CharacterSymbolMeaning.CHARACTER_STATE, 
				CharacterSymbolType.ATOMIC_STATE));
		
		return tokenSet;
	}
	
	
	protected StoreObjectData<LabeledIDEvent> createCharSet(String id, boolean multipleIntervals) {	
		StoreObjectData<LabeledIDEvent> charSet = new StoreObjectData<LabeledIDEvent>(
				new LabeledIDEvent(EventContentType.CHARACTER_SET, id, "char set label"), new ArrayList<JPhyloIOEvent>());		
		
		if (multipleIntervals) {
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 5));
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(10, sequences.get(0).size() - 1));
		}
		else {
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(4, 9));
		}
		
		return charSet;
	}
	
	
	protected StoreObjectData<LinkedLabeledIDEvent> createSingleTokens(String otuID) {		
		StoreObjectData<LinkedLabeledIDEvent> singleTokens = new StoreObjectData<LinkedLabeledIDEvent>(new LinkedLabeledIDEvent(EventContentType.OTU, 
				ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + getIdIndex(), "token definition", otuID), null);
		singleTokens.getObjectContent().add(new SingleSequenceTokenEvent("label", "A"));
		singleTokens.getObjectContent().add(new SingleSequenceTokenEvent("label", "C"));
		singleTokens.getObjectContent().add(new SingleSequenceTokenEvent("label", "G"));
		singleTokens.getObjectContent().add(new SingleSequenceTokenEvent("label", "T"));
		return singleTokens;
	}
	
	
	private TestTreeDataAdapter createTree(String prefix) {
		String treeID = ReadWriteConstants.DEFAULT_TREE_ID_PREFIX + getIdIndex();
		TestTreeDataAdapter tree = new TestTreeDataAdapter(treeID, null, prefix);
		tree.setLinkedOTUsID(prefix);
		return tree;
	}
	
	
	@Test
	public void test_writeDocument() throws Exception {
		createTestDocument();
		NeXMLEventWriter writer = new NeXMLEventWriter();		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		writer.writeDocument(document, new File("data/testOutput/NeXMLTest.xml"), parameters);	
	}
}
