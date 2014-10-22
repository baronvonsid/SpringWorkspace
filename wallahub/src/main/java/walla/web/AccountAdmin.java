package walla.web;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.*;

import walla.datatypes.java.*;
import walla.datatypes.web.*;
import walla.datatypes.auto.*;
import walla.business.*;
import walla.utils.*;

@Controller
@RequestMapping("web")
public class AccountAdmin extends WebMvcConfigurerAdapter {

	private static final Logger meLogger = Logger.getLogger(AccountAdmin.class);
	
	@Autowired
	private AccountService accountService;
	
	
	//@Override
	//public void addViewControllers(ViewControllerRegistry registry)
	//{
	//	registry.addViewController("/results").setViewName("results");
	//}
	
	@RequestMapping(value="/newprofile", method=RequestMethod.GET)
	public String NewProfileGet(
			NewProfile newProfile,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Profile could be setup at this time.";
		String responseJsp = "generalerror";
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

			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				newProfile.setKey(key);
				responseJsp = "newprofile";
			}
			else
			{
				model.addAttribute("message", defaultMessage);
			}
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("NewProfileGet", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/newprofile", method=RequestMethod.POST)
	public String NewProfilePost(
			@Valid NewProfile newProfile, 
			BindingResult bindingResult,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		//int responseCode = HttpStatus.OK.value();
		long startMS = System.currentTimeMillis();
		String responseJsp = "generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "newprofile";
			}
			else
			{
				Account account = new Account();
				account.setProfileName(newProfile.getProfileName());
				account.setEmail(newProfile.getEmail());
				account.setDesc(newProfile.getDescription());
				account.setPassword(newProfile.getPassword());
				account.setAccountType(newProfile.getAccountType());
				account.setKey(newProfile.getKey());
				
				CustomSessionState customSession = UserTools.CheckNewUserSession(account, request, meLogger);
				if (customSession != null)
				{
					CustomResponse customResponse = new CustomResponse();
					accountService.CreateAccount(account, customResponse, customSession);
					if (customResponse.getResponseCode() == HttpStatus.CREATED.value())
					{
						Cookie wallaSessionIdCookie = new Cookie("X-Walla-Id", UserTools.GetLatestWallaId(customSession));
						wallaSessionIdCookie.setPath("/wallahub/");
						response.addCookie(wallaSessionIdCookie);
						
						responseJsp = "redirect:" + account.getProfileName() + "/profilesummary";
					}
					else if (customResponse.getResponseCode() == HttpStatus.BAD_REQUEST.value())
					{			
						String key = accountService.GetNewUserToken(request, customSession, customResponse);

						if (customResponse.getResponseCode() == HttpStatus.OK.value())
						{
							newProfile.setKey(key);
							responseJsp = "newprofile";
						}
						model.addAttribute("message", "Request failed, reason - " + customResponse.getMessage());
					}
					else
					{
						model.addAttribute("message", "Unexpected error, no profile could be setup at this time.");
					}
				}
				else
				{
					model.addAttribute("message", "Current session failed validation for new user creation.");
				}
			}
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", "Unexpected error, no profile could be setup at this time.");
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("NewProfilePost", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/logon", method=RequestMethod.GET)
	public String LogonGet(
			@RequestParam(value="referrer", required=false) String referrer,
			@RequestParam(value="message", required=false) String message,
			Logon logon,
			Model model,
			BindingResult bindingResult,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Logon could not be processed at this time.";
		String responseJsp = "generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
		
			if (message != null)
				model.addAttribute("message", message);
			
			if (referrer != null)
				model.addAttribute("referrer", referrer);
			
			//TODO add remote address to the DB, to check for other sessions, from other IPs coming in.
			logon = new Logon();
			responseJsp = "logon";
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("LogonGet", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/logon", method=RequestMethod.POST)
	public String LogonPost(
			@Valid Logon logon, 
			BindingResult bindingResult,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Logon could not be processed at this time.";
		String responseJsp = "generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "logon";
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
				
				CustomResponse customResponse = new CustomResponse();
				
				String key = accountService.GetLogonToken(logon, request, customSession, customResponse);
				
				if (customResponse.getResponseCode() == HttpStatus.OK.value())
				{
					logon.setKey(key);
					if (accountService.LogonCheck(logon, request, customSession))
					{
						Map modelVals = model.asMap();
						String referrer = (String)modelVals.get("referrer");
						
						if (referrer == null || referrer.length() < 1)
							responseJsp = "redirect:" + logon.getProfileName() + "/profilesummary";
						else
							responseJsp = "redirect:" + referrer;
						
						Cookie wallaSessionIdCookie = new Cookie("X-Walla-Id", UserTools.GetLatestWallaId(customSession));
						wallaSessionIdCookie.setPath("/wallahub/");
						response.addCookie(wallaSessionIdCookie);
					}
					else
					{
						Thread.sleep(1000);
						model.addAttribute("message", "Logon failed, please check your details and try again");
						responseJsp = "logon";
					}
				}
				else if (customResponse.getResponseCode() == HttpStatus.BAD_REQUEST.value())
				{
					Thread.sleep(1000);
					model.addAttribute("message", "Logon failed, please check your details and try again");
					responseJsp = "logon";
				}
				else
				{
					model.addAttribute("message", "Logon failed, due to an invalid action");
				}
				
			}
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("LogonPost", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/profilesummary", method=RequestMethod.GET)
	public String ProfileSummaryGet(
			@PathVariable("profileName") String profileName,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Profile summary could not be retrieved at this time.";
		String responseJsp = "generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				String message = "Account summary request not authorised.";
				meLogger.warn(message);

				return "redirect:../logon?referrer=" 
					+ UriUtils.encodePath(request.getRequestURI(),"UTF-8") 
					+ "&message=" + EncodeString(message, request);
			}
			
			Account account = accountService.GetAccount(customSession.getUserId(), customResponse);
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				model.addAttribute(account);
				responseJsp = "profilesummary";
			}
			else
			{
				model.addAttribute("message", defaultMessage);
			}
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("ShowProfileSummary", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	
	@RequestMapping(value="/{profileName}/profilesummary", method=RequestMethod.POST)
	public String ProfileSummaryPost(
			@Valid Account account, 
			BindingResult bindingResult,
			Model model,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Profile summary could not be updated at this time.";
		String responseJsp = "generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			//Account simon = (Account)model.asMap().get("account");
			if (bindingResult.hasErrors())
			{
				responseJsp = "profilesummary";
			}
			else
			{
				CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
				if (customSession == null)
				{
					String message = "Account update failed, your session has ended.  Please login again.";
					meLogger.warn(message);
					responseJsp = "redirect:../logon?message=" + EncodeString(message, request);
				}
				else
				{
					if (!customSession.getProfileName().equalsIgnoreCase(account.getProfileName())
							|| customSession.getUserId() != account.getId())
					{
						String message = "Account update failed, an invalid action was requested.  Your session has been closed.";
						meLogger.warn(message);
						responseJsp = "redirect:../logon?message=" + EncodeString(message, request);
					}
					else
					{
						CustomResponse customResponse = new CustomResponse();
						if (accountService.UpdateAccount(account) == HttpStatus.OK.value())
						{
							account = accountService.GetAccount(customSession.getUserId(), customResponse);
							model.addAttribute("message", "Updated!");
							responseJsp = "profilesummary";
						}
						else
						{
							account = accountService.GetAccount(customSession.getUserId(), customResponse);
							model.addAttribute("message", "Update failed, there was an error on the server");
							responseJsp = "profilesummary";
						}
					}
				}
			}
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", "Unexpected error, account could not be updated at this time.");
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("ProfileSummaryPost", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	private String EncodeString(String string, HttpServletRequest request) throws UnsupportedEncodingException
	{
		String enc=request.getCharacterEncoding();
		if (enc == null)
		    enc=WebUtils.DEFAULT_CHARACTER_ENCODING;
		
		return UriUtils.encodeQueryParam(string, enc);
	}
}
