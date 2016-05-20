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
package info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter;


import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.MetadataAdapter;
import info.bioinfweb.jphyloio.events.LabeledIDEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;



public class StoreMetadataAdapter implements MetadataAdapter{
	private Map<String, Map<String, String>> metaStructureMap = new HashMap<String, Map<String,String>>();
	private List<LabeledIDEvent> metaEvents;
	
	
	public StoreMetadataAdapter(Map<String, Map<String, String>> metaStructureMap, List<LabeledIDEvent> metaEvents) {
		super();
		this.metaStructureMap = metaStructureMap;
		this.metaEvents = metaEvents;
	}
	

	@Override
	public Iterator<String> getRootElementIDs() {
		return metaStructureMap.keySet().iterator();
	}
	

	@Override
	public Iterator<String> getChildElementIDs(String parentID) {
		return metaStructureMap.get(parentID).keySet().iterator();
	}
	

	@Override
	public Iterator<String> getIDsByPredicate(QName predicate) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public Iterator<URIOrStringIdentifier> getAlternativeIdentifiers(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public LabeledIDEvent getMetadataStartEvent(String id) {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public void writeLiteralContent(JPhyloIOEventReceiver receiver, String id) throws IOException {
		// TODO Auto-generated method stub
	}	
}
