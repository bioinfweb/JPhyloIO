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
package info.bioinfweb.jphyloio.formats.nexus.commandreaders.trees;


import java.util.List;

import info.bioinfweb.jphyloio.formats.newick.NewickNodeLabelProcessor;
import info.bioinfweb.jphyloio.formats.nexus.NexusStreamDataProvider;



public class NexusNewickNodeLabelProcessor implements NewickNodeLabelProcessor {
	private NexusStreamDataProvider streamDataProvider;
	
	
	public NexusNewickNodeLabelProcessor(NexusStreamDataProvider streamDataProvider) {
		super();
		this.streamDataProvider = streamDataProvider;
	}


	@Override
	public String processLabel(String originalLabel) {
		if (originalLabel == null) {  // e.g. for some internal nodes
			return null;
		}
		else {
			NexusTranslationTable table = streamDataProvider.getTreesTranslationTable();
			List<String> taxaList = streamDataProvider.getTaxaList();
			String result = table.get(originalLabel);
			if (result == null) {
				try {
					int index = Integer.parseInt(originalLabel) - 1;  // Nexus indices start with 1.
					if (index < table.size()) {
						result = table.get(index);
					}
					else if (index < taxaList.size()) {
						result = taxaList.get(index);
					}
				}
				catch (NumberFormatException e) {}
				
				if (result == null) {  // No replacement was successful.
					result = originalLabel;
				}
			}
			return result;
		}
	}

	
	@Override
	public String getLinkedOTUID(String processedLabel) {
		if (processedLabel == null) {
			return null;
		}
		else {
			return streamDataProvider.getTaxaToIDMap().get(processedLabel);
		}
	}
}