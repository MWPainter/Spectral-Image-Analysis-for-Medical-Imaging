package uk.ac.cam.mp703.RandomDecisionForests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

/***
 * A simple class to contain a training sequence.
 * @author michaelpainter
 *
 */
public class TrainingSequence implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * List of training samples forming the training sequence
	 */
	List<TrainingSample> trainingSequence;
	
	/***
	 * The list of class names is specified in the file and we should keep a mapping.
	 * The forests must keep a mapping from class numbers to class names, which is part of training.
	 */
	List<String> classNames;

	/***
	 * Only a getter is provided for the training sequence as we shouldn't need to change it.
	 * @return the trainingSequence
	 */
	public List<TrainingSample> getTrainingSequence() {
		return trainingSequence;
	}

	/**
	 * This initially seems unecessary, but we will be splitting training sequences up through the 
	 * training proceedure, so need a method to set the list of training samples.
	 * @param trainingSequence the trainingSequence to set
	 */
	public void setTrainingSequence(List<TrainingSample> trainingSequence) {
		this.trainingSequence = trainingSequence;
	}
	
	/**
	 * @return the classNames
	 */
	public List<String> getClassNames() {
		return classNames;
	}

	/**
	 * @param classNames the classNames to set
	 */
	public void setClassNames(List<String> classNames) {
		this.classNames = classNames;
	}
	
	/***
	 * @return The number of classes in this sequence
	 */
	public int getNoClasses() {
		return this.classNames.size();
	}
	
	/*** 
	 * @return Size of the training sequence
	 */
	public int size() {
		return trainingSequence.size();
	}
	
	/***
	 * Constructor for a training sequence
	 * @param trainingSequence The sequence of (Class,Instance) pairs
	 * @param classNames The array of class names used in the training sequence 
	 */
	public TrainingSequence(List<TrainingSample> trainingSequence, List<String> classNames) {
		this.trainingSequence = trainingSequence;
		this.classNames = classNames;
	}

	/***
	 * Load in a training sequence for Nd real vector instances from a text file. Note that loading 
	 * in training sequences needs to be specific to the type of instance. 
	 * The format of the text file should be:
	 * ------------------------------
	 * | Class1Name, 0xFFFFFF, 		|
	 * | Class2Name, 0xFFFFFF, 		|
	 * | Class3Name, 0xFFFFFF, 		|
	 * | ...		 				|
	 * | ClassMName, 0xFFFFFF; 		|
	 * | c_1,x_11,x_12,...,x_1n;	|
	 * | c_2,x_21,x_22,...,x_2n;	|
	 * | c_3,x_31,x_32,...,x_3n;	|
	 * | c_4,x_41,x_42,...,x_4n;	|
	 * | c_5,x_51,x_52,...,x_5n;	|
	 * | ...						|
	 * | c_k,x_k1,x_k2,...,x_kn;	|
	 * ------------------------------
	 * Where m is the number of classes, c's are classes, x's are feature vectors, each x_ij is a 
	 * real number, n is the dimension of data and k is the number of samples in the sequence
	 * 
	 * N.B. new lines are ignored, only the commas and semi-colons are used for reading. All class 
	 * names must begin with an alphabetic character, but they may use any valid unicode characters 
	 * subsequently.
	 * 
	 * N.B.B. The numbers 0xFFFFFF stand for a hex number that should correspond to the colour 
	 * associated with the class.
	 * 
	 * @param filename The text file 
	 * @throws TrainingSequenceFormatException
	 * @throws FileNotFoundException 
	 */
	public static TrainingSequence newNDRealVectorTrainingSequence(String filename) 
			throws TrainingSequenceFormatException, FileNotFoundException {
		
		// Open the file using a Scanner, and use ";" to initially separate out the data
		// N.B. We use the \s at the beginning and end to ignore unnecessary whitespace.
		Scanner scanner = new Scanner(new File(filename));
		scanner.useDelimiter("\\s*;\\s*");
		
		// Get the array of class names (from the first string from the scanner) and using a 
		// secondary scanner, which has a delimiter of "," with surrounding whitespace.
		List<String> classNames = new ArrayList<String>();
		String classNameString = scanner.next(); 
		Scanner classNameScanner = new Scanner(classNameString);
		classNameScanner.useDelimiter("\\s*,\\s*");
		while (classNameScanner.hasNext()) {
			classNames.add(classNameScanner.next());
		}
		classNameScanner.close();
		
		// Check that all class names are unique, and begin with an alphabetic character
		Set<String> uniqueClassNames = new HashSet<String>();
		for (int i = 0; i < classNames.size(); i++) {
			if (uniqueClassNames.contains(classNames.get(i))) {
				scanner.close();
				throw new TrainingSequenceFormatException("Duplicate class name in the training "
						+ "sequence file.");
			} else if (!classNames.get(i).matches("[a-zA-z].*")) {
				scanner.close();
				throw new TrainingSequenceFormatException("Class names must begin with alphabetic "
						+ "characters");
			}
			uniqueClassNames.add(classNames.get(i));
		}
		
		// If there are no class names provided then the file is incorrect in format
		if (classNames.isEmpty()) {
			scanner.close();
			throw new TrainingSequenceFormatException("No class names were provided in the file.");
		}
		
		// Load in training samples
		List<TrainingSample> trainingSamples = new ArrayList<TrainingSample>();
		
		// Go through each vector and create a scanner for it
		while (scanner.hasNext()) {
			String nextVector = scanner.next(); 
			Scanner vectorScanner = new Scanner(nextVector); 
			vectorScanner.useDelimiter("\\s*,\\s*");
			
			// Get the class number if possible, and check that it's in a valid range
			// N.B. USER SEES numbers 1->M, INTERNALLY USE 0->(M-1)
			int classNumber = 0;
			try {
				classNumber = vectorScanner.nextInt() - 1;	
				if (classNumber < 0 || classNumber >= classNames.size()) {
					scanner.close();
					vectorScanner.close();
					throw new TrainingSequenceFormatException("Class number was either too large to negative");
				}
			} catch (NoSuchElementException e) {
				scanner.close();
				vectorScanner.close();
				throw new TrainingSequenceFormatException(
						"Each vector must start with an integer class number");
			}
			
			// Now iterate through the rest of the numbers in the vector
			List<Double> vector = new ArrayList<Double>();
			while (vectorScanner.hasNextDouble()) {
				vector.add(vectorScanner.nextDouble());
			}
			
			// Create the training sample now we've read the data in, and add it to the list
			trainingSamples.add(new TrainingSample(classNumber, new NDRealVector(vector)));
			vectorScanner.close();
		}
		
		// Remember to close the scanner
		scanner.close();
		
		// Sanity check that we did load in some vectors for the training sequence
		if (trainingSamples.isEmpty()) {
			throw new TrainingSequenceFormatException("No vectors specified in the training file.");
		}
		
		// Final sanity check where we check that all vectors input are of the correct dimension
		// and of positive integer dimension
		int dimension = trainingSamples.get(0).getInstance().getDimension();
		for (TrainingSample sample : trainingSamples) {
			int sampleDimension = sample.getInstance().getDimension();
			if (sampleDimension <= 0) {
				throw new TrainingSequenceFormatException("All vectors need a positive dimension.");
			} else if (sampleDimension != dimension) {
				throw new TrainingSequenceFormatException(
						"All vectors should have the same dimension.");
			}
		}
		
		// Return the training sequence that was just loaded in
		return new TrainingSequence(trainingSamples, classNames);
	}
	
	/***
	 * Compute the empirical probability distribution 
	 * This function replaces the need for dedicated functions for:
	 *   mostFrequentClass
	 * @throws MalformedProbabilityDistributionException 
	 */
	public ProbabilityDistribution empiricalDistribution() throws MalformedProbabilityDistributionException {
		// If there is nothing in the sequence, then we can't generate a probability distribution
		if (this.trainingSequence.size() == 0) {
			throw new MalformedProbabilityDistributionException("Taking the empirical distribution of an "
					+ "empty sequence does not make sense.");
		}
		
		// Create an array of counts, and populate it
		int numberOfClasses = classNames.size();
		int[] counts = new int[classNames.size()];
		for (TrainingSample sample : trainingSequence) {
			counts[sample.classNumber]++;
		}
		
		// Now compute the emperical probabilities in a hashmap
		// Note that totalCount is a double to force the correct value of the emperical probability 
		double totalCount = (double) trainingSequence.size();
		Map<Integer, Double> empericalProbabilities = new HashMap<Integer, Double>(classNames.size());
		for (int i = 0; i < numberOfClasses; i++) {
			empericalProbabilities.put(i, counts[i] / totalCount);
		}
		
		// Now simply return a probability distribution with the given probabilities
		return new ProbabilityDistribution(empericalProbabilities, numberOfClasses);
	}
	
	/***
	 * Use the empirical probability distribution to calculate entropy. However it won't be valid
	 * if we only have an empty sequence. We know that the entropy of the empty sequence is 0.0 
	 * however. 
	 * @return
	 * @throws MalformedProbabilityDistributionException 
	 */
	public double entropy() throws MalformedProbabilityDistributionException {
		if (this.size() == 0) {
			return 0.0;
		}
		return this.empiricalDistribution().entropy();
	}
	
	/***
	 * Join two training sequences into one. 
	 * This function assumes that the classNames for both sequences are identical, and doesn't 
	 * check itself.
	 * @param sequence1 First sequence to be joined
	 * @param sequence2 Second sequence to be joined
	 * @return Returns a training sequence that is the union of the two sequences given.
	 */
	static TrainingSequence join(TrainingSequence sequence1, TrainingSequence sequence2) {
		List<TrainingSample> newTrainingSequence = new ArrayList<TrainingSample>(
				sequence1.trainingSequence.size() + sequence2.trainingSequence.size());
		newTrainingSequence.addAll(sequence1.trainingSequence);
		newTrainingSequence.addAll(sequence2.trainingSequence);
		return new TrainingSequence(newTrainingSequence, sequence1.classNames);
	}
	
	/***
	 * Compute the information gain by splitting a training sequence into two further sequences.
	 * The original sequence should be the union of the two sequences input. The information gain 
	 * is given by:
	 * entropy(OriginalSequence) - s1 * entropy(Sequence1) - s2 entropy(Sequence2)
	 * where
	 * sI = cardinality(SequenceI) / cardinality(OriginalSequence) 
	 * @param sequence1 One of the sequences that we have split into
	 * @param sequence2 The other sequence that we have split into
	 * @throws MalformedProbabilityDistributionException 
	 */
	public static double informationGain(TrainingSequence sequence1, TrainingSequence sequence2) 
			throws MalformedProbabilityDistributionException {
		
		TrainingSequence jointSeq = join(sequence1, sequence2);
		double sequence1Proportion = sequence1.trainingSequence.size() / ((double) jointSeq.trainingSequence.size());
		double sequence2Proportion = sequence2.trainingSequence.size() / ((double) jointSeq.trainingSequence.size());
		return jointSeq.entropy() - sequence1Proportion * sequence1.entropy() - 
				sequence2Proportion * sequence2.entropy();
	}
}
