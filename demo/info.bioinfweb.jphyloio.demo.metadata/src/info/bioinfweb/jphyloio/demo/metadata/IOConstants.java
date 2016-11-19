/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2016  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.demo.metadata;


import javax.xml.namespace.QName;



public interface IOConstants {
	public static final String TREE_ID = "tree";
	public static final String EDGE_ID_PREFIX = "edge";
	public static final String NODE_ID_PREFIX = "node";
	
  public static final String NAMESPACE_URI = "http://example.org/annotations/";
  public static final String NAMESPACE_PREFIX = "a";

  public static final QName PREDICATE_HAS_SUPPORT = new QName(NAMESPACE_URI, "isSupportedWith", NAMESPACE_PREFIX);
  public static final QName PREDICATE_HAS_TAXONOMY = new QName(NAMESPACE_URI, "hasTaxonomy", NAMESPACE_PREFIX);
  public static final QName PREDICATE_HAS_GENUS = new QName(NAMESPACE_URI, "hasGenus", NAMESPACE_PREFIX);
  public static final QName PREDICATE_HAS_SPECIES = new QName(NAMESPACE_URI, "hasSpecies", NAMESPACE_PREFIX);

}
