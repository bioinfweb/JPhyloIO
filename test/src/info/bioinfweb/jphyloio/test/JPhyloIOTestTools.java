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
package info.bioinfweb.jphyloio.test;


import static org.junit.Assert.*;

import java.net.URI;

import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.commons.bio.CharacterSymbolMeaning;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.LabelEditingReporter;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.events.CharacterSetIntervalEvent;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.PartEndEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.TokenSetDefinitionEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.test.dataadapters.SingleTokenTestMatrixDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestMatrixDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestOTUListDataAdapter;



public class JPhyloIOTestTools {
	public static ListBasedDocumentDataAdapter createTestDocument(String... sequences) {
		ListBasedDocumentDataAdapter result = new ListBasedDocumentDataAdapter();
		result.getMatrices().add(new TestMatrixDataAdapter("matrix1", "A matrix", false, sequences));
		return result;
	}
	
	
	public static ListBasedDocumentDataAdapter createTestDocumentWithLabels(String... labelsAndSequences) {
		ListBasedDocumentDataAdapter result = new ListBasedDocumentDataAdapter();
		TestMatrixDataAdapter matrixAdapter = new TestMatrixDataAdapter("matrix1", "A matrix", true, labelsAndSequences);
		TestOTUListDataAdapter otuListAdapter = matrixAdapter.createAccordingOTUList(0);
		matrixAdapter.setLinkedOTUsID(ReadWriteConstants.DEFAULT_OTU_LIST_ID_PREFIX + 0);
		result.getOTUListsMap().put(otuListAdapter.getStartEvent().getID(), otuListAdapter);
		result.getMatrices().add(matrixAdapter);
		return result;
	}
	
	
	public static ListBasedDocumentDataAdapter createSingleTokenTestDocument(String... sequences) {
		ListBasedDocumentDataAdapter result = new ListBasedDocumentDataAdapter();
		result.getMatrices().add(new SingleTokenTestMatrixDataAdapter("matrix1", "A matrix", false, sequences));
		return result;
	}
	
	
	public static void assertEventType(EventContentType expectedContentType, 
			EventTopologyType expectedTopologyType, JPhyloIOEvent event) throws Exception {
		
		assertEquals(expectedContentType, event.getType().getContentType());
		assertEquals(expectedTopologyType, event.getType().getTopologyType());
	}
	
	
	public static void assertEventType(EventContentType expectedContentType, EventTopologyType expectedTopologyType, 
			JPhyloIOEventReader reader) throws Exception {
		
		assertTrue(reader.hasNextEvent());
		assertEventType(expectedContentType, expectedTopologyType, reader.next());
	}
	
	
	public static void assertEndEvent(EventContentType expectedContentType, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		assertEventType(expectedContentType, EventTopologyType.END, reader);
	}
	
	
	public static LabeledIDEvent assertLabeledIDEvent(EventContentType expectedType, String expectedID, String expectedLabel, 
			JPhyloIOEventReader reader) throws Exception {
		
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(expectedType, EventTopologyType.START, event);

		LabeledIDEvent labeledIDEvent = event.asLabeledIDEvent();
		if (expectedID != null) {
			assertEquals(expectedID, labeledIDEvent.getID());
		}
		assertEquals(expectedLabel, labeledIDEvent.getLabel());
		return labeledIDEvent;
	}
		
	
	public static String assertLinkedLabeledIDEvent(EventContentType expectedType, String expectedID, String expectedLabel, 
			String expectedOTUOrOTUsID,	JPhyloIOEventReader reader) throws Exception {
		
		LinkedLabeledIDEvent otuEvent = assertLabeledIDEvent(expectedType, expectedID, expectedLabel, reader).asLinkedLabeledIDEvent();
		if (expectedOTUOrOTUsID == null) {
			assertNull(otuEvent.getLinkedID());
		}
		else {
			assertEquals(expectedOTUOrOTUsID, otuEvent.getLinkedID());
		}
		return otuEvent.getID();
	}
	
	
	public static void assertPartEndEvent(EventContentType contentType, boolean expectedSequenceTerminated, 
			JPhyloIOEventReader reader) throws Exception {
		
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(contentType, EventTopologyType.END, event);
		PartEndEvent endEvent = event.asPartEndEvent();
		assertEquals(expectedSequenceTerminated, endEvent.isTerminated());
	}
	
	
	public static void assertSingleTokenEvent(String expectedToken, boolean testEndEvent, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.SINGLE_SEQUENCE_TOKEN, event.getType().getContentType());
		SingleSequenceTokenEvent tokensEvent = event.asSingleSequenceTokenEvent();
		assertEquals(expectedToken, tokensEvent.getToken());
		
		if (testEndEvent) {
			assertEndEvent(EventContentType.SINGLE_SEQUENCE_TOKEN, reader);
		}
	}
	
	
	public static void assertCharactersEvent(String expectedSequence, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.SEQUENCE_TOKENS, event.getType().getContentType());
		SequenceTokensEvent tokensEvent = event.asSequenceTokensEvent();
		assertEquals(expectedSequence.length(), tokensEvent.getCharacterValues().size());
		for (int i = 0; i < expectedSequence.length(); i++) {
			assertEquals(expectedSequence.substring(i, i + 1), tokensEvent.getCharacterValues().get(i));
		}
	}
	
	
	public static void assertCharactersEvent(String[] expectedSequence, JPhyloIOEventReader reader) throws Exception {
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.SEQUENCE_TOKENS, event.getType().getContentType());
		SequenceTokensEvent tokensEvent = event.asSequenceTokensEvent();
		assertEquals(expectedSequence.length, tokensEvent.getCharacterValues().size());
		for (int i = 0; i < expectedSequence.length; i++) {
			assertEquals(expectedSequence[i], tokensEvent.getCharacterValues().get(i));
		}
	}
	
	
  public static void assertCommentEvent(String expectedContent, JPhyloIOEventReader reader) throws Exception {
  	assertCommentEvent(expectedContent, false, reader);  	
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
	
	
  public static String assertLiteralMetaStartEvent(URIOrStringIdentifier expectedPredicate, LiteralContentSequenceType expectedSequenceType,  
  		JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(EventContentType.META_LITERAL, EventTopologyType.START, event);
		
		LiteralMetadataEvent metaInformationEvent = event.asLiteralMetadataEvent();
		assertEquals(expectedSequenceType, metaInformationEvent.getSequenceType());  //TODO Also check other types?
		assertEquals(expectedPredicate, metaInformationEvent.getPredicate());
		
		return metaInformationEvent.getID();
  }
  
  	
  public static void assertLiteralMetaContentEvent(URIOrStringIdentifier expectedOriginalType, 
  		String expectedStringValue, String expectedAlternativeStringValue, Object expectedObjectValue, boolean testEndEvent, 
  		JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(EventContentType.META_LITERAL_CONTENT, EventTopologyType.SOLE, event);
		LiteralMetadataContentEvent contentEvent = event.asLiteralMetadataContentEvent();
		assertEquals(expectedOriginalType, contentEvent.getOriginalType());
		assertEquals(expectedStringValue, contentEvent.getStringValue());
		assertEquals(expectedObjectValue, contentEvent.getObjectValue());
		assertEquals(expectedAlternativeStringValue, contentEvent.getAlternativeStringValue());
		
		if (testEndEvent) {
			assertEndEvent(EventContentType.META_LITERAL, reader);
		}
  }
  
  
  public static String assertLiteralMetaEvent(URIOrStringIdentifier expectedPredicate, URIOrStringIdentifier expectedOriginalType, 
  		String expectedStringValue, String expectedAlternativeStringValue, Object expectedObjectValue, boolean testEndEvent, 
  		JPhyloIOEventReader reader) throws Exception {
  	
		String result = assertLiteralMetaStartEvent(expectedPredicate, LiteralContentSequenceType.SIMPLE, reader);
  	assertLiteralMetaContentEvent(expectedOriginalType, expectedStringValue, expectedAlternativeStringValue, expectedObjectValue, testEndEvent, reader);		
		return result;
  }
  
  
  public static String assertResourceMetaEvent(URIOrStringIdentifier expectedRel, URI expectedHref, String expectedAbout, boolean testEndEvent, JPhyloIOEventReader reader) throws Exception {
  	assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(EventContentType.META_RESOURCE, EventTopologyType.START, event);
		
		ResourceMetadataEvent metaInformationEvent = event.asResourceMetadataEvent();
		assertEquals(expectedRel, metaInformationEvent.getRel());
		assertEquals(expectedHref, metaInformationEvent.getHRef());
		assertEquals(expectedAbout, metaInformationEvent.getAbout());
  	
  	if (testEndEvent) {
			assertEndEvent(EventContentType.META_RESOURCE, reader);
		}
  	
  	return metaInformationEvent.getID();
  }
  
  
  public static void assertTokenSetDefinitionEvent(CharacterStateSetType expectedType, String expectedLabel, 
  		JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.START, event);
		
		TokenSetDefinitionEvent tokenSetEvent = event.asTokenSetDefinitionEvent();
		assertEquals(expectedType, tokenSetEvent.getSetType());
		assertEquals(expectedLabel, tokenSetEvent.getLabel());
  }
  
  
  @Deprecated
  public static void assertTokenSetDefinitionEvent(CharacterStateSetType expectedType, String expectedLabel, 
  		String expectedCharSetID, JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(EventContentType.TOKEN_SET_DEFINITION, EventTopologyType.START, event);
		
		TokenSetDefinitionEvent tokenSetEvent = event.asTokenSetDefinitionEvent();
		assertEquals(expectedType, tokenSetEvent.getSetType());
		assertEquals(expectedLabel, tokenSetEvent.getLabel());
		assertEquals(expectedCharSetID, tokenSetEvent.getCharacterSetID());
  }
	
	
  public static void assertSingleTokenDefinitionEvent(String expectedTokenName, 
  		CharacterSymbolMeaning expectedMeaning, boolean testEndEvent, JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEventType(EventContentType.SINGLE_TOKEN_DEFINITION, EventTopologyType.START, event);
		
		SingleTokenDefinitionEvent tokenSetEvent = event.asSingleTokenDefinitionEvent();
		assertEquals(expectedTokenName, tokenSetEvent.getTokenName());
		assertEquals(expectedMeaning, tokenSetEvent.getMeaning());
		
		if (testEndEvent) {
			assertEndEvent(EventContentType.SINGLE_TOKEN_DEFINITION, reader);
		}
  }  
	
	
  public static void assertCharacterSetIntervalEvent(long expectedStart, long expectedEnd, 
  		JPhyloIOEventReader reader) throws Exception {
  	
		assertTrue(reader.hasNextEvent());
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.CHARACTER_SET_INTERVAL, event.getType().getContentType());
		
		CharacterSetIntervalEvent charSetEvent = event.asCharacterSetEvent();
		assertEquals(expectedStart, charSetEvent.getStart());
		assertEquals(expectedEnd, charSetEvent.getEnd());
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
  
  
  public static void assertEditedLabelMapping(LabelEditingReporter.LabelStatus expectedLabelStatus, String expectedLabel, 
  		EventContentType contentType,	String id, LabelEditingReporter reporter) {
  	
		assertEquals(expectedLabelStatus, reporter.getLabelStatus(contentType, id));
		if (expectedLabelStatus.equals(LabelEditingReporter.LabelStatus.EDITED) ||
				expectedLabelStatus.equals(LabelEditingReporter.LabelStatus.UNCHANGED)) {
			
			assertEquals(expectedLabel, reporter.getEditedLabel(contentType, id));
		}
		else {
			assertNull(reporter.getEditedLabel(contentType, id));
		}
  }
}
