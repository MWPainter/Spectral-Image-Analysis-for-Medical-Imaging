package uk.ac.cam.mp703.RandomDecisionForests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/***
 * A split function/weak learner for splitting {@link NDRealVectors} using 1 dimension for each
 * decision. Any instances with a value (in the chosen dimension) strictly lower than the threshold 
 * we will make a choice to traverse left in the tree, otherwise traverse right. 
 * @author michaelpainter
 *
 */
public class OneDimensionalLinearWeakLearner extends WeakLearner {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Minimum values we should pick when generating split parameters. Only used as an internal 
	 * variable to optimise searching the split parameter space.
	 */
	List<Double> minimumValues;
	
	/***
	 * Maximum values we should pick when generating split parameters. Also only used as an internal 
	 * variable to optimise searching the split parameter space. 
	 */
	List<Double> maximumValues;
	
	/***
	 * Return the type of the weak learner.
	 */
	@Override
	public WeakLearnerType getWeakLearnerType() {
		return WeakLearnerType.ONE_DIMENSIONAL_LINEAR;
	}
	
	/***
	 * Using the split parameters make a decision whether to make a left or right choice in the 
	 * traversal of the tree for this instance.
	 */
	@Override
	public Direction split(SplitParameters splitParams, Instance instance) {
		// Cast the split parameters and instance into their correct subclasses
		OneDimensionalLinearSplitParameters params = null;
		NDRealVector vector = null;
		try {
			params = (OneDimensionalLinearSplitParameters) splitParams;
			vector = (NDRealVector) instance;
		} catch (ClassCastException e) {
			throw new IncompatibleInstanceAndWeakLearnerException("The split parameters and "
					+ "instance are incompatible with this weak learner.");
		}
		
		// Now make a left choice if we are strictly lower than the threshold, otherwise right
		if (vector.vector.get(params.dimension) < params.threshold) {
			return Direction.LEFT;
		} else {
			return Direction.RIGHT;
		}
	}
	
	/***
	 * When learning we will need to be able to generate random split parameters to try.
	 */
	@Override
	public SplitParameters generateRandomSplitParameters(int dataDimension) {
		// Get a random instance
		Random rand = ThreadLocalRandom.current();
		
		// Pick one of the dimensions randomly
		int dimension = rand.nextInt(dataDimension);
		
		// Get the minimum and maximum values
		double minValue = Double.MIN_VALUE;
		double maxValue = Double.MAX_VALUE;
		
		if (minimumValues != null) {
			minValue = minimumValues.get(dimension);
		}
		if (maximumValues != null) {
			maxValue = maximumValues.get(dimension);
		}
		
		// Use a random double in [0,1) do generate a value in [minValue, maxValue)
		double cutoffValue = minValue + rand.nextDouble() * (maxValue - minValue);
		
		// Return the generated parameters
		return new OneDimensionalLinearSplitParameters(dimension, cutoffValue);
		
	}
	
	/***
	 * For a one dimensional linear split function, it would be useful to only generate split 
	 * parameters that will lead to two non-empty sub sequences. When give the training sequence 
	 * for a hint we can store the maximum and minimum values in each dimension of the data so that 
	 * we can do this.
	 */
	@Override
	public void giveHint(TrainingSequence trainingSequence) {
		// Initialise the minimum and maximum value vectors
		int dataDimension = trainingSequence.trainingSequence.get(0).instance.getDimension();
		minimumValues = new ArrayList<Double>(dataDimension);
		maximumValues = new ArrayList<Double>(dataDimension);

		try {
			// Initialise minimum and maximum values to the first value in the training sequence
			NDRealVector vector = (NDRealVector) trainingSequence.trainingSequence.get(0).instance;
			for (int i = 0; i < dataDimension; i++) {
				minimumValues.add(i, vector.vector.get(i));
				maximumValues.add(i, vector.vector.get(i));
			}
			
			// Iterate through each sample in the training sequence to find the minimum and maximum 
			// values
			for (TrainingSample sample : trainingSequence.trainingSequence) {
				vector = (NDRealVector) sample.instance;
				for (int i = 0; i < dataDimension; i++) {
					if (vector.vector.get(i) < minimumValues.get(i)) {
						minimumValues.set(i, vector.vector.get(i));
					} else if (vector.vector.get(i) > maximumValues.get(i)) {
						maximumValues.set(i, vector.vector.get(i));
					}
				}
			}
		}
		catch (ClassCastException e) {
			// If there was an invalid cast, then we were passed an instance of the incorrect type
			throw new IncompatibleInstanceAndWeakLearnerException("The instances in the training "
					+ "sequence are incompatible with this weak learner.");
		}
	}
	
	/***
	 * Split parameters for a one dimensional linear split function are simply:
	 * - The dimension that we are using for the choice.
	 * - The value that we use for the decision.
	 * @author michaelpainter
	 *
	 */
	public static class OneDimensionalLinearSplitParameters extends SplitParameters {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/***
		 * The dimension we use to make a decision on the vector given.
		 */
		int dimension;
		
		/***
		 * The threshold used to make a decision.
		 */
		double threshold;
		
		/***
		 * Constructor
		 * @param dimension The dimension to be used for a decision
		 * @param threshold The threshold value to be used for a decision
		 */
		public OneDimensionalLinearSplitParameters(int dimension, double threshold) {
			this.dimension = dimension;
			this.threshold = threshold;
		}
		
		/**
		 * @return the dimension
		 */
		public int getDimension() {
			return dimension;
		}


		/**
		 * @param dimension the dimension to set
		 */
		public void setDimension(int dimension) {
			this.dimension = dimension;
		}


		/**
		 * @return the threshold
		 */
		public double getThreshold() {
			return threshold;
		}


		/**
		 * @param value the value to set
		 */
		public void setThreshold(double threshold) {
			this.threshold = threshold;
		}


		/***
		 * Return the type of the weak learner these parameters are relevant to.
		 */
		@Override
		public WeakLearnerType getWeakLearnerType() {
			return WeakLearnerType.ONE_DIMENSIONAL_LINEAR;
		}	
	}
}
