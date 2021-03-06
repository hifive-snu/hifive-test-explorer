package com.htmlhifive.testexplorer.entity;

/**
 * immutable structure of all repositories
 */
public class Repositories {
	private final ConfigRepository configRepository;
	private final ProcessedImageRepository processedImageRepository;
	private final ScreenshotRepository screenshotRepository;
	private final TestExecutionRepository testExecutionRepository;

	/**
	 * Constructor. Must provide all repositories to create.
	 *
	 * @param configRepository configRepository 
	 * @param processedImageRepository processedImageRepository
	 * @param screenshotRepository screenshotRepository
	 * @param testExecutionRepository testExecutionRepository
	 */
	public Repositories(ConfigRepository configRepository,
		ProcessedImageRepository processedImageRepository,
		ScreenshotRepository screenshotRepository,
		TestExecutionRepository testExecutionRepository)
	{
		this.configRepository = configRepository;
		this.processedImageRepository = processedImageRepository;
		this.screenshotRepository = screenshotRepository;
		this.testExecutionRepository = testExecutionRepository;
	}

	/**
	 * @return the configRepository
	 */
	public ConfigRepository getConfigRepository() {
		return configRepository;
	}

	/**
	 * @return the processedImageRepository
	 */
	public ProcessedImageRepository getProcessedImageRepository() {
		return processedImageRepository;
	}

	/**
	 * @return the screenshotRepository
	 */
	public ScreenshotRepository getScreenshotRepository() {
		return screenshotRepository;
	}

	/**
	 * @return the testExecutionRepository
	 */
	public TestExecutionRepository getTestExecutionRepository() {
		return testExecutionRepository;
	}
}
