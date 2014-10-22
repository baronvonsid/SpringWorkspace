package walla.db;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface CategoryDataHelper {

	//Standard CUD
	public void CreateCategory(long userId, Category newCategory, long categoryId) throws WallaException;
	public void UpdateCategory(long userId, Category existingCategory) throws WallaException;
	public void MarkCategoryAsDeleted(long userId, long[] categoryId, Category existingCategory) throws WallaException;
	public Category GetCategoryMeta(long userId, long categoryId) throws WallaException;
	
	public Date LastCategoryListUpdate(long userId) throws WallaException;
	public ImageList GetCategoryImageListMeta(long userId, long categoryId) throws WallaException;
	
	public void GetCategoryImages(long userId, int imageCursor, int imageCount, ImageList categoryImageList) throws WallaException;
	public CategoryList GetUserCategoryList(long userId) throws WallaException;
	public int GetTotalImageCount(long userId, long categoryId) throws WallaException;
	
	public long[] GetCategoryHierachy(long userId, long categoryId, boolean up) throws WallaException;
	public long[] GetGalleryReferencingCategory(long userId, long[] categoryIds) throws WallaException;
	public void UpdateCategoryTimeAndCount(long userId, long[] categoryIds) throws WallaException;
	
	public long[] GetCategoryIdFromImageMoveList(long userId, ImageIdList moveList) throws WallaException;
	public void MoveImagesToNewCategory(long userId, long categoryId, ImageIdList moveList) throws WallaException;
	
	
}
