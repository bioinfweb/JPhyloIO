package info.bioinfweb.jphyloio.demo.eventwebview.exception;



/**
 * Exceptions of this type are thrown if an error occurs when processing a loaded input file with <i>JPhyloIO</i>.
 * 
 * @author Ben St&ouml;ver
 */
public class ProcessingException extends EventWebViewException {
	public ProcessingException(Throwable throwable) {
		super("An error occurred when processing the input file.", throwable);
	}
}
