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
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.trees.NexusTranslationTable;
import info.bioinfweb.jphyloio.formats.text.AbstractTextEventReader.KeyValueInformation;
import info.bioinfweb.jphyloio.formats.text.TextStreamDataProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Stores data that shall be shared among different implementations of {@link NexusCommandEventReader}
 * reading from the same document.
 * 
 * @author Ben St&ouml;ver
 */
public class NexusStreamDataProvider extends TextStreamDataProvider<NexusEventReader> {
	public static final String INFO_KEY_TAXA_LIST = "info.bioinfweb.jphyloio.nexus.taxa.list";
	public static final String INFO_KEY_TAXA_MAP = "info.bioinfweb.jphyloio.nexus.taxa.taxaidmap";
	public static final String INFO_KEY_TREES_TRANSLATION = "info.bioinfweb.jphyloio.nexus.trees.translate";
	
	
	private ParameterMap sharedInformationMap = new ParameterMap();
	
	
	public NexusStreamDataProvider(NexusEventReader nexusReader) {
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
	
	
	public KeyValueInformation readKeyValueMetaInformation(String keyPrefix) throws IOException {
		return getEventReader().readKeyValueMetaInformation(keyPrefix);
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
	
	
	public List<String> getTaxaList() {
		@SuppressWarnings("unchecked")
		List<String> result = (List<String>)getSharedInformationMap().get(INFO_KEY_TAXA_LIST);  // Casting null is possible.
		if (result == null) {
			result = new ArrayList<String>();
			getSharedInformationMap().put(INFO_KEY_TAXA_LIST, result);
		}
		return result;
	}
	
	
	public Map<String, String> getTaxaToIDMap() {
		@SuppressWarnings("unchecked")
		Map<String, String> result = (Map<String, String>)getSharedInformationMap().get(INFO_KEY_TAXA_MAP);  // Casting null is possible.
		if (result == null) {
			result = new HashMap<String, String>();
			getSharedInformationMap().put(INFO_KEY_TAXA_MAP, result);
		}
		return result;
	}
	
	
	public NexusTranslationTable getTreesTranslationTable() {
		NexusTranslationTable result = (NexusTranslationTable)getSharedInformationMap().get(INFO_KEY_TREES_TRANSLATION);  // Casting null is possible.
		if (result == null) {
			result = new NexusTranslationTable();
			getSharedInformationMap().put(INFO_KEY_TREES_TRANSLATION, result);
		}
		return result;
	}
}
