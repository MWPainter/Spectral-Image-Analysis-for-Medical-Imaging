package uk.ac.cam.mp703.RandomDecisionForests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import java.util.NoSuchElementException;
import java.util.Random;
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
	 * The list of classes is specified in the class colour mapping.
	 * The forests must keep a list of classes, which is part of training.
	 */
	List<ClassLabel> classes;

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
	 * @return the classes
	 */
	public List<ClassLabel> getClasses() {
		return classes;
	}

	/**
	 * @param classes the classes to set
	 */
	public void setClasses(List<ClassLabel> classes) {
		this.classes = classes;
	}

	/***
	 * @return The number of classes in this sequence
	 */
	public int getNoClasses() {
		return this.classes.size();
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
	 * @param classes The array of classes used in the training sequence
	 */
	public TrainingSequence(List<TrainingSample> trainingSequence, List<ClassLabel> classes) {
		this.trainingSequence = trainingSequence;
		this.classes = classes;
	}

	/***
	 * Load in a training sequence for Nd real vector instances from a text file. Note that loading 
	 * in training sequences needs to be specific to the type of instance. 
	 * The format of the text file should be:
	 * ------------------------------
	 * | c_1,x_11,x_12,...,x_1n;	|
	 * | c_2,x_21,x_22,...,x_2n;	|
	 * | c_3,x_31,x_32,...,x_3n;	|
	 * | c_4,x_41,x_42,...,x_4n;	|
	 * | c_5,x_51,x_52,...,x_5n;	|
	 * | ...						|
	 * | c_k,x_k1,x_k2,...,x_kn;	|
	 * ------------------------------
	 * Where m is the number of classes, c's are classes (names, as specified by a class map file), 
	 * x's are feature vectors, each x_ij is a real number, n is the dimension of data and k is 
	 * the number of samples in the sequence
	 * 
	 * N.B. new lines are ignored, only the commas and semi-colons are used for reading. All class 
	 * names must begin with an alphabetic character, but they may use any valid unicode characters 
	 * subsequently.
	 * 
	 * @param trainingSequenceFilename The text file containing the training sequence 
	 * @param classMapFilename The text file containing the mapping from classnames to colours
	 * @throws TrainingSequenceFormatException
	 * @throws FileNotFoundException
	 * @throws FileFormatException 
	 */
	public static TrainingSequence newNDRealVectorTrainingSequence(String trainingSequenceFilename,
			String classMapFilename) 
			throws TrainingSequenceFormatException, FileNotFoundException, FileFormatException{
		
		// Firstly get the list of classes
		List<ClassLabel> classes = ClassLabel.loadClassList(classMapFilename);
		
		// We will use names to reference the classes, so compute the name to class map
		Map<String, ClassLabel> nameToClassMap = ClassLabel.computeNameToClassMap(classes);
		
		// Open the file using a Scanner, and use ";" to initially separate out the data
		// N.B. We use the \s at the beginning and end to ignore unnecessary whitespace.
		Scanner scanner = new Scanner(new File(trainingSequenceFilename));
		scanner.useDelimiter("\\s*;\\s*");
		
		// Load in training samples
		List<TrainingSample> trainingSamples = new ArrayList<TrainingSample>();
		
		// Go through each vector and create a scanner for it
		while (scanner.hasNext()) {
			String nextVector = scanner.next(); 
			Scanner vectorScanner = new Scanner(nextVector); 
			vectorScanner.useDelimiter("\\s*,\\s*");
			
			// Get the class name, and lookup the class label for it
			String className;
			try {
				className = vectorScanner.next();
				if (!nameToClassMap.containsKey(className)) {
					scanner.close();
					vectorScanner.close();
					throw new TrainingSequenceFormatException("Class name in training sequence was "
							+ "not found in the colour class map");
				}
			} catch (NoSuchElementException e) {
				scanner.close();
				vectorScanner.close();
				throw new TrainingSequenceFormatException(
						"Each vector must start with an integer class number");
			}
			
			// Get the class label from the name
			ClassLabel classLabel = nameToClassMap.get(className);
			
			// Now iterate through the rest of the numbers in the vector
			List<Double> vector = new ArrayList<Double>();
			while (vectorScanner.hasNextDouble()) {
				vector.add(vectorScanner.nextDouble());
			}
			
			// Create the training sample now we've read the data in, and add it to the list
			trainingSamples.add(new TrainingSample(classLabel, new NDRealVector(vector)));
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
		return new TrainingSequence(trainingSamples, classes);
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
		int numberOfClasses = classes.size();
		int[] counts = new int[classes.size()];
		for (TrainingSample sample : trainingSequence) {
			counts[sample.classLabel.getClassId()]++;
		}
		
		// Now compute the emperical probabilities in a hashmap
		// Note that totalCount is a double to force the correct value of the emperical probability 
		double totalCount = (double) trainingSequence.size();
		Map<ClassLabel, Double> empericalProbabilities = new HashMap<ClassLabel, Double>(classes.size());
		for (ClassLabel clazz : classes) {
			empericalProbabilities.put(clazz, counts[clazz.getClassId()] / totalCount);
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
	public static TrainingSequence join(TrainingSequence sequence1, TrainingSequence sequence2) {
		List<TrainingSample> newTrainingSequence = new ArrayList<TrainingSample>(
				sequence1.trainingSequence.size() + sequence2.trainingSequence.size());
		newTrainingSequence.addAll(sequence1.trainingSequence);
		newTrainingSequence.addAll(sequence2.trainingSequence);
		return new TrainingSequence(newTrainingSequence, sequence1.classes);
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
	
	/***
	 * Normalisation of a training sequence. We need to do two things here.
	 * We need to normalise each instance with respect to 'normalisationValues'
	 * We also need to compute and return the average normalisation value to be used when labeling
	 * 
	 * N.B. This function has side effects, where the training sequence instances are normalised 
	 * themselves.
	 * @return normalisationValue The value to be used as a reference when classifying
	 */
	public double normalise() {
		// Iterate through all instances computing an average power
		// Compute a running average for numerical stability
		double averagePower = 0.0;
		int i = 0;
		for (TrainingSample sample : trainingSequence) {
			averagePower += (i == 0) ? sample.instance.getNormalisationReference() :
									(sample.instance.getNormalisationReference() - averagePower) / averagePower; 
			i++;
		}
		
		// Now use the average power to normalise each instance
		for (TrainingSample sample : trainingSequence) {
			sample.instance.normalise(averagePower);
		}
		
		// Return the average "power" as the reference value to be used in later classifications
		// now that the training sequence has been normalised to this "power" 
		return averagePower;
	}
	
	/***
	 * Save the training sequence as a text file specified
	 * @throws IOException 
	 */
	public void saveToTextFile(String filename) throws IOException {
		// Make sure the filename has a .txt at the end
		if (!filename.endsWith(".txt")) {
			filename += ".txt";
		}
		
		// Open the file to write to
		File file = new File(filename);
		BufferedWriter writer = new BufferedWriter(new FileWriter(file));
		
		// Now we need to iterate through all of the instances printing out their values and classes
		for (TrainingSample sample : trainingSequence) {
			// Write the class number
			// Reminder: the user sees 1, ..., N, internally we use 0, ..., N-1
			writer.write((sample.classLabel.getName()) + ", ");
			
			// Print out the instance
			writer.write(sample.instance.toString() + ";\n");
		}
		
		// We are done writing all of the information, so close the writer
		writer.close();
	}
	
	/***
	 * Bagging if we don't care about sizes, but want an equal number of each class
	 * @return The bagged training sequence
	 */
	public TrainingSequence bag() {
		return bag(0, Integer.MAX_VALUE);
	}
	
	/***
	 * Return a bagged training sequence, with an equal number of each class
	 * @param minimumSize The minimum number of instances of some class
	 * @param maximumSize The maximum number of instances of some class
	 * @return
	 */
	public TrainingSequence bag(int minimumSize, int maximumSize) {
		// First sort into a number of sets
		Map<ClassLabel, Set<TrainingSample>> classSets = new HashMap<>(classes.size());
		for (ClassLabel clazz : classes) {
			classSets.put(clazz, new HashSet<TrainingSample>());
		}
		
		// Iterate through all of the training samples putting them in the buckets
		for (TrainingSample sample : trainingSequence) {
			classSets.get(sample.classLabel).add(sample);
		}
		
		// Check that all of the sizes are at least big enough for the minimum size
		int smallestSetSize = 0;
		for (Entry<ClassLabel, Set<TrainingSample>> entry : classSets.entrySet()) {
			int size = entry.getValue().size();
			if (size < minimumSize) {
				throw new TrainingSequenceException("Not enough samples for one the classes in the "
						+ "training sequence to perform bagging with a minimum value of " + minimumSize);
			} else if (size < smallestSetSize) {
				smallestSetSize = size;
			}
		}
		
		// Now construct a new training sequence by randomly picking as much as we can from each set
		List<TrainingSample> newTrainingSequence = new ArrayList<>();
		for (ClassLabel clazz : classes) {
			for (int i=0; i < smallestSetSize; i++) {
				newTrainingSequence.add(randomObjectFromSetWithRemoval(classSets.get(clazz)));
			}
		}
		
		// Return the new training sequence
		return new TrainingSequence(newTrainingSequence, classes);
	}
	
	/***
	 * Helper to get a random item from a set, and remove it from the set
	 * @param set The set we want to pick a value from
	 * @param rand An instance of Random to use to randomly pick samples
	 */
	private static <T> T randomObjectFromSetWithRemoval(Set<T> set) {
		Random rand = ThreadLocalRandom.current();
		int size = set.size();
		int r = rand.nextInt(size);
		int i = 0;
		for(T obj : set) {
		    if (i == r) {
		    	//set.remove(obj);
		        return obj;
		    }
		    i++;
		}
		throw new TrainingSequenceException("Something went wrong picking a random element from a set");
	}
}
