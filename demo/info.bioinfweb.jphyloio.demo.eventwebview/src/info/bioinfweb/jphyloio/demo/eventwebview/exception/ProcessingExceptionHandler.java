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
