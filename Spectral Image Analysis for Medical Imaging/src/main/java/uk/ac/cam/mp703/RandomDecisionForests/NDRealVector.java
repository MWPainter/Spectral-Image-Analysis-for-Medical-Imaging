package uk.ac.cam.mp703.RandomDecisionForests;

import java.util.List;

/***
 * NDRealVector is our default type for a concrete Instance class. This is really just a wrapper 
 * around an ArrayList type of Doubles.
 * @author michaelpainter
 * 
 */
public class NDRealVector extends Instance  {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/***
	 * The vector represented as a list of doubles.
	 */
	List<Double> vector;
	
	/***
	 * Construct a vector using a list
	 * @param vector
	 */
	public NDRealVector(List<Double> vector) {
		this.vector = vector;
	}
	
	/***
	 * Get the dimension of the vector/data/instance
	 */
	@Override
	public int getDimension() {
		return vector.size();
	}

}
