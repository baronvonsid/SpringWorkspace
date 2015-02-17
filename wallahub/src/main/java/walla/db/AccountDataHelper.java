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
	public Account GetAccountMeta(long userId);
	public Account GetAccountStorageSummary(long userId);
	public void UpdateAccountStatus(long userId, AccountStatus status) throws WallaException;
	public boolean ValidateEmailConfirm(long userId, String requestValidationString, CustomResponse customResponse);
	public void AddEmail(long userId, String email, boolean principle, boolean secondary) throws WallaException;
	public void UpdateEmail(long userId, String email, EmailAction action, String validationString) throws WallaException;
	public String ShouldAccountBeLive(long userId) throws WallaException;
	
	public boolean EmailIsUnique(long userId, String email) throws WallaException;
	public UserApp GetUserApp(long userId, long userAppId) throws WallaException;
	public long FindExistingUserApp(long userId, int appId, int platformId, String machineName) throws WallaException;
	public void CreateUserApp(long userId, UserApp userApp) throws WallaException;
	public void UpdateUserApp(long userId, UserApp userApp) throws WallaException;
	
	public LogonState GetLogonState(String userName) throws WallaException;
	public void UpdateLogonState(long userId, int failedLoginCount, Date failedLoginLast) throws WallaException;
	public void UpdatePassword(long userId, String passwordHash, String salt) throws WallaException;
}
