package walla.db;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import walla.business.UtilityService;
import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.*;

import org.springframework.http.HttpStatus;

@Repository
public class TagDataHelperImpl implements TagDataHelper {

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(TagDataHelperImpl.class);

	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	public TagDataHelperImpl() {
		meLogger.debug("TagDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public void CreateTag(long userId, Tag newTag, long newId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		String sql = "INSERT INTO [Tag] ([TagId],[Name],[Description],[SystemOwned], "
				+ "[DefinitionId],[ImageCount],[LastUpdated],[RecordVersion],[UserId]) "
				+ "VALUES (?,?,?,?,0,0,dbo.GetDateNoMS(),1,?)";
		
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement bs = null;

		try {			
			int returnCount = 0;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main tag record.
			ps = conn.prepareStatement(sql);
			ps.setLong(1, newId);
			ps.setString(2, newTag.getName());
			ps.setString(3, newTag.getDesc());
			ps.setBoolean(4, newTag.getSystemOwned());
			ps.setLong(5, userId);
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateTag", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
			}

			conn.commit();
				
		} catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { ps.close(); } catch (SQLException logOrIgnore) {}
	        if (bs != null) try { bs.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","CreateTag", startMS, requestId, String.valueOf(newId));
		}
	}
	
	public void UpdateTag(long userId, Tag existingTag, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			String updateVersionSql = null;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			updateVersionSql = "UPDATE [dbo].[Tag] SET [Name] = ?, [Description] = ?, [LastUpdated] = dbo.GetDateNoMS(), [RecordVersion] = [RecordVersion] + 1 WHERE [TagId] = ? AND [RecordVersion] = ?";

			ps = conn.prepareStatement(updateVersionSql);
			ps.setString(1, existingTag.getName());
			ps.setString(2, existingTag.getDesc());
			ps.setLong(3, existingTag.getId());
			ps.setInt(4, existingTag.getVersion());
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "UpdateTag", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","UpdateTag", startMS, requestId, String.valueOf(existingTag.getId()));
		}
	}
	
	public void DeleteTag(long userId, long tagId, int version, String tagName, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		Statement us = null;
		
		try {
			int returnCount = 0;		
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			String deleteSql = "DELETE FROM [Tag] WHERE [TagId]= ? AND [RecordVersion] = ? AND [UserId] = ? AND [Name] = ? AND [SystemOwned] = 0"; 
			ps = conn.prepareStatement(deleteSql);
			ps.setLong(1, tagId);
			ps.setInt(2, version);
			ps.setLong(3, userId);
			ps.setString(4, tagName);

			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Delete statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "DeleteTag", error, HttpStatus.CONFLICT.value()); 
			}

			String updateSql = "UPDATE [User] SET [TagLastDeleted] = dbo.GetDateNoMS() WHERE [UserId] = " + userId;
			us = conn.createStatement();
			returnCount = us.executeUpdate(updateSql);
			us.close();
			
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update timestamp statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("GalleryDataHelperImpl", "DeleteGallery", error, HttpStatus.CONFLICT.value()); 
			}

			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (WallaException wallaEx) {
			throw wallaEx;
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (us != null) try { if (!us.isClosed()) {us.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","DeleteTag", startMS, requestId, String.valueOf(tagId));
		}
	}
	
	public void DeleteTagReferences(long userId, long tagId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement us = null;
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			String deleteImagesSql = "DELETE FROM [TagImage] WHERE [TagId]=" + tagId; 
			String deleteGalleryTagsSql = "DELETE FROM [GalleryTag] WHERE [TagId]=" + tagId;
			us = conn.createStatement();
			us.addBatch(deleteImagesSql);
			us.addBatch(deleteGalleryTagsSql);
			us.executeBatch();
			us.close();

			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (us != null) try { if (!us.isClosed()) {us.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","DeleteTagReferences", startMS, requestId, String.valueOf(tagId));
		}
		
	}
	
	public Tag GetTagMeta(long userId, String tagName, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		Tag tag = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [TagId],[Name],[Description],[SystemOwned],[LastUpdated],[RecordVersion] FROM [dbo].[Tag] WHERE [UserId] = ? AND [Name]= ?";
			ps = conn.prepareStatement(selectSql);
			//ps.setFetchDirection(ResultSet.TYPE_FORWARD_ONLY);
			ps.setLong(1, userId);
			ps.setString(2,tagName);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				return null;
			}
			
			tag = new Tag();
			tag.setId(resultset.getLong(1));
			tag.setName(resultset.getString(2));
			tag.setDesc(resultset.getString(3));
			tag.setSystemOwned(resultset.getBoolean(4));
			
			Calendar lastChangedCalendar = Calendar.getInstance();
			lastChangedCalendar.setTimeInMillis(resultset.getTimestamp(5).getTime());
			
			tag.setLastChanged(lastChangedCalendar);
			tag.setVersion(resultset.getInt(6));
			
			return tag;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","GetTagMeta", startMS, requestId, tagName);
		}
	}
	
	public ImageList GetTagImageListMeta(long userId, String tagName, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		ImageList tagImageList = null;
		
		try {
			conn = dataSource.getConnection();

			String selectSql = "SELECT [TagId],[Name],[Description],[ImageCount],[LastUpdated],"
					+ "[RecordVersion],[SystemOwned] FROM [dbo].[TagView] WHERE [UserId] = ? AND [Name]= ?";
			ps = conn.prepareStatement(selectSql);

			ps.setLong(1, userId);
			ps.setString(2,tagName);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				resultset.close();
				meLogger.warn("Select statement didn't return any records.");
				return null;
			}
			
			tagImageList = new ImageList();
			tagImageList.setId(resultset.getLong(1));
			tagImageList.setType("Tag");
			tagImageList.setName(resultset.getString(2));
			tagImageList.setDesc(resultset.getString(3));
			tagImageList.setTotalImageCount(resultset.getInt(4));
			
			Calendar lastChangedCalendar = Calendar.getInstance();
			lastChangedCalendar.setTimeInMillis(resultset.getTimestamp(5).getTime());
			
			tagImageList.setLastChanged(lastChangedCalendar);
			tagImageList.setVersion(resultset.getInt(6));
			tagImageList.setSystemOwned(resultset.getBoolean(7));
			
			resultset.close();
			return tagImageList;
		}
		catch (SQLException ex) {
			meLogger.error(ex);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","GetTagImageListMeta", startMS, requestId, tagName);
		}
	}
	
	public Date LastTagListUpdate(long userId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		java.util.Date utilDate = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT MAX(UpdateDate) FROM (SELECT T.[LastUpdated] AS [UpdateDate] FROM "
					+ "[Tag] T WHERE T.[UserId] = " + userId + " UNION SELECT [TagLastDeleted] AS [UpdateDate] "
					+ "FROM [User] U WHERE U.[UserId] = " + userId + ") TagDates";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			if (resultset.next())
			{
				utilDate = new java.util.Date(resultset.getTimestamp(1).getTime());
			}
			
			resultset.close();
		
			return utilDate;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","LastTagListUpdate", startMS, requestId, "");
		}
	}

	public int xxxGetTotalImageCount(long userId, long tagId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {
			conn = dataSource.getConnection();

			String selectSql = "SELECT COUNT(1) "
								+ "FROM TagImage ti INNER JOIN Image i ON ti.ImageId = i.ImageId INNER JOIN ImageMeta im ON i.ImageId = im.ImageId "
								+ "WHERE ti.[TagId] = ? AND i.UserId = ? AND i.Status = 4";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, tagId);
			ps.setLong(1, userId);
			
			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getInt(1);
			}
			else
			{
				String error = "Select statement didn't return any records, in GetTotalImageCount.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "GetTotalImageCount", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","GetTotalImageCount", startMS, requestId, String.valueOf(tagId));
		}
	}
	
	public void GetTagImages(long userId, int imageCursor, int imageCount, ImageList tagImageList, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		GregorianCalendar oldGreg = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [Rank],[ImageId],[Name],[Description],[UploadDate],[TakenDate],"
					+ " [RecordVersion], [CategoryId], [ISO], [Aperture], [ShutterSpeed], [Size] "
					+ " FROM(   SELECT RANK() OVER (ORDER BY i.[Name], i.[ImageId]) as [Rank], i.[ImageId],i.[Name],i.[Description], "
					+ " i.[RecordVersion],im.[UploadDate],im.[TakenDate], i.[CategoryId],"
					+ " im.[Size], im.[Aperture],im.[ShutterSpeed],im.[ISO]"
					+ " FROM TagImage ti INNER JOIN Image i ON ti.ImageId = i.ImageId INNER JOIN ImageMeta im ON i.ImageId = im.ImageId"
					+ " WHERE ti.[TagId] = ? AND i.Status = 4 ) AS RR"
					+ " WHERE RR.[Rank] > ? AND RR.[Rank] <= ? ORDER BY [Name]";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, tagImageList.getId());
			ps.setInt(2, imageCursor);
			ps.setInt(3, imageCursor + imageCount);
			//ps.setString(5, "[Name]"); //Sort
			
			
			resultset = ps.executeQuery();
			oldGreg = new GregorianCalendar();
			tagImageList.setImages(new ImageList.Images());

			while (resultset.next())
			{
				ImageList.Images.ImageRef newImageRef = new ImageList.Images.ImageRef(); 
				newImageRef.setId(resultset.getLong(2));
				newImageRef.setName(resultset.getString(3));
				newImageRef.setDesc(resultset.getString(4));
				
				oldGreg.setTime(resultset.getTimestamp(5));
				XMLGregorianCalendar xmlOldGregUpload = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
				newImageRef.setUploadDate(xmlOldGregUpload);
				
				oldGreg.setTime(resultset.getTimestamp(6));
				XMLGregorianCalendar xmlOldGregTaken = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
				newImageRef.setTakenDate(xmlOldGregTaken);
				
				newImageRef.setMetaVersion(resultset.getInt(7));
				newImageRef.setCategoryId(resultset.getLong(8));
				
		        SimpleDateFormat monthDayYearformatter = new SimpleDateFormat("dd MMM yyyy");
		        monthDayYearformatter.format((java.util.Date) resultset.getTimestamp(6));
		        
				String summary = ((resultset.getInt(9) == 0) ? "" : "ISO" + resultset.getInt(9) + " ");
				summary = summary + ((resultset.getString(10) == null) ? "" : resultset.getString(10) + " ");
				summary = summary + ((resultset.getString(11) == null) ? "" : resultset.getString(11));

				String shotSummary = ((resultset.getInt(9) == 0) ? "" : "ISO" + resultset.getInt(9) + " ");
				shotSummary = shotSummary + ((resultset.getString(10) == null) ? "" : resultset.getString(10) + " ");
				shotSummary = shotSummary + ((resultset.getString(11) == null) ? "" : resultset.getString(11));
				newImageRef.setShotSummary(shotSummary);
				
				String fileSummary = UserTools.ConvertBytesToMB(resultset.getLong(12)) + " - ";
				fileSummary = fileSummary + (monthDayYearformatter.format((java.util.Date) resultset.getTimestamp(6)));
				newImageRef.setFileSummary(fileSummary);
				
				tagImageList.getImages().getImageRef().add(newImageRef);
			}
			resultset.close();
			
			tagImageList.setImageCount(tagImageList.getImages().getImageRef().size());
			tagImageList.setImageCursor(imageCursor);
		}
		catch (SQLException | DatatypeConfigurationException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","GetTagImages", startMS, requestId, String.valueOf(tagImageList.getId()));
		}
	}
	
	public TagList GetUserTagList(long userId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		TagList tagList = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT [TagId], [Name], [Description], [ImageCount], [SystemOwned] FROM "
					+ "TagView WHERE [UserId] = " + userId + " ORDER BY [Name]";
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);

			while (resultset.next())
			{
				if (tagList == null)
				{
					tagList = new TagList();
				}
				
				TagList.TagRef newTagRef = new TagList.TagRef(); 
				newTagRef.setId(resultset.getLong(1));
				newTagRef.setName(resultset.getString(2));
				newTagRef.setDesc(resultset.getString(3));
				newTagRef.setCount(resultset.getInt(4));	
				newTagRef.setSystemOwned(resultset.getBoolean(5));
				tagList.getTagRef().add(newTagRef);
			}
			
			resultset.close();
			return tagList;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","GetUserTagList", startMS, requestId, "");
		}
	}

	public void UpdateTagTimeAndCount(long userId, long tagId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement us = null;
		
		try {
			int returnCount = 0;		
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			String updateSql = "UPDATE [Tag] SET [LastUpdated] = dbo.GetDateNoMS(), "
					+ "[ImageCount] = (SELECT COUNT(1) FROM [TagImage] TI INNER JOIN [Image] I ON TI.[ImageId] = I.[ImageId] "
					+ "WHERE TI.[TagId] = [Tag].[TagId] AND I.[Status]=4) "
					+ "WHERE [Tag].TagId = " + tagId + " AND [UserId] = " + userId;
			
			us = conn.createStatement();
			returnCount = us.executeUpdate(updateSql);
			us.close();
			
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update timestamp statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("TagDataHelperImpl", "UpdateTagTimestamp", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (us != null) try { if (!us.isClosed()) {us.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","UpdateTagTimeAndCount", startMS, requestId, String.valueOf(tagId));
		}
	}	

	public void AddRemoveTagImages(long userId, long tagId, ImageIdList moveList, boolean add, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement bs = null;

		try {			
			int controlCount = 0;
			int returnCount = 0;
			int[] responseCounts = null;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Add new images in the tag if there are any
			if (moveList.getImageRef() != null)
			{
				if (moveList.getImageRef().size() > 0)
				{
					controlCount = 0;
					returnCount = 0;
					
					String sql = null;
					
					if (add)
					{
						sql = "EXECUTE TagImageInsert ?, ?";
					}
					else
					{
						sql = "DELETE FROM [dbo].[TagImage] WHERE [TagId] = ? AND [ImageId] = ?";
					}
				    bs = conn.prepareStatement(sql);			   
				    
					for (int i = 0; i < moveList.getImageRef().size(); i++)
					{
						long imageId = moveList.getImageRef().get(i);
						bs.setLong(1, tagId);
						bs.setLong(2, imageId);
						bs.addBatch();
						
						controlCount++;
					}
					
					//Perform updates.
					responseCounts = bs.executeBatch();
					if (!add)
					{
						for (int i = 0; i < responseCounts.length; i++)
						{
							returnCount = returnCount + responseCounts[i];
						}
						
						//Check for unexpected row update count in the database
						if (returnCount != controlCount)
						{
							conn.rollback();
							String error = "Row count update didn't match with number of Tag Images to be updated.";
							meLogger.error(error);
							throw new WallaException("TagDataHelperImpl", "AddRemoveTagImages", error, HttpStatus.CONFLICT.value()); 
						}
					}
				}
			}
			conn.commit();
				
		} catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (bs != null) try { bs.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","AddRemoveTagImages", startMS, requestId, "");
		}
	}

	public long[] GetGalleriesLinkedToTag(long userId, long tagId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT DISTINCT G.GalleryId FROM Gallery G INNER JOIN GalleryTag GT ON "
					+ "G.GalleryId = GT.GalleryId WHERE G.UserId = " + userId + " AND GT.TagId = " + tagId;
			sQuery = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);

			int size = 0;
			try {
				resultset.last();
			    size = resultset.getRow();
			    resultset.beforeFirst();
			}
			catch(Exception ex) {}
			
			long[] galleryIds = new long[size];
			for (int i = 0; i < size; i++)
			{
				resultset.next();
				galleryIds[i] = resultset.getLong(1);
			}
			
			resultset.close();
			return galleryIds;			
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","GetGalleriesLinkedToTag", startMS, requestId, String.valueOf(tagId));
		}
	}
	
	public long[] ReGenDynamicTags(long userId, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement statement = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			conn.setAutoCommit(true);

			String executeSql = "EXECUTE dbo.[ReGenDynamicTags] " + userId;

			statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			int size = 0;
			
			resultset = statement.executeQuery(executeSql);
			try
			{
				resultset.last();
			    size = resultset.getRow();
			    resultset.beforeFirst();
			}
			catch(Exception ex) {}
			
			long[] returnTagId = new long[size];
			for (int i = 0; i < size; i++)
			{
				resultset.next();
				returnTagId[i] = resultset.getLong(1);
			}
			
			return returnTagId;
		}
		catch (SQLException sqlEx) {
			meLogger.error( sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (statement != null) try { if (!statement.isClosed()) {statement.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("TagDataHelperImpl","ReGenDynamicTags", startMS, requestId, "");
		}
	}	
}