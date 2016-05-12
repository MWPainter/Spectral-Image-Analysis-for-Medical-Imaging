package uk.ac.cam.mp703.RandomDecisionForests;

public class MalformedForestException extends RuntimeException {
	/***
	 * Serial id
	 */
	private static final long serialVersionUID = 1L;
	
	/***
	 * Default constructor
	 */
	public MalformedForestException() {
	}
	
	/***
	 * Constructor
	 * @param message Message to be passed to the exception
	 */
	public MalformedForestException(String message) {
		super(message);
	}
}
