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


import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.EmptyAnnotatedDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.NoSetsMatrixDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedOTUEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class FASTAEventWriterTest {
	private DocumentDataAdapter createTestDocument() {
		ListBasedDocumentDataAdapter result = new ListBasedDocumentDataAdapter();
		result.getMatrices().add(new NoSetsMatrixDataAdapter() {
			@Override
			public void writeSequencePartContentData(JPhyloIOEventReceiver receiver, String sequenceID, long startColumn, 
					long endColumn) throws IllegalArgumentException {
				
				switch (sequenceID) {
					case "id1":
						//TODO Generate sequence events
						break;
					case "id2":
						//TODO Generate sequence events
						break;
					default:
						throw new IllegalArgumentException();
				}
			}
			
			@Override
			public LinkedOTUEvent getSequenceStartEvent(String sequenceID) {
				switch (sequenceID) {
					case "id1":
						return new LinkedOTUEvent(EventContentType.SEQUENCE, sequenceID, "Sequence A", null);
					case "id2":
						return new LinkedOTUEvent(EventContentType.SEQUENCE, sequenceID, "Sequence B", null);
					default:
						throw new IllegalArgumentException();
				}
			}
			
			@Override
			public long getSequenceLength(String sequenceID) throws IllegalArgumentException {
				return getColumnCount();
			}
			
			@Override
			public Iterator<String> getSequenceIDIterator() {
				return Arrays.asList("id1", "id2").iterator();
			}
			
			@Override
			public long getSequenceCount() {
				return 2;
			}
			
			@Override
			public long getColumnCount() {
				return 8;
			}
			
			@Override
			public boolean containsLongTokens() {
				return false;
			}
		});
		return result;
	}
	
	
	@Test
	public void test_writeDocument() {
		
	}
}
