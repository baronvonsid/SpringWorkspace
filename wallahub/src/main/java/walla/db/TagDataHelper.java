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
	public void CreateTag(long userId, Tag newTag, long tagId) throws WallaException;
	public void UpdateTag(long userId, Tag existingTag) throws WallaException;
	public void DeleteTag(long userId, long tagId, int version, String tagName) throws WallaException;
	public void DeleteTagReferences(long userId, long tagId) throws WallaException;
	
	//Straight up
	public Date LastTagListUpdate(long userId) throws WallaException;
	public ImageList GetTagImageListMeta(long userId, String tagName) throws WallaException;
	public Tag GetTagMeta(long userId, String tagName) throws WallaException;
	public void GetTagImages(long userId, int imageCursor, int imageCount, ImageList tagImageList) throws WallaException;
	public TagList GetUserTagList(long userId) throws WallaException;
	public int xxxGetTotalImageCount(long userId, long tagId) throws WallaException;
	public void UpdateTagTimeAndCount(long userId, long tagId) throws WallaException;
	public void AddRemoveTagImages(long userId, long tagId, ImageIdList moveList, boolean add) throws WallaException;
	public long[] GetGalleriesLinkedToTag(long userId, long tagId) throws WallaException;
	public long[] ReGenDynamicTags(long userId) throws WallaException;
}
