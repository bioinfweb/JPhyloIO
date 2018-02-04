/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2018  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.phylip;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.junit.* ;

import static org.junit.Assert.* ;



public class PhylipFactoryTest {
	@Test
	public void test_checkFormat() throws IOException {
		Reader reader = new FileReader("data/Phylip/Interleaved.phy");
		try {
			assertTrue(new PhylipFactory().checkFormat(reader, new ReadWriteParameterMap()));
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_checkFormat_FASTA() throws IOException {
		Reader reader = new FileReader("data/Fasta/Test.fasta");
		try {
			assertFalse(new PhylipFactory().checkFormat(reader, new ReadWriteParameterMap()));
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_checkFormat_MEGA() throws IOException {
		Reader reader = new FileReader("data/MEGA/MatchToken.meg");
		try {
			assertFalse(new PhylipFactory().checkFormat(reader, new ReadWriteParameterMap()));
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_checkFormat_Newick() throws IOException {
		Reader reader = new FileReader("data/Newick/Metadata.nwk");
		try {
			assertFalse(new PhylipFactory().checkFormat(reader, new ReadWriteParameterMap()));
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_checkFormat_NeXML() throws IOException {
		Reader reader = new FileReader("data/NeXML/Paralogues.xml");
		try {
			assertFalse(new PhylipFactory().checkFormat(reader, new ReadWriteParameterMap()));
		}
		finally {
			reader.close();
		}
	}
}
