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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.bioinfweb.jphyloio.dataadapters.JPhyloIOEventReceiver;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkGroupDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;



public class StoreTreeNetworkGroupDataAdapter extends StoreAnnotatedDataAdapter implements TreeNetworkGroupDataAdapter {
	private StoreLinkedDataAdapter treeGroupAdapter;
	private List<TreeNetworkDataAdapter> treesAndNetworks = new ArrayList<TreeNetworkDataAdapter>();
	
	
	public StoreTreeNetworkGroupDataAdapter(List<JPhyloIOEvent> annotations, LinkedLabeledIDEvent treeOrNetworkGroupStartEvent) {
		super(annotations);
		this.treeGroupAdapter = new StoreLinkedDataAdapter(treeOrNetworkGroupStartEvent);
	}


	@Override
	public void writeMetadata(JPhyloIOEventReceiver receiver) throws IOException {
		super.writeMetadata(receiver);
	}
	

	@Override
	public boolean hasMetadata() {
		return super.hasMetadata();
	}
	
	
	@Override
	public List<JPhyloIOEvent> getAnnotations() {
		return super.getAnnotations();
	}
	

	@Override
	public LinkedLabeledIDEvent getStartEvent() {
		return treeGroupAdapter.getStartEvent();
	}

	@Override
	public Iterator<TreeNetworkDataAdapter> getTreeNetworkIterator() {
		return treesAndNetworks.iterator();
	}


	public List<TreeNetworkDataAdapter> getTreesAndNetworks() {
		return treesAndNetworks;
	}
}
