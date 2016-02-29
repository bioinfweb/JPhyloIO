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
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.io.IOException;
import java.io.Writer;
import java.util.regex.Pattern;



/**
 * Receiver that writes all encountered sequence tokens and comments to the current line and ignores all metadata.
 * Comments can also be ignored, of {@code null} is specified as the comment start token.
 * <p>
 * It will log an according warning, if comments need to be edited (e.g. because they contain character that are
 * reserved in the target format).
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class SequenceContentReceiver extends AbstractEventReceiver {
	private String commentStart;
	private String commentEnd;
	private boolean longTokens;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param writer the writer to write the sequence to
	 * @param parameterMap the parameter map specified to the calling event writer
	 * @param commentStart a comment start token or {@code null}, if this writer shall ignore comment events
	 * @param commentEnd a comment end token or {@code null}, if this writer shall ignore comment events
	 * @param longTokens Specify {@code true} here if sequence tokens shall be separated by spaces (if one token may
	 *        be longer than one character) or {@code false} otherwise.
	 */
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
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					writeToken(event.asSingleSequenceTokenEvent().getToken());
				}  // End events can be ignored.
				break;
			case SEQUENCE_TOKENS:
				for (String token : event.asSequenceTokensEvent().getCharacterValues()) {
					writeToken(token);
				}
				break;
			case COMMENT:
				if (commentStart != null) {
					getWriter().write(commentStart);
					String content = event.asCommentEvent().getContent();
					String editedContent = content.replaceAll(Pattern.quote(commentEnd), "");
					getWriter().write(editedContent);
					if (!content.equals(editedContent)) {
						getLogger().addWarning("A comment inside a sequence contained one or more comment end symbols used by the target "
								+ "format. The according parts were removed from the comment.");
					}
					getWriter().write(commentEnd);
				}
				else {
					addIgnoredComments(1);
				}
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
