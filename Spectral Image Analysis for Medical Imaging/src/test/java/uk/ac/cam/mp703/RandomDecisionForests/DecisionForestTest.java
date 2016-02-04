package uk.ac.cam.mp703.RandomDecisionForests;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import uk.ac.cam.mp703.RandomDecisionForests.DecisionForest.TreeNode;
import uk.ac.cam.mp703.RandomDecisionForests.OneDimensionalLinearWeakLearner.OneDimensionalLinearSplitParameters;

public class DecisionForestTest {
	
	/***
	 * A decision forest that will be used throughout the tests
	 */
	DecisionForest df;
	
	/***
	 * A reference to df's trees, to be used for testing compactness and traversal
	 */
	TreeNode tree1, tree2, tree3;
	
	/***
	 * Manually create a forest of three trees.
	 * 3 classes: a,b,c covering 1D intervals of [-2,-1], (-1,1), [1,2]
	 * 
	 * Notation for trees, with one dimensional data: "(x)" is a cuttoff value of x for a 1D split,
	 * "n" is a class number n associated with each leaf node.  
	 * 
	 * Tree1: 
	 * 	        |
	 *     ___(-1.1)___
	 *     |          |
	 *  _(-1.5)_   _(1.2)_
	 *  |      |   |     |
	 *  0      0   1     2
	 *  
	 * Tree2:
	 * 	        |
	 *     ___(0.1)___
	 *     |          |
	 *  _(-1.6)_   _(1.3)_
	 *  |      |   |     |
	 *  0      1   1     2
	 *  
	 * Tree3:
	 * 			|
	 * 			1
	 * 
	 * (single leaf node, always votes the same)
	 *  
	 */
	@Before
	public void generateTestForest() {
		// Create the desicion forest to be used with some of the basic data
		df = new DecisionForest();
		df.dataDimension = 1;
		df.classStrings = new ArrayList<String>();
		df.classStrings.add("a");
		df.classStrings.add("b");
		df.classStrings.add("c");
		df.weakLearnerType = WeakLearnerType.ONE_DIMENSIONAL_LINEAR;
		
		// Create tree 1
		TreeNode t1n3 = new TreeNode();
		t1n3.classNumber = 0;
		TreeNode t1n4 = new TreeNode();
		t1n4.classNumber = 0;
		TreeNode t1n5 = new TreeNode();
		t1n5.classNumber = 1;
		TreeNode t1n6 = new TreeNode();
		t1n6.classNumber = 2;

		TreeNode t1n1 = new TreeNode();
		t1n1.splitParams = new OneDimensionalLinearSplitParameters(0, -1.5);
		t1n1.leftChild = t1n3;
		t1n1.rightChild = t1n4;
		TreeNode t1n2 = new TreeNode();
		t1n2.splitParams = new OneDimensionalLinearSplitParameters(0, 1.2);
		t1n2.leftChild = t1n5;
		t1n2.rightChild = t1n6;
		
		tree1 = new TreeNode();
		tree1.splitParams = new OneDimensionalLinearSplitParameters(0, -1.1);
		tree1.leftChild = t1n1;
		tree1.rightChild = t1n2;
		
		
		// Create tree 2
		TreeNode t2n3 = new TreeNode();
		t2n3.classNumber = 0;
		TreeNode t2n4 = new TreeNode();
		t2n4.classNumber = 1;
		TreeNode t2n5 = new TreeNode();
		t2n5.classNumber = 1;
		TreeNode t2n6 = new TreeNode();
		t2n6.classNumber = 2;

		TreeNode t2n1 = new TreeNode();
		t2n1.splitParams = new OneDimensionalLinearSplitParameters(0, -1.6);
		t2n1.leftChild = t2n3;
		t2n1.rightChild = t2n4;
		TreeNode t2n2 = new TreeNode();
		t2n2.splitParams = new OneDimensionalLinearSplitParameters(0, 1.3);
		t2n2.leftChild = t2n5;
		t2n2.rightChild = t2n6;
		
		tree2 = new TreeNode();
		tree2.splitParams = new OneDimensionalLinearSplitParameters(0, 0.1);
		tree2.leftChild = t2n1;
		tree2.rightChild = t2n2;
		
		
		// Create tree 3
		tree3 = new TreeNode();
		tree3.classNumber = 1;
		
		
		// Add the trees to the forest
		df.rootNodes = new HashSet<TreeNode>();
		df.rootNodes.add(tree1);
		df.rootNodes.add(tree2);
		df.rootNodes.add(tree3);
	}
	
	/***
	 * Test the compactness function on tree1 of the forest constructed
	 * Before:
	 * 	        |
	 *     ___(-1.1)___
	 *     |          |
	 *  _(-1.5)_   _(1.2)_
	 *  |      |   |     |
	 *  0      0   1     2
	 *  
	 * After:
	 * 	        |
	 *     ___(-1.1)___
	 *     |          |
	 *     0       _(1.2)_
	 *             |     |
	 *             1     2
	 */
	@Test
	public void testTreeNodeCompaction() {
		tree1.compact();
		assertThat(tree1.leftChild.classNumber, not(equalTo(-1))); 
	}
	
	/***
	 * Test different values on the traversal of tree's 2 and 3
	 * 
	 * Tree2:
	 * 	        |
	 *     ___(0.1)___
	 *     |          |
	 *  _(-1.6)_   _(1.3)_
	 *  |      |   |     |
	 *  0      1   1     2
	 *  
	 * Tree3:
	 * 			|
	 * 			1
	 * @throws MalformedForestException 
	 */
	@Test 
	public void testTreeTraversal() throws MalformedForestException {
		// Make an instance of our weak learner
		OneDimensionalLinearWeakLearner wl = new OneDimensionalLinearWeakLearner();
		
		// Make 4 test vectors
		List<Double> vlist = new ArrayList<Double>();
		vlist.add(-1.7);
		NDRealVector v1 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(-1.5);
		NDRealVector v2 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(1.1);
		NDRealVector v3 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(1.6);
		NDRealVector v4 = new NDRealVector(vlist);
		
		// Check the traversals on each of the vectors on trees 2, 3
		assertThat(df.traverseTree(tree2, wl, v1), equalTo(0));
		assertThat(df.traverseTree(tree2, wl, v2), equalTo(1));
		assertThat(df.traverseTree(tree2, wl, v3), equalTo(1));
		assertThat(df.traverseTree(tree2, wl, v4), equalTo(2));

		assertThat(df.traverseTree(tree3, wl, v1), equalTo(1));
		assertThat(df.traverseTree(tree3, wl, v2), equalTo(1));
		assertThat(df.traverseTree(tree3, wl, v3), equalTo(1));
		assertThat(df.traverseTree(tree3, wl, v4), equalTo(1));
	}
	
	/***
	 * Test different values for their classifications with the whole forest
	 * 
	 * -1.7 (001), -1.55 (011), -1.2 (011), 0.0 (111), 0.5 (111), 1.25 (211), 1.35 (221) 
	 * 
	 * Above is the different values and corresponding expected votes from trees 1,2,3.
	 * @throws MalformedForestException 
	 */
	@Test
	public void testClassification() throws MalformedForestException {		
		// Make test vectors
		List<Double> vlist = new ArrayList<Double>();
		vlist.add(-1.7);
		NDRealVector v1 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(-1.55);
		NDRealVector v2 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(-1.2);
		NDRealVector v3 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(0.0);
		NDRealVector v4 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(0.5);
		NDRealVector v5 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(1.25);
		NDRealVector v6 = new NDRealVector(vlist);
		vlist = new ArrayList<Double>();
		vlist.add(1.35);
		NDRealVector v7 = new NDRealVector(vlist);
		
		// Test the classifications
		assertThat(df.classify(v1), equalTo(0));
		assertThat(df.classify(v2), equalTo(1));
		assertThat(df.classify(v3), equalTo(1));
		assertThat(df.classify(v4), equalTo(1));
		assertThat(df.classify(v5), equalTo(1));
		assertThat(df.classify(v6), equalTo(1));
		assertThat(df.classify(v7), equalTo(2));
	}
	
}
