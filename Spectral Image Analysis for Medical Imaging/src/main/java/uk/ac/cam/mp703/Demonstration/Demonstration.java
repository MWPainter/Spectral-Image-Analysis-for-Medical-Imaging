package uk.ac.cam.mp703.Demonstration;

import java.io.IOException;

import uk.ac.cam.mp703.ImagePixelLabeling.DataCubeTest;
import uk.ac.cam.mp703.RandomDecisionForests.MalformedForestException;
import uk.ac.cam.mp703.RandomDecisionForests.MalformedProbabilityDistributionException;

public class Demonstration {
	
	public static void main() throws IOException, MalformedForestException, MalformedProbabilityDistributionException {
		// Generate a 5D spectral image
		DataCubeTest testRunner = new DataCubeTest();
		testRunner.generate5DimImage();
		testRunner.classify5DimImageRF();
		testRunner.classify5DimImageNN();
	}
	
}
