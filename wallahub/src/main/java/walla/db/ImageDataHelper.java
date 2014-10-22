package walla.db;

import java.util.List;

import javax.sql.DataSource;
import walla.datatypes.auto.*;
import walla.utils.WallaException;

public interface ImageDataHelper {

	public void CreateImage(long userId, ImageMeta newImage) throws WallaException;
	public void UpdateImage(long userId, ImageMeta existingImage) throws WallaException;
	
	public UploadStatusList GetCurrentUploads(long userId, ImageIdList imageIdToCheck) throws WallaException;
	public void MarkImagesAsInactive(long userId, ImageList imagesToDelete) throws WallaException;
	public ImageList GetActiveImagesInCategories(long userId, long[] categoryIds) throws WallaException;
	public ImageMeta GetImageMeta(long userId, long imageId) throws WallaException;
	public long[] GetTagsLinkedToImages(long userId, ImageList imageList) throws WallaException;
	public long[] GetCategoriesLinkedToImages(long userId, ImageList imageList) throws WallaException;
	public void UpdateImageStatus(long userId, long imageId, int status, boolean error, String errorMessage) throws WallaException;
	
	public void setDataSource(DataSource dataSource);
	
	
	
}
