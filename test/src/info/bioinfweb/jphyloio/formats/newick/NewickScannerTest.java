/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.newick;


import info.bioinfweb.commons.io.PeekReader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.junit.* ;

import static org.junit.Assert.* ;



public class NewickScannerTest {
	private static void assertNameToken(String expectedName, NewickScanner scanner) throws IOException {
		NewickToken token = scanner.nextToken();
		assertNotNull(token);
		assertEquals(NewickTokenType.NAME, token.getType());
		assertEquals(expectedName, token.getText());
	}
	
	
	private static void assertLengthToken(double expectedLength, NewickScanner scanner) throws IOException {
		NewickToken token = scanner.nextToken();
		assertNotNull(token);
		assertEquals(NewickTokenType.LENGTH, token.getType());
		assertEquals(expectedLength, token.getLength(), 0.0000001);
	}
	
	
	private static void assertCommentToken(String expectedComment, NewickScanner scanner) throws IOException {
		NewickToken token = scanner.nextToken();
		assertNotNull(token);
		assertEquals(NewickTokenType.COMMENT, token.getType());
		assertEquals(expectedComment, token.getText());
	}
	
	
	@Test
	public void test_nextToken() throws IOException {
		PeekReader reader = new PeekReader(new BufferedReader(new FileReader("data/Newick/InternalsTerminalsLength.nwk")));
		try {
			NewickScanner scanner = new NewickScanner(reader, true);
			assertEquals(NewickTokenType.SUBTREE_START, scanner.nextToken().getType());
			assertEquals(NewickTokenType.SUBTREE_START, scanner.nextToken().getType());
			assertEquals(NewickTokenType.SUBTREE_START, scanner.nextToken().getType());
			assertNameToken("A", scanner);
			assertLengthToken(1.05, scanner);
			assertEquals(NewickTokenType.ELEMENT_SEPARATOR, scanner.nextToken().getType());
			assertNameToken("B", scanner);
			assertLengthToken(1.0, scanner);
			assertEquals(NewickTokenType.SUBTREE_END, scanner.nextToken().getType());
			assertNameToken("N3", scanner);
			assertLengthToken(1.5, scanner);
			assertEquals(NewickTokenType.ELEMENT_SEPARATOR, scanner.nextToken().getType());
			assertNameToken("C", scanner);
			assertLengthToken(2.5, scanner);
			assertEquals(NewickTokenType.SUBTREE_END, scanner.nextToken().getType());
			assertNameToken("N2", scanner);
			assertLengthToken(0.8, scanner);
			assertEquals(NewickTokenType.ELEMENT_SEPARATOR, scanner.nextToken().getType());
			assertEquals(NewickTokenType.SUBTREE_START, scanner.nextToken().getType());
			assertNameToken("D", scanner);
			assertLengthToken(2.0, scanner);
			assertEquals(NewickTokenType.ELEMENT_SEPARATOR, scanner.nextToken().getType());
			assertNameToken("E", scanner);
			assertLengthToken(2.1, scanner);
			assertEquals(NewickTokenType.SUBTREE_END, scanner.nextToken().getType());
			assertNameToken("N4", scanner);
			assertLengthToken(1.4, scanner);
			assertEquals(NewickTokenType.SUBTREE_END, scanner.nextToken().getType());
			assertNameToken("N1", scanner);
			assertEquals(NewickTokenType.TERMNINAL_SYMBOL, scanner.nextToken().getType());
			assertEquals(-1, reader.peek());
		}
		finally {
			reader.close();
		}
	}
	
	
	@Test
	public void test_nextToken_comments() throws IOException {
		PeekReader reader = new PeekReader(new BufferedReader(new FileReader("data/Newick/Comments.nwk")));
		try {
			NewickScanner scanner = new NewickScanner(reader, true);
			assertEquals(NewickTokenType.UNROOTED_COMMAND, scanner.nextToken().getType());
			assertEquals(NewickTokenType.ROOTED_COMMAND, scanner.nextToken().getType());
			assertCommentToken("c1", scanner);
			assertEquals(NewickTokenType.SUBTREE_START, scanner.nextToken().getType());
			assertCommentToken("c2", scanner);
			assertEquals(NewickTokenType.SUBTREE_START, scanner.nextToken().getType());
			assertCommentToken("c3", scanner);
			assertNameToken("A", scanner);
			assertCommentToken("c4", scanner);
			assertCommentToken("c5", scanner);
			assertCommentToken("c6", scanner);
			assertLengthToken(1.0, scanner);
			assertCommentToken("c7", scanner);
			assertEquals(NewickTokenType.ELEMENT_SEPARATOR, scanner.nextToken().getType());
			assertCommentToken("c8", scanner);
			assertNameToken("B", scanner);
			assertCommentToken("c9", scanner);
			assertEquals(NewickTokenType.SUBTREE_END, scanner.nextToken().getType());
			assertCommentToken("c10", scanner);
			assertEquals(NewickTokenType.ELEMENT_SEPARATOR, scanner.nextToken().getType());
			assertNameToken("C", scanner);
			assertEquals(NewickTokenType.SUBTREE_END, scanner.nextToken().getType());
			assertCommentToken("c11", scanner);
			assertEquals(NewickTokenType.TERMNINAL_SYMBOL, scanner.nextToken().getType());
			assertCommentToken("c12", scanner);
			assertEquals(-1, reader.peek());
		}
		finally {
			reader.close();
		}
	}
}
