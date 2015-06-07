package walla.db;

import java.util.List;

import javax.sql.DataSource;
import walla.datatypes.auto.*;
import walla.utils.WallaException;

public interface ImageDataHelper {

	public void CreateImage(long userId, ImageMeta newImage, String requestId) throws WallaException;
	public void UpdateImage(long userId, ImageMeta existingImage, String requestId) throws WallaException;
	
	public UploadStatusList GetCurrentUploads(long userId, ImageIdList imageIdToCheck, String requestId) throws WallaException;
	public void MarkImagesAsInactive(long userId, long[] imagesToDelete, String requestId) throws WallaException;
	public ImageIdList GetActiveImagesInCategories(long userId, long[] categoryIds, String requestId) throws WallaException;
	public ImageMeta GetImageMeta(long userId, long imageId, String requestId) throws WallaException;
	public long[] GetTagsLinkedToImages(long userId, long[] imageList, String requestId) throws WallaException;
	public long[] GetCategoriesLinkedToImages(long userId, long[] imageList, String requestId) throws WallaException;
	public void UpdateImageStatus(long userId, long imageId, int status, boolean error, String errorMessage, String requestId) throws WallaException;
	
	public void setDataSource(DataSource dataSource);
	
	
	
}
