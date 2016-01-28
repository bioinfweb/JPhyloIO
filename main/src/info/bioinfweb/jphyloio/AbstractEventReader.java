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


import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import info.bioinfweb.commons.LongIDManager;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventType;
import info.bioinfweb.jphyloio.tools.SequenceTokensEventManager;



/**
 * Basic implementation for event readers in PhyloIO.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractEventReader implements JPhyloIOEventReader, ReadWriteConstants {
	private JPhyloIOEvent next = null;
	private JPhyloIOEvent previous = null;
	private JPhyloIOEvent lastNonComment = null;
	private StreamDataProvider streamDataProvider;  // Must not be set to anything here.
	private Queue<JPhyloIOEvent> upcomingEvents = new LinkedList<JPhyloIOEvent>();
	private Collection<JPhyloIOEvent> currentEventCollection;
	private boolean beforeFirstAccess = true;
	private boolean dataSourceClosed = false;
	private int maxTokensToRead;
	private boolean translateMatchToken;
	private String matchToken = ".";
	private LongIDManager idManager = new LongIDManager();
	private SequenceTokensEventManager sequenceTokensEventManager = new SequenceTokensEventManager(this);

	
	public AbstractEventReader(boolean translateMatchToken) {
		this(translateMatchToken, DEFAULT_MAX_CHARS_TO_READ);
	}


	public AbstractEventReader(boolean translateMatchToken, int maxTokensToRead) {
		super();
		this.maxTokensToRead = maxTokensToRead;
		this.translateMatchToken = translateMatchToken;
		streamDataProvider = createStreamDataProvider();
		resetCurrentEventCollection();
	}
	
	
	/**
	 * This method is called in the constructor of {@link AbstractEventReader} to initialize the stream
	 * data provider that will be returned by {@link #getStreamDataProvider()}. Inherit classes that use
	 * their own stream data provider implementation should overwrite this method.
	 * <p>
	 * This default implementation creates a new instance of {@link StreamDataProvider}.
	 * 
	 * @return the stream data provider to be used with this instance
	 */
	protected StreamDataProvider createStreamDataProvider() {
		return new StreamDataProvider(this);
	}

	
	protected StreamDataProvider getStreamDataProvider() {
		return streamDataProvider;
	}
	
	
	/**
	 * Sets the current event collection to {@link #getUpcomingEvents()}.
	 * 
	 * @return the replaced event collection
	 */
	protected Collection<JPhyloIOEvent> resetCurrentEventCollection() {
		return setCurrentEventCollection(upcomingEvents);
	}
	
	
	/**
	 * Sets a new current event collection.
	 * 
	 * @param newCollection the new collection to take up new events from now on 
	 * @return the replaced event collection
	 * @throws NullPointerException if {@code newCollection} is {@code null}
	 */
	protected Collection<JPhyloIOEvent> setCurrentEventCollection(Collection<JPhyloIOEvent> newCollection) {
		if (newCollection == null) {
			throw new NullPointerException("The current event collection must not be null.");
		}
		else {
			Collection<JPhyloIOEvent> previous = currentEventCollection;
			currentEventCollection = newCollection;
			return previous;
		}
	}
	
	
	/**
	 * Returns the event collection that is currently used to take up new events.
	 * 
	 * @return the current event collection
	 */
	protected Collection<JPhyloIOEvent> getCurrentEventCollection() {
		return currentEventCollection;
	}
	
	
	/**
	 * Determines whether the current event collection is different from the queue of upcoming events.
	 * 
	 * @return {@code false} if {@link #getCurrentEventCollection()} returns the same instance as {@link #getUpcomingEvents()}
	 *         or {@code true} otherwise
	 */
	protected boolean hasSpecialEventCollection() {
		return upcomingEvents != currentEventCollection;  // equals() does not make sense here, because 
	}
	
	
	/**
	 * Returns the queue to store events in, that shall be returned by upcoming calls of {@link #next()}.
	 * <p>
	 * Note that new events should generally be added to {@link #getCurrentEventCollection()} instead.
	 * 
	 * @return the queue of upcoming events
	 */
	protected Queue<JPhyloIOEvent> getUpcomingEvents() {
		return upcomingEvents;
	}


	@Override
	public int getMaxTokensToRead() {
		return maxTokensToRead;
	}


	@Override
	public void setMaxTokensToRead(int maxTokensToRead) {
		this.maxTokensToRead = maxTokensToRead;
	}


	/**
	 * Returns whether the match character or token (usually '.') shall automatically be replaced by the 
	 * according token from the first sequence.
	 * 
	 * @return {@code true} if a match token will be replaced, {@code false} otherwise
	 */
	@Override
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
	@Override
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
	@Override
	public void setMatchToken(String matchToken) {
		this.matchToken = matchToken;
	}


	protected LongIDManager getIDManager() {
		return idManager;
	}


	/**
	 * Returns the manager object used by this instance to create {@link SequenceTokensEvent}s. Such events should
	 * always be created using the returned object by inherited classes and never directly. 
	 * 
	 * @return the character event manager used by this instance (never {@code null})
	 */
	protected SequenceTokensEventManager getSequenceTokensEventManager() {
		return sequenceTokensEventManager;
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

	
	private JPhyloIOEvent getNextEventFromQueue() throws Exception {
		if (getUpcomingEvents().isEmpty()) {
			readNextEvent();
		}
		return getUpcomingEvents().poll();  // May still return null, if no further event could be added by readNextEvent().
	}
	
	
	@Override
	public boolean hasNextEvent() throws Exception {
		ensureFirstEvent();
		return !dataSourceClosed && (next != null);
	}

	
	@Override
	public JPhyloIOEvent next() throws Exception {
		// ensureFirstEvent() is called in hasNextEvent()
		if (!hasNextEvent()) {
			throw new NoSuchElementException("The end of the document was already reached.");
		}
		else {
			previous = next;  // previous needs to be set before readNextEvent() is called, because it could be accessed in there.
			if (!(previous instanceof CommentEvent)) {  // Also works for possible future subelements of CommentEvent
				lastNonComment = previous;
			}
			next = getNextEventFromQueue();
			return previous;
		}
	}


	@Override
	public JPhyloIOEvent nextOfType(Set<EventType> types) throws Exception {
		try {
			JPhyloIOEvent result = next();
			while (!types.contains(result.getType())) {
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
			next = getNextEventFromQueue();
			beforeFirstAccess = false;
		}
	}
	
	
	/**
	 * Method to be implemented be inherited classes that adds at least one additional event (determined from 
	 * the underlying data source) to the event queue.
	 */
	protected abstract void readNextEvent() throws Exception;


	@Override
	public void close() throws Exception {
		dataSourceClosed = true;
	}
}
