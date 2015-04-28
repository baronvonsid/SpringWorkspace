package walla.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.utils.WallaException;

public interface TagDataHelper {

	//Standard CUD
	public void CreateTag(long userId, Tag newTag, long tagId, String requestId) throws WallaException;
	public void UpdateTag(long userId, Tag existingTag, String requestId) throws WallaException;
	public void DeleteTag(long userId, long tagId, int version, String tagName, String requestId) throws WallaException;
	public void DeleteTagReferences(long userId, long tagId, String requestId) throws WallaException;
	
	//Straight up
	public Date LastTagListUpdate(long userId, String requestId) throws WallaException;
	public ImageList GetTagImageListMeta(long userId, String tagName, String requestId) throws WallaException;
	public Tag GetTagMeta(long userId, String tagName, String requestId) throws WallaException;
	public void GetTagImages(long userId, int imageCursor, int imageCount, ImageList tagImageList, String requestId) throws WallaException;
	public TagList GetUserTagList(long userId, String requestId) throws WallaException;
	public int xxxGetTotalImageCount(long userId, long tagId, String requestId) throws WallaException;
	public void UpdateTagTimeAndCount(long userId, long tagId, String requestId) throws WallaException;
	public void AddRemoveTagImages(long userId, long tagId, ImageIdList moveList, boolean add, String requestId) throws WallaException;
	public long[] GetGalleriesLinkedToTag(long userId, long tagId, String requestId) throws WallaException;
	public long[] ReGenDynamicTags(long userId, String requestId) throws WallaException;
}
