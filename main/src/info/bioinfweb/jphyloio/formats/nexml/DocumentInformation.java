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
package info.bioinfweb.jphyloio.formats.nexml;


import info.bioinfweb.commons.bio.CharacterStateSetType;

import java.util.Set;
import java.util.TreeSet;



public class DocumentInformation {
	private boolean empty; //is true if the document contains nothing or only document meta data
	private boolean hasOTUList; //is true if the document contains at least one OTU list
	private Set<String> metaDataNameSpaces = new TreeSet<String>();
	
	private boolean writeCellsTags;
	private CharacterStateSetType alignmentType;
	
	private boolean writeUndefinedOTU;

	
	public boolean isEmpty() {
		return empty;
	}
	

	public void setEmpty(boolean empty) {
		this.empty = empty;
	}


	public boolean hasOTUList() {
		return hasOTUList;
	}


	public void setHasOTUList(boolean hasOTUList) {
		this.hasOTUList = hasOTUList;
	}


	public Set<String> getMetaDataNameSpaces() {
		return metaDataNameSpaces;
	}


	public boolean isWriteCellsTags() {
		return writeCellsTags;
	}


	public void setWriteCellsTags(boolean writeCellsTags) {
		this.writeCellsTags = writeCellsTags;
	}


	public CharacterStateSetType getAlignmentType() {
		return alignmentType;
	}


	public void setAlignmentType(CharacterStateSetType alignmentType) {
		this.alignmentType = alignmentType;
	}


	public boolean isWriteUndefinedOTU() {
		return writeUndefinedOTU;
	}


	public void setWriteUndefinedOTU(boolean writeUndefinedOTU) {
		this.writeUndefinedOTU = writeUndefinedOTU;
	}
}
