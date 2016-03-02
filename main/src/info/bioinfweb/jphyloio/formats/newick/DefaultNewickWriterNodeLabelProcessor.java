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
package info.bioinfweb.jphyloio.formats.newick;


import info.bioinfweb.jphyloio.AbstractEventWriter;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.dataadapters.OTUListDataAdapter;
import info.bioinfweb.jphyloio.events.LinkedOTUOrOTUsEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;



public class DefaultNewickWriterNodeLabelProcessor implements NewickWriterNodeLabelProcessor {
	private OTUListDataAdapter otuList;
	private ReadWriteParameterMap parameters;
	
	
	public DefaultNewickWriterNodeLabelProcessor(OTUListDataAdapter otuList, ReadWriteParameterMap parameters) {
		super();
		this.otuList = otuList;
		this.parameters = parameters;
	}


	public OTUListDataAdapter getOTUList() {
		return otuList;
	}


	public ReadWriteParameterMap getParameters() {
		return parameters;
	}


	@Override
	public String createNodeName(LinkedOTUOrOTUsEvent nodeEvent) {
		String result;
		if (nodeEvent.isOTUOrOTUsLinked()) {
			result = AbstractEventWriter.createUniqueLinkedOTULabel(parameters,
					new AbstractEventWriter.NoEditUniqueLabelHandler() {
						@Override
						public boolean isUnique(String label) {
							return !parameters.getLabelEditingReporter().isLabelUsed(EventContentType.NODE, label);
						}
					}, 
					nodeEvent, otuList, true);  // Already considers possible maximum length.
		}
		else {
			result = AbstractEventWriter.getLinkedOTUNameOwnFirst(nodeEvent, getOTUList());
		}
		getParameters().getLabelEditingReporter().addEdit(nodeEvent, result);
		return result;
	}
}
