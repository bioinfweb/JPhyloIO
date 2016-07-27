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
package info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters;


import java.awt.Color;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;

import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter.StoreObjectListDataAdapter;
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
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;



public class PhyloXMLSpecificMetadataTreeAdapter extends PhyloXMLEdgeAndNodeMetadataTreeAdapter implements PhyloXMLConstants {

	
	public PhyloXMLSpecificMetadataTreeAdapter(String id, String label, String nodeEdgeIDPrefix, String[] linkedOTUs) {
		super(id, label, nodeEdgeIDPrefix, linkedOTUs);
	}
	

	public PhyloXMLSpecificMetadataTreeAdapter(String id, String label, String nodeEdgeIDPrefix) {
		super(id, label, nodeEdgeIDPrefix);
	}
	

	@Override
	protected void addEdges(StoreObjectListDataAdapter<EdgeEvent> edges) {
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "eRoot", "Root edge", null, getNodeEdgeIDPrefix() + "nRoot", 1.5));
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "e1", "Internal edge", getNodeEdgeIDPrefix() + "nRoot", getNodeEdgeIDPrefix() + "n1", 1.0));
		
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "eA", "Leaf edge A", getNodeEdgeIDPrefix() + "n1", getNodeEdgeIDPrefix() + "nA", 1.1));
		List<JPhyloIOEvent> nestedEvents = edges.getObjectContent(getNodeEdgeIDPrefix() + "eA");
		nestedEvents.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_WIDTH), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
				LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent(7.3, "7.3"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		nestedEvents.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_COLOR), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent(new Color(45, 210, 78), null));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "eB", "Leaf edge B", getNodeEdgeIDPrefix() + "n1", getNodeEdgeIDPrefix() + "nB", 0.9));
		edges.setObjectStartEvent(new EdgeEvent(getNodeEdgeIDPrefix() + "eC", "Leaf edge C", getNodeEdgeIDPrefix() + "nRoot", getNodeEdgeIDPrefix() + "nC", 2.0));
	}

	@Override
	protected void addNodes(StoreObjectListDataAdapter<NodeEvent> nodes) {
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "n1", "Node '_1", null, false));
		List<JPhyloIOEvent> nestedEvents = nodes.getObjectContent(getNodeEdgeIDPrefix() + "n1");
		
		nestedEvents.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null));

		nestedEvents.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_SCIENTIFIC_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent("Mus musculus", "Mus musculus"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		nestedEvents.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI), null, null));		

		nestedEvents.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_ATTR_DESC), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent("Some URI", "Some URI"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		try {
			nestedEvents.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
					new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_VALUE), new URI("http://www.some-uri.com"), null));
			nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		}
		catch (URISyntaxException e) {}

		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		nestedEvents.add(new LiteralMetadataEvent(getNodeEdgeIDPrefix() + ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "XMLPredicate")), LiteralContentSequenceType.XML));		
		nestedEvents.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		nestedEvents.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		nestedEvents.add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null));
		
		nestedEvents.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_ID_REF), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent("someID", "someID"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		nestedEvents.add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		nestedEvents.add(new LiteralMetadataContentEvent("myValue", "myValue"));
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "nRoot", "Node " + getNodeEdgeIDPrefix() + "nRoot", null, true));
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "nA", "Node " + getNodeEdgeIDPrefix() + "nA", getLinkedOTUs() != null ? getLinkedOTUs()[0] : null, false));
		
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "nB", "Node " + getNodeEdgeIDPrefix() + "nB", getLinkedOTUs() != null ? getLinkedOTUs()[1] : null, false));
		nestedEvents = nodes.getObjectContent(getNodeEdgeIDPrefix() + "nB");
		nestedEvents.add(new LiteralMetadataEvent(getNodeEdgeIDPrefix() + ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "XMLPredicate")), LiteralContentSequenceType.XML));		
		nestedEvents.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createStartElement("pre", "http://test.com/", "customTest"), false));
		nestedEvents.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createCharacters("test "), true));
		nestedEvents.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createCharacters("characters"), false));
		nestedEvents.add(new LiteralMetadataContentEvent(XMLEventFactory.newInstance().createEndElement("pre", "http://test.com/", "customTest"), false));		
		nestedEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		nodes.setObjectStartEvent(new NodeEvent(getNodeEdgeIDPrefix() + "nC", "Node " + getNodeEdgeIDPrefix() + "nC", getLinkedOTUs() != null ? getLinkedOTUs()[2] : null, false));
	}

	@Override
	protected void addAnnotations() {
		getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_ID), null, null));

		getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_ID_ATTR_PROVIDER), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		getAnnotations().add(new LiteralMetadataContentEvent("NCBI", "NCBI"));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING),
				LiteralContentSequenceType.SIMPLE));		
		getAnnotations().add(new LiteralMetadataContentEvent("phylogeny1", "phylogeny1"));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_DESCRIPTION), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		getAnnotations().add(new LiteralMetadataContentEvent("example tree", "example tree"));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		
		getAnnotations().add(new ResourceMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null));

		getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		getAnnotations().add(new LiteralMetadataContentEvent("bootstrap", "bootstrap"));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE),
				LiteralContentSequenceType.SIMPLE));
		getAnnotations().add(new LiteralMetadataContentEvent(0.6, "0.6"));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));

		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_RESOURCE));
		
		getAnnotations().add(new LiteralMetadataEvent(ReadWriteConstants.DEFAULT_META_ID_PREFIX + getIDIndex(), null, 
				new URIOrStringIdentifier(null, new QName("http://meta.net/", "predicate")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
				LiteralContentSequenceType.SIMPLE));
		getAnnotations().add(new LiteralMetadataContentEvent("myValue", "myValue"));
		getAnnotations().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	}
}
