package uk.ac.cam.mp703.RandomDecisionForests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/***
 * A forest of decision trees, convenience methods are provided to save and load trees and use the trees
 * to vote in a classification procedure. 
 * @author michaelpainter
 *
 */
public class DecisionForest implements Cloneable, Serializable {
	/**
	 * Serializable ID number
	 */
	private static final long serialVersionUID = 1L;

	/*** 
	 * Set of root nodes 
	 */
	Set<TreeNode> rootNodes;
	
	/*** 
	 * Data dimension of the tree - dimension of the data that this tree will classify 
	 */
	int dataDimension;
	
	/*** 
	 * Map from classification numbers to classifications. 
	 * Each class has a description (string) and an associated number, when we classify a 
	 * piece of data get a number, the number is an index into this array.
	 */
	List<String> classStrings;

	/***
	 * What type of weak learner the tree uses.
	 */
	WeakLearnerType weakLearnerType;

	/**
	 * @return the rootNodes
	 */
	public Set<TreeNode> getRootNodes() {
		return rootNodes;
	}

	/**
	 * @param rootNodes the rootNodes to set
	 */
	public void setRootNodes(Set<TreeNode> rootNodes) {
		this.rootNodes = rootNodes;
	}

	/**
	 * @return the dataDimension
	 */
	public int getDataDimension() {
		return dataDimension;
	}

	/**
	 * @param dataDimension the dataDimension to set
	 */
	public void setDataDimension(int dataDimension) {
		this.dataDimension = dataDimension;
	}

	/**
	 * @return the classStrings
	 */
	public List<String> getClassStrings() {
		return classStrings;
	}

	/**
	 * @param classStrings the classStrings to set
	 */
	public void setClassStrings(List<String> classStrings) {
		this.classStrings = classStrings;
	}

	/**
	 * @return the weakLearnerType
	 */
	public WeakLearnerType getWeakLearnerType() {
		return weakLearnerType;
	}

	/**
	 * @param weakLearnerType the weakLearnerType to set
	 */
	public void setWeakLearnerType(WeakLearnerType weakLearnerType) {
		this.weakLearnerType = weakLearnerType;
	}

	/*** 
	 * Load a forest from a .frst file
	 * @param filename The name of the file we will load from
	 * @throws IOException 
	 * @throws FileNotFoundException  
	 * @throws FileFormatException 
	 */
	public DecisionForest(String filename) throws FileNotFoundException, IOException, FileFormatException {
		try {
			// Get an input stream from the filename 
			if (!filename.endsWith(".frst")) {
				filename += ".frst";
			}
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
			
			// Read in the decision forest and take a shallow copy
			DecisionForest frst = (DecisionForest) in.readObject();
			this.rootNodes = frst.rootNodes;
			this.dataDimension = frst.dataDimension;
			this.classStrings = frst.classStrings;
			this.weakLearnerType = frst.weakLearnerType;
			
			// Close the input stream
			in.close();
		} catch (ClassNotFoundException e) {
			throw new FileFormatException(e.getMessage());
		}
	}
	
	/***
	 * Default constructor
	 */
	DecisionForest() {
	}

	/*** 
	 * Save a forest to a .frst file
	 * @param filename The name of the file we are saving to 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void saveToFrstFile(String filename) throws FileNotFoundException, IOException {
		// Create an file to output to, write this forest object to file and then close the stream
		// Get an input stream from the filename 
		if (!filename.endsWith(".frst")) {
			filename += ".frst";
		}
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
		out.writeObject(this);
		out.close();
	}
	
	/***
	 * Use the decision forest to classify feature vectors. Use the trees in the forest to vote and return a majority vote.
	 * @param splitter Implements the WeakLearner interface, containing one function used to make decisions at each node 
	 * @param instance The feature vector/instance to be classified
	 * @return Returns the number corresponding to the majority classification, if -1 then it was undetermined
	 * @throws MalformedForestException If any tree in the forest is malformed it will throw a MalformedForestException
	 * @throws MalformedProbabilityDistributionException 
	 */
	public ProbabilityDistribution classify(Instance instance) 
			throws MalformedForestException, MalformedProbabilityDistributionException {
		
		// Create a splitter depending on our weak learner type
		WeakLearner splitter = null;
		switch (weakLearnerType) {
			case ONE_DIMENSIONAL_LINEAR:
				splitter = new OneDimensionalLinearWeakLearner();
				break;
			default:
				throw new MalformedForestException("No weak learner for the type given, unable to classify.");
		}
		
		// Initialize an accumulating distribution for summing over/voting
		int numberOfClasses = classStrings.size();
		Map<Integer, Double> outputDistr = new HashMap<Integer, Double>(numberOfClasses);
		for (int i = 0; i < numberOfClasses; i++) {
			outputDistr.put(i, 0.0);
		}
		
		// Itterate through all tree's votes, and sum the distributions
		for (TreeNode node : rootNodes) {
			Map<Integer, Double> leafDistr = traverseTree(node, splitter, instance).getProbabilityDistribution();
			for (int i = 0; i < numberOfClasses; i++) {
				outputDistr.put(i, outputDistr.get(i) + leafDistr.get(i));
			}
		}
		
		// Normalise the distribution 
		// Mathematically this can be done by dividing by the tree count, however we will have 
		// floating point error
		double normalisationFactor = 0.0;
		for (int i = 0; i < numberOfClasses; i++) {
			normalisationFactor += outputDistr.get(i);
		}
		
		for (int i = 0; i < numberOfClasses; i++) {
			outputDistr.put(i, outputDistr.get(i) / normalisationFactor);
		}
		
		// Return the probability distribution
		return new ProbabilityDistribution(outputDistr, numberOfClasses);
	}
	
	/***
	 * Traverse a tree using a given split function
	 * @param rootNode Root node of the tree to traverse
	 * @param splitter Implements the WeakLearner interface, containing one function used to make decisions at each node 
	 * @param instance The feature vector/instance that is being classified currently
	 * @return The probability distribution of class instances 
	 * @throws MalformedForestException If the tree is malformed it will throw a MalformedForestException
	 */
	ProbabilityDistribution traverseTree(TreeNode rootNode, WeakLearner splitter, Instance instance) throws MalformedForestException {
		try {
			// Use the split function to traverse the tree until we hit a root node
			TreeNode currentNode = rootNode;
			while (!currentNode.isLeafNode()) {
				Direction splitDirection = splitter.split(currentNode.splitParams, instance);
				if (splitDirection == Direction.LEFT) {
					currentNode = currentNode.leftChild;
				} else {
					currentNode = currentNode.rightChild;
				}
			}
			
			// Return the class associated with the root node
			return currentNode.probabilityDistribution;
			
		} catch (NullPointerException ex) {
			// If we get a null pointer, then we must have a malformed tree
			throw new MalformedForestException("Failed traversing a tree due to null pointer exception.");
		}
	}
	
	/***
	 * Data structure for nodes in a tree. 
	 * N.B. We don't provide an explicit tree class, as we are providing forests.
	 */
	static class TreeNode implements Serializable {
		/**
		 * Serializable ID number
		 */
		private static final long serialVersionUID = 1L;

		/*** 
		 * Child node - null iff leaf node 
		 */
		TreeNode leftChild;
		
		/***
		 * Child node - null iff leaf node 
		 */
		TreeNode rightChild;
		
		/*** 
		 * Class probability distribution - map from class numbers to doubles (probabilities)
		 */
		ProbabilityDistribution probabilityDistribution;
		
		/*** 
		 * Payload - the parameters for the split function to use at this node 
		 */
		SplitParameters splitParams;
		
		/***
		 * Defualt constructor
		 */
		TreeNode() {
			
		}
		
		/***
		 * Constructor from a sequence, for leaf node (no children)
		 * @throws MalformedProbabilityDistributionException 
		 */
		TreeNode(TrainingSequence sequence) throws MalformedProbabilityDistributionException {
			this.probabilityDistribution = sequence.empiricalDistribution();
		}
		
		/***
		 * Constructor from a sequence, with child nodes and split parameters
		 * @throws MalformedProbabilityDistributionException 
		 */
		TreeNode(TrainingSequence sequence, TreeNode leftChld, TreeNode rightChld, 
				SplitParameters splitParams) throws MalformedProbabilityDistributionException {

			this.probabilityDistribution = sequence.empiricalDistribution();
			this.leftChild = leftChld;
			this.rightChild = rightChld;
			this.splitParams = splitParams;  
		}

		/**
		 * @return the leftChild
		 */
		public TreeNode getLeftChild() {
			return leftChild;
		}

		/**
		 * @param leftChild the leftChild to set
		 */
		public void setLeftChild(TreeNode leftChild) {
			this.leftChild = leftChild;
		}

		/**
		 * @return the rightChild
		 */
		public TreeNode getRightChild() {
			return rightChild;
		}

		/**
		 * @param rightChild the rightChild to set
		 */
		public void setRightChild(TreeNode rightChild) {
			this.rightChild = rightChild;
		}

		/**
		 * @return the probabilityDistribution
		 */
		public ProbabilityDistribution getProbabilityDistribution() {
			return probabilityDistribution;
		}

		/**
		 * @param classProbabilities the classProbabilities to set
		 */
		public void setClassProbabilities(ProbabilityDistribution probabilityDistribution) {
			this.probabilityDistribution = probabilityDistribution;
		}

		/**
		 * @return the splitParams
		 */
		public SplitParameters getSplitParams() {
			return splitParams;
		}

		/**
		 * @param splitParams the splitParams to set
		 */
		public void setSplitParams(SplitParameters splitParams) {
			this.splitParams = splitParams;
		}
		
		/***
		 * Checks if the node is a leaf node
		 * Throws an error if we have a single child (we should have none or two)
		 * @return
		 * @throws MalformedForestException 
		 */
		public boolean isLeafNode() throws MalformedForestException {
			// Check that it is a valid decision node, or root node
			if (this.leftChild == null && this.rightChild != null || 
					this.leftChild != null && this.rightChild == null) {
				throw new MalformedForestException("A tree node exists with a single child, this "
						+ "should never happen");
			}
			
			// Return true if it has no children
			return (this.leftChild == null && this.rightChild == null);
		}
		
		/***
		 * Given a tree we compact nodes to avoid unnecessary traversal. For example, if we have a 
		 * choice node, leading to two leaf nodes which give the same class, then replace it with a 
		 * single leaf node that votes the given class.
		 * @param tree 
		 * @throws MalformedForestException 
		 */
		void compact() throws MalformedForestException {
			// If we are a leaf node no compaction can take place, we are already as compact as possible
			if (this.isLeafNode()) {
				return;
			}
			
			// If we are a choice node, first compact our child nodes
			this.leftChild.compact();
			this.rightChild.compact();
			
			// Now check if the two leaf nodes have the same probability distribution. If the tree's 
			// children were generated using sequences S_L and S_R then our probability distribution
			// at this node is defined by
			// p(x) = (T_L / T_T) * p_L(x) + (T_R / T_T) * p_R(x)
			//      = p_L(x) = p_R(x)
			// where T_i = |S_i|, T_T = T_L+T_R and p_i(x) are the probability distributions of the children
			if (!this.isLeafNode() && 
					this.probabilityDistribution.equals(this.leftChild.probabilityDistribution) &&
					this.probabilityDistribution.equals(this.rightChild.probabilityDistribution)) {
				
				// At this point making a 'decision' leads to the same result, which is the SAME 
				// distribution we have at this node also. So just forget the children, making this
				// a leaf
				this.leftChild = null;
				this.rightChild = null;
			}
		}
	}
}
