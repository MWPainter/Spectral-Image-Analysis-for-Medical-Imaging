package uk.ac.cam.mp703.RandomDecisionForests;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.mp703.RandomDecisionForests.OneDimensionalLinearWeakLearner.OneDimensionalLinearSplitParameters;

public class OneDimensionalLinearWeakLearnerTest {

	/***
	 * Training sequences used by the weak learners
	 */
	TrainingSequence ts1, ts2;
	
	/***
	 * Weak learners that we will use
	 */
	OneDimensionalLinearWeakLearner wl1, wl2;
	
	/***
	 * Before tests we want to have two weak learners, each having been given a hint about two 
	 * seperate training sequences
	 */
	@Before
	public void generateTrainingSequences() {
		// Some data points
		ArrayList<Double> a1List = new ArrayList<Double>();
		a1List.add(-2.0);
		TrainingSample a1 = new TrainingSample(0, new NDRealVector(a1List));
		
		ArrayList<Double> a2List = new ArrayList<Double>();
		a2List.add(-1.9);
		TrainingSample a2 = new TrainingSample(0, new NDRealVector(a2List));
		
		ArrayList<Double> a3List = new ArrayList<Double>();
		a3List.add(-1.1);
		TrainingSample a3 = new TrainingSample(0, new NDRealVector(a3List));

		ArrayList<Double> b1List = new ArrayList<Double>();
		b1List.add(-0.1);
		TrainingSample b1 = new TrainingSample(1, new NDRealVector(b1List));

		ArrayList<Double> b2List = new ArrayList<Double>();
		b2List.add(1.0);
		TrainingSample b2 = new TrainingSample(1, new NDRealVector(b2List));

		ArrayList<Double> b3List = new ArrayList<Double>();
		b3List.add(0.9);
		TrainingSample b3 = new TrainingSample(1, new NDRealVector(b3List));

		ArrayList<Double> c1List = new ArrayList<Double>();
		c1List.add(2.0);
		TrainingSample c1 = new TrainingSample(2, new NDRealVector(c1List));

		ArrayList<Double> c2List = new ArrayList<Double>();
		c2List.add(1.1);
		TrainingSample c2 = new TrainingSample(2, new NDRealVector(c2List));

		ArrayList<Double> c3List = new ArrayList<Double>();
		c3List.add(1.5);
		TrainingSample c3 = new TrainingSample(3, new NDRealVector(c3List));
		
		// Some more data points
		ArrayList<Double> d1List = new ArrayList<Double>();
		d1List.add(-2.0);
		d1List.add(-1.0);
		TrainingSample d1 = new TrainingSample(0, new NDRealVector(d1List));
		
		ArrayList<Double> d2List = new ArrayList<Double>();
		d2List.add(-1.9);
		d2List.add(-2.9);
		TrainingSample d2 = new TrainingSample(0, new NDRealVector(d2List));
		
		ArrayList<Double> d3List = new ArrayList<Double>();
		d3List.add(-1.1);
		d3List.add(1.1);
		TrainingSample d3 = new TrainingSample(0, new NDRealVector(d3List));

		ArrayList<Double> e1List = new ArrayList<Double>();
		e1List.add(-0.1);
		e1List.add(-0.1);
		TrainingSample e1 = new TrainingSample(1, new NDRealVector(e1List));

		ArrayList<Double> e2List = new ArrayList<Double>();
		e2List.add(1.0);
		e2List.add(2.0);
		TrainingSample e2 = new TrainingSample(1, new NDRealVector(e2List));

		ArrayList<Double> e3List = new ArrayList<Double>();
		e3List.add(0.9);
		e3List.add(2.9);
		TrainingSample e3 = new TrainingSample(1, new NDRealVector(e3List));

		ArrayList<Double> f1List = new ArrayList<Double>();
		f1List.add(2.0);
		f1List.add(-1.6);
		TrainingSample f1 = new TrainingSample(2, new NDRealVector(f1List));

		ArrayList<Double> f2List = new ArrayList<Double>();
		f2List.add(1.1);
		f2List.add(-0.1);
		TrainingSample f2 = new TrainingSample(2, new NDRealVector(f2List));

		ArrayList<Double> f3List = new ArrayList<Double>();
		f3List.add(1.5);
		f3List.add(-1.5);
		TrainingSample f3 = new TrainingSample(3, new NDRealVector(f3List));
		
		// Class names for sequence 1
		ArrayList<String> classList1 = new ArrayList<String>();
		classList1.add("a");
		classList1.add("b");
		classList1.add("c");
		
		// Class names for sequence 2
		ArrayList<String> classList2 = new ArrayList<String>();
		classList2.add("d");
		classList2.add("e");
		classList2.add("f");
		
		// Sequences
		ArrayList<TrainingSample> ts1Seq = new ArrayList<TrainingSample>();
		ts1Seq.add(a1);
		ts1Seq.add(a2);
		ts1Seq.add(a3);
		ts1Seq.add(b1);
		ts1Seq.add(b2);
		ts1Seq.add(b3);
		ts1Seq.add(c1);
		ts1Seq.add(c2);
		ts1Seq.add(c3);

		ArrayList<TrainingSample> ts2Seq = new ArrayList<TrainingSample>();
		ts2Seq.add(d1);
		ts2Seq.add(d2);
		ts2Seq.add(d3);
		ts2Seq.add(e1);
		ts2Seq.add(e2);
		ts2Seq.add(e3);
		ts2Seq.add(f1);
		ts2Seq.add(f2);
		ts2Seq.add(f3); 
		
		// Create the concrete training sequences
		ts1 = new TrainingSequence(ts1Seq, classList1);
		ts2 = new TrainingSequence(ts2Seq, classList2);
		
		// Create the weak learners, and give them hints from the two sequences
		wl1 = new OneDimensionalLinearWeakLearner();
		wl2 = new OneDimensionalLinearWeakLearner();
		wl1.giveHint(ts1);
		wl2.giveHint(ts2);
	}
	
	
	/***
	 * Test generation of split parameters in the correct range
	 * 
	 * N.B. dimension 1 (min, max) = (-2, 2)
	 * 		dimension 2 (min, max) = (-2.9, 2.9)
	 */
	@Test
	public void checkWeakLearnersSplitParameterGeneration() {
		Random rand = new Random(100);
		
		// Test 100 random split parameters from wl1
		for (int i = 0; i < 100; i++) {
			OneDimensionalLinearSplitParameters splitParams = 
					(OneDimensionalLinearSplitParameters) wl1.generateRandomSplitParameters(1, rand);
			assertThat(splitParams.dimension, equalTo(0));
			assertTrue(-2.0 <= splitParams.threshold && splitParams.threshold <= 2.0);
		}
		
		// Test 100 random split parameters from wl2
		// Count the number from each dimension, and check that a fair number of both dimension are
		// chosen
		int[] counts = new int[2];
		for (int i = 0; i < 100; i++) {
			OneDimensionalLinearSplitParameters splitParams = 
					(OneDimensionalLinearSplitParameters) wl2.generateRandomSplitParameters(2, rand);
			assertTrue(splitParams.dimension == 0 || splitParams.dimension == 1);
			counts[splitParams.dimension]++;
			assertTrue(-2.0 <= splitParams.threshold && splitParams.threshold <= 2.0 && splitParams.dimension == 0 ||
					-2.9 <= splitParams.threshold && splitParams.threshold <= 2.9 && splitParams.dimension == 1);
		}

		assertTrue(counts[0] > 30);
		assertTrue(counts[1] > 30);
	}
	
	/***
	 * Check that the split function works correctly
	 */
	@Test
	public void testSplitFunction() {
		// 4 instances
		List<Double> vlist = new ArrayList<Double>();
		vlist.add(0.0);
		vlist.add(0.0);
		NDRealVector v1 = new NDRealVector(vlist);
		
		vlist = new ArrayList<Double>();
		vlist.add(1.0);
		vlist.add(0.3);
		NDRealVector v2 = new NDRealVector(vlist);
		
		vlist = new ArrayList<Double>();
		vlist.add(0.5);
		vlist.add(1.1);
		NDRealVector v3 = new NDRealVector(vlist);
		
		vlist = new ArrayList<Double>();
		vlist.add(0.3);
		vlist.add(0.8);
		NDRealVector v4 = new NDRealVector(vlist);
		
		// 2 split parameters
		OneDimensionalLinearSplitParameters sp1 = new OneDimensionalLinearSplitParameters(0, 0.5);
		OneDimensionalLinearSplitParameters sp2 = new OneDimensionalLinearSplitParameters(1, 0.7);
		
		// Check that each of the split parameters divide the vectors correctly
		// N.B. We use wl1, but just because we need an instance to access the method
		assertThat(wl1.split(sp1, v1), equalTo(Direction.LEFT));
		assertThat(wl1.split(sp1, v2), equalTo(Direction.RIGHT));
		assertThat(wl1.split(sp1, v3), equalTo(Direction.RIGHT));
		assertThat(wl1.split(sp1, v4), equalTo(Direction.LEFT));
		assertThat(wl1.split(sp2, v1), equalTo(Direction.LEFT));
		assertThat(wl1.split(sp2, v2), equalTo(Direction.LEFT));
		assertThat(wl1.split(sp2, v3), equalTo(Direction.RIGHT));
		assertThat(wl1.split(sp2, v4), equalTo(Direction.RIGHT));
	}
}
