/*
 * LibrAlign - A GUI library for displaying and editing multiple sequence alignments and attached data
 * Copyright (C) 2014-2015  Ben Stöver
 * <http://bioinfweb.info/LibrAlign>
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
package info.bioinfweb.phyloio.formats.fasta;


import info.bioinfweb.phyloio.events.EventType;
import info.bioinfweb.phyloio.events.TokensEvent;

import java.io.File;

import org.junit.* ;

import static org.junit.Assert.* ;



public class FASTAEventReaderTest {
	private void assertTokensEvent(String expectedSequence, TokensEvent event) {
		assertEquals(expectedSequence.length(), event.getTokens().size());
		for (int i = 0; i < expectedSequence.length(); i++) {
			assertEquals(expectedSequence.substring(i, i + 1), event.getTokens().get(i));
		}
	}
	
	
	@Test
	public void testReadingFasta() {
		try {
			FASTAEventReader reader = new FASTAEventReader(new File("data/Simple.fasta"));
			try {
				reader.setMaxTokensToRead(6);
				
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.ALIGNMENT_START, reader.next().getEventType());
				
				assertTrue(reader.hasNextEvent());
				assertEquals("Seq 1", reader.next().asSequenceStartEvent().getName());
				assertTrue(reader.hasNextEvent());
				assertTokensEvent("ATCGT", reader.next().asTokensEvent());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.SEQUENCE_END, reader.next().getEventType());
				
				assertTrue(reader.hasNextEvent());
				assertEquals("Seq 2", reader.next().asSequenceStartEvent().getName());
				assertTrue(reader.hasNextEvent());
				assertTokensEvent("CGTAAA", reader.next().asTokensEvent());
				assertTrue(reader.hasNextEvent());
				assertTokensEvent("CG", reader.next().asTokensEvent());
				assertTrue(reader.hasNextEvent());
				assertTokensEvent("ACGT", reader.next().asTokensEvent());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.SEQUENCE_END, reader.next().getEventType());
				
				assertTrue(reader.hasNextEvent());
				assertEquals("Seq 3", reader.next().asSequenceStartEvent().getName());
				assertTrue(reader.hasNextEvent());
				assertTokensEvent("GCCAT", reader.next().asTokensEvent());
				assertTrue(reader.hasNextEvent());
				assertEquals(EventType.SEQUENCE_END, reader.next().getEventType());
				
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
