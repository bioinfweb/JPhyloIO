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
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;


import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import static org.junit.Assert.*;



public class JPhyloIOTestTools {
	public static void assertCharactersEvent(String expectedName, String expectedSequence, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.SEQUENCE_CHARACTERS, event.getEventType());
		SequenceTokensEvent tokensEvent = event.asSequenceTokensEvent();
		assertEquals(expectedName, tokensEvent.getSequenceName());
		assertEquals(expectedSequence.length(), tokensEvent.getCharacterValues().size());
		for (int i = 0; i < expectedSequence.length(); i++) {
			assertEquals(expectedSequence.substring(i, i + 1), tokensEvent.getCharacterValues().get(i));
		}
	}
	
	
	public static void assertCharactersEvent(String expectedName, String[] expectedSequence, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.SEQUENCE_CHARACTERS, event.getEventType());
		SequenceTokensEvent tokensEvent = event.asSequenceTokensEvent();
		assertEquals(expectedName, tokensEvent.getSequenceName());
		assertEquals(expectedSequence.length, tokensEvent.getCharacterValues().size());
		for (int i = 0; i < expectedSequence.length; i++) {
			assertEquals(expectedSequence[i], tokensEvent.getCharacterValues().get(i));
		}
	}
	
	
  public static void assertCommentEvent(String expectedContent, JPhyloIOEventReader reader) throws Exception {
  	assertCommentEvent(expectedContent, true, reader);  	
  }
  
  
  public static void assertCommentEvent(String expectedContent, boolean expectedContinuedInNextEvent, 
  		JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.COMMENT, event.getEventType());
		CommentEvent commentEvent = event.asCommentEvent();
		assertEquals(expectedContent, commentEvent.getContent());
		assertEquals(expectedContinuedInNextEvent, commentEvent.isContinuedInNextEvent());
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
	
	
  public static void assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType expectedType, String expectedParsedName, 
  		JPhyloIOEventReader reader) throws Exception {
  	
  	assertTokenSetDefinitionEvent(expectedType, expectedParsedName, null, reader);
  }
  
  
  public static void assertTokenSetDefinitionEvent(TokenSetDefinitionEvent.SetType expectedType, String expectedParsedName, 
  		String expectedCharSetName, JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.TOKEN_SET_DEFINITION, event.getEventType());
		
		TokenSetDefinitionEvent tokenSetEvent = event.asTokenSetDefinitionEvent();
		assertEquals(expectedType, tokenSetEvent.getSetType());
		assertEquals(expectedParsedName, tokenSetEvent.getParsedName());
		assertEquals(expectedCharSetName, tokenSetEvent.getCharacterSetName());
  }  
	
	
  public static void assertSingleTokenDefinitionEvent(String expectedTokenName, 
  		SingleTokenDefinitionEvent.Meaning expectedMeaning,	JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventType.SINGLE_TOKEN_DEFINITION, event.getEventType());
		
		SingleTokenDefinitionEvent tokenSetEvent = event.asSingleTokenDefinitionEvent();
		assertEquals(expectedTokenName, tokenSetEvent.getTokenName());
		assertEquals(expectedMeaning, tokenSetEvent.getMeaning());
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
