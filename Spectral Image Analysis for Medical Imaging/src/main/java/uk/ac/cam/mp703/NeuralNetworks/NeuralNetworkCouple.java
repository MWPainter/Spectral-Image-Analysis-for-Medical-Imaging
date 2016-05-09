package uk.ac.cam.mp703.NeuralNetworks;

import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;

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

	/***
	 * Construct with the network and helper given
	 */
	public NeuralNetworkCouple(BasicNetwork network, NormalizationHelper helper) {
		this.network = network;
		this.helper = helper;
	}
}
