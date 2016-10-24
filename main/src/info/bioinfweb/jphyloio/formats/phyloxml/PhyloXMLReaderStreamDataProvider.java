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


import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * The {@link XMLReaderStreamDataProvider} used by {@link PhyloXMLEventReader}.
 * 
 * @author Sarah Wiechers
 */
public class PhyloXMLReaderStreamDataProvider extends XMLReaderStreamDataProvider<PhyloXMLEventReader>  {
	private String treeLabel;
	
	private boolean createTreeGroupStart;
	private boolean createPhylogenyStart;
	private boolean createTreeGroupEnd;
	
	private String lastNodeID;
	private Map<String, String> cladeIDToNodeEventIDMap = new HashMap<String, String>();
	
	private List<JPhyloIOEvent> propertyEvents = new ArrayList<JPhyloIOEvent>();
	private boolean propertyHasResource;
	private boolean propertyIsURI;
	private boolean resetEventCollection;
	private boolean isFirstContentEvent;
	

	public PhyloXMLReaderStreamDataProvider(PhyloXMLEventReader eventReader) {
		super(eventReader);
	}


	public String getTreeLabel() {
		return treeLabel;
	}


	public void setTreeLabel(String treeLabel) {
		this.treeLabel = treeLabel;
	}


	protected boolean isCreateTreeGroupStart() {
		return createTreeGroupStart;
	}


	protected void setCreateTreeGroupStart(boolean createTreeGroupStart) {
		this.createTreeGroupStart = createTreeGroupStart;
	}


	protected boolean isCreatePhylogenyStart() {
		return createPhylogenyStart;
	}


	protected void setCreatePhylogenyStart(boolean createPhylogenyStart) {
		this.createPhylogenyStart = createPhylogenyStart;
	}


	protected boolean isCreateTreeGroupEnd() {
		return createTreeGroupEnd;
	}


	protected void setCreateTreeGroupEnd(boolean createTreeGroupEnd) {
		this.createTreeGroupEnd = createTreeGroupEnd;
	}


	public String getLastNodeID() {
		return lastNodeID;
	}


	public void setLastNodeID(String lastNodeID) {
		this.lastNodeID = lastNodeID;
	}


	protected Map<String, String> getCladeIDToNodeEventIDMap() {
		return cladeIDToNodeEventIDMap;
	}


	protected List<JPhyloIOEvent> getPropertyEvents() {
		return propertyEvents;
	}
	

	protected boolean isFirstContentEvent() {
		return isFirstContentEvent;
	}


	protected void setFirstContentEvent(boolean isFirstContentEvent) {
		this.isFirstContentEvent = isFirstContentEvent;
	}


	protected boolean isResetEventCollection() {
		return resetEventCollection;
	}


	protected void setResetEventCollection(boolean resetEventCollection) {
		this.resetEventCollection = resetEventCollection;
	}


	protected boolean isPropertyHasResource() {
		return propertyHasResource;
	}


	protected void setPropertyHasResource(boolean propertyHasResource) {
		this.propertyHasResource = propertyHasResource;
	}


	protected boolean isPropertyIsURI() {
		return propertyIsURI;
	}


	protected void setPropertyIsURI(boolean propertyIsURI) {
		this.propertyIsURI = propertyIsURI;
	}
}
