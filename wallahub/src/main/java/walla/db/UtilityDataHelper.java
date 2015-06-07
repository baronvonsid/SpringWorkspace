package walla.db;

import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface UtilityDataHelper {

	public List<Platform> GetPlatformList(String requestId) throws WallaException;
	public List<App> GetAppList(String requestId) throws WallaException;
	public List<AppPlatform> GetAppPlatformList(String requestId) throws WallaException;
	public List<Style> GetStyleList(String requestId) throws WallaException;
	public List<Presentation> GetPresentationList(String requestId) throws WallaException;
	public long GetNewId(String idType, String requestId) throws WallaException;
	public String GetString(String sql, String requestId) throws WallaException;
	public int GetInt(String sql, String requestId) throws WallaException;
	public void AddAction(UserEvent event) throws WallaException;
	public void AddSecurityAction(SecurityEvent event) throws WallaException;
	public void LogMethodCall(LogMethodDetail detail) throws WallaException;
	
	public void setDataSource(DataSource dataSource);
	
	
}
