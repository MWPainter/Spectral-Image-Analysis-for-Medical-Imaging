package uk.ac.cam.mp703.EntryPoint;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import uk.ac.cam.mp703.ImagePixelLabelling.DataCube;
import uk.ac.cam.mp703.ImagePixelLabelling.LabelledImageType;
import uk.ac.cam.mp703.Noise.ArtificialNoise;
import uk.ac.cam.mp703.RandomDecisionForests.ClassLabel;
import uk.ac.cam.mp703.RandomDecisionForests.DecisionForest;
import uk.ac.cam.mp703.RandomDecisionForests.FileFormatException;
import uk.ac.cam.mp703.RandomDecisionForests.Learner;
import uk.ac.cam.mp703.RandomDecisionForests.MalformedForestException;
import uk.ac.cam.mp703.RandomDecisionForests.MalformedProbabilityDistributionException;
import uk.ac.cam.mp703.RandomDecisionForests.OneDimensionalLinearWeakLearner;
import uk.ac.cam.mp703.RandomDecisionForests.TrainingSequence;
import uk.ac.cam.mp703.RandomDecisionForests.TrainingSequenceFormatException;
import uk.ac.cam.mp703.RandomDecisionForests.WeakLearner;
import uk.ac.cam.mp703.TrainingTools.TrainingSequenceGeneration;

public class EntryPoint {
	
	/***
	 * An array of task strings to use
	 */
	static final String[] tasks = {
		"gen3D",
		"gen5D",
		"trainRF",
		"trainNN",
		"runRF",
		"runNN",
		"applyPoissonNoise",
		"applyGaussianNoise",
		"trainingTool"
	};

	/***
	 * Main entry point to using the project
	 * @param args Array of arguments passed into the program
	 * @throws FileFormatException 
	 * @throws IOException 
	 * @throws InterruptedException 
	 * @throws TrainingSequenceFormatException 
	 */
	public static void main(String[] args) throws IOException, FileFormatException, TrainingSequenceFormatException, InterruptedException  {
		
		// Check the first argument is something to decide the usage
		if (args.length == 0) {
			System.out.println("No arguments passed in. \n\n");
			printUsage(System.out);
			return;
		}
		
		// Now for each possible task, load in the data and check that its ok
		if (args[0].equals(tasks[0])) {
			// GENERATE 3 DIM IMAGE
			try {
				mainGenerate3DimImage(args[1]);
				
			} catch (NullPointerException e) {
				System.err.println("Error reading in arguments to task " + tasks[0]);
				printUsage(System.err);
				
			} catch (IOException e) {
				System.err.println("There was an IO error: " + e.getMessage());
			}
			
		} else if (args[0].equals(tasks[1])) {
			// GENERATE 5 DIM IMAGE
			try {
				mainGenerate5DimImage(args[1]);
				
			} catch (NullPointerException e) {
				System.err.println("Error reading in arguments to task " + tasks[1]);
				printUsage(System.err);
				
			} catch (IOException e) {
				System.err.println("There was an IO error: " + e.getMessage());
			}
			
		} else if (args[0].equals(tasks[2])) {
			// TRAIN RANDOM FOREST
			try {
				String trainingFilename = args[1];
				String classColourMapFilename = args[2];
				String outputFilename = args[3];
				int maxTrees = Integer.parseInt(args[4]);
				int maxDepth = Integer.parseInt(args[5]);
				int randomnessParameter = Integer.parseInt(args[6]);
				double informationGainCutoff = Double.parseDouble(args[7]);
				boolean normaliseSpectra = Boolean.parseBoolean(args[8]);
				boolean bagging = Boolean.parseBoolean(args[9]);
				mainLearning(trainingFilename, classColourMapFilename, outputFilename, maxTrees,
						maxDepth, randomnessParameter, informationGainCutoff, normaliseSpectra,
						bagging);
				
			} catch (NullPointerException e) {
				System.err.println("Error reading in arguments to task " + tasks[2]);
				printUsage(System.err);
				
			} catch (IOException e) {
				System.err.println("There was an IO error: " + e.getMessage());
				e.printStackTrace();
				
			} catch (MalformedForestException | MalformedProbabilityDistributionException e) {
				System.err.println("There was an error in the RF library: " + e.getMessage());
			}
			
		} else if (args[0].equals(tasks[3])) {
			// TRAIN NEURAL NETWORK
			System.out.println("TRAINING NEURAL NETS NOT ADDED TO INTERFACE YET.");
			
		} else if (args[0].equals(tasks[4])) {
			// RUN RANDOM FOREST CLASSIFICATION
			try {
				if (args.length > 5) {
					mainRFRun(args[1], args[2], args[3], args[4], args[5]);
				} else {
					mainRFRun(args[1], args[2], args[3], args[4], null);
				}
				
			} catch (NullPointerException e) {
				System.err.println("Error reading in arguments to task " + tasks[0]);
				printUsage(System.err);
				
			} catch (IOException e) {
				System.err.println("There was an IO error: " + e.getMessage());
				
			} catch (MalformedForestException | MalformedProbabilityDistributionException e) {
				System.err.println("There was an error in the RF library: " + e.getMessage());
				
			} catch (FileFormatException e) {
				System.err.println("You need to input a valid .frst file");
			} 
			
			
		} else if (args[0].equals(tasks[5])) {
			// RUN NEURAL NETWORK
			System.out.println("PIXEL LABELLING USING NEURAL NETS NOT ADDED TO INTERFACE YET.");
			
		
		} else if (args[0].equals(tasks[6])) {
			// APPLY A POISSON NOISE
			try {
				mainPNRun(args[1], args[2]);
				
			} catch (NullPointerException e) {
				System.err.println("Error reading in arguments to task " + tasks[0]);
				printUsage(System.err);
				
			} catch (IOException e) {
				System.err.println("There was an IO error: " + e.getMessage());
				
			} catch (FileFormatException e) {
				System.err.println("You need to include a '%' in your input image specifier if and "
						+ "only if the output image specifier contains a '%'. Also check each only "
						+ "contains a single '%'.");
			}
			
			
		} else if (args[0].equals(tasks[7])) {
			// APPLY A GAUSSIAN NOISE
			try {
				String inputImage = args[1];
				double power = Double.parseDouble(args[2]);
				String outputImage = args[3];
				mainGNRun(inputImage, power, outputImage);
				
			} catch (NullPointerException e) {
				System.err.println("Error reading in arguments to task " + tasks[0]);
				printUsage(System.err);
				
			} catch (IOException e) {
				System.err.println("There was an IO error: " + e.getMessage());
				
			} catch (FileFormatException e) {
				System.err.println("You need to include a '%' in your input image specifier if and "
						+ "only if the output image specifier contains a '%'. Also check each only "
						+ "contains a single '%'.");
			}
			
			
		} else if (args[0].equals(tasks[8])) {
			// GENERATE A TRAINING SEQUENCE FROM AN EXAMPLE LABELLING
			mainTrainingTools(args[1], args[2], args[3], args[4]);
			
			
		} else {
			System.out.println("First argument isn't a valid task. \n\n");
			printUsage(System.out);
			return;
		}
	}
	
	/***
	 * Generate an example 3 dimensional image and training sequence
	 * @param dir The directory to put the image + training sequence in
	 * @throws IOException
	 */
	private static void mainGenerate3DimImage(String dir) throws IOException {
		generate3DimImage(dir);
		generate3DimTrainingSequence(dir);
	}
	
	/***
	 * Image portion of mainGenerate3DimImage
	 * @param dir Local directory to put the image into 
	 * @throws IOException
	 */
	private static void generate3DimImage(String dir) throws IOException {
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
		File file = new File(dir + (dir.endsWith("/") ? "" : "/") +"InterpolatedRGBImage.png");
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
	 * Training sequence portion of mainGenerate3DimImage
	 * @param dir Local directory to put the image into 
	 * @throws IOException
	 */
	private static void generate3DimTrainingSequence(String dir) throws IOException {
		//throw new RuntimeException("Not yet implemented.");
	}
	
	/***
	 * Generate an example 5 dimensional image and training sequence
	 * @param dir The directory to put the image + training sequence in
	 * @throws IOException
	 */
	private static void mainGenerate5DimImage(String dir) throws IOException {
		generate5DimImage(dir);
		generate5DimTrainingSequence(dir);
	}
	
	/***
	 * Image portion of mainGenerate3DimImage
	 * @param dir Local directory to put the image into 
	 * @throws IOException
	 */
	private static void generate5DimImage(String dir) throws IOException {
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
			File file = new File(dir + (dir.endsWith("/") ? "" : "/") + "Interpolated5DimImage" + i + ".png");
			ImageIO.write(imgs.get(i-1), "png", file);
		}
	}
	
	/***
	 * Training sequence portion of mainGenerate5DimImage
	 * @param dir Local directory to put the image into 
	 * @throws IOException
	 */
	private static void generate5DimTrainingSequence(String dir) throws IOException {
		//throw new RuntimeException("Not yet implemented.");
	}
	
	/***
	 * Produce a tree from a training file.
	 * @param trainingFilename Filename of the training sequence
	 * @param classColourMapFilename Filename of the map from classes to colours
	 * @param outputFilename Filename of the .frst file to output
	 * @param maxTrees Number of trees in the forest
	 * @param maxDepth Maximum depth of any tree
	 * @param randomnessParameter How many parameters to try at each split node
	 * @param informationGainCutoff Minimal information gain for each split node we allow
	 * @param normaliseSpectra Do we want spectra to be normalised (with reference to power) during 
	 * 			training and classification?
	 * @param bagging Should we perform bagging during training?
	 * @throws IOException
	 * @throws MalformedForestException
	 * @throws MalformedProbabilityDistributionException
	 * @throws FileFormatException 
	 * @throws TrainingSequenceFormatException 
	 * @throws InterruptedException 
	 */
	private static void mainLearning(
			String trainingFilename, 
			String classColourMapFilename, 
			String outputFilename, 
			int maxTrees, 
			int maxDepth, 
			int randomnessParameter, 
			double informationGainCutoff, 
			boolean normaliseSpectra, 
			boolean bagging) 
					throws IOException, 
					MalformedForestException, 
					MalformedProbabilityDistributionException, 
					TrainingSequenceFormatException, 
					FileFormatException, 
					InterruptedException {
		
		// Get the training sequence
		TrainingSequence ts = 
				TrainingSequence.newNDRealVectorTrainingSequence(trainingFilename, classColourMapFilename);
		
		// Weak learner to use
		WeakLearner wl = new OneDimensionalLinearWeakLearner();
		
		// Train the tree
		DecisionForest forest = Learner.trainDecisionForest(ts, wl, maxTrees, maxDepth, 
				randomnessParameter, informationGainCutoff, normaliseSpectra, bagging);
		
		// Save the tree
		forest.saveToFrstFile(outputFilename);
	}
	
	/***
	 * Perform a pixel labeling using a given forest
	 * @param forestFilename The filename of the .frst file to use
	 * @param imageSpecifier The filename of the image. If it contains a '%' char then assume that 
	 * 					it's a set of monochrome images, otherwise assume RGB.
	 * @param outputFilename The filename of the output image
	 * @param imageType The type of image that we wish to output
	 * @param singleProbClassName If we are outputting a single class probability, then we need to 
	 * 					input the class' name 
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws FileFormatException
	 * @throws MalformedForestException
	 * @throws MalformedProbabilityDistributionException
	 * @throws InterruptedException 
	 */
	public static void mainRFRun(String forestFilename, String imageSpecifier, String outputFilename,
			String imageType, String singleProbClassName) 
			throws FileNotFoundException, IOException, FileFormatException, MalformedForestException, 
			MalformedProbabilityDistributionException, InterruptedException {
		// Load forest
		DecisionForest df = new DecisionForest(forestFilename);
		
		// Now use the forest to classify an image
		DataCube dc = DataCube.generateDataCubeFromImageSpecifier(imageSpecifier);
		
		// Generate the pixel labeled image, depending on the image option picked!
		if (imageType.equals("prob")) {
			dc.generatePixelLabeledImage(df, outputFilename, LabelledImageType.PROBABILITY, null);
		} else if (imageType.equals("mostlikely")) {
			dc.generatePixelLabeledImage(df, outputFilename, LabelledImageType.MOST_LIKELY, null);
		} else if (imageType.equals("entropy")) {
			dc.generatePixelLabeledImage(df, outputFilename, LabelledImageType.ENTROPY, null);
		} else if (imageType.equals("singleprob")) {
			// In the case of a single probability class, we need to find the class first!
			ClassLabel ourClass = null;
			for (ClassLabel clazz : df.getClasses()) {
				if (clazz.getName().equals(singleProbClassName)) {
					ourClass = clazz;
				}
			}
			
			// Potentially generate an error
			if (ourClass == null) {
				throw new IllegalArgumentException("A valid class name was not specified for the "
						+ "single class probability image.");
			}
			
			// Generate the image finally
			dc.generatePixelLabeledImage(df, outputFilename, LabelledImageType.SINGLE_CLASS_PROBABILITY, ourClass);
		} else {
			throw new IllegalArgumentException("You need to specify an image type to output.");
		}
	}
	
	/***
	 * Take a hyperspectral image and add a poisson noise to it.
	 * 
	 * @param imageSpecifier The hyperspectral image specifier input
	 * @param outputImageSpecifier The hyperspectral image specifier output 
	 * @throws FileFormatException 
	 * @throws IOException 
	 */
	public static void mainPNRun(String imageSpecifier, String outputImageSpecifier) 
			throws FileFormatException, IOException {
		
		// Check that the inputs are ok
		if (imageSpecifier.contains("%") != outputImageSpecifier.contains("%") ||
				imageSpecifier.indexOf("%") != imageSpecifier.lastIndexOf("%") ||
				outputImageSpecifier.indexOf("%") != outputImageSpecifier.lastIndexOf("%")) {
			throw new IllegalArgumentException("Image and output file specifiers should "
					+ "contain a single '%' character if using a set of monochrome images, they "
					+ "should contain no '%' characters if using an RGB image. The input file "
					+ "specifier should contain a '%' iff the output file specifier does.");
		}
		
		// Load the image in
		DataCube dc = DataCube.generateDataCubeFromImageSpecifier(imageSpecifier);
							
		// Apply the Poisson noise
		ArtificialNoise.applyPoissonNoise(dc);
		
		// Output the image
		if (!outputImageSpecifier.contains("%")) {
			dc.printToColourImage(outputImageSpecifier);
		} else {
			dc.printToMonochromeImages(outputImageSpecifier);
		}
	}
	
	/***
	 * Take a hyperspectral image and add a gaussian noise to it.
	 * 
	 * @param imageSpecifier The hyperspectral image specifier input
	 * @param power The power of the gaussian noise that we will apply
	 * @param outputImageSpecifier The hyperspectral image specifier output 
	 * @throws FileFormatException 
	 * @throws IOException 
	 */
	public static void mainGNRun(String imageSpecifier, double power, String outputImageSpecifier) 
			throws FileFormatException, IOException {
		
		// Check that the inputs are ok
		if (imageSpecifier.contains("%") != outputImageSpecifier.contains("%") ||
				imageSpecifier.indexOf("%") != imageSpecifier.lastIndexOf("%") ||
				outputImageSpecifier.indexOf("%") != outputImageSpecifier.lastIndexOf("%")) {
			throw new IllegalArgumentException("Image and output file specifiers should "
					+ "contain a single '%' character if using a set of monochrome images, they "
					+ "should contain no '%' characters if using an RGB image. The input file "
					+ "specifier should contain a '%' iff the output file specifier does.");
		} else if (power < 0) {
			throw new IllegalArgumentException("Power needs to be a valid non-negative number");
		}
		
		// Load the image in
		DataCube dc = DataCube.generateDataCubeFromImageSpecifier(imageSpecifier);
							
		// Apply the Poisson noise
		ArtificialNoise.applyGaussianNoise(dc, power);
		
		// Output the image
		if (!outputImageSpecifier.contains("%")) {
			dc.printToColourImage(outputImageSpecifier);
		} else {
			dc.printToMonochromeImages(outputImageSpecifier);
		}
	}
	
	/***
	 * Create a training sequence from an example ground truth image
	 * 
	 * N.B. If the outputFilename exists already, then we APPEND to the file
	 * 
	 * @param colourClassMapFilename
	 * @param exampleLabelingFilename
	 * @param exampleImageSpecifier
	 * @param outputFilename
	 * @throws FileFormatException 
	 * @throws IOException 
	 */
	private static void mainTrainingTools(String colourClassMapFilename, String exampleLabelingFilename,
			String exampleImageSpecifier, String outputFilename) throws IOException, FileFormatException {
		
		// Load in the new additions to the training sequence
		TrainingSequence tsImg = 
				TrainingSequenceGeneration.trainingSequenceFromExample(colourClassMapFilename, 
				exampleLabelingFilename, exampleImageSpecifier);
		
		// Check if the output filename ALREADY exists, if it does then APPEND new training data
		TrainingSequence ts;
		if (new File(outputFilename).exists()) {
			TrainingSequence tsExist = 
					TrainingSequence.newNDRealVectorTrainingSequence(outputFilename, colourClassMapFilename);
			ts = TrainingSequence.join(tsExist, tsImg);
		} else {
			ts = tsImg;
		}
		ts.saveToTextFile(outputFilename);
	}
	
	/***
	 * Private method to print usage of jar to a print stream
	 * @param o The output print stream. (System.out or System.err).
	 */
	private static void printUsage(PrintStream o) {
		o.println("Please use the the jar in the following way:");
		o.println("java -jar SIC.jar <task> <arguments>");
		o.println("<task> is a string specifying the task to perform. <arguments> is a "
				+ "\nspace sepearated list of arguments passed to the task.");
		o.println("\n\n");
		o.println("Options for <task> and their arguments are as follows:\n\n");
		o.println("Generate an example 3 dimensional RGB image and training set.");
		o.println("<task> = " + tasks[0]);
		o.println("\t arg1 = dir (string)");
		o.println("\t\t The directory to place the generated image and training "
				+ "\n\t\t sequence files");
		o.println();
		o.println("Generate an example 5 dimensional image (5 greyscale .png's) and training "
				+ "\nsequence.");
		o.println("<task> = " + tasks[1]);
		o.println("\t arg1 = \"dir\"");
		o.println("\t\t The directory to place the generated image and training "
				+ "\n\t\t sequence files");
		o.println();
		o.println("Train a random forest.");
		o.println("<task> = " + tasks[2]);
		o.println("\t arg1 = trainingFilename (string)");
		o.println("\t\t The filename of the training sequence file.");
		o.println("\t arg2 = classColourMapFilename (string)");
		o.println("\t\t The filename of the class to colour mapping text file.");
		o.println("\t arg3 = outputFilename (string)");
		o.println("\t\t The filename of the .frst file to be output.");
		o.println("\t arg4 = numberOfTrees (int)");
		o.println("\t\t The number of trees to be trained in the forest.");
		o.println("\t arg5 = maxDepth (int)");
		o.println("\t\t The maximum depth of a tree.");
		o.println("\t arg6 = randomnessParameter (int)");
		o.println("\t\t How many 'split parameters' to try at each 'split node' "
				+ "\n\t\t whilst training. A greater value corresponds to a less "
				+ "\n\t\t random tree.");
		o.println("\t arg7 = informationGainCutoff (double)");
		o.println("\t\t The minimal 'information' we wish to gain at any given "
				+ "\n\t\t split node.");
		o.println("\t arg8 = normaliseSpectra (bool) (true/false/yes/no)");
		o.println("\t\t Should image spectra be normalised when labelling "
				+ "\n\t\t and training? (Normalise power of the spectrum.)");
		o.println("\t arg8 = bagging (bool) (true/false/yes/no)");
		o.println("\t\t Should we perform bagging of the training sequence "
				+ "\n\t\t whilst training the forest?");
		o.println();
		o.println("Train a Neural Network.");
		o.println("TODO");
		o.println();
		o.println("Use a Random Forest to label pixels in an image");
		o.println("<task> = " + tasks[4]);
		o.println("\t arg1 = forestFilename (string)");
		o.println("\t\t The filename of the .frst file");
		o.println("\t arg2 = imageSpecifier (string)");
		o.println("\t\t The specficier of the image. This is either just the "
				+ "\n\t\t filename of an RGB image or if it contains a '%' "
				+ "\n\t\t character it will replace the '%' with numbers to form "
				+ "\n\t\t an image with higher dimensions."); 
		o.println("\t arg3 = outputFilename (string)");
		o.println("\t\t The filename to output a RGB image indicating the pixel "
				+ "\n\t\t pixel labelling.");
		o.println("\t arg4 = imageType (string)");
		o.println("\t\t This should be one of \"prob\", \"mostlikely\", \"entropy\", "
				+ "\n\t\t \"singleprob\" for a probability output, a most likely "
				+ "labelling, an entropy labelling or a labelling according to "
				+ "the probabiltiy of a single class respectively.");
		o.println("\t (OPTIONAL) arg5 = className");
		o.println("\t\t If we specify \"singleprob\" then we need to specify the "
				+ "\n\t\t associated class name.");
		o.println();
		o.println("Use a Neural Network to label pixels in an image.");
		o.println("TODO");
		o.println();
		o.println("Add a Poisson noise to an image");
		o.println("<task> = " + tasks[6]);
		o.println("\t arg1 = imageSpecifier (string)");
		o.println("\t\t The specficier of the image. This is either just the "
				+ "\n\t\t filename of an RGB image or if it contains a '%' "
				+ "\n\t\t character it will replace the '%' with numbers to form "
				+ "\n\t\t an image with higher dimensions."); 
		o.println("\t arg2 = outputImageSpecifier (string)");
		o.println("\t\t The specficier of the output image. This is either just the "
				+ "\n\t\t filename of an RGB image or if it contains a '%' "
				+ "\n\t\t character it will replace the '%' with numbers to form "
				+ "\n\t\t an image with higher dimensions. arg2 should include a '%'"
				+ "\n\t\t if and only if arg1 does.");
		o.println();
		o.println("Add a Gaussian noise to an image");
		o.println("<task> = " + tasks[7]);
		o.println("\t arg1 = imageSpecifier (string)");
		o.println("\t\t The specficier of the image. This is either just the "
				+ "\n\t\t filename of an RGB image or if it contains a '%' "
				+ "\n\t\t character it will replace the '%' with numbers to form "
				+ "\n\t\t an image with higher dimensions.");
		o.println("\t arg2 = power (double)");
		o.println("\t\t The power of the Gaussian noise that you would like to "
				+ "\n\t\t to the image. Power = (standard deviation)^2");
		o.println("\t arg3 = outputImageSpecifier (string)");
		o.println("\t\t The specficier of the output image. This is either just the "
				+ "\n\t\t filename of an RGB image or if it contains a '%' "
				+ "\n\t\t character it will replace the '%' with numbers to form "
				+ "\n\t\t an image with higher dimensions. arg2 should include a '%'"
				+ "\n\t\t if and only if arg1 does.");
		o.println();
		o.println("Generate a training sequence from an example pixel labeled image.");
		o.println("<task> = " + tasks[8]);
		o.println("\t arg1 = classColourMapFilename (string)");
		o.println("\t\t The filename of a .txt file that describes a mapping between "
				+ "\n\t\t class names and their pixel label colours.");
		o.println("\t arg2 = exampleLabelingFilename (string)");
		o.println("\t\t The filename of an example pixel labeling of the corresponding "
				+ "\n\t\t spectral image (arg3).");
		o.println("\t arg3 = exampleImageSpecifier (string)");
		o.println("\t\t The specficier of the example image. This is either just the "
				+ "\n\t\t filename of an RGB image or if it contains a '%' "
				+ "\n\t\t character it will replace the '%' with numbers to form "
				+ "\n\t\t an image with higher dimensions.");
		o.println("\t arg4 = trainingSequenceFile (string)");
		o.println("\t\t The intended filename for the training sequence being generated."
				+ "\n\t\t If the file already exists, then we APPEND the new data to "
				+ "\n\t\t this new file.");
		
	}

}
