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
package info.bioinfweb.jphyloio.push;


import java.io.File;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.formats.fasta.FASTAEventReader;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class EventForwarderTest {
	@Test
	public void test_readCurrentNode() throws Exception {
		FASTAEventReader reader = new FASTAEventReader(new File("data/Fasta/Test.fasta"), new ReadWriteParameterMap());
		try {
			//assertTrue(reader.getParentInformation().isEmpty());
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			//assertEquals(EventContentType.DOCUMENT, reader.getParentInformation().getDirectParentContentType());
			assertEquals(new EventType(EventContentType.ALIGNMENT, EventTopologyType.START), reader.peek().getType());
			
			EventForwarder forwarder = new EventForwarder();
			forwarder.readCurrentNode(reader);
			
			//assertEquals(EventContentType.DOCUMENT, reader.getParentInformation().getDirectParentContentType());
			assertEndEvent(EventContentType.DOCUMENT, reader);
			//assertTrue(reader.getParentInformation().isEmpty());
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
}
