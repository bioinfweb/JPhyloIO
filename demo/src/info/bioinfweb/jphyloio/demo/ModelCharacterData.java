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
package info.bioinfweb.jphyloio.demo;


import java.util.Collection;
import java.util.Map;

import info.bioinfweb.jphyloio.model.CharacterData;
import info.bioinfweb.jphyloio.model.ElementCollection;



public class ModelCharacterData implements CharacterData {
	private Map<String, ? extends Collection<String>> map;
	private ElementCollection<String> sequenceNames;
	
	
	public ModelCharacterData(Map<String, ? extends Collection<String>> map) {
		super();
		this.sequenceNames = new CollectionToElementCollectionAdapter<String>(map.keySet());
		this.map = map;
	}


	@Override
	public ElementCollection<String> getSequenceNames() {
		return sequenceNames;
	}
	

	@Override
	public ElementCollection<String> getTokens(String sequenceName) {
		return new CollectionToElementCollectionAdapter<String>(map.get(sequenceName));
	}	
}
