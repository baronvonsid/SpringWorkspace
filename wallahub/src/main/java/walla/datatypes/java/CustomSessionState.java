package walla.datatypes.java;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import walla.datatypes.auto.*;

public class CustomSessionState {

	private String profileName = "";
	private long userId = -1;
	private ArrayList<Long> uploadReceivedImageIds = null;
	private long userAppId = -1;
	private int platformId = -1;
	private int appId = -1;
	private boolean isHuman = false;
	private boolean isAuthenticated = false;
	private boolean isGalleryViewer = false;
	private boolean isAdmin = false;
	private Gallery galleryPreview = null;
	private String galleryTempKey = "";
	private String galleryName = "";
	
	private ArrayList<String> customSessionIds = null;
	private String nonceKey = "";
	private int failedLogonCount = 0;
	private Date failedLogonLast = null;
	private String remoteAddress = "";
	
	private Account account = null;
	private AccountStorage accountStorage = null;
	private AccountActionSummary accountActions = null;
	
	public String getProfileName()
	{
		return this.profileName;
	}
	
	public void setProfileName(String value)
	{
		this.profileName = value;
	}
	
	public long getUserId()
	{
		return this.userId;
	}
	
	public void setUserId(long value)
	{
		this.userId = value;
	}
	
    public List<Long> getUploadFilesReceived() {
        if (uploadReceivedImageIds == null) {
        	uploadReceivedImageIds = new ArrayList<Long>();
        }
        return this.uploadReceivedImageIds;
    }
	    
	public long getUserAppId()
	{
		return this.userAppId;
	}
	
	public void setUserAppId(long value)
	{
		this.userAppId = value;
	}
	
	public int getPlatformId()
	{
		return this.platformId;
	}
	
	public void setPlatformId(int value)
	{
		this.platformId = value;
	}
	
	public int getAppId()
	{
		return this.appId;
	}
	
	public void setAppId(int appId)
	{
		this.appId = appId;
	}
	
	public boolean isHuman()
	{
		return this.isHuman;
	}
	
	public void setHuman(boolean value)
	{
		this.isHuman = value;
	}
	
	public boolean isAuthenticated()
	{
		return this.isAuthenticated;
	}
	
	public void setAuthenticated(boolean isAuthenticated)
	{
		this.isAuthenticated = isAuthenticated;
	}
	
	public boolean isGalleryViewer()
	{
		return this.isGalleryViewer;
	}
	
	public void setGalleryViewer(boolean value)
	{
		this.isGalleryViewer = value;
	}
	
	public boolean isAdmin()
	{
		return this.isAdmin;
	}
	
	public void setAdmin(boolean value)
	{
		this.isAdmin = value;
	}
	
	public Gallery getGalleryPreview()
	{
		return this.galleryPreview;
	}
	
	public void setGalleryPreview(Gallery galleryPreview)
	{
		this.galleryPreview = galleryPreview;
	}
	
	public String getGalleryTempKey()
	{
		return this.galleryTempKey;
	}
	
	public void setGalleryTempKey(String value)
	{
		this.galleryTempKey = value;
	}
	
	public String getGalleryName()
	{
		return this.galleryName;
	}
	
	public void setGalleryName(String value)
	{
		this.galleryName = value;
	}
	
    public List<String> getCustomSessionIds() {
        if (customSessionIds == null) {
        	customSessionIds = new ArrayList<String>();
        }
        return this.customSessionIds;
    }
    
	public String getNonceKey()
	{
		return this.nonceKey;
	}
	
	public void setNonceKey(String value)
	{
		this.nonceKey = value;
	}
	
	public int getFailedLogonCount()
	{
		return this.failedLogonCount;
	}
	
	public void setFailedLogonCount(int value)
	{
		this.failedLogonCount = value;
	}

	public Date getFailedLogonLast()
	{
		return this.failedLogonLast;
	}
	
	public void setFailedLogonLast(Date value)
	{
		this.failedLogonLast = value;
	}
	
	public String getRemoteAddress()
	{
		return this.remoteAddress;
	}
	
	public void setRemoteAddress(String value)
	{
		this.remoteAddress = value;
	}
	
	public Account getAccount()
	{
		return this.account;
	}
	
	public void setAccount(Account value)
	{
		this.account = value;
	}
	
	public AccountStorage getAccountStorage()
	{
		return this.accountStorage;
	}
	
	public void setAccountStorage(AccountStorage value)
	{
		this.accountStorage = value;
	}
	
	public AccountActionSummary getAccountActionSummary()
	{
		return this.accountActions;
	}
	
	public void setAccountActionSummary(AccountActionSummary value)
	{
		this.accountActions = value;
	}
}
