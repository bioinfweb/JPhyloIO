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


import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLConstants;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;



public class NHXTools implements NewickConstants, PhyloXMLConstants {
	private static final int NHX_KEY_COUNT = 6;


	private static NHXTools firstInstance = null;
	
	private Map<String, QName> predicateByKeyMap;
	private Map<QName, String> keyByPredicateMap;
	
	
	private NHXTools() {
		super();
		createMaps();
	}
	
	
	private void createMaps() {
		predicateByKeyMap = new HashMap<String, QName>(NHX_KEY_COUNT);
		predicateByKeyMap.put(NHX_KEY_GENE_NAME, PREDICATE_SEQUENCE_NAME);
		predicateByKeyMap.put(NHX_KEY_SEQUENCE_ACCESSION, PREDICATE_SEQUENCE_ACCESSION_VALUE);
		predicateByKeyMap.put(NHX_KEY_CONFIDENCE, PREDICATE_CONFIDENCE_VALUE);
		predicateByKeyMap.put(NHX_KEY_EVENT, PREDICATE_EVENTS_TYPE);  //TODO Is this correct?
		predicateByKeyMap.put(NHX_KEY_SCIENTIFIC_NAME, PREDICATE_TAXONOMY_SCIENTIFIC_NAME);
		predicateByKeyMap.put(NHX_KEY_TAXONOMY_ID, PREDICATE_TAXONOMY_ID_VALUE);
		
		keyByPredicateMap = new HashMap<QName, String>(NHX_KEY_COUNT);
		for (String key : predicateByKeyMap.keySet()) {
			keyByPredicateMap.put(predicateByKeyMap.get(key), key);
		}
	}
	
	
	public static NHXTools getInstance() {
		if (firstInstance == null) {
			firstInstance = new NHXTools();
		}
		return firstInstance;
	}
	
	
	public QName predicateByKey(String key) {
		return predicateByKeyMap.get(key);
	}
	
	
	public String keyByPredicate(QName predicate) {
		return keyByPredicateMap.get(predicate);
	}
}
