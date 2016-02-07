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

import info.bioinfweb.commons.log.ApplicationLoggerMessage;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.SingleTokenTestMatrixDataAdapter;
import info.bioinfweb.jphyloio.test.SystemOutEventReceiver;
import info.bioinfweb.jphyloio.test.TestOTUListDataAdapter;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class FASTAEventWriterTest implements ReadWriteConstants {
	@Test
	public void test_writeDocument() throws Exception {
		File file = new File("data/testOutput/Test.fasta");
		
		// Write file:
		DocumentDataAdapter document = createTestDocument("ACTGC", "A-TCC");
		FASTAEventWriter writer = new FASTAEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">Sequence 0", reader.readLine());
			assertEquals("ACTGC", reader.readLine());
			assertEquals(">Sequence 1", reader.readLine());
			assertEquals("A-TCC", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void test_writeDocument_emptySequence() throws Exception {
		File file = new File("data/testOutput/TestEmptySequence.fasta");
		
		// Write file:
		DocumentDataAdapter document = createTestDocument("", "A-TCC");
		FASTAEventWriter writer = new FASTAEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">Sequence 0", reader.readLine());
			assertEquals(">Sequence 1", reader.readLine());
			assertEquals("A-TCC", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void test_writeDocument_lineBreak() throws Exception {
		File file = new File("data/testOutput/TestLineBreak.fasta");
		
		// Write file:
		DocumentDataAdapter document = createTestDocument("ACTGC", "ACT", "A-TCC");
		FASTAEventWriter writer = new FASTAEventWriter();
		EventWriterParameterMap map = new EventWriterParameterMap();
		map.put(EventWriterParameterMap.KEY_LINE_LENGTH, 3);
		writer.writeDocument(document, file, map);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">Sequence 0", reader.readLine());
			assertEquals("ACT", reader.readLine());
			assertEquals("GC", reader.readLine());
			assertEquals(">Sequence 1", reader.readLine());
			assertEquals("ACT", reader.readLine());
			assertEquals(">Sequence 2", reader.readLine());
			assertEquals("A-T", reader.readLine());
			assertEquals("CC", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void test_writeDocument_labelSources() throws Exception {
		File file = new File("data/testOutput/TestLabelSources.fasta");
		
		// Write file:
		DocumentDataAdapter document = createTestDocumentWithLabels("Label 1", "ACTGC", null, "A-TCC", null, "ACTTC");
		TestOTUListDataAdapter otuList = (TestOTUListDataAdapter)document.getOTUListIterator().next();
		String otuID = DEFAULT_OTU_ID_PREFIX + "2";
		otuList.getOtus().put(otuID, new LabeledIDEvent(EventContentType.OTU, otuID, null));  // Set last OTU label to null
		
		FASTAEventWriter writer = new FASTAEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">Label 1", reader.readLine());  // Label from sequence
			assertEquals("ACTGC", reader.readLine());
			assertEquals(">OTU " + DEFAULT_OTU_ID_PREFIX + "1", reader.readLine());  // Label from OTU
			assertEquals("A-TCC", reader.readLine());
			assertEquals(">" + DEFAULT_SEQUENCE_ID_PREFIX + "2", reader.readLine());  // Sequence ID
			assertEquals("ACTTC", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void test_writeDocument_singleToken() throws Exception {
		File file = new File("data/testOutput/TestSingleToken.fasta");
		
		// Write file:
		DocumentDataAdapter document = createSingleTokenTestDocument("ACTGC", "A-TCC");
		FASTAEventWriter writer = new FASTAEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">Sequence 0", reader.readLine());
			assertEquals("ACTGC", reader.readLine());
			assertEquals(">Sequence 1", reader.readLine());
			assertEquals("A-TCC", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	public static void main(String[] args) throws Exception {
		SingleTokenTestMatrixDataAdapter adapter = new SingleTokenTestMatrixDataAdapter(false, "ACGT-CT");
		adapter.writeSequencePartContentData(new SystemOutEventReceiver(), DEFAULT_SEQUENCE_ID_PREFIX + "0", 0, 7);
	}
}
