package walla.ws;

import javax.validation.Valid;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.stereotype.Controller;
import org.w3c.dom.*;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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

	GetNewUserToken() POST /newusertoken
	CreateUpdateAccount() PUT/POST /{profileName}
	GetAccountMeta() GET /{profileName}
	//AckEmailConfirm() GET /{profileName}/{validationString}
	CheckProfileName() GET /profilename/{profileName}
	
	CreateUpdateUserApp() PUT /{profileName}/userapp
	GetUserAppMarkSession() GET /{profileName}/userapp/{userAppId}
	GetSessionUserApp GET /{profileName}/userapp
	
	-- deleted CheckClientApp() POST /clientapp
	-- deleted SetClientApp PUT /{profileName}/clientapp
	
	VerifyApp() POST /verifyapp
	
	GetLogonToken() GET /logon/token
	Logon() POST /logon
	Logout() POST /logout
	
	Not finished:
	//TODO
	ChangePassword()
	*/

@Controller
@RequestMapping("/ws")
public class AccountController {

	private static final Logger meLogger = Logger.getLogger(AccountController.class);

	@Resource(name="accountServicePooled")
	private AccountService accountService;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	//POST /newusertoken
	@RequestMapping(value="/newusertoken", method=RequestMethod.POST, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Logon GetNewUserToken(
			@RequestBody AppDetail appDetail,
			HttpServletRequest request, 
			HttpServletResponse response)
	{	
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Logon responseLogon = new Logon();
		try
		{
			Thread.sleep(100);
			response.addHeader("Cache-Control", "no-cache");
			
			//TODO add remote address to the DB, to check for other sessions, from other IPs coming in.
			CustomSessionState customSession = null;
			try
			{
				customSession =  UserTools.GetInitialAdminSession(request, meLogger);
			}
			catch (WallaException wallaEx)
			{
				//Unexpected action,  forbidden.
				Thread.sleep(10000);
				responseCode = wallaEx.getCustomStatus();
				return null;
			}
			
			if (customSession == null)
			{
				//Existing session is not valid, so start again.
				HttpSession tomcatSession = request.getSession(false);
				if (tomcatSession != null)
					tomcatSession.invalidate();
				
				tomcatSession = request.getSession(true);
				customSession = new CustomSessionState();
				tomcatSession.setAttribute("CustomSessionState", customSession);
			}
			
			CustomResponse customResponse = new CustomResponse();
			accountService.SetAppAndPlatformWS(appDetail, customSession, customResponse, requestId);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The application/platform key failed validation.");
				responseCode = customResponse.getResponseCode();
				Thread.sleep(500);
				return null;
			}
			
			//A warning might be displayed here.
			if (customResponse.getMessage().length() > 0)
				responseLogon.setMessage(customResponse.getMessage());

			String key = accountService.GetNewUserToken(request, customSession, customResponse, requestId);
			responseCode = customResponse.getResponseCode();
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				responseLogon.setKey(key);
				return responseLogon;
			}
			else
			{
				return null;
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("AccountController","GetNewUserToken", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//  PUT /{profileName}
	@RequestMapping(value = { "/{profileName}" }, method = { RequestMethod.PUT, RequestMethod.POST }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void CreateUpdateAccount(
			@PathVariable("profileName") String profileName,
			@RequestBody Account account,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (account == null)
			{
				meLogger.warn("A valid account object not specified in the request.");
				responseCode = HttpStatus.BAD_REQUEST.value();
				return;
			}
			
			if (request.getMethod().compareTo("POST") == 0)
			{
				//Account record being updated.
				CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
				if (customSession == null)
				{
					responseCode = HttpStatus.UNAUTHORIZED.value();
					return;
				}

				if (!customSession.getProfileName().equalsIgnoreCase(account.getProfileName())
						|| customSession.getUserId() != account.getId())
				{
					CustomResponse customResponse = new CustomResponse();
					accountService.UpdateAccount(account, customResponse, requestId);
					responseCode = customResponse.getResponseCode();
				}
				else
				{
					meLogger.error("Request encountered a conflict between the request requestProfileName: " + profileName + " and the account profileName: "
						+ account.getProfileName() + ". Session userId: " + customSession.getUserId() + " Request UserId: " + account.getId());
					responseCode = HttpStatus.BAD_REQUEST.value();
				}
			}
			else
			{
				CustomSessionState customSession = UserTools.CheckNewUserSession(account, request, meLogger);
				if (customSession != null)
				{
					CustomResponse customResponse = new CustomResponse();
					accountService.CreateAccount(account, customResponse, customSession, requestId);
					responseCode = customResponse.getResponseCode();
				}
				else
				{
					meLogger.warn("Current session failed validation for new user creation.");
					responseCode = HttpStatus.UNAUTHORIZED.value();
					return;
				}
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("AccountController","Logon", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}
	@RequestMapping(value="/{profileName}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Account GetAccountMeta(
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
			Account account = accountService.GetAccountMeta(customSession.getUserId(), customResponse, requestId);

			responseCode = customResponse.getResponseCode();
			return account;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("AccountController","GetAccount", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	/*
	//  GET - /{profileName}/email?valid={validationString}
	@RequestMapping(value="/{profileName}/{validationString}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public void AckEmailConfirm(
			@PathVariable("profileName") String profileName,
			@RequestParam("validationString") String validationString,
			HttpServletRequest request,
			HttpServletResponse response)
	{	
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			responseCode = accountService.AckEmailConfirm(profileName, validationString);

			//TODO redirect to nice page.
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("AccountController","Logon", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
*/
	
	//  POST /{profileName}/userapp
	@RequestMapping(value = { "/{profileName}/userapp" }, method = { RequestMethod.POST }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String CreateUpdateUserApp(
			@PathVariable("profileName") String profileName,
			@RequestBody UserApp userApp,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			if (userApp == null)
			{
				responseCode = HttpStatus.BAD_REQUEST.value();
				meLogger.warn("No UserApp object was received in the request");
				return null;
			}
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger, true);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			
			long userAppId = userApp.getId();
			if (userAppId == 0)
			{
				userAppId = accountService.CreateUserApp(customSession.getUserId(), customSession.getAppId(), customSession.getPlatformId(), userApp, customResponse, requestId);
			}
			else
			{
				accountService.UpdateUserApp(customSession.getUserId(), customSession.getAppId(), customSession.getPlatformId(), userApp, customResponse, requestId);
			}
			
			responseCode = customResponse.getResponseCode();
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value() || customResponse.getResponseCode() == HttpStatus.CREATED.value())
				return "<UserAppId>" + userAppId + "</UserAppId>";
			else
				return null;
			
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("AccountController","CreateUpdateUserApp", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	// GET /{profileName}/userapp/{userAppId}
	@RequestMapping(value = { "/{profileName}/userapp/{userAppId}" }, method = { RequestMethod.GET }, 
			headers={"Accept-Charset=utf-8"}, produces=MediaType.APPLICATION_XML_VALUE )
	public @ResponseBody UserApp GetUserAppMarkSession(
			@PathVariable("userAppId") long userAppId,
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
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger, true);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			//if (customSession.getPlatformId() < 1 || customSession.getAppId() < 1)
			//{
			//	responseCode = HttpStatus.BAD_REQUEST.value();
			//	meLogger.warn("GetUserAppMarkSession request failed because no platform/app was setup, User:" + profileName);
			//	return null;
			//}
			
			CustomResponse customResponse = new CustomResponse();
			UserApp userApp = accountService.GetUserApp(customSession.getUserId(), customSession.getAppId(), customSession.getPlatformId(), userAppId, customResponse, requestId);
			
			if (userApp != null)
			{
				synchronized(customSession) {
					customSession.setUserApp(userApp);
				}
			}
			
			responseCode = customResponse.getResponseCode();
			return userApp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("AccountController","GetUserAppMarkSession", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}

	// GET /{profileName}/userapp
	@RequestMapping(value = { "/{profileName}/userapp" }, method = { RequestMethod.GET }, 
			headers={"Accept-Charset=utf-8"}, produces=MediaType.APPLICATION_XML_VALUE )
	public @ResponseBody UserApp GetSessionUserApp(
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
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger, true);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}
			
			if (customSession.getUserApp() == null)
			{
				responseCode = HttpStatus.NOT_ACCEPTABLE.value();
				return null;
			}
			else
			{
				responseCode = HttpStatus.OK.value();
				return customSession.getUserApp();
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("AccountController","GetSessionUserApp", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}	
	
	// GET /profilename/{profileName}
	@RequestMapping(value="/profilename/{profileName}", method=RequestMethod.GET, 
	produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String CheckProfileName(
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
			
			Thread.sleep(500);
			
			//TODO ensure correct session is used, cannot be anonymous
			
			String profileReturn = "USED";
			CustomResponse customResponse = new CustomResponse();
			if (accountService.CheckProfileNameIsUnique(profileName, customResponse, requestId))
			{
				profileReturn = "OK";
			}

			responseCode = customResponse.getResponseCode();
			return "<ProfileNameCheck>" + profileReturn + "</ProfileNameCheck>";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("AccountController","CheckProfileName", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	// PUT /{profileName}/clientapp
	/*
	@RequestMapping(	value = { "/{profileName}/clientapp" }, 
						method = { RequestMethod.PUT }, 
						consumes = MediaType.APPLICATION_XML_VALUE,
						headers={"Accept-Charset=utf-8"} )
	public void SetClientApp(
			@PathVariable("profileName") String profileName,
			@RequestBody ClientApp clientApp,
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

			CustomResponse customResponse = new CustomResponse();
			int appId = accountService.VerifyApp(clientApp, customResponse, requestId);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The application key failed validation.");
				responseCode = customResponse.getResponseCode();
				return;
			}
							
			int platformId = accountService.GetPlatformId(clientApp, customResponse, requestId);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The platform is not supported.");
				responseCode = customResponse.getResponseCode();
				return;
			}

			synchronized(customSession) {
				customSession.setPlatformId(platformId);
				customSession.setAppId(appId);
			}
			responseCode = HttpStatus.OK.value();
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("AccountController","SetClientApp", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	// POST /clientapp
	@RequestMapping(	value = { "/clientapp" }, 
						method = { RequestMethod.POST }, 
						consumes = MediaType.APPLICATION_XML_VALUE,
						headers={"Accept-Charset=utf-8"} )
	public void CheckClientApp(
			@RequestBody ClientApp clientApp,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			//TODO Anonymous sessions can use this.  Add additional protection
			Thread.sleep(200);
			
			CustomResponse customResponse = new CustomResponse();
			accountService.VerifyApp(clientApp, customResponse, requestId);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The application key failed validation.");
				responseCode = customResponse.getResponseCode();
				Thread.sleep(500);
				return;
			}
	
			accountService.GetPlatformId(clientApp, customResponse, requestId);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The platform is not supported.");
				responseCode = customResponse.getResponseCode();
				Thread.sleep(500);
				return;
			}
			
			responseCode = HttpStatus.OK.value();
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("AccountController","CheckClientApp", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	*/
	
	// POST /verifyapp
	/*
	@RequestMapping(	value = { "/verifyapp" }, 
						method = { RequestMethod.POST }, 
						consumes = MediaType.APPLICATION_XML_VALUE,
						produces=MediaType.APPLICATION_XML_VALUE,
						headers={"Accept-Charset=utf-8"} )
	public @ResponseBody ResponseDetail VerifyApp(
			@RequestBody AppDetail appDetail,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			//TODO Anonymous sessions can use this.  Add additional protection
			Thread.sleep(200);
			
			ResponseDetail responseDetail = new ResponseDetail();
			responseCode = accountService.VerifyApp(appDetail, responseDetail, requestId);
			if (responseCode != HttpStatus.OK.value())
			{
				meLogger.warn("The application/platform key failed validation.");
				Thread.sleep(500);
				return null;
			}

			return responseDetail;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("AccountController","VerifyApp", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	*/
	
	//POST /logon/token
	@RequestMapping(value="/logon/token", consumes = MediaType.APPLICATION_XML_VALUE, method=RequestMethod.POST, produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Logon GetLogonToken(
			@RequestBody AppDetail appDetail,
			HttpServletRequest request, 
			HttpServletResponse response)
	{	
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Logon responseLogon = new Logon();
		try
		{
			Thread.sleep(100);
			response.addHeader("Cache-Control", "no-cache");
			
			//TODO add remote address to the DB, to check for other sessions, from other IPs coming in.
			/*
			if (logon == null)
			{
				meLogger.warn("Logon request made, but no logon object submitted");
				responseCode = HttpStatus.BAD_REQUEST.value();
				return null;
			}
			*/
			
			CustomSessionState customSession = null;
			try
			{
				customSession =  UserTools.GetInitialAdminSession(request, meLogger);
			}
			catch (WallaException wallaEx)
			{
				//Unexpected action,  forbidden.
				Thread.sleep(10000);
				responseCode = wallaEx.getCustomStatus();
				return null;
			}
			
			if (customSession == null)
			{
				//Existing session is not valid, so start again.
				HttpSession tomcatSession = request.getSession(false);
				if (tomcatSession != null)
					tomcatSession.invalidate();
				
				tomcatSession = request.getSession(true);
				customSession = new CustomSessionState();
				tomcatSession.setAttribute("CustomSessionState", customSession);
			}
			
			CustomResponse customResponse = new CustomResponse();
			accountService.SetAppAndPlatformWS(appDetail, customSession, customResponse, requestId);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The application/platform key failed validation.");
				responseCode = customResponse.getResponseCode();
				Thread.sleep(500);
				return null;
			}
			
			//A warning might be displayed here.
			if (customResponse.getMessage().length() > 0)
				responseLogon.setMessage(customResponse.getMessage());

			customResponse = new CustomResponse();
			String key = accountService.GetLogonToken(request, customSession, customResponse, requestId);
			responseCode = customResponse.getResponseCode();
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				responseLogon.setKey(key);
				return responseLogon;
			}
			else
			{
				return null;
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { utilityService.LogWebMethod("AccountController","GetLogonToken", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//POST /logon
	@RequestMapping(value = { "/logon" }, method = { RequestMethod.POST }, 
			headers={"Accept-Charset=utf-8"}, consumes = MediaType.APPLICATION_XML_VALUE )
	public void Logon(
			@RequestBody Logon logon,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			
			CustomSessionState customSession = null;
			try
			{
				customSession =  UserTools.GetInitialAdminSession(request, meLogger);
			}
			catch (WallaException wallaEx)
			{
				//Unexpected action,  forbidden.
				Thread.sleep(10000);
				meLogger.warn("Logon request made, but invalid session present.");
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
			
			/*
			HttpSession tomcatSession = request.getSession(false);
			if (tomcatSession == null)
			{
				meLogger.warn("Logon request made, but no Tomcat session has been established.");
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
			
			CustomSessionState customSession = (CustomSessionState)tomcatSession.getAttribute("CustomSessionState");
			if (customSession == null)
			{
				meLogger.warn("Logon request made, but no custom session has been established.");
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return;
			}
			*/
			
			if (logon == null)
			{
				meLogger.warn("Logon request made, but no logon object submitted");
				responseCode = HttpStatus.BAD_REQUEST.value();
				return;
			}
			
			CustomResponse customResponse = new CustomResponse();
			accountService.LogonCheck(logon, request, customSession, customResponse, requestId);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				Cookie wallaSessionIdCookie = new Cookie("X-Walla-Id", UserTools.GetLatestWallaId(customSession));
				wallaSessionIdCookie.setPath("/wallahub/");
				response.addCookie(wallaSessionIdCookie);
				responseCode = HttpStatus.OK.value();
			}
			else
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
			}
			
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("AccountController","Logon", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}

	//POST /logout
	@RequestMapping(value = { "/logout" }, method = { RequestMethod.POST }, 
			headers={"Accept-Charset=utf-8"})
	public void Logout(
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			HttpSession tomcatSession = request.getSession(false);
			if (tomcatSession == null)
			{
				meLogger.warn("Logout request made, but no Tomcat session has been established.");
				responseCode = HttpStatus.BAD_REQUEST.value();
				return;
			}
			
			tomcatSession.invalidate();
			responseCode = HttpStatus.OK.value();
		}
		catch (Exception ex) {
			meLogger.error(ex);
		}
		finally { utilityService.LogWebMethod("AccountController","Logout", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}
	
	//GET /{profileName}/logon
	@RequestMapping(value="/{profileName}/logontoken", method=RequestMethod.GET, produces=MediaType.APPLICATION_XML_VALUE,
			headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String GetGalleryPassThroughToken(
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
			String token = accountService.GetAdminPassThroughToken(request, customSession, customResponse, requestId);
			
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
		finally { utilityService.LogWebMethod("AccountController","GetGalleryPassThroughToken", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(responseCode); }
	}

	//  GET /session
	@RequestMapping(value="/session", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE )
	public @ResponseBody String GetSession(HttpServletRequest request, HttpServletResponse response)
	{	
		try
		{
			
			
			HttpSession tomcatSession = request.getSession(false);
			if (tomcatSession == null)
			{
				meLogger.warn("Logon request made, but no Tomcat session has been established.");
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return "";
			}
			
			CustomSessionState customSession = (CustomSessionState)tomcatSession.getAttribute("CustomSessionState");
			if (customSession == null)
			{
				meLogger.warn("Logon request made, but no custom session has been established.");
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				return "";
			}
			
			response.setStatus(HttpStatus.OK.value());
			return  "ProfileName:" + customSession.getProfileName() + 
					" UserID:" + customSession.getUserId() +
					" PlatformId:" + customSession.getPlatformId() +
					" AppId:" + customSession.getAppId() +
					" UserAppId:" + ((customSession.getUserApp() == null) ? -1 : customSession.getUserApp().getId()) +
					" LogonKey:" + customSession.getNonceKey() +
					" Authenticated:" + customSession.isAuthenticated();
		}
		catch (Exception ex) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
	}
}
