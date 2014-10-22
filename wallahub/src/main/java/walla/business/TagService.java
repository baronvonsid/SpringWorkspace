package walla.business;

import java.util.*;
import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;

import javax.sql.DataSource;
import javax.xml.datatype.*;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;

@Service("TagService")
public class TagService {

	private TagDataHelperImpl tagDataHelper;
	private UtilityDataHelperImpl utilityDataHelper;
	private CachedData cachedData;
	private GalleryService galleryService;
	
	private static final Logger meLogger = Logger.getLogger(TagService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	public int CreateUpdateTag(long userId, Tag newtag, String tagName)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			Tag existingTag = tagDataHelper.GetTagMeta(userId, tagName);

			if (existingTag == null)
			{
				if (!newtag.getName().equals(tagName))
				{
					meLogger.warn("Create Tag failed, names don't match.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				long newTagId = utilityDataHelper.GetNewId("TagId");
				tagDataHelper.CreateTag(userId, newtag, newTagId);
				return HttpStatus.CREATED.value();
			}
			else
			{
				if (existingTag.getVersion() != newtag.getVersion())
				{
					meLogger.warn("Update Tag failed, record versions don't match.");
					return HttpStatus.CONFLICT.value(); 
				}
				
				tagDataHelper.UpdateTag(userId, newtag);
				
				if (!existingTag.getName().equals(tagName))
				{
					return HttpStatus.MOVED_PERMANENTLY.value();
				}
				else
				{
					return HttpStatus.OK.value();
				}
			}
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {UserTools.LogMethod("CreateUpdateTag", meLogger, startMS, String.valueOf(userId) + " " + tagName);}
	}

	public int DeleteTag(long userId, Tag tag, String tagName)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			if (!tag.getName().equals(tagName))
			{
				meLogger.warn("Delete Tag failed, names don't match.");
				return HttpStatus.CONFLICT.value(); 
			}
			
			tagDataHelper.DeleteTag(userId, tag.getId(), tag.getVersion(), tagName);

			//TODO decouple method
			TagRippleDelete(userId, tag.getId());
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {UserTools.LogMethod("DeleteTag", meLogger, startMS, String.valueOf(userId) + " " + tagName);}
	}
	
	public Tag GetTagMeta(long userId, String tagName, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			//Get tag list for response.
			Tag tag = tagDataHelper.GetTagMeta(userId, tagName);
			if (tag == null)
			{
				meLogger.warn("GetTagMeta didn't return a valid Tag object");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return null;
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return tag;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {UserTools.LogMethod("GetTagMeta", meLogger, startMS, String.valueOf(userId) + " " + tagName);}
	}
	
	public TagList GetTagListForUser(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			TagList tagList = null;
			Date lastUpdate = tagDataHelper.LastTagListUpdate(userId);
			if (lastUpdate == null)
			{
				meLogger.warn("Last updated date for tag could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			//Check if tag list changed
			if (clientVersionTimestamp != null)
			{
				if (!lastUpdate.after(clientVersionTimestamp) || lastUpdate.equals(clientVersionTimestamp))
				{
					meLogger.debug("No tag list generated because server timestamp (" + lastUpdate.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}
			
			//Get tag list for response.
			tagList = tagDataHelper.GetUserTagList(userId);
			if (tagList != null)
			{
				GregorianCalendar gregory = new GregorianCalendar();
				gregory.setTime(lastUpdate);
				XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
				
				tagList.setLastChanged(xmlOldGreg);
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());

			return tagList;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {UserTools.LogMethod("GetTagListForUser", meLogger, startMS, String.valueOf(userId));}
	}

	public int AddRemoveTagImages(long userId, String tagName, ImageIdList moveList, boolean add)
	{
		long startMS = System.currentTimeMillis();
		try 
		{
			Tag tag = tagDataHelper.GetTagMeta(userId, tagName);
			if (tag == null)
			{
				meLogger.warn("AddRemoveTagImages didn't return a valid Tag object");
				return HttpStatus.INTERNAL_SERVER_ERROR.value(); 
			}
			
			tagDataHelper.AddRemoveTagImages(userId, tag.getId(), moveList, add);

			//TODO decouple method
			TagRippleUpdate(userId, tag.getId());
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {UserTools.LogMethod("AddRemoveTagImages", meLogger, startMS, String.valueOf(userId) + " " + tagName);}
	}
	
	public long CreateOrFindUserAppTag(long userId, int platformId, String machineName) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			Platform platform = cachedData.GetPlatform(platformId, "", "", 0, 0);
			if (platform == null)
			{
				String error = "Platform not found. platformId:" + platformId;
				throw new WallaException("TagService", "CreateOrFindUserAppTag", error, HttpStatus.BAD_REQUEST.value()); 
			}
			
			String tagName = platform.getShortName() + " " + machineName;
			if (tagName.length() > 30)
				tagName = tagName.substring(0,30);
			
			String sql = "SELECT [TagId] FROM [Tag] WHERE [SystemOwned] = 1 AND [Name] = '" + tagName + "' AND [UserId] = " + userId;
			long tagId = utilityDataHelper.GetLong(sql);
			if (tagId > 1)
			{
				return tagId;
			}
			else
			{
				Tag newTag = new Tag();
				long newTagId = utilityDataHelper.GetNewId("TagId");
				newTag.setName(tagName);
				newTag.setDesc("Auto generated tag for fotos uploaded from " + machineName + " - " + platform.getShortName());
				newTag.setSystemOwned(true);
				tagDataHelper.CreateTag(userId, newTag, newTagId);
				
				return newTagId;
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			throw new WallaException(ex);
		}
		finally { UserTools.LogMethod("CreateOrFindUserAppTag", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(platformId)); }
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void ReGenDynamicTags(long userId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			//Returns an array of affected tags.
			long tags[] = tagDataHelper.ReGenDynamicTags(userId);
			
			for (int ii = 0; ii < tags.length; ii++)
			{
				long[] galleryIds = tagDataHelper.GetGalleriesLinkedToTag(userId, tags[ii]);
				for (int i = 0; i < galleryIds.length; i++)
				{
					//TODO decouple
					galleryService.RefreshGalleryImages(userId, galleryIds[i]);
				}
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("ReGenDynamicTags failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("ReGenDynamicTags failed with an error", ex);
		}
		finally {UserTools.LogMethod("ReGenDynamicTags", meLogger, startMS, String.valueOf(userId));}
	}
	
 	public void TagRippleUpdate(long userId, long tagId)
	{
 		long startMS = System.currentTimeMillis();
		try
		{
			//Update tag updated dates
			tagDataHelper.UpdateTagTimeAndCount(userId, tagId);
			
			//Find any views which reference this tag and update last updated.
			long[] galleryIds = tagDataHelper.GetGalleriesLinkedToTag(userId, tagId);
			for (int i = 0; i < galleryIds.length; i++)
			{
				//TODO decouple
				galleryService.RefreshGalleryImages(userId, galleryIds[i]);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("TagRippleUpdate failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("TagRippleUpdate failed with an error", ex);
		}
		finally {UserTools.LogMethod("TagRippleUpdate", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(tagId));}
	}
 	
 	public void TagRippleDelete(long userId, long tagId)
 	{
 		long startMS = System.currentTimeMillis();
		try
		{
			//Find any views which reference this tag and update last updated.
			long[] galleryIds = tagDataHelper.GetGalleriesLinkedToTag(userId, tagId);
			
			//Update tag updated dates
			tagDataHelper.DeleteTagReferences(userId, tagId);
			
			for (int i = 0; i < galleryIds.length; i++)
			{
				//TODO decouple
				galleryService.RefreshGalleryImages(userId, galleryIds[i]);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("TagRippleDelete failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("TagRippleDelete failed with an error", ex);
		}
		finally {UserTools.LogMethod("TagRippleDelete", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(tagId));}
 	}
	
	public void setTagDataHelper(TagDataHelperImpl tagDataHelper)
	{
		this.tagDataHelper = tagDataHelper;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
	
	public void setGalleryService(GalleryService galleryService)
	{
		this.galleryService = galleryService;
	}
}
