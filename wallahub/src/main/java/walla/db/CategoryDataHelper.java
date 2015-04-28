package walla.db;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface CategoryDataHelper {

	//Standard CUD
	public void CreateCategory(long userId, Category newCategory, long categoryId, String requestId) throws WallaException;
	public void UpdateCategory(long userId, Category existingCategory, String requestId) throws WallaException;
	public void MarkCategoryAsDeleted(long userId, long[] categoryId, Category existingCategory, String requestId) throws WallaException;
	public Category GetCategoryMeta(long userId, long categoryId, String requestId) throws WallaException;
	
	public Date LastCategoryListUpdate(long userId, String requestId) throws WallaException;
	public ImageList GetCategoryImageListMeta(long userId, long categoryId, String requestId) throws WallaException;
	
	public void GetCategoryImages(long userId, int imageCursor, int imageCount, ImageList categoryImageList, String requestId) throws WallaException;
	public CategoryList GetUserCategoryList(long userId, String requestId) throws WallaException;
	public int GetTotalImageCount(long userId, long categoryId, String requestId) throws WallaException;
	
	public long[] GetCategoryHierachy(long userId, long categoryId, boolean up, String requestId) throws WallaException;
	public long[] GetGalleryReferencingCategory(long userId, long[] categoryIds, String requestId) throws WallaException;
	public void UpdateCategoryTimeAndCount(long userId, long[] categoryIds, String requestId) throws WallaException;
	
	public long[] GetCategoryIdFromImageMoveList(long userId, ImageIdList moveList, String requestId) throws WallaException;
	public void MoveImagesToNewCategory(long userId, long categoryId, ImageIdList moveList, String requestId) throws WallaException;
	
	
}
