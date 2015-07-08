/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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


import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class FASTAEventWriterTest {
	@Test
	public void test_writeEvent() throws IOException {
		File file = new File("data/Fasta/OutputTest.fasta");
		FASTAEventWriter writer = new FASTAEventWriter(file, false);
		try {
			writer.writeEvent(new ConcreteJPhyloIOEvent(EventType.DOCUMENT_START));
			writer.writeEvent(new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_START));
			writer.writeEvent(new SequenceTokensEvent("A", StringUtils.charSequenceToStringList("ACT")));
			writer.writeEvent(new SequenceTokensEvent("A", StringUtils.charSequenceToStringList("GGAT")));
			writer.writeEvent(new CommentEvent("Some comment", false));
			writer.writeEvent(new CommentEvent("Second comment", false));
			writer.writeEvent(new SequenceTokensEvent("A", StringUtils.charSequenceToStringList("ACT")));
			writer.writeEvent(new SequenceTokensEvent("B", StringUtils.charSequenceToStringList("")));
			writer.writeEvent(new CommentEvent("Some other ", true));
			writer.writeEvent(new CommentEvent("comment", false));
			writer.writeEvent(new SequenceTokensEvent("B", StringUtils.charSequenceToStringList("CCTGT")));
			writer.writeEvent(new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_END));
			writer.writeEvent(new ConcreteJPhyloIOEvent(EventType.DOCUMENT_END));
			
		}
		finally {
			writer.close();
		}

		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			assertEquals(">A", reader.readLine());
			assertEquals("ACTGGAT", reader.readLine());
			assertEquals(";Some comment", reader.readLine());
			assertEquals(";Second comment", reader.readLine());
			assertEquals("ACT", reader.readLine());
			assertEquals(">B", reader.readLine());
			assertEquals(";Some other comment", reader.readLine());
			assertEquals("CCTGT", reader.readLine());
		}
		finally {
			reader.close();
		}
	}
}
