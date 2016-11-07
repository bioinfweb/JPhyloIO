/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
 * <http://bioinfweb.info/JPhyloIO>
 * 
 * This file is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This file is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package info.bioinfweb.jphyloio.test.tests.jaxb;


import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
public class CustomObject {
  @XmlAttribute
	private String id;
  
	private String stringProperty;
	
	private double numericProperty;

	
	public String getID() {
		return id;
	}

	
	public void setID(String id) {
		this.id = id;
	}

	
	public String getStringProperty() {
		return stringProperty;
	}

	
	public void setStringProperty(String stringValue) {
		this.stringProperty = stringValue;
	}

	
	public double getNumericProperty() {
		return numericProperty;
	}
	

	public void setNumericProperty(double numericValue) {
		this.numericProperty = numericValue;
	}
}
