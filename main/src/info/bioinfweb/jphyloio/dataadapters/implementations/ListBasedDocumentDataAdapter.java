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
package info.bioinfweb.jphyloio.dataadapters.implementations;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.dataadapters.TreeNetworkDataAdapter;



/**
 * Default implementation of {@link DocumentDataAdapter}. Instances of this class contain a list
 * for each property (OTU lists, matrices and trees/networks), which can be filled by the application
 * with their according adapter instances.
 * <p>
 * If documents with many elements (e.g. many trees) shall be written, application developers may choose to
 * overwrite the according iterator property and return an iterator that directly accesses their application
 * business model for performance reasons. (Otherwise a large list would have to be stored in this instance,
 * which probably is a copy of an according list in the application business model.)
 * 
 * @author Ben St&ouml;ver
 */
public class ListBasedDocumentDataAdapter extends EmptyAnnotatedDataAdapter implements DocumentDataAdapter {
	private List<OTUListDataAdapter> otuLists;
	private List<MatrixDataAdapter> matrices;
	private List<TreeNetworkDataAdapter> treesNetworks;
	
	
	/**
	 * Creates a new instance of this class using the specified lists. If {@code null} is specified for a
	 * list an empty array list will be created internally for this property.
	 * 
	 * @param otuLists the list of OTU lists to be used in this instance (or {@code null} to create a new empty list)
	 * @param matrices the list of matrices to be used in this instance (or {@code null} to create a new empty list)
	 * @param treesNetworks the list of trees or networks to be used in this instance (or {@code null} to create a 
	 *        new empty list)
	 */
	public ListBasedDocumentDataAdapter(List<OTUListDataAdapter> otuLists, List<MatrixDataAdapter> matrices,
			List<TreeNetworkDataAdapter> treesNetworks) {
		
		super();
		
		if (otuLists == null) {
			this.otuLists = new ArrayList<OTUListDataAdapter>();
		}
		else {
			this.otuLists = otuLists;
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
	 * Using this constructor is equivalent to calling {@link #ListBasedDocumentDataAdapter(List, List, List)}
	 * with only {@code null} arguments.
	 */
	public ListBasedDocumentDataAdapter() {
		this(null, null, null);
	}
	
	
	public List<OTUListDataAdapter> getOtuLists() {
		return otuLists;
	}


	public List<MatrixDataAdapter> getMatrices() {
		return matrices;
	}


	public List<TreeNetworkDataAdapter> getTreesNetworks() {
		return treesNetworks;
	}


	@Override
	public Iterator<OTUListDataAdapter> getOTUListIterator() {
		return otuLists.iterator();
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
