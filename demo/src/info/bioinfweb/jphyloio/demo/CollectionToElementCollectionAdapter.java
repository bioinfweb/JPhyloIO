package info.bioinfweb.jphyloio.demo;


import java.util.Collection;
import java.util.Iterator;

import info.bioinfweb.jphyloio.model.implementations.AbstractElementCollection;



public class CollectionToElementCollectionAdapter<E> extends AbstractElementCollection<E> {
	private Collection<E> dataElements;
	
	
	public CollectionToElementCollectionAdapter(Collection<E> dataElements) {
		super();
		this.dataElements = dataElements;
	}


	@Override
	public Iterator<E> iterator() {
		return dataElements.iterator();
	}
	

	@Override
	public long size() {
		return dataElements.size();
	}
}
