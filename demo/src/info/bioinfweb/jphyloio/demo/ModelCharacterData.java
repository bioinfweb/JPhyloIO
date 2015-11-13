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
