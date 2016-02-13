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


import static org.junit.Assert.*;


import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestTreeDataAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.*;



public class NewickEventWriterTest {
	@Test
	public void test_writeDocument() throws Exception {
		File file = new File("data/testOutput/Test.nwk");
		
		// Write file:
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		document.getTreesNetworks().add(new TestTreeDataAdapter());
		document.getTreesNetworks().add(new TestTreeDataAdapter());
		NewickEventWriter writer = new NewickEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals("[&R] ((Node_nA:1.1[&annotation=100], Node_nB:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, Node_nC:2.0)Node_nRoot:1.5;", reader.readLine());
			assertEquals("[&R] ((Node_nA:1.1[&annotation=100], Node_nB:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, Node_nC:2.0)Node_nRoot:1.5;", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
}
