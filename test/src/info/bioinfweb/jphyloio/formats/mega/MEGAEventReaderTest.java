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

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.mega.MEGAEventReader;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class MEGAEventReaderTest implements MEGAConstants {
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
			
			assertMetaEvent(MEGAEventReader.COMMAND_NAME_TITLE, "Nucleotide sequences of three human class I HLA-A alleles", 
					true, false, reader);
			assertMetaEvent(MEGAEventReader.COMMAND_NAME_DESCRIPTION, 
					"Extracellular domains 1, 2, and 3 are marked. Antigen recognition sites\r\n(ARS) are shown by plus sign", 
					true, false,  reader);
			assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "DATATYPE", "Nucleotide", true, false, reader);
			assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "DATAFORMAT", "Interleaved", true, false, reader);
			assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "NTAXA", "3", true, false, reader);
			assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "NSITES", "822", true, false, reader);
			assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "IDENTICAL", ".", true, false, reader);
			assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "MISSING", "?", true, false, reader);
			assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "INDEL", "-", true, false, reader);
			assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "CODETABLE", "Standard", true, false, reader);
			
			assertCommentEvent("Nested [comment]", reader);
			assertCommentEvent("[Nested] comment", reader);
			
			assertCommentEvent("in command statement", reader);
			String id1 = assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "A-2301", null, reader);
			assertCharactersEvent("GGCTCCCACTCCATGAGGTATTTCTCC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String id2 = assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "A-2501", null, reader);
			assertCharactersEvent("GGCTCCCACTCCATGAGGTATTTCTAC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			String id3 = assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "A-3301", null, reader);
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
			
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCommentEvent("comment 1 in characters", reader);
			assertCharactersEvent("GAGGGGCCGGAGTATTGGGACGAGGAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("GAGGGGCCGGAGTATTGGGACCGGAAC", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
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
			
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCharactersEvent("GGTTCTCACACCCTCCAGATGATGTTT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("GGTTCTCACACCATC", reader);
			assertCommentEvent("comment 2 in characters", reader);
			assertCharactersEvent("CAGAGGATGTAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
			assertCharactersEvent("GGTTCTCACACCATCCAGATGATGTAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertLabeledIDEvent(EventContentType.CHARACTER_SET, LABEL_CHAR_SET_ID, Character.toString(DEFAULT_LABEL_CHAR), reader);
			assertCommentEvent("comment in label", reader);
			assertCharacterSetEvent(93, 96, reader);
			assertCharacterSetEvent(99, 102, reader);
			assertCharacterSetEvent(105, 108, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, false, reader);

			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);

			assertCommentEvent("Nested [comment]", reader);
			assertCommentEvent("[Nested] comment", reader);

			assertLabeledIDEvent(EventContentType.CHARACTER_SET, COMMAND_NAME_DOMAIN + ".Alpha_2", 
					"Domain=Alpha_2 Property=Coding", reader);
			assertCharacterSetEvent(81, 123, reader);
			assertPartEndEvent(EventContentType.CHARACTER_SET, false, reader);
			
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id1, "A-2301", null, reader);
			assertCharactersEvent("GACCCCCCCAAGACACAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id2, "A-2501", null, reader);
			assertCharactersEvent("GACGCCCCCAAGACGCAT", reader);
			assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
			assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, id3, "A-3301", null, reader);
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
				
				assertMetaEvent(MEGAEventReader.COMMAND_NAME_TITLE, "Nucleotide sequences of three human class I HLA-A alleles", 
						true, false, reader);
				assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "DATATYPE", "Nucleotide", true, false, reader);
				assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "DATAFORMAT", "Interleaved", true, false, reader);
				assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "NTAXA", "3", true, false, reader);
				assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "NSITES", "18", true, false, reader);
				assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "IDENTICAL", "i", true, false, reader);
				assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "MISSING", "?", true, false, reader);
				assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "INDEL", "-", true, false, reader);
				assertMetaEvent(MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "CODETABLE", "Standard", true, false, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCharactersEvent("TATTTCTCC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCharactersEvent("TATTTCTAC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "C", null, reader);
				assertCharactersEvent("TATTTCACC", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "A", null, reader);
				assertCharactersEvent("CGCTAGTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "B", null, reader);
				assertCharactersEvent("CGCTAGTAA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, false, reader);
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "C", null, reader);
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
