package walla.ws;

import javax.validation.Valid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
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
	
	CreateCategory() POST /{profileName}/category
	UpdateCategory() PUT /{profileName}/category/{categoryId}
	DeleteCategory() DELETE /{profileName}/category/{categoryId}
	GetCategoryMeta() GET /{profileName}/category/{categoryId}
	
	GetCategoryList() GET /{profileName}/categories
	MoveToNewCategory() PUT {profileName}/category/{categoryId}/images	
	
	*/

@Controller
@RequestMapping("/ws")
public class CategoryController {

	private static final Logger meLogger = Logger.getLogger(CategoryController.class);

	@Resource(name="categoryServicePooled")
	private CategoryService categoryService;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	// POST /{profileName}/category
	//no client cache, no server cache
	@RequestMapping(value = { "/{profileName}/category" }, method = { RequestMethod.POST }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String CreateCategory(
			@RequestBody Category newCategory,
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
			long newCategoryId = categoryService.CreateCategory(customSession.getUserId(), newCategory, customResponse, customSession.getUserAppId(), requestId);
	
			responseCode = customResponse.getResponseCode();
			return "<CategoryId>" + newCategoryId + "</CategoryId>";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("CategoryController","CreateCategory", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	// PUT /{profileName}/category/{categoryId}
	//no client cache, no server cache
	@RequestMapping(value = { "/{profileName}/category/{categoryId}" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void UpdateCategory(
			@RequestBody Category newCategory,
			@PathVariable("profileName") String profileName,
			@PathVariable("categoryId") long categoryId,
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
	
			responseCode = categoryService.UpdateCategory(customSession.getUserId(), newCategory, categoryId, customSession.getUserAppId(), requestId);
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return;
		}
		finally { utilityService.LogWebMethod("CategoryController","UpdateCategory", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	// DELETE /{profileName}/category/{categoryId}
	// no client cache, no server cache
	@RequestMapping(value = { "/{profileName}/category/{categoryId}" }, method = { RequestMethod.DELETE } , 
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void DeleteCategory(
			@RequestBody Category existingCategory, 
			@PathVariable("categoryId") long categoryId,
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
	
			responseCode = categoryService.DeleteCategory(customSession.getUserId(), existingCategory, categoryId, customSession.getUserAppId(), requestId);
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return;
		}
		finally { utilityService.LogWebMethod("CategoryController","DeleteCategory", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}/category/{categoryId}
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/category/{categoryId}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Category GetCategoryMeta(
			@PathVariable("categoryId") long categoryId,
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
			Category responseCategory = categoryService.GetCategoryMeta(customSession.getUserId(), categoryId, customResponse, requestId);
			responseCode = customResponse.getResponseCode();
			
			return responseCategory;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("CategoryController","GetCategoryMeta", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//  GET /{profileName}/categories
	//  No client caching.  Check client side version against db timestamp.
	@RequestMapping(value="/{profileName}/categories", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody CategoryList GetCategoryList(
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
			{
				clientVersionTimestamp = new Date(headerDateLong);
			}
			
			CustomResponse customResponse = new CustomResponse();
			CategoryList categoryList = categoryService.GetCategoryListForUser(customSession.getUserId(), clientVersionTimestamp, customResponse, requestId);
			responseCode = customResponse.getResponseCode();
			
			return categoryList;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("CategoryController","GetCategoryList", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	// PUT {profileName}/category/{categoryId}/images
	// No client caching
	@RequestMapping(value = { "/{profileName}/category/{categoryId}/images" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void MoveToNewCategory(
			@RequestBody ImageIdList imageIdList,
			@PathVariable("categoryId") long categoryId,
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
	
			responseCode = categoryService.MoveToNewCategory(customSession.getUserId(), categoryId, imageIdList, customSession.getUserAppId(), requestId);
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("CategoryController","MoveToNewCategory", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}

}

