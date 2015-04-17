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
package info.bioinfweb.jphyloio.events;



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

	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param content the content of the represented comment
	 */
	public CommentEvent(String content) {
		super(EventType.COMMENT);
		this.content = content;
	}


	/**
	 * Returns the content of the comment.
	 * 
	 * @return the content of the comment
	 */
	public String getContent() {
		return content;
	}
}
