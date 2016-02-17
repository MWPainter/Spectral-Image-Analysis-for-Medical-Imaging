//package uk.ac.cam.mp703.RandomDecisionForests;
//
//import static org.hamcrest.core.IsEqual.equalTo;
//import static org.junit.Assert.assertThat;
//import static org.junit.Assert.assertTrue;
//
//import java.security.InvalidParameterException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import uk.ac.cam.mp703.RandomDecisionForests.DecisionForest.TreeNode;
//
//public class LearnerTest {
//	
//	/*** 
//	 * Training sequence we will frequently want to use. 
//	 */
//	TrainingSequence ts;
//	
//	/***
//	 * A weak learner instance we will frequntly want to use.
//	 */
//	WeakLearner wl;
//	
//	/***
//	 * Generation of the 'frequently used variables'
//	 */
//	@Before
//	public void generateVariables() {
//		// Some data points
//		ArrayList<Double> a1List = new ArrayList<Double>();
//		a1List.add(-2.0);
//		TrainingSample a1 = new TrainingSample(0, new NDRealVector(a1List));
//		
//		ArrayList<Double> a2List = new ArrayList<Double>();
//		a2List.add(-1.9);
//		TrainingSample a2 = new TrainingSample(0, new NDRealVector(a2List));
//		
//		ArrayList<Double> a3List = new ArrayList<Double>();
//		a3List.add(-1.1);
//		TrainingSample a3 = new TrainingSample(0, new NDRealVector(a3List));
//
//		ArrayList<Double> b1List = new ArrayList<Double>();
//		b1List.add(-0.1);
//		TrainingSample b1 = new TrainingSample(1, new NDRealVector(b1List));
//
//		ArrayList<Double> b2List = new ArrayList<Double>();
//		b2List.add(1.0);
//		TrainingSample b2 = new TrainingSample(1, new NDRealVector(b2List));
//
//		ArrayList<Double> b3List = new ArrayList<Double>();
//		b3List.add(0.9);
//		TrainingSample b3 = new TrainingSample(1, new NDRealVector(b3List));
//
//		ArrayList<Double> c1List = new ArrayList<Double>();
//		c1List.add(2.0);
//		TrainingSample c1 = new TrainingSample(2, new NDRealVector(c1List));
//
//		ArrayList<Double> c2List = new ArrayList<Double>();
//		c2List.add(1.1);
//		TrainingSample c2 = new TrainingSample(2, new NDRealVector(c2List));
//
//		ArrayList<Double> c3List = new ArrayList<Double>();
//		c3List.add(1.5);
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
//		ts1Seq.add(a2);
//		ts1Seq.add(a3);
//		ts1Seq.add(b1);
//		ts1Seq.add(b2);
//		ts1Seq.add(b3);
//		ts1Seq.add(c1);
//		ts1Seq.add(c2);
//		ts1Seq.add(c3);
//		
//		// Finally create the actual sequence instance
//		ts = new TrainingSequence(ts1Seq, classList);
//		
//		// Also create an instance of a weak learner
//		wl = new OneDimensionalLinearWeakLearner();
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testNullSequenceGeneratesException() {
//		Learner.trainDecisionForest(null, wl, 1, 1, 1);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testEmptySequenceGeneratesException() {
//		TrainingSequence ts = new TrainingSequence(new ArrayList<TrainingSample>(), new ArrayList<String>());
//		Learner.trainDecisionForest(ts, wl, 1, 1, 1);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testEmptyClassesSequenceGeneratesException() {
//		TrainingSequence ts = new TrainingSequence(new ArrayList<TrainingSample>(), new ArrayList<String>());
//		ts.trainingSequence.add(new TrainingSample(0, new NDRealVector(new ArrayList<Double>())));
//		Learner.trainDecisionForest(ts, wl, 1, 1, 1);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testNullWeakLearnerGeneratesException() {
//		Learner.trainDecisionForest(ts, null, 1, 1, 1);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testNegativeTreesGeneratesException() {
//		Learner.trainDecisionForest(ts, wl, -2, 1, 1);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testZeroTreesGeneratesException() {
//		Learner.trainDecisionForest(ts, wl, 0, 1, 1);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testNegativeDepthGeneratesException() {
//		Learner.trainDecisionForest(ts, wl, 1, -2, 0);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testZeroDepthGeneratesException() {
//		Learner.trainDecisionForest(ts, wl, 1, 0, 0);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testNegativeRandomnessParamterGeneratesException() {
//		Learner.trainDecisionForest(ts, wl, 1, 1, -2);
//	}
//	
//	@Test(expected=InvalidParameterException.class)
//	public void testZeroRandomnessParamterGeneratesException() {
//		Learner.trainDecisionForest(ts, wl, 1, 1, 0);
//	}
//			
//	/***
//	 * A normal execution of the learning function. We check the depth 
//	 * @throws MalformedForestException 
//	 */ 
//	@Test
//	public void testNormalLearningExecution() throws MalformedForestException {
//		// Extend the training sequence for this test
//		// Class g is the interval (2.0, infty)
//		ArrayList<Double> g1List = new ArrayList<Double>();
//		g1List.add(2.2);
//		TrainingSample g1 = new TrainingSample(3, new NDRealVector(g1List));
//
//		ArrayList<Double> g2List = new ArrayList<Double>();
//		g2List.add(2.4);
//		TrainingSample g2 = new TrainingSample(3, new NDRealVector(g2List));
//
//		ArrayList<Double> g3List = new ArrayList<Double>();
//		g3List.add(2.6);
//		TrainingSample g3 = new TrainingSample(3, new NDRealVector(g3List));
//
//		ts.trainingSequence.add(g1);
//		ts.trainingSequence.add(g2);
//		ts.trainingSequence.add(g3);
//		
//		ts.classNames.add("g");
//		
//		// Train the forest, randomness should be high enough that we always get a depth of 2
//		// I.e. there is little randomness as we search the parameter space thoroughly
//		DecisionForest frst = Learner.trainDecisionForest(ts, wl, 50, 2, 100);
//		
//		// Check that the properties of the decision tree hold, i.e. the depth is 2, with 5 forests
//		assertThat(frst.rootNodes.size(), equalTo(50));
//		
//		// Check that most of the trees have a depth of 2 down the left hand side
//		int count = 0;
//		for (TreeNode tn : frst.rootNodes) {
//			if (tn.leftChild != null && tn.leftChild.leftChild != null && 
//					tn.leftChild.leftChild.classNumber != -1) {
//				count++;
//			}
//		}
//		assertTrue(count > 30);
//		
//		
//		// Just 'soft test' that it approximately sort out the inputs into the correct intervals:
//		// [-2,-1], (-1,1), [-1,2], (2,infty)
//		// N.B. lowest value in (-1,1) in the test sequence is -0.1, so the (-1,0) values are mixed
//		List<Double> vlist = new ArrayList<Double>();
//		vlist.add(-1.5);
//		System.out.println("-1.5 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(-1.3);
//		System.out.println("-1.3 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(-1.1);
//		System.out.println("-1.1 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(-0.9);
//		System.out.println("-0.9 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(-0.7);
//		System.out.println("-0.7 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(-0.5);
//		System.out.println("-0.5 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(-1.5);
//		System.out.println("-0.3 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(-0.3);
//		System.out.println("-0.1 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(-0.1);
//		System.out.println("0.1 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(0.1);
//		System.out.println("0.3 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(0.3);
//		System.out.println("0.5 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(0.5);
//		System.out.println("0.7 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(0.7);
//		System.out.println("0.9 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(0.9);
//		System.out.println("1.1 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(1.1);
//		System.out.println("1.3 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(1.3);
//		System.out.println("1.5 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(1.5);
//		System.out.println("1.7 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(1.7);
//		System.out.println("1.9 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(1.9);
//		System.out.println("2.1 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(2.1);
//		System.out.println("2.3 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(2.3);
//		System.out.println("2.5 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(2.5);
//		System.out.println("2.7 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(2.7);
//		System.out.println("2.9 output: " + frst.classify(new NDRealVector(vlist)));
//		vlist = new ArrayList<Double>();
//		vlist.add(2.9);
//	}
//}
