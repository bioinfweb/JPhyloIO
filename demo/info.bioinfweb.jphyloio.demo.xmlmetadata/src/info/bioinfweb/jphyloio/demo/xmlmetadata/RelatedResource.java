/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.demo.xmlmetadata;


import java.net.URL;



/**
 * An example model class representing a resource related to a phylogenetic dataset. The data stored in instances of this class
 * is read and written from and to phylogenetic documents as <i>XML</i> metadata.
 * 
 * @author Ben St&ouml;ver
 */
public class RelatedResource {
	public static enum Type {
		JOURNAL_ARTICLE,
		WEBSITE,
		DATASET,
		OTHER;
	}
	
	
	private String title = "";
	private URL url = null;
	private Type type = null;
	
	
	public RelatedResource() {
		super();
	}
	
	
	public RelatedResource(String title, URL url, Type type) {
		super();
		this.title = title;
		this.url = url;
		this.type = type;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public URL getURL() {
		return url;
	}


	public void setURL(URL url) {
		this.url = url;
	}


	public Type getType() {
		return type;
	}


	public void setType(Type type) {
		this.type = type;
	}


	@Override
	public String toString() {
		return "Related resource (" + getType() + "): " + getTitle() + " <" + getURL() + ">";
	}
}
