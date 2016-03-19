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
package info.bioinfweb.jphyloio;


import java.io.IOException;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Set;

import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventType;



/**
 * The main JPhyloIO interface to be implemented by all format specific event readers.
 * <p>
 * Events can be processed by subsequent calls of {@link #next()}. The event sequence generated by
 * implementing event readers must match the following 
 * <a href="https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_Form">EBNF</a>:
 * <pre>
 * Document = "DOCUMENT.START", {DocumentContent,} "DOCUMENT.END";
 * DocumentContent = OTUSet | Matrix | Tree | Network | CharacterSetPart | MetaInformation;
 * 
 * OTUSet = "OTUS.START", {OTUContent,} "OTUS.END";
 * OTUContent = OTU | MetaInformation;
 * OTU = "OTU.START", {MetaInformation,} "OTU.END";
 * 
 * Matrix = "ALIGNMENT.START", {MatrixContent,} "ALIGNMENT.END";
 * MatrixContent = TokenSetDefinition | SequencePart | CharacterSetPart | MetaInformation;
 * 
 * TokenSetDefinition = "TOKEN_SET_DEFINITION.START", {TokenSetDefinitionContent,} "TOKEN_SET_DEFINITION.END";
 * TokenSetDefinitionContent = SingleTokenDefinition | MetaInformation;
 * SingleTokenDefinition = "SINGLE_TOKEN_DEFINITION.START", {MetaInformation,} "SINGLE_TOKEN_DEFINITION.END";
 * 
 * SequencePart = "SEQUENCE.START", {SequencePartContent,}, "SEQUENCE.END";
 * SequencePartContent = "SEQUENCE_TOKENS.SOLE" | SingleSequenceToken | MetaInformation;
 * SingleSequenceToken = "SINGLE_SEQUENCE_TOKEN.START", {MetaInformation,} "SINGLE_SEQUENCE_TOKEN.END";
 * 
 * CharacterSetPart = "CHARACTER_SET.START", {CharacterSetPartContent,} "CHARACTER_SET.END";
 * CharacterSetPartContent = "CHARACTER_SET_PART.SOLE" | MetaInformation;
 * 
 * Tree = "TREE.START", {TreeOrNetworkContent,} "TREE.END";
 * Network = "NETWORK.START", {TreeOrNetworkContent,} "NETWORK.END";
 * TreeOrNetworkContent = Node | Edge | MetaInformation;
 * Node = "NODE.START", {MetaInformation,} "NODE.END";
 * Edge = "EDGE.START", {MetaInformation,} "EDGE.END";
 * 
 * MetaInformation = ResourceMeta | LiteralMeta;
 * ResourceMeta = "RESOURCE_META.START", {MetaInformation,} "RESOURCE_META.END";
 * LiteralMeta = "LITERAL_META.START", {"LITERAL_META_CONTENT.SOLE",} "LITERAL_META.END";
 * </pre>
 * Additionally {@link CommentEvent}s ({@code "COMMENT.SOLE"}) may occur at any position in the stream, which 
 * is not shown in the grammar for greater clarity.
 * <p>
 * The documentation of {@link EventContentType} contains information on which event classes are used for which
 * event type.
 * 
 * @author Ben St&ouml;ver
 * @see EventContentType
 * @see JPhyloIOEventWriter
 */
//TODO Would it make sense, to have no content event nested in LITERAL_META or must there be at least one? (Can NeXML have empty meta tags?)
//TODO LITRAL_META_CONTENT can represent XML events, simple numeric or string values or also contain more complex objects as values. (In such cases custom XML could already be parsed as (optional) an alternative to giving single XML content events.)
public interface JPhyloIOEventReader extends JPhyloIOFormatSpecificObject {
	/**
	 * Checks if another event could be parsed from the underlying document.
	 * 
	 * @return {@code true} if another event is waiting, {@code false} if the end of the underlying document was reached
	 */
	public boolean hasNextEvent() throws IOException;
	
	/**
	 * Returns the next event from the underlying document and moves one step forward.
	 * 
	 * @return the next event object
	 * @throws NoSuchElementException if the end of the document has been reached with the previous call of this method
	 * @throws IOException Implementing classes might throw additional exceptions
	 */
	public JPhyloIOEvent next() throws IOException;
	
	/**
	 * Reads elements from the underlying stream until one of the specified is found or the end of the document is reached.
	 * All elements of other types are consumed and ignored.
	 * 
	 * @param types a set of valid types to be returned
	 * @return the next element of the specified type or {@code null} if end of the file was reached before an according
	 *         element was found
	 * @throws IOException Implementing classes might throw additional exceptions
	 */
	public JPhyloIOEvent nextOfType(Set<EventType> types) throws IOException;

	/**
	 * Returns the event from the underlying document that will be returned in the next call of {@link #next()}
	 * without moving forward.
	 * 
	 * @return the next event object
	 * @throws NoSuchElementException if the end of the document has been reached with the previous call of this method
	 * @throws IOException Implementing classes might throw additional exceptions
	 */
	public JPhyloIOEvent peek() throws IOException;
	
	/**
	 * Returns the event that was returned with the last call of {@link #next()}.
	 * <p>
	 * Note that calling this method will not move the cursor, but will only return the last event. 
	 * Subsequent calls would therefore always return the same event. (This method is different from
	 * e.g. {@link ListIterator#previous()}.) 
	 * 
	 * @return the last event or {@code null} if no event was returned yet
	 * @see #getLastNonCommentEvent()
	 */
	public JPhyloIOEvent getPreviousEvent();	
	
	/**
	 * Returns the last event that has been returned by previous calls of {@link #readNextEvent()}
	 * that was not a comment event.
	 * 
	 * @return the last non-comment event or {@code null} if no non-comment event was returned until now
	 * @see #getPreviousEvent()
	 */
	public JPhyloIOEvent getLastNonCommentEvent();
	
	/**
	 * Closes the underlying document source (usually a stream).
	 * 
	 * @throws IOException Implementing classes might throw different types of exceptions if the stream cannot 
	 *         be closed properly
	 */
	public void close() throws IOException;
}
