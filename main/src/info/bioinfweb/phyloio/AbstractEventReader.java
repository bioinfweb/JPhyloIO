/*
 * PhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
 * <http://bioinfweb.info/PhyloIO>
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
package info.bioinfweb.phyloio;


import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;

import info.bioinfweb.commons.io.PeekReader;
import info.bioinfweb.phyloio.events.CommentEvent;
import info.bioinfweb.phyloio.events.ConcretePhyloIOEvent;
import info.bioinfweb.phyloio.events.EventType;
import info.bioinfweb.phyloio.events.PhyloIOEvent;
import info.bioinfweb.phyloio.events.SequenceCharactersEvent;



/**
 * Basic implementation for event readers in PhyloIO.
 * 
 * @author Ben St&ouml;ver
 */
public abstract class AbstractEventReader implements PhyloIOEventReader, ReadWriteConstants {
	private PhyloIOEvent next = null;
	private PhyloIOEvent previous = null;
	private PhyloIOEvent lastNonComment = null;
	private boolean beforeFirstAccess = true;
	private boolean dataSourceClosed = false;
	private int maxTokensToRead;

	
	public AbstractEventReader() {
		this(DEFAULT_MAX_CHARS_TO_READ);
	}


	public AbstractEventReader(int maxTokensToRead) {
		super();
		this.maxTokensToRead = maxTokensToRead;
	}


	public int getMaxTokensToRead() {
		return maxTokensToRead;
	}


	public void setMaxTokensToRead(int maxTokensToRead) {
		this.maxTokensToRead = maxTokensToRead;
	}


	/**
	 * Returns the event that has been returned by the previous call of {@link #readNextEvent()}.
	 * 
	 * @return the previous event or {@code null} if there was no previous call of {@link #readNextEvent()}
	 */
	public PhyloIOEvent getPreviousEvent() {
		return previous;
	}
	
	
	/**
	 * Returns the last event that has been returned by previous calls of {@link #readNextEvent()}
	 * that was not a comment event.
	 * 
	 * @return the last non-comment event or {@code null} if no non-comment event was returned until now
	 */
	public PhyloIOEvent getLastNonCommentEvent() {
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

	
	protected PhyloIOEvent createAlignmentStartEvent(int sequenceCount, int characterCount) {
		PhyloIOEvent event = new ConcretePhyloIOEvent(EventType.ALIGNMENT_START);
		event.getMetaInformationMap().put(META_KEY_SEQUENCE_COUNT, sequenceCount);
		event.getMetaInformationMap().put(META_KEY_CHARACTER_COUNT, characterCount);
		return event;
	}
	

	@Override
	public boolean hasNextEvent() throws Exception {
		ensureFirstEvent();
		return !dataSourceClosed && (next != null);
	}

	
	@Override
	public PhyloIOEvent next() throws Exception {
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
	public PhyloIOEvent nextOfType(EnumSet<EventType> types) throws Exception {
		try {
			PhyloIOEvent result = next();
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
	public PhyloIOEvent peek() throws Exception {
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
	protected abstract PhyloIOEvent readNextEvent() throws Exception;


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
