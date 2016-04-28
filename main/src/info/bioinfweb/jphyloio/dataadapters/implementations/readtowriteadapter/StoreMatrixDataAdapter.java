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
import info.bioinfweb.jphyloio.dataadapters.MetadataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;

import java.io.IOException;
import java.util.Iterator;



public class StoreMatrixDataAdapter extends StoreAnnotatedDataAdapter<LinkedLabeledIDEvent> implements MatrixDataAdapter {
	private LinkedLabeledIDEvent startEvent;
	private StoreObjectListDataAdapter<LinkedLabeledIDEvent> matrix = new StoreObjectListDataAdapter<LinkedLabeledIDEvent>();
	private StoreObjectListDataAdapter<TokenSetDefinitionEvent> tokenSets = new StoreObjectListDataAdapter<TokenSetDefinitionEvent>();
	private StoreObjectListDataAdapter<LinkedLabeledIDEvent> characterSets = new StoreObjectListDataAdapter<LinkedLabeledIDEvent>();
	private boolean longTokens;
	
	
	public StoreMatrixDataAdapter(MetadataAdapter annotations, LinkedLabeledIDEvent alignmentStartEvent, boolean longTokens) {
		super(annotations);
		this.startEvent = alignmentStartEvent;
		this.longTokens = longTokens;
	}


	@Override
	public LinkedLabeledIDEvent getStartEvent() {
		return startEvent;
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


	public StoreObjectListDataAdapter<LinkedLabeledIDEvent> getMatrix() {
		return matrix;
	}


	@Override
	public StoreObjectListDataAdapter<LinkedLabeledIDEvent> getCharacterSets() {
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
	public LinkedLabeledIDEvent getSequenceStartEvent(String sequenceID) throws IllegalArgumentException {
		if (matrix.getObjectMap().keyList().contains(sequenceID)) {
			return matrix.getObjectStartEvent(sequenceID);
		}
		else {
			throw new IllegalArgumentException("The alignment does not contain a sequence with the ID \"" + sequenceID +"\".");
		}
	}
	

	@Override
	public long getSequenceLength(String sequenceID) throws IllegalArgumentException {
		if (matrix.getObjectMap().keyList().contains(sequenceID)) {
			int sequenceLength = 0;
			for (JPhyloIOEvent event : matrix.getObjectContent(sequenceID)) {
				if (event.getType().equals(new EventType(EventContentType.SINGLE_SEQUENCE_TOKEN, EventTopologyType.START))) {
					sequenceLength++;
				}
				else if (event.getType().getContentType().equals(EventContentType.SEQUENCE_TOKENS)) {
					sequenceLength += event.asSequenceTokensEvent().getCharacterValues().size();
				}
			}
			return sequenceLength;
		}
		else {
			throw new IllegalArgumentException("The alignment does not contain a sequence with the ID \"" + sequenceID +"\".");
		}
	}
	

	@Override
	public void writeSequencePartContentData(JPhyloIOEventReceiver receiver, String sequenceID, long startColumn, 
			long endColumn) throws IOException, IllegalArgumentException {
		if (matrix.getObjectMap().keyList().contains(sequenceID)) {
			for (JPhyloIOEvent event : matrix.getObjectContent(sequenceID)) {
				receiver.add(event);
			}
		}
		else {
			throw new IllegalArgumentException("The alignment does not contain a sequence with the ID \"" + sequenceID +"\".");
		}		
	}
}
