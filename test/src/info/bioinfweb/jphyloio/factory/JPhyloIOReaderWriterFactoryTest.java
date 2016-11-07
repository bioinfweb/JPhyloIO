/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.factory;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;

import java.io.File;
import java.io.FileReader;

import org.junit.Test;



public class JPhyloIOReaderWriterFactoryTest implements JPhyloIOFormatIDs {
	//TODO Also test GZIPed files for all formats.

	@Test
	public void test_guessFormat_File() throws Exception {
		JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
		assertEquals(FASTA_FORMAT_ID, factory.guessFormat(new File("data/Fasta/Comment.fasta")));  // Uses a FileInputStream.
		assertEquals(MEGA_FORMAT_ID, factory.guessFormat(new File("data/MEGA/HLA-3Seq.meg")));
		assertEquals(NEWICK_FORMAT_ID, factory.guessFormat(new File("data/Newick/OneNodeLength.nwk")));
		assertEquals(NEXML_FORMAT_ID, factory.guessFormat(new File("data/NeXML/MetaElements.xml")));
		assertEquals(NEXUS_FORMAT_ID, factory.guessFormat(new File("data/Nexus/Matrix.nex")));
		assertEquals(PDE_FORMAT_ID, factory.guessFormat(new File("data/PDE/SimpleDNASeq.pde")));
		assertEquals(PHYLIP_FORMAT_ID, factory.guessFormat(new File("data/Phylip/Interleaved.phy")));
		assertEquals(PHYLOXML_FORMAT_ID, factory.guessFormat(new File("data/PhyloXML/BranchLengths.xml")));
		assertEquals(XTG_FORMAT_ID, factory.guessFormat(new File("data/XTG/ExampleXTGDocument.xml")));
		assertNull(factory.guessFormat(new File("data/other/Text.txt")));
	}
	
	
	private void testGuessFormatReader(String exptectedFormat, JPhyloIOReaderWriterFactory factory, String file) throws Exception {
		FileReader reader = new FileReader(new File(file));
		try {
			assertEquals(exptectedFormat, factory.guessFormat(reader));
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_guessFormat_Reader() throws Exception {
		JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
		testGuessFormatReader(FASTA_FORMAT_ID, factory, "data/Fasta/Comment.fasta");
		testGuessFormatReader(MEGA_FORMAT_ID, factory, "data/MEGA/HLA-3Seq.meg");
		testGuessFormatReader(NEWICK_FORMAT_ID, factory, "data/Newick/OneNodeLength.nwk");
		testGuessFormatReader(NEXML_FORMAT_ID, factory, "data/NeXML/MetaElements.xml");
		testGuessFormatReader(NEXUS_FORMAT_ID, factory, "data/Nexus/Matrix.nex");
		testGuessFormatReader(PDE_FORMAT_ID, factory, "data/PDE/SimpleDNASeq.pde");
		testGuessFormatReader(PHYLIP_FORMAT_ID, factory, "data/Phylip/Interleaved.phy");
		testGuessFormatReader(PHYLOXML_FORMAT_ID, factory, "data/PhyloXML/BranchLengths.xml");
		testGuessFormatReader(XTG_FORMAT_ID, factory, "data/XTG/ExampleXTGDocument.xml");
		testGuessFormatReader(null, factory, "data/other/Text.txt");
	}
	
	
	private void testGuessReader(String exptectedFormat, JPhyloIOReaderWriterFactory factory, String file) throws Exception {
		JPhyloIOEventReader reader = factory.guessReader(new File(file), new ReadWriteParameterMap());
		try {
			if (exptectedFormat == null) {
				assertNull(reader);
			}
			else {
				assertEquals(exptectedFormat, reader.getFormatID());
			}
		}
		finally {
			if (reader != null) {
				reader.close();
			}
		}
	}
	
	
	@Test
	public void test_guessReader() throws Exception {
		JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
		testGuessReader(FASTA_FORMAT_ID, factory, "data/Fasta/Comment.fasta");
		testGuessReader(MEGA_FORMAT_ID, factory, "data/MEGA/HLA-3Seq.meg");
		testGuessReader(NEWICK_FORMAT_ID, factory, "data/Newick/OneNodeLength.nwk");
		testGuessReader(NEXML_FORMAT_ID, factory, "data/NeXML/MetaElements.xml");
		testGuessReader(NEXUS_FORMAT_ID, factory, "data/Nexus/Matrix.nex");
		testGuessReader(PDE_FORMAT_ID, factory, "data/PDE/SimpleDNASeq.pde");
		testGuessReader(PHYLIP_FORMAT_ID, factory, "data/Phylip/Interleaved.phy");
		testGuessReader(PHYLOXML_FORMAT_ID, factory, "data/PhyloXML/BranchLengths.xml");
		testGuessReader(XTG_FORMAT_ID, factory, "data/XTG/ExampleXTGDocument.xml");
		testGuessReader(null, factory, "data/other/Text.txt");
	}
	
	
	@Test
	public void test_guessReader_GZIP() throws Exception {
		JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
		testGuessReader(FASTA_FORMAT_ID, factory, "data/Fasta/Comment.fasta.gz");
		testGuessReader(MEGA_FORMAT_ID, factory, "data/MEGA/HLA-3Seq.meg.gz");
		testGuessReader(NEWICK_FORMAT_ID, factory, "data/Newick/OneNodeLength.nwk.gz");
		testGuessReader(NEXML_FORMAT_ID, factory, "data/NeXML/MetaElements.xml.gz");
		testGuessReader(NEXUS_FORMAT_ID, factory, "data/Nexus/Matrix.nex.gz");
		testGuessReader(PDE_FORMAT_ID, factory, "data/PDE/SimpleDNASeq.pde.gz");
		testGuessReader(PHYLIP_FORMAT_ID, factory, "data/Phylip/Interleaved.phy.gz");
		testGuessReader(PHYLOXML_FORMAT_ID, factory, "data/PhyloXML/BranchLengths.xml.gz");
		testGuessReader(XTG_FORMAT_ID, factory, "data/XTG/ExampleXTGDocument.xml.gz");
		testGuessReader(null, factory, "data/other/Text.txt.gz");
	}
}
