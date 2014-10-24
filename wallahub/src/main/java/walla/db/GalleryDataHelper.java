package walla.db;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface GalleryDataHelper {

	//Standard CUD
	public void CreateGallery(long userId, Gallery newGallery, long galleryId, String urlComplex) throws WallaException;
	public void UpdateGallery(long userId, Gallery existingGallery) throws WallaException;
	public void DeleteGallery(long userId, long galleryId, int version, String galleryName) throws WallaException;
	
	//Straight up
	public Date LastGalleryListUpdate(long userId) throws WallaException;
	public ImageList GetGalleryImageListMeta(long userId, String galleryName, long sectionId) throws WallaException;
	public Gallery GetGalleryMeta(long userId, String galleryName) throws WallaException;
	public void GetGalleryImages(long userId, int imageCursor, int imageCount, ImageList galleryImageList) throws WallaException;
	public GalleryList GetUserGalleryList(long userId) throws WallaException;
	public GalleryLogon GetGalleryLogonDetail(String userName, String galleryName, String urlComplex) throws WallaException;
	//public void UpdateGalleryTimestamp(long galleryId) throws WallaException;
	public void UpdateLoginSalt(long userId, String galleryName, String salt) throws WallaException;
	public void RegenerateGalleryImages(long userId, long galleryId) throws WallaException;
	
	public Gallery GetGallerySections(long userId, Gallery requestGallery, long tempGalleryId) throws WallaException;
}
