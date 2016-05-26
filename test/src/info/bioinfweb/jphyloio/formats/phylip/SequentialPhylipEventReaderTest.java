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
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.File;

import org.junit.* ;

import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.* ;



public class SequentialPhylipEventReaderTest implements ReadWriteConstants {
	@Test
	public void testReadingLongerLength() {
		try {
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_MAXIMUM_TOKENS_TO_READ, 20);
			SequentialPhylipEventReader reader = new SequentialPhylipEventReader(new File("data/Phylip/Sequential.phy"), parameters);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_COUNT), null, "5", null, new Long(5), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CHARACTER_COUNT), null, "20", null, new Long(20), true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Seq 1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Seq 2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Seq 3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertCharactersEvent("CCGT-CT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Seq 4ATCGA", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertCharactersEvent("CCGT-GTTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Seq 5", null, reader);
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
			ReadWriteParameterMap parameters = new ReadWriteParameterMap();
			parameters.put(ReadWriteParameterMap.KEY_MAXIMUM_TOKENS_TO_READ, 20);
			parameters.put(ReadWriteParameterMap.KEY_RELAXED_PHYLIP, true);
			SequentialPhylipEventReader reader = new SequentialPhylipEventReader(new File("data/Phylip/RelaxedSequential.phy"), parameters);
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
				assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SEQUENCE_COUNT), null, "5", null, new Long(5), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_CHARACTER_COUNT), null, "20", null, new Long(20), true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Sequence_name_1", null, reader);
				assertCharactersEvent("ATG-T--CCG", reader);
				assertCharactersEvent("CCGT-GT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Sequence_name_2", null, reader);
				assertCharactersEvent("ATG-TT-CCG", reader);
				assertCharactersEvent("CCGT-GTT-A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Sequence_name_3", null, reader);
				assertCharactersEvent("ATG-T--CGG", reader);
				assertCharactersEvent("CCGT-CT--A", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Sequence_name_4", null, reader);
				assertCharactersEvent("ATG-TTTCCG", reader);
				assertCharactersEvent("CCGT-GTTTA", reader);
				assertPartEndEvent(EventContentType.SEQUENCE, true, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.SEQUENCE, null, "Sequence_name_5", null, reader);
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
