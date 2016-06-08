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
package info.bioinfweb.jphyloio.formats.text;


import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.XMLStreamException;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.implementations.receivers.BasicEventReceiver;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.exception.IllegalEventException;



/**
 * Basic event receiver implementation that writes comments to text formats that are delimited by a start and an end string. 
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class BasicCommentEventReceiver extends BasicEventReceiver<Writer> {
	private String commentStart;
	private String commentEnd;
	private boolean isInComment = false;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param writer the writer to write data to
	 * @param parameterMap the parameter map of the event writer that will be using this receiver instance
	 * @param commentStart a string indicating the start of a comment in the target format
	 * @param commentEnd a string indicating the end of a comment in the target format
	 */
	public BasicCommentEventReceiver(Writer writer, ReadWriteParameterMap parameterMap, String commentStart, String commentEnd) {
		super(writer, parameterMap);
		this.commentStart = commentStart;
		this.commentEnd = commentEnd;
	}


	@Override
	public boolean add(JPhyloIOEvent event) throws IOException {
		if (isInComment && !event.getType().getContentType().equals(EventContentType.COMMENT)) {
			throw new IllegalEventException(
					"A non-comment event was encountered after a comment event that indicated to be continued in the next event.", 
					this, getParentEvent(), event);
		}
		else {
			return super.add(event);
		}
	}


	@Override
	protected void handleComment(CommentEvent event) throws IOException, XMLStreamException {
		if (!isInComment) {
			getWriter().write(commentStart);
		}
		getWriter().write(event.getContent());
		isInComment = event.isContinuedInNextEvent();
		if (!isInComment) {
			getWriter().write(commentEnd);
		}
	}
}