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


import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

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
			
			String idA = assertBasicOTUEvent(EventContentType.NODE, null, "A", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idB = assertBasicOTUEvent(EventContentType.NODE, null, "B", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN3 = assertBasicOTUEvent(EventContentType.NODE, null, "N3", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN3, idA, 1.05, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN3, idB, 1.0, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String idC = assertBasicOTUEvent(EventContentType.NODE, null, "C", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN2 = assertBasicOTUEvent(EventContentType.NODE, null, "N2", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idN3, 1.5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idC, 2.5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String idD = assertBasicOTUEvent(EventContentType.NODE, null, "D", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idE = assertBasicOTUEvent(EventContentType.NODE, null, "E", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN4 = assertBasicOTUEvent(EventContentType.NODE, null, "N4", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN4, idD, 2.0, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN4, idE, 2.1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String idN1 = assertBasicOTUEvent(EventContentType.NODE, null, "N1", null, reader);
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
			
			String id1 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
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
			
			String id1 = assertBasicOTUEvent(EventContentType.NODE, null, "A", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertBasicOTUEvent(EventContentType.NODE, null, "B", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertBasicOTUEvent(EventContentType.NODE, null, "C", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertBasicOTUEvent(EventContentType.NODE, null, "D", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
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
			
			String id1 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, .3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, .4, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertBasicOTUEvent(EventContentType.NODE, null, null, null, reader);
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
