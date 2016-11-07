/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats.
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
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


import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.ReadWriteConstants;
import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.ResourceMetadataEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;

import java.io.IOException;
import java.net.URI;
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


	public static void writeListSequencePartContent(JPhyloIOEventReceiver receiver, long startColumn, long endColumn,
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
		receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.LITERAL_META));
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


    /**
     * Writes a {@link ResourceMetadataEvent} and its respective end event to the specified receiver.
     *
     * @param receiver the receiver to write the events to
     * @param id the unique ID associated with the represented data element (Must be a valid
     *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
     * @param label a label associated with the represented data element (Maybe {@code null}.)
     * @param rel the <i>RDF</i> rel URI of this element
     * @param hRef the <i>RDF</i> hRef URI of this element
     * @param about the content of a specific about attribute to be written on the according <i>XML</i> representation of this element
     *        (Maybe {@code null}.)
     * @throws NullPointerException if {@code id} or {@code rel} are {@code null}
     * @throws IllegalArgumentException if the specified ID is not a valid
     *         <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>
     * @throws IOException if an I/O error occurs when writing to the specified receiver
     */
    public static void writeTerminalResourceMetadata(JPhyloIOEventReceiver receiver, String id, String label,
            URIOrStringIdentifier rel, URI hRef, String about) throws IOException {

        receiver.add(new ResourceMetadataEvent(id, label, rel, hRef, about));
        receiver.add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.RESOURCE_META));
    }


    /**
     * Writes a {@link ResourceMetadataEvent} and its respective end event to the specified receiver.
     *
     * @param receiver the receiver to write the events to
     * @param id the unique ID associated with the represented data element (Must be a valid
     *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
     * @param label a label associated with the represented data element (Maybe {@code null}.)
     * @param rel the <i>RDF</i> rel URI of this element
     * @param hRef the <i>RDF</i> hRef URI of this element
     * @throws NullPointerException if {@code id} or {@code rel} are {@code null}
     * @throws IllegalArgumentException if the specified ID is not a valid
     *         <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>
     * @throws IOException if an I/O error occurs when writing to the specified receiver
     */
    public static void writeTerminalResourceMetadata(JPhyloIOEventReceiver receiver, String id, String label,
            URIOrStringIdentifier rel, URI hRef) throws IOException {

        writeTerminalResourceMetadata(receiver, id, label, rel, hRef, null);
    }


    /**
     * Writes a {@link ResourceMetadataEvent} and its respective end event to the specified receiver.
     * The string representations of {@code rel} will be set to {@code null}.
     *
     * @param receiver the receiver to write the events to
     * @param id the unique ID associated with the represented data element (Must be a valid
     *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
     * @param label a label associated with the represented data element (Maybe {@code null}.)
     * @param rel the <i>RDF</i> rel URI of this element
     * @param hRef the <i>RDF</i> hRef URI of this element
     * @param about the content of a specific about attribute to be written on the according <i>XML</i> representation of this element
     *        (Maybe {@code null}.)
     * @throws NullPointerException if {@code id} or {@code rel} are {@code null}
     * @throws IllegalArgumentException if the specified ID is not a valid
     *         <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>
     * @throws IOException if an I/O error occurs when writing to the specified receiver
     */
    public static void writeTerminalResourceMetadata(JPhyloIOEventReceiver receiver, String id, String label,
            QName rel, URI hRef, String about) throws IOException {

        writeTerminalResourceMetadata(receiver, id, label, new URIOrStringIdentifier(null, rel), hRef, about);
    }


    /**
     * Writes a {@link ResourceMetadataEvent} and its respective end event to the specified receiver.
     * The string representations of {@code rel} will be set to {@code null}.
     *
     * @param receiver the receiver to write the events to
     * @param id the unique ID associated with the represented data element (Must be a valid
     *        <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>.)
     * @param label a label associated with the represented data element (Maybe {@code null}.)
     * @param rel the <i>RDF</i> rel URI of this element
     * @param hRef the <i>RDF</i> hRef URI of this element
     * @throws NullPointerException if {@code id} or {@code rel} are {@code null}
     * @throws IllegalArgumentException if the specified ID is not a valid
     *         <a href="https://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName">NCName</a>
     * @throws IOException if an I/O error occurs when writing to the specified receiver
     */
    public static void writeTerminalResourceMetadata(JPhyloIOEventReceiver receiver, String id, String label,
            QName rel, URI hRef) throws IOException {

        writeTerminalResourceMetadata(receiver, id, label, rel, hRef, null);
    }
}
