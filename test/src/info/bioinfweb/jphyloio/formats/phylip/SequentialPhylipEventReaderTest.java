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
package info.bioinfweb.jphyloio.formats.phylip;


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.File;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertLinkedOTUOrOTUsEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertCharactersEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertEventType;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertMetaEvent;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.assertPartEndEvent;
import static org.junit.Assert.* ;



public class SequentialPhylipEventReaderTest {
	@Test
	public void testReadingLongerLength() {
		try {
			SequentialPhylipEventReader reader = new SequentialPhylipEventReader(new File("data/Phylip/Sequential.phy"), false, false);
			try {
				reader.setMaxTokensToRead(20);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "20", null, new Long(20), true, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertCharactersEvent("CCGT-CT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertCharactersEvent("CCGT-GTTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
				assertCharactersEvent("ATG-TT-CCC", reader);
				assertCharactersEvent("CGGT-CTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
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
	public void testReadingRelaxedLongerLength() {
		try {
			SequentialPhylipEventReader reader = new SequentialPhylipEventReader(new File("data/Phylip/RelaxedSequential.phy"), false, true);
			try {
				reader.setMaxTokensToRead(20);
				
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertMetaEvent(ReadWriteConstants.META_KEY_SEQUENCE_COUNT, "5", null, new Long(5), true, true, reader);
				assertMetaEvent(ReadWriteConstants.META_KEY_CHARACTER_COUNT, "20", null, new Long(20), true, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Sequence_name_1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Sequence_name_2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Sequence_name_3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertCharactersEvent("CCGT-CT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Sequence_name_4", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertCharactersEvent("CCGT-GTTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedOTUOrOTUsEvent(EventContentType.SEQUENCE, null, "Sequence_name_5", null, reader);
				assertCharactersEvent("ATG-TT-CCC", reader);
				assertCharactersEvent("CGGT-CTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
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
