package uk.ac.cam.mp703.ImagePixelLabelling;

public class DataCubeException extends RuntimeException {
	/**
	 * Version ID
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Default constructor
	 */
	public DataCubeException() {	
	}
	
	/***
	 * Constructor with message
	 * @param msg The message to be passed to the exception
	 */
	public DataCubeException(String msg) {
		super(msg);
	}
}
