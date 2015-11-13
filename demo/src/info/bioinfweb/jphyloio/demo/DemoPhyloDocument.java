package info.bioinfweb.jphyloio.demo;


import java.util.Collection;
import java.util.Iterator;

import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.model.CharacterData;
import info.bioinfweb.jphyloio.model.ElementCollection;
import info.bioinfweb.jphyloio.model.PhyloDocument;



public class DemoPhyloDocument implements PhyloDocument {
	private ElementCollection<CharacterData> characterData;
	private Collection<JPhyloIOEvent> metaCommentEvents;
	
	
	public DemoPhyloDocument(ElementCollection<CharacterData> characterData, Collection<JPhyloIOEvent> metaCommentEvents) {
		super();
		this.characterData = characterData;
		this.metaCommentEvents = metaCommentEvents;
	}


	@Override
	public ElementCollection<CharacterData> getCharacterDataCollection() {
		return characterData;
	}
	

	@Override
	public long getMetaCommentEventCount() {
		return metaCommentEvents.size();
	}	
	

	@Override
	public Iterator<JPhyloIOEvent> getMetaCommentEventIterator() {
		return metaCommentEvents.iterator();
	}	
}
