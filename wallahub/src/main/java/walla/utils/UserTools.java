package walla.utils;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import walla.business.UtilityService;
import walla.datatypes.*;
import walla.datatypes.auto.Account;
import walla.datatypes.auto.Gallery;
import walla.datatypes.java.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

public final class UserTools {

	public static void Copyfile(String sourceFile, String destinationFile, UtilityService utilityService, String requestId) throws IOException
	{
		long startMS = System.currentTimeMillis();
		InputStream in = null;
		OutputStream out = null;
		
		try
		{
			  File f1 = new File(sourceFile);
			  File f2 = new File(destinationFile);
			  
			  in = new FileInputStream(f1);
			  out = new FileOutputStream(f2);
			
			  byte[] buf = new byte[1024];
			  int len;
			  while ((len = in.read(buf)) > 0)
			  {
				  out.write(buf, 0, len);
			  }
			  out.flush();
		}
		finally { 
			if (in != null) try { in.close(); } catch (Exception logOrIgnore) {}
			if (out != null) try { out.close(); } catch (Exception logOrIgnore) {}
			utilityService.LogMethod("UserTools","Copyfile", startMS, requestId, sourceFile); 
		}
	}
	
	public static File FileExistsNoExt(String folderPath, final String fileName) 
	{
		File folder = new File(folderPath);
		FilenameFilter select = new FileListFilter(fileName + ".");
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
		if (matchingFiles != null && matchingFiles.length == 1)
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
	
	public static void MoveFile(String sourceFile, String destinationFile, UtilityService utilityService, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			  File source = new File(sourceFile);
			  source.renameTo(new File(destinationFile));
		}
		finally { 
			utilityService.LogMethod("UserTools","MoveFile", startMS, requestId, sourceFile); 
		}
	}
	
	public static void DeleteFile(String filePath, UtilityService utilityService, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			File deleteFile = new File(filePath);
			deleteFile.delete();
		}
		finally {utilityService.LogMethod("UserTools","DeleteFile", startMS, requestId, filePath);}
	}

	public static void CompressToZip(String sourceFile, String destinationFile, UtilityService utilityService, String requestId) throws WallaException, IOException
	{
        FileInputStream fileInputStream = null;
        ZipOutputStream zipOutputStream = null;
        FileOutputStream fileOutputStream = null;
        ZipEntry zipEntry = null;
        long startMS = System.currentTimeMillis();
        
        try {

        	fileOutputStream = new FileOutputStream(destinationFile);
        	zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));

            File input = new File(sourceFile);
            fileInputStream = new FileInputStream(input);
            
            zipEntry = new ZipEntry(input.getName());
            zipOutputStream.putNextEntry(zipEntry);

        	byte[] buffer = new byte[1024];
        	fileOutputStream = new FileOutputStream(destinationFile);

            int size = 0;
            while((size = fileInputStream.read(buffer)) != -1){
            	zipOutputStream.write(buffer, 0 , size);
            }

            zipOutputStream.flush();

        } catch (FileNotFoundException ex) {
        	throw ex;
        } catch (IOException ex) {
        	throw ex;
        }
		finally { 
			if (fileInputStream != null) try { fileInputStream.close(); } catch (Exception logOrIgnore) {}
			if (zipOutputStream != null) try { zipOutputStream.close(); } catch (Exception logOrIgnore) {}
			if (fileOutputStream != null) try { fileOutputStream.close(); } catch (Exception logOrIgnore) {}
			utilityService.LogMethod("UserTools","CompressToZip", startMS, requestId, sourceFile); 
		}
	}
	
	public static void DecompressFromZip(String sourceFile, String destinationFile, Logger meLogger, UtilityService utilityService, String requestId) throws WallaException, IOException
	{
        FileInputStream fileInputStream = null;
        ZipInputStream zipInputStream = null;
        ZipEntry zipEntry = null;
        FileOutputStream fileOutputStream = null;
        long startMS = System.currentTimeMillis();
        
        try {

        	fileInputStream = new FileInputStream(sourceFile);
        	zipInputStream = new ZipInputStream(new BufferedInputStream(fileInputStream));

        	zipEntry = zipInputStream.getNextEntry();
        	if (zipEntry == null)
        	{
        		String error = "No zip file was found in the zip archive: " + sourceFile;
				meLogger.error(error);
				throw new WallaException("UserTools", "DecompressFromZip", error, HttpStatus.BAD_REQUEST.value()); 
        	}
            
        	byte[] buffer = new byte[1024];

        	fileOutputStream = new FileOutputStream(destinationFile);

            int size = 0;
            while((size = zipInputStream.read(buffer)) != -1){
            	fileOutputStream.write(buffer, 0 , size);
            }

            fileOutputStream.flush();

        	zipEntry = zipInputStream.getNextEntry();
        	if (zipEntry != null)
        	{
        		String error = "The zip archive contained more than one file: " + sourceFile;
				meLogger.error(error);
				throw new WallaException("UserTools", "DecompressFromZip", error, HttpStatus.BAD_REQUEST.value()); 
        	}

        } catch (FileNotFoundException ex) {
        	throw ex;
        } catch (IOException ex) {
        	throw ex;
        }
		finally { 
			if (fileInputStream != null) try { fileInputStream.close(); } catch (Exception logOrIgnore) {}
			if (zipInputStream != null) try { zipInputStream.close(); } catch (Exception logOrIgnore) {}
			if (fileOutputStream != null) try { fileOutputStream.close(); } catch (Exception logOrIgnore) {}
			utilityService.LogMethod("UserTools","DecompressFromZip", startMS, requestId, sourceFile); 
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
	
	public static String GetRequestId()
	{
		Random rand = new Random();
		long randomNumber = Math.abs(rand.nextLong());
		
		String id = String.valueOf(System.currentTimeMillis()) + "-" + String.valueOf(randomNumber);
		
		return id;
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
	
	public static String ObjectToXml(Object object) throws JAXBException
	{
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	
        StringWriter writer = new StringWriter(); 
        marshaller.marshal(object, writer);
        
        return writer.toString(); 
	}
	
	public static CustomSessionState GetInitialAdminSession(HttpServletRequest request, Logger meLogger) throws WallaException
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
			
		if (customSession.isAuthenticated())
		{
			meLogger.warn("The session has already been authorised.");
			return null;
		}	
		
		if (customSession.getRemoteAddress().compareTo(GetIpAddress(request)) != 0)
		{
			String error = "IP address of the session has changed since the logon was established.";
			meLogger.error(error);
			throw new WallaException("UserTools", "GetInitialAdminSession", error, HttpStatus.FORBIDDEN.value()); 
		}
		
		return customSession;
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
	
	public static CustomSessionState GetGallerySession(String requestProfileName, String requestGalleryName, boolean checkName, boolean checkAuth, HttpServletRequest request, Logger meLogger) throws WallaException
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
			
		if (!customSession.isAuthenticated() && checkAuth)
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
		
		if (!customSession.getGalleryName().equalsIgnoreCase(requestGalleryName) && checkName)
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

	public static CustomSessionState GetGalleryPreviewSession(String requestProfileName, HttpServletRequest request, Logger meLogger) throws WallaException
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

		if (!customSession.getProfileName().equalsIgnoreCase(requestProfileName))
		{
			meLogger.warn("The profile name does not match between request and session");
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

	public static String EncodeString(String string, HttpServletRequest request) throws UnsupportedEncodingException
	{
		String enc=request.getCharacterEncoding();
		if (enc == null)
		    enc=WebUtils.DEFAULT_CHARACTER_ENCODING;
		
		return UriUtils.encodeQueryParam(string, enc);
	}
	
	public static String GetColourVariantHex(String startColourHex, String endColourHex, float percentage)
	{
		//Start colour should be brighter.
		
		Color startColour = Color.decode(startColourHex);
		Color endColour = Color.decode(endColourHex);
		
		float[] startHSBArray = Color.RGBtoHSB(startColour.getRed(), startColour.getGreen(), startColour.getBlue(), null);
		float[] endHSBArray = Color.RGBtoHSB(endColour.getRed(), endColour.getGreen(), endColour.getBlue(), null);
		
		float newHue = startHSBArray[0];
		float newSat = startHSBArray[1];
		float newBright = startHSBArray[2];
		
		if (startHSBArray[0] > endHSBArray[0])
			newHue = startHSBArray[0] - ((float) (((startHSBArray[0] - endHSBArray[0]) / 100.0) * percentage));
		else
			newHue = startHSBArray[0] + ((float) (((endHSBArray[0] - startHSBArray[0]) / 100.0) * percentage));
		
		if (startHSBArray[1] > endHSBArray[1])
			newSat = startHSBArray[1] - ((float) (((startHSBArray[1] - endHSBArray[1]) / 100.0) * percentage));
		else
			newSat = startHSBArray[1] + ((float) (((endHSBArray[1] - startHSBArray[1]) / 100.0) * percentage));
		
		if (startHSBArray[2] > endHSBArray[2])
			newBright = startHSBArray[2] - ((float) (((startHSBArray[2] - endHSBArray[2]) / 100.0) * percentage));
		else
			newBright = startHSBArray[2] + ((float) (((endHSBArray[2] - startHSBArray[2]) / 100.0) * percentage));

		Color newColour = Color.getHSBColor(newHue, newSat, newBright);
		
		return toHex(newColour.getRed(), newColour.getGreen(), newColour.getBlue());
	}
	
	private static String toHex(int r, int g, int b) 
	{
	    return "#" + toBrowserHexValue(r) + toBrowserHexValue(g) + toBrowserHexValue(b);
	}

	private static String toBrowserHexValue(int number) 
	{
	    StringBuilder builder = new StringBuilder(Integer.toHexString(number & 0xff));
	    while (builder.length() < 2) {
	      builder.append("0");
	    }
	    return builder.toString().toUpperCase();
	}
}
