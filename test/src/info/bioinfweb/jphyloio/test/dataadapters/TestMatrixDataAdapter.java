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
package info.bioinfweb.jphyloio.test.dataadapters;


import info.bioinfweb.commons.LongIDManager;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.NoSetsMatrixDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.map.ListOrderedMap;



public class TestMatrixDataAdapter extends NoSetsMatrixDataAdapter implements ReadWriteConstants {
	public static class SequenceData {
		public String label;
		public List<JPhyloIOEvent> leadingEvents;
		public List<String> tokens;
		
		public SequenceData(String label, List<String> tokens) {
			super();
			this.label = label;
			this.leadingEvents = new ArrayList<JPhyloIOEvent>();
			this.tokens = tokens;
		}
	}
	
	
	private String id = null;
	private String label = null;
	private String linkedOTUsID = null;
	private ListOrderedMap<String, SequenceData> matrix = new ListOrderedMap<>();
	private ObjectListDataAdapter<TokenSetDefinitionEvent> tokenSets = null;
	private ObjectListDataAdapter<LabeledIDEvent> characterSets = null;
	private long columnCount;
	private boolean longTokens;

	
	public TestMatrixDataAdapter(String id, String label, boolean containsLabels, String... sequencesOrLabelsAndSequences) {
		super();
		this.id = id;
		this.label = label;
		if (sequencesOrLabelsAndSequences.length == 0) {
			longTokens = true;
			columnCount = 0;
		}
		else {
			if (containsLabels) {
				createSingleCharTokenInstanceWithLabels(sequencesOrLabelsAndSequences);
			}
			else {
				createSingleCharTokenInstance(sequencesOrLabelsAndSequences);
			}
		}
	}
	
	
	private void createSingleCharTokenInstance(String... sequences) {
		LongIDManager idManager = new LongIDManager();
		longTokens = false;
		columnCount = sequences[0].length();
		for (int i = 0; i < sequences.length; i++) {
			long id = idManager.createNewID();
			getMatrix().put(DEFAULT_SEQUENCE_ID_PREFIX + id, new SequenceData("Sequence " + id, 
					StringUtils.charSequenceToStringList(sequences[i])));
			if ((columnCount != -1) && (sequences[i].length() != columnCount)) {
				columnCount = -1;
			}
		}
	}
	
	
	public void createSingleCharTokenInstanceWithLabels(String... labelsAndSequences) {
		if (labelsAndSequences.length % 2 != 0) {
			throw new IllegalArgumentException("There must be the same number of labels and sequences.");
		}
		else {
			LongIDManager idManager = new LongIDManager();
			longTokens = false;
			columnCount = labelsAndSequences[1].length();
			for (int i = 0; i < labelsAndSequences.length; i += 2) {
				getMatrix().put(DEFAULT_SEQUENCE_ID_PREFIX + idManager.createNewID(), new SequenceData(labelsAndSequences[i], 
						StringUtils.charSequenceToStringList(labelsAndSequences[i + 1])));
				if ((columnCount != -1) && (labelsAndSequences[i + 1].length() != columnCount)) {
					columnCount = -1;
				}
			}
		}
	}
	
	
	public void setID(String id) {
		this.id = id;
	}


	public void setLabel(String label) {
		this.label = label;
	}


	public ListOrderedMap<String, SequenceData> getMatrix() {
		return matrix;
	}
	
	
	public TestOTUListDataAdapter createAccordingOTUList(int indexOfList) {
		String[] otuIDs = new String[getMatrix().size()];
		int index = 0;
		for (String sequenceID : getMatrix().keySet()) {
			otuIDs[index] = sequenceID.replace(DEFAULT_SEQUENCE_ID_PREFIX, DEFAULT_OTU_ID_PREFIX);
			index++;
		}
		return new TestOTUListDataAdapter(indexOfList, otuIDs);
	}
	
	
	protected SequenceData getSequence(String sequenceID) throws IllegalArgumentException {
		SequenceData result = getMatrix().get(sequenceID);
		if (result != null) {
			return result;
		}
		else {
			throw new IllegalArgumentException("No sequence with the ID " + sequenceID + " present.");
		}
	}
	

	public void setLinkedOTUsID(String linkedOTUsID) {
		this.linkedOTUsID = linkedOTUsID;
	}


	@Override
	public LinkedOTUOrOTUsEvent getStartEvent() {
		return new LinkedOTUOrOTUsEvent(EventContentType.ALIGNMENT, id, label, linkedOTUsID);
	}


	@Override
	public void writeSequencePartContentData(JPhyloIOEventReceiver receiver, String sequenceID, long startColumn, 
			long endColumn) throws IOException {
		
		SequenceData data = getSequence(sequenceID);
		if (startColumn == 0) {  // Write leading events.
			for (JPhyloIOEvent event : data.leadingEvents) {
				receiver.add(event);
			}
		}
		receiver.add(new SequenceTokensEvent(data.tokens.subList((int)startColumn, (int)endColumn)));
	}
	
	
	@Override
	public LinkedOTUOrOTUsEvent getSequenceStartEvent(String sequenceID) {
		return new LinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, sequenceID, getSequence(sequenceID).label, 
				sequenceID.replace(DEFAULT_SEQUENCE_ID_PREFIX, DEFAULT_OTU_ID_PREFIX));
	}
	
	
	@Override
	public long getSequenceLength(String sequenceID) throws IllegalArgumentException {
		return getSequence(sequenceID).tokens.size();
	}
	
	
	@Override
	public Iterator<String> getSequenceIDIterator() {
		return getMatrix().keySet().iterator();
	}
	
	
	@Override
	public long getSequenceCount() {
		return getMatrix().size();
	}
	
	
	@Override
	public long getColumnCount() {
		return columnCount;
	}
	
	
	@Override
	public boolean containsLongTokens() {
		return longTokens;
	}


	@Override
	public ObjectListDataAdapter<TokenSetDefinitionEvent> getTokenSets() {
		if (tokenSets == null) {
			return super.getTokenSets();
		}
		else {
			return tokenSets;
		}
	}


	public void setTokenSets(ObjectListDataAdapter<TokenSetDefinitionEvent> tokenSets) {
		this.tokenSets = tokenSets;
	}


	@Override
	public ObjectListDataAdapter<LabeledIDEvent> getCharacterSets() {
		if (characterSets == null) {
			return super.getCharacterSets();
		}
		else {
			return characterSets;
		}
	}

	
	public void setCharacterSets(ObjectListDataAdapter<LabeledIDEvent> characterSets) {
		this.characterSets = characterSets;
	}
}
