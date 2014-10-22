package walla.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface AccountDataHelper {

	public long CreateAccount(Account newAccount, String passwordHash, String salt) throws WallaException;
	public void UpdateAccount(Account acount) throws WallaException;
	public Account GetAccount(long userId) throws WallaException;
	public void UpdateMainStatus(long userId, int status) throws WallaException;
	public void UpdateEmailStatus(long userId, int status, String validationString) throws WallaException;
	
	public boolean ProfileNameIsUnique(String profileName) throws WallaException;
	public UserApp GetUserApp(long userId, long userAppId) throws WallaException;
	public long FindExistingUserApp(long userId, int appId, int platformId, String machineName) throws WallaException;
	public void CreateUserApp(long userId, UserApp userApp) throws WallaException;
	public void UpdateUserApp(long userId, UserApp userApp) throws WallaException;
	
	public LogonState GetLogonState(String userName, String email) throws WallaException;
	public void UpdateLogonState(long userId, int failedLoginCount, Date failedLoginLast) throws WallaException;
}
