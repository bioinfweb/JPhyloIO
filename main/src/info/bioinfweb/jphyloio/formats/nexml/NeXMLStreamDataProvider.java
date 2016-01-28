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

import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLElementReader.OTUEventInformation;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;


public class NeXMLStreamDataProvider {
	NeXMLEventReader nexmlEventReader;

	private String currentBranchLengthsFormat;
	
	private Map<String, NeXMLTokenSetInformation> tokenSets = new TreeMap<String, NeXMLTokenSetInformation>();	
	
	private String currentTokenSetID = null;
	private String currentTokenSetType;
	private Collection<String> constituents;
	
	private Map<String, String> otuIDToLabelMap = new TreeMap<String, String>();	
	private Map<String, String> tokenDefinitionIDToSymbolMap = new TreeMap<String, String>();	
	private String currentSingleTokenDefinitionID = null;	
	private int startChar;
	private int currentChar;
	private String currentStates;
	
	
	public NeXMLStreamDataProvider(NeXMLEventReader nexmlEventReader) {
		super();
		this.nexmlEventReader = nexmlEventReader;
	}
	

	public NeXMLEventReader getNexmlEventReader() {
		return nexmlEventReader;
	}
	

	public void setNexmlEventReader(NeXMLEventReader nexmlEventReader) {
		this.nexmlEventReader = nexmlEventReader;
	}


	public Map<String, String> getOtuIDToLabelMap() {
		return otuIDToLabelMap;
	}


	public void setOtuIDToLabelMap(Map<String, String> otuIDToLabelMap) {
		this.otuIDToLabelMap = otuIDToLabelMap;
	}


	public Map<String, NeXMLTokenSetInformation> getTokenSets() {
		return tokenSets;
	}


	public void setTokenSets(Map<String, NeXMLTokenSetInformation> tokenSets) {
		this.tokenSets = tokenSets;
	}


	public String getCurrentTokenSetType() {
		return currentTokenSetType;
	}


	public void setCurrentTokenSetType(String currentTokenSetType) {
		this.currentTokenSetType = currentTokenSetType;
	}


	public Map<String, String> getTokenDefinitionIDToSymbolMap() {
		return tokenDefinitionIDToSymbolMap;
	}


	public void setTokenDefinitionIDToSymbolMap(
			Map<String, String> tokenDefinitionIDToSymbolMap) {
		this.tokenDefinitionIDToSymbolMap = tokenDefinitionIDToSymbolMap;
	}


	public String getCurrentTokenSetID() {
		return currentTokenSetID;
	}


	public void setCurrentTokenSetID(String currentTokenSetID) {
		this.currentTokenSetID = currentTokenSetID;
	}


	public String getCurrentSingleTokenDefinitionID() {
		return currentSingleTokenDefinitionID;
	}


	public void setCurrentSingleTokenDefinitionID(
			String currentSingleTokenDefinitionID) {
		this.currentSingleTokenDefinitionID = currentSingleTokenDefinitionID;
	}


//	public Collection<JPhyloIOEvent> getCurrentSingleTokens() {
//		return currentSingleTokens;
//	}
//
//
//	public void setCurrentSingleTokens(Collection<JPhyloIOEvent> currentSingleTokens) {
//		this.currentSingleTokens = currentSingleTokens;
//	}
//
//
//	public Collection<JPhyloIOEvent> getCurrentCharSetIntervals() {
//		return currentCharSetIntervals;
//	}
//
//
//	public void setCurrentCharSetIntervals(
//			Collection<JPhyloIOEvent> currentCharSetIntervals) {
//		this.currentCharSetIntervals = currentCharSetIntervals;
//	}


	public Collection<String> getConstituents() {
		return constituents;
	}


	public void setConstituents(Collection<String> constituents) {
		this.constituents = constituents;
	}


	public String getCurrentBranchLengthsFormat() {
		return currentBranchLengthsFormat;
	}


	public void setCurrentBranchLengthsFormat(String currentBranchLengthsFormat) {
		this.currentBranchLengthsFormat = currentBranchLengthsFormat;
	}


	public int getStartChar() {
		return startChar;
	}


	public void setStartChar(int startChar) {
		this.startChar = startChar;
	}


	public int getCurrentChar() {
		return currentChar;
	}


	public void setCurrentChar(int currentChar) {
		this.currentChar = currentChar;
	}


	public String getCurrentStates() {
		return currentStates;
	}


	public void setCurrentStates(String currentStates) {
		this.currentStates = currentStates;
	}
}
