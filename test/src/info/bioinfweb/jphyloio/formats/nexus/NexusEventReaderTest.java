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
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSet.nex"), factory);
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/CharSetInvalidVectorSymbol.nex"), factory);
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Format.nex"), factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "DATATYPE", "DNA", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "MISSING", "?", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "GAP", "-", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "NOLABELS", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "INTERLEAVE", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "TRANSPOSE", "", reader);
				
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatTokens.nex"), factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "DATATYPE", "DNA", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "MISSING", "?", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "GAP", "-", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "NOLABELS", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "INTERLEAVE", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "TRANSPOSE", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "TOKENS", "", reader);
				
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/FormatContinuous.nex"), factory);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "DATATYPE", "Continuous", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "MISSING", "?", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "GAP", "-", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "NOLABELS", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "INTERLEAVE", "", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "TRANSPOSE", "", reader);
				
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
			NexusEventReader reader = new NexusEventReader(new File("data/Nexus/Matrix.nex"), factory);
			reader.setMetaEventsForUnknownCommands(false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "DATATYPE", "DNA", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "MISSING", "?", reader);
				assertMetaInformationEvent(FormatReader.KEY_PREFIX + "GAP", "-", reader);

				assertCharactersEvent("A", "CGGTCAT", reader);
				assertCharactersEvent("B", "CG-TCTT", reader);
				assertCharactersEvent("C", "CG-TC-T", reader);
				
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