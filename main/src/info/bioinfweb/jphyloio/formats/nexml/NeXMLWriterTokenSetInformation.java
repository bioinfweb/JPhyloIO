/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class NeXMLWriterTokenSetInformation {
	private boolean isNucleotideType = false;
	private Set<String> singleTokenDefinitions = new HashSet<String>();
	private Map<String, String> tokenTranslationMap = new HashMap<String, String>();


	public boolean isNucleotideType() {
		return isNucleotideType;
	}

	
	public void setNucleotideType(boolean isNucleotideType) {
		this.isNucleotideType = isNucleotideType;
	}


	public Set<String> getSingleTokenDefinitions() {
		return singleTokenDefinitions;
	}


	public Map<String, String> getTokenTranslationMap() {
		return tokenTranslationMap;
	}
}