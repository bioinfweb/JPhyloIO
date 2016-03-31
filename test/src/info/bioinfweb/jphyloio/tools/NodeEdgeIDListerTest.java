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


import info.bioinfweb.jphyloio.test.dataadapters.TestTreeDataAdapter;

import org.junit.* ;

import static org.junit.Assert.* ;



public class NodeEdgeIDListerTest {
	@Test
	public void test() {
		NodeEdgeIDLister lister = new NodeEdgeIDLister(new TestTreeDataAdapter("tree0", "first tree", ""));
		
		assertTrue(lister.getEdgeIDs().contains("eRoot"));
		assertTrue(lister.getEdgeIDs().contains("e1"));
		assertTrue(lister.getEdgeIDs().contains("eA"));
		assertTrue(lister.getEdgeIDs().contains("eB"));
		assertTrue(lister.getEdgeIDs().contains("eC"));
		assertFalse(lister.getEdgeIDs().contains("eD"));
		
		assertTrue(lister.getNodeIDs().contains("nRoot"));
		assertTrue(lister.getNodeIDs().contains("n1"));
		assertTrue(lister.getNodeIDs().contains("nA"));
		assertTrue(lister.getNodeIDs().contains("nB"));
		assertTrue(lister.getNodeIDs().contains("nC"));
		assertFalse(lister.getNodeIDs().contains("nD"));
	}
}
