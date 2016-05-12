package uk.ac.cam.mp703.NeuralNetworks;

import java.util.List;

import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;

import uk.ac.cam.mp703.RandomDecisionForests.ClassLabel;

/***
 * Encog has a neural network class and a data normaliser class.
 * We need both to be able to correctly classify images
 * 
 * @author michaelpainter
 *
 */
public class NeuralNetworkCouple {
	
	/***
	 * The network
	 */
	BasicNetwork network;
	
	/***
	 * Normalisation helper
	 */
	NormalizationHelper helper;
	
	/***
	 * Class list
	 */
	List<ClassLabel> classes;
	
	/**
	 * @return the network
	 */
	public BasicNetwork getNetwork() {
		return network;
	}

	/**
	 * @param network the network to set
	 */
	public void setNetwork(BasicNetwork network) {
		this.network = network;
	}

	/**
	 * @return the helper
	 */
	public NormalizationHelper getHelper() {
		return helper;
	}

	/**
	 * @param helper the helper to set
	 */
	public void setHelper(NormalizationHelper helper) {
		this.helper = helper;
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
	 * Construct with the network and helper given
	 */
	public NeuralNetworkCouple(BasicNetwork network, NormalizationHelper helper) {
		this.network = network;
		this.helper = helper;
	}
}
