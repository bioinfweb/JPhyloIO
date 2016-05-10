/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben St�ver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.phyloxml;


import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;



public class PhyloXMLReaderStreamDataProvider extends XMLReaderStreamDataProvider<PhyloXMLEventReader>  {
	private boolean rooted;	
	private String treeLabel;
	

	public PhyloXMLReaderStreamDataProvider(PhyloXMLEventReader eventReader) {
		super(eventReader);
	}


	public boolean isRooted() {
		return rooted;
	}


	public void setRooted(boolean rooted) {
		this.rooted = rooted;
	}


	public String getTreeLabel() {
		return treeLabel;
	}


	public void setTreeLabel(String treeLabel) {
		this.treeLabel = treeLabel;
	}
}
