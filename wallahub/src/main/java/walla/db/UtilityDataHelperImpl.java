package walla.db;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.sql.DataSource;
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
public class UtilityDataHelperImpl implements UtilityDataHelper{

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(UtilityDataHelperImpl.class);

	@Resource(name="utilityServicePooled")
	private UtilityService utilityService;
	
	public List<Platform> GetPlatformList(String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [PlatformId],[ShortName],[OperatingSystem],[MachineType],[MajorVersion],[MinorVersion] FROM [Platform]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<Platform> platformList = new ArrayList<Platform>();
			
			while (resultset.next())
			{
				Platform platform = new Platform();
				platform.setPlatformId(resultset.getInt(1));
				platform.setShortName(resultset.getString(2));
				platform.setOperatingSystem(resultset.getString(3));
				platform.setMachineType(resultset.getString(4));
				platform.setMajorVersion(resultset.getInt(5));
				platform.setMinorVersion(resultset.getInt(6));
				platformList.add(platform);
			}
			resultset.close();

			return platformList;
			
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetPlatformList", startMS, requestId, "");
		}
	}

	public List<AppPlatform> GetAppPlatformList(String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {
			conn = dataSource.getConnection();

			String selectSql = "SELECT [AppId], [PlatformId] FROM [AppPlatform]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<AppPlatform> appPlatformList = new ArrayList<AppPlatform>();
			
			while (resultset.next())
			{
				AppPlatform appPlatform = new AppPlatform();
				appPlatform.setAppId(resultset.getInt(1));
				appPlatform.setPlatformId(resultset.getInt(2));
				appPlatformList.add(appPlatform);
			}
			resultset.close();

			return appPlatformList;
			
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetAppPlatformList", startMS, requestId, "");
		}
	}
	
	public List<App> GetAppList(String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [AppId],[Name],[AppKey],[MajorVersion],[MinorVersion],[AppCRC],[Status],[UserMessage],[DefaultFetchSize],[DefaultThumbCacheMB],[DefaultMainCopyCacheMB],[DefaultGalleryType] FROM [App]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<App> appList = new ArrayList<App>();
			
			while (resultset.next())
			{
				App app = new App();
				app.setAppId(resultset.getInt(1));
				app.setName(resultset.getString(2));
				app.setAppKey(resultset.getString(3));
				app.setMajorVersion(resultset.getInt(4));
				app.setMinorVersion(resultset.getInt(5));
				app.setAppCRC(resultset.getLong(6));
				app.setStatus(resultset.getInt(7));
				app.setUserMessage(resultset.getString(8));
				app.setDefaultFetchSize(resultset.getInt(9));
				app.setDefaultThumbCacheMB(resultset.getInt(10));
				app.setDefaultMainCopyCacheMB(resultset.getInt(11));
				app.setDefaultGalleryType(resultset.getInt(12));
				appList.add(app);
			}
			resultset.close();

			return appList;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetAppList", startMS, requestId, "");
		}
	}
	
	public List<Style> GetStyleList(String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [StyleId],[Name],[Description],[CssFolder],[LastUpdated]  FROM [dbo].[GalleryStyle]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<Style> styleList = new ArrayList<Style>();
			
			while (resultset.next())
			{
				Style style = new Style();
				style.setStyleId(resultset.getInt(1));
				style.setName(resultset.getString(2));
				style.setDesc(resultset.getString(3));
				style.setCssFolder(resultset.getString(4));
				style.setLastUpdated(new java.util.Date(resultset.getTimestamp(5).getTime()));
				styleList.add(style);
			}
			resultset.close();
			
			return styleList;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetStyleList", startMS, requestId, "");
		}
	}
	
	public List<Presentation> GetPresentationList(String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [PresentationId],[Name],[Description],[JspName],[CssExtension],[MaxSections],[MaxImagesInSection]," +
					"[ThumbWidth],[ThumbHeight],[OptionGalleryName],[OptionGalleryDesc],[OptionImageName],[OptionImageDesc],[OptionGroupingDesc]," +
					"[LastUpdated] FROM [dbo].[GalleryPresentation]";

			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<Presentation> presentationList = new ArrayList<Presentation>();
			
			while (resultset.next())
			{
				Presentation presentation = new Presentation();
				presentation.setPresentationId(resultset.getInt(1));
				presentation.setName(resultset.getString(2));
				presentation.setDesc(resultset.getString(3));
				presentation.setJspName(resultset.getString(4));
				presentation.setCssExtension(resultset.getString(5));
				presentation.setMaxSections(resultset.getInt(6));
				presentation.setMaxImagesInSection(resultset.getInt(7));
				
				presentation.setThumbWidth(resultset.getInt(8));
				presentation.setThumbHeight(resultset.getInt(9));
				
				presentation.setOptionGalleryName(resultset.getBoolean(10));
				presentation.setOptionGalleryDesc(resultset.getBoolean(11));
				presentation.setOptionImageName(resultset.getBoolean(12));
				presentation.setOptionImageDesc(resultset.getBoolean(13));
				presentation.setOptionGroupingDesc(resultset.getBoolean(14));
				
				presentation.setLastUpdated(new java.util.Date(resultset.getTimestamp(15).getTime()));
				
				presentationList.add(presentation);
			}
			resultset.close();

			return presentationList;
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			return null;
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
	        if (sQuery != null) try { if (!sQuery.isClosed()) {sQuery.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetPresentationList", startMS, requestId, "");
		}
	}

	public long GetNewId(String idType, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		CallableStatement idSproc = null;
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(true);
			
			String sprocSql = "EXEC [dbo].[GetId] ?, ?";
			
		    idSproc = conn.prepareCall(sprocSql);
		    idSproc.setString(1, idType);
		    idSproc.registerOutParameter(2, Types.INTEGER);
		    idSproc.execute();
		    
		    long newTagId = idSproc.getLong(2);
		    if (newTagId > 0)
		    {
		    	return newTagId;
		    }
		    else
		    {
		    	String error = "GETID sproc didn't return a positive number";
				meLogger.error(error);
				throw new WallaException("UtilityDataHelperImpl", "GetNewId", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 
		    }
		}
		catch (SQLException sqlEx) {
			meLogger.error("Unexpected SQLException in GetNewId", sqlEx);
			throw new WallaException("UtilityDataHelperImpl", "GetInt", sqlEx.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()); 
		} 
		finally {
	        if (idSproc != null) try { idSproc.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetNewId", startMS, requestId, idType);
		}
	}

	public int GetInt(String sql, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			ps = conn.prepareStatement(sql);

			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getInt(1);
			}
			else
			{
				return -1;
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException("UtilityDataHelperImpl", "GetInt", sqlEx.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()); 
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetInt", startMS, requestId, sql);
		}
	}
	
	public long GetLong(String sql, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try 
		{			
			conn = dataSource.getConnection();

			ps = conn.prepareStatement(sql);

			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getLong(1);
			}
			else
			{
				return -1;
			}
		} 
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException("UtilityDataHelperImpl", "GetLong", sqlEx.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()); 
		}
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetLong", startMS, requestId, sql);
		}
	}
		
	public String GetString(String sql, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			ps = conn.prepareStatement(sql);

			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getString(1);
			}
			else
			{
				return null;
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException("UtilityDataHelperImpl", "GetString", sqlEx.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()); 
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetString", startMS, requestId, sql);
		}
	}
	
	public Object GetValueParamString(String sql, String param, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			ps = conn.prepareStatement(sql);
			ps.setString(1, param);
			
			resultset = ps.executeQuery();
			if (resultset.next())
			{
				return resultset.getObject(1);
			}
			else
			{
				return null;
			}
		}
		catch (SQLException sqlEx) {
			meLogger.error(sqlEx);
			throw new WallaException("UtilityDataHelperImpl", "GetStringParamString", sqlEx.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value()); 
		} 
		finally {
			if (resultset != null) try { if (!resultset.isClosed()) {resultset.close();} } catch (SQLException logOrIgnore) {}
			if (ps != null) try { if (!ps.isClosed()) {ps.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","GetStringParamString", startMS, requestId, sql);
		}
	}
	
	public int ExecuteSql(String sql, String requestId) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement ds = null;
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);

			ds = conn.createStatement();
			int recordCount = ds.executeUpdate(sql);

			ds.close();
			conn.commit();
			
			return recordCount;
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
			if (ds != null) try { if (!ds.isClosed()) {ds.close();} } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { if (!conn.isClosed()) {conn.close();} } catch (SQLException logOrIgnore) {}
	        utilityService.LogMethod("UtilityDataHelperImpl","ExecuteSql", startMS, requestId, sql);
		}
	}

	public void AddAction(UserEvent event) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		String sql = "";

		Connection conn = null;
		PreparedStatement ps = null;
		try {			
			int returnCount = 0;

			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			switch (event.getActionType())
			{
				case "Account":
					sql = "INSERT INTO [ActionAccount] ([UserId],[Action],[ExtraInfo],[ActionDateTime])"
							+ " VALUES (?,?,?,?)";
					break;
				case "UserApp":
					sql = "INSERT INTO [ActionUserApp] ([UserAppId],[Action],[ExtraInfo],[ActionDateTime])"
							+ " VALUES (?,?,?,?)";
					break;
				case "Gallery":
					sql = "INSERT INTO [ActionGallery] ([GalleryId],[Action],[ExtraInfo],[ActionDateTime])"
							+ " VALUES (?,?,?,?)";
			}
			
			//Insert main tag record.
			ps = conn.prepareStatement(sql);
			ps.setLong(1, event.getId());
			ps.setString(2, event.getAction());
			ps.setString(3, event.getExtraInfo());
			//ps.setDate(4, new java.sql.Date(event.getActionDate().getTime()));
			ps.setTimestamp(4, new java.sql.Timestamp(event.getActionDate().getTime()));
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "AddAction", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
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
		}
	}
	
	public void AddSecurityAction(SecurityEvent event) throws WallaException
	{
		long startMS = System.currentTimeMillis();

		Connection conn = null;
		PreparedStatement ps = null;
		try {			
			int returnCount = 0;

			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			String sql = "INSERT INTO [dbo].[ActionSecurity] ([Action],[ExtraInfo],[ActionDate],[objectProfileName],[objectEmail],[objectKey],[objectGalleryName],"
				+ "[requestCookie],[requestHeader],[requestRemoteAddress],[requestLocalAddress],[requestMethod],[requestRemoteHost],"
				+ "[requestRequestURL],[sessionRemoteAddress],[sessionFailedLogonCount],[sessionFailedLogonLast],[sessionProfileName],[sessionUserId],[sessionUserAppId],"
				+ "[sessionPlatformId],[sessionAppId],[sessionGalleryTempKey],[sessionGalleryName],[sessionCustomSessionIds],[sessionNonceKey])"
				+ "VALUES (?,?,?, ?,?,?,?, ?,?,?,?,?,?,?, ?,?,?,?,?,?,?,?,?,?,?,?)";

			ps = conn.prepareStatement(sql);
			ps.setString(1, event.getAction());
			ps.setString(2, event.getExtraInfo());
			ps.setTimestamp(3, new java.sql.Timestamp(event.getActionDate().getTime()));
			
			ps.setString(4, event.getObjectProfileName());
			ps.setString(5, event.getObjectEmail());
			ps.setString(6, event.getObjectKey());
			ps.setString(7, event.getObjectGalleryName());
			
			ps.setString(8, event.getRequestCookie());
			ps.setString(9, event.getRequestHeader());
			ps.setString(10, event.getRequestRemoteAddress());
			ps.setString(11, event.getRequestLocalAddress());
			ps.setString(12, event.getRequestMethod());
			ps.setString(13, event.getRequestRemoteHost());
			ps.setString(14, event.getRequestRequestURL());
			
			ps.setString(15, event.getSessionRemoteAddress());
			ps.setInt(16, event.getSessionFailedLogonCount());
			
			if (event.getSessionFailedLogonLast() != null)
				ps.setTimestamp(17, new java.sql.Timestamp(event.getSessionFailedLogonLast().getTime()));
			else
				ps.setNull(17, java.sql.Types.DATE);
			
			ps.setString(18, event.getSessionProfileName());
			ps.setLong(19, event.getSessionUserId());
			ps.setLong(20, event.getSessionUserAppId());
			ps.setInt(21, event.getSessionPlatformId());
			ps.setInt(22, event.getSessionAppId());
			ps.setString(23, event.getSessionGalleryTempKey());
			ps.setString(24, event.getSessionGalleryName());
			ps.setString(25, event.getSessionCustomSessionIds());
			ps.setString(26, event.getSessionNonceKey());
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "AddSecurityAction", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
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
		}
	}
	
	public void LogMethodCall(LogMethodDetail detail) throws WallaException
	{
		long startMS = System.currentTimeMillis();
		String sql = "";

		Connection conn = null;
		PreparedStatement ps = null;
		try {			
			int returnCount = 0;

			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			switch (detail.getLogMethodType())
			{
				case "Internal":
					sql = "INSERT INTO [LogMethod] ([RequestId],[Object],[Method],[Duration],[StartTime],[Params])"
							+ " VALUES (?, ?, ?, ?, ?, ?)";
					break;
				case "Web":
					sql = "INSERT INTO [LogWebMethod] ([RequestId],[Object],[Method],[Duration],[StartTime],[Params],[Response],[UserId], [Session], [RequestPath])"
							+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					break;
				case "Message":
					sql = "";
			}
			
			ps = conn.prepareStatement(sql);
			ps.setString(1, detail.getRequestId());
			ps.setString(2, detail.getObject());
			ps.setString(3,  detail.getMethod());
			ps.setLong(4, detail.getDuration());
			ps.setTimestamp(5, new java.sql.Timestamp(detail.getStartDate().getTime()));
			ps.setString(6, detail.getParams());
			
			switch (detail.getLogMethodType())
			{
				case "Web":
					ps.setString(7, detail.getResponse());
					ps.setLong(8, detail.getUserId());
					ps.setString(9, detail.getSession());
					ps.setString(10, detail.getRequestPath());
					break;
				case "Message":
					sql = "";
			}
			
			//Execute insert statement.
			returnCount = ps.executeUpdate();
			
			//Validate new record was successful.
			if (returnCount != 1)
			{
				conn.rollback();
				String error = "Insert statement didn't return a success count of 1.";
				meLogger.error(error);
				throw new WallaException(this.getClass().getName(), "LogMethodCall", error, HttpStatus.INTERNAL_SERVER_ERROR.value()); 				
			}
			
			conn.commit();
		}
		catch (SQLException sqlEx) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(sqlEx);
		} 
		catch (Exception ex) {
			if (conn != null) { try { conn.rollback(); } catch (SQLException ignoreEx) {} }
			meLogger.error(ex);
		}
		finally {
	        if (ps != null) try { ps.close(); } catch (SQLException logOrIgnore) {}
	        if (conn != null) try { conn.close(); } catch (SQLException logOrIgnore) {}
		}
	}
	
	public UtilityDataHelperImpl() throws JMSException {
		
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
