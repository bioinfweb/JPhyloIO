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
package info.bioinfweb.jphyloio.dataadapters.implementations.store;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;

import java.util.List;



public class StoreTreeNetworkDataAdapter extends StoreAnnotatedDataAdapter<LinkedLabeledIDEvent> implements TreeNetworkDataAdapter {
	private LabeledIDEvent startEvent;
	private boolean isTree;
	private StoreObjectListDataAdapter<NodeEvent> nodes = new StoreObjectListDataAdapter<NodeEvent>();
	private StoreObjectListDataAdapter<EdgeEvent> edges = new StoreObjectListDataAdapter<EdgeEvent>();
	private StoreObjectListDataAdapter<LinkedLabeledIDEvent> nodeEdgeSets = new StoreObjectListDataAdapter<LinkedLabeledIDEvent>();
	
	
	public StoreTreeNetworkDataAdapter(LabeledIDEvent startEvent, boolean isTree, List<JPhyloIOEvent> annotations) {
		super(annotations);
		this.startEvent = startEvent;
		this.isTree = isTree;
	}


	@Override
	public LabeledIDEvent getStartEvent(ReadWriteParameterMap parameters) {
		return startEvent;
	}


	@Override
	public boolean isTree(ReadWriteParameterMap parameters) {
		return isTree;
	}


	@Override
	public StoreObjectListDataAdapter<NodeEvent> getNodes(ReadWriteParameterMap parameters) {
		return nodes;
	}


	@Override
	public StoreObjectListDataAdapter<EdgeEvent> getEdges(ReadWriteParameterMap parameters) {
		return edges;
	}


	@Override
	public ObjectListDataAdapter<LinkedLabeledIDEvent> getNodeEdgeSets(ReadWriteParameterMap parameters) {
		return nodeEdgeSets;
	}	
}
