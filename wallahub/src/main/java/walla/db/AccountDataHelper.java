package walla.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface AccountDataHelper {

	public long CreateAccount(Account newAccount, String passwordHash, String salt, String requestId) throws WallaException;
	public void UpdateAccount(Account acount, String requestId) throws WallaException;
	public Account GetAccountMeta(long userId, String requestId);
	public AccountStorage GetAccountStorage(long userId, String requestId);
	public void UpdateAccountStatus(long userId, AccountStatus status, String requestId) throws WallaException;
	public boolean ValidateEmailConfirm(long userId, String requestValidationString, CustomResponse customResponse, String requestId);
	public void AddEmail(long userId, String email, boolean principle, boolean secondary, String requestId) throws WallaException;
	public void UpdateEmail(long userId, String email, EmailAction action, String validationString, String requestId) throws WallaException;
	public String ShouldAccountBeLive(long userId, String requestId) throws WallaException;
	
	public boolean EmailIsUnique(long userId, String email, String requestId) throws WallaException;
	public UserApp GetUserApp(long userId, long userAppId, String requestId) throws WallaException;
	public long FindExistingUserApp(long userId, int appId, int platformId, String machineName, String requestId) throws WallaException;
	public void CreateUserApp(long userId, UserApp userApp, String requestId) throws WallaException;
	public void UpdateUserApp(long userId, UserApp userApp, String requestId) throws WallaException;
	public void UserAppBlockUnblock(long userId, long userAppId, boolean block, String requestId) throws WallaException;
	
	public LogonState GetLogonState(String userName, String requestId) throws WallaException;
	public void UpdateLogonState(long userId, int failedLoginCount, Date failedLoginLast, String requestId) throws WallaException;
	public void UpdatePassword(long userId, String passwordHash, String salt, String requestId) throws WallaException;
	public void UpdateTempSalt(long userId, String salt, String requestId) throws WallaException;
	public AccountActionSummary GetAccountActions(long userId, String requestId);
}
