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


import static org.junit.Assert.fail;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;

import java.io.File;

import org.junit.Test;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class PhyloXMLEventReaderTest {
	@Test
	public void testOutputPhyloXML() {
		try {
//			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/PhyloXMLDocument.xml"));
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/MultipleTrees.xml"), new ReadWriteParameterMap());
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType().getContentType() + " " + event.getType().getTopologyType());
					if (event.getType().equals(new EventType(EventContentType.META_INFORMATION, EventTopologyType.START))) {
//						System.out.println("Key: " + event.asMetaInformationEvent().getKey());
					}
					else if (event.getType().equals(new EventType(EventContentType.TREE, EventTopologyType.START))) {
//						System.out.println("ID: " + event.asLabeledIDEvent().getID() + " Label: " + event.asLabeledIDEvent().getLabel());
					}
					else if (event.getType().equals(new EventType(EventContentType.NODE, EventTopologyType.START))) {
//						System.out.println("ID: " + event.asLabeledIDEvent().getID() + " Label: " + event.asLabeledIDEvent().getLabel());
					}
					else if (event.getType().equals(new EventType(EventContentType.EDGE, EventTopologyType.START))) {
						System.out.println("Source: " + event.asEdgeEvent().getSourceID() + " Target: " + event.asEdgeEvent().getTargetID());
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
	public void testReadingIDsAndLabels() {
		try {
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/IDsAndLabels.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
				assertMetaEvent("phyloXML.phyloxml.phylogeny.rooted", "true", true, true, reader);
				assertMetaEvent("phyloXML.phylogeny.name", "Test Tree", true, true, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "internal1", "internalNode1", reader);
				assertMetaEvent("phyloXML.clade.node_id", "internal1", true, true, reader);
				assertMetaEvent("phyloXML.clade.name", "internalNode1", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("root", "internal1", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "N1", "AA bb", reader);
				assertMetaEvent("phyloXML.clade.node_id", "N1", true, true, reader);
				assertMetaEvent("phyloXML.clade.taxonomy", null, false, true, reader);
				assertMetaEvent("phyloXML.taxonomy.id", "taxonomy ID", true, true, reader);
				assertMetaEvent("phyloXML.taxonomy.scientific_name", "AA bb", true, true, reader);
				assertMetaEvent("phyloXML.taxonomy.common_name", "common name", true, true, reader);
				assertEndEvent(EventContentType.META_INFORMATION, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("internal1", "N1", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "N2", "internal2", reader);
				assertMetaEvent("phyloXML.clade.taxonomy", null, false, true, reader);
				assertMetaEvent("phyloXML.taxonomy.id", "N2", true, true, reader);
				assertMetaEvent("phyloXML.taxonomy.common_name", "internal2", true, true, reader);
				assertEndEvent(EventContentType.META_INFORMATION, reader);
				assertMetaEvent("phyloXML.clade.sequence", null, false, true, reader);
				assertMetaEvent("phyloXML.sequence.name", "sequence name", true, true, reader);
				assertEndEvent(EventContentType.META_INFORMATION, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("internal1", "N2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "n4", "BB cc", reader);
				assertMetaEvent("phyloXML.clade.sequence", null, false, true, reader);
				assertMetaEvent("phyloXML.sequence.name", "BB cc", true, true, reader);
				assertEndEvent(EventContentType.META_INFORMATION, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("N2", "n4", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "n6", null, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("N2", "n6", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.DOCUMENT, reader);
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
				
				assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
				assertMetaEvent("phyloXML.phyloxml.phylogeny.rooted", "true", true, true, reader);
				assertMetaEvent("phyloXML.phylogeny.name", "Tree 1", true, true, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "1", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "1", true, true, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("root", "1", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "A", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "A", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("1", "A", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "2", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "2", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("1", "2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "B", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "B", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("2", "B", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "C", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "C", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("2", "C", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				
				assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
				assertMetaEvent("phyloXML.phyloxml.phylogeny.rooted", "false", true, true, reader);
				assertMetaEvent("phyloXML.phylogeny.name", "Tree 2", true, true, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "1", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "1", true, true, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "2", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "2", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("1", "2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "A", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "A", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("2", "A", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "B", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "B", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("2", "B", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLabeledIDEvent(EventContentType.NODE, "C", null, reader);
				assertMetaEvent("phyloXML.clade.node_id", "C", true, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("1", "C", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.DOCUMENT, reader);
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
