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
package info.bioinfweb.jphyloio.formats.pde;


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;

import java.io.File;
import java.io.IOException;

import org.junit.Test;



public class PDEEventReaderTest implements PDEConstants {
//	@Test
//	public void testOutputPDE() {
//		try {
//			PDEEventReader reader = new PDEEventReader(new File("data/PDE/SimpleDNASeq.pde"), new ReadWriteParameterMap());
////			PDEEventReader reader = new PDEEventReader(new File("data/PDE/sequencesWithCustomMeta.pde"), new ReadWriteParameterMap());
//			try {
//				while (reader.hasNextEvent()) {
//					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType());
////					if (event.getType().getContentType().equals(EventContentType.META_LITERAL_CONTENT)) {
//////						System.out.println("Content: " + event.asLiteralMetadataContentEvent().getStringValue());
////					}
////					else if (event.getType().getContentType().equals(EventContentType.META_LITERAL)) {
////						if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
////							if (event.asLiteralMetadataEvent().getPredicate() != null) {
////								if (event.asLiteralMetadataEvent().getPredicate().getURI() != null) {
//////									System.out.println("Literal: " + event.asLiteralMetadataEvent().getPredicate().getURI().getLocalPart());
////								}
////							}
////						}
////					}
////					else if (event.getType().getContentType().equals(EventContentType.META_RESOURCE)) {
////						if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
//////							System.out.println("Resource: " + event.asResourceMetadataEvent().getRel().getLocalPart() + ", " + event.asResourceMetadataEvent().getHRef().toString());
////						}
////					}
////					else if (event.getType().getContentType().equals(EventContentType.CHARACTER_SET_INTERVAL)) {
//////						System.out.println(event.asCharacterSetEvent().getStart() + " " + event.asCharacterSetEvent().getEnd());
////					}
//				}
//			}
//			finally {
//				reader.close();
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getLocalizedMessage());
//		}
//	}
	
	
	@Test
	public void readDNASequences() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/SimpleDNASeq.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix2", null, "otus1", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet3", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet3", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "C", "otu0", reader);
				assertCharactersEvent("ACGTCGCTCGAG-CTGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq1", "B", "otu1", reader);
				assertCharactersEvent("CACGGTG--CTAGCAGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq2", "A", "otu2", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier("string", null), "some sequence", null, null, true, reader);
				assertCharactersEvent("TT-ACGATGAATTGCTGGCA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
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
	public void readMissingSequenceEndToken() throws Exception {
		PDEEventReader reader = new PDEEventReader(new File("data/PDE/MissingEndToken.pde"), new ReadWriteParameterMap());
		try {
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix2", null, "otus1", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet3", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet3", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "C", "otu0", reader);
				
				fail("Exception not thrown");
			}
			catch (JPhyloIOReaderException e) {
				assertEquals(e.getMessage(), "A sequence was found that was longer than the specified alignment length. This is not allowed in PDE files.");
			}
		}
		finally {
			reader.close();
		}		
	}
	
	
	@Test
	public void readLabeledAlignment() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/Label.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix2", null, "otus1", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet3", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet3", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "C", "otu0", reader);
				assertCharactersEvent("ACGTCGCTCGAG-CTGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq1", "B", "otu1", reader);
				assertCharactersEvent("CACGGTG--CTAGCAGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq2", "A", "otu2", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier("string", null), "some sequence", null, null, true, reader);
				assertCharactersEvent("TT-ACGATGAATTGCTGGCA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
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
	
	
//	@Test
//	public void readCharSets() {
//		try {
//			PDEEventReader reader = new PDEEventReader(new File("data/PDE/SimpleDNASeq.pde"), new ReadWriteParameterMap());
//			try {
//				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
//				
//				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
//				assertEndEvent(EventContentType.META_LITERAL, reader);
//				
//				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu0", "C", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu1", "B", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertEndEvent(EventContentType.OTU_LIST, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix2", null, "otus1", reader);
//				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet3", null, reader);
//				assertCharacterSetEvent(0, 20, reader);
//				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
//				
//				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet3", reader);
//				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
//				
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "C", "otu0", reader);
//				assertCharactersEvent("ACGTCGCTCGAG-CTGATCG", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq1", "B", "otu1", reader);
//				assertCharactersEvent("CACGGTG--CTAGCAGATCG", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq2", "A", "otu2", reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier("string", null), "some sequence", null, null, true, reader);
//				assertCharactersEvent("TT-ACGATGAATTGCTGGCA", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertEndEvent(EventContentType.ALIGNMENT, reader);
//				assertEndEvent(EventContentType.DOCUMENT, reader);
//				
//				assertFalse(reader.hasNextEvent());
//			}
//			finally {
//				reader.close();
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getLocalizedMessage());
//		}
//	}
	
	
	@Test
	public void readProteinSequences() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/SimpleProteinSeq.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix2", null, "otus1", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet3", null, reader);
				assertCharacterSetEvent(0, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet3", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "C", "otu0", reader);
				assertCharactersEvent("ATWSATWSATWSATWSATWS", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq1", "B", "otu1", reader);
				assertCharactersEvent("ATWSATWSATWSATWSATWS", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq2", "A", "otu2", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier("string", null), "some sequence", null, null, true, reader);
				assertCharactersEvent("ATWSATWSATWSATWSATWS", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
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
	
	
//	@Test
//	public void readSequencesWithCustomMeta() { //TODO create assertResourceMeta()
//		try {
//			PDEEventReader reader = new PDEEventReader(new File("data/PDE/customMetaDNASeq.pde"), new ReadWriteParameterMap());
//			try {
//				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
//				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus0", null, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu0", "sequence1", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu1", "sequence2", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu2", "sequence3", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertEndEvent(EventContentType.OTU_LIST, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix1", null, "otus0", reader);
//				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet2", null, reader);
//				assertCharacterSetEvent(0, 20, reader);
//				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
//				
//				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet2", reader);
//				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "sequence1", "otu0", reader);
//				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq1", "sequence2", "otu1", reader);
//				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq2", "sequence3", "otu2", reader);
//				assertCharactersEvent("ACTGACTGACTGACTGACTG", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertEndEvent(EventContentType.ALIGNMENT, reader);
//				assertEndEvent(EventContentType.DOCUMENT, reader);
//	
//				assertFalse(reader.hasNextEvent());
//			}
//			finally {
//				reader.close();
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getLocalizedMessage());
//		}
//	}
	
	
	@Test
	public void readSequenceWithMatchTokens() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/DNASeqMatchToken.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu0", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu1", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix2", null, "otus1", reader);
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet3", null, reader);
				assertCharacterSetEvent(0, 23, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet3", reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "23", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "C", "otu0", reader);
				assertCharactersEvent("ACTGACTGAC-A-TGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq1", "B", "otu1", reader);
				assertCharactersEvent("ACTGACTGACAACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq2", "A", "otu2", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier("string", null), "some sequence", null, null, true, reader);
				assertCharactersEvent("ACTGACTGAC---TGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertEndEvent(EventContentType.ALIGNMENT, reader);
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
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
	
	
//	@Test
//	public void readSequenceWithUnknownTokens() { //TODO PhyDE uses wrong match tokens
//		try {
//			PDEEventReader reader = new PDEEventReader(new File("data/PDE/DNASeqMissingToken.pde"), new ReadWriteParameterMap());
//			try {
//				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
//				
//				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
//				assertEndEvent(EventContentType.META_LITERAL, reader);
//				
//				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu0", "C", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu1", "B", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
//				assertEndEvent(EventContentType.OTU, reader);
//				assertEndEvent(EventContentType.OTU_LIST, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix2", null, "otus1", reader);
//				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet3", null, reader);
//				assertCharacterSetEvent(0, 28, reader);
//				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
//				
//				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, "charSet3", reader);
//				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
//				
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "28", null, null, true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "C", "otu0", reader);
//				assertCharactersEvent("ACTGACTGAC-A-T??????????????", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq1", "B", "otu1", reader);
//				assertCharactersEvent("????????????CATGACTGACTG????", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq2", "A", "otu2", reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier("string", null), "some sequence", null, null, true, reader);
//				assertCharactersEvent("GTC-GACTGAC---TGACTG????ACTG", reader);
//				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
//				
//				assertEndEvent(EventContentType.ALIGNMENT, reader);
//				assertEndEvent(EventContentType.DOCUMENT, reader);
//	
//				assertFalse(reader.hasNextEvent());
//			}
//			finally {
//				reader.close();
//			}
//		}
//		catch (Exception e) {
//			e.printStackTrace();
//			fail(e.getLocalizedMessage());
//		}
//	}
}
