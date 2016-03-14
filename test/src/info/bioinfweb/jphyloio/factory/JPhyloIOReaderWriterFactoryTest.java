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
package info.bioinfweb.jphyloio.factory;


import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;

import java.io.File;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class JPhyloIOReaderWriterFactoryTest implements JPhyloIOFormatIDs {
	@Test
	public void test_guessFormat() throws Exception {
		JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
		assertEquals(FASTA_FORMAT_ID, factory.guessFormat(new File("data/Fasta/Comment.fasta")));
		assertEquals(MEGA_FORMAT_ID, factory.guessFormat(new File("data/MEGA/HLA-3Seq.meg")));
		assertEquals(NEWICK_FORMAT_ID, factory.guessFormat(new File("data/Newick/OneNodeLength.nwk")));
		assertEquals(NEXML_FORMAT_ID, factory.guessFormat(new File("data/NeXML/MetaElements.xml")));
		assertEquals(NEXUS_FORMAT_ID, factory.guessFormat(new File("data/Nexus/Matrix.nex")));
		assertEquals(PDE_FORMAT_ID, factory.guessFormat(new File("data/PDE/shortSequences.pde")));
		assertEquals(PHYLIP_FORMAT_ID, factory.guessFormat(new File("data/Phylip/Interleaved.phy")));
		assertEquals(PHYLOXML_FORMAT_ID, factory.guessFormat(new File("data/PhyloXML/BranchLengths.xml")));
		assertEquals(XTG_FORMAT_ID, factory.guessFormat(new File("data/XTG/ExampleXTGDocument.xml")));
		
		//TODO Also test with input stream.
		//TODO Also test guessReader().
		//TODO Also test GZIPed files for all formats.
	}
}
