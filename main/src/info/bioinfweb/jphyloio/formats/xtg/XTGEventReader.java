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


import info.bioinfweb.commons.io.XMLUtils;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.EdgeEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.LinkedLabeledIDEvent;
import info.bioinfweb.jphyloio.events.NodeEvent;
import info.bioinfweb.jphyloio.events.meta.LiteralMetadataContentEvent;
import info.bioinfweb.jphyloio.events.type.EventContentType;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.formats.JPhyloIOFormatIDs;
import info.bioinfweb.jphyloio.formats.NodeEdgeInfo;
import info.bioinfweb.jphyloio.formats.xml.AbstractXMLEventReader;
import info.bioinfweb.jphyloio.formats.xml.XMLReaderStreamDataProvider;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.AbstractXMLElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.CommentElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.XMLElementReaderKey;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.XMLEndElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.XMLNoCharactersAllowedElementReader;
import info.bioinfweb.jphyloio.formats.xml.elementreaders.XMLStartElementReader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;



/**
 * Event reader for the <a href="http://bioinfweb.info/xmlns/xtg">extensible TreeGraph 2 format (XTG)</a> used by the 
 * phylogenetic tree editor <a href="http://treegraph.bioinfweb.info/">TreeGraph 2</a>.
 * 
 * <h3><a name="parameters"></a>Recognized parameters</h3> 
 * <ul>
 *   <li>{@link ReadWriteParameterMap#KEY_ALLOW_DEFAULT_NAMESPACE}</li>
 *   <li>{@link ReadWriteParameterMap#KEY_LOGGER}</li>
 * </ul>
 * 
 * @author Sarah Wiechers
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class XTGEventReader extends AbstractXMLEventReader<XMLReaderStreamDataProvider<XTGEventReader>> implements XTGConstants {	
	public XTGEventReader(File file, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(file, parameters);
	}


	public XTGEventReader(InputStream stream, ReadWriteParameterMap parameters)	throws IOException, XMLStreamException {
		super(stream, parameters);
	}


	public XTGEventReader(Reader reader, ReadWriteParameterMap parameters) throws IOException, XMLStreamException {
		super(reader, parameters);
	}


	public XTGEventReader(XMLEventReader xmlReader,	ReadWriteParameterMap parameters) {
		super(xmlReader, parameters);
	}


	@Override
	public String getFormatID() {
		return JPhyloIOFormatIDs.XTG_FORMAT_ID;
	}


	@SuppressWarnings("unchecked")
	@Override
	protected void fillMap() {
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> nodeStartReader = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();				
				String id = DEFAULT_NODE_ID_PREFIX + streamDataProvider.getIDManager().createNewID();
				String label = XMLUtils.readStringAttr(element, ATTR_TEXT, null);
				
				createNodeEvents(streamDataProvider);  // Create node events for previous node to avoid buffering large amounts of meta data
				
				// Add node info for this node
				NodeEdgeInfo nodeInfo = new NodeEdgeInfo(id, Double.NaN, new ArrayList<JPhyloIOEvent>(), new ArrayList<JPhyloIOEvent>());				
				if ((label != null) && !label.isEmpty()) {
					nodeInfo.setLabel(label); //TODO Add rooted information
				}
				streamDataProvider.getSourceNode().add(nodeInfo);
				streamDataProvider.setCreateNodeStart(true);
				
				// Add edge info for this level
				streamDataProvider.getEdgeInfos().add(new ArrayDeque<NodeEdgeInfo>());
				
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedNodeEvents());
				
				readAttributes(getStreamDataProvider(), event.asStartElement(), "",	ATTR_TEXT_IS_DECIMAL, PREDICATE_NODE_ATTR_IS_DECIMAL, 
						ATTR_TEXT_COLOR, PREDICATE_NODE_ATTR_TEXT_COLOR, ATTR_TEXT_HEIGHT, PREDICATE_NODE_ATTR_TEXT_HEIGHT, 
						ATTR_TEXT_STYLE, PREDICATE_NODE_ATTR_TEXT_STYLE, ATTR_FONT_FAMILY, PREDICATE_NODE_ATTR_FONT_FAMILY, 
						ATTR_DECIMAL_FORMAT, PREDICATE_NODE_ATTR_DECIMAL_FORMAT, ATTR_LOCALE_LANG, PREDICATE_NODE_ATTR_LOCALE_LANG, 
						ATTR_LOCALE_COUNTRY, PREDICATE_NODE_ATTR_LOCALE_COUNTRY, ATTR_LOCALE_VARIANT, PREDICATE_NODE_ATTR_LOCALE_VARIANT,
						ATTR_LINE_COLOR, PREDICATE_NODE_ATTR_LINE_COLOR, ATTR_LINE_WIDTH, PREDICATE_NODE_ATTR_LINE_WIDTH, 
						ATTR_UNIQUE_NAME, PREDICATE_NODE_ATTR_UNIQUE_NAME, ATTR_EDGE_RADIUS, PREDICATE_NODE_ATTR_EDGE_RADIUS);
			}
		};
		
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> nodeEndReader = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				createNodeEvents(streamDataProvider);
				streamDataProvider.setCreateNodeStart(false);
				
				createEdgeEvents(streamDataProvider);
				
				streamDataProvider.getSourceNode().pop();
			}
		};
		
		XMLEndElementReader resourceEndReader = new XMLEndElementReader(false, true, false);
		
		XMLEndElementReader edgeResourceEndReader = new XMLEndElementReader(false, true, true);
		
		XMLStartElementReader labelMarginStartReader = new XMLStartElementReader(null, PREDICATE_LABEL_MARGIN, null, false, 
				ATTR_LEFT, PREDICATE_LABEL_MARGIN_ATTR_LEFT, ATTR_TOP, PREDICATE_LABEL_MARGIN_ATTR_TOP, 
				ATTR_RIGHT, PREDICATE_LABEL_MARGIN_ATTR_RIGHT, ATTR_BOTTOM, PREDICATE_LABEL_MARGIN_ATTR_BOTTOM);
		
		putElementReader(new XMLElementReaderKey(TAG_LABEL_MARGIN, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>> emptyReader = new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {}
		};
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.START_DOCUMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(EventContentType.DOCUMENT, EventTopologyType.START));
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.END_DOCUMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.DOCUMENT));
			}
		});
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.START_ELEMENT), emptyReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(null, TAG_ROOT, XMLStreamConstants.END_ELEMENT), emptyReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_TREE, XMLStreamConstants.START_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getEdgeInfos().add(new ArrayDeque<NodeEdgeInfo>());
				streamDataProvider.setCreateNodeStart(false);  // Prevents creation of node events at start of root node
				
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE_NETWORK_GROUP, 
						DEFAULT_TREE_NETWORK_GROUP_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, null));  // Since there can only be one tree in an XTG document, the tree group start event can be fired here
				
				streamDataProvider.getCurrentEventCollection().add(new LinkedLabeledIDEvent(EventContentType.TREE, 
						DEFAULT_TREE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, null));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREE, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_TREE, XMLStreamConstants.END_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				createEdgeEvents(streamDataProvider);
				
				streamDataProvider.getSourceNode().clear();
				streamDataProvider.getEdgeInfos().clear();
				
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE));
				
				streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.TREE_NETWORK_GROUP));  // Since there can only be one tree in an XTG document, the tree group end event can be fired here
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		
		putElementReader(new XMLElementReaderKey(TAG_NODE, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.START_ELEMENT), nodeStartReader);
		
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_NODE, XMLStreamConstants.END_ELEMENT), nodeEndReader);

		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.START_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				StartElement element = event.asStartElement();	
				
				streamDataProvider.getSourceNode().peek().setLength(XMLUtils.readDoubleAttr(element, ATTR_BRANCH_LENGTH, Double.NaN));
				streamDataProvider.setCurrentEventCollection(streamDataProvider.getSourceNode().peek().getNestedEdgeEvents());
				
				readAttributes(getStreamDataProvider(), event.asStartElement(), "", ATTR_LINE_COLOR, PREDICATE_BRANCH_ATTR_LINE_COLOR, 
						ATTR_LINE_WIDTH, PREDICATE_BRANCH_ATTR_LINE_WIDTH, ATTR_CONSTANT_WIDTH, PREDICATE_BRANCH_ATTR_CONSTANT_WIDTH, 
						ATTR_MIN_BRANCH_LENGTH, PREDICATE_BRANCH_ATTR_MIN_LENGTH, ATTR_MIN_SPACE_ABOVE, PREDICATE_BRANCH_ATTR_MIN_SPACE_ABOVE, 
						ATTR_MIN_SPACE_BELOW, PREDICATE_BRANCH_ATTR_MIN_SPACE_BELOW);
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_BRANCH, XMLStreamConstants.END_ELEMENT), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {				
				if (streamDataProvider.hasSpecialEventCollection()) {
					streamDataProvider.resetCurrentEventCollection();
				}
			}
		});
		
		// TreegraphDocument.GlobalFormats
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_GLOBAL_FORMATS, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_GLOBAL_FORMATS, null,	false, ATTR_BG_COLOR, PREDICATE_GLOBAL_FORMATS_ATTR_BG_COLOR, 
						ATTR_BRANCH_LENGTH_SCALE, PREDICATE_GLOBAL_FORMATS_ATTR_BRANCH_LENGTH_SCALE, ATTR_SHOW_SCALE_BAR, PREDICATE_GLOBAL_FORMATS_ATTR_SHOW_SCALE_BAR,
						ATTR_SHOW_ROOTED, PREDICATE_GLOBAL_FORMATS_ATTR_SHOW_ROOTED, ATTR_ALIGN_TO_SUBTREE, PREDICATE_GLOBAL_FORMATS_ATTR_ALIGN_TO_SUBTREE, 
						ATTR_POSITION_LABELS_TO_LEFT, PREDICATE_GLOBAL_FORMATS_ATTR_POSITION_LABELS_TO_LEFT));		
		putElementReader(new XMLElementReaderKey(TAG_GLOBAL_FORMATS, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_GLOBAL_FORMATS, TAG_DOCUMENT_MARGIN, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_DOCUMENT_MARGIN, null, false, ATTR_LEFT, PREDICATE_DOCUMENT_MARGIN_ATTR_LEFT, 
						ATTR_TOP, PREDICATE_DOCUMENT_MARGIN_ATTR_TOP, ATTR_RIGHT, PREDICATE_DOCUMENT_MARGIN_ATTR_RIGHT, 
						ATTR_BOTTOM, PREDICATE_DOCUMENT_MARGIN_ATTR_BOTTOM));		
		putElementReader(new XMLElementReaderKey(TAG_DOCUMENT_MARGIN, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());		
		putElementReader(new XMLElementReaderKey(TAG_GLOBAL_FORMATS, TAG_DOCUMENT_MARGIN, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_GLOBAL_FORMATS, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		// TreegraphDocument.NodeBranchDataAdapters
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_NODE_BRANCH_DATA_ADAPTERS, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS, null,	false));		
		putElementReader(new XMLElementReaderKey(TAG_NODE_BRANCH_DATA_ADAPTERS, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_NODE_BRANCH_DATA_ADAPTERS, TAG_ADAPTER, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER, null, false, 
						ATTR_ADAPTER_NAME, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_NAME, ATTR_ADAPTER_ID, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_ID,
						ATTR_ADAPTER_PURPOSE, PREDICATE_NODE_BRANCH_DATA_ADAPTERS_ADAPTER_ATTR_PURPOSE));	
		putElementReader(new XMLElementReaderKey(TAG_ADAPTER, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());		
		putElementReader(new XMLElementReaderKey(TAG_NODE_BRANCH_DATA_ADAPTERS, TAG_ADAPTER, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_ROOT, TAG_NODE_BRANCH_DATA_ADAPTERS, XMLStreamConstants.END_ELEMENT), resourceEndReader);
	
//		// Tree.ScaleBar
//		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_SCALE_BAR, XMLStreamConstants.START_ELEMENT), 
//				new XMLStartElementReader(null, PREDICATE_SCALE_BAR, null,	false, ATTR_TEXT, PREDICATE_SCALE_BAR_ATTR_TEXT, 
//						ATTR_TEXT_IS_DECIMAL, PREDICATE_SCALE_BAR_ATTR_IS_DECIMAL, ATTR_TEXT_COLOR, PREDICATE_SCALE_BAR_ATTR_TEXT_COLOR, 
//						ATTR_TEXT_HEIGHT, PREDICATE_SCALE_BAR_ATTR_TEXT_HEIGHT, ATTR_TEXT_STYLE, PREDICATE_SCALE_BAR_ATTR_TEXT_STYLE, 
//						ATTR_FONT_FAMILY, PREDICATE_SCALE_BAR_ATTR_FONT_FAMILY, ATTR_DECIMAL_FORMAT, PREDICATE_SCALE_BAR_ATTR_DECIMAL_FORMAT, 
//						ATTR_LOCALE_LANG, PREDICATE_SCALE_BAR_ATTR_LOCALE_LANG, ATTR_LOCALE_COUNTRY, PREDICATE_SCALE_BAR_ATTR_LOCALE_COUNTRY, 
//						ATTR_LOCALE_VARIANT, PREDICATE_SCALE_BAR_ATTR_LOCALE_VARIANT, ATTR_LINE_COLOR, PREDICATE_SCALE_BAR_ATTR_LINE_COLOR, 
//						ATTR_LINE_WIDTH, PREDICATE_SCALE_BAR_ATTR_LINE_WIDTH, ATTR_SCALE_BAR_ALIGN, PREDICATE_SCALE_BAR_ATTR_ALIGN, 
//						ATTR_SCALE_BAR_DISTANCE, PREDICATE_SCALE_BAR_ATTR_TREE_DISTANCE, ATTR_SCALE_BAR_WIDTH, PREDICATE_SCALE_BAR_ATTR_WIDTH, 
//						ATTR_SCALE_BAR_HEIGHT, PREDICATE_SCALE_BAR_ATTR_HEIGHT, ATTR_SMALL_INTERVAL, PREDICATE_SCALE_BAR_ATTR_SMALL_INTERVAL, 
//						ATTR_LONG_INTERVAL, PREDICATE_SCALE_BAR_ATTR_LONG_INTERVAL, ATTR_SCALE_BAR_START, PREDICATE_SCALE_BAR_ATTR_START_LEFT, 
//						ATTR_SCALE_BAR_INCREASE, PREDICATE_SCALE_BAR_ATTR_INCREASING));
//		putElementReader(new XMLElementReaderKey(TAG_SCALE_BAR, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());		
//		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_SCALE_BAR, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		// Node.LeafMargin
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_LEAF_MARGIN, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_LEAF_MARGIN, null, false, ATTR_LEFT, PREDICATE_LEAF_MARGIN_ATTR_LEFT, 
						ATTR_TOP, PREDICATE_LEAF_MARGIN_ATTR_TOP, ATTR_RIGHT, PREDICATE_LEAF_MARGIN_ATTR_RIGHT, 
						ATTR_BOTTOM, PREDICATE_LEAF_MARGIN_ATTR_BOTTOM));		
		putElementReader(new XMLElementReaderKey(TAG_LEAF_MARGIN, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());		
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_LEAF_MARGIN, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		// Branch.TextLabel
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_TEXT_LABEL, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_TEXT_LABEL, null,	true, ATTR_TEXT, PREDICATE_TEXT_LABEL_ATTR_TEXT, 
						ATTR_TEXT_IS_DECIMAL, PREDICATE_TEXT_LABEL_ATTR_IS_DECIMAL, ATTR_TEXT_COLOR, PREDICATE_TEXT_LABEL_ATTR_TEXT_COLOR, 
						ATTR_TEXT_HEIGHT, PREDICATE_TEXT_LABEL_ATTR_TEXT_HEIGHT, ATTR_TEXT_STYLE, PREDICATE_TEXT_LABEL_ATTR_TEXT_STYLE, 
						ATTR_FONT_FAMILY, PREDICATE_TEXT_LABEL_ATTR_FONT_FAMILY, ATTR_DECIMAL_FORMAT, PREDICATE_TEXT_LABEL_ATTR_DECIMAL_FORMAT, 
						ATTR_LOCALE_LANG, PREDICATE_TEXT_LABEL_ATTR_LOCALE_LANG, ATTR_LOCALE_COUNTRY, PREDICATE_TEXT_LABEL_ATTR_LOCALE_COUNTRY, 
						ATTR_LOCALE_VARIANT, PREDICATE_TEXT_LABEL_ATTR_LOCALE_VARIANT, ATTR_ID, PREDICATE_TEXT_LABEL_ATTR_ID, 
						ATTR_LABEL_ABOVE, PREDICATE_TEXT_LABEL_ATTR_ABOVE, ATTR_LINE_NO, PREDICATE_TEXT_LABEL_ATTR_LINE_NO, 
						ATTR_LINE_POS, PREDICATE_TEXT_LABEL_ATTR_LINE_POS));		
		putElementReader(new XMLElementReaderKey(TAG_TEXT_LABEL, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_TEXT_LABEL, TAG_LABEL_MARGIN, XMLStreamConstants.START_ELEMENT), labelMarginStartReader);		
		// Element reader for character content of label margin tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_TEXT_LABEL, TAG_LABEL_MARGIN, XMLStreamConstants.END_ELEMENT), edgeResourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_TEXT_LABEL, XMLStreamConstants.END_ELEMENT), edgeResourceEndReader);
		
		// Branch.IconLabel
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_ICON_LABEL, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_ICON_LABEL, null,	true, ATTR_LINE_COLOR, PREDICATE_ICON_LABEL_ATTR_LINE_COLOR, 
						ATTR_LINE_WIDTH, PREDICATE_ICON_LABEL_ATTR_LINE_WIDTH, ATTR_ICON, PREDICATE_ICON_LABEL_ATTR_ICON, 
						ATTR_ICON_WIDTH, PREDICATE_ICON_LABEL_ATTR_WIDTH, ATTR_ICON_HEIGHT, PREDICATE_ICON_LABEL_ATTR_HEIGHT, 
						ATTR_ICON_FILLED, PREDICATE_ICON_LABEL_ATTR_ICON_FILLED, ATTR_ID, PREDICATE_ICON_LABEL_ATTR_ID, 
						ATTR_LABEL_ABOVE, PREDICATE_ICON_LABEL_ATTR_ABOVE, ATTR_LINE_NO, PREDICATE_ICON_LABEL_ATTR_LINE_NO, 
						ATTR_LINE_POS, PREDICATE_ICON_LABEL_ATTR_LINE_POS));
		putElementReader(new XMLElementReaderKey(TAG_ICON_LABEL, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_ICON_LABEL, TAG_LABEL_MARGIN, XMLStreamConstants.START_ELEMENT), labelMarginStartReader);
		// Element reader for character content of label margin tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_ICON_LABEL, TAG_LABEL_MARGIN, XMLStreamConstants.END_ELEMENT), edgeResourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_ICON_LABEL, XMLStreamConstants.END_ELEMENT), edgeResourceEndReader);
		
		// Branch.PieChartLabel
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_PIE_CHART_LABEL, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_PIE_CHART_LABEL, null,	true, ATTR_LINE_COLOR, PREDICATE_PIE_CHART_LABEL_ATTR_LINE_COLOR, 
						ATTR_LINE_WIDTH, PREDICATE_PIE_CHART_LABEL_ATTR_LINE_WIDTH, ATTR_LABEL_WIDTH, PREDICATE_PIE_CHART_LABEL_ATTR_WIDTH, 
						ATTR_LABEL_HEIGHT, PREDICATE_PIE_CHART_LABEL_ATTR_HEIGHT, ATTR_SHOW_INTERNAL_LINES, PREDICATE_PIE_CHART_LABEL_ATTR_INTERNAL_LINES, 
						ATTR_SHOW_NULL_LINES, PREDICATE_PIE_CHART_LABEL_ATTR_NULL_LINES, ATTR_ID, PREDICATE_PIE_CHART_LABEL_ATTR_ID, 
						ATTR_LABEL_ABOVE, PREDICATE_PIE_CHART_LABEL_ATTR_ABOVE, ATTR_LINE_NO, PREDICATE_PIE_CHART_LABEL_ATTR_LINE_NO, 
						ATTR_LINE_POS, PREDICATE_PIE_CHART_LABEL_ATTR_LINE_POS));		
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_LABEL, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_LABEL, TAG_LABEL_MARGIN, XMLStreamConstants.START_ELEMENT), labelMarginStartReader);
		// Element reader for character content of label margin tag was registered before
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_LABEL, TAG_LABEL_MARGIN, XMLStreamConstants.END_ELEMENT), edgeResourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_LABEL, TAG_PIE_CHART_IDS, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_DATA_IDS, null, true));
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_IDS, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_IDS, TAG_PIE_CHART_ID, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(PREDICATE_DATA_ID, null, null, true));
		
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_ID, null, XMLStreamConstants.CHARACTERS), new AbstractXMLElementReader<XMLReaderStreamDataProvider<XTGEventReader>>() {			
			@Override
			public void readEvent(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider, XMLEvent event) throws IOException, XMLStreamException {
				streamDataProvider.getCurrentEventCollection().add(new LiteralMetadataContentEvent(event.asCharacters().getData(), 
						streamDataProvider.getXMLReader().peek().isCharacters()));
			}
		});
		
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_IDS, TAG_PIE_CHART_ID, XMLStreamConstants.END_ELEMENT), new XMLEndElementReader(true, false, true));
		
		putElementReader(new XMLElementReaderKey(TAG_PIE_CHART_LABEL, TAG_PIE_CHART_IDS, XMLStreamConstants.END_ELEMENT), edgeResourceEndReader);
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_PIE_CHART_LABEL, XMLStreamConstants.END_ELEMENT), edgeResourceEndReader);
		
		// Node.InvisibleData
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_HIDDEN_DATA, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_INVISIBLE_DATA, null,	false, ATTR_ID, PREDICATE_INVISIBLE_DATA_ATTR_ID, 
						ATTR_TEXT, PREDICATE_INVISIBLE_DATA_ATTR_TEXT, ATTR_TEXT_IS_DECIMAL, PREDICATE_INVISIBLE_DATA_ATTR_IS_DECIMAL));
		
		putElementReader(new XMLElementReaderKey(TAG_HIDDEN_DATA, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
		
		putElementReader(new XMLElementReaderKey(TAG_NODE, TAG_HIDDEN_DATA, XMLStreamConstants.END_ELEMENT), resourceEndReader);
		
		// Branch.InvisibleData
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_HIDDEN_DATA, XMLStreamConstants.START_ELEMENT), 
				new XMLStartElementReader(null, PREDICATE_INVISIBLE_DATA, null,	true, ATTR_ID, PREDICATE_INVISIBLE_DATA_ATTR_ID, 
						ATTR_TEXT, PREDICATE_INVISIBLE_DATA_ATTR_TEXT, ATTR_TEXT_IS_DECIMAL, PREDICATE_INVISIBLE_DATA_ATTR_IS_DECIMAL));
		
		// Element reader for character content of hidden data tag was registered before
		
		putElementReader(new XMLElementReaderKey(TAG_BRANCH, TAG_HIDDEN_DATA, XMLStreamConstants.END_ELEMENT), edgeResourceEndReader);
		
//		// Tree.Legend
//		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND, XMLStreamConstants.START_ELEMENT), 
//				new XMLStartElementReader(null, PREDICATE_LEGEND, null,	false, ATTR_TEXT, PREDICATE_LEGEND_ATTR_TEXT, 
//						ATTR_TEXT_IS_DECIMAL, PREDICATE_LEGEND_ATTR_IS_DECIMAL, ATTR_TEXT_COLOR, PREDICATE_LEGEND_ATTR_TEXT_COLOR, 
//						ATTR_TEXT_HEIGHT, PREDICATE_LEGEND_ATTR_TEXT_HEIGHT, ATTR_TEXT_STYLE, PREDICATE_LEGEND_ATTR_TEXT_STYLE, 
//						ATTR_FONT_FAMILY, PREDICATE_LEGEND_ATTR_FONT_FAMILY, ATTR_DECIMAL_FORMAT, PREDICATE_LEGEND_ATTR_DECIMAL_FORMAT, 
//						ATTR_LOCALE_LANG, PREDICATE_LEGEND_ATTR_LOCALE_LANG, ATTR_LOCALE_COUNTRY, PREDICATE_LEGEND_ATTR_LOCALE_COUNTRY, 
//						ATTR_LOCALE_VARIANT, PREDICATE_LEGEND_ATTR_LOCALE_VARIANT, ATTR_LINE_COLOR, PREDICATE_LEGEND_ATTR_LINE_COLOR, 
//						ATTR_LINE_WIDTH, PREDICATE_LEGEND_ATTR_LINE_WIDTH, ATTR_LEGEND_POS, PREDICATE_LEGEND_ATTR_LEGEND_POSITION, 
//						ATTR_MIN_TREE_DISTANCE, PREDICATE_LEGEND_ATTR_MIN_TREE_DISTANCE, ATTR_LEGEND_SPACING, PREDICATE_LEGEND_ATTR_LEGEND_SPACING, 
//						ATTR_LEGEND_STYLE, PREDICATE_LEGEND_ATTR_LEGEND_STYLE, ATTR_TEXT_ORIENTATION, PREDICATE_LEGEND_ATTR_ORIENTATION, 
//						ATTR_EDGE_RADIUS, PREDICATE_LEGEND_ATTR_EDGE_RADIUS, ATTR_ANCHOR_0, PREDICATE_LEGEND_ATTR_ANCHOR_0,
//						ATTR_ANCHOR_1, PREDICATE_LEGEND_ATTR_ANCHOR_1));
//		putElementReader(new XMLElementReaderKey(TAG_LEGEND, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());
//		
//		putElementReader(new XMLElementReaderKey(TAG_LEGEND, TAG_LEGEND_MARGIN, XMLStreamConstants.START_ELEMENT), 
//				new XMLStartElementReader(null, PREDICATE_LEGEND_MARGIN, null, false, ATTR_LEFT, PREDICATE_LEGEND_MARGIN_ATTR_LEFT, 
//						ATTR_TOP, PREDICATE_LEGEND_MARGIN_ATTR_TOP, ATTR_RIGHT, PREDICATE_LEGEND_MARGIN_ATTR_RIGHT, 
//						ATTR_BOTTOM, PREDICATE_LEGEND_MARGIN_ATTR_BOTTOM));
//		putElementReader(new XMLElementReaderKey(TAG_LEGEND_MARGIN, null, XMLStreamConstants.CHARACTERS), new XMLNoCharactersAllowedElementReader());		
//		putElementReader(new XMLElementReaderKey(TAG_LEGEND, TAG_LEGEND_MARGIN, XMLStreamConstants.END_ELEMENT), resourceEndReader);
//		
//		putElementReader(new XMLElementReaderKey(TAG_TREE, TAG_LEGEND, XMLStreamConstants.END_ELEMENT), resourceEndReader);		
		
		// Comments
		putElementReader(new XMLElementReaderKey(null, null, XMLStreamConstants.COMMENT), new CommentElementReader());
	}
	
	
	private void createNodeEvents(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider) {
		if (streamDataProvider.hasSpecialEventCollection()) {
			streamDataProvider.resetCurrentEventCollection();
		}
		
		if (streamDataProvider.isCreateNodeStart()) {
			NodeEdgeInfo nodeInfo = streamDataProvider.getSourceNode().peek();
			
			getStreamDataProvider().getCurrentEventCollection().add(new NodeEvent(nodeInfo.getID(), nodeInfo.getLabel(), null, nodeInfo.isRoot()));
			for (JPhyloIOEvent nextEvent : nodeInfo.getNestedNodeEvents()) {
				getStreamDataProvider().getCurrentEventCollection().add(nextEvent);  // Might lead to an exception, if nodeInfo.getNestedEvents() is the currentEventCollection at the time this method is called
			}
			
			streamDataProvider.getCurrentEventCollection().add(ConcreteJPhyloIOEvent.createEndEvent(EventContentType.NODE));
		}
	}
	
	
	private void createEdgeEvents(XMLReaderStreamDataProvider<XTGEventReader> streamDataProvider) {
		Queue<NodeEdgeInfo> edgeInfos = streamDataProvider.getEdgeInfos().pop();  // All edges leading to children of this node
		String sourceID = null;
		NodeEdgeInfo edgeInfo;
		
		if (!streamDataProvider.getEdgeInfos().isEmpty()) {
			streamDataProvider.getEdgeInfos().peek().add(streamDataProvider.getSourceNode().peek());  // Add info for this node to top level queue
		}
		
		if (!streamDataProvider.getSourceNode().isEmpty()) {
			sourceID = streamDataProvider.getSourceNode().peek().getID();
		}
		
		while (!edgeInfos.isEmpty()) {
			edgeInfo = edgeInfos.poll();
			
			if (!((sourceID == null) && Double.isNaN(edgeInfo.getLength()) && edgeInfo.getNestedEdgeEvents().isEmpty())) {  // Do not add root edge if no information about it is present
				getStreamDataProvider().getCurrentEventCollection().add(new EdgeEvent(DEFAULT_EDGE_ID_PREFIX + streamDataProvider.getIDManager().createNewID(), null, 
						sourceID, edgeInfo.getID(), edgeInfo.getLength()));
				
				for (JPhyloIOEvent nextEvent : edgeInfo.getNestedEdgeEvents()) {
					getStreamDataProvider().getCurrentEventCollection().add(nextEvent);
				}
				
				streamDataProvider.getCurrentEventCollection().add(new ConcreteJPhyloIOEvent(
						sourceID == null ? EventContentType.ROOT_EDGE : EventContentType.EDGE, EventTopologyType.END));
			}
		}		
	}
}
