package uk.ac.cam.mp703.Noise;

import java.awt.image.BufferedImage;
import java.io.File;

import boofcv.abst.denoise.FactoryImageDenoise;
import boofcv.abst.denoise.WaveletDenoiseFilter;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;

public class Denoise {
	
	public static void denoiseDataCube(String inputFileSpecifier, String outputFileSpecifier) {
		// Perform checks that the specifiers are correct (check the formats of the strings)
		// Check the format of the string
		if (!inputFileSpecifier.contains("%") || 
				inputFileSpecifier.indexOf("%") != inputFileSpecifier.lastIndexOf("%") ||
				!outputFileSpecifier.contains("%") || 
				outputFileSpecifier.indexOf("%") != outputFileSpecifier.lastIndexOf("%")) {
			throw new IllegalArgumentException("Filename specifiers must contain exactly one '%'.");
		}
		
		// Iterate through all of the files that exist, and denoise them
		int imgNo = 1;
		String inputFilename = inputFileSpecifier.replaceAll("%", Integer.toString(imgNo));
		String outputFilename = outputFileSpecifier.replaceAll("%", Integer.toString(imgNo));
		File inputFile = new File(inputFilename);
		while (inputFile.exists()) {
			denoise(inputFilename, outputFilename);
			imgNo++;
			inputFilename = inputFileSpecifier.replaceAll("%", Integer.toString(imgNo));
			outputFilename = outputFileSpecifier.replaceAll("%", Integer.toString(imgNo));
			inputFile = new File(inputFilename);
		}
	}
 
	private static void denoise(String inputFilename, String outputFilename) {
 
		// load the input image, declare data structures, create a noisy image
		GrayF32 input = UtilImageIO.loadImage(inputFilename, GrayF32.class);
		
		// The image to output
		GrayF32 denoised = input.createSameShape();
 
		// How many levels in wavelet transform
		int numLevels = 3;
		// Create the noise removal algorithm
		WaveletDenoiseFilter<GrayF32> denoiser =
				FactoryImageDenoise.waveletVisu(GrayF32.class,numLevels,0,255);
 
		// remove noise from the image
		denoiser.process(input,denoised);
		
		// Convert it to an image we can output
		BufferedImage img = ConvertBufferedImage.convertTo(denoised,null);
		
		// Save the image to the file specified
		UtilImageIO.saveImage(img, outputFilename);
	}
}
