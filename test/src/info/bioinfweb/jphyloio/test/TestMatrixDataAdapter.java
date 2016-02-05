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
package info.bioinfweb.jphyloio.test;


import info.bioinfweb.commons.LongIDManager;
import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.implementations.NoSetsMatrixDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;



public class TestMatrixDataAdapter extends NoSetsMatrixDataAdapter {
	private SortedMap<String, List<String>> matrix = new TreeMap<>();
	private long columnCount;
	private boolean longTokens;

	
	private TestMatrixDataAdapter() {
		super();
	}
	
	
	public static TestMatrixDataAdapter newSingleCharTokenInstance(String... sequences) {
		LongIDManager idManager = new LongIDManager();
		TestMatrixDataAdapter result = new TestMatrixDataAdapter();
		result.longTokens = false;
		result.columnCount = sequences[0].length();  // Specifying an empty array leads to an exception.
		for (int i = 0; i < sequences.length; i++) {
			result.getMatrix().put("id" + idManager.createNewID(), StringUtils.charSequenceToStringList(sequences[i]));
			if ((result.columnCount != -1) && (sequences[i].length() != result.columnCount)) {
				result.columnCount = -1;
			}
		}
		return result;
	}
	
	
	public SortedMap<String, List<String>> getMatrix() {
		return matrix;
	}
	
	
	private List<String> getSequence(String sequenceID) throws IllegalArgumentException {
		List<String> sequence = getMatrix().get(sequenceID);
		if (sequence != null) {
			return sequence;
		}
		else {
			throw new IllegalArgumentException("No sequence with the ID " + sequenceID + " present.");
		}
	}
	

	@Override
	public void writeSequencePartContentData(JPhyloIOEventReceiver receiver, String sequenceID, long startColumn, 
			long endColumn) throws IllegalArgumentException, IOException {
		
		receiver.add(new SequenceTokensEvent(getSequence(sequenceID).subList((int)startColumn, (int)endColumn)));
	}
	
	
	@Override
	public LinkedOTUEvent getSequenceStartEvent(String sequenceID) {
		if (getMatrix().containsKey(sequenceID)) {
			return new LinkedOTUEvent(EventContentType.SEQUENCE, sequenceID, "Sequence " + sequenceID, null);
		}
		else {
			throw new IllegalArgumentException("No sequence with the ID " + sequenceID + " present.");
		}
	}
	
	
	@Override
	public long getSequenceLength(String sequenceID) throws IllegalArgumentException {
		return getSequence(sequenceID).size();
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
