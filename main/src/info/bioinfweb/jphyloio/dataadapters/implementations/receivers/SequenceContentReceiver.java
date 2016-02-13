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
package info.bioinfweb.jphyloio.dataadapters.implementations.receivers;


import info.bioinfweb.jphyloio.EventWriterParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;




public class SequenceContentReceiver extends AbstractEventReceiver {
	private String commentStart;
	private String commentEnd;
	private boolean longTokens;
	
	
	public SequenceContentReceiver(Writer writer,	EventWriterParameterMap parameterMap, String commentStart,
			String commentEnd, boolean longTokens) {
		
		super(writer, parameterMap);
		this.commentStart = commentStart;
		this.commentEnd = commentEnd;
		this.longTokens = longTokens;
	}
	
	
	private void writeToken(String token) throws IOException {
		getWriter().write(token);
		if (longTokens) {
			getWriter().write(' ');
		}
	}


	@Override
	public boolean add(JPhyloIOEvent event) throws IllegalArgumentException, ClassCastException, IOException {
		switch (event.getType().getContentType()) {
			case SINGLE_SEQUENCE_TOKEN:
				writeToken(event.asSingleSequenceTokenEvent().getToken());
				break;
			case SEQUENCE_TOKENS:
				for (String token : event.asSequenceTokensEvent().getCharacterValues()) {
					writeToken(token);
				}
				break;
			case COMMENT:
				getWriter().write(commentStart);
				getWriter().write(event.asCommentEvent().getContent().replaceAll(Pattern.quote(commentStart), ""));
				getWriter().write(commentEnd);
				break;
			case META_INFORMATION:  //TODO Filter comments nested in metadata by counting metadata level. (Possibly use superclass shared with NewickNodeEdgeEventReceiver.)
				addIgnoredMetadata(1);
				break;
			default:
				break;
		}
		return true;
	}	
}
