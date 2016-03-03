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
package info.bioinfweb.jphyloio.formats.pde;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.bioinfweb.jphyloio.formats.xml.XMLStreamDataProvider;



/**
 * The XML stream data provider used by {@link PDEEventReader}.
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class PDEStreamDataProvider extends XMLStreamDataProvider<PDEEventReader>{
	private int alignmentLength;
	
	private String otuListID;
	
	private int currentSequenceIndex;
	private List<Map<Integer, String>> sequences;
	
	private String remainingCharacters = null;
	private Map<Long, PDEMetaColumnDefintion> metaColumns = new HashMap<Long, PDEMetaColumnDefintion>();
	
	
	public PDEStreamDataProvider(PDEEventReader eventReader) {
		super(eventReader);
	}


	public int getAlignmentLength() {
		return alignmentLength;
	}


	public void setAlignmentLength(int alignmentLength) {
		this.alignmentLength = alignmentLength;
	}


	public String getOtuListID() {
		return otuListID;
	}


	public void setOtuListID(String otuListID) {
		this.otuListID = otuListID;
	}


	public int getCurrentSequenceIndex() {
		return currentSequenceIndex;
	}


	public void setCurrentSequenceIndex(int currentSequenceIndex) {
		this.currentSequenceIndex = currentSequenceIndex;
	}


	public List<Map<Integer, String>> getSequences() {
		return sequences;
	}


	public void setSequences(List<Map<Integer, String>> sequences) {
		this.sequences = sequences;
	}


	public boolean hasRemainingCharacters() {
		return remainingCharacters != null;
	}
	
	
	public String getRemainingCharacters() {
		return remainingCharacters;
	}


	public void setRemainingCharacters(String remainingCharacters) {
		this.remainingCharacters = remainingCharacters;
	}


	public Map<Long, PDEMetaColumnDefintion> getMetaColumns() {
		return metaColumns;
	}
}
