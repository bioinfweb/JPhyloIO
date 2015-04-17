/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015  Ben Stöver
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



/**
 * Provides general constants for reading and writing phylogenetic file formats.
 * 
 * @author Ben St&ouml;ver
 */
public interface ReadWriteConstants {
	public static final int DEFAULT_MAX_CHARS_TO_READ = 2048;
	
	public static final String META_KEY_SEQUENCE_COUNT = "info.bioinfweb.jphyloio.sequenceCount";
	public static final String META_KEY_CHARACTER_COUNT = "info.bioinfweb.jphyloio.characterCount";
}
