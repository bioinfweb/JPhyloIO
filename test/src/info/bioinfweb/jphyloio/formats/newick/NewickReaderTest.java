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
package info.bioinfweb.jphyloio.formats.newick;


import info.bioinfweb.jphyloio.events.EventContentType;
import info.bioinfweb.jphyloio.events.EventTopologyType;

import java.io.File;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class NewickReaderTest {
	@Test
	public void test_readNextEvent_InternalsTerminalsLength() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/InternalsTerminalsLength.nwk"));
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertNodeEvent("A", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idB = assertNodeEvent("B", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN3 = assertNodeEvent("N3", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN3, idA, 1.05, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN3, idB, 1.0, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String idC = assertNodeEvent("C", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN2 = assertNodeEvent("N2", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idN3, 1.5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idC, 2.5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String idD = assertNodeEvent("D", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idE = assertNodeEvent("E", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN4 = assertNodeEvent("N4", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN4, idD, 2.0, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN4, idE, 2.1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String idN1 = assertNodeEvent("N1", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idN2, .8, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idN4, 1.4, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			//TODO Event for root node with length?

			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_NoNamedNodes() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/NoNamedNodes.nwk"));
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			//TODO Event for root node with length?

			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_LeafNodesNamed() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/LeafNodesNamed.nwk"));
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertNodeEvent("A", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertNodeEvent("B", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertNodeEvent("C", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertNodeEvent("D", reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			//TODO Event for root node with length?

			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_OnlyBranchLengths() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/OnlyBranchLengths.nwk"));
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, .3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, .4, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertNodeEvent(null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id1, .1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id2, .2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id3, .5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			//TODO Event for root node with length?

			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
}
