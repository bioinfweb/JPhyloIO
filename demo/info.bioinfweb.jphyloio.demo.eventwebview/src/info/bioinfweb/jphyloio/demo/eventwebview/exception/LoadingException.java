package info.bioinfweb.jphyloio.demo.eventwebview.exception;



/**
 * Exceptions of this type are thrown if an error occurs when accessing the input file before processing it with <i>JPhyloIO</i>.
 * 
 * @author Ben St&ouml;ver
 */
public class LoadingException extends EventWebViewException {
	public LoadingException(String message, Throwable throwable) {
		super(message, throwable);
	}

	
	public LoadingException(String message) {
		super(message);
	}
}
