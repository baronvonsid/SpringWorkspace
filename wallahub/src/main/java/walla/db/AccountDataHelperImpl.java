package walla.db;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Repository;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.*;

import org.springframework.http.HttpStatus;

@Repository
public class AccountDataHelperImpl implements AccountDataHelper {

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(AccountDataHelperImpl.class);

	public AccountDataHelperImpl() {
		meLogger.debug("AccountDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource)
	{
		this.dataSource = dataSource;
	}
	
	public long CreateAccount(Account newAccount, String passwordHash, String salt) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		CallableStatement createSproc = null;

		try {			
			//Execute SetupNewUser 'Stanley', 'Stanley Prem', 'stanley@fotowalla.com', 'QWERTYUI', 'IUYTREW', 1

			conn = dataSource.getConnection();
			conn.setAutoCommit(true);
			
			String sprocSql = "EXEC [dbo].[SetupNewUser] ?, ?, ?, ?, ?, ?, ?";
				
			createSproc = conn.prepareCall(sprocSql);
			createSproc.setString(1, newAccount.getProfileName());
			createSproc.setString(2, newAccount.getDesc());
			createSproc.setString(3, passwordHash);
			createSproc.setString(4, salt);
			createSproc.setInt(5, newAccount.getAccountType());
			createSproc.setString(6, newAccount.getCountry());
			createSproc.setString(7, newAccount.getTimezone());
			createSproc.setBoolean(8, newAccount.isNewsletter());
			createSproc.registerOutParameter(9, Types.INTEGER);
			createSproc.execute();
			    
			return createSproc.getLong(7);
		    //if (newUserId < 1)
		    //{
		    //	String error = "SetupNewUser sproc didn't return a valid user number";
			//	meLogger.error(error);
			//	throw new WallaException(this.getClass().getName(), "CreateAccount", error, HttpStatus.INTERNAL_SERVER_ERROR.value());
		    //}
		} 
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		}
		finally {
	        if (createSproc != null) try { createSproc.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("CreateAccount", meLogger, startMS, newAccount.getProfileName());
		}
	}

	public void UpdateAccount(Account account) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			String updateSql = "UPDATE [dbo].[User] SET [Description] = ?, [Country] = ?,"
					+ "[Timezone] = ?,[Newsletter] = ?,"
					+ "[RecordVersion] = [RecordVersion] + 1 WHERE [UserId] = ? AND [RecordVersion] = ?";
			
			ps = conn.prepareStatement(updateSql);
			ps.setString(1, account.getDesc());
			ps.setString(2, account.getCountry());
			ps.setString(3, account.getTimezone());
			ps.setBoolean(4, account.isNewsletter());
			ps.setLong(5, account.getId());
			ps.setInt(6, account.getVersion());
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "UpdateAccount", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		} 
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("UpdateAccount", meLogger, startMS, String.valueOf(account.getId()));
		}
	}
	
	public void UpdateAccountStatus(long userId, AccountStatus status) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		String updateSql = null;
		
		try {
			int returnCount = 0;		
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			
			switch (status)
			{
				case Live:
					updateSql = "UPDATE [User] SET [RecordVersion] = [RecordVersion] + 1, [Status] = 2 "
							+ "WHERE UserId = ? AND [Status] IN (1,3)";
					break;
				case Frozen:
					updateSql = "UPDATE [User] SET [RecordVersion] = [RecordVersion] + 1, [Status] = 3 "
						+ "WHERE UserId = ? AND [Status] IN 2";
					break;
				case Closing:
					updateSql = "UPDATE [User] SET [RecordVersion] = [RecordVersion] + 1, [Status] = 4, [CloseDate] = GetDate() "
							+ "WHERE UserId = ? AND [Status] IN (1,2,3)";
				case Closed:
					updateSql = "UPDATE [User] SET [RecordVersion] = [RecordVersion] + 1, [Status] = 5 "
							+ "WHERE UserId = ? AND [Status] = 4";
			}
			 
			ps = conn.prepareStatement(updateSql);
			ps.setLong(1, userId);
			
			returnCount = ps.executeUpdate();
			ps.close();
			
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update status didn't return a success count of 1.";
				throw new WallaException("ImageDataHelperImpl", "UpdateMainStatus", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("UpdateAccountStatus", meLogger, startMS, String.valueOf(userId));
		}
	}

	//TODO
	public boolean ShouldAccountBeLive(long userId)
	{
		//Checks to see if bank details and email are OK, and account can accept images.
		return false;
	}
	
	public boolean ValidateEmailConfirm(long userId, String requestValidationString, CustomResponse customResponse)
	{
		//Return email address if correctly validated.
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;

		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			String sql = "SELECT E.[Address] FROM [Email] E"
			+ " WHERE E.[ValidationSent] < GetDate() - 1 AND E.[Verified] = 0 AND E.[ValidationString] = ?"
			+ " AND E.[UserId] = ?";
			
			ps = conn.prepareStatement(sql);
			ps.setString(1, requestValidationString);
			ps.setLong(2, userId);
			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				if (!resultset.isClosed()) {resultset.close();}
				if (!ps.isClosed()) {ps.close();}
				
				// Try again but check for older values.
				sql = "SELECT E.[Address] FROM [Email] E"
						+ " WHERE E.[ValidationSent] > GetDate() - 1 AND E.[Verified] = 0 AND E.[ValidationString] = ?"
						+ " AND E.[UserId] = ?";
				
				ps = conn.prepareStatement(sql);
				ps.setString(1, requestValidationString);
				ps.setLong(2, userId);
				resultset = ps.executeQuery();
				
				if (resultset.next())
				{
					customResponse.setMessage("Email address could not be validated, this link has expired.");
				}
				else
				{
					customResponse.setMessage("Email address could not be validated, the details don't match.");
				}
				return false;
			}
			
			customResponse.setMessage(resultset.getString(1));
			return true;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			customResponse.setMessage("Email address could not be validated, to an unexpected system error.");
			return false;
		} 
		catch (Exception ex) {
			meLogger.error(ex);
			customResponse.setMessage("Email address could not be validated, to an unexpected system error.");
			return false;
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("ValidateEmailConfirm", meLogger, startMS, String.valueOf(userId));
		}
	}

	//TODO
	public void AddEmail(long userId, String email, boolean principle, boolean secondary) throws WallaException
	{
		String sql = "INSERT INTO [Email] ([UserId],[Address],[Active],[Principle],[Secondary],[Verified])"
				+ " VALUES (?,?,1,0,0,0)";
			
	}
	
	public void UpdateEmail(long userId, String email, EmailAction action, String validationString) throws WallaException
	{

		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement psTwo = null;
		
		try {
			int returnCount = 0;		
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			String sql = "";
			String secondSql = "";
			
			
			if (action == EmailAction.Delete || action == EmailAction.SetupValidation || action == EmailAction.Verified)
			{
				switch (action)
				{
					case Delete:
						sql = "UPDATE [Email] SET [Active] = 0 WHERE [UserId] = ? AND [Address] = ? AND [Principle] = 0 AND [Active] = 1";
						break;
					case SetupValidation:
						sql = "UPDATE [Email] SET [ValidationString] = ?, [ValidationSent] = GetDate() WHERE [UserId] = ? AND [Address] = ? AND [Verified] = 0 AND [Active] = 1";
						break;
					case Verified:
						sql = "UPDATE [Email] SET [ValidationString] = '', [Verified] = 1 WHERE [UserId] = ? AND [Address] = ? AND [Verified] = 0 AND [Active] = 1";
						break;
				}

				ps = conn.prepareStatement(sql);
				if (action == EmailAction.SetupValidation)
				{
					ps.setString(1, validationString);
					ps.setLong(2, userId);
					ps.setString(3, email);
				}
				else
				{
					ps.setLong(1, userId);
					ps.setString(2, email);
				}
			
				returnCount = ps.executeUpdate();
				ps.close();
				
				if (returnCount != 1)
				{
					conn.rollback();
					String error = "Update email status didn't return a success count of 1.";
					throw new WallaException("AccountDataHelperImpl", "UpdateEmail", error, HttpStatus.CONFLICT.value()); 
				}
				
				conn.commit();
			}
			else if (action == EmailAction.Principle || action == EmailAction.Secondary)
			{
				if (action == EmailAction.Principle)
				{
					sql = "UPDATE [Email] SET [Principle] = 0 WHERE [UserId] = ? AND [Address] <> ? AND [Principle] = 1 AND [Active] = 1";
					secondSql = "UPDATE [Email] SET [Principle] = 1 WHERE [UserId] = ? AND [Address] = ? AND [Active] = 1";
				}
				else
				{
					sql = "UPDATE [Email] SET [Secondary] = 0 WHERE [UserId] = ? AND [Address] <> ? AND [Secondary] = 1 AND [Active] = 1";
					secondSql = "UPDATE [Email] SET [Secondary] = 1 WHERE [UserId] = ? AND [Address] = ? AND [Active] = 1";
				}
			
				ps = conn.prepareStatement(sql);
				ps.setLong(1, userId);
				ps.setString(2, email);
				returnCount = ps.executeUpdate();
				ps.close();
				
				psTwo = conn.prepareStatement(secondSql);
				psTwo.setLong(1, userId);
				psTwo.setString(2, email);
			
				returnCount = returnCount + psTwo.executeUpdate();
				psTwo.close();
				
				if (returnCount != 2)
				{
					conn.rollback();
					String error = "Update email status didn't return a success count of 2.";
					throw new WallaException("AccountDataHelperImpl", "UpdateEmail", error, HttpStatus.CONFLICT.value()); 
				}
				
				conn.commit();
			}
			else
			{
				String error = "Incorrect email status update was used";
				throw new WallaException("AccountDataHelperImpl", "UpdateEmail", error, HttpStatus.BAD_REQUEST.value()); 
			}
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (psTwo != null) try { if (!psTwo.isClosed()) {psTwo.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
		}
		UserTools.LogMethod("UpdateEmailStatus", meLogger, startMS, String.valueOf(userId));
	}
	
	public boolean EmailIsUnique(long userId, String email) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT COUNT(1) FROM [Email] WHERE [UserId] = ? AND UPPER([Address]) = UPPER(?) AND [Active] = 1";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			ps.setString(2, email);
			
			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				String error = "No record returned from count sql: " + selectSql;
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "EmailIsUnique", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}

			if (resultset.getInt(1) == 0)
				return true;
			else
				return false;
			
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("EmailIsUnique", meLogger, startMS, String.valueOf(userId) + " " + email);
		}
	}
	
	/*
	public boolean ProfileNameIsUnique(String profileName) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT 1 FROM [User] WHERE UPPER([ProfileName]) = UPPER('" + profileName + "')";
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			if (resultset.next())
			{
				return false;
			}
			else
			{
				return true;
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("ProfileNameIsUnique", meLogger, startMS, profileName);
		}
	}
	*/
	
	//TODO remove any storage, add email bits.
	public Account GetAccountMeta(long userId)
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		Account account = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [ProfileName],[Description],[Email],[Status],[AccountTypeName],[RecordVersion],[OpenDate],[CloseDate],[StorageGBLimit],"
								+ "[StorageGBCurrent],[TotalImages],[MonthlyUploadCap],[UploadCount30Days] FROM [dbo].[AccountSummary] "
								+ "WHERE [UserId] = ?";
							
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				return null;
			}
			
			account = new Account();
			account.setId(userId);
			account.setProfileName(resultset.getString(1));
			account.setDesc(resultset.getString(2));
			//account.setEmail(resultset.getString(3));
			account.setStatus(resultset.getInt(4));
			account.setAccountTypeName(resultset.getString(5));
			account.setVersion(resultset.getInt(6));
			
			GregorianCalendar oldGreg = new GregorianCalendar();
			XMLGregorianCalendar xmlOldGreg = null;
			if (resultset.getTimestamp(7) != null)
			{
				oldGreg.setTime(resultset.getTimestamp(7));
				xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
				account.setOpenDate(xmlOldGreg);
			}
			
			if (resultset.getTimestamp(8) != null)
			{
				oldGreg.setTime(resultset.getTimestamp(8));
				xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
				account.setCloseDate(xmlOldGreg);
			}
			
			account.setStorageGBLimit(resultset.getDouble(9));
			//account.setStorageGBCurrent(resultset.getDouble(10));
			//account.setTotalImages(resultset.getInt(11));
			account.setMonthlyUploadCap(resultset.getInt(12));
			account.setUploadCount30Days(resultset.getInt(13));

			return account;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		catch (Exception ex) {
			meLogger.error(ex);
			return null;
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("GetAccount", meLogger, startMS, String.valueOf(userId));
		}
	}

	//TODO implement, along with view
	public Account GetAccountStorageSummary(long userId)
	{
		
		return null;
		
	}
	
	public void CreateUserApp(long userId, UserApp userApp) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		String sql = "INSERT INTO [dbo].[UserApp]([UserAppId],[PlatformId],[AppId],[MachineName],[LastUsed],[Blocked],[TagId],[UserAppCategoryId],"
					+ "[UserDefaultCategoryId],[GalleryId],[FetchSize],[ThumbCacheMB],[MainCopyCacheMB],[MainCopyFolder],[AutoUpload],[AutoUploadFolder],[RecordVersion],[UserId])"
					+ "VALUES(?,?,?,?,dbo.GetDateNoMS(),0,?,?,?,?,?,?,?,?,?,?,1,?)";

		Connection conn = null;
		PreparedStatement ps = null;
		try {			
			int returnCount = 0;

			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main tag record.
			ps = conn.prepareStatement(sql);
			ps.setLong(1, userApp.getId());
			ps.setInt(2, userApp.getPlatformId());
			ps.setInt(3, userApp.getAppId());
			ps.setString(4, userApp.getMachineName());
			ps.setLong(5, userApp.getTagId());
			ps.setLong(6, userApp.getUserAppCategoryId());
			ps.setLong(7, userApp.getUserDefaultCategoryId());
			ps.setLong(8, userApp.getGalleryId());
			ps.setInt(9, userApp.getFetchSize());
			ps.setInt(10, userApp.getThumbCacheSizeMB());
			ps.setInt(11, userApp.getMainCopyCacheSizeMB());
			ps.setString(12, userApp.getMainCopyFolder());
			ps.setBoolean(13, userApp.isAutoUpload());
			ps.setString(14, userApp.getAutoUploadFolder());
			ps.setLong(15, userId);
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "CreateUserApp", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { ps.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("CreateUserApp", meLogger, startMS, String.valueOf(userId));
		}
	}
	
	public void UpdateUserApp(long userId, UserApp userApp) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			String updateVersionSql = null;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			updateVersionSql = "UPDATE [dbo].[UserApp] SET [MachineName] = ?,[LastUsed] = dbo.GetDateNoMS()"
								+ ",[TagId] = ?,[UserAppCategoryId] = ?,[GalleryId] = ?,[FetchSize] = ?,[ThumbCacheMB] = ?"
								+ ",[MainCopyCacheMB] = ?,[MainCopyFolder] = ?,[AutoUpload] = ?,[AutoUploadFolder] = ?,"
								+ "[RecordVersion] = [RecordVersion] + 1 WHERE [UserId] = ? AND [UserAppId] = ? AND [RecordVersion] = ?";

			ps = conn.prepareStatement(updateVersionSql);
			ps.setString(1, userApp.getMachineName());
			ps.setLong(2, userApp.getTagId());
			ps.setLong(3, userApp.getUserAppCategoryId());
			ps.setLong(4, userApp.getGalleryId());
			ps.setInt(5, userApp.getFetchSize());
			ps.setInt(6, userApp.getThumbCacheSizeMB());
			ps.setInt(7, userApp.getMainCopyCacheSizeMB());
			ps.setString(8, userApp.getMainCopyFolder());
			ps.setBoolean(9, userApp.isAutoUpload());
			ps.setString(10, userApp.getAutoUploadFolder());
			
			ps.setLong(11, userId);
			ps.setLong(12, userApp.getId());
			ps.setInt(13, userApp.getVersion());
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "UpdateUserApp", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("UpdateUserApp", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(userApp.getId()));
		}
	}
	
	public long FindExistingUserApp(long userId, int appId, int platformId, String machineName) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;

		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [UserAppId] FROM [dbo].[UserApp] WHERE [UserId] = ? AND [AppId] = ? AND [PlatformId] = ? AND [MachineName] = ?";
							
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			ps.setInt(2, appId);
			ps.setInt(3, platformId);
			ps.setString(4, machineName);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				if (meLogger.isDebugEnabled()) { meLogger.debug("Existing user app was not found for user:" + String.valueOf(userId)); }
				return 0;
			}
			
			return resultset.getLong(1);
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return 0;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("FindExistingUserApp", meLogger, startMS, String.valueOf(userId));
		}
	}
	
	public UserApp GetUserApp(long userId, long userAppId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		UserApp userApp = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [AppId],[PlatformId],[MachineName],[LastUsed],[Blocked]"
								+ ",[TagId],[UserAppCategoryId],[UserDefaultCategoryId],[GalleryId],[RecordVersion],[FetchSize]"
								+ ",[ThumbCacheMB],[MainCopyCacheMB],[MainCopyFolder],[AutoUpload],[AutoUploadFolder]"
								+ "FROM [dbo].[UserApp] WHERE [UserId] = ? AND [UserAppId] = ?";
							
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			ps.setLong(2, userAppId);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				return null;
			}
			
			if (resultset.getBoolean(5))
			{
		    	String error = "User app has been explicitly blocked, request cannot continue.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "GetUserApp", error, HttpStatus.FORBIDDEN.value());
			}
			
			userApp = new UserApp();
			userApp.setId(userAppId);
			userApp.setAppId(resultset.getInt(1));
			userApp.setPlatformId(resultset.getInt(2));
			userApp.setMachineName(resultset.getString(3));

			GregorianCalendar oldGreg = new GregorianCalendar();
			oldGreg.setTime(resultset.getTimestamp(4));
			XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(oldGreg);
			userApp.setLastUsed(xmlOldGreg);
			
			userApp.setTagId(resultset.getLong(6));
			userApp.setUserAppCategoryId(resultset.getLong(7));
			userApp.setUserDefaultCategoryId(resultset.getLong(8));
			userApp.setGalleryId(resultset.getLong(9));
			userApp.setVersion(resultset.getInt(10));
			userApp.setFetchSize(resultset.getInt(11));
			userApp.setThumbCacheSizeMB(resultset.getInt(12));
			userApp.setMainCopyCacheSizeMB(resultset.getInt(13));
			userApp.setMainCopyFolder(resultset.getString(14));
			userApp.setAutoUpload(resultset.getBoolean(15));
			userApp.setAutoUploadFolder(resultset.getString(16));

			//Update Timestamp.
			
			
			return userApp;
		}
		catch (SQLException | DatatypeConfigurationException ex) {
			meLogger.error(ex);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("GetUserApp", meLogger, startMS, String.valueOf(userId) + " " + String.valueOf(userAppId));
		}
	}

	public LogonState GetLogonState(String userName, String email)
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		LogonState logonState = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [UserId], [ProfileName], [PasswordHash], [Salt], [FailedLoginCount], [FailedLoginLast] "
								+ "FROM [dbo].[User] WHERE ([ProfileName] = ? OR [email] = ?) AND [Status] < 4";
							
			ps = conn.prepareStatement(selectSql);
			ps.setString(1, userName);
			ps.setString(2, email);

			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				return null;
			}
			
			logonState = new LogonState();
			logonState.setUserId(resultset.getLong(1));
			logonState.setProfileName(resultset.getString(2));
			logonState.setPasswordHash(resultset.getString(3));
			logonState.setSalt(resultset.getString(4));
			logonState.setFailedLogonCount(resultset.getInt(5));
			
			if (resultset.getTimestamp(6) != null)
				logonState.setFailedLogonLast(resultset.getTimestamp(6));
			
			return logonState;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("GetLogonState", meLogger, startMS, userName + " " + email);
		}
	}
	
	public void UpdateLogonState(long userId, int failedLoginCount, Date failedLoginLast) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			String updateSql = null;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			updateSql = "UPDATE [dbo].[User] SET [FailedLoginCount] = ?, [FailedLoginLast] = ? WHERE [UserId] = ?";

			ps = conn.prepareStatement(updateSql);
			ps.setInt(1, failedLoginCount);
			
			if (failedLoginLast != null)
				ps.setTimestamp(2, new java.sql.Timestamp(failedLoginLast.getTime()));
			else
				ps.setNull(2, java.sql.Types.DATE);

			ps.setLong(3, userId);

			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "UpdateLogonState", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		}
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("UpdateLogonState", meLogger, startMS, String.valueOf(userId));
		}
	}
	
}
