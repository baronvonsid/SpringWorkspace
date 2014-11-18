USE [master]
GO
/****** Object:  Database [fotowalla]    Script Date: 18/11/2014 22:56:09 ******/
CREATE DATABASE [fotowalla]
 CONTAINMENT = NONE
 ON  PRIMARY 
( NAME = N'ImageWalla', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL11.SQLEXPRESS2012\MSSQL\DATA\ImageWalla.mdf' , SIZE = 10240KB , MAXSIZE = UNLIMITED, FILEGROWTH = 1024KB )
 LOG ON 
( NAME = N'ImageWalla_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL11.SQLEXPRESS2012\MSSQL\DATA\ImageWalla_log.ldf' , SIZE = 2560KB , MAXSIZE = 2048GB , FILEGROWTH = 10%)
GO
ALTER DATABASE [fotowalla] SET COMPATIBILITY_LEVEL = 110
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [fotowalla].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [fotowalla] SET ANSI_NULL_DEFAULT OFF 
GO
ALTER DATABASE [fotowalla] SET ANSI_NULLS OFF 
GO
ALTER DATABASE [fotowalla] SET ANSI_PADDING OFF 
GO
ALTER DATABASE [fotowalla] SET ANSI_WARNINGS OFF 
GO
ALTER DATABASE [fotowalla] SET ARITHABORT OFF 
GO
ALTER DATABASE [fotowalla] SET AUTO_CLOSE OFF 
GO
ALTER DATABASE [fotowalla] SET AUTO_CREATE_STATISTICS ON 
GO
ALTER DATABASE [fotowalla] SET AUTO_SHRINK OFF 
GO
ALTER DATABASE [fotowalla] SET AUTO_UPDATE_STATISTICS ON 
GO
ALTER DATABASE [fotowalla] SET CURSOR_CLOSE_ON_COMMIT OFF 
GO
ALTER DATABASE [fotowalla] SET CURSOR_DEFAULT  GLOBAL 
GO
ALTER DATABASE [fotowalla] SET CONCAT_NULL_YIELDS_NULL OFF 
GO
ALTER DATABASE [fotowalla] SET NUMERIC_ROUNDABORT OFF 
GO
ALTER DATABASE [fotowalla] SET QUOTED_IDENTIFIER OFF 
GO
ALTER DATABASE [fotowalla] SET RECURSIVE_TRIGGERS OFF 
GO
ALTER DATABASE [fotowalla] SET  DISABLE_BROKER 
GO
ALTER DATABASE [fotowalla] SET AUTO_UPDATE_STATISTICS_ASYNC OFF 
GO
ALTER DATABASE [fotowalla] SET DATE_CORRELATION_OPTIMIZATION OFF 
GO
ALTER DATABASE [fotowalla] SET TRUSTWORTHY OFF 
GO
ALTER DATABASE [fotowalla] SET ALLOW_SNAPSHOT_ISOLATION OFF 
GO
ALTER DATABASE [fotowalla] SET PARAMETERIZATION SIMPLE 
GO
ALTER DATABASE [fotowalla] SET READ_COMMITTED_SNAPSHOT OFF 
GO
ALTER DATABASE [fotowalla] SET HONOR_BROKER_PRIORITY OFF 
GO
ALTER DATABASE [fotowalla] SET RECOVERY SIMPLE 
GO
ALTER DATABASE [fotowalla] SET  MULTI_USER 
GO
ALTER DATABASE [fotowalla] SET PAGE_VERIFY CHECKSUM  
GO
ALTER DATABASE [fotowalla] SET DB_CHAINING OFF 
GO
ALTER DATABASE [fotowalla] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF ) 
GO
ALTER DATABASE [fotowalla] SET TARGET_RECOVERY_TIME = 0 SECONDS 
GO
USE [fotowalla]
GO
/****** Object:  User [TM_DEV]    Script Date: 18/11/2014 22:56:09 ******/
CREATE USER [TM_DEV] FOR LOGIN [TM_DEV] WITH DEFAULT_SCHEMA=[dbo]
GO
/****** Object:  User [fotowalla-web]    Script Date: 18/11/2014 22:56:09 ******/
CREATE USER [fotowalla-web] FOR LOGIN [fotowalla-web] WITH DEFAULT_SCHEMA=[dbo]
GO
ALTER ROLE [db_datareader] ADD MEMBER [fotowalla-web]
GO
ALTER ROLE [db_datawriter] ADD MEMBER [fotowalla-web]
GO
/****** Object:  StoredProcedure [dbo].[GenerateGalleryImages]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO





CREATE PROCEDURE [dbo].[GenerateGalleryImages] @UserId int, @GalleryId int
AS
BEGIN
	/* Create temp table for duration during sproc */
	DECLARE @NewImageList TABLE (ImageId int, SectionId int);
	DECLARE @GroupingType tinyint;
	DECLARE @SelectionType tinyint;

	SELECT @GroupingType = GroupingType, @SelectionType = SelectionType FROM Gallery WHERE GalleryId=@GalleryId;

	/* @SelectionType = 0 - Category, 1 - Tag, 2 - Category AND Tag, 3 - Category OR Tag */

	/* Add images from categories */
	IF @SelectionType = 0 OR @SelectionType = 3
	BEGIN
		INSERT INTO @NewImageList
		SELECT I.ImageId, CHOOSE(@GroupingType+1,0,CategoryId,0) AS SectionId FROM Image I WHERE 
		I.[Status] = 4
		AND I.CategoryId IN 
			(
				SELECT GC.CategoryId FROM GalleryCategory GC
				INNER JOIN Category C ON GC.CategoryId = C.CategoryId 
				WHERE GC.GalleryId = @GalleryId AND GC.[Recursive] = 0 AND C.UserId = @UserId
				UNION
				SELECT CD.CategoryId FROM GalleryCategory GC 
					CROSS APPLY [CategoryListDown] (@UserId ,GC.CategoryId) AS CD
					WHERE GC.GalleryId = @GalleryId AND GC.[Recursive] = 1
			);
	END

	/* Add images from include tags */
	IF @SelectionType = 1 OR @SelectionType = 3
	BEGIN
		INSERT INTO @NewImageList
		SELECT TI.ImageId, CHOOSE(@GroupingType+1,0,I.CategoryId,T.TagId) 
		FROM TagImage TI 
		INNER JOIN GalleryTag GT ON TI.TagId = GT.TagId 
		INNER JOIN Tag T ON TI.TagId = T.TagId
		INNER JOIN [Image] I ON TI.ImageId = I.ImageId
		WHERE GT.GalleryId = @GalleryId 
			AND GT.Exclude = 0 
			AND T.UserId = @UserId
			AND I.[Status] = 4
	END

	IF @SelectionType = 2
	BEGIN
		INSERT INTO @NewImageList
		SELECT DISTINCT I.ImageId, CHOOSE(@GroupingType+1,0,CategoryId,T.TagId) AS SectionId 
		FROM Image I 
		INNER JOIN TagImage TI ON TI.ImageId = I.ImageId
		INNER JOIN Tag T ON TI.TagId = T.TagId
		INNER JOIN GalleryTag GT ON T.TagId = GT.TagId 
		WHERE 
		GT.GalleryId = @GalleryId 
		AND GT.Exclude = 0 
		AND T.UserId = @UserId
		AND I.[Status] = 4
		AND I.CategoryId IN 
			(
				SELECT GC.CategoryId FROM GalleryCategory GC 
				INNER JOIN Category C ON GC.CategoryId = C.CategoryId 
				WHERE GC.GalleryId = @GalleryId AND GC.[Recursive] = 0 AND C.UserId = @UserId
				UNION
				SELECT CD.CategoryId FROM GalleryCategory GC 
					CROSS APPLY [CategoryListDown] (@UserId ,GC.CategoryId) AS CD
					WHERE GC.GalleryId = @GalleryId AND GC.[Recursive] = 1
			);
	END

	/* Remove images from exclude tags */
	BEGIN
		DELETE FROM @NewImageList
		WHERE ImageId IN
		(
			SELECT TI.ImageId FROM TagImage TI 
			INNER JOIN GalleryTag GT ON TI.TagId = GT.TagId 
			INNER JOIN Tag T ON TI.TagId = T.TagId
			INNER JOIN [Image] I ON TI.ImageId = I.ImageId
			WHERE GT.GalleryId = @GalleryId AND GT.Exclude = 1 AND T.UserId = @UserId AND I.[Status] = 4
		)
	END

	IF @SelectionType = 3
	BEGIN
		IF @GroupingType = 0 OR @GroupingType = 1
		BEGIN
			/* Only expect one of each image to be present, so remove duplicates */
			DELETE FROM @NewImageList WHERE ImageId IN (SELECT I2.ImageId FROM @NewImageList I2 GROUP BY I2.ImageId HAVING COUNT(I2.ImageId) > 1);
		END

		IF @GroupingType = 2
		BEGIN
			/* Duplicates valid, but records to be removed where SectionId = 0 and the same ImageId exists with a valid section Id */
			DELETE FROM @NewImageList WHERE SectionId = 0 AND ImageId IN (SELECT I2.ImageId FROM @NewImageList I2 WHERE I2.SectionId > 0 AND I2.ImageId = ImageId);
		END
	END

	

	/* Delete image from current GalleryImage where NOT IN temp table 
	BEGIN
		DELETE FROM GalleryImage
		WHERE ImageId NOT IN
		(
			SELECT TempImages.ImageId FROM @NewImageList TempImages 
		)
		AND GalleryId = @GalleryId
	END
	PRINT 'S1';
*/
	/* Remove images from temp table where already in GalleryImage with the correct SectionId */
	/*
	BEGIN
		DELETE @NewImageList FROM 
		@NewImageList NI INNER JOIN GalleryImage GI 
			ON NI.ImageId = GI.ImageId AND NI.SectionId = GI.SectionId
			
		WHERE ImageId IN
		(
			SELECT GI.ImageId FROM GalleryImage GI WHERE GI.GalleryId = @GalleryId AND GI.SectionId = @NewImageList.SectionId
		)
		
	END
*/

	/* Insert records into GalleryImage from temp table */
	BEGIN
		DELETE FROM GalleryImage WHERE GalleryId = @GalleryId;

		INSERT INTO GalleryImage
			SELECT DISTINCT @GalleryId, SectionId, ImageId FROM @NewImageList;

		/*
		DELETE FROM GallerySection 
		WHERE 
			GalleryId = @GalleryId
			AND ImageCount = 0
			AND [Name] = NULL
			AND [Desc] = NULL
			AND [Sequence] = NULL
		*/

		MERGE INTO GallerySection currTable
			USING (
						SELECT GI.GalleryId, GI.SectionId, 
							COUNT(GI.ImageId) AS ImageCount, 0 AS Sequence, '' AS NameOverride, '' AS DescOverride
						FROM GalleryImage GI 
						WHERE GI.GalleryId = @GalleryId 
						GROUP BY GI.GalleryId, GI.SectionId
					) AS newTable
			ON currTable.GalleryId = newTable.GalleryId
				AND currTable.SectionId = newTable.SectionId
		WHEN MATCHED THEN 
			UPDATE SET currTable.ImageCount = newTable.ImageCount
		WHEN NOT MATCHED THEN 
			INSERT ([GalleryId],[SectionId],[ImageCount],[Sequence],[NameOverride],[DescOverride])
			VALUES (newTable.GalleryId, newTable.SectionId, newTable.ImageCount, 0, null, null);

		/*
		

		UPDATE GallerySection SET ImageCount = GI.ImageId

		INSERT INTO GallerySection
			SELECT GI.GalleryId, GI.SectionId, COUNT(GI.ImageId) 
			FROM GalleryImage GI 
			WHERE GI.GalleryId = @GalleryId 
			GROUP BY GI.GalleryId, GI.SectionId
		*/
		
		UPDATE Gallery SET 
			RecordVersion = RecordVersion + 1, 
			LastUpdated = dbo.GetDateNoMS(),
			TotalImageCount = (SELECT COUNT(1) FROM GalleryImage GI WHERE GI.[GalleryId] = Gallery.GalleryId)
		WHERE Gallery.GalleryId = @GalleryId AND Gallery.UserId = @UserId
	END
END








GO
/****** Object:  StoredProcedure [dbo].[GetId]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[GetId] @IDTYPE VARCHAR(20), @NEWID INT OUTPUT
AS  
DECLARE @ERROR int 
DECLARE @ROWCOUNT int 

IF @IDTYPE = 'TagId' 
	BEGIN 
		BEGIN TRAN 
		UPDATE Ids SET TagId = TagId + 1

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		/* Get new ID and return */
		SELECT @NEWID = TagId FROM Ids 

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		COMMIT TRAN 
	END 

IF @IDTYPE = 'CategoryId' 
	BEGIN 
		BEGIN TRAN 
		UPDATE Ids SET CategoryId = CategoryId + 1

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		/* Get new ID and return */
		SELECT @NEWID = CategoryId FROM Ids 

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		COMMIT TRAN 
	END 

IF @IDTYPE = 'ImageId' 
	BEGIN 
		BEGIN TRAN 
		UPDATE Ids SET ImageId = ImageId + 1

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		/* Get new ID and return */
		SELECT @NEWID = ImageId FROM Ids 

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		COMMIT TRAN 
	END 

IF @IDTYPE = 'UserId' 
	BEGIN 
		BEGIN TRAN 
		UPDATE Ids SET UserId = UserId + 1

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		/* Get new ID and return */
		SELECT @NEWID = UserId FROM Ids 

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		COMMIT TRAN 
	END 

IF @IDTYPE = 'GalleryId' 
	BEGIN 
		BEGIN TRAN 
		UPDATE Ids SET GalleryId = GalleryId + 1

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		/* Get new ID and return */
		SELECT @NEWID = GalleryId FROM Ids 

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		COMMIT TRAN 
	END 

IF @IDTYPE = 'UserAppId' 
	BEGIN 
		BEGIN TRAN 
		UPDATE Ids SET UserAppId = UserAppId + 1

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		/* Get new ID and return */
		SELECT @NEWID = UserAppId FROM Ids 

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		COMMIT TRAN 
	END

IF @IDTYPE = 'TempGalleryId' 
	BEGIN 
		BEGIN TRAN 
		UPDATE Ids SET TempGalleryId = TempGalleryId + 1

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		/* Get new ID and return */
		SELECT @NEWID = TempGalleryId FROM Ids 

		SELECT @ERROR = @@ERROR, @ROWCOUNT = @@ROWCOUNT 
		IF @ROWCOUNT = 0 OR @ERROR <> 0 
		BEGIN 
			ROLLBACK TRAN 
			RETURN -1 
		END 

		COMMIT TRAN 
	END

GO
/****** Object:  StoredProcedure [dbo].[ReGenDynamicTags]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[ReGenDynamicTags] @UserId int
AS
SET NOCOUNT ON 

BEGIN
	DECLARE @TagId int;
	DECLARE @SQLExpression varchar(300)
	DECLARE @SortField varchar(50)
	DECLARE @MaxImageCount int
	DECLARE @RowCount int
	
	DECLARE @SQLQuery AS NVARCHAR(500)
	DECLARE @ParamDef AS NVARCHAR(100)
	CREATE TABLE #NewImageList (ImageId int)
	CREATE TABLE #AffectedTagsList (TagId int)
	CREATE INDEX TempIndex on #NewImageList (ImageId)
	
	DECLARE LoopingCursor CURSOR FOR  
	SELECT t.[TagId], td.[SqlExpression], td.[SortField], td.[MaxImageCount]
	FROM [Tag] t INNER JOIN [TagDefinition] td ON t.[DefinitionId] = td.[DefinitionId]
	WHERE t.[UserId] = @UserId
	AND td.[DynamicContent] = 1
	AND t.[SystemOwned] = 1

	OPEN LoopingCursor   
	FETCH NEXT FROM LoopingCursor INTO @TagId, @SQLExpression, @SortField, @MaxImageCount

	WHILE @@FETCH_STATUS = 0   
	BEGIN 

		BEGIN TRAN

		SET @SQLQuery = 'INSERT INTO #NewImageList ' +
						'SELECT ' + CASE WHEN @MaxImageCount > 0 THEN 'TOP ' + CAST(@MaxImageCount AS VARCHAR(10)) ELSE '' END +
						' i.[ImageId] 
						FROM [Image] i
						INNER JOIN [ImageMeta] im ON i.[ImageId] = im.[ImageId]
						WHERE i.[UserId] = ' + CAST(@UserId AS VARCHAR(10)) + ' AND i.[Status] = 4 ' +
						@SQLExpression +
						' ORDER BY ' + @SortField
		
		PRINT @SQLQuery
				
		EXECUTE(@SQLQuery)  --Insert into temp table
		
		--Delete any images in TagImage which are not in the new data set
		DELETE FROM [TagImage] WHERE [TagId] = @TagId AND [ImageId] NOT IN (SELECT NI.[ImageId] FROM #NewImageList NI)
		SET @RowCount = @@ROWCOUNT
		
		--Insert any new records not currently in the TagImage table.
		DELETE FROM #NewImageList WHERE [ImageId] IN (SELECT TI.[ImageId] FROM [TagImage] TI WHERE TI.[TagId] = @TagId)
		INSERT INTO [TagImage] SELECT @TagId, [ImageId] FROM #NewImageList NI 
		SET @RowCount = @RowCount + @@ROWCOUNT
		
		--If difference, then update Tag record with count and lastupdated
		IF @RowCount > 0
		BEGIN
			UPDATE [Tag] SET 
				[LastUpdated] = dbo.GetDateNoMS(), 
				[ImageCount] = (SELECT COUNT(1) FROM [TagImage] TI WHERE TI.[TagId] = @TagId)
			WHERE [TagId] = @TagId

			INSERT INTO #AffectedTagsList VALUES (@TagID)
		END
		
		COMMIT TRAN;
		
		TRUNCATE TABLE #NewImageList

		FETCH NEXT FROM LoopingCursor INTO @TagId, @SQLExpression, @SortField, @MaxImageCount
	END   

	CLOSE LoopingCursor   
	DEALLOCATE LoopingCursor

	SELECT TagId FROM #AffectedTagsList
END


GO
/****** Object:  StoredProcedure [dbo].[SetupNewDb]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO


CREATE PROCEDURE [dbo].[SetupNewDb]
AS 
BEGIN
	DELETE FROM [dbo].[AccountType]
	DELETE FROM [dbo].[App]
	DELETE FROM [dbo].[Category]
	DELETE FROM [dbo].[Gallery]
	DELETE FROM [dbo].[GalleryCategory]
	DELETE FROM [dbo].[GalleryImage]
	DELETE FROM [dbo].[GalleryPresentation]
	DELETE FROM [dbo].[GallerySection]
	DELETE FROM [dbo].[GallerySort]
	DELETE FROM [dbo].[GalleryStyle]
	DELETE FROM [dbo].[GalleryTag]
	DELETE FROM [dbo].[GalleryUser]
	DELETE FROM [dbo].[Ids]
	DELETE FROM [dbo].[Image]
	DELETE FROM [dbo].[ImageMeta]
	DELETE FROM [dbo].[MetaReference]
	DELETE FROM [dbo].[Platform]
	DELETE FROM [dbo].[Tag]
	DELETE FROM [dbo].[TagImage]
	DELETE FROM [dbo].[TagDefinition]
	DELETE FROM [dbo].[UdfDefinition]
	DELETE FROM [dbo].[User]
	DELETE FROM [dbo].[UserApp]

	INSERT INTO [dbo].[Platform] VALUES (100,'Web','Web Site','PC',1,NULL,NULL)
	INSERT INTO [dbo].[Platform] VALUES (200,'Win7','Windows','PC',1,6,1)
	INSERT INTO [dbo].[Platform] VALUES (300,'Win8','Windows','PC',1,6,2)
	INSERT INTO [dbo].[Platform] VALUES (310,'Win8.1','Windows','PC',1,6,3)
	INSERT INTO [dbo].[Platform] VALUES (500,'IPhone5','IOS', 'IPhone5',6,NULL,NULL)
	INSERT INTO [dbo].[Platform] VALUES (601,'IPad4','IOS','IPadRetina',6,NULL,NULL)
	INSERT INTO [Ids] VALUES (300000, 200000, 1000000, 100000, 400000, 500000, 600000)

	INSERT INTO [dbo].[App] VALUES (1,'Windows7','1234567890',1,1,1,200,100,200,1)

	INSERT INTO [dbo].[AccountType] VALUES (1,'Basic',50,-1,GETDATE(),NULL)

	INSERT INTO [dbo].[GalleryStyle] VALUES (1,'Dark Grey','','custom-darkness', dbo.GetDateNoMS())
	INSERT INTO [dbo].[GalleryStyle] VALUES (2,'Light Grey','','custom-lightness', dbo.GetDateNoMS())
	INSERT INTO [dbo].[GalleryStyle] VALUES (3,'Sky Blue','','custom-cupertino', dbo.GetDateNoMS())
	INSERT INTO [dbo].[GalleryStyle] VALUES (4,'Black and White','','custom-blacktie', GetDate())
	
	INSERT INTO [dbo].[GalleryPresentation] VALUES (1,'Classic','Show your images in a square grid.  Also group them together under your own sections.','gallery/viewer-standard','',10, 500, 75, 75, 1, 1, 1, 1, 1, dbo.GetDateNoMS())
	INSERT INTO [dbo].[GalleryPresentation] VALUES (2,'Classic (large)','Images displayed as large thumbnails.  Use sections to group them together.','gallery/viewer-standard','',10, 300, 300, 300, 1, 1, 1, 1, 1, dbo.GetDateNoMS())
	INSERT INTO [dbo].[GalleryPresentation] VALUES (3,'Lightbox','A contemporary gallery with slick interactions.  Show your images within grid, then zoom into each one.','gallery/viewer-lightbox','',0, 300, 75, 75, 1, 1, 1, 1, 0, dbo.GetDateNoMS())
	INSERT INTO [dbo].[GalleryPresentation] VALUES (4,'Lightbox (large)','A contemporary gallery with slick interactions.  Shows larger previews, then zoom into each one.','gallery/viewer-lightbox','',0, 150, 150, 150, 1, 1, 1, 1, 0, dbo.GetDateNoMS())

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(1,'Last Uploads','Latest Photos uploaded','','im.[UploadDate] DESC',1,0,500)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(2,'Today','Photos taken Today.','AND im.[TakenDateMeta] > CAST(GETDATE() AS DATE)','im.[TakenDateMeta] DESC',1,0,0)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(3,'This Week','Photos taken in the last 7 days.','AND im.[TakenDateMeta] > CAST(GETDATE() - 7 AS DATE)','im.[TakenDateMeta] DESC',1,0,0)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(4,'This Month','Photos taken during the current month.','AND DATEPART(mm,im.[TakenDateMeta]) = DATEPART(mm,GETDATE()) AND DATEPART(yy,im.[TakenDateMeta]) = DATEPART(yy,GETDATE())','im.[TakenDateMeta] DESC',1,0,0)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(5,'Last Month','Photos taken last month.','AND DATEPART(mm,im.[TakenDateMeta]) = DATEPART(mm,DATEADD(mm,-1,GETDATE())) AND DATEPART(yy,im.[TakenDateMeta]) = DATEPART(yy,DATEADD(mm,-1,GETDATE()))','im.[TakenDateMeta] DESC',1,0,0)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(6,'This Year','Photos taken this year.','AND DATEPART(yy,im.[TakenDateMeta]) = DATEPART(yy,GETDATE())','im.[TakenDateMeta] DESC',1,0,0)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(7,'Last Year','Photos taken last year.','AND DATEPART(yy,im.[TakenDateMeta]) = DATEPART(yy,DATEADD(yy,-1,GETDATE()))','im.[TakenDateMeta] DESC',1,0,0)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(101,'Jan','Photos taken during January.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 1 AND (DATEPART(mm,GETDATE()) = 1 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 1)','im.[TakenDateMeta] DESC',1,1,0)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(102,'Feb','Photos taken during February.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 2 AND (DATEPART(mm,GETDATE()) = 2 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 2)','im.[TakenDateMeta] DESC',1,2,0)

	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(103,'Mar','Photos taken during March.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 3 AND (DATEPART(mm,GETDATE()) = 3 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 3)','im.[TakenDateMeta] DESC',1,3,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(104,'Apr','Photos taken during April.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 4 AND (DATEPART(mm,GETDATE()) = 4 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 4)','im.[TakenDateMeta] DESC',1,4,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(105,'May','Photos taken during May.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 5 AND (DATEPART(mm,GETDATE()) = 5 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 5)','im.[TakenDateMeta] DESC',1,5,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(106,'Jun','Photos taken during June.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 6 AND (DATEPART(mm,GETDATE()) = 6 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 6)','im.[TakenDateMeta] DESC',1,6,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(107,'Jul','Photos taken during July.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 7 AND (DATEPART(mm,GETDATE()) = 7 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 7)','im.[TakenDateMeta] DESC',1,7,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(108,'Aug','Photos taken during August.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 8 AND (DATEPART(mm,GETDATE()) = 8 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 8)','im.[TakenDateMeta] DESC',1,8,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(109,'Sep','Photos taken during September.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 9 AND (DATEPART(mm,GETDATE()) = 9 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 9)','im.[TakenDateMeta] DESC',1,9,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(110,'Oct','Photos taken during October.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 10 AND (DATEPART(mm,GETDATE()) = 10 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 10)','im.[TakenDateMeta] DESC',1,10,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(111,'Nov','Photos taken during November.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 11 AND (DATEPART(mm,GETDATE()) = 11 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 11)','im.[TakenDateMeta] DESC',1,11,0)
	
	INSERT INTO [fotowalla].[dbo].[TagDefinition] VALUES
	(112,'Dec','Photos taken during December.','AND im.[TakenDateMeta] > GETDATE() - 365 AND DATEPART(mm,im.[TakenDateMeta]) = 12 AND (DATEPART(mm,GETDATE()) = 12 AND DATEPART(yy,[TakenDateMeta]) = DATEPART(yy,GETDATE()) OR DATEPART(mm,GETDATE()) != 12)','im.[TakenDateMeta] DESC',1,12,0)

END




GO
/****** Object:  StoredProcedure [dbo].[SetupNewUser]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[SetupNewUser] @ProfileName varchar(30), @Description varchar(200), 
								@Email varchar(100), @PasswordHash varchar(100),  @Salt varchar(100),
								@AccountType tinyint, @newUserId int out
AS
BEGIN

BEGIN TRAN

	EXECUTE GetId 'UserId', @newUserId out

	INSERT INTO [User] VALUES (@newUserId,@ProfileName,@Description,
							@Email,@PasswordHash,@Salt,0,NULL,dbo.GetDateNoMS(),dbo.GetDateNoMS(),
							dbo.GetDateNoMS(),1,@AccountType,1,dbo.GetDateNoMS(),null,0,'',null,0)

	DECLARE @newBaseCategory int;
	EXECUTE GetId 'CategoryId', @newBaseCategory out

	DECLARE @newUserCategory int;
	EXECUTE GetId 'CategoryId', @newUserCategory out

	INSERT INTO [Category] VALUES (@newBaseCategory,0,'Base Category','Account base category',0,dbo.GetDateNoMS(),1,1,1,@newUserId)
	INSERT INTO [Category] VALUES (@newUserCategory,@newBaseCategory,'My Categories','User defined categories',0,dbo.GetDateNoMS(),1,1,1,@newUserId)

	DECLARE @newPrivateTagId int;

	EXECUTE GetId 'TagId', @newPrivateTagId out
	INSERT INTO [dbo].[Tag] VALUES (@newPrivateTagId,'Private','Example tag which could be used to exlcude specific fotos from shared galleries.',0,0,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagId int;

	EXECUTE GetId 'TagId', @newTagId out
	INSERT INTO [dbo].[Tag] VALUES (@newTagId,'Favourite','Example tag which could hold your favourites',0,0,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDef1Id int;
	EXECUTE GetId 'TagId', @newTagDef1Id out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDef1Id,'','',1,1,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDef2Id int;
	EXECUTE GetId 'TagId', @newTagDef2Id out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDef2Id,'','',1,2,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDef3Id int;
	EXECUTE GetId 'TagId', @newTagDef3Id out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDef3Id,'','',1,3,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDef4Id int;
	EXECUTE GetId 'TagId', @newTagDef4Id out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDef4Id,'','',1,4,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDef5Id int;
	EXECUTE GetId 'TagId', @newTagDef5Id out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDef5Id,'','',1,5,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDef6Id int;
	EXECUTE GetId 'TagId', @newTagDef6Id out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDef6Id,'','',1,6,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDef7Id int;
		EXECUTE GetId 'TagId', @newTagDef7Id out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDef7Id,'','',1,7,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdJan int;
	EXECUTE GetId 'TagId', @newTagDefIdJan out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdJan,'','',1,101,0,dbo.GetDateNoMS(),1,@newUserId)

	DECLARE @newTagDefIdFeb int;
	EXECUTE GetId 'TagId', @newTagDefIdFeb out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdFeb,'','',1,102,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdMar int;
	EXECUTE GetId 'TagId', @newTagDefIdMar out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdMar,'','',1,103,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdApr int;
	EXECUTE GetId 'TagId', @newTagDefIdApr out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdApr,'','',1,104,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdMay int;
	EXECUTE GetId 'TagId', @newTagDefIdMay out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdMay,'','',1,105,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdJun int;
	EXECUTE GetId 'TagId', @newTagDefIdJun out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdJun,'','',1,106,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdJul int;
	EXECUTE GetId 'TagId', @newTagDefIdJul out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdJul,'','',1,107,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdAug int;
	EXECUTE GetId 'TagId', @newTagDefIdAug out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdAug,'','',1,108,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdSep int;
	EXECUTE GetId 'TagId', @newTagDefIdSep out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdSep,'','',1,109,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdOct int;
	EXECUTE GetId 'TagId', @newTagDefIdOct out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdOct,'','',1,110,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdNov int;
	EXECUTE GetId 'TagId', @newTagDefIdNov out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdNov,'','',1,111,0,dbo.GetDateNoMS(),1,@newUserId)
	
	DECLARE @newTagDefIdDec int;
	EXECUTE GetId 'TagId', @newTagDefIdDec out
	INSERT INTO [dbo].[Tag] VALUES (@newTagDefIdDec,'','',1,112,0,dbo.GetDateNoMS(),1,@newUserId)

	DECLARE @newGalleryId int;

	EXECUTE GetId 'GalleryId', @newGalleryId out
	INSERT INTO [dbo].[Gallery] VALUES
	(@newGalleryId,'Last Uploads','Recent fotowalla uploads.',REPLACE(newid(),'-',''),0,'',
	'',1,0,2,3,0,1,0,1,0,1,0,1,'',dbo.GetDateNoMS(),0,1,@newUserId);

	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDef1Id,0);

	EXECUTE GetId 'GalleryId', @newGalleryId out
	INSERT INTO [dbo].[Gallery] VALUES
	(@newGalleryId,'This Year','Photos taken during the last year, grouped by month',REPLACE(newid(),'-',''),0,'',
	'',1,2,2,1,0,1,0,1,0,1,0,0,'',dbo.GetDateNoMS(),0,1,@newUserId);

	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdJan,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdFeb,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdMar,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdApr,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdMay,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdJun,0);

	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdJul,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdAug,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdSep,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdOct,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdNov,0);
	INSERT INTO [dbo].[GalleryTag] VALUES (@newGalleryId,@newTagDefIdDec,0);


	COMMIT
END


GO
/****** Object:  StoredProcedure [dbo].[TagImageInsert]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [dbo].[TagImageInsert]
    @TagId int,
    @ImageId int
AS 
BEGIN
	SET NOCOUNT ON;

	IF NOT EXISTS(SELECT * FROM [TagImage] WHERE [TagId] = @TagId AND [ImageId] = @ImageId)
	BEGIN
		INSERT INTO [dbo].[TagImage] ([TagId],[ImageId]) VALUES (@TagId, @ImageId);
	END

END


GO
/****** Object:  UserDefinedFunction [dbo].[CategoryListDown]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[CategoryListDown] (@UserId int, @StartCategoryId int)
RETURNS @ReturnCategoryList TABLE
(
	[CategoryId] int primary key NOT NULL,
	[Name] varchar(30) NOT NULL,
	[Description] varchar(200) NULL,
	[ParentId] int NOT NULL
)
AS
BEGIN
	WITH HierachyFromItemCTE ([CategoryId], [Name], [Description], [ParentId])
	AS
	(
		select 
			CC.[CategoryId], CC.[Name], CC.[Description], CC.[ParentId] 
		from 
			Category CC 
		where
			CC.[CategoryId]= @StartCategoryId
			AND CC.[UserId] = @UserId
			AND CC.[Active] = 1
		union all
		select C.[CategoryId], C.[Name], C.[Description], C.[ParentId]
		from Category C inner join HierachyFromItemCTE T
		on T.[CategoryId] = C.[ParentId]
		WHERE C.[UserId] = @UserId
		AND C.[Active] = 1
	)
	INSERT @ReturnCategoryList SELECT * FROM HierachyFromItemCTE
	RETURN
END;


GO
/****** Object:  UserDefinedFunction [dbo].[CategoryListUp]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[CategoryListUp] (@UserId int, @StartCategoryId int)
RETURNS @ReturnCategoryList TABLE
(
	[CategoryId] int primary key NOT NULL,
	[ParentId] int NOT NULL
)
AS
BEGIN
	WITH HierachyFromItemCTE ([CategoryId], [ParentId])
	AS
	(
		select 
			CC.[CategoryId], CC.[ParentId] 
		from 
			Category CC 
		where
			CC.[CategoryId]= @StartCategoryId
			AND CC.[UserId] = @UserId
			AND CC.[Active] = 1
		union all
		select C.[CategoryId], C.[ParentId]
		from Category C inner join HierachyFromItemCTE T
		on T.[ParentId] = C.[CategoryId]
		WHERE C.[UserId] = @UserId
		AND C.[Active] = 1
	)
	INSERT @ReturnCategoryList SELECT * FROM HierachyFromItemCTE
	RETURN
END;


GO
/****** Object:  UserDefinedFunction [dbo].[CategoryListUpNoUser]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[CategoryListUpNoUser] (@StartCategoryId int)
RETURNS @ReturnCategoryList TABLE
(
	[CategoryId] int primary key NOT NULL,
	[ParentId] int NOT NULL
)
AS
BEGIN
	WITH HierachyFromItemCTE ([CategoryId], [ParentId])
	AS
	(
		select 
			CC.[CategoryId], CC.[ParentId] 
		from 
			Category CC 
		where
			CC.[CategoryId]= @StartCategoryId
		union all
		select C.[CategoryId], C.[ParentId]
		from Category C inner join HierachyFromItemCTE T
		on T.[ParentId] = C.[CategoryId]
	)
	INSERT @ReturnCategoryList SELECT * FROM HierachyFromItemCTE
	RETURN
END;


GO
/****** Object:  UserDefinedFunction [dbo].[GenerateGallerySectionsTemp]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[GenerateGallerySectionsTemp] (@UserId int, @TempGalleryId int, 
	@GroupingType tinyint, @SelectionType tinyint)
RETURNS @NewSectionList TABLE
(
	SectionId int NOT NULL,
	SectionName varchar(30) NOT NULL,
	SectionDesc varchar(200) NULL
)
AS
BEGIN

	/* @SelectionType = 0 - Category, 1 - Tag, 2 - Category AND Tag, 3 - Category OR Tag */
	/* @GroupingType = 0 - No Grouping, 1 - Category, 2 - Tag */

	IF @SelectionType = 0
	BEGIN
		--IF @SelectionType = 0 OR @SelectionType = 2 OR @SelectionType = 3
		--BEGIN
			INSERT INTO @NewSectionList
			SELECT GC.CategoryId, C.Name, C.[Description] FROM TempGalleryCategory GC
			INNER JOIN Category C ON GC.CategoryId = C.CategoryId 
			WHERE GC.TempGalleryId = @TempGalleryId AND GC.[Recursive] = 0 AND C.UserId = @UserId
			UNION
			SELECT CD.CategoryId, C.Name, C.[Description] FROM TempGalleryCategory GC 
				CROSS APPLY [CategoryListDown] (@UserId ,GC.CategoryId) AS CD
				INNER JOIN Category C ON C.CategoryId = CD.CategoryId 
				WHERE GC.TempGalleryId = @TempGalleryId AND GC.[Recursive] = 1
		--END

	END

	IF @SelectionType = 1
	BEGIN

		--IF @SelectionType = 1 OR @SelectionType = 2 OR @SelectionType = 3
		--BEGIN
			INSERT INTO @NewSectionList
			SELECT T.TagId, T.Name, T.[Description]
			FROM TempGalleryTag GT
			INNER JOIN TagView T ON GT.TagId = T.TagId
			WHERE GT.TempGalleryId = @TempGalleryId 
				AND T.UserId = @UserId
		--END	
	END

		--DELETE FROM TempGalleryCategory WHERE TempGalleryId = @TempGalleryId AND UserId = @UserId;
		--DELETE FROM TempGalleryTag WHERE TempGalleryId = @TempGalleryId AND UserId = @UserId;


	RETURN;
END






GO
/****** Object:  UserDefinedFunction [dbo].[GetDateNoMS]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE FUNCTION [dbo].[GetDateNoMS] ()
RETURNS DateTime
AS
BEGIN

	-- Return the result of the function
	RETURN DATEADD(ms, -DATEPART(ms,GETDATE()), GETDATE())

END


GO
/****** Object:  Table [dbo].[AccountType]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[AccountType](
	[AccountType] [tinyint] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[StorageGBLimit] [smallint] NOT NULL,
	[MonthlyUploadCap] [smallint] NOT NULL,
	[AvailableFrom] [date] NOT NULL,
	[AvailableTo] [date] NULL,
 CONSTRAINT [PK_AccountType] PRIMARY KEY CLUSTERED 
(
	[AccountType] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[App]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[App](
	[AppId] [smallint] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[WSKey] [varchar](200) NOT NULL,
	[MajorVersion] [tinyint] NOT NULL,
	[MinorVersion] [tinyint] NOT NULL,
	[Status] [smallint] NOT NULL,
	[DefaultFetchSize] [tinyint] NOT NULL,
	[DefaultThumbCacheMB] [tinyint] NOT NULL,
	[DefaultMainCopyCacheMB] [tinyint] NOT NULL,
	[DefaultGalleryType] [tinyint] NULL,
 CONSTRAINT [PK_Application] PRIMARY KEY CLUSTERED 
(
	[AppId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[Category]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[Category](
	[CategoryId] [int] NOT NULL,
	[ParentId] [int] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[Description] [varchar](200) NULL,
	[ImageCount] [int] NOT NULL,
	[LastUpdated] [datetime] NOT NULL,
	[RecordVersion] [smallint] NOT NULL,
	[Active] [bit] NOT NULL,
	[SystemOwned] [bit] NOT NULL,
	[UserId] [int] NOT NULL,
 CONSTRAINT [PK_Category] PRIMARY KEY CLUSTERED 
(
	[CategoryId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[Gallery]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[Gallery](
	[GalleryId] [int] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[Description] [varchar](200) NULL,
	[UrlComplex] [varchar](200) NOT NULL,
	[AccessType] [tinyint] NOT NULL,
	[PasswordHash] [varchar](100) NULL,
	[GallerySalt] [varchar](100) NULL,
	[SelectionType] [tinyint] NOT NULL,
	[GroupingType] [tinyint] NOT NULL,
	[StyleId] [tinyint] NOT NULL,
	[PresentationId] [tinyint] NOT NULL,
	[TotalImageCount] [int] NULL,
	[ShowGalleryName] [bit] NULL,
	[ShowGalleryDesc] [bit] NULL,
	[ShowImageName] [bit] NULL,
	[ShowImageDesc] [bit] NULL,
	[ShowImageMeta] [bit] NULL,
	[ShowGroupingDesc] [bit] NULL,
	[GalleryType] [tinyint] NOT NULL,
	[TempSalt] [varchar](100) NULL,
	[LastUpdated] [datetime] NOT NULL,
	[RecordVersion] [smallint] NOT NULL,
	[SystemOwned] [bit] NOT NULL,
	[UserId] [int] NOT NULL,
 CONSTRAINT [PK_UserView] PRIMARY KEY CLUSTERED 
(
	[GalleryId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[GalleryCategory]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[GalleryCategory](
	[GalleryId] [int] NOT NULL,
	[CategoryId] [int] NOT NULL,
	[Recursive] [bit] NOT NULL,
 CONSTRAINT [PK_ViewCategory] PRIMARY KEY CLUSTERED 
(
	[GalleryId] ASC,
	[CategoryId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[GalleryImage]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[GalleryImage](
	[GalleryId] [int] NOT NULL,
	[SectionId] [int] NOT NULL,
	[ImageId] [int] NOT NULL,
 CONSTRAINT [PK_GalleryImage_1] PRIMARY KEY CLUSTERED 
(
	[GalleryId] ASC,
	[SectionId] ASC,
	[ImageId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[GalleryPresentation]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[GalleryPresentation](
	[PresentationId] [tinyint] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[Description] [varchar](200) NOT NULL,
	[JspName] [varchar](30) NOT NULL,
	[CssExtension] [varchar](30) NOT NULL,
	[MaxSections] [tinyint] NOT NULL,
	[MaxImagesInSection] [int] NOT NULL,
	[ThumbWidth] [smallint] NOT NULL,
	[ThumbHeight] [smallint] NOT NULL,
	[OptionGalleryName] [bit] NOT NULL,
	[OptionGalleryDesc] [bit] NOT NULL,
	[OptionImageName] [bit] NOT NULL,
	[OptionImageDesc] [bit] NOT NULL,
	[OptionGroupingDesc] [bit] NOT NULL,
	[LastUpdated] [datetime] NOT NULL,
 CONSTRAINT [PK_GalleryPresentation] PRIMARY KEY CLUSTERED 
(
	[PresentationId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[GallerySection]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[GallerySection](
	[GalleryId] [int] NOT NULL,
	[SectionId] [int] NOT NULL,
	[ImageCount] [int] NULL,
	[Sequence] [tinyint] NULL,
	[NameOverride] [varchar](30) NULL,
	[DescOverride] [varchar](200) NULL
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[GallerySort]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[GallerySort](
	[GalleryId] [int] NOT NULL,
	[FieldName] [varchar](30) NOT NULL,
	[Ascending] [bit] NOT NULL,
 CONSTRAINT [PK_ViewSort] PRIMARY KEY CLUSTERED 
(
	[GalleryId] ASC,
	[FieldName] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[GalleryStyle]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[GalleryStyle](
	[StyleId] [tinyint] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[Description] [varchar](200) NOT NULL,
	[CssFolder] [varchar](20) NOT NULL,
	[LastUpdated] [datetime] NOT NULL,
 CONSTRAINT [PK_GalleryStyle] PRIMARY KEY CLUSTERED 
(
	[StyleId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[GalleryTag]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[GalleryTag](
	[GalleryId] [int] NOT NULL,
	[TagId] [int] NOT NULL,
	[Exclude] [bit] NOT NULL,
 CONSTRAINT [PK_ViewTag] PRIMARY KEY CLUSTERED 
(
	[GalleryId] ASC,
	[TagId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[GalleryUser]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[GalleryUser](
	[GalleryId] [int] NOT NULL,
	[EmailAddress] [varchar](100) NULL,
 CONSTRAINT [PK_ViewUser] PRIMARY KEY CLUSTERED 
(
	[GalleryId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[Ids]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[Ids](
	[CategoryId] [int] NOT NULL,
	[TagId] [int] NOT NULL,
	[ImageId] [int] NOT NULL,
	[UserId] [int] NOT NULL,
	[GalleryId] [int] NOT NULL,
	[UserAppId] [int] NOT NULL,
	[TempGalleryId] [int] NULL
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[Image]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[Image](
	[ImageId] [int] NOT NULL,
	[CategoryId] [int] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[Description] [varchar](200) NULL,
	[OriginalFileName] [varchar](255) NOT NULL,
	[Format] [varchar](5) NOT NULL,
	[Status] [tinyint] NOT NULL,
	[RecordVersion] [smallint] NOT NULL,
	[LastUpdated] [datetime] NOT NULL,
	[UserAppId] [int] NOT NULL,
	[Error] [bit] NOT NULL,
	[ErrorMessage] [varchar](200) NULL,
	[UserId] [int] NOT NULL,
 CONSTRAINT [PK_Image] PRIMARY KEY CLUSTERED 
(
	[ImageId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[ImageMeta]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ImageMeta](
	[ImageId] [int] NOT NULL,
	[Width] [smallint] NOT NULL,
	[Height] [smallint] NOT NULL,
	[Size] [int] NOT NULL,
	[CameraMaker] [varchar](100) NULL,
	[CameraModel] [varchar](100) NULL,
	[Aperture] [varchar](10) NULL,
	[ShutterSpeed] [varchar](20) NULL,
	[ISO] [smallint] NULL,
	[Orientation] [tinyint] NULL,
	[TakenDate] [datetime] NULL,
	[TakenDateFile] [datetime] NULL,
	[TakenDateMeta] [datetime] NULL,
	[UploadDate] [datetime] NULL,
	[UdfChar1] [varchar](30) NULL,
	[UdfChar2] [varchar](30) NULL,
	[UdfChar3] [varchar](30) NULL,
	[UdfText1] [varchar](500) NULL,
	[UdfNum1] [float] NULL,
	[UdfNum2] [float] NULL,
	[UdfNum3] [float] NULL,
	[UdfDate1] [date] NULL,
	[UdfDate2] [date] NULL,
	[UdfDate3] [date] NULL,
 CONSTRAINT [PK_ImageMeta] PRIMARY KEY CLUSTERED 
(
	[ImageId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[ImageMetaRaw]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[ImageMetaRaw](
	[ImageId] [int] NOT NULL,
	[MetaType] [varchar](4) NOT NULL,
	[MetaRaw] [varchar](max) NOT NULL,
 CONSTRAINT [PK_ImageMetaRaw] PRIMARY KEY CLUSTERED 
(
	[ImageId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[MetaReference]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[MetaReference](
	[UserId] [int] NOT NULL,
	[FieldName] [varchar](30) NOT NULL,
	[StringValue] [varchar](30) NULL,
	[IntValue] [int] NULL,
 CONSTRAINT [PK_MetaReference] PRIMARY KEY CLUSTERED 
(
	[UserId] ASC,
	[FieldName] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[Platform]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[Platform](
	[PlatformId] [smallint] NOT NULL,
	[ShortName] [varchar](10) NOT NULL,
	[OperatingSystem] [varchar](30) NOT NULL,
	[MachineType] [varchar](30) NOT NULL,
	[Supported] [bit] NOT NULL,
	[MajorVersion] [smallint] NULL,
	[MinorVersion] [smallint] NULL,
 CONSTRAINT [PK_Platform] PRIMARY KEY CLUSTERED 
(
	[PlatformId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[Tag]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[Tag](
	[TagId] [int] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[Description] [varchar](200) NULL,
	[SystemOwned] [bit] NOT NULL,
	[DefinitionId] [tinyint] NULL,
	[ImageCount] [int] NOT NULL,
	[LastUpdated] [datetime] NOT NULL,
	[RecordVersion] [smallint] NOT NULL,
	[UserId] [int] NOT NULL,
 CONSTRAINT [PK_Tag1] PRIMARY KEY CLUSTERED 
(
	[TagId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TagDefinition]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[TagDefinition](
	[DefinitionId] [int] NOT NULL,
	[Name] [varchar](30) NOT NULL,
	[Description] [varchar](200) NULL,
	[SqlExpression] [varchar](400) NOT NULL,
	[SortField] [varchar](400) NOT NULL,
	[DynamicContent] [bit] NOT NULL,
	[Month] [smallint] NOT NULL,
	[MaxImageCount] [smallint] NOT NULL,
 CONSTRAINT [PK_Definition] PRIMARY KEY CLUSTERED 
(
	[DefinitionId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[TagImage]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TagImage](
	[TagId] [int] NOT NULL,
	[ImageId] [int] NOT NULL,
 CONSTRAINT [PK_TagImage1] PRIMARY KEY CLUSTERED 
(
	[TagId] ASC,
	[ImageId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[TempGalleryCategory]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TempGalleryCategory](
	[TempGalleryId] [int] NOT NULL,
	[CategoryId] [int] NOT NULL,
	[Recursive] [bit] NOT NULL,
	[UserId] [int] NOT NULL,
 CONSTRAINT [PK_TempGalleryCategory] PRIMARY KEY CLUSTERED 
(
	[TempGalleryId] ASC,
	[CategoryId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[TempGalleryTag]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE TABLE [dbo].[TempGalleryTag](
	[TempGalleryId] [int] NOT NULL,
	[TagId] [int] NOT NULL,
	[UserId] [int] NOT NULL,
 CONSTRAINT [PK_TempGalleryTag] PRIMARY KEY CLUSTERED 
(
	[TempGalleryId] ASC,
	[TagId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
/****** Object:  Table [dbo].[UdfDefinition]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[UdfDefinition](
	[UserId] [int] NOT NULL,
	[FieldName] [varchar](20) NOT NULL,
	[UserDefinedName] [varchar](20) NULL,
	[Active] [bit] NOT NULL,
	[Reference] [bit] NOT NULL,
	[Description] [varchar](200) NULL,
 CONSTRAINT [PK_MetaDefinition] PRIMARY KEY CLUSTERED 
(
	[UserId] ASC,
	[FieldName] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[User]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[User](
	[UserId] [int] NOT NULL,
	[ProfileName] [varchar](30) NOT NULL,
	[Description] [varchar](200) NULL,
	[Email] [varchar](100) NOT NULL,
	[PasswordHash] [varchar](100) NOT NULL,
	[Salt] [varchar](100) NULL,
	[FailedLoginCount] [tinyint] NULL,
	[FailedLoginLast] [datetime] NULL,
	[TagLastDeleted] [datetime] NULL,
	[CategoryLastDeleted] [datetime] NULL,
	[GalleryLastDeleted] [datetime] NULL,
	[Status] [tinyint] NULL,
	[AccountType] [tinyint] NOT NULL,
	[RecordVersion] [tinyint] NOT NULL,
	[OpenDate] [datetime] NULL,
	[CloseDate] [datetime] NULL,
	[EmailStatus] [tinyint] NULL,
	[ValidationString] [varchar](32) NULL,
	[EmailSent] [datetime] NULL,
	[BankStatus] [tinyint] NULL,
 CONSTRAINT [PK_User] PRIMARY KEY CLUSTERED 
(
	[UserId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  Table [dbo].[UserApp]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
SET ANSI_PADDING ON
GO
CREATE TABLE [dbo].[UserApp](
	[UserAppId] [int] NOT NULL,
	[PlatformId] [smallint] NOT NULL,
	[AppId] [smallint] NOT NULL,
	[MachineName] [varchar](100) NOT NULL,
	[LastUsed] [datetime] NOT NULL,
	[Blocked] [bit] NOT NULL,
	[TagId] [int] NOT NULL,
	[UserAppCategoryId] [int] NOT NULL,
	[UserDefaultCategoryId] [int] NOT NULL,
	[GalleryId] [int] NOT NULL,
	[FetchSize] [smallint] NOT NULL,
	[ThumbCacheMB] [smallint] NOT NULL,
	[MainCopyCacheMB] [smallint] NOT NULL,
	[MainCopyFolder] [varchar](500) NOT NULL,
	[AutoUpload] [bit] NOT NULL,
	[AutoUploadFolder] [varchar](500) NOT NULL,
	[RecordVersion] [smallint] NOT NULL,
	[UserId] [int] NOT NULL,
 CONSTRAINT [PK_Machine] PRIMARY KEY CLUSTERED 
(
	[UserAppId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]

GO
SET ANSI_PADDING OFF
GO
/****** Object:  View [dbo].[AccountSummary]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[AccountSummary]
AS
SELECT U.[UserId],U.[ProfileName], U.[Description], U.[Email], U.[Status], AT.[Name] AS AccountTypeName, 
U.[RecordVersion], U.[OpenDate], U.[CloseDate], AT.[StorageGBLimit], AT.[MonthlyUploadCap],
CASE WHEN Totals.TotalImages IS NULL THEN 0 ELSE Totals.TotalImages END AS TotalImages, 
CASE WHEN Totals.TotalSizeBytes IS NULL THEN 0.0 ELSE ROUND((Totals.TotalSizeBytes / 1024.0 / 1024.0 / 1024.0),2) END AS StorageGBCurrent, 
CASE WHEN MonthTotal.UploadCount30Days IS NULL THEN 0 ELSE MonthTotal.UploadCount30Days END AS UploadCount30Days 
FROM [dbo].[User] U 
INNER JOIN [dbo].[AccountType] AT ON U.AccountType = AT.AccountType
LEFT OUTER JOIN (SELECT I.[UserId], Count(1) AS TotalImages, Sum(IM.[Size]) AS TotalSizeBytes
			FROM [Image] I INNER JOIN [ImageMeta] IM ON I.ImageId = IM.ImageId 
			WHERE I.[Status] IN (0,1,2,3,4)
			GROUP BY I.UserId) AS Totals ON U.UserId = Totals.UserId 
LEFT OUTER JOIN (SELECT I.[UserId], Count(1) AS UploadCount30Days
			FROM [Image] I INNER JOIN [ImageMeta] IM ON I.ImageId = IM.ImageId 
			WHERE I.[Status] IN (0,1,2,3,4) AND IM.UploadDate > DATEADD(d,-30,GetDate())
			GROUP BY I.UserId) AS MonthTotal ON U.UserId = MonthTotal.UserId


GO
/****** Object:  View [dbo].[GallerysLinkedToCategories]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[GallerysLinkedToCategories]
AS
SELECT G.GalleryId, ALLCATS.CategoryId, G.UserId 
FROM Gallery G 
INNER JOIN 
	(
		SELECT GC.GalleryId, GC.CategoryId FROM GalleryCategory GC INNER JOIN Category C ON GC.CategoryId = C.CategoryId 
		WHERE GC.[Recursive] = 0
		UNION
		SELECT GC.GalleryId, CD.CategoryId FROM GalleryCategory GC 
		CROSS APPLY [CategoryListUpNoUser] (GC.CategoryId) AS CD
		WHERE GC.[Recursive] = 1
	) AS ALLCATS ON G.GalleryId = ALLCATS.GalleryId


GO
/****** Object:  View [dbo].[HierachyFromItemView]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[HierachyFromItemView]
as
WITH HierachyFromItemCTE ([CategoryId], [Name], [Description], [ParentId])
AS
(
	select 
		CC.[CategoryId], CC.[Name], CC.[Description], CC.[ParentId] 
	from 
		Category CC 
	where 
		CC.[ParentId]=0
	union all
	select C.[CategoryId], C.[Name], C.[Description], C.[ParentId]
	from Category C inner join HierachyFromItemCTE T
	on C.[ParentId] = T.[CategoryId]
)
select * from HierachyFromItemCTE


GO
/****** Object:  View [dbo].[TagView]    Script Date: 18/11/2014 22:56:09 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE VIEW [dbo].[TagView]
AS
SELECT [TagId], [Name], [Description], [ImageCount], [LastUpdated], [RecordVersion], [SystemOwned], [UserId] 
FROM [Tag] WHERE [DefinitionId] = 0
UNION
SELECT [TagId], td.[Name], td.[Description], [ImageCount], [LastUpdated], [RecordVersion], [SystemOwned], [UserId] 
FROM [Tag] t INNER JOIN [TagDefinition] td on t.DefinitionId = td.DefinitionId
WHERE t.[DefinitionId] > 0


GO
/****** Object:  Index [IX_Category]    Script Date: 18/11/2014 22:56:09 ******/
CREATE NONCLUSTERED INDEX [IX_Category] ON [dbo].[Category]
(
	[UserId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
/****** Object:  Index [IX_Category_1]    Script Date: 18/11/2014 22:56:09 ******/
CREATE NONCLUSTERED INDEX [IX_Category_1] ON [dbo].[Category]
(
	[ParentId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
/****** Object:  Index [IX_UserView]    Script Date: 18/11/2014 22:56:09 ******/
CREATE NONCLUSTERED INDEX [IX_UserView] ON [dbo].[Gallery]
(
	[UserId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
/****** Object:  Index [IX_Image]    Script Date: 18/11/2014 22:56:09 ******/
CREATE NONCLUSTERED INDEX [IX_Image] ON [dbo].[Image]
(
	[UserId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
/****** Object:  Index [IX_Image_1]    Script Date: 18/11/2014 22:56:09 ******/
CREATE NONCLUSTERED INDEX [IX_Image_1] ON [dbo].[Image]
(
	[CategoryId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
SET ANSI_PADDING ON

GO
/****** Object:  Index [IX_User]    Script Date: 18/11/2014 22:56:09 ******/
CREATE UNIQUE NONCLUSTERED INDEX [IX_User] ON [dbo].[User]
(
	[ProfileName] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
/****** Object:  Index [IX_Machine]    Script Date: 18/11/2014 22:56:09 ******/
CREATE NONCLUSTERED INDEX [IX_Machine] ON [dbo].[UserApp]
(
	[UserId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
/****** Object:  Index [IX_Machine_1]    Script Date: 18/11/2014 22:56:09 ******/
CREATE NONCLUSTERED INDEX [IX_Machine_1] ON [dbo].[UserApp]
(
	[PlatformId] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
GO
ALTER TABLE [dbo].[Category]  WITH NOCHECK ADD  CONSTRAINT [FK_Category_Category] FOREIGN KEY([UserId])
REFERENCES [dbo].[User] ([UserId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[Category] NOCHECK CONSTRAINT [FK_Category_Category]
GO
ALTER TABLE [dbo].[Gallery]  WITH NOCHECK ADD  CONSTRAINT [FK_UserView_User] FOREIGN KEY([UserId])
REFERENCES [dbo].[User] ([UserId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[Gallery] NOCHECK CONSTRAINT [FK_UserView_User]
GO
ALTER TABLE [dbo].[GalleryCategory]  WITH NOCHECK ADD  CONSTRAINT [FK_GalleryCategory_Category] FOREIGN KEY([CategoryId])
REFERENCES [dbo].[Category] ([CategoryId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[GalleryCategory] NOCHECK CONSTRAINT [FK_GalleryCategory_Category]
GO
ALTER TABLE [dbo].[GalleryCategory]  WITH NOCHECK ADD  CONSTRAINT [FK_GalleryCategory_Gallery] FOREIGN KEY([GalleryId])
REFERENCES [dbo].[Gallery] ([GalleryId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[GalleryCategory] NOCHECK CONSTRAINT [FK_GalleryCategory_Gallery]
GO
ALTER TABLE [dbo].[GalleryImage]  WITH NOCHECK ADD  CONSTRAINT [FK_GalleryImage_Gallery] FOREIGN KEY([GalleryId])
REFERENCES [dbo].[Gallery] ([GalleryId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[GalleryImage] NOCHECK CONSTRAINT [FK_GalleryImage_Gallery]
GO
ALTER TABLE [dbo].[GalleryImage]  WITH NOCHECK ADD  CONSTRAINT [FK_GalleryImage_Image] FOREIGN KEY([ImageId])
REFERENCES [dbo].[Image] ([ImageId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[GalleryImage] NOCHECK CONSTRAINT [FK_GalleryImage_Image]
GO
ALTER TABLE [dbo].[GallerySort]  WITH NOCHECK ADD  CONSTRAINT [FK_GallerySort_UserView] FOREIGN KEY([GalleryId])
REFERENCES [dbo].[Gallery] ([GalleryId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[GallerySort] NOCHECK CONSTRAINT [FK_GallerySort_UserView]
GO
ALTER TABLE [dbo].[GalleryTag]  WITH NOCHECK ADD  CONSTRAINT [FK_GalleryTag_UserView] FOREIGN KEY([GalleryId])
REFERENCES [dbo].[Gallery] ([GalleryId])
GO
ALTER TABLE [dbo].[GalleryTag] NOCHECK CONSTRAINT [FK_GalleryTag_UserView]
GO
ALTER TABLE [dbo].[GalleryUser]  WITH NOCHECK ADD  CONSTRAINT [FK_GalleryUser_Gallery] FOREIGN KEY([GalleryId])
REFERENCES [dbo].[Gallery] ([GalleryId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[GalleryUser] NOCHECK CONSTRAINT [FK_GalleryUser_Gallery]
GO
ALTER TABLE [dbo].[Image]  WITH NOCHECK ADD  CONSTRAINT [FK_Image_User] FOREIGN KEY([UserId])
REFERENCES [dbo].[User] ([UserId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[Image] NOCHECK CONSTRAINT [FK_Image_User]
GO
ALTER TABLE [dbo].[ImageMeta]  WITH NOCHECK ADD  CONSTRAINT [FK_ImageMeta_Image] FOREIGN KEY([ImageId])
REFERENCES [dbo].[Image] ([ImageId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[ImageMeta] NOCHECK CONSTRAINT [FK_ImageMeta_Image]
GO
ALTER TABLE [dbo].[MetaReference]  WITH NOCHECK ADD  CONSTRAINT [FK_MetaReference_User] FOREIGN KEY([UserId])
REFERENCES [dbo].[User] ([UserId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[MetaReference] NOCHECK CONSTRAINT [FK_MetaReference_User]
GO
ALTER TABLE [dbo].[UdfDefinition]  WITH NOCHECK ADD  CONSTRAINT [FK_UdfDefinition_User] FOREIGN KEY([UserId])
REFERENCES [dbo].[User] ([UserId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[UdfDefinition] NOCHECK CONSTRAINT [FK_UdfDefinition_User]
GO
ALTER TABLE [dbo].[UserApp]  WITH NOCHECK ADD  CONSTRAINT [FK_Machine_Platform] FOREIGN KEY([PlatformId])
REFERENCES [dbo].[Platform] ([PlatformId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[UserApp] NOCHECK CONSTRAINT [FK_Machine_Platform]
GO
ALTER TABLE [dbo].[UserApp]  WITH NOCHECK ADD  CONSTRAINT [FK_Machine_User] FOREIGN KEY([UserId])
REFERENCES [dbo].[User] ([UserId])
NOT FOR REPLICATION 
GO
ALTER TABLE [dbo].[UserApp] NOCHECK CONSTRAINT [FK_Machine_User]
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'0 - Category, 1 - Tag, 2 - Category & Tag, 3 - Category OR Tag' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'Gallery', @level2type=N'COLUMN',@level2name=N'SelectionType'
GO
EXEC sys.sp_addextendedproperty @name=N'MS_Description', @value=N'0 - None, 1 - Category, 2 - Tags' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'Gallery', @level2type=N'COLUMN',@level2name=N'GroupingType'
GO
USE [master]
GO
ALTER DATABASE [fotowalla] SET  READ_WRITE 
GO
