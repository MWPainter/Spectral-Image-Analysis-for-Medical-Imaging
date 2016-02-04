package uk.ac.cam.mp703.RandomDecisionForests;

public class TrainingSequenceFormatException extends RuntimeException {
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Default constructor
	 */
	public TrainingSequenceFormatException() {	
	}
	
	/***
	 * Constructor with message
	 * @param msg The message to be passed to the exception
	 */
	public TrainingSequenceFormatException(String msg) {
		super(msg);
	}
}
