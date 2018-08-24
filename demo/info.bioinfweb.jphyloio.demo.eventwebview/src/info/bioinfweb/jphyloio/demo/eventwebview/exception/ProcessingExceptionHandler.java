package info.bioinfweb.jphyloio.demo.eventwebview.exception;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.exception.ExceptionUtils;

import info.bioinfweb.commons.servlet.GetPostServlet;



public class ProcessingExceptionHandler extends GetPostServlet {
	public static final String OUTPUT_JSP = "/exception/processingOutput.jsp";

	public static final String ATTR_CAUSES = "causes";
	
	
	public static class ExceptionInfo {
		private String message;
		private String stackTrace;
		
		public ExceptionInfo(String message, String stackTrace) {
			super();
			this.message = message;
			this.stackTrace = stackTrace;
		}
		
		public String getMessage() {
			return message;
		}
		
		public String getStackTrace() {
			return stackTrace;
		}
	}
	
	
	@Override
	protected void doGetPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			Throwable exception = ((Throwable)request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).getCause();  // Tomcat already extracts the servlet exception and this extracts the ProcessingException.

			List<ExceptionInfo> causes = new ArrayList<>();
			while (exception != null) {
				causes.add(new ExceptionInfo(exception.getMessage(), ExceptionUtils.getStackTrace(exception)));
				exception = exception.getCause();
			}
			request.setAttribute(ATTR_CAUSES, causes);
			
			getServletContext().getRequestDispatcher(OUTPUT_JSP).include(request, response);

			
	//		Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
	//		String servletName = (String) request.getAttribute("javax.servlet.error.servlet_name");
	//		if (servletName == null) {
	//			servletName = "Unknown";
	//		}
	//		String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
	//		if (requestUri == null) {
	//			requestUri = "Unknown";
	//		}
		}
		catch (Exception e) {
			if (e instanceof ServletException) {
				throw e;
			}
			else {
				throw new ServletException(e);
			}
		}
	}
}
