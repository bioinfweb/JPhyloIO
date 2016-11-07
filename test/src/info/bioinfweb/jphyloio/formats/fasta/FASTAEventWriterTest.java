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


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.createSingleTokenTestDocument;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.createTestDocument;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.createTestDocumentWithLabels;
import static org.junit.Assert.assertEquals;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestMatrixDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestOTUListDataAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import javax.xml.namespace.QName;

import org.junit.Test;



public class FASTAEventWriterTest implements ReadWriteConstants {
	@Test
	public void test_writeDocument() throws Exception {
		File file = new File("data/testOutput/Test.fasta");
		
		// Write file:
		DocumentDataAdapter document = createTestDocument("ACTGC", "A-TCC");
		FASTAEventWriter writer = new FASTAEventWriter();
		writer.writeDocument(document, file, new ReadWriteParameterMap());
		
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
		writer.writeDocument(document, file, new ReadWriteParameterMap());
		
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
		ReadWriteParameterMap map = new ReadWriteParameterMap();
		map.put(ReadWriteParameterMap.KEY_LINE_LENGTH, 3);
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
		TestOTUListDataAdapter otuList = (TestOTUListDataAdapter)document.getOTUListIterator(null).next();  // Specifying null here may become a problem in the future.
		String otuID = DEFAULT_OTU_ID_PREFIX + "2";
		otuList.getOtus().put(otuID, new LabeledIDEvent(EventContentType.OTU, otuID, null));  // Set last OTU label to null
		
		FASTAEventWriter writer = new FASTAEventWriter();
		writer.writeDocument(document, file, new ReadWriteParameterMap());
		
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
		writer.writeDocument(document, file, new ReadWriteParameterMap());
		
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
	public void test_writeDocument_comments() throws Exception {
		File file = new File("data/testOutput/TestComments.fasta");
		
		// Write file:
		ListBasedDocumentDataAdapter document = createTestDocument("ACTGC", "A-TCC");
		TestMatrixDataAdapter matrix = (TestMatrixDataAdapter)document.getMatrices().get(0);
		List<JPhyloIOEvent> leadingEvents = matrix.getMatrix().get("seq0").leadingEvents;
		leadingEvents.add(new CommentEvent("com", true));
		leadingEvents.add(new CommentEvent("ment 1", false));
		leadingEvents.add(new LiteralMetadataEvent("meta1", null, new URIOrStringIdentifier("someKey", new QName("http://example.org/", "somePredicate")), 
				LiteralContentSequenceType.SIMPLE));
		leadingEvents.add(new LiteralMetadataContentEvent("someValue", false));
		leadingEvents.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
		leadingEvents.add(new CommentEvent("comment 2", false));
		matrix.getMatrix().get("seq1").leadingEvents.add(new CommentEvent("comment 3", false));
		FASTAEventWriter writer = new FASTAEventWriter();
		writer.writeDocument(document, file, new ReadWriteParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">Sequence 0", reader.readLine());
			assertEquals(";comment 1", reader.readLine());
			assertEquals(";comment 2", reader.readLine());
			assertEquals("ACTGC", reader.readLine());
			assertEquals(">Sequence 1", reader.readLine());
			assertEquals(";comment 3", reader.readLine());
			assertEquals("A-TCC", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void test_writeDocument_extendSequences() throws Exception {
		File file = new File("data/testOutput/TestExtendSequences.fasta");
		
		// Write file:
		DocumentDataAdapter document = createTestDocument("ACTGCTG", "A-TCC");
		FASTAEventWriter writer = new FASTAEventWriter();
		ReadWriteParameterMap parameters = new ReadWriteParameterMap();
		parameters.put(ReadWriteParameterMap.KEY_SEQUENCE_EXTENSION_TOKEN, "?");
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">Sequence 0", reader.readLine());
			assertEquals("ACTGCTG", reader.readLine());
			assertEquals(">Sequence 1", reader.readLine());
			assertEquals("A-TCC??", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
//	public static void main(String[] args) throws Exception {
//		SingleTokenTestMatrixDataAdapter adapter = new SingleTokenTestMatrixDataAdapter("matrixID", "a matrix", false, "ACGT-CT");
//		adapter.writeSequencePartContentData(null, new SystemOutEventReceiver(), DEFAULT_SEQUENCE_ID_PREFIX + "0", 0, 7);
//	}
}
