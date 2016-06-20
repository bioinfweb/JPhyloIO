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
package info.bioinfweb.jphyloio.dataadapters.implementations.readtowriteadapter;


import info.bioinfweb.jphyloio.dataadapters.MetadataAdapter;
import info.bioinfweb.jphyloio.dataadapters.ObjectListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;



public class StoreTreeNetworkGroupDataAdapter extends StoreAnnotatedDataAdapter<LinkedLabeledIDEvent> implements TreeNetworkGroupDataAdapter {
	private LinkedLabeledIDEvent startEvent;
	private List<TreeNetworkDataAdapter> treesAndNetworks = new ArrayList<TreeNetworkDataAdapter>();
	private StoreObjectListDataAdapter<LinkedLabeledIDEvent> treeAndNetworkSets = new StoreObjectListDataAdapter<LinkedLabeledIDEvent>();
	
	
	public StoreTreeNetworkGroupDataAdapter(MetadataAdapter annotations, LinkedLabeledIDEvent treeOrNetworkGroupStartEvent) {
		super(annotations);
		this.startEvent = treeOrNetworkGroupStartEvent;
	}


	@Override
	public LinkedLabeledIDEvent getStartEvent() {
		return startEvent;
	}


	@Override
	public Iterator<TreeNetworkDataAdapter> getTreeNetworkIterator() {
		return treesAndNetworks.iterator();
	}


	public List<TreeNetworkDataAdapter> getTreesAndNetworks() {
		return treesAndNetworks;
	}


	@Override
	public ObjectListDataAdapter<LinkedLabeledIDEvent> getTreeSets() {
		return treeAndNetworkSets;
	}
}