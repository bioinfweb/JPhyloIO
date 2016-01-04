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
package info.bioinfweb.jphyloio.model;



public interface GraphData {
	public boolean isTree();
	
	public String getRootID();  //TODO Possibly allow multiple roots for graphs
	
	public NodeData getNodeInfoByID(String id);
	
	public ElementCollection<String> getEdgesByNode(int id);  //TODO Return type must contain the target node ID, the branch length and meta information for each element
	//TODO ElementCollection should probably not be used here, but a type for each element containing meta information should be used instead. That would be suitable everywhere, except for sequence tokens.
}
