package com.htmlhifive.testexplorer.entity;

import org.junit.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/spring/test-context.xml")
public class RepositoriesTest {
	@Autowired
	private TestExecutionRepository testExecutionRepo;

	@Autowired
	private ScreenshotRepository screenshotRepo;
	
	@Autowired
	private ConfigRepository configRepo;

	@Autowired
	private ProcessedImageRepository processedImageRepo;
	
	@Test
	public void TestGetters()
	{
		Repositories repositories = new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo);
		Assert.assertEquals(configRepo, repositories.getConfigRepository());
		Assert.assertEquals(processedImageRepo, repositories.getProcessedImageRepository());
		Assert.assertEquals(screenshotRepo, repositories.getScreenshotRepository());
		Assert.assertEquals(testExecutionRepo, repositories.getTestExecutionRepository());
	}
}
