package walla.ws;

import javax.validation.Valid;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
	CreateUpdateTag() PUT /{profileName}/tag/{tagName}
	DeleteTag() DELETE /{profileName}/tag/{tagName}
	GetTagMeta() GET /{profileName}/tag/{tagName}

	GetTagList() GET /{profileName}/tags
	AddImagesToTag() PUT /{profileName}/tag/{tagName}/images
	RemoveImagesFromTag DELETE /{profileName}/tag/{tagName}/images
	*/

@Controller
@RequestMapping("/ws")
public class TagController {

	private static final Logger meLogger = Logger.getLogger(TagController.class);

	//@Autowired
	//private CustomSessionState sessionState;
	
	@Autowired
	private TagService tagService;
	
	//  PUT /{profileName}/tag/{tagName}
	@RequestMapping(value = { "/{profileName}/tag/{tagName}" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateTag(
			@RequestBody Tag newTag,
			@PathVariable("tagName") String tagName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		long startMS = System.currentTimeMillis();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
		//			@Valid BindingResult validResult,	
			/*  Schema validation.  Bug\Feature in Spring. SPR-9378 - Fixed in 3.1.3 & 3.2
			 *  @RequestBody @Valid Tag newTag,
			 @Valid BindingResult validResult
			if (validResult.hasErrors())
			{
				meLogger.error("Tag request is not valid, User:" + profileName.toString() + ", Tag:" + tagName.toString() + " Validation: " + validResult.getAllErrors().get(0).toString());
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}
			*/
			
			/*
			 * HttpStatus.BAD_REQUEST.value() is schema\tag mapping fails.
			 * HttpStatus.INTERNAL_SERVER_ERROR.value() - unexpected error.
			 */
			
			/*
			if (validResult.hasErrors())
			{
				meLogger.error("Tag request is not valid, User:" + profileName.toString() + ", Tag:" + tagName.toString() + " Validation: " + validResult.getAllErrors().get(0).toString());
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return null;
			}
			*/
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
	
			responseCode = tagService.CreateUpdateTag(customSession.getUserId(), newTag, tagName);
			if (responseCode == HttpStatus.MOVED_PERMANENTLY.value())
			{
				String newLocation = "/" + profileName + "/tag/" + newTag.getName();
				response.addHeader("Location", newLocation);
			}
	
			response.setStatus(responseCode);
			if (meLogger.isDebugEnabled()) {meLogger.debug("CreateUpdateTag tag request completed, User:" + profileName.toString() + ", Tag:" + tagName.toString() + " Response code: " + responseCode);}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("CreateUpdateTag", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  DELETE /{profileName}/tag/{tagName}
	@RequestMapping(value = { "/{profileName}/tag/{tagName}" }, method = { RequestMethod.DELETE } , 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void DeleteTag(
			@RequestBody Tag existingTag, 
			@PathVariable("tagName") String tagName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		long startMS = System.currentTimeMillis();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
	
			responseCode = tagService.DeleteTag(customSession.getUserId(), existingTag, tagName);
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("DeleteTag", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}/tag/{tagName}
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/tag/{tagName}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Tag GetTagMeta(
			@PathVariable("tagName") String tagName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		long startMS = System.currentTimeMillis();
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
			Tag responseTag = tagService.GetTagMeta(customSession.getUserId(), tagName, customResponse);
			responseCode = customResponse.getResponseCode();
			return responseTag;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetTagMeta", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//  GET /{profileName}/tags
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/tags", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody TagList GetTagList(
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		Date clientVersionTimestamp = null;
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		long startMS = System.currentTimeMillis();
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
			TagList tagList = tagService.GetTagListForUser(customSession.getUserId(), clientVersionTimestamp, customResponse);
			responseCode = customResponse.getResponseCode();

			return tagList;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("TagList", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	// PUT /{profileName}/tag/{tagName}/images
	// No client caching
	@RequestMapping(value = { "/{profileName}/tag/{tagName}/images" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void AddImagesToTag(
			@RequestBody ImageIdList moveList,
			@PathVariable("tagName") String tagName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		long startMS = System.currentTimeMillis();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
	
			responseCode = tagService.AddRemoveTagImages(customSession.getUserId(), tagName, moveList, true);
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("AddImagesToTag", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	// PUT /{profileName}/tag/{tagName}/images
	// No client caching
	@RequestMapping(value = { "/{profileName}/tag/{tagName}/images" }, method = { RequestMethod.DELETE }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void RemoveImagesFromTag(
			@RequestBody ImageIdList moveList,
			@PathVariable("tagName") String tagName,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		long startMS = System.currentTimeMillis();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
	
			responseCode = tagService.AddRemoveTagImages(customSession.getUserId(), tagName, moveList, false);
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { UserTools.LogWebMethod("RemoveImagesFromTag", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
}
