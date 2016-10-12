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
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;

import java.io.File;
import java.math.BigInteger;
import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;

import org.junit.Test;



public class NeXMLEventReaderTest implements NeXMLConstants, ReadWriteConstants {
	
	
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				String alignment = assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", null, taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);
				assertCharacterSetIntervalEvent(0, 1, reader);
				assertCharacterSetIntervalEvent(2, 4, reader);				
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);				
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("AACTG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("ACGTT", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("ACCTG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, "trees", taxonList, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, null, reader);
				
				String node1 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node2 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node3 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node4 = assertNodeEvent(null, null, false, null, true, reader);
				String node5 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(null, node4, 0.778, reader);
				assertEndEvent(EventContentType.ROOT_EDGE, reader);
				
				assertEdgeEvent(node4, node5, 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node1, 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, null, null, reader);
				
				String node6 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node7 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node8 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node9 = assertNodeEvent(null, null, false, null, true, reader);
				String node10 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(node9, node10, 0.44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node9, node8, 0.67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node10, node6, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node10, node7, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node7, node8, reader);
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
	public void readSimpleDocumentUseOTULabel() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_USE_OTU_LABEL, true);
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/SimpleDocument.xml"), parameters);
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				String alignment = assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "alignment", null, taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);
				assertCharacterSetIntervalEvent(0, 1, reader);
				assertCharacterSetIntervalEvent(2, 4, reader);				
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);				
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("AACTG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "species2", taxon2, reader);
				assertCharactersEvent("ACGTT", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("ACCTG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, "trees", taxonList, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, null, reader);
				
				String node1 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node2 = assertNodeEvent(null, "species2", false, taxon2, true, reader);
				String node3 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node4 = assertNodeEvent(null, null, false, null, true, reader);
				String node5 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(null, node4, 0.778, reader);
				assertEndEvent(EventContentType.ROOT_EDGE, reader);
				
				assertEdgeEvent(node4, node5, 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node1, 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, null, null, reader);
				
				String node6 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node7 = assertNodeEvent(null, "species2", false, taxon2, true, reader);
				String node8 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node9 = assertNodeEvent(null, null, false, null, true, reader);
				String node10 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(node9, node10, 0.44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node9, node8, 0.67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node10, node6, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node10, node7, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node7, node8, reader);
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
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "hasCustomXML", "ex")), LiteralContentSequenceType.XML, 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Literal", "rdf")), "customXML", reader);
				assertXMLContentEvent("characters", XMLStreamConstants.CHARACTERS, null, "characters", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.example.net/", "customTag", "ex"), null, false, reader);
				assertXMLContentEvent("some more ", XMLStreamConstants.CHARACTERS, null, "some more ", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.example.net/", "nestedTag", "ex"), null, false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.example.net/", "nestedTag", "ex"), null, false, reader);
				assertXMLContentEvent("characters", XMLStreamConstants.CHARACTERS, null, "characters", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.example.net/", "customTag", "ex"), null, true, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), LiteralContentSequenceType.XML, null, 
						null, reader);
				assertXMLContentEvent("47", XMLStreamConstants.CHARACTERS, null, "47", true, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "someString1", null, "someString1", true, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", "forty-seven", 47, true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "someAlternativeString", "someAlternativeString", 
						"someAlternativeString", true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "someString2", "alternativeString", "someString2", true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), null, null, false, reader);
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), null, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "someString", "someString", "someString", true, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "rel", "ex")), new URI("http://www.test.org/test1"), 
						null, true, reader);
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", "47", 47, true, reader);
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				String alignment = assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList, reader);
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), LiteralContentSequenceType.SIMPLE, 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), null, reader);
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
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);				
				assertCharacterSetIntervalEvent(0, 1, reader);
				assertCharacterSetIntervalEvent(2, 4, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);				
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
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "string", "xsd")), "N", null, "N", true, reader);
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertCharactersEvent("AACTG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("ACGTT", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("ACCTG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, taxonList, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, null, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "48", null, 48, true, reader);
				
				String node1 = assertNodeEvent(null, null, false, taxon1, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "49", null, 49, true, reader);
				assertEndEvent(EventContentType.NODE, reader);
				
				String node2 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node3 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node4 = assertNodeEvent(null, null, false, null, true, reader);
				String node5 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(null, node4, 0.778, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "50", null, 50, true, reader);
				assertEndEvent(EventContentType.ROOT_EDGE, reader);
				
				assertEdgeEvent(node4, node5, 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "51", null, 51, true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node1, 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, null, null, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "52", null, 52, true, reader);
				
				String node6 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node7 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node8 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node9 = assertNodeEvent(null, null, false, null, true, reader);
				String node10 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(node9, node10, 0.44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node9, node8, 0.67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node10, node6, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node10, node7, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node7, node8, reader);
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
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "hasCustomNeXML", "ex")), LiteralContentSequenceType.XML, 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Literal", "rdf")), "customNeXML", reader);
				assertXMLContentEvent("\n\t\t\t", XMLStreamConstants.CHARACTERS, null, "\n\t\t\t", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.nexml.org/2009", "otus"), null, false, reader);
				assertXMLContentEvent("\n\t\t\t\t", XMLStreamConstants.CHARACTERS, null, "\n\t\t\t\t", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent("\n\t\t\t\t", XMLStreamConstants.CHARACTERS, null, "\n\t\t\t\t", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent("\n\t\t\t\t", XMLStreamConstants.CHARACTERS, null, "\n\t\t\t\t", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.START_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.nexml.org/2009", "otu"), null, false, reader);
				assertXMLContentEvent("\n\t\t\t", XMLStreamConstants.CHARACTERS, null, "\n\t\t\t", false, reader);
				assertXMLContentEvent(null, XMLStreamConstants.END_ELEMENT, new QName("http://www.nexml.org/2009", "otus"), null, false, reader);
				assertXMLContentEvent("\n\t\t", XMLStreamConstants.CHARACTERS, null, "\n\t\t", true, reader);
				
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.OTU_SET, null, null, taxonList, reader);
				assertSetElementEvent(taxon1, EventContentType.OTU, reader);
				assertSetElementEvent(taxon3, EventContentType.OTU, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.OTU_SET, true, reader);
				
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				String alignment = assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);				
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
				
				String row1 = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("AACTG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("ACGTT", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				String row3 = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("ACCTG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE_SET, null, null, alignment, reader);
				assertSetElementEvent(row1, EventContentType.SEQUENCE, reader);
				assertSetElementEvent(row3, EventContentType.SEQUENCE, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE_SET, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				String treeGroup = assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, taxonList, reader);
				
				String tree = assertLabeledIDEvent(EventContentType.TREE, null, null, reader).getID();
				
				String node1 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node2 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node3 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node4 = assertNodeEvent(null, null, false, null, true, reader);
				String node5 = assertNodeEvent(null, null, false, null, true, reader);
				
				String rootedge = assertEdgeEvent(null, node4, 0.778, reader).getID();
				assertEndEvent(EventContentType.ROOT_EDGE, reader);
				
				assertEdgeEvent(node4, node5, 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String edge2 = assertEdgeEvent(node4, node3, reader).getID();
				assertEndEvent(EventContentType.EDGE, reader);
				
				String edge3 = assertEdgeEvent(node5, node1, 0.98, reader).getID();
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE_EDGE_SET, null, null, tree, reader);
				assertSetElementEvent(node1, EventContentType.NODE, reader);
				assertSetElementEvent(node4, EventContentType.NODE, reader);
				assertSetElementEvent(rootedge, EventContentType.ROOT_EDGE, reader);
				assertSetElementEvent(edge2, EventContentType.EDGE, reader);
				assertSetElementEvent(edge3, EventContentType.EDGE, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.NODE_EDGE_SET, true, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				String network = assertLabeledIDEvent(EventContentType.NETWORK, null, null, reader).getID();
				
				String node6 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node7 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node8 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node9 = assertNodeEvent(null, null, false, null, true, reader);
				String node10 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(node9, node10, 0.44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node9, node8, 0.67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String edge7 = assertEdgeEvent(node10, node6, reader).getID();
				assertEndEvent(EventContentType.EDGE, reader);
				
				String edge8 = assertEdgeEvent(node10, node7, reader).getID();
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node7, node8, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.NODE_EDGE_SET, null, null, network, reader);
				assertSetElementEvent(node7, EventContentType.NODE, reader);
				assertSetElementEvent(node10, EventContentType.NODE, reader);
				assertSetElementEvent(edge7, EventContentType.EDGE, reader);
				assertSetElementEvent(edge8, EventContentType.EDGE, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, new QName("http://www.example.net/", "predicate", "ex")), 
						new URIOrStringIdentifier(null, new QName("http://www.w3.org/2001/XMLSchema", "int", "xsd")), "47", null, 47, true, reader);
				assertPartEndEvent(EventContentType.NODE_EDGE_SET, true, reader);
				
				assertEndEvent(EventContentType.NETWORK, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_SET, null, null, treeGroup, reader);
				assertSetElementEvent(tree, EventContentType.TREE, reader);
				assertSetElementEvent(network, EventContentType.NETWORK, reader);
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
				
				String taxonList1 = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, "taxon1", null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, "taxon2", "species2", reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, "taxon3", null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				String taxonList2 = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				String taxon4 = assertLabeledIDEvent(EventContentType.OTU, "taxon4", null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon5 = assertLabeledIDEvent(EventContentType.OTU, "taxon5", "species5", reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon6 = assertLabeledIDEvent(EventContentType.OTU, "taxon6", null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList1, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("AATCG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("ACGCT", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("ACTCG", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList2, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon5, reader);
				assertSingleTokenEvent("A", true, reader);
				assertSingleTokenEvent("G", true, reader);
				assertSingleTokenEvent("A", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon4, reader);
				assertSingleTokenEvent("A", true, reader);
				assertSingleTokenEvent("G", true, reader);
				assertSingleTokenEvent("T", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList1, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 3, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("ACC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("CAC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("AAC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, taxonList1, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, null, reader);
				
				String node1 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node2 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node3 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node4 = assertNodeEvent(null, null, false, null, true, reader);
				String node5 = assertNodeEvent(null, null, true, null, true, reader);
				
				assertEdgeEvent(null, node4, 0.778, reader);
				assertEndEvent(EventContentType.ROOT_EDGE, reader);
				
				assertEdgeEvent(node4, node5, 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node4, node3, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node1, 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node5, node2, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, null, null, reader);
				
				String node6 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node7 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node8 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node9 = assertNodeEvent(null, null, false, null, true, reader);
				String node10 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(node9, node10, 0.44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node9, node8, 0.67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node10, node6, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node10, node7, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node7, node8, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.NETWORK, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, null, reader);
				
				String node11 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node12 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node13 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node14 = assertNodeEvent(null, null, false, null, true, reader);
				String node15 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(node14, node15, 17, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node14, node13, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node15, node11, 89, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node15, node12, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertLabeledIDEvent(EventContentType.NETWORK, null, null, reader);
				
				String node16 = assertNodeEvent(null, null, false, taxon1, true, reader);				
				String node17 = assertNodeEvent(null, null, false, taxon2, true, reader);
				String node18 = assertNodeEvent(null, null, false, taxon3, true, reader);
				String node19 = assertNodeEvent(null, null, false, null, true, reader);
				String node20 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(node19, node20, 44, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node19, node18, 67, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node20, node16, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node20, node17, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node17, node18, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEndEvent(EventContentType.NETWORK, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, taxonList2, reader);
				
				assertLabeledIDEvent(EventContentType.TREE, null, null, reader);
				
				String node21 = assertNodeEvent(null, null, false, taxon4, true, reader);				
				String node22 = assertNodeEvent(null, null, false, taxon5, true, reader);
				String node23 = assertNodeEvent(null, null, false, taxon6, true, reader);
				String node24 = assertNodeEvent(null, null, true, null, true, reader);
				String node25 = assertNodeEvent(null, null, false, null, true, reader);
				
				assertEdgeEvent(node24, node25, 1, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node24, node23, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node25, node21, 0.98, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node25, node22, reader);
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
	public void readMultipleCharactersTags() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/MultipleCharactersTags.xml"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon4 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon5 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("0", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 4, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("0101", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("0101", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("0101", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "Continuous characters", taxonList, reader);
				
				assertCharacterDefinitionEvent(null, "this is character 1", 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, null, reader);
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertSingleTokenEvent("-1.545414144070023", true, reader);
				assertSingleTokenEvent("-2.3905621575431044", true, reader);
				assertSingleTokenEvent("-2.9610221833467265", true, reader);
				assertSingleTokenEvent("0.7868662069161243", true, reader);
				assertSingleTokenEvent("0.22968509237534918", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertSingleTokenEvent("-1.6259836379710066", true, reader);
				assertSingleTokenEvent("3.649352410850134", true, reader);
				assertSingleTokenEvent("1.778885099660406", true, reader);
				assertSingleTokenEvent("-1.2580877968480846", true, reader);
				assertSingleTokenEvent("0.22335354995610862", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);		
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertSingleTokenEvent("-1.5798979984134964", true, reader);
				assertSingleTokenEvent("2.9548251411133157", true, reader);
				assertSingleTokenEvent("1.522005675256233", true, reader);
				assertSingleTokenEvent("-0.8642016921755289", true, reader);
				assertSingleTokenEvent("-0.938129801832388", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);		
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon4, reader);
				assertSingleTokenEvent("2.7436692306788086", true, reader);
				assertSingleTokenEvent("-0.7151148143399818", true, reader);
				assertSingleTokenEvent("4.592207937774776", true, reader);
				assertSingleTokenEvent("-0.6898841440534845", true, reader);
				assertSingleTokenEvent("0.5769509574453064", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);		
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon5, reader);
				assertSingleTokenEvent("3.1060827493657683", true, reader);
				assertSingleTokenEvent("-1.0453787389160105", true, reader);
				assertSingleTokenEvent("2.67416332763427", true, reader);
				assertSingleTokenEvent("-1.4045634106692808", true, reader);
				assertSingleTokenEvent("0.019890469925520196", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);		
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "DNA sequences", taxonList, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("ACGCTCGCATCGCATC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("ACGCTCGCATCGCATC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("ACGCTCGCATCGCATC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "RNA sequences", taxonList, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("ACGCUCGCAUCGCAUC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("ACGCUCGCAUCGCAUC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("ACGCUCGCAUCGCAUC", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "Continuous sequences", taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, null, reader);
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent(new String[]{"-1.545414144070023", "-2.3905621575431044", "-2.9610221833467265", "0.7868662069161243", "0.22968509237534918"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent(new String[]{"-1.6259836379710066", "3.649352410850134", "1.778885099660406", "-1.2580877968480846", "0.22335354995610862"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent(new String[]{"-1.5798979984134964", "2.9548251411133157", "1.522005675256233", "-0.8642016921755289", "-0.938129801832388"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon4, reader);
				assertCharactersEvent(new String[]{"2.7436692306788086", "-0.7151148143399818", "4.592207937774776", "-0.6898841440534845", "0.5769509574453064"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon5, reader);
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon4 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon5 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "Categorical characters", taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("2", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("3", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("4", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("5", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
				assertCharacterSetIntervalEvent(0, 2, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertSingleTokenEvent("1", true, reader);
				assertSingleTokenEvent("2", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertSingleTokenEvent("?", true, reader);
				assertSingleTokenEvent("2", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertSingleTokenEvent("3", true, reader);
				assertSingleTokenEvent("4", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon4, reader);
				assertSingleTokenEvent("2", true, reader);
				assertSingleTokenEvent("-", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon5, reader);
				assertSingleTokenEvent("4", true, reader);
				assertSingleTokenEvent("1", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "Standard sequences", taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				assertCharacterDefinitionEvent(null, null, 5, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("2", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("3", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("4", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("10", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 6, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("1 - 3 - 10 ?", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("2 2 - - 3 - ", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("3413??", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon4, reader);
				assertCharactersEvent("? ? 10 10 2 3", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon5, reader);
				assertCharactersEvent("4?34-3", false, reader);
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, "taxa1", null, reader).getID();
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon4 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon5 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "Categorical characters", taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("2", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("3", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("4", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("5", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
				assertCharacterSetIntervalEvent(0, 2, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertSingleTokenEvent("StandardCategoricalState1", true, reader);
				assertSingleTokenEvent("blue", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertSingleTokenEvent("?", true, reader);
				assertSingleTokenEvent("blue", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertSingleTokenEvent("StandardCategoricalState3", true, reader);
				assertSingleTokenEvent("green", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon4, reader);
				assertSingleTokenEvent("blue", true, reader);
				assertSingleTokenEvent("-", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon5, reader);
				assertSingleTokenEvent("green", true, reader);
				assertSingleTokenEvent("StandardCategoricalState1", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "Standard sequences", taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				assertCharacterDefinitionEvent(null, null, 5, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "DISCRETE", reader);
				assertSingleTokenDefinitionEvent("1", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("2", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("3", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("4", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("10", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
				assertCharacterSetIntervalEvent(0, 6, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertCharactersEvent("standardstates1 - standardstates3 - standardstates5 ?", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent("blue blue - - standardstates3 - ", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent("standardstates3 green standardstates1 standardstates3 ? ? ", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon4, reader);
				assertCharactersEvent("? ? standardstates5 standardstates5 blue standardstates3", true, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon5, reader);
				assertCharactersEvent("green ? standardstates3 green - standardstates3", true, reader);
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);				
				
				String alignment = assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);				
				assertCharacterSetIntervalEvent(0, 1, reader);
				assertCharacterSetIntervalEvent(2, 4, reader);				
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, null, alignment, reader);				
				
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);	
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList, reader);
				
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
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList, reader);
				
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, "species2", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList, reader);
				
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
			
			String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertLabeledIDEvent(EventContentType.OTU, null, null, reader);
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, taxonList, reader);
			
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
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
			
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
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
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
				
				String taxonList = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
				String taxon1 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon2 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon3 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon4 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				String taxon5 = assertLabeledIDEvent(EventContentType.OTU, null, null, reader).getID();
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "Continuous sequences", taxonList, reader);
				
				assertCharacterDefinitionEvent(null, null, 0, true, reader);
				assertCharacterDefinitionEvent(null, null, 1, true, reader);
				assertCharacterDefinitionEvent(null, null, 2, true, reader);
				assertCharacterDefinitionEvent(null, null, 3, true, reader);
				assertCharacterDefinitionEvent(null, null, 4, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, null, reader);
				assertCharacterSetIntervalEvent(0, 5, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon1, reader);
				assertSplitCharactersEventLongTokens("data/NeXML/longContinuousSequence.txt", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon2, reader);
				assertCharactersEvent(new String[]{"-1.6259836379710066", "3.649352410850134", "1.778885099660406", "-1.2580877968480846", "0.22335354995610862"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon3, reader);
				assertCharactersEvent(new String[]{"-1.5798979984134964", "2.9548251411133157", "1.522005675256233", "-0.8642016921755289", "-0.938129801832388"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon4, reader);
				assertCharactersEvent(new String[]{"2.7436692306788086", "-0.7151148143399818", "4.592207937774776", "-0.6898841440534845", "0.5769509574453064"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, null, taxon5, reader);
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