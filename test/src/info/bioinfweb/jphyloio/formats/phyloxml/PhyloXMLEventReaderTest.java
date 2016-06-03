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


import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.test.JPhyloIOTestTools;
import info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.GregorianCalendar;

import javax.xml.bind.DatatypeConverter;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class PhyloXMLEventReaderTest implements PhyloXMLConstants {
	@Test
	public void testOutputPhyloXML() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/LongString.xml"), parameters);
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
					System.out.println(event.getType());
					
					if (event instanceof LabeledIDEvent) {
						System.out.println(event.asLabeledIDEvent().getID() + " " + event.asLabeledIDEvent().getLabel());
					}
					
					if (event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))) {
//						System.out.println("Predicate: " + event.asLiteralMetadataEvent().getPredicate().getURI());
					}
					else if (event.getType().equals(new EventType(EventContentType.META_LITERAL_CONTENT, EventTopologyType.SOLE))) {
						if (event.asLiteralMetadataContentEvent().hasXMLEventValue()) {
//							System.out.println(event.asLiteralMetadataContentEvent().getXMLEvent());
						}
						else {
//							System.out.println(event.asLiteralMetadataContentEvent().getStringValue());
							if (event.asLiteralMetadataContentEvent().getObjectValue() != null) {
//								System.out.println(event.asLiteralMetadataContentEvent().getObjectValue() + " " + event.asLiteralMetadataContentEvent().getObjectValue().getClass());
							}
						}
					}
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
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", null, reader);		
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
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", null, reader);		
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
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree14", "Tree 2", null, reader);		
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
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", null, reader);		
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
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", null, reader);		
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
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", null, reader);		
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
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_DISTANCE), null, null, null, null, true, reader);
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
				
				assertLinkedLabeledIDEvent(EventContentType.NETWORK, "network2", "Tree 1", null, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", "B", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID1", null, null, true, reader);	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n10", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n15", "C", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_TOKEN), 
						"ID2", null, null, true, reader);	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n15", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n8", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("n10", "n15", 0.5, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "true", null, true, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), null, "extraEdge", null, null, true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_ATTR_TYPE), null, "bootstrap", null, null, true, reader);	
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CONFIDENCE_VALUE), new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), 
						"78.0", null, 78.0, true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);		
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("n15", "n10", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "true", null, true, true, reader);
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
	
	
	//TODO test various meta events from phyloXML tags
	
	
	@Test
	public void testReadingNodeLabels() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/NodeLabels.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", null, reader);		
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
			XMLEventFactory eventFactory = XMLEventFactory.newInstance();
			
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/OnlyCustomXML.xml"), parameters);
			
			try {
				StartElement element;
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://example.org/align", "alignment", "align")), LiteralContentSequenceType.XML, reader);
				
				element = eventFactory.createStartElement(new QName("http://example.org/align", "alignment", "align"), 
						null, Arrays.asList(new Namespace []{eventFactory.createNamespace("align", "http://example.org/align")}).iterator());
				assertLiteralMetaContentEvent(null, element.toString(), null, element, false, reader);
				
				element = eventFactory.createStartElement(new QName("http://example.org/align", "seq", "align"), 
						Arrays.asList(new Attribute []{eventFactory.createAttribute(new QName("http://example.org/align", "name", "align"), "A")}).iterator(), null);
				assertLiteralMetaContentEvent(null, element.toString(), null, element, false, reader);
				assertLiteralMetaContentEvent(null, null, null, eventFactory.createCharacters("acgtcgcggcccgtggaagtcctctcct"), false, reader);
				assertLiteralMetaContentEvent(null, null, null, eventFactory.createEndElement("align", "http://example.org/align", "seq"), false, reader);
				
				element = eventFactory.createStartElement(new QName("http://example.org/align", "seq", "align"), 
						Arrays.asList(new Attribute []{eventFactory.createAttribute(new QName("http://example.org/align", "name", "align"), "B")}).iterator(), null);
				assertLiteralMetaContentEvent(null, element.toString(), null, element, false, reader);
				assertLiteralMetaContentEvent(null, null, null, eventFactory.createCharacters("aggtcgcggcctgtggaagtcctctcct"), false, reader);
				assertLiteralMetaContentEvent(null, null, null, eventFactory.createEndElement("align", "http://example.org/align", "seq"), false, reader);
				
				element = eventFactory.createStartElement(new QName("http://example.org/align", "seq", "align"), 
						Arrays.asList(new Attribute []{eventFactory.createAttribute(new QName("http://example.org/align", "name", "align"), "C")}).iterator(), null);
				assertLiteralMetaContentEvent(null, element.toString(), null, element, false, reader);
				assertLiteralMetaContentEvent(null, null, null, eventFactory.createCharacters("taaatcgc--cccgtgg-agtccc-cct"), false, reader);
				assertLiteralMetaContentEvent(null, null, null, eventFactory.createEndElement("align", "http://example.org/align", "seq"), false, reader);
				
				assertLiteralMetaContentEvent(null, null, null, eventFactory.createEndElement("align", "http://example.org/align", "alignment"), false, reader);
				
				assertEndEvent(EventContentType.META_LITERAL, reader);
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
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", null, reader);		
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
				
				assertLinkedLabeledIDEvent(EventContentType.NETWORK, "network2", "Tree 1", null, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n5", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n5", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n10", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n13", "C", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n8", "n13", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n3", "n8", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n3", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n3", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_IS_CROSSLINK), null, "false", null, false, true, reader);
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
	public void testReadingLongString() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE, true);
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/SingleTree.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treesOrNetworks0", null, null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree2", "Tree 1", null, reader);		
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
						"ID1", null, "ID1", true, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_MOL_SEQ), null, null, false, reader);	
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_MOL_SEQ_VALUE), LiteralContentSequenceType.SIMPLE, reader);
//				assertLiteralMetaContentEvent(null, expectedStringValue, null, expectedObjectValue, true, reader);
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
}