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


import info.bioinfweb.jphyloio.ReadWriteConstants;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;



public interface PhyloXMLConstants {
	public static final String PHYLOXML_FORMAT_NAME = "PhyloXML";
	public static final String PHYLOXML_DEFAULT_PRE = "phy";
	public static final String PHYLOXML_NAMESPACE = "http://www.phyloxml.org";
	
	public static final QName TAG_ROOT = new QName(PHYLOXML_NAMESPACE, "phyloxml");
	
	public static final QName TAG_PHYLOGENY = new QName(PHYLOXML_NAMESPACE, "phylogeny");
	public static final QName TAG_NAME = new QName(PHYLOXML_NAMESPACE, "name");
	public static final QName TAG_ID = new QName(PHYLOXML_NAMESPACE, "id");
	public static final QName TAG_DESCRIPTION = new QName(PHYLOXML_NAMESPACE, "description");
	public static final QName TAG_DATE = new QName(PHYLOXML_NAMESPACE, "date");
	public static final QName TAG_CONFIDENCE = new QName(PHYLOXML_NAMESPACE, "confidence");
	public static final QName TAG_CLADE_RELATION = new QName(PHYLOXML_NAMESPACE, "clade_relation");
	public static final QName TAG_SEQUENCE_RELATION = new QName(PHYLOXML_NAMESPACE, "sequence_relation");
	public static final QName TAG_PROPERTY = new QName(PHYLOXML_NAMESPACE, "property");	
	
	public static final QName TAG_CLADE = new QName(PHYLOXML_NAMESPACE, "clade");
	public static final QName TAG_BRANCH_LENGTH = new QName(PHYLOXML_NAMESPACE, "branch_length");
	public static final QName TAG_BRANCH_WIDTH = new QName(PHYLOXML_NAMESPACE, "width");
	public static final QName TAG_BRANCH_COLOR = new QName(PHYLOXML_NAMESPACE, "color");
	public static final QName TAG_RED = new QName(PHYLOXML_NAMESPACE, "red");
	public static final QName TAG_GREEN = new QName(PHYLOXML_NAMESPACE, "green");
	public static final QName TAG_BLUE = new QName(PHYLOXML_NAMESPACE, "blue");	
	public static final QName TAG_NODE_ID = new QName(PHYLOXML_NAMESPACE, "node_id");
	
	public static final QName TAG_TAXONOMY = new QName(PHYLOXML_NAMESPACE, "taxonomy");
	public static final QName TAG_CODE = new QName(PHYLOXML_NAMESPACE, "code");
	public static final QName TAG_SCI_NAME = new QName(PHYLOXML_NAMESPACE, "scientific_name");
	public static final QName TAG_AUTHORITY = new QName(PHYLOXML_NAMESPACE, "authority");
	public static final QName TAG_COMMON_NAME = new QName(PHYLOXML_NAMESPACE, "common_name");
	public static final QName TAG_SYNONYM = new QName(PHYLOXML_NAMESPACE, "synonym");
	public static final QName TAG_RANK = new QName(PHYLOXML_NAMESPACE, "rank");
	public static final QName TAG_URI = new QName(PHYLOXML_NAMESPACE, "uri");
	
	public static final QName TAG_SEQUENCE = new QName(PHYLOXML_NAMESPACE, "sequence");
	public static final QName TAG_SYMBOL = new QName(PHYLOXML_NAMESPACE, "symbol");
	public static final QName TAG_ACCESSION = new QName(PHYLOXML_NAMESPACE, "accession");
	public static final QName TAG_LOCATION = new QName(PHYLOXML_NAMESPACE, "location");
	public static final QName TAG_MOL_SEQ = new QName(PHYLOXML_NAMESPACE, "mol_seq");
	public static final QName TAG_ANNOTATION = new QName(PHYLOXML_NAMESPACE, "annotation");
	public static final QName TAG_DOMAIN_ARCHITECTURE = new QName(PHYLOXML_NAMESPACE, "domain_architecture");	
	
	public static final QName TAG_EVENTS = new QName(PHYLOXML_NAMESPACE, "events");
	public static final QName TAG_TYPE = new QName(PHYLOXML_NAMESPACE, "type");
	public static final QName TAG_DUPLICATIONS = new QName(PHYLOXML_NAMESPACE, "duplications");
	public static final QName TAG_SPECIATIONS = new QName(PHYLOXML_NAMESPACE, "speciations");
	public static final QName TAG_LOSSES = new QName(PHYLOXML_NAMESPACE, "losses");	
	
	public static final QName TAG_BINARY_CHARACTERS = new QName(PHYLOXML_NAMESPACE, "binary_characters");
	public static final QName TAG_GAINED = new QName(PHYLOXML_NAMESPACE, "gained");
	public static final QName TAG_LOST = new QName(PHYLOXML_NAMESPACE, "lost");
	public static final QName TAG_PRESENT = new QName(PHYLOXML_NAMESPACE, "present");
	public static final QName TAG_ABSENT = new QName(PHYLOXML_NAMESPACE, "absent");
	public static final QName TAG_BC = new QName(PHYLOXML_NAMESPACE, "bc");
	
	public static final QName TAG_DISTRIBUTION = new QName(PHYLOXML_NAMESPACE, "distribution");
	public static final QName TAG_DESC = new QName(PHYLOXML_NAMESPACE, "desc");
	public static final QName TAG_POINT = new QName(PHYLOXML_NAMESPACE, "point");
	public static final QName TAG_LAT = new QName(PHYLOXML_NAMESPACE, "lat");
	public static final QName TAG_LONG = new QName(PHYLOXML_NAMESPACE, "long");
	public static final QName TAG_ALT = new QName(PHYLOXML_NAMESPACE, "alt");	
	public static final QName TAG_POLYGON = new QName(PHYLOXML_NAMESPACE, "polygon");	
	
	public static final QName TAG_REFERENCE = new QName(PHYLOXML_NAMESPACE, "reference");
	public static final QName TAG_VALUE = new QName(PHYLOXML_NAMESPACE, "value");
	public static final QName TAG_MINIMUM = new QName(PHYLOXML_NAMESPACE, "minimum");
	public static final QName TAG_MAXIMUM = new QName(PHYLOXML_NAMESPACE, "maximum");
	
		
	public static final QName ATTR_ROOTED = new QName("rooted");
	public static final QName ATTR_REROOTABLE = new QName("rerootable");
	public static final QName ATTR_BRANCH_LENGTH_UNIT = new QName("branch_length_unit");
	public static final QName ATTR_TYPE = new QName("type");
	
	public static final QName ATTR_BRANCH_LENGTH = new QName("branch_length");
	public static final QName ATTR_ID_SOURCE = new QName("id_source");
	
	public static final QName ATTR_ID_PROVIDER = new QName("provider");
	
	public static final QName ATTR_ID_REF_0 = new QName("id_ref_0");
	public static final QName ATTR_ID_REF_1 = new QName("id_ref_1");
	public static final QName ATTR_DISTANCE = new QName("distance");
	
	public static final QName ATTR_REF = new QName("ref");
	public static final QName ATTR_UNIT = new QName("unit");
	public static final QName ATTR_DATATYPE = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "datatype");
	public static final QName ATTR_APPLIES_TO = new QName("applies_to");
	public static final QName ATTR_ID_REF = new QName("id_ref");
	
	public static final QName ATTR_DESC = new QName("desc");
	public static final QName ATTR_SOURCE = new QName("source");
	public static final QName ATTR_IS_ALIGNED = new QName("is_aligned");
	public static final QName ATTR_EVIDENCE = new QName("evidence");
	public static final QName ATTR_LENGTH = new QName("length");
	public static final QName ATTR_GEO_DATUM = new QName("geodetic_datum");
	public static final QName ATTR_ALT_UNIT = new QName("alt_unit");
	
	public static final QName ATTR_GAINED_COUNT = new QName("gained_count");
	public static final QName ATTR_LOST_COUNT = new QName("lost_count");
	public static final QName ATTR_PRESENT_COUNT = new QName("present_count");
	public static final QName ATTR_ABSENT_COUNT = new QName("absent_count");	
	
	
	public static final String PHYLOXML_NAMESPACE_PREFIX = ReadWriteConstants.JPHYLOIO_NAMESPACE_PREFIX + "Formats/PhyloXML/";
	
	public static final String PHYLOXML_PREDICATE_NAMESPACE = PHYLOXML_NAMESPACE_PREFIX + "Predicates/";
	
	public static final QName PREDICATE_PHYLOGENY = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny");
	public static final QName PREDICATE_PHYLOGENY_NAME = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny/Name");
	public static final QName PREDICATE_PHYLOGENY_ID = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny/ID");
	public static final QName PREDICATE_PHYLOGENY_ATTR_ID_PROVIDER = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny/ID@Provider");
	public static final QName PREDICATE_PHYLOGENY_DESCRIPTION = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny/Description");
	public static final QName PREDICATE_PHYLOGENY_DATE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny/Date");
	public static final QName PREDICATE_PHYLOGENY_CONFIDENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny/Confidence");
	public static final QName PREDICATE_PHYLOGENY_CONFIDENCE_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny/Confidence@Type");
	public static final QName PREDICATE_PHYLOGENY_ATTR_ROOTED = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny@Rooted");
	public static final QName PREDICATE_PHYLOGENY_ATTR_REROOTABLE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny@Rerootable");
	public static final QName PREDICATE_PHYLOGENY_ATTR_BRANCH_LENGTH_UNIT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny@BranchLengthUnit");
	public static final QName PREDICATE_PHYLOGENY_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny@Type");
	
	public static final QName PREDICATE_CLADE_REL = new QName(PHYLOXML_PREDICATE_NAMESPACE, "CladeRelation");
	public static final QName PREDICATE_CLADE_REL_ATTR_IDREF0 = new QName(PHYLOXML_PREDICATE_NAMESPACE, "CladeRelation@IDRef0");
	public static final QName PREDICATE_CLADE_REL_ATTR_IDREF1 = new QName(PHYLOXML_PREDICATE_NAMESPACE, "CladeRelation@IDRef1");
	public static final QName PREDICATE_CLADE_REL_ATTR_DISTANCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "CladeRelation@Distance");
	public static final QName PREDICATE_CLADE_REL_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "CladeRelation@Type");
	public static final QName PREDICATE_CLADE_REL_CONFIDENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "CladeRelation/Confidence");
	public static final QName PREDICATE_CLADE_REL_CONFIDENCE_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "CladeRelation/Confidence@Type");	
	
	public static final QName PREDICATE_SEQ_REL = new QName(PHYLOXML_PREDICATE_NAMESPACE, "SequenceRelation");
	public static final QName PREDICATE_SEQ_REL_ATTR_IDREF0 = new QName(PHYLOXML_PREDICATE_NAMESPACE, "SequenceRelation@IDRef0");
	public static final QName PREDICATE_SEQ_REL_ATTR_IDREF1 = new QName(PHYLOXML_PREDICATE_NAMESPACE, "SequenceRelation@IDRef1");
	public static final QName PREDICATE_SEQ_REL_ATTR_DISTANCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "SequenceRelation@Distance");
	public static final QName PREDICATE_SEQ_REL_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "SequenceRelation@Type");
	public static final QName PREDICATE_SEQ_REL_CONFIDENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "SequenceRelation/Confidence");
	public static final QName PREDICATE_SEQ_REL_CONFIDENCE_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "SequenceRelation/Confidence@Type");
	
//	public static final QName PREDICATE_PHYLOGENY_CLADE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Phylogeny/Clade"); //TODO predicate for Phylogeny/Clade and Clade/Clade?
	public static final QName PREDICATE_CLADE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade");
	public static final QName PREDICATE_CLADE_NAME = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade/Name");
	public static final QName PREDICATE_CLADE_BRANCH_LENGTH = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade/BranchLength");	
	public static final QName PREDICATE_CLADE_CONFIDENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade/Confidence");
	public static final QName PREDICATE_CLADE_CONFIDENCE_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade/Confidence@Type");	
	public static final QName PREDICATE_CLADE_WIDTH = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade/Width");	
	public static final QName PREDICATE_CLADE_NODE_ID = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade/NodeID");
	public static final QName PREDICATE_CLADE_NODE_ID_ATTR_PROVIDER = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade/NodeID@Provider");	
	public static final QName PREDICATE_CLADE_ATTR_BRANCH_LENGTH = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade@BranchLength");
	public static final QName PREDICATE_CLADE_ATTR_ID_SOURCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Clade@IDSource");	
	
	public static final QName PREDICATE_COLOR = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Color");
	public static final QName PREDICATE_COLOR_RED = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Color/Red");
	public static final QName PREDICATE_COLOR_GREEN = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Color/Green");
	public static final QName PREDICATE_COLOR_BLUE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Color/Blue");
	
	public static final QName PREDICATE_TAXONOMY = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy");
	public static final QName PREDICATE_TAXONOMY_ID = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/ID");
	public static final QName PREDICATE_TAXONOMY_ID_ATTR_PROVIDER = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/ID@Provider");
	public static final QName PREDICATE_TAXONOMY_CODE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/Code");
	public static final QName PREDICATE_TAXONOMY_SCI_NAME = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/ScientificName");
	public static final QName PREDICATE_TAXONOMY_AUTHORITY = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/Authority");
	public static final QName PREDICATE_TAXONOMY_COMMON_NAME = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/CommonName");
	public static final QName PREDICATE_TAXONOMY_SYNONYM = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/Synonym");
	public static final QName PREDICATE_TAXONOMY_RANK = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/Rank");
	public static final QName PREDICATE_TAXONOMY_URI = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/URI");
	public static final QName PREDICATE_TAXONOMY_URI_ATTR_DESC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/URI@Desc");
	public static final QName PREDICATE_TAXONOMY_URI_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy/URI@Type");	
	public static final QName PREDICATE_TAXONOMY_ATTR_ID_SOURCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Taxonomy@IDSource");
	
	public static final QName PREDICATE_SEQUENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence");
	public static final QName PREDICATE_SEQUENCE_SYMBOL = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/Symbol");
	public static final QName PREDICATE_SEQUENCE_ACCESSION = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/Accession");
	public static final QName PREDICATE_SEQUENCE_ACCESSION_ATTR_SOURCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/Accession@Source");
	public static final QName PREDICATE_SEQUENCE_NAME = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/Name");
	public static final QName PREDICATE_SEQUENCE_LOCATION = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/Location");
	public static final QName PREDICATE_SEQUENCE_MOL_SEQ = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/MolSeq");
	public static final QName PREDICATE_SEQUENCE_MOL_SEQ_ATTR_IS_ALIGNED = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/MolSeq@IsAligned");
	public static final QName PREDICATE_SEQUENCE_URI = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/URI");
	public static final QName PREDICATE_SEQUENCE_URI_ATTR_DESC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/URI@Desc");
	public static final QName PREDICATE_SEQUENCE_URI_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Sequence/URI@Type");
	
	public static final QName PREDICATE_DOMAIN_ARCHITECTURE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "DomainArchitecture");
	public static final QName PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN = new QName(PHYLOXML_PREDICATE_NAMESPACE, "DomainArchitecture/Domain");
	public static final QName PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_FROM = new QName(PHYLOXML_PREDICATE_NAMESPACE, "DomainArchitecture/Domain@From");
	public static final QName PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_TO = new QName(PHYLOXML_PREDICATE_NAMESPACE, "DomainArchitecture/Domain@To");
	public static final QName PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_CONFIDENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "DomainArchitecture/Domain@Confidence");
	public static final QName PREDICATE_DOMAIN_ARCHITECTURE_DOMAIN_ATTR_ID = new QName(PHYLOXML_PREDICATE_NAMESPACE, "DomainArchitecture/Domain@ID");
	public static final QName PREDICATE_DOMAIN_ARCHITECTURE_ATTR_LENGTH = new QName(PHYLOXML_PREDICATE_NAMESPACE, "DomainArchitecture@Length");
	
	public static final QName PREDICATE_ANNOTATION = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation");
	public static final QName PREDICATE_ANNOTATION_DESC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Desc");
	public static final QName PREDICATE_ANNOTATION_CONFIDENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Confidence");
	public static final QName PREDICATE_ANNOTATION_CONFIDENCE_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Confidence@Type");
	public static final QName PREDICATE_ANNOTATION_PROPERTY = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Property");
	public static final QName PREDICATE_ANNOTATION_PROPERTY_ATTR_REF = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Property@Ref");
	public static final QName PREDICATE_ANNOTATION_PROPERTY_ATTR_UNIT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Property@Unit");
	public static final QName PREDICATE_ANNOTATION_PROPERTY_ATTR_DATATYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Property@Datatype");
	public static final QName PREDICATE_ANNOTATION_PROPERTY_ATTR_APPLIES_TO = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Property@AppliesTo");
	public static final QName PREDICATE_ANNOTATION_PROPERTY_ATTR_ID_REF = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/Property@IDRef");	
	public static final QName PREDICATE_ANNOTATION_URI = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/URI");
	public static final QName PREDICATE_ANNOTATION_URI_ATTR_DESC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/URI@Desc");
	public static final QName PREDICATE_ANNOTATION_URI_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation/URI@Type");
	public static final QName PREDICATE_ANNOTATION_ATTR_REF = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation@Ref");
	public static final QName PREDICATE_ANNOTATION_ATTR_SOURCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation@Source");
	public static final QName PREDICATE_ANNOTATION_ATTR_EVIDENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation@Evidence");
	public static final QName PREDICATE_ANNOTATION_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Annotation@Type");
	
	public static final QName PREDICATE_EVENTS = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Events");
	public static final QName PREDICATE_EVENTS_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Events/Type");
	public static final QName PREDICATE_EVENTS_DUPLICATIONS = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Events/Duplications");
	public static final QName PREDICATE_EVENTS_SPECIATIONS = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Events/Speciations");
	public static final QName PREDICATE_EVENTS_LOSSES = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Events/Losses");
	public static final QName PREDICATE_EVENTS_CONFIDENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Events/Confidence");
	public static final QName PREDICATE_EVENTS_CONFIDENCE_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Events/Confidence@Type");
	
	public static final QName PREDICATE_BINARY_CHARACTERS = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters");
	public static final QName PREDICATE_BINARY_CHARACTERS_GAINED = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters/Gained");
	public static final QName PREDICATE_BINARY_CHARACTERS_GAINED_BC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters/Gained/Bc");
	public static final QName PREDICATE_BINARY_CHARACTERS_LOST = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters/Lost");
	public static final QName PREDICATE_BINARY_CHARACTERS_LOST_BC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters/Lost/Bc");
	public static final QName PREDICATE_BINARY_CHARACTERS_PRESENT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters/Present");
	public static final QName PREDICATE_BINARY_CHARACTERS_PRESENT_BC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters/Present/Bc");
	public static final QName PREDICATE_BINARY_CHARACTERS_ABSENT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters/Absent");
	public static final QName PREDICATE_BINARY_CHARACTERS_ABSENT_BC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters/Absent/Bc");
	public static final QName PREDICATE_BINARY_CHARACTERS_ATTR_TYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters@Type");
	public static final QName PREDICATE_BINARY_CHARACTERS_ATTR_GAINED_COUNT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters@GainedCount");
	public static final QName PREDICATE_BINARY_CHARACTERS_ATTR_LOST_COUNT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters@LostCount");
	public static final QName PREDICATE_BINARY_CHARACTERS_ATTR_PRESENT_COUNT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters@PresentCount");
	public static final QName PREDICATE_BINARY_CHARACTERS_ATTR_ABSENT_COUNT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "BinaryCharacters@AbsentCount");
	
	public static final QName PREDICATE_DISTRIBUTION = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution");
	public static final QName PREDICATE_DISTRIBUTION_DESC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Desc");
	public static final QName PREDICATE_DISTRIBUTION_POINT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Point");
	public static final QName PREDICATE_DISTRIBUTION_POINT_LAT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Point/Lat");
	public static final QName PREDICATE_DISTRIBUTION_POINT_LONG = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Point/Long");
	public static final QName PREDICATE_DISTRIBUTION_POINT_ALT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Point/Alt");
	public static final QName PREDICATE_DISTRIBUTION_POINT_GEODETIC_DATUM = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Point@GeodeticDatum");
	public static final QName PREDICATE_DISTRIBUTION_POINT_ALT_UNIT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Point@AltUnit");
	public static final QName PREDICATE_DISTRIBUTION_POLYGON = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Polygon");
	public static final QName PREDICATE_DISTRIBUTION_POLYGON_POINT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Polygon/Point");
	public static final QName PREDICATE_DISTRIBUTION_POLYGON_POINT_LAT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Polygon/Point/Lat");
	public static final QName PREDICATE_DISTRIBUTION_POLYGON_POINT_LONG = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Polygon/Point/Long");
	public static final QName PREDICATE_DISTRIBUTION_POLYGON_POINT_ALT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Polygon/Point/Alt");
	public static final QName PREDICATE_DISTRIBUTION_POLYGON_POINT_GEODETIC_DATUM = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Polygon/Point@GeodeticDatum");
	public static final QName PREDICATE_DISTRIBUTION_POLYGON_POINT_ALT_UNIT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Distribution/Polygon/Point@AltUnit");
	
	public static final QName PREDICATE_DATE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Date");
	public static final QName PREDICATE_DATE_DESC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Date/Desc");
	public static final QName PREDICATE_DATE_VALUE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Date/Value");
	public static final QName PREDICATE_DATE_MINIMUM = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Date/Minimum");
	public static final QName PREDICATE_DATE_MAXIMUM = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Date/Maximum");
	public static final QName PREDICATE_DATE_ATTR_UNIT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Date@Unit");
	
	public static final QName PREDICATE_REFERENCE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Reference");	
	public static final QName PREDICATE_REFERENCE_DESC = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Reference/Desc");
	public static final QName PREDICATE_REFERENCE_ATTR_DOI = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Reference@DOI");
	
	public static final QName PREDICATE_PROPERTY = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Property");
	public static final QName PREDICATE_PROPERTY_ATTR_REF = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Property@Ref");
	public static final QName PREDICATE_PROPERTY_ATTR_UNIT = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Property@Unit");
	public static final QName PREDICATE_PROPERTY_ATTR_DATATYPE = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Property@Datatype");
	public static final QName PREDICATE_PROPERTY_ATTR_APPLIES_TO = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Property@AppliesTo");
	public static final QName PREDICATE_PROPERTY_ATTR_ID_REF = new QName(PHYLOXML_PREDICATE_NAMESPACE, "Property@IDRef");
	
	

	public static final String PHYLOXML_DATA_TYPE_NAMESPACE = PHYLOXML_NAMESPACE_PREFIX + "DataTypes/";
	public static final QName DATA_TYPE_TOKEN = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "Token");
	public static final QName DATA_TYPE_APPLIES_TO = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "Property/AppliesTo");
	public static final QName DATA_TYPE_EVENTTYPE = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "Event/EventType");
	public static final QName DATA_TYPE_PROPERTY_DATATYPE = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "Property/PropertyDataType");
	public static final QName DATA_TYPE_RANK = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "Taxonomy/Rank");
	public static final QName DATA_TYPE_SEQUENCE_RELATION_TYPE = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "SequenceRelation/SequenceRelationType");
	public static final QName DATA_TYPE_SEQUENCE_SYMBOL = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "Sequence/SequenceSymbol");
	public static final QName DATA_TYPE_SEQUENCE_TYPE = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "Sequence/SequenceType");
	public static final QName DATA_TYPE_TAXONOMY_CODE = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "Sequence/TaxonomyCode");
	public static final QName DATA_TYPE_ID_REF = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "id_ref");
	public static final QName DATA_TYPE_ID_SOURCE = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "id_source");
	public static final QName DATA_TYPE_REF = new QName(PHYLOXML_DATA_TYPE_NAMESPACE, "ref");
}
