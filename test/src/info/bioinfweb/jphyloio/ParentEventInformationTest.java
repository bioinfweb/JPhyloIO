/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2018  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio;


import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.formats.fasta.FASTAEventReader;
import info.bioinfweb.jphyloio.push.EventForwarder;

import java.io.File;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class ParentEventInformationTest {
	@Test
	public void test() throws Exception {
		FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/ParentEventInformationTest.fasta"), new ReadWriteParameterMap());
		try {
			assertTrue(reader.getParentInformation().isEmpty());
			
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertTrue(reader.getParentInformation().isEmpty());
			
			assertEventType(EventContentType.ALIGNMENT, EventTopologyType.START, reader);
			assertEquals(EventContentType.DOCUMENT, reader.getParentInformation().getDirectParentContentType());
			
			assertEventType(EventContentType.SEQUENCE, EventTopologyType.START, reader);
			assertEquals(EventContentType.ALIGNMENT, reader.getParentInformation().getDirectParentContentType());
			
			assertEventType(EventContentType.SEQUENCE_TOKENS, EventTopologyType.SOLE, reader);
			assertEquals(EventContentType.SEQUENCE, reader.getParentInformation().getDirectParentContentType());
			
			assertEndEvent(EventContentType.SEQUENCE, reader);
			assertEquals(EventContentType.ALIGNMENT, reader.getParentInformation().getDirectParentContentType());

			assertEndEvent(EventContentType.ALIGNMENT, reader);
			assertEquals(EventContentType.DOCUMENT, reader.getParentInformation().getDirectParentContentType());

			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertTrue(reader.getParentInformation().isEmpty());

			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
}
