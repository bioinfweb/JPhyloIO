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
import java.math.BigInteger;
import java.net.URI;

import javax.xml.namespace.QName;

import org.junit.Test;



public class NeXMLEventReaderTest implements NeXMLConstants, ReadWriteConstants {
	@Test
	public void testOutputNeXML() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_TRANSLATION_STRATEGY, TokenTranslationStrategy.SYMBOL_TO_LABEL);
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MultipleCharactersTags.xml"), parameters);
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
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node6", "node6", "taxon1", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node7", "species2", "taxon2", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node8", "node8", "taxon3", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node9", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node10", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("node9", "node10", 0.44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node9", "node8", 0.67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node10", "node6", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node10", "node7", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node7", "node8", reader);
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
	public void readMultipleElements() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MultipleElements.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist2", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon4", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon5", "species5", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon6", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment1", "DNA", "taxonlist1", reader);				
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);				
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);				
				assertCharacterSetIntervalEvent(0, 2, reader);
				assertCharacterSetIntervalEvent(3, 4, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("T", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);				
				assertSingleTokenDefinitionEvent("N", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);	
				assertCharacterSetIntervalEvent(2, 3, reader);
				assertCharacterSetIntervalEvent(4, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row1", "row1", "taxon1", reader);
				assertCharactersEvent("AATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row2", "species2", "taxon2", reader);
				assertCharactersEvent("ACGCT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row3", "row3", "taxon3", reader);
				assertCharactersEvent("ACTCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment2", "DNA", "taxonlist2", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("T", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 3, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row4", "species5", "taxon5", reader);
				assertSingleTokenEvent("A", true, reader);
				assertSingleTokenEvent("G", true, reader);
				assertSingleTokenEvent("A", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row5", "row5", "taxon4", reader);
				assertSingleTokenEvent("A", true, reader);
				assertSingleTokenEvent("G", true, reader);
				assertSingleTokenEvent("T", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment3", "DNA", "taxonlist1", reader);				
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 3, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row6", "row6", "taxon1", reader);
				assertCharactersEvent("ACC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row7", "species2", "taxon2", reader);
				assertCharactersEvent("CAC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row8", "row8", "taxon3", reader);
				assertCharactersEvent("AAC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treegroup1", "treegroup1", "taxonlist1", reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree1", null, reader);
				
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
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TRUE_ROOT), null, "true", null, true, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node4", "node3", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node5", "node1", 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node5", "node2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, true, true, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, "network1", null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node6", "node6", "taxon1", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node7", "species2", "taxon2", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node8", "node8", "taxon3", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node9", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node10", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("node9", "node10", 0.44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node9", "node8", 0.67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node10", "node6", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node10", "node7", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node7", "node8", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.NETWORK, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree2", null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node11", "node11", "taxon1", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node12", "species2", "taxon2", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node13", "node13", "taxon3", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node14", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node15", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("node14", "node15", 17, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node14", "node13", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node15", "node11", 89, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node15", "node12", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, true, true, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, "network2", null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node16", "node16", "taxon1", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node17", "species2", "taxon2", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node18", "node18", "taxon3", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node19", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node20", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("node19", "node20", 44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node19", "node18", 67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node20", "node16", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node20", "node17", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node17", "node18", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.NETWORK, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, "treegroup2", "treegroup2", "taxonlist2", reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree3", null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node21", "node21", "taxon4", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node22", "species5", "taxon5", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node23", "node23", "taxon6", reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node24", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node25", null, null, reader);				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent("node24", "node25", 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node24", "node23", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node25", "node21", 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node25", "node22", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(null, "node24", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TRUE_ROOT), null, "true", null, true, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DISPLAY_TREE_ROOTED), null, "true", null, true, true, reader);
				
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
			
			assertSingleTokenEvent("A", false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://bioinfweb.info/xmlns/example", "hasLiteralMeta", "foo")), 
					new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema#", "string", "xsd")), "another text", null, null, 
					true,	reader);
			assertEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, reader);
			
			assertSingleTokenEvent("G", false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://bioinfweb.info/xmlns/example", "hasLiteralMeta", "foo")), 
					new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema#", "integer", "xsd")), "18", null,
					new BigInteger("18"), true,	reader);
			assertEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, reader);
			
			assertSingleTokenEvent("A", false, reader);
			assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://bioinfweb.info/xmlns/example", "linksResource", "foo")), 
					new URI("http://example.org/someURI"), null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://bioinfweb.info/xmlns/example", "hasLiteralMeta", "foo")), 
					new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema#", "string", "xsd")), "some text", null, null, true, 
					reader);
			assertEndEvent(EventContentType.META_RESOURCE, reader);
			assertEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, reader);
			
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