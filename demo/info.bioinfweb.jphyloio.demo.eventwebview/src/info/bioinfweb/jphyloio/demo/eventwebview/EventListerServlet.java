package info.bioinfweb.jphyloio.demo.eventwebview;


import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringEscapeUtils;

import info.bioinfweb.jphyloio.JPhyloIOEventReader;
import info.bioinfweb.jphyloio.ReadWriteParameterMap;
import info.bioinfweb.jphyloio.events.JPhyloIOEvent;
import info.bioinfweb.jphyloio.events.type.EventTopologyType;
import info.bioinfweb.jphyloio.factory.JPhyloIOReaderWriterFactory;



public class EventListerServlet extends HttpServlet {
	public static final String PARAM_SOURCE_URL = "sourceURL";
	public static final String PARAM_FORMAT = "format";
	
	public static final String ATTR_EVENT = "event";
	public static final String ATTR_INDENTION= "indention";
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
			throw new ServletException("No reader found.");
			//TODO Output error (Redirect to special JSP)
		}
		else {
			int indentionLevel = 0;
			while (reader.hasNextEvent()) {
				JPhyloIOEvent event = reader.next();
				
				if (EventTopologyType.START.equals(event.getType().getTopologyType())) {
					getServletContext().getRequestDispatcher(SUBTREE_START_OUTPUT_JSP).include(request, response);
				}
				if (EventTopologyType.END.equals(event.getType().getTopologyType())) {
					indentionLevel--;
				}
				
				request.setAttribute(ATTR_EVENT, event);
				request.setAttribute(ATTR_INDENTION, indentionLevel);
				getServletContext().getRequestDispatcher(EVENT_OUTPUT_JSP).include(request, response);
				
				if (EventTopologyType.START.equals(event.getType().getTopologyType())) {
					indentionLevel++;
				}
				if (EventTopologyType.END.equals(event.getType().getTopologyType())) {
					getServletContext().getRequestDispatcher(SUBTREE_END_OUTPUT_JSP).include(request, response);
				}
			}
		}
		
		getServletContext().getRequestDispatcher(END_OUTPUT_JSP).include(request, response);
	}
	
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String sourceURL = request.getParameter(PARAM_SOURCE_URL);
		if (sourceURL == null) {
			 //TODO Output error (or possibly handle text case)
			throw new ServletException("No source URL was specified.");  //TODO This may or may not have to be replaced, depending on how outputting errors is implemented.
		}
		else {
			request.setAttribute(ATTR_SOURCE, StringEscapeUtils.escapeHtml4(sourceURL));
			
			InputStream stream = new URL(sourceURL).openStream();  //TODO Catch MalformedURLException with a meaningful error message to the user here or outside of the method.
			try {
				outputEvents(request, response, stream, request.getParameter(PARAM_FORMAT));
			}
			catch (ServletException e) {
				throw e;
			}
			catch (Exception e) {
				throw new ServletException(e);  //TODO Exceptions should be presented to the user using a layouted page and it should be indicated whether it was a system error or an error when parsing the file. This could either be done by a custom output page on project level (which should be possible) or by catching JPhyloIO exceptions here.
			}
			finally {
				stream.close();
			}
		}
	}


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// https://commons.apache.org/proper/commons-fileupload/streaming.html
		throw new ServletException("Post not yet implemented.");
	}
}
