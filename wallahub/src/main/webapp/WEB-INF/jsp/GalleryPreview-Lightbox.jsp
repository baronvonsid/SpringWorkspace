<%@page import="walla.datatypes.auto.*" pageEncoding="UTF-8"%>
<%@page import="walla.datatypes.java.*"%>
<%@page import="walla.business.*"%>
<%@page import="java.util.*"%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>

    <title>FotoWalla - Viewer</title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="content-script-type" content="text/javascript" />
    <meta name='description' content='FotoWalla - your trusted online foto gallery.' />
    <meta name='apple-mobile-web-app-title' content='FotoWalla' />

    <!-- Ensure that on IE the page is rendered to the maximum IE spec. -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />

    <!-- Used for viewing on mobile devices.  Ensuring the width is correctly infered -->

    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <!-- Adding javascript to enable CSS style for semantic tags pre IE9 -->
    <!--[if lt IE 9]>
         <script src="../../static/scripts/html5-format-ie8.js"></script>
    <![endif]-->

    <link rel="stylesheet" href="../../../../static/css/blueimp/blueimp-gallery-indicator.css" />
    <link rel="stylesheet" href="../../../../static/css/blueimp/blueimp-gallery.css" />
    <link href="../../../../static/css/${css}/ThemeExt-Lightbox.css" rel="stylesheet" />
    <link href="../../../../static/css/TemplateExt-Lightbox.css" rel="stylesheet" />

    <!-- Browser bar icon (16x16) -->
    <link href="../../../../static/images/fotowallabrowser.png" rel="shortcut icon" type="image/vnd.microsoft.icon" />

</head>

<%
	boolean showGalleryName = Boolean.parseBoolean(request.getAttribute("showGalleryName").toString());
	boolean showGalleryDesc = Boolean.parseBoolean(request.getAttribute("showGalleryDesc").toString());
	boolean showImageName = Boolean.parseBoolean(request.getAttribute("showImageName").toString());
	boolean showImageDesc = Boolean.parseBoolean(request.getAttribute("showImageDesc").toString());
	boolean isPreview = Boolean.parseBoolean(request.getAttribute("isPreview").toString());
	
	//TODO Change!! Was just switched because of building new project
	//int imageSize = (int)request.getAttribute("imageSize");
	int imageSize = 200;
	ImageList imageList = (ImageList)request.getAttribute("imageList");
%>

<body id="galleryBody" data-images-fetchsize="576" data-groupings-type="${groupingType}" data-total-image-count="${totalImageCount}">

        <nav id="pageNavigations">

        <%if (showGalleryName || showGalleryDesc) {%>
        	<header id="pageHeader" class="HeaderStyle">
        	<%if (showGalleryName) {%>
        		<h1>${name}</h1>
        	<%} if (showGalleryDesc) { %>
        		<span class="HeaderStyle">${desc}</span>
        	<%}%>
        	</header>
        <%}%>
        
       	<div style="clear: both;"></div>
       	</nav>
        
    <div id="blueimp-gallery" class="blueimp-gallery">
        <div class="slides"></div>
        <h3 class="title"></h3>
        <a class="prev">‹</a>
        <a class="next">›</a>
        <a class="close">×</a>
        <a class="play-pause"></a>
        <ol class="indicator"></ol>
    </div>

	<div id="links" class="links">
	
<% 
	int mainImageWidth = 1920;
	int mainImageHeight = 1080;

	if (imageList.getImages() != null)
	{
		if (imageList.getImages().getImageRef().size() > 0)
		{
			//Construct update SQL statements
			for (Iterator<ImageList.Images.ImageRef> imageIterater = imageList.getImages().getImageRef().iterator(); imageIterater.hasNext();)
			{
				ImageList.Images.ImageRef current = (ImageList.Images.ImageRef)imageIterater.next();
				
				if (current.getName() == null || current.getName().isEmpty())
					current.setName("");
				
				if (current.getDesc() == null || current.getDesc().isEmpty())
					current.setDesc("");
				
				String name = "";
				String fullNameDesc = "";
				
				if (showImageName && showImageDesc)
				{
					name = current.getName();
					fullNameDesc = current.getName() + ((current.getDesc().length() > 0) ? ". " + current.getDesc() : "");
				}
				else if (showImageName)
				{
					name = current.getName();
					fullNameDesc = current.getName();
				}
				else if (showImageDesc)
				{
					name = current.getDesc();
					fullNameDesc = current.getDesc();
				}
								
				String imageMainPath = "../../../ws/" + request.getAttribute("profileName").toString() + "/imagepreview/" + current.getId() + "/" + mainImageWidth + "/" + mainImageHeight + "/";
				String imageThumbPath = "../../../ws/" + request.getAttribute("profileName").toString() + "/imagepreview/" + current.getId() + "/" + imageSize + "/" + imageSize + "/";
				String output = "<a href=\"" + imageMainPath + "\" title=\"" + fullNameDesc + "\"><img src=\"" + imageThumbPath + "\" title=\"" + name + "\"/></a>";
				%><%=output%><%
			}
		}
	}
%>
    </div>

    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>

    <script src="../../../../static/scripts/blueimp/blueimp-gallery.min.js"></script>
    <!-- 
    <script src="../../../../static/scripts/blueimp/blueimp-gallery-fullscreen.js"></script>
    <script src="../../../../static/scripts/blueimp/blueimp-gallery-indicator.js"></script>-->

    <script>
        document.getElementById("links").onclick = function (event) {
            event = event || window.event;
            var target = event.target || event.srcElement,
                link = target.src ? target.parentNode : target,
                options = { index: link, event: event },
                links = this.getElementsByTagName('a');
            blueimp.Gallery(links, options);
        };
    </script>

</body>
   

</html>
