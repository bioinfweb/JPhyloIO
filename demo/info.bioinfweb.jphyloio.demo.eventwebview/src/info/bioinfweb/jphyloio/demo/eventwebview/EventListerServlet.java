package info.bioinfweb.jphyloio.demo.eventwebview;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.demo.eventwebview.exception.LoadingException;
import info.bioinfweb.jphyloio.demo.eventwebview.exception.ProcessingException;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.factory.JPhyloIOReaderWriterFactory;



public class EventListerServlet extends HttpServlet {
	public static final String PARAM_SOURCE_URL = "sourceURL";
	public static final String PARAM_FORMAT = "format";
	
	public static final String ATTR_EVENT = "event";
	public static final String ATTR_SOURCE = "source";

	public static final String START_OUTPUT_JSP = "/start.jsp";
	public static final String END_OUTPUT_JSP = "/end.jsp";
	public static final String SUBTREE_START_OUTPUT_JSP = "/subtreeStart.jsp";
	public static final String SUBTREE_END_OUTPUT_JSP = "/subtreeEnd.jsp";
	public static final String EVENT_OUTPUT_JSP = "/eventOutput.jsp";
		
	
	private JPhyloIOReaderWriterFactory factory = new JPhyloIOReaderWriterFactory();
	
	
	
	private void outputEvents(HttpServletRequest request, HttpServletResponse response, InputStream stream, String formatID) throws Exception {
		getServletContext().getRequestDispatcher(START_OUTPUT_JSP).include(request, response);
		
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
				
				if (EventTopologyType.START.equals(event.getType().getTopologyType())) {
					getServletContext().getRequestDispatcher(SUBTREE_START_OUTPUT_JSP).include(request, response);
				}
				
				request.setAttribute(ATTR_EVENT, event);
				getServletContext().getRequestDispatcher(EVENT_OUTPUT_JSP).include(request, response);
				
				if (EventTopologyType.END.equals(event.getType().getTopologyType())) {
					getServletContext().getRequestDispatcher(SUBTREE_END_OUTPUT_JSP).include(request, response);
				}
			}
		}
		
		getServletContext().getRequestDispatcher(END_OUTPUT_JSP).include(request, response);
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
				
				try {
					outputEvents(request, response, stream, request.getParameter(PARAM_FORMAT));
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
		// https://commons.apache.org/proper/commons-fileupload/streaming.html
		throw new ServletException("Post not yet implemented.");
	}
}
