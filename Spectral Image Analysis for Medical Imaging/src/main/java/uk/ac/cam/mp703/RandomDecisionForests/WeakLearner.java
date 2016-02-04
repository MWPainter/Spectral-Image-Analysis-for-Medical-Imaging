package uk.ac.cam.mp703.RandomDecisionForests;

import java.security.InvalidParameterException;
import java.util.Random;

/***
 * A weak learner contains a split function. It takes two arguments, split parameters and 
 * an instance. We call this function a weak learner as the parameters are learned from 
 * training. The weak learner is used after training to make decisions at each node of a 
 * decision tree, and the parameters allow flexibility in the function to make decisions 
 * accordingly.
 * 
 * Example weak learner: 
 * An example is where we split points in the feature space (instances are elements of the
 * feature space) into two according to a hyper-plane. All instances on one 'side' of the 
 * hyper-plane are split into a group, and instances on the other side are split into a 
 * second group. In this case the 'split parameters' are the variables needed to uniquely 
 * distinguish the hyper-plane. I.e. a point and a normal vector.
 *  
 * @author michaelpainter
 *
 */
public abstract class WeakLearner {
	/***
	 * A weak learner must identify as one of the given weak learner types.  
	 */
	public abstract WeakLearnerType getWeakLearnerType();
	
	/***
	 * A function to make decisions at nodes in a decision tree.
	 * @param splitParams Parameters passed to the function by the node
	 * @param instance The feature vector/instance being classified by the tree
	 * @return Returns The direction in which we need to move for the next node 
	 */
	public abstract Direction split(SplitParameters splitParams, Instance instance);
	
	/***
	 * Weak learners need to have a method to generate random split parameters. The learning method 
	 * is general and uses it in training.
	 * @param dataDimension We pass in the dimension of the data to help generate the parameter
	 * @param rand An instance of Random which should be used to generate random values
	 * @return Returns an instance of SplitParameters relevant for the given weak learner
	 */
	public abstract SplitParameters generateRandomSplitParameters(int dataDimension, Random rand);
	
	/***
	 * Give a weak learner a chance to look at the training sequence as a hint of what split 
	 * parameters it might want to use.
	 * @param trainingSequence The training sequence being used for training
	 */
	public abstract void giveHint(TrainingSequence trainingSequence);
	
	/***
	 * Java math and random libraries don't give a method a method to generate a double within a 
	 * given range, especially when higherBound-lowerBound > Double.MAX_VALUE
	 * @param lowerBound The lowest number that we want to generate
	 * @param higherBound The highest number that we want to generate
	 * @param rand An instance of Random
	 * @return A randomly distributed random number in the interval [lowerBound, higherBound]
	 */
	public final double uniformRandomDoubleInRange(double lowerBound, double higherBound, Random rand) {
		// First check for silly input, and handle each case appropriately
		if (lowerBound == higherBound) {
			return lowerBound;
		} 
		if (lowerBound > higherBound) {
			throw new InvalidParameterException("The higher bound needs to be larger than the lower bound");
		}
		if (Double.isNaN(lowerBound) || Double.isNaN(higherBound)) {
			throw new InvalidParameterException("Neither bound can be NaN");
		}
		
		// If one of the bounds is infinite then alter it to the max value
		if (lowerBound == Double.NEGATIVE_INFINITY) {
			lowerBound = -Double.MAX_VALUE;
		}
		if (higherBound == Double.POSITIVE_INFINITY) {
			higherBound = Double.MAX_VALUE;
		}
		
		// If the range is 'infinite', then split the interval in half, pick one of them randomly
		// and pick a uniform variable in that. If we don't have an 'infinite' range then pick 
		// a random number in the normal way
		double range = higherBound - lowerBound;
		
		if (Double.isInfinite(range)) {
			double average = lowerBound*0.5 + higherBound*0.5;
			double halfRange = average - lowerBound;
			return (rand.nextBoolean()) ? lowerBound + halfRange * rand.nextDouble() 
					: average + halfRange * rand.nextDouble();		
		}
		
		return lowerBound + range * rand.nextDouble();
		
	}
}
