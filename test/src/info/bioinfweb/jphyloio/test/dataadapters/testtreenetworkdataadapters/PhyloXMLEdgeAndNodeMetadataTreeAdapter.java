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
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.implementations.store.StoreObjectListDataAdapter;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;



public class PhyloXMLEdgeAndNodeMetadataTreeAdapter extends EdgeAndNodeMetaDataTreeAdapter {
	private long idIndex;
	
	
	public PhyloXMLEdgeAndNodeMetadataTreeAdapter(String id, String label, String nodeEdgeIDPrefix, String[] linkedOTUs) {
		super(id, label, nodeEdgeIDPrefix, linkedOTUs);
		addAnnotations();
		idIndex = 1;
	}

	
	public PhyloXMLEdgeAndNodeMetadataTreeAdapter(String id, String label, String nodeEdgeIDPrefix) {
		super(id, label, nodeEdgeIDPrefix);
		addAnnotations();
		idIndex = 1;
	}

	
	public long getIDIndex() {
		long index = idIndex;
		idIndex++;
		return index;
	}
	

	@Override
	protected void addEdges(StoreObjectListDataAdapter<EdgeEvent> edges) {
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "eRoot", "Root edge", null, getNodeEdgeIDPrefix() + "nRoot", 1.5));
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "e1", "Internal edge", getNodeEdgeIDPrefix() + "nRoot", getNodeEdgeIDPrefix() + "n1", 1.0));
		
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "eA", "Leaf edge A", getNodeEdgeIDPrefix() + "n1", getNodeEdgeIDPrefix() + "nA", 1.1));
		List<JPhyloIOEvent> nestedEvents = edges.getObjectContent(getNodeEdgeIDPrefix() + "eA");
		nestedEvents.add(new LiteralMetadataEvent(getNodeEdgeIDPrefix() + "eAmeta1", null, 
				new URIOrStringIdentifier("splitString", new QName("http://example.org/", "somePredicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING),
				LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent("edge ", true));
		nestedEvents.add(new LiteralMetadataContentEvent("meta", false));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		nestedEvents.add(new LiteralMetadataEvent(getNodeEdgeIDPrefix() + "eAmeta2", null,
				new URIOrStringIdentifier("a1", new QName("http://example.org/", "somePredicate")),
				new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent(new Integer(100), "200"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "eB", "Leaf edge B", getNodeEdgeIDPrefix() + "n1", getNodeEdgeIDPrefix() + "nB", 0.9));
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "eC", "Leaf edge C", getNodeEdgeIDPrefix() + "nRoot", getNodeEdgeIDPrefix() + "nC", 2.0));
	}


	@Override
	protected void addNodes(StoreObjectListDataAdapter<NodeEvent> nodes) {
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "n1", "Node '_1", null, false));
		List<JPhyloIOEvent> nestedEvents = nodes.getObjectContent(getNodeEdgeIDPrefix() + "n1");
		
		nestedEvents.add(new LiteralMetadataEvent(getNodeEdgeIDPrefix() + "n1meta1", null,
				new URIOrStringIdentifier("a1", new QName("http://example.org/", "somePredicate")),
				new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent(new Integer(100), "100"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		nestedEvents.add(new LiteralMetadataEvent(getNodeEdgeIDPrefix() + "n1meta2", null,
				new URIOrStringIdentifier("a1", new QName("http://example.org/", "somePredicate")), LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent(new Integer(200), "200"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "nRoot", "Node " + getNodeEdgeIDPrefix() + "nRoot", null, true));
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "nA", "Node " + getNodeEdgeIDPrefix() + "nA", getLinkedOTUs() != null ? getLinkedOTUs()[0] : null, false));
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "nB", "Node " + getNodeEdgeIDPrefix() + "nB", getLinkedOTUs() != null ? getLinkedOTUs()[1] : null, false));
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "nC", "Node " + getNodeEdgeIDPrefix() + "nC", getLinkedOTUs() != null ? getLinkedOTUs()[2] : null, false));
	}
	
	
	protected void addAnnotations() {
		URI href = null;
		URI href2 = null;
		
		try {
			href = new URI("www.test.de");
			href2 = new URI("www.test2.de");
		} 
		catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		// Resource meta without children
		getAnnotations().add(new ResourceMetadataEvent(getNodeEdgeIDPrefix() + ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), href, null));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		// Resource meta with child element
		getAnnotations().add(new ResourceMetadataEvent(getNodeEdgeIDPrefix() + ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "relations")), href2, null));		
		getAnnotations().add(new LiteralMetadataEvent(getNodeEdgeIDPrefix() + ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		// Custom XML
		getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null));
		getAnnotations().add(new LiteralMetadataEvent(getNodeEdgeIDPrefix() + ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), LiteralContentSequenceType.XML));		
		getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		getAnnotations().add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
	}
}
