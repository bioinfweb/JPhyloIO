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


import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.map.ListOrderedMap;



public class StoreDocumentDataAdapter extends StoreAnnotatedDataAdapter implements DocumentDataAdapter {
	private ListOrderedMap<String, OTUListDataAdapter> otuListsMap;
	private List<MatrixDataAdapter> matrices;
	private List<TreeNetworkDataAdapter> treesNetworks;
	
	
	public StoreDocumentDataAdapter(ListOrderedMap<String, OTUListDataAdapter> otusMap, List<MatrixDataAdapter> matrices,
			List<TreeNetworkDataAdapter> treesNetworks, List<JPhyloIOEvent> annotations) {		
		super(annotations);
		
		if (otusMap == null) {
			this.otuListsMap = new ListOrderedMap<String, OTUListDataAdapter>();
		}
		else {
			this.otuListsMap = otusMap;
		}
		
		if (matrices == null) {
			this.matrices = new ArrayList<MatrixDataAdapter>();
		}
		else {
			this.matrices = matrices;
		}
		
		if (treesNetworks == null) {
			this.treesNetworks = new ArrayList<TreeNetworkDataAdapter>();
		}
		else {
			this.treesNetworks = treesNetworks;
		}
	}
	
	
	/**
	 * Creates a new instance of this class with empty array lists for all properties.
	 * <p>
	 * Using this constructor is equivalent to calling {@link #StoreDocumentDataAdapter(List, List, List)}
	 * with only {@code null} arguments.
	 */
	public StoreDocumentDataAdapter() {
		this(null, null, null, null);
	}
	
	
	public ListOrderedMap<String, OTUListDataAdapter> getOTUListsMap() {
		return otuListsMap;
	}


	public List<MatrixDataAdapter> getMatrices() {
		return matrices;
	}


	public List<TreeNetworkDataAdapter> getTreesNetworks() {
		return treesNetworks;
	}


	@Override
	public Iterator<OTUListDataAdapter> getOTUListIterator() {
		return otuListsMap.valueList().iterator();
	}

	
	@Override
	public long getOTUListCount() {
		return otuListsMap.size();
	}


	@Override
	public OTUListDataAdapter getOTUList(String id)	throws IllegalArgumentException {
		return otuListsMap.get(id);
	}


	@Override
	public Iterator<MatrixDataAdapter> getMatrixIterator() {
		return matrices.iterator();
	}

	
	@Override
	public Iterator<TreeNetworkDataAdapter> getTreeNetworkIterator() {
		return treesNetworks.iterator();
	}
}