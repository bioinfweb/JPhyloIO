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
package info.bioinfweb.jphyloio.formats.mega;


import java.io.File;

import info.bioinfweb.jphyloio.events.EventContentType;
import info.bioinfweb.jphyloio.events.EventTopologyType;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
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
  
  
  private void assertMetaInformation(MEGAEventReader reader, String name) throws Exception {
		assertTrue(reader.hasNextEvent());
		
		JPhyloIOEvent event = reader.next();
		assertEquals(EventContentType.META_INFORMATION, event.getType().getContentType());
		assertEquals(name, event.asMetaInformationEvent().getKey().toUpperCase());
  }
  
  
  @Test
  public void testReadingMEGA() {
		try {
			MEGAEventReader reader = new MEGAEventReader(new File("data/MEGA/HLA-3Seq.meg"), false);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaInformation(reader, MEGAEventReader.COMMAND_NAME_TITLE);
				assertMetaInformation(reader, MEGAEventReader.COMMAND_NAME_DESCRIPTION);
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "DATATYPE");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "DATAFORMAT");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "NTAXA");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "NSITES");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "IDENTICAL");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "MISSING");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "INDEL");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "CODETABLE");
				
				assertCommentEvent("Nested [comment]", reader);
				assertCommentEvent("[Nested] comment", reader);
				
				assertCommentEvent("in command statement", reader);
				assertCharactersEvent("A-2301", "GGCTCCCACTCCATGAGGTATTTCTCC", reader);
				assertCharactersEvent("A-2501", "GGCTCCCACTCCATGAGGTATTTCTAC", reader);
				assertCharactersEvent("A-3301", "GGCTCCCACTCCATGAGGTATTTCACC", reader);
				
				assertCharacterSetEvent("+", 12, 15, reader);
				assertCharacterSetEvent("+", 18, 21, reader);
				assertCharacterSetEvent("+", 24, 27, reader);
				
				assertCharactersEvent("A-2301", "GTGGACGACACGCAGTTCGTGCGGTTC", reader);
				assertCharactersEvent("A-2501", "GTGGACGACACGCAGTTCGTGCGGTTC", reader);
				assertCharactersEvent("A-3301", "GTGGACGACACGCAGTTCGTGCGGTTC", reader);
				
				assertCharactersEvent("A-2301", "GAGGGGCCGGAGTATTGGGACGAGGAG", reader);
				assertCommentEvent("comment 1 in characters", reader);
				assertCharactersEvent("A-2501", "GAGGGGCCGGAGTATTGGGACCGGAAC", reader);
				assertCharactersEvent("A-3301", "GAGGGGCCGGAGTATTGGGACCGGAAC", reader);
				
				assertCharacterSetEvent("+", 60, 69, reader);
				assertCharacterSetEvent("+", 72, 81, reader);
				assertCharacterSetEvent("Domain=Alpha_1  Property=Coding", 0, 81, reader);
				
				assertCharactersEvent("A-2301", "GGTTCTCACACCCTCCAGATGATGTTT", reader);
				assertCharactersEvent("A-2501", "GGTTCTCACACCATCCAGAGGATGTAT", reader);
				assertCommentEvent("comment 2 in characters", reader);
				assertCharactersEvent("A-3301", "GGTTCTCACACCATCCAGATGATGTAT", reader);

				assertCommentEvent("comment in label", reader);
				assertCharacterSetEvent("+", 93, 96, reader);
				assertCharacterSetEvent("+", 99, 102, reader);
				assertCharacterSetEvent("+", 105, 108, reader);

				assertCharactersEvent("A-2301", "CTGGAGAACGGGAAG", reader);
				assertCharactersEvent("A-2501", "CTGGAGAACGGGAAG", reader);
				assertCharactersEvent("A-3301", "CTGGAGAACGGGAAG", reader);

				assertCommentEvent("Nested [comment]", reader);
				assertCommentEvent("[Nested] comment", reader);

				assertCharacterSetEvent("Domain=Alpha_2 Property=Coding", 81, 123, reader);
				
				assertCharactersEvent("A-2301", "GACCCCCCCAAGACACAT", reader);
				assertCharactersEvent("A-2501", "GACGCCCCCAAGACGCAT", reader);
				assertCharactersEvent("A-3301", "GACCCCCCCAGGACGCAT", reader);

				assertCharacterSetEvent("Domain=Alpha_3 Property=Coding", 123, 141, reader);
				
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
  
  
  @Test
  public void testReadingMEGAMatchCharacter() {
		try {
			MEGAEventReader reader = new MEGAEventReader(new File("data/MEGA/MatchToken.meg"), true);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaInformation(reader, MEGAEventReader.COMMAND_NAME_TITLE);
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "DATATYPE");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "DATAFORMAT");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "NTAXA");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "NSITES");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "IDENTICAL");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "MISSING");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "INDEL");
				assertMetaInformation(reader, MEGAEventReader.FORMAT_KEY_PREFIX.toUpperCase() + "CODETABLE");
				
				assertCharactersEvent("A", "TATTTCTCC", reader);
				assertCharactersEvent("B", "TATTTCTAC", reader);
				assertCharactersEvent("C", "TATTTCACC", reader);
				
				assertCharactersEvent("A", "CGCTAGTTA", reader);
				assertCharactersEvent("B", "CGCTAGTAA", reader);
				assertCharactersEvent("C", "CGCTAGATA", reader);
				
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
