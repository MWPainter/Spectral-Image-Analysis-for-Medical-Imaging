package uk.ac.cam.mp703.RandomDecisionForests;

import java.io.Serializable;

/***
 * A training sample is simply a pairing between a class and an instance/feature vector 
 * which is known before and to be used in training.
 * @author michaelpainter
 *
 */
public class TrainingSample implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/***
	 * The class label that this training sample has been assigned by hand.
	 */
	ClassLabel classLabel;
	
	/***
	 * An instance which has a well known classification.
	 */
	Instance instance;
	

	/**
	 * @return the classLabel
	 */
	public ClassLabel getClassNumber() {
		return classLabel;
	}


	/**
	 * @param classLabel the classLabel to set
	 */
	public void setClassLabel(ClassLabel classLabel) {
		this.classLabel = classLabel;
	}


	/**
	 * @return the instance
	 */
	public Instance getInstance() {
		return instance;
	}


	/**
	 * @param instance the instance to set
	 */
	public void setInstance(Instance instance) {
		this.instance = instance;
	}


	/***
	 * Constructor using a NDRealVector instance.
	 * @param classLabel
	 * @param ndRealVector
	 */
	public TrainingSample(ClassLabel classLabel, NDRealVector vector) {
		this.classLabel = classLabel;
		this.instance = vector;
	}
}
