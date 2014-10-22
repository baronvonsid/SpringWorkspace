package walla.db;

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

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.*;

import org.springframework.http.HttpStatus;

@Repository
public class UtilityDataHelperImpl implements UtilityDataHelper{

	private DataSource dataSource;
	
	private static final Logger meLogger = Logger.getLogger(UtilityDataHelperImpl.class);

	public List<Platform> GetPlatformList() throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [PlatformId],[ShortName],[OperatingSystem],[MachineType],[Supported],[MajorVersion],[MinorVersion] FROM [Platform]";
			
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
				platform.setSupported(resultset.getBoolean(5));
				platform.setMajorVersion(resultset.getInt(6));
				platform.setMinorVersion(resultset.getInt(7));
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
	        UserTools.LogMethod("GetPlatformList", meLogger, startMS, "");
		}
	}

	public List<App> GetAppList() throws WallaException
	{
		long startMS = System.currentTimeMillis();
		Connection conn = null;
		Statement sQuery = null;
		ResultSet resultset = null;
		
		try {			
			conn = dataSource.getConnection();

			String selectSql = "SELECT [AppId],[Name],[WSKey],[MajorVersion],[MinorVersion],[Status],[DefaultFetchSize],[DefaultThumbCacheMB],[DefaultMainCopyCacheMB],[DefaultGalleryType] FROM [App]";
			
			sQuery = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			resultset = sQuery.executeQuery(selectSql);
			
			List<App> appList = new ArrayList<App>();
			
			while (resultset.next())
			{
				App app = new App();
				app.setAppId(resultset.getInt(1));
				app.setName(resultset.getString(2));
				app.setWSKey(resultset.getString(3));
				app.setMajorVersion(resultset.getInt(4));
				app.setMinorVersion(resultset.getInt(5));
				app.setStatus(resultset.getInt(6));
				app.setDefaultFetchSize(resultset.getInt(7));
				app.setDefaultThumbCacheMB(resultset.getInt(8));
				app.setDefaultMainCopyCacheMB(resultset.getInt(9));
				app.setDefaultGalleryType(resultset.getInt(10));
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
	        UserTools.LogMethod("GetAppList", meLogger, startMS, "");
		}
	}
	
	public List<Style> GetStyleList() throws WallaException
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
	        UserTools.LogMethod("GetStyleList", meLogger, startMS, "");
		}
	}
	
	public List<Presentation> GetPresentationList() throws WallaException
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
	        UserTools.LogMethod("GetPresentationList", meLogger, startMS, "");
		}
	}

	public long GetNewId(String idType) throws WallaException
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
	        UserTools.LogMethod("GetNewId", meLogger, startMS, idType);
		}
	}

	public int GetInt(String sql) throws WallaException
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
	        UserTools.LogMethod("GetInt", meLogger, startMS, sql);
		}
	}
	
	public long GetLong(String sql) throws WallaException
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
	        UserTools.LogMethod("GetLong", meLogger, startMS, sql);
		}
	}
		
	public String GetString(String sql) throws WallaException
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
	        UserTools.LogMethod("GetString", meLogger, startMS, sql);
		}
	}
	
	public UtilityDataHelperImpl() {
		meLogger.debug("UtilityDataHelperImpl object instantiated.");
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
