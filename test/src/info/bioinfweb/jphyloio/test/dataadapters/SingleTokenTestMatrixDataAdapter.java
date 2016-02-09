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


import info.bioinfweb.commons.Math2;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.IOException;



public class SingleTokenTestMatrixDataAdapter extends TestMatrixDataAdapter {
	public static final int SINGLE_TOKEN_INDEX = 2;
	
	
	public SingleTokenTestMatrixDataAdapter(boolean containsLabels,	String... sequencesOrLabelsAndSequences) {
		super(containsLabels, sequencesOrLabelsAndSequences);
	}

	
	@Override
	public void writeSequencePartContentData(JPhyloIOEventReceiver receiver, String sequenceID, long startColumn, long endColumn)
			throws IllegalArgumentException, IOException {
		
		String firstID = getMatrix().keySet().iterator().next();  // Throws exception, if map is empty.
		if (firstID.equals(sequenceID) && Math2.isBetween(SINGLE_TOKEN_INDEX, startColumn, endColumn - 1)) {
			if (startColumn < SINGLE_TOKEN_INDEX) {
				super.writeSequencePartContentData(receiver, sequenceID, startColumn, SINGLE_TOKEN_INDEX);
			}
			
			receiver.add(new SingleSequenceTokenEvent(getMatrix().get(firstID).tokens.get(SINGLE_TOKEN_INDEX)));
			receiver.add(new MetaInformationEvent("someKey", "someType", "someValue"));
			receiver.add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
			receiver.add(new ConcreteJPhyloIOEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, EventTopologyType.END));
			
			if (SINGLE_TOKEN_INDEX < endColumn) {
				super.writeSequencePartContentData(receiver, sequenceID, SINGLE_TOKEN_INDEX + 1, endColumn);
			}
		}
		else {
			super.writeSequencePartContentData(receiver, sequenceID, startColumn, endColumn);
		}
	}
}