package uk.ac.cam.mp703.TrainingTools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.imageio.ImageIO;

import uk.ac.cam.mp703.ImagePixelLabelling.DataCube;
import uk.ac.cam.mp703.RandomDecisionForests.FileFormatException;
import uk.ac.cam.mp703.RandomDecisionForests.NDRealVector;
import uk.ac.cam.mp703.RandomDecisionForests.TrainingSample;
import uk.ac.cam.mp703.RandomDecisionForests.TrainingSequence;

public class TrainingSequenceGeneration {
	
	/***
	 * We provide a mapping between RGB colours and class names, with the following file format:
	 * ------------------------------
	 * | Class1Name, 0xFFFFFF, 		|
	 * | Class2Name, 0xFFFFFF, 		|
	 * | Class3Name, 0xFFFFFF, 		|
	 * | ...		 				|
	 * | ClassMName, 0xFFFFFF 		|
	 * ------------------------------
	 * N.B. The numbers 0xFFFFFF stand for a hex number that should correspond to the colour 
	 * associated with the class.
	 * 
	 * N.B.B. No semi colon at the end - it will still work with a semi-colon at the end however 
	 * 
	 * 
	 * We then have an example labeling for an example image. Any colours NOT found in the class 
	 * map will be IGNORED in the example labelling, and will NOT appear in the output.
	 * 
	 * 
	 * After loading the class map, we look at the example labelling, and generate a training 
	 * sequence from the related class colours and pixel 'values'
	 * 
	 * @param colourClassMapFilename
	 * @param exampleLabelingFilename
	 * @param exampleImageSpecifier
	 * @throws IOException 
	 * @throws FileFormatException 
	 */
	public static TrainingSequence trainingSequenceFromExample(String colourClassMapFilename, 
			String exampleLabelingFilename, String exampleImageSpecifier) 
					throws IOException, FileFormatException {
		
		// Load in the mapping between class names and colours (in the example labelling) 
		Map<Integer, String> colourClassMap = loadClassMap(colourClassMapFilename);
		
		// Load in the examplePixelLabeling
		File labelingImageFile = new File(exampleLabelingFilename);
		BufferedImage examplePixelLabeling = ImageIO.read(labelingImageFile);
		
		// Load in the datacube from the image specifier
		DataCube datacube = DataCube.generateDataCubeFromImageSpecifier(exampleImageSpecifier);
		
		// Check the dimensions of the labeling are correct
		if (examplePixelLabeling.getHeight() != datacube.getHeight() ||
				examplePixelLabeling.getWidth() != datacube.getWidth()) {
			throw new InvalidParameterException("The width and height of the example image and "
					+ "example labeling should agree.");
		}
		
		// Get the list of class names, we use the indices as the class numbers
		// We get the class strings from the class map, and shove the collection into a list
		List<String> classNames = new ArrayList<String>(colourClassMap.values());
		
		// Create the list of training samples we're going to create
		List<TrainingSample> trainingSamples = new ArrayList<TrainingSample>();
		
		// Create a training sequence from iterating through the pixels
		short[][][] dc = datacube.getDataCube();
		for (int i = 0; i < examplePixelLabeling.getWidth(); i++) {
			for (int j = 0; j < examplePixelLabeling.getHeight(); j++) {
				
				// Check that the colour in the pixel is in the class map, skip this pixel if not
				// We are only concerned with the RGB values and not the alpha values
				int colour = examplePixelLabeling.getRGB(i, j) & 0x00FFFFFF;
				if (!colourClassMap.containsKey(colour)) {
					continue;
				}
						
				// Create an NDVector from the data cube pixel
				List<Double> lst = new ArrayList<>(datacube.getDepth());
				for (int k = 0; k < datacube.getDepth(); k++) {
					lst.add((double) dc[i][j][k]);
				}
				NDRealVector vec = new NDRealVector(lst);
				
				// Add the sample to the list
				String className = colourClassMap.get(colour);
				int classNumber = classNames.indexOf(className);
				TrainingSample sample = new TrainingSample(classNumber, vec);
				trainingSamples.add(sample);
			}
		}
		
		// Finally we need to compute the list of the class colours, with corresponding indices to 
		// class names. We can compute this from the mapping from colours to class names and the 
		// class name list
		List<Integer> classColours = computeClassColours(colourClassMap, classNames);
		
		// Create the training sequence structure and return it
		return new TrainingSequence(trainingSamples, classNames, classColours);
	}
	
	/***
	 * List a mapping between class names and colours:
	 * ------------------------------
	 * | Class1Name, 0xFFFFFF, 		|
	 * | Class2Name, 0xFFFFFF, 		|
	 * | Class3Name, 0xFFFFFF, 		|
	 * | ...		 				|
	 * | ClassMName, 0xFFFFFF 		|
	 * ------------------------------
	 * N.B. The numbers 0xFFFFFF stand for a hex number that should correspond to the colour 
	 * associated with the class.
	 * 
	 * N.B.B. 0x000000 is reserved to indicate that the pixel in the example image should be ignored
	 * 
	 * N.B.B.B. No semi colon at the end - it will still work with a semi-colon at the end however
	 * 
	 * @param classMapFilename The text file 
	 * @throws FileFormatException 
	 * @throws FileNotFoundException 
	 */
	private static Map<Integer, String> loadClassMap(String colourClassMapFilename) 
			throws FileFormatException, FileNotFoundException {
		
		// Make a scanner to scan through the file, separated at commas (ignoring whitespace)
		Scanner scanner = new Scanner(new File(colourClassMapFilename));
		scanner.useDelimiter("\\s*,\\s*");
		
		// Create the mapping from integers to class names
		Map<Integer, String> colourClassMap = new HashMap<>();
		
		// Iterate through the class names and colours and add them to the map
		int classColour;
		String className;
		while (scanner.hasNext()) {
			// Get the name
			className = scanner.next();
			
			// Check that there is a corresponding colour
			if (!scanner.hasNext()) {
				scanner.close();
				throw new FileFormatException("Every class name needs to have a corresponsing "
						+ "colour in the class map.");
			}
			
			// GET the colour, remember that it may have a prepended "0x" and a postpended ";"
			try {
				// extract the hex string
				String classColourString = scanner.next();
				classColourString = classColourString.replaceAll("0x", "");
				classColourString = classColourString.replaceAll("\\s*;", ""); // For extra convenience allow whitespace
				
				// Check that its the correct length
				if (classColourString.length() != 6) {
					throw new FileFormatException("The colour for each class should specified by a "
							+ "6 digit hex number");
				}
				
				// Parse the integer
				classColour = Integer.parseInt(classColourString, 16); 
				
			// Catch a number format exception, that means that there was a format error
			} catch (NumberFormatException e) {
				scanner.close();
				throw new FileFormatException("Each class needs to have an associated colour, "
						+ "specified by a 6 digit hex number.");
			}
			
			// Add the number, string mapping, checking that class names and colours aren't repeated
			if (colourClassMap.containsKey(classColour) || colourClassMap.containsValue(className)) {
				scanner.close();
				throw new FileFormatException("Colours and class names must each be unique");
			}
			
			colourClassMap.put(classColour, className);
		}
		
		// Close the scanner
		scanner.close();
		
		// Return the class map
		return colourClassMap;
	}
	
	/***
	 * We want to compute a list of colours (List<Integer>) that corresponds to the list of class names.
	 * What we can do this by just looking up each name in the map once, then setting the correct 
	 * index in the new colour list to the correct colour.
	 * @param colourClassMap A map from colours to classNames (and not the other way around)
	 * @param classNames A list of class names
	 * @return
	 */
	private static List<Integer> computeClassColours(Map<Integer, String> colourClassMap, List<String> classNames) {
		// Make the new list (and fill it with zeros)
		List<Integer> classColours = new ArrayList<Integer>(classNames.size());
		for (int i = 0; i < classNames.size(); i++) {
			classColours.add(0);
		}
		
		// Iterate through each pair in the mapping, look up the class number using class names 
		// (i.e. the index into the class names array) then place the colour in the correct place in 
		// the classColours list
		for (Entry<Integer, String> entry : colourClassMap.entrySet()) {
			int index = classNames.indexOf(entry.getValue());
			classColours.set(index, entry.getKey());
		}
		
		// Return the classColours list
		return classColours;
	}
}
