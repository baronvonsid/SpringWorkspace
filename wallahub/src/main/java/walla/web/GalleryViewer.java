package walla.web;

import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
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
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;

import walla.datatypes.java.*;
import walla.business.*;
import walla.datatypes.auto.*;
import walla.db.UtilityDataHelper;
import walla.utils.*;

	/*
	GetGalleryViewer() GET /{profileName}/gallery/{galleryName}
	GetGalleryImageList() GET /{profileName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}
	*/

@Controller
@RequestMapping("web")
public class GalleryViewer {

	private static final Logger meLogger = Logger.getLogger(GalleryViewer.class);

	//private long previewUserId = 66666666;
	
	//@Autowired
	//private CustomSessionState sessionState;
	
	@Resource(name="utilityDataHelper")
	private UtilityDataHelper utilityDataHelper;
	
	@Resource(name="galleryServicePooled")
	private GalleryService galleryService;
	 
	@Resource(name="imageServicePooled")
	private ImageService imageService;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	
	//  GET /{profileName}/gallery/{galleryName}
	@RequestMapping(value = { "/{profileName}/gallery/{galleryName}" }, method = { RequestMethod.GET }, produces=MediaType.APPLICATION_XHTML_XML_VALUE )
	public String GetGalleryViewer(
			@PathVariable("galleryName") String galleryName,
			@RequestParam(value="key", required=false) String urlComplex,
			@RequestParam(value="logonToken", required=false) String logonToken,
			@PathVariable("profileName") String profileName,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String responseView = "GalleryViewerError";
		String message = "";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				try
				{
					customSession = UserTools.GetGallerySession(profileName, galleryName, true, true, request, meLogger);
				}
				catch (WallaException wallaEx)
				{
					//Unexpected action,  forbidden.
					Thread.sleep(10000);
					model.addAttribute("message", "Request failed security checks.");
					return responseView;
				}
				
				if (customSession == null)
				{
					//Login still required.
					logonToken = (logonToken == null) ? "" : logonToken;
					urlComplex = (urlComplex == null) ? "" : urlComplex;

					if (urlComplex.length() == 32 || logonToken.length() == 28)
					{
						//No existing login, so use token to validate.  Create new sessions.
						HttpSession tomcatSession = request.getSession(false);
						if (tomcatSession != null)
							tomcatSession.invalidate();
						
						tomcatSession = request.getSession(true);
						customSession = new CustomSessionState();
						tomcatSession.setAttribute("CustomSessionState", customSession);
						
						customResponse = new CustomResponse();
						
						boolean pass = false;
						if (logonToken.length() == 28)
							pass = galleryService.AutoLoginGalleryUser(true, -1, logonToken, urlComplex, "", profileName, galleryName, request, customSession, customResponse);
						else
							pass = galleryService.AutoLoginGalleryUser(false, 2, logonToken, urlComplex, "", profileName, galleryName, request, customSession, customResponse);
						
						if (pass)
						{
							meLogger.debug("View gallery authorised.  User:" + profileName.toString() + " Gallery:" + galleryName);
							//utilityService.AddAction(ActionType.Gallery, customSession.getUserId(), "GalViewOK", galleryName);
						}
						else
						{
							message = "GetGalleryViewer request not authorised.  Profile User:" + profileName.toString() + " Gallery:" + galleryName;
							meLogger.warn(message);
							model.addAttribute("errorMessage", message);
							return responseView;
						}
					}
					else
					{
						int accessType = galleryService.GetGalleryAccessType(profileName, galleryName, customResponse);
						if (accessType == 1)
						{
							String path = new UrlPathHelper().getPathWithinApplication(request);
							message = "Gallery " + galleryName + " requires a password to view, please enter this to continue.";
							responseView = "redirect:./" + galleryName + "/logon?referrer=" 
								+ UriUtils.encodePath(path,"UTF-8") 
								+ "&message=" + UserTools.EncodeString(message, request);
						}
						else
						{
							if (accessType == 0)
							{					
								String path = new UrlPathHelper().getPathWithinApplication(request);
								//request.getPathInfo()
								message = "Gallery: " + galleryName + " is marked as Private.  To view this please login as: " + profileName;
								responseView = "redirect:/v1/web/logon?referrer=" 
									+ UriUtils.encodePath(path,"UTF-8")
									+ "&message=" + UserTools.EncodeString(message, request);
							}
							else if (accessType == 2)
							{
								message = "Gallery access is denied, the gallery url maybe incorrect.";
								model.addAttribute("errorMessage", message);
							}
							else
							{
								message = "Gallery login had an error and cannot continue.";
								model.addAttribute("errorMessage", message);
							}
						}
						
						meLogger.info(message);
						return responseView;

					}
				}
			}
			
			//**************************************************
			//**********  Passed security checks  **************
			//**************************************************
			
			Gallery gallery = galleryService.GetGalleryMeta(customSession.getUserId(), galleryName, customResponse);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				//TODO Verify that gallery view is recorded for all views.  ID might not be correct.
				if (customSession.isGalleryViewer())
					utilityService.AddAction(ActionType.Gallery, gallery.getId(), "GalViewOK", gallery.getName());
				
				String presentationJsp = CombineModelAndGallery(customSession, model, gallery, false);
				if (presentationJsp != null)
					responseView = presentationJsp;
			}
			else
			{
				String error = "Gallery could not be loaded.  The error code received was: " + customResponse.getResponseCode();
				model.addAttribute("errorMessage", error);
			}
			
			return responseView;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("errorMessage", "Gallery could not be loaded.  Error message: " + ex.getMessage()); 
			return responseView;
		}
		finally { UserTools.LogWebFormMethod("GetGalleryViewer", meLogger, startMS, request, responseView); response.setStatus(HttpStatus.OK.value()); }
	}
	
	//  GET /{profileName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}?preview=true
	@RequestMapping(value="/{profileName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}", method=RequestMethod.GET, 
			produces=MediaType.TEXT_HTML_VALUE )
	public String GetGalleryImageList(
			@PathVariable("galleryName") String galleryName,
			@PathVariable("sectionId") long sectionId,
			@PathVariable("profileName") String profileName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String responseView = "";
		String message = "";
		Date clientVersionTimestamp = null;
		int responseCode = HttpStatus.OK.value();
		
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				customSession = UserTools.GetGallerySession(profileName, galleryName, true, true, request, meLogger);
				if (customSession == null)
				{
					message = "GetGalleryImageList request not authorised.  Profile User:" + profileName.toString() + " Gallery:" + galleryName;
					meLogger.warn(message);
					responseCode = HttpStatus.UNAUTHORIZED.value();
					return null;
				}
			}
					
			long headerDateLong = request.getDateHeader("If-Modified-Since");
			if (headerDateLong > 0)
			{
				clientVersionTimestamp = new Date(headerDateLong);
			}

			ImageList imageList = imageService.GetImageList(customSession.getUserId(), "gallery", galleryName, sectionId, imageCursor, size, clientVersionTimestamp, customResponse);
			responseCode = customResponse.getResponseCode();
			
			if (responseCode == HttpStatus.OK.value())
			{
				Gallery gallery = galleryService.GetGalleryMeta(customSession.getUserId(), galleryName, customResponse);
				responseCode = customResponse.getResponseCode();
				if (responseCode == HttpStatus.OK.value())
				{
					model.addAttribute(gallery);
					model.addAttribute(customSession); 
					model.addAttribute("isPreview", false); 
					model.addAttribute(imageList);
					
					Style style = galleryService.GetStyle(gallery.getStyleId());
					model.addAttribute(style);

					Presentation presentation = galleryService.GetPresentation(gallery.getPresentationId());
					model.addAttribute(presentation);

					responseView = "gallery/viewer-standard-imagelist";
				}
			}
			
			return responseView;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			return null;
		}
		finally { UserTools.LogWebFormMethod("GetGalleryImageList", meLogger, startMS, request, responseView + " " + responseCode); response.setStatus(responseCode); }
	}
	
	//  GET /{profileName}/gallery/stylepreview?key=1234567890
	@RequestMapping(value = { "/{profileName}/gallery/stylepreview" }, method = { RequestMethod.GET }, produces=MediaType.APPLICATION_XHTML_XML_VALUE )
	public String GetGalleryPreview(
			@RequestParam(value="key", required=false) String galleryTempId,
			@PathVariable("profileName") String profileName,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String responseView = "GalleryViewerError";
		String message = "";
		
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			//Get application wide gallery preview list.
			ServletContext context = request.getSession().getServletContext();
			List<GalleryPreview> previewList = (List<GalleryPreview>)context.getAttribute("GalleryPreviewList");
			
			if (previewList == null)
			{
				Thread.sleep(3000);
				message = "Gallery preview request not allowed, not pending previews found.";
				meLogger.warn(message);
				model.addAttribute("errorMessage", message);
				return responseView;
			}
			
			GalleryPreview galleryPreview = null;
			
			//Find gallery object
			for (Iterator<GalleryPreview> iterater = previewList.iterator(); iterater.hasNext();)
			{
				GalleryPreview current = (GalleryPreview)iterater.next();

				if (current.getProfileName().equals(profileName)
						&& current.getKey().equals(galleryTempId))
				{
					galleryPreview = current;
					continue;
				}
			}
			
			if (galleryPreview == null)
			{
				Thread.sleep(3000);
				message = "Gallery Preview request not associated with a valid Gallery.";
				meLogger.warn(message);
				model.addAttribute("errorMessage", message);
			}
			else
			{
				HttpSession tomcatSession = request.getSession(true);
				
				CustomSessionState customSession = (CustomSessionState)tomcatSession.getAttribute("CustomSessionState");
				if (customSession == null)
				{
					customSession = new CustomSessionState();
					tomcatSession.setAttribute("CustomSessionState", customSession);
				}
				
				synchronized(customSession) {
					customSession.setProfileName(profileName);
					customSession.setGalleryPreview(galleryPreview.getGallery());
					customSession.setRemoteAddress(request.getRemoteAddr());
				}
				
				responseView = CombineModelAndGallery(customSession, model, galleryPreview.getGallery(), true);
			}

			return responseView;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("errorMessage", "Gallery preview could not be loaded."); 
			return responseView;
		}
		finally { UserTools.LogWebFormMethod("GetGalleryPreview", meLogger, startMS, request, responseView); response.setStatus(HttpStatus.OK.value()); }
	}
	
	//  GET /{profileName}/gallery/stylepreview/{sectionId}/{imageCursor}/{size}?preview=true
	@RequestMapping(value="/{profileName}/gallery/stylepreview/{sectionId}/{imageCursor}/{size}", method=RequestMethod.GET, 
			produces=MediaType.TEXT_HTML_VALUE )
	public String GetGalleryPreviewImageList(
			@PathVariable("sectionId") long sectionId,
			@PathVariable("profileName") String profileName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String responseView = "";
		String message = "";
		int responseCode = HttpStatus.OK.value();
		
		try
		{
			response.addHeader("Cache-Control", "no-cache");

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
					return null;
				}
			}

			ImageList imageList = imageService.GetPreviewImageList(sectionId, size);

			model.addAttribute(customSession.getGalleryPreview());
			model.addAttribute(customSession); 
			model.addAttribute("isPreview", true); 
			model.addAttribute(imageList);
			
			Style style = galleryService.GetStyle(customSession.getGalleryPreview().getStyleId());
			model.addAttribute(style);

			Presentation presentation = galleryService.GetPresentation(customSession.getGalleryPreview().getPresentationId());
			model.addAttribute(presentation);

			responseView = "gallery/viewer-standard-imagelist";

			return responseView;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
			return null;
		}
		finally { UserTools.LogWebFormMethod("GetGalleryImageList", meLogger, startMS, request, responseView + " " + responseCode); response.setStatus(responseCode); }
	}
	
	private String CombineModelAndGallery(CustomSessionState customSession, Model model, Gallery gallery, boolean isPreview) throws WallaException
	{
		model.addAttribute(gallery);
		model.addAttribute(customSession);
		model.addAttribute("isPreview", isPreview); 
		
		
		Style style = galleryService.GetStyle(gallery.getStyleId());
		model.addAttribute(style);

		Presentation presentation = galleryService.GetPresentation(gallery.getPresentationId());
		//model.addAttribute("jsp", presentation.getJspName());
		//model.addAttribute("imageSize", presentation.getThumbWidth());
		
		model.addAttribute(presentation);
		
		//model.addAttribute("groupingType", gallery.getGroupingType()); /* 0-None, 1-category, 2-tag */
		
		//if (gallery.getGroupingType().intValue() > 0)
		//	model.addAttribute("sectionList", gallery.getSections().getSectionRef());
		
		//model.addAttribute("totalImageCount", gallery.getTotalImageCount()); 

		//Get gallery name and description
		//model.addAttribute("name", gallery.getName()); 
		//model.addAttribute("desc", gallery.getDesc());
		
		//model.addAttribute("showGalleryName", gallery.isShowGalleryName());
		//model.addAttribute("showGalleryDesc", gallery.isShowGalleryDesc());
		//model.addAttribute("showImageName", gallery.isShowImageName());
		//model.addAttribute("showImageDesc", gallery.isShowImageDesc());
		//model.addAttribute("showImageMeta", gallery.isShowImageMeta());

		if (presentation.getMaxSections() == 0)
		{
			//Get image list embedded into initial jsp response.
			ImageList imageList;
			if (isPreview)
			{
				imageList = imageService.GetPreviewImageList(1, presentation.getMaxImagesInSection());
				model.addAttribute(imageList);
			}
			else
			{
				CustomResponse customResponse = new CustomResponse();
				imageList = imageService.GetImageList(customSession.getUserId(), "gallery", 
						gallery.getName(), -1, 0, presentation.getMaxImagesInSection(), null, customResponse);
				
				if (customResponse.getResponseCode() == HttpStatus.OK.value())
				{
					model.addAttribute(imageList);
				}
				else
				{
					meLogger.error(customResponse.getMessage());
					model.addAttribute("errorMessage", customResponse.getMessage());
					return null;
				}
			}
		}

		return presentation.getJspName();
	}
	
	
	
	
}
