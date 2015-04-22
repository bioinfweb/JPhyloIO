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
package info.bioinfweb.jphyloio.test;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.CharacterSetEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.SequenceCharactersEvent;


import static org.junit.Assert.*;



public class JPhyloIOTestTools {
	public static void assertCharactersEvent(String expectedName, String expectedSequence, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.SEQUENCE_CHARACTERS, event.getEventType());
		SequenceCharactersEvent charEvent = event.asSequenceCharactersEvent();
		assertEquals(expectedName, charEvent.getSequenceName());
		assertEquals(expectedSequence.length(), charEvent.getCharacterValues().size());
		for (int i = 0; i < expectedSequence.length(); i++) {
			assertEquals(expectedSequence.substring(i, i + 1), charEvent.getCharacterValues().get(i));
		}
	}
	
	
	public static void assertCharactersEvent(String expectedName, String[] expectedSequence, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.SEQUENCE_CHARACTERS, event.getEventType());
		SequenceCharactersEvent charEvent = event.asSequenceCharactersEvent();
		assertEquals(expectedName, charEvent.getSequenceName());
		assertEquals(expectedSequence.length, charEvent.getCharacterValues().size());
		for (int i = 0; i < expectedSequence.length; i++) {
			assertEquals(expectedSequence[i], charEvent.getCharacterValues().get(i));
		}
	}
	
	
  public static void assertCommentEvent(String expectedContent, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.COMMENT, event.getEventType());
		assertEquals(expectedContent, event.asCommentEvent().getContent());
  }  
	
	
  public static void assertMetaInformationEvent(String expectedKey, String expectedValue, 
  		JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.META_INFORMATION, event.getEventType());
		
		MetaInformationEvent metaInformationEvent = event.asMetaInformationEvent();
		assertEquals(expectedKey, metaInformationEvent.getKey());
		assertEquals(expectedValue, metaInformationEvent.getValue());
  }  
	
	
  public static void assertCharacterSetEvent(String expectedName, long expectedStart, long expectedEnd, 
  		JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.CHARACTER_SET, event.getEventType());
		
		CharacterSetEvent charSetEvent = event.asCharacterSetEvent();
		assertEquals(expectedName, charSetEvent.getName());
		assertEquals(expectedStart, charSetEvent.getStart());
		assertEquals(expectedEnd, charSetEvent.getEnd());
  }
}
