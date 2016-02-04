package uk.ac.cam.mp703.RandomDecisionForests;

/***
 * Exception to be thrown when trying to load from a incorrectly formatted file
 * @author michaelpainter
 *
 */
public class FileFormatException extends Exception {
	
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Default constructor
	 */
	public FileFormatException() {	
	}
	
	/***
	 * Constructor with message
	 * @param msg The message to be passed to the exception
	 */
	public FileFormatException(String msg) {
		super(msg);
	}
}
