package walla.business;

import com.drew.imaging.*;
import com.drew.metadata.*;
import com.drew.metadata.exif.*;
import com.drew.metadata.jpeg.*;

import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.im4java.core.ConvertCmd;
import org.im4java.core.GraphicsMagickCmd;
import org.im4java.core.IM4JavaException;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.core.InfoException;
import org.springframework.http.HttpStatus;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;
import walla.ws.*;

public final class ImageUtilityHelper 
{
	//private static Logger meLogger = null;
	
	public static String EnrichImageMetaFromFileData(String imageFilePath, String originalZipFile, ImageMeta imageMeta, Logger meLogger, long imageId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			File imageFile = new File(imageFilePath);
			File zipFile = new File(originalZipFile);
			
			/* Size, Format, Date taken */
			String response = EnrichMetaFromFile(imageFile, zipFile, imageMeta, meLogger, imageId);
			if (!response.equals("OK"))
				return response;
			
			Metadata fileMetaData = null;
			try
			{
				fileMetaData = ImageMetadataReader.readMetadata(imageFile);
			}
			catch (ImageProcessingException | IOException e) 
			{
				imageMeta.setTakenDate(imageMeta.getTakenDateFile());
				return "OK";
				/*
				response = LoadFileIntoMemoryReadAttributes(imageFilePath, imageMeta, meLogger, imageId);
				if (!response.equals("OK"))
				{
					return "Meta data not supported, image could not be loaded." + response;
				}
				else
				{
					imageMeta.setTakenDate(imageMeta.getTakenDateFile());
					return "OK";
				}
				*/
			}
			
			if (imageMeta.getFormat().equals("JPG"))
			{
				JpegDirectory jpegDirectory = fileMetaData.getDirectory(JpegDirectory.class);
				if (jpegDirectory != null)
				{
					response = EnrichMetaFromJPEG(jpegDirectory, imageMeta, meLogger, imageId);
					if (!response.equals("OK"))
						return response;
				}
			}
			
			ExifIFD0Directory exifDirectory = fileMetaData.getDirectory(ExifIFD0Directory.class);
			if (exifDirectory != null)
			{
				response = EnrichMetaFromEXIF(exifDirectory, imageMeta, meLogger, imageId);
				if (!response.equals("OK"))
					return response;
			}
			
			ExifSubIFDDirectory exifSubDirectory = fileMetaData.getDirectory(ExifSubIFDDirectory.class);
			if (exifSubDirectory != null)
			{
				response = EnrichMetaFromEXIFSub(exifSubDirectory, imageMeta, meLogger, imageId);
				if (!response.equals("OK"))
					return response;
			}
	
			if (imageMeta.getTakenDate() == null)
			{
				if (imageMeta.getTakenDateMeta() != null)
				{
					imageMeta.setTakenDate(imageMeta.getTakenDateMeta());
				}
				else
				{
					imageMeta.setTakenDate(imageMeta.getTakenDateFile());
				}
			}
			
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return ex.getMessage();
		}
		finally {UserTools.LogMethod("EnrichImageMetaFromFileData", meLogger, startMS, String.valueOf(imageId));}
		
		/*
		for (Directory directory : fileMetaData.getDirectories()) 
		{   
		System.out.println(directory.getName());
		for (com.drew.metadata.Tag tag : directory.getTags()) 
		{         
		System.out.println(tag);     
			}
		}
		*/
		
		/* TODO Get Manufacturer and apply */
		/* TODO Process Manufacturer specifics and flash */
		/* TODO GPSInfo -> Phase 2. */
		/*
		Orientation:
		1 = Horizontal (normal) 
		2 = Mirror horizontal 
		3 = Rotate 180 
		4 = Mirror vertical 
		5 = Mirror horizontal and rotate 270 CW 
		6 = Rotate 90 CW 
		7 = Mirror horizontal and rotate 90 CW 
		8 = Rotate 270 CW
		 */

	}
	
	private static String EnrichMetaFromFile(File currentFile, File zipFile, ImageMeta imageMeta, Logger meLogger, long imageId)
	{
		//Enrich:
		/* Size, Format, Date taken */
		long startMS = System.currentTimeMillis();
		try
		{
			Path path = currentFile.toPath();
			BasicFileAttributeView attributeView = Files.getFileAttributeView(path, BasicFileAttributeView.class);
			BasicFileAttributes attributes = attributeView.readAttributes();
			
			imageMeta.setSize(attributes.size());

			Path zipPath = zipFile.toPath();
			BasicFileAttributeView zipAttributeView = Files.getFileAttributeView(zipPath, BasicFileAttributeView.class);
			BasicFileAttributes zipAttributes = zipAttributeView.readAttributes();
			
			imageMeta.setCompressedSize(zipAttributes.size());
			
			Calendar lastModifiedCalendar = Calendar.getInstance();
			lastModifiedCalendar.setTimeInMillis(attributes.lastModifiedTime().toMillis());
			imageMeta.setTakenDateFile(lastModifiedCalendar);
			
			/* Supported types 
			•JPG,JPEG,TIF,TIFF,PSD,PNG,BMP,GIF,CR2,ARW,NEF
			•Need to investigate CRW/NEF/ORF/RW2 and other RAW types.
			 */
	        
	        //Ensure integers are set OK when not supplied.
			imageMeta.setISO(0);
			imageMeta.setOrientation(0);
	        
			CustomInfo imageInfo = new CustomInfo(path.toString());
			//img = ImageIO.read(currentFile);
			imageMeta.setHeight(imageInfo.getImageHeight());
			imageMeta.setWidth(imageInfo.getImageWidth());
			
			//String extension = imageMeta.getOriginalFileName().substring(imageMeta.getOriginalFileName().lastIndexOf(".")+1);
			
			switch (imageInfo.getImageFormat().toUpperCase())
			{
				case "JPG":
				case "JPEG":
					imageMeta.setFormat("JPG");
					break;
				case "TIF":
				case "TIFF":
					imageMeta.setFormat("TIF");
					break;
				case "PSD":
				case "PNG":
				case "BMP":
				case "GIF":
				case "CR2":
				case "ARW":
				case "NEF":
					imageMeta.setFormat(imageInfo.getImageFormat().toUpperCase());
					break;
				default:
					String message = "ImageId:" + imageMeta.getId() + " Format not supported:" + imageInfo.getImageFormat().toUpperCase();
					meLogger.warn(message);
					return message;
			}
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return ex.getMessage();
		}
		finally {UserTools.LogMethod("EnrichMetaFromFile", meLogger, startMS, String.valueOf(imageId));}
	}
	
	/*
	private static String LoadFileIntoMemoryReadAttributes(String currentFilePath, ImageMeta imageMeta, Logger meLogger, long imageId)
	{
		long startMS = System.currentTimeMillis();
		//BufferedImage img = null;
		
		try
		{
			CustomInfo imageInfo = new CustomInfo(currentFilePath);
			//img = ImageIO.read(currentFile);
			imageMeta.setHeight(imageInfo.getImageHeight());
			imageMeta.setWidth(imageInfo.getImageWidth());
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return ex.getMessage();
		}
		finally {UserTools.LogMethod("LoadFileIntoMemoryReadAttributes", meLogger, startMS, String.valueOf(imageId));}
	}
	*/
	
	private static String EnrichMetaFromJPEG(JpegDirectory jpegDirectory, ImageMeta imageMeta, Logger meLogger, long imageId)
	{
		/*
 		height - integer
 		width - integer
		*/
		long startMS = System.currentTimeMillis();
		try
		{
			JpegDescriptor descriptor = new JpegDescriptor(jpegDirectory); 
			
			for (com.drew.metadata.Tag tag : jpegDirectory.getTags()) 
			{ 
				switch (tag.getTagType())
				{
					case JpegDirectory.TAG_JPEG_IMAGE_HEIGHT:
						imageMeta.setHeight(jpegDirectory.getInt(JpegDirectory.TAG_JPEG_IMAGE_HEIGHT));
						break;
					case JpegDirectory.TAG_JPEG_IMAGE_WIDTH:
						imageMeta.setWidth(jpegDirectory.getInt(JpegDirectory.TAG_JPEG_IMAGE_WIDTH));
						break;
				}
			}
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return ex.getMessage();
		}
		finally {UserTools.LogMethod("EnrichMetaFromJPEG", meLogger, startMS, String.valueOf(imageId));}
	}
	
	private static String EnrichMetaFromEXIF(ExifIFD0Directory exifDirectory, ImageMeta imageMeta, Logger meLogger, long imageId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			/*
			Create Date - Datetime
			Make - string
			Model - string
			Orientation - integer
			 */
			//ExifIFD0Descriptor descriptor = new ExifIFD0Descriptor(exifDirectory); 
			
			for (com.drew.metadata.Tag tag : exifDirectory.getTags()) 
			{ 
				switch (tag.getTagType())
				{
					case ExifIFD0Directory.TAG_DATETIME:
						Calendar takenDateMetaCalendar = Calendar.getInstance();
						takenDateMetaCalendar.setTimeInMillis(exifDirectory.getDate(ExifIFD0Directory.TAG_DATETIME).getTime());
						imageMeta.setTakenDateMeta(takenDateMetaCalendar);
						
						break;
					case ExifIFD0Directory.TAG_MAKE:
						imageMeta.setCameraMaker(exifDirectory.getString(ExifIFD0Directory.TAG_MAKE));
						break;
					case ExifIFD0Directory.TAG_MODEL:
						imageMeta.setCameraModel(exifDirectory.getString(ExifIFD0Directory.TAG_MODEL));
						break;
					case ExifIFD0Directory.TAG_ORIENTATION:
						imageMeta.setOrientation(exifDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION));
						break;
					case 256:  //RICOH only ?
						imageMeta.setHeight(exifDirectory.getInt(256));
						break;
					case 257: //RICOH only ?
						imageMeta.setHeight(exifDirectory.getInt(257));
						break;
				}
			}
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return ex.getMessage();
		}
		finally {UserTools.LogMethod("EnrichMetaFromEXIF", meLogger, startMS, String.valueOf(imageId));}
	}
	
	private static String EnrichMetaFromEXIFSub(ExifSubIFDDirectory exifSubDirectory, ImageMeta imageMeta, Logger meLogger, long imageId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			ExifSubIFDDescriptor descriptor = new ExifSubIFDDescriptor(exifSubDirectory); 
			
			/*
			Aperture string F9.1 
			ShutterSpeed string 1/255 sec
			ISO 100 integer
			Height 1024 integer
			Width 768 integer
			Create Date
			Time zone offsett integer
			 */
			
			for (com.drew.metadata.Tag tag : exifSubDirectory.getTags()) 
			{ 
				switch (tag.getTagType())
				{
					case ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL:
						/*Check for empty date, if so leave as blank*/
						//String tempDate = exifSubDirectory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
						Date tempDate = exifSubDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
						if (tempDate != null) 
						{
							Calendar takenDateMetaCalendar = Calendar.getInstance();
							takenDateMetaCalendar.setTimeInMillis(tempDate.getTime());
							imageMeta.setTakenDateMeta(takenDateMetaCalendar);
						}
						break;
					//case ExifSubIFDDirectory.TAG_TIME_ZONE_OFFSET:
						//System.out.println("timezoneoffset:" + exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_TIME_ZONE_OFFSET));
						//break;
					case ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT:
						imageMeta.setHeight(exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_HEIGHT));
						break;
					case ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH:
						imageMeta.setWidth(exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_EXIF_IMAGE_WIDTH));
						break;
					case ExifSubIFDDirectory.TAG_SHUTTER_SPEED:
						String shutterSpeed = (descriptor.getShutterSpeedDescription());
						if (shutterSpeed != null)
						{
							//if (shutterSpeed.length() > 10)
							//{
							//	String formated = shutterSpeed.substring(shutterSpeed.indexOf("/"), shutterSpeed.indexOf(" sec") - shutterSpeed.indexOf("/"));
							//	shutterSpeed = shutterSpeed.substring(0,shutterSpeed.indexOf("/")) + Long.parseLong(formated) + " sec";
							//}
							imageMeta.setShutterSpeed(shutterSpeed);
						}
						break;
					case ExifSubIFDDirectory.TAG_APERTURE:
						imageMeta.setAperture(descriptor.getApertureValueDescription());
						break;
					case ExifSubIFDDirectory.TAG_FNUMBER:
						if (imageMeta.getAperture() == null)
						{
							imageMeta.setAperture("f" + exifSubDirectory.getDouble(ExifSubIFDDirectory.TAG_FNUMBER));
						}
						break;
					case ExifSubIFDDirectory.TAG_EXPOSURE_TIME:
						if (imageMeta.getShutterSpeed() == null)
						{
							imageMeta.setShutterSpeed(descriptor.getExposureTimeDescription());
						}
						break;
					case ExifSubIFDDirectory.TAG_ISO_EQUIVALENT:
						imageMeta.setISO(exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
						//System.out.println("isoequivalent:" + descriptor.getIsoEquivalentDescription());
						//System.out.println("isoequivalent:" + exifSubDirectory.getInt(ExifSubIFDDirectory.TAG_ISO_EQUIVALENT));
						break;
				}
			}
			return "OK";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return ex.getMessage();
		}
		finally {UserTools.LogMethod("EnrichMetaFromEXIFSub", meLogger, startMS, String.valueOf(imageId));}
	}

	/********************************************************************************/
	/********************************************************************************/
	/**************************  Image file manipulations  **************************/
	/********************************************************************************/
	/**
	 * @throws InfoException ******************************************************************************/
	
	public static boolean CheckForPortrait(String mainImagePath, Logger meLogger) throws IOException, InfoException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			CustomInfo imageInfo = new CustomInfo(mainImagePath);
			if (imageInfo.getImageHeight() > imageInfo.getImageWidth())
				return true;
			else
				return false;
		}
		finally {UserTools.LogMethod("CheckForPortrait", meLogger, startMS, mainImagePath);}
	}
	
	/*
	public static boolean CheckForPortrait-old(String mainImagePath, Logger meLogger) throws IOException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			boolean portrait = false;
			
			BufferedImage img = null; //ImageIO.read(new File(mainImagePath));
			
			
			//Image img = null;
			ImageInputStream iis = new FileImageInputStream(new File(mainImagePath));

			for (Iterator<ImageReader> i = ImageIO.getImageReaders(iis); img == null && i.hasNext(); ) 
			{
				ImageReader r = i.next();
				try 
				{
				    r.setInput(iis);
				    img = r.read(0);
				} catch (IOException e) {}
			}
			iis.close();
			
			int height = img.getHeight();
			int width = img.getWidth();
			
			double aspectRatio = (double)width / (double)height;
			aspectRatio = UserTools.DoRound(aspectRatio,0);
			if (aspectRatio < 1)
				portrait = true;
			
			return portrait;
		}
		finally {UserTools.LogMethod("CheckForPortrait", meLogger, startMS, mainImagePath);}
	}
	*/
	
	
	public static void SwitchHeightWidth(String mainImagePath, ImageMeta imageMeta, Logger meLogger) throws IOException, InfoException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			
			//int imageMetaWidth = imageMeta.getWidth().intValue();
			//int imageMetaHeight = imageMeta.getHeight().intValue();
			
			CustomInfo imageInfo = new CustomInfo(mainImagePath);
			//if (imageInfo.getImageHeight() > imageInfo.getImageWidth())
				
			
			//BufferedImage img = ImageIO.read(new File(mainImagePath));
			//int updatedHeight = img.getHeight();
			//int updatedWidth = img.getWidth();
	
			//Calculate aspect ratio or target image.
			double updatedAspectRatio = (double)imageInfo.getImageWidth() / (double)imageInfo.getImageHeight();
			updatedAspectRatio = UserTools.DoRound(updatedAspectRatio,2);
			
			//Calculate aspect ratio of current image.
			double originalAspectRatio = (double)imageMeta.getWidth() / (double)imageMeta.getHeight();
			originalAspectRatio = UserTools.DoRound(originalAspectRatio,2);
			
			if (updatedAspectRatio != originalAspectRatio)
			{
				//Switch them.
				imageMeta.setHeight(imageMeta.getWidth());
				imageMeta.setWidth(imageMeta.getHeight());
			}
			
			
		}
		finally {UserTools.LogMethod("SwitchHeightWidth", meLogger, startMS, mainImagePath);}
	}
	
	public static void SaveMainImage(long userId, long imageId, String sourceFilePath, String destinationFilePath, String tempFilePath, int targetWidth, int targetHeight, Logger meLogger) throws IOException, InterruptedException, IM4JavaException
	{
		//Using the original image, save a JPEG version, correctly orientated with no EXIF
		long startMS = System.currentTimeMillis();
		try
		{

			
			CustomInfo imageInfo = new CustomInfo(sourceFilePath);
			String format = imageInfo.getImageFormat();
			if (format.compareToIgnoreCase("BMP") == 0)
			{
				//Special case, as resizing seems to mash it up.  So resize the jpeg.
				GraphicsMagickCmd cmd = new GraphicsMagickCmd("convert");
				IMOperation op = new IMOperation();
				op.addImage(sourceFilePath);
				op.autoOrient();
				op.strip();
				op.quality(90.0);
				op.addImage(tempFilePath);
				cmd.run(op);
				
				op = new IMOperation();
				op.addImage(tempFilePath);
				op.autoOrient();
				op.strip();
				op.quality(90.0);
				op.resize(targetWidth,targetHeight);
				op.addImage(destinationFilePath);
				cmd.run(op);				
			}
			else
			{
				GraphicsMagickCmd cmd = new GraphicsMagickCmd("convert");
				IMOperation op = new IMOperation();
				op.addImage(sourceFilePath);
				op.autoOrient();
				op.strip();
				op.quality(90.0);
				op.resize(targetWidth,targetHeight);
				op.addImage(destinationFilePath);
				cmd.run(op);
			}
		}
		finally {UserTools.LogMethod("SaveMainImage", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(imageId));}
	}
	
	public static void SaveOriginal(long userId, String fromFilePath, String originalFileDest, Logger meLogger) throws IOException, InterruptedException, IM4JavaException, WallaException
	{	
		long startMS = System.currentTimeMillis();
		try
		{
			//Path destinationFile = Paths.get(toFolderPath, imageId + "." + extension);
			UserTools.CompressToZip(fromFilePath, originalFileDest, meLogger);
			//UserTools.Copyfile(fromFilePath, originalFileDest, meLogger);
			
			//return destinationFile.toString();
		}
		finally {UserTools.LogMethod("SaveOriginal", meLogger, startMS, originalFileDest);}
	}
	
	public static void SaveReducedSizeImages(long userId, long imageId, String sourceFilePath, String destinationFilePath, int targetWidth, int targetHeight, Logger meLogger) throws IOException, InterruptedException, IM4JavaException
	{
		//Load image into memory, then save of JPEGs of particular sizes.
		//50, 300, 800.  Square Dimensions.  Cropping required.
		//800 and 1600.  Maintain aspect ratio.
		long startMS = System.currentTimeMillis();
		BufferedImage img = null;
		
		try
		{
			CustomInfo imageInfo = new CustomInfo(sourceFilePath);
			//img = ImageIO.read(new File(sourceFilePath));
			int imageMetaHeight = imageInfo.getImageHeight();
			int imageMetaWidth = imageInfo.getImageWidth();
			
			//int imageMetaWidth = imageMeta.getWidth().intValue();
			//int imageMetaHeight = imageMeta.getHeight().intValue();
			
			GraphicsMagickCmd cmd = new GraphicsMagickCmd("convert");
			
			// create the operation, add images and operators/options
			IMOperation op = new IMOperation();
			op.addImage(sourceFilePath);
			
			//Calculate aspect ratio or target image.
			double targetAspectRatio = (double)targetWidth / (double)targetHeight;
			targetAspectRatio = UserTools.DoRound(targetAspectRatio,2);
			
			//Calculate aspect ratio of current image.
			double currentAspectRatio = (double)imageMetaWidth / (double)imageMetaHeight;
			currentAspectRatio = UserTools.DoRound(currentAspectRatio,2);
			
			if (targetWidth > imageMetaWidth || targetHeight > imageMetaHeight)
			{
				op.resize(targetWidth, targetHeight, "^");
			}
			
			//Both target dimensions OK for a standard reduction is required.
			
			if (targetAspectRatio == currentAspectRatio)
			{
				//Same aspect, so just force a resize to the target dimensions
				op.resize(targetWidth,targetHeight);
			}
			else if (targetAspectRatio > currentAspectRatio)
			{
				//Target is wider vs current.
				//ie. a larger difference between width and height compared to current, so crop height.
				
				//Calculate best height for current to match target aspect ratio.
				int newTempHeight = (int)Math.floor((double)imageMetaWidth / targetAspectRatio);
				
				//Calculate height padding to centralise crop.
				int padding = (int) Math.floor((double)(imageMetaHeight - newTempHeight) / 2);
	
				op.crop(imageMetaWidth, newTempHeight, 0, padding);
			}
			else
			{
				//Target is thinner vs current.
				//Crop width.
			
				//Calculate best width for current to match target aspect ratio.
				int newTempWidth = (int)Math.floor((double)imageMetaHeight * targetAspectRatio);
				
				//Calculate width padding for crop
				int padding = (int)Math.floor((double)(imageMetaWidth - newTempWidth) / 2);
				
				op.crop(newTempWidth, imageMetaHeight, padding, 0);
			}
			
			op.resize(targetWidth,targetHeight);
	
			/*
			//If both dimensions smaller, so just leave the image dimensions as-is.
			
			//Image size is less than the target size needed.  So just do what you can.
			if (targetWidth < imageMetaWidth && targetHeight > imageMetaHeight)
			{
				//Image height is short.
				op.resize(targetWidth,imageMetaHeight);
				
			}
			
			if (targetWidth > imageMetaWidth && targetHeight < imageMetaHeight)
			{
				//Image if too skinny, so just resize height
				op.resize(targetWidth,imageMetaHeight);
			}
			*/
	
			op.quality(90.0);
			op.strip();
	
			op.addImage(destinationFilePath);
	
			cmd.run(op);
		
		}
		finally {UserTools.LogMethod("SaveReducedSizeImages", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(imageId));}
	}
}


/*
EXIF - Use to get at format.  Not needed currently.
private boolean GetEmbeddedFormat(Metadata metaData, ImageMeta imageMeta)
{
	//Check for particular embedded image information
	for (Directory directory : metaData.getDirectories()) 
	{     
		if (directory.getName().equals("JPEG1"))
		{
			imageMeta.setFormat("JPG");
		}

		if (directory.getName().equals("TIFF"))
		{
			imageMeta.setFormat("TIF");
		}
		
		//TODO Add in other formats.
	}
	
	return true;
}
*/