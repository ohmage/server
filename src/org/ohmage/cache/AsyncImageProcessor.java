package org.ohmage.cache;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.log4j.Logger;
import org.ohmage.domain.Image;
import org.ohmage.domain.Image.Size;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ServiceException;
import org.ohmage.service.ImageServices;
import org.springframework.beans.factory.DisposableBean;

/**
 * <p>
 * A background process for retrieving images that have not been processed and
 * processing them.
 * </p> 
 *
 * @author John Jenkins
 */
public class AsyncImageProcessor
	extends TimerTask
	implements DisposableBean {
	
	/**
	 * <p>
	 * A task for processing the images that always runs as long as this thread
	 * is running.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class ImageProcessor extends Thread {
		/**
		 * Whether or not this process should continue running.
		 */
		private boolean running = true;
		
		/**
		 * The number of milliseconds between each sweep of the images.
		 */
		private static final long MILLISECONDS_TO_SLEEP = 1000 * 60 * 5;
		
		/**
		 * The queue of images to be processed.
		 */
		private final ConcurrentLinkedDeque<Image> imageQueue =
			new ConcurrentLinkedDeque<Image>();
		
		/**
		 * Adds an individual Image to be processed and moves it to the front
		 * of the list.
		 * 
		 * @param image The Image to add to the list.
		 */
		public void queueImage(final Image image) {
			imageQueue.addFirst(image);
		}
		
		/**
		 * Adds a set of image Images whose data should be processed and
		 * updated.
		 * 
		 * @param images The Images to add to the list.
		 */
		protected synchronized void queueImages(
			final Collection<Image> images) {
			
			// If the images are null, then we ignore it the same as if the
			// list was empty.
			if(images == null) {
				return;
			}
			
			// Remove duplicates.
			images.removeAll(imageQueue);
			
			// Add the ones that were not already part of the list.
			imageQueue.addAll(images);
		}

		/**
		 * Shuts the process down by setting
		 */
		public void shutdown() {
			running = false;
			interrupt();
		}
		
		/*
		 * (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			while(running) {
				// Attempt to retrieve the next image in the queue.
				Image nextUrl = imageQueue.poll();
				
				// If we don't have any images to process, put ourselves to
				// sleep for a while. This can be set relatively high because
				// if new images are found we will be interrupted. It shouldn't
				// be set too high in case the controller is shutdown, in which
				// case we will sleep for too long never being interrupted.
				if(nextUrl == null) {
					try {
						sleep(MILLISECONDS_TO_SLEEP);
					}
					catch(InterruptedException e) {
						// Someone has woken us up, so we need to check the
						// list.
					}
				}
				// If an image Image was returned, process it.
				else {
					processImage(nextUrl);
				}
			}
		}
		
		/**
		 * Reads the original data, creates the sub-images and saves them.
		 * 
		 * @param image
		 *        The image that should be validated and have its variants
		 *        saved and processed.
		 */
		private void processImage(final Image image) {
			// Validate that the image data is valid.
			try {
				image.validate();
			}
			catch(DomainException e) {
				LOGGER
					.error(
						"The image data is invalid: " + 
							image.getId().toString(),
						e);
				return;
			}
			
			// Create the sub-images.
			try {
				for(Size size : Image.getSizes()) {
					// If the size of the image does not exist, create it.
					if(! image.sizeExists(size)) {
						image.saveImage(size);
					}
				}
			}
			catch(DomainException e) {
				LOGGER
					.error(
						"One of the sizes of the image could not be " +
							"created: " + 
							image.getId().toString(),
						e);
				return;
			}
			
			// Mark the image as processed.
			try {
				ImageServices.instance().markImageAsProcessed(image.getId());
			}
			catch(ServiceException e) {
				LOGGER
					.error(
						"The image could not be marked as processed: " + 
							image.getId().toString(),
						e);
				return;
			}
		}
	}
	/**
	 * The instance of the image processor.
	 */
	private final ImageProcessor processor = new ImageProcessor();
	
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER = 
		Logger.getLogger(AsyncImageProcessor.class);
	
	/**
	 * The cleanup task that is periodically run to clean up expired 
	 * registration requests.
	 */
	private static final Timer PROCESSOR = new Timer("Image Processor", true);
	
	/**
	 * The number of milliseconds between each sweep of the images.
	 */
	private static final long MILLISECONDS_BETWEEN_CHECKING = 1000 * 30;
	
	/**
	 * Default constructor that will be called by Spring via reflection.
	 */
	private AsyncImageProcessor() {
		LOGGER.info("Creating the image processing task.");
		
		// Create the task that will be run periodically.
		PROCESSOR.schedule(
			this, 
			MILLISECONDS_BETWEEN_CHECKING, 
			MILLISECONDS_BETWEEN_CHECKING);
		
		// Start the image processing sub-process.
		processor.start();
	}
	
	/**
	 * Retrieves the images that need to be processed, and adds them to its
	 * queue.
	 */
	@Override
	public void run() {
		LOGGER.info("Queueing unprocessed images.");
		try {
			processor
				.queueImages(
					ImageServices.instance().getUnprocessedImages());
			processor.interrupt();
		}
		catch(ServiceException e) {
			LOGGER.error("Failed to retrieve the unprocessed images.", e);
		}
	}
	
	/**
	 * Stops the cleanup task.
	 */
	@Override
	public void destroy() throws Exception {
		processor.shutdown();
		PROCESSOR.cancel();
	}
}