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
package info.bioinfweb.jphyloio.events.meta;


import javax.xml.namespace.QName;

import org.semanticweb.owlapi.io.XMLUtils;



public class UriOrStringIdentifier {  //TODO Rename to allow use as datatype
	private String stringRepresentation;
	private QName predicate;
	
	
	public UriOrStringIdentifier(String alternativeStringRepresentation, QName predicate) {
		super();
		this.stringRepresentation = alternativeStringRepresentation;		
		
		if ((predicate.getPrefix() == null || XMLUtils.isNCName(predicate.getPrefix())) //TODO is the prefix allowed to be an empty string?
				&& (predicate.getLocalPart() != null && XMLUtils.isNCName(predicate.getLocalPart()))) {
			this.predicate = predicate;
		}
		else { //predicate is not a valid QName
			//TODO throw exception?
		}
	}


	public String getStringRepresentation() {
		return stringRepresentation;
	}


	public QName getURI() {
		return predicate;
	}
}
