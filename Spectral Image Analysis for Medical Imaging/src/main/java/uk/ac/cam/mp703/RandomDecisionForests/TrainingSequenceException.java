package uk.ac.cam.mp703.RandomDecisionForests;

public class TrainingSequenceException extends RuntimeException {
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Default constructor
	 */
	public TrainingSequenceException() {	
	}
	
	/***
	 * Constructor with message
	 * @param msg The message to be passed to the exception
	 */
	public TrainingSequenceException(String msg) {
		super(msg);
	}
}