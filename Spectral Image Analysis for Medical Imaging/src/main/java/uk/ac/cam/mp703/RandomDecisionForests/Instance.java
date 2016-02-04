package uk.ac.cam.mp703.RandomDecisionForests;

/***
 * Abstract class for feature vectors/instances. We don't specify anything about what form the data 
 * is in at this point, but we require that a dimension is specified.
 * @author michaelpainter
 *
 */
public abstract class Instance {
	/***
	 * Get the dimension of the data this instance contains.
	 */
	public abstract int getDimension();
}

