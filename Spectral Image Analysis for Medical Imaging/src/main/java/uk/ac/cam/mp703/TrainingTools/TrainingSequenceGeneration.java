mpackage uk.ac.cam.mp703.TrainingTools;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.imageio.ImageIO;

import uk.ac.cam.mp703.ImagePixelLabelling.DataCube;
import uk.ac.cam.mp703.RandomDecisionForests.ClassLabel;
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
		
		// Load in the class list and  mapping between class names and colours 
		// (in the example labelling)
		List<ClassLabel> classes = ClassLabel.loadClassList(colourClassMapFilename);
		Map<Integer, ClassLabel> colourClassMap = ClassLabel.computeColourToClassMap(classes);
		
		// Load in the examplePixelLabeling
		File labelingImageFile = new File(exampleLabelingFilename);
		BufferedImage examplePixelLabeling = ImageIO.read(labelingImageFile);
		
		// Load in the datacube from the image specifier
		DataCube datacube = DataCube.generateDataCubeFromImageSpecifier(exampleImageSpecifier);
		
		// Check the dimensions of the labeling are correct
		if (examplePixelLabeling.getHeight() != datacube.getHeight() ||
				examplePixelLabeling.getWidth() != datacube.getWidth()) {
			throw new IllegalArgumentException("The width and height of the example image and "
					+ "example labeling should agree.");
		}
		
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
				ClassLabel classLabel = colourClassMap.get(colour);
				TrainingSample sample = new TrainingSample(classLabel, vec);
				trainingSamples.add(sample);
			}
		}
		
		// Create the training sequence structure and return it
		return new TrainingSequence(trainingSamples, classes);
	}
}
