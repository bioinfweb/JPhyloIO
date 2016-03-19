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


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public class TestTreeDataAdapter extends EmptyAnnotatedDataAdapter implements TreeNetworkDataAdapter {
	private String id = null;
	private String label = null;
	private String linkedOTUsID = null;
	private String nodeEdgeIDPrefix = "";
	private String[] linkedOTUs;
	
	
	public TestTreeDataAdapter(String id, String label, String nodeEdgeIDPrefix, String[] linkedOTUs) {
		super();
		this.id = id;
		this.label = label;
		if (linkedOTUs.length != 3) {
			throw new IllegalArgumentException("Invalid number of linked OTUs (" + linkedOTUs.length + ").");
		}
		this.nodeEdgeIDPrefix = nodeEdgeIDPrefix;
		this.linkedOTUs = linkedOTUs;
	}


	public TestTreeDataAdapter(String id, String label, String nodeEdgeIDPrefix) {
		this(id, label, nodeEdgeIDPrefix, new String[]{null, null, null});
	}
	
	
	public void setLinkedOTUsID(String linkedOTUsID) {
		this.linkedOTUsID = linkedOTUsID;
	}


	public String getNodeEdgeIDPrefix() {
		return nodeEdgeIDPrefix;
	}


	@Override
	public LinkedOTUOrOTUsEvent getStartEvent() {
		return new LinkedOTUOrOTUsEvent(EventContentType.TREE, id, label, linkedOTUsID);
	}


	@Override
	public boolean isTree() {
		return true;
	}

	
	@Override
	public boolean considerRooted() {
		return true;
	}


	@Override
	public Iterator<String> getRootEdgeIDs() {
		return Arrays.asList(new String[]{nodeEdgeIDPrefix + "eRoot"}).iterator();
	}

	
	@Override
	public void writeNodeData(JPhyloIOEventReceiver receiver, String nodeID) throws IOException {
		if (nodeID.startsWith(nodeEdgeIDPrefix)) {
			switch (nodeID.substring(nodeEdgeIDPrefix.length())) {
				case "n1":
					receiver.add(new LinkedOTUOrOTUsEvent(EventContentType.NODE, nodeID, "Node '_1", null));
					receiver.add(new MetaInformationEvent("a1", null, "100", new Integer(100)));
					receiver.add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
					receiver.add(new MetaInformationEvent("a2", null, "ab 'c", "ab 'c"));
					receiver.add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
					break;
				case "nRoot":  
					receiver.add(new LinkedOTUOrOTUsEvent(EventContentType.NODE, nodeID, "Node " + nodeID, null));
					break;
				case "nA":
					receiver.add(new LinkedOTUOrOTUsEvent(EventContentType.NODE, nodeID, "Node " + nodeID, linkedOTUs[0]));
					break;
				case "nB":
					receiver.add(new LinkedOTUOrOTUsEvent(EventContentType.NODE, nodeID, "Node " + nodeID, linkedOTUs[1]));
					break;
				case "nC":
					receiver.add(new LinkedOTUOrOTUsEvent(EventContentType.NODE, nodeID, "Node " + nodeID, linkedOTUs[2]));
					break;
				default:
					throw new IllegalArgumentException("No node with the ID \"" + nodeID + "\" available.");
			}
			receiver.add(new ConcreteJPhyloIOEvent(EventContentType.NODE, EventTopologyType.END));
		}
		else {
			throw new IllegalArgumentException("No node with the ID \"" + nodeID + "\" available.");
		}
	}

	
	@Override
	public Iterator<String> getEdgeIDsFromNode(String nodeID)	throws IllegalArgumentException {
		if (nodeID.startsWith(nodeEdgeIDPrefix)) {
			switch (nodeID.substring(nodeEdgeIDPrefix.length())) {
				case "nRoot":
					return Arrays.asList(new String[]{nodeEdgeIDPrefix + "e1", nodeEdgeIDPrefix + "eC"}).iterator();
				case "n1":
					return Arrays.asList(new String[]{nodeEdgeIDPrefix + "eA", nodeEdgeIDPrefix + "eB"}).iterator();
				case "nA":
				case "nB":
				case "nC":
					return Collections.emptyIterator();
				default:  // fall through to exception below
			}
		}
		throw new IllegalArgumentException("No node with the ID \"" + nodeID + "\" available.");
	}

	
	@Override
	public void writeEdgeData(JPhyloIOEventReceiver receiver, String edgeID) throws IOException {
		if (edgeID.startsWith(nodeEdgeIDPrefix)) {
			switch (edgeID.substring(nodeEdgeIDPrefix.length())) {
				case "eRoot":
					receiver.add(new EdgeEvent(edgeID, "Root edge", null, nodeEdgeIDPrefix + "nRoot", 1.5));
					break;
				case "e1":
					receiver.add(new EdgeEvent(edgeID, "Internal edge", nodeEdgeIDPrefix + "nRoot", nodeEdgeIDPrefix + "n1", 1.0));
					break;
				case "eA":
					receiver.add(new EdgeEvent(edgeID, "Leaf edge A", nodeEdgeIDPrefix + "n1", nodeEdgeIDPrefix + "nA", 1.1));
					receiver.add(new MetaInformationEvent("annotation", null, "100", new Integer(100)));
					receiver.add(new ConcreteJPhyloIOEvent(EventContentType.META_INFORMATION, EventTopologyType.END));
					break;
				case "eB":
					receiver.add(new EdgeEvent(edgeID, "Leaf edge B", nodeEdgeIDPrefix + "n1", nodeEdgeIDPrefix + "nB", 0.9));
					break;
				case "eC":
					receiver.add(new EdgeEvent(edgeID, "Leaf edge C", nodeEdgeIDPrefix + "nRoot", nodeEdgeIDPrefix + "nC", 2.0));
					break;
				default:
					throw new IllegalArgumentException("No edge with the ID \"" + edgeID + "\" available.");
			}
			receiver.add(new ConcreteJPhyloIOEvent(EventContentType.EDGE, EventTopologyType.END));
		}
		else {
			throw new IllegalArgumentException("No edge with the ID \"" + edgeID + "\" available.");
		}
	}
}
