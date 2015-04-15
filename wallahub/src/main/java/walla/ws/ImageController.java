package walla.ws;

import javax.validation.Valid;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;




import org.springframework.beans.factory.annotation.Value;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;



//To move/
import java.awt.*;  
import java.awt.image.*;  

import javax.annotation.Resource;
import javax.imageio.*;  
import javax.imageio.stream.FileImageOutputStream;

import walla.datatypes.java.*;
import walla.business.*;
import walla.datatypes.auto.*;
import walla.utils.*;

/*
	Images and Uploads

	CreateUpdateImageMeta() PUT /{profileName}/image/{imageId}/meta
	GetImageMeta() GET /{profileName}/image/{imageId}/meta
	DeleteImage() DELETE /{profileName}/image/{imageId}
	DeleteImageBulk() DELETE /{profileName}/image

	UploadImage POST /image - Two requests needed for image file upload.
	GetUploadStatus GET /image/uploadstatus

	GetImageList() /{type}/{identity}/{imageCursor}/{size}

	GetOriginalImage GET /{profileName}/image/{imageId}/original
	GetMainCopyImage GET /{profileName}/image/{imageId}/maincopy
	GetImage GET /{profileName}/image/{imageId}/{width}/{height}
	
	GetAppImage GET /{profileName}/appimage/{imageRef}/{width}/{height}
	GetPreviewMainCopyImage GET /{profileName}/imagepreview/{imageId}/maincopy
	GetPreviewImage GET /{profileName}/imagepreview/{imageId}/{width}
*/

@Controller
@RequestMapping("/ws")
public class ImageController {
	
	//@Autowired
	//private CustomSessionState sessionState;
	
	@Resource(name="imageServicePooled")
	private ImageService imageService;
	
	@Autowired
	private ServletContext servletContext;

	/*
	private enum ImageStatus
	{
		Pending 0, Queued 1, Processing 2, Complete 3, Error 4, Inactive 5
		None = 0, FileReceived = 1, AwaitingProcessed = 2, BeingProcessed = 3, Complete = 4, Inactive = 5
	}
	*/
	
	@Value( "${path.appWorkingFolder}" ) private String appWorkingFolder;
	@Value( "${upload.maxImageSizeMB}" ) private long maxImageSizeMB = 50;
	private static final Logger meLogger = Logger.getLogger(ImageController.class);

	//  PUT /{profileName}/image/{imageId}/meta
	@RequestMapping(value = { "/{profileName}/image/{imageId}/meta" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateImageMeta(
			@RequestBody ImageMeta imageMeta,
			@PathVariable("imageId") long imageId,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
			
			//Use status to work if create or update
			if (imageMeta.getStatus() == 4)
			{
				responseCode = imageService.CreateUpdateImageMeta(customSession.getUserId(), imageMeta, imageId, customSession.getUserAppId());	
			}
			else if (imageMeta.getStatus() == 1)
			{
				int foundIndex = -1;
				
				for (int i = 0; i < customSession.getUploadFilesReceived().size(); i++)
				{
					if (imageId == customSession.getUploadFilesReceived().get(i))
					{
						foundIndex = i;
						continue;
					}
				}
				
				if (foundIndex >= 0)
				{
					synchronized(customSession) {
						customSession.getUploadFilesReceived().remove(foundIndex);
					}
					responseCode = imageService.CreateUpdateImageMeta(customSession.getUserId(), imageMeta, imageId, customSession.getUserAppId());
				}
				else
				{
					String message = "A corresponding image file was not found to match the meta received.";
					meLogger.warn(message);
					responseCode = HttpStatus.NOT_ACCEPTABLE.value();
				}
			}
			else
			{
				String message = "Meta data cannot be updated for an image with this status: " + imageMeta.getStatus();
				meLogger.warn(message);
				responseCode = HttpStatus.NOT_ACCEPTABLE.value();
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("CreateUpdateImageMeta", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}/image/{imageId}/meta
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/image/{imageId}/meta", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody ImageMeta GetImageMeta(
			@PathVariable("imageId") long imageId,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			ImageMeta responseImageMeta = imageService.GetImageMeta(customSession.getUserId(), imageId, customResponse);
			
			responseCode = customResponse.getResponseCode();
			return responseImageMeta;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetImageMeta", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  DELETE /{profileName}/image/{imageId}
	@RequestMapping(value = { "/{profileName}/image/{imageId}" }, method = { RequestMethod.DELETE },  headers={"Accept-Charset=utf-8"} )
	public void DeleteImage(
			@PathVariable("imageId") long imageId,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}

			//Build object with one image reference.
			ImageList.Images.ImageRef toDelete = new ImageList.Images.ImageRef();
			toDelete.setId(imageId);
			ImageList imagesToDelete = new ImageList();
			imagesToDelete.setImages(new ImageList.Images());
			imagesToDelete.getImages().getImageRef().add(toDelete);
			
			responseCode = imageService.DeleteImages(customSession.getUserId(), imagesToDelete);
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("DeleteImage", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  DELETE /{profileName}/images
	@RequestMapping(value = { "/{profileName}/images" }, method = { RequestMethod.DELETE },  headers={"Accept-Charset=utf-8"}, consumes = MediaType.APPLICATION_XML_VALUE )
	public void DeleteImageBulk(
			@RequestBody ImageList imagesToDelete,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
	
			responseCode = imageService.DeleteImages(customSession.getUserId(), imagesToDelete);
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("DeleteImageBulk", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  POST /image
	@RequestMapping(value = { "/{profileName}/image" }, consumes=MediaType.APPLICATION_OCTET_STREAM_VALUE, method = { RequestMethod.POST }, headers={"Accept-Charset=utf-8", "Content-length"}, produces=MediaType.TEXT_PLAIN_VALUE )
	public @ResponseBody String UploadImage(
			@PathVariable("profileName") String profileName,
			@RequestHeader("Content-length") long contentLength,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			long maxImageSizeBytes = maxImageSizeMB * 1024 * 1024;
			
			if (contentLength > maxImageSizeBytes || request.getContentLength() != contentLength)
			{
				responseCode = HttpStatus.REQUEST_ENTITY_TOO_LARGE.value();
				meLogger.warn("UploadImage request failed, it was too large.  Content Length: " + contentLength + " Actual length: " + request.getContentLength());
				return null;
			}
			
			long newImageId = imageService.GetImageId(customSession.getUserId());
			if (newImageId > 0)
			{
				if (!SaveFileToQue(customSession.getUserId(), newImageId, request.getInputStream()))
				{
					responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
					meLogger.warn("UploadImage request failed,  no image could be saved.");
					return null;
				}
			}
			else
			{
				responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
				meLogger.warn("UploadImage request failed,  no image id could be retrieved.");
				return null;
			}
			
			synchronized(customSession) {
				customSession.getUploadFilesReceived().add(newImageId);
			}
			
			responseCode = HttpStatus.ACCEPTED.value();
			return "<ImageId>" + newImageId + "</ImageId>";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("UploadImage", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//  GET /{type}/{identity}/{imageCursor}/{size}
	//  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/{type}/{identity}/{imageCursor}/{size}", method=RequestMethod.GET,
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody ImageList GetImageList(
			@PathVariable("type") String type,
			@PathVariable("identity") String identity,
			@PathVariable("profileName") String profileName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			@RequestParam(value="sectionId", required=false) String paramSectionId,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Date clientVersionTimestamp = null;
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			long headerDateLong = request.getDateHeader("If-Modified-Since");
			if (headerDateLong > 0)
			{
				clientVersionTimestamp = new Date(headerDateLong);
			}
			
			CustomResponse customResponse = new CustomResponse();
			
			long sectionId = -1;
			if (paramSectionId != null)
				sectionId = Long.parseLong(paramSectionId);

			ImageList responseImageList = imageService.GetImageList(customSession.getUserId(), type, identity, sectionId, imageCursor, size, clientVersionTimestamp, customResponse);
			responseCode = customResponse.getResponseCode();

			return responseImageList;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetImageList", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//  GET /image/image/uploadstatus
	//  None = 0, FileReceived = 1, AwaitingProcessed = 2, BeingProcessed = 3, Complete = 4, Inactive = 5
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/image/uploadstatus", method=RequestMethod.POST, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody UploadStatusList GetUploadStatus(
			@RequestBody ImageIdList imageIdList,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}

			CustomResponse customResponse = new CustomResponse();
			UploadStatusList uploadStatusList = imageService.GetUploadStatusList(customSession.getUserId(), imageIdList, customSession.getUploadFilesReceived(),customResponse);
			responseCode = customResponse.getResponseCode();

			return uploadStatusList;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetUploadStatus", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//GET /image/{imageId}/original
	//Not client or server side caching.
	@RequestMapping(value = { "/{profileName}/image/{imageId}/original" }, method = { RequestMethod.GET }, produces=MediaType.APPLICATION_OCTET_STREAM_VALUE )
	public @ResponseBody void GetOriginalImage(
			@PathVariable("profileName") String profileName,
			@PathVariable("imageId") long imageId,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "private, max-age=31557600");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}

			File file = null;
			CustomResponse customResponse = new CustomResponse();
			ImageMeta responseImageMeta = imageService.GetImageMeta(customSession.getUserId(), imageId,customResponse);
			responseCode = customResponse.getResponseCode();
			
        	if (responseCode == HttpStatus.OK.value())
        	{
        		if (responseImageMeta.getStatus().intValue() != 4)
        		{
        			String message = "Image request cannot be completed.  Image has an incorrect status for downloading.  Status:" + responseImageMeta.getStatus();
        			meLogger.warn(message);
        			responseCode = HttpStatus.BAD_REQUEST.value();
        		}
        		else
        		{
        			file = imageService.GetOriginalImageFile(customSession.getUserId(), imageId, customResponse);
	        		response.setHeader("Content-Disposition","attachment;filename=" + imageId + "." + responseImageMeta.getFormat());
	        		
	        		if (file != null)
	        			UserTools.PopulateServletStream(file, response.getOutputStream());
	        		
	        		UserTools.DeleteFile(file.getPath().toString(), meLogger);
	        	}
        	}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("GetOriginalImage", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	
	//GET /{profileName}/image/{imageId}/maincopy
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{profileName}/image/{imageId}/maincopy" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetMainCopyImage(
			@PathVariable("profileName") String profileName,
			@PathVariable("imageId") long imageId,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "private, max-age=31557600");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				customSession = UserTools.GetGallerySession(profileName, "", false, true, request, meLogger);
			}
			
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			byte[] imageArray = imageService.GetMainCopyImageFile(customSession.getUserId(), imageId, false, customResponse);
			responseCode = customResponse.getResponseCode();
			
			if (imageArray != null && imageArray.length > 0)
			{
				response.setContentType("image/jpeg");
				OutputStream outputStream = response.getOutputStream();
				outputStream.write(imageArray);
			}
			
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("GetMainCopyImage", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//GET /{profileName}/image/{imageId}/{width}/{height}
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{profileName}/image/{imageId}/{width}/{height}" }, 
			method = { RequestMethod.GET }, 
			produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetImage(
			@PathVariable("profileName") String profileName,
			@PathVariable("imageId") long imageId,
			@PathVariable("width") int width,
			@PathVariable("height") int height,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "private, max-age=31557600");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				customSession = UserTools.GetGallerySession(profileName, "", false, true, request, meLogger);
			}
			
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			byte[] imageArray = imageService.GetScaledImageFile(customSession.getUserId(), imageId, width, height, false, customResponse);
			responseCode = customResponse.getResponseCode();
			
			if (imageArray != null && imageArray.length > 0)
			{
				response.setContentType("image/jpeg");
				OutputStream outputStream = response.getOutputStream();
				outputStream.write(imageArray);
			}
			
			/*
	        File file = new File("C:\\temp\\WallaRepo\\Thumb\\100001\\75x75\\1000021.jpg");

	         byte[] b = new byte[(int) file.length()];

           FileInputStream fileInputStream = new FileInputStream(file);
           fileInputStream.read(b);
           fileInputStream.close();
           
*/
			
			//File file = new File("C:\\temp\\WallaRepo\\Thumb\\100001\\75x75\\1000021.jpg");
			
			//FileImageOutputStream output = new FileImageOutputStream(file);
			//output.readFully(arg0)
			
			
			
			
			//if (responseImage != null)
			//	ImageIO.write(responseImage, "jpg", response.getOutputStream());
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("GetImage", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//GET /{profileName}/appimage/{imageRef}/{width}/{height}
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{profileName}/appimage/{imageRef}/{width}/{height}" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetAppImage(
			@PathVariable("profileName") String profileName,
			@PathVariable("imageRef") String imageRef,
			@PathVariable("width") int width,
			@PathVariable("height") int height,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "private, max-age=31557600");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			byte[] imageArray = imageService.GetAppImageFile(imageRef, width, height, customResponse);
			responseCode = customResponse.getResponseCode();
			
			if (imageArray != null && imageArray.length > 0)
			{
				response.setContentType("image/jpeg");
				OutputStream outputStream = response.getOutputStream();
				outputStream.write(imageArray);
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("GetAppImage", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//GET /{profileName}/imagepreview/{imageId}/maincopy
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{profileName}/imagepreview/{imageId}/maincopy" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetPreviewMainCopyImage(
			@PathVariable("profileName") String profileName,
			@PathVariable("imageId") long imageId,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String message = "";
		try
		{
			response.addHeader("Cache-Control", "private, max-age=31557600");

			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				customSession = UserTools.GetGalleryPreviewSession(profileName, request, meLogger);
				if (customSession == null)
				{
					Thread.sleep(3000);
					message = "GetPreviewMainCopyImage request not authorised.  Profile User:" + profileName.toString();
					meLogger.warn(message);
					responseCode = HttpStatus.UNAUTHORIZED.value();
					return;
				}
			}
			
			CustomResponse customResponse = new CustomResponse();
			byte[] imageArray = imageService.GetMainCopyImageFile(0, imageId, true, customResponse);
			responseCode = customResponse.getResponseCode();
			
			if (imageArray != null && imageArray.length > 0)
			{
				response.setContentType("image/jpeg");
				OutputStream outputStream = response.getOutputStream();
				outputStream.write(imageArray);
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("GetPreviewMainCopyImage", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//GET /{profileName}/imagepreview/{imageId}/{width}/{height}
	//Server side caching.  Client side caching.
	@RequestMapping(value = { "/{profileName}/imagepreview/{imageId}/{width}/{height}" }, method = { RequestMethod.GET }, produces=MediaType.IMAGE_JPEG_VALUE )
	public @ResponseBody void GetPreviewImage(
			@PathVariable("profileName") String profileName,
			@PathVariable("imageId") long imageId,
			@PathVariable("width") int width,
			@PathVariable("height") int height,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String message = "";
		
		try
		{
			response.addHeader("Cache-Control", "private, max-age=31557600");
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				customSession = UserTools.GetGalleryPreviewSession(profileName, request, meLogger);
				if (customSession == null)
				{
					Thread.sleep(3000);
					message = "GetGalleryPreviewImageList request not authorised.  Profile User:" + profileName.toString();
					meLogger.warn(message);
					responseCode = HttpStatus.UNAUTHORIZED.value();
					return;
				}
			}
			
			if (imageId == 1)
			{
				String simon = "simon";
				
			}
			
			CustomResponse customResponse = new CustomResponse();
			byte[] imageArray = imageService.GetScaledImageFile(0, imageId, width, height, true, customResponse);
			responseCode = customResponse.getResponseCode();
			
			if (imageArray != null && imageArray.length > 0)
			{
				OutputStream outputStream = response.getOutputStream();
				outputStream.write(imageArray);
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("GetPreviewImage", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	
	//***************************************
	//***************************************
	
	
	private boolean SaveFileToQue(long userId, long imageId, InputStream inputStream)
	{
		long startMS = System.currentTimeMillis();
		FileOutputStream outputStream = null;
        try {
            if (inputStream != null) 
            {
        		Path uploadedFilePath = Paths.get(appWorkingFolder, "Que", String.valueOf(imageId) + "-" + Long.toString(userId) + ".zip");
            	File file = uploadedFilePath.toFile();
                outputStream = new FileOutputStream(file);
 
                byte[] buffer = new byte[1024];
                int bytesRead;
 
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
 
                outputStream.flush();
                return true;
            }
            else
            {
            	return false;
            }
 
        } catch (FileNotFoundException ex) {
			meLogger.error(ex);
			return false;
        } catch (IOException ex) {
        	meLogger.error(ex);
        	return false;
        }
		finally { 
			if (outputStream != null) try { outputStream.close(); } catch (Exception logOrIgnore) {}
			if (inputStream != null) try { inputStream.close(); } catch (Exception logOrIgnore) {}
			UserTools.LogMethod("SaveFileToTemp", meLogger, startMS, "" + imageId); 
		}
	}

	private boolean SaveFileToTemptodelete(long userId, long imageId, InputStream inputStream)
	{
		long startMS = System.currentTimeMillis();
		ZipInputStream zipIs = null;
		FileOutputStream outputStream = null;
        try {
        	
            if (inputStream != null)
            {
            	zipIs = new ZipInputStream(new BufferedInputStream(inputStream));
            	ZipEntry zEntry = zipIs.getNextEntry();

        		Path uploadedFilePath = Paths.get(appWorkingFolder, "Que", String.valueOf(imageId) + "." + Long.toString(userId));
            	File file = uploadedFilePath.toFile();
                outputStream = new FileOutputStream(file);
 
                byte[] buffer = new byte[1024];
                int bytesRead;
 
                while ((bytesRead = zipIs.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
                return true;
            }
            return false;
 
        } catch (FileNotFoundException ex) {
			meLogger.error(ex);
			return false;
        } catch (IOException ex) {
        	meLogger.error(ex);
        	return false;
        }
		finally { 
			if (outputStream != null) try { outputStream.close(); } catch (Exception logOrIgnore) {}
			if (zipIs != null) try { zipIs.close(); } catch (Exception logOrIgnore) {}
			UserTools.LogMethod("SaveFileToTemp", meLogger, startMS, "" + imageId); 
		}
	}

}


/*
Images and Uploads

CreateUpdateImageMeta() PUT /{profileName}/image/{imageId}/meta
UpdateMultiImageMeta() PUT /image/multi/meta
DeleteImage() DELETE /{profileName}/image/{imageId}
GetImageMeta() GET /{profileName}/image/{imageId}/meta

GetOriginalImage GET /image/{imageId}
GetViewedImage GET /image/{platformId}/{imageId}
GetThumbnail GET /image/{platformId}/thumb/{imageId}

GetImageStatus GET /image/{imageId}/status  (received 0 \ processing 1\ complete 2\ error 3\ inactive 4)

UploadImage POST /image
Two requests needed for image file upload.

First - Send Headers
	Content-length: 1234567890
	Expect: 100-Continue
	X-WallaUploadId: {client generated id}
	
	Validate - HttpStatus.LENGTH_REQUIRED.value() is present.
	Depending on size: Respond with 417 Expectation Failed or 100 Continue
	Add to user session array of accepted upload ids.

Second, standard post
	Content-length: 1234567890
	X-WallaUploadId: {client generated id}
	
	Validate - Upload Id is valid for user.
			   Content length is correct and within tolerance.
	
	If upload is successful response with {imageId} in body
	response.setStatus(HttpStatus.ACCEPTED.value());

Client send Meta upload request on receiving Accepted.

*/
