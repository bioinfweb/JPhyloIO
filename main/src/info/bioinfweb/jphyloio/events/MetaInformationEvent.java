/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
 * <http://bioinfweb.info/PhyloIO>
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
package info.bioinfweb.jphyloio.events;



/**
 * Indicates that meta data has been encountered at the current position of the document.  
 * 
 * @author Ben St&ouml;ver
 */
public class MetaInformationEvent extends ConcreteJPhyloIOEvent {
	private String name;
	private String value;
	
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param name the name or key of the meta information 
	 * @param value the value of the meta information
	 */
	public MetaInformationEvent(String name, String value) {
		super(EventType.META_INFORMATION);
		
		this.name = name;
		if (value == null) {
			throw new NullPointerException("The parameter \"value\" cannot be null.");
		}
		else {
			this.value = value;
		}
	}


	/**
	 * Returns the name or key of the meta information, if provided in the document. 
	 * 
	 * @return the name of {@code null} if no key or name is provided in the document
	 */
	public String getName() {
		return name;
	}


	/**
	 * Returns the value of the meta information.
	 * 
	 * @return the string representation of the meta information (Will never be {@code null}.)
	 */
	public String getValue() {
		return value;
	}
}
