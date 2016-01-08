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


import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.commons.bio.ChracterStateMeaning;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.events.CharacterSetEvent;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.EventContentType;
import info.bioinfweb.jphyloio.events.EventTopologyType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;


import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;


import info.bioinfweb.jphyloio.events.UnknownCommandEvent;
import static org.junit.Assert.*;



public class JPhyloIOTestTools {
	public static void assertEventType(EventContentType expectedContentType, 
			EventTopologyType expectedTopologyType, JPhyloIOEvent event) throws Exception {
		
		assertEquals(expectedContentType, event.getType().getContentType());
		assertEquals(expectedTopologyType, event.getType().getTopologyType());
	}
	
	
	public static void assertEventType(EventContentType expectedContentType, 
			EventTopologyType expectedTopologyType, JPhyloIOEventReader reader) throws Exception {
		
		assertTrue(reader.hasNextEvent());
		assertEventType(expectedContentType, expectedTopologyType, reader.next());
	}
	
	
	public static void assertCharactersEvent(String expectedName, String expectedSequence, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.SEQUENCE_CHARACTERS, event.getType().getContentType());
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
		assertEquals(EventContentType.SEQUENCE_CHARACTERS, event.getType().getContentType());
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
		assertEquals(EventContentType.COMMENT, event.getType().getContentType());
		CommentEvent commentEvent = event.asCommentEvent();
		assertEquals(expectedContent, commentEvent.getContent());
		assertEquals(expectedContinuedInNextEvent, commentEvent.isContinuedInNextEvent());
  }  
	
	
  public static void assertMetaEvent(String expectedKey, String expectedValue, JPhyloIOEventReader reader) throws Exception {
  	assertMetaEvent(expectedKey, expectedValue, true, reader);
  }
  
  
  public static void assertMetaEvent(String expectedKey, String expectedValue, boolean keyCaseSensitive, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.META_INFORMATION, event.getType().getContentType());
		
		MetaInformationEvent metaInformationEvent = event.asMetaInformationEvent();
		if (keyCaseSensitive == true) {			
			assertEquals(expectedKey, metaInformationEvent.getKey());
			assertEquals(expectedValue, metaInformationEvent.getStringValue());
		}
		else {
			assertEquals(expectedKey.toUpperCase(), metaInformationEvent.getKey().toUpperCase());
			assertEquals(expectedValue.toUpperCase(), metaInformationEvent.getStringValue().toUpperCase());
		}
  }  
	
	
  public static void assertTokenSetDefinitionEvent(CharacterStateType expectedType, String expectedParsedName, 
  		JPhyloIOEventReader reader) throws Exception {
  	
  	assertTokenSetDefinitionEvent(expectedType, expectedParsedName, null, reader);
  }
  
  
  public static void assertTokenSetDefinitionEvent(CharacterStateType expectedType, String expectedParsedName, 
  		String expectedCharSetName, JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.TOKEN_SET_DEFINITION, event.getType().getContentType());
		
		TokenSetDefinitionEvent tokenSetEvent = event.asTokenSetDefinitionEvent();
		assertEquals(expectedType, tokenSetEvent.getSetType());
		assertEquals(expectedParsedName, tokenSetEvent.getParsedName());
		assertEquals(expectedCharSetName, tokenSetEvent.getCharacterSetName());
  }  
	
	
  public static void assertSingleTokenDefinitionEvent(String expectedTokenName, 
  		ChracterStateMeaning expectedMeaning,	JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.SINGLE_TOKEN_DEFINITION, event.getType().getContentType());
		
		SingleTokenDefinitionEvent tokenSetEvent = event.asSingleTokenDefinitionEvent();
		assertEquals(expectedTokenName, tokenSetEvent.getTokenName());
		assertEquals(expectedMeaning, tokenSetEvent.getMeaning());
  }  
	
	
  public static void assertCharacterSetEvent(String expectedName, long expectedStart, long expectedEnd, 
  		JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.CHARACTER_SET, event.getType().getContentType());
		
		CharacterSetEvent charSetEvent = event.asCharacterSetEvent();
		assertEquals(expectedName, charSetEvent.getName());
		assertEquals(expectedStart, charSetEvent.getStart());
		assertEquals(expectedEnd, charSetEvent.getEnd());
  }


  public static String assertNodeEvent(String expectedLabel, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(EventContentType.NODE, EventTopologyType.START, event);
		
		NodeEvent nodeEvent = event.asNodeEvent();
		assertEquals(expectedLabel, nodeEvent.getLabel());
		return nodeEvent.getID();
  }


  public static void assertEdgeEvent(String expectedSourceID, String expectedTargetID, 
  		JPhyloIOEventReader reader) throws Exception {
  	
  	assertEdgeEvent(expectedSourceID, expectedTargetID, Double.NaN, reader);
  }
  
  	
  public static void assertEdgeEvent(String expectedSourceID, String expectedTargetID, 
  		double expectedLength, JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(EventContentType.EDGE, EventTopologyType.START, event);
		
		EdgeEvent edgeEvent = event.asEdgeEvent();
		assertEquals(expectedSourceID, edgeEvent.getSourceID());
		assertEquals(expectedTargetID, edgeEvent.getTargetID());
		if (Double.isNaN(expectedLength)) {
			assertTrue(Double.isNaN(edgeEvent.getLength()));
		}
		else {
			assertEquals(expectedLength, edgeEvent.getLength(), 0.0000001);
		}
  }
}
