package uk.ac.cam.mp703.RandomDecisionForests;

import java.io.Serializable;
import java.util.Map;

/***
 * This class represents a probability distribution, and basically just wraps a Map from classes to 
 * probabilities. We pre-compute some properties (such as entropy) for speed during classification.
 * @author michaelpainter
 *
 */
public class ProbabilityDistribution implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * The maximum class number in the distribution
	 * So we have class numbers 0, 1, ..., maxClassNumber
	 */
	final int maxClassNumber;
	
	/***
	 * A mapping from class numbers to probabilities
	 */
	final Map<Integer, Double> probabilities;
	
	/***
	 * The most probable classification
	 */
	final int mostProbableClass;
	
	/***
	 * The entropy of this distribution
	 */
	final double entropy;
	
	/***
	 * Constructor
	 * @param distribution The initial probability distribution
	 * @param noClasses How many classifications that the distribution spans
	 * @throws MalformedProbabilityDistributionException 
	 */
	public ProbabilityDistribution(Map<Integer, Double> distribution, int noClasses) 
			throws MalformedProbabilityDistributionException {
		
		// Validate that the distribution is correct
		double sum = 0.0;
		for (Map.Entry<Integer, Double> entry : distribution.entrySet()) {
			// Classes correct
			if (entry.getKey() < 0 || entry.getKey() >= noClasses) {
				throw new MalformedProbabilityDistributionException("Distribution includes an invalid class "
						+ "number");
			}
			
			// Prob correct
			if (entry.getValue() < 0.0 || entry.getValue() > 1.0) {
				throw new MalformedProbabilityDistributionException("Invalid probability, not in interval [0,1]");
			}
			
			sum += entry.getValue();
		}
		
		// Sum is approximately correct, we need to account for floating point error
		double eps = 1e-10;
		if (sum < 1.0-eps || 1.0+eps < sum) {
			throw new MalformedProbabilityDistributionException("Probabilities don't sum to one");
		}
		
		// Assign variables
		this.probabilities = distribution;
		this.maxClassNumber = noClasses;
		this.mostProbableClass = computeMostProbableClass();
		this.entropy = computeEntropy();
	}
	
	/***
	 * Get the number of possible classifications
	 * @return Number of classes
	 */
	public int getMaxClassNumber() {
		return this.maxClassNumber;
	}
	
	/***
	 * Get the distributions
	 * @return Map representing the distributions
	 */
	public Map<Integer, Double> getProbabilityDistribution() {
		return this.probabilities;
	}
	
	/***
	 * Compute the most probable class
	 * @return The most probable class number
	 */
	private int computeMostProbableClass() {
		int classNo = 0;
		double prob = 0.0;
		for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
			if (entry.getValue() > prob) {
				classNo = entry.getKey();
				prob = entry.getValue();
			}
		}
		return classNo;
	}

	/***
	 * Get the most probable class
	 * @return The most probable class number
	 */
	public int mostProbableClass() {
		return this.mostProbableClass;
	}
	
	/***
	 * Compute the entropy of the distribution
	 * @return The entropy of the distribution
	 */
	private double computeEntropy() {
		// Loop over all elements in the sum
		// Be careful to avoid the case when prob = 0 as it may give an infinity
		double entropy = 0.0; 
		for (int i = 0; i < probabilities.size(); i++) {
			double prob = probabilities.get(i);
			if (prob != 0.0) {
				entropy -= prob * Math.log(prob);
			}
		}
		
		// Return the entropy, the division by log(2) is because we worked in log base e rather 
		// than log base 2
		return entropy / Math.log(2);
	}

	/***
	 * Get the entropy of the distribution
	 * @return The entropy of the distribution
	 */
	public double entropy() {
		return this.entropy;
	}
	
	/***
	 * Determine if two probability distributions are the same
	 * We note that 
	 * @return If the two probabilities are considered equal or not
	 */
	@Override
	public boolean equals(Object probDistr) {
		return this.probabilities.equals(((ProbabilityDistribution) probDistr).probabilities);
	}
	
	/***
	 * As we override equals, we should also override hash code. Two objects that are equal have 
	 * equal hashes, and hence we can just return the hash of the probability distribution, as that 
	 * completely determines the distribution
	 */
	@Override
	public int hashCode() {
		return probabilities.hashCode();
	}
}
