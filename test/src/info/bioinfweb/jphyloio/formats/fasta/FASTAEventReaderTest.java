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
package info.bioinfweb.jphyloio.formats.fasta;


import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;

import java.io.File;
import java.util.EnumSet;

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
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertCharactersEvent("Seq 1", "ATCGT", reader);
				
				assertCharactersEvent("Seq 2", "CGT>AA", reader);
				assertCharactersEvent("Seq 2", ">CG", reader);
				assertCharactersEvent("Seq 2", "ACGT", reader);
				
				assertTrue(reader.hasNextEvent());
				SequenceTokensEvent event = reader.next().asSequenceTokensEvent();
				assertEquals("Empty sequence", event.getSequenceName());
				assertEquals(0, event.getCharacterValues().size());
				
				assertCharactersEvent("Seq 3", "GCCAT", reader);
				
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
	public void testnextOfType() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/Test.fasta"), false);
			try {
				reader.setMaxTokensToRead(20);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertEquals(EventType.ALIGNMENT_END, reader.nextOfType(EnumSet.of(EventType.ALIGNMENT_END)).getEventType());
				assertNull(reader.nextOfType(EnumSet.of(EventType.ALIGNMENT_END)));
				
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
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertCharactersEvent("Seq 1", "ATCG-AG", reader);
				assertCharactersEvent("Seq 2", "ATGG-AG", reader);
				assertCharactersEvent("Seq 3", "AACGTAG", reader);
				
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
	public void testReadingFastaWithComments() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/Comment.fasta"), false);
			try {
				reader.setMaxCommentLength(16);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertCommentEvent("comment 1", false, reader);
				assertCharactersEvent("Seq 1", "ATCGT", reader);
				
				assertCharactersEvent("Seq 2", "CGT>AA>CG", reader);
				assertCharactersEvent("Seq 2", "ACGT", reader);
				
				assertCommentEvent(" comment 2 ", false, reader);
				assertTrue(reader.hasNextEvent());
				SequenceTokensEvent event = reader.next().asSequenceTokensEvent();
				assertEquals("Empty sequence", event.getSequenceName());
				assertEquals(0, event.getCharacterValues().size());
				
				assertCommentEvent("longer comment 0", true, reader);
				assertCommentEvent("123456789", false, reader);
				assertCommentEvent(" another comment", false, reader);
				assertCharactersEvent("Seq 3", "GCCAT", reader);
				
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
	public void testReadingFastaWithIndices() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/Indices.fasta"), false);
			try {
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertCharactersEvent("Seq 1", "ATCGT", reader);
				assertCharactersEvent("Seq 1", "TCGTA", reader);
				assertCharactersEvent("Seq 1", "TCCTG", reader);
				assertCharactersEvent("Seq 1", "TA", reader);
				
				assertCharactersEvent("Seq 2", "CGT>AA>CG", reader);
				assertCharactersEvent("Seq 2", "ACGT", reader);
				
				assertTrue(reader.hasNextEvent());
				SequenceTokensEvent event = reader.next().asSequenceTokensEvent();
				assertEquals("Empty sequence", event.getSequenceName());
				assertEquals(0, event.getCharacterValues().size());
				
				assertCharactersEvent("Seq 3", "GCCAT", reader);
				
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
