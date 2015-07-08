/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.domain;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;

/**
 * A representation of an image.
 * 
 * @author John Jenkins
 */
public class Image implements IMedia{
	
	private static final Logger LOGGER = 
			Logger.getLogger(Image.class);
	
	/**
	 * These are the different possible values for an image's size. It also
	 * defines the functionality for each size including how to store and read
	 * its data.
	 * 
	 * @author John Jenkins
	 */
	public abstract static class Size {
		/**
		 * The file format to use to save images.
		 */
		public static final String IMAGE_STORE_FORMAT = "jpg";
		/**
		 * The dimension to use when scaling images.
		 */
		public static final double IMAGE_SCALED_MAX_DIMENSION = 128.0;
		
		/**
		 * The name of the image without an extension.
		 */
		private final String name;
		/**
		 * The extension for the variant of the image.
		 */
		private final String extension;
		
		/**
		 * Creates a new Size object with the given name and extension.
		 * 
		 * @param name The user-friendly name of this size.
		 * 
		 * @param extension The extension to use place on the end of each
		 *					image.
		 */
		protected Size(final String name, final String extension) {
			if(name == null) {
				throw new IllegalArgumentException("The name is null.");
			}
			if(extension == null) {
				throw new IllegalArgumentException("The extension is null.");
			}
			
			this.name = name;
			this.extension = extension;
		}
		
		/**
		 * Returns the user-friendly name of this image size.
		 * 
		 * @return The user-friendly name of this image size.
		 */
		public String getName() {
			return name;
		}
		
		/**
		 * Returns the file extension appended to the filename when writing a
		 * file of this size.
		 * 
		 * @return The file extension appended to the filename when writing a
		 * 		   file of this size.
		 */
		public String getExtension() {
			return extension;
		}
		
		/**
		 * Returns the URL for this size image.
		 * 
		 * @param originalUrl The URL for the original image.
		 * 
		 * @return The URL for the image of this size.
		 * 
		 * @throws DomainException The new URL was malformed.
		 */
		public static URL getUrl(
			final Size size,
			final URL originalUrl)
			throws DomainException {
			
			try {
				return new URL(originalUrl.toString() + size.getExtension());
			}
			catch(MalformedURLException e) {
				throw new DomainException("The URL is malformed.", e);
			}
		}

		/**
		 * Outputs the name of this size.
		 */
		@Override
		public String toString() {
			return name;
		}
		
		/**
		 * Performs a transformation on image data by converting it to this
		 * type of image.
		 * 
		 * @param original The original image data to be transformed.
		 * 
		 * @return The image data transformed to be this type of image.
		 * 
		 * @throws DomainException There was an error reading the image or
		 * 						   transforming it.
		 */
		public abstract ImageData transform(
			final ImageData original)
			throws DomainException;
	};
	
	/**
	 * An original image's contents, including metadata.
	 *
	 * @author John Jenkins
	 */
	public static class Original extends Size {
		public static final String NAME = "original";
		public static final String EXTENSION = "";
		
		private static final Original SELF = new Original();
		
		/**
		 * Constructor for the single, static instance of this class.
		 */
		protected Original() {
			super(NAME, EXTENSION);
		}
		
		/**
		 * Returns the single, static instance of this class.
		 * 
		 * @return The single, static instance of this class.
		 */
		public static Original getInstance() {
			return SELF;
		}

		/** 
		 * Performs no actual transformation of the data.
		 */
		@Override
		public ImageData transform(
			final ImageData original) 
			throws DomainException {
			
			return original;
		}
	}
	
	/**
	 * A thumbnail version of the image scaled down to have its maximum
	 * dimension not exceed {@link Size#IMAGE_SCALED_MAX_DIMENSION}.
	 *
	 * @author John Jenkins
	 */
	public static class Small extends Size {
		public static final String NAME = "small";
		public static final String EXTENSION = "-s";
		
		private static final Small SELF = new Small();
		
		/**
		 * Constructor for the single, static instance of this class.
		 */
		protected Small() {
			super(NAME, EXTENSION);
		}
		
		/**
		 * Returns the single, static instance of this class.
		 * 
		 * @return The single, static instance of this class.
		 */
		public static Small getInstance() {
			return SELF;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.domain.Image.Size#transform(org.ohmage.domain.Image.ImageData)
		 */
		@Override
		public ImageData transform(
			final ImageData original)
			throws DomainException {
			
			LOGGER.debug("HT: Start the transformation process");
			// Get the BufferedImage from the image data.
			BufferedImage imageContents = original.getBufferedImage();

			LOGGER.debug("HT: Obtaining original data");
			// Get the percentage to scale the image.
			Double scalePercentage;
			if(imageContents.getWidth() > imageContents.getHeight()) {
				scalePercentage =
					IMAGE_SCALED_MAX_DIMENSION / imageContents.getWidth();
			}
			else {
				scalePercentage =
					IMAGE_SCALED_MAX_DIMENSION / imageContents.getHeight();
			}
			LOGGER.debug("HT: original content (width,height) = " + imageContents.getWidth() + ", " + 
					imageContents.getHeight());
			LOGGER.debug("IAMGE_SCALED_MAX_DIMENSION: " + IMAGE_SCALED_MAX_DIMENSION);
			LOGGER.debug("HT: calculating scalePercentage to be " + scalePercentage);
			
			// Calculate the scaled image's width and height.
			int width = 
				(new Double(
					imageContents.getWidth() * scalePercentage)).intValue();
			int height =
				(new Double(
					imageContents.getHeight() * scalePercentage)).intValue();
		
			LOGGER.debug("HT: width = " + width + " , height = " + height);
			
			// Create the new image of the same type as the original and of the
			// scaled dimensions.
			BufferedImage scaledContents =
				new BufferedImage(width, height, imageContents.getType());
			
			LOGGER.debug("HT: Creating scaledContents");
			
			// Paint the original image onto the scaled canvas.
			Graphics2D graphics2d = scaledContents.createGraphics();
			graphics2d
				.setRenderingHint(
					RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2d.drawImage(imageContents, 0, 0, width, height, null);
			
			// Cleanup.
			graphics2d.dispose();
			LOGGER.debug("HT: Finished drawing images");
			
			// Create a buffer stream to read the result of the transformation.
			ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
			
			// Get an image type.
			String imageType = original.getImageType();
			if(imageType == null) {
				imageType = IMAGE_STORE_FORMAT;
			}
			
			LOGGER.debug("HT: transforming to small with type: " + imageType);
			// Write the scaled image to the buffer.
			try {
				ImageIO.write(scaledContents, imageType, bufferStream);
				LOGGER.debug("HT: Writing ImageIO");
			}
			catch(IOException e) {
				LOGGER.debug("HT: ERROR Writing ImageIO");	
				throw new DomainException("Error writing the image.", e);
			}
			
			// Create an input stream to use to create the image data.
			ByteArrayInputStream resultStream =
				new ByteArrayInputStream(bufferStream.toByteArray());
			
			// Create the image data and return it.
			return new ImageData(resultStream);
		}
	}

	/**
	 * A cropped version of the image scaled down to have its dimensions be a
	 * square of length {@link Size#IMAGE_SCALED_MAX_DIMENSION}.
	 *
	 * @author John Jenkins
	 */
	public static class Icon extends Size {
		public static final String NAME = "icon";
		public static final String EXTENSION = "-i";
		
		private static final Icon SELF = new Icon();

		/**
		 * Constructor for the single, static instance of this class.
		 */
		protected Icon() {
			super(NAME, EXTENSION);
		}
		
		/**
		 * Returns the single, static instance of this class.
		 * 
		 * @return The single, static instance of this class.
		 */
		public static Icon getInstance() {
			return SELF;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.ohmage.domain.Image.Size#transform(org.ohmage.domain.Image.ImageData)
		 */
		@Override
		public ImageData transform(
			final ImageData original)
			throws DomainException {
			
			// Get the BufferedImage from the image data.
			BufferedImage imageContents = original.getBufferedImage();
			
			// Get the original image's width and height and the offset from
			// the corner for the smaller image.
			int originalWidth = imageContents.getWidth();
			int originalHeight = imageContents.getHeight();
			int buffer = Math.abs(originalWidth - originalHeight) / 2;
			
			// Create the cropped image from the center image.
			BufferedImage croppedContents;
			if(originalWidth < originalHeight) {
				croppedContents =
					imageContents
						.getSubimage(0, buffer, originalWidth, originalWidth);
			}
			else {
				croppedContents =
					imageContents
						.getSubimage(
							buffer, 
							0, 
							originalHeight, 
							originalHeight);
			}
			
			// Create the new image of the same type as the original and of the
			// scaled dimensions.
			BufferedImage scaledContents =
				new BufferedImage(
					(new Double(IMAGE_SCALED_MAX_DIMENSION)).intValue(),
					(new Double(IMAGE_SCALED_MAX_DIMENSION)).intValue(),
					imageContents.getType());
			
			// Paint the original image onto the scaled canvas.
			Graphics2D graphics2d = scaledContents.createGraphics();
			graphics2d
				.setRenderingHint(
					RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphics2d
				.drawImage(
					croppedContents, 
					0, 
					0, 
					(new Double(IMAGE_SCALED_MAX_DIMENSION)).intValue(),
					(new Double(IMAGE_SCALED_MAX_DIMENSION)).intValue(),
					null);
			
			// Cleanup.
			graphics2d.dispose();
			
			// Create a buffer stream to read the result of the transformation.
			ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
			
			// Get an image type.
			String imageType = original.getImageType();
			if(imageType == null) {
				imageType = IMAGE_STORE_FORMAT;
			}
			
			// Write the scaled image to the buffer.
			try {
				ImageIO.write(scaledContents, imageType, bufferStream);
			}
			catch(IOException e) {
				throw new DomainException("Error writing the image.", e);
			}
			
			// Create an input stream to use to create the image data.
			ByteArrayInputStream resultStream =
				new ByteArrayInputStream(bufferStream.toByteArray());
			
			// Create the image data and return it.
			return new ImageData(resultStream);
		}
	}
	public static final Size ORIGINAL = Original.getInstance();
	public static final Size SMALL = Small.getInstance();
	public static final Size ICON = Icon.getInstance();
	
	/**
	 * A lookup table of user-friendly names for the sizes to their actual Size
	 * object.
	 */
	private static final Map<String, Size> SIZES;
	static {
		Map<String, Size> temp = new HashMap<String, Size>();
		temp.put(ORIGINAL.getName(), ORIGINAL);
		temp.put(SMALL.getName(), SMALL);
		temp.put(ICON.getName(), ICON);
		SIZES = Collections.unmodifiableMap(temp);
	}
	
	/**
	 * The actual image data.
	 *
	 * @author John Jenkins
	 */
	private static class ImageData {
		private InputStream inputStream;
		private final URL url;
		private final String imageType;
		
		// A memoized version of the image that has already been validated.
		private BufferedImage bufferedImage = null;
		
		/**
		 * Stores the InputStream used to reference the image data.
		 * 
		 * @param inputStream An InputStream that points to the image data.
		 * 
		 * @throws DomainException The InputStream was null.
		 */
		public ImageData(
			final InputStream inputStream)
			throws DomainException {
			
			if(inputStream == null) {
				throw new DomainException("The InputStream is null.");
			}
			
			this.inputStream = inputStream;
			this.url = null;
			imageType = null;
		}
		
		/**
		 * Stores the URL used to reference the image data.
		 * 
		 * @param url A URL that references the image data.
		 * 
		 * @throws DomainException The URL was null.
		 */
		public ImageData(final URL url) throws DomainException {
			if(url == null) {
				throw new DomainException("The URL is null.");
			}
			
			this.inputStream = null;
			this.url = url;
			
			try {
				imageType =
					url.openConnection().getContentType().split("/")[1];
			}
			catch(IOException e) {
				throw new DomainException("Could not connect to the file.", e);
			}
		}
		
		/**
		 * Returns the size of the image data.
		 * 
		 * @return The size of the image data.
		 */
		public long getSize() throws DomainException {
			// If it's a URL, just ask for the content length.
			if(url != null) {
				try {
					return url.openConnection().getContentLength();
				}
				catch(IOException e) {
					throw
						new DomainException(
							"There was an error connecting to the URL.",
							e);
				}
			}
			// If it's an InputStream, mark where we are in the contents, read
			// the number of bytes until the end, and then reset the mark.
			else if(inputStream != null) {
				long result = 0;
				
				inputStream.mark(Integer.MAX_VALUE);
				
				try {
					int amountRead;
					byte[] chunk = new byte[4096];
					while((amountRead = inputStream.read(chunk)) != -1) {
						result += amountRead;
					}
				}
				catch(IOException e) {
					throw
						new DomainException(
							"There was an error reading from the input stream.",
							e);
				}
				finally {
					try {
						inputStream.reset();
					}
					catch(IOException e) {
						throw
							new DomainException(
								"There was an error resetting the stream.",
								e);
					}
				}
				
				return result;
			}
			
			// If we have added a new type of image data, we need to have a way
			// to report how long the data stream that represents the data is.
			throw 
				new IllegalArgumentException(
					"The new data format needs to have its size read.");
		}
		
		/**
		 * Returns the URL for this image data. If no URL exists, null is
		 * returned.
		 * 
		 * @return The URL for this image data or null.
		 */
		public URL getUrl() {
			return url;
		}
		
		/**
		 * Returns an InputStream to the data.
		 * 
		 * @return An InputStream to the data.
		 * 
		 * @throws DomainException There was an error opening a connection to
		 * 						   the data.
		 */
		public InputStream getInputStream() throws DomainException {
			// If we already have an input stream, use it.
			if(inputStream != null) {
				// Always reset the input stream first.
				try {
					inputStream.reset();
				}
				catch(IOException e) {
					throw
						new DomainException(
							"The input stream could not be reset.",
							e);
				}
				
				// Then, return it.
				return inputStream;
			}
			// If we have a URL, get a new input stream and return it.
			else if(url != null) {
				try {
					// HT: set inputStream
					inputStream = url.openStream();
					return inputStream;
				}
				catch(IOException e) {
					throw
						new DomainException(
							"Error opening the stream to the URL.",
							e);
				}
			}
			
			// If we have added a new type of image data, we need to have a way
			// to report how long the data stream that represents the data is.
			throw 
				new IllegalStateException(
					"The new data format needs to have an InputStream " +
						"generated.");
		}
		
		/**
		 * Returns the image type of the image or null if it was unknown.
		 * 
		 * @return The image type of the image or null if it was unknown.
		 */
		public String getImageType() {
			return imageType;
		}
		
		/**
		 * Creates a BufferedImage from the image data.
		 * 
		 * @return A BufferedImage from the image data.
		 * 
		 * @throws DomainException There was an error reading the image data or
		 * 						   the image data did not define an image.
		 */
		public BufferedImage getBufferedImage() throws DomainException {
			// If we have already memoized the BufferedImage, return it.
			if(bufferedImage == null) {
				// Get an InputStream for the image data.
				InputStream imageStream = getInputStream();
				
				// Memoize the BufferedImage.
				try {
					bufferedImage = ImageIO.read(imageStream);
					
					// If the image was not a valid image, we should get null
					// for the buffered image and should throw an exception.
					if(bufferedImage == null) {
						throw
							new DomainException(
								"The image contents are invalid.");
					}
				}
				catch(IOException e) {
					throw
						new DomainException("The image could not be read.", e);
				}
			}
			
			return bufferedImage;
		}
		
		/**
		 * Close the inputStream to the data.
		 * 
		 * @throws DomainException There was an error closing a connection to
		 * 						   the data.
		 */
		public void closeInputStream() throws DomainException {
			
			try {
				if(inputStream != null) {
					LOGGER.info("Closing input stream");
					inputStream.close();
				}
				bufferedImage = null;
				
			} catch(IOException e) {
				if (url != null)
					throw new DomainException("The input stream could not be close: " + url, e);
				else 
					throw new DomainException("The input stream could not be close. ", e);
			}
		}
		
	}
	
	// The unique identifier for this image.
	private final UUID id;
	
	// The map of image sizes to their corresponding data.
	private final Map<Size, ImageData> imageData =
		new HashMap<Size, ImageData>();
	
	private final Media.ContentInfo contentInfo; 
	
	/**
	 * Creates a new Image object from a URL object.
	 * 
	 * @param url The URL object to use to retrieve the image.
	 * 
	 * @throws DomainException The URL is null.
	 */
	public Image(
		final UUID id, 
		final Map<Size, URL> sizeToUrlMap) 
		throws DomainException {
		
		if(id == null) {
			throw new DomainException("The image's ID is null.");
		}
		this.id = id;
		
		if(sizeToUrlMap == null) {
			throw new DomainException("The size to URL is null.");
		}
		else if(sizeToUrlMap.isEmpty()) {
			throw new DomainException("The size to URL map is empty.");
		}
		// Ensure that the original size is always present. If this requirement
		// is removed, be sure to audit the rest of the code which may depened
		// on its existance.
		else if(! sizeToUrlMap.containsKey(ORIGINAL)) {
			throw
				new DomainException(
					"The size to URL map is missing the required original " +
						"size.");
		}
		
		for(Size size : sizeToUrlMap.keySet()) {
			imageData.put(size, new ImageData(sizeToUrlMap.get(size)));
		}
		this.contentInfo = new Media.ContentInfo(null, null);
	}
	
	/**
	 * Creates an original image from the image's byte[]
	 * 
	 * @param imageId The ID for this image.
	 *  
	 * @param imageContents The image's contents as a byte array.
	 * 
	 * @return Returns null if the image's contents are null or have a length 
	 * 		   of zero; otherwise, a BufferedImage representing a decoded  
	 * 		   version of the image are returned.
	 * 
	 * @throws ValidationException Thrown if the image is not null, has a 
	 * 							   length greater than 0, and isn't decodable 
	 * 							   as any type of known image.
	 */
	public Image (final UUID id, String contentType, String fileName,
			final byte[] imageByteArray) throws DomainException {
		
		if(id == null) {
			throw new DomainException("The image's ID is null.");
		}

		if((imageByteArray == null) || (imageByteArray.length == 0)) {
			throw new DomainException(ErrorCode.IMAGE_INVALID_DATA, "The image's data is empty.");
		}
		InputStream contents = new ByteArrayInputStream(imageByteArray);
		
		this.id = id;
		imageData.put(ORIGINAL, new ImageData(contents));
		this.contentInfo = new Media.ContentInfo(contentType, fileName);
	}
	
	/**
	 * Creates an original image from the image's input stream.
	 * 
	 * @param id The unique identifier for this image.
	 * 
	 * @param contents The original image data.
	 * 
	 * @throws DomainException The ID and/or data are null.
	 */
	public Image(
		final UUID id, 
		final InputStream contents)
		throws DomainException {
		
		if(id == null) {
			throw new DomainException("The image's ID is null.");
		}
		if(contents == null) {
			throw new DomainException("The image's data is null.");
		}
		
		this.id = id;
		imageData.put(ORIGINAL, new ImageData(contents));
		this.contentInfo = new Media.ContentInfo(null,  null);
	}
	
	/**
	 * Creates an original image from the image's file.
	 * 
	 * @param id The unique identifier for this image.
	 * 
	 * @param url The URL for the original data.
	 * 
	 * @throws DomainException The ID and/or data are null.
	 */
	public Image(
		final UUID id, 
		final URL url, 
		final String info)
		throws DomainException {
		
		if(id == null) {
			throw new DomainException("The image's ID is null.");
		}
		if(url == null) {
			throw new DomainException("The image's URL is null.");
		}
		
		this.id = id;
		imageData.put(ORIGINAL, new ImageData(url));
		this.contentInfo = Media.ContentInfo.createContentInfoFromUrl(url, info);
	}
	
	/**
	 * Validates that the data that this image references is valid image data.
	 * 
	 * @throws DomainException
	 *         The data could not be read or is not valid image data.
	 */
	public void validate() throws DomainException {
		imageData.get(ORIGINAL).getBufferedImage();
	}
	
	/**
	 * Returns whether or not a certain size of an image exists.
	 * 
	 * @param size
	 *        The size in question.
	 * 
	 * @return Whether or not the image was saved.
	 * 
	 * @throws DomainException
	 *         The size was null or this image was not built with a default
	 *         URL.
	 */
	public boolean sizeExists(final Size size) throws DomainException {
		// Validate the parameters.
		if(size == null) {
			throw new DomainException("The size is null.");
		}
		
		// Get the original file's URL.
		URL originalUrl = imageData.get(ORIGINAL).getUrl();
		
		// If the original file was not built with a URL, then the variants may
		// or may not exist, but we have no way of checking.
		if(originalUrl == null) {
			throw
				new DomainException(
					"This Image object was not built with a default URL.");
		}
		
		// Attempt to connect to the file. If it doesn't exist, an exception
		// will be thrown.
		try {
			Size.getUrl(size, originalUrl).openStream().close();
			LOGGER.debug("Size.getURL exists: " + Size.getUrl(size,originalUrl).toString());
			return true;
		}
		// The file does not exist.
		catch(IOException e) {
			LOGGER.debug("Size.getURL doesn't exists: " + Size.getUrl(size,originalUrl).toString());
			return false;
		}
	}
	
	// ==== beginning of iMedia implementation
	/**
	 * The ID of the image.
	 * 
	 * @return The image's ID.
	 */
	public UUID getId() {
		return id;
	}
				
	public InputStream getContentStream() throws DomainException {
		return getInputStream(ORIGINAL);
	}
	
	public long getFileSize() throws DomainException{
		return getSizeBytes(ORIGINAL);
	}
	
	//public Media.ContentInfo getContentInfo(){
	//	return contentInfo;
	//}

	public String getContentType() {
		return contentInfo.getContentType();
	}
	
	public String getFileName() { 
		return contentInfo.getFileName();
	}
	
	public String getMetadata() { 
		return contentInfo.toMetadata();
	}
	
	public File writeContent(final File directory) throws DomainException{
		return saveImage(directory);
	}
	
	// ==== end of iMedia implementation
	
	/**
	 * Returns the size of the image.
	 * 
	 * @param size Which size of the file to connect to.
	 * 
	 * @return The size of the image in bytes.
	 * 
	 * @throws DomainException The image doesn't have a URL for that size or 
	 * 						   there was a problem connecting to the image.
	 */
	public long getSizeBytes(final Size size) throws DomainException {
		return getImageData(size).getSize();
	}
	
	/**
	 * Gets the content type of the image.
	 * 
	 * @param size The size of the image for which the content type is desired.
	 * 
	 * @return The MIME-type of this image.
	 * 
	 * @throws DomainException There was an error reading the image's content
	 * 						   type.
	 */
	public String getContentType(final Size size) throws DomainException {
		return "image/" + getImageData(size).getImageType();
	}
	
	/**
	 * Returns an InputStream connected to the image.
	 * 
	 * @param size The desired {@link Size} of the image.
	 * 
	 * @return An InputStream connected to the image of the given size.
	 * 
	 * @throws DomainException There was an error connecting to the image.
	 */
	public InputStream getInputStream(final Size size) throws DomainException {
		return getImageData(size).getInputStream();
	}
	
	
	/**
	 * Close InputStreams of all Size connected to the image.
	 * 
	 * @throws DomainException There was an error closing the image streams.
	 */
	public void closeImageStreams() {
		LOGGER.debug("Attempting to close streams.");
		LOGGER.error("Error: attempting to close streams.");
		try { 
			for (Size size : imageData.keySet()) {
				LOGGER.info("HT: About to close Inputstream: " + size.toString());
				imageData.get(size).closeInputStream();
			}
		} catch (DomainException e){
			LOGGER.error("There was an error closing image streams associated with" + id);
		}
	}
	
	/**
	 * <p>Saves the images contents to disk in the given directory. This
	 * includes one file for each {@link Size}. The original file will be named
	 * with the  To
	 * save only a specific file size, use the
	 * {@link #saveImage(Size, File, boolean)} function. 
	 * 
	 * @param rootFile The location to save the original file. The different
	 * 				   file sizes will be saved in the same directory with a
	 * 				   filename of their {@link #getId() ID} and their 
	 * 				   respective {@link Size#getExtension() extensions}
	 * 				   appended.
	 *  
	 * @throws DomainException There was an error reading the image contents or
	 * 						   writing the file.
	 */
	public File saveImage(final File directory) throws DomainException {
		// This will keep track of the images as we create them.
		Map<Size, File> files = new HashMap<Size, File>();
		
		// Save the individual sizes.
		try {
			// For sizes that have already been decoded, save the file now.
			for(Size size : imageData.keySet()) {
				files.put(size, saveImage(size, directory, false));
			}
		}
		// If something happens, roll back and delete the files.
		catch(DomainException e) {
			for(File file : files.values()) {
				file.delete();
			}
			
			throw new DomainException("There was an error saving a file.", e);
		}
		
		// Return only the original file.
		return files.get(ORIGINAL);
	}
	
	/**
	 * Saves a specific size of the file. This checks to be sure that the 
	 * original image has a URL built with it.
	 * 
	 * @param size The size of the file to save.
	 * 
	 * @see #saveImage(File)
	 */
	public void saveImage(final Size size) throws DomainException {
		// Validate the input.
		if(size == null) {
			throw new DomainException("The size is null.");
		}
		// The original image must be saved with a save directory.
		if(ORIGINAL.equals(size)) {
			return;
		}
		
		// Get the URL for the original image.
		URL originalUrl = imageData.get(ORIGINAL).getUrl();
		// If the original image doesn't have a URL, then it may have never
		// been saved, which means there is nowhere to save the variants of the
		// image.
		try {
			saveImage(size, new File(originalUrl.getFile()), false);
		}
		catch(DomainException e) {
			throw
				new DomainException(
					"There was a problem creating the file.",
					e);
		}
	}
	
	/**
	 * <p>Saves a single size of this image into the desired destination.</p>
	 * 
	 * <p>If the destination is a directory, it will construct a filename for
	 * this image based on its ID and the file size. If the destination is a
	 * specific filename, it will save the image in that file. If 'absolute' is
	 * 'true', the filename will not be modified, but, if it is 'false', the
	 * filename will be appended with the size's
	 * {@link Size#getExtension() extension}.</p>
	 * 
	 * @param size The size of the image to save.
	 * 
	 * @param destination The destination to save the image's contents.
	 * 
	 * @param absolute Whether or not to append the extension onto the
	 * 				   filename.
	 * 
	 * @return The file where the image's contents were written.
	 * 
	 * @throws DomainException There was an error reading the image or writing
	 * 						   the file.
	 */
	private File saveImage(
		final Size size,
		final File destination,
		final boolean absolute)
		throws DomainException {
		
		// Create a file reference to the destination for this file.
		File fileDestination;
		// If it's a directory, create a file in the directory whose value is
		// the ID of the image and with the appropriate size extension.
		if(destination.isDirectory()) {
			fileDestination = 
				new File(
					destination.getAbsolutePath() + "/" +
					id.toString() + 
					size.getExtension());
		}
		else {
			StringBuilder destinationBuilder = 
				new StringBuilder(destination.getAbsolutePath());
			
			if(! absolute) {
				destinationBuilder.append(size.extension);
			}
			
			fileDestination = new File(destinationBuilder.toString());
		}
		
		// If the file doesn't exist, create it. If it already exists, there is
		// no point in overwriting it. Two images with the same ID should be a
		// check performed elsewhere in the system.
		if(! fileDestination.exists()) {
			writeFile(getImageData(size), fileDestination);
		}
		
		// Return the reference to the file.
		return fileDestination;
	}
	
	/**
	 * Retrieves the {@link Size} object that is associated with the
	 * user-friendly size parameter.
	 * 
	 * @param size The user-friendly name for the desired size.
	 * 
	 * @return The Size object that is associated with the given user-friendly
	 * 		   size parameter.
	 * 
	 * @throws IllegalArgumentException The size is unknown.
	 * 
	 * @see #ORIGINAL
	 * @see #SMALL
	 * @see #ICON
	 */
	public static Size getSize(final String size) {
		Size result = SIZES.get(size);
		
		if(result == null) {
			throw new IllegalArgumentException("The size is unknown.");
		}
		
		return result;
	}
	
	/**
	 * Returns all of the registered sizes.
	 * 
	 * @return All of the registered sizes.
	 */
	public static Collection<Size> getSizes() {
		return SIZES.values();
	}
	
	/**
	 * Retrieves the image data for this image of a given size.
	 * 
	 * @param size The size of the desired image.
	 * 
	 * @return The image data for the desired size.
	 * 
	 * @throws DomainException There was an error retrieving the image data.
	 */
	private ImageData getImageData(final Size size) throws DomainException {
		// Attempt to get the image data from the map of image data.
		ImageData result = imageData.get(size);
		
		// If the map didn't have the image data, create it and add it to the
		// map.
		if(result == null) {
			// Get the image data for the original image.
			ImageData originalData = imageData.get(ORIGINAL);
			
			// Get the original file's URL.
			URL originalUrl = originalData.getUrl();
			
			// If no URL exists, then the resulting image data will need to be
			// the transformation of the existing image.
			if(originalUrl == null) {
				result = size.transform(originalData);
			}
			// If the original URL exists,
			else {
				// Check if this size's image exists as well.
				URL sizeUrl = Size.getUrl(size, originalUrl);
				try {
					sizeUrl.openStream().close();
					result = new ImageData(sizeUrl);
				}
				// If this size's image does not exist, create it.
				catch(IOException e) {
					LOGGER.debug("HT: Transforming data to size " + size.getName());
					result = size.transform(originalData);
				}
			}
			
			// Save the new image data in the map.
			imageData.put(size, result);
			LOGGER.debug("HT: saving data to imageData " + size.getName());
		}
		
		// Return the image data.
		return result;
	}
	
	/**
	 * Writes the image data to the given file. This file *should* end with
	 * the string given by the {@link #getExtension()} function.
	 * 
	 * @param imageData The image data to be written.
	 * 
	 * @param destination The file to write the image to.
	 * 
	 * @throws DomainException There was an error reading the image data
	 * 						   or writing the file.
	 * 
	 * @see {@link #getExtension()}
	 */
	private final void writeFile(
		final ImageData imageData,
		final File destination)
		throws DomainException {
	
		LOGGER.debug("HT: Writing imageData to location: " + destination.toString());
		if(imageData == null) {
			throw new DomainException("The contents parameter is null.");
		}
		
		// Get the image data.
		InputStream contents = imageData.getInputStream();
		
		// Connect to the file that should write it.
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(destination);
		}
		catch(SecurityException e) {
			throw
				new DomainException(
					"The file is not allowed to be created.",
					e);
		}
		catch(FileNotFoundException e) {
			throw new DomainException("The file cannot be created.", e);
		}
		
		// Write the image data.
		try {
			int bytesRead;
			byte[] buffer = new byte[4096];
			while((bytesRead = contents.read(buffer)) != -1) {
				fos.write(buffer, 0, bytesRead);
			}
		}
		catch(IOException e) {
			throw
				new DomainException(
					"Error reading or writing the data.",
					e);
		}
		finally {
			try {
				fos.close();
			}
			catch(IOException e) {
				throw new DomainException("Could not close the file.", e);
			}
		}
	}
}
