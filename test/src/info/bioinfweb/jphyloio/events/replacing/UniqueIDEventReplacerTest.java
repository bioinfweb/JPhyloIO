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
package info.bioinfweb.jphyloio.events.replacing;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class UniqueIDEventReplacerTest {
	@Test
	public void test_() {
		UniqueIDEventReplacer replacer = new UniqueIDEventReplacer();
		
		assertEquals("someOTU", replacer.replaceEvent(new LabeledIDEvent(EventContentType.OTU, "someOTU", null)).getID());
		NodeEvent nodeEvent = replacer.replaceEvent(new NodeEvent("someNode", null, "someOTU", false));
		assertEquals("someNode", nodeEvent.getID());
		assertEquals("someOTU", nodeEvent.getLinkedID());
		
		assertEquals("someOTU1", replacer.replaceEvent(new LabeledIDEvent(EventContentType.OTU, "someOTU", null)).getID());
		nodeEvent = replacer.replaceEvent(new NodeEvent("someNode", null, "someOTU", false));
		assertEquals("someNode1", nodeEvent.getID());
		assertEquals("someOTU1", nodeEvent.getLinkedID());
		
		//TODO If an edge event would reference someNode now it cannot be determined which of the two node was originally meant. 
		//     Edge events before the second node would not be translated. Ones after it would. If there is a third node with the same ID, everything from 
		//     then on would be translated to the latest node. This might work in most contexts. If it is done like this, it needs to be documented well. 
	}
}
