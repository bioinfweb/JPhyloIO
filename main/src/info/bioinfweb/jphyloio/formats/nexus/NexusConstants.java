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
package info.bioinfweb.jphyloio.formats.nexus;


import java.util.regex.Pattern;



/**
 * Defines constants necessary to read and write Nexus files.
 * 
 * @author Ben St&ouml;ver
 */
public interface NexusConstants {
	public static final String FIRST_LINE = "#NEXUS";
	public static final String BEGIN_COMMAND = "BEGIN";
	public static final String END_COMMAND = "END";
	public static final String ALTERNATIVE_END_COMMAND = "ENDBLOCK";
	public static final char COMMAND_END = ';';
	public static final char COMMENT_START = '[';
	public static final char COMMENT_END = ']';
	public static final char KEY_VALUE_SEPARATOR = '=';
	public static final char VALUE_DELIMITER = '"';
	public static final char WORD_DELIMITER = '\'';
	public static final char CHAR_SET_TO = '-';
	public static final char ELEMENT_SEPARATOR = ',';
	
	public static final String BLOCK_NAME_TAXA = "TAXA";
	public static final String BLOCK_NAME_CHARACTERS = "CHARACTERS";
	public static final String BLOCK_NAME_UNALIGNED = "UNALIGNED";
	public static final String BLOCK_NAME_DATA = "DATA";
	public static final String BLOCK_NAME_SETS = "SETS";
	public static final String BLOCK_NAME_TREES = "TREES";
	
	public static final String DIMENSIONS_SUBCOMMAND_NTAX = "NTAX";
	public static final String DIMENSIONS_SUBCOMMAND_NCHAR = "NCHAR";
	
	public static final String FORMAT_NAME_STANDARD = "STANDARD";
	public static final String FORMAT_NAME_VECTOR = "VECTOR";

	public static final String FORMAT_SUBCOMMAND_DATA_TYPE = "DATATYPE";
	public static final String FORMAT_SUBCOMMAND_TOKENS = "TOKENS";
	public static final String FORMAT_SUBCOMMAND_INTERLEAVE = "INTERLEAVE";
	public static final String FORMAT_SUBCOMMAND_TRANSPOSE = "TRANSPOSE";
	public static final String FORMAT_SUBCOMMAND_NO_LABELS = "NOLABELS";
	public static final String FORMAT_SUBCOMMAND_MATCH_CHAR = "MATCHCHAR";
	public static final String FORMAT_SUBCOMMAND_MISSING_CHAR = "MISSING";
	public static final String FORMAT_SUBCOMMAND_GAP_CHAR = "GAP";
	public static final String FORMAT_SUBCOMMAND_SYMBOLS = "SYMBOLS";
	
	public static final String FORMAT_VALUE_STANDARD_DATA_TYPE = "STANDARD";
	public static final String FORMAT_VALUE_NUCLEOTIDE_DATA_TYPE = "NUCLEOTIDE";
	public static final String FORMAT_VALUE_DNA_DATA_TYPE = "DNA";
	public static final String FORMAT_VALUE_RNA_DATA_TYPE = "RNA";
	public static final String FORMAT_VALUE_PROTEIN_DATA_TYPE = "PROTEIN";
	public static final String FORMAT_VALUE_CONTINUOUS_DATA_TYPE = "CONTINUOUS";
	/** This is not part of the official Nexus definition, but is a MrBayes extension. */
	public static final String FORMAT_VALUE_MIXED_DATA_TYPE = "MIXED";

	public static final char MATRIX_POLYMORPHIC_TOKEN_START = '(';
	public static final char MATRIX_POLYMORPHIC_TOKEN_END = ')';
	public static final char MATRIX_UNCERTAINS_TOKEN_START = '{';
	public static final char MATRIX_UNCERTAINS_TOKEN_END = '}';
	
	public static final char CHAR_SET_NOT_CONTAINED = '0';
	public static final char CHAR_SET_CONTAINED = '1';
	
	public static final Pattern INTEGER_PATTERN = Pattern.compile("[0-9]+");  //TODO Move to more general class (e.g. in PeekReader or somewhere else in commons).
	public static final Pattern UNTIL_WHITESPACE_COMMENT_COMMAND_PATTERN = Pattern.compile(
			".*(\\s|\\" + COMMENT_START + "|\\" + COMMAND_END + ")");
	public static final Pattern UNTIL_WHITESPACE_COMMENT_COMMAND_EQUAL_PATTERN = Pattern.compile(
			".*(\\s|\\" + COMMENT_START + "|\\" + COMMAND_END + "|\\=)");
}
