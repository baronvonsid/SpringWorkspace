package walla.web;

import java.io.UnsupportedEncodingException;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
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
	private final String urlPrefix = "/v1/web";
	
	
	@Resource(name="accountServicePooled")
	private AccountService accountService;
	
	@Resource(name="galleryServicePooled")
	private GalleryService galleryService;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	@RequestMapping(value="/start", method=RequestMethod.GET)
	public String StartGet(
			HttpServletResponse response)
	{
		response.setStatus(HttpStatus.OK.value());
		return "webapp/start";
	}
	
	@RequestMapping(value="/forgotpassword", method=RequestMethod.GET)
	public String ForgotPasswordGet(
			HttpServletResponse response)
	{
		response.setStatus(HttpStatus.OK.value());
		return "webapp/forgotpassword";
	}
	
	@RequestMapping(value="/logout", method=RequestMethod.GET)
	public String LogoutGet(
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		String defaultMessage = "Logout encountered an unexpected issue.";
		String responseJsp = "webapp/generalerror";

		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		int responseCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			HttpSession tomcatSession = request.getSession(false);
			if (tomcatSession == null)
				meLogger.warn("Logout request made, but no Tomcat session has been established.");
			else
				tomcatSession.invalidate();

			return "webapp/logout";
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "LogoutGet", startMS, request, requestId, String.valueOf(responseCode)); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/newaccount", method=RequestMethod.GET)
	public String NewAccountGet(
			@RequestParam(value="accountType", required=true) int accountType,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Profile could be setup at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			Thread.sleep(300);
			response.addHeader("Cache-Control", "no-cache");
			
			//Check for existing session.
			CustomSessionState customSession = null;
			try
			{
				customSession =  UserTools.GetInitialAdminSession(request, meLogger);
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
			accountService.SetAppAndPlatformWeb(2, request, customSession, customResponse, requestId);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The application/platform key failed validation.");
				Thread.sleep(500);
				model.addAttribute("message", "Logon failed application\browser checks when issuing a token.");
				return responseJsp;
			}
			
			//A warning might be displayed here.
			if (customResponse.getMessage().length() > 0)
				model.addAttribute("message", customResponse.getMessage());

			customResponse = new CustomResponse();
			String key = accountService.GetNewUserToken(request, customSession, customResponse, requestId);

			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				NewAccount newAccount = new NewAccount();
				newAccount.setKey(key);
				newAccount.setAccountType(accountType);
				model.addAttribute("newAccount", newAccount);
				responseJsp = "webapp/newaccount";
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
		finally { utilityService.LogWebMethod("AccountAdmin", "NewAccountGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
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
		String requestId = UserTools.GetRequestId();
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "webapp/newaccount";
			}
			else
			{
				Account account = new Account();
				account.setProfileName(newAccount.getProfileName());
				account.setDesc(newAccount.getDescription());
				account.setPassword(newAccount.getPassword());
				account.setAccountType(newAccount.getAccountType());
				account.setKey(newAccount.getKey());
				account.setCountry(newAccount.getCountry());
				account.setNewsletter(true);
				
				Account.Emails.EmailRef emailRef = new Account.Emails.EmailRef();
				emailRef.setAddress(newAccount.getEmail());
				emailRef.setPrinciple(true);
				emailRef.setSecondary(false);
				emailRef.setVerified(false);
				account.setEmails(new Account.Emails());
				account.getEmails().getEmailRef().add(emailRef);
				
				CustomSessionState customSession = UserTools.CheckNewUserSession(account, request, meLogger);
				if (customSession != null)
				{
					CustomResponse customResponse = new CustomResponse();
					accountService.CreateAccount(account, customResponse, customSession, requestId);
					if (customResponse.getResponseCode() == HttpStatus.CREATED.value())
					{
						UserApp userApp = accountService.GetUserAppWeb(customSession.getUserId(), customSession.getAppId(), customSession.getPlatformId(), customResponse, requestId);
						if (userApp != null)
						{
							synchronized(customSession) {
								customSession.setUserApp(userApp);
							}
						}
						else
						{
							model.addAttribute("message", "Unexpected error, profile could not be logged on at this time.");
						}
						
						
						Cookie wallaSessionIdCookie = new Cookie("X-Walla-Id", UserTools.GetLatestWallaId(customSession));
						wallaSessionIdCookie.setPath("/wallahub/");
						response.addCookie(wallaSessionIdCookie);
						
						responseJsp = "redirect:" + customSession.getProfileName() + "/settings/account";
					}
					else if (customResponse.getResponseCode() == HttpStatus.BAD_REQUEST.value())
					{			
						String key = accountService.GetNewUserToken(request, customSession, customResponse, requestId);

						if (customResponse.getResponseCode() == HttpStatus.OK.value())
						{
							newAccount.setKey(key);
							responseJsp = "webapp/settings/account";
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
		finally { utilityService.LogWebMethod("AccountAdmin", "NewAccountPost", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
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
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Logon could not be processed at this time.";
		String responseJsp = "webapp/generalerror";

		try
		{
			response.addHeader("Cache-Control", "no-cache");
		
			if (message != null)
				model.addAttribute("message", message);
			
			if (referrer != null)
				model.addAttribute("referrer", referrer);
			
			
			
			//TODO add remote address to the DB, to check for other sessions, from other IPs coming in.
			//TODO Check for existing admin session, if so, then redirect to logout.

			//Check for existing session.
			CustomSessionState customSession = null;
			try
			{
				customSession =  UserTools.GetInitialAdminSession(request, meLogger);
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
			accountService.SetAppAndPlatformWeb(2, request, customSession, customResponse, requestId);
			if (customResponse.getResponseCode() != HttpStatus.OK.value())
			{
				meLogger.warn("The application/platform key failed validation.");
				Thread.sleep(500);
				model.addAttribute("message", "Logon failed application\browser checks when issuing a token.");
				return responseJsp;
			}
			
			//A warning might be displayed here.
			if (customResponse.getMessage().length() > 0)
				logon.setMessage(customResponse.getMessage());
			
			customResponse = new CustomResponse();
			String key = accountService.GetLogonToken(request, customSession, customResponse, requestId);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				logon.setKey(key);
				responseJsp = "webapp/logon";
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
		finally { utilityService.LogWebMethod("AccountAdmin", "LogonGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
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
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Logon could not be processed at this time.";
		String responseJsp = "webapp/generalerror";
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
				responseJsp = RedirectToLogon("Request failed security checks.",request);
				return responseJsp;
			}
			
			//If not valid session, then this must start again.
			if (customSession == null)
			{
				//Existing session is not valid, so start again.
				Thread.sleep(2000);
				responseJsp = RedirectToLogon("Request failed security checks.",request);
				return responseJsp;
			}
			
			CustomResponse customResponse = new CustomResponse();
			accountService.LogonCheck(logon, request, customSession, customResponse, requestId);
			
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				UserApp userApp = accountService.GetUserAppWeb(customSession.getUserId(), customSession.getAppId(), customSession.getPlatformId(), customResponse, requestId);
				if (userApp != null)
				{
					synchronized(customSession) {
						customSession.setUserApp(userApp);
					}
				}
				else
				{
					String message = customResponse.getMessage() == null ? "Logon failed, internal error." : customResponse.getMessage();
					responseJsp = RedirectToLogonMaintainReferrer(message,request, referrer);
				}
				
				if (referrer == null || referrer.length() < 1)
					responseJsp = "redirect:" + customSession.getProfileName() + "/settings/account";
				else
					responseJsp = "redirect:" + referrer;
				
				Cookie wallaSessionIdCookie = new Cookie("X-Walla-Id", UserTools.GetLatestWallaId(customSession));
				wallaSessionIdCookie.setPath("/wallahub/");
				response.addCookie(wallaSessionIdCookie);
			}
			else
			{
				String message = customResponse.getMessage() == null ? "Logon failed, please check your details and try again" : customResponse.getMessage();
				responseJsp = RedirectToLogonMaintainReferrer(message,request, referrer);
			}
				
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "LogonPost", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/settings/account", method=RequestMethod.GET)
	public String SettingsAccountGet(
			@PathVariable("profileName") String profileName,
			@RequestParam(value="message", required=false) String message,
			@RequestParam(value="logonToken", required=false) String token,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Account settings could not be retrieved at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				String tempToken = (token == null) ? "" : token;
				if (tempToken.length() == 28)
				{
					try
					{
						customSession =  UserTools.GetInitialAdminSession(request, meLogger);
					}
					catch (WallaException wallaEx)
					{
						//Unexpected action,  forbidden.
						Thread.sleep(10000);
						responseJsp = RedirectToLogon("Account settings request not authorised.", request);
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
					
					customResponse = new CustomResponse();
					accountService.SetAppAndPlatformWeb(2, request, customSession, customResponse, requestId);
					if (customResponse.getResponseCode() != HttpStatus.OK.value())
					{
						meLogger.warn("The application/platform key failed validation.");
						message = (customResponse.getMessage() == null ? "Logon failed application\browser checks when issuing a token." : customResponse.getMessage());
						responseJsp = RedirectToLogon(message, request);
						return responseJsp;
					}
					
					accountService.AutoLoginAdminUser(tempToken, profileName, request, customSession, customResponse, requestId);
					if (customResponse.getResponseCode() == HttpStatus.OK.value())
					{						
						Cookie wallaSessionIdCookie = new Cookie("X-Walla-Id", UserTools.GetLatestWallaId(customSession));
						wallaSessionIdCookie.setPath("/wallahub/");
						response.addCookie(wallaSessionIdCookie);
					}
					else
					{
						message = (customResponse.getMessage() == null ? "Account settings request not authorised." : customResponse.getMessage());
						responseJsp = RedirectToLogon(message, request);
						return responseJsp;
					}
				}
				else
				{
					responseJsp = RedirectToLogon("Account settings request not authorised.", request);
					return responseJsp;
				}
			}
			
			if (customSession.getAccount() == null)
			{
				Account account = accountService.GetAccountMeta(customSession.getUserId(), customResponse, requestId);
				if (customResponse.getResponseCode() != HttpStatus.OK.value())
				{
					model.addAttribute("message", defaultMessage);
					return responseJsp;
				}
				customSession.setAccount(account);
			}
			
			//MergeSettingsToAccount(customSession.getAccount(), accountSettings);

			if (message != null)
				model.addAttribute("message", message);
			
			model.addAttribute("account", customSession.getAccount());
			responseJsp = "webapp/settings/account";
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsAccountGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/settings/account", method=RequestMethod.POST)
	public String SettingsAccountPost(
			@Valid Account account,
			@ModelAttribute("action") String action,
			BindingResult bindingResult,
			Model model,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Account summary could not be updated at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "webapp/settings/account";
			}
			else
			{
				CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
				if (customSession == null)
				{
					responseJsp = RedirectToLogon("Account settings update failed, your session has ended.  Please login again.", request);
					return responseJsp;
				}
				else
				{
					if (!customSession.getProfileName().equalsIgnoreCase(account.getProfileName())
							|| customSession.getUserId() != account.getId())
					{
						responseJsp = RedirectToLogon("Account settings update failed, an invalid action was requested.  Your session has been closed.", request);
						return responseJsp;
					}
					else
					{
						CustomResponse customResponse = new CustomResponse();
						if (action.compareTo("CloseAccount") == 0)
						{
							/*
							Account account = new Account();
							account.setId(customSession.getUserId());
							account.setProfileName(customSession.getProfileName());
							account.setVersion(accountSettings.getVersion());
							account.setPassword(accountSettings.getCurrentPassword());
							*/
							
							accountService.CloseAccount(account, customResponse, customSession, requestId);
						}
						else
						{
							/*
							Account account = new Account();
							account.setId(accountSettings.getId());
							account.setVersion(accountSettings.getVersion());
							account.setDesc(accountSettings.getDescription());
							account.setCountry(accountSettings.getCountry());
							account.setNewsletter(accountSettings.getNewsletter());
							*/
							
							accountService.UpdateAccount(account, customResponse, requestId);
						}

						
						if (customResponse.getResponseCode() == HttpStatus.OK.value())
						{
							responseJsp = "redirect:" + urlPrefix + "/" + customSession.getProfileName() + "/settings/account";
							model.addAttribute("message", "Account updated.");
							customSession.setAccount(null);
						}
						else
						{
							//MergeSettingsToAccount(customSession.getAccount(), accountSettings);
							
							if (customResponse.getMessage() == null)
								model.addAttribute("message", "Account settings update failed, there was an error on the server");
							else
								model.addAttribute("message", customResponse.getMessage());
							
							model.addAttribute("account", customSession.getAccount());
							responseJsp = "webapp/settings/account";
						}
					}
				}
			}
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsAccountPost", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/settings/security", method=RequestMethod.GET)
	public String SettingsSecurityGet(
			@PathVariable("profileName") String profileName,
			@RequestParam(value="message", required=false) String message,
			Logon logon,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Security settings could not be retrieved at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseJsp = RedirectToLogon("Security settings request not authorised.", request);
				return responseJsp;
			}
			
			if (customSession.getAccount() == null)
			{
				Account account = accountService.GetAccountMeta(customSession.getUserId(), customResponse, requestId);
				if (customResponse.getResponseCode() != HttpStatus.OK.value())
				{
					model.addAttribute("message", defaultMessage);
					return responseJsp;
				}
				customSession.setAccount(account);
			}
			
			//Convert from internal object to web facing object.
			logon.setProfileName(customSession.getAccount().getProfileName());
			
			//TODO ascertain what security message should go here.
			//logon.setSecurityMessage(customSession.getAccount().getSecurityMessage());

			if (message != null)
				model.addAttribute("message", message);
			
			responseJsp = "webapp/settings/security";

			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsSecurityGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/settings/security", method=RequestMethod.POST)
	public String SettingsSecurityPost(
			Logon logon,
			BindingResult bindingResult,
			Model model,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Security settings could not be updated at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "webapp/settings/security";
				return responseJsp;
			}

			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseJsp = RedirectToLogon("Security settings request not authorised.", request);
				return responseJsp;
			}
		
			if (!customSession.getProfileName().equalsIgnoreCase(logon.getProfileName()))
			{
				responseJsp = RedirectToLogon("Security settings request not authorised, an invalid action was requested.", request);
				return responseJsp;
			}
			
			CustomResponse customResponse = new CustomResponse();
			accountService.ChangePassword(logon, request, customResponse, customSession, requestId);

			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				responseJsp = "redirect:" + urlPrefix + "/" + customSession.getProfileName() + "/settings/security";
				model.addAttribute("message", "Security details updated successfully.");
			}
			else
			{
				if (customResponse.getMessage() == null)
					model.addAttribute("message", "Security settings update failed, there was an error on the server");
				else
					model.addAttribute("message", customResponse.getMessage());
				
				responseJsp = "webapp/settings/security";
			}
			
			customSession.setAccount(null);
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsSecurityPost", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}	

	@RequestMapping(value="/{profileName}/settings/contact", method=RequestMethod.GET)
	public String SettingsContactGet(
			@PathVariable("profileName") String profileName,
			@RequestParam(value="message", required=false) String message,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Contact settings could not be retrieved at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseJsp = RedirectToLogon("Contact settings request not authorised.", request);
				return responseJsp;
			}
			
			if (customSession.getAccount() == null)
			{
				Account account = accountService.GetAccountMeta(customSession.getUserId(), customResponse, requestId);
				if (customResponse.getResponseCode() != HttpStatus.OK.value())
				{
					model.addAttribute("message", defaultMessage);
					return responseJsp;
				}
				customSession.setAccount(account);
			}

			if (message != null)
				model.addAttribute("message", message);

			model.addAttribute("account", customSession.getAccount());

			responseJsp = "webapp/settings/contact";
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsContactGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/settings/contact", method=RequestMethod.POST)
	public String SettingsContactPost(
			@Valid Account account,
			@ModelAttribute("action") String action,
			@ModelAttribute("actionEmail") String actionEmail,
			@PathVariable("profileName") String profileName,
			BindingResult bindingResult,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Account contacts could not be updated at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "webapp/settings/contact";
			}
			else
			{
				CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
				if (customSession == null)
				{
					responseJsp = RedirectToLogon("Account settings update failed, your session has ended.  Please login again.", request);
					return responseJsp;
				}
				else
				{
					if (!customSession.getProfileName().equalsIgnoreCase(account.getProfileName())
							|| customSession.getUserId() != account.getId())
					{
						responseJsp = RedirectToLogon("Account settings update failed, an invalid action was requested.  Your session has been closed.", request);
						return responseJsp;
					}
					else
					{
						CustomResponse customResponse = new CustomResponse();
						switch (action)
						{
							case "AddEmail":
								accountService.AddEmail(customSession.getUserId(), actionEmail, customResponse, requestId);
								break;
								
							case "SetPrinciple":
								accountService.UpdateEmailAction(customSession.getUserId(), actionEmail, EmailAction.Principle, customResponse, requestId);
								break;

							case "ResendEmail":
								accountService.VerifyEmailRequest(customSession.getUserId(), actionEmail, customResponse, requestId);
								break;
								
							case "DeleteEmail":
								accountService.UpdateEmailAction(customSession.getUserId(), actionEmail, EmailAction.Delete, customResponse, requestId);
								break;								
						}

						
						
						if (customResponse.getResponseCode() == HttpStatus.OK.value())
						{
							responseJsp = "redirect:" + urlPrefix + "/" + customSession.getProfileName() + "/settings/contact";
							model.addAttribute("message", "Contact updated.");
							customSession.setAccount(null);
							
							
						}
						else
						{
							if (customResponse.getMessage() == null)
								model.addAttribute("message", "Contact update failed, there was an error on the server");
							else
								model.addAttribute("message", customResponse.getMessage());
							
							//MergeSettingsToAccount(customSession.getAccount(), accountSettings);
							model.addAttribute("account", customSession.getAccount());
							responseJsp = "webapp/settings/contact";
						}
					}
				}

			}
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsContactPost", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/settings/billing", method=RequestMethod.GET)
	public String SettingsBillingGet(
			@PathVariable("profileName") String profileName,
			@RequestParam(value="message", required=false) String message,
			AccountSettings accountSettings,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Billing settings could not be retrieved at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseJsp = RedirectToLogon("Billing settings request not authorised.", request);
				return responseJsp;
			}
			
			if (customSession.getAccount() == null)
			{
				Account account = accountService.GetAccountMeta(customSession.getUserId(), customResponse, requestId);
				if (customResponse.getResponseCode() != HttpStatus.OK.value())
				{
					model.addAttribute("message", defaultMessage);
					return responseJsp;
				}
				customSession.setAccount(account);
			}
			
			//MergeSettingsToAccount(customSession.getAccount(), accountSettings);

			//TODO actually implement billing.
			
			if (message != null)
				model.addAttribute("message", message);
			
			responseJsp = "webapp/settings/billing";
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsBillingGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/settings/applications", method=RequestMethod.GET)
	public String SettingsApplicationsGet(
			@PathVariable("profileName") String profileName,
			//@RequestParam(value="force", required=false) Boolean force,
			@RequestParam(value="message", required=false) String message,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Application settings could not be retrieved at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseJsp = RedirectToLogon("Application settings request not authorised.", request);
				return responseJsp;
			}
			
			if (customSession.getAccountActionSummary() == null)
			{
				AccountActionSummary summary = accountService.GetAccountActions(customSession.getUserId(), customResponse, requestId);
				if (customResponse.getResponseCode() != HttpStatus.OK.value())
				{
					model.addAttribute("message", defaultMessage);
					return responseJsp;
				}
				customSession.setAccountActionSummary(summary);
			}
			
			model.addAttribute("accountActionSummary", customSession.getAccountActionSummary());
			//model.addAttribute("action", "");
			//model.addAttribute("actionUserAppId", "");
			
			if (message != null)
				model.addAttribute("message", message);
			
			responseJsp = "webapp/settings/applications";
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsApplicationsGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/settings/applications", method=RequestMethod.POST)
	public String SettingsApplicationsPost(
			@Valid @ModelAttribute("accountActionSummary") AccountActionSummary accountActionSummary,
			BindingResult bindingResult,
			Model model,
			@PathVariable("profileName") String profileName,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Account applications could not be updated at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");

			//AccountActionSummary testOne = summary;
			Map modelMap = (Map)model.asMap();
			
			AccountActionSummary testTwo = (AccountActionSummary)modelMap.get("accountActionSummary");
			String action = accountActionSummary.getAction(); //(String)modelMap.get("action");
			long actionUserAppId = accountActionSummary.getActionId(); //(String)modelMap.get("actionUserAppId");
			//long userAppId = Long.parseLong(actionUserAppId);
			boolean block = (action.compareTo("block") == 0) ? true : false;
			
			if (bindingResult.hasErrors())
			{
				responseJsp = "webapp/settings/applications";
			}
			else
			{
				CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
				if (customSession == null)
				{
					responseJsp = RedirectToLogon("Account applications update failed, your session has ended.  Please login again.", request);
					return responseJsp;
				}
				else
				{

					CustomResponse customResponse = new CustomResponse();

					accountService.UserAppBlockUnblock(customSession.getUserId(), actionUserAppId, block, customResponse, requestId);

					if (customResponse.getResponseCode() == HttpStatus.OK.value())
					{
						responseJsp = "redirect:" + urlPrefix + "/" + customSession.getProfileName() + "/settings/applications";
						model.addAttribute("message", "Applications updated.");
						customSession.setAccountActionSummary(null);
					}
					else
					{
						if (customResponse.getMessage() == null)
							model.addAttribute("message", "Applications update failed, there was an error on the server");
						else
							model.addAttribute("message", customResponse.getMessage());
						
						//MergeSettingsToAccount(customSession.getAccount(), accountSettings);
						
						responseJsp = "webapp/settings/applications";
					}
				}
			}
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsApplicationsPost", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}

	@RequestMapping(value="/{profileName}/settings/storage", method=RequestMethod.GET)
	public String SettingsStorageGet(
			@PathVariable("profileName") String profileName,
			@RequestParam(value="message", required=false) String message,
			//@RequestParam(value="force", required=false) Boolean force,
			//AccountStorage accountStorage,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Storage settings could not be retrieved at this time.";
		String responseJsp = "webapp/generalerror";
		try
		{
			response.addHeader("Cache-Control", "no-cache");
			
			CustomResponse customResponse = new CustomResponse();
			CustomSessionState customSession = UserTools.GetValidAdminSession(profileName, request, meLogger);
			if (customSession == null)
			{
				responseJsp = RedirectToLogon("Storage settings request not authorised.", request);
				return responseJsp;
			}
			
			
			if (customSession.getAccountStorage() == null) // || force)
			{
				AccountStorage storage = accountService.GetAccountStorage(customSession.getUserId(), customResponse, requestId);
				if (customResponse.getResponseCode() != HttpStatus.OK.value())
				{
					model.addAttribute("message", defaultMessage);
					return responseJsp;
				}
				customSession.setAccountStorage(storage);
			}			

			model.addAttribute("accountStorage", customSession.getAccountStorage());
			
			String yearChartData = CreateChartDataStruct(customSession.getAccountStorage(), "YEAR");
			if (yearChartData != null)
				model.addAttribute("yearChartData", yearChartData);
			
			String formatChartData = CreateChartDataStruct(customSession.getAccountStorage(), "FORMAT");
			if (formatChartData != null)
				model.addAttribute("formatChartData", formatChartData);
			
			String uploadChartData = CreateChartDataStruct(customSession.getAccountStorage(), "UPLOAD");
			if (uploadChartData != null)
				model.addAttribute("uploadChartData", uploadChartData);
			
			//MergeStorageObjects(accountStorage, customSession.getAccountStorage());

			if (message != null)
				model.addAttribute("message", message);
			
			responseJsp = "webapp/settings/storage";
			
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "SettingsStorageGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
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
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Gallery logon could not be processed at this time.  Please try later.";
		String responseJsp = "webapp/generalerror";
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
				customSession = UserTools.GetGallerySession(profileName, galleryName, true, false, request, meLogger);
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
			String key = galleryService.GetGalleryUserLogonToken(profileName, galleryName, request, customSession, customResponse, requestId);
			if (customResponse.getResponseCode() == HttpStatus.OK.value())
			{
				galleryLogon.setProfileName(profileName);
				galleryLogon.setGalleryName(galleryName);
				galleryLogon.setKey(key);
				responseJsp = "webapp/gallerylogon";
				
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
		finally { utilityService.LogWebMethod("AccountAdmin", "GalleryLogonGet", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	@RequestMapping(value="/{profileName}/gallery/{galleryName}/logon", method=RequestMethod.POST)
	public String GalleryLogonPost(
			@PathVariable("profileName") String profileName,
			@PathVariable("galleryName") String galleryName,
			@RequestParam(value="referrer", required=true) String referrer,
			@Valid GalleryLogon galleryLogon, 
			BindingResult bindingResult,
			Model model,
			HttpServletRequest request,
			HttpServletResponse response)
	{
		long startMS = System.currentTimeMillis();
		String requestId = UserTools.GetRequestId();
		String defaultMessage = "Gallery logon could not be processed at this time.";
		String responseJsp = "webapp/generalerror";
		String message = "";
		boolean failed = false;
		
		try
		{
			response.addHeader("Cache-Control", "no-cache");
						
			if (bindingResult.hasErrors())
			{
				responseJsp = "webapp/gallerylogon";
			}
			else
			{
				HttpSession tomcatSession = request.getSession(false);
				if (tomcatSession == null)
				{
					meLogger.warn("Logon request made, but no Tomcat session has been established.");
					failed = true;
				}
				
				CustomSessionState customSession = (CustomSessionState)tomcatSession.getAttribute("CustomSessionState");
				if (!failed && customSession == null)
				{
					meLogger.warn("Logon request made, but no custom session has been established.");
					failed = true;
				}
				
				if (!failed && galleryLogon == null)
				{
					meLogger.warn("Logon request made, but no logon object submitted");
					failed = true;
				}
				
				if (!failed && !galleryName.equals(galleryLogon.getGalleryName()))
				{
					meLogger.warn("Gallery name mismatch, logon failed.  Request Name: " + galleryName + " Logon object Name: " + galleryLogon.getGalleryName());
					failed = true;
				}
				
				if (!failed && !profileName.equals(galleryLogon.getProfileName()))
				{
					meLogger.warn("Profile name mismatch, logon failed.  Request Name: " + profileName + " Logon object Name: " + galleryLogon.getProfileName());
					failed = true;
				}
				
				if (failed)
				{
					model.addAttribute("message", "Logon failed, please check your details and try again");
					return responseJsp;
				}
				
				
				if (galleryService.LoginGalleryUser(galleryLogon, request, customSession, requestId))
				{
					meLogger.debug("Gallery login authorised.  User:" + customSession.getProfileName() + " Gallery:" + customSession.getGalleryName());
					
					responseJsp = "redirect:" + referrer;
					
					Cookie wallaSessionIdCookie = new Cookie("X-Walla-Id", UserTools.GetLatestWallaId(customSession));
					wallaSessionIdCookie.setPath("/wallahub/");
					response.addCookie(wallaSessionIdCookie);
				}
				else
				{
					Thread.sleep(1000);
					//model.addAttribute("message", "Logon failed, please check your details and try again");
					//responseJsp = "redirect:x/gallerylogon";

					message = "Logon failed, please check your details and try again";
					responseJsp = "redirect:./logon?referrer=" 
						+ UriUtils.encodePath(referrer,"UTF-8") 
						+ "&message=" + UserTools.EncodeString(message, request);
					
				}
			}
			return responseJsp;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			model.addAttribute("message", defaultMessage);
			return responseJsp;
		}
		finally { utilityService.LogWebMethod("AccountAdmin", "GalleryLogonPost", startMS, request, requestId, responseJsp); response.setStatus(HttpStatus.OK.value()); }
	}
	
	private String RedirectToLogon(String message, HttpServletRequest request) throws UnsupportedEncodingException
	{
		meLogger.warn(message);
		String path = new UrlPathHelper().getPathWithinApplication(request);
		
		return "redirect:" + urlPrefix + "/logon?referrer=" 
			+ UriUtils.encodePath(path,"UTF-8") 
			+ "&message=" + UserTools.EncodeString(message, request);
	}
	
	private String RedirectToLogonMaintainReferrer(String message, HttpServletRequest request, String referrer) throws UnsupportedEncodingException
	{
		meLogger.warn(message);
		
		return "redirect:" + urlPrefix + "/logon?referrer=" 
			+ UriUtils.encodePath(referrer,"UTF-8") 
			+ "&message=" + UserTools.EncodeString(message, request);	
	}
	
	private void ToDeleteMergeStorageObjects(AccountStorage request, AccountStorage session)
	{
		request.setStorageMessage(session.getStorageMessage());
		request.setStorageGBLimit(session.getStorageGBLimit());
		request.setMonthlyUploadCap(session.getMonthlyUploadCap());
		request.setUploadCount30Days(session.getUploadCount30Days());
		request.setSizeGB(session.getSizeGB());
		request.setCompressedSizeGB(session.getCompressedSizeGB());
		request.setImageCount(session.getImageCount());


	}
	
	private void ToDeleteMergeSettingsToAccount(Account account, AccountSettings accountSettings)
	{
		accountSettings.setProfileName(account.getProfileName());
		accountSettings.setDescription(account.getDesc());
		accountSettings.setCountry(account.getCountry());
		accountSettings.setTimezone(account.getTimezone());
		accountSettings.setNewsletter(account.getNewsletter());
		accountSettings.setAccountMessage(account.getAccountMessage());
		accountSettings.setAccountTypeName(account.getAccountTypeName());
		accountSettings.setOpenDate(account.getOpenDate().getTime());
		accountSettings.setVersion(account.getVersion());
		accountSettings.setId(account.getId());
		accountSettings.setAccountMessage(account.getAccountMessage());	
		
		if (account.getEmails().getEmailRef().size() > 0)
		{

			for (Iterator<Account.Emails.EmailRef> imageIterater = account.getEmails().getEmailRef().iterator(); imageIterater.hasNext();)
			{
				Account.Emails.EmailRef current = (Account.Emails.EmailRef)imageIterater.next();
				
				AccountSettings.EmailRef emailRef = new AccountSettings.EmailRef();
				emailRef.setAddress(current.getAddress());
				emailRef.setPrinciple(current.getPrinciple());
				emailRef.setSecondary(current.getSecondary());
				emailRef.setVerified(current.getVerified());
				
				accountSettings.getEmails().add(emailRef);
			}
		}
	}
	
	private String CreateChartDataStruct(AccountStorage storage, String type)
	{
		String response = null;
		
		if (type.compareTo("YEAR") == 0 && storage.getImageYearRef().size() > 0)
		{
			response = "[";
			
			float imageCount = 0;
			float totalImages = 0;
			for (int i = 0; i < storage.getImageYearRef().size(); i++)
				totalImages = totalImages + (float)storage.getImageYearRef().get(i).getImageCount();
			
			
			for (int i = 0; i < storage.getImageYearRef().size(); i++)
			{
				AccountStorage.ImageYearRef current = storage.getImageYearRef().get(i);
				imageCount = imageCount + current.getImageCount();
				float percent = (float) ((100.0 / totalImages) * imageCount);
				String colour = UserTools.GetColourVariantHex("#FFB6EB", "#FFA0BB", (float)Math.max(percent, 1.0));
				String highlight = UserTools.GetColourVariantHex("#FF6388", "#FF5E6D", (float)Math.max(percent, 1.0));
				
				current.setColour(colour);
				//String label = current.getYear() + " (Size: " + current.getSizeGB().toString() + " / Compressed: " + current.getCompressedSizeGB().toString() + ")";
				String label = current.getYear(); //+ " (Size: " + current.getSizeGB().toString() + ")";

				String line = (i == 0) ? "" : ",";
				line = line + "{value: " + current.getImageCount() + ",color:'" + colour + "',highlight: '" + highlight + "',label: '" + label + "'}";
				
				response = response + line;
			}

			response = response + "]";
		}
		
		if (type.compareTo("FORMAT") == 0 && storage.getFormatRef().size() > 0)
		{
			response = "[";
			
			float imageCount = 0;
			int totalImages = 0;
			for (int i = 0; i < storage.getFormatRef().size(); i++)
				totalImages = totalImages + storage.getFormatRef().get(i).getImageCount();
			
			for (int i = 0; i < storage.getFormatRef().size(); i++)
			{
				AccountStorage.FormatRef current = storage.getFormatRef().get(i);
				imageCount = imageCount + current.getImageCount();
				float percent = (float) ((100.0 / totalImages) * imageCount);
				String colour = UserTools.GetColourVariantHex("#D6EBFF", "#246BB2", (float)Math.max(percent, 1.0));
				String highlight = UserTools.GetColourVariantHex("#EBD6FF", "#8F6BB2", (float)Math.max(percent, 1.0));
				
				current.setColour(colour);
				String label = current.getFormat(); // + " (Size: " + current.getSizeGB().toString() + ")";
				
				String line = (i == 0) ? "" : ",";
				line = line + "{value: " + current.getImageCount() + ",color:'" + colour + "',highlight: '" + highlight + "',label: '" + label + "'}";
				
				response = response + line;
			}

			response = response + "]";
		}
		
		if (type.compareTo("UPLOAD") == 0 && storage.getUploadSourceRef().size() > 0)
		{
			response = "[";
			
			float imageCount = 0;
			int totalImages = 0;
			for (int i = 0; i < storage.getUploadSourceRef().size(); i++)
				totalImages = totalImages + storage.getUploadSourceRef().get(i).getImageCount();
			
			for (int i = 0; i < storage.getUploadSourceRef().size(); i++)
			{
				AccountStorage.UploadSourceRef current = storage.getUploadSourceRef().get(i);
				imageCount = imageCount + current.getImageCount();
				float percent = (float) ((100.0 / totalImages) * imageCount);
				String colour = UserTools.GetColourVariantHex("#D6EBFF", "#246BB2", (float)Math.max(percent, 1.0));
				String highlight = UserTools.GetColourVariantHex("#EBD6FF", "#8F6BB2", (float)Math.max(percent, 1.0));
				
				current.setColour(colour);
				String label = current.getName(); // + " (Size: " + current.getSizeGB().toString() + ")";

				String line = (i == 0) ? "" : ",";
				line = line + "{value: " + current.getImageCount() + ",color:'" + colour + "',highlight: '" + highlight + "',label: '" + label + "'}";
				
				response = response + line;
			}

			response = response + "]";
		}
		return response;
	}
	
}
