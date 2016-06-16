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
package info.bioinfweb.jphyloio.formats.nexml;


import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;



public class NonStoringCollection<E> implements Collection<E> {

	
	/**
	 * Always returns {@code true}. The object will not be stored.
	 * 
	 * @return {@code true}
	 */
	@Override
	public boolean add(Object e) {
		return true;
	}

	
	/**
	 * Always returns {@code true}. The object in the collection will not be stored.
	 * 
	 * @return {@code true}
	 */
	@Override
	public boolean addAll(Collection c) {
		return true;
	}
	

	@Override
	public void clear() {}
	

	/**
	 * Since no objects are stored in this collection this method will always return {@code false}.
	 * 
	 * @return always {@code false}
	 */
	@Override
	public boolean contains(Object o) {		
		return false;
	}
	

	/**
	 * Since no objects are stored in this collection this method will always return {@code false}.
	 * 
	 * @return always {@code false}
	 */
	@Override
	public boolean containsAll(Collection c) {
		return false;
	}
	

	/**
	 * Since no objects are stored in this collection this method will always return {@code true}.
	 * 
	 * @return always {@code true}
	 */
	@Override
	public boolean isEmpty() {
		return true;
	}

	
	/**
	 * Since no objects are stored in this collection this method will always return an empty iterator.
	 * 
	 * @return an empty iterator
	 */
	@Override
	public Iterator iterator() {
		return Collections.emptyIterator();
	}
	

	/**
	 * Since no objects are stored in this collection, no objects can be removed. 
	 * Therefore this method will always return {@code false}.
	 * 
	 * @return always {@code false}
	 */
	@Override
	public boolean remove(Object o) {
		return false;
	}

	
	/**
	 * Since no objects are stored in this collection, no objects can be removed. 
	 * Therefore this method will always return {@code false}.
	 * 
	 * @return always {@code false}
	 */
	@Override
	public boolean removeAll(Collection c) {
		return false;
	}

	
	/**
	 * Since no objects are stored in this collection, no objects can be removed. 
	 * Therefore this method will always return {@code false}.
	 * 
	 * @return always {@code false}
	 */
	@Override
	public boolean retainAll(Collection c) {
		return false;
	}


	/**
	 * Since no objects are stored in this collection its size will always be 0.
	 * 
	 * @return 0
	 */
	@Override
	public int size() {
		return 0;
	}
	

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException("Since this list does not store any contents no array could be created.");
	}
	

	@Override
	public Object[] toArray(Object[] a) {
		throw new UnsupportedOperationException("Since this list does not store any contents no array could be created.");
	}	
}
