package uk.ac.cam.mp703.RandomDecisionForests;

/***
 * A training sample is simply a pairing between a class and an instance/feature vector 
 * which is known before and to be used in training.
 * @author michaelpainter
 *
 */
public class TrainingSample {
	/***
	 * The class number that this training sample has been assigned by hand.
	 */
	int classNumber;
	
	/***
	 * An instance which has a well known classification.
	 */
	Instance instance;
	

	/**
	 * @return the classNumber
	 */
	public int getClassNumber() {
		return classNumber;
	}


	/**
	 * @param classNumber the classNumber to set
	 */
	public void setClassNumber(int classNumber) {
		this.classNumber = classNumber;
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
	 * @param classNumber
	 * @param ndRealVector
	 */
	public TrainingSample(int classNumber, NDRealVector vector) {
		this.classNumber = classNumber;
		this.instance = vector;
	}
}
