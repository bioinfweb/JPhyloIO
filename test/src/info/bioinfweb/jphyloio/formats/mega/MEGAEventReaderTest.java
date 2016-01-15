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

import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.mega.MEGAEventReader;



import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class MEGAEventReaderTest {
//  @Test  // READ_COMMAND_PATTERN needs to be set public to run this test.
//  public void test_READ_COMMAND_PATTERN() {
//  	assertTrue(MEGAEventReader.READ_COMMAND_PATTERN.matcher("SomeCommand [").matches());
//  	assertTrue(MEGAEventReader.READ_COMMAND_PATTERN.matcher("SomeCommand ;").matches());
//  	assertTrue(MEGAEventReader.READ_COMMAND_PATTERN.matcher("SomeCommand \n abc;").matches());
//  	assertFalse(MEGAEventReader.READ_COMMAND_PATTERN.matcher("SomeCommand ").matches());
//  }
  
  
  @Test
  public void testReadingMEGA() throws Exception {
		MEGAEventReader reader = new MEGAEventReader(new File("data/MEGA/HLA-3Seq.meg"), false);
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
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2301", null, reader);
			assertCharactersEvent("GGCTCCCACTCCATGAGGTATTTCTCC", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2501", null, reader);
			assertCharactersEvent("GGCTCCCACTCCATGAGGTATTTCTAC", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-3301", null, reader);
			assertCharactersEvent("GGCTCCCACTCCATGAGGTATTTCACC", reader);
			assertSequenceEndEvent(false, reader);
			
			assertCharacterSetEvent("+", 12, 15, reader);
			assertCharacterSetEvent("+", 18, 21, reader);
			assertCharacterSetEvent("+", 24, 27, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2301", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2501", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-3301", null, reader);
			assertCharactersEvent("GTGGACGACACGCAGTTCGTGCGGTTC", reader);
			assertSequenceEndEvent(false, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2301", null, reader);
			assertCommentEvent("comment 1 in characters", reader);
			assertCharactersEvent("GAGGGGCCGGAGTATTGGGACGAGGAG", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2501", null, reader);
			assertCharactersEvent("GAGGGGCCGGAGTATTGGGACCGGAAC", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-3301", null, reader);
			assertCharactersEvent("GAGGGGCCGGAGTATTGGGACCGGAAC", reader);
			assertSequenceEndEvent(false, reader);
			
			assertCharacterSetEvent("+", 60, 69, reader);
			assertCharacterSetEvent("+", 72, 81, reader);
			assertCharacterSetEvent("Domain=Alpha_1  Property=Coding", 0, 81, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2301", null, reader);
			assertCharactersEvent("GGTTCTCACACCCTCCAGATGATGTTT", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2501", null, reader);
			assertCharactersEvent("GGTTCTCACACCATC", reader);
			assertCommentEvent("comment 2 in characters", reader);
			assertCharactersEvent("CAGAGGATGTAT", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-3301", null, reader);
			assertCharactersEvent("GGTTCTCACACCATCCAGATGATGTAT", reader);
			assertSequenceEndEvent(false, reader);

			assertCommentEvent("comment in label", reader);
			assertCharacterSetEvent("+", 93, 96, reader);
			assertCharacterSetEvent("+", 99, 102, reader);
			assertCharacterSetEvent("+", 105, 108, reader);

			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2301", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2501", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-3301", null, reader);
			assertCharactersEvent("CTGGAGAACGGGAAG", reader);
			assertSequenceEndEvent(false, reader);

			assertCommentEvent("Nested [comment]", reader);
			assertCommentEvent("[Nested] comment", reader);

			assertCharacterSetEvent("Domain=Alpha_2 Property=Coding", 81, 123, reader);
			
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2301", null, reader);
			assertCharactersEvent("GACCCCCCCAAGACACAT", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-2501", null, reader);
			assertCharactersEvent("GACGCCCCCAAGACGCAT", reader);
			assertSequenceEndEvent(false, reader);
			assertBasicOTUEvent(EventContentType.SEQUENCE, "A-3301", null, reader);
			assertCharactersEvent("GACCCCCCCAGGACGCAT", reader);
			assertSequenceEndEvent(false, reader);

			assertCharacterSetEvent("Domain=Alpha_3 Property=Coding", 123, 141, reader);
			
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
			MEGAEventReader reader = new MEGAEventReader(new File("data/MEGA/MatchToken.meg"), true);
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
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
				assertCharactersEvent("TATTTCTCC", reader);
				assertSequenceEndEvent(false, reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
				assertCharactersEvent("TATTTCTAC", reader);
				assertSequenceEndEvent(false, reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
				assertCharactersEvent("TATTTCACC", reader);
				assertSequenceEndEvent(false, reader);
				
				assertBasicOTUEvent(EventContentType.SEQUENCE, "A", null, reader);
				assertCharactersEvent("CGCTAGTTA", reader);
				assertSequenceEndEvent(false, reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "B", null, reader);
				assertCharactersEvent("CGCTAGTAA", reader);
				assertSequenceEndEvent(false, reader);
				assertBasicOTUEvent(EventContentType.SEQUENCE, "C", null, reader);
				assertCharactersEvent("CGCTAGATA", reader);
				assertSequenceEndEvent(false, reader);
				
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
