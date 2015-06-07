package walla.ws;

import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

import walla.datatypes.java.*;
import walla.business.*;
import walla.datatypes.auto.*;
import walla.utils.*;

	/*
	CreateUpdateGallery() PUT /{profileName}/gallery/{galleryName}
	DeleteGallery() DELETE /{profileName}/gallery/{galleryName}
	GetGalleryMeta() GET /{profileName}/gallery/{galleryName}
	GetGalleryList() GET /{profileName}/galleries
	GetGalleryOptions() GET /{profileName}/gallery/galleryoptions
	GetGallerySections() GET /{profileName}/gallery/gallerysections
	PostGalleryPreview() POST /{profileName}/gallery/preview
	GetGalleryPassThroughToken GET /{profileName}/gallery/{galleryName}/gallerylogon
	*/

@Controller
@RequestMapping("/ws")
public class GalleryController {

	private static final Logger meLogger = Logger.getLogger(GalleryController.class);
	
	@Resource(name="galleryServicePooled")
	private GalleryService galleryService;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	//  PUT /{profileName}/gallery/{galleryName}
	@RequestMapping(value = { "/{profileName}/gallery/{galleryName}" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateGallery(
			@RequestBody Gallery newGallery,
			@PathVariable("galleryName") String galleryName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
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
	
			responseCode = galleryService.CreateUpdateGallery(customSession.getUserId(), newGallery, galleryName, customSession.getUserAppId(), requestId);
			if (responseCode == HttpStatus.MOVED_PERMANENTLY.value())
			{
				String newLocation = "/" + profileName + "/gallery/" + newGallery.getName();
				response.addHeader("Location", newLocation);
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("GalleryController","CreateUpdateGallery", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//  DELETE /{profileName}/gallery/{galleryName}
	@RequestMapping(value = { "/{profileName}/gallery/{galleryName}" }, method = { RequestMethod.DELETE } , 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void DeleteGallery(
			@RequestBody Gallery existingGallery, 
			@PathVariable("galleryName") String galleryName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
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
	
			responseCode = galleryService.DeleteGallery(customSession.getUserId(), existingGallery, galleryName, customSession.getUserAppId(), requestId);
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("GalleryController","DeleteGallery", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}/gallery/{galleryName}
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/gallery/{galleryName}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Gallery GetGalleryMeta(
			@PathVariable("galleryName") String galleryName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
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
			Gallery responseGallery = galleryService.GetGalleryMeta(customSession.getUserId(), galleryName, customResponse, requestId);
			
			responseCode = customResponse.getResponseCode();
			return responseGallery;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("GalleryController","GetGalleryMeta", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}

	//  GET /{profileName}/galleries
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/galleries", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody GalleryList GetGalleryList(
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
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
				clientVersionTimestamp = new Date(headerDateLong);
			
			CustomResponse customResponse = new CustomResponse();
			GalleryList galleryList = galleryService.GetGalleryListForUser(customSession.getUserId(), clientVersionTimestamp, customResponse, requestId);
			responseCode = customResponse.getResponseCode();
			
			return galleryList;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("GalleryController","GetGalleryList", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}

	//  GET - /{profileName}/gallery/galleryoption
	@RequestMapping(value="/{profileName}/gallery/galleryoption", method=RequestMethod.GET, 
	produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody GalleryOption GetGalleryOption(
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Date clientVersionTimestamp = null;
		try
		{
			response.addHeader("Cache-Control", "private, max-age=86400");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			long headerDateLong = request.getDateHeader("If-Modified-Since");
			if (headerDateLong > 0)
				clientVersionTimestamp = new Date(headerDateLong);

			CustomResponse customResponse = new CustomResponse();
			GalleryOption galleryOptions = galleryService.GetGalleryOption(customSession.getUserId(), clientVersionTimestamp, customResponse, requestId);
	
			responseCode = customResponse.getResponseCode();

			return galleryOptions;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("GalleryController","GetGalleryOption", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}/gallery/gallerysections
	//  No client caching.  No Server caching
	@RequestMapping(value="/{profileName}/gallery/gallerysections", method=RequestMethod.POST, consumes = MediaType.APPLICATION_XML_VALUE,
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Gallery GetGallerySections(
			@RequestBody Gallery requestGallery,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
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
			Gallery responseGallery = galleryService.GetGallerySections(customSession.getUserId(), requestGallery, customResponse, requestId);
			
			responseCode = customResponse.getResponseCode();
			return responseGallery;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("GalleryController","GetGallerySections", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}

	//  POST /{profileName}/gallery/preview
	@RequestMapping(value = { "/{profileName}/gallerypreview" }, method = { RequestMethod.POST }, produces=MediaType.TEXT_PLAIN_VALUE, 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String PostGalleryPreview(
			@RequestBody Gallery galleryPreview,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
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
	
			//Prepare Gallery Object for preview mode.
			galleryService.ResetGallerySectionForPreview(galleryPreview);

			//Get-set temporary gallery preview key
			synchronized(customSession) {
				if (customSession.getGalleryTempKey().length() != 32)
					customSession.setGalleryTempKey(UserTools.GetComplexString());
			}
			
			//Get application wide gallery preview list.
			ServletContext context = request.getSession().getServletContext();
			List<GalleryPreview> previewList = (List<GalleryPreview>)context.getAttribute("GalleryPreviewList");
			GalleryPreview existingItem = null;
			
			if (previewList == null)
			{
				//New List, so initialise.
				previewList = new ArrayList<GalleryPreview>();
				context.setAttribute("GalleryPreviewList", previewList);
			}
			else
			{
				//Find existing.  better to update, than add.  User can refresh browser more easily.
				for (Iterator<GalleryPreview> iterater = previewList.iterator(); iterater.hasNext();)
				{
					GalleryPreview current = (GalleryPreview)iterater.next();

					if (current.getProfileName().equals(customSession.getProfileName())
							&& current.getKey().equals(customSession.getGalleryTempKey()))
					{
						existingItem = current;
						continue;
					}
				}
			}
			
			GalleryPreview newPreviewItem = new GalleryPreview();
			newPreviewItem.setKey(customSession.getGalleryTempKey());
			newPreviewItem.setProfileName(customSession.getProfileName());
			newPreviewItem.setGallery(galleryPreview);
			newPreviewItem.setLastUpdated(new Date());

			synchronized(previewList) {
				if (existingItem != null)
					previewList.remove(existingItem);
				
				previewList.add(newPreviewItem);
			}
			
			responseCode = HttpStatus.OK.value();
			return "<GalleryTempKey>" + customSession.getGalleryTempKey() + "</GalleryTempKey>";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("GalleryController","PostGalleryPreview", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}

	//GET /{profileName}/gallery/{galleryName}/gallerylogon
	@RequestMapping(value="/{profileName}/gallery/{galleryName}/gallerylogon", method=RequestMethod.GET, produces=MediaType.APPLICATION_XML_VALUE,
			headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String GetGalleryPassThroughToken(
			@PathVariable("galleryName") String galleryName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request, 
			HttpServletResponse response)
	{	
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return "";
			}
			
			CustomResponse customResponse = new CustomResponse();
			String token = galleryService.GetGalleryPassThroughToken(galleryName, request, customSession, customResponse, requestId);
			responseCode = customResponse.getResponseCode();
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				return "<token>" + token + "</token>";
			}
			else
			{
				return "";
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return "";
		}
		finally { utilityService.LogWebMethod("GalleryController","GetGalleryPassThroughToken", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
}
