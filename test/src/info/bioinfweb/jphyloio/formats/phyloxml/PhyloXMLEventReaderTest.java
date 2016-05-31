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


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEdgeEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEndEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEventType;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLinkedLabeledIDEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLiteralMetaEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertResourceMetaEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;

import java.io.File;

import org.junit.Test;



public class PhyloXMLEventReaderTest implements PhyloXMLConstants {
	@Test
	public void testOutputPhyloXML() {
		try {
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/NodeLabels.xml"), new ReadWriteParameterMap());
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType());
					
					if (event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))) {
//						System.out.println(event.asLiteralMetadataEvent().getPredicate().getURI().getLocalPart());
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
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/SingleTree.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree1", "Tree 1", null, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n4", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n4", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n6", "n8", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", "C", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n6", "n10", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n6", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n6", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n2", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n2", reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertEndEvent(EventContentType.TREE, reader);
				
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
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/MultipleTrees.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree1", "Tree 1", null, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n4", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n4", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n6", "n8", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", "C", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n6", "n10", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n6", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n6", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n2", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n2", reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree13", "Tree 2", null, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "false", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n18", "y1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n16", "n18", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n16", "x1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n14", "n16", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n22", "y2", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n20", "n22", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n20", "x2", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n14", "n20", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n14", "x", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n14", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
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
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/BranchLengths.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree1", "Tree 1", null, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n4", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n4", 0.8, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "B", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n6", "n8", 0.6, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n10", "C", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n6", "n10", Double.NaN, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n6", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n6", 0.4, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n2", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n2", 0.6, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertEndEvent(EventContentType.TREE, reader);
				
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
	public void testReadingCladeRelations() {
		try {
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/CladeRelation.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree1", "Tree 1", null, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n4", "A", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n4", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n8", "B", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID_VALUE), new URIOrStringIdentifier(null, DATA_TYPE_TOKEN), 
						"ID1", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n6", "n8", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n12", "C", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ID_VALUE), new URIOrStringIdentifier(null, DATA_TYPE_TOKEN), 
						"ID2", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n6", "n12", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n6", "2", null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n6", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n2", "1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("n8", "n12", reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CLADE_REL_ATTR_TYPE), null, 
						"extraEdge", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
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
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/NodeLabels.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, "tree1", "Tree 1", null, reader);		
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n4", "Name A", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_SCIENTIFIC_NAME), new URIOrStringIdentifier(null, DATA_TYPE_TOKEN), 
						"Sci Name A", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_COMMON_NAME), new URIOrStringIdentifier(null, DATA_TYPE_TOKEN), 
						"Com Name A", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n4", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n15", "Com Name B", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_COMMON_NAME), new URIOrStringIdentifier(null, DATA_TYPE_TOKEN), 
						"Com Name B", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n9", "n15", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n19", "Seq Name C", null, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, DATA_TYPE_TOKEN), 
						"Seq Name C", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n9", "n19", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n9", "Sci Name 2", null, reader);				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TAXONOMY_SCIENTIFIC_NAME), new URIOrStringIdentifier(null, DATA_TYPE_TOKEN), 
						"Sci Name 2", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_NAME), new URIOrStringIdentifier(null, DATA_TYPE_TOKEN), 
						"Seq Name 2", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("n2", "n9", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "n2", "Name 1", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "n2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
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