/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2018  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.demo.eventwebview.exception;



public class EventWebViewException extends Exception {
	private static final long serialVersionUID = 1L;


	public EventWebViewException() {
		super();
	}

	
	public EventWebViewException(String message, Throwable throwable) {
		super(message, throwable);
	}

	
	public EventWebViewException(String message) {
		super(message);
	}

	
	public EventWebViewException(Throwable throwable) {
		super(throwable);
	}
}
