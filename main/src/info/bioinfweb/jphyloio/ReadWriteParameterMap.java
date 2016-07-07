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
package info.bioinfweb.jphyloio;


import info.bioinfweb.commons.collections.ParameterMap;
import info.bioinfweb.commons.log.ApplicationLogger;
import info.bioinfweb.jphyloio.dataadapters.MatrixDataAdapter;
import info.bioinfweb.jphyloio.events.CommentEvent;
import info.bioinfweb.jphyloio.events.SequenceTokensEvent;
import info.bioinfweb.jphyloio.events.SingleTokenDefinitionEvent;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventReader;
import info.bioinfweb.jphyloio.formats.nexml.NeXMLEventWriter;
import info.bioinfweb.jphyloio.formats.nexml.TokenDefinitionLabelHandling;
import info.bioinfweb.jphyloio.formats.nexml.TokenTranslationStrategy;
import info.bioinfweb.jphyloio.formats.nexus.NexusEventReader;
import info.bioinfweb.jphyloio.formats.nexus.blockhandlers.NexusBlockHandler;
import info.bioinfweb.jphyloio.formats.nexus.commandreaders.NexusCommandReaderFactory;
import info.bioinfweb.jphyloio.formats.phyloxml.PhyloXMLEventReader;
import info.bioinfweb.jphyloio.objecttranslation.ObjectTranslatorFactory;



/**
 * Parameter map that allows to specify (optional) parameters to implementations of {@link JPhyloIOEventWriter}.
 * 
 * @author Ben St&ouml;ver
 */
public class ReadWriteParameterMap extends ParameterMap {
	/** 
	 * The prefix of all keys used in <i>JPhyloIO</i> for parameter maps.
	 * <p>
	 * Application or third party library developers who implement additional readers or writers using additional
	 * parameter keys should define keys according to the reverse domain name pattern that do not start with this
	 * prefix ({@value #KEY_PREFIX}). 
	 */
	public static final String KEY_PREFIX = "info.bioinfweb.jphyloio.";
	
	/** 
	 * This key is used by implementations of {@link JPhyloIOEventWriter} to provide a reference to the currently 
	 * active instance. The according entry is useful for implementations of a data adapter, if they wish to obtain
	 * information on the writer requesting data from them.
	 * <p>
	 * Note that this key identifies a return value of a writer implementation and not a parameter for the writer. 
	 * Entries provided by the implementation prior to calling a write method of a writer will be directly overwritten 
	 * by the actual target format. 
	 * <p>
	 * The value must have the type {@link JPhyloIOEventWriter}. 
	 */
	public static final String KEY_WRITER_INSTANCE = KEY_PREFIX + "writerInstance";
	
	/** 
	 * Identifies an application logger to write log messages to.
	 * <p>
	 * The value should have the type {@link ApplicationLogger}. 
	 */
	public static final String KEY_LOGGER = KEY_PREFIX + "logger";
	
	/** 
	 * Parameter used with instances of {@link JPhyloIOEventReader} that specifies the maximal number of tokens
	 * to be included in a single {@link SequenceTokensEvent}. Some readers may ignore this parameter. (Refer to the 
	 * documentations of the single readers for details on which parameters each of them supports.)
	 * <p>
	 * The value should be an integer value (e.g. {@link Integer}).
	 */
	public static final String KEY_MAXIMUM_TOKENS_TO_READ = KEY_PREFIX + "maxTokensToRead";
	
	/** 
	 * Parameter used with instances of {@link JPhyloIOEventReader} that specifies the maximal length of the text in
	 * a {@link CommentEvent}. Readers that support this parameter will separate longer comments into separate events.
	 * (Refer to the documentations of the single readers for details on which parameters each of them supports.)
	 * <p>
	 * The value should be an integer value (e.g. {@link Integer}).
	 */
	public static final String KEY_MAXIMUM_COMMENT_LENGTH = KEY_PREFIX + "maxCommentLength";
	
	/** 
	 * Parameter used with instances of {@link JPhyloIOEventReader} that specifies whether possibly encountered match
	 * tokens shall be replaced by the according tokens from the first sequence. Some readers may ignore this parameter
	 * and always show a default behavior. (Refer to the documentations of the single readers for details on which 
	 * parameters each of them supports.)
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code true} is specified, match tokens will be replaced, if 
	 * {@code false} is specified match tokens will be treated as valid tokens and directly by passed to the sequence
	 * token events.
	 * 
	 * @see #KEY_MATCH_TOKEN
	 */
	public static final String KEY_REPLACE_MATCH_TOKENS = KEY_PREFIX + "replaceMatchTokens";
	
	/** 
	 * Parameter used with instances of {@link JPhyloIOEventReader} that specifies the match token in the sequences to
	 * be read. Note that this parameter is only relevant if no match token is directly defined in the format. (Refer 
	 * to the documentations of the single readers for details on which parameters each of them supports.)
	 * <p>
	 * The value should have the type {@link String}. If it is omitted, readers supporting this parameter will use
	 * {@code "."} as the default match token.
	 * 
	 * @see #KEY_REPLACE_MATCH_TOKENS
	 */
	public static final String KEY_MATCH_TOKEN = KEY_PREFIX + "matchToken";
	
	/** 
	 * Parameter which determines whether Phylip readers should consider the input as relaxed Phylip (allowing longer
	 * taxon names).
	 * <p>
	 * The value must have the type {@link Boolean}. If {@code true} is specified, relaxed Phylip will be expected. If
	 * {@code false} is specified or this parameter is omitted, strict Phylip will be expected. 
	 */
	public static final String KEY_RELAXED_PHYLIP = KEY_PREFIX + "relaxedPhylip";

	/** 
	 * Parameter which determines whether interleaved input files (e.g. in Phylip) shall be supported by this parser 
	 * instance. (In order to support this feature some readers need to keep a list of all sequence names. To parse 
	 * files with a very large number of sequences which are not interleaved, this feature can be switched off to save 
	 * memory. If this switch is set to {@code true} non-interleaved files can also still be parsed.)
	 * <p>
	 * Note that not all readers for possibly interleaved formats will make use of this parameter. (Refer to the single
	 * reader documentations for details.)
	 * <p>
	 * The value must have the type {@link Boolean}. If {@code true} is specified or this parameter is omitted, parsing 
	 * interleaved files will be possible. If {@code false} is specified parsing interleaved files will not be possible. 
	 */
	public static final String KEY_ALLOW_INTERLEAVED_PARSING = KEY_PREFIX + "allowInterleavedParsing";
	
	/**
	 * This parameter will only be used by {@link NexusEventReader} and allows to define a custom block handler map.
	 * <p>
	 * It must have the type {@link NexusBlockHandler}. If it is omitted a block handler for all blocks supported in
	 * <i>JPhyloIO</i> will be used.
	 */
	public static final String KEY_NEXUS_BLOCK_HANDLER_MAP = KEY_PREFIX + "nexusBlockHandlerMap";
	
	/**
	 * This parameter will only be used by {@link NexusEventReader} and allows to define a custom command reader factory.
	 * <p>
	 * It must have the type {@link NexusCommandReaderFactory}. If it is omitted a factory providing all command readers 
	 * available in <i>JPhyloIO</i> will be used.
	 */
	public static final String KEY_NEXUS_COMMAND_READER_FACTORY = KEY_PREFIX + "nexusCommandReaderFactory";
	
	/**
	 * This parameter can be used to specify whether special events shall be fired if the reader encounters unknown
	 * commands (e.g. Nexus commands).
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code false} or no value is specified, no such events will be
	 * fired. If {@code true} is specified, an event for each unknown command will be created.
	 */
	public static final String KEY_CREATE_UNKNOWN_COMMAND_EVENTS = KEY_PREFIX + "createUnknownCommandEvents";
	
	/**
	 * This parameter will only be used by {@link NeXMLEventReader} and allows to define the way tokens stored in a 
	 * NeXML characters block of type {@code standard} should be parsed.
	 * <p>
	 * It must have the type {@link TokenTranslationStrategy}. If it is omitted 
	 * {@link TokenTranslationStrategy#SYMBOL_TO_LABEL} will be used as the default.
	 */
	public static final String KEY_NEXML_TOKEN_TRANSLATION_STRATEGY = KEY_PREFIX + "neXMLTokenTranslationStrategy";
	
	/**
	 * This parameter will only be used by {@link NeXMLEventWriter} and allows to define whether the token name or the label 
	 * of a {@link SingleTokenDefinitionEvent} shall be written to the label-attribute of a state element in a 
	 * NeXML characters block of type {@code standard}.
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code false} or no value is specified, the event label will always be 
	 * written to the label attribute. If {@code true} is specified, the name of the token defined in the event will be used.
	 */
	public static final String KEY_NEXML_STANDARD_DATA_LABEL = KEY_PREFIX + "neXMLstandardDataLabel";
	
	/**
	 * This parameter will only be used by {@link NeXMLEventWriter} and allows to define whether the token name and the label 
	 * of a {@link SingleTokenDefinitionEvent} shall be represented as metadata always, never or only if one of these properties 
	 * could not be written to the label-attribute of a state element in a NeXML characters block of type {@code standard}.
	 * <p>
	 * It must have the type {@link TokenDefinitionLabelHandling}. If it is omitted 
	 * {@link TokenDefinitionLabelHandling#NEITHER} will be used as the default.
	 */
	public static final String KEY_NEXML_STANDARD_DATA_LABEL_METADATA = KEY_PREFIX + "neXMLstandardDataLabelMetadata";
	
	/** 
	 * Identifies a comment describing the application generating the output, which shall be added at the beginning
	 * of formats that support this.
	 * <p>
	 * The value should be a {@link String}.
	 */
	public static final String KEY_APPLICATION_COMMENT = KEY_PREFIX + "applicationComment";
	
	/** 
	 * If a line separator different from that of the current operating system shall be used by a writer, it can be 
	 * specified using this key. (Note that writers for XML formats will not necessarily make use of this parameter.)
	 * <p>
	 * The value should be a {@link String}.
	 */
	public static final String KEY_LINE_SEPARATOR = KEY_PREFIX + "lineSeparator";
	
	/** 
	 * Specifies the preferred line length for writers that support this.
	 * <p> 
	 * The value should have an integer type (e.g. {@link Integer}).
	 */
	public static final String KEY_LINE_LENGTH = KEY_PREFIX + "lineLength";
	
	/** 
	 * This parameter can be used to specify that sequences with unequal lengths (in character matrix data) shall be filled 
	 * up with, until all have an equal length.  
	 * <p>
	 * The value should have the type {@link String} and define the token to be used to fill up sequences. If this parameter
	 * is omitted or {@code null} is specified, sequences will not be filled up. Note that the specified string should only
	 * be longer than one character, if it is only used with {@link MatrixDataAdapter}s that provide long tokens.
	 * ({@link MatrixDataAdapter#containsLongTokens(ReadWriteParameterMap)} must return {@code true}.)
	 * <p>
	 * Note that this parameter is valid for all alignments. It is not possible to extend only a subset alignments of the 
	 * same file using this parameter. 
	 */
	public static final String KEY_SEQUENCE_EXTENSION_TOKEN = KEY_PREFIX + "sequenceExtensionToken";
	
	/**
	 * This parameter can be used to specify whether comment events should be written to the output, at positions where 
	 * the format (or reader) supports it.
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code true} is specified, comment events will be ignored by 
	 * writers supporting this parameter. If {@code false} is specified or this parameter is omitted, comments will be 
	 * written to the output at all supported positions.
	 */
	public static final String KEY_IGNORE_COMMENTS = KEY_PREFIX + "ignoreComments";
	
	/**
	 * This parameter can be used to specify whether readers of XML formats shall also accept tags of their target format
	 * without any namespace definition.
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code true} or no value is specified, tags without the target format
	 * namespace declaration will be supported, if their local part fits. If {@code false} is specified, the correct namespace
	 * is required.
	 */
	public static final String KEY_ALLOW_DEFAULT_NAMESPACE = KEY_PREFIX + "allowDefaultNamespace";
	
	/**
	 * This parameter can be used to specify a custom maximum lengths for element names. An example would be the length
	 * of sequence names in Phylip, which may be customized using this parameter.
	 * <p>
	 * The value should have an integer type (e.g. {@link Integer}). Writers that support this parameter will us a default
	 * length if this parameter is omitted. Only writers for formats that impose length restrictions will make use of this
	 * parameter. Note that some formats may define a fixed maximum length and specifying this parameter may lead to a
	 * deviation from the standards of this format. Refer to the documentation of the according writer for details in that. 
	 */
	public static final String KEY_MAXIMUM_NAME_LENGTH = KEY_PREFIX + "maximumNameLength";
	//TODO Should formats that can use any lengths and formats that usually impose a defined length be using separate parameters to avoid accidental deviation from format standards?
	//     (Since currently Phylip should be the only relevant format here, only one parameter would currently be needed and that is more a question of which definition to use in the documentation.)
	
	/**
	 * This parameter can be used to specify whether a {@code TRANSLATE} command should be included in the {@code TREES}
	 * block of a Nexus document.
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code true} is specified, a {@code TRANSLATE} command will be 
	 * written and references to will be used as node labels in the following tree(s). If {@code false} or no value is 
	 * specified, the full node labels will included in the tree(s).
	 */
	public static final String KEY_GENERATE_NEXUS_TRANSLATION_TABLE = KEY_PREFIX + "generateTranslationTable";
	
	/**
	 * This parameter can be used to specify whether a nodes in a Nexus TREE command should always be written as full 
	 * labels of if their index in the associated TAXA block should be used, when possible.
	 * <p>
	 * The value should have the type {@link Boolean}. If {@code false} or no value is specified, labels will be replaced
	 * by indices, whenever possible. If {@code true} is specified, the full labels will always be used.
	 */
	public static final String KEY_ALWAYS_WRITE_NEXUS_NODE_LABELS = KEY_PREFIX + "alwaysWriteNexusNodeLabels";
	
	/**
	 * Writers may have to edit names of OTUs, sequences or tree/network nodes according to the limitations of a specific
	 * format. In such cases, the generated names used in the output will be registered in an information object, which 
	 * will be stored in the parameter map under this key.
	 * <p>
	 * The value will have the type {@link LabelEditingReporter}. If no instance is specified, writers supporting this
	 * key will create a new one and put it in their parameter map. If a reporter is specified, its previous contents 
	 * will be cleared by the writer. If an object of another type is specified using this key, it will be replaced.
	 * 
	 * @see #getLabelEditingReporter()
	 */
	public static final String KEY_LABEL_EDITING_REPORTER = KEY_PREFIX + "labelEditingReporter";
	
	/**
	 * This parameter can be used to specify a custom instance of {@link ObjectTranslatorFactory} that is used by some
	 * readers to create literal meta object values. Specifying a custom factory allows to provide translators for custom
	 * data types or to overwrite default translators for common types.
	 * 
	 * @see #getObjectTranslatorFactory()
	 */
	public static final String KEY_OBJECT_TRANSLATOR_FACTORY = KEY_PREFIX + "objectTranslatorFactory";
	
	/**
	 * This parameter will only be used by {@link PhyloXMLEventReader} and allows to define if trees read from a 
	 * PhyloXML file should always be considered as trees or networks. In the first case, "clade relation" tags 
	 * contained in a phylogeny will only be represented as meta information, not as additional edge events.
	 * <p>
	  * The value should have the type {@link Boolean}. If {@code false} or no value is specified, a phylogeny will 
	  * always be considered as a network. If {@code true} is specified, it will always be considered as a tree. 
	 */
	public static final String KEY_PHYLOXML_CONSIDER_PHYLOGENY_AS_TREE = KEY_PREFIX + "considerPhylogenyAsTree";
	
	
	/**
	 * Returns the writer instance registered under {@link #KEY_WRITER_INSTANCE}.
	 * 
	 * @return the current writer instance or {@code null} if none is present in the map (e.g. because it is used in a reader).
	 */
	public JPhyloIOEventWriter getWriterInstance() {
		return (JPhyloIOEventWriter)get(KEY_WRITER_INSTANCE);
	}
	
	
	public ApplicationLogger getLogger() {
		return getApplicationLogger(KEY_LOGGER);
	}
	
	
	public int getMaxTokensToRead() {
		return getInteger(KEY_MAXIMUM_TOKENS_TO_READ, ReadWriteConstants.DEFAULT_MAX_TOKENS_TO_READ);
	}
	
	
	public int getMaxCommentLength() {
		return getInteger(KEY_MAXIMUM_COMMENT_LENGTH, ReadWriteConstants.DEFAULT_MAX_COMMENT_LENGTH);
	}
	
	
	public String getMatchToken() {
		if (getBoolean(KEY_REPLACE_MATCH_TOKENS, true)) {
			return getString(KEY_MATCH_TOKEN, ReadWriteConstants.DEFAULT_MATCH_TOKEN);
		}
		else {
			return null;
		}
	}
	
	
	/**
	 * Returns the label editing reporter of this map stored under {@link #KEY_LABEL_EDITING_REPORTER}. If no object for 
	 * this key is present in this instance, a new one is created, added to this instance and returned. The same is done, 
	 * if an object which is not an instance of {@link LabelEditingReporter} is found for this key. 
	 * 
	 * @return the map instance
	 */
	public LabelEditingReporter getLabelEditingReporter() {
		Object result = get(KEY_LABEL_EDITING_REPORTER);
		if (!(result instanceof LabelEditingReporter)) {  // Also checks for null.
			result = new LabelEditingReporter();
			put(KEY_LABEL_EDITING_REPORTER, result);
		}
		return (LabelEditingReporter)result;
	}
	
	
	public TokenTranslationStrategy getTranslateTokens() {
		return getObject(ReadWriteParameterMap.KEY_NEXML_TOKEN_TRANSLATION_STRATEGY, 
				TokenTranslationStrategy.SYMBOL_TO_LABEL, TokenTranslationStrategy.class);
	}
	
	
	public TokenDefinitionLabelHandling getLabelHandling() {
		return getObject(ReadWriteParameterMap.KEY_NEXML_STANDARD_DATA_LABEL_METADATA, 
				TokenDefinitionLabelHandling.NEITHER, TokenDefinitionLabelHandling.class);
	}


	/**
	 * Returns the object translator factory stored under {@link #KEY_OBJECT_TRANSLATOR_FACTORY}. If no object for 
	 * this key is present in this instance, a new one is created, added to this instance and returned. The same is done, 
	 * if an object which is not an instance of {@link LabelEditingReporter} is found for this key. The created default
	 * instance will contain all default translators provided with <i>JPhyloIO</i>.
	 * 
	 * @return the factory instance
	 */
	public ObjectTranslatorFactory getObjectTranslatorFactory() {
		Object result = get(KEY_OBJECT_TRANSLATOR_FACTORY);
		if (!(result instanceof ObjectTranslatorFactory)) {  // Also checks for null.
			result = new ObjectTranslatorFactory();
			((ObjectTranslatorFactory)result).addXSDTranslators(true);
			put(KEY_OBJECT_TRANSLATOR_FACTORY, result);
		}
		return (ObjectTranslatorFactory)result;
	}
}
