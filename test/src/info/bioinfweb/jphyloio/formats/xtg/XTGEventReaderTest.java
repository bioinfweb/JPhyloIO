/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.formats.xtg;


import static info.bioinfweb.jphyloio.test.JPhyloIOTestTools.*;
import static org.junit.Assert.*;
import info.bioinfweb.commons.io.W3CXSConstants;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.meta.URIOrStringIdentifier;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;

import java.awt.Color;
import java.io.File;

import org.junit.Test;



public class XTGEventReaderTest implements XTGConstants {
	
	
	@Test
	public void testReadingTree() throws Exception {
		XTGEventReader reader = new XTGEventReader(new File("data/XTG/ExampleXTGDocument.xml"), new ReadWriteParameterMap());
		
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);

			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_BG_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#FFFFFF", null, Color.decode("#FFFFFF"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_BRANCH_LENGTH_SCALE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.1", null, 0.1, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_SHOW_SCALE_BAR), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ALIGN_TO_SUBTREE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_POSITION_LABELS_TO_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);				
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS), null, null, false, reader);				
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					"info.bioinfweb.treegraph.nodeName", null, "info.bioinfweb.treegraph.nodeName", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_PURPOSE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					VALUE_LEAVES_ADAPTER, null, VALUE_LEAVES_ADAPTER, true, reader);			
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					"info.bioinfweb.treegraph.voidNodeBranchData", null, "info.bioinfweb.treegraph.voidNodeBranchData", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_PURPOSE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					VALUE_SUPPORT_VALUES_ADAPTER, null, VALUE_SUPPORT_VALUES_ADAPTER, true, reader);			
			assertEndEvent(EventContentType.RESOURCE_META, reader);

			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.TREE, null, null, null, reader);
			
			String node1 = assertNodeEvent(null, "Internal1", false, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "qy07eczjbe", null, "qy07eczjbe", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.NODE, reader);
			
			String node2 = assertNodeEvent(null, "Internal2", false, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "3gozymvt9n", null, "3gozymvt9n", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_INVISIBLE_DATA), null, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "9.4", null, 9.4, true, reader);			
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLUMN_ID), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "ID2", null, "ID2", true, reader);
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.NODE, reader);
			
			String node3 = assertNodeEvent(null, "A", false, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "09q8i4x4s6", null, "09q8i4x4s6", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.NODE, reader);
			
			String node4 = assertNodeEvent(null, "B", false, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "rggkem0ajy", null, "rggkem0ajy", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.NODE, reader);
	
			assertEdgeEvent(node2, node3, 5.0, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(node2, node4, 3.0, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_LABEL), null, null, false, reader);		
			
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "test", null, "test", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLUMN_ID), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "ID1", null, "ID1", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_ABOVE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_LINE_NO), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), "0", null, 0, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_LINE_POS), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.0", null, 0.0, true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.EDGE, reader);
			
			String node5 = assertNodeEvent(null, "C", false, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "iq80ulayeq", null, "iq80ulayeq", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_INVISIBLE_DATA), null, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "test invisible data", null, "test invisible data", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLUMN_ID), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "ID2", null, "ID2", true, reader);
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.NODE, reader);
			
			assertEdgeEvent(node1, node2, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PIE_CHART_LABEL), null, null, false, reader);
			
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "3.0", null, new Float(3.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_HEIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "3.0", null, new Float(3.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PIE_CHART_LABEL_INTERNAL_LINES), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PIE_CHART_LABEL_NULL_LINES), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PIE_CHART_LABEL_SHOW_TITLE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
			assertLiteralMetaEvent(PREDICATE_PIE_CHART_LABEL_CAPTION_TYPE, DATA_TYPE_PIE_CHART_LABEL_CAPTION_TYPE, 
					"NONE", null, null, true, reader);
			assertLiteralMetaEvent(PREDICATE_PIE_CHART_LABEL_CAPTION_LINK_TYPE, DATA_TYPE_PIE_CHART_LABEL_CAPTION_LINK_TYPE, 
					"STRAIGHT_LINES", null, null, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLUMN_ID), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "ID1", null, "ID1", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_ABOVE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_LINE_NO), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), "0", null, 0, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_LINE_POS), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.0", null, 0.0, true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DATA_IDS), null, null, false, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DATA_ID), null, null, false, reader);	
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PIE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#0000FF", null, Color.decode("#0000FF"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DATA_ID_VALUE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "ID2", null, null, true, reader);
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DATA_ID), null, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_PIE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#FF0000", null, Color.decode("#FF0000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DATA_ID_VALUE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "ID3", null, null, true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_INVISIBLE_DATA), null, null, false, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "18.3", null, 18.3, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLUMN_ID), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "ID3", null, "ID3", true, reader);
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.EDGE, reader);
			
			assertEdgeEvent(node1, node5, 7.0, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_ICON_LABEL), null, null, false, reader);		
			
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#FF9900", null, Color.decode("#FF9900"), true, reader);
			assertLiteralMetaEvent(PREDICATE_LINE_WIDTH, W3CXSConstants.DATA_TYPE_FLOAT, "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "3.0", null, new Float(3.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_HEIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "3.0", null, new Float(3.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_ICON_LABEL_ICON), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Star", null, "Star", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_ICON_LABEL_ICON_FILLED), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_COLUMN_ID), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "ID1", null, "ID1", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_ABOVE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_LINE_NO), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_INT), "0", null, 0, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_LINE_POS), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.0", null, 0.0, true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LABEL_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertEndEvent(EventContentType.EDGE, reader);				
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR), null, null, false, reader);				
			assertLiteralMetaEvent(PREDICATE_TEXT_COLOR, DATA_TYPE_COLOR, "#000000", null, Color.BLACK, true, reader);
			assertLiteralMetaEvent(PREDICATE_TEXT_HEIGHT, W3CXSConstants.DATA_TYPE_FLOAT, "4.0", null, new Float(4), true, reader);
			assertLiteralMetaEvent(PREDICATE_TEXT_STYLE, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_FONT_FAMILY, W3CXSConstants.DATA_TYPE_STRING, "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(PREDICATE_DECIMAL_FORMAT, W3CXSConstants.DATA_TYPE_STRING, "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_LANG, W3CXSConstants.DATA_TYPE_STRING, "en", null, "en", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_COUNTRY, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_VARIANT, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_LINE_COLOR, DATA_TYPE_COLOR, "#000000", null, Color.BLACK, true, reader);
			assertLiteralMetaEvent(PREDICATE_LINE_WIDTH, W3CXSConstants.DATA_TYPE_FLOAT, "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_ALIGN, DATA_TYPE_SCALE_BAR_ALIGNMENT, "left", null, null, true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_TREE_DISTANCE, W3CXSConstants.DATA_TYPE_FLOAT, "3.0", null, new Float(3.0), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_START_LEFT, W3CXSConstants.DATA_TYPE_BOOLEAN, "true", null, new Boolean(true), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_INCREASING, W3CXSConstants.DATA_TYPE_BOOLEAN, "true", null, new Boolean(true), true, reader);				
			assertLiteralMetaEvent(PREDICATE_WIDTH, DATA_TYPE_SCALE_VALUE, "20.0mm", null, null, true, reader);  // Object value is null as long as not translator is defined.
			assertLiteralMetaEvent(PREDICATE_HEIGHT, W3CXSConstants.DATA_TYPE_FLOAT, "3.0", null, new Float(3), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_LONG_INTERVAL, W3CXSConstants.DATA_TYPE_INT, "10", null, new Integer(10), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_SMALL_INTERVAL, W3CXSConstants.DATA_TYPE_FLOAT, "10.0", null, new Float(10), true, reader);
			assertEndEvent(EventContentType.RESOURCE_META, reader);			

			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEGEND), null, null, false, reader);				
			assertLiteralMetaEvent(PREDICATE_TEXT, W3CXSConstants.DATA_TYPE_STRING, "L", null, "L", true, reader);
			//assertLiteralMetaEvent(PREDICATE_IS_DECIMAL, W3CXSConstants.DATA_TYPE_BOOLEAN, "false", null, new Boolean(false), true, reader);
			assertLiteralMetaEvent(PREDICATE_TEXT_COLOR, DATA_TYPE_COLOR, "#000000", null, Color.BLACK, true, reader);
			assertLiteralMetaEvent(PREDICATE_TEXT_HEIGHT, W3CXSConstants.DATA_TYPE_FLOAT, "6.0", null, new Float(6), true, reader);
			assertLiteralMetaEvent(PREDICATE_TEXT_STYLE, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_FONT_FAMILY, W3CXSConstants.DATA_TYPE_STRING, "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(PREDICATE_DECIMAL_FORMAT, W3CXSConstants.DATA_TYPE_STRING, "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_LANG, W3CXSConstants.DATA_TYPE_STRING, "en", null, "en", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_COUNTRY, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_VARIANT, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_LINE_COLOR, DATA_TYPE_COLOR, "#000000", null, Color.BLACK, true, reader);
			assertLiteralMetaEvent(PREDICATE_LINE_WIDTH, W3CXSConstants.DATA_TYPE_FLOAT, "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(PREDICATE_LEGEND_POSITION, W3CXSConstants.DATA_TYPE_INT, "0", null, new Integer(0), true, reader);
			assertLiteralMetaEvent(PREDICATE_LEGEND_MIN_TREE_DISTANCE, W3CXSConstants.DATA_TYPE_FLOAT, "0.0", null, new Float(0), true, reader);
			assertLiteralMetaEvent(PREDICATE_LEGEND_SPACING, W3CXSConstants.DATA_TYPE_FLOAT, "1.0", null, new Float(1), true, reader);
			assertLiteralMetaEvent(PREDICATE_LEGEND_STYLE, DATA_TYPE_LEGEND_STYLE, "bracket", null, null, true, reader);  //TODO In TG-NeXML this is in capital letters. Would the object translator work here anyway?
			assertLiteralMetaEvent(PREDICATE_LEGEND_ORIENTATION, DATA_TYPE_TEXT_ORIENTATION, "horizontal", null, null, true, reader);  //TODO In TG-NeXML this is in capital letters. Would the object translator work here anyway?
			assertLiteralMetaEvent(PREDICATE_EDGE_RADIUS, W3CXSConstants.DATA_TYPE_FLOAT, "1.0", null, new Float(1.0), true, reader);
			assertLiteralMetaEvent(PREDICATE_LEGEND_ANCHOR_0, W3CXSConstants.DATA_TYPE_STRING, "rggkem0ajy", null, "rggkem0ajy", true, reader);
			assertLiteralMetaEvent(PREDICATE_LEGEND_ANCHOR_1, W3CXSConstants.DATA_TYPE_STRING, "iq80ulayeq", null, "iq80ulayeq", true, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEGEND_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);

			assertEndEvent(EventContentType.RESOURCE_META, reader);			
			
			assertEdgeEvent(null, node1, 2.0, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
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
	
	
	@Test
	public void testReadingPolytomy() {
		try {
			XTGEventReader reader = new XTGEventReader(new File("data/XTG/Polytomy.xml"), new ReadWriteParameterMap());
			
			try {
				assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);

				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_BG_COLOR), new URIOrStringIdentifier(null, DATA_TYPE_COLOR), 
						"#FFFFFF", null, Color.decode("#FFFFFF"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_BRANCH_LENGTH_SCALE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.1", null, 0.1, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_SHOW_SCALE_BAR), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ALIGN_TO_SUBTREE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_POSITION_LABELS_TO_LEFT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS), null, null, false, reader);				
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_NAME), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
						"info.bioinfweb.treegraph.nodeName", null, "info.bioinfweb.treegraph.nodeName", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_PURPOSE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
						VALUE_LEAVES_ADAPTER, null, VALUE_LEAVES_ADAPTER, true, reader);			
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_NAME), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
						"info.bioinfweb.treegraph.voidNodeBranchData", null, "info.bioinfweb.treegraph.voidNodeBranchData", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_PURPOSE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
						VALUE_SUPPORT_VALUES_ADAPTER, null, VALUE_SUPPORT_VALUES_ADAPTER, true, reader);			
				assertEndEvent(EventContentType.RESOURCE_META, reader);
	
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
				
				assertLinkedLabeledIDEvent(EventContentType.TREE, null, null, null, reader);
				
				String node1 = assertNodeEvent(null, null, true, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "p4k85vwcty", null, "p4k85vwcty", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node2 = assertNodeEvent(null, null, false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "pckllkjam2", null, "pckllkjam2", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node3 = assertNodeEvent(null, "A", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "80gir7emve", null, "80gir7emve", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node4 = assertNodeEvent(null, "B1", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "kyfgptfoh5", null, "kyfgptfoh5", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				String node5 = assertNodeEvent(null, "B2", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "5a39qte2rt", null, "5a39qte2rt", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(node2, node3, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node2, node4, 5.6, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node2, node5, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				String node6 = assertNodeEvent(null, "C", false, null, false, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_IS_DECIMAL), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_HEIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "6.0", null, new Float(6.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_TEXT_STYLE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_FONT_FAMILY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DECIMAL_FORMAT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_LANG), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "en", null, "en", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_COUNTRY), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LOCALE_VARIANT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "", null, "", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_UNIQUE_NAME), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), "tcyl060pe6", null, "tcyl060pe6", true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_EDGE_RADIUS), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LEAF_MARGIN), null, null, false, reader);				
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "1.0", null, new Float(1.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);				
				assertEndEvent(EventContentType.RESOURCE_META, reader);
				
				assertEndEvent(EventContentType.NODE, reader);
				
				assertEdgeEvent(node1, node2, 0.7, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertEdgeEvent(node1, node6, 1.4, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertEndEvent(EventContentType.EDGE, reader);
				
				assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR), null, null, false, reader);				
				assertLiteralMetaEvent(PREDICATE_TEXT_COLOR, DATA_TYPE_COLOR, "#000000", null, Color.BLACK, true, reader);
				assertLiteralMetaEvent(PREDICATE_TEXT_HEIGHT, W3CXSConstants.DATA_TYPE_FLOAT, "4.0", null, new Float(4), true, reader);
				assertLiteralMetaEvent(PREDICATE_TEXT_STYLE, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
				assertLiteralMetaEvent(PREDICATE_FONT_FAMILY, W3CXSConstants.DATA_TYPE_STRING, "Arial", null, "Arial", true, reader);
				assertLiteralMetaEvent(PREDICATE_DECIMAL_FORMAT, W3CXSConstants.DATA_TYPE_STRING, "#0.0#####", null, "#0.0#####", true, reader);
				assertLiteralMetaEvent(PREDICATE_LOCALE_LANG, W3CXSConstants.DATA_TYPE_STRING, "en", null, "en", true, reader);
				assertLiteralMetaEvent(PREDICATE_LOCALE_COUNTRY, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
				assertLiteralMetaEvent(PREDICATE_LOCALE_VARIANT, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
				assertLiteralMetaEvent(PREDICATE_LINE_COLOR, DATA_TYPE_COLOR, "#000000", null, Color.BLACK, true, reader);
				assertLiteralMetaEvent(PREDICATE_LINE_WIDTH, W3CXSConstants.DATA_TYPE_FLOAT, "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(PREDICATE_SCALE_BAR_ALIGN, DATA_TYPE_SCALE_BAR_ALIGNMENT, "left", null, null, true, reader);
				assertLiteralMetaEvent(PREDICATE_SCALE_BAR_TREE_DISTANCE, W3CXSConstants.DATA_TYPE_FLOAT, "2.0", null, new Float(2.0), true, reader);
				assertLiteralMetaEvent(PREDICATE_SCALE_BAR_START_LEFT, W3CXSConstants.DATA_TYPE_BOOLEAN, "true", null, new Boolean(true), true, reader);
				assertLiteralMetaEvent(PREDICATE_SCALE_BAR_INCREASING, W3CXSConstants.DATA_TYPE_BOOLEAN, "true", null, new Boolean(true), true, reader);				
				assertLiteralMetaEvent(PREDICATE_WIDTH, DATA_TYPE_SCALE_VALUE, "20.0mm", null, null, true, reader);  // Object value is null as long as not translator is defined.
				assertLiteralMetaEvent(PREDICATE_HEIGHT, W3CXSConstants.DATA_TYPE_FLOAT, "3.0", null, new Float(3), true, reader);
				assertLiteralMetaEvent(PREDICATE_SCALE_BAR_LONG_INTERVAL, W3CXSConstants.DATA_TYPE_INT, "10", null, new Integer(10), true, reader);
				assertLiteralMetaEvent(PREDICATE_SCALE_BAR_SMALL_INTERVAL, W3CXSConstants.DATA_TYPE_FLOAT, "10.0", null, new Float(10), true, reader);
				assertEndEvent(EventContentType.RESOURCE_META, reader);			

				assertEdgeEvent(null, node1, 3.0, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_COLOR), 
						new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#000000", null, Color.decode("#000000"), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_LINE_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.3", null, new Float(0.3), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_CONSTANT_WIDTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_LENGTH), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "4.0", null, new Float(4.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_ABOVE), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
				assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_BRANCH_MIN_SPACE_BELOW), 
						new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "0.0", null, new Float(0.0), true, reader);
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
	public void testReadingEmptyDocument() throws Exception {
		XTGEventReader reader = new XTGEventReader(new File("data/XTG/EmptyDocument.xml"), new ReadWriteParameterMap());
		
		try {
			assertEventType(EventContentType.DOCUMENT, EventTopologyType.START, reader);

			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_BG_COLOR), 
					new URIOrStringIdentifier(null, DATA_TYPE_COLOR), "#FFFFFF", null, Color.decode("#FFFFFF"), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_BRANCH_LENGTH_SCALE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_DOUBLE), "0.1", null, 0.1, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_SHOW_SCALE_BAR), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "false", null, false, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_ALIGN_TO_SUBTREE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_GLOBAL_FORMATS_POSITION_LABELS_TO_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_BOOLEAN), "true", null, true, true, reader);				
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_DOCUMENT_MARGIN), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_LEFT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_TOP), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_RIGHT), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_MARGIN_BOTTOM), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_FLOAT), "2.0", null, new Float(2.0), true, reader);				
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS), null, null, false, reader);				
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					"info.bioinfweb.treegraph.nodeName", null, "info.bioinfweb.treegraph.nodeName", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_PURPOSE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					VALUE_LEAVES_ADAPTER, null, VALUE_LEAVES_ADAPTER, true, reader);			
			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER), null, null, false, reader);				
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_NAME), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					"info.bioinfweb.treegraph.voidNodeBranchData", null, "info.bioinfweb.treegraph.voidNodeBranchData", true, reader);
			assertLiteralMetaEvent(new URIOrStringIdentifier(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_PURPOSE), 
					new URIOrStringIdentifier(null, W3CXSConstants.DATA_TYPE_STRING), 
					VALUE_SUPPORT_VALUES_ADAPTER, null, VALUE_SUPPORT_VALUES_ADAPTER, true, reader);			
			assertEndEvent(EventContentType.RESOURCE_META, reader);

			assertEndEvent(EventContentType.RESOURCE_META, reader);
			
			assertLinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, null, null, null, reader);
			assertLinkedLabeledIDEvent(EventContentType.TREE, null, null, null, reader);
			
			assertResourceMetaEvent(new URIOrStringIdentifier(null, PREDICATE_SCALE_BAR), null, null, false, reader);				
			assertLiteralMetaEvent(PREDICATE_TEXT_COLOR, DATA_TYPE_COLOR, "#000000", null, Color.BLACK, true, reader);
			assertLiteralMetaEvent(PREDICATE_TEXT_HEIGHT, W3CXSConstants.DATA_TYPE_FLOAT, "4.0", null, new Float(4), true, reader);
			assertLiteralMetaEvent(PREDICATE_TEXT_STYLE, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_FONT_FAMILY, W3CXSConstants.DATA_TYPE_STRING, "Arial", null, "Arial", true, reader);
			assertLiteralMetaEvent(PREDICATE_DECIMAL_FORMAT, W3CXSConstants.DATA_TYPE_STRING, "#0.0#####", null, "#0.0#####", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_LANG, W3CXSConstants.DATA_TYPE_STRING, "en", null, "en", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_COUNTRY, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_LOCALE_VARIANT, W3CXSConstants.DATA_TYPE_STRING, "", null, "", true, reader);
			assertLiteralMetaEvent(PREDICATE_LINE_COLOR, DATA_TYPE_COLOR, "#000000", null, Color.BLACK, true, reader);
			assertLiteralMetaEvent(PREDICATE_LINE_WIDTH, W3CXSConstants.DATA_TYPE_FLOAT, "0.3", null, new Float(0.3), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_ALIGN, DATA_TYPE_SCALE_BAR_ALIGNMENT, "left", null, null, true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_TREE_DISTANCE, W3CXSConstants.DATA_TYPE_FLOAT, "2.0", null, new Float(2.0), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_START_LEFT, W3CXSConstants.DATA_TYPE_BOOLEAN, "true", null, new Boolean(true), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_INCREASING, W3CXSConstants.DATA_TYPE_BOOLEAN, "true", null, new Boolean(true), true, reader);				
			assertLiteralMetaEvent(PREDICATE_WIDTH, DATA_TYPE_SCALE_VALUE, "20.0mm", null, null, true, reader);  // Object value is null as long as not translator is defined.
			assertLiteralMetaEvent(PREDICATE_HEIGHT, W3CXSConstants.DATA_TYPE_FLOAT, "3.0", null, new Float(3), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_LONG_INTERVAL, W3CXSConstants.DATA_TYPE_INT, "10", null, new Integer(10), true, reader);
			assertLiteralMetaEvent(PREDICATE_SCALE_BAR_SMALL_INTERVAL, W3CXSConstants.DATA_TYPE_FLOAT, "10.0", null, new Float(10), true, reader);
			assertEndEvent(EventContentType.RESOURCE_META, reader);			
			
			assertEndEvent(EventContentType.TREE, reader);
			assertEndEvent(EventContentType.TREE_NETWORK_GROUP, reader);				
			
			assertEndEvent(EventContentType.DOCUMENT, reader);
			
			assertFalse(reader.hasNextEvent());
		}
		finally {
			reader.close();
		}			
	}
}