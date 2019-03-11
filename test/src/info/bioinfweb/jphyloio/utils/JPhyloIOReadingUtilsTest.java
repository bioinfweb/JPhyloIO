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
package info.bioinfweb.jphyloio.utils;


import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.stream.XMLStreamException;

import org.junit.* ;

import static org.junit.Assert.* ;
import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;



public class JPhyloIOReadingUtilsTest {
	@Test
	public void test_collectEvents() throws IOException, XMLStreamException {
		JPhyloIOEventReader reader = new NeXMLEventReader(new File("data/NeXML/MultipleElements.xml"), new ReadWriteParameterMap());
		Set<EventType> types = new TreeSet<EventType>();
		types.add(new EventType(EventContentType.ALIGNMENT, EventTopologyType.START));
		try {
			List<LinkedLabeledIDEvent> list = JPhyloIOReadingUtils.collectEvents(reader, types, LinkedLabeledIDEvent.class);
			
			assertEquals(3, list.size());
			assertEquals("alignment1", list.get(0).getID());
			assertEquals("alignment2", list.get(1).getID());
			assertEquals("alignment3", list.get(2).getID());
		}
		finally {
			reader.close();
		}
	}
}
