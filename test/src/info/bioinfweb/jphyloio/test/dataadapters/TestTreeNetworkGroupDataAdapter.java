/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.EdgeAndNodeMetaDataTree;

import java.util.Arrays;
import java.util.Iterator;



public class TestTreeNetworkGroupDataAdapter extends EmptyAnnotatedDataAdapter<LinkedLabeledIDEvent> implements TreeNetworkGroupDataAdapter {
	private String id = null;
	private String label = null;
	private String linkedOTUsID = null;
	private TreeNetworkDataAdapter tree = null;

	
	public TestTreeNetworkGroupDataAdapter(String id, String label, String nodeEdgeIDPrefix, String[] linkedOTUs) {
		super();
		this.id = ReadWriteConstants.DEFAULT_TREES_ID_PREFIX + id;
		this.label = null;
		if (linkedOTUs.length != 3) {
			throw new IllegalArgumentException("Invalid number of linked OTUs (" + linkedOTUs.length + ").");
		}
		this.tree = new EdgeAndNodeMetaDataTree(id, label, nodeEdgeIDPrefix, linkedOTUs);
	}


	public TestTreeNetworkGroupDataAdapter(String id, String label, String nodeEdgeIDPrefix) {
		this(id, label, nodeEdgeIDPrefix, new String[]{null, null, null});
	}


	@Override
	public LinkedLabeledIDEvent getStartEvent() {
		return new LinkedLabeledIDEvent(EventContentType.TREE, id, label, linkedOTUsID);
	}
	

	@Override
	public Iterator<TreeNetworkDataAdapter> getTreeNetworkIterator() {
		return Arrays.asList(new TreeNetworkDataAdapter[]{tree}).iterator();
	}


	public void setLinkedOTUsID(String linkedOTUsID) {
		this.linkedOTUsID = linkedOTUsID;
	}
}
