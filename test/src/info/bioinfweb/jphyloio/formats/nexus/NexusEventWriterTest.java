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
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
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
	@Test
	public void test_writeDocument() throws Exception {
		File file = new File("data/testOutput/Test.nex");
		
		// Write file:
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		SingleTokenTestMatrixDataAdapter matrix = new SingleTokenTestMatrixDataAdapter(true, 
				"Label 1", "ACTGC", null, "A-TCC", null, "ACTTC");
		TestOTUListDataAdapter otuList = matrix.createAccordingOTUList(0); 
		document.getOtuListsMap().put(otuList.getID(), otuList);
		document.getMatrices().add(matrix);
		
		String otuID = ReadWriteConstants.DEFAULT_OTU_ID_PREFIX + "2";
		otuList.getOtus().put(otuID, new LabeledIDEvent(EventContentType.OTU, otuID, null));  // Set last OTU label to null
		
		matrix.setLinkedOTUsID("otus0");
		matrix.setTokenSets(new TestSingleTokenSetAdapter());
		
		document.getTreesNetworks().add(new TestTreeDataAdapter());
		document.getTreesNetworks().add(new TestTreeDataAdapter());
		
		NexusEventWriter writer = new NexusEventWriter();
		EventWriterParameterMap parameterMap = new EventWriterParameterMap();
		parameterMap.put(EventWriterParameterMap.KEY_APPLICATION_COMMENT, "Some application comment.");
		writer.writeDocument(document, file, parameterMap);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(FIRST_LINE, reader.readLine());
			assertEquals(COMMENT_START + "Some application comment." + COMMENT_END, reader.readLine());
			assertTrue(reader.readLine().matches(
					"\\[This file was generated by an application using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+. <http://bioinfweb.info/JPhyloIO/>\\]"));
			assertEquals("", reader.readLine());
			
			assertEquals("BEGIN TAXA;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=3;", reader.readLine());
			assertEquals("\tTAXLABELS", reader.readLine());
			assertEquals("\t\t\tOTU_otu0", reader.readLine());
			assertEquals("\t\t\tOTU_otu1", reader.readLine());
			assertEquals("\t\t\totu2;", reader.readLine());  // No label present.
			assertEquals("END;", reader.readLine());
			assertEquals("", reader.readLine());
			//TODO Taxa need to have unique names. (Two labels must not be identical and a label and an ID must not be identical.)
			
			assertEquals("BEGIN CHARACTERS;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX=3 NCHAR=5;", reader.readLine());
			assertEquals("\tFORMAT DATATYPE=DNA GAP=- MISSING=? MATCHCHAR=. SYMBOLS=\"A T C G\";", reader.readLine());
			assertEquals("\tMATRIX", reader.readLine());
			assertEquals("\t\t\tOTU_otu0 ACT[comment 1]GC", reader.readLine());  // Currently the sequence label "Label_1" will be used.
			assertEquals("\t\t\tOTU_otu1 A-TCC", reader.readLine());
			assertEquals("\t\t\totu2 ACTTC;", reader.readLine());  // Using the OTU ID here is currently not implemented. (Currently the sequence ID will be used.)
			assertEquals("END;", reader.readLine());
			//TODO Taxon names and sequence names must match. The sequence label must not be used, if an OTU is linked.
			//     In practice OTU and sequence labels should be identical.
			//     => Should the JPhyloIO model be changed to solve such problems (e.g. by removing some properties or forcing some of them to be equal)?
			//        -> Probably not, because writing different sequence and OTU labels into NeXML should be possible.
			//TODO If sequences without linked OTUs are written, the according format subcommand must be specified. Therefore JPhyloIO needs to know that before the sequences are written. => Probably an additional adapter method

			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
}
