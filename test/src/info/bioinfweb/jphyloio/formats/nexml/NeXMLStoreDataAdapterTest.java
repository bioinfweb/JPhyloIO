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
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;



public class NeXMLStoreDataAdapterTest {
	private StoreDocumentDataAdapter document = new StoreDocumentDataAdapter();
	private List<List<String>> sequences = new ArrayList<List<String>>();
	
	
	protected void createTestDocument() {		
		document.getOTUListsMap().put("taxa0", createOTUList());
		document.getMatrices().add(createMatrix(0, "taxa0"));
	}
	
	
	protected List<JPhyloIOEvent> createMetaData(String about) {
		List<JPhyloIOEvent> metaData = new ArrayList<JPhyloIOEvent>();
		
		metaData.add(new ResourceMetadataEvent("meta1", "resourceMeta", new QName("relations"), null, about));
		
		return metaData;
	}
	
	
	protected StoreOTUListDataAdapter createOTUList() {
		StoreOTUListDataAdapter otuList = new StoreOTUListDataAdapter(new LabeledIDEvent(EventContentType.OTU_LIST, "taxaID", "taxa label"), 
				createMetaData("taxaID"));
		
		for (int i = 0; i < 5; i++) {
			otuList.getObjectMap().put(ReadWriteConstants.DEFAULT_OTU_ID_PREFIX + i, createOTU(i));
		}		
		
		return otuList;
	}
	
	
	protected StoreObjectData<LabeledIDEvent> createOTU(int index) {		
		StoreObjectData<LabeledIDEvent> otu = new StoreObjectData<LabeledIDEvent>(new LabeledIDEvent(EventContentType.OTU, 
				ReadWriteConstants.DEFAULT_OTU_ID_PREFIX + index, "taxon " + index), null);		
		return otu;
	}
	
	
	protected StoreMatrixDataAdapter createMatrix(int index, String otusID) {
		String matrixID = ReadWriteConstants.DEFAULT_MATRIX_ID_PREFIX + index;
		LinkedOTUOrOTUsEvent startEvent = new LinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, matrixID, "matrix" + index, otusID);
		StoreMatrixDataAdapter matrix = new StoreMatrixDataAdapter(createMetaData(matrixID), startEvent, false);
		
		Iterator<String> iterator = document.getOTUList(otusID).getIDIterator();
		int sequenceIndex = 0;
		List<String> tokens = StringUtils.charSequenceToStringList("AGTCTTGCGCTTAGCAGTCGAC");
		sequences.add(tokens);
		
		while (iterator.hasNext()) {
			matrix.getMatrix().getObjectMap().put(ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + sequenceIndex, createSequence(sequenceIndex, 
					tokens, document.getOTUList(otusID).getObjectStartEvent(iterator.next()).getID()));
			sequenceIndex++;
		}
		
		matrix.getTokenSets().getObjectMap().put(ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + 0, createTokenSet(0, CharacterStateSetType.DNA));
		matrix.getCharacterSets().getObjectMap().put(ReadWriteConstants.DEFAULT_CHAR_SET_ID_PREFIX + 0, createCharSet(0));
		matrix.getCharacterSets().getObjectMap().put(ReadWriteConstants.DEFAULT_CHAR_SET_ID_PREFIX + 1, createCharSet(1));
		
		return matrix;
	}
	
	
	protected StoreObjectData<TokenSetDefinitionEvent> createTokenSet(int index, CharacterStateSetType type) {		
		StoreObjectData<TokenSetDefinitionEvent> tokenSet = new StoreObjectData<TokenSetDefinitionEvent>(
				new TokenSetDefinitionEvent(CharacterStateSetType.DNA, ReadWriteConstants.DEFAULT_TOKEN_SET_ID_PREFIX + 0, "token set label"), new ArrayList<JPhyloIOEvent>());
		
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
	
	
	protected StoreObjectData<LabeledIDEvent> createCharSet(int index) {		
		StoreObjectData<LabeledIDEvent> charSet = new StoreObjectData<LabeledIDEvent>(
				new LabeledIDEvent(EventContentType.CHARACTER_SET, ReadWriteConstants.DEFAULT_CHAR_SET_ID_PREFIX, "char set label"), new ArrayList<JPhyloIOEvent>());
		
		if (index == 0) {
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(0, 5));
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(10, sequences.get(0).size() - 1));
		}
		else if (index == 1) {
			charSet.getObjectContent().add(new CharacterSetIntervalEvent(4, 9));
		}
		
		
		return charSet;
	}
	
	
	protected StoreObjectData<LinkedOTUOrOTUsEvent> createSequence(int index, List<String> tokens, String otuID) {		
		StoreObjectData<LinkedOTUOrOTUsEvent> sequence = new StoreObjectData<LinkedOTUOrOTUsEvent>(new LinkedOTUOrOTUsEvent(EventContentType.OTU, 
				ReadWriteConstants.DEFAULT_SEQUENCE_ID_PREFIX + index, "taxon " + index, otuID), null);
		sequence.getObjectContent().add(new SequenceTokensEvent(tokens));
		return sequence;
	}
	
	
	@Test
	public void test_writeDocument() throws Exception {
		createTestDocument();
		NeXMLEventWriter writer = new NeXMLEventWriter();		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		writer.writeDocument(document, new File("data/testOutput/NeXMLTest.xml"), parameters);		
	}
}
