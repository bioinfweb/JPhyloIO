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


import info.bioinfweb.commons.bio.CharacterStateType;
import info.bioinfweb.jphyloio.formats.xml.XMLStreamDataProvider;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



public class NeXMLStreamDataProvider extends XMLStreamDataProvider {
	private Map<String, String> otuIDToLabelMap = new TreeMap<String, String>();	
	
	private Map<String, NeXMLTokenSetInformation> tokenSets = new TreeMap<String, NeXMLTokenSetInformation>();	
	
	private String tokenSetID = null;
	private CharacterStateType characterSetType;
	private boolean allowLongTokens;
	
	private String symbol = null;
	private Map<String, String> tokenDefinitionIDToSymbolMap;
	private Collection<String> constituents;
	
	private List<String> charIDs;
	private Map<String, String> charIDToStatesMap;
	
	private Map<String, String[]> directCharSets;
	
	private String lastToken = null;
	
	private String branchLengthsFormat = null;	
	
	
	public NeXMLStreamDataProvider(NeXMLEventReader nexmlEventReader) {
		super(nexmlEventReader);
	}


	@Override
	public NeXMLEventReader getEventReader() {
		return (NeXMLEventReader)super.getEventReader();
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


	public String getTokenSetID() {
		return tokenSetID;
	}


	public void setTokenSetID(String tokenSetID) {
		this.tokenSetID = tokenSetID;
	}


	public CharacterStateType getCharacterSetType() {
		return characterSetType;
	}


	public void setCharacterSetType(CharacterStateType characterSetType) {
		this.characterSetType = characterSetType;
	}


	public boolean isAllowLongTokens() {
		return allowLongTokens;
	}


	public void setAllowLongTokens(boolean allowLongTokens) {
		this.allowLongTokens = allowLongTokens;
	}


	public String getSymbol() {
		return symbol;
	}


	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}


	public Map<String, String> getTokenDefinitionIDToSymbolMap() {
		return tokenDefinitionIDToSymbolMap;
	}


	public void setTokenDefinitionIDToSymbolMap(
			Map<String, String> tokenDefinitionIDToSymbolMap) {
		this.tokenDefinitionIDToSymbolMap = tokenDefinitionIDToSymbolMap;
	}


	public Collection<String> getConstituents() {
		return constituents;
	}


	public void setConstituents(Collection<String> constituents) {
		this.constituents = constituents;
	}


	public List<String> getCharIDs() {
		return charIDs;
	}


	public void setCharIDs(List<String> charIDs) {
		this.charIDs = charIDs;
	}


	public Map<String, String> getCharIDToStatesMap() {
		return charIDToStatesMap;
	}


	public void setCharIDToStatesMap(Map<String, String> charIDToStatesMap) {
		this.charIDToStatesMap = charIDToStatesMap;
	}


	public Map<String, String[]> getDirectCharSets() {
		return directCharSets;
	}


	public void setDirectCharSets(Map<String, String[]> directCharSets) {
		this.directCharSets = directCharSets;
	}


	public String getLastToken() {
		return lastToken;
	}


	public void setLastToken(String lastToken) {
		this.lastToken = lastToken;
	}


	public String getBranchLengthsFormat() {
		return branchLengthsFormat;
	}


	public void setBranchLengthsFormat(String branchLengthsFormat) {
		this.branchLengthsFormat = branchLengthsFormat;
	}
}
