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
	private final String urlPRefix = "/v1";
	
	
	@Autowired
	private AccountService accountService;
	
	@Autowired
	private GalleryService galleryService;
	
	@RequestMapping(value="/start", method=RequestMethod.GET)
	public String StartGet(
			HttpServletResponse response)
	{
		response.setStatus(HttpStatus.OK.value());
		return "x/start";
	}
	
	@RequestMapping(value="/logout", method=RequestMethod.GET)
	public String LogoutGet(
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		String defaultMessage = "Logout encountered an unexpected issue.";
		String responseJsp = "x/generalerror";
		
		long startMS = System.currentTimeMillis();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			HttpSession tomcatSession = request.getSession(false);
			if (tomcatSession == null)
				meLogger.warn("Logout request made, but no Tomcat session has been established.");
			else
				tomcatSession.invalidate();

			return "x/logout";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { UserTools.LogWebMethod("LogoutGet", meLogger, startMS, request, responseCode); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/newaccount", method=RequestMethod.GET)
	public String NewAccountGet(
			@RequestParam(value="accountType", required=true) int accountType,
			NewAccount newAccount,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Profile could be setup at this time.";
		String responseJsp = "x/generalerror";
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
				newAccount.setKey(key);
				newAccount.setAccountType(accountType);
				responseJsp = "x/newaccount";
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
		finally { UserTools.LogWebFormMethod("NewAccountGet", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/newaccount", method=RequestMethod.POST)
	public String NewAccountPost(
			@Valid NewAccount newAccount, 
			BindingResult bindingResult,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		//int responseCode = HttpStatus.OK.value();
		long startMS = System.currentTimeMillis();
		String responseJsp = "x/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "x/newaccount";
			}
			else
			{
				Account account = new Account();
				account.setProfileName(newAccount.getProfileName());
				account.setEmail(newAccount.getEmail());
				account.setDesc(newAccount.getDescription());
				account.setPassword(newAccount.getPassword());
				account.setAccountType(newAccount.getAccountType());
				account.setKey(newAccount.getKey());
				
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
						
						responseJsp = "redirect:" + account.getProfileName() + "/accountsummary";
					}
					else if (customResponse.getResponseCode() == HttpStatus.BAD_REQUEST.value())
					{			
						String key = accountService.GetNewUserToken(request, customSession, customResponse);

						if (customResponse.getResponseCode() == HttpStatus.OK.value())
						{
							newAccount.setKey(key);
							responseJsp = "x/newaccount";
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
			model.addAttribute("message", "Unexpected error, no account could be setup at this time.");
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("NewAccountPost", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
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
		String responseJsp = "x/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
		
			if (message != null)
				model.addAttribute("message", message);
			
			if (referrer != null)
				model.addAttribute("referrer", referrer);
			
			//TODO add remote address to the DB, to check for other sessions, from other IPs coming in.
			//logon = new Logon();
			responseJsp = "x/logon";
			
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
			@RequestParam(value="referrer", required=false) String referrer,
			@Valid Logon logon, 
			BindingResult bindingResult,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Logon could not be processed at this time.";
		String responseJsp = "x/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "x/logon";
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
						if (referrer == null)
							responseJsp = "redirect:" + customSession.getProfileName() + "/accountsummary";
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
						responseJsp = "x/logon";
					}
				}
				else if (customResponse.getResponseCode() == HttpStatus.BAD_REQUEST.value())
				{
					Thread.sleep(1000);
					model.addAttribute("message", "Logon failed, please check your details and try again");
					responseJsp = "x/logon";
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
	
	@RequestMapping(value="/{profileName}/accountsummary", method=RequestMethod.GET)
	public String AccountSummaryGet(
			@PathVariable("profileName") String profileName,
			AccountSummary accountSummary,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Account summary could not be retrieved at this time.";
		String responseJsp = "x/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				String message = "Account summary request not authorised.";
				meLogger.warn(message);
				String path = new UrlPathHelper().getPathWithinApplication(request);
				
				return "redirect:../logon?referrer=" 
					+ UriUtils.encodePath(path,"UTF-8") 
					+ "&message=" + UserTools.EncodeString(message, request);
			}
			
			Account account = accountService.GetAccount(customSession.getUserId(), customResponse);
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				accountSummary.setEmail(account.getEmail());
				accountSummary.setDescription(account.getDesc());
				accountSummary.setAccountTypeName(account.getAccountTypeName());
				accountSummary.setProfileName(account.getProfileName());
				accountSummary.setVersion(account.getVersion());
				accountSummary.setId(account.getId());

				responseJsp = "x/accountsummary";
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
		finally { UserTools.LogWebFormMethod("AccountSummaryGet", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	
	@RequestMapping(value="/{profileName}/accountsummary", method=RequestMethod.POST)
	public String AccountSummaryPost(
			@Valid AccountSummary accountSummary, 
			BindingResult bindingResult,
			Model model,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Account summary could not be updated at this time.";
		String responseJsp = "x/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "x/accountsummary";
			}
			else
			{
				CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
				if (customSession == null)
				{
					String message = "Account update failed, your session has ended.  Please login again.";
					meLogger.warn(message);
					responseJsp = "redirect:../logon?message=" + UserTools.EncodeString(message, request);
				}
				else
				{
					if (!customSession.getProfileName().equalsIgnoreCase(accountSummary.getProfileName())
							|| customSession.getUserId() != accountSummary.getId())
					{
						String message = "Account update failed, an invalid action was requested.  Your session has been closed.";
						meLogger.warn(message);
						responseJsp = "redirect:../logon?message=" + UserTools.EncodeString(message, request);
					}
					else
					{
						CustomResponse customResponse = new CustomResponse();
						Account account = new Account();
						account.setId(accountSummary.getId());
						account.setVersion(accountSummary.getVersion());
						account.setDesc(accountSummary.getDescription());
						
						int updateStatus = accountService.UpdateAccount(account);
						
						account = accountService.GetAccount(customSession.getUserId(), customResponse);
						accountSummary.setEmail(account.getEmail());
						accountSummary.setDescription(account.getDesc());
						accountSummary.setAccountTypeName(account.getAccountTypeName());
						accountSummary.setProfileName(account.getProfileName());
						accountSummary.setVersion(account.getVersion());
						accountSummary.setId(account.getId());
						
						responseJsp = "x/accountsummary";
						
						if (updateStatus == HttpStatus.OK.value())
							model.addAttribute("message", "Updated!");
						else
							model.addAttribute("message", "Update failed, there was an error on the server");
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
		finally { UserTools.LogWebFormMethod("AccountSummaryPost", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/gallery/{galleryName}/logon", method=RequestMethod.GET)
	public String GalleryLogonGet(
			@PathVariable("profileName") String profileName,
			@PathVariable("galleryName") String galleryName,
			@RequestParam(value="referrer", required=false) String referrer,
			@RequestParam(value="message", required=false) String message,
			GalleryLogon galleryLogon,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Gallery logon could not be processed at this time.  Please try later.";
		String responseJsp = "x/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
		
			if (message != null)
				model.addAttribute("message", message);
			
			if (referrer != null)
				model.addAttribute("referrer", referrer);
			
			//TODO add remote address to the DB, to check for other sessions, from other IPs coming in.
			
			//Check for existing session.
			CustomSessionState customSession = null;
			try
			{
				customSession = UserTools.GetGallerySessionNoAuth(profileName, galleryName, request, meLogger);
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
				//Existing session is not valid, so start again.
				HttpSession tomcatSession = request.getSession(false);
				if (tomcatSession != null)
					tomcatSession.invalidate();
				
				tomcatSession = request.getSession(true);
				customSession = new CustomSessionState();
				tomcatSession.setAttribute("CustomSessionState", customSession);
			}
			
			CustomResponse customResponse = new CustomResponse();
			String key = galleryService.GetGalleryUserLogonToken(profileName, galleryName, request, customSession, customResponse);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				galleryLogon.setProfileName(profileName);
				galleryLogon.setGalleryName(galleryName);
				galleryLogon.setKey(key);
				responseJsp = "x/gallerylogon";
				
			}
			else if (customResponse.getResponseCode() == HttpStatus.FORBIDDEN.value())
			{
				Thread.sleep(10000);
				model.addAttribute("message", "Request failed security checks when issuing a token.");
			}
			else
			{
				Thread.sleep(2000);
				model.addAttribute("message", defaultMessage);
			}

			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("GalleryLogonGet", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/gallery/{galleryName}/logon", method=RequestMethod.POST)
	public String GalleryLogonPost(
			@RequestParam(value="referrer", required=true) String referrer,
			@Valid GalleryLogon galleryLogon, 
			BindingResult bindingResult,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String defaultMessage = "Gallery logon could not be processed at this time.";
		String responseJsp = "x/generalerror";
		String message = "";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "x/gallerylogon";
			}
			else
			{
				//Get the valid session, using the key.
				//TOTOTOTOTO   DODODODODOD
				
				
				CustomSessionState customSession = new CustomSessionState();
				
				
				
				CustomResponse customResponse = new CustomResponse();
				
				galleryService.LoginGalleryUser(false, 1, "", "", galleryLogon.getPassword(), galleryLogon.getProfileName(), galleryLogon.getGalleryName(), request, customSession, customResponse);
				
				if (customResponse.getResponseCode() == HttpStatus.OK.value())
				{
					meLogger.debug("View gallery authorised.  User:" + customSession.getProfileName() + " Gallery:" + customSession.getGalleryName());
				}
				else
				{
					if (customResponse.getResponseCode() == HttpStatus.UNAUTHORIZED.value())
						message = "Logon failed.  Please check your password and try again.";
					else
						message = "Gallery login had an error and cannot continue.";
					
					meLogger.warn(message);
					model.addAttribute("errorMessage", message);
					return responseJsp;
				}
				
				if (customResponse.getResponseCode() == HttpStatus.OK.value())
				{
					responseJsp = "redirect:" + referrer;
					
					Cookie wallaSessionIdCookie = new Cookie("X-Walla-Id", UserTools.GetLatestWallaId(customSession));
					wallaSessionIdCookie.setPath("/wallahub/");
					response.addCookie(wallaSessionIdCookie);
				}
				else
				{
					Thread.sleep(1000);
					model.addAttribute("message", "Logon failed, please check your details and try again");
					responseJsp = "x/logon";
				}
				
			}
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { UserTools.LogWebFormMethod("GalleryLogonPost", meLogger, startMS, request, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
}
