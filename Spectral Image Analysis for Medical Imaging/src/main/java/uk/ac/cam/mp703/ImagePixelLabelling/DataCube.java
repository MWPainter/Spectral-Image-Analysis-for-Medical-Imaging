package uk.ac.cam.mp703.ImagePixelLabelling;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.neural.networks.BasicNetwork;

import uk.ac.cam.mp703.RandomDecisionForests.ClassLabel;
import uk.ac.cam.mp703.RandomDecisionForests.DecisionForest;
import uk.ac.cam.mp703.RandomDecisionForests.MalformedForestException;
import uk.ac.cam.mp703.RandomDecisionForests.MalformedProbabilityDistributionException;
import uk.ac.cam.mp703.RandomDecisionForests.NDRealVector;
import uk.ac.cam.mp703.RandomDecisionForests.ProbabilityDistribution;

/***
 * A structure to hold a data cube, and related operations.
 * We have three dimensions in an image, x, y and a spectrum of information.
 * 
 * @author michaelpainter
 *
 */
public class DataCube {
	
	/***
	 * The coordinates used in the data cube will be:
	 * [x][y][s]
	 * where s ranges over the spectral bin's indices.u
	 */
	short[][][] dataCube;	
	
	/***
	 * Width of the data cube image
	 */
	int width;

	/***
	 * Height of the cube image
	 */
	int height;
	
	/***
	 * The depth, or number of spectral bins in the data cube
	 */
	int depth;
	
	/**
	 * @return the dataCube
	 */
	public short[][][] getDataCube() {
		return dataCube;
	}
	 
	/**
	 * @param dataCube the dataCube to set
	 */
	public void setDataCube(short[][][] dataCube) {
		this.dataCube = dataCube;
		this.width = dataCube.length;
		this.height = dataCube[0].length;
		this.depth = dataCube[0][0].length;
	}
	
	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @return the depth
	 */
	public int getDepth() {
		return depth;
	}
	
	/***
	 * Create a datacube from an image specifier (a filename). 
	 * 
	 * If the image specifier contains a '%' then that stands for a wildcard, and will load data 
	 * from (assumed) monochrome images, replacing '%' by 1, 2, ..., N, where (N+1) is the first 
	 * replacement that doesn't lead to a valid image filename.
	 * 
	 * If the image specifier doesn't contain a '%' then we assume that the image is a RGB image and 
	 * we should consider the image to have 3 spectral bins.
	 * 
	 * 
	 * @param filenameSpecifier A string representing the format of many images. We use a single '%' 
	 * 			to denote replacement by a number starting at 1. For example "image%" will search 
	 * 			for files "image1", "image2", ..., "imageN", until there is a gap. It will then put 
	 * 			the value of (x,y) in image I into the value dataCube[x][y][I]. If there is no '%' then
	 * 			we assume that we have an RGB image.
 	 * @return Returns a datacube with N spectral bins, specified by N images, or 3 bins by an RGB image
	 * @throws IOException
	 * @throws IOException
	 */
	public static DataCube generateDataCubeFromImageSpecifier(String imageSpecifier) throws IOException {
		return imageSpecifier.contains("%") ? 
					generateDataCubeFromMonochromeImages(imageSpecifier) :
					generateDataCubeFromColourImage(imageSpecifier);
	}

	/***
	 * Create a datacube from an RGB image
	 * @param filename The filename containing the image
	 * @return The data cube, with 3 "spectral bins", red, green, blue 
	 * @throws IOException 
	 */
	private static DataCube generateDataCubeFromColourImage(String filename) throws IOException {
		// Get an image from the filename
		BufferedImage image = ImageIO.read(new File(filename));
		
		// Read meta data from image and create a data cube
		int width = image.getWidth();
		int height = image.getHeight();
		DataCube dc = new DataCube();
		dc.width = width;
		dc.height = height;
		dc.depth = 3;
		dc.dataCube = new short[width][height][3];
		
		// Read all of the data from the image into the datacube data
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int colour = image.getRGB(i, j);
				dc.dataCube[i][j][0] = (short) ((colour >> 16) & 0xFF);
				dc.dataCube[i][j][1] = (short) ((colour >> 8)  & 0xFF);
				dc.dataCube[i][j][2] = (short) ((colour >> 0)  & 0xFF);
			}
		}
		
		// We've loaded all the data, so return the data cube
		return dc;
	}
	
	/***
	 * Create a datacube from a series of N images.
	 * @param filenameSpecifier A string representing the format of many images. We use a single '%' 
	 * 			to denote replacement by a number starting at 1. For example "image%" will search 
	 * 			for files "image1", "image2", ..., "imageN", until there is a gap. It will then put 
	 * 			the value of (x,y) in image I into the value dataCube[x][y][I].
 	 * @return Returns a datacube with N spectral bins, specified by N images.
	 * @throws IOException 
	 */
	private static DataCube generateDataCubeFromMonochromeImages(String filenameSpecifier) throws IOException {
		// Check the format of the string
		if (!filenameSpecifier.contains("%") || 
				filenameSpecifier.indexOf("%") != filenameSpecifier.lastIndexOf("%")) {
			throw new IllegalArgumentException("Filename specifier must contain exactly one '%'.");
		}
		
		// Find the dimension (number of monochrome images) of the hyperspectral image
		int noImages = 0;
		File file = new File(filenameSpecifier.replace("%",  Integer.toString(noImages+1)));
		while (file.exists() && !file.isDirectory()) {
			noImages++;
			file = new File(filenameSpecifier.replace("%",  Integer.toString(noImages+1)));
		}
		
		// Check that we loaded at least one image
		if (noImages == 0) {
			throw new FileNotFoundException("No images were found using the specifier provided.");
		}
		
		// Get the width and height of the first image, to check that they have consistent dimensions
		file = new File(filenameSpecifier.replace("%",  Integer.toString(1)));
		BufferedImage bi = ImageIO.read(file);
		int height = bi.getHeight();
		int width = bi.getWidth();
		
		// Create a data cube and load the images into the structure
		DataCube dc = new DataCube();
		dc.width = width;
		dc.height = height;
		dc.depth = noImages;
		dc.dataCube = new short[width][height][noImages];
		
		// Loop through each image, and add its contents to the data cube
		// We loop through images as our outer loop because we want to optimise memory management
		// (Avoid loading all images into memory at once)
		for (int k = 0; k < noImages; k++) {
			File imageFile = new File(filenameSpecifier.replace("%", Integer.toString(k+1))); // images indexed from 1
			BufferedImage image = ImageIO.read(imageFile);
		
			// Check for consistent image dimension
			if (image.getHeight() != height || image.getWidth() != width) {
				throw new IOException("Images have inconsistent dimensions.");
			}
			
			// Iterate through image adding its data to the datacube
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					int colour = image.getRGB(i, j);
					dc.dataCube[i][j][k] = (short) (colour & 0xFF);
				}
			}	
		}
		
		// Return the data cube
		return dc;
	}
	
	/***
	 * A routine to print a datacube out for inspection. Not really necessary in the end system, 
	 * but useful for testing and seeing what's happening.
	 * 
	 * @param filename The image's filename
	 * @throws IOException
	 */
	public void printToColourImage(String filename) throws IOException {
		// Check the format of the string
		if (filename.contains("%")) {
			throw new IllegalArgumentException("Filename specifier for a colour image shouldn't "
					+ "contain a '%'.");
		}
		
		// Also check that we only have 3 dimensions, otherwise this doesnt work
		if (this.dataCube[0][0].length != 3) {
			throw new DataCubeException("Datacube must have 3 spectral bands to be output as an RGB"
					+ " image.");
		}
		
	    // Retreive the image
	    BufferedImage img = new BufferedImage(this.dataCube.length, this.dataCube[0].length, 
	    		BufferedImage.TYPE_3BYTE_BGR);
	    for (int i = 0; i < dataCube.length; i++) {
	    	for (int j = 0; j < dataCube[0].length; j++) {
	    		int rgb = (((int) dataCube[i][j][0]) << 16) |
	    				(((int) dataCube[i][j][1]) << 8) |
	    				(((int) dataCube[i][j][2]) << 0);
	    		img.setRGB(i, j, rgb);
	    	}
	    }
	    
	    // Output the file as a png image
	    File outputfile = new File(filename.endsWith(".png") ? filename : filename + ".png");
	    ImageIO.write(img, "png", outputfile);
	}
	
	/***
	 * A routine to print a datacube out for inspection. Not really necessary in the end system, 
	 * but useful for testing and seeing what's happening.
	 * 
	 * @param filename The image's filename
	 * @throws IOException
	 */
	public void printToMonochromeImages(String filenameSpecifier) throws IOException {
		// Check the format of the string
		if (!filenameSpecifier.contains("%") || 
				filenameSpecifier.indexOf("%") != filenameSpecifier.lastIndexOf("%")) {
			throw new IllegalArgumentException("Filename specifier must contain exactly one '%'.");
		}
		
		// An image data structure to use to print out
	    BufferedImage img = new BufferedImage(this.dataCube.length, this.dataCube[0].length, 
	    		BufferedImage.TYPE_3BYTE_BGR);
	    
	    // Iterate through each spectral bin
	    for (int k = 0; k < dataCube[0][0].length; k++) {
	    	
	    	// Iterate through each pixel in the image, setting the (grey) value in the image
		    for (int i = 0; i < dataCube.length; i++) {
		    	for (int j = 0; j < dataCube[0].length; j++) {
		    		int colour = (((int) dataCube[i][j][k]) << 0) | 
		    				(((int) dataCube[i][j][k]) << 8) |
		    				(((int) dataCube[i][j][k]) << 16) ;
		    		img.setRGB(i, j, colour);
		    	}
	    	}
		    
		    // Output the file to the k-th image 
		    // N.B. Take care to start indices from 1
		    String filename = filenameSpecifier.replaceAll("%", Integer.toString(k+1));
		    File outputfile = new File(filename.endsWith(".png") ? filename : filename + ".png");
		    ImageIO.write(img, "png", outputfile);
	    }
	}
	
	/***
	 * Using a decision forest, label each pixel of an image, producing a new image - a pixel labelling
	 * 
	 * @param classifier A decision forest used for classifications/pixel labeling
	 * @param classMap A map from class numbers to colours for the pixel labeled image
	 * @param filename The filename to save the image into
	 * @param imageType The type of image to output, i.e. how do we want to convert the probability 
	 * 		distribution into an image
	 * @param clazz If imageType = singleClassProbability then this is the class we are looking at
	 * @throws MalformedForestException
	 * @throws IOException
	 * @throws MalformedProbabilityDistributionException 
	 */
	public void generatePixelLabeledImage(DecisionForest classifier, String filename, 
			LabelledImageType imageType, ClassLabel clazz) 
			throws MalformedForestException, IOException, MalformedProbabilityDistributionException {
		
		// Get the file extension, if no extension then throw exception immediately
		String extension = "";
		int l = filename.lastIndexOf('.');
		if (l > 0) {
		    extension = filename.substring(l+1);
		} else {
			throw new IllegalArgumentException("No file extension for the file.");
		}
		
		// Create an RGB image that we will output for our pixel labeling
		int width = dataCube.length;
		int height = dataCube[0].length;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		
		// Iterate through each pixel, classify/label each pixel, and write it to the new image
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				ArrayList<Double> arr = new ArrayList<>(dataCube[i][j].length);
				for (int k = 0; k < dataCube[i][j].length; k++) {
					arr.add(k, (double) dataCube[i][j][k]);
				}
				NDRealVector v = new NDRealVector(arr);
				ProbabilityDistribution probabilities = classifier.classify(v);
				if (imageType == LabelledImageType.PROBABILITY) {
					image.setRGB(i, j, interpolatedColour(probabilities, classifier.getClasses()));
				} else if (imageType == LabelledImageType.SINGLE_CLASS_PROBABILITY) {
					image.setRGB(i, j, singleProbColour(probabilities, clazz));
				} else if (imageType == LabelledImageType.MOST_LIKELY) {
					image.setRGB(i, j, mostlikelyColour(probabilities, classifier.getClasses()));
				} else if (imageType == LabelledImageType.ENTROPY) {
					image.setRGB(i, j, entropyColour(probabilities));
				}  
			}
		}
		
		// Write the image to file
		File file = new File(filename);
		ImageIO.write(image, extension, file);
	}
	
	/***
	 * Using a neural network, label each pixel of an image, producing a new image - a pixel labeling
	 * 
	 * @param classifier A neural network used for classifications/pixel labeling
	 * @param helper A normalisation helper, used to normalise data before it's classified by the network
	 * @param classMap A map from class names to colours for the pixel labeled image
	 * @param filename The filename to save the image into
	 * @throws MalformedForestException
	 * @throws IOException
	 */
	public void generatePixelLabeledImageNeuralNetwork(BasicNetwork classifier, NormalizationHelper helper, 
			Map<String, Color> classMap, String filename) throws MalformedForestException, IOException {
		
		// Get the file extension, if no extension then throw exception immediately
		String extension = "";
		int l = filename.lastIndexOf('.');
		if (l > 0) {
		    extension = filename.substring(l+1);
		} else {
			throw new IllegalArgumentException("No file extension for the file.");
		}
		
		// Create an RGB image that we will output for our pixel labeling
		int width = dataCube.length;
		int height = dataCube[0].length;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
		
		// Iterate through each pixel, classify/label each pixel, and write it to the new image
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				// Build an item of MLData from the image, which is input to the neural network
				String[] strs = new String[dataCube[i][j].length];
				for (int k = 0; k < dataCube[i][j].length; k++) {
					strs[k] = Short.toString(dataCube[i][j][k]);
				}
				MLData datum = helper.allocateInputVector();
				helper.normalizeInputVector(strs, datum.getData(), false);
				
				// Use the neural network to provide a classification from the datum
				MLData output = classifier.compute(datum);
				
				// Get the class from the output
				String clazz = helper.denormalizeOutputVectorToString(output)[0];
				
				// Set the RGB image colour
				image.setRGB(i, j, classMap.get(clazz).getRGB());
			}
		}
		
		// Write the image to file
		File file = new File(filename);
		ImageIO.write(image, extension, file);
	}
	

	
	/***
	 * Given a probability distribution and a class -> colour map, compute an interpolated colour 
	 * from these.
	 * @param probDistr The probability distribution of the instance being each class
	 * @param classes A list of class colours (indexed by the class number)
	 * @return A colour for the pixel labeling. THe closer it is to a one of the colours in the 
	 * 		colour map, the more cirtain we are that is 
	 */
	public int interpolatedColour(ProbabilityDistribution probDistr, List<ClassLabel> classes) {
		Map<ClassLabel, Double> probabilities = probDistr.getProbabilityDistribution();
		double rSum = 0.0;
		double gSum = 0.0;
		double bSum = 0.0;
		
		// Sum colours according to probs
		// Split into components to do so
		for (ClassLabel clazz : classes) {
			int colour = clazz.getColour();
			int r = ((colour >> 16) & 0xFF);
			int g = ((colour >> 8)  & 0xFF);
			int b = ((colour >> 0)  & 0xFF);
			double prob = probabilities.get(clazz);
			rSum += prob * r;
			gSum += prob * g;
			bSum += prob * b; 
		}
		
		// Return the colour in an integer form |..R..|..G..|..B..|
		return (((int)rSum & 0xFF) << 16) + (((int)gSum & 0xFF) << 8) + (((int)bSum & 0xFF) << 0);
	}
	
	/***
	 * Given a probability distribution and a class, compute the colour depending on 
	 * a single probability
	 * 
	 * @param probDistr The probability distribution of the instance being each class
	 * @param clazz The class we are looking at outputting the probability distribution of
	 * @return A colour for the pixel labeling. THe closer it is to a one of the colours in the 
	 * 		colour map, the more cirtain we are that is 
	 */
	public int singleProbColour(ProbabilityDistribution probDistr, ClassLabel clazz) {
		Map<ClassLabel, Double> probabilities = probDistr.getProbabilityDistribution();
		
		// Get the probability depending on the class
		double prob = probabilities.get(clazz);
		
		// Compute the magnitude of the greyscale, white being an area of high probability
		int val = (int) (prob * 255);
		
		// Return the colour in an integer form |..R..|..G..|..B..|
		return (((int)val & 0xFF) << 16) + (((int)val & 0xFF) << 8) + (((int)val & 0xFF) << 0);
	}
	
	/***
	 * Given a probability distribution and a class -> colour map, compute an interpolated colour 
	 * from these.
	 * @param probDistr The probability distribution of the instance being each class
	 * @param classes A list of class colours (indexed by the class number)
	 * @return A colour for the pixel labeling. THe closer it is to a one of the colours in the 
	 * 		colour map, the more cirtain we are that is 
	 */
	public int mostlikelyColour(ProbabilityDistribution probDistr, List<ClassLabel> classes) {
		return probDistr.mostProbableClass().getColour();
	}
	
	/***
	 * Given a probability distribution compute the entropy and output a suitable pixel value.
	 * Note that white means high entropy, and so we have NO IDEA what it is. BLACK IS GOOD HERE!
	 * 
	 * @param probDistr The probability distribution of the instance being each class
	 * @return A colour for the pixel labeling. THe closer it is to a one of the colours in the 
	 * 		colour map, the more cirtain we are that is 
	 */
	public int entropyColour(ProbabilityDistribution probDistr) {
		// Get the entropy from the probability distribution
		double entropy = probDistr.entropy();
		
		// Work out what the maximum possible entropy was from the number of classes
		double maxEntropy = Math.log(probDistr.getProbabilityDistribution().entrySet().size()) / Math.log(2);
		
		// Compute a greyscale value based on normalised entropy
		double normalisedEntropy = entropy / maxEntropy;
		int val = (int) (normalisedEntropy * 255);
		
		// Return the colour in an integer form |..R..|..G..|..B..|
		return (((int)val & 0xFF) << 16) + (((int)val & 0xFF) << 8) + (((int)val & 0xFF) << 0);
	}
}