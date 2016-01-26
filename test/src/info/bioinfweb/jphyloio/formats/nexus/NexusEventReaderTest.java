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
import info.bioinfweb.jphyloio.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.nexus.blockhandlers.NexusBlockHandlerMap;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandReaderFactory;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.characters.DimensionsReader;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.characters.FormatReader;

import java.io.File;
import java.io.IOException;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class NexusEventReaderTest {
	private NexusBlockHandlerMap handlerMap;
	private NexusCommandReaderFactory factory;
	
	
	public NexusEventReaderTest() {
		super();
		handlerMap = new NexusBlockHandlerMap();
		handlerMap.addJPhyloIOHandlers();
		factory = new NexusCommandReaderFactory();
		factory.addJPhyloIOReaders();
	}


	@Test
	public void testReadingCharSets() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSet.nex"), false, handlerMap, factory);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertCommentEvent("comment 1", reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set01", reader);
				assertCharacterSetEvent(3, 6, reader);
				assertCharacterSetEvent(9, 10, reader);
				assertCharacterSetEvent(12, 17, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertCommentEvent("comment 1a", reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set02", reader);
				assertCharacterSetEvent(4, 10, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertCommentEvent("comment 2", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set03", reader);
				assertCommentEvent("comment 3", reader);
				assertCharacterSetEvent(4, 7, reader);
				assertCharacterSetEvent(12, 13, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set04", reader);
				assertCharacterSetEvent(4, 7, reader);
				assertCommentEvent("comment 4", reader);
				assertCharacterSetEvent(12, 13, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);

				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set05", reader);
				assertCharacterSetEvent(3, 4, reader);
				assertCharacterSetEvent(5, 7, reader);
				assertCommentEvent("comment 5", reader);
				assertCharacterSetEvent(7, 8, reader);
				assertCharacterSetEvent(9, 10, reader);
				assertCharacterSetEvent(11, 13, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);

				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set06", reader);
				assertCharacterSetEvent(0, 1, reader);
				assertCharacterSetEvent(2, 5, reader);
				assertCharacterSetEvent(6, 7, reader);
				assertCharacterSetEvent(8, 10, reader);
				assertCommentEvent("comment 6", reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set07", reader);
				assertCharacterSetEvent(0, 0, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set08", reader);
				assertCharacterSetEvent(0, 1, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set09", reader);
				assertCharacterSetEvent(1, 2, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set10", reader);
				assertCharacterSetEvent(0, 0, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set11", reader);
				assertCharacterSetEvent(12, 18, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set12", reader);
				assertCharacterSetEvent(3, 4, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);

				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set.13", reader);
				assertCharacterSetEvent(1, 2, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set.14", reader);
				assertCharacterSetEvent(1, 2, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set'15", reader);
				assertCharacterSetEvent(1, 2, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, "set 16;", reader);
				assertCharacterSetEvent(1, 2, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);

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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSetInvalidVectorSymbol.nex"), false, handlerMap, factory);
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
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Format.nex"), false, handlerMap, factory);
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", true, true, reader);
			assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
			assertMetaEvent(FormatReader.KEY_PREFIX + "transpose", "", true, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
			assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.END, reader);
			
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
	
	
	@Test
	public void testReadingFormatCommandTokens() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatTokens.nex"), false, handlerMap, factory);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "transpose", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "tokens", "", true, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatContinuous.nex"), false, handlerMap, factory);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "transpose", "", true, true, reader);

				assertTokenSetDefinitionEvent(CharacterStateType.CONTINUOUS, "Continuous", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
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
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Matrix.nex"), false, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);
			
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader);
			
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
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "7", null, new Long(7), true, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", idA, reader);
			assertCommentEvent("comment 1", reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", idB, reader);
			assertCommentEvent("comment 2", reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C", idC, reader);
			assertCharactersEvent("CG-T", reader);
			assertCommentEvent("comment 3", reader);
			assertCharactersEvent("C-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "D E", idDE, reader);
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
	public void testReadingMatrixLongTokens() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixLongTokens.nex"), false, handlerMap, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "3", null, new Long(3), true, true, reader);
				
				assertMetaEvent(FormatReader.KEY_PREFIX + "tokens", "", true, true, reader);

				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent(new String[]{"Eins", "Zwei", "Eins"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent(new String[]{"Zwei", "Zwei", "Eins"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C", null, reader);
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
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingMatrixLongContinuous() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixContinuous.nex"), false, handlerMap, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "3", null, new Long(3), true, true, reader);

				assertTokenSetDefinitionEvent(CharacterStateType.CONTINUOUS, "continuous", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent(new String[]{"18", "20", "2"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent(new String[]{"4.1", "-3.9", "2E10"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C", null, reader);
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
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingMatrixDNAAlternatives() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixDNAAlternatives.nex"), false, handlerMap, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "7", null, new Long(7), true, true, reader);

				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent(new String[]{"(CG)", "G", "G", "T", "C", "A", "T"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent(new String[]{"C", "G", "-", "T", "C", "{TA}", "T"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C", null, reader);
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
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingMatrixLongTokensAlternatives() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixLongTokensAlternatives.nex"), false, handlerMap, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "3", null, new Long(3), true, true, reader);
				
				assertMetaEvent(FormatReader.KEY_PREFIX + "tokens", "", true, true, reader);
				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCommentEvent("comment 1", reader);
				assertCharactersEvent(new String[]{"Eins", "(Zwei Drei)", "Eins"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCommentEvent("comment 2", reader);
				assertCharactersEvent(new String[]{"Zwei", "Zwei", "Eins"}, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);

				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C", null, reader);
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
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingMatrixInterleaved() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleaved.nex"), false, handlerMap, factory);
			reader.setCreateUnknownCommandEvents(false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
				assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "14", null, new Long(14), true, true, reader);

				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

				assertCommentEvent(" 0....5. ", reader);
				String idA = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCharactersEvent("CGGTCAT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				String idB = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCharactersEvent("CG-TCTT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				String idC = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C", null, reader);
				assertCharactersEvent("CG-TC-T", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertNotEquals(idA, idB);
				assertNotEquals(idA, idC);
				assertNotEquals(idB, idC);

				assertCommentEvent(" ...10.. ", reader);
				assertLinkedOTUEvent(EventContentType.SEQUENCE, idA, "A", null, reader);
				assertCharactersEvent("A-CGGAT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				assertLinkedOTUEvent(EventContentType.SEQUENCE, idB, "B", null, reader);
				assertCharactersEvent("ATCGCAT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				assertLinkedOTUEvent(EventContentType.SEQUENCE, idC, "C", null, reader);
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
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingMatrixInterleavedMatchCharacterSplit() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleavedMatchCharacter.nex"), true, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);
			reader.setMaxTokensToRead(4);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "14", null, new Long(14), true, true, reader);

			assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);

			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
			assertSingleTokenDefinitionEvent("i", CharacterStateMeaning.MATCH, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertCommentEvent(" 0....5. ", reader);
			String idA = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCharactersEvent("CGGT", reader);
			assertCharactersEvent("CAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idB = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCharactersEvent("CG-T", reader);
			assertCharactersEvent("CTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idC = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent("CG-T", reader);
			assertCharactersEvent("C-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idB, idC);

			assertCommentEvent(" ...10.. ", reader);
			assertLinkedOTUEvent(EventContentType.SEQUENCE, idA, "A", null, reader);
			assertCharactersEvent("A-CG", reader);
			assertCharactersEvent("GAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedOTUEvent(EventContentType.SEQUENCE, idB, "B", null, reader);
			assertCharactersEvent("ATCG", reader);
			assertCharactersEvent("CAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedOTUEvent(EventContentType.SEQUENCE, idC, "C", null, reader);
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
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixInterleavedMatchCharacter.nex"), true, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "3", null, new Long(3), true, true, reader);
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "14", null, new Long(14), true, true, reader);

			assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
			assertSingleTokenDefinitionEvent("i", CharacterStateMeaning.MATCH, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertCommentEvent(" 0....5. ", reader);
			String idA = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", null, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idB = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", null, reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String idC = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C", null, reader);
			assertCharactersEvent("CG-TC-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idB, idC);

			assertCommentEvent(" ...10.. ", reader);
			assertLinkedOTUEvent(EventContentType.SEQUENCE, idA, "A", null, reader);
			assertCharactersEvent("A-CGGAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedOTUEvent(EventContentType.SEQUENCE, idB, "B", null, reader);
			assertCharactersEvent("ATCGCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			assertLinkedOTUEvent(EventContentType.SEQUENCE, idC, "C", null, reader);
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/SplitComments.nex"), false, handlerMap, factory);
			reader.setMaxCommentLength(13);
			reader.setCreateUnknownCommandEvents(false);
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
	public void testReadingSymbols() {
		try {
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Symbols.nex"), false, handlerMap, factory);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "interleave", "", true, true, reader);
				assertMetaEvent(FormatReader.KEY_PREFIX + "transpose", "", true, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateType.DISCRETE, "Standard", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent(".", CharacterStateMeaning.MATCH, true, reader);
				assertSingleTokenDefinitionEvent("A", CharacterStateMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("B", CharacterStateMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("C", CharacterStateMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("D", CharacterStateMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("E", CharacterStateMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("F", CharacterStateMeaning.CHARACTER_STATE, true, reader);
				assertSingleTokenDefinitionEvent("G", CharacterStateMeaning.CHARACTER_STATE, true, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

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
	public void testReadingFormatSymbols() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatContinuousSymbols.nex"), false, handlerMap, factory);
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			try {
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				assertTokenSetDefinitionEvent(CharacterStateType.CONTINUOUS, "Continuous", reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);  // Parsing is one event ahead
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
		NexusEventReader reader = new NexusEventReader(new File(file), false, handlerMap, factory);
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "1", reader);
			assertCharacterSetEvent(1, 1452, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertLabeledIDEvent(EventContentType.CHARACTER_SET, null, FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "2", reader);
			assertCharacterSetEvent(1452, 1549, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "1", reader);
			if (testSingleDefs) {
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
			}
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateType.DISCRETE, "Standard", FormatReader.DATA_TYPE_CHARACTER_SET_NAME_PREFIX + "2", reader);
			if (testSingleDefs) {
				assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
				assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
			}
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
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MatrixUnaligned.nex"), false, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "taxon-1", null, reader);
			assertCharactersEvent("ACTAGGACTAGATCAAGTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "taxon-2", null, reader);
			assertCharactersEvent("ACCAGGACTAGCGGATCAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "taxon-3", null, reader);
			assertCharactersEvent("ACCAGGACTAGATCAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "taxon-4", null, reader);
			assertCharactersEvent("AGCCAGGACTAGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "taxon-5", null, reader);
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
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TaxaNoLabels.nex"), false, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			assertCommentEvent("comment 1", reader);
			assertCommentEvent("comment 2", reader);
			assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader);
			
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
			
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "5", null, new Long(5), true, true, reader);
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "7", null, new Long(7), true, true, reader);
			
			assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", null, "", true, true, reader);
			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", idA, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", idB, reader);
			assertCharactersEvent("CG-TCTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "C C", idC, reader);
			assertCharactersEvent("CG-TC-T", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "D'", idD, reader);
			assertCharactersEvent("CGTCATG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "E", idE, reader);
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
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TaxaNoLabelsInvalid.nex"), false, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader);
			
			String idA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String idB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			
			assertNotEquals(idA, idB);
			assertEndEvent(EventContentType.OTU_LIST, reader);
			
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "ntax", "5", null, new Long(5), true, true, reader);
			assertMetaEvent(DimensionsReader.KEY_PREFIX + "nchar", "7", null, new Long(7), true, true, reader);
			
			assertMetaEvent(FormatReader.KEY_PREFIX + "nolabels", "", null, "", true, true, reader);
			assertTokenSetDefinitionEvent(CharacterStateType.DNA, "DNA", reader);
			assertSingleTokenDefinitionEvent("?", CharacterStateMeaning.MISSING, true, reader);
			assertSingleTokenDefinitionEvent("-", CharacterStateMeaning.GAP, true, reader);
			assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);

			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "A", idA, reader);
			assertCharactersEvent("CGGTCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "B", idB, reader);
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
		
		String nodeIDScarabaeus = assertLinkedOTUEvent(EventContentType.NODE, null, "Scarabaeus", otuIDScarabaeus, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		String nodeIDDrosophila = assertLinkedOTUEvent(EventContentType.NODE, null, "Drosophila", otuIDDrosophila, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		
		String nodeIDN1 = assertLinkedOTUEvent(EventContentType.NODE, null, null, null, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		assertEdgeEvent(nodeIDN1, nodeIDScarabaeus, reader);
		assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
		assertEdgeEvent(nodeIDN1, nodeIDDrosophila, reader);
		assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
		
		String nodeIDAranaeus = assertLinkedOTUEvent(EventContentType.NODE, null, "Aranaeus", otuIDAranaeus, reader);
		assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
		String nodeIDN2 = assertLinkedOTUEvent(EventContentType.NODE, null, null, null, reader);
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
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TreesTranslate.nex"), false, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader);
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
			
			testReadingTreesTranslateSingleTree(reader, "tree1", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			testReadingTreesTranslateSingleTree(reader, "tree2", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			testReadingTreesTranslateSingleTree(reader, "tree3", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}


	@Test
	public void testReadingTreesNumericName() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TreesNumericName.nex"), false, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader);
			String otuIDScarabaeus = assertLabeledIDEvent(EventContentType.OTU, null, "Scarabaeus bug", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDDrosophila = assertLabeledIDEvent(EventContentType.OTU, null, "Drosophila", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);

			assertCommentEvent("comment 1", reader);
			assertLabeledIDEvent(EventContentType.TREE, null, "my tree", reader);
			
			String nodeIDScarabaeus = assertLinkedOTUEvent(EventContentType.NODE, null, "Scarabaeus bug", otuIDScarabaeus, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String nodeIDDrosophila = assertLinkedOTUEvent(EventContentType.NODE, null, "Drosophila", otuIDDrosophila, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String nodeIDN1 = assertLinkedOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN1, nodeIDScarabaeus, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN1, nodeIDDrosophila, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String nodeIDAranaeus = assertLinkedOTUEvent(EventContentType.NODE, null, "3", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String nodeIDN2 = assertLinkedOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			assertEdgeEvent(nodeIDN2, nodeIDN1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN2, nodeIDAranaeus, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, nodeIDN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);

			assertCommentEvent("comment 2", reader);
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}


	@Test
	public void testReadingTreesMetadata() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/TreesMetadata.nex"), false, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader);
			String otuIDA = assertLabeledIDEvent(EventContentType.OTU, null, "A", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDB = assertLabeledIDEvent(EventContentType.OTU, null, "B", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			String otuIDC = assertLabeledIDEvent(EventContentType.OTU, null, "C", reader).getID();
			assertEndEvent(EventContentType.OTU, reader);
			assertEndEvent(EventContentType.OTU_LIST, reader);

			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			assertMetaEvent(ReadWriteConstants.META_KEY_DISPLAY_TREE_ROOTED, "false", null, new Boolean(false), true, true, reader); 
			
			String idA = assertLinkedOTUEvent(EventContentType.NODE, null, "A", otuIDA, reader);
			assertMetaEvent("prob", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_stddev", "0", null, new Double(0.0), true, true, reader);
			assertMetaEvent("prob_range", null, null, null, false, true, reader);
			assertMetaEvent("prob_range[0]", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_range[1]", "1", null, new Double(1.0), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertMetaEvent("prob(percent)", "100", null, "100", true, true, reader);
			assertMetaEvent("prob+-sd", "100+-0", null, "100+-0", true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idB = assertLinkedOTUEvent(EventContentType.NODE, null, "B", otuIDB, reader);
			assertMetaEvent("prob", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_stddev", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("prob_range", null, null, null, false, true, reader);
			assertMetaEvent("prob_range[0]", "AB C", null, "AB C", true, true, reader);
			assertMetaEvent("prob_range[1]", "ABC", null, "ABC", true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertMetaEvent("prob(percent)", "100", null, "100", true, true, reader);
			assertMetaEvent("prob+-sd", "100+-0", null, "100+-0", true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idC = assertLinkedOTUEvent(EventContentType.NODE, null, "C", otuIDC, reader);
			assertMetaEvent("prob", "6.364056912805381e-001", null, new Double(6.364056912805381e-001), true, true, reader);
			assertMetaEvent("prob_stddev", "7.249475639180907e-004", null, new Double(7.249475639180907e-004), true, true, reader);	
			assertMetaEvent("prob_range", null, null, null, false, true, reader);
			assertMetaEvent("prob_range[0]", "6.358930759420870e-001", null, new Double(6.358930759420870e-001), true, true, reader);
			assertMetaEvent("prob_range[1]", "6.369183066189893e-001", null, new Double(6.369183066189893e-001), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertMetaEvent("prob(percent)", "64", null, "64", true, true, reader);
			assertMetaEvent("prob+-sd", "64+-0", null, "64+-0", true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			
			String idN1 = assertLinkedOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertMetaEvent("prob", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_stddev", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("prob_range", null, null, null, false, true, reader);
			assertMetaEvent("prob_range[0]", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_range[1]", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertMetaEvent("prob(percent)", "100", null, "100", true, true, reader);
			assertMetaEvent("prob+-sd", "100+-0", null, "100+-0", true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN1, idB, 6.244293083853111e-001, reader);
			assertMetaEvent("length_mean", "6.345415111023917e-001", null, new Double(6.345415111023917e-001), true, true, reader);
			assertMetaEvent("length_median", "6.244293083853111e-001", null, new Double(6.244293083853111e-001), true, true, reader);
			assertMetaEvent("length_95%HPD", null, null, null, false, true, reader);
			assertMetaEvent("length_95%HPD[0]", "4.360295861156825e-001", null, new Double(4.360295861156825e-001), true, true, reader);
			assertMetaEvent("length_95%HPD[1]", "8.441623753050405e-001", null, new Double(8.441623753050405e-001), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN1, idC, 7.039004236028111e-002, reader);
			assertMetaEvent("length_mean", "7.419012044002400e-002", null, new Double(7.419012044002400e-002), true, true, reader);
			assertMetaEvent("length_median", "7.039004236028111e-002", null, new Double(7.039004236028111e-002), true, true, reader);
			assertMetaEvent("length_95%HPD", null, null, null, false, true, reader);
			assertMetaEvent("length_95%HPD[0]", "9.114712766459516e-003", null, new Double(9.114712766459516e-003), true, true, reader);
			assertMetaEvent("length_95%HPD[1]", "1.418351647155842e-001", null, new Double(1.418351647155842e-001), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertEndEvent(EventContentType.EDGE, reader);

			String idN2 = assertLinkedOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertCommentEvent("18", false, reader);
			//assertMetaEvent(HotCommentDataReader.UNNAMED_NODE_DATA_NAME, "18", null, new Double(18), true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN2, idA, .3682008685714568, reader);
			assertMetaEvent("length_mean", "3.744759260623280e-001", null, new Double(3.744759260623280e-001), true, true, reader);
			assertMetaEvent("length_median", "3.682008685714568e-001", null, new Double(3.682008685714568e-001), true, true, reader);
			assertMetaEvent("length_95%HPD", null, null, null, false, true, reader);
			assertMetaEvent("length_95%HPD[0]", "2.494893056441154e-001", null, new Double(2.494893056441154e-001), true, true, reader);
			assertMetaEvent("length_95%HPD[1]", "5.088322191162278e-001", null, new Double(5.088322191162278e-001), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN2, idN1, reader);
			assertMetaEvent("length_mean", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("length_median", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("length_95%HPD", null, null, null, false, true, reader);
			assertMetaEvent("length_95%HPD[0]", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("length_95%HPD[1]", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
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

			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void testReadingTreesMultipleBlocks() throws Exception {
		NexusEventReader reader = new NexusEventReader(new File("data/Nexus/MultipleTreesBlocks.nex"), false, handlerMap, factory);
		try {
			reader.setCreateUnknownCommandEvents(false);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			assertLabeledIDEvent(EventContentType.OTU_LIST, null, null, reader);
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
			
			testReadingTreesTranslateSingleTree(reader, "tree1", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			testReadingTreesTranslateSingleTree(reader, "tree2", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			testReadingTreesTranslateSingleTree(reader, "tree3", otuIDScarabaeus, otuIDDrosophila, otuIDAranaeus);
			
			assertLabeledIDEvent(EventContentType.TREE, null, "otherTree", reader);
			
			String nodeIDBeetle = assertLinkedOTUEvent(EventContentType.NODE, null, "beetle", otuIDBeetle, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String nodeIDFly = assertLinkedOTUEvent(EventContentType.NODE, null, "fly", otuIDFly, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String nodeIDN1 = assertLinkedOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN1, nodeIDBeetle, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN1, nodeIDFly, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String nodeIDSpider = assertLinkedOTUEvent(EventContentType.NODE, null, "spider", otuIDSpider, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String nodeIDN2 = assertLinkedOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			assertEdgeEvent(nodeIDN2, nodeIDN1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(nodeIDN2, nodeIDSpider, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, nodeIDN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);

			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
}
