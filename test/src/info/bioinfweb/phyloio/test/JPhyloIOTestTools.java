/*
 * PhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
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
package info.bioinfweb.phyloio.test;


import info.bioinfweb.phyloio.PhyloIOEventReader;
import info.bioinfweb.phyloio.events.EventType;
import info.bioinfweb.phyloio.events.PhyloIOEvent;
import info.bioinfweb.phyloio.events.SequenceCharactersEvent;


import static org.junit.Assert.*;



public class JPhyloIOTestTools {
	public static void assertCharactersEvent(String expectedName, String expectedSequence, PhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		PhyloIOEvent event = reader.next();
		assertEquals(EventType.SEQUENCE_CHARACTERS, event.getEventType());
		SequenceCharactersEvent charEvent = event.asCharactersEvent();
		assertEquals(expectedName, charEvent.getSequenceName());
		assertEquals(expectedSequence.length(), charEvent.getCharacterValues().size());
		for (int i = 0; i < expectedSequence.length(); i++) {
			assertEquals(expectedSequence.substring(i, i + 1), charEvent.getCharacterValues().get(i));
		}
	}
}
