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
package info.bioinfweb.jphyloio.formats.nexml;


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.*;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;

import java.io.File;

import org.junit.Test;



public class NeXMLEventReaderTest implements NeXMLConstants, ReadWriteConstants {
	@Test
	public void testOutputNeXML() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_TRANSLATION_STRATEGY, TokenTranslationStrategy.SYMBOL_TO_LABEL);
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MultipleElements.xml"), parameters);
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
					System.out.println(event.getType());
					
					if (event.getType().equals(new EventType(EventContentType.META_RESOURCE, EventTopologyType.START))) {
//					System.out.println("Predicate: " + event.asResourceMetadataEvent().getRel().getURI());
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
	public void readSimpleDocument() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/SimpleDocument.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", "DNA", "taxonlist", reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, "charset1", null, "alignment", reader);				
				assertCharacterSetIntervalEvent(0, 1, reader);
				assertCharacterSetIntervalEvent(2, 4, reader);				
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, "charset2", null, "alignment", reader);				
				assertCharacterSetIntervalEvent(2, 3, reader);
				assertCharacterSetIntervalEvent(4, 5, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("T", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				
				assertSingleTokenDefinitionEvent("N", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
				
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row1", "row1", "taxon1", reader);
				assertCharactersEvent("AACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row2", "species2", "taxon2", reader);
				assertCharactersEvent("ACGTT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row3", "row3", "taxon3", reader);
				assertCharactersEvent("ACCTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treegroup", "treegroup", "taxonlist", reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree", null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node1", "node1", "taxon1", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node2", "species2", "taxon2", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node3", "node3", "taxon3", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node4", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node5", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(null, "node4", 0.778, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node4", "node5", 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node4", "node3", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node5", "node1", 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node5", "node2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DISPLAY_TREE_ROOTED), null, "false", null, false, true, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, "network", null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node1", "node1", "taxon1", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node2", "species2", "taxon2", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node3", "node3", "taxon3", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node4", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node5", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("node4", "node5", 0.44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node4", "node3", 0.67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node5", "node1", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node5", "node2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node2", "node3", reader);
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
	public void readUnknownCharIDInCharSet() throws Exception {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/UnknownCharID_CharSet.xml"), new ReadWriteParameterMap());
		try {			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", "DNA", "taxonlist", reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, "charset1", null, "alignment", reader);				
				assertCharacterSetIntervalEvent(0, 1, reader);
				assertCharacterSetIntervalEvent(2, 4, reader);				
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, "charset2", null, "alignment", reader);				
				
				fail("Exception not thrown");
			}
			catch (JPhyloIOReaderException e) {
				assertEquals(e.getMessage(), "A character set referenced the ID \"unknownChar\" of a character that was not specified before.");
			}
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void readUnknownCharIDInCells() throws Exception {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/UnknownCharID_Cells.xml"), new ReadWriteParameterMap());
		try {			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", "DNA", "taxonlist", reader);			
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("T", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);				
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row1", "row1", "taxon1", reader);
				assertSingleTokenEvent("A", true, reader);
				
				fail("Exception not thrown");		
			}
			catch (JPhyloIOReaderException e) {
				assertEquals(e.getMessage(), "1 cell tag(s) referencing an undeclared column ID was/were found.");
			}
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void readUnknownStateID() throws Exception {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/UnknownStateID.xml"), new ReadWriteParameterMap());
		try {			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist", null, reader);
				
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", "DNA", "taxonlist", reader);			
				
				fail("Exception not thrown");
			}
			catch (JPhyloIOReaderException e) {
				assertEquals(e.getMessage(), "A single token definition referenced the ID \"unknownState\" of a state that was not specified before.");
			}
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void readUnknownTokenSetID() throws Exception {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/UnknownTokenSetID.xml"), new ReadWriteParameterMap());
		try {			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", "DNA", "taxonlist", reader);			
				
				fail("Exception not thrown");		
			}
			catch (JPhyloIOReaderException e) {
				assertEquals(e.getMessage(), "A character referenced the ID \"unknownStates\" of a token set that was not specified before.");
			}
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void readMissingElementID() throws Exception {
		NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MissingOTUsID.xml"), new ReadWriteParameterMap());
		try {			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				fail("Exception not thrown");		
			}
			catch (JPhyloIOReaderException e) {
				assertEquals(e.getMessage(), "The element \"otus\" must specify an ID.");
			}
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingDNACellsOrdered() throws Exception {
		testReadingDNACells("data/NeXML/DNACells.xml");
	}
	
	
	@Test
	public void testReadingDNACellsUnordered() throws Exception {
		testReadingDNACells("data/NeXML/DNACellsDifferentOrder.xml");
	}
	
	
	private void testReadingDNACells(String file) throws Exception {
		NeXMLEventReader reader = new NeXMLEventReader(new File(file), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			assertLabeledIDEvent(EventContentType.OTU_LIST, "taxa", null, reader);
			assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
			assertEndEvent(EventContentType.OTU, reader);
			assertLabeledIDEvent(EventContentType.OTU, "taxon2", null, reader);
			assertEndEvent(EventContentType.OTU, reader);
			assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", "DNA", "taxa", reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("T", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertCharacterSetIntervalEvent(0, 3, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row1", "row1", "taxon1", reader);
			assertSingleTokenEvent("A", true, reader);
			assertSingleTokenEvent("G", true, reader);
			assertSingleTokenEvent("A", true, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row2", "row2", "taxon2", reader);
			assertSingleTokenEvent("A", true, reader);
			assertSingleTokenEvent("G", true, reader);
			assertSingleTokenEvent("T", true, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEndEvent(EventContentType.ALIGNMENT, reader);
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
		}
		finally {
			reader.close();
		}		
	}
}