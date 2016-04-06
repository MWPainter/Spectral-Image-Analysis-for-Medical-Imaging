package uk.ac.cam.mp703.RandomDecisionForests;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uk.ac.cam.mp703.RandomDecisionForests.DecisionForest.TreeNode;


public class Learner implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Construct a forest using a given weak learner, given a training sequence.
	 * 
	 * @param trainingSequence The training sequence (list of (vector, class) pairs) to generate a 
	 * 		forest
	 * @param weakLearner An instance of the weak learner that we will be using for this tree 
	 * @param maxTrees The maximum number of trees that will be in the forest
	 * @param maxDepth The maximum depth of any tree in the forest
	 * @param randomnessParameter The number of times we will randomly try a value in the 
	 * 		split parameter space, for each tree node
	 * @param rand An instance of Random that is used for generating split params
	 * @param informationGainCutoff The cutoff for how much information is gained at a node, if 
	 * 		we don't find a division that leads to such a gain, then we don't bother splitting.
	 * @param normaliseInstances Should a normalisation of instances be taken when we classify?
	 * @return Returns a trained DecisionForest from the training sequence
	 * @throws MalformedForestException 
	 * @throws MalformedProbabilityDistributionException 
	 * @throws Exception 
	 */
	public static DecisionForest trainDecisionForest(TrainingSequence trainingSequence, 
			WeakLearner weakLearner, int maxTrees, int maxDepth, int randomnessParameter,
			double informationGainCutoff, boolean normaliseInstances) 
					throws MalformedForestException, MalformedProbabilityDistributionException {
		
		// If we are given nonsensical input, throw an exception
		if (trainingSequence == null || trainingSequence.size() == 0 ||
				trainingSequence.classNames.size() == 0) {
			throw new InvalidParameterException("We need a non-empty, non-null training sequence to "
					+ "train a tree.");
		} else if (weakLearner == null) {
			throw new InvalidParameterException("We need a non-null weak learner to be able to train "
					+ "a tree.");
		} else if (maxTrees <= 0) {
			throw new InvalidParameterException("A non-positive number of trees doesn't make sense "
					+ "for a forest.");
		} else if (maxDepth <= 0) {
			throw new InvalidParameterException("A non-negative depth doesn't make sense for a tree. A "
					+ "tree consisting of a single node has depth 0. If we limit trees to be just "
					+ "leaves then there is no decisions being made - the tree does nothing.");
		} else if (randomnessParameter <= 0) {
			throw new InvalidParameterException("We need a positive randomness parameter, it doens't "
					+ "make sense to have a negative quantity.");
		} else if (informationGainCutoff < 0.0) {
			throw new InvalidParameterException("Information gain is a strictly non-negative value, "
					+ "so must have a cutoff of more than or equal to zero.");
		}
		
		// Normalise the training sequence if we want that
		// Remember the reference value (power)
		double normalisationReference = 0.0;
		if (normaliseInstances) {
			normalisationReference = trainingSequence.normalise();
		}
		
		// Create a random instance
		Random rand = new Random();
		
		// Create a DecisionForest instance
		DecisionForest forest = new DecisionForest();
		forest.setDataDimension(trainingSequence.trainingSequence.get(0).instance.getDimension());
		forest.setClassStrings(trainingSequence.classNames);
		forest.setWeakLearnerType(weakLearner.getWeakLearnerType());

		// Now build each of the trees in turn, compact it, and add it to the root set
		Set<DecisionForest.TreeNode> rootNodes = new HashSet<>();
		for (int i = 0; i < maxTrees; i++) {
			TreeNode node = generateTree(trainingSequence, weakLearner, maxDepth, 
					randomnessParameter, rand, informationGainCutoff);
			node.compact();
			rootNodes.add(node);
		}
		
		// Add the root nodes to the forest structure
		forest.rootNodes = rootNodes;
		
		// Add normalisation variables to the forest structure
		forest.normalisedClassification = normaliseInstances;
		forest.normalisationReference = normalisationReference;
		
		// Return the constructed forest
		return forest;
	}
	
	/***
	 * Generate a single tree of a given depth. This function is called recursively to build the 
	 * tree.
	 * @param trainingSequence The sequence given to build the tree.
	 * @param weakLearner The weak learner to use.
	 * @param maxDepth The depth of the tree that we are building
	 * @param randomnessParameter The number of split parameters to try at each node
	 * @param rand An instance of Random that is used for generating split params
	 * @param informationGainCutoff The cutoff for how much information is gained at a node, if 
	 * 		we don't find a division that leads to such a gain, then we don't bother splitting.
	 * @return Return a randomly trained tree
	 * @throws MalformedProbabilityDistributionException 
	 * @throws Exception 
	 */
	private static TreeNode generateTree(TrainingSequence trainingSequence, 
			WeakLearner weakLearner, int depth, int randomnessParameter, Random rand,
			double informationGainCutoff) 
					throws MalformedProbabilityDistributionException {
		// Check for the training sequence being 0 in size, that should never happen
		if (trainingSequence.size() == 0) {
			throw new InvalidParameterException("Can't generate a tree from an empty sequence.");
		}
		
		// If the depth is zero or there is only one sample in the training sequences, then we need 
		// to create a leaf node, take the class as the majority vote from the training sequence.
		if (depth <= 0 || trainingSequence.size() == 1) {
			return new TreeNode(trainingSequence);
		}
		
		// Give the weak learner the training sequence as a hint for what subspace of the split 
		// parameter space it should search
		weakLearner.giveHint(trainingSequence);
		
		// We will generate split paramters, and we will keep track of the
		// Initialise best information gain to -1.0 so we explicitly set it at least once in the loop
		// as entropies are non negative
		double bestInformationGain = -1.0;
		TrainingSequence bestLeftSplit = null;
		TrainingSequence bestRightSplit = null;
		SplitParameters bestSplitParameters = null;
		int dataDimension = trainingSequence.trainingSequence.get(0).instance.getDimension();
		
		// Try "randomnessParameter" number of random split parameters
		for (int i = 0; i < randomnessParameter; i++) {
			SplitParameters splitParameters = weakLearner.generateRandomSplitParameters(dataDimension, rand);
			List<TrainingSample> leftList = new ArrayList<>();
			List<TrainingSample> rightList = new ArrayList<>();
			
			// Split the training sequence into a left and right split
			for (TrainingSample sample : trainingSequence.trainingSequence) {
				if (weakLearner.split(splitParameters, sample.instance) == Direction.LEFT) {
					leftList.add(sample);
				} else {
					rightList.add(sample);
				}
			}
			
			// See what information gain this leads to
			TrainingSequence leftSplit = new TrainingSequence(leftList, trainingSequence.classNames);
			TrainingSequence rightSplit = new TrainingSequence(rightList, trainingSequence.classNames);
			double informationGain = TrainingSequence.informationGain(leftSplit, rightSplit);
			
			// If its the best information gain found so far, remember it!
			if (informationGain > bestInformationGain) {
				bestInformationGain = informationGain;
				bestLeftSplit = leftSplit;
				bestRightSplit = rightSplit;
				bestSplitParameters = splitParameters;
			}
		}
		
		// We make a greedy choice using the best split that we found, and recursively build our 
		// child nodes.
		// N.B. If the split doesn't gain enough information, then just try again, (so we've used 
		// up "one depth".
		// N.B.B. informationGainCutoff >= 0.0, and if one of the sequences is empty, then there is 
		// and information gain of 0.0, hence if one of the sequences is empty we don't EVER pass 
		// the information gain cutoff. Hence any split that actually causes a recursion will have 
		// two non empty subsequences.
		TreeNode node = null;
		if (bestInformationGain <= informationGainCutoff) {
			node = generateTree(trainingSequence, weakLearner, depth-1, randomnessParameter, 
					rand, informationGainCutoff);
		} else {
			TreeNode leftChild = generateTree(bestLeftSplit, weakLearner, depth-1, randomnessParameter, 
					rand, informationGainCutoff);
			TreeNode rightChild = generateTree(bestRightSplit, weakLearner, depth-1, randomnessParameter, 
					rand, informationGainCutoff);
			node = new TreeNode(trainingSequence, leftChild, rightChild, bestSplitParameters);
		}
		
		// We have trained a tree! Return it
		return node;
	}
}
