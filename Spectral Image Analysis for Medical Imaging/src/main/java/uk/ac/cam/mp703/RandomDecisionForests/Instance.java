package uk.ac.cam.mp703.RandomDecisionForests;

/***
 * Abstract class for feature vectors/instances. We don't specify anything about what form the data 
 * is in at this point, but we require that a dimension is specified.
 * @author michaelpainter
 *
 */
public interface Instance {

	/***
	 * Get the dimension of the data this instance contains.
	 * @return The dimension of data in the instance
	 */
	public int getDimension();
	
	/***
	 * Get a normalisation reference
	 * I.e. What is the "power" value of this instance?
	 * @return The "power" value of this instance
	 */
	public double getNormalisationReference(); 
	
	/***
	 * Normalise with respect to some normalisation reference (which could be ignored!)
	 */
	public void normalise(double referenceValue);
}

