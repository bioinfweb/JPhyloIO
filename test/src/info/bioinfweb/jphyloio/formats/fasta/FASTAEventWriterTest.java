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
package info.bioinfweb.jphyloio.formats.fasta;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.test.TestMatrixDataAdapter;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class FASTAEventWriterTest {
	private DocumentDataAdapter createTestDocument() {
		ListBasedDocumentDataAdapter result = new ListBasedDocumentDataAdapter();
		result.getMatrices().add(TestMatrixDataAdapter.newSingleCharTokenInstance("ACTGC", "A-TCC"));
		return result;
	}
	
	
	@Test
	public void test_writeDocument() throws Exception {
		File file = new File("data/testOutput/Test.fasta");
		
		// Write file:
		DocumentDataAdapter document = createTestDocument();
		FASTAEventWriter writer = new FASTAEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">Sequence id0", reader.readLine());
			assertEquals("ACTGC", reader.readLine());
			assertEquals(">Sequence id1", reader.readLine());
			assertEquals("A-TCC", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
}
