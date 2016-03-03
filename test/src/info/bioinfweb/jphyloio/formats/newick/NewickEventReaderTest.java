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


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.File;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class NewickEventReaderTest {
	@Test
	public void test_readNextEvent_InternalsTerminalsLength() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/InternalsTerminalsLength.nwk"), 
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idB = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "B", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN3 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "N3", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN3, idA, 1.05, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN3, idB, 1.0, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String idC = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "C", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "N2", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idN3, 1.5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idC, 2.5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String idD = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "D", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idE = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "E", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN4 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "N4", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN4, idD, 2.0, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN4, idE, 2.1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String idN1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "N1", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idN2, .8, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idN4, 1.4, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			assertEdgeEvent(null, idN1, reader);
			assertEndEvent(EventContentType.EDGE, reader);			

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
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/NoNamedNodes.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			assertEdgeEvent(null, id0, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
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
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/LeafNodesNamed.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "B", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "C", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "D", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			assertEdgeEvent(null, id0, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
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
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/OnlyBranchLengths.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, .3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, .4, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id1, .1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id2, .2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id3, .5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			assertEdgeEvent(null, id0, 0.0, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_NoTrees() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/NoTrees.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_Metadata() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/Metadata.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
			assertMetaEvent("prob", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_stddev", "0", null, new Double(0.0), true, true, reader);
			assertMetaEvent("prob_range", null, null, null, false, true, reader);
			assertMetaEvent("prob_range[0]", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_range[1]", "1", null, new Double(1.0), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertMetaEvent("prob(percent)", "100", null, "100", true, true, reader);
			assertMetaEvent("prob+-sd", "100+-0", null, "100+-0", true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idB = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "B", null, reader);
			assertMetaEvent("prob", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_stddev", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("prob_range", null, null, null, false, true, reader);
			assertMetaEvent("prob_range[0]", "AB \"C", null, "AB \"C", true, true, reader);
			assertMetaEvent("prob_range[1]", "ABC", null, "ABC", true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertMetaEvent("prob(percent)", "100", null, "100", true, true, reader);
			assertMetaEvent("prob+-sd", "100+-0", null, "100+-0", true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idC = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "C", null, reader);
			assertMetaEvent("prob", "6.364056912805381e-001", null, new Double(6.364056912805381e-001), true, true, reader);
			assertMetaEvent("prob_stddev", "7.249475639180907e-004", null, new Double(7.249475639180907e-004), true, true, reader);	
			assertMetaEvent("prob_range", null, null, null, false, true, reader);
			assertMetaEvent("prob_range[0]", "6.358930759420870e-001", null, new Double(6.358930759420870e-001), true, true, reader);
			assertMetaEvent("prob_range[1]", "6.369183066189893e-001", null, new Double(6.369183066189893e-001), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertMetaEvent("prob(percent)", "64", null, "64", true, true, reader);
			assertMetaEvent("prob+-sd", "64+-0", null, "64+-0", true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			
			String idN1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertMetaEvent("prob", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_stddev", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("prob_range", null, null, null, false, true, reader);
			assertMetaEvent("prob_range[0]", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertMetaEvent("prob_range[1]", "1.000000000000000e+000", null, new Double(1.0), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertMetaEvent("prob(percent)", "100", null, "100", true, true, reader);
			assertMetaEvent("prob+-sd", "100+-0", null, "100+-0", true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN1, idB, 6.244293083853111e-001, reader);
			assertMetaEvent("length_mean", "6.345415111023917e-001", null, new Double(6.345415111023917e-001), true, true, reader);
			assertMetaEvent("length_median", "6.244293083853111e-001", null, new Double(6.244293083853111e-001), true, true, reader);
			assertMetaEvent("length_95%HPD", null, null, null, false, true, reader);
			assertMetaEvent("length_95%HPD[0]", "4.360295861156825e-001", null, new Double(4.360295861156825e-001), true, true, reader);
			assertMetaEvent("length_95%HPD[1]", "8.441623753050405e-001", null, new Double(8.441623753050405e-001), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN1, idC, 7.039004236028111e-002, reader);
			assertMetaEvent("length_mean", "7.419012044002400e-002", null, new Double(7.419012044002400e-002), true, true, reader);
			assertMetaEvent("length_median", "7.039004236028111e-002", null, new Double(7.039004236028111e-002), true, true, reader);
			assertMetaEvent("length_95%HPD", null, null, null, false, true, reader);
			assertMetaEvent("length_95%HPD[0]", "9.114712766459516e-003", null, new Double(9.114712766459516e-003), true, true, reader);
			assertMetaEvent("length_95%HPD[1]", "1.418351647155842e-001", null, new Double(1.418351647155842e-001), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertEndEvent(EventContentType.EDGE, reader);

			String idN2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertCommentEvent("18", false, reader);
			//assertMetaEvent(HotCommentDataReader.UNNAMED_NODE_DATA_NAME, "18", null, new Double(18), true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN2, idA, .3682008685714568, reader);
			assertMetaEvent("length_mean", "3.744759260623280e-001", null, new Double(3.744759260623280e-001), true, true, reader);
			assertMetaEvent("length_median", "3.682008685714568e-001", null, new Double(3.682008685714568e-001), true, true, reader);
			assertMetaEvent("length_95%HPD", null, null, null, false, true, reader);
			assertMetaEvent("length_95%HPD[0]", "2.494893056441154e-001", null, new Double(2.494893056441154e-001), true, true, reader);
			assertMetaEvent("length_95%HPD[1]", "5.088322191162278e-001", null, new Double(5.088322191162278e-001), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN2, idN1, reader);
			assertMetaEvent("length_mean", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("length_median", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("length_95%HPD", null, null, null, false, true, reader);
			assertMetaEvent("length_95%HPD[0]", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertMetaEvent("length_95%HPD[1]", "0.000000000000000e+000", null, new Double(0.0), true, true, reader);
			assertEndEvent(EventContentType.META_INFORMATION, reader);
			assertCommentEvent("comment 1", false, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(null, idN2, 0.0, reader);
			assertCommentEvent("20", false, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertNotEquals(idA, idB);
			assertNotEquals(idA, idC);
			assertNotEquals(idA, idN1);
			assertNotEquals(idA, idN2);
			assertNotEquals(idB, idC);
			assertNotEquals(idB, idN1);
			assertNotEquals(idB, idN2);
			assertNotEquals(idC, idN1);
			assertNotEquals(idC, idN2);
			assertNotEquals(idN1, idN2);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_ProblematicComments() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/ProblematicComments.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
			assertMetaEvent("a", "1", null, new Double(1.0), true, true, reader);
			assertMetaEvent("b", "2", null, new Double(2.0), true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idB = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "B", null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idC = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "C", null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idN1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN1, idB, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN1, idC, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			String idN2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN2, idA, 18.0, reader);
			assertMetaEvent("c", "3", null, new Double(3.0), true, true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN2, idN1, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(null, idN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_MultipleTrees() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/MultipleTrees.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			String id1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "B", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "C", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "D", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			String id0 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id0, id3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, id0, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			String idA = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idB = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "B", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idC = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "C", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idB, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idC, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			String idN2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idA, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idN1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, idN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);

			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			idA = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			idB = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "B", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			idN1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idA, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idB, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			idC = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "C", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			idN2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idN1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idC, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, idN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);

			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}

	
	@Test
	public void test_readNextEvent_NHX2() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/NHX2.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idADH2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "ADH2", null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String idADH1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "ADH1", null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String idN1 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN1, idADH2, 0.1, reader);
			assertMetaEvent("NHX:S", "hu\"man", null, "hu\"man", true, true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(idN1, idADH1, 0.11, reader);
			assertMetaEvent("NHX:S", "human", null, "human", true, true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			String idADHX = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "ADHX", null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String idN2 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEndEvent(EventContentType.NODE, reader);

			assertEdgeEvent(idN2, idN1, 0.05, reader);
			assertMetaEvent("NHX:S", "primates", null, "primates", true, true, reader);
			assertMetaEvent("NHX:D", "Y", null, "Y", true, true, reader);
			assertMetaEvent("NHX:B", "100", null, new Double(100), true, true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(idN2, idADHX, 0.12, reader);
			assertCommentEvent("&&NHX:=insect", reader);  // Not a valid NHX hot comment.
			assertEndEvent(EventContentType.EDGE, reader);
			
			String idADH3 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "ADH3", null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String idN3 = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertMetaEvent("NHX:D", "N", null, "N", true, true, reader);  // This metadata should theoretically be associated with the root branch, since NHX does not offer attaching data to nodes instead of branches.
			assertEndEvent(EventContentType.NODE, reader);

			assertEdgeEvent(idN3, idN2, 0.1, reader);
			assertMetaEvent("NHX:S", "metazoa", null, "metazoa", true, true, reader);
			assertMetaEvent("NHX:D", "N", null, "N", true, true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(idN3, idADH3, 0.1, reader);
			assertMetaEvent("NHX:S", "Fungi", null, "Fungi", true, true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(null, idN3, Double.NaN, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertNotEquals(idADH2, idADH1);
			assertNotEquals(idADH2, idN1);
			assertNotEquals(idADH2, idADHX);
			assertNotEquals(idADH2, idN2);
			assertNotEquals(idADH2, idADH3);
			assertNotEquals(idADH2, idN3);
			assertNotEquals(idADH1, idN1);
			assertNotEquals(idADH1, idADHX);
			assertNotEquals(idADH1, idN2);
			assertNotEquals(idADH1, idADH3);
			assertNotEquals(idADH1, idN3);
			assertNotEquals(idN1, idADHX);
			assertNotEquals(idN1, idN2);
			assertNotEquals(idN1, idADH3);
			assertNotEquals(idN1, idN3);
			assertNotEquals(idADHX, idN2);
			assertNotEquals(idADHX, idADH3);
			assertNotEquals(idADHX, idN3);
			assertNotEquals(idADHX, idADH3);
			assertNotEquals(idADHX, idN3);
			assertNotEquals(idADH3, idN3);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEndEvent(EventContentType.DOCUMENT, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_OneNodeNone() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/OneNodeNone.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
//			String idA = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
//			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//
//			assertEdgeEvent(null, idA, Double.NaN, reader);
//			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_OneNodeName() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/OneNodeName.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, "A", null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);

			assertEdgeEvent(null, idA, Double.NaN, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_OneNodeLength() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/OneNodeLength.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertLinkedOTUOrOTUsEvent(EventContentType.NODE, null, null, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);

			assertEdgeEvent(null, idA, 2.0, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
}
