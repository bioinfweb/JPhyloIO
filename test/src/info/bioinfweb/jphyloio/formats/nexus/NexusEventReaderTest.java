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


import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.commons.bio.CharacterStateMeaning;
import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.characters.DimensionsReader;
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
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertCommentEvent("comment 1", reader);
				
				assertCharacterSetEvent("set01", 3, 6, reader);
				assertCharacterSetEvent("set01", 9, 10, reader);
				assertCharacterSetEvent("set01", 12, 17, reader);
				
				assertCharacterSetEvent("set02", 4, 10, reader);
				
				assertCommentEvent("comment 2", reader);
				assertCommentEvent("comment 3", reader);
				assertCharacterSetEvent("set03", 4, 7, reader);
				assertCharacterSetEvent("set03", 12, 13, reader);
				
				assertCharacterSetEvent("set04", 4, 7, reader);
				assertCommentEvent("comment 4", reader);
				assertCharacterSetEvent("set04", 12, 13, reader);

				assertCharacterSetEvent("set05", 3, 4, reader);
				assertCharacterSetEvent("set05", 5, 7, reader);
				assertCommentEvent("comment 5", reader);
				assertCharacterSetEvent("set05", 7, 8, reader);
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
	public void testReadingCharSetsInvalidSymbol() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSetInvalidVectorSymbol.nex"), false, factory);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
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
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "transpose", "", true, true, reader);
				
				ParameterMap map = reader.getStreamDataProvider().getSharedInformationMap();
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
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "transpose", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "tokens", "", true, true, reader);
				
				ParameterMap map = reader.getStreamDataProvider().getSharedInformationMap();
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
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.CONTINUOUS, "Continuous", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "transpose", "", true, true, reader);
				
				ParameterMap map = reader.getStreamDataProvider().getSharedInformationMap();
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
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingMatrix() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Matrix.nex"), false, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);
			
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "7", null, new Long(7), true, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);  // Reads sequence start and comment in here.

			assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
			assertCommentEvent("comment 1", reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertSequenceEndEvent(true, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
			assertCommentEvent("comment 2", reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertSequenceEndEvent(true, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
			assertCharactersEvent("CG-T", reader);
			assertCommentEvent("comment 3", reader);
			assertCharactersEvent("C-T", reader);
			assertSequenceEndEvent(true, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "D E", null, reader);
			assertCharactersEvent("CGTCATG", reader);
			assertSequenceEndEvent(true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingMatrixLongTokens() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixLongTokens.nex"), false, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "3", null, new Long(3), true, true, reader);

				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "tokens", "", true, true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent(new String[]{"Eins", "Zwei", "Eins"}, reader);
				assertSequenceEndEvent(true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent(new String[]{"Zwei", "Zwei", "Eins"}, reader);
				assertSequenceEndEvent(true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
				assertCharactersEvent(new String[]{"Drei"}, reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent(new String[]{"Zwei", "-"}, reader);
				assertSequenceEndEvent(true, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
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
	public void testReadingMatrixLongContinuous() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixContinuous.nex"), false, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "3", null, new Long(3), true, true, reader);

				assertTokenSetDefinitionEvent(CharacterStateType.CONTINUOUS, "continuous", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent(new String[]{"18", "20", "2"}, reader);
				assertSequenceEndEvent(true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent(new String[]{"4.1", "-3.9", "2E10"}, reader);
				assertSequenceEndEvent(true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
				assertCharactersEvent(new String[]{"19"}, reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent(new String[]{"20.5", "-"}, reader);
				assertSequenceEndEvent(true, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
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
	public void testReadingMatrixDNAAlternatives() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixDNAAlternatives.nex"), false, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "7", null, new Long(7), true, true, reader);

				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent(new String[]{"(CG)", "G", "G", "T", "C", "A", "T"}, reader);
				assertSequenceEndEvent(true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent(new String[]{"C", "G", "-", "T", "C", "{TA}", "T"}, reader);
				assertSequenceEndEvent(true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
				assertCharactersEvent(new String[]{"C", "G", "-", "T"}, reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent(new String[]{"C", "-", "T"}, reader);
				assertSequenceEndEvent(true, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
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
	public void testReadingMatrixLongTokensAlternatives() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixLongTokensAlternatives.nex"), false, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "3", null, new Long(3), true, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "tokens", "", true, true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent(new String[]{"Eins", "(Zwei Drei)", "Eins"}, reader);
				assertSequenceEndEvent(true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent(new String[]{"Zwei", "Zwei", "Eins"}, reader);
				assertSequenceEndEvent(true, reader);

				assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
				assertCharactersEvent(new String[]{"Drei"}, reader);
				assertCommentEvent("comment 3", reader);
				assertCharactersEvent(new String[]{"{Zwei Eins}", "-"}, reader);
				assertSequenceEndEvent(true, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
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
	public void testReadingMatrixInterleaved() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleaved.nex"), false, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "14", null, new Long(14), true, true, reader);

				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);

				assertCommentEvent(" 0....5. ", reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
				assertCharactersEvent("CGGTCAT", reader);
				assertSequenceEndEvent(false, reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
				assertCharactersEvent("CG-TCTT", reader);
				assertSequenceEndEvent(false, reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
				assertCharactersEvent("CG-TC-T", reader);
				assertSequenceEndEvent(false, reader);

				assertCommentEvent(" ...10.. ", reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
				assertCharactersEvent("A-CGGAT", reader);
				assertSequenceEndEvent(true, reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
				assertCharactersEvent("ATCGCAT", reader);
				assertSequenceEndEvent(true, reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
				assertCharactersEvent("A-CCGAT", reader);
				assertSequenceEndEvent(true, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
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
	public void testReadingMatrixInterleavedMatchCharacterSplit() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleavedMatchCharacter.nex"), true, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);
			reader.setMaxTokensToRead(4);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "14", null, new Long(14), true, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
			assertSingleTokenDefinitionEvent("i", CharacterStateMeaning.MATCH, reader);

			assertCommentEvent(" 0....5. ", reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
			assertCharactersEvent("CGGT", reader);
			assertCharactersEvent("CAT", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
			assertCharactersEvent("CG-T", reader);
			assertCharactersEvent("CTT", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
			assertCharactersEvent("CG-T", reader);
			assertCharactersEvent("C-T", reader);
			assertSequenceEndEvent(false, reader);

			assertCommentEvent(" ...10.. ", reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
			assertCharactersEvent("A-CG", reader);
			assertCharactersEvent("GAT", reader);
			assertSequenceEndEvent(true, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
			assertCharactersEvent("ATCG", reader);
			assertCharactersEvent("CAT", reader);
			assertSequenceEndEvent(true, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
			assertCharactersEvent("A-CC", reader);
			assertCharactersEvent("GAT", reader);
			assertSequenceEndEvent(true, reader);
			
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
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleavedMatchCharacter.nex"), true, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "14", null, new Long(14), true, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
			assertSingleTokenDefinitionEvent("i", CharacterStateMeaning.MATCH, reader);

			assertCommentEvent(" 0....5. ", reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
			assertCharactersEvent("CG-TC-T", reader);
			assertSequenceEndEvent(false, reader);

			assertCommentEvent(" ...10.. ", reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
			assertCharactersEvent("A-CGGAT", reader);
			assertSequenceEndEvent(true, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
			assertCharactersEvent("ATCGCAT", reader);
			assertSequenceEndEvent(true, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
			assertCharactersEvent("A-CCGAT", reader);
			assertSequenceEndEvent(true, reader);
			
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/SplitComments.nex"), false, factory);
			reader.setMaxCommentLength(13);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertCommentEvent("comment", true, reader);
				assertCommentEvent("short comment", true, reader);
				assertCommentEvent("long comment ", false, reader);
				assertCommentEvent("!", true, reader);
				assertCommentEvent("long comment ", false, reader);
				assertCommentEvent("0123456789", true, reader);
				
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
	public void testReadingSymbols() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Symbols.nex"), false, factory);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.DISCRETE, "Standard", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				assertSingleTokenDefinitionEvent(".", CharacterStateMeaning.MATCH, reader);
				assertSingleTokenDefinitionEvent("A", CharacterStateMeaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("B", CharacterStateMeaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("C", CharacterStateMeaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("D", CharacterStateMeaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("E", CharacterStateMeaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("F", CharacterStateMeaning.CHARACTER_STATE, reader);
				assertSingleTokenDefinitionEvent("G", CharacterStateMeaning.CHARACTER_STATE, reader);

				assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "transpose", "", true, true, reader);
				
				ParameterMap map = reader.getStreamDataProvider().getSharedInformationMap();
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
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.CONTINUOUS, "Continuous", reader);
				try {
					assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);  // Parsing is one event ahead
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
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertCharacterSetEvent(FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "1", 1, 1452, reader);
				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "1", reader);
				assertCharacterSetEvent(FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "2", 1452, 1549, reader);
				assertTokenSetDefinitionEvent(CharacterStateType.DISCRETE, "Standard", FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "2", reader);
				
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, reader);
				
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
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
	public void testReadingMatrixUnaligned() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixUnaligned.nex"), false, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);

			assertBasicOTUEvent(EventContentType.SEQUENCE, "taxon-1", null, reader);
			assertCharactersEvent("ACTAGGACTAGATCAAGTT", reader);
			assertSequenceEndEvent(true, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "taxon-2", null, reader);
			assertCharactersEvent("ACCAGGACTAGCGGATCAAG", reader);
			assertSequenceEndEvent(true, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "taxon-3", null, reader);
			assertCharactersEvent("ACCAGGACTAGATCAAG", reader);
			assertSequenceEndEvent(true, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "taxon-4", null, reader);
			assertCharactersEvent("AGCCAGGACTAGTTC", reader);
			assertSequenceEndEvent(true, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "taxon-5", null, reader);
			assertCharactersEvent("ATCAGGACTAGATCAAGTTC", reader);
			assertSequenceEndEvent(true, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
}
