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


import javax.xml.namespace.QName;



public class XMLElementType {
	private QName parentTag;
	private QName tagName;
	private int xmlEventType;
	
	
	public XMLElementType(QName parentTag, QName tagName, int xmlEventType) {
		super();
		this.parentTag = parentTag; //can be null in case of root element
		this.tagName = tagName; //can be null in case of character or comment event
		this.xmlEventType = xmlEventType;
	}


	public QName getParentTag() {
		return parentTag;
	}


	public void setParentTag(QName parentTag) {
		this.parentTag = parentTag;
	}


	public QName getTagname() {
		return tagName;
	}


	public void setTagname(QName tagname) {
		this.tagName = tagname;
	}


	public int getXmlEventType() {
		return xmlEventType;
	}


	public void setXmlEventType(int xmlEventType) {
		this.xmlEventType = xmlEventType;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parentTag == null) ? 0 : parentTag.hashCode());
		result = prime * result + ((tagName == null) ? 0 : tagName.hashCode());
		result = prime * result + xmlEventType;
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
		XMLElementType other = (XMLElementType) obj;
		if (parentTag == null) {
			if (other.parentTag != null)
				return false;
		} else if (!parentTag.equals(other.parentTag))
			return false;
		if (tagName == null) {
			if (other.tagName != null)
				return false;
		} else if (!tagName.equals(other.tagName))
			return false;
		if (xmlEventType != other.xmlEventType)
			return false;
		return true;
	}


	@Override
	public String toString() {
		return getParentTag().getLocalPart() + "." + getXmlEventType();
	}
}
