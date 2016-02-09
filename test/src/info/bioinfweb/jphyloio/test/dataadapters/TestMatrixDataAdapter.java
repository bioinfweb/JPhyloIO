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
import info.bioinfweb.jphyloio.dataadapters.implementations.NoSetsMatrixDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;



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
	
	
	private SortedMap<String, SequenceData> matrix = new TreeMap<>();
	private long columnCount;
	private boolean longTokens;

	
	public TestMatrixDataAdapter(boolean containsLabels, String... sequencesOrLabelsAndSequences) {
		super();
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
	
	
	public SortedMap<String, SequenceData> getMatrix() {
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
	
	
	private SequenceData getSequence(String sequenceID) throws IllegalArgumentException {
		SequenceData result = getMatrix().get(sequenceID);
		if (result != null) {
			return result;
		}
		else {
			throw new IllegalArgumentException("No sequence with the ID " + sequenceID + " present.");
		}
	}
	

	@Override
	public void writeSequencePartContentData(JPhyloIOEventReceiver receiver, String sequenceID, long startColumn, 
			long endColumn) throws IllegalArgumentException, IOException {
		
		SequenceData data = getSequence(sequenceID);
		if (startColumn == 0) {  // Write leading events.
			for (JPhyloIOEvent event : data.leadingEvents) {
				receiver.add(event);
			}
		}
		receiver.add(new SequenceTokensEvent(data.tokens.subList((int)startColumn, (int)endColumn)));
	}
	
	
	@Override
	public LinkedOTUEvent getSequenceStartEvent(String sequenceID) {
		return new LinkedOTUEvent(EventContentType.SEQUENCE, sequenceID, getSequence(sequenceID).label, 
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
}
