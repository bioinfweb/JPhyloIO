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
package info.bioinfweb.jphyloio.formats.phyloxml;


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.*;
import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.commons.testing.XMLTestTools;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
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
	public void testOutputPhyloXML() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/VariousMetaEventsFromPhyloXMLTags.xml"), parameters);
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType());
				}
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
	public void testReadingSingleTree() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/SingleTree.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n9", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n9", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n11", "C", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n11", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n7", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n7", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n9", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n9", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n11", "C", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n11", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n7", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n7", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree14", "Tree 2", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "false", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n19", "y1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n17", "n19", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n17", "x1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n15", "n17", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n23", "y2", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n21", "n23", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n21", "x2", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n15", "n21", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n15", "x", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n15", reader);
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", 0.8, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n9", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n9", 0.6, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n11", "C", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n11", Double.NaN, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n7", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n7", 0.4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", 0.6, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "A", null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_APPLIES_TO), null, 
						"clade", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), 
						"1200", null, 1200, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), null, 
						"METRIC:m", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), 
						"1200", null, 1200, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n5", "n8", reader);				
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n16", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), null, 
						"METRIC:m", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DATE), 
						"2016-05-31", null, DatatypeConverter.parseDate("2016-05-31"), true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEdgeEvent("n5", "n16", reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), null, 
						"METRIC:m", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"400.0", null, 400.0, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "2", null, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING),
					"two-hundred", null, "two-hundred", true, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n24", "C", null, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_ANNOTATION), null, null, false, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_QNAME), 
						"ex:name", null, new QName("http://example.org", "name", "ex"), true, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), null, 
						"METRIC:m", null, null, true, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_APPLIES_TO), null, 
						"node", null, null, true, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), 
						new URI("http://www.phyloxml.org/documentation/version_1.10/phyloxml.xsd.html#h-676012345"), null, false, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), null, 
						"METRIC:m", null, null, true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_APPLIES_TO), null, 
						"other", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), 
						"true", null, true, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n24", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), 
						" 1 ", null, 1, true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_UNIT), null, 
						"METRIC:m", null, null, true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PROPERTY_ATTR_APPLIES_TO), null, 
						"node", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "predicate", "ex")), new URIOrStringIdentifier(null, new QName("http://example.org", "newType", "ex")), 
						"-0.545", null, "-0.545", true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n9", "B", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, null, true, reader);	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n9", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n13", "C", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID2", null, null, true, reader);	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n13", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n7", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n7", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_EDGE_SOURCE_NODE), null, "ID1", null, null, true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_EDGE_TARGET_NODE), null, "ID2", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_EDGE_LENGTH), null, null, null, 0.5, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_DISTANCE), null, "0.5", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_IDREF0), null, "ID1", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), null, "extraEdge", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_IDREF1), null, "ID2", null, null, true, reader);	
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), null, "bootstrap", null, null, true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"78.0", null, 78.0, true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_EDGE_SOURCE_NODE), null, "ID2", null, null, true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_EDGE_TARGET_NODE), null, "ID1", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_IDREF0), null, "ID2", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), null, "extraEdge", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_IDREF1), null, "ID1", null, null, true, reader);	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, "network2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", "B", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, null, true, reader);	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n10", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n15", "C", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID2", null, null, true, reader);	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n15", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n8", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("n10", "n15", 0.5, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, true, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), null, "extraEdge", null, null, true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), null, "bootstrap", null, null, true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"78.0", null, 78.0, true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);		
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("n15", "n10", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, true, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), null, "extraEdge", null, null, true, reader);
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
	public void testReadingVariousMetaEvents() { //TODO
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/VariousMetaEventsFromPhyloXMLTags.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_DESCRIPTION), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"An example tree to test meta data event creation", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PHYLOGENY_DATE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DATE_TIME),
						"2016-06-02T09:00:00", null, DatatypeConverter.parseDateTime("2016-06-02T09:00:00"), true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", null, null, reader);				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.phyloxml.org", "illegalValueTag")), LiteralContentSequenceType.XML, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "illegalValueTag"), null, false, reader);
				assertXMLContentEvent(null, "A", null, XMLStreamConstants.CHARACTERS, null, "A", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "illegalValueTag"), null, true, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n5", "n10", reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_WIDTH), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.5", null, 
						0.5, true, reader);				
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n16", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n14", "n16", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_WIDTH), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), " 0.5 ", null, 
						0.5, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLOR), new URIOrStringIdentifier(null, DATA_TYPE_BRANCH_COLOR), null, null, 
						new Color(60, 255, 155), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n20", "C", null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_RANK), new URIOrStringIdentifier(null, DATA_TYPE_RANK), "phylum", null, null, true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_ATTR_TYPE), null, "exampleURL", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_ATTR_DESC), null, "example", null, null, true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_URI_VALUE), new URI("http://www.phyloxml.org/documentation/version_1.10/phyloxml.xsd.html#h-676012345"), 
						null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.phyloxml.org", "color")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "color"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "red"), null, false, reader);
				assertXMLContentEvent(null, "60", null, XMLStreamConstants.CHARACTERS, null, "60", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "red"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "green"), null, false, reader);
				assertXMLContentEvent(null, "255", null, XMLStreamConstants.CHARACTERS, null, "255", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "green"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://www.phyloxml.org", "blue"), null, false, reader);
				assertXMLContentEvent(null, "155", null, XMLStreamConstants.CHARACTERS, null, "155", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "blue"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://www.phyloxml.org", "color"), null, true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n14", "n20", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n14", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n5", "n14", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n5", reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), null, "bootstrap", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"56", null, 56.0, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
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
	public void testReadingNodeLabels() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/NodeLabels.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "Name A", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_SCIENTIFIC_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Sci Name A", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_COMMON_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Com Name A", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n16", "Com Name B", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_COMMON_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Com Name B", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n10", "n16", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n20", "Seq Name C", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Seq Name C", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n10", "n20", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", "Sci Name 2", null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_SCIENTIFIC_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Sci Name 2", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"Seq Name 2", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n10", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "Name 1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
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
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org/align", "alignment", "align")), LiteralContentSequenceType.XML, reader);

				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/align", "alignment", "align"), null, false, reader);				
				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				assertXMLContentEvent(null, "acgtcgcggcccgtggaagtcctctcct", null, XMLStreamConstants.CHARACTERS, null, "acgtcgcggcccgtggaagtcctctcct", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				assertXMLContentEvent(null, "aggtcgcggcctgtggaagtcctctcct", null, XMLStreamConstants.CHARACTERS, null, "aggtcgcggcctgtggaagtcctctcct", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				assertXMLContentEvent(null, "taaatcgc--cccgtgg-agtccc-cct", null, XMLStreamConstants.CHARACTERS, null, "taaatcgc--cccgtgg-agtccc-cct", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/align", "seq", "align"), null, false, reader);
				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org/align", "alignment", "align"), null, true, reader);
				
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, "network2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n10", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n13", "C", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n13", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n8", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, null, null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				
				element = assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				XMLTestTools.assertAttribute(new QName("http://example.org", "attr", "ex"), "A", element);
				assertXMLContentEvent(null, "example Text 1", null, XMLStreamConstants.CHARACTERS, null, "example Text 1", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n14", "B", null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, null, true, reader);				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				element = assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				XMLTestTools.assertAttribute(new QName("http://example.org", "attr", "ex"), "C", element);
				assertXMLContentEvent(null, "example Text 3", null, XMLStreamConstants.CHARACTERS, null, "example Text 3", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n14", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n19", "C", null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EVENTS), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EVENTS_DUPLICATIONS), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_NON_NEGATIVE_INTEGER), 
						"50", null, new BigInteger("50"), true, reader);				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "illegalTag", "ex")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "illegalTag", "ex"), null, false, reader);
				assertXMLContentEvent(null, "70", null, XMLStreamConstants.CHARACTERS, null, "70", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "illegalTag", "ex"), null, true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n19", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "2", null, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_ID), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				element = assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				XMLTestTools.assertAttribute(new QName("http://example.org", "attr", "ex"), "B", element);
				assertXMLContentEvent(null, "example Text 2", null, XMLStreamConstants.CHARACTERS, null, "example Text 2", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);				
				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n8", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				element = assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				XMLTestTools.assertAttribute(new QName("http://example.org", "attr", "ex"), "D", element);
				assertXMLContentEvent(null, "example Text 4", null, XMLStreamConstants.CHARACTERS, null, "example Text 4", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org", "exampleTag", "ex")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, false, reader);				
				element = assertXMLContentEvent(null, null, null, XMLStreamConstants.START_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader).asStartElement();
				XMLTestTools.assertAttribute(new QName("http://example.org", "attr", "ex"), "E", element);
				assertXMLContentEvent(null, "example Text 5", null, XMLStreamConstants.CHARACTERS, null, "example Text 5", false, reader);
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "subTag", "ex"), null, false, reader);				
				assertXMLContentEvent(null, null, null, XMLStreamConstants.END_ELEMENT, new QName("http://example.org", "exampleTag", "ex"), null, true, reader);				
				
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n9", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n9", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n11", "C", null, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, null, true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_MOL_SEQ), null, null, false, reader);	
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_MOL_SEQ_VALUE), LiteralContentSequenceType.SIMPLE, reader);
				assertSeparatedStringLiteralContentEvent(new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), "data/PhyloXML/LongString_expected.txt", true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n11", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n7", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n7", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
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
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n9", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n9", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLOR), new URIOrStringIdentifier(null, DATA_TYPE_BRANCH_COLOR), null, null, 
						new Color(66, 255, 0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n12", "C", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n7", "n12", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLOR), new URIOrStringIdentifier(null, DATA_TYPE_BRANCH_COLOR), null, null, 
						new Color(255, 0, 0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n7", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n7", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
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