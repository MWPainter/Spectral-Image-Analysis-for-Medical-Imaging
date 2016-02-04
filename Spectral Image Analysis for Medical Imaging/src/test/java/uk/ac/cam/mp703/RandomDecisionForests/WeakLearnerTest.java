package uk.ac.cam.mp703.RandomDecisionForests;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;

import java.security.InvalidParameterException;
import java.util.Random;

import org.junit.Test;


public class WeakLearnerTest {
	
	/***
	 * Concrete 'implementation' of a weak learner. Blank abstract methods as we only want to use 
	 * it to test the uniformRandomDoubleInRange method.
	 * @author michaelpainter
	 *
	 */
	private static class ConcreteWeakLearner extends WeakLearner {
		@Override
		public WeakLearnerType getWeakLearnerType() {
			return null;
		}

		@Override
		public Direction split(SplitParameters splitParams, Instance instance) {
			return null;
		}

		@Override
		public SplitParameters generateRandomSplitParameters(int dataDimension, Random rand) {
			return null;
		}

		@Override
		public void giveHint(TrainingSequence trainingSequence) {
			
		}
	}
	
	/***
	 * Test for when lowerBound is higher than higherBound
	 */
	@Test(expected=InvalidParameterException.class)
	public void testUniformDoubleInRangeWithInvalidRange() {
		ConcreteWeakLearner wl = new ConcreteWeakLearner();
		wl.uniformRandomDoubleInRange(1.0, -1.0, new Random());
	}
	
	/***
	 * Test the we get the correct exceptions for NaN's being put in each input
	 */
	@Test(expected=InvalidParameterException.class)
	public void testUniformDoubleInRangeWithNaNs() {
		ConcreteWeakLearner wl = new ConcreteWeakLearner();
		try {
			wl.uniformRandomDoubleInRange(Double.NaN, 0.0, new Random());
		} catch (InvalidParameterException e) {
			wl.uniformRandomDoubleInRange(0.0, Double.NaN, new Random());
		}
	}
	
	/***
	 * Test for infinities working (that it doesn't return a NaN or Infinity)
	 */
	@Test
	public void testUniformDoubleInRangeWithInfinities() {
		ConcreteWeakLearner wl = new ConcreteWeakLearner();
		double randomDouble = wl.uniformRandomDoubleInRange(Double.NEGATIVE_INFINITY, 
				Double.POSITIVE_INFINITY, new Random());
		assertTrue("Invalid result from random in range", Double.isFinite(randomDouble));
	}
	
	/***
	 * Normal case, use a seeded random for deterministic result, and check that it produces a 
	 * number in the given range, and that they aren't all the same
	 */
	@Test
	public void testUniformDoubleInRange() {
		// Setup
		ConcreteWeakLearner wl = new ConcreteWeakLearner();
		Random rand = new Random(1);
		
		// Check some different ranges, and one range multiple times
		double min = -1e20;
		double max = 1e20;
		double r1 = wl.uniformRandomDoubleInRange(min, max, rand);
		assertTrue("r1 not in expected range", min < r1);
		assertTrue("r1 not in expected range", r1 < max);
		
		min = -1.3323e20;
		max = -2.3e5;
		double r2 = wl.uniformRandomDoubleInRange(min, max, rand);
		assertTrue("r2 not in expected range", min < r2);
		assertTrue("r2 not in expected range", r2 < max);
		
		min = 1.11;
		max = 1.12;
		double r3 = wl.uniformRandomDoubleInRange(min, max, rand);
		double r4 = wl.uniformRandomDoubleInRange(min, max, rand);
		double r5 = wl.uniformRandomDoubleInRange(min, max, rand);
		assertTrue("r3 not in expected range", min < r3);
		assertTrue("r3 not in expected range", r3 < max);
		assertTrue("r4 not in expected range", min < r4);
		assertTrue("r4 not in expected range", r4 < max);
		assertTrue("r5 not in expected range", min < r5);
		assertTrue("r5 not in expected range", r5 < max);
		
		// something might be a bit wrong if it generate three of the same value also...
		assertFalse("r3 == r4 == r5", r3 == r4 && r4 == r5);
		
		// Check the equal case
		double r6 = wl.uniformRandomDoubleInRange(0.5, 0.5, rand);
		assertThat("r6 should be 0.5", r6, is(0.5));
		
	}
}
