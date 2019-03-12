/*
 * JPhyloIO - Event based parsing and stream writing of multiple sequence alignment and tree formats. 
 * Copyright (C) 2015-2019  Ben Stöver, Sarah Wiechers
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
package info.bioinfweb.jphyloio.demo.eventwebview;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;

import info.bioinfweb.commons.text.StringUtils;
import info.bioinfweb.jphyloio.JPhyloIO;
import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.demo.eventwebview.exception.LoadingException;
import info.bioinfweb.jphyloio.demo.eventwebview.exception.ProcessingException;
import info.bioinfweb.jphyloio.events.ConcreteJPhyloIOEvent;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.factory.JPhyloIOReaderWriterFactory;



public class EventListerServlet extends HttpServlet {
	public static final String PARAM_SOURCE_URL = "sourceURL";
	public static final String PARAM_SOURCE_FILE = "sourceFile";
	public static final String PARAM_SOURCE_CONTENT = "sourceContent";
	public static final String PARAM_FORMAT = "format";
	
	public static final String ATTR_SOURCE = "source";
	public static final String ATTR_VERSION = "jPhyloIOVersion";
	public static final String ATTR_EVENT = "event";
	public static final String ATTR_PROPERTIES = "properties";

	public static final String START_OUTPUT_JSP = "/start.jsp";
	public static final String END_OUTPUT_JSP = "/end.jsp";
	public static final String SUBTREE_START_OUTPUT_JSP = "/subtreeStart.jsp";
	public static final String SUBTREE_END_OUTPUT_JSP = "/subtreeEnd.jsp";
	public static final String EVENT_OUTPUT_JSP = "/eventOutput.jsp";
	public static final String SUBTREE_BUTTON_OUTPUT_JSP = "/subtreeButtonOutput.jsp";
		
	
	private JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
	
	
	private Method getMethod(Class<?> eventClass, String prefix, Field field) {
		try {
			return eventClass.getMethod(prefix + StringUtils.firstCharToUpperCase(field.getName()));
		}
		catch (NoSuchMethodException | SecurityException e) {}  // Nothing to do. (Return null later.)
		return null;
	}
	
	
	private String formatValue(Object value) {
		if (value instanceof Iterable<?>) {
			StringBuilder result = new StringBuilder();
			result.append("[");
			
			Iterator<?> iterator = ((Iterable<?>)value).iterator();
			while (iterator.hasNext()) {
				result.append(formatValue(iterator.next()));
				if (iterator.hasNext()) {
					result.append(", ");
				}
			}
			
			result.append("]");
			return result.toString();
		}
		else {
			String result = "null";
			if (value != null) {
				result = value.toString();
			}
			
			if (value instanceof CharSequence) {
				result = "&quot;" + result + "&quot;";
			}
			else {
				result = "<code>" + result + "</code>";
			}
			return result;
		}
	}
	
	
	private List<EventProperty> createPropertyList(JPhyloIOEvent event) throws ServletException {
		List<EventProperty> result = new ArrayList<>();
		
		Class<?> eventClass = event.getClass();
		while ((eventClass != null) && !ConcreteJPhyloIOEvent.class.getName().equals(eventClass.getName())) {
			for (Field field : eventClass.getDeclaredFields()) {
				try {
					Method getter = getMethod(eventClass, "get", field);
					if (getter == null) {
						getter = getMethod(eventClass, "is", field);
					}
					
					if (getter != null) {
						result.add(new EventProperty(field.getName(), formatValue(getter.invoke(event))));
					}
				} 
				catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
					throw new ServletException(e);  // ServletExceptions are not wrapped into ProcessingExceptions.
				}
			}
			eventClass = eventClass.getSuperclass();
		}
		
		Collections.reverse(result);  // Inherited fields should be displayed first.
		return result;
	}
	
	
	private void outputEvents(HttpServletRequest request, HttpServletResponse response, InputStream stream, String formatID) throws Exception {
		getServletContext().getRequestDispatcher(START_OUTPUT_JSP).include(request, response);
		getServletContext().getRequestDispatcher(SUBTREE_START_OUTPUT_JSP).include(request, response);
		
		ReadWriteParameterMap params = new ReadWriteParameterMap();  //TODO Add any parameters?
		JPhyloIOEventReader reader;
		if (formatID != null) {
			reader = factory.getReader(formatID, stream, params);
		}
		else {
			reader = factory.guessReader(stream, params);
		}
		
		if (reader == null) {
			throw new LoadingException("No appropriate reader could be found for the specified file.");
		}
		else {
			while (reader.hasNextEvent()) {
				JPhyloIOEvent event = reader.next();
				
				request.setAttribute(ATTR_EVENT, event);
				request.setAttribute(ATTR_PROPERTIES, createPropertyList(event));
				
				getServletContext().getRequestDispatcher(EVENT_OUTPUT_JSP).include(request, response);
				
				if (EventTopologyType.START.equals(event.getType().getTopologyType()) && reader.hasNextEvent() && 
						!EventTopologyType.END.equals(reader.peek().getType().getTopologyType())) {
					
					getServletContext().getRequestDispatcher(SUBTREE_BUTTON_OUTPUT_JSP).include(request, response);
					getServletContext().getRequestDispatcher(SUBTREE_START_OUTPUT_JSP).include(request, response);
				}
				
				else if (!EventTopologyType.START.equals(event.getType().getTopologyType()) && reader.hasNextEvent() && 
						EventTopologyType.END.equals(reader.peek().getType().getTopologyType())) {
					
					getServletContext().getRequestDispatcher(SUBTREE_END_OUTPUT_JSP).include(request, response);
				}
			}
		}
		
		getServletContext().getRequestDispatcher(SUBTREE_END_OUTPUT_JSP).include(request, response);
		getServletContext().getRequestDispatcher(END_OUTPUT_JSP).include(request, response);
	}
	
	
	private void processStream(HttpServletRequest request, HttpServletResponse response, InputStream stream, String formatID) 
				throws ServletException, LoadingException, ProcessingException, IOException {
		
		try {
			outputEvents(request, response, stream, formatID);  // Note the the current input form does not allow to specify the format type. This might or might not be implemented in the future.
		}
		catch (ServletException | LoadingException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ProcessingException(e);
		}
		finally {
			stream.close();
		}
	}
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			String sourceURL = request.getParameter(PARAM_SOURCE_URL);
			if (sourceURL == null) {
				throw new LoadingException("No source URL was specified.");
			}
			else {
				request.setAttribute(ATTR_SOURCE, StringEscapeUtils.escapeHtml4(sourceURL));
				request.setAttribute(ATTR_VERSION, JPhyloIO.getInstance().getVersion());
				
				InputStream stream;
				try {
					stream = new URL(sourceURL).openStream();
				}
				catch (MalformedURLException e) {
					throw new LoadingException("The specified URL is invalid: " + e.getMessage());
				}
				catch (FileNotFoundException e) {
					throw new LoadingException("The specified file could not be found.");
				}
				catch (Exception e) {
					throw new LoadingException(e.toString());
				}
				
				processStream(request, response, stream, request.getParameter(PARAM_FORMAT));
			}
		}
		catch (ServletException | IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setAttribute(ATTR_VERSION, JPhyloIO.getInstance().getVersion());
		try {
			if (ServletFileUpload.isMultipartContent(request)) {  // File upload
				request.setAttribute(ATTR_SOURCE, "Uploaded file");
				boolean streamProcessed = false;
				FileItemIterator iterator = new ServletFileUpload().getItemIterator(request);
				String formatID = null;
				while (iterator.hasNext()) {
			    FileItemStream item = iterator.next();
			    if (PARAM_FORMAT.equals(item.getFieldName()) && item.isFormField()) {
			    	formatID = IOUtils.toString(item.openStream(), request.getCharacterEncoding());
			    }
			    else if (PARAM_SOURCE_FILE.equals(item.getFieldName()) && !item.isFormField()) {
						processStream(request, response, item.openStream(), formatID);
						streamProcessed = true;
			    }
				}
				
				if (!streamProcessed) {
					throw new LoadingException("No file could be read from the upload stream.");
				}
			}
			else {  // Direct source input
				request.setAttribute(ATTR_SOURCE, "Uploaded content");
				String content = request.getParameter(PARAM_SOURCE_CONTENT);
				if (content != null) {
					processStream(request, response, IOUtils.toInputStream(content, request.getCharacterEncoding()), request.getParameter(PARAM_FORMAT));
				}
				else {
					throw new LoadingException("No content could be read from the upload stream.");
				}
			}
		}
		catch (ServletException | IOException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
	}
}
