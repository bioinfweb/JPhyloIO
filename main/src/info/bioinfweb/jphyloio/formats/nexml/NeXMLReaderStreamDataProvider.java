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


import info.bioinfweb.jphyloio.events.SingleSequenceTokenEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.formats.BufferedEventInfo;
import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	
	private Map<String, BufferedEventInfo<SingleSequenceTokenEvent>> currentCellsBuffer = new HashMap<String, BufferedEventInfo<SingleSequenceTokenEvent>>();
	private Iterator<String> currentCharIDIterator = null;
	private String currentExpectedCharID = null;
	private boolean currentCellBuffered = false;
	
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


	/**
	 * Returns a stack of {@link EventContentType} that represents the currently encountered nested meta events. 
	 * 
	 * @return the stack of EventContentType representing the encountered meta events
	 */
	public Stack<EventContentType> getMetaType() {
		return metaType;
	}


	/**
	 * Returns the datatype of character content nested under the current literal meta element. 
	 * 
	 * @return the datatype of character content nested under the current literal meta element
	 */
	public QName getNestedMetaType() {
		return nestedMetaType;
	}


	public void setNestedMetaType(QName nestedMetaType) {
		this.nestedMetaType = nestedMetaType;
	}


	/**
	 * Returns the value of the content attribute of the current literal meta element to be used as an alternative 
	 * string representation by a following {@link LiteralMetadataContentEvent}.
	 * 
	 * @return the value of the content attribute of the current literal meta element
	 */
	public String getAlternativeStringRepresentation() {
		return alternativeStringRepresentation;
	}


	public void setAlternativeStringRepresentation(String alternativeStringRepresentation) {
		this.alternativeStringRepresentation = alternativeStringRepresentation;
	}
	
	
	/**
	 * Returns a map that links a label to a certain OTU ID. This map is used to determine a sequence label 
	 * from the OTU linked to it, in case no sequence label could be found.
	 * 
	 * @return the map linking a label to an OTU ID
	 */
	public Map<String, String> getOtuIDToLabelMap() {
		return otuIDToLabelMap;
	}


	/**
	 * Returns the ID of the states element (representing a token set definition) that is currently read.
	 * 
	 * @return the current token set ID
	 */
	public String getCurrentTokenSetID() {
		return currentTokenSetID;
	}


	public void setCurrentTokenSetID(String tokenSetID) {
		this.currentTokenSetID = tokenSetID;
	}
	
	/**
	 * Returns a map that links a {@link NeXMLTokenSetInformation} to a certain token set ID. 
	 * This is used to buffer information about the token set until the start event is created and to be able 
	 * to find the right translation map when reading sequences.
	 * 
	 * @return the map linking a {@link NeXMLTokenSetInformation} to an token set ID
	 */
	public Map<String, NeXMLTokenSetInformation> getTokenSets() {
		return tokenSets;
	}	


	/**
	 * Returns a map that links a symbol (i.e. the name of a token definition like 'A' for Adenin) to a single token definition ID.
	 * 
	 * @return the map linking a symbol to a single token definition ID
	 */
	public Map<String, String> getTokenDefinitionIDToSymbolMap() {
		return tokenDefinitionIDToSymbolMap;
	}


	/**
	 * Returns the {@link NeXMLSingleTokenDefinitionInformation} instance of the currently read single token definition, 
	 * e.g to add more constituents.
	 * 
	 * @return the {@link NeXMLSingleTokenDefinitionInformation} instance of the currently read single token definition
	 */
	public NeXMLSingleTokenDefinitionInformation getCurrentSingleTokenDefinition() {
		return currentSingleTokenDefinition;
	}


	public void setCurrentSingleTokenDefinition(NeXMLSingleTokenDefinitionInformation currentSingleTokenDefinition) {
		this.currentSingleTokenDefinition = currentSingleTokenDefinition;
	}
	
	
	/**
	 * Returns a map that links a symbol (i.e. the name of a token definition like 'A' for Adenin) to a single token definition ID.
	 * 
	 * @return the map linking a symbol to a single token definition ID
	 */
	public Map<String, List<String>> getTokenSetIDtoColumnsMap() {
		return tokenSetIDtoColumnsMap;
	}	


	/**
	 * Returns a list of column IDs obtained from NeXML char elements.
	 * 
	 * @return a list of column IDs
	 */
	public List<String> getCharIDs() {
		return charIDs;
	}

	
	/**
	 * Returns a map linking column indices to column IDs obtained from NeXML char elements.
	 * 
	 * @return a map linking column indices to column IDs
	 */
	protected Map<String, Integer> getCharIDToIndexMap() {
		return charIDToIndexMap;
	}


	/**
	 * Returns a map linking a token set definition ID to a column ID obtained from a NeXML char elements.
	 * 
	 * @return a map linking a token set definition ID to a column ID
	 */
	public Map<String, String> getCharIDToStatesMap() {
		return charIDToStatesMap;
	}	


	/**
	 * The returned map is used to buffer {@link SingleTokenDefinitionEvent}s and their nested events, in cases where the order of
	 * {@code cell} tags does not match the order of the referenced columns.
	 * 
	 * @return the map instance to be used for buffering
	 */
	public Map<String, BufferedEventInfo<SingleSequenceTokenEvent>> getCurrentCellsBuffer() {
		return currentCellsBuffer;
	}
	
	
	/**
	 * Method used for reading cell tags that clears the map of buffered cell informations ({@link #getCurrentCellsBuffer()}) and resets
	 * the columns ID iterator used by {@link #nextCharID()}.
	 */
	public void clearCurrentRowInformation() {
		currentCharIDIterator = null;
		currentExpectedCharID = null;
		currentCellsBuffer.clear();
	}
	
	
	/**
	 * Returns the next NeXML columns ID (ID of a {@code char} tag) at the current position of the underlying iterator and stores it
	 * in the property {@link #getCurrentExpectedCharID()}.
	 * <p>
	 * If this method is called the first time after creation of this object or after a call of {@link #clearCurrentRowInformation()}
	 * a new iterator will be created before returning an event.
	 * 
	 * @return the ID of the next {@code char} tag or {@code null} if no additional columns are available
	 */
	public String nextCharID() {
		if (currentCharIDIterator == null) {
			currentCharIDIterator = getCharIDs().iterator();
		}
		if (currentCharIDIterator.hasNext()) {
			currentExpectedCharID = currentCharIDIterator.next();
		}
		else {
			currentExpectedCharID = null;
		}
		return currentExpectedCharID;
	}


	/**
	 * Returns the column ID that has been the result of the last call of {@link #nextCharID()}.
	 * 
	 * @return the currently expected column ID or {@code null} if no more IDs are to come
	 */
	public String getCurrentExpectedCharID() {
		if (currentExpectedCharID == null) {
			nextCharID();  // Set ID if iterator was not yet created. If the end of the list was reached, the value will remain null.
		}
		return currentExpectedCharID;
	}


	/**
	 * Determines whether the contents of the current {@code cell} tag are buffered or not. Buffering becomes necessary, if the order
	 * of {@code cell} tags differs from the order of column definitions from {@code char} tags.
	 * 
	 * @return {@code true} if the current cell is buffered or {@code false} otherwise
	 */
	public boolean isCurrentCellBuffered() {
		return currentCellBuffered;
	}


	/**
	 * Allow to specify whether the contents of the current {@code cell} tag are buffered or not.
	 * 
	 * @return Specify {@code true} if the current cell is buffered or {@code false} otherwise.
	 */
	public void setCurrentCellBuffered(boolean currentCellBuffered) {
		this.currentCellBuffered = currentCellBuffered;
	}


	/**
	 * Returns {@code true} if at least one node was encountered that specified {@code true} as the value of {@link NeXMLConstants.ATTR_ROOT}.
	 * 
	 * @return {@code true} if at least one root node was encountered
	 */
	protected boolean isTrulyRooted() {
		return isTrulyRooted;
	}


	protected void setTrulyRooted(boolean isTrulyRooted) {
		this.isTrulyRooted = isTrulyRooted;
	}


	/**
	 * Returns a list of all encountered nodes that specified {@code true} as the value of {@link NeXMLConstants.ATTR_ROOT}.
	 * 
	 * @return a list of all encountered root nodes
	 */
	protected Set<String> getRootNodeIDs() {
		return rootNodeIDs;
	}
	

	protected void setRootNodeIDs(Set<String> rootNodeIDs) {
		this.rootNodeIDs = rootNodeIDs;
	}
}
