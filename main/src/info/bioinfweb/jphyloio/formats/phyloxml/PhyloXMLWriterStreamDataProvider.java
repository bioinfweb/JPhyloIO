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


import info.bioinfweb.jphyloio.formats.xml.XMLWriterStreamDataProvider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;



public class PhyloXMLWriterStreamDataProvider extends XMLWriterStreamDataProvider<PhyloXMLEventWriter> implements PhyloXMLConstants, PhyloXMLPrivateConstants {
	
	
	
	private Map<QName, PhyloXMLPredicateInfo> predicateInfoMap = new HashMap<QName, PhyloXMLPredicateInfo>();
	
	private Map<String, PhyloXMLMetaeventInfo> metaEvents = new HashMap<String, PhyloXMLMetaeventInfo>();
	private Set<String> metaIDs = new HashSet<String>();
	
	
	public PhyloXMLWriterStreamDataProvider(PhyloXMLEventWriter eventWriter) {
		super(eventWriter);
		
		fillMetaPredicateMap();
	}
	
	
	@Override
	public PhyloXMLEventWriter getEventWriter() { //TODO is this still necessary (generics)?
		return (PhyloXMLEventWriter)super.getEventWriter();
	}


	public Map<String, PhyloXMLMetaeventInfo> getMetaEvents() {
		return metaEvents;
	}


	public Set<String> getMetaIDs() {
		return metaIDs;
	}
	
	
	public Map<QName, PhyloXMLPredicateInfo> getPredicateInfoMap() {
		return predicateInfoMap;
	}


	private void fillMetaPredicateMap() { //TODO leave out unused predicates?
		predicateInfoMap.put(IDENTIFIER_PHYLOGENY, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG, TAG_PHYLOGENY, PREDICATE_PHYLOGENY_ATTR_REROOTABLE, 
				PREDICATE_PHYLOGENY_ATTR_BRANCH_LENGTH_UNIT, PREDICATE_PHYLOGENY_ATTR_TYPE, IDENTIFIER_PHYLOGENY_NAME, PREDICATE_PHYLOGENY_ID, 
				PREDICATE_PHYLOGENY_DESCRIPTION, PREDICATE_PHYLOGENY_DATE, PREDICATE_CONFIDENCE, PREDICATE_CLADE_REL, PREDICATE_SEQ_REL, PREDICATE_PROPERTY));
		predicateInfoMap.put(PREDICATE_PHYLOGENY_ATTR_REROOTABLE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_REROOTABLE));
		predicateInfoMap.put(PREDICATE_PHYLOGENY_ATTR_BRANCH_LENGTH_UNIT, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_BRANCH_LENGTH_UNIT));
		predicateInfoMap.put(PREDICATE_PHYLOGENY_ATTR_TYPE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_TYPE));
		predicateInfoMap.put(IDENTIFIER_PHYLOGENY_NAME, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG_AND_VALUE, null));
		predicateInfoMap.put(PREDICATE_PHYLOGENY_ID, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG, TAG_ID, PREDICATE_PHYLOGENY_ID_ATTR_PROVIDER, 
				PREDICATE_PHYLOGENY_ID_VALUE));
		predicateInfoMap.put(PREDICATE_PHYLOGENY_ID_ATTR_PROVIDER, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_ID_PROVIDER));
		predicateInfoMap.put(PREDICATE_PHYLOGENY_ID_VALUE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.VALUE, null));
		predicateInfoMap.put(PREDICATE_PHYLOGENY_DESCRIPTION, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG_AND_VALUE, TAG_DESCRIPTION));
		predicateInfoMap.put(PREDICATE_PHYLOGENY_DATE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG_AND_VALUE, TAG_DATE));
		
		predicateInfoMap.put(PREDICATE_CLADE_REL, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG, TAG_CLADE_RELATION, PREDICATE_CLADE_REL_ATTR_IDREF0, 
				PREDICATE_CLADE_REL_ATTR_IDREF1, PREDICATE_CLADE_REL_ATTR_DISTANCE, PREDICATE_CLADE_REL_ATTR_TYPE));
		predicateInfoMap.put(PREDICATE_CLADE_REL_ATTR_IDREF0, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_ID_REF_0));
		predicateInfoMap.put(PREDICATE_CLADE_REL_ATTR_IDREF1, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_ID_REF_1));
		predicateInfoMap.put(PREDICATE_CLADE_REL_ATTR_DISTANCE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_DISTANCE));
		predicateInfoMap.put(PREDICATE_CLADE_REL_ATTR_TYPE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_TYPE));
		
		predicateInfoMap.put(PREDICATE_SEQ_REL, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG, TAG_SEQUENCE_RELATION, PREDICATE_SEQ_REL_ATTR_IDREF0, 
				PREDICATE_SEQ_REL_ATTR_IDREF1, PREDICATE_SEQ_REL_ATTR_DISTANCE, PREDICATE_SEQ_REL_ATTR_TYPE, PREDICATE_SEQ_REL_CONFIDENCE));
		predicateInfoMap.put(PREDICATE_CLADE_REL_ATTR_IDREF0, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_ID_REF_0));
		predicateInfoMap.put(PREDICATE_CLADE_REL_ATTR_IDREF1, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_ID_REF_1));
		predicateInfoMap.put(PREDICATE_CLADE_REL_ATTR_DISTANCE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_DISTANCE));
		predicateInfoMap.put(PREDICATE_CLADE_REL_ATTR_TYPE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_TYPE));
		
		predicateInfoMap.put(PREDICATE_SEQ_REL_CONFIDENCE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG, TAG_CONFIDENCE, PREDICATE_SEQ_REL_CONFIDENCE_ATTR_TYPE, 
				PREDICATE_SEQ_REL_CONFIDENCE_VALUE));
		predicateInfoMap.put(PREDICATE_SEQ_REL_CONFIDENCE_ATTR_TYPE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_TYPE));
		predicateInfoMap.put(PREDICATE_SEQ_REL_CONFIDENCE_VALUE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.VALUE, null));
		
		predicateInfoMap.put(PREDICATE_CONFIDENCE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG, TAG_CONFIDENCE, PREDICATE_CONFIDENCE_ATTR_TYPE, 
				PREDICATE_CONFIDENCE_VALUE));
		predicateInfoMap.put(PREDICATE_CONFIDENCE_ATTR_TYPE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_TYPE));
		predicateInfoMap.put(PREDICATE_CONFIDENCE_VALUE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.VALUE, null));
		
		predicateInfoMap.put(PREDICATE_PROPERTY, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.TAG_AND_VALUE, TAG_PROPERTY, PREDICATE_PROPERTY_ATTR_REF, 
				PREDICATE_PROPERTY_ATTR_UNIT, PREDICATE_PROPERTY_ATTR_DATATYPE, PREDICATE_PROPERTY_ATTR_APPLIES_TO, PREDICATE_PROPERTY_ATTR_ID_REF));
		predicateInfoMap.put(PREDICATE_PROPERTY_ATTR_REF, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_REF));
		predicateInfoMap.put(PREDICATE_PROPERTY_ATTR_UNIT, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_UNIT));
		predicateInfoMap.put(PREDICATE_PROPERTY_ATTR_DATATYPE, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_DATATYPE));
		predicateInfoMap.put(PREDICATE_PROPERTY_ATTR_APPLIES_TO, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_APPLIES_TO));
		predicateInfoMap.put(PREDICATE_PROPERTY_ATTR_ID_REF, new PhyloXMLPredicateInfo(PhyloXMLPredicateTreatment.ATTRIBUTE, ATTR_ID_REF));
	}
}