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
		assertEquals(NewickTokenType.NAME, token.getType());
		assertEquals(expectedName, token.getText());
	}
	
	
	private static void assertLengthToken(double expectedLength, NewickScanner scanner) throws IOException {
		NewickToken token = scanner.nextToken();
		assertEquals(NewickTokenType.LENGTH, token.getType());
		assertEquals(expectedLength, token.getLength(), 0.0000001);
	}
	
	
	@Test
	public void test_nextToken() throws IOException {
		PeekReader reader = new PeekReader(new BufferedReader(new FileReader("data/Newick/InternalsTerminalsLength.nwk")));
		try {
			NewickScanner scanner = new NewickScanner(reader);
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
}
