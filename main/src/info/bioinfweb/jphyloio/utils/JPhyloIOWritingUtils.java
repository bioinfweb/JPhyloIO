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
package info.bioinfweb.jphyloio.utils;


import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;



public class JPhyloIOWritingUtils {
	private static List<String> createTokenList(int maxTokenCount) {
		return new ArrayList<String>(
				Math.min(ReadWriteConstants.DEFAULT_MAX_TOKENS_TO_READ, maxTokenCount));
	}
	
	
	public static void writeCharSequencePartContent(JPhyloIOEventReceiver receiver, long startColumn, long endColumn, 
			CharSequence source) throws IOException, IllegalArgumentException {
		
		startColumn = Math.max(0, startColumn);
		endColumn = Math.min(source.length(), endColumn);
		receiver.add(new SequenceTokensEvent(StringUtils.charSequenceToStringList(
				source.subSequence((int)startColumn, (int)endColumn))));
	}
	
	
	public static void writeListSequencePartContent(JPhyloIOEventReceiver receiver, long startColumn,	long endColumn, 
			List<?> source) throws IOException, IllegalArgumentException {
		
		startColumn = Math.max(0, startColumn);
		endColumn = Math.min(source.size(), endColumn);
		int maxTokenCount = (int)(endColumn - startColumn);  // Cannot be out of integer range, since the list size is an int. (Does not work for lists that have more entries than Integer.MAX_VALUE.)
		
		int tokenCount = 0;
		List<String> tokens = createTokenList(maxTokenCount);
		for (Object element : source) {
			tokens.add(element.toString());
			if (tokenCount >= ReadWriteConstants.DEFAULT_MAX_TOKENS_TO_READ) {
				receiver.add(new SequenceTokensEvent(tokens));
				tokenCount = 0;
				tokens = createTokenList(maxTokenCount);
			}
		}
		if (tokenCount > 0) {
			receiver.add(new SequenceTokensEvent(tokens));
		}
	}
	
	
	public static void writeSimpleLiteralMetadata(JPhyloIOEventReceiver receiver, String id, String label, 
			URIOrStringIdentifier predicate, URIOrStringIdentifier originalType, Object objectValue, String stringRepresentation)
			throws IOException {
		
		receiver.add(new LiteralMetadataEvent(id, label, predicate, originalType, LiteralContentSequenceType.SIMPLE));
		receiver.add(new LiteralMetadataContentEvent(objectValue, stringRepresentation));
		receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.META_LITERAL));
	}
	
	
	public static void writeSimpleLiteralMetadata(JPhyloIOEventReceiver receiver, String id, String label, 
			QName predicate, QName originalType, Object objectValue, String stringRepresentation)
			throws IOException {
		
		writeSimpleLiteralMetadata(receiver, id, label, new URIOrStringIdentifier(null, predicate), 
				new URIOrStringIdentifier(null, originalType), objectValue, stringRepresentation);
	}
	
	
	public static void writeSimpleLiteralMetadata(JPhyloIOEventReceiver receiver, String id, String label, 
			QName predicate, QName originalType, Object objectValue) throws IOException {
		
		writeSimpleLiteralMetadata(receiver, id, label, predicate, originalType, objectValue, null);
	}
}
