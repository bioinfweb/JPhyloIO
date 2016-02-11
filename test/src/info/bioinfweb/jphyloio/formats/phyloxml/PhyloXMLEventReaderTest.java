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
package info.bioinfweb.jphyloio.formats.phyloxml;


import static org.junit.Assert.fail;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;

import java.io.File;

import org.junit.Test;



public class PhyloXMLEventReaderTest {
	@Test
	public void testPhyloXML() {
		try {
			PhyloXMLEventReader reader = new PhyloXMLEventReader(new File("data/PhyloXML/PhyloXMLDocument.xml"));
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType().getContentType() + " " + event.getType().getTopologyType());
					if (event.getType().equals(new EventType(EventContentType.META_INFORMATION, EventTopologyType.START))) {
//						System.out.println("Key: " + event.asMetaInformationEvent().getKey());
					}
					else if (event.getType().equals(new EventType(EventContentType.TREE, EventTopologyType.START))) {
//						System.out.println("ID: " + event.asLabeledIDEvent().getID() + " Label: " + event.asLabeledIDEvent().getLabel());
					}
					else if (event.getType().equals(new EventType(EventContentType.NODE, EventTopologyType.START))) {
//						System.out.println("ID: " + event.asLabeledIDEvent().getID() + " Label: " + event.asLabeledIDEvent().getLabel());
					}
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
