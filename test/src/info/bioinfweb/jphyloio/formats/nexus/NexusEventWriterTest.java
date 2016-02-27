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
package info.bioinfweb.jphyloio.formats.nexus;


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.*;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.LabelEditingReporter;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.SharedOTUTestMatrixAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.SingleTokenTestMatrixDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestMatrixDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestOTUListDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestSingleTokenSetAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeDataAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;



public class NexusEventWriterTest implements NexusConstants {
	private void testNodeLabelMappings(LabelEditingReporter reporter, String idPrefix) {
		assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "Node '_1", EventContentType.NODE, idPrefix + "n1", 
				reporter);
		assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "Node " + idPrefix + "nRoot", EventContentType.NODE, 
				idPrefix + "nRoot", reporter);
		assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "Node " + idPrefix + "nA", EventContentType.NODE, 
				idPrefix + "nA", reporter);
		assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "Node " + idPrefix + "nB", EventContentType.NODE, 
				idPrefix + "nB", reporter);
		assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "Node " + idPrefix + "nC", EventContentType.NODE, 
				idPrefix + "nC", reporter);
	}
	
	
	@Test
	public void test_writeDocument_indicesAsNodeLabels() throws Exception {
		testwriteDocument(false, true);
	}

	
	@Test
	public void test_writeDocument_alwaysNodeLabels() throws Exception {
		testwriteDocument(true, false);
	}

	
	public void testwriteDocument(boolean writeNodeLabels, boolean generateTranslationTable) throws Exception {
		File file = new File("data/testOutput/Test_" + writeNodeLabels + "_" + generateTranslationTable + ".nex");
		
		// Write file:
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		SingleTokenTestMatrixDataAdapter matrix = new SingleTokenTestMatrixDataAdapter("matrix0", "a matrix", true, 
				"Label 1", "ACTGC", null, "A-TCC", null, "ACTTC");
		TestOTUListDataAdapter otuList = matrix.createAccordingOTUList(0); 
		document.getOTUListsMap().put(otuList.getListStartEvent().getID(), otuList);
		document.getMatrices().add(matrix);
		
		String otuID = ReadWriteConstants.DEFAULT_OTU_ID_PREFIX + "2";
		otuList.getOtus().put(otuID, new LabeledIDEvent(EventContentType.OTU, otuID, null));  // Set last OTU label to null
		
		matrix.setLinkedOTUsID("otus0");
		matrix.setTokenSets(new TestSingleTokenSetAdapter());
		
		document.getTreesNetworks().add(new TestTreeDataAdapter("tree0", "tree", "t0"));
		TestTreeDataAdapter tree = new TestTreeDataAdapter("tree1", "tree", "t1", new String[]{"otu0", "otu1", "otu2"});
		tree.setLinkedOTUsID("otus0");
		document.getTreesNetworks().add(tree);
		document.getTreesNetworks().add(new TestTreeDataAdapter("tree2", "tree", "t2"));
		
		NexusEventWriter writer = new NexusEventWriter();
		EventWriterParameterMap parameters = new EventWriterParameterMap();
		parameters.put(EventWriterParameterMap.KEY_APPLICATION_COMMENT, "Some application comment.");
		parameters.put(EventWriterParameterMap.KEY_ALWAYS_WRITE_NEXUS_NODE_LABELS, writeNodeLabels);
		parameters.put(EventWriterParameterMap.KEY_GENERATE_NEXUS_TRANSLATION_TABLE, generateTranslationTable);
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(FIRST_LINE, reader.readLine());
			assertEquals(COMMENT_START + "Some application comment." + COMMENT_END, reader.readLine());
			assertTrue(reader.readLine().matches(
					"\\[This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+. <http://bioinfweb.info/JPhyloIO/>\\]"));
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN TAXA;", reader.readLine());
			assertEquals("\tTITLE OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=3;", reader.readLine());
			assertEquals("\tTAXLABELS", reader.readLine());
			assertEquals("\t\t\tOTU_otu0", reader.readLine());
			assertEquals("\t\t\tOTU_otu1", reader.readLine());
			assertEquals("\t\t\totu2;", reader.readLine());  // No label present.
			assertEquals("END;", reader.readLine());
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN CHARACTERS;", reader.readLine());
			assertEquals("\tTITLE a_matrix;", reader.readLine());
			assertEquals("\tLINK TAXA=OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=3 NCHAR=5;", reader.readLine());
			assertEquals("\tFORMAT DATATYPE=DNA GAP=- MISSING=? MATCHCHAR=. SYMBOLS=\"A T C G\" NOTOKENS;", reader.readLine());
			assertEquals("\tMATRIX", reader.readLine());
			assertEquals("\t\t\tOTU_otu0 ACT[comment 1]GC", reader.readLine());
			assertEquals("\t\t\tOTU_otu1 A-TCC", reader.readLine());
			assertEquals("\t\t\totu2 ACTTC;", reader.readLine());
			assertEquals("END;", reader.readLine());
			assertEquals("", reader.readLine());

			assertEquals("BEGIN TREES;", reader.readLine());
			assertEquals("\tTITLE Trees_linked_to_no_TAXA_block;", reader.readLine());
			assertEquals("\tTREE tree = [&R] ((Node_t0nA:1.1[&annotation=100], Node_t0nB:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, Node_t0nC:2.0)Node_t0nRoot:1.5;", reader.readLine());
			assertEquals("\tTREE 'tree2_tree' = [&R] ((Node_t2nA:1.1[&annotation=100], Node_t2nB:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, Node_t2nC:2.0)Node_t2nRoot:1.5;", reader.readLine());
			assertEquals("END;", reader.readLine());
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN TREES;", reader.readLine());
			assertEquals("\tTITLE Trees_linked_to_OTU_list_0;", reader.readLine());
			assertEquals("\tLINK TAXA=OTU_list_0;", reader.readLine());
			if (generateTranslationTable) {
				assertEquals("\tTRANSLATE", reader.readLine());
				assertEquals("\t\t\t1 OTU_otu0,", reader.readLine());
				assertEquals("\t\t\t2 OTU_otu1,", reader.readLine());
				assertEquals("\t\t\t3 otu2;", reader.readLine());
			}
			if (writeNodeLabels) {
				assertEquals("\tTREE tree = [&R] ((OTU_otu0:1.1[&annotation=100], OTU_otu1:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, otu2:2.0)Node_t1nRoot:1.5;", reader.readLine());
			}
			else {
				assertEquals("\tTREE tree = [&R] ((1:1.1[&annotation=100], 2:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, 3:2.0)Node_t1nRoot:1.5;", reader.readLine());
			}
			assertEquals("END;", reader.readLine());
			
			LabelEditingReporter reporter = parameters.getLabelEditingReporter();
			testNodeLabelMappings(reporter, "t0");
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "Node '_1", EventContentType.NODE, "t1n1", 
					reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "Node t1nRoot", EventContentType.NODE, 
					"t1nRoot", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "OTU otu0", EventContentType.NODE, 
					"t1nA", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "OTU otu1", EventContentType.NODE, 
					"t1nB", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu2", EventContentType.NODE, 
					"t1nC", reporter);
			testNodeLabelMappings(reporter, "t2");
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.NOT_FOUND, null, EventContentType.OTU, "otherID", reporter);
			assertTrue(reporter.anyLabelEdited(EventContentType.OTU));

			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void test_writeDocument_conflictingLabels() throws Exception {
		File file = new File("data/testOutput/ConflictingLabels.nex");
		
		// Write file:
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		TestOTUListDataAdapter otuList = new TestOTUListDataAdapter(0, 
				new LabeledIDEvent(EventContentType.OTU, "otu0", "label1"),
				new LabeledIDEvent(EventContentType.OTU, "otu1", null),
				new LabeledIDEvent(EventContentType.OTU, "otu2", "otu0"),
				new LabeledIDEvent(EventContentType.OTU, "otu3", "otu0"),
				new LabeledIDEvent(EventContentType.OTU, "otu4", "otu3_otu0"),
				new LabeledIDEvent(EventContentType.OTU, "otu5", "otu7"),
				new LabeledIDEvent(EventContentType.OTU, "otu6", "otu7_2"),
				new LabeledIDEvent(EventContentType.OTU, "otu7", null));
		document.getOTUListsMap().put(otuList.getListStartEvent().getID(), otuList);
		TestMatrixDataAdapter matrix = new TestMatrixDataAdapter("matrix0", "a matrix", false, 
				"ACGT", "ACCT", "AC-T", "AGGT", "AG-T", "TCGT", "CCGT", "GCGT");
		matrix.setLinkedOTUsID("otus0");
		document.getMatrices().add(matrix);
		
		NexusEventWriter writer = new NexusEventWriter();
		EventWriterParameterMap parameters = new EventWriterParameterMap();
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(FIRST_LINE, reader.readLine());
			assertTrue(reader.readLine().matches(
					"\\[This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+. <http://bioinfweb.info/JPhyloIO/>\\]"));
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN TAXA;", reader.readLine());
			assertEquals("\tTITLE OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=8;", reader.readLine());
			assertEquals("\tTAXLABELS", reader.readLine());
			assertEquals("\t\t\tlabel1", reader.readLine());
			assertEquals("\t\t\totu1", reader.readLine());
			assertEquals("\t\t\totu0", reader.readLine());
			assertEquals("\t\t\t'otu3_otu0'", reader.readLine());
			assertEquals("\t\t\t'otu4_otu3_otu0'", reader.readLine());
			assertEquals("\t\t\totu7", reader.readLine());
			assertEquals("\t\t\t'otu7_2'", reader.readLine());
			assertEquals("\t\t\t'otu7_3';", reader.readLine());
			assertEquals("END;", reader.readLine());
			assertEquals("", reader.readLine());
			
			LabelEditingReporter reporter = parameters.getLabelEditingReporter();
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "label1", EventContentType.OTU, "otu0", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu1", EventContentType.OTU, "otu1", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "otu0", EventContentType.OTU, "otu2", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu3_otu0", EventContentType.OTU, "otu3", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu4_otu3_otu0", EventContentType.OTU, "otu4", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "otu7", EventContentType.OTU, "otu5", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "otu7_2", EventContentType.OTU, "otu6", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu7_3", EventContentType.OTU, "otu7", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.NOT_FOUND, null, EventContentType.OTU, "otherID", reporter);
			assertTrue(reporter.anyLabelEdited(EventContentType.OTU));

			assertEquals("BEGIN CHARACTERS;", reader.readLine());
			assertEquals("\tTITLE a_matrix;", reader.readLine());
			assertEquals("\tLINK TAXA=OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=8 NCHAR=4;", reader.readLine());
			//TODO Some format information should be written. Writer should maybe throw an exception, if according data is missing. 
			//assertEquals("\tFORMAT DATATYPE=DNA GAP=- MISSING=? MATCHCHAR=. SYMBOLS=\"A T C G\" NOTOKENS;", reader.readLine());
			assertEquals("\tMATRIX", reader.readLine());
			assertEquals("\t\t\tlabel1 ACGT", reader.readLine());
			assertEquals("\t\t\totu1 ACCT", reader.readLine());
			assertEquals("\t\t\totu0 AC-T", reader.readLine());
			assertEquals("\t\t\t'otu3_otu0' AGGT", reader.readLine());
			assertEquals("\t\t\t'otu4_otu3_otu0' AG-T", reader.readLine());
			assertEquals("\t\t\totu7 TCGT", reader.readLine());
			assertEquals("\t\t\t'otu7_2' CCGT", reader.readLine());
			assertEquals("\t\t\t'otu7_3' GCGT;", reader.readLine());
			assertEquals("END;", reader.readLine());

			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "label1", EventContentType.SEQUENCE, "seq0", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu1", EventContentType.SEQUENCE, "seq1", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu0", EventContentType.SEQUENCE, "seq2", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu3_otu0", EventContentType.SEQUENCE, "seq3", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu4_otu3_otu0", EventContentType.SEQUENCE, "seq4", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu7", EventContentType.SEQUENCE, "seq5", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu7_2", EventContentType.SEQUENCE, "seq6", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "otu7_3", EventContentType.SEQUENCE, "seq7", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.NOT_FOUND, null, EventContentType.SEQUENCE, "otherID", reporter);
			assertTrue(reporter.anyLabelEdited(EventContentType.SEQUENCE));

			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}


	@Test
	public void test_writeDocument_sameOTU() throws Exception {
		File file = new File("data/testOutput/sameOTU.nex");
		
		// Write file:
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		TestOTUListDataAdapter otuList = new TestOTUListDataAdapter(0, 
				new LabeledIDEvent(EventContentType.OTU, "otu0", "someOTU"));
		document.getOTUListsMap().put(otuList.getListStartEvent().getID(), otuList);
		SharedOTUTestMatrixAdapter matrix = new SharedOTUTestMatrixAdapter("matrix0", "a matrix", "otu0", false, 
				"ACGT", "ACCT");
		matrix.setLinkedOTUsID("otus0");
		document.getMatrices().add(matrix);
		
		NexusEventWriter writer = new NexusEventWriter();
		EventWriterParameterMap parameters = new EventWriterParameterMap();
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(FIRST_LINE, reader.readLine());
			assertTrue(reader.readLine().matches(
					"\\[This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+. <http://bioinfweb.info/JPhyloIO/>\\]"));
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN TAXA;", reader.readLine());
			assertEquals("\tTITLE OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=1;", reader.readLine());
			assertEquals("\tTAXLABELS", reader.readLine());
			assertEquals("\t\t\tsomeOTU;", reader.readLine());
			assertEquals("END;", reader.readLine());
			assertEquals("", reader.readLine());
			
			LabelEditingReporter reporter = parameters.getLabelEditingReporter();
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "someOTU", EventContentType.OTU, "otu0", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.NOT_FOUND, null, EventContentType.OTU, "otherID", reporter);
			assertFalse(reporter.anyLabelEdited(EventContentType.OTU));

			assertEquals("BEGIN CHARACTERS;", reader.readLine());
			assertEquals("\tTITLE a_matrix;", reader.readLine());
			assertEquals("\tLINK TAXA=OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NEWTAXA NTAX=2 NCHAR=4;", reader.readLine());
			assertEquals("\tTAXLABELS", reader.readLine());
			assertEquals("\t\t\tSequence_1;", reader.readLine());
			assertEquals("\t\t\t[These additional taxon definitions were automatically added by JPhyloIO, because sequences without linked taxa had to be written or more than one sequence was linked to the same taxon (which is both invalid in Nexus).]", reader.readLine());
			//TODO Some format information should be written. Writer should maybe throw an exception, if according data is missing. 
			//assertEquals("\tFORMAT DATATYPE=DNA GAP=- MISSING=? MATCHCHAR=. SYMBOLS=\"A T C G\" NOTOKENS;", reader.readLine());
			assertEquals("\tMATRIX", reader.readLine());
			assertEquals("\t\t\tsomeOTU ACGT", reader.readLine());
			assertEquals("\t\t\tSequence_1 ACCT;", reader.readLine());
			assertEquals("END;", reader.readLine());

			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.EDITED, "someOTU", EventContentType.SEQUENCE, "seq0", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.UNCHANGED, "Sequence 1", EventContentType.SEQUENCE, "seq1", reporter);
			assertEditedLabelMapping(LabelEditingReporter.LabelStatus.NOT_FOUND, null, EventContentType.SEQUENCE, "otherID", reporter);
			assertFalse(reporter.anyLabelEdited(EventContentType.OTU));
			
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}		
	}

	
	private ListBasedDocumentDataAdapter createUnequalLengthDocument() {
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		TestOTUListDataAdapter otuList = new TestOTUListDataAdapter(0, 
				new LabeledIDEvent(EventContentType.OTU, "otu0", "long"),
				new LabeledIDEvent(EventContentType.OTU, "otu1", "short"));
		document.getOTUListsMap().put(otuList.getListStartEvent().getID(), otuList);
		TestMatrixDataAdapter matrix = new TestMatrixDataAdapter("matrix0", "a matrix", false, "A-CGTT", "ACCT");
		matrix.setLinkedOTUsID("otus0");
		document.getMatrices().add(matrix);
		return document;
	}
	

	@Test
	public void test_writeDocument_extendWithGaps() throws Exception {
		File file = new File("data/testOutput/extendWithGaps.nex");
		
		// Write file:
		ListBasedDocumentDataAdapter document = createUnequalLengthDocument();
		NexusEventWriter writer = new NexusEventWriter();
		EventWriterParameterMap parameters = new EventWriterParameterMap();
		parameters.put(EventWriterParameterMap.KEY_SEQUENCE_EXTENSION_TOKEN, "?");
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(FIRST_LINE, reader.readLine());
			assertTrue(reader.readLine().matches(
					"\\[This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+. <http://bioinfweb.info/JPhyloIO/>\\]"));
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN TAXA;", reader.readLine());
			assertEquals("\tTITLE OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=2;", reader.readLine());
			assertEquals("\tTAXLABELS", reader.readLine());
			assertEquals("\t\t\tlong", reader.readLine());
			assertEquals("\t\t\tshort;", reader.readLine());
			assertEquals("END;", reader.readLine());
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN CHARACTERS;", reader.readLine());
			assertEquals("\tTITLE a_matrix;", reader.readLine());
			assertEquals("\tLINK TAXA=OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=2 NCHAR=6;", reader.readLine());
			//TODO Some format information should be written. Writer should maybe throw an exception, if according data is missing. 
			//assertEquals("\tFORMAT DATATYPE=DNA GAP=- MISSING=? MATCHCHAR=. SYMBOLS=\"A T C G\" NOTOKENS;", reader.readLine());
			assertEquals("\tMATRIX", reader.readLine());
			assertEquals("\t\t\tlong A-CGTT", reader.readLine());
			assertEquals("\t\t\tshort ACCT??;", reader.readLine());
			assertEquals("END;", reader.readLine());
			
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}		
	}
	

	@Test
	public void test_writeDocument_unaligned() throws Exception {
		File file = new File("data/testOutput/unaligned.nex");
		
		// Write file:
		ListBasedDocumentDataAdapter document = createUnequalLengthDocument();
		NexusEventWriter writer = new NexusEventWriter();
		EventWriterParameterMap parameters = new EventWriterParameterMap();
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(FIRST_LINE, reader.readLine());
			assertTrue(reader.readLine().matches(
					"\\[This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+. <http://bioinfweb.info/JPhyloIO/>\\]"));
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN TAXA;", reader.readLine());
			assertEquals("\tTITLE OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=2;", reader.readLine());
			assertEquals("\tTAXLABELS", reader.readLine());
			assertEquals("\t\t\tlong", reader.readLine());
			assertEquals("\t\t\tshort;", reader.readLine());
			assertEquals("END;", reader.readLine());
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN UNALIGNED;", reader.readLine());
			assertEquals("\tTITLE a_matrix;", reader.readLine());
			assertEquals("\tLINK TAXA=OTU_list_0;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=2;", reader.readLine());
			//TODO Some format information should be written. Writer should maybe throw an exception, if according data is missing. 
			//assertEquals("\tFORMAT DATATYPE=DNA GAP=- MISSING=? MATCHCHAR=. SYMBOLS=\"A T C G\" NOTOKENS;", reader.readLine());
			assertEquals("\tMATRIX", reader.readLine());
			assertEquals("\t\t\tlong A-CGTT,", reader.readLine());
			assertEquals("\t\t\tshort ACCT;", reader.readLine());
			assertEquals("END;", reader.readLine());
			
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}		
	}
}
