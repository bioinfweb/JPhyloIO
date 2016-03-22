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
package info.bioinfweb.jphyloio.dataadapters;


import info.bioinfweb.jphyloio.JPhyloIOEventWriter;

import java.util.Iterator;



/**
 * The root adapter interface to be used with {@link JPhyloIOEventWriter}s. Implementations model a document 
 * containing phylogenetic data, consisting of taxon lists, character matrices and trees or networks.
 * <p>
 * Application developers need to implement this and one or more of the references interfaces with an adapter
 * class, that allows {@link JPhyloIOEventWriter}s to access the business model of their application. 
 * (Alternatively the business model classes of the application may also implement these interfaces directly,
 * if suitable.) <i>JPhyloIO</i> offers a set of default implementations that could be used as superclasses of
 * application adapter implementations, which may reduce the workload in application development.
 * 
 * @author Ben St&ouml;ver
 * @see JPhyloIOEventWriter
 * @see OTUListDataAdapter
 * @see MatrixDataAdapter
 * @see TreeNetworkDataAdapter
 */
public interface DocumentDataAdapter extends AnnotatedDataAdapter {
	/**
	 * Returns an iterator providing access to all OTU lists contained in the document
	 * to be written. 
	 * 
	 * @return the iterator (May be empty but not {@code null}.)
	 */
	public Iterator<OTUListDataAdapter> getOTUListIterator();
	
	/**
	 * Returns the number of OTU lists provided by this document adapter.
	 * 
	 * @return the number of OTU lists, that will be returned by {@link #getOTUListIterator()}
	 */
	public long getOTUListCount();
	
	/**
	 * Returns the OTU list referenced by the specified ID.
	 * 
	 * @param id the ID of the OTU list to be returned
	 * @return the OTU list referenced by the specified ID
	 * @throws IllegalArgumentException if no OTU list with the specified ID is available
	 */
	public OTUListDataAdapter getOTUList(String id) throws IllegalArgumentException;

	/**
	 * Returns an iterator providing access to all matrices contained in the document
	 * to be written. 
	 * 
	 * @return the iterator (Maybe empty but not {@code null}.)
	 */
	public Iterator<MatrixDataAdapter> getMatrixIterator();

	/**
	 * Returns an iterator providing access to all trees and networks contained in the 
	 * document to be written. 
	 * 
	 * @return the iterator (Maybe empty but not {@code null}.)
	 */
	public Iterator<TreeNetworkDataAdapter> getTreeNetworkIterator();
}
