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


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertCharacterSetIntervalEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertCharactersEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEndEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEventType;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLabeledIDEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLinkedLabeledIDEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLiteralMetaEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLiteralMetaStartEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertPartEndEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertTokenSetDefinitionEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import info.bioinfweb.commons.bio.CharacterStateSetType;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.exception.JPhyloIOReaderException;
import info.bioinfweb.jphyloio.test.JPhyloIOTestTools;

import java.awt.Color;
import java.io.File;
import java.net.URI;

import org.junit.Test;



public class PDEEventReaderTest implements PDEConstants {
	@Test
	public void testOutputPDE() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/MissingSequenceInfo.pde"), new ReadWriteParameterMap());
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType());
				}
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
	public void readDNASequences() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/SimpleDNASeq.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
								
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "C", "otu2", reader);
				assertCharactersEvent("ACGTCGCTCGAG-CTGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq10", "B", "otu3", reader);
				assertCharactersEvent("CACGGTG--CTAGCAGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq11", "A", "otu4", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);
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
	public void readNoAlignment() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/MissingMatrix.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
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
	public void readNoHeader() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/MissingHeader.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix1", null, null, reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq5", null, null, reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq6", null, null, reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq7", null, null, reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
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
	public void readMissingSequenceInfo() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/MissingSequenceInfo.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix3", null, "otus1", reader);
								
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq7", "C", "otu2", reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq8", null, null, reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", null, null, reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
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
	public void readNoAlignmentAndNoHeader() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/MissingHeaderAndAlignment.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
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
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq0", "C", "otu0", reader);
				
				fail("Exception not thrown");
			}
			catch (JPhyloIOReaderException e) {
				assertEquals(e.getMessage(), "The sequence with the index \"0\" was found to be longer"
											+ " than the specified alignment length of 20. This is not allowed in PDE files.");
			}
		}
		finally {
			reader.close();
		}		
	}
	
	
	@Test
	public void readUnequalSequenceLength() throws Exception {
		PDEEventReader reader = new PDEEventReader(new File("data/PDE/UnequalSequenceLength.pde"), new ReadWriteParameterMap());
		try {
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "C", "otu2", reader);
				assertCharactersEvent("ACGTCGCTCGAG-CTGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq10", "B", "otu3", reader);
				
				fail("Exception not thrown");
			}
			catch (JPhyloIOReaderException e) {
				assertEquals(e.getMessage(), "The sequence with the index \"1\" was found to be shorter"
						+ " than the specified alignment length of 20. This is not allowed in PDE files.");
			}
		}
		finally {
			reader.close();
		}		
	}
	
	
	@Test
	public void readMissingMetaDefinitions() throws Exception {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/MissingMetaDefinition.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 23, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "23", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "A", "otu2", reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);	
				JPhyloIOTestTools.assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINKED_FILE), new URI("someFile.scf"), null, true, reader);
				assertCharactersEvent("ACTGACTGAC---TGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq12", "B", "otu3", reader);
				JPhyloIOTestTools.assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINKED_FILE), new URI("someFile.scf"), null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_ACCESS_NUMBER), new URIOrStringIdentifier(META_TYPE_NUMBER, null), "45", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some comment", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("testNumber", ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA), new URIOrStringIdentifier(META_TYPE_NUMBER, null), 
						"18", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("33", ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA), null, "some String", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("34", ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA), null, "someFile.txt", null, null, true, reader);
				assertCharactersEvent("ACTGACTGACAACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq19", "C", "otu4", reader);
				JPhyloIOTestTools.assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINKED_FILE), new File("\\\\nwz.wwu.de\\dfs\\home\\s\\s_wiec03\\Desktop\\sample.scf").toURI(), null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some comment", null, null, true, reader);				
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
	
	
	@Test
	public void readLabeledAlignment() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/Label.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "C", "otu2", reader);
				assertCharactersEvent("ACGTCGCTCGAG-CTGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq10", "B", "otu3", reader);
				assertCharactersEvent("CACGGTG--CTAGCAGATCG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq11", "A", "otu4", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);
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
	public void readTaxonSet() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/TaxonSet.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "C", "otu2", reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq10", "B", "otu3", reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq11", "A", "otu4", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
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
	public void readCharSets() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/SimpleCharSet.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet5", "charSet1", reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CHARSET_VISIBILITY), null, "true", null, true, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CHARSET_COLOR), null, "FC3D33", null, Color.decode("#FC3D33"), true, reader);
				assertCharacterSetIntervalEvent(2, 2, reader);
				assertCharacterSetIntervalEvent(4, 14, reader);
				assertCharacterSetIntervalEvent(16, 18, reader);
				assertCharacterSetIntervalEvent(20, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);
				
				assertLabeledIDEvent(EventContentType.CHARACTER_SET, "charSet8", "charSet2", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CHARSET_VISIBILITY), null, "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CHARSET_COLOR), null, "FFFF33", null, Color.decode("#FFFF33"), true, reader);				
				assertCharacterSetIntervalEvent(5, 9, reader);
				assertCharacterSetIntervalEvent(11, 11, reader);
				assertCharacterSetIntervalEvent(13, 20, reader);
				assertPartEndEvent(EventContentType.CHARACTER_SET, true, reader);				
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix11", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq15", "C", "otu2", reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq16", "B", "otu3", reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq17", "A", "otu4", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);
				assertCharactersEvent("ACTGACTGACTGTGACCATA", reader);
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
	public void readProteinSequences() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/SimpleProteinSeq.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 20, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "20", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "C", "otu2", reader);
				assertCharactersEvent("ATWSATWSATWSATWSATWS", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq10", "B", "otu3", reader);
				assertCharactersEvent("ATWSATWSATWSATWSATWS", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq11", "A", "otu4", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);
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
	
	
	@Test
	public void readSequencesWithCustomMeta() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/customMetaDNASeq.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 23, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "23", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "A", "otu2", reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);	
				JPhyloIOTestTools.assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINKED_FILE), new URI("someFile.scf"), null, true, reader);
				assertCharactersEvent("ACTGACTGAC---TGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq12", "B", "otu3", reader);
				JPhyloIOTestTools.assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINKED_FILE), new URI("someFile.scf"), null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_ACCESS_NUMBER), new URIOrStringIdentifier(META_TYPE_NUMBER, null), "45", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some comment", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("testNumber", ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA), new URIOrStringIdentifier(META_TYPE_NUMBER, null), 
						"18", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("testString", ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA), new URIOrStringIdentifier(META_TYPE_STRING, null), "some String", null, null, true, reader);
				JPhyloIOTestTools.assertResourceMetaEvent(new URIOrStringIdentifier("testFile", ReadWriteConstants.PREDICATE_HAS_RESOURCE_METADATA), new URI("someFile.txt"), null, true, reader);
				assertCharactersEvent("ACTGACTGACAACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq19", "C", "otu4", reader);
				JPhyloIOTestTools.assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINKED_FILE), new File("\\\\nwz.wwu.de\\dfs\\home\\s\\s_wiec03\\Desktop\\sample.scf").toURI(), null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some comment", null, null, true, reader);				
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
	
	
	@Test
	public void readSequenceWithMatchTokens() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/DNASeqMatchToken.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 23, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "23", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "C", "otu2", reader);
				assertCharactersEvent("ACTGACTGAC-A-TGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq10", "B", "otu3", reader);
				assertCharactersEvent("ACTGACTGACAACTGACTGACTG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq11", "A", "otu4", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);
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
	
	
	@Test
	public void readSequenceWithUnknownTokens() {
		try {
			PDEEventReader reader = new PDEEventReader(new File("data/PDE/DNASeqMissingToken.pde"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaStartEvent(new URIOrStringIdentifier(null, PREDICATE_DESCRIPTION), LiteralContentSequenceType.SIMPLE, reader);
				assertEndEvent(EventContentType.META_LITERAL, reader);
				
				assertLabeledIDEvent(EventContentType.OTU_LIST, "otus1", null, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu2", "C", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu3", "B", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertLabeledIDEvent(EventContentType.OTU, "otu4", "A", reader);
				assertEndEvent(EventContentType.OTU, reader);
				assertEndEvent(EventContentType.OTU_LIST, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.ALIGNMENT, "matrix5", null, "otus1", reader);
				
				assertTokenSetDefinitionEvent(CharacterStateSetType.DNA, null, reader);
				assertCharacterSetIntervalEvent(0, 25, reader);
				assertEndEvent(EventContentType.TOKEN_SET_DEFINITION, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_CHARACTER_COUNT), null, "25", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, ReadWriteConstants.PREDICATE_SEQUENCE_COUNT), null, "3", null, null, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq9", "C", "otu2", reader);
				assertCharactersEvent("AAAAGTGATAA-CTTTCAAATTCAG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq10", "B", "otu3", reader);
				assertCharactersEvent("????????????CTTTCAAATTCAG", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, "seq11", "A", "otu4", reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COMMENT), new URIOrStringIdentifier(META_TYPE_STRING, null), "some sequence", null, null, true, reader);
				assertCharactersEvent("AAAAGTGATAACTT???????????", reader);
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
}
