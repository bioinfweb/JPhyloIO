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
package info.bioinfweb.jphyloio.formats.newick;


import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.EventType;

import java.io.File;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class NewickReaderTest {
	@Test
	public void test_readNextEvent() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/InternalsTerminalsLength.nwk"));
		try {
			assertTrue(reader.hasNextEvent());
			assertEquals(EventType.DOCUMENT_START, reader.next().getEventType());
			assertTrue(reader.hasNextEvent());
			assertEquals(EventType.TREE_START, reader.next().getEventType());
			
			String idA = assertNodeEvent("A", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			String idB = assertNodeEvent("B", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			String idN3 = assertNodeEvent("N3", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			assertEdgeEvent(idN3, idA, 1.05, reader);
			assertEquals(EventType.EDGE_END, reader.next().getEventType());
			assertEdgeEvent(idN3, idB, 1.0, reader);
			assertEquals(EventType.EDGE_END, reader.next().getEventType());

			String idC = assertNodeEvent("C", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			String idN2 = assertNodeEvent("N2", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			assertEdgeEvent(idN2, idN3, 1.5, reader);
			assertEquals(EventType.EDGE_END, reader.next().getEventType());
			assertEdgeEvent(idN2, idC, 2.5, reader);
			assertEquals(EventType.EDGE_END, reader.next().getEventType());
			
			String idD = assertNodeEvent("D", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			String idE = assertNodeEvent("E", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			String idN4 = assertNodeEvent("N4", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			assertEdgeEvent(idN4, idD, 2.0, reader);
			assertEquals(EventType.EDGE_END, reader.next().getEventType());
			assertEdgeEvent(idN4, idE, 2.1, reader);
			assertEquals(EventType.EDGE_END, reader.next().getEventType());
			
			String idN1 = assertNodeEvent("N1", reader);
			assertEquals(EventType.NODE_END, reader.next().getEventType());
			assertEdgeEvent(idN1, idN2, .8, reader);
			assertEquals(EventType.EDGE_END, reader.next().getEventType());
			assertEdgeEvent(idN1, idN4, 1.4, reader);
			assertEquals(EventType.EDGE_END, reader.next().getEventType());
			//TODO Event for root node with length?

			assertEquals(EventType.TREE_END, reader.next().getEventType());
			assertTrue(reader.hasNextEvent());
			assertEquals(EventType.DOCUMENT_END, reader.next().getEventType());
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
}
