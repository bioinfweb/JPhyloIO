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
package info.bioinfweb.jphyloio.formats.xtg;


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.*;

import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.events.type.EventType;

import java.io.File;

import org.junit.Test;



public class XTGEventReaderTest implements XTGConstants {
	@Test
	public void testOutput() {
		try {
			XTGEventReader reader = new XTGEventReader(new File("data/XTG/ExampleXTGDocument.xml"), new ReadWriteParameterMap());
			try {
				while (reader.hasNextEvent()) {
					JPhyloIOEvent event = reader.next();
//					System.out.println(event.getType());

					if (event.getType().equals(new EventType(EventContentType.META_LITERAL, EventTopologyType.START))) {
//						System.out.println("Predicate: " + event.asLiteralMetadataEvent().getPredicate().getURI().getLocalPart());
					}
					else if (event.getType().equals(new EventType(EventContentType.META_LITERAL_CONTENT, EventTopologyType.SOLE))) {
						System.out.println("Content: " + event.asLiteralMetadataContentEvent().getStringValue());
					}
					else if (event.getType().equals(new EventType(EventContentType.META_RESOURCE, EventTopologyType.START))) {
						System.out.println("Rel: " + event.asResourceMetadataEvent().getRel().getURI().getLocalPart());
					}
					else if (event.getType().equals(new EventType(EventContentType.META_RESOURCE, EventTopologyType.END))) {
//						System.out.println("Resource end");
					}
					else if (event.getType().equals(new EventType(EventContentType.NODE, EventTopologyType.START))) {
						System.out.println("Node: " + event.asNodeEvent().getID() + " " + event.asNodeEvent().getLabel());
					}
					else if (event.getType().equals(new EventType(EventContentType.EDGE, EventTopologyType.START))) {
						System.out.println("Edge: " + event.asEdgeEvent().getID() + " " + event.asEdgeEvent().getSourceID() + " " + event.asEdgeEvent().getTargetID());
					}
				}
			}
			finally {
				reader.close();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingTree() {
		try {
			XTGEventReader reader = new XTGEventReader(new File("data/XTG/ExampleXTGDocument.xml"), new ReadWriteParameterMap());
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);

				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_BG_COLOR), null, "#FFFFFF", null, "#FFFFFF", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_BRANCH_LENGTH_SCALE), null, "0.1", null, "0.1", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_SHOW_SCALE_BAR), null, "true", null, "true", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_SHOW_ROOTED), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_ALIGN_TO_SUBTREE), null, "true", null, "true", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_POSITION_LABELS_TO_LEFT), null, "true", null, "true", true, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_LEFT), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_TOP), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_RIGHT), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_BOTTOM), null, "2.0", null, "2.0", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS), null, null, false, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_NAME), null, 
						"info.bioinfweb.treegraph.nodeName", null, "info.bioinfweb.treegraph.nodeName", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_PURPOSE), null, 
						VALUE_LEAVES_ADAPTER, null, VALUE_LEAVES_ADAPTER, true, reader);			
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_NAME), null, 
						"info.bioinfweb.treegraph.voidNodeBranchData", null, "info.bioinfweb.treegraph.voidNodeBranchData", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_PURPOSE), null, 
						VALUE_SUPPORT_VALUES_ADAPTER, null, VALUE_SUPPORT_VALUES_ADAPTER, true, reader);			
				assertEndEvent(EventContentType.META_RESOURCE, reader);
	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, null, null, null, reader);
				
				String node1 = assertNodeEvent(null, "Internal1", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "qy07eczjbe", null, "qy07eczjbe", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node2 = assertNodeEvent(null, "Internal2", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "3gozymvt9n", null, "3gozymvt9n", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node3 = assertNodeEvent(null, "A", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "09q8i4x4s6", null, "09q8i4x4s6", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node4 = assertNodeEvent(null, "B1", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "rggkem0ajy", null, "rggkem0ajy", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
//				
//				String node5 = assertNodeEvent(null, "B2", false, null, false, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "5a39qte2rt", null, "5a39qte2rt", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
//				
//				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
//				assertEndEvent(EventContentType.META_RESOURCE, reader);
//				
//				assertEndEvent(EventContentType.NODE, reader);
//				
//				assertEdgeEvent(node2, node3, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
//				assertEndEvent(EventContentType.EDGE, reader);
//				
//				assertEdgeEvent(node2, node4, 5.6, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
//				assertEndEvent(EventContentType.EDGE, reader);
//				
//				assertEdgeEvent(node2, node5, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
//				assertEndEvent(EventContentType.EDGE, reader);
//				
//				String node6 = assertNodeEvent(null, "C", false, null, false, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "tcyl060pe6", null, "tcyl060pe6", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
//				
//				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
//				assertEndEvent(EventContentType.META_RESOURCE, reader);
//				
//				assertEndEvent(EventContentType.NODE, reader);
//				
//				assertEdgeEvent(node1, node2, 0.7, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
//				assertEndEvent(EventContentType.EDGE, reader);
//				
//				assertEdgeEvent(node1, node6, 1.4, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
//				assertEndEvent(EventContentType.EDGE, reader);				
//				
//				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR), null, null, false, reader);				
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_HEIGHT), null, "4.0", null, "4.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_STYLE), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_COUNTRY), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_VARIANT), null, "", null, "", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_ALIGN), null, "left", null, "left", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TREE_DISTANCE), null, "2.0", null, "2.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_WIDTH), null, "20.0mm", null, "20.0mm", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_HEIGHT), null, "3.0", null, "3.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_SMALL_INTERVAL), null, "10.0", null, "10.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LONG_INTERVAL), null, "10", null, "10", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_START_LEFT), null, "true", null, "true", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_INCREASING), null, "true", null, "true", true, reader);				
//				assertEndEvent(EventContentType.META_RESOURCE, reader);
//				
//				assertEdgeEvent(null, node1, 3.0, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
//				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
//				assertEndEvent(EventContentType.ROOT_EDGE, reader);				
//				
//				assertEndEvent(EventContentType.TREE, reader);
//				
//				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);				
//				
//				assertEndEvent(EventContentType.DOCUMENT, reader);
//				
//				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingPolytomy() {
		try {
			XTGEventReader reader = new XTGEventReader(new File("data/XTG/Polytomy.xml"), new ReadWriteParameterMap());
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);

				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_BG_COLOR), null, "#FFFFFF", null, "#FFFFFF", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_BRANCH_LENGTH_SCALE), null, "0.1", null, "0.1", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_SHOW_SCALE_BAR), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_SHOW_ROOTED), null, "true", null, "true", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_ALIGN_TO_SUBTREE), null, "true", null, "true", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_POSITION_LABELS_TO_LEFT), null, "true", null, "true", true, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_LEFT), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_TOP), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_RIGHT), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_BOTTOM), null, "2.0", null, "2.0", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS), null, null, false, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_NAME), null, 
						"info.bioinfweb.treegraph.nodeName", null, "info.bioinfweb.treegraph.nodeName", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_PURPOSE), null, 
						VALUE_LEAVES_ADAPTER, null, VALUE_LEAVES_ADAPTER, true, reader);			
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_NAME), null, 
						"info.bioinfweb.treegraph.voidNodeBranchData", null, "info.bioinfweb.treegraph.voidNodeBranchData", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_PURPOSE), null, 
						VALUE_SUPPORT_VALUES_ADAPTER, null, VALUE_SUPPORT_VALUES_ADAPTER, true, reader);			
				assertEndEvent(EventContentType.META_RESOURCE, reader);
	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, null, null, null, reader);
				
				String node1 = assertNodeEvent(null, null, false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "p4k85vwcty", null, "p4k85vwcty", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node2 = assertNodeEvent(null, null, false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "pckllkjam2", null, "pckllkjam2", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node3 = assertNodeEvent(null, "A", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "80gir7emve", null, "80gir7emve", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node4 = assertNodeEvent(null, "B1", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "kyfgptfoh5", null, "kyfgptfoh5", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node5 = assertNodeEvent(null, "B2", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "5a39qte2rt", null, "5a39qte2rt", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(node2, node3, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node2, node4, 5.6, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node2, node5, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node6 = assertNodeEvent(null, "C", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_IS_DECIMAL), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_HEIGHT), null, "6.0", null, "6.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_UNIQUE_NAME), null, "tcyl060pe6", null, "tcyl060pe6", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_ATTR_EDGE_RADIUS), null, "1.0", null, "1.0", true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_LEFT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_TOP), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_RIGHT), null, "1.0", null, "1.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM), null, "0.3", null, "0.3", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(node1, node2, 0.7, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node1, node6, 1.4, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
				assertEndEvent(EventContentType.EDGE, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_HEIGHT), null, "4.0", null, "4.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_ALIGN), null, "left", null, "left", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TREE_DISTANCE), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_WIDTH), null, "20.0mm", null, "20.0mm", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_HEIGHT), null, "3.0", null, "3.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_SMALL_INTERVAL), null, "10.0", null, "10.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LONG_INTERVAL), null, "10", null, "10", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_START_LEFT), null, "true", null, "true", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_INCREASING), null, "true", null, "true", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertEdgeEvent(null, node1, 3.0, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_LENGTH), null, "4.0", null, "4.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE), null, "0.0", null, "0.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW), null, "0.0", null, "0.0", true, reader);
				assertEndEvent(EventContentType.ROOT_EDGE, reader);				
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);				
				
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
	
	
	@Test
	public void testReadingEmptyDocument() {
		try {
			XTGEventReader reader = new XTGEventReader(new File("data/XTG/EmptyDocument.xml"), new ReadWriteParameterMap());
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);

				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_BG_COLOR), null, "#FFFFFF", null, "#FFFFFF", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_BRANCH_LENGTH_SCALE), null, "0.1", null, "0.1", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_SHOW_SCALE_BAR), null, "false", null, "false", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_SHOW_ROOTED), null, "true", null, "true", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_ALIGN_TO_SUBTREE), null, "true", null, "true", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ATTR_POSITION_LABELS_TO_LEFT), null, "true", null, "true", true, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_LEFT), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_TOP), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_RIGHT), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN_ATTR_BOTTOM), null, "2.0", null, "2.0", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS), null, null, false, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_NAME), null, 
						"info.bioinfweb.treegraph.nodeName", null, "info.bioinfweb.treegraph.nodeName", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_PURPOSE), null, 
						VALUE_LEAVES_ADAPTER, null, VALUE_LEAVES_ADAPTER, true, reader);			
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_NAME), null, 
						"info.bioinfweb.treegraph.voidNodeBranchData", null, "info.bioinfweb.treegraph.voidNodeBranchData", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_PURPOSE), null, 
						VALUE_SUPPORT_VALUES_ADAPTER, null, VALUE_SUPPORT_VALUES_ADAPTER, true, reader);			
				assertEndEvent(EventContentType.META_RESOURCE, reader);
	
				assertEndEvent(EventContentType.META_RESOURCE, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, null, null, null, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_HEIGHT), null, "4.0", null, "4.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TEXT_STYLE), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_FONT_FAMILY), null, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_DECIMAL_FORMAT), null, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_LANG), null, "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_COUNTRY), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LOCALE_VARIANT), null, null, null, null, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LINE_COLOR), null, "#000000", null, "#000000", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LINE_WIDTH), null, "0.3", null, "0.3", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_ALIGN), null, "left", null, "left", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_TREE_DISTANCE), null, "2.0", null, "2.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_WIDTH), null, "20.0mm", null, "20.0mm", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_HEIGHT), null, "3.0", null, "3.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_SMALL_INTERVAL), null, "10.0", null, "10.0", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_LONG_INTERVAL), null, "10", null, "10", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_START_LEFT), null, "true", null, "true", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR_ATTR_INCREASING), null, "true", null, "true", true, reader);				
				assertEndEvent(EventContentType.META_RESOURCE, reader);			
				
				assertEndEvent(EventContentType.TREE, reader);
				
				assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);				
				
				assertEndEvent(EventContentType.DOCUMENT, reader);
				
				assertFalse(reader.hasNextEvent());
			}
			finally {
				reader.close();
			}			
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getLocalizedMessage());
		}
	}
}
