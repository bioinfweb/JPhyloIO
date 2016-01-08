/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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
package info.bioinfweb.jphyloio.events;


import info.bioinfweb.commons.collections.ParameterMap;



public class MetaEventParameterMap extends ParameterMap {
	public static final String KEY_ANNOTATED_BLOCK = "annotatedBlock";
	public static final String KEY_TYPE = "type";
	
	public static final String KEY_DATATYPE = "datatype";	
	public static final String KEY_REL = "rel";
	public static final String KEY_HREF = "href";
	public static final String KEY_CONTENT = "content";
	public static final String KEY_PROPERTY = "property";
}
