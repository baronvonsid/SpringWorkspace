package walla.business;

import java.text.DateFormat;
import java.util.*;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.datatype.*;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

@Service("CategoryService")
public class CategoryService {

	@Resource(name="categoryDataHelper")
	private CategoryDataHelperImpl categoryDataHelper;
	
	@Resource(name="utilityDataHelper")
	private UtilityDataHelperImpl utilityDataHelper;
	
	@Resource(name="cachedData")
	private CachedData cachedData;
	
	@Resource(name="imageServicePooled")
	private ImageService imageService;
	
	@Resource(name="galleryServicePooled")
	private GalleryService galleryService;
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	@Value( "${messaging.enabled}" ) private boolean messagingEnabled;
	
	private static final Logger meLogger = Logger.getLogger(CategoryService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	public CategoryService()
	{
		meLogger.debug("CategoryService object instantiated.");
	}
	
	public long CreateCategory(long userId, Category newCategory, CustomResponse customResponse, long userAppId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			if (newCategory.getParentId() == 0)
			{
				meLogger.warn("CreateUpdateCategory failed, root category cannot be used in this context.");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value());
				return 0;
			}
			
			//New Category
			long categoryId = utilityDataHelper.GetNewId("CategoryId", requestId);
			categoryDataHelper.CreateCategory(userId, newCategory, categoryId, requestId);
			customResponse.setResponseCode(HttpStatus.CREATED.value());
			
			//TODO decouple this method.  Don't think we need.
			//CategoryRippleUpdate(userId, categoryId);
			
			utilityService.AddAction(ActionType.UserApp, userAppId, "CatAdd", "");
			
			return categoryId;
		}
		catch (WallaException wallaEx) {
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return 0;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
		finally {utilityService.LogMethod("CategoryService","CreateCategory", startMS, requestId, "");}
	}

	public int UpdateCategory(long userId, Category newCategory, long categoryId, long userAppId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			if (newCategory.getParentId() == 0)
			{
				meLogger.warn("UpdateCategory failed, root category cannot be updated.");
				return HttpStatus.BAD_REQUEST.value();
			}
			
			if (newCategory.getId() != categoryId)
			{
				meLogger.warn("UpdateCategory failed, category Ids don't match.");
				return HttpStatus.CONFLICT.value();
			}
			
			Category existingCategory = categoryDataHelper.GetCategoryMeta(userId, categoryId, requestId);
			if (existingCategory == null)
			{
				meLogger.warn("Couldn't return a valid Category object");
				return HttpStatus.BAD_REQUEST.value();
			}
			
			if (existingCategory.getVersion() != newCategory.getVersion())
			{
				meLogger.warn("Update Category failed, record versions don't match.");
				return HttpStatus.CONFLICT.value();
			}

			categoryDataHelper.UpdateCategory(userId, newCategory, requestId);
			
			if (existingCategory.getParentId() != newCategory.getParentId())
			{
				if (messagingEnabled)
				{
					RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "CategoryService", "CategoryRippleUpdate", requestId, categoryId, 0, null);
					utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "CATUPD");
				}
				else
					CategoryRippleUpdate(userId, categoryId, requestId);
			}

			utilityService.AddAction(ActionType.UserApp, userAppId, "CatUpd", "");
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {utilityService.LogMethod("CategoryService","UpdateCategory", startMS, requestId, String.valueOf(categoryId));}
	}
	
	public int DeleteCategory(long userId, Category category, long categoryId, long userAppId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			if (category.getId() != categoryId)
			{
				meLogger.warn("DeleteCategory failed, category Ids don't match.");
				return HttpStatus.CONFLICT.value();
			}

			if (category.getParentId() == 0)
			{
				meLogger.warn("Delete Category failed, root category cannot be deleted.");
				return HttpStatus.BAD_REQUEST.value();
			}
			
			long[] categoryIds = categoryDataHelper.GetCategoryHierachy(userId, categoryId, false, requestId);
			
			categoryDataHelper.MarkCategoryAsDeleted(userId, categoryIds, category, requestId);

			if (messagingEnabled)
			{
				RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "CategoryService", "CategoryRippleDelete", requestId, 0, 0, categoryIds);
				utilityService.SendMessageToQueue(QueueTemplate.NoAgg, requestMessage, "CATDEL");
			}
			else
				CategoryRippleDelete(userId, categoryIds, requestId);
			
			
			
			utilityService.AddAction(ActionType.UserApp, userAppId, "CatDel", "");
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {utilityService.LogMethod("CategoryService","DeleteCategory", startMS, requestId, String.valueOf(categoryId));}
	}
	
	public Category GetCategoryMeta(long userId, long categoryId, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			Category category = categoryDataHelper.GetCategoryMeta(userId, categoryId, requestId);
			if (category == null)
			{
				meLogger.warn("GetCategoryMeta didn't return a valid Category object");
				customResponse.setResponseCode(HttpStatus.BAD_REQUEST.value()); 
				return null;
			}

			customResponse.setResponseCode(HttpStatus.OK.value());
			return category;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		finally {utilityService.LogMethod("CategoryService","GetCategoryMeta", startMS, requestId, String.valueOf(categoryId));}
	}
	
	public ImageList GetCategoryWithImages(long userId, long categoryId, int imageCursor, int size, Date clientVersionTimestamp, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			ImageList categoryImageList = null;

			//Get main category for response.
			categoryImageList = categoryDataHelper.GetCategoryImageListMeta(userId, categoryId, requestId);
			if (categoryImageList == null)
			{
				meLogger.warn("No category image list header could not be retrieved from the database.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}
			
			//Check if category list changed
			if (clientVersionTimestamp != null)
			{
				Date lastUpdated = categoryImageList.getLastChanged().getTime();
				if (!lastUpdated.after(clientVersionTimestamp))
				{
					meLogger.debug("No category images list generated because server timestamp (" + lastUpdated.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}

			//Get total count for the result set (if first request) - TODO this works for EVERY request, need to change.
			int totalImageCount = categoryDataHelper.GetTotalImageCount(userId, categoryId, requestId);
			categoryImageList.setTotalImageCount(totalImageCount);
			if (totalImageCount > 0)
			{
				categoryDataHelper.GetCategoryImages(userId, imageCursor, size, categoryImageList, requestId);
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			return categoryImageList;
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
		finally {utilityService.LogMethod("CategoryService","GetCategoryImageListMeta", startMS, requestId, String.valueOf(categoryId));}
	}
	
	public CategoryList GetCategoryListForUser(long userId, Date clientVersionTimestamp, CustomResponse customResponse, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try {
			CategoryList categoryList = null;
			Date lastUpdate = categoryDataHelper.LastCategoryListUpdate(userId, requestId);
			if (lastUpdate == null)
			{
				meLogger.warn("Last updated date for category could not be retrieved.");
				customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
				return null;
			}

			//Check if category list changed
			if (clientVersionTimestamp != null)
			{
				if (!lastUpdate.after(clientVersionTimestamp) || lastUpdate.equals(clientVersionTimestamp))
				{
					if (meLogger.isDebugEnabled()) {meLogger.debug("No category list generated because server timestamp (" + lastUpdate.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");}
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}
			
			//Get category list for response.
			categoryList = categoryDataHelper.GetUserCategoryList(userId, requestId);
			if (categoryList != null)
			{
				Calendar lastUpdateCalendar = Calendar.getInstance();
				lastUpdateCalendar.setTime(lastUpdate);
				categoryList.setLastChanged(lastUpdateCalendar);
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());

			return categoryList;
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
		finally {utilityService.LogMethod("CategoryService","GetCategoryListForUser", startMS, requestId, "");}
	}

	public int MoveToNewCategory(long userId, long categoryId, ImageIdList moveList, long userAppId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			//Retrieve existing category list.
			long[] categoriesAffected = categoryDataHelper.GetCategoryIdFromImageMoveList(userId, moveList, requestId);
			if (categoriesAffected == null)
			{
				meLogger.warn("Unexpected error, No categories were identified for update.");
				return HttpStatus.BAD_REQUEST.value();
			}
			
			//Apply category updates to db.
			categoryDataHelper.MoveImagesToNewCategory(userId, categoryId, moveList, requestId);
			
			if (messagingEnabled)
			{
				RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "CategoryService", "CategoryRippleUpdate", requestId, categoryId, 0, null);
				utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "CATUPD");
			}
			else
				CategoryRippleUpdate(userId, categoryId, requestId);

			
			//TODO decouple this method.
			for (int i = 0; i< categoriesAffected.length; i++)
			{
				if (messagingEnabled)
				{
					RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "CategoryService", "CategoryRippleUpdate", requestId, categoriesAffected[i], 0, null);
					utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "CATUPD");
				}
				else
					CategoryRippleUpdate(userId, categoriesAffected[i], requestId);
			}
			
			utilityService.AddAction(ActionType.UserApp, userAppId, "CatMove", "");
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
		finally {utilityService.LogMethod("CategoryService","MoveToNewCategory", startMS, requestId, String.valueOf(categoryId));}
	}
	
	public long CreateOrFindUserAppCategory(long userId, int platformId, String machineName, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			Platform platform = cachedData.GetPlatform(platformId, "", "", 0, 0, requestId);
			if (platform == null)
			{
				throw new WallaException("CategoryService", "CreateOrFindUserAppCategory", "Platform not found. platformId:" + platformId, HttpStatus.BAD_REQUEST.value()); 
			}
			
			String categoryName = platform.getShortName() + " " + machineName;
			if (categoryName.length() > 30)
				categoryName = categoryName.substring(0,30);
			
			String sql = "SELECT [CategoryId] FROM [Category] WHERE [SystemOwned] = 1 AND [Name] = '" + categoryName + "' AND [UserId] = " + userId;
			long categoryId = utilityDataHelper.GetLong(sql, requestId);
			if (categoryId > 1)
			{
				return categoryId;
			}
			else
			{
				sql = "SELECT [CategoryId] FROM [Category] WHERE [ParentId] = 0 AND [UserId] = " + userId;
				long parentCategoryId = utilityDataHelper.GetLong(sql, requestId);
				
				if (parentCategoryId < 1)
				{
					String error = "Couldn't retrieve a valid parent category";
					throw new WallaException("CategoryService", "CreateOrFindUserAppCategory", error, 0); 
				}
				
				Category newCategory = new Category();
				categoryId = utilityDataHelper.GetNewId("CategoryId", requestId);
				newCategory.setName(categoryName);
				newCategory.setDesc("Auto generated tag for fotos uploaded from " + machineName + " - " + platform.getShortName());
				newCategory.setSystemOwned(true);
				newCategory.setParentId(parentCategoryId);
				
				categoryDataHelper.CreateCategory(userId, newCategory, categoryId, requestId);
				return categoryId;
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			throw new WallaException(ex);
		}
		finally { utilityService.LogMethod("CategoryService","CreateOrFindUserAppCategory", startMS, requestId, String.valueOf(platformId)); }
	}
	
	public long FindDefaultUserCategory(long userId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		try
		{
			String sql = "SELECT MIN([CategoryId]) from Category where ParentId = (SELECT [CategoryId] FROM [Category] WHERE [ParentId] = 0 AND [UserId] = " + userId + ")";
			long categoryId = utilityDataHelper.GetLong(sql, requestId);
			if (categoryId > 1)
			{
				return categoryId;
			}
			else
			{
				String error = "Couldn't retrieve a valid default category";
				throw new WallaException("CategoryService", "FindDefaultUserCategory", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}
		}
		catch (Exception ex) {
			meLogger.error(ex);
			throw new WallaException(ex);
		}
		finally { utilityService.LogMethod("CategoryService","FindDefaultUserCategory", startMS, requestId, ""); }
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void CategoryRippleDelete(long userId, long[] categoryIds, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			/*
				Should be called when a category is deleted.
			*/

			long[] galleryIds = categoryDataHelper.GetGalleryReferencingCategory(userId, categoryIds, requestId);
			
			for (int i = 0; i < galleryIds.length; i++)
			{
				if (messagingEnabled)
				{
					RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "GalleryService", "RefreshGalleryImages", requestId, galleryIds[i], 0, null);
					utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "GALRFH");
				}
				else
					galleryService.RefreshGalleryImages(userId, galleryIds[i], requestId);
			}
			
			imageService.DeleteAllImagesCategory(userId, categoryIds, requestId);
		}
		catch (WallaException wallaEx) {
			meLogger.error("CategoryRippleDelete failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("CategoryRippleDelete failed with an error", ex);
		}
		finally {utilityService.LogMethod("CategoryService","CategoryRippleDelete", startMS, requestId, "");}
	}

	public void CategoryRippleUpdate(long userId, long categoryId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		try
		{
			/*
				Messages must be aggregated up before method is called.
				Should be called when categories are moved, or the parent is changed.
				Should be called when images are added\removed to a category.
				
				Gets a list categories which are affected by changes to this category.
			*/
			
			//Category has been updated, get all categories which might be affected.
			long[] categoryIds = categoryDataHelper.GetCategoryHierachy(userId, categoryId, true, requestId);
			if (categoryIds.length == 0)
			{
				String error = "No categories were returned from the database.  UserId:" + userId + " CategoryId: " + categoryId;
				meLogger.debug(error);
				return;
			}
				
			//Update LastUpdated dates for each category traversed.
			categoryDataHelper.UpdateCategoryTimeAndCount(userId, categoryIds, requestId);
			
			long[] galleryIds = categoryDataHelper.GetGalleryReferencingCategory(userId, categoryIds, requestId);
			if (galleryIds == null || galleryIds.length == 0)
			{
				String error = "No galleries were returned from the database.  UserId:" + userId;;
				meLogger.debug(error);
				return;
			}

			for (int i = 0; i < galleryIds.length; i++)
			{
				if (messagingEnabled)
				{
					RequestMessage requestMessage = utilityService.BuildRequestMessage(userId, "GalleryService", "RefreshGalleryImages", requestId, galleryIds[i], 0, null);
					utilityService.SendMessageToQueue(QueueTemplate.Agg, requestMessage, "GALRFH");
				}
				else
					galleryService.RefreshGalleryImages(userId, galleryIds[i], requestId);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("CategoryRippleUpdate failed with an error");
		}
		catch (Exception ex) {
			meLogger.error("CategoryRippleUpdate failed with an error", ex);
		}
		finally {utilityService.LogMethod("CategoryService","CategoryRippleUpdate", startMS, requestId, String.valueOf(categoryId));}
	}

	
	
	//*************************************************************************************************************
	//*************************************  Plumbing *************************************************************
	//*************************************************************************************************************
	
	/*
	public void setCategoryDataHelper(CategoryDataHelperImpl categoryDataHelper)
	{
		this.categoryDataHelper = categoryDataHelper;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
	
	
	public void setImageService(ImageService imageService)
	{
		this.imageService = imageService;
	}
	
	public void setGalleryService(GalleryService galleryService)
	{
		this.galleryService = galleryService;
	}
	
	public void setUtilityService(UtilityService utilityService)
	{
		this.utilityService = utilityService;
	}
	*/
	
}
