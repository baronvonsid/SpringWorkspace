package walla.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import walla.datatypes.*;
import walla.datatypes.auto.Account;
import walla.datatypes.auto.Gallery;
import walla.datatypes.java.CustomSessionState;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

public final class UserTools {

	public static void Copyfile(String sourceFile, String destinationFile) throws IOException
	{
		  File f1 = new File(sourceFile);
		  File f2 = new File(destinationFile);
		  
		  InputStream in = new FileInputStream(f1);
		  OutputStream out = new FileOutputStream(f2);
		
		  byte[] buf = new byte[1024];
		  int len;
		  while ((len = in.read(buf)) > 0)
		  {
			  out.write(buf, 0, len);
		  }
		  in.close();
		  out.close();
	}
	
	public static File FileExistsNoExt(String folderPath, final String fileName) 
	{
		File folder = new File(folderPath);
		FilenameFilter select = new FileListFilter(fileName);
		File[] matchingFiles = folder.listFiles(select);
		
		/*
		
		File[] matchingFiles = folder.listFiles(new FilenameFilter() 
								{
									public boolean accept(File pathname) 
									{
										return pathname.getName().equals(fileName + "*");
									}
								});
								*/
		if (matchingFiles.length == 1)
		{
			return matchingFiles[0];
		}
		else
		{
			return null;
		}
		
	}
	
	static class FileListFilter implements FilenameFilter 
	{
		  private String name; 

		  public FileListFilter(String name) {
		    this.name = name;
		  }

		  public boolean accept(File directory, String filename) {
		      return filename.startsWith(name);
		    }
		  }
	
	
	public static void PopulateServletStream(File fileIn, ServletOutputStream outStream) throws IOException
	{
		FileInputStream inStream = new FileInputStream(fileIn);                	
		//ServletOutputStream out = httpResponse.getOutputStream();
		 
		byte[] outputByte = new byte[4096];
		//copy binary content to output stream
		while(inStream.read(outputByte, 0, 4096) != -1)
		{
			outStream.write(outputByte, 0, 4096);
		}
		inStream.close();
		outStream.flush();
		outStream.close();
	}
	
	public static void MoveFile(String sourceFile, String destinationFile, Logger meLogger)
	{
		try
		{
			  File source = new File(sourceFile);
			  source.renameTo(new File(destinationFile));
		}
		catch (Exception ex)
		{
			//Capture error and suppress subsequent error bubbling.
			meLogger.error("File failed to be moved.  Source:" + sourceFile + "  Destination:" + destinationFile + " Error received:" + ex.getMessage());
			throw ex;
		}
	}

	
	public static double DoRound(double unrounded, int precision)
	{
	    BigDecimal bd = new BigDecimal(unrounded);
	    BigDecimal rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_UP);
	    return rounded.doubleValue();
	}
	
	public static int RandInt(int min, int max) {

	    // Usually this should be a field rather than a method variable so
	    // that it is not re-seeded every call.
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	public static String GetComplexString()
	{
		UUID identifier = java.util.UUID.randomUUID();
		return identifier.toString().replace("-", "").toUpperCase();
	}
	
	public static String GetIpAddress(HttpServletRequest request)
	{
		return request.getRemoteAddr();
	}
	
	public static Gallery.Sections.SectionRef GetExampleSections(int count)
	{
		return null;
		
	}
	
	public static String ConvertBytesToMB(long size)
	{
		double newSize = (double)size / 1024.0 / 1024.0;
		return DoRound(newSize, 2) + "MB";
	}
	
	public static boolean ValidEmailAddress(String email)
	{
		Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
		Matcher m = p.matcher(email);
		return m.matches();
	}
	
	public static boolean CheckPasswordStrength(String password)
	{
		//Match a string at least 8 characters long, with at least one lower case and at least one uppercase letter 
		Pattern p = Pattern.compile("^.*(?=.{8,})(?=.*[a-z])(?=.*[A-Z]).*$");
		Matcher m = p.matcher(password);
		return m.matches();
	}
	
	public static void LogWebMethod(String method, Logger meLogger, long startMS, HttpServletRequest request, int responseCode)
	{
		if (meLogger.isDebugEnabled())
		{
			long duration = System.currentTimeMillis() - startMS;
			String queryString = request.getQueryString();
			
			if (queryString == null)
				queryString = "";
			
			String message = method + "|" + duration + "|" + responseCode + "|" + request.getPathInfo() + "|" + queryString;
			meLogger.debug(message);
		}
	}
	
	public static void LogWebFormMethod(String method, Logger meLogger, long startMS, HttpServletRequest request, String responseJsp)
	{
		if (meLogger.isDebugEnabled())
		{
			long duration = System.currentTimeMillis() - startMS;
			String queryString = request.getQueryString();
			
			if (queryString == null)
				queryString = "";
			
			String message = method + "|" + duration + "|" + responseJsp + "|" + request.getPathInfo() + "|" + queryString;
			meLogger.debug(message);
		}
	}
	
	public static void LogMethod(String method, Logger meLogger, long startMS, String params)
	{
		if (meLogger.isDebugEnabled())
		{
			long duration = System.currentTimeMillis() - startMS;
			String message = method + "|" + duration + "|" + params;
			meLogger.debug(message);
		}
	}
	
	public static CustomSessionState GetValidAdminSession(String requestProfileName, HttpServletRequest request, Logger meLogger)
	{
		HttpSession session = request.getSession(false);
		if (session == null)
		{
			meLogger.warn("The tomcat session has not been established.");
			return null;
		}

		CustomSessionState customSession = (CustomSessionState)session.getAttribute("CustomSessionState");
		if (customSession == null)
		{
			meLogger.warn("The custom session state has not been established.");
			return null;
		}
			
		if (!customSession.isAuthenticated())
		{
			meLogger.warn("The session has not been authorised.");
			return null;
		}	

		if (!customSession.isAdmin())
		{
			meLogger.warn("The session is not for an admin.");
			return null;
		}
		
		if (!customSession.getProfileName().equalsIgnoreCase(requestProfileName))
		{
			meLogger.warn("The profile name does not match between request and session");
			return null;
		}
		
		boolean found = false;
		String requestSessionId = "";
		for (int i = 0; i < request.getCookies().length; i++)
		{
			if (request.getCookies()[i].getName().compareTo("X-Walla-Id") == 0)
			{
				requestSessionId = request.getCookies()[i].getValue();
			}
		}
		
		if (requestSessionId.length() == 32)
		{
			for (int i = 0; i < customSession.getCustomSessionIds().size(); i++)
			{
				if (requestSessionId.compareTo(customSession.getCustomSessionIds().get(i)) == 0)
					found = true;
			}
		}
		
		if (!found)
		{
			meLogger.warn("The custom session id does not have a match.");
			return null;
		}	
		
		if (customSession.getRemoteAddress().compareTo(GetIpAddress(request)) != 0)
		{
			meLogger.warn("IP address of the session has changed since the logon key was issued.");
			return null;
		}
		
		return customSession;
	}

	public static CustomSessionState CheckNewUserSession(Account account, HttpServletRequest request, Logger meLogger)
	{		
		HttpSession session = request.getSession(false);
		if (session == null)
		{
			meLogger.warn("The tomcat session has not been established.");
			return null;
		}

		CustomSessionState customSession = (CustomSessionState)session.getAttribute("CustomSessionState");
		if (customSession == null)
		{
			meLogger.warn("The custom session state has not been established.");
			return null;
		}
		
		String requestKey = (account.getKey() == null) ? "" : account.getKey();
		String sessionKey = "";
		synchronized(customSession) {
			sessionKey = customSession.getNonceKey();
			customSession.setNonceKey("");
		}
		
		if (sessionKey.compareTo(requestKey) != 0)
		{
			meLogger.warn("One off new user key, does not match request.  ServerKey:" + sessionKey + " RequestKey:" + requestKey);
			return null;
		}
		
		if (customSession.isAuthenticated())
		{
			meLogger.warn("The session has already been authenticated and is not valid for creating a user");
			return null;
		}	
		
		if (customSession.getRemoteAddress().compareTo(GetIpAddress(request)) != 0)
		{
			meLogger.warn("IP address of the session has changed since the logon key was issued.");
			return null;
		}

		//TODO add isHuman check.
		
		return customSession;
	}
	

	public static CustomSessionState GetValidGallerySession(String requestProfileName, String galleryName, boolean checkName, HttpServletRequest request, Logger meLogger)
	{
		HttpSession session = request.getSession(false);
		if (session == null)
		{
			meLogger.warn("The tomcat session has not been established.");
			return null;
		}

		CustomSessionState customSession = (CustomSessionState)session.getAttribute("CustomSessionState");
		if (customSession == null)
		{
			meLogger.warn("The custom session state has not been established.");
			return null;
		}
			
		if (!customSession.isAuthenticated())
		{
			meLogger.warn("The session has not been authorised.");
			return null;
		}	

		if (!customSession.isGalleryViewer())
		{
			meLogger.warn("The session is not for a gallery viewer.");
			return null;
		}
		
		if (!customSession.getProfileName().equalsIgnoreCase(requestProfileName))
		{
			meLogger.warn("The profile name does not match between request and session");
			return null;
		}
		
		if (!customSession.getGalleryName().equalsIgnoreCase(galleryName) && checkName)
		{
			meLogger.warn("The gallery name name does not match between request and session");
			return null;
		}
		
		if (customSession.getRemoteAddress().compareTo(GetIpAddress(request)) != 0)
		{
			meLogger.warn("IP address of the session has changed since the logon was established.");
			return null;
		}
		
		return customSession;
	}
	
	public static String GetLatestWallaId(CustomSessionState customSession)
	{
		//Todo - remove old Ids.
		return String.valueOf(customSession.getCustomSessionIds().get(customSession.getCustomSessionIds().size()-1));	
	}

}
