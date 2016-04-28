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


import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;



public class NoAnnotationsTree extends EmptyAnnotatedDataAdapter<LabeledIDEvent> implements TreeNetworkDataAdapter {
	private String id = null;
	private String label = null;
	private String nodeEdgeIDPrefix = "";
	private String[] linkedOTUs;
	
	
	public NoAnnotationsTree(String id, String label, String nodeEdgeIDPrefix, String[] linkedOTUs) {
		super();
		this.id = id;
		this.label = label;
		if (linkedOTUs.length != 3) {
			throw new IllegalArgumentException("Invalid number of linked OTUs (" + linkedOTUs.length + ").");
		}
		else {
			this.linkedOTUs = linkedOTUs;
		}
		this.nodeEdgeIDPrefix = nodeEdgeIDPrefix;
	}


	public NoAnnotationsTree(String id, String label, String nodeEdgeIDPrefix) {
		this(id, label, nodeEdgeIDPrefix, new String[]{null, null, null});
	}


	public String getNodeEdgeIDPrefix() {
		return nodeEdgeIDPrefix;
	}


	@Override
	public LabeledIDEvent getStartEvent() {
		return new LabeledIDEvent(EventContentType.TREE, id, label);
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
	public LinkedLabeledIDEvent getNodeStartEvent(String nodeID) {
		if (nodeID.startsWith(nodeEdgeIDPrefix)) {
			switch (nodeID.substring(nodeEdgeIDPrefix.length())) {
				case "n1":
					return new LinkedLabeledIDEvent(EventContentType.NODE, nodeID, "Node '_1", null);
				case "nRoot":  
					return new LinkedLabeledIDEvent(EventContentType.NODE, nodeID, "Node " + nodeID, null);
				case "nA":
					return new LinkedLabeledIDEvent(EventContentType.NODE, nodeID, "Node " + nodeID, linkedOTUs[0]);
				case "nB":
					return new LinkedLabeledIDEvent(EventContentType.NODE, nodeID, "Node " + nodeID, linkedOTUs[1]);
				case "nC":
					return new LinkedLabeledIDEvent(EventContentType.NODE, nodeID, "Node " + nodeID, linkedOTUs[2]);
				default:
					throw new IllegalArgumentException("No node with the ID \"" + nodeID + "\" available.");
			}
		}
		else {
			throw new IllegalArgumentException("No node with the ID \"" + nodeID + "\" available.");
		}
	}


	@Override
	public void writeNodeContentData(JPhyloIOEventReceiver receiver, String nodeID) throws IOException {}

	
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
	public EdgeEvent getEdgeStartEvent(String edgeID) {
		if (edgeID.startsWith(nodeEdgeIDPrefix)) {
			switch (edgeID.substring(nodeEdgeIDPrefix.length())) {
				case "eRoot":
					return new EdgeEvent(edgeID, "Root edge", null, nodeEdgeIDPrefix + "nRoot", 1.5);
				case "e1":
					return new EdgeEvent(edgeID, "Internal edge", nodeEdgeIDPrefix + "nRoot", nodeEdgeIDPrefix + "n1", 1.0);
				case "eA":
					return new EdgeEvent(edgeID, "Leaf edge A", nodeEdgeIDPrefix + "n1", nodeEdgeIDPrefix + "nA", 1.1);
				case "eB":
					return new EdgeEvent(edgeID, "Leaf edge B", nodeEdgeIDPrefix + "n1", nodeEdgeIDPrefix + "nB", 0.9);
				case "eC":
					return new EdgeEvent(edgeID, "Leaf edge C", nodeEdgeIDPrefix + "nRoot", nodeEdgeIDPrefix + "nC", 2.0);
				default:
					throw new IllegalArgumentException("No edge with the ID \"" + edgeID + "\" available.");
			}
		}
		else {
			throw new IllegalArgumentException("No edge with the ID \"" + edgeID + "\" available.");
		}
	}


	@Override
	public void writeEdgeContentData(JPhyloIOEventReceiver receiver, String edgeID) throws IOException {}
}
