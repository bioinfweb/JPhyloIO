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

import info.bioinfweb.jphyloio.dataadapters.DocumentDataAdapter;



/**
 * Exception which is thrown by an instance of {@link JPhyloIOEventWriter} if the data in the specified 
 * {@link DocumentDataAdapter} is inconsistent. (An example could be a sequence event referencing an OTU 
 * which is not defined in the document adapter.)
 * 
 * @author Ben St&ouml;ver
 */
public class InconsistentAdapterDataException extends RuntimeException {
	public InconsistentAdapterDataException() {
		super();
	}

	
	public InconsistentAdapterDataException(String message, Throwable cause) {
		super(message, cause);
	}

	
	public InconsistentAdapterDataException(String message) {
		super(message);
	}

	
	public InconsistentAdapterDataException(Throwable cause) {
		super(cause);
	}
}
