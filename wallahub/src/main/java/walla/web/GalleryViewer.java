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
	
	@Autowired
	private GalleryService galleryService;
	 
	@Autowired
	private ImageService imageService;
	
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
		String responseJsp = "GalleryViewerError";
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
					return responseJsp;
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
						}
						else
						{
							message = "GetGalleryViewer request not authorised.  Profile User:" + profileName.toString() + " Gallery:" + galleryName;
							meLogger.warn(message);
							model.addAttribute("errorMessage", message);
							return responseJsp;
						}
					}
					else
					{
						int accessType = galleryService.GetGalleryAccessType(profileName, galleryName, customResponse);
						if (accessType == 1)
						{
							String path = new UrlPathHelper().getPathWithinApplication(request);
							message = "Gallery " + galleryName + " requires a password to view, please enter this to continue.";
							responseJsp = "redirect:./" + galleryName + "/logon?referrer=" 
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
								responseJsp = "redirect:/v1/web/logon?referrer=" 
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
						return responseJsp;

					}
				}
			}
			
			//**************************************************
			//**********  Passed security checks  **************
			//**************************************************
			
			Gallery gallery = galleryService.GetGalleryMeta(customSession.getUserId(), galleryName, customResponse);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				String presentationJsp = CombineModelAndGallery(customSession, model, gallery, false);
				if (presentationJsp != null)
					responseJsp = presentationJsp;
			}
			else
			{
				String error = "Gallery could not be loaded.  The error code received was: " + customResponse.getResponseCode();
				model.addAttribute("errorMessage", error);
			}
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("errorMessage", "Gallery could not be loaded.  Error message: " + ex.getMessage()); 
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("GetGalleryViewer", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	//  GET /{profileName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}?preview=true
	@RequestMapping(value="/{profileName}/gallery/{galleryName}/{sectionId}/{imageCursor}/{size}", method=RequestMethod.GET, 
			produces=MediaType.TEXT_HTML_VALUE )
	public void GetGalleryImageList(
			@PathVariable("galleryName") String galleryName,
			@PathVariable("sectionId") long sectionId,
			@PathVariable("profileName") String profileName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Date clientVersionTimestamp = null;
		
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
					meLogger.warn("GetGalleryViewer request not authorised.  No session and no key or token supplied.  User:" + profileName.toString());
					return;
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
				    PrintWriter out = response.getWriter();
				    WriteOutImageList(profileName, out, gallery, imageList,false);
				}
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("GetGalleryImageList", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  GET /{profileName}/gallerypreview/sample?key=1234567890
	@RequestMapping(value = { "/{profileName}/gallerypreview/sample" }, method = { RequestMethod.GET }, produces=MediaType.APPLICATION_XHTML_XML_VALUE )
	public String GetGalleryPreview(
			@RequestParam(value="key", required=false) String galleryTempId,
			@PathVariable("profileName") String profileName,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		String responseJsp = "GalleryPreviewError";
		
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			//Get application wide gallery preview list.
			ServletContext context = request.getSession().getServletContext();
			List<GalleryPreview> previewList = (List<GalleryPreview>)context.getAttribute("GalleryPreviewList");
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
				String message = "Gallery Preview request not associated with a valid Gallery.";
				meLogger.warn(message);
				model.addAttribute("errorMessage", message);
			}
			else
			{
				CustomSessionState customSession = new CustomSessionState();
				customSession.setProfileName(profileName);

				String presentationJsp = CombineModelAndGallery(customSession, model, galleryPreview.getGallery(), true);
				if (presentationJsp != null)
				{
					HttpSession tomcatSession = request.getSession(true);
					tomcatSession.setAttribute("Gallery", galleryPreview.getGallery());
					tomcatSession.setAttribute("ProfileName", profileName);
					
					responseJsp = presentationJsp;
					responseCode = HttpStatus.OK.value();
				}
			}

			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("errorMessage", "Gallery preview could not be loaded.  Error message: " + ex.getMessage()); 
			return responseJsp;
		}
		finally { UserTools.LogWebMethod("GetGalleryPreview", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  GET /{profileName}/gallerypreview/sample/{sectionId}/{imageCursor}/{size}?preview=true
	@RequestMapping(value="/{profileName}/gallerypreview/sample/{sectionId}/{imageCursor}/{size}", method=RequestMethod.GET, 
			produces=MediaType.TEXT_HTML_VALUE )
	public void GetGalleryPreviewImageList(
			@PathVariable("sectionId") long sectionId,
			@PathVariable("profileName") String profileName,
			@PathVariable("imageCursor") int imageCursor,
			@PathVariable("size") int size,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			HttpSession tomcatSession = request.getSession(false);
			String sessionProfileName = (String)tomcatSession.getAttribute("ProfileName");
			Gallery gallery = (Gallery)tomcatSession.getAttribute("Gallery");
			
			if (!profileName.equalsIgnoreCase(sessionProfileName))
			{
				Thread.sleep(3000);
				responseCode = HttpStatus.UNAUTHORIZED.value();
				String message = "Gallery Preview ImageList request not authorised";
				meLogger.warn(message);
				return;
			}
			
			
			
			
			ImageList imageList = imageService.GetPreviewImageList(sectionId, size);
			//Gallery gallery = this.sessionState.getGalleryPreview();
		    PrintWriter out = response.getWriter();
			
			WriteOutImageList(profileName, out, gallery, imageList, true);
			
			response.setStatus(HttpStatus.OK.value());
			
			if (meLogger.isDebugEnabled()) {meLogger.debug("GetGalleryPreviewImageList completed, User:" + profileName + " Section Id:" + sectionId);}
		}
		catch (Exception ex) {
			meLogger.error("Received Exception in GetGalleryPreviewImageList", ex);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
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
	
	private void WriteOutImageList(String profileName, PrintWriter out, Gallery gallery, ImageList imageList, boolean isPreview) throws WallaException
	{
	    int lastImage = imageList.getImageCount() + imageList.getImageCursor();
	    
		Presentation presentation = galleryService.GetPresentation(gallery.getPresentationId());
		int imageSize = presentation.getThumbWidth();
		
	    out.println("<section id=\"imagesPane\""
	    		+ " class=\"ImagesPaneStyle\""
	    		+ " data-section-id=\"" + imageList.getSectionId() + "\"" 
	    		+ " data-section-image-count=\"" + imageList.getSectionImageCount() + "\""
	    		+ " data-images-first=\"" + imageList.getImageCursor() + "\""
	    		+ " data-images-last=\"" + lastImage + "\">");

		if (imageList.getImages() != null)
		{
			if (imageList.getImages().getImageRef().size() > 0)
			{
				//Construct update SQL statements
				for (Iterator<ImageList.Images.ImageRef> imageIterater = imageList.getImages().getImageRef().iterator(); imageIterater.hasNext();)
				{
					ImageList.Images.ImageRef current = (ImageList.Images.ImageRef)imageIterater.next();

					String name = "";
					String fullNameDesc = "";
					String imageUrl = "";
					String thumbUrl = "";
					
					if (gallery.isShowImageName() && gallery.isShowGalleryDesc())
					{
						name = current.getName();
						fullNameDesc = current.getName() + ((current.getDesc().length() > 0) ? ". " + current.getDesc() : "");
					}
					else if (gallery.isShowImageName())
					{
						name = current.getName();
						fullNameDesc = current.getName();
					}
					else if (gallery.isShowGalleryDesc())
					{
						name = current.getDesc();
						fullNameDesc = current.getDesc();
					}
					
					if (name == null)
						name = "";
						
					if (fullNameDesc == null)
						fullNameDesc = "";
					
					if (isPreview)
					{
						imageUrl = "../../../ws/" + profileName + "/imagepreview/" + current.getId() + "/" + 1920 + "/" + 1080 + "/";
						thumbUrl = "../../../ws/" + profileName + "/imagepreview/" + current.getId() + "/" + imageSize + "/" + imageSize + "/";
					}
					else
					{
						imageUrl = "../../../ws/" + profileName + "/image/" + current.getId() + "/" + 1920 + "/" + 1080 + "/";
						thumbUrl = "../../../ws/" + profileName + "/image/" + current.getId() + "/" + imageSize + "/" + imageSize + "/";
					}
					
					StringBuilder output = new StringBuilder();
					
					if (gallery.isShowImageName() || gallery.isShowGalleryDesc())
					{
						int addHeight = 20;
						
						if (gallery.isShowImageName() || gallery.isShowGalleryDesc())
							addHeight = 40;
						
						output.append("<article class=\"ImagesArticleStyle\" style=\"width:" + imageSize + "px;height:" + (imageSize + addHeight) + "px;\" ");
						output.append("id=\"imageId" + current.getId() + "\" data-image-id=\"" + current.getId() + "\">");

						output.append("<a class=\"image-popup-no-margins\" href=\"" + imageUrl + "\" title=\"" + name + "\">");
						output.append("<img class=\"thumbStyle\" title=\"" + name + "\" src=\"" + thumbUrl + "\"/></a>");
						
						output.append("<div class=\"ImagesArticleStyle\" style=\"width:" + imageSize + "px;height:" + addHeight + "px;\"><span>" + fullNameDesc + "</span></div></article>");
					}
					else
					{
						output.append("<article class=\"ImagesArticleNoNameStyle\" ");
						output.append("id=\"imageId" + current.getId() + "\" data-image-id=\"" + current.getId() + "\">");

						output.append("<a class=\"image-popup-no-margins\" href=\"" + imageUrl + "\" title=\"" + name + "\">");
						output.append("<img class=\"thumbStyle\" title=\"" + name + "\" src=\"" + thumbUrl + "\"/></a>");
					}

					
					
					out.println(output.toString());

				}
			}
		}
		out.println("</section>");
		out.close();
	}
	
	
	
	
}
