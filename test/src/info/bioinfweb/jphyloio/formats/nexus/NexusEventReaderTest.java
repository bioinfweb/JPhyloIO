/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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


import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.characters.FormatReader;

import java.io.File;
import java.io.IOException;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class NexusEventReaderTest {
	private NexusCommandReaderFactory factory;
	
	
	public NexusEventReaderTest() {
		super();
		factory = new NexusCommandReaderFactory();
		factory.addJPhyloIOReaders();
	}


	@Test
	public void testReadingCharSets() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSet.nex"), false, factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertCommentEvent("comment 1", reader);
				
				assertCharacterSetEvent("set01", 3, 6, reader);
				assertCharacterSetEvent("set01", 9, 10, reader);
				assertCharacterSetEvent("set01", 12, 17, reader);
				
				assertCharacterSetEvent("set02", 4, 10, reader);
				
				assertCharacterSetEvent("set03", 4, 7, reader);
				assertCommentEvent("comment 2", reader);
				assertCommentEvent("comment 3", reader);
				assertCharacterSetEvent("set03", 12, 13, reader);
				
				assertCharacterSetEvent("set04", 4, 7, reader);
				assertCommentEvent("comment 4", reader);
				assertCharacterSetEvent("set04", 12, 13, reader);

				assertCharacterSetEvent("set05", 3, 4, reader);
				assertCharacterSetEvent("set05", 5, 8, reader);
				assertCommentEvent("comment 5", reader);
				assertCharacterSetEvent("set05", 9, 10, reader);
				assertCharacterSetEvent("set05", 11, 13, reader);

				assertCharacterSetEvent("set06", 0, 1, reader);
				assertCharacterSetEvent("set06", 2, 5, reader);
				assertCharacterSetEvent("set06", 6, 7, reader);
				assertCharacterSetEvent("set06", 8, 10, reader);
				assertCommentEvent("comment 6", reader);
				
				assertCharacterSetEvent("set07", 0, 0, reader);
				assertCharacterSetEvent("set08", 0, 1, reader);
				assertCharacterSetEvent("set09", 1, 2, reader);
				assertCharacterSetEvent("set10", 0, 0, reader);
				assertCharacterSetEvent("set11", 12, 18, reader);
				assertCharacterSetEvent("set12", 3, 4, reader);

				assertCharacterSetEvent("set.13", 1, 2, reader);
				assertCharacterSetEvent("set.14", 1, 2, reader);
				assertCharacterSetEvent("set'15", 1, 2, reader);
				assertCharacterSetEvent("set 16;", 1, 2, reader);

				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingCharSetsInvalidSymbol() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSetInvalidVectorSymbol.nex"), false, factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				
				assertCharacterSetEvent("set1", 2, 4, reader);  // The exception happens here already, because the parser is always one element ahead.
				reader.next();
			}
			finally {
				reader.close();
			}
			fail("Expected exception not fired.");
		}
		catch (Exception e) {
			if (e instanceof IOException) {
				assertEquals("Invalid CharSet vector symbol '2' found.", e.getMessage());
			}
			else {
				fail(e.getLocalizedMessage());
			}
		}
	}
	
	
	@Test
	public void testReadingFormatCommand() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Format.nex"), false, factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "nolabels", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "interleave", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "transpose", "", reader);
				
				ParameterMap map = reader.getStreamDataProvider().getSharedInformationMap();
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false));
				assertFalse(map.getBoolean(FormatReader.INFO_KEY_LABELS, true));
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false));
				assertFalse(map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false));  // May be false or not contained in the map.
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingFormatCommandTokens() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatTokens.nex"), false, factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "nolabels", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "interleave", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "transpose", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "tokens", "", reader);
				
				ParameterMap map = reader.getStreamDataProvider().getSharedInformationMap();
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false));
				assertFalse(map.getBoolean(FormatReader.INFO_KEY_LABELS, true));
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false));
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false));
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingFormatCommandContinuous() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatContinuous.nex"), false, factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.CONTINUOUS, "Continuous", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "nolabels", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "interleave", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "transpose", "", reader);
				
				ParameterMap map = reader.getStreamDataProvider().getSharedInformationMap();
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false));
				assertFalse(map.getBoolean(FormatReader.INFO_KEY_LABELS, true));
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false));
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false));
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingMatrix() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Matrix.nex"), false, factory);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);

				assertCommentEvent("comment 1", reader);
				assertCharactersEvent("A", "CGGTCAT", reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent("B", "CG-TCTT", reader);
				assertCharactersEvent("C", "CG-T", reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent("C", "C-T", reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingMatrixLongTokens() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixLongTokens.nex"), false, factory);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "tokens", "", reader);

				assertCommentEvent("comment 1", reader);
				assertCharactersEvent("A", new String[]{"Eins", "Zwei", "Eins"}, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent("B", new String[]{"Zwei", "Zwei", "Eins"}, reader);
				assertCharactersEvent("C", new String[]{"Drei"}, reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent("C", new String[]{"Zwei", "-"}, reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingMatrixLongContinuous() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixContinuous.nex"), false, factory);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.CONTINUOUS, "continuous", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);

				assertCommentEvent("comment 1", reader);
				assertCharactersEvent("A", new String[]{"18", "20", "2"}, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent("B", new String[]{"4.1", "-3.9", "2E10"}, reader);
				assertCharactersEvent("C", new String[]{"19"}, reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent("C", new String[]{"20.5", "-"}, reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingMatrixDNAAlternatives() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixDNAAlternatives.nex"), false, factory);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent("A", new String[]{"(CG)", "G", "G", "T", "C", "A", "T"}, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent("B", new String[]{"C", "G", "-", "T", "C", "{TA}", "T"}, reader);
				assertCharactersEvent("C", new String[]{"C", "G", "-", "T"}, reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent("C", new String[]{"C", "-", "T"}, reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingMatrixLongTokensAlternatives() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixLongTokensAlternatives.nex"), false, factory);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "tokens", "", reader);

				assertCommentEvent("comment 1", reader);
				assertCharactersEvent("A", new String[]{"Eins", "(Zwei Drei)", "Eins"}, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent("B", new String[]{"Zwei", "Zwei", "Eins"}, reader);
				assertCharactersEvent("C", new String[]{"Drei"}, reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent("C", new String[]{"{Zwei Eins}", "-"}, reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingMatrixInterleaved() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleaved.nex"), false, factory);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);

				assertCommentEvent(" 0....5. ", reader);
				assertCharactersEvent("A", "CGGTCAT", reader);
				assertCharactersEvent("B", "CG-TCTT", reader);
				assertCharactersEvent("C", "CG-TC-T", reader);

				assertCommentEvent(" ...10.. ", reader);
				assertCharactersEvent("A", "A-CGGAT", reader);
				assertCharactersEvent("B", "ATCGCAT", reader);
				assertCharactersEvent("C", "A-CCGAT", reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingMatrixInterleavedMatchCharacter() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleavedMatchCharacter.nex"), true, factory);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				assertSingleTokenDefinitionEvent("i", SingleTokenDefinitionEvent.Meaning.MATCH, reader);

				assertCommentEvent(" 0....5. ", reader);
				assertCharactersEvent("A", "CGGTCAT", reader);
				assertCharactersEvent("B", "CG-TCTT", reader);
				assertCharactersEvent("C", "CG-TC-T", reader);

				assertCommentEvent(" ...10.. ", reader);
				assertCharactersEvent("A", "A-CGGAT", reader);
				assertCharactersEvent("B", "ATCGCAT", reader);
				assertCharactersEvent("C", "A-CCGAT", reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingSplitComments() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/SplitComments.nex"), false, factory);
			reader.setMaxCommentLength(13);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				
				assertCommentEvent("comment", true, reader);
				assertCommentEvent("short comment", true, reader);
				assertCommentEvent("long comment ", false, reader);
				assertCommentEvent("!", true, reader);
				assertCommentEvent("long comment ", false, reader);
				assertCommentEvent("0123456789", true, reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingSymbols() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Symbols.nex"), false, factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DISCRETE, "Standard", reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				assertSingleTokenDefinitionEvent(".", SingleTokenDefinitionEvent.Meaning.MATCH, reader);
				assertSingleTokenDefinitionEvent("A", SingleTokenDefinitionEvent.Meaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("B", SingleTokenDefinitionEvent.Meaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("C", SingleTokenDefinitionEvent.Meaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("D", SingleTokenDefinitionEvent.Meaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("E", SingleTokenDefinitionEvent.Meaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("F", SingleTokenDefinitionEvent.Meaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("G", SingleTokenDefinitionEvent.Meaning.CHARACTER_STATE, reader);

				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "nolabels", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "interleave", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "transpose", "", reader);
				
				ParameterMap map = reader.getStreamDataProvider().getSharedInformationMap();
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_INTERLEAVE, false));
				assertFalse(map.getBoolean(FormatReader.INFO_KEY_LABELS, true));
				assertTrue(map.getBoolean(FormatReader.INFO_KEY_TRANSPOSE, false));
				assertFalse(map.getBoolean(FormatReader.INFO_KEY_TOKENS_FORMAT, false));  // May be false or not contained in the map.
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
	public void testReadingFormatSymbols() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatContinuousSymbols.nex"), false, factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.CONTINUOUS, "Continuous", reader);
				try {
					assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);  // Parsing is one event ahead
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
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingMrBayesDataTypeExtension() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatMrBayesDataType.nex"), false, factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertCharacterSetEvent(FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "1", 1, 1452, reader);
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DNA, "DNA", reader);
				assertCharacterSetEvent(FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "2", 1452, 1549, reader);
				assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType.DISCRETE, "Standard", reader);
				
				assertSingleTokenDefinitionEvent("-", SingleTokenDefinitionEvent.Meaning.GAP, reader);
				assertSingleTokenDefinitionEvent("?", SingleTokenDefinitionEvent.Meaning.MISSING, reader);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_END, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
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
