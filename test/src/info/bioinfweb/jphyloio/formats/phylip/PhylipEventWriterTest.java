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
package info.bioinfweb.jphyloio.formats.phylip;


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.*;


import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.test.dataadapters.TestOTUListDataAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;



public class PhylipEventWriterTest implements ReadWriteConstants {
	@Test
	public void test_writeDocument() throws Exception {
		File file = new File("data/testOutput/Test.phy");
		
		// Write file:
		DocumentDataAdapter document = createTestDocument("ACTGC", "A-TCC");
		PhylipEventWriter writer = new PhylipEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals("\t2\t5", reader.readLine());
			assertEquals("Sequence 0ACTGC", reader.readLine());
			assertEquals("Sequence 1A-TCC", reader.readLine());
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
		PhylipEventWriter writer = new PhylipEventWriter();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		EventWriterParameterMap map = new EventWriterParameterMap();
		map.put(EventWriterParameterMap.KEY_LOGGER, logger);
		writer.writeDocument(document, file, map);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals("\t2\t5", reader.readLine());
			assertEquals("Sequence 0", reader.readLine());
			assertEquals("Sequence 1A-TCC", reader.readLine());
			assertEquals(-1, reader.read());

			assertEquals(1, logger.getMessageList().size());
			assertEquals(ApplicationLoggerMessageType.WARNING, logger.getMessageList().get(0).getType());
			assertTrue(logger.getMessageList().get(0).getMessage().contains(
					"The column count written to the Phylip document is the length of the longest sequence."));
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
		
		PhylipEventWriter writer = new PhylipEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals("\t3\t5", reader.readLine());
			assertEquals("Label 1   ACTGC", reader.readLine());
			assertEquals("OTU otu1  A-TCC", reader.readLine());  //TODO Fix this. (Phylip reader behaves different here then FASTA reader.)
			assertEquals("OTU otu2  ACTTC", reader.readLine());  //TODO see above
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
		PhylipEventWriter writer = new PhylipEventWriter();
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals("\t2\t5", reader.readLine());
			assertEquals("Sequence 0ACTGC", reader.readLine());
			assertEquals("Sequence 1A-TCC", reader.readLine());
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
		PhylipEventWriter writer = new PhylipEventWriter();
		EventWriterParameterMap parameters = new EventWriterParameterMap();
		parameters.put(EventWriterParameterMap.KEY_SEQUENCE_EXTENSION_TOKEN, "?");
		writer.writeDocument(document, file, parameters);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals("\t2\t7", reader.readLine());
			assertEquals("Sequence 0ACTGCTG", reader.readLine());
			assertEquals("Sequence 1A-TCC??", reader.readLine());
			assertEquals(-1, reader.read());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
}
