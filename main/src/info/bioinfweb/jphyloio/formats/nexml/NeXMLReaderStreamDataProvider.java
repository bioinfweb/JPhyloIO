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


import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;

import javax.xml.namespace.QName;



public class NeXMLReaderStreamDataProvider extends XMLReaderStreamDataProvider<NeXMLEventReader> {	
	private boolean allowLongTokens;
	
	private Stack<EventContentType> metaType = new Stack<EventContentType>();
	private QName nestedMetaType;
	private String alternativeStringRepresentation;
	
	private Map<String, String> otuIDToLabelMap = new TreeMap<String, String>();
	
	private String currentTokenSetID = null;
	private Map<String, NeXMLTokenSetInformation> tokenSets = new TreeMap<String, NeXMLTokenSetInformation>();
	private Map<String, String> tokenDefinitionIDToSymbolMap = new HashMap<String, String>();
	private NeXMLSingleTokenDefinitionInformation currentSingleTokenDefinition;	

	private Map<String, List<String>> tokenSetIDtoColumnsMap = new HashMap<String, List<String>>();
	private List<String> charIDs = new ArrayList<String>();
	private Map<String, Integer> charIDToIndexMap = new HashMap<String, Integer>();
	private Map<String, String> charIDToStatesMap = new HashMap<String, String>();
	
	private String branchLengthsFormat = null;
	private boolean isTrulyRooted = false;
	private Set<String> rootNodeIDs;
	
	
	public NeXMLReaderStreamDataProvider(NeXMLEventReader nexmlEventReader) {
		super(nexmlEventReader);
	}


	@Override
	public NeXMLEventReader getEventReader() {
		return (NeXMLEventReader)super.getEventReader();
	}
	
	
	public boolean isAllowLongTokens() {
		return allowLongTokens;
	}


	public void setAllowLongTokens(boolean allowLongTokens) {
		this.allowLongTokens = allowLongTokens;
	}


	public Stack<EventContentType> getMetaType() {
		return metaType;
	}


	public QName getNestedMetaType() {
		return nestedMetaType;
	}


	public void setNestedMetaType(QName nestedMetaType) {
		this.nestedMetaType = nestedMetaType;
	}


	public String getAlternativeStringRepresentation() {
		return alternativeStringRepresentation;
	}


	public void setAlternativeStringRepresentation(String alternativeStringRepresentation) {
		this.alternativeStringRepresentation = alternativeStringRepresentation;
	}
	
	
	public Map<String, String> getOtuIDToLabelMap() {
		return otuIDToLabelMap;
	}

	
	public String getCurrentTokenSetID() {
		return currentTokenSetID;
	}


	public void setCurrentTokenSetID(String tokenSetID) {
		this.currentTokenSetID = tokenSetID;
	}
	

	public Map<String, NeXMLTokenSetInformation> getTokenSets() {
		return tokenSets;
	}	


	public Map<String, String> getTokenDefinitionIDToSymbolMap() {
		return tokenDefinitionIDToSymbolMap;
	}


	public NeXMLSingleTokenDefinitionInformation getCurrentSingleTokenDefinition() {
		return currentSingleTokenDefinition;
	}


	public void setCurrentSingleTokenDefinition(NeXMLSingleTokenDefinitionInformation currentSingleTokenDefinition) {
		this.currentSingleTokenDefinition = currentSingleTokenDefinition;
	}
	
	
	public Map<String, List<String>> getTokenSetIDtoColumnsMap() {
		return tokenSetIDtoColumnsMap;
	}	


	public List<String> getCharIDs() {
		return charIDs;
	}

	
	protected Map<String, Integer> getCharIDToIndexMap() {
		return charIDToIndexMap;
	}


	public Map<String, String> getCharIDToStatesMap() {
		return charIDToStatesMap;
	}	


	public String getBranchLengthsFormat() {
		return branchLengthsFormat;
	}


	public void setBranchLengthsFormat(String branchLengthsFormat) {
		this.branchLengthsFormat = branchLengthsFormat;
	}


	protected Set<String> getRootNodeIDs() {
		return rootNodeIDs;
	}


	protected boolean isTrulyRooted() {
		return isTrulyRooted;
	}


	protected void setTrulyRooted(boolean isTrulyRooted) {
		this.isTrulyRooted = isTrulyRooted;
	}


	protected void setRootNodeIDs(Set<String> rootNodeIDs) {
		this.rootNodeIDs = rootNodeIDs;
	}
}
