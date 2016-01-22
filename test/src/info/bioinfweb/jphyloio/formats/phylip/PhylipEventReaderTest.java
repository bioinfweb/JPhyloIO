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
package info.bioinfweb.jphyloio.formats.phylip;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.File;
import java.io.IOException;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class PhylipEventReaderTest {
	@Test
	public void testReadingNonInterleavedExactLength() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/NonInterleaved.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(10);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "10", null, new Long(10), true, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("ATG-TT-CCC", reader);
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
	public void testReadingNonInterleavedLongerLength() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/NonInterleaved.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(20);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "10", null, new Long(10), true, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("ATG-TT-CCC", reader);
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
	public void testReadingNonInterleavedShorterLength() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/NonInterleaved.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(6);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "10", null, new Long(10), true, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T-", reader);
				assertCharactersEvent("-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT", reader);
				assertCharactersEvent("-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T-", reader);
				assertCharactersEvent("-CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TT", reader);
				assertCharactersEvent("TCCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("ATG-TT", reader);
				assertCharactersEvent("-CCC", reader);
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
	
	
	private void testInvalidCount(String fileName, String errorMessage) {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/" + fileName), false, true, false);
			try {
				//assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());  // To allow peek() the first two events are generated here already.
				reader.next();
				fail("No excpetion");
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			assertTrue(e instanceof IOException);
			assertEquals(errorMessage, e.getMessage());
		}
	}
	
	
	@Test
	public void testInvalidCounts() {
		testInvalidCount("InvalidSequenceCount.phy", "Invalid integer constant \"A\" found for the sequence count in line 1.");
		testInvalidCount("InvalidCharacterCount.phy", "Invalid integer constant \"A\" found for the character count in line 1.");
		testInvalidCount("MissingCharacterCount.phy", "The first line of a Phylip file needs to contain exactly two integer values "
				+ "spcifying the sequence and character count. 1 value(s) was/were found instead.");
		testInvalidCount("TooManyCounts.phy", "The first line of a Phylip file needs to contain exactly two integer values spcifying "
				+ "the sequence and character count. 3 value(s) was/were found instead.");
	}
	
	
	@Test
	public void testReadingInterleavedExactLength() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/Interleaved.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(10);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "20", null, new Long(20), true, true, reader);
				
				String id1 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id2 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id3 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id4 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id5 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("ATG-TT-CCC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id1, "Seq 1", null, reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id2, "Seq 2", null, reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id3, "Seq 3", null, reader);
				assertCharactersEvent("CCGT-CT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id4, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("CCGT-GTTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id5, "Seq 5", null, reader);
				assertCharactersEvent("CGGT-CTT-A", reader);
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
	public void testReadingInterleavedLongerLength() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/Interleaved.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(20);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "20", null, new Long(20), true, true, reader);
				
				String id1 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id2 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id3 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id4 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id5 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("ATG-TT-CCC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id1, "Seq 1", null, reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id2, "Seq 2", null, reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id3, "Seq 3", null, reader);
				assertCharactersEvent("CCGT-CT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id4, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("CCGT-GTTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id5, "Seq 5", null, reader);
				assertCharactersEvent("CGGT-CTT-A", reader);
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
	public void testReadingInterleavedShorterLength() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/Interleaved.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(6);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "20", null, new Long(20), true, true, reader);
				
				String id1 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T-", reader);
				assertCharactersEvent("-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id2 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT", reader);
				assertCharactersEvent("-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id3 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T-", reader);
				assertCharactersEvent("-CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id4 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TT", reader);
				assertCharactersEvent("TCCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				String id5 = assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("ATG-TT", reader);
				assertCharactersEvent("-CCC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id1, "Seq 1", null, reader);
				assertCharactersEvent("CCGT-G", reader);
				assertCharactersEvent("T--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id2, "Seq 2", null, reader);
				assertCharactersEvent("CCGT-G", reader);
				assertCharactersEvent("TT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id3, "Seq 3", null, reader);
				assertCharactersEvent("CCGT-C", reader);
				assertCharactersEvent("T--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id4, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("CCGT-G", reader);
				assertCharactersEvent("TTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, id5, "Seq 5", null, reader);
				assertCharactersEvent("CGGT-C", reader);
				assertCharactersEvent("TT-A", reader);
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
	public void testReadingInterleavedHalfLength() throws Exception {
		PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/Interleaved.phy"), false, true, false);
		try {
			reader.setMaxTokensToRead(5);
			
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);

			assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
			assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "20", null, new Long(20), true, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
			assertCharactersEvent("ATG-T", reader);
			assertCharactersEvent("--CCG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
			assertCharactersEvent("ATG-T", reader);
			assertCharactersEvent("T-CCG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
			assertCharactersEvent("ATG-T", reader);
			assertCharactersEvent("--CGG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
			assertCharactersEvent("ATG-T", reader);
			assertCharactersEvent("TTCCG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
			assertCharactersEvent("ATG-T", reader);
			assertCharactersEvent("T-CCC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
			assertCharactersEvent("CCGT-", reader);
			assertCharactersEvent("GT--A", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
			assertCharactersEvent("CCGT-", reader);
			assertCharactersEvent("GTT-A", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
			assertCharactersEvent("CCGT-", reader);
			assertCharactersEvent("CT--A", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
			assertCharactersEvent("CCGT-", reader);
			assertCharactersEvent("GTTTA", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
			
			assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
			assertCharactersEvent("CGGT-", reader);
			assertCharactersEvent("CTT-A", reader);
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
	public void testReadingInterleaved3Blocks() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/Interleaved3Blocks.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(20);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "25", null, new Long(25), true, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("ATG-TT-CCC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("CCGT-CT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("CCGT-GTTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("CGGT-CTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
								
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATA-G", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATT-G", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATTTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATTAG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("TTT-G", reader);
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
	public void testReadingRelaxedInterleaved3Blocks() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/RelaxedInterleaved3Blocks.phy"), false, true, true);
			try {
				reader.setMaxTokensToRead(20);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "25", null, new Long(25), true, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_4", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_5", null, reader);
				assertCharactersEvent("ATG-TT-CCC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_1", null, reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_2", null, reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_3", null, reader);
				assertCharactersEvent("CCGT-CT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_4", null, reader);
				assertCharactersEvent("CCGT-GTTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_5", null, reader);
				assertCharactersEvent("CGGT-CTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
								
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_1", null, reader);
				assertCharactersEvent("ATA-G", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_2", null, reader);
				assertCharactersEvent("ATT-G", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_3", null, reader);
				assertCharactersEvent("ATTTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_4", null, reader);
				assertCharactersEvent("ATTAG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Sequence_name_5", null, reader);
				assertCharactersEvent("TTT-G", reader);
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
	public void testReadingInterleavedMatchCharacter() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/InterleavedMatchCharacter.phy"), true, true, false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "3", null, new Long(3), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "20", null, new Long(20), true, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("CCGT-CT--A", reader);
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
}
