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
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreTreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.newick.NewickConstants;

import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;



public class EdgeAndNodeMetaDataTreeAdapter extends StoreTreeNetworkDataAdapter implements TreeNetworkDataAdapter {
	private String id = null;
	private String label = null;
	private String nodeEdgeIDPrefix = "";
	private String[] linkedOTUs;
	
	
	public EdgeAndNodeMetaDataTreeAdapter(String id, String label, String nodeEdgeIDPrefix, String[] linkedOTUs) {
		super(new LabeledIDEvent(EventContentType.TREE, id, label), true, null);
		
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
	}
	
	
	protected void addEdges(StoreObjectListDataAdapter<EdgeEvent> edges) {
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "eRoot", "Root edge", null, nodeEdgeIDPrefix + "nRoot", 1.5));
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "e1", "Internal edge", nodeEdgeIDPrefix + "nRoot", nodeEdgeIDPrefix + "n1", 1.0));
		
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "eA", "Leaf edge A", nodeEdgeIDPrefix + "n1", nodeEdgeIDPrefix + "nA", 1.1));
		List<JPhyloIOEvent> nestedEvents = edges.getObjectContent(nodeEdgeIDPrefix + "eA");
		nestedEvents.add(new LiteralMetadataEvent(nodeEdgeIDPrefix + "eAmeta1", null,  //TODO Prefix was not added here in previous implementation. => Adjust test cases. 
				new URIOrStringIdentifier("splitString", new QName("http://example.org/", "somePredicate")), LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent("ABC", true));
		nestedEvents.add(new LiteralMetadataContentEvent("DEF", false));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		
		nestedEvents.add(new LiteralMetadataEvent(nodeEdgeIDPrefix + "eAmeta2", null,  //TODO Prefix was not added here in previous implementation. => Adjust test cases. 
				new URIOrStringIdentifier("array", new QName("http://example.org/", "somePredicate")), 
				new URIOrStringIdentifier(null, NewickConstants.DATA_TYPE_NEWICK_ARRAY), LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent(Arrays.asList(new Integer(100), "abc"), null));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "eB", "Leaf edge B", nodeEdgeIDPrefix + "n1", nodeEdgeIDPrefix + "nB", 0.9));
		edges.setObjectStartEvent(new EdgeEvent(nodeEdgeIDPrefix + "eC", "Leaf edge C", nodeEdgeIDPrefix + "nRoot", nodeEdgeIDPrefix + "nC", 2.0));
	}


	protected void addNodes(StoreObjectListDataAdapter<NodeEvent> nodes) {
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "n1", "Node '_1", null, false));
		List<JPhyloIOEvent> nestedEvents = nodes.getObjectContent(nodeEdgeIDPrefix + "n1");
		nestedEvents.add(new LiteralMetadataEvent(nodeEdgeIDPrefix + "n1meta1", null,   //TODO Prefix was not added here in previous implementation. => Adjust test cases.
				new URIOrStringIdentifier("a1", new QName("http://example.org/", "somePredicate")),
				new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent(new Integer(100), "100"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		nestedEvents.add(new LiteralMetadataEvent(nodeEdgeIDPrefix + "n1meta2", null, new URIOrStringIdentifier("a2", new QName("http://example.org/", "somePredicate")),  //TODO Prefix was not added here in previous implementation. => Adjust test cases. 
				LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent("ab 'c", "ab 'c"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "nRoot", "Node " + nodeEdgeIDPrefix + "nRoot", null, true));
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "nA", "Node " + nodeEdgeIDPrefix + "nA", linkedOTUs != null ? linkedOTUs[0] : null, false));
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "nB", "Node " + nodeEdgeIDPrefix + "nB", linkedOTUs != null ? linkedOTUs[1] : null, false));
		nodes.setObjectStartEvent(new NodeEvent(nodeEdgeIDPrefix + "nC", "Node " + nodeEdgeIDPrefix + "nC", linkedOTUs != null ? linkedOTUs[2] : null, false));
	}
	
	
	public EdgeAndNodeMetaDataTreeAdapter(String id, String label, String nodeEdgeIDPrefix) {
		this(id, label, nodeEdgeIDPrefix, new String[]{null, null, null});
	}


	public String getNodeEdgeIDPrefix() {
		return nodeEdgeIDPrefix;
	}
	

	protected String[] getLinkedOTUs() {
		return linkedOTUs;
	}


	@Override
	public LabeledIDEvent getStartEvent(ReadWriteParameterMap parameters) {
		return new LabeledIDEvent(EventContentType.TREE, id, label);
	}


	@Override
	public boolean isTree(ReadWriteParameterMap parameters) {
		return true;
	}
}