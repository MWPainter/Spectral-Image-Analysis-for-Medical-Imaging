package uk.ac.cam.mp703.Noise;

import java.util.Random;

import uk.ac.cam.mp703.ImagePixelLabelling.DataCube;


/***
 * We want to be able to take a hyperspectral image (i.e. a data cube) and add a poisson noise to it
 * 
 * @author michaelpainter
 *
 */
public class PoissonNoise {
	
	/***
	 * We apply a Poisson noise to a hyperspectral image. Poisson noise simulates noise seen by 
	 * modelling interarrival times of photons as following an exponential distribution, as in a 
	 * Poisson process.
	 * 
	 * If we have image I, then the new image I' where I'[i][j][k] follows a Poisson random variable 
	 * with mean I[i][j][k]. 
	 * 
	 * For space concerns we apply the noise directly to the image values.
	 * 
	 * @param img
	 */
	public static void applyPoissonNoise(DataCube img) {
		// Get the cube and make a random number source
		short[][][] cube = img.getDataCube();
		Random rand = new Random();
		
		// Iterate through each pixel and bin and apply a poisson noise
		// Use saturate arithmetic so that 
		for (int i = 0; i < cube.length; i++) {
			for (int j = 0; j < cube[i].length; j++) {
				for (int k = 0; k < cube[i][j].length; k++) {
					int poisson = generatePoisson(rand, cube[i][j][k]);
					cube[i][j][k] = (short) saturate(poisson, 0, 255);
				}
			}
		}
	}
	
	/***
	 * Convenience method to perform saturate arithmetic 
	 * 
	 * @param value Value we're considering
	 * @param min Minimum value we allow
	 * @param max Maximum value we allow
	 * @return
	 */
	private static int saturate(int value, int min, int max) {
		if (value < min) {
			return min;
		} 
		return (value > max) ? max : value;
	}
	
	/***
	 * Function to generate a Poisson random variable with mean lambda 
	 * 
	 * If we consider a Poisson process with rate lambda then the number of events N(1) in [0,1] 
	 * follows a Poisson distribution with mean lambda. The interarrival times of events follows an 
	 * exponential distributions with mean 1/lambda. 
	 * 
	 * Hence we pick the maximum from {k | sum_i [-log(U_i)/lambda] > 1}
	 * Which can be shown to be equal to [min{k | U_1 * ... * U_k < e^-lambda} - 1]  
	 * 
	 * We can show easily that 
	 * 
	 * @param rand Random number generator
	 * @param lambda The mean value
	 * @return A randomly generated 
	 */
	private static int generatePoisson(Random rand, int lambda) {
		double threshold = Math.exp(-lambda);
		int k = 0;
		double product = 1.0;
		while (product > threshold) {
			k++;
			product *= rand.nextDouble();
		}
		return k-1;
	}
	
}
