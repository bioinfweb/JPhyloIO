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


import java.util.EnumSet;
import java.util.NoSuchElementException;
import java.util.Set;

import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventType;



/**
 * The main PhyloIO interface to be implemented by all format specific event readers.
 * 
 * @author Ben St&ouml;ver
 */
public interface JPhyloIOEventReader {
	/**
	 * Returns the maximum number of sequence tokens that shall be bundled into a single event.
	 * Sequences might still be distributed among events containing a smaller number of tokens.
	 * 
	 * @return the maximum number of tokens
	 */
	public int getMaxTokensToRead();
	
	/**
	 * Specify the the maximum number of sequence tokens that shall be bundled into a single event.
	 * Sequences might still be distributed among events containing a smaller number of tokens.
	 * 
	 * @param maxTokensToRead the maximum number of tokens
	 */
	public void setMaxTokensToRead(int maxTokensToRead);
	
	/**
	 * Returns the maximum length a comment may have, before it is split into separate events.
	 * <p>
	 * The default value is {@link #DEFAULT_MAX_COMMENT_LENGTH}.
	 * 
	 * @return the maximum allowed number of characters for a single comment
	 */
	public int getMaxCommentLength();

	/**
	 * Allows the specify the maximum length a comment may have, before it is split into separate events.
	 * 
	 * @param maxCommentLength the maximum allowed number of characters for a single comment that shall be 
	 *        used from now on
	 */
	public void setMaxCommentLength(int maxCommentLength);
	
	/**
	 * Returns whether the match character or token (usually '.') shall automatically be replaced by the 
	 * according token from the first sequence.
	 * 
	 * @return {@code true} if a match token will be replaced, {@code false} otherwise
	 */
	public boolean isTranslateMatchToken();

	/**
	 * Returns the match token to be used for parsing.
	 * <p>
	 * The match token (usually '.') is a token that can be used in all sequences after the first to indicate that
	 * its position is identical with the same position in the first sequence. 
	 * 
	 * @return the match token ('.' by default)
	 */
	public String getMatchToken();

	/**
	 * Allows to specify the match token to be used for parsing. This property should usually not be changed during the
	 * parsing of an alignment.
	 * <p>
	 * The match token (usually '.') is a token that can be used in all sequences after the first to indicate that
	 * its position is identical with the same position in the first sequence.
	 * <p>
	 * Note that this property is only relevant, if {@link #isTranslateMatchToken()} was set to {@code true} in the
	 * constructor. 
	 * 
	 * @param matchToken the new match token to be used from now on
	 */
	public void setMatchToken(String matchToken);
	
	/**
	 * Checks if another event could be parsed from the underlying document.
	 * 
	 * @return {@code true} if another event is waiting, {@code false} if the end of the underlying document was reached
	 */
	public boolean hasNextEvent() throws Exception;
	
	/**
	 * Returns the next event from the underlying document and moves one step forward.
	 * 
	 * @return the next event object
	 * @throws NoSuchElementException if the end of the document has been reached with the previous call of this method
	 * @throws Exception Implementing classes might throw additional exceptions
	 */
	public JPhyloIOEvent next() throws Exception;
	
	/**
	 * Reads elements from the underlying stream until one of the specified is found or the end of the document is reached.
	 * All elements of other types are consumed and ignored.
	 * 
	 * @param types a set of valid types to be returned
	 * @return the next element of the specified type or {@code null} if end of the file was reached before an according
	 *         element was found
	 * @throws Exception Implementing classes might throw additional exceptions
	 */
	public JPhyloIOEvent nextOfType(Set<EventType> types) throws Exception;

	/**
	 * Returns the event from the underlying document that will be returned in the next call of {@link #next()}
	 * without moving forward.
	 * 
	 * @return the next event object
	 * @throws NoSuchElementException if the end of the document has been reached with the previous call of this method
	 * @throws Exception Implementing classes might throw additional exceptions
	 */
	public JPhyloIOEvent peek() throws Exception;
	
	/**
	 * Returns the event that was returned with the last call of {@link #next()}.
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
	 * @throws Exception Implementing classes might throw different types of exceptions if the stream cannot 
	 *         be closed properly
	 */
	public void close() throws Exception;
}
