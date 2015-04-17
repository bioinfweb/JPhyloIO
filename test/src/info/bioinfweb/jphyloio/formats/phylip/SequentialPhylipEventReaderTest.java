/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
 * <http://bioinfweb.info/PhyloIO>
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


import info.bioinfweb.jphyloio.events.EventType;

import java.io.File;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertCharactersEvent;
import static org.junit.Assert.* ;



public class SequentialPhylipEventReaderTest {
	@Test
	public void testReadingLongerLength() {
		try {
			SequentialPhylipEventReader reader = new SequentialPhylipEventReader(new File("data/Phylip/Sequential.phy"), false);
			try {
				reader.setMaxTokensToRead(20);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertCharactersEvent("Seq 1", "ATG-T--CCG", reader);
				assertCharactersEvent("Seq 1", "CCGT-GT--A", reader);
				
				assertCharactersEvent("Seq 2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Seq 2", "CCGT-GTT-A", reader);
				
				assertCharactersEvent("Seq 3", "ATG-T--CGG", reader);
				assertCharactersEvent("Seq 3", "CCGT-CT--A", reader);
				
				assertCharactersEvent("Seq 4ATCGA", "ATG-TTTCCG", reader);
				assertCharactersEvent("Seq 4ATCGA", "CCGT-GTTTA", reader);
				
				assertCharactersEvent("Seq 5", "ATG-TT-CCC", reader);
				assertCharactersEvent("Seq 5", "CGGT-CTT-A", reader);
				
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
	public void testReadingRelaxedLongerLength() {
		try {
			SequentialPhylipEventReader reader = new SequentialPhylipEventReader(new File("data/Phylip/RelaxedSequential.phy"), true);
			try {
				reader.setMaxTokensToRead(20);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertCharactersEvent("Sequence_name_1", "ATG-T--CCG", reader);
				assertCharactersEvent("Sequence_name_1", "CCGT-GT--A", reader);
				
				assertCharactersEvent("Sequence_name_2", "ATG-TT-CCG", reader);
				assertCharactersEvent("Sequence_name_2", "CCGT-GTT-A", reader);
				
				assertCharactersEvent("Sequence_name_3", "ATG-T--CGG", reader);
				assertCharactersEvent("Sequence_name_3", "CCGT-CT--A", reader);
				
				assertCharactersEvent("Sequence_name_4", "ATG-TTTCCG", reader);
				assertCharactersEvent("Sequence_name_4", "CCGT-GTTTA", reader);
				
				assertCharactersEvent("Sequence_name_5", "ATG-TT-CCC", reader);
				assertCharactersEvent("Sequence_name_5", "CGGT-CTT-A", reader);
				
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
