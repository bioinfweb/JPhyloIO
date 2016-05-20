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



public class URIOrStringIdentifier {
	private String stringRepresentation;
	private QName uri;
	
	
	public URIOrStringIdentifier(String alternativeStringRepresentation, QName predicate) {
		super();
		this.stringRepresentation = alternativeStringRepresentation;		
		this.uri = predicate;		
	}


	public String getStringRepresentation() {
		return stringRepresentation;
	}


	public QName getURI() {
		return uri;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((stringRepresentation == null) ? 0 : stringRepresentation.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		URIOrStringIdentifier other = (URIOrStringIdentifier) obj;
		if (stringRepresentation == null) {
			if (other.stringRepresentation != null)
				return false;
		} else if (!stringRepresentation.equals(other.stringRepresentation))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
}
