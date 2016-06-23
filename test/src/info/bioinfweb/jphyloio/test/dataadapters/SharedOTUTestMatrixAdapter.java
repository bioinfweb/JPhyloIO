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


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class SharedOTUTestMatrixAdapter extends TestMatrixDataAdapter {
	private String sharedOTUID;
	
	
	public SharedOTUTestMatrixAdapter(String id, String label, String sharedOTUID, boolean containsLabels, 
			String... sequencesOrLabelsAndSequences) {
		
		super(id, label, containsLabels, sequencesOrLabelsAndSequences);
		this.sharedOTUID = sharedOTUID;
	}
	

	@Override
	public LinkedLabeledIDEvent getSequenceStartEvent(ReadWriteParameterMap parameters, String sequenceID) {
		return new LinkedLabeledIDEvent(EventContentType.SEQUENCE, sequenceID, getSequence(sequenceID).label, sharedOTUID);
	}	
}
