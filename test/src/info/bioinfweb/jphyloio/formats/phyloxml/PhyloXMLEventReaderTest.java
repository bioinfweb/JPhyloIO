/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.formats.phyloxml;


import static info.bioinfweb.commons.testing.XMLAssert.assertAttribute;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEdgeEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEndEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEventType;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLabeledIDEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLinkedLabeledIDEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLiteralMetaEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLiteralMetaStartEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertNodeEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertResourceMetaEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertSeparatedStringLiteralContentEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertXMLContentEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.awt.Color;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.events.StartElement;

import org.junit.Test;



public class PhyloXMLEventReaderTest implements PhyloXMLConstants {	
	@Test
	public void testReadingSingleTree() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/SingleTree.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, true, reader);				
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);				
				String node3 = assertNodeEvent(null, "C", false, null, true, reader);
				
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);				
				assertEdgeEvent(node4, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);				
				assertEdgeEvent(node5, node1, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingMultipleTrees() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/MultipleTrees.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, true, reader);				
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);				
				String node3 = assertNodeEvent(null, "C", false, null, true, reader);
				
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);				
				assertEdgeEvent(node4, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);				
				assertEdgeEvent(node5, node1, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 2", reader);
				
				node1 = assertNodeEvent(null, "y1", false, null, true, reader);				
				
				node2 = assertNodeEvent(null, "x1", false, null, true, reader);
				assertEdgeEvent(node2, node1, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				node3 = assertNodeEvent(null, "y2", false, null, true, reader);				
				
				node4 = assertNodeEvent(null, "x2", false, null, true, reader);
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				node5 = assertNodeEvent(null, "x", false, null, true, reader);
				assertEdgeEvent(node5, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingBranchLengths() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			MessageListApplicationLogger logger = new MessageListApplicationLogger();
			parameters.put(ReadWriteParameterMap.KEY_LOGGER, logger);
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/BranchLengths.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, true, reader);				
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);				
				String node3 = assertNodeEvent(null, "C", false, null, true, reader);
				
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);				
				assertEdgeEvent(node4, node2, 0.6, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);				
				assertEdgeEvent(node5, node1, 0.8, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node5, node4, 0.4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(null, node5, 0.6, reader);
				assertEndEvent(EventContentType.ROOT_EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
				
				assertEquals(1, logger.getMessageList().size());
				assertEquals(ApplicationLoggerMessageType.WARNING, logger.getMessageList().get(0).getType());
				assertEquals("Two different branch lengths of \"0.6\" and \"0.9\" are present for the same branch in the document.", logger.getMessageList().get(0).getMessage());
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingProperty() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/Property.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_APPLIES_TO), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "clade", null, "clade", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), 
						"1200", null, 1200, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "METRIC:m", null, "METRIC:m", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), 
						"1200", null, 1200, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.NODE, reader);				
				
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "METRIC:m", null, "METRIC:m", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DATE), 
						"2016-05-31", null, DatatypeConverter.parseDate("2016-05-31"), true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				
				String node3 = assertNodeEvent(null, "2", false, null, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING),
					"two-hundred", null, "two-hundred", true, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(node3, node1, reader);				
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node3, node2, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "METRIC:m", null, "METRIC:m", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"400.0", null, 400.0, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node4 = assertNodeEvent(null, "C", false, null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_ANNOTATION), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_QNAME), "ex:name", null, new QName("http://example.org", "name", "ex"), true, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_APPLIES_TO), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "node", null, "node", true, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "METRIC:m", null, "METRIC:m", true, reader);		
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), 
						new URI("http://www.phyloxml.org/documentation/version_1.10/phyloxml.xsd.html#h-676012345"), null, false, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null, false, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_APPLIES_TO), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "other", null, "other", true, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "METRIC:m", null, "METRIC:m", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), 
						"true", null, true, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				assertEndEvent(EventContentType.NODE, reader);				
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);				
				assertEdgeEvent(node5, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), 
						" 1 ", null, 1, true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY), null, null, false, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_APPLIES_TO), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "node", null, "node", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "METRIC:m", null, "METRIC:m", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"-0.545", null, -0.545, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);		
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingCladeRelationsTree() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/CladeRelation.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, true, reader);				
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);				
				String node3 = assertNodeEvent(null, "C", false, null, true, reader);				
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);
				
				assertEdgeEvent(node4, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);
				
				assertEdgeEvent(node5, node1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_SOURCE_NODE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "ID1", null, "ID1", true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_TARGET_NODE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "ID2", null, "ID2", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_LENGTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.5", null, 0.5, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_IDREF0), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "ID1", null, "ID1", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_IDREF1), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "ID2", null, "ID2", true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_DISTANCE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.5", null, 0.5, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "extraEdge", null, "extraEdge", true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "bootstrap", null, "bootstrap", true, reader);	//TODO why is this missing?
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"78.0", null, 78.0, true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_SOURCE_NODE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "ID2", null, "ID2", true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_TARGET_NODE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "ID1", null, "ID1", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_IDREF0), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "ID2", null, "ID2", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_IDREF1), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "ID1", null, "ID1", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "extraEdge", null, "extraEdge", true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingCladeRelationsNetwork() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, false);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/CladeRelation.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, true, reader);
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);
				String node3 = assertNodeEvent(null, "C", false, null, true, reader);
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);
				
				assertEdgeEvent(node4, node2, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node4, node3, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);			
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);				
				assertEdgeEvent(node5, node1, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node5, node4, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertEdgeEvent(node2, node3, 0.5, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "extraEdge", null, "extraEdge", true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "bootstrap", null, "bootstrap", true, reader);	//TODO why is this missing?
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"78.0", null, 78.0, true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node3, node2, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "extraEdge", null, "extraEdge", true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.NETWORK, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingVariousMetaEvents() { 
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/VariousMetaEventsFromPhyloXMLTags.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_DESCRIPTION), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"An example tree to test meta data event creation", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_DATE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DATE_TIME),
						"2016-06-02T09:00:00", null, DatatypeConverter.parseDateTime("2016-06-02T09:00:00"), true, reader);
				
				String node1 = assertNodeEvent(null, null, false, null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.phyloxml.org", "illegalValueTag")), LiteralContentSequenceType.XML, null, null,
						reader);				
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "illegalValueTag"), null, false, reader);
				assertXMLContentEvent("A", XMLStreamConstants.CHARACTERS, null, "A", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "illegalValueTag"), null, true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);
				
				String node3 = assertNodeEvent(null, "C", false, null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_RANK), new URIOrStringIdentifier(null, DATA_TYPE_RANK), "phylum", null, null, true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_ATTR_DESC), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "example", null, "example", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_ATTR_TYPE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "exampleURL", null, "exampleURL", true, reader); //TODO why are these missing?
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_VALUE), new URI("http://www.phyloxml.org/documentation/version_1.10/phyloxml.xsd.html#h-676012345"), 
						null, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.phyloxml.org", "color")), LiteralContentSequenceType.XML, null, null, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "color"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "red"), null, false, reader);
				assertXMLContentEvent("60", XMLStreamConstants.CHARACTERS, null, "60", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "red"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "green"), null, false, reader);
				assertXMLContentEvent("255", XMLStreamConstants.CHARACTERS, null, "255", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "green"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "blue"), null, false, reader);
				assertXMLContentEvent("155", XMLStreamConstants.CHARACTERS, null, "155", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "blue"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "color"), null, true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.NODE, reader);				
				
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);
				
				assertEdgeEvent(node4, node2, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_WIDTH), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), " 0.5 ", null, 
						0.5, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLOR), new URIOrStringIdentifier(null, DATA_TYPE_BRANCH_COLOR), null, null, 
						new Color(60, 255, 155), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);
				
				assertEdgeEvent(node5, node1, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_WIDTH), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.5", null, 
						0.5, true, reader);				
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(null, node5, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "bootstrap", null, "bootstrap", true, reader); //TODO why is this missing?
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"56", null, 56.0, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				assertEndEvent(EventContentType.ROOT_EDGE, reader);				
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingNodeLabels() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/NodeLabels.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "Name A", false, null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_SCIENTIFIC_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Sci Name A", null, "Sci Name A", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_COMMON_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Com Name A", null, "Com Name A", true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				String node2 = assertNodeEvent(null, "Com Name B", false, null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_COMMON_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Com Name B", null, "Com Name B", true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.NODE, reader);				
				
				String node3 = assertNodeEvent(null, "Seq Name C", false, null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Seq Name C", null, "Seq Name C", true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				String node4 = assertNodeEvent(null, "Sci Name 2", false, null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_SCIENTIFIC_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Sci Name 2", null, "Sci Name 2", true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Seq Name 2", null, "Seq Name 2", true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(node4, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "Name 1", true, null, true, reader);
				
				assertEdgeEvent(node5, node1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testOnlyCustomXML() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/OnlyCustomXML.xml"), parameters);
			
			try {				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org/align", "alignment", "align")), LiteralContentSequenceType.XML, 
						null, null, reader);

				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/align", "alignment", "align"), null, false, reader);				
				
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				assertXMLContentEvent("acgtcgcggcccgtggaagtcctctcct", XMLStreamConstants.CHARACTERS, null, "acgtcgcggcccgtggaagtcctctcct", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				assertXMLContentEvent("aggtcgcggcctgtggaagtcctctcct", XMLStreamConstants.CHARACTERS, null, "aggtcgcggcctgtggaagtcctctcct", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				assertXMLContentEvent("taaatcgc--cccgtgg-agtccc-cct", XMLStreamConstants.CHARACTERS, null, "taaatcgc--cccgtgg-agtccc-cct", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/align", "alignment", "align"), null, true, reader);
				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testEmptyPhylogeny() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/EmptyPhylogeny.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingCladeRelationsNetworkWithoutNodeIDs() throws Exception {
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, false);
		PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/CladeRelationNoNodeIDs.xml"), parameters);
		
		try {			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, true, reader);					
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);				
				String node3 = assertNodeEvent(null, "C", false, null, true, reader);
				
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);				
				assertEdgeEvent(node4, node2, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node4, node3, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);				
				assertEdgeEvent(node5, node1, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertEdgeEvent(node5, node4, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(null, node5, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_CROSSLINK), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertEndEvent(EventContentType.ROOT_EDGE, reader);
				
				fail("Exception not thrown");
			}
			catch (Exception e) {
				assertEquals(e.getMessage(), "A node ID was referenced by a clade relation element, that was not defined before.");
			}
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingCustomXML() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/CustomXML.xml"), parameters);
			
			try {
				StartElement element;
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, 
						null, null, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);
				
				element = assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();				
				assertAttribute(new QName("http://example.org", "attr", "ex"), "A", element);
				assertXMLContentEvent("example Text 1", XMLStreamConstants.CHARACTERS, null, "example Text 1", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "secondExampleTag", "ex")), LiteralContentSequenceType.XML, 
						null, null, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "secondExampleTag", "ex"), null, false, reader);	
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "secondExampleTag", "ex"), null, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node2 = assertNodeEvent(null, "B", false, null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, "ID1", true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, 
						null, null, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				element = assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				assertAttribute(new QName("http://example.org", "attr", "ex"), "C", element);
				assertXMLContentEvent("example Text 3", XMLStreamConstants.CHARACTERS, null, "example Text 3", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.NODE, reader);				
				
				String node3 = assertNodeEvent(null, "C", false, null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EVENTS), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EVENTS_DUPLICATIONS), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_NON_NEGATIVE_INTEGER), 
						"50", null, new BigInteger("50"), true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "illegalTag", "ex")), LiteralContentSequenceType.XML, 
						null, null, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "illegalTag", "ex"), null, false, reader);
				assertXMLContentEvent("70", XMLStreamConstants.CHARACTERS, null, "70", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "illegalTag", "ex"), null, true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);	
				assertEndEvent(EventContentType.NODE, reader);				
				
				String node4 = assertNodeEvent(null, "2", false, null, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_ID), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, null, true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, 
						null, null, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				element = assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				assertAttribute(new QName("http://example.org", "attr", "ex"), "B", element);
				assertXMLContentEvent("example Text 2", XMLStreamConstants.CHARACTERS, null, "example Text 2", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);				
				
				assertEdgeEvent(node4, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(node5, node1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);	
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, 
						null, null, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				element = assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				assertAttribute(new QName("http://example.org", "attr", "ex"), "D", element);
				assertXMLContentEvent("example Text 4", XMLStreamConstants.CHARACTERS, null, "example Text 4", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);		
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_HAS_CUSTOM_XML), null, null, false, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, 
						null, null, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				element = assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				assertAttribute(new QName("http://example.org", "attr", "ex"), "E", element);
				assertXMLContentEvent("example Text 5", XMLStreamConstants.CHARACTERS, null, "example Text 5", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingLongString() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/LongString.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, true, reader);
				
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);
				
				String node3 = assertNodeEvent(null, "C", false, null, false, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, "ID1", true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_MOL_SEQ), null, null, false, reader);	
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_MOL_SEQ_VALUE), LiteralContentSequenceType.SIMPLE, 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), null, reader);
				assertSeparatedStringLiteralContentEvent("data/PhyloXML/LongString_expected.txt", true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);
				
				assertEdgeEvent(node4, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);
				
				assertEdgeEvent(node5, node1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingBranchColor() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/BranchColor.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, "Tree 1", reader);
				
				String node1 = assertNodeEvent(null, "A", false, null, true, reader);				
				
				String node2 = assertNodeEvent(null, "B", false, null, true, reader);				
				
				String node3 = assertNodeEvent(null, "C", false, null, true, reader);				
				
				String node4 = assertNodeEvent(null, "2", false, null, true, reader);				
				
				assertEdgeEvent(node4, node2, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLOR), new URIOrStringIdentifier(null, DATA_TYPE_BRANCH_COLOR), null, null, 
						new Color(66, 255, 0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLOR), new URIOrStringIdentifier(null, DATA_TYPE_BRANCH_COLOR), null, null, 
						new Color(255, 0, 0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node5 = assertNodeEvent(null, "1", true, null, true, reader);
				
				assertEdgeEvent(node5, node1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node4, reader);
				assertEndEvent(EventContentType.EDGE, reader);			
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
}