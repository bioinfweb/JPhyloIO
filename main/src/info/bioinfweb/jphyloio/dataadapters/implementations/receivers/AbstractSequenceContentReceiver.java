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


import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.MetaInformationEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;



public abstract class AbstractSequenceContentReceiver<W extends Object> extends AbstractEventReceiver<W> {
	private boolean longTokens;
	
	
	public AbstractSequenceContentReceiver(W writer,	ReadWriteParameterMap parameterMap, String commentStart,
			String commentEnd, boolean longTokens) {
		
		super(writer, parameterMap);
		this.longTokens = longTokens;
	}
	
	
	protected abstract void writeSingleToken(String token) throws Exception;
	
	
	protected abstract void writeTokens(SequenceTokensEvent event) throws Exception;

	
	protected abstract void writeComment(CommentEvent event) throws Exception;
	
	
	protected abstract void writeMetaData(MetaInformationEvent event) throws Exception;
	

	@Override
	public boolean add(JPhyloIOEvent event) throws Exception {
		switch (event.getType().getContentType()) {
			case SINGLE_SEQUENCE_TOKEN:
				if (event.getType().getTopologyType().equals(EventTopologyType.START)) {
					writeSingleToken(event.asSingleSequenceTokenEvent().getToken());
				}  // End events can be ignored.
				break;
			case SEQUENCE_TOKENS:
				writeTokens(event.asSequenceTokensEvent());
				break;
			case COMMENT:
				writeComment(event.asCommentEvent());
				break;
			case META_INFORMATION:  //TODO Filter comments nested in metadata by counting metadata level. (Possibly use superclass shared with NewickNodeEdgeEventReceiver.)
				writeMetaData(event.asMetaInformationEvent());
				break;
			default:
				break;
		}
		return true;
	}


	public boolean isLongTokens() {
		return longTokens;
	}
}
