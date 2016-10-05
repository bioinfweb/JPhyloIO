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
package info.bioinfweb.jphyloio.formatinfo;


import info.bioinfweb.jphyloio.events.meta.LiteralContentSequenceType;

import java.util.Collections;
import java.util.Set;



public class MetadataModeling {
	private MetadataTopologyType topologyType;
	private Set<LiteralContentSequenceType> contentTypes;
	
	
	public MetadataModeling(MetadataTopologyType topologyType, Set<LiteralContentSequenceType> contentTypes) {
		super();
		this.topologyType = topologyType;
		this.contentTypes = Collections.unmodifiableSet(contentTypes);
	}


	public MetadataTopologyType getTopologyType() {
		return topologyType;
	}


	public Set<LiteralContentSequenceType> getContentTypes() {
		return contentTypes;
	}
}
