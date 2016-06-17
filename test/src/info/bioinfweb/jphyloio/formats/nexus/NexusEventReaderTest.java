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
package info.bioinfweb.jphyloio.formats.nexus;


import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.commons.testing.TestTools;
import info.bioinfweb.jphyloio.AbstractEventReader;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.characters.FormatReader;

import java.io.File;
import java.io.IOException;

import javax.xml.namespace.QName;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class NexusEventReaderTest implements NexusConstants, ReadWriteConstants {
	public NexusEventReaderTest() {
		super();
	}


	@Test
	public void testReadingCharSets() throws Exception {
		// Note that Nexus indices start with 1 and JPhyloIO indices start with 0.
		
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSet.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertCommentEvent("comment 1", reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set01", null, reader);
			assertCharacterSetIntervalEvent(2, 5, reader);
			assertCharacterSetIntervalEvent(8, 9, reader);
			assertCharacterSetIntervalEvent(11, 16, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertCommentEvent("comment 1a", reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set02", null, reader);
			assertCharacterSetIntervalEvent(3, 9, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertCommentEvent("comment 2", reader);
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set03", null, reader);
			assertCommentEvent("comment 3", reader);
			assertCharacterSetIntervalEvent(3, 6, reader);
			assertCharacterSetIntervalEvent(11, 12, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set04", null, reader);
			assertCharacterSetIntervalEvent(3, 6, reader);
			assertCommentEvent("comment 4", reader);
			assertCharacterSetIntervalEvent(11, 12, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set05", null, reader);
			assertCharacterSetIntervalEvent(3, 4, reader);
			assertCharacterSetIntervalEvent(5, 7, reader);
			assertCommentEvent("comment 5", reader);
			assertCharacterSetIntervalEvent(7, 8, reader);
			assertCharacterSetIntervalEvent(9, 10, reader);
			assertCharacterSetIntervalEvent(11, 13, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set06", null, reader);
			assertCharacterSetIntervalEvent(0, 1, reader);
			assertCharacterSetIntervalEvent(2, 5, reader);
			assertCharacterSetIntervalEvent(6, 7, reader);
			assertCharacterSetIntervalEvent(8, 10, reader);
			assertCommentEvent("comment 6", reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set07", null, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set08", null, reader);
			assertCharacterSetIntervalEvent(0, 1, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set09", null, reader);
			assertCharacterSetIntervalEvent(0, 1, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set10", null, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set11", null, reader);
			assertCharacterSetIntervalEvent(11, 17, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set12", null, reader);
			assertCharacterSetIntervalEvent(2, 3, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set.13", null, reader);
			assertCharacterSetIntervalEvent(0, 1, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set.14", null, reader);
			assertCharacterSetIntervalEvent(0, 1, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set'15", null, reader);
			assertCharacterSetIntervalEvent(0, 1, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set 16;", null, reader);
			assertCharacterSetIntervalEvent(0, 1, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}


	@Test
	public void testReadingCharSetsInvalidSymbol() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSetInvalidVectorSymbol.nex"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				//assertCharacterSetEvent("set1", 2, 4, reader);  // The exception happens here already, because the parser is always one element ahead.
				reader.next();
				reader.next();
			}
			finally {
				reader.close();
			}
			fail("Expected exception not fired.");
		}
		catch (Exception e) {
			if (e instanceof JPhyloIOReaderException) {
				assertEquals("Invalid CHARSET vector symbol '2' found.", e.getMessage());
			}
			else {
				fail(e.getLocalizedMessage());
			}
		}
	}
	
	
	@Test
	public void testReadingFormatCommand() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Format.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("nolabels", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_NO_LABELS)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("interleave", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_INTERLEAVE)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("transpose", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_TRANSPOSE)), null, "", null, null, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.END, reader);
			
			ParameterMap map = ((NexusReaderStreamDataProvider)TestTools.getPrivateMethod(
					AbstractEventReader.class, "getStreamDataProvider").invoke(reader)).getSharedInformationMap();
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false));
			assertFalse(map.getBoolean(FormatReader.INFO_KEY_LABELS, true));
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false));
			assertFalse(map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false));  // May be false or not contained in the map.
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingFormatCommandTokens() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatTokens.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("nolabels", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_NO_LABELS)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("interleave", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_INTERLEAVE)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("transpose", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_TRANSPOSE)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("tokens", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_TOKENS)), null, "", null, null, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
			
			ParameterMap map = ((NexusReaderStreamDataProvider)TestTools.getPrivateMethod(
					AbstractEventReader.class, "getStreamDataProvider").invoke(reader)).getSharedInformationMap();
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false));
			assertFalse(map.getBoolean(FormatReader.INFO_KEY_LABELS, true));
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false));
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false));
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingFormatCommandContinuous() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatContinuous.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("nolabels", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_NO_LABELS)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("interleave", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_INTERLEAVE)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("transpose", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_TRANSPOSE)), null, "", null, null, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, "Continuous", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
			
			ParameterMap map = ((NexusReaderStreamDataProvider)TestTools.getPrivateMethod(
					AbstractEventReader.class, "getStreamDataProvider").invoke(reader)).getSharedInformationMap();
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false));
			assertFalse(map.getBoolean(FormatReader.INFO_KEY_LABELS, true));
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false));
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false));
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrix() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Matrix.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			
			String idA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idC = assertLabeledIDEvent(EventContentType.OTU, null, "C", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idDE = assertLabeledIDEvent(EventContentType.OTU, null, "D E", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idF = assertLabeledIDEvent(EventContentType.OTU, null, "F", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			
			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idA, idDE);
			assertNotEquals(idA, idF);
			assertNotEquals(idB, idC);
			assertNotEquals(idB, idDE);
			assertNotEquals(idB, idF);
			assertNotEquals(idC, idDE);
			assertNotEquals(idC, idF);
			assertNotEquals(idDE, idF);			
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "someMatrix", otusID, reader);  //TODO Check linked OTU ID
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "4", null, new Long(4), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "7", null, new Long(7), true, reader);

			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", idA, reader);
			assertCommentEvent("comment 1", reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", idB, reader);
			assertCommentEvent("comment 2", reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", idC, reader);
			assertCharactersEvent("CG-T", reader);
			assertCommentEvent("comment 3", reader);
			assertCharactersEvent("C-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "D E", idDE, reader);
			assertCharactersEvent("CGTCATG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrixLongTokens() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixLongTokens.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "3", null, new Long(3), true, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("tokens", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_TOKENS)), null, "", null, null, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCommentEvent("comment 1", reader);
			assertCharactersEvent(new String[]{"Eins", "Zwei", "Eins"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCommentEvent("comment 2", reader);
			assertCharactersEvent(new String[]{"Zwei", "Zwei", "Eins"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent(new String[]{"Drei"}, reader);
			assertCommentEvent("comment 3", reader);
			assertCharactersEvent(new String[]{"Zwei", "-"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrixLongContinuous() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixContinuous.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "3", null, new Long(3), true, reader);

			assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, "continuous", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCommentEvent("comment 1", reader);
			assertCharactersEvent(new String[]{"18", "20", "2"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCommentEvent("comment 2", reader);
			assertCharactersEvent(new String[]{"4.1", "-3.9", "2E10"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent(new String[]{"19"}, reader);
			assertCommentEvent("comment 3", reader);
			assertCharactersEvent(new String[]{"20.5", "-"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrixDNAAlternatives() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixDNAAlternatives.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "7", null, new Long(7), true, reader);

			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCommentEvent("comment 1", reader);
			assertCharactersEvent(new String[]{"(CG)", "G", "G", "T", "C", "A", "T"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCommentEvent("comment 2", reader);
			assertCharactersEvent(new String[]{"C", "G", "-", "T", "C", "{TA}", "T"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent(new String[]{"C", "G", "-", "T"}, reader);
			assertCommentEvent("comment 3", reader);
			assertCharactersEvent(new String[]{"C", "-", "T"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrixLongTokensAlternatives() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixLongTokensAlternatives.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "3", null, new Long(3), true, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("tokens", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_TOKENS)), null, "", null, null, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCommentEvent("comment 1", reader);
			assertCharactersEvent(new String[]{"Eins", "(Zwei Drei)", "Eins"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCommentEvent("comment 2", reader);
			assertCharactersEvent(new String[]{"Zwei", "Zwei", "Eins"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent(new String[]{"Drei"}, reader);
			assertCommentEvent("comment 3", reader);
			assertCharactersEvent(new String[]{"{Zwei Eins}", "-"}, reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrixInterleaved() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleaved.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "14", null, new Long(14), true, reader);

			assertLiteralMetaEvent(new URIOrStringIdentifier("interleave", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_INTERLEAVE)), null, "", null, null, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertCommentEvent(" 0....5. ", reader);
			String idA = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idB = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idC = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent("CG-TC-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idB, idC);

			assertCommentEvent(" ...10.. ", reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idA, "A", null, reader);
			assertCharactersEvent("A-CGGAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idB, "B", null, reader);
			assertCharactersEvent("ATCGCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idC, "C", null, reader);
			assertCharactersEvent("A-CCGAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrixInterleavedMatchCharacterSplit() throws Exception {
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterMap.KEY_MAXIMUM_TOKENS_TO_READ, 4);
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleavedMatchCharacter.nex"), parameters);
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "14", null, new Long(14), true, reader);

			assertLiteralMetaEvent(new URIOrStringIdentifier("interleave", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_INTERLEAVE)), null, "", null, null, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertSingleTokenDefinitionEvent("i", CharacterSymbolMeaning.MATCH, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertCommentEvent(" 0....5. ", reader);
			String idA = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCharactersEvent("CGGT", reader);
			assertCharactersEvent("CAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idB = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCharactersEvent("CG-T", reader);
			assertCharactersEvent("CTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idC = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent("CG-T", reader);
			assertCharactersEvent("C-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idB, idC);

			assertCommentEvent(" ...10.. ", reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idA, "A", null, reader);
			assertCharactersEvent("A-CG", reader);
			assertCharactersEvent("GAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idB, "B", null, reader);
			assertCharactersEvent("ATCG", reader);
			assertCharactersEvent("CAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idC, "C", null, reader);
			assertCharactersEvent("A-CC", reader);
			assertCharactersEvent("GAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrixInterleavedMatchCharacter() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleavedMatchCharacter.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "14", null, new Long(14), true, reader);

			assertLiteralMetaEvent(new URIOrStringIdentifier("interleave", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_INTERLEAVE)), null, "", null, null, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertSingleTokenDefinitionEvent("i", CharacterSymbolMeaning.MATCH, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertCommentEvent(" 0....5. ", reader);
			String idA = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idB = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idC = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent("CG-TC-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idB, idC);

			assertCommentEvent(" ...10.. ", reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idA, "A", null, reader);
			assertCharactersEvent("A-CGGAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idB, "B", null, reader);
			assertCharactersEvent("ATCGCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, idC, "C", null, reader);
			assertCharactersEvent("A-CCGAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingSplitComments() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_MAXIMUM_COMMENT_LENGTH, 13);
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/SplitComments.nex"), parameters);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertCommentEvent("comment", false, reader);
				assertCommentEvent("short comment", false, reader);
				assertCommentEvent("long comment ", true, reader);
				assertCommentEvent("!", false, reader);
				assertCommentEvent("long comment ", true, reader);
				assertCommentEvent("0123456789", false, reader);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
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
	public void testReadingSymbols() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Symbols.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("nolabels", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_NO_LABELS)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("interleave", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_INTERLEAVE)), null, "", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("transpose", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_TRANSPOSE)), null, "", null, null, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "Standard", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertSingleTokenDefinitionEvent(".", CharacterSymbolMeaning.MATCH, true, reader);
			assertSingleTokenDefinitionEvent("A", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("B", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("C", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("D", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("E", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("F", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertSingleTokenDefinitionEvent("G", CharacterSymbolMeaning.CHARACTER_STATE, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			ParameterMap map = ((NexusReaderStreamDataProvider)TestTools.getPrivateMethod(
					AbstractEventReader.class, "getStreamDataProvider").invoke(reader)).getSharedInformationMap();
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false));
			assertFalse(map.getBoolean(FormatReader.INFO_KEY_LABELS, true));
			assertTrue(map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false));
			assertFalse(map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false));  // May be false or not contained in the map.
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingFormatSymbols() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatContinuousSymbols.nex"), new ReadWriteParameterMap());
		try {
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				assertTokenSetDefinitionEvent(CharacterStateSetType.CONTINUOUS, "Continuous", reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);  // Parsing is one event ahead
				fail("Exception not thrown");
			}
			catch (IOException e) {
				assertEquals(e.getMessage(), "The subcommand SYMBOLS of FORMAT is not allowed if DATATYPE=CONTINUOUS was specified."); 
			}
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMrBayesDataTypeExtensionValid() throws Exception {
		testReadingMrBayesDataTypeExtension("data/Nexus/FormatMrBayesDataType.nex", true);
	}
	
	
	@Test
	public void testReadingMrBayesDataTypeExtensionInvalid() throws Exception {
		testReadingMrBayesDataTypeExtension("data/Nexus/FormatMrBayesDataTypeInvalidSymbols.nex", true);
	}
	
	
	@Test
	public void testReadingMrBayesDataTypeExtensionNoSingleDefs() throws Exception {
		testReadingMrBayesDataTypeExtension("data/Nexus/FormatMrBayesDataTypeNoSingleDefs.nex", true);
	}
	
	
	private void testReadingMrBayesDataTypeExtension(String file, boolean testSingleDefs) throws Exception {
		NexusEventReader reader = new NexusEventReader(new File(file), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			if (testSingleDefs) {
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			}
			assertCharacterSetIntervalEvent(1, 1452, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DISCRETE, "Standard", reader);
			if (testSingleDefs) {
				assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			}
			assertCharacterSetIntervalEvent(1452, 1549, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}


	@Test
	public void testReadingMatrixUnaligned() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixUnaligned.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "taxon-1", null, reader);
			assertCharactersEvent("ACTAGGACTAGATCAAGTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "taxon-2", null, reader);
			assertCharactersEvent("ACCAGGACTAGCGGATCAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "taxon-3", null, reader);
			assertCharactersEvent("ACCAGGACTAGATCAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "taxon-4", null, reader);
			assertCharactersEvent("AGCCAGGACTAGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "taxon-5", null, reader);
			assertCharactersEvent("ATCAGGACTAGATCAAGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}


	@Test
	public void testReadingTaxaNoLabels() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TaxaNoLabels.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			assertCommentEvent("comment 1", reader);
			assertCommentEvent("comment 2", reader);
			
			String idA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertCommentEvent("comment 3", reader);
			String idB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idC = assertLabeledIDEvent(EventContentType.OTU, null, "C C", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertCommentEvent("comment 4", reader);
			String idD = assertLabeledIDEvent(EventContentType.OTU, null, "D'", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertCommentEvent("comment 5", reader);
			String idE = assertLabeledIDEvent(EventContentType.OTU, null, "E", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			
			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idA, idD);
			assertNotEquals(idA, idE);
			assertNotEquals(idB, idC);
			assertNotEquals(idB, idD);
			assertNotEquals(idB, idE);
			assertNotEquals(idC, idD);
			assertNotEquals(idC, idE);
			assertNotEquals(idD, idE);			
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			
			assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, otusID, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "5", null, new Long(5), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "7", null, new Long(7), true, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("nolabels", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_NO_LABELS)), null, "", null, null, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", idA, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", idB, reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C C", idC, reader);
			assertCharactersEvent("CG-TC-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "D'", idD, reader);
			assertCharactersEvent("CGTCATG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "E", idE, reader);
			assertCharactersEvent("CCTGATG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}


	@Test(expected=IOException.class)
	public void testReadingTaxaNoLabelsInvalid() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TaxaNoLabelsInvalid.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			
			String idA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			
			assertNotEquals(idA, idB);
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			
			assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, null, otusID, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "5", null, new Long(5), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "7", null, new Long(7), true, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("nolabels", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_NO_LABELS)), null, "", null, null, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", idA, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", idB, reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	private void testReadingTreesTranslateSingleTree(NexusEventReader reader, String label,
			String otuIDScarabaeus, String otuIDDrosophila, String otuIDAranaeus) throws Exception {
		assertLabeledIDEvent(EventContentType.TREE, null, label, reader);
		
		String nodeIDScarabaeus = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "Scarabaeus", otuIDScarabaeus, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		String nodeIDDrosophila = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "Drosophila", otuIDDrosophila, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		
		String nodeIDN1 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		assertEdgeEvent(nodeIDN1, nodeIDScarabaeus, reader);
		assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
		assertEdgeEvent(nodeIDN1, nodeIDDrosophila, reader);
		assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
		
		String nodeIDAranaeus = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "Aranaeus", otuIDAranaeus, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		String nodeIDN2 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		
		assertEdgeEvent(nodeIDN2, nodeIDN1, reader);
		assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
		assertEdgeEvent(nodeIDN2, nodeIDAranaeus, reader);
		assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
		assertEdgeEvent(null, nodeIDN2, reader);
		assertEndEvent(EventContentType.EDGE, reader);
		assertEventType(EventContentType.TREE, EventTopologyType.END, reader);

		assertNotEquals(nodeIDScarabaeus, nodeIDDrosophila);
		assertNotEquals(nodeIDScarabaeus, nodeIDAranaeus);
		assertNotEquals(nodeIDScarabaeus, nodeIDN1);
		assertNotEquals(nodeIDScarabaeus, nodeIDN2);
		assertNotEquals(nodeIDDrosophila, nodeIDAranaeus);
		assertNotEquals(nodeIDDrosophila, nodeIDN1);
		assertNotEquals(nodeIDDrosophila, nodeIDN2);
		assertNotEquals(nodeIDAranaeus, nodeIDN1);
		assertNotEquals(nodeIDAranaeus, nodeIDN2);
		assertNotEquals(nodeIDN1, nodeIDN2);
	}


	@Test
	public void testReadingTreesTranslate() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TreesTranslate.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			String otuIDScarabaeus = assertLabeledIDEvent(EventContentType.OTU, null, "Scarabaeus", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDDrosophila = assertLabeledIDEvent(EventContentType.OTU, null, "Drosophila", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDAranaeus = assertLabeledIDEvent(EventContentType.OTU, null, "Aranaeus", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);

			assertNotEquals(otuIDScarabaeus, otuIDDrosophila);
			assertNotEquals(otuIDScarabaeus, otuIDAranaeus);
			assertNotEquals(otuIDDrosophila, otuIDAranaeus);
			
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, otusID, reader);
			testReadingTreesTranslateSingleTree(reader, "tree1", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			testReadingTreesTranslateSingleTree(reader, "tree2", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			testReadingTreesTranslateSingleTree(reader, "tree3", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}


	@Test
	public void testReadingTreesNumericName() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TreesNumericName.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			String otuIDScarabaeus = assertLabeledIDEvent(EventContentType.OTU, null, "Scarabaeus bug", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDDrosophila = assertLabeledIDEvent(EventContentType.OTU, null, "Drosophila", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);

			assertCommentEvent("comment 1", reader);
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, otusID, reader);
			assertLabeledIDEvent(EventContentType.TREE, null, "my tree", reader);
			
			String nodeIDScarabaeus = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "Scarabaeus bug", otuIDScarabaeus, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String nodeIDDrosophila = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "Drosophila", otuIDDrosophila, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String nodeIDN1 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN1, nodeIDScarabaeus, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN1, nodeIDDrosophila, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String nodeIDAranaeus = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "3", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String nodeIDN2 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			assertEdgeEvent(nodeIDN2, nodeIDN1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN2, nodeIDAranaeus, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, nodeIDN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);

			assertCommentEvent("comment 2", reader);
			assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}


	@Test
	public void testReadingTreesMetadata() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TreesMetadata.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			String otuIDA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDC = assertLabeledIDEvent(EventContentType.OTU, null, "C", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);

			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, otusID, reader);
			assertLabeledIDEvent(EventContentType.TREE, null, "tree", reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DISPLAY_TREE_ROOTED), null, "false", null, new Boolean(false), true, reader);
			
			String idA = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "A", otuIDA, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob", PREDICATE_HAS_LITERAL_METADATA), null, "1.000000000000000e+000", null, new Double(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob_stddev", PREDICATE_HAS_LITERAL_METADATA), null, "0", null, new Double(0.0), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("prob_range", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE_ARRAY, reader);
			assertLiteralMetaContentEvent(null, "1.000000000000000e+000", null, new Double(1.0), false, reader);
			assertLiteralMetaContentEvent(null, "1", null, new Double(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob(percent)", PREDICATE_HAS_LITERAL_METADATA), null, "100", null, "100", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob+-sd", PREDICATE_HAS_LITERAL_METADATA), null, "100+-0", null, "100+-0", true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idB = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "B", otuIDB, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob", PREDICATE_HAS_LITERAL_METADATA), null, "1.000000000000000e+000", null, new Double(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob_stddev", PREDICATE_HAS_LITERAL_METADATA), null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("prob_range", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE_ARRAY, reader);
			assertLiteralMetaContentEvent(null, "AB C", null, "AB C", false, reader);
			assertLiteralMetaContentEvent(null, "ABC", null, "ABC", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob(percent)", PREDICATE_HAS_LITERAL_METADATA), null, "100", null, "100", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob+-sd", PREDICATE_HAS_LITERAL_METADATA), null, "100+-0", null, "100+-0", true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idC = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "C", otuIDC, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob", PREDICATE_HAS_LITERAL_METADATA), null, "6.364056912805381e-001", null, new Double(6.364056912805381e-001), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob_stddev", PREDICATE_HAS_LITERAL_METADATA), null, "7.249475639180907e-004", null, new Double(7.249475639180907e-004), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("prob_range", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE_ARRAY, reader);
			assertLiteralMetaContentEvent(null, "6.358930759420870e-001", null, new Double(6.358930759420870e-001), false, reader);
			assertLiteralMetaContentEvent(null, "6.369183066189893e-001", null, new Double(6.369183066189893e-001), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob(percent)", PREDICATE_HAS_LITERAL_METADATA), null, "64", null, "64", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob+-sd", PREDICATE_HAS_LITERAL_METADATA), null, "64+-0", null, "64+-0", true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			
			String idN1 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob", PREDICATE_HAS_LITERAL_METADATA), null, "1.000000000000000e+000", null, new Double(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob_stddev", PREDICATE_HAS_LITERAL_METADATA), null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("prob_range", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE_ARRAY, reader);
			assertLiteralMetaContentEvent(null, "1.000000000000000e+000", null, new Double(1.0), false, reader);
			assertLiteralMetaContentEvent(null, "1.000000000000000e+000", null, new Double(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob(percent)", PREDICATE_HAS_LITERAL_METADATA), null, "100", null, "100", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob+-sd", PREDICATE_HAS_LITERAL_METADATA), null, "100+-0", null, "100+-0", true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN1, idB, 6.244293083853111e-001, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_mean", PREDICATE_HAS_LITERAL_METADATA), null, "6.345415111023917e-001", null, new Double(6.345415111023917e-001), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_median", PREDICATE_HAS_LITERAL_METADATA), null, "6.244293083853111e-001", null, new Double(6.244293083853111e-001), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("length_95%HPD", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE_ARRAY, reader);
			assertLiteralMetaContentEvent(null, "4.360295861156825e-001", null, new Double(4.360295861156825e-001), false, reader);
			assertLiteralMetaContentEvent(null, "8.441623753050405e-001", null, new Double(8.441623753050405e-001), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN1, idC, 7.039004236028111e-002, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_mean", PREDICATE_HAS_LITERAL_METADATA), null, "7.419012044002400e-002", null, new Double(7.419012044002400e-002), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_median", PREDICATE_HAS_LITERAL_METADATA), null, "7.039004236028111e-002", null, new Double(7.039004236028111e-002), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("length_95%HPD", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE_ARRAY, reader);
			assertLiteralMetaContentEvent(null, "9.114712766459516e-003", null, new Double(9.114712766459516e-003), false, reader);
			assertLiteralMetaContentEvent(null, "1.418351647155842e-001", null, new Double(1.418351647155842e-001), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);

			String idN2 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
			assertCommentEvent("18", false, reader);
			//assertMetaEvent(HotCommentDataReader.UNNAMED_NODE_DATA_NAME, "18", null, new Double(18), true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN2, idA, .3682008685714568, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_mean", PREDICATE_HAS_LITERAL_METADATA), null, "3.744759260623280e-001", null, new Double(3.744759260623280e-001), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_median", PREDICATE_HAS_LITERAL_METADATA), null, "3.682008685714568e-001", null, new Double(3.682008685714568e-001), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("length_95%HPD", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE_ARRAY, reader);
			assertLiteralMetaContentEvent(null, "2.494893056441154e-001", null, new Double(2.494893056441154e-001), false, reader);
			assertLiteralMetaContentEvent(null, "5.088322191162278e-001", null, new Double(5.088322191162278e-001), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN2, idN1, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_mean", PREDICATE_HAS_LITERAL_METADATA), null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_median", PREDICATE_HAS_LITERAL_METADATA), null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("length_95%HPD", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE_ARRAY, reader);
			assertLiteralMetaContentEvent(null, "0.000000000000000e+000", null, new Double(0.0), false, reader);
			assertLiteralMetaContentEvent(null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertCommentEvent("comment 1", false, reader);
			assertEndEvent(EventContentType.EDGE, reader);

			assertEdgeEvent(null, idN2, 0.0, reader);
			assertCommentEvent("20", false, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idA, idN1);
			assertNotEquals(idA, idN2);
			assertNotEquals(idB, idC);
			assertNotEquals(idB, idN1);
			assertNotEquals(idB, idN2);
			assertNotEquals(idC, idN1);
			assertNotEquals(idC, idN2);
			assertNotEquals(idN1, idN2);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingTreesMultipleBlocks() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MultipleTreesBlocks.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			String otuIDScarabaeus = assertLabeledIDEvent(EventContentType.OTU, null, "Scarabaeus", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDDrosophila = assertLabeledIDEvent(EventContentType.OTU, null, "Drosophila", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDAranaeus = assertLabeledIDEvent(EventContentType.OTU, null, "Aranaeus", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDBeetle = assertLabeledIDEvent(EventContentType.OTU, null, "beetle", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDFly = assertLabeledIDEvent(EventContentType.OTU, null, "fly", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDSpider = assertLabeledIDEvent(EventContentType.OTU, null, "spider", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);

			assertNotEquals(otuIDScarabaeus, otuIDDrosophila);
			assertNotEquals(otuIDScarabaeus, otuIDAranaeus);
			assertNotEquals(otuIDDrosophila, otuIDAranaeus);
			
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, otusID, reader);
			testReadingTreesTranslateSingleTree(reader, "tree1", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			testReadingTreesTranslateSingleTree(reader, "tree2", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			testReadingTreesTranslateSingleTree(reader, "tree3", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, otusID, reader);
			assertLabeledIDEvent(EventContentType.TREE, null, "otherTree", reader);
			
			String nodeIDBeetle = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "beetle", otuIDBeetle, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String nodeIDFly = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "fly", otuIDFly, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String nodeIDN1 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN1, nodeIDBeetle, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN1, nodeIDFly, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String nodeIDSpider = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "spider", otuIDSpider, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String nodeIDN2 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			assertEdgeEvent(nodeIDN2, nodeIDN1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN2, nodeIDSpider, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, nodeIDN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);

			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testTitleLink() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TitleLink.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			// TAXA:
			assertLabeledIDEvent(EventContentType.OTU_LIST, null, "taxon list 1", reader);
			assertCommentEvent("comment 1", reader);
			assertCommentEvent("comment 2", reader);
			assertCommentEvent("comment 3", reader);
			String idD = assertLabeledIDEvent(EventContentType.OTU, null, "D", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idE = assertLabeledIDEvent(EventContentType.OTU, null, "E", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idF = assertLabeledIDEvent(EventContentType.OTU, null, "F", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertCommentEvent("comment 4", reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, "taxon list 2", reader).getID();
			String otuIDA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDC = assertLabeledIDEvent(EventContentType.OTU, null, "C", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			assertNotEquals(otuIDA, otuIDB);
			assertNotEquals(otuIDA, otuIDC);
			assertNotEquals(otuIDA, idD);
			assertNotEquals(otuIDA, idE);
			assertNotEquals(otuIDA, idF);
			assertNotEquals(otuIDB, otuIDC);
			assertNotEquals(otuIDB, idD);
			assertNotEquals(otuIDB, idE);
			assertNotEquals(otuIDB, idF);
			assertNotEquals(otuIDC, idD);
			assertNotEquals(otuIDC, idE);
			assertNotEquals(otuIDC, idF);
			assertNotEquals(idD, idE);
			assertNotEquals(idD, idF);
			assertNotEquals(idE, idF);
			
			// CHARACTERS:
			assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "someMatrix", otusID, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "5", null, new Long(5), true, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("nolabels", new QName(NEXUS_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + FORMAT_SUBCOMMAND_NO_LABELS)), null, "", null, null, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", otuIDA, reader);
			assertCharactersEvent("ACTGT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", otuIDB, reader);
			assertCharactersEvent("AC-GT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", otuIDC, reader);
			assertCharactersEvent("AC-CT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEndEvent(EventContentType.ALIGNMENT, reader);
			
			// TREES:
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, "someTrees", otusID, reader);
			assertLabeledIDEvent(EventContentType.TREE, null, "someTree", reader);
			
			String nodeIDA = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "A", otuIDA, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String nodeIDB = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "B", otuIDB, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String nodeIDC = assertLinkedLabeledIDEvent(EventContentType.NODE, null, "C", otuIDC, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String nodeIDN1 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(nodeIDN1, nodeIDB, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(nodeIDN1, nodeIDC, reader);
			assertEndEvent(EventContentType.EDGE, reader);

			String nodeIDN2 = assertLinkedLabeledIDEvent(EventContentType.NODE, null, null, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(nodeIDN2, nodeIDA, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(nodeIDN2, nodeIDN1, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(null, nodeIDN2, reader);
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
	
	
	@Test
	public void testCharSetsMultipleMatrices() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSetsMultipleMatrices.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			// TAXA:
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			String otuIDA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDC = assertLabeledIDEvent(EventContentType.OTU, null, "C", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			// CHARACTERS 1:
			String matrixID1 = assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "matrix1", otusID, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "7", null, new Long(7), true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
	
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", otuIDA, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", otuIDB, reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", otuIDC, reader);
			assertCharactersEvent("CG-TC-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEndEvent(EventContentType.ALIGNMENT, reader);
			
			
			// CHARACTERS 2:
			String matrixID2 = assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, null, "matrix2", otusID, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntax", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nchar", PREDICATE_CHARACTER_COUNT), null, "7", null, new Long(7), true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterSymbolMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterSymbolMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
			
			assertCharacterDefinitionEvent(null, "col0", 0, true, reader);
			assertCharacterDefinitionEvent(null, "col1", 1, true, reader);
			assertCharacterDefinitionEvent(null, "col2", 2, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", otuIDA, reader);
			assertCharactersEvent("AGGT-AT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", otuIDB, reader);
			assertCharactersEvent("AC-GCTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", otuIDC, reader);
			assertCharactersEvent("AG-TC-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertEndEvent(EventContentType.ALIGNMENT, reader);
			
			
			// SETS 1:
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set01", matrixID1, reader);
			assertCharacterSetIntervalEvent(1, 4, reader);
			assertCharacterSetIntervalEvent(5, 6, reader);
			assertEndEvent(EventContentType.CHARACTER_SET, reader);
			
			// SETS 2:
			String referencedSetID = assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set01", matrixID2, reader);
			assertCharacterSetIntervalEvent(0, 3, reader);
			assertEndEvent(EventContentType.CHARACTER_SET, reader);
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set02", matrixID2, reader);
			assertCharacterSetIntervalEvent(2, 7, reader);
			assertEndEvent(EventContentType.CHARACTER_SET, reader);
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set03", matrixID2, reader);
			assertCharacterSetIntervalEvent(0, 7, reader);
			assertEndEvent(EventContentType.CHARACTER_SET, reader);
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set04", matrixID2, reader);
			assertCharacterSetIntervalEvent(0, 1, reader);
			assertCharacterSetIntervalEvent(3, 4, reader);
			assertCharacterSetIntervalEvent(6, 7, reader);
			assertEndEvent(EventContentType.CHARACTER_SET, reader);
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set05", matrixID2, reader);
			assertCharacterSetIntervalEvent(1, 2, reader);
			assertCharacterSetIntervalEvent(3, 4, reader);
			assertCharacterSetIntervalEvent(5, 6, reader);
			assertEndEvent(EventContentType.CHARACTER_SET, reader);
//			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set06", matrixID2, reader);
//			assertSetElementEvent(referencedSetID, EventContentType.CHARACTER_SET, reader);
//			assertCharacterSetIntervalEvent(4, 5, reader);
//			assertEndEvent(EventContentType.CHARACTER_SET, reader);
			
			// SETS 3:
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set01", matrixID1, reader);
			assertCharacterSetIntervalEvent(0, 2, reader);
			assertEndEvent(EventContentType.CHARACTER_SET, reader);
			
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test(expected=JPhyloIOReaderException.class)
	public void testCharSetsInvalidOrder() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSetsMatrixInvalidOrder.nex"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			// TAXA:
			String otusID = assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader).getID();
			String otuIDA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDC = assertLabeledIDEvent(EventContentType.OTU, null, "C", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			// SETS 1:
			assertLinkedLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set01", null, reader);
			assertCharacterSetIntervalEvent(1, 4, reader);
			assertCharacterSetIntervalEvent(5, 6, reader);
			assertEndEvent(EventContentType.CHARACTER_SET, reader);
		}
		finally {
			reader.close();
		}
	}
}
