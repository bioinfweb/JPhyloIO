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
package info.bioinfweb.jphyloio.formats.mega;



public interface MEGAConstants {
	public static final String FIRST_LINE = "#MEGA";
	public static final char COMMAND_END = ';';
	public static final char COMMAND_START = '!';
	public static final char SEUQUENCE_START = '#';
	public static final char COMMENT_START = '[';
	public static final char COMMENT_END = ']';
	public static final char DEFAULT_LABEL_CHAR = '_';

	public static final String COMMAND_NAME_TITLE = "TITLE";
	public static final String COMMAND_NAME_DESCRIPTION = "DESCRIPTION"; 
	public static final String COMMAND_NAME_FORMAT = "FORMAT";
	public static final String COMMAND_NAME_LABEL = "LABEL";
	public static final String COMMAND_NAME_GENE = "GENE";
	public static final String COMMAND_NAME_DOMAIN = "DOMAIN";

	public static final String FORMAT_SUBCOMMAND_IDENTICAL = "IDENTICAL";
}
