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


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.createTestDocument;
import static org.junit.Assert.assertEquals;


import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.implementations.ListBasedDocumentDataAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.junit.Test;



public class FASTAEventWriterLogTest {
	private void testLogMessage(DocumentDataAdapter document, ApplicationLoggerMessageType expectedType, String expectedMessage) 
			throws Exception {
		
		File file = new File("data/testOutput/TestLogMessage.fasta");
		
		// Write file:
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		FASTAEventWriter writer = new FASTAEventWriter(logger);
		writer.writeDocument(document, file, new EventWriterParameterMap());
		
		// Validate file:
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(-1, reader.read());
			
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
		testLogMessage(createTestDocument(),	ApplicationLoggerMessageType.WARNING, 
				"An empty FASTA file was written since the first matrix model adapter did not provide any sequences.");
	}
	
	
	@Test
	public void test_writeDocument_logNoMatrix() throws Exception {
		testLogMessage(new ListBasedDocumentDataAdapter(),	ApplicationLoggerMessageType.WARNING, 
				"An empty FASTA file was written since the specified document adapter contained contained no matrices.");
	}
}
