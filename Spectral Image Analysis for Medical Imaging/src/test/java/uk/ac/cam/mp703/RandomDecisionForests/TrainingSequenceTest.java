//package uk.ac.cam.mp703.RandomDecisionForests;
//
//import static org.hamcrest.core.IsEqual.equalTo;
//import static org.junit.Assert.assertThat;
//
//import java.io.FileNotFoundException;
//import java.util.ArrayList;
//
//import org.junit.Before;
//import org.junit.Test;
//
//public class TrainingSequenceTest {
//	/***
//	 * Some training sequences we will use to test the entropy functions
//	 */
//	TrainingSequence ts1;
//	TrainingSequence ts1Left;
//	TrainingSequence ts1Right;
//	TrainingSequence ts2;
//	TrainingSequence ts2Left;
//	TrainingSequence ts2Right;
//	TrainingSequence ts3;
//	
//	/***
//	 * Generate simple training sequence used for testing the entropy functions
//	 * a = [-2,-1)
//	 * b = [-1, 1]
//	 * c = (1,  2] 
//	 */
//	@Before
//	public void generateTestTrainingSequences() {
//		// Some data points
//		ArrayList<Double> a1List = new ArrayList<Double>();
//		a1List.add(-2.0);
//		TrainingSample a1 = new TrainingSample(0, new NDRealVector(a1List));
//		
//		ArrayList<Double> a2List = new ArrayList<Double>();
//		a1List.add(-1.9);
//		TrainingSample a2 = new TrainingSample(0, new NDRealVector(a2List));
//		
//		ArrayList<Double> a3List = new ArrayList<Double>();
//		a1List.add(-1.1);
//		TrainingSample a3 = new TrainingSample(0, new NDRealVector(a3List));
//
//		ArrayList<Double> b1List = new ArrayList<Double>();
//		a1List.add(-0.1);
//		TrainingSample b1 = new TrainingSample(1, new NDRealVector(b1List));
//
//		ArrayList<Double> b2List = new ArrayList<Double>();
//		a1List.add(1.0);
//		TrainingSample b2 = new TrainingSample(1, new NDRealVector(b2List));
//
//		ArrayList<Double> b3List = new ArrayList<Double>();
//		a1List.add(0.9);
//		TrainingSample b3 = new TrainingSample(1, new NDRealVector(b3List));
//
//		ArrayList<Double> c1List = new ArrayList<Double>();
//		a1List.add(2.0);
//		TrainingSample c1 = new TrainingSample(2, new NDRealVector(c1List));
//
//		ArrayList<Double> c2List = new ArrayList<Double>();
//		a1List.add(1.1);
//		TrainingSample c2 = new TrainingSample(2, new NDRealVector(c2List));
//
//		ArrayList<Double> c3List = new ArrayList<Double>();
//		a1List.add(1.5);
//		TrainingSample c3 = new TrainingSample(3, new NDRealVector(c3List));
//		
//		// Class names
//		ArrayList<String> classList = new ArrayList<String>();
//		classList.add("a");
//		classList.add("b");
//		classList.add("c");
//		
//		// Sequences
//		ArrayList<TrainingSample> ts1Seq = new ArrayList<TrainingSample>();
//		ts1Seq.add(a1);
//		ts1Seq.add(b1);
//		ts1Seq.add(b2);
//		ts1Seq.add(b3);
//		ts1Seq.add(c1);
//		
//		ArrayList<TrainingSample> ts1LeftSeq = new ArrayList<TrainingSample>();
//		ts1LeftSeq.add(a1);
//		ts1LeftSeq.add(b1);
//		
//		ArrayList<TrainingSample> ts1RightSeq = new ArrayList<TrainingSample>();
//		ts1RightSeq.add(b2);
//		ts1RightSeq.add(b3);
//		ts1RightSeq.add(c1);
//		
//		ArrayList<TrainingSample> ts2Seq = new ArrayList<TrainingSample>();
//		ts2Seq.add(a1);
//		ts2Seq.add(b1);
//		ts2Seq.add(c1);
//		
//		ArrayList<TrainingSample> ts2LeftSeq = new ArrayList<TrainingSample>();
//		ts2LeftSeq.add(a1);
//		ts2LeftSeq.add(b1);
//		
//		ArrayList<TrainingSample> ts2RightSeq = new ArrayList<TrainingSample>();
//		ts2RightSeq.add(c1);
//		
//		ArrayList<TrainingSample> ts3Seq = new ArrayList<TrainingSample>();
//		ts3Seq.add(a1);
//		
//		// Finally create the actual sequence instances
//		ts1 = new TrainingSequence(ts1Seq, classList);
//		ts2 = new TrainingSequence(ts2Seq, classList);
//		ts3 = new TrainingSequence(ts3Seq, classList);
//		ts1Left = new TrainingSequence(ts1LeftSeq, classList);
//		ts1Right = new TrainingSequence(ts1RightSeq, classList);
//		ts2Left = new TrainingSequence(ts2LeftSeq, classList);
//		ts2Right = new TrainingSequence(ts2RightSeq, classList);
//	}
//	
//
//	/***
//	 * Load in a normal sequence
//	 */
//	@Test
//	public void testLoadingTrainingSequenceNormal() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceNormalSpacing";
//		TrainingSequence ts = TrainingSequence.newNDRealVectorTrainingSequence(filename);
//		
//		assertThat("Classes incorrectly loaded", ts.classNames.get(0), equalTo("Class1Name"));
//		assertThat("Classes incorrectly loaded", ts.classNames.get(3), equalTo("WhyIsThisClassNameHere?"));
//		
//		assertThat("Number of classes incorrect", ts.classNames.size(), equalTo(4));
//
//		NDRealVector instance = (NDRealVector) ts.trainingSequence.get(0).instance;
//		assertThat("Vector value incorrect", instance.vector.get(0), equalTo(1.1));
//		instance = (NDRealVector) ts.trainingSequence.get(1).instance;
//		assertThat("Vector value incorrect", instance.vector.get(3), equalTo(1234567.1234567));
//		instance = (NDRealVector) ts.trainingSequence.get(3).instance;
//		assertThat("Vector value incorrect", instance.vector.get(1), equalTo(1.4));
//		instance = (NDRealVector) ts.trainingSequence.get(5).instance;
//		assertThat("Vector value incorrect", instance.vector.get(0), equalTo(1.4));
//		
//		assertThat("Instance has incorrect dimesnion", instance.getDimension(), equalTo(4));
//	}
//	
//	/***
//	 * A sequence with funky spacing should still work, we want the users to be able to format these 
//	 * in a readable way
//	 */
//	@Test
//	public void testLoadingTrainingSequenceWeird() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceWeirdSpacing";
//		TrainingSequence ts = TrainingSequence.newNDRealVectorTrainingSequence(filename);
//		
//		// Check some of the values are correct
//		assertThat("Classes incorrectly loaded", ts.classNames.get(0), equalTo("Class1Name"));
//		assertThat("Classes incorrectly loaded", ts.classNames.get(3), equalTo("WhyIsThisClassNameHere?"));
//		
//		assertThat("Number of classes incorrect", ts.classNames.size(), equalTo(4));
//
//		NDRealVector instance = (NDRealVector) ts.trainingSequence.get(0).instance;
//		assertThat("Vector value incorrect", instance.vector.get(0), equalTo(1.1));
//		instance = (NDRealVector) ts.trainingSequence.get(5).instance;
//		assertThat("Vector value incorrect", instance.vector.get(3), equalTo(1234567.1234566));
//		instance = (NDRealVector) ts.trainingSequence.get(2).instance;
//		assertThat("Vector value incorrect", instance.vector.get(1), equalTo(1.4));
//		instance = (NDRealVector) ts.trainingSequence.get(4).instance;
//		assertThat("Vector value incorrect", instance.vector.get(0), equalTo(1.4));
//		
//		assertThat("Instance has incorrect dimesnion", instance.getDimension(), equalTo(4));
//	}
//	
//	/***
//	 * Test when there is a duplicate class name in the training sequence .txt file
//	 */
//	@Test(expected=TrainingSequenceFormatException.class)
//	public void testLoadingTrainingSequenceDuplicateClassName() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceDuplicateClassName";
//		TrainingSequence.newNDRealVectorTrainingSequence(filename);
//	}
//	
//	/***
//	 * Test when there is inconsistent vector dimensions in the training sequence .txt file
//	 */
//	@Test(expected=TrainingSequenceFormatException.class)
//	public void testLoadingTrainingSequenceInconsistentFeatureVectorDimensions() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceInconsistentFeatureVectorDimensions";
//		TrainingSequence.newNDRealVectorTrainingSequence(filename);
//	}
//	
//	/***
//	 * Test when there is incorrect class numbers used in the training sequence .txt file
//	 */
//	@Test(expected=TrainingSequenceFormatException.class)
//	public void testLoadingTrainingSequenceIncorrectClassNumbers() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceIncorrectClassNumbers";
//		TrainingSequence.newNDRealVectorTrainingSequence(filename);
//	}
//	
//	/***
//	 * Test when there is missing semi colons used in the training sequence .txt file
//	 */
//	@Test(expected=TrainingSequenceFormatException.class)
//	public void testLoadingTrainingSequenceMissingSemiColons() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceMissingSemiColons";
//		TrainingSequence.newNDRealVectorTrainingSequence(filename);
//	}
//	
//	/***
//	 * Test when there is no feature vectors in the training sequence .txt file
//	 */
//	@Test(expected=TrainingSequenceFormatException.class)
//	public void testLoadingTrainingSequenceNoFeatureVectors() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceNoFeatureVectors";
//		TrainingSequence.newNDRealVectorTrainingSequence(filename);
//	}
//	
//	/***
//	 * Test when there is a vector with 0 dimensions in the training sequence .txt file
//	 */
//	@Test(expected=TrainingSequenceFormatException.class)
//	public void testLoadingTrainingSequenceNullFeatureVector() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceNullFeatureVector";
//		TrainingSequence.newNDRealVectorTrainingSequence(filename);
//	}
//	
//	/***
//	 * Test when there is no class names specified
//	 * This fails because of the restriction that we have with class names having to begin with an 
//	 * alphabetic character, although there is no problem with using numbers as class name, it 
//	 * prevents an easy mistake that may be confusing if we let it slip
//	 * If they really wanted numeric class names they can use a, b, c, ... or c1, c2, c3, ... as an 
//	 * alternative.
//	 */
//	@Test(expected=TrainingSequenceFormatException.class)
//	public void testLoadingTrainingSequenceMissingClassNames() throws TrainingSequenceFormatException, FileNotFoundException {
//		String filename = "src/test/java/TestTrainingSequenceMissingClassNames";
//		TrainingSequence.newNDRealVectorTrainingSequence(filename);
//	}
//	
//	/***
//	 * Test for when the file cannot be found
//	 */
//	@Test(expected=FileNotFoundException.class)
//	public void testLoadingTrainingSequenceIncorrectFileName() throws TrainingSequenceFormatException, FileNotFoundException {
//		TrainingSequence.newNDRealVectorTrainingSequence("1");
//	}
//	
//	/***
//	 * Test the entropy functions on the test sequences generated
//	 */
//	@Test
//	public void testEntropyFunctions() {
//		// ts1 probability distribution = {1/5, 3/5, 1/5}
//		// ts1 size = 5
//		double ts1ExpectedEntropy = 1.0/5.0 * Math.log(5.0) + 3.0/5.0 * Math.log(5.0/3.0) + 
//				1.0/5.0 * Math.log(5.0); 
//		assertThat("Entropy computed incorrectly.", ts1.entropy(), equalTo(ts1ExpectedEntropy));
//		
//		// ts2 probability distribution = {1/3, 1/3, 1/3}
//		// ts2 size = 3
//		double ts2ExpectedEntropy = 1.0/3.0 * Math.log(3.0) + 1.0/3.0 * Math.log(3.0) + 
//				1.0/3.0 * Math.log(3.0); 
//		assertThat("Entropy computed incorrectly.", ts2.entropy(), equalTo(ts2ExpectedEntropy));
//		
//		// ts3 probability distribution = {1, 0, 0}
//		// ts3 size = 1
//		// expected entropy = 0
//		assertThat("Entropy computed incorrectly.", ts3.entropy(), equalTo(0.0));
//		
//		// entropy for an empty sequence should be zero
//		TrainingSequence tsEmpty = new TrainingSequence(new ArrayList<TrainingSample>(), 
//				new ArrayList<String>());
//		assertThat("Entropy of an empty sequence should be 0", tsEmpty.entropy(), equalTo(0.0));
//
//		// ts1Left size = 2 
//		// ts1Right size = 3
//		// ts1 size = 5
//		double expectedInformationGain1 = ts1.entropy() - 2.0/5.0 * ts1Left.entropy() - 
//				3.0/5.0 * ts1Right.entropy();
//		assertThat("Information gain computed incorrectly.", 
//				TrainingSequence.informationGain(ts1Left, ts1Right), equalTo(expectedInformationGain1));
//
//		// ts2Left size = 2 
//		// ts2Right size = 1
//		// ts2 size = 3
//		double expectedInformationGain2 = ts2.entropy() - 2.0/3.0 * ts2Left.entropy() - 
//				1.0/3.0 * ts2Right.entropy();
//		assertThat("Information gain computed incorrectly.", 
//				TrainingSequence.informationGain(ts2Left, ts2Right), equalTo(expectedInformationGain2));
//	}
//	
//	/***
//	 * Test the "mostFrequenctClass" function. 
//	 *
//	 * N.B. Current behavior if two classes are equally well represented is to pick the first one
//	 * in the class list. Tests currently test for that behavior, but should change the behaviour 
//	 * if it leads to a significant bias. 
//	 */
//	@Test
//	public void testMostFrequentClass() {		
//		// ts1 probability distribution = {1/5, 3/5, 1/5}
//		// most frequent class is b, with class number one
//		int classNumber = ts1.mostFrequentClass();
//		assertThat("Incorrect most frequent class identified", classNumber, equalTo(1));
//		assertThat("Incorrect most frequent class identified", ts1.classNames.get(classNumber), equalTo("b"));
//		
//		// ts2 has distribution = {1/3, 1/3, 1/3}
//		// all are the most frequent class, as 0 < 1 < 2, it should return 0
//		classNumber = ts2.mostFrequentClass();
//		assertThat("Incorrect most frequent class identified", classNumber, equalTo(0));
//		assertThat("Incorrect most frequent class identified", ts2.classNames.get(classNumber), equalTo("a"));
//	}
//}
