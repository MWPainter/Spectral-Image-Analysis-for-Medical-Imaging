package uk.ac.cam.mp703.RandomDecisionForests;

public class IncompatibleInstanceAndWeakLearnerException extends RuntimeException {
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Default constructor
	 */
	public IncompatibleInstanceAndWeakLearnerException() {	
	}
	
	/***
	 * Constructor with message
	 * @param msg The message to be passed to the exception
	 */
	public IncompatibleInstanceAndWeakLearnerException(String msg) {
		super(msg);
	}
}
