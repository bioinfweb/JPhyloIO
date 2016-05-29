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



public class ObjectTranslatorFactory {
	private class MapEntry {
		public ObjectTranslator<?> defaultTranslator;
		public Map<Class<?>, ObjectTranslator<?>> alternatives;
		
		
		public MapEntry(ObjectTranslator<?> defaultTranslator, ObjectTranslator<?>... alternatives) {
			super();
			this.defaultTranslator = defaultTranslator;
			if (alternatives.length > 0) {
				this.alternatives = new HashMap<Class<?>, ObjectTranslator<?>>(alternatives.length);
				for (int i = 0; i < alternatives.length; i++) {
					this.alternatives.put(alternatives[i].getObjectClass(), alternatives[i]);
				}
			}
		}
	}
	
	
	private Map<QName, MapEntry> translatorMap = new HashMap<QName, ObjectTranslatorFactory.MapEntry>();
	
	
	public void addTranslator(ObjectTranslator<?> translator) {
		translatorMap.put(translator.getDataType(), new MapEntry(translator));
	}
	
	
	public void addXDSTranslators() {
		addTranslator(new BooleanTranslator());
	}
	
	
	public <O> ObjectTranslator<O> getTranslator(QName dataType, Class<O> objectClass) {
		MapEntry entry = translatorMap.get(dataType);
		if (entry != null) {
			if (entry.defaultTranslator.getObjectClass().equals(objectClass)) {
				return (ObjectTranslator<O>)entry.defaultTranslator;
			}
			else if (entry.alternatives != null) {
				return (ObjectTranslator<O>)entry.alternatives.get(objectClass);
			}
		}
		return null;
	}

	
	public ObjectTranslator<?> getDefaultTranslator(QName dataType) {
		MapEntry entry = translatorMap.get(dataType);
		if (entry != null) {
			return entry.defaultTranslator;
		}
		else {
			return null;
		}
	}
}
