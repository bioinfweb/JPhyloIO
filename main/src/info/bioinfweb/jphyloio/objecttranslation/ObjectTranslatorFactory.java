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
package info.bioinfweb.jphyloio.objecttranslation;


import info.bioinfweb.jphyloio.objecttranslation.implementations.xsd.BooleanTranslator;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;



/**
 * Factory to create instances of {@link ObjectTranslator} to be used with readers and writers of <i>JPhyloIO</i>.
 * <p>
 * After creation this factory is empty. New translators can be added to this factory using 
 * {@link #addTranslator(ObjectTranslator, boolean)}. A default set of translators for XSD types can be added by calling
 * {@link #addXDSTranslators(boolean)}. 
 * 
 * @author Ben St&ouml;ver
 * @since 0.0.0
 */
public class ObjectTranslatorFactory {
	private Map<TranslatorMapKey, ObjectTranslator<?>> translatorMap = new HashMap<TranslatorMapKey, ObjectTranslator<?>>();
	
	
	/**
	 * Registers a new translator in this factory.
	 * 
	 * @param translator the translator to be registered
	 * @param asDefault Determines whether this translator shall become the default translator for its data type. (It will
	 *        always become the default, if no other translator is currently registered for this data type.)
	 */
	public void addTranslator(ObjectTranslator<?> translator, boolean asDefault) {
		translatorMap.put(new TranslatorMapKey(translator.getDataType(), translator.getObjectClass()), translator);
		if (asDefault || (getDefaultTranslator(translator.getDataType()) == null)) {
			translatorMap.put(new TranslatorMapKey(translator.getDataType(), null), translator);
		}
	}
	
	
	/**
	 * Adds all translators for XSD types available in <i>JPhyloIO</i>.
	 * 
	 * @param asDefault Determines whether the added translators shall become the default translators for their data type, 
	 *        if another default instance is already registered. (If {@code true} is specified, previous defaults will be
	 *        overwritten. If {@code false} is specified, previous defaults will be maintained. In all cases previous entries
	 *        will remain in the factory, of they have a different object type and will be completely overwritten if they have 
	 *        the same.)  
	 */
	public void addXDSTranslators(boolean asDefault) {
		addTranslator(new BooleanTranslator(), asDefault);
	}
	
	
	public <O> ObjectTranslator<O> getTranslator(QName dataType, Class<O> objectClass) {
		return (ObjectTranslator<O>)translatorMap.get(new TranslatorMapKey(dataType, objectClass));
	}

	
	public ObjectTranslator<?> getDefaultTranslator(QName dataType) {
		return getTranslator(dataType, null);
	}
}
