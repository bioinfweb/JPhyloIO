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
package info.bioinfweb.jphyloio.objecttranslation.implementations;


import info.bioinfweb.jphyloio.objecttranslation.InvalidObjectSourceDataException;

import java.math.BigDecimal;
import java.util.List;

import org.junit.* ;

import static org.junit.Assert.* ;



public class ListTranslatorTest {
	private void assertListEntriesEqual(List<Object> list, Object... expectedEntries) {
		assertEquals(expectedEntries.length, list.size());
		int pos = 0;
		for (Object entry : list) {
			assertEquals(expectedEntries[pos], entry);
			assertEquals(expectedEntries[pos].getClass(), entry.getClass());
			pos++;
		}
	}
	
	
	@Test
	public void test_representationToJava() throws UnsupportedOperationException, InvalidObjectSourceDataException {
		ListTranslator translator = new ListTranslator();
		assertListEntriesEqual(translator.representationToJava("{}", null));
		assertListEntriesEqual(translator.representationToJava("{\"ABC\", 18, 20.2, 'AB, C', \"AB\"\"C\", \"{'ABC'}\", 2E500}", null), 
				"ABC", new Double(18), new Double(20.2), "AB, C", "AB\"C", "{'ABC'}", new BigDecimal("2E500"));
	}
}
