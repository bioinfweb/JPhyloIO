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


/**
 * Enumerates possible combinations of meta events to be nested between start and end events of other content types.
 * 
 * @author Sarah Wiechers
 * @author Ben St&ouml;ver
 * @since 0.1.0
 * @see MetadataModeling
 * @see JPhyloIOFormatInfo#getMetadataModeling(info.bioinfweb.jphyloio.events.type.EventContentType, boolean)
 */
public enum MetadataTopologyType {
	/** No nested metadata is supported. */
	NONE,
	
	/** Only literal metadata on the first level is supported. */
	LITERAL_ONLY,
	
	/** Nested meta data including literal meta data on the first level is supported, 
	 * but not full meta data trees according to the <i>RDFa</a> model. */
	LIMITED_TREE,
	
	/** Nested metadata according to the <i>RDFa</a> model are supported. */
	FULL_TREE;
}