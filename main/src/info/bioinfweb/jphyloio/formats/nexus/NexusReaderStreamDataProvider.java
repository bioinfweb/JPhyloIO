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
package info.bioinfweb.jphyloio.formats.nexus;


import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandEventReader;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.all.BlockTitleToIDMap;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.trees.NexusTranslationTable;
import info.bioinfweb.jphyloio.formats.text.KeyValueInformation;
import info.bioinfweb.jphyloio.formats.text.TextReaderStreamDataProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;



/**
 * Stores data that shall be shared among different implementations of {@link NexusCommandEventReader}
 * reading from the same document.
 * 
 * @author Ben St&ouml;ver
 */
public class NexusReaderStreamDataProvider extends TextReaderStreamDataProvider<NexusEventReader> implements NexusConstants {
	public static final String INFO_KEY_BLOCK_START_EVENT_FIRED = "info.bioinfweb.jphyloio.nexus.blockStartEventFired";
	
	/** Used to store the ID of the according <i>JPhyloIO</i> event to the current Nexus block (if one exists). */ 
	public static final String INFO_KEY_CURRENT_BLOCK_ID = "info.bioinfweb.jphyloio.nexus.currentBlockID";
	
	/** Used to store the title of the current block if specified by a TITLE command. */
	public static final String INFO_KEY_BLOCK_TITLE = "info.bioinfweb.jphyloio.nexus.blockTitle";
	
	public static final String INFO_KEY_BLOCK_LINKS = "info.bioinfweb.jphyloio.nexus.blockLinks";
	public static final String INFO_KEY_BLOCK_ID_MAP = "info.bioinfweb.jphyloio.nexus.taxa.blockTitleToIDMap";
	public static final String INFO_KEY_TAXA_LIST = "info.bioinfweb.jphyloio.nexus.taxa.list";
	public static final String INFO_KEY_TAXA_MAP = "info.bioinfweb.jphyloio.nexus.taxa.taxaIDMap";
	public static final String INFO_KEY_TREES_TRANSLATION = "info.bioinfweb.jphyloio.nexus.trees.translate";
	
	
	private ParameterMap sharedInformationMap = new ParameterMap();
	
	
	public NexusReaderStreamDataProvider(NexusEventReader nexusReader) {
		super(nexusReader);
	}


	@Override
	public NexusEventReader getEventReader() {
		return (NexusEventReader)super.getEventReader();
	}


	public void consumeWhiteSpaceAndComments() throws IOException {
		getEventReader().consumeWhiteSpaceAndComments();
	}
	
	
	public String readNexusWord() throws IOException {
		return getEventReader().readNexusWord();
	}
	
	
	public void readComment() throws IOException {
		getEventReader().readComment();
	}
	
	
	public KeyValueInformation readKeyValueMetaInformation() throws IOException {
		return getEventReader().readKeyValueMetaInformation();
	}
	
	
	/**
	 * This map can be used to store objects to be shared between different instances of 
	 * {@link NexusCommandEventReader}.
	 * 
	 * @return a map providing access to shared data objects
	 */
	public ParameterMap getSharedInformationMap() {
		return sharedInformationMap;
	}
	
	
	/**
	 * Returns the map that contains links to other blocks that are explicitly linked to this block.
	 * <p>
	 * The returned map is used internally in <i>JPhyloIO</i> to manage links of the current block that are defined with a 
	 * {@code LINK} command. It should never be used to determine a currently linked block, but {@link #getCurrentLinkedBlockID(String)}
	 * should be used instead. 
	 * 
	 * @return the map containing the blocks explicitly linked to the current block
	 */
	public Map<String, String> getBlockLinks() {
		@SuppressWarnings("unchecked")
		Map<String, String> result = (Map<String, String>)getSharedInformationMap().get(INFO_KEY_BLOCK_LINKS);  // Casting null is possible.
		if (result == null) {
			result = new TreeMap<String, String>();
			getSharedInformationMap().put(INFO_KEY_BLOCK_LINKS, result);
		}
		return result;
	}
	
	
	/**
	 * Returns the ID if a {@code CHARACTERS}, {@code DATA} or {@code UNALIGNED} block that is linked to the current block.
	 * If none of these blocks is explicitly linked, returning a default linked block will be tried in this order. If no default
	 * linked block is found, {@code null} will be returned. 
	 * 
	 * @return a linked block containing a sequence matrix or {@code null}
	 */
	public String getMatrixLink() {
		Map<String, String> map = getBlockLinks();
		String result = map.get(BLOCK_NAME_CHARACTERS);
		if (result == null) {
			result = map.get(BLOCK_NAME_DATA);
			if (result == null) {
				result = map.get(BLOCK_NAME_UNALIGNED);  //TODO Can sets be defined for unaligned blocks?
				
				if (result == null) {
					result = getBlockTitleToIDMap().getDefaultBlockID(BLOCK_NAME_CHARACTERS);
					if (result == null) {
						result = getBlockTitleToIDMap().getDefaultBlockID(BLOCK_NAME_DATA);
						if (result == null) {
							result = getBlockTitleToIDMap().getDefaultBlockID(BLOCK_NAME_UNALIGNED);  //TODO Can sets be defined for unaligned blocks?
						}
					}
				}
			}
		}  //TODO Should an exception be thrown if more than one link is defined in a SETS block?
		return result;
	}
	
	
	public void clearBlockInformation() {
		getSharedInformationMap().remove(INFO_KEY_BLOCK_TITLE);
		getSharedInformationMap().remove(INFO_KEY_BLOCK_LINKS);
		getSharedInformationMap().remove(INFO_KEY_CURRENT_BLOCK_ID);
		getSharedInformationMap().remove(INFO_KEY_BLOCK_START_EVENT_FIRED);
	}
	
	
	/**
	 * Gets a map object from the shared information map. If no such object if found, a new {@link HashMap} instance is stored under
	 * the specified key and returned. 
	 * 
	 * @param key the key in the shared information map
	 * @return the map object contained in the shared information map
	 */
	private Map<String, String> getMap(String key) {
		@SuppressWarnings("unchecked")
		Map<String, String> result = (Map<String, String>)getSharedInformationMap().get(key);  // Casting null is possible.
		if (result == null) {
			result = new HashMap<String, String>();
			getSharedInformationMap().put(key, result);
		}
		return result;
	}
	
	
	/**
	 * Returns a map object that can translate Nexus block titles to the IDs of the according <i>JPhyloIO</i> start events.
	 * The returned object can also determine default block links.
	 * 
	 * @return the translation map
	 */
	public BlockTitleToIDMap getBlockTitleToIDMap() {
		BlockTitleToIDMap result = getSharedInformationMap().getObject(INFO_KEY_BLOCK_ID_MAP, null, BlockTitleToIDMap.class);
		if (result == null) {
			result = new BlockTitleToIDMap();
			getSharedInformationMap().put(INFO_KEY_BLOCK_ID_MAP, result);  // If an object of another type is stored under this key, getObject() would also return null and it will be overwritten here. 
		}
		return result;
	}
	
	
	/**
	 * Returns the <i>JPhyloIO</i> ID of the start event describing the Nexus block of the specified type that is linked to the current 
	 * block. It will first look for an explicitly linked block in {@link #getBlockLinks()}. If no link is found, the default link
	 * from {@link BlockTitleToIDMap#getDefaultBlockID(String)} will be returned.
	 * 
	 * @param blockTypeName the name of the Nexus block type that could be linked
	 * @return the start event ID or {@code null} if no according block could be found
	 */
	public String getCurrentLinkedBlockID(String blockTypeName) {
		String result = getBlockLinks().get(blockTypeName);
		if (result == null) {
			result = getBlockTitleToIDMap().getDefaultBlockID(blockTypeName);
		}
		return result;
	}
	
	
	public List<String> getTaxaList(String listID) {
		if (listID == null) {
			throw new NullPointerException("The specified listID must not be null.");
		}
		else {
			@SuppressWarnings("unchecked")
			List<String> result = (List<String>)getSharedInformationMap().get(INFO_KEY_TAXA_LIST + "." + listID);  // Casting null is possible.
			if (result == null) {
				result = new ArrayList<String>();
				getSharedInformationMap().put(INFO_KEY_TAXA_LIST + "." + listID, result);
			}
			return result;
		}
	}
	
	
	/**
	 * Returns a map object that translates between a Nexus taxon name and the <i>JPhyloIO</i> ID of the according OTU event.
	 * 
	 * @param listID the <i>JPhyloIO</i> ID of the according OTU list start event
	 * @return the map or {@code null} of no map is present for the specified OTU list ID
	 * @throws NullPointerException if {@code listID is null}
	 */
	public Map<String, String> getTaxaToIDMap(String listID) {
		if (listID == null) {
			throw new NullPointerException("The specified listID must not be null.");
		}
		else {
			return getMap(INFO_KEY_TAXA_MAP + "." + listID);
		}
	}
	
	
	/**
	 * Returns the current Nexus translation table object. If none is present, a new empty instance will be created and stored in
	 * the shared information map.
	 * 
	 * @return the current table
	 */
	public NexusTranslationTable getTreesTranslationTable() {
		NexusTranslationTable result = (NexusTranslationTable)getSharedInformationMap().get(INFO_KEY_TREES_TRANSLATION);  // Casting null is possible.
		if (result == null) {
			result = new NexusTranslationTable();
			getSharedInformationMap().put(INFO_KEY_TREES_TRANSLATION, result);
		}
		return result;
	}
}
