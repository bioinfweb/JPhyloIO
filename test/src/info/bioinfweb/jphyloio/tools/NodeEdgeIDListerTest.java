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
package info.bioinfweb.jphyloio.tools;


import java.util.Iterator;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.test.dataadapters.testtreenetworkdataadapters.EdgeAndNodeMetaDataTree;

import org.junit.* ;

import static org.junit.Assert.* ;



public class NodeEdgeIDListerTest {
	@Test
	public void test() {
		NodeEdgeIDLister lister = new NodeEdgeIDLister(new EdgeAndNodeMetaDataTree("tree0", "first tree", ""), new ReadWriteParameterMap());
		
		Iterator<String> iterator = lister.getEdgeIDs().iterator();
		assertEquals("eRoot", iterator.next());
		assertEquals("e1", iterator.next());
		assertEquals("eC", iterator.next());
		assertEquals("eA", iterator.next());
		assertEquals("eB", iterator.next());
		assertFalse(iterator.hasNext());
		
		iterator = lister.getNodeIDs().iterator();
		assertEquals("nRoot", iterator.next());
		assertEquals("n1", iterator.next());
		assertEquals("nC", iterator.next());
		assertEquals("nA", iterator.next());
		assertEquals("nB", iterator.next());
		assertFalse(iterator.hasNext());
		
		assertFalse(lister.getEdgeIDs().contains("eD"));
		assertFalse(lister.getNodeIDs().contains("nD"));
	}
}
