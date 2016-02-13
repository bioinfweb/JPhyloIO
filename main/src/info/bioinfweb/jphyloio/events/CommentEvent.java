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
package info.bioinfweb.jphyloio.events;

import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



/**
 * Event that indicates that a comment has been parsed at the current position or within the data the last
 * non-comment event has been parsed from.
 * <p>
 * Nested comments as they are possible e.g. in the MEGA format do not produce separate events.   
 * 
 * @author Ben St&ouml;ver
 */
public class CommentEvent extends ConcreteJPhyloIOEvent {
	private String content;
	private boolean continuedInNextEvent;

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param content the content of the represented comment
	 * @param continuedInNextEvent Specify {@code true} here if this event does not contain the final part of 
	 *        this comment are more events are ahead.
	 */
	public CommentEvent(String content, boolean continuedInNextEvent) {
		super(EventContentType.COMMENT, EventTopologyType.SOLE);  //TODO Possibly change to START later.
		this.content = content;
		this.continuedInNextEvent = continuedInNextEvent;
	}


	/**
	 * Creates a new instance of this class that is not contunied in the next event.
	 * 
	 * @param content the content of the represented comment
	 */
	public CommentEvent(String content) {
		this(content, false);
	}
	
	
	/**
	 * Returns the content of the comment.
	 * 
	 * @return the content of the comment
	 */
	public String getContent() {
		return content;
	}


	/**
	 * Returns whether this event only contains a part of a long comment and the next event(s) will contain
	 * additional characters from the current comment. (The final event of a split comment returns {@code true} here.)
	 * 
	 * @return {@code false} if this event includes the final characters of the current comment (always the case for
	 *         comments that are not split between events) and {@code true} if future events will contain the
	 *         remaining characters from the current comment.
	 */
	public boolean isContinuedInNextEvent() {
		return continuedInNextEvent;
	}
}
