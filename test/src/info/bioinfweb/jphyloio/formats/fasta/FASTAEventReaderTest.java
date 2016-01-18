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
package info.bioinfweb.jphyloio.formats.fasta;


import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class FASTAEventReaderTest {
	@Test
	public void testReadingFasta() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/Test.fasta"), false);
			try {
				reader.setMaxTokensToRead(6);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 1", null, reader);
				assertCharactersEvent("ATCGT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 2", null, reader);
				assertCharactersEvent("CGT>AA", reader);
				assertCharactersEvent(">CG", reader);
				assertCharactersEvent("ACGT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Empty sequence", null, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 3", null, reader);
				assertCharactersEvent("GCCAT", reader);
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
	public void testnextOfType() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/Test.fasta"), false);
			try {
				reader.setMaxTokensToRead(20);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				Set<EventType> set = new HashSet<EventType>();
				set.add(new EventType(EventContentType.ALIGNMENT, EventTopologyType.END));
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, 
						reader.nextOfType(set));
				assertNull(reader.nextOfType(set));
				
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
	public void testReadingFastaMatchToken() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/MatchToken.fasta"), true);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 1", null, reader);
				assertCharactersEvent("ATCG-AG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 2", null, reader);
				assertCharactersEvent("ATGG-AG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 3", null, reader);
				assertCharactersEvent("AACGTAG", reader);
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
	public void testReadingFastaWithComments() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/Comment.fasta"), false);
			try {
				reader.setMaxCommentLength(16);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 1", null, reader);
				assertCommentEvent("comment 1", false, reader);
				assertCharactersEvent("ATCGT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 2", null, reader);
				assertCharactersEvent("CGT>AA>CG", reader);
				assertCharactersEvent("ACGT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Empty sequence", null, reader);
				assertCommentEvent(" comment 2 ", false, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				assertTrue(reader.hasNextEvent());
//				SequenceTokensEvent event = reader.next().asSequenceTokensEvent();
//				assertEquals("Empty sequence", event.getSequenceName());
//				assertEquals(0, event.getCharacterValues().size());
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 3", null, reader);
				assertCommentEvent("longer comment 0", true, reader);
				assertCommentEvent("123456789", false, reader);
				assertCommentEvent(" another comment", false, reader);
				assertCharactersEvent("GCCAT", reader);
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
	public void testReadingFastaWithIndices() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/Indices.fasta"), false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 1", null, reader);
				assertCharactersEvent("ATCGT", reader);
				assertCharactersEvent("TCGTA", reader);
				assertCharactersEvent("TCCTG", reader);
				assertCharactersEvent("TA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 2", null, reader);
				assertCharactersEvent("CGT>AA>CG", reader);
				assertCharactersEvent("ACGT", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Empty sequence", null, reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				assertTrue(reader.hasNextEvent());
//				SequenceTokensEvent event = reader.next().asSequenceTokensEvent();
//				assertEquals("Empty sequence", event.getSequenceName());
//				assertEquals(0, event.getCharacterValues().size());
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "Seq 3", null, reader);
				assertCharactersEvent("GCCAT", reader);
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
