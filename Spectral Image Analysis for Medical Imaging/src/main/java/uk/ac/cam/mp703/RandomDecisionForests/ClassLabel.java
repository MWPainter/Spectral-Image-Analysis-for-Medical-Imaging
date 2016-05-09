package uk.ac.cam.mp703.RandomDecisionForests;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class ClassLabel implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	/**
	 * An (unique) id to identify this class label. THis should be the class' index in any lists
	 */
	int classId;
	
	
	/**
	 * An integer representing the class' unique colour 
	 */
	int colour;
	
	
	/**
	 * The name of the class
	 */
	String name;
	
	
	/**
	 * @return the classId
	 */
	public int getClassId() {
		return classId;
	}


	/**
	 * @return the colour
	 */
	public int getColour() {
		return colour;
	}


	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}


	/***
	 * Constructor, all information should be present at creation of a ClassLabel instance
	 * 
	 * @param id The id of the class. If we have a list the id should be its index
	 * @param name The name of the class
	 * @param colour The colour associated (in ground truth images and labellings) with this class
	 */
	public ClassLabel(int id, String name, int colour) {
		this.classId = id;
		this.colour = colour;
		this.name = name;
	}
	
	/***
	 * Load in the list of classes
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
	public static List<ClassLabel> loadClassList(String colourClassMapFilename) 
			throws FileFormatException, FileNotFoundException {
		
		// Make a scanner to scan through the file, separated at commas (ignoring whitespace)
		Scanner scanner = new Scanner(new File(colourClassMapFilename));
		scanner.useDelimiter("\\s*,\\s*");

		// Create the list of classes
		List<ClassLabel> classes = new ArrayList<ClassLabel>();
		
		// Iterate through the class names and colours and add them to the map
		int classColour;
		int classId = 0;
		String className;
		Set<String> uniqueClassNames = new HashSet<String>();
		Set<Integer> uniqueClassColours = new HashSet<Integer>();
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
				
				// Check that this isn't a duplication of a class name or a class number and its a 
				// valid class name
				if (uniqueClassNames.contains(className)) {
					scanner.close();
					throw new TrainingSequenceFormatException("Duplicate class name in the training "
							+ "sequence file.");
					
				} else if (uniqueClassColours.contains(classColour)) {
					scanner.close();
					throw new TrainingSequenceFormatException("Duplicate class colour in the training "
							+ "sequence file.");
					
				} else if (!className.matches("[a-zA-z].*")) {
					scanner.close();
					throw new TrainingSequenceFormatException("Class names must begin with alphabetic "
							+ "characters");
					
				}
				
				// Add the class to the list!
				classes.add(new ClassLabel(classId++, className, classColour));
				
				// Add the class name and colour to the hash sets for checking duplicates
				uniqueClassNames.add(className);
				uniqueClassColours.add(classColour);
				
			// Catch a number format exception, that means that there was a format error
			} catch (NumberFormatException e) {
				scanner.close();
				throw new FileFormatException("Each class needs to have an associated colour, "
						+ "specified by a 6 digit hex number.");
			}
		}
		
		// Close the scanner
		scanner.close();
		
		// If there are no classes provided then the file is incorrect in format
		if (classes.isEmpty()) {
			scanner.close();
			throw new TrainingSequenceFormatException("No class names were provided in the file.");
		}
		
		// Return the class map
		return classes;
	}
	
	/***
	 * Its useful for us to have a mapping from colours to classes, so make this mapping from the 
	 * class list by iterating though the classes
	 * 
	 * Just iterate through each of the classes, adding it to the map and check for repeats
	 * 
	 * @param classes The list of class labels
	 * @throws FileFormatException 
	 */
	public static Map<Integer, ClassLabel> computeColourToClassMap(List<ClassLabel> classes) throws FileFormatException {
		// Create the map
		Map<Integer, ClassLabel> colourClassMap = new HashMap<>();
		
		// Iterate through the classes 
		for (ClassLabel classLabel : classes) {
			
			// Check that this isn't a repeat colour or class
			if (colourClassMap.containsKey(classLabel.getColour()) || 
					colourClassMap.containsValue(classLabel)) {
				throw new FileFormatException("Colours and class names must each be unique");
			}
			
			// Add the entry to the map
			colourClassMap.put(classLabel.getColour(), classLabel);
		}
		
		// Return the map
		return colourClassMap;
	}
	
	/***
	 * Its useful for us to have a mapping from names to classes, so make this mapping from the 
	 * class list by iterating though the classes
	 * 
	 * Just iterate through each of the classes, adding it to the map and check for repeats
	 * 
	 * @param classes The list of class labels
	 * @throws FileFormatException 
	 */
	public static Map<String, ClassLabel> computeNameToClassMap(List<ClassLabel> classes) throws FileFormatException {
		// Create the map
		Map<String, ClassLabel> nameClassMap = new HashMap<>();
		
		// Iterate through the classes 
		for (ClassLabel classLabel : classes) {
			
			// Check that this isn't a repeat colour or class name
			if (nameClassMap.containsKey(classLabel.getName()) || 
					nameClassMap.containsValue(classLabel)) {
				throw new FileFormatException("Colours and class names must each be unique");
			}
			
			// Add the entry to the map
			nameClassMap.put(classLabel.getName(), classLabel);
		}
		
		// Return the map
		return nameClassMap;
	}
	
	// We override equals and hash as we are commonly used in maps (in probability distributions)
	@Override
	public boolean equals(Object obj) {
		ClassLabel otherClass = (ClassLabel) obj;
		return (this.classId == otherClass.classId && 
				this.colour == otherClass.colour &&
				this.name == otherClass.name);
	}

	// We override equals and hash as we are commonly used in maps (in probability distributions)
	@Override
	public int hashCode() {
		// The id and colour should both be unique, and suitable for a hash
		return classId;
	}
}
