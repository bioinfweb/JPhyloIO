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
package info.bioinfweb.jphyloio.formats.xml;



import static info.bioinfweb.commons.testing.XMLAssert.*;

import static org.junit.Assert.fail;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;

import java.io.File;

import org.junit.Test;



public class AbstractXMLEventReaderTest {
	/*
	 * methode mit reader als parameter
	 * einmal nexml einmal phyloXML testen mit jedem reader
	 * grundsätzliches lesen des customXMl mit allen verfügbaren teilen
	 * lesen mit mehreren readern gleichzeitig/abwechselnd
	 * aus dokument udn element metadaten nacheinander (damit beim erzeugen neuer reader nichts hängen bleibt aus vorherigen vorgängen)
	 */
	
	@Test
	public void testMetaXMLStreamReaderinNeXML() {
		try {
			NeXMLEventReader reader = new NeXMLEventReader(new File("data/NeXML/CustomXMLTest.xml"), new ReadWriteParameterMap());
			try {
				readWithMetaXMLEventReader(reader);
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
	

	private void readWithMetaXMLEventReader(JPhyloIOXMLEventReader reader) {
		try {
			JPhyloIOEvent event = reader.next();
			while (reader.hasNextEvent() && !(event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))
					&& event.asLiteralMetadataEvent().getSequenceType().equals(LiteralContentSequenceType.XML))) {
				event = reader.next();
			}

			MetaXMLEventReader customXMLReader = reader.createMetaXMLEventReader();
			
			assertStartDocument(customXMLReader);
			assertCharactersEvent("characters", customXMLReader);
			
			
			while (reader.hasNextEvent()) {
				event = reader.next();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
}
