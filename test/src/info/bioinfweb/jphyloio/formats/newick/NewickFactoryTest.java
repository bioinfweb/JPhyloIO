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
package info.bioinfweb.jphyloio.formats.newick;


import info.bioinfweb.jphyloio.ReadWriteParameterMap;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.junit.* ;

import static org.junit.Assert.* ;



public class NewickFactoryTest {
//	@Test
//	public void test_checkFormat_comments() throws IOException {
//		Reader reader = new FileReader("data/Newick/Comments.nwk");
//		try {
//			assertTrue(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
//	
//	
	@Test
	public void test_checkFormat_oneNodeNone() throws IOException {
		Reader reader = new FileReader("data/Newick/OneNodeNone.nwk");
		try {
			assertTrue(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_checkFormat_oneNodeName() throws IOException {
		Reader reader = new FileReader("data/Newick/OneNodeName.nwk");
		try {
			assertTrue(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
		}
		finally {
			reader.close();
		}
	}
	
	
//	@Test
//	public void test_checkFormat_NHX2() throws IOException {
//		Reader reader = new FileReader("data/Newick/NHX2.nwk");
//		try {
//			assertTrue(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
//	
//	
//	@Test
//	public void test_checkFormat_Metadata() throws IOException {
//		Reader reader = new FileReader("data/Newick/Metadata.nwk");
//		try {
//			assertTrue(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
//	
//	
//	@Test
//	public void test_checkFormat_invalid() throws IOException {
//		Reader reader = new FileReader("data/Newick/InvalidNewick.txt");
//		try {
//			assertFalse(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
//	
//	
//	@Test
//	public void test_checkFormat_Nexus() throws IOException {
//		Reader reader = new FileReader("data/Nexus/Matrix.nex");
//		try {
//			assertFalse(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
//	
//	
//	@Test
//	public void test_checkFormat_FASTA() throws IOException {
//		Reader reader = new FileReader("data/Fasta/Test.fasta");
//		try {
//			assertFalse(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
//	
//	
//	@Test
//	public void test_checkFormat_MEGA() throws IOException {
//		Reader reader = new FileReader("data/MEGA/MatchToken.meg");
//		try {
//			assertFalse(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
//	
//	
//	@Test
//	public void test_checkFormat_Phylip() throws IOException {
//		Reader reader = new FileReader("data/Phylip/Interleaved.phy");
//		try {
//			assertFalse(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
//	
//	
//	@Test
//	public void test_checkFormat_NeXML() throws IOException {
//		Reader reader = new FileReader("data/NeXML/Paralogues.xml");
//		try {
//			assertFalse(new NewickFactory().checkFormat(reader, new ReadWriteParameterMap()));
//		}
//		finally {
//			reader.close();
//		}
//	}
}
