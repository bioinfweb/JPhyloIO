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
package info.bioinfweb.jphyloio.dataadapters.implementations.receivers;


import static org.junit.Assert.*;


import info.bioinfweb.commons.log.ApplicationLoggerMessageType;
import info.bioinfweb.commons.log.MessageListApplicationLogger;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;

import java.io.IOException;

import org.junit.*;



public class IgnoreObjectListMetadataReceiverTest {
	@Test
	public void test_add() throws IOException {
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		IgnoreObjectListMetadataReceiver receiver = new IgnoreObjectListMetadataReceiver(logger, "someObject", "someFormat");
		assertFalse(receiver.isIgnoredMetadata());
		
		assertTrue(receiver.add(new EdgeEvent("id1", "Edge", null, "id2", 1.0)));
		assertFalse(receiver.isIgnoredMetadata());
		assertTrue(logger.getMessageList().isEmpty());
		
		assertFalse(receiver.add(new MetaInformationEvent("someKey", "someType", "someValue")));
		assertTrue(receiver.isIgnoredMetadata());
		assertEquals(1, logger.getMessageList().size());
		assertEquals(ApplicationLoggerMessageType.WARNING, logger.getMessageList().get(0).getType());
		assertEquals("Metadata attached to someObject was ignored, since the someFormat format does not support this.", 
				logger.getMessageList().get(0).getMessage());
	}
}
