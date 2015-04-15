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
import java.util.Calendar;
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
			
			String sprocSql = "EXEC [dbo].[SetupNewUser] ?, ?, ?, ?, ?, ?, ?, ?, ?";
				
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
			    
			return createSproc.getLong(9);
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

	public void UpdatePassword(long userId, String passwordHash, String salt) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {			
			int returnCount = 0;
			String updateSql = null;
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			updateSql = "UPDATE [dbo].[User] SET [FailedLoginCount] = 0, [FailedLoginLast] = NULL, "
					+ " [PasswordHash] = ?, [Salt] = ?, [PasswordChangeDate] = GetDate() WHERE [UserId] = ?";

			ps = conn.prepareStatement(updateSql);
			ps.setString(1, passwordHash);
			ps.setString(2, salt);
			ps.setLong(3, userId);

			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "UpdatePassword", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		}
		catch (Exception ex) {
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("UpdatePassword", meLogger, startMS, String.valueOf(userId));
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
					break;
				case Closed:
					updateSql = "UPDATE [User] SET [RecordVersion] = [RecordVersion] + 1, [Status] = 5 "
							+ "WHERE UserId = ? AND [Status] = 4";
					break;
			}
			 
			ps = conn.prepareStatement(updateSql);
			ps.setLong(1, userId);
			
			returnCount = ps.executeUpdate();
			ps.close();
			
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update status didn't return a success count of 1.";
				meLogger.error(error);
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

	public String ShouldAccountBeLive(long userId) throws WallaException
	{
		//Checks to see if bank details and email are OK, and account can accept images.
		//TODO add banking stuff.
		//TODO check image limit.
		
		
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();
			
			String selectSql = "SELECT COUNT(1) FROM [Email] WHERE [UserId] = ? AND [Verified] = 1 AND [Principle] = 1 AND [Active] = 1";
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			
			resultset = ps.executeQuery();

			if (!resultset.next())
			{
				String error = "No record returned from count sql: " + selectSql;
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "ShouldAccountBeLive", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}

			if (resultset.getInt(1) != 1)
				return "Principle email address is not valid.  Go to the account tab for email setup to enter new details.";
			
			//TODO Change to compressed size.
			selectSql = "SELECT CASE WHEN CAST([StorageGBLimit] as float) > CAST([SizeGB] AS float) " +
					" THEN 0 ELSE 1 END AS ISOVER FROM [AccountStorageSummary] WHERE [UserId] = ?";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			
			resultset = ps.executeQuery();
			if (!resultset.next())
			{
				String error = "No record returned from sql: " + selectSql;
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "ShouldAccountBeLive", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
			}

			if (resultset.getInt(1) == 0)
				return "OK";
			else
				return "You have reached the maximum upload size for your account.  Go to the account storage tab to view your current usage.";
			
			
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx);
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("ShouldAccountBeLive", meLogger, startMS, String.valueOf(userId));
		}
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

	public void AddEmail(long userId, String email, boolean principle, boolean secondary) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		String sql = "INSERT INTO [Email] ([UserId],[Address],[Active],[Principle],[Secondary],[Verified])"
				+ " VALUES (?,?,1,?,?,0)";

		Connection conn = null;
		PreparedStatement ps = null;
		try {			
			int returnCount = 0;

			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Insert main tag record.
			ps = conn.prepareStatement(sql);
			ps.setLong(1, userId);
			ps.setString(2, email);
			ps.setBoolean(3, principle);
			ps.setBoolean(4, secondary);
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "AddEmail", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
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
	        UserTools.LogMethod("AddEmail", meLogger, startMS, String.valueOf(userId) + " " + email);
		}
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
					meLogger.error(error);
					throw new WallaException("AccountDataHelperImpl", "UpdateEmail", error, HttpStatus.CONFLICT.value()); 
				}
				
				conn.commit();
			}
			else
			{
				String error = "Incorrect email status update was used";
				meLogger.error(error);
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
	
	public Account GetAccountMeta(long userId)
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		Account account = null;
		
		try {
			conn = dataSource.getConnection();

			String selectSql = "SELECT U.[ProfileName],U.[Description],U.[Country],U.[Timezone],U.[Newsletter], "
					+ "U.[Status],AT.[AccountType],AT.[Name],U.[PasswordChangeDate],U.[OpenDate],U.[CloseDate],U.[RecordVersion] "
					+ "FROM [User] U INNER JOIN [dbo].[AccountType] AT ON U.AccountType = AT.AccountType "
					+ "WHERE U.[UserId] = ?";

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
			account.setCountry(resultset.getString(3));
			account.setTimezone(resultset.getString(4));
			account.setNewsletter(resultset.getBoolean(5));
			
			account.setStatus(resultset.getInt(6));
			account.setAccountType(resultset.getInt(7));
			account.setAccountTypeName(resultset.getString(8));
			
			GregorianCalendar oldGreg = new GregorianCalendar();
			XMLGregorianCalendar xmlOldGreg = null;
			
			if (resultset.getTimestamp(9) != null)
			{
				Calendar passwordChangeCalendar = Calendar.getInstance();
				passwordChangeCalendar.setTimeInMillis(resultset.getTimestamp(9).getTime());
				account.setPasswordChangeDate(passwordChangeCalendar);
			}
			
			if (resultset.getTimestamp(10) != null)
			{
				Calendar openDateCalendar = Calendar.getInstance();
				openDateCalendar.setTimeInMillis(resultset.getTimestamp(10).getTime());
				account.setOpenDate(openDateCalendar);
			}
			
			if (resultset.getTimestamp(11) != null)
			{
				Calendar closeDateCalendar = Calendar.getInstance();
				closeDateCalendar.setTimeInMillis(resultset.getTimestamp(11).getTime());
				account.setCloseDate(closeDateCalendar);
			}
			
			account.setVersion(resultset.getInt(12));
			
			resultset.close();
			ps.close();


			selectSql = "SELECT [Address],[Principle],[Secondary],[Verified] "
					  + "FROM [Email] WHERE [Active] = 1 AND [UserId] = ? ORDER BY [Principle], [Secondary]";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			resultset = ps.executeQuery();

			account.setEmails(new Account.Emails());
			while (resultset.next())
			{
				Account.Emails.EmailRef email = new Account.Emails.EmailRef();
				email.setAddress(resultset.getString(1));
				email.setPrinciple(resultset.getBoolean(2));
				email.setSecondary(resultset.getBoolean(3));
				email.setVerified(resultset.getBoolean(4));
				
				account.getEmails().getEmailRef().add(email);
			}
			
			resultset.close();
			ps.close();
			
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
	        UserTools.LogMethod("GetAccountMeta", meLogger, startMS, String.valueOf(userId));
		}
	}

	public AccountStorage GetAccountStorage(long userId)
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		AccountStorage accountStorage = null;
		
		try {
			conn = dataSource.getConnection();

			String selectSql = "SELECT [StorageGBLimit], [MonthlyUploadCap], [UploadCount30Days], [ImageCount], "
					+ "[SizeGB], [CompressedSizeGB] FROM [AccountStorageSummary] WHERE [UserId] = ?";

			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);

			resultset = ps.executeQuery();

			if (!resultset.next())
				return null;
			
			accountStorage = new AccountStorage();
			accountStorage.setId(userId);
			accountStorage.setStorageGBLimit(resultset.getDouble(1));
			accountStorage.setMonthlyUploadCap(resultset.getInt(2));
			accountStorage.setUploadCount30Days(resultset.getInt(3));
			accountStorage.setImageCount(resultset.getInt(4));
			accountStorage.setSizeGB(resultset.getDouble(5));
			accountStorage.setCompressedSizeGB(resultset.getDouble(6));

			resultset.close();
			ps.close();

			/**************************************************************************/
			/* Add format summary */
			selectSql = "SELECT [Format], ImageCount, SizeGB, CompressedSizeGB FROM AccountStorageFormat "
						+ "WHERE UserId = ? ORDER BY 3 desc";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			resultset = ps.executeQuery();

			while (resultset.next())
			{
				AccountStorage.FormatRef format = new AccountStorage.FormatRef();
				format.setFormat(resultset.getString(1));
				format.setImageCount(resultset.getInt(2));
				format.setSizeGB(resultset.getDouble(3));
				format.setCompressedSizeGB(resultset.getDouble(4));
				
				accountStorage.getFormatRef().add(format);
			}
			
			resultset.close();
			ps.close();
			
			/**************************************************************************/
			/* Add upload source summary */
			selectSql = "SELECT UploadSource, ImageCount, SizeGB, CompressedSizeGB FROM AccountStorageSource "
						+ "WHERE UserId = ? ORDER BY 3 desc";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			resultset = ps.executeQuery();

			while (resultset.next())
			{
				AccountStorage.UploadSourceRef uploadSource = new AccountStorage.UploadSourceRef();
				uploadSource.setName(resultset.getString(1));
				uploadSource.setImageCount(resultset.getInt(2));
				uploadSource.setSizeGB(resultset.getDouble(3));
				uploadSource.setCompressedSizeGB(resultset.getDouble(4));
				
				accountStorage.getUploadSourceRef().add(uploadSource);
			}
			
			resultset.close();
			ps.close();
			
			/**************************************************************************/
			/* Add format summary */
			selectSql = "SELECT [Year], ImageCount, SizeGB, CompressedSizeGB FROM AccountStorageYear "
						+ "WHERE UserId = ? ORDER BY 1";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			resultset = ps.executeQuery();

			while (resultset.next())
			{
				AccountStorage.ImageYearRef imageYear = new AccountStorage.ImageYearRef();
				imageYear.setYear(resultset.getString(1));
				imageYear.setImageCount(resultset.getInt(2));
				imageYear.setSizeGB(resultset.getDouble(3));
				imageYear.setCompressedSizeGB(resultset.getDouble(4));
				
				accountStorage.getImageYearRef().add(imageYear);
			}
			
			resultset.close();
			ps.close();
			
			return accountStorage;
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
	        UserTools.LogMethod("GetAccountStorage", meLogger, startMS, String.valueOf(userId));
		}
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
			
			//TODO check when object is retrieved for an app.
			//if (resultset.getBoolean(5))
			//{
		    //	String error = "User app has been explicitly blocked, request cannot continue.";
			//	meLogger.error(error);
			//	throw new WallaException(this.getClass().getName(), "GetUserApp", error, HttpStatus.FORBIDDEN.value());
			//}
			
			userApp = new UserApp();
			userApp.setId(userAppId);
			userApp.setAppId(resultset.getInt(1));
			userApp.setPlatformId(resultset.getInt(2));
			userApp.setMachineName(resultset.getString(3));

			Calendar lastUsedCalendar = Calendar.getInstance();
			lastUsedCalendar.setTimeInMillis(resultset.getTimestamp(4).getTime());
			userApp.setLastUsed(lastUsedCalendar);
			
			userApp.setBlocked(resultset.getBoolean(5));
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
		catch (SQLException ex) {
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

	public void UserAppBlockUnblock(long userId, long userAppId, boolean block) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		
		try {
			int returnCount = 0;		
			
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			String updateSql = "UPDATE [UserApp] SET Blocked = ?, [RecordVersion] = [RecordVersion] + 1 WHERE [UserId] = ? AND [UserAppId] = ?";

			ps = conn.prepareStatement(updateSql);
			ps.setBoolean(1, block);
			ps.setLong(2, userId);
			ps.setLong(3, userAppId);
			
			returnCount = ps.executeUpdate();
			ps.close();
			
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update status didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("ImageDataHelperImpl", "UserAppBlockUnblock", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
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
	        UserTools.LogMethod("UserAppBlockUnblock", meLogger, startMS, String.valueOf(userAppId));
		}
	}
	
	public LogonState GetLogonState(String userName)
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		LogonState logonState = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [UserId], [ProfileName], [PasswordHash], [Salt], [TempSalt], [FailedLoginCount], [FailedLoginLast] "
								+ "FROM [dbo].[User] WHERE [ProfileName] = ? AND [Status] < 5";
							
			ps = conn.prepareStatement(selectSql);
			ps.setString(1, userName);
			//ps.setString(2, email);

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
			logonState.setTempSalt(resultset.getString(5));
			logonState.setFailedLogonCount(resultset.getInt(6));
			
			if (resultset.getTimestamp(7) != null)
				logonState.setFailedLogonLast(resultset.getTimestamp(7));
			
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
	        UserTools.LogMethod("GetLogonState", meLogger, startMS, userName);
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
	
	public void UpdateTempSalt(long userId, String salt) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		int returnCount = 0;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			//Process an update to the main record.
			String updateVersionSql = "UPDATE [dbo].[User] SET [TempSalt] = ? "
					+ "WHERE [UserId] = ?";

			ps = conn.prepareStatement(updateVersionSql);
			ps.setString(1, salt);
			ps.setLong(2, userId);
			
			//Execute update and check response.
			returnCount = ps.executeUpdate();
			ps.close();
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Update statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException("AccountDataHelperImpl", "UpdateTempSalt", error, HttpStatus.CONFLICT.value()); 
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
			throw new WallaException(sqlEx,HttpStatus.INTERNAL_SERVER_ERROR.value());
		} catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			throw ex;
		}
		finally {
	        if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        UserTools.LogMethod("UpdateTempSalt", meLogger, startMS, String.valueOf(userId));
		}
	}
	
	public AccountActionSummary GetAccountActions(long userId)
	{
		/*
		SELECT ACT.[Description] as ActionDesc, AA.[ActionDateTime], COUNT(1) AS NumTimes
		FROM [ActionAccount] AA 
		INNER JOIN [Action] ACT ON AA.[Action] = ACT.[Action]
		WHERE AA.[ActionDateTime] > GetDate()-30
		AND AA.[UserId] = 100001
		GROUP BY ACT.[Description], AA.[ActionDateTime]
		ORDER BY 3 DESC


		SELECT A.[Name], P.ShortName, UA.[MachineName], UA.[Blocked], UA.[LastUsed]
		FROM [UserApp] UA
		INNER JOIN [App] A ON UA.[AppId] = A.[AppId]
		INNER JOIN [Platform] P ON UA.[PlatformId] = P.[PlatformId]
		WHERE UA.[UserId] = 100001


		SELECT ACT.[Description] as ActionDesc, AU.[ActionDateTime], COUNT(1) AS NumTimes
		FROM [ActionUserApp] AU
		INNER JOIN [Action] ACT ON AU.[Action] = ACT.[Action]
		WHERE AU.[ActionDateTime] > GetDate()-30
		AND AU.[UserAppId] = 12345678
		GROUP BY ACT.[Description], AU.[ActionDateTime]


		SELECT G.[Name], G.[Description], ViewTotal.NumTimesTotal, ViewMonth.NumTimesMonth
		FROM [Gallery] G 
		INNER JOIN 
			(SELECT AG.[GalleryId], COUNT(1) AS NumTimesTotal
			FROM [ActionGallery] AG
			WHERE AG.[Action] = 'GalViewOK'
			GROUP BY AG.GalleryId) ViewTotal
			ON G.[GalleryId] = ViewTotal.[GalleryId]
		INNER JOIN 
			(SELECT AG2.[GalleryId], COUNT(1) AS NumTimesMonth
			FROM [ActionGallery] AG2
			WHERE AG2.[Action] = 'GalViewOK' AND AG2.[ActionDateTime] > GetDate()-30
			GROUP BY AG2.GalleryId) ViewMonth
			ON G.[GalleryId] = ViewMonth.[GalleryId]
		WHERE 
			G.[AccessType] IN (1,2) 
			AND G.[UserId] = 100001	
	*/
		
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		AccountActionSummary summary = null;
		
		try {
			conn = dataSource.getConnection();

			/**************************************************************************/
			/* Add account summary */
			String selectSql = "SELECT ACT.[Description] as ActionDesc, CAST(AA.[ActionDateTime] AS Date) AS ActionDate, COUNT(1) AS NumTimes" +
								" FROM [ActionAccount] AA" +
								" INNER JOIN [Action] ACT ON AA.[Action] = ACT.[Action]" +
								" WHERE AA.[ActionDateTime] > GetDate()-30" +
								" AND AA.[UserId] = ?" +
								" GROUP BY ACT.[Description], CAST(AA.[ActionDateTime] AS Date)" +
								" ORDER BY 3 DESC";

			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			resultset = ps.executeQuery();

			summary = new AccountActionSummary();
			
			while (resultset.next())
			{
				AccountActionSummary.AccountActionRef current = new AccountActionSummary.AccountActionRef();
				current.setActionDesc(resultset.getString(1));
				
				Calendar actionUsedCalendar = Calendar.getInstance();
				actionUsedCalendar.setTimeInMillis(resultset.getTimestamp(2).getTime());
				current.setActionDate(actionUsedCalendar);

				current.setNumTimes(resultset.getInt(3));
				
				summary.getAccountActionRef().add(current);
			}
			
			resultset.close();
			ps.close();

			/**************************************************************************/
			/* Add gallery summary */
			selectSql = "SELECT G.[Name], G.[Description], COALESCE(ViewTotal.NumTimesTotal,0) AS NumTimesTotal, COALESCE(ViewMonth.NumTimesMonth,0) AS NumTimesTotal " +
						"FROM [Gallery] G " +
						"LEFT OUTER JOIN " +
						"(SELECT AG.[GalleryId], COUNT(1) AS NumTimesTotal " +
						"FROM [ActionGallery] AG " +
						"WHERE AG.[Action] = 'GalViewOK' " +
						"GROUP BY AG.GalleryId) ViewTotal " +
						"ON G.[GalleryId] = ViewTotal.[GalleryId] " +
						"LEFT OUTER JOIN " +
						"(SELECT AG2.[GalleryId], COUNT(1) AS NumTimesMonth " +
						"FROM [ActionGallery] AG2 " +
						"WHERE AG2.[Action] = 'GalViewOK' AND AG2.[ActionDateTime] > GetDate()-30 " +
						"GROUP BY AG2.GalleryId) ViewMonth " +
						"ON G.[GalleryId] = ViewMonth.[GalleryId] " +
						"WHERE " +
						"G.[AccessType] IN (1,2) " +
						"AND G.[UserId] = ?";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			resultset = ps.executeQuery();

			while (resultset.next())
			{
				AccountActionSummary.GalleryActionRef current = new AccountActionSummary.GalleryActionRef();
				current.setGalleryName(resultset.getString(1));
				current.setGalleryDesc(resultset.getString(2));
				current.setViewMonth(resultset.getInt(3));
				current.setViewTotal(resultset.getInt(4));
				
				summary.getGalleryActionRef().add(current);
			}
			
			resultset.close();
			ps.close();
			
			/**************************************************************************/
			/* Add UserApps */
			selectSql = "SELECT UA.[UserAppId], A.[Name], P.ShortName, UA.[MachineName], UA.[Blocked], UA.[LastUsed] " +
						"FROM [UserApp] UA " +
						"INNER JOIN [App] A ON UA.[AppId] = A.[AppId] " +
						"INNER JOIN [Platform] P ON UA.[PlatformId] = P.[PlatformId] " +
						"WHERE UA.[UserId] = ? " +
						"ORDER BY 5";
			
			ps = conn.prepareStatement(selectSql);
			ps.setLong(1, userId);
			resultset = ps.executeQuery();

			while (resultset.next())
			{
				AccountActionSummary.UserAppRef current = new AccountActionSummary.UserAppRef();
				current.setUserAppId(resultset.getLong(1));
				current.setAppName(resultset.getString(2));
				current.setPlatform(resultset.getString(3));
				current.setMachineName(resultset.getString(4));
				current.setBlocked(resultset.getBoolean(5));
				
				Calendar lastUsedCalendar = Calendar.getInstance();
				lastUsedCalendar.setTimeInMillis(resultset.getTimestamp(6).getTime());
				current.setLastUsed(lastUsedCalendar);

				summary.getUserAppRef().add(current);
			}
			
			resultset.close();
			ps.close();
			
			/**************************************************************************/
			/* Add userapp action summary */
			selectSql = "SELECT ACT.[Description] as ActionDesc, CAST(AU.[ActionDateTime] as Date) AS ActionDateTime, COUNT(1) AS NumTimes " +
						"FROM [ActionUserApp] AU " +
						"INNER JOIN [Action] ACT ON AU.[Action] = ACT.[Action] " +
						"WHERE AU.[ActionDateTime] > GetDate()-30 " +
						"AND AU.[UserAppId] = ? " +
						"GROUP BY ACT.[Description], CAST(AU.[ActionDateTime] as Date) ORDER BY 2 DESC";
			
			for (int i = 0; i < summary.getUserAppRef().size(); i++)
			{
				AccountActionSummary.UserAppRef current = summary.getUserAppRef().get(i);
				if (!current.isBlocked())
				{
					ps = conn.prepareStatement(selectSql);
					ps.setLong(1, current.getUserAppId());
					resultset = ps.executeQuery();
		
					while (resultset.next())
					{
						AccountActionSummary.UserAppRef.UserAppActionRef currentAction = new AccountActionSummary.UserAppRef.UserAppActionRef();
						
						currentAction.setActionDesc(resultset.getString(1));
						
						Calendar lastUsedCalendar = Calendar.getInstance();
						lastUsedCalendar.setTimeInMillis(resultset.getTimestamp(2).getTime());
						currentAction.setActionDate(lastUsedCalendar);

						currentAction.setNumTimes(resultset.getInt(3));
						
						current.getUserAppActionRef().add(currentAction);
					}
					
					resultset.close();
					ps.close();
				}
			}
			
			return summary;
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
	        UserTools.LogMethod("GetAccountActions", meLogger, startMS, String.valueOf(userId));
		}
		
	}
	
	
}
