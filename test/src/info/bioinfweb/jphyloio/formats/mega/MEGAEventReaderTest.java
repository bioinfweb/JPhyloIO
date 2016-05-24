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
package info.bioinfweb.jphyloio.formats.mega;


import java.io.File;

import javax.xml.namespace.QName;

import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.mega.MEGAEventReader;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class MEGAEventReaderTest implements MEGAConstants, ReadWriteConstants {
//  @Test  // READ_COMMAND_PATTERN needs to be set public to run this test.
//  public void test_READ_COMMAND_PATTERN() {
//  	assertTrue(MEGAEventReader.READ_COMMAND_PATTERN.matcher("SomeCommand [").matches());
//  	assertTrue(MEGAEventReader.READ_COMMAND_PATTERN.matcher("SomeCommand ;").matches());
//  	assertTrue(MEGAEventReader.READ_COMMAND_PATTERN.matcher("SomeCommand \n abc;").matches());
//  	assertFalse(MEGAEventReader.READ_COMMAND_PATTERN.matcher("SomeCommand ").matches());
//  }
  
  
  @Test
  public void testReadingMEGA() throws Exception {
		MEGAEventReader reader = new MEGAEventReader(new File("data/MEGA/HLA-3Seq.meg"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier("Title", new QName(MEGA_PREDICATE_NAMESPACE, COMMAND_NAME_TITLE)), 
					null, "Nucleotide sequences of three human class I HLA-A alleles", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("Description", new QName(MEGA_PREDICATE_NAMESPACE, COMMAND_NAME_DESCRIPTION)), 
					null, "Extracellular domains 1, 2, and 3 are marked. Antigen recognition sites\r\n(ARS) are shown by plus sign", 
					null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("datatype", new QName(MEGA_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "DATATYPE")), null, "Nucleotide", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("dataformat", new QName(MEGA_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "DATAFORMAT")), null, "Interleaved", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("ntaxa", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("nsites", PREDICATE_CHARACTER_COUNT), null, "822", null, new Long(822), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("identical", new QName(MEGA_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "IDENTICAL")), null, ".", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("missing", new QName(MEGA_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "MISSING")), null, "?", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("indel", new QName(MEGA_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "INDEL")), null, "-", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("codetable", new QName(MEGA_PREDICATE_NAMESPACE, 
					COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "CODETABLE")), null, "Standard", null, null, true, reader);
			
			assertCommentEvent("Nested [comment]", reader);
			assertCommentEvent("[Nested] comment", reader);
			
			assertCommentEvent("in command statement", reader);
			String id1 = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A-2301", null, reader);
			assertCharactersEvent("GGCTCCCACTCCATGAGGTATTTCTCC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String id2 = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A-2501", null, reader);
			assertCharactersEvent("GGCTCCCACTCCATGAGGTATTTCTAC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String id3 = assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A-3301", null, reader);
			assertCharactersEvent("GGCTCCCACTCCATGAGGTATTTCACC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertNotEquals(id1, id2);
			assertNotEquals(id1, id3);
			assertNotEquals(id2, id3);

			assertLabeledIDEvent(EventContentType.CHARACTER_SET, LABEL_CHAR_SET_ID, Character.toString(DEFAULT_LABEL_CHAR), reader);
			assertCharacterSetEvent(12, 15, reader);
			assertCharacterSetEvent(18, 21, reader);
			assertCharacterSetEvent(24, 27, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, false, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCommentEvent("comment 1 in characters", reader);
			assertCharactersEvent("GAGGGGCCGGAGTATTGGGACGAGGAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("GAGGGGCCGGAGTATTGGGACCGGAAC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
			assertCharactersEvent("GAGGGGCCGGAGTATTGGGACCGGAAC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertLabeledIDEvent(EventContentType.CHARACTER_SET, LABEL_CHAR_SET_ID, Character.toString(DEFAULT_LABEL_CHAR), reader);
			assertCharacterSetEvent(60, 69, reader);
			assertCharacterSetEvent(72, 81, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, false, reader);
			assertLabeledIDEvent(EventContentType.CHARACTER_SET, COMMAND_NAME_DOMAIN + ".Alpha_1", 
					"Domain=Alpha_1  Property=Coding", reader);
			assertCharacterSetEvent(0, 81, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, false, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCharactersEvent("GGTTCTCACACCCTCCAGATGATGTTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("GGTTCTCACACCATC", reader);
			assertCommentEvent("comment 2 in characters", reader);
			assertCharactersEvent("CAGAGGATGTAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
			assertCharactersEvent("GGTTCTCACACCATCCAGATGATGTAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertLabeledIDEvent(EventContentType.CHARACTER_SET, LABEL_CHAR_SET_ID, Character.toString(DEFAULT_LABEL_CHAR), reader);
			assertCommentEvent("comment in label", reader);
			assertCharacterSetEvent(93, 96, reader);
			assertCharacterSetEvent(99, 102, reader);
			assertCharacterSetEvent(105, 108, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, false, reader);

			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertCommentEvent("Nested [comment]", reader);
			assertCommentEvent("[Nested] comment", reader);

			assertLabeledIDEvent(EventContentType.CHARACTER_SET, COMMAND_NAME_DOMAIN + ".Alpha_2", 
					"Domain=Alpha_2 Property=Coding", reader);
			assertCharacterSetEvent(81, 123, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, false, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCharactersEvent("GACCCCCCCAAGACACAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("GACGCCCCCAAGACGCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
			assertCharactersEvent("GACCCCCCCAGGACGCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertLabeledIDEvent(EventContentType.CHARACTER_SET, COMMAND_NAME_DOMAIN + ".Alpha_3", 
					"Domain=Alpha_3 Property=Coding", reader);
			assertCharacterSetEvent(123, 141, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, false, reader);
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
  }
  
  
  @Test
  public void testReadingMEGAMatchCharacter() {
		try {
			MEGAEventReader reader = new MEGAEventReader(new File("data/MEGA/MatchToken.meg"), new ReadWriteParameterMap());
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier("Title", new QName(MEGA_PREDICATE_NAMESPACE, COMMAND_NAME_TITLE)), 
						null, "Nucleotide sequences of three human class I HLA-A alleles", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("datatype", new QName(MEGA_PREDICATE_NAMESPACE, 
						COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "DATATYPE")), null, "Nucleotide", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("dataformat", new QName(MEGA_PREDICATE_NAMESPACE, 
						COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "DATAFORMAT")), null, "Interleaved", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("ntaxa", PREDICATE_SEQUENCE_COUNT), null, "3", null, new Long(3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("nsites", PREDICATE_CHARACTER_COUNT), null, "18", null, new Long(18), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("identical", new QName(MEGA_PREDICATE_NAMESPACE, 
						COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "IDENTICAL")), null, "i", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("missing", new QName(MEGA_PREDICATE_NAMESPACE, 
						COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "MISSING")), null, "?", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("indel", new QName(MEGA_PREDICATE_NAMESPACE, 
						COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "INDEL")), null, "-", null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier("codetable", new QName(MEGA_PREDICATE_NAMESPACE, 
						COMMAND_NAME_FORMAT + PREDICATE_PART_SEPERATOR + "CODETABLE")), null, "Standard", null, null, true, reader);

				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCharactersEvent("TATTTCTCC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCharactersEvent("TATTTCTAC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
				assertCharactersEvent("TATTTCACC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCharactersEvent("CGCTAGTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCharactersEvent("CGCTAGTAA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "C", null, reader);
				assertCharactersEvent("CGCTAGATA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
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
