/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.xtg;


import static org.junit.Assert.fail;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;

import java.io.File;

import org.junit.Test;



public class XTGEventReaderTest {
	@Test
	public void testOutput() {
		try {
			XTGEventReader reader = new XTGEventReader(new File("data/XTG/NoNamespaceShortened.xml"), new ReadWriteParameterMap());
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();					
//					System.out.println(event.getType());
//
//					if (event.getType().equals(new EventType(EventContentType.NODE, EventTopologyType.START))) {
//						System.out.println("ID: " + event.asNodeEvent().getID() + " Label: " + event.asNodeEvent().getLabel());
//					}
//					else if (event.getType().equals(new EventType(EventContentType.EDGE, EventTopologyType.START))) {
//						System.out.println("Source: " + event.asEdgeEvent().getSourceID() + " Target: " + event.asEdgeEvent().getTargetID());
//					}					
				}
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
}
