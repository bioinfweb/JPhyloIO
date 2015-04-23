/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;

import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EventType;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceCharactersEvent;



/**
 * Basic implementation for event readers in PhyloIO.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractEventReader implements JPhyloIOEventReader, ReadWriteConstants {
	private JPhyloIOEvent next = null;
	private JPhyloIOEvent previous = null;
	private JPhyloIOEvent lastNonComment = null;
	private boolean beforeFirstAccess = true;
	private boolean dataSourceClosed = false;
	private int maxTokensToRead;
	private boolean translateMatchToken;
	private String matchToken = ".";
	private List<String> firstSequence = new ArrayList<String>();
	private long currentPosition = -1;
	/** Start position for sequences in current interleaved block */
	private long currentBlockStartPosition = 0;
	private long currentBlockLength = 0;
	private String firstSequenceName = null;
	private String currentSequenceName = null;
	

	
	public AbstractEventReader(boolean translateMatchToken) {
		this(translateMatchToken, DEFAULT_MAX_CHARS_TO_READ);
	}


	public AbstractEventReader(boolean translateMatchToken, int maxTokensToRead) {
		super();
		this.maxTokensToRead = maxTokensToRead;
		this.translateMatchToken = translateMatchToken;
	}


	public int getMaxTokensToRead() {
		return maxTokensToRead;
	}


	public void setMaxTokensToRead(int maxTokensToRead) {
		this.maxTokensToRead = maxTokensToRead;
	}


	/**
	 * Returns whether the match character or token (usually '.') shall automatically be replaced by the 
	 * according token from the first sequence.
	 * 
	 * @return {@code true} if a match token will be replaced, {@code false} otherwise
	 */
	public boolean isTranslateMatchToken() {
		return translateMatchToken;
	}


	/**
	 * Returns the match token to be used for parsing.
	 * <p>
	 * The match token (usually '.') is a token that can be used in all sequences after the first to indicate that
	 * its position is identical with the same position in the first sequence. 
	 * 
	 * @return the match token ('.' by default)
	 */
	public String getMatchToken() {
		return matchToken;
	}


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
	public void setMatchToken(String matchToken) {
		this.matchToken = matchToken;
	}


	/**
	 * Returns the event that has been returned by the previous call of {@link #readNextEvent()}.
	 * 
	 * @return the previous event or {@code null} if there was no previous call of {@link #readNextEvent()}
	 */
	public JPhyloIOEvent getPreviousEvent() {
		return previous;
	}
	
	
	/**
	 * Returns the last event that has been returned by previous calls of {@link #readNextEvent()}
	 * that was not a comment event.
	 * 
	 * @return the last non-comment event or {@code null} if no non-comment event was returned until now
	 */
	public JPhyloIOEvent getLastNonCommentEvent() {
		return lastNonComment;
	}


	/**
	 * Indicates whether there have been any previous calls of {@link #readNextEvent()} since the last call
	 * of {@link #reset()}.
	 * <p>
	 * This method in meant for internal use in {@link #readNextEvent()}.
	 * 
	 * @return {@code true} if there were no previous calls, {@code false} otherwise
	 */
	protected boolean isBeforeFirstAccess() {
		return beforeFirstAccess;
	}

	
	protected JPhyloIOEvent createAlignmentStartEvent(int sequenceCount, int characterCount) {
		JPhyloIOEvent event = new ConcreteJPhyloIOEvent(EventType.ALIGNMENT_START);
		event.getMetaInformationMap().put(META_KEY_SEQUENCE_COUNT, sequenceCount);
		event.getMetaInformationMap().put(META_KEY_CHARACTER_COUNT, characterCount);
		return event;
	}
	
	
	protected String replaceMatchToken(String token) {
		if (token.equals(getMatchToken())) {
			if (currentPosition > Integer.MAX_VALUE) {
				throw new IndexOutOfBoundsException("Sequences with more than " + Integer.MAX_VALUE + 
						" characters are not supported if replacing match tokens is switched on."); 
			}
			else if (currentPosition >= firstSequence.size()) {
				throw new IndexOutOfBoundsException("The match token in column " + currentPosition + 
						" cannot be replaced because the first sequence only has " + firstSequence.size() + " characters."); 
			}
			else {
				return firstSequence.get((int)currentPosition);
			}
		}
		else {
			return token;
		}
	}
	
	
	/**
	 * Creates a sequence character event object from the provided data and manages the replacement of match tokens
	 * by tokens of the first sequence.
	 * <p>
	 * This method stores the tokens of the first sequence and the current sequence position. Therefore implementing 
	 * readers supporting match token replacement must always use this method to create sequence character event 
	 * objects and never do this directly, otherwise the replacement by this method will not work.
	 * 
	 * @param sequenceName the name of the sequence to append the tokens
	 * @param tokens the newly read tokens
	 * @return the event object
	 * @throws NullPointerException if either the sequence name or the token list is {@code null}
	 */
	protected SequenceCharactersEvent createSequenceCharactersEvent(String sequenceName, List<String> tokens) {
		if ((sequenceName == null) || (tokens == null)) {
			throw new NullPointerException("Sequence names must not be null.");
		}
		else {
			if (isTranslateMatchToken()) {
				if (!sequenceName.equals(currentSequenceName)) {
					currentSequenceName = sequenceName;
					currentPosition = currentBlockStartPosition;
				}
				if (firstSequenceName == null) {
					firstSequenceName = sequenceName;
				}
				
				if (firstSequenceName.equals(sequenceName)) {
					firstSequence.addAll(tokens);
					currentBlockStartPosition += currentBlockLength;  // Add length of previous block that is now finished.
					currentBlockLength = tokens.size();  // Save length of current block to add it to the start after it was processed.
				}
				else {
					for (int i = 0; i < tokens.size(); i++) {
						tokens.set(i, replaceMatchToken(tokens.get(i)));
						currentPosition++;
					}
				}
			}
			return new SequenceCharactersEvent(sequenceName, tokens);
		}
	}
	

	@Override
	public boolean hasNextEvent() throws Exception {
		ensureFirstEvent();
		return !dataSourceClosed && (next != null);
	}

	
	@Override
	public JPhyloIOEvent next() throws Exception {
		// ensureFirstEvent() is called in hasNextEvent()
		if (!hasNextEvent()) {  //
			throw new NoSuchElementException("The end of the document was already reached.");
		}
		else {
			previous = next;  // previous needs to be set before readNextEvent() is called, because it could be accessed in there.
			if (!(previous instanceof CommentEvent)) {  // Also works for possible future subelements of CommentEvent
				lastNonComment = previous;
			}
			next = readNextEvent();
			return previous;
		}
	}


	@Override
	public JPhyloIOEvent nextOfType(EnumSet<EventType> types) throws Exception {
		try {
			JPhyloIOEvent result = next();
			while (!types.contains(result.getEventType())) {
				result = next();
			}
			return result;
		}
		catch (NoSuchElementException e) {
			return null;
		}
	}


	@Override
	public JPhyloIOEvent peek() throws Exception {
		// ensureFirstEvent() is called in hasNextEvent()
		if (!hasNextEvent()) {  //
			throw new NoSuchElementException("The end of the document was already reached.");
		}
		else {
			return next;
		}
	}


	private void ensureFirstEvent() throws Exception {
		if (beforeFirstAccess) {
			next = readNextEvent();
			beforeFirstAccess = false;
		}
	}
	
	
	/**
	 * Method to be implemented be inherited classes that returns the next event determined from the underlying
	 * data source.
	 * 
	 * @return the next event or {@code null} if the end of the document has been reached
	 */
	protected abstract JPhyloIOEvent readNextEvent() throws Exception;


	@Override
	public void close() throws Exception {
		dataSourceClosed = true;
	}


	@Override
	public void reset() throws Exception {
		next = null;
		previous = null;
		lastNonComment = null;
		beforeFirstAccess = true;
	}
}
