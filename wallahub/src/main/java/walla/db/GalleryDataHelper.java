package walla.db;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface GalleryDataHelper {

	//Standard CUD
	public void CreateGallery(long userId, Gallery newGallery, long newGalleryId, String passwordHash, String gallerySalt, String urlComplex, String requestId) throws WallaException;
	public void UpdateGallery(long userId, Gallery existingGallery, String passwordHash, String gallerySalt, String requestId) throws WallaException;
	public void DeleteGallery(long userId, long galleryId, int version, String galleryName, String requestId) throws WallaException;
	
	//Straight up
	public Date LastGalleryListUpdate(long userId, String requestId) throws WallaException;
	public ImageList GetGalleryImageListMeta(long userId, String galleryName, long sectionId, String requestId) throws WallaException;
	public Gallery GetGalleryMeta(long userId, String galleryName, String requestId) throws WallaException;
	public void GetGalleryImages(long userId, int imageCursor, int imageCount, ImageList galleryImageList, String requestId) throws WallaException;
	public GalleryList GetUserGalleryList(long userId, String requestId) throws WallaException;
	public GalleryLogon GetGalleryLogonDetail(String userName, String galleryName, String urlComplex, String requestId) throws WallaException;
	public void UpdateTempSalt(long userId, String galleryName, String salt, String requestId) throws WallaException;
	public void RegenerateGalleryImages(long userId, long galleryId, String requestId) throws WallaException;
	
	public Gallery GetGallerySections(long userId, Gallery requestGallery, long tempGalleryId, String requestId) throws WallaException;
}
