package uk.ac.cam.mp703.NeuralNetworks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.encog.ConsoleStatusReportable;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.neural.networks.BasicNetwork;
import org.encog.util.csv.CSVFormat;
import org.encog.util.simple.EncogUtility;

import uk.ac.cam.mp703.RandomDecisionForests.FileFormatException;

/***
 * Provide static methods to save and load a neural network using encog
 * @author michaelpainter
 *
 */
public class NeuralNetworkLoader {
	
	/***
	 * Only want static helper methods for using Encog here, so make the constructor private
	 */
	private NeuralNetworkLoader() {
	}
	
	/***
	 * Train a nueral network using encog, we need to provide a training sequence using a csv file.
	 * Here we are given n columns of numerical data (given as a parameter), and a class name at 
	 * the end of each sample
	 * 
	 * x_11, ..., x_1n, C_1
	 * ...
	 * x_m1, ..., x_mn, C_m
	 * 
	 * where C_i is a string (quotes not necessary) and x_ij is a decimal value
	 * 
	 * @param csv The csv file for the training data
	 * @param n The number of collumns of numerical data. (So there is n+1 columns including the class name)
	 * @param validationPercentage Percentage of random samples held back to validate the neural network
	 * @param crossValidate The number of crossvalidations used in training
	 * @return The neural network + normalisation helper packaged into one instance
	 */
	public static NeuralNetworkCouple loadAndTrainNeuralNetworkFromCSV(String csv, int n, 
			double validationPercentage, int crossValidate) {
		
		// Load in the data set
		VersatileDataSource source = new CSVDataSource(new File(csv), false, CSVFormat.DECIMAL_POINT);
		VersatileMLDataSet data = new VersatileMLDataSet(source);
		
		// Tell it what's in each column
		for (int i = 0; i < n; i++) {
			data.defineSourceColumn("dim "+i, i, ColumnType.continuous);
		}
		
		// The last column is the class name, keep a ref to this as its the output column
		ColumnDefinition outputColumn = data.defineSourceColumn("class", n, ColumnType.nominal);
		
		// Allow the data to be analysed, i.e. compute mins + maxes etc
		data.analyze();
		
		// Define a model for the data and normalise it
		data.defineSingleOutputOthersInput(outputColumn);
		EncogModel model = new EncogModel(data);
		model.selectMethod(data,  MLMethodFactory.TYPE_FEEDFORWARD);
		model.setReport(new ConsoleStatusReportable());
		data.normalize();
		
		// Train the model (network)
		model.holdBackValidation(validationPercentage, true, (int) System.currentTimeMillis());
		model.selectTrainingType(data);
		BasicNetwork network = (BasicNetwork) model.crossvalidate(crossValidate, true);
		
		// Display the training and validation errors.
		System.out.println("Training error: " + EncogUtility.calculateRegressionError(network, model.getTrainingDataset()));
		System.out.println("Validation error: " + EncogUtility.calculateRegressionError(network, model.getValidationDataset()));
		
		// Display our normalization parameters.
		NormalizationHelper helper = data.getNormHelper();
		System.out.println(helper.toString());
		
		// Display the final model.
		System.out.println("Final model: " + network);
		
		// Return the neural network and normalisation helper pair
		return new NeuralNetworkCouple(network, helper);
	}
	
	
	/***
	 * Load a pre trained neural network from a .nn file
	 * @param filename The name of the file we will load from
	 * @return The NeuralNetworkCouple of a network and a normalisation helper
	 * @throws IOException 
	 * @throws FileFormatException 
	 */
	public NeuralNetworkCouple loadNeuralNetworkFromFile(String filename) throws IOException, FileFormatException {
		try {
			// Get an input stream from the filename 
			ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename + ".nn"));
			
			// Read in the network
			NeuralNetworkCouple couple = (NeuralNetworkCouple) in.readObject();
			
			// Close the input stream
			in.close();
			
			// Return the neural network
			return couple;
			
		} catch (ClassNotFoundException e) {
			// If we have an incorrect case, then the file doesn't contain a network => wrong file
			throw new FileFormatException(e.getMessage());
		}
	}

	/*** 
	 * Save a neural network to a .nn file
	 * @param couple The BasicNetwork and NormalizationHleper instances put into a NeuralNetworkCouple instance 
	 * @param filename The name of the file we are saving to 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public void saveNeuralNetworkToFile(NeuralNetworkCouple couple, String filename) throws FileNotFoundException, IOException {
		// Create an file to output to, write this forest object to file and then close the stream
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename + ".nn"));
		out.writeObject(couple);
		out.close();
	}
	
}
