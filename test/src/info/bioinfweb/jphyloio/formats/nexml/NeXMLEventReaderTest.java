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


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertCharacterDefinitionEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertCharacterSetIntervalEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertCharactersEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEdgeEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEndEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEventType;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLabeledIDEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLinkedLabeledIDEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLiteralMetaEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLiteralMetaStartEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertPartEndEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertResourceMetaEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertSetElementEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertSingleTokenDefinitionEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertSingleTokenEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertSplitCharactersEventLongTokens;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertTokenSetDefinitionEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertXMLContentEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;

import org.junit.Test;



public class NeXMLEventReaderTest implements NeXMLConstants, ReadWriteConstants {
	@Test
	public void testOutputNeXML() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_TRANSLATION_STRATEGY, TokenTranslationStrategy.SYMBOL_TO_LABEL);
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/nexml_treebase_example.xml"), parameters);
//			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/SimpleDocumentWithMetadata.xml"), parameters);
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType());
					if (event.getType().equals(new EventType(EventContentType.META_LITERAL_CONTENT, EventTopologyType.SOLE))) {
//						System.out.println(event.asLiteralMetadataContentEvent().getStringValue() + " " + event.asLiteralMetadataContentEvent().getObjectValue());
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
		readDocument(new File("data/NeXML/SimpleDocument.xml"));
	}
	
	
	@Test
	public void readDocumentWithUnknownTags() {
		readDocument(new File("data/NeXML/UnknownTag.xml"));
	}
	
	
	public void readDocument(File file) {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(file, new ReadWriteParameterMap());
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
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", ALIGNMENT_TYPE_DNA, "taxonlist", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
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
	public void readSimpleDocumentWithMetadata() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/SimpleDocumentWithMetadata.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "hasCustomXML", "ex")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, "characters", "customXML", XMLStreamConstants.CHARACTERS, null, "characters", false, reader);
				assertXMLContentEvent(null, null, "customXML", XMLStreamConstants.START_ELEMENT, new QName("http://www.example.net/", "customTag", "ex"), null, false, reader);
				assertXMLContentEvent(null, "some more ", "customXML", XMLStreamConstants.CHARACTERS, null, "some more ", false, reader);
				assertXMLContentEvent(null, null, "customXML", XMLStreamConstants.START_ELEMENT, new QName("http://www.example.net/", "nestedTag", "ex"), null, false, reader);
				assertXMLContentEvent(null, null, "customXML", XMLStreamConstants.END_ELEMENT, new QName("http://www.example.net/", "nestedTag", "ex"), null, false, reader);
				assertXMLContentEvent(null, "characters", "customXML", XMLStreamConstants.CHARACTERS, null, "characters", false, reader);
				assertXMLContentEvent(null, null, "customXML", XMLStreamConstants.END_ELEMENT, new QName("http://www.example.net/", "customTag", "ex"), null, true, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "someString1", null, null, true, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", "forty-seven", 47, true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "someAlternativeString", "someAlternativeString", 
						"someAlternativeString", true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "someString2", null, null, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), null, null, false, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "someString", "someString", "someString", true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), new URI("http://www.test.org/test1"), 
						null, true, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist", null, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", "47", 47, true, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", ALIGNMENT_TYPE_DNA, "taxonlist", reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FORMAT), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertEndEvent(EventContentType.CHARACTER_DEFINITION, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, "charset1", null, "alignment", reader);				
				assertCharacterSetIntervalEvent(0, 1, reader);
				assertCharacterSetIntervalEvent(2, 4, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, "charset2", null, "alignment", reader);				
				assertCharacterSetIntervalEvent(2, 3, reader);
				assertCharacterSetIntervalEvent(4, 5, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("T", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				
				assertSingleTokenDefinitionEvent("N", CharacterSymbolMeaning.CHARACTER_STATE, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "N", null, null, true, reader);
				assertEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION, reader);
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
				
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MATRIX), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "row1", "row1", "taxon1", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
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
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, "tree", null, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE, "node1", "node1", "taxon1", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
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
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node4", "node5", 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node4", "node3", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node5", "node1", 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent("node5", "node2", reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DISPLAY_TREE_ROOTED), null, "false", null, false, true, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, "network", null, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				
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
	public void readCustomXMLWithNeXMLElements() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/CustomNeXML.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist", null, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "hasCustomNeXML", "ex")), LiteralContentSequenceType.XML, reader);
				assertXMLContentEvent(null, null, "customNeXML", XMLStreamConstants.START_ELEMENT, new QName("http://www.nexml.org/2009", "otus"), null, false, reader);
				assertXMLContentEvent(null, null, "customNeXML", XMLStreamConstants.START_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, null, "customNeXML", XMLStreamConstants.END_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, null, "customNeXML", XMLStreamConstants.START_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, null, "customNeXML", XMLStreamConstants.END_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, null, "customNeXML", XMLStreamConstants.START_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, null, "customNeXML", XMLStreamConstants.END_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, null, "customNeXML", XMLStreamConstants.END_ELEMENT, new QName("http://www.nexml.org/2009", "otus"), null, true, reader);
				
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
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
	public void readSets() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/Sets.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxonlist", null, reader);
				
				assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.OTU_SET, "otuSet", null, "taxonlist", reader);
				assertSetElementEvent("taxon1", EventContentType.OTU, reader);
				assertSetElementEvent("taxon3", EventContentType.OTU, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.OTU_SET, true, reader);
				
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", ALIGNMENT_TYPE_DNA, "taxonlist", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet", null, "alignment", reader);				
				assertCharacterSetIntervalEvent(0, 1, reader);
				assertCharacterSetIntervalEvent(2, 4, reader);				
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE_SET, "rowSet", null, "alignment", reader);
				assertSetElementEvent("row1", EventContentType.SEQUENCE, reader);
				assertSetElementEvent("row3", EventContentType.SEQUENCE, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE_SET, true, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.NODE_EDGE_SET, "nodeEdgeSet1", null, "tree", reader);
				assertSetElementEvent("node1", EventContentType.NODE, reader);
				assertSetElementEvent("node4", EventContentType.NODE, reader);
				assertSetElementEvent("rootedge", EventContentType.EDGE, reader);
				assertSetElementEvent("edge2", EventContentType.EDGE, reader);
				assertSetElementEvent("edge3", EventContentType.EDGE, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.NODE_EDGE_SET, true, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.NODE_EDGE_SET, "nodeEdgeSet2", null, "network", reader);
				assertSetElementEvent("node7", EventContentType.NODE, reader);
				assertSetElementEvent("node10", EventContentType.NODE, reader);
				assertSetElementEvent("edge7", EventContentType.EDGE, reader);
				assertSetElementEvent("edge8", EventContentType.EDGE, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.NODE_EDGE_SET, true, reader);
				
				assertEndEvent(EventContentType.NETWORK, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_SET, "treeNetworkSet", null, "treegroup", reader);
				assertSetElementEvent("tree", EventContentType.TREE, reader);
				assertSetElementEvent("network", EventContentType.NETWORK, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.TREE_NETWORK_SET, true, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment1", ALIGNMENT_TYPE_DNA, "taxonlist1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment2", ALIGNMENT_TYPE_DNA, "taxonlist2", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment3", ALIGNMENT_TYPE_DNA, "taxonlist1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				
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
	public void readMultipleCharactersTags() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MultipleCharactersTags.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxa1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t2", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t4", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t5", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "m1", ALIGNMENT_TYPE_RESTRICTION, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("0", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 4, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "RestrictionSiteRow1", "RestrictionSiteRow1", "t1", reader);
				assertCharactersEvent("0101", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "RestrictionSiteRow2", "RestrictionSiteRow2", "t2", reader);
				assertCharactersEvent("0101", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "RestrictionSiteRow3", "RestrictionSiteRow3", "t3", reader);
				assertCharactersEvent("0101", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "m3", ALIGNMENT_TYPE_CONTINUOUS, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, "this is character 1", 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, null, reader);
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "ContinuousCellsRow1", "ContinuousCellsRow1", "t1", reader);
				assertSingleTokenEvent("-1.545414144070023", true, reader);
				assertSingleTokenEvent("-2.3905621575431044", true, reader);
				assertSingleTokenEvent("-2.9610221833467265", true, reader);
				assertSingleTokenEvent("0.7868662069161243", true, reader);
				assertSingleTokenEvent("0.22968509237534918", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "ContinuousCellsRow2", "ContinuousCellsRow2", "t2", reader);
				assertSingleTokenEvent("-1.6259836379710066", true, reader);
				assertSingleTokenEvent("3.649352410850134", true, reader);
				assertSingleTokenEvent("1.778885099660406", true, reader);
				assertSingleTokenEvent("-1.2580877968480846", true, reader);
				assertSingleTokenEvent("0.22335354995610862", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);		
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "ContinuousCellsRow3", "ContinuousCellsRow3", "t3", reader);
				assertSingleTokenEvent("-1.5798979984134964", true, reader);
				assertSingleTokenEvent("2.9548251411133157", true, reader);
				assertSingleTokenEvent("1.522005675256233", true, reader);
				assertSingleTokenEvent("-0.8642016921755289", true, reader);
				assertSingleTokenEvent("-0.938129801832388", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);		
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "ContinuousCellsRow4", "ContinuousCellsRow4", "t4", reader);
				assertSingleTokenEvent("2.7436692306788086", true, reader);
				assertSingleTokenEvent("-0.7151148143399818", true, reader);
				assertSingleTokenEvent("4.592207937774776", true, reader);
				assertSingleTokenEvent("-0.6898841440534845", true, reader);
				assertSingleTokenEvent("0.5769509574453064", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);		
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "ContinuousCellsRow5", "ContinuousCellsRow5", "t5", reader);
				assertSingleTokenEvent("3.1060827493657683", true, reader);
				assertSingleTokenEvent("-1.0453787389160105", true, reader);
				assertSingleTokenEvent("2.67416332763427", true, reader);
				assertSingleTokenEvent("-1.4045634106692808", true, reader);
				assertSingleTokenEvent("0.019890469925520196", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);		
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "characters3", ALIGNMENT_TYPE_DNA, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				assertCharacterDefinitionEvent(null, null, 5, true, reader);
				assertCharacterDefinitionEvent(null, null, 6, true, reader);
				assertCharacterDefinitionEvent(null, null, 7, true, reader);
				assertCharacterDefinitionEvent(null, null, 8, true, reader);
				assertCharacterDefinitionEvent(null, null, 9, true, reader);
				assertCharacterDefinitionEvent(null, null, 10, true, reader);
				assertCharacterDefinitionEvent(null, null, 11, true, reader);
				assertCharacterDefinitionEvent(null, null, 12, true, reader);
				assertCharacterDefinitionEvent(null, null, 13, true, reader);
				assertCharacterDefinitionEvent(null, null, 14, true, reader);
				assertCharacterDefinitionEvent(null, null, 15, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("T", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("K", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("M", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("R", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("S", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("W", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("Y", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("B", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("D", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("H", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("V", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("N", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("X", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
				assertCharacterSetIntervalEvent(0, 16, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "DNASequence1", "DNASequence1", "t1", reader);
				assertCharactersEvent("ACGCTCGCATCGCATC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "DNASequence2", "DNASequence2", "t2", reader);
				assertCharactersEvent("ACGCTCGCATCGCATC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "DNASequence3", "DNASequence3", "t3", reader);
				assertCharactersEvent("ACGCTCGCATCGCATC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "rnaseqs4", ALIGNMENT_TYPE_RNA, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				assertCharacterDefinitionEvent(null, null, 5, true, reader);
				assertCharacterDefinitionEvent(null, null, 6, true, reader);
				assertCharacterDefinitionEvent(null, null, 7, true, reader);
				assertCharacterDefinitionEvent(null, null, 8, true, reader);
				assertCharacterDefinitionEvent(null, null, 9, true, reader);
				assertCharacterDefinitionEvent(null, null, 10, true, reader);
				assertCharacterDefinitionEvent(null, null, 11, true, reader);
				assertCharacterDefinitionEvent(null, null, 12, true, reader);
				assertCharacterDefinitionEvent(null, null, 13, true, reader);
				assertCharacterDefinitionEvent(null, null, 14, true, reader);
				assertCharacterDefinitionEvent(null, null, 15, true, reader);
				assertCharacterDefinitionEvent(null, null, 16, true, reader);
				assertCharacterDefinitionEvent(null, null, 17, true, reader);
				assertCharacterDefinitionEvent(null, null, 18, true, reader);
				assertCharacterDefinitionEvent(null, null, 19, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.RNA, "RNA", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("U", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("K", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("M", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("R", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("S", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("W", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("Y", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("B", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("D", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("H", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("V", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("N", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("X", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "rnarow1", "rnarow1", "t1", reader);
				assertCharactersEvent("ACGCUCGCAUCGCAUC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "rnarow2", "rnarow2", "t2", reader);
				assertCharactersEvent("ACGCUCGCAUCGCAUC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "rnarow3", "rnarow3", "t3", reader);
				assertCharactersEvent("ACGCUCGCAUCGCAUC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "contchars5", ALIGNMENT_TYPE_CONTINUOUS, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, null, reader);
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw1", "controw1", "t1", reader);
				assertCharactersEvent(new String[]{"-1.545414144070023", "-2.3905621575431044", "-2.9610221833467265", "0.7868662069161243", "0.22968509237534918"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw2", "controw2", "t2", reader);
				assertCharactersEvent(new String[]{"-1.6259836379710066", "3.649352410850134", "1.778885099660406", "-1.2580877968480846", "0.22335354995610862"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw3", "controw3", "t3", reader);
				assertCharactersEvent(new String[]{"-1.5798979984134964", "2.9548251411133157", "1.522005675256233", "-0.8642016921755289", "-0.938129801832388"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw4", "controw4", "t4", reader);
				assertCharactersEvent(new String[]{"2.7436692306788086", "-0.7151148143399818", "4.592207937774776", "-0.6898841440534845", "0.5769509574453064"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw5", "controw5", "t5", reader);
				assertCharactersEvent(new String[]{"3.1060827493657683", "-1.0453787389160105", "2.67416332763427", "-1.4045634106692808", "0.019890469925520196"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
						
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
	public void readStandardDataNeverTranslate() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_TRANSLATION_STRATEGY, TokenTranslationStrategy.NEVER);
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/StandardData.xml"), parameters);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxa1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t2", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t4", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t5", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "m1", ALIGNMENT_TYPE_STANDARD, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("2", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("3", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("4", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("5", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 2, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow1", "StandardCategoricalStateCellsRow1", "t1", reader);
				assertSingleTokenEvent("1", true, reader);
				assertSingleTokenEvent("2", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow2", "StandardCategoricalStateCellsRow2", "t2", reader);
				assertSingleTokenEvent("2", true, reader);
				assertSingleTokenEvent("2", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow3", "StandardCategoricalStateCellsRow3", "t3", reader);
				assertSingleTokenEvent("3", true, reader);
				assertSingleTokenEvent("4", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow4", "StandardCategoricalStateCellsRow4", "t4", reader);
				assertSingleTokenEvent("2", true, reader);
				assertSingleTokenEvent("3", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow5", "StandardCategoricalStateCellsRow5", "t5", reader);
				assertSingleTokenEvent("4", true, reader);
				assertSingleTokenEvent("1", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "m2", ALIGNMENT_TYPE_STANDARD, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("2", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("3", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("4", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("5", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 2, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr1", "standardr1", "t1", reader);
				assertCharactersEvent("12", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr2", "standardr2", "t2", reader);
				assertCharactersEvent("22", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr3", "standardr3", "t3", reader);
				assertCharactersEvent("34", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr4", "standardr4", "t4", reader);
				assertCharactersEvent("23", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr5", "standardr5", "t5", reader);
				assertCharactersEvent("41", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
					
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
	public void readStandardDataTranslateSymbolToLabel() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_TRANSLATION_STRATEGY, TokenTranslationStrategy.SYMBOL_TO_LABEL);
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/StandardData.xml"), parameters);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxa1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t2", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t4", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t5", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "m1", ALIGNMENT_TYPE_STANDARD, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("2", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("3", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("4", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("5", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 2, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow1", "StandardCategoricalStateCellsRow1", "t1", reader);
				assertSingleTokenEvent("1", true, reader);
				assertSingleTokenEvent("2", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow2", "StandardCategoricalStateCellsRow2", "t2", reader);
				assertSingleTokenEvent("2", true, reader);
				assertSingleTokenEvent("2", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow3", "StandardCategoricalStateCellsRow3", "t3", reader);
				assertSingleTokenEvent("3", true, reader);
				assertSingleTokenEvent("4", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow4", "StandardCategoricalStateCellsRow4", "t4", reader);
				assertSingleTokenEvent("2", true, reader);
				assertSingleTokenEvent("3", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "StandardCategoricalStateCellsRow5", "StandardCategoricalStateCellsRow5", "t5", reader);
				assertSingleTokenEvent("4", true, reader);
				assertSingleTokenEvent("1", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "m2", ALIGNMENT_TYPE_STANDARD, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("2", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("3", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("4", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("5", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 2, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr1", "standardr1", "t1", reader);
				assertCharactersEvent(new String[]{"standardstates1", "blue"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr2", "standardr2", "t2", reader);
				assertCharactersEvent(new String[]{"blue", "blue"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr3", "standardr3", "t3", reader);
				assertCharactersEvent(new String[]{"standardstates3", "green"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr4", "standardr4", "t4", reader);
				assertCharactersEvent(new String[]{"blue", "standardstates3"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "standardr5", "standardr5", "t5", reader);
				assertCharactersEvent(new String[]{"green", "standardstates1"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
					
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
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", ALIGNMENT_TYPE_DNA, "taxonlist", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", ALIGNMENT_TYPE_DNA, "taxonlist", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", ALIGNMENT_TYPE_DNA, "taxonlist", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", ALIGNMENT_TYPE_DNA, "taxonlist", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
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
			
			assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", ALIGNMENT_TYPE_DNA, "taxa", reader);
			
			assertCharacterDefinitionEvent(null, null, 0, true, reader);
			assertCharacterDefinitionEvent(null, null, 1, true, reader);
			assertCharacterDefinitionEvent(null, null, 2, true, reader);
			
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
					new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "another text", "another text", "another text", 
					true,	reader);
			assertEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, reader);
			
			assertSingleTokenEvent("G", false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://bioinfweb.info/xmlns/example", "hasLiteralMeta", "foo")), 
					new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "integer", "xsd")), "18", "18",
					new BigInteger("18"), true,	reader);
			assertEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, reader);
			
			assertSingleTokenEvent("A", false, reader);
			assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://bioinfweb.info/xmlns/example", "linksResource", "foo")), 
					new URI("http://example.org/someURI"), null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://bioinfweb.info/xmlns/example", "hasLiteralMeta", "foo")), 
					new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "some text", "some text", "some text", true, 
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
	
	
	@Test
	public void readLongSequences() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_NEXML_TOKEN_TRANSLATION_STRATEGY, TokenTranslationStrategy.NEVER);
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/largeSequences.xml"), parameters); //file is not valid NeXML (missing column definitions)
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "taxa1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t1", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t2", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t3", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t4", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "t5", null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "m1", ALIGNMENT_TYPE_CONTINUOUS, "taxa1", reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, null, reader);
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw1", "controw1", "t1", reader);
				assertSplitCharactersEventLongTokens("data/NeXML/longContinuousSequence.txt", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw2", "controw2", "t2", reader);
				assertCharactersEvent(new String[]{"-1.6259836379710066", "3.649352410850134", "1.778885099660406", "-1.2580877968480846", "0.22335354995610862"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw3", "controw3", "t3", reader);
				assertCharactersEvent(new String[]{"-1.5798979984134964", "2.9548251411133157", "1.522005675256233", "-0.8642016921755289", "-0.938129801832388"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw4", "controw4", "t4", reader);
				assertCharactersEvent(new String[]{"2.7436692306788086", "-0.7151148143399818", "4.592207937774776", "-0.6898841440534845", "0.5769509574453064"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "controw5", "controw5", "t5", reader);
				assertCharactersEvent(new String[]{"3.1060827493657683", "-1.0453787389160105", "2.67416332763427", "-1.4045634106692808", "0.019890469925520196"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
					
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