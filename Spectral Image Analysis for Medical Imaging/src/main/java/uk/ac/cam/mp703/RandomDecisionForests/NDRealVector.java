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
	 * The power of the spectrum
	 */
	Double power = null;
	
	/***
	 * Construct a vector using a list
	 * @param vector
	 */
	public NDRealVector(List<Double> vector) {
		this.vector = vector;
	}
	
	/***
	 * Get the dimension of the vector/data/instance
	 * @return The dimension of the vector
	 */
	@Override
	public int getDimension() {
		return vector.size();
	}
	
	/***
	 * Get the normalisation reference value, in this case the power of the spectrum
	 * @return The "power" value of this instance
	 */
	@Override
	public double getNormalisationReference() {
		return getPower();
	}
	
	/***
	 * Get the power of the spectrum
	 * @return The power of the spectrum
	 */
	public double getPower() {
		if (power == null) {
			computePower();
		}
		return power;
	}
	
	/***
	 * Compute the power (lazily)
	 */
	private void computePower() {
		// Compute
		double power = getPower();
		for (Double d : vector) {
			power += d * d;
		}
		
		// Set the variable
		this.power = power;
	}
	
	/***
	 * Normalize the spectrum to some reference power
	 */
	@Override
	public void normalise(double power) {
		// Compute the original power
		double originalPower = getPower();
		
		// Return if 0 (any scaling leads to of 0 is 0)
		if (originalPower == 0.0) {
			return;
		}
		
		// Scale values
		// We recompute the new power because of rounding issues
		double ratio = power / originalPower;
		double newPower = 0.0;
		for (int i = 0; i < vector.size(); i++) {
			double val = vector.get(i);
			vector.set(i, val * ratio);
			newPower += val * val;
		}
		
		// Set the new power
		this.power = newPower;
	}
}
