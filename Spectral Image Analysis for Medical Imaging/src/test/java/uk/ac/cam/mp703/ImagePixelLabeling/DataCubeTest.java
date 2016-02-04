package uk.ac.cam.mp703.ImagePixelLabeling;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;
import org.junit.Test;

import uk.ac.cam.mp703.ImagePixelLabelling.DataCube;
import uk.ac.cam.mp703.NeuralNetworks.NeuralNetworkCouple;
import uk.ac.cam.mp703.NeuralNetworks.NeuralNetworkLoader;
import uk.ac.cam.mp703.RandomDecisionForests.DecisionForest;
import uk.ac.cam.mp703.RandomDecisionForests.Learner;
import uk.ac.cam.mp703.RandomDecisionForests.MalformedForestException;
import uk.ac.cam.mp703.RandomDecisionForests.MalformedProbabilityDistributionException;
import uk.ac.cam.mp703.RandomDecisionForests.NDRealVector;
import uk.ac.cam.mp703.RandomDecisionForests.OneDimensionalLinearWeakLearner;
import uk.ac.cam.mp703.RandomDecisionForests.TrainingSample;
import uk.ac.cam.mp703.RandomDecisionForests.TrainingSequence;

public class DataCubeTest {

	/***
	 * Profiles:
	 * 
	 * C1 = 200, 179, 32 
	 * C2 = 41, 198, 37 
	 * C3 = 199, 40, 205 
	 * C4 = 5, 10, 220
	 * 
	 * Generate an image with linear interpolations between C1, C2, C3, C4
	 * 
	 * C1 ------ C2 
	 * |          | 
	 * |          | 
	 * |          | 
	 * C3 ------ C4
	 * 
	 * and a second image with
	 * 
	 * --- WHITE ---
	 * |           | 
	 * |           | 
	 * |           | 
	 * C1 ------- C2
	 * |           | 
	 * |           | 
	 * |           | 
	 * --- BLACK ---  
	 * 
	 * @throws IOException
	 */
	@Test
	public void generate3DimImages() throws IOException {
		// Create an image
		BufferedImage img = new BufferedImage(256, 256,
				BufferedImage.TYPE_3BYTE_BGR);

		// Create three image profiles
		short[] c1 = { 200, 100, 0 };
		short[] c2 = { 200, 0, 100 };
		short[] c3 = { 100, 200, 0 };
		short[] c4 = { 0, 100, 200 };

		// Loop through the pixels of the image and interpolate between them
		// appropriately
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				double xRatio = i / (double) 256;
				double yRatio = j / (double) 256;
				int r = (short) ((1.0 - yRatio) * ((1.0 - xRatio) * c1[0] + xRatio * c2[0]) + 
						yRatio * ((1.0 - xRatio) * c3[0] + xRatio * c4[0]));
				int g = (short) ((1.0 - yRatio) * ((1.0 - xRatio) * c1[1] + xRatio * c2[1]) + 
						yRatio * ((1.0 - xRatio) * c3[1] + xRatio * c4[1]));
				int b = (short) ((1.0 - yRatio) * ((1.0 - xRatio) * c1[2] + xRatio * c2[2]) + 
						yRatio * ((1.0 - xRatio) * c3[2] + xRatio * c4[2]));
				img.setRGB(i, j, new Color(r, g, b).getRGB());
			}
		}

		// Output to the filename given
		File file = new File("src/test/java/InterpolatedRGBImage.png");
		ImageIO.write(img, "png", file);
		
		
		// A second image to test with
		BufferedImage img2 = new BufferedImage(256, 512, BufferedImage.TYPE_3BYTE_BGR);
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				double xRatio = i / (double) 256;
				double yRatio = j / (double) 256;
				int r = (short) (yRatio * ((1.0 - xRatio) * c1[0] + xRatio * c2[0]) + 
						(1.0 - yRatio) *  255.0);
				int g = (short) (yRatio * ((1.0 - xRatio) * c1[1] + xRatio * c2[1]) + 
						(1.0 - yRatio) * 255.0);
				int b = (short) (yRatio * ((1.0 - xRatio) * c1[2] + xRatio * c2[2]) + 
						(1.0 - yRatio) * 255.0);
				img2.setRGB(i, j, new Color(r, g, b).getRGB());
			}
		}
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				double xRatio = i / (double) 256;
				double yRatio = j / (double) 256;
				int r = (short) ((1.0 - yRatio) * ((1.0 - xRatio) * c1[0] + xRatio * c2[0]));
				int g = (short) ((1.0 - yRatio) * ((1.0 - xRatio) * c1[1] + xRatio * c2[1]));
				int b = (short) ((1.0 - yRatio) * ((1.0 - xRatio) * c1[2] + xRatio * c2[2]));
				img2.setRGB(i, j + 256, new Color(r, g, b).getRGB());
			}
		}
		

		// Output to the filename given
		File file2 = new File("src/test/java/InterpolatedRGBImage2.png");
		ImageIO.write(img2, "png", file2);
	}
	
	/***
	 * Train a tree using the profiles
	 * 
	 * C1 = 200, 179, 32 
	 * C2 = 41, 198, 37 
	 * C3 = 199, 40, 205 
	 * C4 = 5, 10, 220
	 * 
	 * 
	 * Then use it to classify the images generated in "generate3DimImages"
	 * @throws IOException 
	 * @throws MalformedForestException 
	 * @throws MalformedProbabilityDistributionException 
	 */
	@Test
	public void classifyRGBImageRF() throws MalformedForestException, IOException, MalformedProbabilityDistributionException {
		// Hard code data points used for training
		ArrayList<Double> aList = new ArrayList<Double>();
		aList.add(200.0);
		aList.add(100.0);
		aList.add(0.0);
		TrainingSample a = new TrainingSample(0, new NDRealVector(aList));

		ArrayList<Double> bList = new ArrayList<Double>();
		bList.add(200.0);
		bList.add(0.0);
		bList.add(100.0);
		TrainingSample b = new TrainingSample(1, new NDRealVector(bList));

		ArrayList<Double> cList = new ArrayList<Double>();
		cList.add(100.0);
		cList.add(200.0);
		cList.add(0.0);
		TrainingSample c = new TrainingSample(2, new NDRealVector(cList));

		ArrayList<Double> dList = new ArrayList<Double>();
		dList.add(0.0);
		dList.add(100.0);
		dList.add(200.0);
		TrainingSample d = new TrainingSample(3, new NDRealVector(dList));
				
		// Class names
		ArrayList<String> classList = new ArrayList<String>();
		classList.add("C1");
		classList.add("C2");
		classList.add("C3");
		classList.add("C4");
				
		// Sequence
		ArrayList<TrainingSample> seq = new ArrayList<TrainingSample>();
		seq.add(a);
		seq.add(b);
		seq.add(c);
		seq.add(d);
		
		TrainingSequence ts = new TrainingSequence(seq, classList);
		
		// Train a decision forest using the above
		DecisionForest frst = Learner.trainDecisionForest(ts, new OneDimensionalLinearWeakLearner(), 1000, 2, 5000, 0.0);
		
		// Now use the forest to classify an image
		DataCube dc = DataCube.generateDataCubeFromColourImage("src/test/java/InterpolatedRGBImage.png");
		Map<Integer, Color> colourMap = new HashMap<>();
		colourMap.put(0, Color.RED);
		colourMap.put(1, Color.GREEN);
		colourMap.put(2, Color.BLUE);
		colourMap.put(3, Color.BLACK);
		dc.generatePixelLabeledImage(frst, colourMap, "src/test/java/InterpolatedRGBImageLabeled.png");
		
		// Now use the forest to classify a 2nd image
		DataCube dc2 = DataCube.generateDataCubeFromColourImage("src/test/java/InterpolatedRGBImage2.png");
		dc2.generatePixelLabeledImage(frst, colourMap, "src/test/java/InterpolatedRGBImageLabeled2.png");
	}

	/***
	 * Train a tree using the profiles
	 * 
	 * C1 = 200, 179, 32 
	 * C2 = 41, 198, 37 
	 * C3 = 199, 40, 205 
	 * C4 = 5, 10, 220
	 * 
	 * BUT here we train using different power intensities of the spectra.
	 * 
	 * Then use it to classify the images generated in "generate3DimImages"
	 * @throws IOException 
	 * @throws MalformedForestException 
	 * @throws MalformedProbabilityDistributionException 
	 */
	@Test
	public void classifyRGBImageRF2() throws MalformedForestException, IOException, MalformedProbabilityDistributionException {
		// Create a training sequence
		ArrayList<TrainingSample> seq = new ArrayList<TrainingSample>();
		
		for (int i = 15; i <= 37; i++) {
			double p = i / 30.0;

			ArrayList<Double> aList = new ArrayList<Double>();
			aList.add(200.0*p);
			aList.add(100.0*p);
			aList.add(0.0*p);
			TrainingSample a = new TrainingSample(0, new NDRealVector(aList));

			ArrayList<Double> bList = new ArrayList<Double>();
			bList.add(200.0*p);
			bList.add(0.0*p);
			bList.add(100.0*p);
			TrainingSample b = new TrainingSample(1, new NDRealVector(bList));

			ArrayList<Double> cList = new ArrayList<Double>();
			cList.add(100.0*p);
			cList.add(200.0*p);
			cList.add(0.0*p);
			TrainingSample c = new TrainingSample(2, new NDRealVector(cList));

			ArrayList<Double> dList = new ArrayList<Double>();
			dList.add(0.0*p);
			dList.add(100.0*p);
			dList.add(200.0*p);
			TrainingSample d = new TrainingSample(3, new NDRealVector(dList));

			// Add to sequence
			seq.add(a);
			seq.add(b);
			seq.add(c);
			seq.add(d);
		}
		
		// Class names
		ArrayList<String> classList = new ArrayList<String>();
		classList.add("C1");
		classList.add("C2");
		classList.add("C3");
		classList.add("C4");
		
		TrainingSequence ts = new TrainingSequence(seq, classList);
		
		// Train a decision forest using the above
		DecisionForest frst = Learner.trainDecisionForest(ts, new OneDimensionalLinearWeakLearner(), 1000, 2, 1000, 0.0);
		
		// Now use the forest to classify an image
		DataCube dc = DataCube.generateDataCubeFromColourImage("src/test/java/InterpolatedRGBImage.png");
		Map<Integer, Color> colourMap = new HashMap<>();
		colourMap.put(0, Color.RED);
		colourMap.put(1, Color.GREEN);
		colourMap.put(2, Color.BLUE);
		colourMap.put(3, Color.BLACK);
		dc.generatePixelLabeledImage(frst, colourMap, "src/test/java/InterpolatedRGBImageLabeledLargeTrainer.png");
		
		// Now use the forest to classify a 2nd image
		DataCube dc2 = DataCube.generateDataCubeFromColourImage("src/test/java/InterpolatedRGBImage2.png");
		dc2.generatePixelLabeledImage(frst, colourMap, "src/test/java/InterpolatedRGBImageLabeled2LargeTrainer.png");
	}
	
	/***
	 * Train a network using the profiles
	 * 
	 * C1 = 200, 179, 32 
	 * C2 = 41, 198, 37 
	 * C3 = 199, 40, 205 
	 * C4 = 5, 10, 220
	 * 
	 * given in NN3DTrainingSet.txt. The values are repeated as encog splits the training sequence 
	 * into multiple training sets
	 * 
	 * Then use it to classify the image generated in "generate3DimImage"
	 * @throws IOException 
	 * @throws MalformedForestException 
	 */
	@Test
	public void classifyRGBImageNN() throws MalformedForestException, IOException {
		// Train the neural network using the training set provided
		String filename = "src/test/java/NN3DTrainingSet.txt";
		NeuralNetworkCouple couple = NeuralNetworkLoader.loadAndTrainNeuralNetworkFromCSV(filename, 3, 0.0, 2);
		BasicNetwork network = couple.getNetwork();
		NormalizationHelper helper = couple.getHelper();
		
		// Make a map from class names to colours		
		Map<String, Color> colourMap = new HashMap<>();
		colourMap.put("C1", Color.RED);
		colourMap.put("C2", Color.GREEN);
		colourMap.put("C3", Color.BLUE);
		colourMap.put("C4", Color.BLACK);
		
		// Try generating an image from the datacube
		DataCube dc = DataCube.generateDataCubeFromColourImage("src/test/java/InterpolatedRGBImage.png");
		String outputFilename = "src/test/java/InterpolatedRGBImageLabeledNN.png";
		dc.generatePixelLabeledImageNeuralNetwork(network, helper, colourMap, outputFilename);
	}
		
	
	
	/***
	 * Profiles:
	 * 
	 * C1 = 210, 100, 100, 30, 30
	 * C2 = 30, 100, 210, 100, 30
	 * C3 = 210, 100, 30, 100, 210
	 * C4 = 20, 30, 100, 170, 210
	 * 
	 * Generate an image with linear interpolations between C1, C2, C3, C4
	 * 
	 * C1 ------ C2 
	 * |          | 
	 * |          | 
	 * |          | 
	 * C3 ------ C4
	 * 
	 * @throws IOException
	 */
	@Test
	public void generate5DimImage() throws IOException {
		// Create 5 images
		List<BufferedImage> imgs = new ArrayList<>(5);
		for (int i = 0; i < 5; i++) {
			imgs.add(i, new BufferedImage(256, 256, BufferedImage.TYPE_3BYTE_BGR));
		}		
		
		// Create three image profiles
		short[] c1 = {210, 100, 100, 30, 30};
		short[] c2 = {30, 100, 210, 100, 30};
		short[] c3 = {210, 100, 30, 100, 210};
		short[] c4 = {20, 30, 100, 170, 210};

		// Loop through the pixels of the image and interpolate between them
		// appropriately
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < 256; j++) {
				double xRatio = i / (double) 256;
				double yRatio = j / (double) 256;
				for (int k = 0; k < 5; k++) {
					int grey = (short) ((1.0 - yRatio) * ((1.0 - xRatio) * c1[k] + xRatio * c2[k]) + 
							yRatio * ((1.0 - xRatio) * c3[k] + xRatio * c4[k]));
					imgs.get(k).setRGB(i, j, new Color(grey, grey, grey).getRGB());
				}
			}
		}

		// Output to the filename given
		for (int i = 1; i <= 5; i++) {
			File file = new File("src/test/java/Interpolated5DimImage" + i + ".png");
			ImageIO.write(imgs.get(i-1), "png", file);
		}
	}
	
	/***
	 * Train a tree using the profiles
	 * 
	 * C1 = 210, 100, 100, 30, 30
	 * C2 = 30, 100, 210, 100, 30
	 * C3 = 210, 100, 30, 100, 210
	 * C4 = 20, 30, 100, 170, 210
	 * 
	 * Then use it to classify the image generated in "generate3DimImage"
	 * @throws IOException 
	 * @throws MalformedForestException 
	 * @throws MalformedProbabilityDistributionException 
	 */
	@Test
	public void classiousfy5DimImageRF() throws MalformedForestException, IOException, MalformedProbabilityDistributionException {
		// Hard code data points used for training
		ArrayList<Double> aList = new ArrayList<Double>();
		aList.add(210.0);
		aList.add(100.0);
		aList.add(100.0);
		aList.add(30.0);
		aList.add(30.0);
		TrainingSample a = new TrainingSample(0, new NDRealVector(aList));

		ArrayList<Double> bList = new ArrayList<Double>();
		bList.add(30.0);
		bList.add(100.0);
		bList.add(210.0);
		bList.add(100.0);
		bList.add(30.0);
		TrainingSample b = new TrainingSample(1, new NDRealVector(bList));

		ArrayList<Double> cList = new ArrayList<Double>();
		cList.add(210.0);
		cList.add(100.0);
		cList.add(30.0);
		cList.add(100.0);
		cList.add(210.0);
		TrainingSample c = new TrainingSample(2, new NDRealVector(cList));

		ArrayList<Double> dList = new ArrayList<Double>();
		dList.add(30.0);
		dList.add(30.0);
		dList.add(100.0);
		dList.add(100.0);
		dList.add(210.0);
		TrainingSample d = new TrainingSample(3, new NDRealVector(dList));
				
		// Class names
		ArrayList<String> classList = new ArrayList<String>();
		classList.add("C1");
		classList.add("C2");
		classList.add("C3");
		classList.add("C4");
				
		// Sequence
		ArrayList<TrainingSample> seq = new ArrayList<TrainingSample>();
		seq.add(a);
		seq.add(b);
		seq.add(c);
		seq.add(d);
		
		TrainingSequence ts = new TrainingSequence(seq, classList);
		
		// Train a decision forest using the above
		DecisionForest frst = Learner.trainDecisionForest(ts, new OneDimensionalLinearWeakLearner(), 2000, 2, 5000, 0.0);
		
		System.out.println(System.currentTimeMillis());
		
		// Now use the forest to classify an image
		DataCube dc = DataCube.generateDataCubeFromMonochromeImages("src/test/java/Interpolated5DimImage%.png");
		Map<Integer, Color> colourMap = new HashMap<>();
		colourMap.put(0, Color.RED);
		colourMap.put(1, Color.GREEN);
		colourMap.put(2, Color.BLUE);
		colourMap.put(3, Color.YELLOW);
		dc.generatePixelLabeledImage(frst, colourMap, "src/test/java/Interpolated5DimImageLabeled.png");
		
		System.out.println(System.currentTimeMillis());
	}
	
	/***
	 * Train a network using the profiles
	 * 
	 * C1 = 210, 100, 100, 30, 30
	 * C2 = 30, 100, 210, 100, 30
	 * C3 = 210, 100, 30, 100, 210
	 * C4 = 20, 30, 100, 170, 210
	 * 
	 * given in NN3DTrainingSet.txt. The values are repeated as encog splits the training sequence 
	 * into multiple training sets
	 * 
	 * Then use it to classify the image generated in "generate3DimImage"
	 * @throws IOException 
	 * @throws MalformedForestException 
	 */
	@Test
	public void classify5DimImageNN() throws MalformedForestException, IOException {
		// Train the neural network using the training set provided
		String filename = "src/test/java/NN5DTrainingSet.txt";
		NeuralNetworkCouple couple = NeuralNetworkLoader.loadAndTrainNeuralNetworkFromCSV(filename, 5, 0.0, 2);
		BasicNetwork network = couple.getNetwork();
		NormalizationHelper helper = couple.getHelper();
		
		// Make a map from class names to colours		
		Map<String, Color> colourMap = new HashMap<>();
		colourMap.put("C1", Color.RED);
		colourMap.put("C2", Color.GREEN);
		colourMap.put("C3", Color.BLUE);
		colourMap.put("C4", Color.BLACK);
		
		// Try generating an image from the datacube
		DataCube dc = DataCube.generateDataCubeFromMonochromeImages("src/test/java/Interpolated5DimImage%.png");
		String outputFilename = "src/test/java/Interpolated5DimImageLabeledNN.png";
		dc.generatePixelLabeledImageNeuralNetwork(network, helper, colourMap, outputFilename);
	}
}
