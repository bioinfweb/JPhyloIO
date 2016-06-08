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
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.io.Writer;

import org.junit.*;



public class BasicEventReceiverTest {
	@Test
	public void test_add() throws IOException {
		MessageListApplicationLogger logger = new MessageListApplicationLogger();
		BasicEventReceiver<Writer> receiver = new BasicEventReceiver<Writer>(null, new ReadWriteParameterMap());
		assertFalse(receiver.didIgnoreMetadata());
		assertTrue(logger.getMessageList().isEmpty());
		
		receiver.add(new LiteralMetadataEvent("someID", "someLabel", new URIOrStringIdentifier("someKey", ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA), LiteralContentSequenceType.SIMPLE));
		receiver.add(new LiteralMetadataContentEvent(new URIOrStringIdentifier("someKey", ReadWriteConstants.PREDICATE_HAS_LITERAL_METADATA), "someValue", "someValue"));
		receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
		receiver.add(new CommentEvent("some comment"));
		
		assertTrue(receiver.didIgnoreMetadata());
		receiver.addIgnoreLogMessage(logger, "someObject", "someFormat");
		assertEquals(2, logger.getMessageList().size());
		assertEquals(ApplicationLoggerMessageType.WARNING, logger.getMessageList().get(0).getType());
		assertEquals("1 metadata element(s) attached to someObject was/were ignored, since the someFormat format does not support it at this position.", 
				logger.getMessageList().get(0).getMessage());
		assertEquals("1 comment(s) attached to someObject was/were ignored, since the someFormat format does not support it at this position.", 
				logger.getMessageList().get(1).getMessage());
	}
}
