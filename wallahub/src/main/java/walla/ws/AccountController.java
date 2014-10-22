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

	GetNewUserToken() GET /newusertoken
	CreateUpdateAccount() PUT/POST /{profileName}
	GetAccount() GET /{profileName}
	AckEmailConfirm() GET /{profileName}/{validationString}
	CheckProfileName() GET /profilename/{profileName}
	
	CreateUpdateUserApp() PUT /{profileName}/userapp
	GetUserAppMarkSession() GET /{profileName}/userapp/{userAppId}
	
	CheckClientApp() POST /clientapp
	SetClientApp PUT /{profileName}/clientapp
	GetLogonToken() POST /logontoken
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

	//@Autowired
	//private CustomSessionState sessionState;
	
	@Autowired
	private AccountService accountService;
	
	
	//GET /newusertoken
	@RequestMapping(value="/newusertoken", method=RequestMethod.GET, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Logon GetNewUserToken(
			HttpServletRequest request, 
			HttpServletResponse response)
	{	
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Logon responseLogon = new Logon();
		try
		{
			Thread.sleep(300);
			response.addHeader("Cache-Control", "no-cache");
			
			//TODO add remote address to the DB, to check for other sessions, from other IPs coming in.
			
			HttpSession tomcatSession = request.getSession(true);
			
			CustomSessionState customSession = (CustomSessionState)tomcatSession.getAttribute("CustomSessionState");
			if (customSession == null)
			{
				customSession = new CustomSessionState();
				tomcatSession.setAttribute("CustomSessionState", customSession);
			}
			
			CustomResponse customResponse = new CustomResponse();
			String key = accountService.GetNewUserToken(request, customSession, customResponse);
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
		finally { UserTools.LogWebMethod("GetNewUserToken", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
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
					responseCode = accountService.UpdateAccount(account);
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
				if (UserTools.CheckNewUserSession(account, request, meLogger))
				{
					CustomResponse customResponse = new CustomResponse();
					accountService.CreateAccount(account, customResponse);
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
		finally { UserTools.LogWebMethod("Logon", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//  GET - /{profileName}
	@RequestMapping(value="/{profileName}", method=RequestMethod.GET, 
			produces=MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Account GetAccount(
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
			Account account = accountService.GetAccount(customSession.getUserId(), customResponse);

			responseCode = customResponse.getResponseCode();
			return account;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetAccount", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
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
		finally { UserTools.LogWebMethod("Logon", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}

	//  PUT /{profileName}/userapp
	@RequestMapping(value = { "/{profileName}/userapp" }, method = { RequestMethod.PUT }, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody String CreateUpdateUserApp(
			@PathVariable("profileName") String profileName,
			@RequestBody UserApp userApp,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
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
			
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseCode = HttpStatus.UNAUTHORIZED.value();
				return null;
			}

			if (customSession.getPlatformId() < 1 || customSession.getAppId() < 1)
			{
				responseCode = HttpStatus.BAD_REQUEST.value();
				meLogger.warn("CreateUpdateUserApp request failed because no platform/app was setup, User:" + profileName);
				return null;
			}

			CustomResponse customResponse = new CustomResponse();
			
			long userAppId = userApp.getId();
			if (userAppId == 0)
			{
				userAppId = accountService.CreateUserApp(customSession.getUserId(), customSession.getAppId(), customSession.getPlatformId(), userApp, customResponse);
			}
			else
			{
				accountService.UpdateUserApp(customSession.getUserId(), customSession.getAppId(), customSession.getPlatformId(), userApp, customResponse);
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
		finally { UserTools.LogWebMethod("CreateUpdateUserApp", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
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
			
			if (customSession.getPlatformId() < 1 || customSession.getAppId() < 1)
			{
				responseCode = HttpStatus.BAD_REQUEST.value();
				meLogger.warn("GetUserAppMarkSession request failed because no platform/app was setup, User:" + profileName);
				return null;
			}
			
			CustomResponse customResponse = new CustomResponse();
			UserApp userApp = accountService.GetUserApp(customSession.getUserId(), customSession.getAppId(), customSession.getPlatformId(), userAppId, customResponse);
			
			synchronized(customSession) {
				customSession.setUserAppId(userApp.getId());
			}
			
			responseCode = customResponse.getResponseCode();
			return userApp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally { UserTools.LogWebMethod("GetUserAppMarkSession", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
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
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			Thread.sleep(500);
			
			String profileReturn = "USED";
			CustomResponse customResponse = new CustomResponse();
			if (accountService.CheckProfileNameIsUnique(profileName, customResponse))
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
		finally { UserTools.LogWebMethod("CheckProfileName", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	// PUT /{profileName}/clientapp
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
			int appId = accountService.VerifyApp(clientApp, customResponse);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The application key failed validation.");
				responseCode = customResponse.getResponseCode();
				return;
			}
							
			int platformId = accountService.GetPlatformId(clientApp, customResponse);
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
		finally { UserTools.LogWebMethod("SetClientApp", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
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
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			CustomResponse customResponse = new CustomResponse();
			accountService.VerifyApp(clientApp, customResponse);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The application key failed validation.");
				responseCode = customResponse.getResponseCode();
				Thread.sleep(500);
				return;
			}
	
			accountService.GetPlatformId(clientApp, customResponse);
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
		finally { UserTools.LogWebMethod("CheckClientApp", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//POST /logontoken
	@RequestMapping(value="/logontoken", method=RequestMethod.POST, produces=MediaType.APPLICATION_XML_VALUE,
			consumes = MediaType.APPLICATION_XML_VALUE, headers={"Accept-Charset=utf-8"} )
	public @ResponseBody Logon GetLogonToken(
			@RequestBody Logon logon,
			HttpServletRequest request, 
			HttpServletResponse response)
	{	
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		Logon responseLogon = new Logon();
		try
		{
			Thread.sleep(300);
			response.addHeader("Cache-Control", "no-cache");
			
			//TODO add remote address to the DB, to check for other sessions, from other IPs coming in.
			
			if (logon == null)
			{
				meLogger.warn("Logon request made, but no logon object submitted");
				responseCode = HttpStatus.BAD_REQUEST.value();
				return null;
			}
			
			HttpSession tomcatSession = request.getSession(true);
			
			CustomSessionState customSession = (CustomSessionState)tomcatSession.getAttribute("CustomSessionState");
			if (customSession == null)
			{
				customSession = new CustomSessionState();
				tomcatSession.setAttribute("CustomSessionState", customSession);
			}
			
			CustomResponse customResponse = new CustomResponse();
			String key = accountService.GetLogonToken(logon, request, customSession, customResponse);
			responseCode = customResponse.getResponseCode();
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				responseLogon.setKey(key);
				responseLogon.setProfileName(customSession.getProfileName());
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
		finally { UserTools.LogWebMethod("GetLogonToken", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
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
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
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
			
			if (logon == null)
			{
				meLogger.warn("Logon request made, but no logon object submitted");
				responseCode = HttpStatus.BAD_REQUEST.value();
				return;
			}
			
			if (accountService.LogonCheck(logon, request, customSession))
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
		finally { UserTools.LogWebMethod("Logon", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//POST /logout
	@RequestMapping(value = { "/logout" }, method = { RequestMethod.POST }, 
			headers={"Accept-Charset=utf-8"})
	public void Logout(
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
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
		finally { UserTools.LogWebMethod("Logout", meLogger, startMS, request, responseCode); response.setStatus(responseCode); }
	}
	
	//TODO Change Password.
	
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
					" UserAppId:" + customSession.getUserAppId() +
					" LogonKey:" + customSession.getNonceKey() +
					" Authenticated:" + customSession.isAuthenticated();
		}
		catch (Exception ex) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return "";
		}
	}
}
