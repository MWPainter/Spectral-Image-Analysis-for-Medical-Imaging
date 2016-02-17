package uk.ac.cam.mp703.RandomDecisionForests;

import java.io.Serializable;

public abstract class SplitParameters implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * Split parameters are specific to each type of weak learner, and we wish for parameters to be 
	 * able to identify which they are intended for.
	 * @return The type of weak learner these split parameters are for
	 */
	public abstract  WeakLearnerType getWeakLearnerType();
}
