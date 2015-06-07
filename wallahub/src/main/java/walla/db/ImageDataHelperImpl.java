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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import walla.business.UtilityService;
import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.*;

import org.springframework.http.HttpStatus;

@Repository
public class ImageDataHelperImpl implements ImageDataHelper {

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(ImageDataHelperImpl.class);
	
	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	public ImageDataHelperImpl() {
		meLogger.debug("ImageDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public UploadStatusList GetCurrentUploads(long userId, ImageIdList imageIdToCheck, String requestId)
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String addImages = "";
			boolean hasIds = false;
			
			for (int i = 0; i < imageIdToCheck.getImageRef().size(); i++)
			{
				if (i==0)
					addImages = Long.toString(imageIdToCheck.getImageRef().get(i));
				else
					addImages = addImages + "," + Long.toString(imageIdToCheck.getImageRef().get(i));

				hasIds = true;
			}
			
			if (hasIds)
			{
				addImages = "OR [ImageId] IN (" + addImages + ")";
			}
			
			String selectSql = "SELECT [ImageId], [Status], [Name], [LastUpdated], [Error], [ErrorMessage] "
					+ "FROM [Image] WHERE [UserId] = ? AND "
					+ "(([Status] IN (1,2,3) AND [LastUpdated] > DATEADD(d,-30,dbo.GetDateNoMS())) " + addImages + ")";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			
			resultset = ps.executeQuery();
			
			GregorianCalendar oldGreg = new GregorianCalendar();
			UploadStatusList currentUploads = new UploadStatusList();

			while (resultset.next())
			{
				UploadStatusList.ImageUploadRef imageRef = new UploadStatusList.ImageUploadRef();
				imageRef.setImageId(resultset.getLong(1));
				imageRef.setStatus(resultset.getInt(2));
				imageRef.setName(resultset.getString(3));
				
				Calendar lastUpdatedCalendar = Calendar.getInstance();
				lastUpdatedCalendar.setTimeInMillis(resultset.getTimestamp(4).getTime());
				imageRef.setLastUpdated(lastUpdatedCalendar);
				
				imageRef.setError(resultset.getBoolean(5));
				imageRef.setErrorMessage(resultset.getString(6));
				
				currentUploads.getImageUploadRef().add(imageRef);
			}
			resultset.close();

			return currentUploads;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("ImageDataHelperImpl","GetCurrentUploads", startMS, requestId, "");
		}
	}

	public void MarkImagesAsInactive(long userId, long[] imagesToDelete, String requestId) throws WallaException 
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			int returnCount = 0;		
			int controlCount = 0;
			int responseCounts[];
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			if (imagesToDelete != null && imagesToDelete.length > 0)
			{
				//controlCount = 0;
				returnCount = 0;
				
				String deleteSql = "UPDATE [Image] SET [Status] = 5,[RecordVersion] = [RecordVersion] + 1, [LastUpdated] = dbo.GetDateNoMS() WHERE [ImageId]= ? AND [UserId] = ?"; 
			    ps = conn.prepareStatement(deleteSql);			   
			    
				//Construct update SQL statements
				//for (Iterator<ImageList.Images.ImageRef> imageIterater = imagesToDelete.getImages().getImageRef().iterator(); imageIterater.hasNext();)
				//{
				//	ImageList.Images.ImageRef currentImageRef = (ImageList.Images.ImageRef)imageIterater.next();
				//	
				//	ps.setLong(1,currentImageRef.getId());
				//	ps.setLong(2,userId);
				//	ps.addBatch();

					//controlCount++;
				//}
				
				for (int i = 0; i < imagesToDelete.length; i++)
				{
					ps.setLong(1,imagesToDelete[i]);
					ps.setLong(2,userId);
					ps.addBatch();
				}
				
				//Perform updates.
				responseCounts = ps.executeBatch();
				for (int i = 0; i < responseCounts.length; i++)
				{
					returnCount = returnCount + responseCounts[i];
				}
				
				ps.close();
				
				//Check for unexpected row update count in the database
				if (returnCount != controlCount)
				{
					conn.rollback();
					String error = "Row count update didn't match with number of new imageref objects to be deleted";
					meLogger.error(error);
					throw new WallaException("ImageDataHelperImpl", "MarkImageAsInactive", error, HttpStatus.CONFLICT.value()); 
				}

				conn.commit();
			}
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
	        utilityService.LogMethod("ImageDataHelperImpl","MarkImagesAsInactive", startMS, requestId, "");
		}
	}
	
	public ImageIdList GetActiveImagesInCategories(long userId, long[] categoryIds, String requestId)
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement statement = null;
		ResultSet resultset = null;
		ImageIdList deleteImageList = null;
		try {			
			
			if (categoryIds.length == 0)
				return null;
			
			conn = dataSource.getConnection();
			String selectSql = "SELECT ImageId FROM [Image] WHERE [UserId] = " + userId + " AND [Status] = 4 AND [CategoryId] IN (";
			for (int i = 0; i < categoryIds.length; i++)
			{
				selectSql = selectSql + ((i == 0) ? categoryIds[i] : "," + categoryIds[i]);
			}
			selectSql = selectSql + ")";
			
			statement = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = statement.executeQuery(selectSql);
			
			while (resultset.next())
			{
				if (deleteImageList == null)
				{
					deleteImageList = new ImageIdList();
				}
				
				deleteImageList.getImageRef().add(resultset.getLong(1));
			}
			resultset.close();
			
			return deleteImageList;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (statement != null) try { if (!statement.isClosed()) {statement.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("ImageDataHelperImpl","GetActiveImagesInCategories", startMS, requestId, "");
		}
	}
	
	public ImageMeta GetImageMeta(long userId, long imageId, String requestId)
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement psMeta = null;
		ResultSet rsMeta = null;
		PreparedStatement psTag = null;
		ResultSet rsTag = null;
		ImageMeta image = null;
		GregorianCalendar oldGreg = new GregorianCalendar();
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT I.[ImageId],[CategoryId],[Name],[Description],[OriginalFileName], "
					+ "[Format],[RecordVersion],"
					+ "[Width],[Height],[Size],[CompressedSize],[CameraMaker],[CameraModel],[Aperture],[ShutterSpeed],"
					+ "[ISO],[Orientation],[TakenDate],[TakenDateFile],[TakenDateMeta],[UploadDate],"
					+ "[UdfChar1],[UdfChar2],[UdfChar3], "
					+ "[UdfText1],[UdfNum1],[UdfNum2],[UdfNum3],[UdfDate1],[UdfDate2],[UdfDate3],[Status] "      
					+ "FROM [Image] I INNER JOIN [ImageMeta] IM ON I.ImageId = IM.ImageId "
					+ "WHERE I.ImageId = ? AND I.[UserId] = ?";
			
			String selectTagSql = "SELECT DISTINCT TagId FROM TagImage WHERE ImageId=?";
			
			psMeta = conn.prepareStatement(selectSql);
			psMeta.setLong(1, imageId);
			psMeta.setLong(2, userId);
			rsMeta = psMeta.executeQuery();

			if (!rsMeta.next())
			{
				return null;
			}
			
			image = new ImageMeta();
			image.setId(rsMeta.getLong(1));
			image.setCategoryId(rsMeta.getLong(2));
			image.setName(rsMeta.getString(3));
			image.setDesc(rsMeta.getString(4));
			image.setOriginalFileName(rsMeta.getString(5));
			image.setFormat(rsMeta.getString(6));
			image.setVersion(rsMeta.getInt(7));
			
			image.setWidth(rsMeta.getInt(8));
			image.setHeight(rsMeta.getInt(9));
			image.setSize(rsMeta.getLong(10));
			image.setCompressedSize(rsMeta.getLong(11));
			image.setCameraMaker(rsMeta.getString(12));
			image.setCameraModel(rsMeta.getString(13));
			image.setAperture(rsMeta.getString(14));
			image.setShutterSpeed(rsMeta.getString(15));
			
			int iso = rsMeta.getInt(16);
			if (iso > 0)
				image.setISO(iso);
			
			image.setOrientation(rsMeta.getInt(17));

			Calendar calendar = Calendar.getInstance();
			if (rsMeta.getDate(18) != null)
			{
				Calendar takenDateCalendar = Calendar.getInstance();
				takenDateCalendar.setTimeInMillis(rsMeta.getTimestamp(18).getTime());
				image.setTakenDate(takenDateCalendar);
			}
			if (rsMeta.getDate(19) != null)
			{
				Calendar takenDateFileCalendar = Calendar.getInstance();
				takenDateFileCalendar.setTimeInMillis(rsMeta.getTimestamp(19).getTime());
				image.setTakenDateFile(takenDateFileCalendar);
			}
			if (rsMeta.getDate(20) != null)
			{
				Calendar takenDateMetaCalendar = Calendar.getInstance();
				takenDateMetaCalendar.setTimeInMillis(rsMeta.getTimestamp(20).getTime());
				image.setTakenDateMeta(takenDateMetaCalendar);
			}
			if (rsMeta.getDate(21) != null)
			{
				Calendar uploadDateCalendar = Calendar.getInstance();
				uploadDateCalendar.setTimeInMillis(rsMeta.getTimestamp(21).getTime());
				image.setUploadDate(uploadDateCalendar);
			}

			image.setUdfChar1(rsMeta.getString(22));
			image.setUdfChar2(rsMeta.getString(23));
			image.setUdfChar3(rsMeta.getString(24));
			
			image.setUdfText1(rsMeta.getString(25));
			image.setUdfNum1(rsMeta.getBigDecimal(26));
			image.setUdfNum2(rsMeta.getBigDecimal(27));
			image.setUdfNum3(rsMeta.getBigDecimal(28));
			
			if (rsMeta.getDate(29) != null)
			{
				Calendar udfDate1Calendar = Calendar.getInstance();
				udfDate1Calendar.setTimeInMillis(rsMeta.getTimestamp(29).getTime());
				image.setUdfDate1(udfDate1Calendar);
			}
			if (rsMeta.getDate(30) != null)
			{
				Calendar udfDate2Calendar = Calendar.getInstance();
				udfDate2Calendar.setTimeInMillis(rsMeta.getTimestamp(30).getTime());
				image.setUdfDate2(udfDate2Calendar);
			}
			if (rsMeta.getDate(31) != null)
			{
				Calendar udfDate3Calendar = Calendar.getInstance();
				udfDate3Calendar.setTimeInMillis(rsMeta.getTimestamp(31).getTime());
				image.setUdfDate3(udfDate3Calendar);
			}

			image.setStatus(rsMeta.getInt(32));
			
			psTag = conn.prepareStatement(selectTagSql);
			psTag.setLong(1, imageId);
			rsTag = psTag.executeQuery();
			

			while (rsTag.next())
			{
				ImageMeta.Tags.TagRef tagRef = new ImageMeta.Tags.TagRef();
				tagRef.setId(rsTag.getLong(1));
				tagRef.setOp("S");

				if (image.getTags() == null)
				{
					image.setTags(new ImageMeta.Tags());
				}
				image.getTags().getTagRef().add(tagRef);
			}
			rsTag.close();
			
			return image;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		finally {
			if (rsMeta != null) try { if (!rsMeta.isClosed()) {rsMeta.close();} } catch (SQLException logOrIgnore) {}
			if (psMeta != null) try { if (!psMeta.isClosed()) {psMeta.close();} } catch (SQLException logOrIgnore) {}
			if (rsTag != null) try { if (!rsTag.isClosed()) {rsTag.close();} } catch (SQLException logOrIgnore) {}
			if (psTag != null) try { if (!psTag.isClosed()) {psTag.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("ImageDataHelperImpl","GetImageMeta", startMS, requestId, String.valueOf(imageId));
		}
	}

	public void CreateImage(long userId, ImageMeta newImage, String requestId) throws WallaException 
	{
		long startMS = System.currentTimeMillis();
		String sqlImage = "INSERT INTO [Image] ([ImageId],[CategoryId],[Name],[Description],[OriginalFileName],[Format],[Status],"
				+ "[RecordVersion],[LastUpdated],[UserAppId],[Error],[UserId]) "
				+ "VALUES (?,?,?,?,?,?,?,?,dbo.GetDateNoMS(),?,0,?)";
		
		String sqlMeta = "INSERT INTO [ImageMeta] ([ImageId],"
				+ "[Width],[Height],[Size],[CompressedSize],[TakenDate],[TakenDateFile],[UploadDate],"
				+ "[UdfChar1],[UdfChar2],[UdfChar3],[UdfText1],"
				+ "[UdfNum1],[UdfNum2],[UdfNum3],[UdfDate1],[UdfDate2],[UdfDate3]) "
				+ "VALUES (?,?,?,?,?,?,?,dbo.GetDateNoMS(),?,?,?,?,?,?,?,?,?,?)";
		
		Connection conn = null;
		PreparedStatement psImage = null;
		PreparedStatement psMeta = null;
		PreparedStatement bsTagInsert = null;

		try
		{			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main tag record.
			psImage = conn.prepareStatement(sqlImage);
			psImage.setLong(1, newImage.getId());
			psImage.setLong(2, newImage.getCategoryId());
			psImage.setString(3, newImage.getName());
			psImage.setString(4, newImage.getDesc());
			psImage.setString(5, newImage.getOriginalFileName());
			psImage.setString(6, newImage.getFormat());
			psImage.setInt(7, 2);
			psImage.setInt(8, 0);
			psImage.setLong(9, newImage.getUserAppId());
			psImage.setLong(10, userId);
			
			//Validate new record was successful.
			if (1 != psImage.executeUpdate())
			{
				conn.rollback();
				String error = "Insert statement for [Image] didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateImage", error, HttpStatus.CONFLICT.value()); 				
			}

			psMeta = conn.prepareStatement(sqlMeta);
			psMeta.setLong(1, newImage.getId());
			psMeta.setLong(2, newImage.getWidth());
			psMeta.setLong(3, newImage.getHeight());
			psMeta.setLong(4, newImage.getSize());
			psMeta.setLong(5, newImage.getCompressedSize());
			//psMeta.setLong(5, 0);
			
			Calendar aLongTimeAgo = Calendar.getInstance();
			aLongTimeAgo.set(1800, 1, 1);
			
			if (newImage.getTakenDateSet()  && newImage.getTakenDate().after(aLongTimeAgo))
			{ psMeta.setDate(6,new java.sql.Date(newImage.getTakenDate().getTimeInMillis())); }
			else
			{ psMeta.setNull(6, java.sql.Types.DATE); }
			
			if (newImage.getTakenDateFile() != null && newImage.getTakenDateFile().after(aLongTimeAgo))
			{ psMeta.setDate(7,new java.sql.Date(newImage.getTakenDateFile().getTimeInMillis())); }
			else
			{ psMeta.setNull(7, java.sql.Types.DATE); }
			
			psMeta.setString(8, newImage.getUdfChar1());
			psMeta.setString(9, newImage.getUdfChar2());
			psMeta.setString(10, newImage.getUdfChar3());
			
			psMeta.setString(11, newImage.getUdfText1());
			psMeta.setBigDecimal(12, newImage.getUdfNum1());
			psMeta.setBigDecimal(13, newImage.getUdfNum2());
			psMeta.setBigDecimal(14, newImage.getUdfNum3());
			
			if (newImage.getUdfDate1() != null)
			{ psMeta.setDate(15,new java.sql.Date(newImage.getUdfDate1().getTimeInMillis())); }
			else
			{ psMeta.setNull(15, java.sql.Types.DATE); }
			
			if (newImage.getUdfDate2() != null)
			{ psMeta.setDate(16,new java.sql.Date(newImage.getUdfDate2().getTimeInMillis())); }
			else
			{ psMeta.setNull(16, java.sql.Types.DATE); }
			
			if (newImage.getUdfDate3() != null)
			{ psMeta.setDate(17,new java.sql.Date(newImage.getUdfDate3().getTimeInMillis())); }
			else
			{ psMeta.setNull(17, java.sql.Types.DATE); }

			//Validate new record was successful.
			if (1 != psMeta.executeUpdate())
			{
				conn.rollback();
				String error = "Insert statement for [ImageMeta] didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateImage", error, HttpStatus.CONFLICT.value()); 				
			}
			
			//Add Tag references.
			if (newImage.getTags() != null)
			{
				if (newImage.getTags().getTagRef().size() > 0)
				{
					String insertTagSql = "INSERT INTO [dbo].[TagImage] ([TagId],[ImageId]) VALUES (?, ?)";
					bsTagInsert = conn.prepareStatement(insertTagSql);	
					
					for(ImageMeta.Tags.TagRef tagRef : newImage.getTags().getTagRef())
					{
						bsTagInsert.setLong(1, tagRef.getId());
						bsTagInsert.setLong(2, newImage.getId());
						bsTagInsert.addBatch();
					}
					
					bsTagInsert.executeBatch();
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
	        if (psImage != null) try { psImage.close(); } catch (SQLException logOrIgnore) {}
	        if (psMeta != null) try { psMeta.close(); } catch (SQLException logOrIgnore) {}
	        if (bsTagInsert != null) try { bsTagInsert.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("ImageDataHelperImpl","CreateImage", startMS, requestId, String.valueOf(newImage.getId()));
		}
	}

	public void UpdateImage(long userId, ImageMeta existingImage, String requestId) throws WallaException 
	{
		long startMS = System.currentTimeMillis();
		String sqlImage = "UPDATE [Image] SET [Name] = ?,[Description] = ?, [RecordVersion] = [RecordVersion] + 1, [LastUpdated] = dbo.GetDateNoMS() "
				+ "WHERE [ImageId] = ? AND [UserId] = ? AND [RecordVersion]= ?";
						
		String sqlMeta = "UPDATE [ImageMeta] SET [Width] = ?,[Height] = ?,[Size] = ?,[CompressedSize] = ?,"
				+ "[CameraMaker] = ?,[CameraModel] = ?,[Aperture] = ?,[ShutterSpeed] = ?,"
				+ "[ISO] = ?,[Orientation] = ?,[TakenDate] = ?,[TakenDateFile] = ?,[TakenDateMeta] = ?,"
				+ "[UdfChar1] = ?,[UdfChar2] = ?,[UdfChar3] = ?,[UdfText1] = ?,[UdfNum1] = ?,[UdfNum2] = ?,"
				+ "[UdfNum3] = ?,[UdfDate1] = ?,[UdfDate2] = ?,[UdfDate3] = ? "
				+ "WHERE [ImageId] = ?";
		
		Connection conn = null;
		PreparedStatement psImage = null;
		PreparedStatement psMeta = null;
		PreparedStatement bsTagInsert = null;
		PreparedStatement bsTagDelete = null;
		
		try 
		{			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main tag record.
			psImage = conn.prepareStatement(sqlImage);
			
			psImage.setString(1, existingImage.getName());
			psImage.setString(2, existingImage.getDesc());
			psImage.setLong(3, existingImage.getId());
			psImage.setLong(4, userId);
			psImage.setInt(5, existingImage.getVersion());
			
			//Validate new record was successful.
			if (1 != psImage.executeUpdate())
			{
				conn.rollback();
				String error = "Update statement for [Image] didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "UpdateImage", error, HttpStatus.CONFLICT.value()); 				
			}

			psMeta = conn.prepareStatement(sqlMeta);

			psMeta.setLong(1, existingImage.getWidth());
			psMeta.setLong(2, existingImage.getHeight());
			psMeta.setLong(3, existingImage.getSize());
			psMeta.setLong(4,  existingImage.getCompressedSize());
			psMeta.setString(5, existingImage.getCameraMaker());
			psMeta.setString(6, existingImage.getCameraModel());
			psMeta.setString(7, existingImage.getAperture());
			psMeta.setString(8, existingImage.getShutterSpeed());
			psMeta.setInt(9, existingImage.getISO());
			psMeta.setInt(10, existingImage.getOrientation());
			
			Calendar aLongTimeAgo = Calendar.getInstance();
			aLongTimeAgo.set(1800, 1, 1);
			
			if (existingImage.getTakenDate() != null && existingImage.getTakenDate().after(aLongTimeAgo))
			{ psMeta.setDate(11,new java.sql.Date(existingImage.getTakenDate().getTimeInMillis())); }
			else
			{ psMeta.setNull(11, java.sql.Types.DATE); }
			
			
			if (existingImage.getTakenDateFile() != null && existingImage.getTakenDateFile().after(aLongTimeAgo))
			{ psMeta.setDate(12,new java.sql.Date(existingImage.getTakenDateFile().getTimeInMillis())); }
			else
			{ psMeta.setNull(12, java.sql.Types.DATE); }
			
			if (existingImage.getTakenDateMeta() != null && existingImage.getTakenDateMeta().after(aLongTimeAgo))
			{ psMeta.setDate(13,new java.sql.Date(existingImage.getTakenDateMeta().getTimeInMillis())); }
			else
			{ psMeta.setNull(13, java.sql.Types.DATE); }
			
			psMeta.setString(14, existingImage.getUdfChar1());
			psMeta.setString(15, existingImage.getUdfChar2());
			psMeta.setString(16, existingImage.getUdfChar3());
			psMeta.setString(17, existingImage.getUdfText1());
			psMeta.setBigDecimal(18, existingImage.getUdfNum1());
			psMeta.setBigDecimal(19, existingImage.getUdfNum2());
			psMeta.setBigDecimal(20, existingImage.getUdfNum3());
			
			if (existingImage.getUdfDate1() != null && existingImage.getUdfDate1().after(aLongTimeAgo))
			{ psMeta.setDate(21,new java.sql.Date(existingImage.getUdfDate1().getTimeInMillis())); }
			else
			{ psMeta.setNull(21, java.sql.Types.DATE); }
			
			if (existingImage.getUdfDate2() != null && existingImage.getUdfDate2().after(aLongTimeAgo))
			{ psMeta.setDate(22,new java.sql.Date(existingImage.getUdfDate2().getTimeInMillis())); }
			else
			{ psMeta.setNull(22, java.sql.Types.DATE); }
			
			if (existingImage.getUdfDate3() != null && existingImage.getUdfDate3().after(aLongTimeAgo))
			{ psMeta.setDate(23,new java.sql.Date(existingImage.getUdfDate3().getTimeInMillis())); }
			else
			{ psMeta.setNull(23, java.sql.Types.DATE); }
			
			psMeta.setLong(24, existingImage.getId());
			
			//Validate new record was successful.
			if (1 != psMeta.executeUpdate())
			{
				conn.rollback();
				String error = "Update statement for [ImageMeta] didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "UpdateImage", error, HttpStatus.CONFLICT.value()); 				
			}

			//Add Tag references.
			if (existingImage.getTags() != null)
			{
				if (existingImage.getTags().getTagRef().size() > 0)
				{
					String insertTagSql = "INSERT INTO [dbo].[TagImage] ([TagId],[ImageId]) VALUES (?, ?)";
					String deleteTagSql = "DELETE FROM [dbo].[TagImage] WHERE [TagId] = ? AND [ImageId]= ?";
					
					bsTagInsert = conn.prepareStatement(insertTagSql);
					bsTagDelete = conn.prepareStatement(deleteTagSql);
					
					for(ImageMeta.Tags.TagRef tagRef : existingImage.getTags().getTagRef())
					{
						if (tagRef.getOp().equals("C"))
						{
							bsTagInsert.setLong(1, tagRef.getId());
							bsTagInsert.setLong(2, existingImage.getId());
							bsTagInsert.addBatch();
						}
						else if (tagRef.getOp().equals("D"))
						{
							bsTagDelete.setLong(1, tagRef.getId());
							bsTagDelete.setLong(2, existingImage.getId());
							bsTagDelete.addBatch();
						}
						else if (tagRef.getOp().equals("S"))
						{
							//Just ignore.
						}
						else
						{
							conn.rollback();
							String error = "TagRef didn't have a valid operation";
							meLogger.error(error);
							throw new WallaException("ImageDataHelperImpl", "UpdateImage", error, 0); 
						}
					}
					
					//Perform updates.
					bsTagInsert.executeBatch();
					bsTagDelete.executeBatch();
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
	        if (psImage != null) try { psImage.close(); } catch (SQLException logOrIgnore) {}
	        if (psMeta != null) try { psMeta.close(); } catch (SQLException logOrIgnore) {}
	        if (bsTagInsert != null) try { bsTagInsert.close(); } catch (SQLException logOrIgnore) {}
	        if (bsTagDelete != null) try { bsTagDelete.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("ImageDataHelperImpl","UpdateImage", startMS, requestId, String.valueOf(existingImage.getId()));
		}		
	}
	
	public long[] GetTagsLinkedToImages(long userId, long[] imageList, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement statement = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			if (imageList != null && imageList.length > 0)
			{
				String sql = "SELECT DISTINCT TI.TagId FROM TagImage TI INNER JOIN Tag T ON TI.TagId = T.TagId "
						+ "WHERE T.UserId=" + userId + " AND TI.ImageId IN (";
				
				statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

				//Construct update IN statement
				//for (Iterator<ImageList.Images.ImageRef> imageIterater = imageList.getImages().getImageRef().iterator(); imageIterater.hasNext();)
				//{
				//	ImageList.Images.ImageRef currentImageRef = (ImageList.Images.ImageRef)imageIterater.next();
				//	sql = sql + currentImageRef.getId() + ",";
				//}
				
				for (int i = 0; i < imageList.length; i++)
				{
					sql = sql + imageList[i] + ",";
				}
				
				sql = sql.substring(0, sql.length()-1) + ")";
				
				resultset = statement.executeQuery(sql);
				
				int size = 0;
				try {
					resultset.last();
				    size = resultset.getRow();
				    resultset.beforeFirst();
				}
				catch(Exception ex) {}
				
				long[] tagIds = new long[size];
				for (int i = 0; i < size; i++)
				{
					resultset.next();
					tagIds[i] = resultset.getLong(1);
				}
				
				resultset.close();
				return tagIds;	
			}
			return new long[0];
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (statement != null) try { if (!statement.isClosed()) {statement.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("ImageDataHelperImpl","GetTagsLinkedToImages", startMS, requestId, "");
		}
	}
	
	public long[] GetCategoriesLinkedToImages(long userId, long[] imageList, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement statement = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			if (imageList != null && imageList.length > 0)
			{
				String sql = "SELECT DISTINCT C.CategoryId FROM Category C INNER JOIN Image I ON C.CategoryId = I.CategoryId "
						+ "WHERE C.UserId=" + userId + " AND C.[Active] = 1 AND I.ImageId IN (";
				
				statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

				//Construct update IN statement
				//for (Iterator<ImageList.Images.ImageRef> imageIterater = imageList.getImages().getImageRef().iterator(); imageIterater.hasNext();)
				//{
				//	ImageList.Images.ImageRef currentImageRef = (ImageList.Images.ImageRef)imageIterater.next();
				//	sql = sql + currentImageRef.getId() + ",";
				//}
				
				for (int i = 0; i < imageList.length; i++)
				{
					sql = sql + imageList[i] + ",";
				}
				
				sql = sql.substring(0, sql.length()-1) + ")";
				
				resultset = statement.executeQuery(sql);
				
				int size = 0;
				try {
					resultset.last();
				    size = resultset.getRow();
				    resultset.beforeFirst();
				}
				catch(Exception ex) {}
				
				long[] categoryIds = new long[size];
				for (int i = 0; i < size; i++)
				{
					resultset.next();
					categoryIds[i] = resultset.getLong(1);
				}
				
				resultset.close();
				return categoryIds;	
			}
			return new long[0];
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (statement != null) try { if (!statement.isClosed()) {statement.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("ImageDataHelperImpl","GetCategoriesLinkedToImages", startMS, requestId, "");
		}
	}

	public void UpdateImageStatus(long userId, long imageId, int status, boolean error, String errorMessage, String requestId) throws WallaException
	{
		//check new status is previous status + 1.
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		String updateSql = "";
		
		try {
			int returnCount = 0;		
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			if (error)
			{
				updateSql = "UPDATE [Image] SET [RecordVersion] = [RecordVersion] + 1, [LastUpdated] = dbo.GetDateNoMS(), "
						+ "[ErrorMessage] = ?, [Error] = 1 "
						+ "WHERE ImageId = ? AND [UserId] = ?";
				
				if (errorMessage.length() > 200)
					errorMessage = errorMessage.substring(1, 200);
				
				ps = conn.prepareStatement(updateSql);
				ps.setString(1, errorMessage);
				ps.setLong(2, imageId);
				ps.setLong(3, userId);
			}
			else
			{
				updateSql = "UPDATE [Image] SET [RecordVersion] = [RecordVersion] + 1, [LastUpdated] = dbo.GetDateNoMS(), "
						+ "[Status] = ? "
						+ "WHERE ImageId = ? AND [UserId] = ? AND [Status] = ?";
				
				ps = conn.prepareStatement(updateSql);
				ps.setInt(1, status);
				ps.setLong(2, imageId);
				ps.setLong(3, userId);
				ps.setInt(4, status-1);
			}
				
			returnCount = ps.executeUpdate();
			ps.close();
			
			if (returnCount != 1)
			{
				conn.rollback();
				String message = "Update status didn't return a success count of 1.";
				throw new WallaException("ImageDataHelperImpl", "UpdateImageStatus", message, 0); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx, HttpStatus.INTERNAL_SERVER_ERROR.value());
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("ImageDataHelperImpl","UpdateImageStatus", startMS, requestId, String.valueOf(imageId));
		}
	}
	
}
