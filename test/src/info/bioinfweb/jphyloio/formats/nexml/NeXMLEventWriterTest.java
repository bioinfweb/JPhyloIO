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
package info.bioinfweb.jphyloio.formats.nexml;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestMatrixDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestOTUListDataAdapter;

import java.io.File;

import org.junit.Test;



public class NeXMLEventWriterTest implements NeXMLConstants {
	@Test
	public void test_writeDocument() throws Exception {		
		ListBasedDocumentDataAdapter document = createOTUListDocument();
		NeXMLEventWriter writer = new NeXMLEventWriter();		
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		writer.writeDocument(document, new File("data/testOutput/NeXMLTest.xml"), parameters);		
	}
	
	
	private ListBasedDocumentDataAdapter createOTUListDocument() {
		ListBasedDocumentDataAdapter document = new ListBasedDocumentDataAdapter();
		TestOTUListDataAdapter otuList = new TestOTUListDataAdapter(0, 
				new LabeledIDEvent(EventContentType.OTU, "otu0", "species1"),
				new LabeledIDEvent(EventContentType.OTU, "otu1", "species2"),
				new LabeledIDEvent(EventContentType.OTU, "otu2", "species3"));
		TestMatrixDataAdapter alignment = new TestMatrixDataAdapter("alignment", null, false, "AGCTCTGAAACGGARTGTAG", "AGTTTAGAAAAGGGBTGTCG");
		document.getOTUListsMap().put(otuList.getStartEvent().getID(), otuList);
		document.getMatrices().add(alignment);
		return document;
	}
}
