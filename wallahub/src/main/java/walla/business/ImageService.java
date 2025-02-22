package walla.business;

import java.sql.SQLException;
import java.util.*;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;
import walla.ws.*;

import javax.annotation.Resource;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.sql.DataSource;
import javax.xml.datatype.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;

import org.im4java.core.IM4JavaException;
import org.im4java.process.*;

@Service("ImageService")
public class ImageService {

	/*
	private String graphicsMagickPath="C:\\Program Files\\GraphicsMagick-1.3;";
	private final String originalsFolder = "C:\\temp\\WallaRepo\\Original";
	private final String thumbsFolder = "C:\\temp\\WallaRepo\\Thumbs";
	private final String mainCopyFolder = "C:\\temp\\WallaRepo\\MainCopy";
	private final String appWorkingFolder = "C:\\temp\\WallaRepo\\AppWorking";
	 */
	
	/*
	C:\temp\WallaRepo\Original\100001\556552.jpg
	C:\temp\WallaRepo\MainCopy\100001\556552.jpg
	C:\temp\WallaRepo\Thumb\75x75\100001\556552.jpg
	
	C:\temp\WallaRepo\AppWorking\Preview\Original\556552.jpg
	C:\temp\WallaRepo\AppWorking\Preview\MainCopy\556552.jpg
	C:\temp\WallaRepo\AppWorking\Preview\Thumb\72x72\556552.jpg
	 */
	
	@Value( "${path.graphicsMagickPath}" ) private String graphicsMagickPath;
	@Value( "${path.originalFolder}" ) private String originalFolder;
	@Value( "${path.thumbFolder}" ) private String thumbFolder;
	@Value( "${path.mainCopyFolder}" ) private String mainCopyFolder;
	@Value( "${path.appWorkingFolder}" ) private String appWorkingFolder;
	
	@Value( "${path.previewOriginalFolder}" ) private String previewOriginalFolder;
	@Value( "${path.previewThumbFolder}" ) private String previewThumbFolder;
	@Value( "${path.previewMainCopyFolder}" ) private String previewMainCopyFolder;

	@Resource(name="galleryDataHelper")
	private GalleryDataHelperImpl galleryDataHelper;
	
	@Resource(name="categoryDataHelper")
	private CategoryDataHelperImpl categoryDataHelper;
	
	@Resource(name="tagDataHelper")
	private TagDataHelperImpl tagDataHelper;
	
	@Resource(name="imageDataHelper")
	private ImageDataHelperImpl imageDataHelper;
	
	@Resource(name="utilityDataHelper")
	private UtilityDataHelperImpl utilityDataHelper;
	
	@Resource(name="tagServicePooled")
	private TagService tagService;
	
	@Resource(name="categoryServicePooled")
	private CategoryService categoryService;
	
	@Resource(name="cachedData")
	private CachedData cachedData;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	@Value( "${messaging.enabled}" ) private boolean messagingEnabled;
	
	private static final Logger meLogger = Logger.getLogger(ImageService.class);
	
	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	public ImageService()
	{
		meLogger.debug("ImageService object instantiated.");
	}

	public int CreateUpdateImageMeta(long userId, ImageMeta imageMeta, long imageId, long userAppId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			int responseCode = 0;
			
			if (imageMeta.getId() != imageId)
			{
				meLogger.warn("ImageId does not match Image Object data");
				return HttpStatus.CONFLICT.value();
			}
			
			if (imageMeta.getStatus().intValue() == 1)
			{
				imageDataHelper.CreateImage(userId, imageMeta, requestId);
				responseCode = HttpStatus.CREATED.value();
				
				if (messagingEnabled)
				{
					RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "ImageService", "SetupNewImage", requestId, imageId, userAppId, null);
					utilityService.SendMessageToQueue(QueueTemplate.NewImage, requestMessage, "NEWIMG");
				}
				else
					SetupNewImage(userId, imageId, userAppId, requestId);
				
			}
			else if (imageMeta.getStatus().intValue() == 4)
			{
				imageDataHelper.UpdateImage(userId, imageMeta, requestId);
				responseCode = HttpStatus.OK.value();
				
				utilityService.AddAction(ActionType.UserApp, userAppId, "ImgMetaUpd", "");
				
				if (imageMeta.getTags() != null && imageMeta.getTags().getTagRef().size() > 0)
				{
					for(ImageMeta.Tags.TagRef tagRef : imageMeta.getTags().getTagRef())
					{
						if (messagingEnabled)
						{
							RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "TagService", "TagRippleUpdate", requestId, tagRef.getId(), 0, null);
							utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "TAGUPD");
						}
						else
							tagService.TagRippleUpdate(userId, tagRef.getId(), requestId);
					}
				}
				
			}
			else
			{
				meLogger.warn("Image object is not in a valid state for this action");
				return HttpStatus.CONFLICT.value();
			}

			return responseCode;
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {utilityService.LogMethod("ImageService","CreateUpdateImageMeta", startMS, requestId, String.valueOf(imageId));}
	}

	public ImageMeta GetImageMeta(long userId, long imageId, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			//Get tag list for response.
			ImageMeta imageMeta = imageDataHelper.GetImageMeta(userId, imageId, requestId);
			if (imageMeta == null)
			{
				meLogger.warn("GetImageMeta didn't return a valid Image object");
				customResponse.setResponseCode(HttpStatus.NO_CONTENT.value());
				return null; 
			}
			else if (imageMeta.getStatus() == 5)
			{
				meLogger.warn("Image has now been deleted.");
				customResponse.setResponseCode(HttpStatus.GONE.value());
				return null; 
			}

			customResponse.setResponseCode(HttpStatus.OK.value());
			return imageMeta;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {utilityService.LogMethod("ImageService","GetImageMeta", startMS, requestId, String.valueOf(imageId));}
	}
	
	public int DeleteImages(long userId, ImageIdList imagesToDelete, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			long[] imageList = ArrayUtils.toPrimitive(imagesToDelete.getImageRef().toArray(new Long[imagesToDelete.getImageRef().size()]));
			
			//Mark images as inactive.
			imageDataHelper.MarkImagesAsInactive(userId, imageList, requestId);

			if (messagingEnabled)
			{
				RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "ImageService", "ImageDeletePermanent", requestId, 0, 0, imageList);
				utilityService.SendMessageToQueue(QueueTemplate.NoAgg, requestMessage, "IMGDEL");
			}
			else
				ImageDeletePermanent(userId, imageList, requestId);
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {utilityService.LogMethod("ImageService","DeleteImages", startMS, requestId, "");}
	}

	public UploadStatusList GetUploadStatusList(long userId, ImageIdList imageIdToCheck, List<Long> filesReceived, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			UploadStatusList currentUploadList = imageDataHelper.GetCurrentUploads(userId, imageIdToCheck, requestId);
			if (currentUploadList == null)
			{
				meLogger.warn("Current uploads could not be retrieved from the database.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			for (int i = 0; i < filesReceived.size(); i++)
			{
				//Check image now being returned has been correctly removed from the session files received list.
				boolean found = false;
				for (int ii = 0; ii < currentUploadList.getImageUploadRef().size(); ii++)
				{
					if (filesReceived.get(i) == currentUploadList.getImageUploadRef().get(ii).getImageId())
					{
						found = true;
					}
				}
				
				if (!found)
				{
					UploadStatusList.ImageUploadRef imageRef = new UploadStatusList.ImageUploadRef();
					imageRef.setImageId(filesReceived.get(i));
					imageRef.setStatus(1);

					currentUploadList.getImageUploadRef().add(imageRef);
				}
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());

			return currentUploadList;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {utilityService.LogMethod("ImageService","GetUploadStatusList", startMS, requestId, "");}
	}

	public long GetImageId(String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			long newImageId = utilityDataHelper.GetNewId("ImageId", requestId);

			return newImageId;
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {utilityService.LogMethod("ImageService","GetImageId", startMS, requestId, "");}
	}
	
	public ImageList GetImageList(long userId, String type, String identity, long sectionId, int imageCursor, int size, Date clientVersionTimestamp, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		//identity - For Category, this must be the category Id, for tags and galleries, this is the name.
		try
		{
			ImageList imageList = null;
			switch (type.toUpperCase())
			{
				case "TAG":
					imageList = tagDataHelper.GetTagImageListMeta(userId, identity, requestId);
					break;
				case "CATEGORY":
					try
					{
						long categoryId = Long.parseLong(identity);
						imageList = categoryDataHelper.GetCategoryImageListMeta(userId, categoryId, requestId);
					}
					catch (NumberFormatException ex)
					{
						meLogger.debug("An invalid category id was supplied.  Id:" + identity.toString());
						customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
						return null;
					}
					break;
				case "GALLERY":
					imageList = galleryDataHelper.GetGalleryImageListMeta(userId, identity, sectionId, requestId);
					break;
				default:
					String error = "GetImageList process failed, an invalid image list type was supplied:" + type;
					meLogger.error(error);
					return null;
			}

			if (imageList == null)
			{
				meLogger.warn("No image list header could not be retrieved from the database.  Type:" + type + " Identity:" + identity);
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			//Check if gallery list changed
			if (clientVersionTimestamp != null)
			{
				Date lastUpdated = imageList.getLastChanged().getTime();
				if (!lastUpdated.after(clientVersionTimestamp))
				{
					meLogger.debug("No image list generated because server timestamp (" + lastUpdated.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}
			
			if (imageList.getTotalImageCount() > 0)
			{
				switch (type.toUpperCase())
				{
					case "TAG":
						tagDataHelper.GetTagImages(userId, imageCursor, size, imageList, requestId);
						break;
					case "CATEGORY":
						categoryDataHelper.GetCategoryImages(userId,imageCursor, size, imageList, requestId);
						break;
					case "GALLERY":
						galleryDataHelper.GetGalleryImages(userId, imageCursor, size, imageList, requestId);
						break;
				}
			}
			else
			{
				imageList.setImageCursor(imageCursor);
				imageList.setImageCount(0);
				imageList.setImages(new ImageList.Images());
			}
				
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			return imageList;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {utilityService.LogMethod("ImageService","GetImageList", startMS, requestId, "Type:" + type + " Name:" + identity.toString());}
	}
	
	//TODO build out properly.
	public ImageList GetPreviewImageList(long sectionId, int size, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			ImageList imagesList = new ImageList();
			
			imagesList.setId(sectionId);
			imagesList.setImageCount(size);
			imagesList.setImageCursor(0);
			imagesList.setName("Sample name");
			imagesList.setDesc("Sample description");
			imagesList.setSectionId(sectionId);
			imagesList.setSectionImageCount(size);
			imagesList.setTotalImageCount(size);
			imagesList.setType("Gallery");
			
			ImageList.Images images = new ImageList.Images();
			
			for (int i = 0; i < size; i++)
			{
				ImageList.Images.ImageRef image = new ImageList.Images.ImageRef();
				image.setName("Sample foto " + i);
				image.setDesc("Sample description");
				image.setId((long)UserTools.RandInt(1, 10));
				images.getImageRef().add(image);
			}
			
			imagesList.setImages(images);
			
			return imagesList;
		}
		finally {utilityService.LogMethod("ImageService","GetPreviewImageList", startMS, requestId, String.valueOf(sectionId) + " " + String.valueOf(size));}
	}

	public File GetOriginalImageFile(long userId, long imageId, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			String path = GetOriginalUncompressed(userId, imageId, false, requestId);
			if (path.isEmpty())
			{
				customResponse.setMessage("Original image file could not be retreived.  ImageId:" + imageId);
				customResponse.setResponseCode(HttpStatus.GONE.value());
			}
			customResponse.setResponseCode(HttpStatus.OK.value());
			return new File(path);
		}
		catch (Exception ex)
		{
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {utilityService.LogMethod("ImageService","GetOriginalImageFile", startMS, requestId, String.valueOf(imageId));}
	}
	
	private String GetOriginalUncompressed(long userId, long imageId, boolean isPreview, String requestId) throws WallaException, IOException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			String path = GetFilePathIfExists(originalFolder, previewOriginalFolder, userId, "", imageId, isPreview);
			if (path.isEmpty())
				return "";
			
			Path originalUncompressedPath = Paths.get(appWorkingFolder,"ZipTemp", userId + "" + imageId);
			UserTools.DecompressFromZip(path, originalUncompressedPath.toString(), meLogger, utilityService, requestId);

			return originalUncompressedPath.toString();
		}
		finally {utilityService.LogMethod("ImageService","GetOriginalUncompressed", startMS, requestId, String.valueOf(imageId));}
	}
	
	public byte[] GetScaledImageFile(long userId, long imageId, int width, int height, boolean isPreview, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		byte[] bytes = null;
		try
		{
			String sourceFile;
			String destFile;
			
			//Check for aspect ratio, supported ratio is 1.0 or 1.77
			double requestAspectRatio = (double)width / (double)height;
			requestAspectRatio = UserTools.DoRound(requestAspectRatio,2);
			
			if (requestAspectRatio == 1.0 || requestAspectRatio == 1.78)
			{
				if (requestAspectRatio == 1.0)
				{
					if (width > 800)
					{
						String message = "Image size requested is not supported.  Size is too large for the aspect Ratio:" 
								+ String.valueOf(requestAspectRatio) + " Width:" + width;
						meLogger.warn(message);
						customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
						return null;
					}
					
					String folder = null;
					switch (width)
					{
						case 20:
							folder = "20x20";
							break;
						case 75:
							folder = "75x75";
							break;
						case 150:
							folder = "150x150";
							break;
						case 300:
							folder = "300x300";
							break;
						case 800:
							folder = "800x800";
							break;
					}

					boolean doResize = false;
					if (folder == null)
					{
						//Need to dynamically resize image
						doResize = true;
						if (width<20)
						{
							folder = "20x20";
						}
						else if (width<75)
						{
							folder = "75x75";	
						}
						else if (width<150)
						{
							folder = "150x150";	
						}
						else if (width<300)
						{
							folder = "300x300";	
						}
						else
						{
							folder = "800x800";
						}
					}
					
					//Check for file existing
					String imageFilePath = GetFilePathIfExists(thumbFolder, previewThumbFolder, userId, folder, imageId, isPreview);
					if (imageFilePath.isEmpty())
					{
						//File not present, so create it.
						String masterImageFilePath = GetFilePathIfExists(mainCopyFolder, previewMainCopyFolder, userId, "", imageId, isPreview);
						if (masterImageFilePath.isEmpty())
						{
							//Master file not present, so create a new one.
							//String originalFilePath = GetFilePathIfExists(originalFolder, previewOriginalFolder, userId, "", imageId, isPreview);
							String originalFilePath = GetOriginalUncompressed(userId, imageId, isPreview, requestId);
							if (originalFilePath.isEmpty())
							{
								meLogger.warn("GetImageFile didn't find a valid original Image object to process");
								customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
								return null;
							}
							
							destFile = GetFilePath(mainCopyFolder, previewMainCopyFolder, userId, "", imageId, "jpg", isPreview);
							ResizeAndSaveFile(userId, imageId, originalFilePath, destFile, 1920, 1920, true, requestId);
							
							UserTools.DeleteFile(originalFilePath, utilityService, requestId);
							masterImageFilePath = destFile;
						}

						int newWidth = Integer.valueOf(folder.substring(0, folder.indexOf("x")));
						int newHeight = Integer.valueOf(folder.substring(folder.indexOf("x")+1));
						
						destFile = GetFilePath(thumbFolder, previewThumbFolder, userId, newWidth + "x" + newHeight, imageId, "jpg", isPreview);
						ResizeAndSaveFile(userId, imageId, masterImageFilePath, destFile, newWidth, newHeight, false, requestId);
						
						imageFilePath = GetFilePathIfExists(thumbFolder, previewThumbFolder, userId, folder, imageId, isPreview);
					}
					
					if (doResize)
					{
				    	//BufferedImage bufferedImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB); 
				    	
				    	BufferedImage originalImage = ImageIO.read(new File(imageFilePath));
				    	int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();
				    	
				    	BufferedImage resizedImage = new BufferedImage(width, height, type);
				    	Graphics2D g = resizedImage.createGraphics();
				    	g.drawImage(originalImage, 0, 0, width, height, null);
				    	g.dispose();
				    	
				    	customResponse.setResponseCode(HttpStatus.OK.value());
				    	return ConvertImageToBytes(resizedImage);
					}
					else
					{
						customResponse.setResponseCode(HttpStatus.OK.value());
						return ConvertFileToBytes(imageFilePath);
					}
				}
				else
				{
					//If aspect ratio is 1.77 and requested size is greater than 1080, then this is too large for the main copy.
					if (height > 1080)
					{
						String message = "Image size requested is not supported.  Size is too large for the aspect Ratio:" 
								+ String.valueOf(requestAspectRatio) + " Height:" + width;
						meLogger.warn(message);
						customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
						return null;
					}
					
					String imageFilePath = GetFilePathIfExists(mainCopyFolder, previewMainCopyFolder, userId, "", imageId, isPreview);
					if (imageFilePath.isEmpty())
					{
						//Master file not present, so create a new one.
						//String originalFilePath = GetFilePathIfExists(originalFolder, previewOriginalFolder, userId, "", imageId, isPreview);
						String originalFilePath = GetOriginalUncompressed(userId, imageId, isPreview, requestId);
						if (originalFilePath.isEmpty())
						{
							String error = "GetImageFile didn't find a valid original Image object to process. UserId:" + userId + " ImageId:" + imageId;
							throw new WallaException("ImageService", "GetImageFile", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
						}
						
						destFile = GetFilePath(mainCopyFolder, previewMainCopyFolder, userId, "", imageId, "jpg", isPreview);
						ResizeAndSaveFile(userId, imageId, originalFilePath, destFile, 1920, 1920, true, requestId);
						UserTools.DeleteFile(originalFilePath, utilityService, requestId);
						
						imageFilePath = destFile;
					}
					
					if (height < 1080)
					{
				    	BufferedImage originalImage = ImageIO.read(new File(imageFilePath));
				    	int type = originalImage.getType() == 0? BufferedImage.TYPE_INT_ARGB : originalImage.getType();

						double currenctAspectRatio = (double)originalImage.getWidth() / (double)originalImage.getHeight();
						currenctAspectRatio = UserTools.DoRound(currenctAspectRatio,2);
						double newHeight = (double)width / currenctAspectRatio;

				    	BufferedImage resizedImage = new BufferedImage(width, (int)Math.round(newHeight), type);
				    	Graphics2D g = resizedImage.createGraphics();
				    	g.drawImage(originalImage, 0, 0, width, (int)Math.round(newHeight), null);
				    	g.dispose();
				    	
				    	customResponse.setResponseCode(HttpStatus.OK.value());
				    	return ConvertImageToBytes(resizedImage);
					}
					else
					{
						//No resize needed.
						customResponse.setResponseCode(HttpStatus.OK.value());
						return ConvertFileToBytes(imageFilePath);
					}
				}
			}
			else
			{
				meLogger.warn("Image size requested is not supported.  Aspect ratio doesnt match a known type.  Aspect Ratio:" + String.valueOf(requestAspectRatio));
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return null;
			}
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {utilityService.LogMethod("ImageService","GetScaledImageFile", startMS, requestId, String.valueOf(imageId));}
	}
	
	public byte[] GetMainCopyImageFile(long userId, long imageId, boolean isPreview, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			String imageFilePath = GetFilePathIfExists(mainCopyFolder, previewMainCopyFolder, userId, "", imageId, isPreview);
			
			if (imageFilePath.isEmpty())
			{
				//Master file not present, so create a new one.
				//String sourceFolder = GetFilePathIfExists(originalFolder, previewOriginalFolder, userId, "", imageId, isPreview);
				String originalFilePath = GetOriginalUncompressed(userId, imageId, isPreview, requestId);
				if (originalFilePath.isEmpty())
				{
					meLogger.warn("GetImageFile didn't find a valid original Image object to process");
					customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
					return null;
				}
				
				String destFile = GetFilePath(mainCopyFolder, previewMainCopyFolder, userId, "", imageId, "jpg", isPreview);
				
				ResizeAndSaveFile(userId, imageId, originalFilePath, destFile, 1920, 1920, true, requestId);
				
				UserTools.DeleteFile(originalFilePath, utilityService, requestId);
				imageFilePath = GetFilePathIfExists(mainCopyFolder, previewMainCopyFolder, userId, "", imageId, isPreview);
			}
			
			//No resize needed.
			customResponse.setResponseCode(HttpStatus.OK.value());
			return ConvertFileToBytes(imageFilePath);
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {utilityService.LogMethod("ImageService","GetMainCopyImageFile", startMS, requestId, String.valueOf(imageId));}
	}
	
	public byte[] GetAppImageFile(String imageRef, int width, int height, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			//Check for folder existing.  //Check for image existing.
			Path filePath = Paths.get(appWorkingFolder,"AppImages", width + "x" + height, imageRef + ".jpg");
			File filePathTemp = filePath.toFile();
    		if (!filePathTemp.exists())
    		{
				String message = "Image not found. ImageRef: " + imageRef + " Height: " + height + " Width: " + width;
				meLogger.warn(message);
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return null;
    		}

			//Return image.
			customResponse.setResponseCode(HttpStatus.OK.value());
			return ConvertFileToBytes(filePath.toString());
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {utilityService.LogMethod("ImageService","GetAppImageFile", startMS, requestId, imageRef);}
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void SetupNewImage(long userId, long imageId, long userAppId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			ProcessStarter.setGlobalSearchPath(graphicsMagickPath);

			//Update image to being processed.
			imageDataHelper.UpdateImageStatus(userId, imageId, 3, false, "", requestId);
			
			File uploadedFile = Paths.get(appWorkingFolder, "Que", String.valueOf(imageId) + "-" + Long.toString(userId) + ".zip").toFile();
			if (!uploadedFile.exists())
			{
				String error = "Uploaded file could not be found.  ImageId:" + imageId + " UserId:" + userId;
				throw new WallaException("ImageService", "SetupNewImage", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}

			ImageMeta imageMeta = imageDataHelper.GetImageMeta(userId, imageId, requestId);
			if (imageMeta == null)
			{
				String error = "SetupNewImage didn't return a valid Image object. UserId:" + userId + " ImageId:" + imageId;
				throw new WallaException("ImageService", "SetupNewImage", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}
			
			if (imageMeta.getStatus().intValue() != 3)
			{
				String error = "SetupNewImage didn't return an Image object with the correct status. UserId:" + userId + " ImageId:" + imageId + " Status:" + imageMeta.getStatus();
				throw new WallaException("ImageService", "SetupNewImage", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
			}

			/**************************************************************************/
			/****************** Enrich with Exif & Save image copies ******************/
			/**************************************************************************/
			//imageMeta.getFormat()
			
			
			//UserTools.Copyfile(uploadedFile.getPath().toString(), originalZipFile, meLogger);
			
			String originalUncompressedPath = GetFilePath(thumbFolder, previewThumbFolder, userId, "ziptemp", imageId, imageMeta.getFormat(), false);
			UserTools.DecompressFromZip(uploadedFile.getPath().toString(), originalUncompressedPath, meLogger, utilityService, requestId);
			
			
			//Archive original image
			String originalZipFile = GetFilePath(originalFolder, previewOriginalFolder, userId, "", imageId, "zip", false);
    		ImageUtilityHelper.SaveOriginal(userId, originalUncompressedPath, originalZipFile, utilityService, requestId);
    		
    		//Make one initial copy, to drive subsequent resizing and also to orient correctly.
			String mainImagePath = GetFilePath(mainCopyFolder, previewMainCopyFolder, userId, "", imageId, "jpg", false); 
			ResizeAndSaveFile(userId, imageId, originalUncompressedPath, mainImagePath, 1920, 1920, true, requestId);

			//String mainImagePath = GetFilePathIfExists(userId, "MainCopy", imageId, false);
			//if (mainImagePath.isEmpty())
			//{
			//	String error = "Unexpected error retrieving a resized image in the folder: MainCopy.  ImageId:" + imageId;
			//	throw new WallaException("ImageService", "SetupNewImage", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			//}
    				
			//Load image meta into memory and enrich properties.
			//TODO switch to wired class.
			String response = ImageUtilityHelper.EnrichImageMetaFromFileData(originalUncompressedPath, originalZipFile, imageMeta, meLogger, imageId, utilityService, requestId);
			if (!response.equals("OK"))
				throw new WallaException("ImageService", "SetupNewImage", response, 0); 
            
			ImageUtilityHelper.SwitchHeightWidth(mainImagePath, imageMeta, utilityService, requestId);
    		
			imageDataHelper.UpdateImage(userId, imageMeta, requestId);
    		
        	/*
	       	 PC 1600x900 - 1.77, 1024x768 - 1.33
	       	 iPhone 1136x640 - 1.75, 960x640 - 1.5
	       	 iPad 2048x1536 - 1.33
	       	 Surface2 1920x1080 - 1.77
	       	 */
			
			String destFile = GetFilePath(thumbFolder, previewThumbFolder, userId, "75x75", imageId, "jpg", false);
			ResizeAndSaveFile(userId, imageId, mainImagePath, destFile, 75, 75, false, requestId);

			destFile = GetFilePath(thumbFolder, previewThumbFolder, userId, "150x150", imageId, "jpg", false);
			ResizeAndSaveFile(userId, imageId, mainImagePath, destFile, 150, 150, false, requestId);
			
			destFile = GetFilePath(thumbFolder, previewThumbFolder, userId, "300x300", imageId, "jpg", false);
			ResizeAndSaveFile(userId, imageId, mainImagePath, destFile, 300, 300, false, requestId);
			
			destFile = GetFilePath(thumbFolder, previewThumbFolder, userId, "800x800", imageId, "jpg", false);
			ResizeAndSaveFile(userId, imageId, mainImagePath, destFile, 800, 800, false, requestId);

            //TODO Delete original uploaded image.
			UserTools.DeleteFile(uploadedFile.getPath(), utilityService, requestId);
			UserTools.DeleteFile(originalUncompressedPath.toString(), utilityService, requestId);
			
			
			imageDataHelper.UpdateImageStatus(userId, imageId, 4, false, "", requestId);
			
			//For Each Tag associated, call TagRippleUpdates decoupled
			if (imageMeta.getTags() != null)
			{
				if (imageMeta.getTags().getTagRef().size() > 0)
				{
					for(ImageMeta.Tags.TagRef tagRef : imageMeta.getTags().getTagRef())
					{
						if (messagingEnabled)
						{
							RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "TagService", "TagRippleUpdate", requestId, tagRef.getId(), 0, null);
							utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "TAGUPD");
						}
						else
							tagService.TagRippleUpdate(userId, tagRef.getId(), requestId);
						
					}
				}
			}
			
			if (messagingEnabled)
			{
				RequestMessage requestMessageTag = utilityService.BuildRequestMessage(userId, "TagService", "ReGenDynamicTags", requestId, imageMeta.getCategoryId(), 0, null);
				utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessageTag, "TAGDYNGEN");
				
				RequestMessage requestMessageCat = utilityService.BuildRequestMessage(userId, "CategoryService", "CategoryRippleUpdate", requestId, imageMeta.getCategoryId(), 0, null);
				utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessageCat, "CATUPD");
			}
			else
			{
				tagService.ReGenDynamicTags(userId, requestId);
				categoryService.CategoryRippleUpdate(userId, imageMeta.getCategoryId(), requestId);
			}

			utilityService.AddAction(ActionType.UserApp, userAppId, "ImgAdd", "");
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process SetupNewImage",ex);
			
			try {MoveImageToErrorFolder(userId, imageId, requestId);} catch (Exception logOrIgnore) {}
			try {imageDataHelper.UpdateImageStatus(userId, imageId, -1, true, ex.getMessage(), requestId);} catch (Exception logOrIgnore) {}
		}
		finally {utilityService.LogMethod("ImageService","SetupNewImage", startMS, requestId, String.valueOf(imageId));}
	}

	public void DeleteAllImagesCategory(long userId, long[] categoryIds, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			//Select all images in the deleted categories.
			ImageIdList imagesToDelete = imageDataHelper.GetActiveImagesInCategories(userId, categoryIds, requestId);
			if (imagesToDelete == null)
			{
				meLogger.warn("No images were found to delete.");
			}
			
			DeleteImages(userId, imagesToDelete, requestId);
		}
		catch (Exception ex) {
			meLogger.error("Image deletion for a category failed with an error", ex);
		}
		finally {utilityService.LogMethod("ImageService","DeleteAllImagesCategory", startMS, requestId, "");}
	}
	
	public void ImageDeletePermanent(long userId, long[] imagesToDelete, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			//Distinct tag list, a valid tagimage record
			long[] tags = imageDataHelper.GetTagsLinkedToImages(userId, imagesToDelete, requestId);
			if (tags == null)
			{
				meLogger.warn("Tags linked to images could not be retrieved from the database");
			}
			else
			{
				for (int i = 0; i < tags.length; i++)
				{
					if (messagingEnabled)
					{
						RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "TagService", "TagRippleUpdate", requestId, tags[i], 0, null);
						utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "TAGUPD");
					}
					else
						tagService.TagRippleUpdate(userId, tags[i], requestId);
					
				}
			}

			//Distinct list of categories - check if category still Active.
			long[] categories = imageDataHelper.GetCategoriesLinkedToImages(userId, imagesToDelete, requestId);
			if (categories == null)
			{
				meLogger.warn("Categories linked to images could not be retrieved from the database");
			}
			else
			{
				for (int i = 0; i < categories.length; i++)
				{
					if (messagingEnabled)
					{
						RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "CategoryService", "CategoryRippleUpdate", requestId, categories[i], 0, null);
						utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "CATUPD");
					}
					else
						categoryService.CategoryRippleUpdate(userId, categories[i], requestId);
				}
			}

			if (messagingEnabled)
			{
				RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "TagService", "ReGenDynamicTags", requestId, userId, 0, null);
				utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "TAGDYNGEN");
			}
			else
			{
				tagService.ReGenDynamicTags(userId, requestId);
			}
			
			//Clear up physical files.
			//Remove all temporary files stored.
			//Archive originals into Glacial storage.
		}
		catch (WallaException wallaEx) {
			meLogger.error("Image deletion failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("Image deletion failed with an error", ex);
		}
		finally {utilityService.LogMethod("ImageService","ImageDeletePermanent", startMS, requestId, "");}
	}
	
	//***********************************************************************************************************************
	//***********************************************************************************************************************
	//******************************************   private helper methods ***************************************************
	//***********************************************************************************************************************


	
	private String GetFilePathIfExists(String root, String previewRoot, long userId, String sizeFolder, long imageId, boolean isPreview)
	{
		/* Check for file exists, return path if present */
		Path folderPath = null;
		
		if (isPreview)
			if (sizeFolder.length() > 0)
				folderPath = Paths.get(previewRoot, sizeFolder);
			else
				folderPath = Paths.get(previewRoot);
		else
			if (sizeFolder.length() > 0)
				folderPath = Paths.get(root, String.valueOf(userId), sizeFolder);
			else
				folderPath = Paths.get(root, String.valueOf(userId));

		File file = UserTools.FileExistsNoExt(folderPath.toString(), String.valueOf(imageId));
		if (file != null)
		{
			return file.getPath();
		}
		return "";
	}
	
	private String GetFilePath(String root, String previewRoot, long userId, String sizeFolder, long imageId, String extension, boolean isPreview)
	{
		Path folderPath = null;
		File folder = null;
		
		if (isPreview)
			folderPath = Paths.get(previewRoot);
		else
			folderPath = Paths.get(root, String.valueOf(userId));
		
		folder = folderPath.toFile();
		if (!folder.exists())
			folder.mkdir();
		
		if (sizeFolder.length() > 0)
		{
			if (isPreview)
				folderPath = Paths.get(previewRoot, sizeFolder);
			else
				folderPath = Paths.get(root, String.valueOf(userId), sizeFolder);
			
			folder = folderPath.toFile();
			if (!folder.exists())
				folder.mkdir();
		}
		  
		return Paths.get(folderPath.toString(), String.valueOf(imageId) + "." + extension).toString();
	}
	
	private void ResizeAndSaveFile(long userId, long imageId, String sourceImagePath, String destinationImagePath, int width, int height, boolean isMain, String requestId) throws IOException, InterruptedException, IM4JavaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			if (isMain)
			{
				String tempFile = GetFilePath(thumbFolder, previewThumbFolder, userId, "ziptemp", imageId, "jpg", false);
				
				ImageUtilityHelper.SaveMainImage(userId, imageId, sourceImagePath, destinationImagePath, tempFile, width, height, utilityService, requestId);
				//Check if switch is needed.
	
				//if (ImageUtilityHelper.CheckForPortrait(destinationImagePath, meLogger))
				//{
					//Resize with portrait dimensions.
				//	UserTools.DeleteFile(destinationImagePath, meLogger);
				//	ImageUtilityHelper.SaveMainImage(userId, imageId, sourceImagePath, destinationImagePath, height, width, meLogger);
				//}
			}
			else
			{
				ImageUtilityHelper.SaveReducedSizeImages(userId, imageId, sourceImagePath, destinationImagePath, width, height, utilityService, requestId);
			}
		}
		finally {utilityService.LogMethod("ImageService","ResizeAndSaveFile", startMS, requestId, String.valueOf(imageId));}
	}

	private void MoveImageToErrorFolder(long userId, long imageId, String requestId)
	{
		Path sourceFile = Paths.get(appWorkingFolder, "Que", String.valueOf(imageId) + "-" + Long.toString(userId) + ".zip");
		Path destinationFile = Paths.get(appWorkingFolder, "Error", String.valueOf(imageId) + "-" + Long.toString(userId) + ".zip");
		
		File source = sourceFile.toFile();
		if (source.exists())
		{
			UserTools.MoveFile(sourceFile.toString(), destinationFile.toString(), utilityService, requestId);
		}
	}
	
	private byte[] ConvertFileToBytes(String filePath) throws IOException
	{
		File file = new File(filePath);
		byte[] bytes = new byte[(int) file.length()];
		
	    FileInputStream fileInputStream = new FileInputStream(file);
	    fileInputStream.read(bytes);
	    fileInputStream.close();

	    return bytes;
	}
	
	private byte[] ConvertImageToBytes(BufferedImage image) throws IOException
	{
		byte[] imageBytes=null;
		Iterator<ImageWriter> writers=ImageIO.getImageWritersByFormatName("jpg");
		
		ImageWriter writer=writers.next();
		if (writer == null)
		    throw new RuntimeException("JPG not supported");
		  
	    ImageWriteParam iwp = writer.getDefaultWriteParam();
	    iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    iwp.setCompressionQuality(0.9f);
	    

		/*
		Working but without sufficient quality.
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( image, "jpg", baos );
		baos.flush();
		imageBytes = baos.toByteArray();
		baos.close();
		*/	
		
	    //iw.write((IIOMetadata)null,new IIOImage(img,null,null),iwp);

		
	    final ByteArrayOutputStream byteOut=new ByteArrayOutputStream();	
		ImageOutputStream imageOut=ImageIO.createImageOutputStream(byteOut);
		
		writer.setOutput(imageOut);
		//writer.write(image);
		writer.write((IIOMetadata)null,new IIOImage(image,null,null),iwp);
		imageOut.flush();
		imageOut.close();
		imageBytes=byteOut.toByteArray();
		byteOut.close();
		
		return imageBytes;
		
		/*
		ImageOutputStream  outputStream =  ImageIO.createImageOutputStream(image);
	    Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg");
	    ImageWriter writer = iter.next();
	    ImageWriteParam iwp = writer.getDefaultWriteParam();
	    iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
	    iwp.setCompressionQuality(0.9f);
	    writer.setOutput(outputStream);
	    writer.write(null, new IIOImage(newImage,null,null), iwp);
	    writer.dispose();

		byte[] byteArray = ((DataBufferByte) newImage.getData().getDataBuffer()).getData();
		return byteArray;
		*/

	}

}