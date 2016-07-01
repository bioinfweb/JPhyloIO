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


import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyObjectListDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.newick.NewickConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import javax.xml.namespace.QName;



public class EdgeAndNodeMetaDataTreeAdapter extends EmptyAnnotatedDataAdapter<LabeledIDEvent> implements TreeNetworkDataAdapter {
	private String id = null;
	private String label = null;
	private String nodeEdgeIDPrefix = "";
	private String[] linkedOTUs;
	
	
	public EdgeAndNodeMetaDataTreeAdapter(String id, String label, String nodeEdgeIDPrefix, String[] linkedOTUs) {
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


	public EdgeAndNodeMetaDataTreeAdapter(String id, String label, String nodeEdgeIDPrefix) {
		this(id, label, nodeEdgeIDPrefix, new String[]{null, null, null});
	}


	public String getNodeEdgeIDPrefix() {
		return nodeEdgeIDPrefix;
	}


	@Override
	public LabeledIDEvent getStartEvent(ReadWriteParameterMap parameters) {
		return new LabeledIDEvent(EventContentType.TREE, id, label);
	}


	@Override
	public boolean isTree(ReadWriteParameterMap parameters) {
		return true;
	}

	
	@Override
	public boolean considerRooted(ReadWriteParameterMap parameters) {
		return true;
	}


	@Override
	public Iterator<String> getRootEdgeIDs(ReadWriteParameterMap parameters) {
		return Arrays.asList(new String[]{nodeEdgeIDPrefix + "eRoot"}).iterator();
	}

	
	@Override
	public NodeEvent getNodeStartEvent(ReadWriteParameterMap parameters, String nodeID) {
		if (nodeID.startsWith(nodeEdgeIDPrefix)) {
			switch (nodeID.substring(nodeEdgeIDPrefix.length())) {
				case "n1":
					return new NodeEvent(nodeID, "Node '_1", null, false);
				case "nRoot":  
					return new NodeEvent(nodeID, "Node " + nodeID, null, true);
				case "nA":
					return new NodeEvent(nodeID, "Node " + nodeID, linkedOTUs[0], false);
				case "nB":
					return new NodeEvent(nodeID, "Node " + nodeID, linkedOTUs[1], false);
				case "nC":
					return new NodeEvent(nodeID, "Node " + nodeID, linkedOTUs[2], false);
				default:
					throw new IllegalArgumentException("No node with the ID \"" + nodeID + "\" available.");
			}
		}
		else {
			throw new IllegalArgumentException("No node with the ID \"" + nodeID + "\" available.");
		}
	}


	@Override
	public void writeNodeContentData(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, String nodeID) throws IOException {
		if (nodeID.startsWith(nodeEdgeIDPrefix)) {
			if(nodeID.substring(nodeEdgeIDPrefix.length()).equals("n1")) {

				receiver.add(new LiteralMetadataEvent("n1meta1", null, 
						new URIOrStringIdentifier("a1", new QName("http://example.org/", "somePredicate")),
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), LiteralContentSequenceType.SIMPLE));
				receiver.add(new LiteralMetadataContentEvent(new Integer(100), "100"));
				receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				
				receiver.add(new LiteralMetadataEvent("n1meta2", null, new URIOrStringIdentifier("a2", new QName("http://example.org/", "somePredicate")), 
						LiteralContentSequenceType.SIMPLE));
				receiver.add(new LiteralMetadataContentEvent("ab 'c", "ab 'c"));
				receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
			}
		}
	}

	
	@Override
	public Iterator<String> getEdgeIDsFromNode(ReadWriteParameterMap parameters, String nodeID)	throws IllegalArgumentException {
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
	public EdgeEvent getEdgeStartEvent(ReadWriteParameterMap parameters, String edgeID) {
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
	public void writeEdgeContentData(ReadWriteParameterMap parameters, JPhyloIOEventReceiver receiver, String edgeID) throws IOException {
		if (edgeID.startsWith(nodeEdgeIDPrefix)) {
			if (edgeID.substring(nodeEdgeIDPrefix.length()).equals("eA")) {
				receiver.add(new LiteralMetadataEvent("eAmeta1", null, 
						new URIOrStringIdentifier("splitString", new QName("http://example.org/", "somePredicate")), LiteralContentSequenceType.SIMPLE));
				receiver.add(new LiteralMetadataContentEvent("ABC", true));
				receiver.add(new LiteralMetadataContentEvent("DEF", false));
				receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
				
				receiver.add(new LiteralMetadataEvent("eAmeta2", null, 
						new URIOrStringIdentifier("array", new QName("http://example.org/", "somePredicate")), 
						new URIOrStringIdentifier(null, NewickConstants.DATA_TYPE_NEWICK_ARRAY), LiteralContentSequenceType.SIMPLE));
				receiver.add(new LiteralMetadataContentEvent(Arrays.asList(new Integer(100), "abc"), null));
				receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	@Override
	public ObjectListDataAdapter<LinkedLabeledIDEvent> getNodeEdgeSets(ReadWriteParameterMap parameters) {
		return EmptyObjectListDataAdapter.SHARED_EMPTY_OBJECT_LIST_ADAPTER;
	}
}