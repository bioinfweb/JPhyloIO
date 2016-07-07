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


import javax.xml.namespace.QName;



public interface XTGConstants {
	public static final String XTG_FORMAT_NAME = "Extensible TreeGraph 2 format";
	public static final String XTG = "xtg";
	
	public static final String NAMESPACE_URI = "http://bioinfweb.info/xmlns/xtg";
	public static final String NAMESPACE_URI_XSI = "http://www.w3.org/2001/XMLSchema-instance";
	
	public static final QName TAG_ROOT = new QName(NAMESPACE_URI, "TreegraphDocument");
	public static final QName TAG_GLOBAL_FORMATS = new QName(NAMESPACE_URI, "GlobalFormats");
	public static final QName TAG_DOCUMENT_MARGIN = new QName(NAMESPACE_URI, "DocMargin");	
	public static final QName TAG_TREE = new QName(NAMESPACE_URI, "Tree");
	public static final QName TAG_NODE = new QName(NAMESPACE_URI, "Node");
	public static final QName TAG_LEAF_MARGIN = new QName(NAMESPACE_URI, "LeafMargin");
	public static final QName TAG_BRANCH = new QName(NAMESPACE_URI, "Branch");
	public static final QName TAG_TEXT_LABEL = new QName(NAMESPACE_URI, "TextLabel");
	public static final QName TAG_ICON_LABEL = new QName(NAMESPACE_URI, "IconLabel");
	public static final QName TAG_PIE_CHART_LABEL = new QName(NAMESPACE_URI, "PieChartLabel");
	public static final QName TAG_SCALE_BAR = new QName(NAMESPACE_URI, "ScaleBar");
	public static final QName TAG_LABEL_MARGIN = new QName(NAMESPACE_URI, "LabelMargin");
	public static final QName TAG_LEGEND = new QName(NAMESPACE_URI, "Legend");
	public static final QName TAG_LEGEND_MARGIN = new QName(NAMESPACE_URI, "LegendMargin");
	public static final QName TAG_DATA_IDS = new QName(NAMESPACE_URI, "DataIDs");
	public static final QName TAG_DATA_ID = new QName(NAMESPACE_URI, "DataID");
	public static final QName TAG_INVISIBLE_DATA = new QName(NAMESPACE_URI, "InvisibleData");
	
	public static final QName ATTR_UNIQUE_NAME = new QName("UniqueName");
	public static final QName ATTR_TEXT = new QName("Text");
	public static final QName ATTR_SHOW_ROOTED = new QName("ShowRooted");
	public static final QName ATTR_BRANCH_LENGTH = new QName("Length");
}
