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
package info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreObjectData;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreTreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.SetElementEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class NetworkDataAdapter extends StoreTreeNetworkDataAdapter implements TreeNetworkDataAdapter {
	private String id = null;
	private String label = null;
	private String nodeEdgeIDPrefix = "";
	private String[] linkedOTUs;
	
	private String linkedOTUList = null;
	
	
	public NetworkDataAdapter(String id, String label, String nodeEdgeIDPrefix, String[] linkedOTUs) {
		super(new LabeledIDEvent(EventContentType.NETWORK, id, label), true, null);
		this.id = id;
		this.label = label;
		if (linkedOTUs.length != 3) {
			throw new IllegalArgumentException("Invalid number of linked OTUs (" + linkedOTUs.length + ").");
		}
		else {
			this.linkedOTUs = linkedOTUs;
		}
		this.nodeEdgeIDPrefix = nodeEdgeIDPrefix;		

		addEdges(getEdges(null));
		addNodes(getNodes(null));
		addNodeEdgeSets((StoreObjectListDataAdapter<LinkedLabeledIDEvent>)getNodeEdgeSets(null));
	}
	

	public NetworkDataAdapter(String id, String label, String nodeEdgeIDPrefix) {
		this(id, label, nodeEdgeIDPrefix, new String[]{null, null, null});
	}
	

	private void addEdges(StoreObjectListDataAdapter<EdgeEvent> edges) {
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "eRoot", "Root edge", null, nodeEdgeIDPrefix + "nRoot", 1.5));
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "e1", "Internal edge", nodeEdgeIDPrefix + "nRoot", nodeEdgeIDPrefix + "n1", 1.0));		
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "eA", "Leaf edge A", nodeEdgeIDPrefix + "n1", nodeEdgeIDPrefix + "nA", 1.1));		
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "eB", "Leaf edge B", nodeEdgeIDPrefix + "n1", nodeEdgeIDPrefix + "nB", Double.NaN));
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "eC", "Leaf edge C", nodeEdgeIDPrefix + "nRoot", nodeEdgeIDPrefix + "nC", 2.0));
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "e2", "network edge", nodeEdgeIDPrefix + "nB", nodeEdgeIDPrefix + "nC", 1.4));
	}


	private void addNodes(StoreObjectListDataAdapter<NodeEvent> nodes) {
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "n1", "Node '_1", null, false));		
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "nRoot", "Node " + nodeEdgeIDPrefix + "nRoot", null, true));
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "nA", "Node " + nodeEdgeIDPrefix + "nA", linkedOTUs != null ? linkedOTUs[0] : null, false));
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "nB", "Node " + nodeEdgeIDPrefix + "nB", linkedOTUs != null ? linkedOTUs[1] : null, false));
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "nC", "Node " + nodeEdgeIDPrefix + "nC", linkedOTUs != null ? linkedOTUs[2] : null, false));
	}
	
	
	private void addNodeEdgeSets(StoreObjectListDataAdapter<LinkedLabeledIDEvent> nodeEdgeSets) {
		String nodeEdgeSetID = nodeEdgeIDPrefix + ReadWriteConstants.DEFAULT_NODE_EDGE_SET_ID_PREFIX;
		StoreObjectData<LinkedLabeledIDEvent> nodeEdgeSet = new StoreObjectData<LinkedLabeledIDEvent>(
				new LinkedLabeledIDEvent(EventContentType.NODE_EDGE_SET, nodeEdgeSetID, null, getLinkedOTUList()));
		nodeEdgeSet.getObjectContent().add(new SetElementEvent(nodeEdgeIDPrefix + "nA", EventContentType.NODE));
		nodeEdgeSet.getObjectContent().add(new SetElementEvent(nodeEdgeIDPrefix + "nB", EventContentType.NODE));
		nodeEdgeSet.getObjectContent().add(new SetElementEvent(nodeEdgeIDPrefix + "e1", EventContentType.EDGE));
		nodeEdgeSet.getObjectContent().add(new SetElementEvent(nodeEdgeIDPrefix + "eRoot", EventContentType.ROOT_EDGE));
		
		nodeEdgeSets.getObjectMap().put(nodeEdgeSetID, nodeEdgeSet);
	}


	public String getNodeEdgeIDPrefix() {
		return nodeEdgeIDPrefix;
	}


	@Override
	public LabeledIDEvent getStartEvent(ReadWriteParameterMap parameters) {
		return new LabeledIDEvent(EventContentType.NETWORK, id, label);
	}


	@Override
	public boolean isTree(ReadWriteParameterMap parameters) {
		return false;
	}
	

	public String getLinkedOTUList() {
		return linkedOTUList;
	}


	public void setLinkedOTUList(String linkedOTUList) {
		this.linkedOTUList = linkedOTUList;
	}
}