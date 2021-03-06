package com.htmlhifive.testexplorer.cache;

import org.junit.Test;

public class ProcessedImageUtilityTest {
	@Test
	public void testGetAlgorithmName()
	{
		new ProcessedImageUtility();
		org.junit.Assert.assertEquals("edge",
				ProcessedImageUtility.getAlgorithmNameForEdge(-1));
		org.junit.Assert.assertEquals("edge",
				ProcessedImageUtility.getAlgorithmNameForEdge(3));
		org.junit.Assert.assertEquals("edge_0",
				ProcessedImageUtility.getAlgorithmNameForEdge(0));
		org.junit.Assert.assertEquals("edge_1",
				ProcessedImageUtility.getAlgorithmNameForEdge(1));
	}
}
