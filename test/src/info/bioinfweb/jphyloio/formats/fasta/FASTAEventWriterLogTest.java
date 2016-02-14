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


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.assertEquals;


import static org.junit.Assert.assertNotEquals;
import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;
import info.bioinfweb.jphyloio.test.dataadapters.TestMatrixDataAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import org.junit.Test;



public class FASTAEventWriterLogTest {
	private void testLogMessage(DocumentDataAdapter document, boolean testEmpty, ApplicationLoggerMessageType expectedType, 
			String expectedMessage)	throws Exception {
		
		File file = new File("data/testOutput/TestLogMessage.fasta");
		
		// Write file:
		FASTAEventWriter writer = new FASTAEventWriter();
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		EventWriterParameterMap map = new EventWriterParameterMap();
		map.put(EventWriterParameterMap.KEY_LOGGER, logger);
		writer.writeDocument(document, file, map);
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			if (testEmpty) {
				assertEquals(-1, reader.read());
			}
			else {
				assertNotEquals(-1, reader.read());
			}
			
			assertEquals(1, logger.getMessageList().size());
			assertEquals(expectedType, logger.getMessageList().get(0).getType());
			assertEquals(expectedMessage, logger.getMessageList().get(0).getMessage());
		}
		finally {
			reader.close();
			file.delete();
		}
	}
	
	
	@Test
	public void test_writeDocument_logEmptyMatrix() throws Exception {
		testLogMessage(createTestDocument(), true, ApplicationLoggerMessageType.WARNING, 
				"An empty FASTA file was written since the first matrix model adapter did not provide any sequences.");
	}
	
	
	@Test
	public void test_writeDocument_logNoMatrix() throws Exception {
		testLogMessage(new ListBasedDocumentDataAdapter(), true, ApplicationLoggerMessageType.WARNING, 
				"An empty FASTA file was written since the specified document adapter contained contained no matrices.");
	}
	
	
	@Test
	public void test_writeDocument_logSecondMatrix() throws Exception {
		ListBasedDocumentDataAdapter document = createTestDocument("ATG", "CGT");
		document.getMatrices().add(new TestMatrixDataAdapter(false, "AAA", "ATA"));
		testLogMessage(document, false, ApplicationLoggerMessageType.WARNING, 
				"The specified document adapter contained more than one character matrix adapter. Since the FASTA "
						+ "format does not support multiple alignments in one file, only the first matrix was written.");
	}
	
	
	@Test
	public void test_writeDocument_logOTUWarning() throws Exception {
		ListBasedDocumentDataAdapter document = createTestDocumentWithLabels("S1", "ATG", "S2", "CGT");
		testLogMessage(document, false, ApplicationLoggerMessageType.WARNING, 
				"The specified OTU list(s) will not be written, since the FASTA format does not support this. "
						+ "Referenced lists will though be used to try to label sequences if necessary.");
	}
	
	
	@Test
	public void test_writeDocument_logTreeNetworkWarning() throws Exception {
		ListBasedDocumentDataAdapter document = createTestDocument("ATG", "CGT");
		document.getTreesNetworks().add(new TreeNetworkDataAdapter() {
			@Override
			public void writeMetadata(JPhyloIOEventReceiver writer) {}
			
			@Override
			public String getLinkedOTUListID() {
				return null;
			}

			@Override
			public boolean hasMetadata() {
				return false;
			}

			@Override
			public void writeNodeData(JPhyloIOEventReceiver receiver, String nodeID) throws IllegalArgumentException, IOException {}
			
			@Override
			public void writeEdgeData(JPhyloIOEventReceiver receiver, String edgeID) throws IllegalArgumentException, IOException {}
			
			@Override
			public boolean isTree() {
				return false;
			}
			
			@Override
			public boolean considerRooted() {
				return false;
			}

			@Override
			public Iterator<String> getRootEdgeIDs() {
				return Collections.emptyIterator();
			}
			
			@Override
			public Iterator<String> getEdgeIDsFromNode(String nodeID)	throws IllegalArgumentException {
				return Collections.emptyIterator();
			}
		});
		testLogMessage(document, false, ApplicationLoggerMessageType.WARNING, 
				"The specified tree or network definitions(s) will not be written, since the FASTA format does not support this.");
	}
}
