package uk.ac.cam.mp703.RandomDecisionForests;

public class MalformedProbabilityDistributionException extends Exception {
	/***
	 * Serial id
	 */
	private static final long serialVersionUID = 1L;
	
	/***
	 * Default constructor
	 */
	public MalformedProbabilityDistributionException() {
	}
	
	/***
	 * Constructor
	 * @param message Message to be passed to the exception
	 */
	public MalformedProbabilityDistributionException(String message) {
		super(message);
	}
}
