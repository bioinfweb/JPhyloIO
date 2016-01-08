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


import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.File;
import java.io.IOException;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertCharactersEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEventType;
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
				
				assertCharactersEvent("Seq 1", "ATG-T--CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T--CGG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATG-TTTCCG", reader);
				assertCharactersEvent("Seq 5", "ATG-TT-CCC", reader);
				
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
				
				assertCharactersEvent("Seq 1", "ATG-T--CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T--CGG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATG-TTTCCG", reader);
				assertCharactersEvent("Seq 5", "ATG-TT-CCC", reader);
				
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
				
				assertCharactersEvent("Seq 1", "ATG-T-", reader);
				assertCharactersEvent("Seq 1", "-CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-TT", reader);
				assertCharactersEvent("Seq 2", "-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T-", reader);
				assertCharactersEvent("Seq 3", "-CGG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATG-TT", reader);
				assertCharactersEvent("Seq 4ATCGA", "TCCG", reader);
				assertCharactersEvent("Seq 5", "ATG-TT", reader);
				assertCharactersEvent("Seq 5", "-CCC", reader);
				
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
				
				assertCharactersEvent("Seq 1", "ATG-T--CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T--CGG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATG-TTTCCG", reader);
				assertCharactersEvent("Seq 5", "ATG-TT-CCC", reader);
				
				assertCharactersEvent("Seq 1", "CCGT-GT--A", reader);
				assertCharactersEvent("Seq 2", "CCGT-GTT-A", reader);
				assertCharactersEvent("Seq 3", "CCGT-CT--A", reader);
				assertCharactersEvent("Seq 4ATCGA", "CCGT-GTTTA", reader);
				assertCharactersEvent("Seq 5", "CGGT-CTT-A", reader);
				
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
				
				assertCharactersEvent("Seq 1", "ATG-T--CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T--CGG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATG-TTTCCG", reader);
				assertCharactersEvent("Seq 5", "ATG-TT-CCC", reader);
				
				assertCharactersEvent("Seq 1", "CCGT-GT--A", reader);
				assertCharactersEvent("Seq 2", "CCGT-GTT-A", reader);
				assertCharactersEvent("Seq 3", "CCGT-CT--A", reader);
				assertCharactersEvent("Seq 4ATCGA", "CCGT-GTTTA", reader);
				assertCharactersEvent("Seq 5", "CGGT-CTT-A", reader);
				
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
				
				assertCharactersEvent("Seq 1", "ATG-T-", reader);
				assertCharactersEvent("Seq 1", "-CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-TT", reader);
				assertCharactersEvent("Seq 2", "-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T-", reader);
				assertCharactersEvent("Seq 3", "-CGG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATG-TT", reader);
				assertCharactersEvent("Seq 4ATCGA", "TCCG", reader);
				assertCharactersEvent("Seq 5", "ATG-TT", reader);
				assertCharactersEvent("Seq 5", "-CCC", reader);
				
				assertCharactersEvent("Seq 1", "CCGT-G", reader);
				assertCharactersEvent("Seq 1", "T--A", reader);
				assertCharactersEvent("Seq 2", "CCGT-G", reader);
				assertCharactersEvent("Seq 2", "TT-A", reader);
				assertCharactersEvent("Seq 3", "CCGT-C", reader);
				assertCharactersEvent("Seq 3", "T--A", reader);
				assertCharactersEvent("Seq 4ATCGA", "CCGT-G", reader);
				assertCharactersEvent("Seq 4ATCGA", "TTTA", reader);
				assertCharactersEvent("Seq 5", "CGGT-C", reader);
				assertCharactersEvent("Seq 5", "TT-A", reader);
				
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
	public void testReadingInterleavedHalfLength() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/Interleaved.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(5);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertCharactersEvent("Seq 1", "ATG-T", reader);
				assertCharactersEvent("Seq 1", "--CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-T", reader);
				assertCharactersEvent("Seq 2", "T-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T", reader);
				assertCharactersEvent("Seq 3", "--CGG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATG-T", reader);
				assertCharactersEvent("Seq 4ATCGA", "TTCCG", reader);
				assertCharactersEvent("Seq 5", "ATG-T", reader);
				assertCharactersEvent("Seq 5", "T-CCC", reader);
				
				assertCharactersEvent("Seq 1", "CCGT-", reader);
				assertCharactersEvent("Seq 1", "GT--A", reader);
				assertCharactersEvent("Seq 2", "CCGT-", reader);
				assertCharactersEvent("Seq 2", "GTT-A", reader);
				assertCharactersEvent("Seq 3", "CCGT-", reader);
				assertCharactersEvent("Seq 3", "CT--A", reader);
				assertCharactersEvent("Seq 4ATCGA", "CCGT-", reader);
				assertCharactersEvent("Seq 4ATCGA", "GTTTA", reader);
				assertCharactersEvent("Seq 5", "CGGT-", reader);
				assertCharactersEvent("Seq 5", "CTT-A", reader);
				
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
	public void testReadingInterleaved3Blocks() {
		try {
			PhylipEventReader reader = new PhylipEventReader(new File("data/Phylip/Interleaved3Blocks.phy"), false, true, false);
			try {
				reader.setMaxTokensToRead(20);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertCharactersEvent("Seq 1", "ATG-T--CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T--CGG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATG-TTTCCG", reader);
				assertCharactersEvent("Seq 5", "ATG-TT-CCC", reader);
				
				assertCharactersEvent("Seq 1", "CCGT-GT--A", reader);
				assertCharactersEvent("Seq 2", "CCGT-GTT-A", reader);
				assertCharactersEvent("Seq 3", "CCGT-CT--A", reader);
				assertCharactersEvent("Seq 4ATCGA", "CCGT-GTTTA", reader);
				assertCharactersEvent("Seq 5", "CGGT-CTT-A", reader);
				
				assertCharactersEvent("Seq 1", "ATA-G", reader);
				assertCharactersEvent("Seq 2", "ATT-G", reader);
				assertCharactersEvent("Seq 3", "ATTTG", reader);
				assertCharactersEvent("Seq 4ATCGA", "ATTAG", reader);
				assertCharactersEvent("Seq 5", "TTT-G", reader);
				
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
				
				assertCharactersEvent("Sequence_name_1", "ATG-T--CCG", reader);
				assertCharactersEvent("Sequence_name_2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Sequence_name_3", "ATG-T--CGG", reader);
				assertCharactersEvent("Sequence_name_4", "ATG-TTTCCG", reader);
				assertCharactersEvent("Sequence_name_5", "ATG-TT-CCC", reader);
				
				assertCharactersEvent("Sequence_name_1", "CCGT-GT--A", reader);
				assertCharactersEvent("Sequence_name_2", "CCGT-GTT-A", reader);
				assertCharactersEvent("Sequence_name_3", "CCGT-CT--A", reader);
				assertCharactersEvent("Sequence_name_4", "CCGT-GTTTA", reader);
				assertCharactersEvent("Sequence_name_5", "CGGT-CTT-A", reader);
				
				assertCharactersEvent("Sequence_name_1", "ATA-G", reader);
				assertCharactersEvent("Sequence_name_2", "ATT-G", reader);
				assertCharactersEvent("Sequence_name_3", "ATTTG", reader);
				assertCharactersEvent("Sequence_name_4", "ATTAG", reader);
				assertCharactersEvent("Sequence_name_5", "TTT-G", reader);
				
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
				
				assertCharactersEvent("Seq 1", "ATG-T--CCG", reader);
				assertCharactersEvent("Seq 2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Seq 3", "ATG-T--CGG", reader);
				
				assertCharactersEvent("Seq 1", "CCGT-GT--A", reader);
				assertCharactersEvent("Seq 2", "CCGT-GTT-A", reader);
				assertCharactersEvent("Seq 3", "CCGT-CT--A", reader);
				
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
