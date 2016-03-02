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
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestOTUListDataAdapter;
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
		document.getTreesNetworks().add(new TestTreeDataAdapter("tree0", "first tree", "t0"));
		document.getTreesNetworks().add(new TestTreeDataAdapter("tree1", "second tree", "t1"));
		NewickEventWriter writer = new NewickEventWriter();
		writer.writeDocument(document, file, new ReadWriteParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals("[&R] ((Node_t0nA:1.1[&annotation=100], Node_t0nB:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, Node_t0nC:2.0)Node_t0nRoot:1.5;", reader.readLine());
			assertEquals("[&R] ((Node_t1nA:1.1[&annotation=100], Node_t1nB:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, Node_t1nC:2.0)Node_t1nRoot:1.5;", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void test_writeDocument_linkedOTUs() throws Exception {
		File file = new File("data/testOutput/LinkedOTUs.nwk");
		
		// Write file:
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		
		TestOTUListDataAdapter otuList = new TestOTUListDataAdapter(0, 
				new LabeledIDEvent(EventContentType.OTU, "otu0", "label1"),
				new LabeledIDEvent(EventContentType.OTU, "otu1", null),
				new LabeledIDEvent(EventContentType.OTU, "otu2", "otu0"));
		document.getOTUListsMap().put(otuList.getListStartEvent().getID(), otuList);
		
		document.getTreesNetworks().add(new TestTreeDataAdapter("tree0", "first tree", "t0"));
		TestTreeDataAdapter tree = new TestTreeDataAdapter("tree1", "second tree", "t1", new String[]{"otu0", "otu1", "otu2"});
		tree.setLinkedOTUsID("otus0");
		document.getTreesNetworks().add(tree);
		
		NewickEventWriter writer = new NewickEventWriter();
		writer.writeDocument(document, file, new ReadWriteParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals("[&R] ((Node_t0nA:1.1[&annotation=100], Node_t0nB:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, Node_t0nC:2.0)Node_t0nRoot:1.5;", reader.readLine());
			assertEquals("[&R] ((label1:1.1[&annotation=100], Node_t1nB:0.9)'Node ''_1'[&a1=100, a2='ab ''c']:1.0, otu0:2.0)Node_t1nRoot:1.5;", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
}
