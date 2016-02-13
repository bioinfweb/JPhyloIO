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
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
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
		ListBasedDocumentDataAdapter document = createTestDocumentWithLabels("Label 1", "ACTGC", null, "A-TCC", null, "ACTTC");
		document.getTreesNetworks().add(new TestTreeDataAdapter());
		document.getTreesNetworks().add(new TestTreeDataAdapter());
		NexusEventWriter writer = new NexusEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(FIRST_LINE, reader.readLine());
			assertTrue(reader.readLine().matches(
					"\\[This file was generated using JPhyloIO \\d+\\.\\d+\\.\\d+-\\d+ .+. <http://bioinfweb.info/JPhyloIO/>\\]"));
			assertEquals("", reader.readLine());
			assertEquals("BEGIN TAXA;", reader.readLine());
			assertEquals("\tDIMENSIONS NTAX = 3;", reader.readLine());
			assertEquals("\tTAXLABELS", reader.readLine());
			assertEquals("\t\t\tOTU_otu0", reader.readLine());
			assertEquals("\t\t\tOTU_otu1", reader.readLine());
			assertEquals("\t\t\tOTU_otu2;", reader.readLine());
			assertEquals("END;", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
}
