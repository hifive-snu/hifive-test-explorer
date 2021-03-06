/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.api;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.htmlhifive.testexplorer.cache.BackgroundImageDispatcher;
import com.htmlhifive.testexplorer.cache.CacheTaskQueue;
import com.htmlhifive.testexplorer.cache.ProcessedImageUtility;
import com.htmlhifive.testexplorer.entity.ConfigRepository;
import com.htmlhifive.testexplorer.entity.ProcessedImageRepository;
import com.htmlhifive.testexplorer.entity.Repositories;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.entity.TestExecutionRepository;
import com.htmlhifive.testexplorer.file.ImageFileUtility;
import com.htmlhifive.testexplorer.image.EdgeDetector;
import com.htmlhifive.testlib.image.utlity.ImageUtility;

@Controller
@RequestMapping("/image")
public class ImageController {

	@Autowired
	private ConfigRepository configRepo;
	@Autowired
	private ScreenshotRepository screenshotRepo;
	@Autowired
	private TestExecutionRepository testExecutionRepo;
	@Autowired
	private ProcessedImageRepository processedImageRepo;

	@Autowired
	private HttpServletRequest request;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ImageController.class);
	
	protected ImageFileUtility imageFileUtil;
	private CacheTaskQueue cacheTaskQueue;
	private BackgroundImageDispatcher backgroundImageDispatcher;

	/**
	 * This method is called by spring after auto wiring.
	 *
	 * Do initialization here.
	 */
	@PostConstruct
	public void init()
	{
		Repositories repositories = new Repositories(configRepo, processedImageRepo, screenshotRepo, testExecutionRepo);
		this.imageFileUtil = new ImageFileUtility(repositories);

		this.cacheTaskQueue = new CacheTaskQueue();
		this.backgroundImageDispatcher = new BackgroundImageDispatcher(repositories, cacheTaskQueue);
		/* start background worker */
		this.backgroundImageDispatcher.start();
	}

	/**
	 * This method is called when the application is about to die.
	 * 
	 * Cleanup things.
	 * 
	 * @throws InterruptedException
	 */
	@PreDestroy
	public void destory() throws InterruptedException
	{
		this.backgroundImageDispatcher.requestStop();
		this.cacheTaskQueue.interruptAndJoin();
		this.backgroundImageDispatcher.join();
	}

	/**
	 * Get the image from id.
	 *
	 * @param id screenshot id
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public void getImage(@RequestParam Integer id, HttpServletResponse response) {
		Screenshot screenshot = screenshotRepo.findOne(id);

		if (screenshot == null)
		{
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return;
		}

		// Send PNG image
		try {
			File file = imageFileUtil.getFile(screenshot);
			sendFile(file, response);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	/**
	 * Get edge detection result of an image.
	 *
	 * @param id id of screenshot to be processed by edge detector.
	 * @param allparams all parameters received by API
	 * @param response HttpServletResponse
	 */
	public void getEdgeImage(Integer id,
							Map<String, String> allparams, HttpServletResponse response)
	{
		Screenshot screenshot = screenshotRepo.findOne(id);

		if (screenshot == null)
		{
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return;
		}

		try {
			EdgeDetector edgeDetector = new EdgeDetector(0.5);
			
			int colorIndex = -1;
			if (allparams.containsKey("colorIndex")) {
				try {
					colorIndex = Integer.parseInt(allparams.get("colorIndex"));
				} catch (NumberFormatException nfe) { }
			}

			File cachedFile = imageFileUtil.searchProcessedImageFile(id, ProcessedImageUtility.getAlgorithmNameForEdge(colorIndex));
			if (cachedFile != null)
			{
				sendFile(cachedFile, response);
				return;
			}

			switch (colorIndex) {
			case 0:
				edgeDetector.setForegroundColor(new Color(255, 0, 0, 255));
				break;
			case 1:
				edgeDetector.setForegroundColor(new Color(0, 0, 255, 255));
				break;
			}

			BufferedImage image = ImageIO.read(imageFileUtil.getFile(screenshot));
			BufferedImage edgeImage = edgeDetector.DetectEdge(image);
			sendImage(edgeImage, response);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	/**
	 * Get processed image.
	 * 
	 * @param id id of an image to be processed  
	 * @param algorithm currently only "edge" is supported
	 * @param allparams received all parameters
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/getProcessed", method = RequestMethod.GET)
	public void getProcessed(@RequestParam Integer id,
			@RequestParam String algorithm,
			@RequestParam Map<String, String> allparams, HttpServletResponse response)
	{
		switch(algorithm)
		{
		case "edge":
			getEdgeImage(id, allparams, response);
			break;
		default:
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			break;
		}
	}

	/**
	 * Get the diff image with a marker of comparison result. If there is no difference, return normal image.
	 *
	 * @param sourceId comparison source image id
	 * @param targetId comparison target image id
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/getDiff", method = RequestMethod.GET)
	public void getDiffImage(@RequestParam Integer sourceId, @RequestParam Integer targetId, HttpServletResponse response) {
		Screenshot sourceScreenshot = screenshotRepo.findOne(sourceId);
		Screenshot targetScreenshot = screenshotRepo.findOne(targetId);

		if (sourceScreenshot == null || targetScreenshot == null)
		{
			response.setStatus(HttpStatus.NOT_FOUND.value());
			return;
		}

		try {
			File source = imageFileUtil.getFile(sourceScreenshot);
			File target = imageFileUtil.getFile(targetScreenshot);

			// Create a partial image
			BufferedImage actual = ImageIO.read(source);
			BufferedImage expected = ImageIO.read(target);

			// Compare.
			List<Point> diffPoints = ImageUtility.compareImages(expected, null, actual, null, null, null);
			if (diffPoints.isEmpty()) {
				sendFile(source, response);
			} else {
				BufferedImage marked = ImageUtility.getMarkedImage(actual, diffPoints);
				sendImage(marked, response);
			}
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}


	/**
	 * Send a file over http response
	 * 
	 * @param file file to send
	 * @param response response to use
	 * @throws IOException
	 */
	private void sendFile(File file, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");
		response.flushBuffer();
		IOUtils.copy(new FileInputStream(file), response.getOutputStream());
	}

	/**
	 * Send image over response
	 * 
	 * @param image image to send
	 * @param response response to use
	 * @throws IOException
	 */
	private void sendImage(BufferedImage image, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");
		response.flushBuffer();
		ImageIO.write(image, "png", response.getOutputStream());
	}
}
