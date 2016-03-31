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
package info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter;


import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;



public class StoreMatrixDataAdapter extends StoreAnnotatedDataAdapter implements MatrixDataAdapter {
	private StoreLinkedOTUsDataAdapter storeLinkedOTUsAdapter;
	private StoreObjectListDataAdapter<LinkedOTUOrOTUsEvent> matrix = new StoreObjectListDataAdapter<LinkedOTUOrOTUsEvent>();
	private StoreObjectListDataAdapter<TokenSetDefinitionEvent> tokenSets = new StoreObjectListDataAdapter<TokenSetDefinitionEvent>();
	private StoreObjectListDataAdapter<LabeledIDEvent> characterSets = new StoreObjectListDataAdapter<LabeledIDEvent>();
	private boolean longTokens;
	
	
	public StoreMatrixDataAdapter(List<JPhyloIOEvent> annotations, LinkedOTUOrOTUsEvent alignmentStartEvent, boolean longTokens) {
		super(annotations);
		this.storeLinkedOTUsAdapter = new StoreLinkedOTUsDataAdapter(alignmentStartEvent);
		this.longTokens = longTokens;
	}


	@Override
	public List<JPhyloIOEvent> getAnnotations() {
		return super.getAnnotations();
	}	
	

	@Override
	public void writeMetadata(JPhyloIOEventReceiver receiver) throws IOException {
		super.writeMetadata(receiver);
	}


	@Override
	public boolean hasMetadata() {
		return super.hasMetadata();
	}
	

	public LinkedOTUOrOTUsEvent getStartEvent() {
		return storeLinkedOTUsAdapter.getStartEvent();
	}


	public void setStartEvent(LinkedOTUOrOTUsEvent startEvent) {
		storeLinkedOTUsAdapter.setStartEvent(startEvent);
	}


	@Override
	public long getSequenceCount() {
		return matrix.getObjectMap().size();
	}
	

	@Override
	public long getColumnCount() { //TODO do not go through all sequences in case they are very long
		long previousLength = 0;
		long currentLength = 0;
		
		Iterator<String> sequences = getSequenceIDIterator();
		while (sequences.hasNext()) {
			currentLength = getSequenceLength(sequences.next());
			if ((previousLength != 0) && (previousLength != currentLength)) {
				return -1;
			}
		}
	
		return currentLength;
	}
	

	@Override
	public boolean containsLongTokens() {
		return longTokens;
	}


	public StoreObjectListDataAdapter<LinkedOTUOrOTUsEvent> getMatrix() {
		return matrix;
	}


	@Override
	public StoreObjectListDataAdapter<LabeledIDEvent> getCharacterSets() {
		return characterSets;
	}
	

	@Override
	public StoreObjectListDataAdapter<TokenSetDefinitionEvent> getTokenSets() {
		return tokenSets;
	}
	

	@Override
	public Iterator<String> getSequenceIDIterator() {
		return matrix.getObjectMap().keyList().iterator();
	}
	

	@Override
	public LinkedOTUOrOTUsEvent getSequenceStartEvent(String sequenceID) {
		return matrix.getObjectStartEvent(sequenceID);
	}
	

	@Override
	public long getSequenceLength(String sequenceID) throws IllegalArgumentException {
		int sequenceLength = 0;
		for (JPhyloIOEvent event : matrix.getObjectContent(sequenceID)) {
			if (event.getType().getContentType().equals(EventContentType.SINGLE_SEQUENCE_TOKEN)) {
				sequenceLength++;
			}
			else if (event.getType().getContentType().equals(EventContentType.SEQUENCE_TOKENS)) {
				sequenceLength += event.asSequenceTokensEvent().getCharacterValues().size();
			}
		}
		return sequenceLength;
	}
	

	@Override
	public void writeSequencePartContentData(JPhyloIOEventReceiver receiver, String sequenceID, long startColumn, 
			long endColumn) throws IOException, IllegalArgumentException {
		for (JPhyloIOEvent event : matrix.getObjectContent(sequenceID)) {
			receiver.add(event);
		}
	}
}
