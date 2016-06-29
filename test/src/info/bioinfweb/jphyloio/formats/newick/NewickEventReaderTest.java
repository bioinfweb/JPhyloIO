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


import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;

import java.io.File;
import java.util.Arrays;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class NewickEventReaderTest implements ReadWriteConstants, NewickConstants, PhyloXMLConstants {
	@Test
	public void test_readNextEvent_InternalsTerminalsLength() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/InternalsTerminalsLength.nwk"), 
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertNodeEvent(null, "A", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idB = assertNodeEvent(null, "B", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN3 = assertNodeEvent(null, "N3", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN3, idA, 1.05, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN3, idB, 1.0, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String idC = assertNodeEvent(null, "C", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN2 = assertNodeEvent(null, "N2", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idN3, 1.5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idC, 2.5, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String idD = assertNodeEvent(null, "D", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idE = assertNodeEvent(null, "E", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN4 = assertNodeEvent(null, "N4", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN4, idD, 2.0, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN4, idE, 2.1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			String idN1 = assertNodeEvent(null, "N1", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idN2, .8, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idN4, 1.4, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			
			assertEdgeEvent(null, idN1, reader);
			assertEndEvent(EventContentType.EDGE, reader);			

			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertNodeEvent(null, null, false, null, reader);
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
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertNodeEvent(null, "A", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertNodeEvent(null, "B", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertNodeEvent(null, "C", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertNodeEvent(null, "D", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertNodeEvent(null, null, false, null, reader);
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
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String id1 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3_1 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			
			String id3 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, .3, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, .4, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);

			String id0 = assertNodeEvent(null, null, false, null, reader);
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
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_NoTrees() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/NoTrees.nwk"), new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertNodeEvent(null, "A", false, null, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob", PREDICATE_HAS_LITERAL_METADATA), null, "1.000000000000000e+000", null, new Double(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob_stddev", PREDICATE_HAS_LITERAL_METADATA), null, "0", null, new Double(0.0), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("prob_range", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE,  
					new URIOrStringIdentifier(null, DATA_TYPE_NEWICK_ARRAY), null, reader);
			assertLiteralMetaContentEvent("{1.000000000000000e+000,1}", Arrays.asList(new Double(1.0), new Double(1.0)), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob(percent)", PREDICATE_HAS_LITERAL_METADATA), null, "100", null, "100", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob+-sd", PREDICATE_HAS_LITERAL_METADATA), null, "100+-0", null, "100+-0", true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idB = assertNodeEvent(null, "B", false, null, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob", PREDICATE_HAS_LITERAL_METADATA), null, "A, =B", null, "A, =B", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob_stddev", PREDICATE_HAS_LITERAL_METADATA), null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("prob_range", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE,  
					new URIOrStringIdentifier(null, DATA_TYPE_NEWICK_ARRAY), null, reader);
			assertLiteralMetaContentEvent("{\"AB \"\"C\", ABC}", Arrays.asList("AB \"C", "ABC"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob(percent)", PREDICATE_HAS_LITERAL_METADATA), null, "100", null, "100", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob+-sd", PREDICATE_HAS_LITERAL_METADATA), null, "100+-0", null, "100+-0", true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idC = assertNodeEvent(null, "C", false, null, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob", PREDICATE_HAS_LITERAL_METADATA), null, "6.364056912805381e-001", null, new Double(6.364056912805381e-001), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob_stddev", PREDICATE_HAS_LITERAL_METADATA), null, "7.249475639180907e-004", null, new Double(7.249475639180907e-004), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("prob_range", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE,  
					new URIOrStringIdentifier(null, DATA_TYPE_NEWICK_ARRAY), null, reader);
			assertLiteralMetaContentEvent("{6.358930759420870e-001,6.369183066189893e-001}", 
					Arrays.asList(new Double(6.358930759420870e-001), new Double(6.369183066189893e-001)), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob(percent)", PREDICATE_HAS_LITERAL_METADATA), null, "64", null, "64", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob+-sd", PREDICATE_HAS_LITERAL_METADATA), null, "64+-0", null, "64+-0", true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			
			String idN1 = assertNodeEvent(null, null, false, null, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob", PREDICATE_HAS_LITERAL_METADATA), null, "1.000000000000000e+000", null, new Double(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob_stddev", PREDICATE_HAS_LITERAL_METADATA), null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("prob_range", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE,  
					new URIOrStringIdentifier(null, DATA_TYPE_NEWICK_ARRAY), null, reader);
			assertLiteralMetaContentEvent("{1.000000000000000e+000,1.000000000000000e+000}", Arrays.asList(new Double(1.0), new Double(1.0)), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob(percent)", PREDICATE_HAS_LITERAL_METADATA), null, "100", null, "100", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("prob+-sd", PREDICATE_HAS_LITERAL_METADATA), null, "100+-0", null, "100+-0", true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN1, idB, 6.244293083853111e-001, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_mean", PREDICATE_HAS_LITERAL_METADATA), null, "6.345415111023917e-001", null, new Double(6.345415111023917e-001), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_median", PREDICATE_HAS_LITERAL_METADATA), null, "6.244293083853111e-001", null, new Double(6.244293083853111e-001), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("length_95%HPD", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE,  
					new URIOrStringIdentifier(null, DATA_TYPE_NEWICK_ARRAY), null, reader);
			assertLiteralMetaContentEvent("{4.360295861156825e-001,8.441623753050405e-001}", 
					Arrays.asList(new Double(4.360295861156825e-001), new Double(8.441623753050405e-001)), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN1, idC, 7.039004236028111e-002, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_mean", PREDICATE_HAS_LITERAL_METADATA), null, "7.419012044002400e-002", null, new Double(7.419012044002400e-002), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_median", PREDICATE_HAS_LITERAL_METADATA), null, "7.039004236028111e-002", null, new Double(7.039004236028111e-002), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("length_95%HPD", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE,  
					new URIOrStringIdentifier(null, DATA_TYPE_NEWICK_ARRAY), null, reader);
			assertLiteralMetaContentEvent("{9.114712766459516e-003,1.418351647155842e-001}", 
					Arrays.asList(new Double(9.114712766459516e-003), new Double(1.418351647155842e-001)), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);

			String idN2 = assertNodeEvent(null, null, false, null, reader);
			assertCommentEvent("18", false, reader);
			//assertMetaEvent(HotCommentDataReader.UNNAMED_NODE_DATA_NAME, "18", null, new Double(18), true, true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN2, idA, .3682008685714568, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_mean", PREDICATE_HAS_LITERAL_METADATA), null, "3.744759260623280e-001", null, new Double(3.744759260623280e-001), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_median", PREDICATE_HAS_LITERAL_METADATA), null, "3.682008685714568e-001", null, new Double(3.682008685714568e-001), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("length_95%HPD", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE,  
					new URIOrStringIdentifier(null, DATA_TYPE_NEWICK_ARRAY), null, reader);
			assertLiteralMetaContentEvent("{2.494893056441154e-001, \"A, =B\"}", Arrays.asList(new Double(2.494893056441154e-001), "A, =B"), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN2, idN1, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_mean", PREDICATE_HAS_LITERAL_METADATA), null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("length_median", PREDICATE_HAS_LITERAL_METADATA), null, "0.000000000000000e+000", null, new Double(0.0), true, reader);
			assertLiteralMetaStartEvent(new URIOrStringIdentifier("length_95%HPD", PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE,  
					new URIOrStringIdentifier(null, DATA_TYPE_NEWICK_ARRAY), null, reader);
			assertLiteralMetaContentEvent("{}", Arrays.asList(), true, reader);
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
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertNodeEvent(null, "A", false, null, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("a", PREDICATE_HAS_LITERAL_METADATA), null, "1", null, new Double(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("b", PREDICATE_HAS_LITERAL_METADATA), null, "2", null, new Double(2.0), true, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idB = assertNodeEvent(null, "B", false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idC = assertNodeEvent(null, "C", false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			String idN1 = assertNodeEvent(null, null, false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN1, idB, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN1, idC, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			String idN2 = assertNodeEvent(null, null, false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN2, idA, 18.0, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("c", PREDICATE_HAS_LITERAL_METADATA), null, "3", null, new Double(3.0), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(idN2, idN1, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(null, idN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			String id1 = assertNodeEvent(null, "A", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id2 = assertNodeEvent(null, "B", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_1 = assertNodeEvent(null, "C", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3_2 = assertNodeEvent(null, "D", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String id3 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(id3, id3_2, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			String id0 = assertNodeEvent(null, null, false, null, reader);
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
			String idA = assertNodeEvent(null, "A", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idB = assertNodeEvent(null, "B", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idC = assertNodeEvent(null, "C", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			String idN1 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idB, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idC, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			String idN2 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idA, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idN1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, idN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);

			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			idA = assertNodeEvent(null, "A", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			idB = assertNodeEvent(null, "B", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			idN1 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idA, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN1, idB, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			idC = assertNodeEvent(null, "C", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			idN2 = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idN1, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(idN2, idC, reader);
			assertEventType(EventContentType.EDGE, EventTopologyType.END, reader);
			assertEdgeEvent(null, idN2, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);

			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idADH2 = assertNodeEvent(null, "ADH2", false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String idADH1 = assertNodeEvent(null, "ADH1", false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String idN1 = assertNodeEvent(null, null, false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(idN1, idADH2, 0.1, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:S", PREDICATE_TAXONOMY_SCIENTIFIC_NAME), null, "hu\"man", null, "hu\"man", true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(idN1, idADH1, 0.11, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:S", PREDICATE_TAXONOMY_SCIENTIFIC_NAME), null, "human", null, "human", true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			String idADHX = assertNodeEvent(null, "ADHX", false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String idN2 = assertNodeEvent(null, null, false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);

			assertEdgeEvent(idN2, idN1, 0.05, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:S", PREDICATE_TAXONOMY_SCIENTIFIC_NAME), null, "primates", null, "primates", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:D", PREDICATE_HAS_LITERAL_METADATA), null, "Y", null, "Y", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:B", PREDICATE_CONFIDENCE_VALUE), null, "100", null, new Double(100), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(idN2, idADHX, 0.12, reader);
			assertCommentEvent("&&NHX:=insect", reader);  // Not a valid NHX hot comment.
			assertEndEvent(EventContentType.EDGE, reader);
			
			String idADH3 = assertNodeEvent(null, "ADH3", false, null, reader);
			assertEndEvent(EventContentType.NODE, reader);
			String idN3 = assertNodeEvent(null, null, false, null, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:D", PREDICATE_HAS_LITERAL_METADATA), null, "N", null, "N", true, reader);  // This metadata should theoretically be associated with the root branch, since NHX does not offer attaching data to nodes instead of branches.
			assertEndEvent(EventContentType.NODE, reader);

			assertEdgeEvent(idN3, idN2, 0.1, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:S", PREDICATE_TAXONOMY_SCIENTIFIC_NAME), null, "metazoa", null, "metazoa", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:D", PREDICATE_HAS_LITERAL_METADATA), null, "N", null, "N", true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			assertEdgeEvent(idN3, idADH3, 0.1, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier("NHX:S", PREDICATE_TAXONOMY_SCIENTIFIC_NAME), null, "Fungi", null, "Fungi", true, reader);
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
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
//			String idA = assertNodeEvent(null, "A", null, reader);
//			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);
//
//			assertEdgeEvent(null, idA, Double.NaN, reader);
//			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertNodeEvent(null, "A", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);

			assertEdgeEvent(null, idA, Double.NaN, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
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
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertNodeEvent(null, null, false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);

			assertEdgeEvent(null, idA, 2.0, reader);
			assertCommentEvent("comment", reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_readNextEvent_OneNodeNameAndLength() throws Exception {
		NewickEventReader reader = new NewickEventReader(new File("data/Newick/OneNodeNameAndLength.nwk"),
				new ReadWriteParameterMap());
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertEventType(EventContentType.TREE, EventTopologyType.START, reader);
			
			String idA = assertNodeEvent(null, "A", false, null, reader);
			assertEventType(EventContentType.NODE, EventTopologyType.END, reader);

			assertEdgeEvent(null, idA, 2.0, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEventType(EventContentType.TREE, EventTopologyType.END, reader);
			assertCommentEvent("comment", reader);
			assertEventType(EventContentType.TREE_NETWORK_GROUP, EventTopologyType.END, reader);
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.END, reader);
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}
	}
}
