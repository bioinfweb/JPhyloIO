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
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.push.JPhyloIOEventListener;



/**
 * This is main <i>JPhyloIO</i> interface to be implemented by all format specific event readers.
 * <p>
 * Events representing the content of a document can be processed by subsequent calls of {@link #next()}. The event sequence 
 * generated by implementing event readers must match the following 
 * <a href="https://en.wikipedia.org/wiki/Extended_Backus%E2%80%93Naur_Form">EBNF</a>. All events are objects that implement
 * {@link JPhyloIOEvent} and the type of data they represent is determined by their {@link EventContentType}, which is shown
 * in this grammar.
 * <p>
 * Some events enclose a subsequence of the event stream and are therefore separated into a {@code START}
 * and an {@code END} version. Between such two events other events may be present that model nested content (e.g. sequences 
 * between an matrix {@code START} and {@code END} event). Other events that may not contain any nested content only occur in 
 * a single <code>SOLE</code> version. Which is type of event is found is determined by its {@link EventTopologyType}.
 * 
 * <h3><a id="grammar"></a>Event grammar</h3> 
 * <pre>
 * Document = "DOCUMENT.START", {DocumentContent,} "DOCUMENT.END";
 * DocumentContent = OTUList | Matrix | TreeNetworkGroup | OTUSet | CharacterSetPart | TreeNetworkSet | MetaInformation;
 * 
 * OTUList = "OTUS.START", {OTUListContent,} {OTUSet,} "OTUS.END";
 * OTUListContent = OTU | MetaInformation;
 * OTU = "OTU.START", {MetaInformation,} "OTU.END";
 * OTUSet = "OTU_SET.START", {SetContent,} "OTU_SET.END";
 * 
 * Matrix = "ALIGNMENT.START", {MatrixContent,} "ALIGNMENT.END";
 * MatrixContent = CharacterDefinition | TokenSetDefinition | SequencePart | CharacterSetPart | SequenceSet | MetaInformation;
 * 
 * CharacterDefinition = "CHARACTER_DEFINITION.START" {MetaInformation,} "CHARACTER_DEFINITION.END";
 * SequenceSet = "SEQUENCE_SET.START" {SetContent,} "SEQUENCE_SET.END";
 * 
 * TokenSetDefinition = "TOKEN_SET_DEFINITION.START", {TokenSetDefinitionContent,} "TOKEN_SET_DEFINITION.END";
 * TokenSetDefinitionContent = "CHARACTER_SET_INTERVAL.SOLE" | SingleTokenDefinition | MetaInformation;
 * SingleTokenDefinition = "SINGLE_TOKEN_DEFINITION.START", {MetaInformation,} "SINGLE_TOKEN_DEFINITION.END";
 * 
 * SequencePart = "SEQUENCE.START", {SequencePartContent,} "SEQUENCE.END";
 * SequencePartContent = "SEQUENCE_TOKENS.SOLE" | SingleSequenceToken | MetaInformation;
 * SingleSequenceToken = "SINGLE_SEQUENCE_TOKEN.START", {MetaInformation,} "SINGLE_SEQUENCE_TOKEN.END";
 * 
 * CharacterSetPart = "CHARACTER_SET.START", {CharacterSetPartContent,} "CHARACTER_SET.END";
 * CharacterSetPartContent = "CHARACTER_SET_INTERVAL.SOLE" | SetContent;  (* In character sets only references to other character sets (and not single character definitions) are using "SET_ELEMENT.SOLE". *)
 * 
 * TreeNetworkGroup = "TREE_NETWORK_GROUP.START", {TreeNetworkGroupContent,} "TREE_NETWORK_GROUP.END";
 * TreeNetworkGroupContent = Tree | Network | TreeNetworkSet;
 * Tree = "TREE.START", {TreeOrNetworkContent,} ["ROOT_EDGE.START",] {TreeOrNetworkContent,} {NodeEdgeSet,} "TREE.END";
 * Network = "NETWORK.START", {TreeOrNetworkContent,} {NodeEdgeSet,} "NETWORK.END";
 * TreeOrNetworkContent = Node | Edge | MetaInformation;
 * Node = "NODE.START", {MetaInformation,} "NODE.END";
 * Edge = "EDGE.START", {MetaInformation,} "EDGE.END";
 * 
 * TreeNetworkSet = "TREE_NETWORK_SET.START" {SetContent,} "TREE_NETWORK_SET.END";
 * NodeEdgeSet = "NODE_EDGE_SET.START" {SetContent,} "NODE_EDGE_SET.END";
 * 
 * SetContent = "SET_ELEMENT.SOLE" | MetaInformation;  (* Single elements and other sets of the same type can be linked using "SET_ELEMENT.SOLE". *)
 * 
 * MetaInformation = ResourceMeta | LiteralMeta;
 * ResourceMeta = "RESOURCE_META.START", {MetaInformation,} "RESOURCE_META.END";
 * LiteralMeta = "LITERAL_META.START", {"LITERAL_META_CONTENT.SOLE",} "LITERAL_META.END";
 * </pre>
 * Additionally {@link CommentEvent}s ({@code "COMMENT.SOLE"}) may occur at any position in the stream, which 
 * is not shown in the grammar for greater clarity.
 * <p>
 * Note that one event may only reference other events that have been fired before, although this is not directly expressed by the
 * grammar in all cases. (Examples are edges referencing nodes or sequences referencing OTUs.)   
 * <p>
 * The documentation of {@link EventContentType} contains information on which event classes are used for which
 * event type.
 * 
 * @author Ben St&ouml;ver
 * @see EventContentType
 * @see EventTopologyType
 * @see JPhyloIOEvent
 * @see JPhyloIOEventWriter
 */
public interface JPhyloIOEventReader extends JPhyloIOFormatSpecificObject {
	//TODO Grammar: Should the SequenceSet and NodeEdgeSet also be allowed in document level? (It would currently not be necessary, since it does not exists in Nexus.)
	
	/*
	 * In contrast the the structure of NeXML, this grammar does not differentiate between the actual matrix and data related
	 * to it (e.g. token and character sets). That is not done, because parsing interleaved MEGA would create the need to
	 * buffer all sequence events, if mixing them with character set events on the same level would not be legal. As a 
	 * consequence the NeXML matrix and format tags have no equivalent JPhyloIO event and their metadata need to be nested
	 * under an implicit resource meta event. 
	 */
	
	/**
	 * The returned object provides information on the start events fired by this reader until now. It allows
	 * applications to determine information on the nesting of the current event.
	 * 
	 * @return the parent information object
	 */
	public ParentEventInformation getParentInformation();
		

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
	
	/**
	 * Adds a {@link JPhyloIOEventListener} to the reader. 
	 * 
	 * This is not intended to replace processing events gained by calling a readers next() method, 
	 * but is just used by certain tool classes.
	 * 
	 * @param listener the listener that shall be added
	 */
	public void addEventListener(JPhyloIOEventListener listener);
	
	/**
	 * Removes a {@link JPhyloIOEventListener} from the reader.
	 * 
	 * @param listener the listener that shall be removed
	 */
	public void removeEventListener(JPhyloIOEventListener listener);
}
