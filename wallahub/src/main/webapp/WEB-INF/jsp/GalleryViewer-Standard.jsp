<%@page import="walla.datatypes.auto.*"%>
<%@page import="walla.datatypes.java.*"%>
<%@page import="java.util.*"%>


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>FotoWalla - Viewer</title>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta http-equiv="content-script-type" content="text/javascript" />
    <meta name='description' content='FotoWalla - your trusted online foto gallery.'/>
    <meta name='apple-mobile-web-app-title' content='FotoWalla'/>

    <!-- Ensure that on IE the page is rendered to the maximum IE spec. -->
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>

    <!-- Used for viewing on mobile devices.  Ensuring the width is correctly infered -->

    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <!-- Adding javascript to enable CSS style for semantic tags pre IE9 -->
    <!--[if lt IE 9]>
         <script src="../../../../static/scripts/html5-format-ie8.js"></script>
    <![endif]-->

    <link href="../../../../static/css/TemplateExt-Standard.css" rel="stylesheet" />
    <link href="../../../../static/css/${css}/jquery-ui.min.css" rel="stylesheet" />
    <link href="../../../../static/css/${css}/ThemeExt-Standard.css" rel="stylesheet" />
	<link href="../../../../static/css/magnific-popup/magnific-popup.css" rel="stylesheet" />
	
    <!-- Browser bar icon (16x16) -->
    <link href="../../../../static/images/fotowallabrowser.png" rel="shortcut icon" type="image/vnd.microsoft.icon" />

</head>

<%
	boolean showGalleryName = Boolean.parseBoolean(request.getAttribute("showGalleryName").toString());
	boolean showGalleryDesc = Boolean.parseBoolean(request.getAttribute("showGalleryDesc").toString());
	int groupingType = Integer.valueOf((String)request.getAttribute("groupingType").toString());
%>


<body id="galleryBody" data-images-fetchsize="50" data-groupings-type="${groupingType}" data-total-image-count="${totalImageCount}" data-is-preview="${isPreview}">
        
        <nav id="pageNavigations">

        <%if (showGalleryName || showGalleryDesc) {%>
        	<header id="pageHeader" class="HeaderStyle">
        	<%if (showGalleryName) {%>
        		<h1>${name}</h1>
        	<%} if (showGalleryDesc) { %>
        		<span class="HeaderStyle">${desc}</span>
        	<%}%>
        	</header>
        <%}
        
        if (groupingType == 0)
        {
        	//Nav and sections on same line.
        	%>
       	        <nav id="imageNav" width="200px">
		            <span id="imageNavTextTotal"></span>
		            <button id="imageNavFirst">first</button>
		            <button id="imageNavPrevious">previous</button>
		            <span id="imageNavTextCursor">0 to 0</span>
		            <button id="imageNavNext">next</button>
		            <button id="imageNavLast">last</button>
		        </nav>
        	<div style="clear: both;"></div></nav>
        	<%
        }
        else
        {
        	if (showGalleryName || showGalleryDesc)
        	{
        	 %><div style="clear: both;"></div><%	
        	}
        	
        	
        	//Header and nav on same line.
        	List<Gallery.Sections.SectionRef> sectionList = (List<Gallery.Sections.SectionRef>)request.getAttribute("sectionList");
        	if (sectionList != null)
        	{
        		%><nav id="sectNavHor"><%
        		for (Iterator<Gallery.Sections.SectionRef> sectionIterater = sectionList.iterator() ; sectionIterater.hasNext();)
        		{
        			Gallery.Sections.SectionRef currentSectionRef = (Gallery.Sections.SectionRef)sectionIterater.next();
        			%>
        		    <input type="radio" id="section<%=currentSectionRef.getId() %>" name="sectNavHor" data-section-id="<%=currentSectionRef.getId()%>" />
        		    <label for="section<%=currentSectionRef.getId()%>"><%=currentSectionRef.getName()%></label>
        			<%
        		}
        		%></nav><%
        	}
        	%>
        	    <nav id="imageNav" width="200px">
		            <span id="imageNavTextTotal"></span>
		            <button id="imageNavFirst">first</button>
		            <button id="imageNavPrevious">previous</button>
		            <span id="imageNavTextCursor">0 to 0</span>
		            <button id="imageNavNext">next</button>
		            <button id="imageNavLast">last</button>
		        </nav>
        	</nav>
        	
        	

        	
        	
        	<div style="clear: both;"></div><%}%>


	<div class="ImagesPaneStyle" id="imagesPaneContainer">
		<section id="imagesPane" class="ImagesPaneStyle" data-section-id="0" data-section-image-count="-1" data-images-first="0" data-images-last="0"></section>
	</div>
    
    <script src="//ajax.googleapis.com/ajax/libs/jquery/1.9.1/jquery.min.js"></script>
    <!-- <script src="../../../../static/scripts/jquery-2.0.3.js"></script>-->
    <script src="../../../../static/scripts/jquery-ui-1.10.3.custom.js"></script>
    <!-- <script src="../../../../static/scripts/jquery-1.9.1.min.js"></script>-->
    <script src="../../../../static/scripts/magnific-popup/jquery.magnific-popup.js"></script>
    
    <script>
    var numberOfImages = 0;

    $(document).ready
    (
        function () {
            /* Setup defaults - run once on document ready */
            $("#imageNavFirst").button({ text: false, icons: { primary: "ui-icon-arrowthickstop-1-w" } });
            $("#imageNavPrevious").button({ text: false, icons: { primary: "ui-icon-arrowthick-1-w" } });
            $("#imageNavNext").button({ text: false, icons: { primary: "ui-icon-arrowthick-1-e " } });
            $("#imageNavLast").button({ text: false, icons: { primary: "ui-icon-arrowthickstop-1-e" } });

            $("#sectNavHor").buttonset();
            $("#sectNavHor > input").button({ icons: { primary: "ui-icon-bullet" } });

            ResizeDiv(true);
			
            
            $('#imagesPaneContainer').magnificPopup({
            	  delegate: 'a', // child items selector, by clicking on it popup will open
            	  type: 'image',
                      closeOnContentClick: true,
                      closeBtnInside: false,
                      fixedContentPos: true,
                      mainClass: "mfp-no-margins mfp-with-zoom",
                      image: {
                        verticalFit: true
                      },
                      zoom: {
                        enabled: true,
                        duration: 300
                      }
            	});
            
            /*
            $(".image-popup-no-margins").magnificPopup({
                type: "image",
                closeOnContentClick: true,
                closeBtnInside: false,
                fixedContentPos: true,
                mainClass: "mfp-no-margins mfp-with-zoom",
                image: {
                  verticalFit: true
                },
                zoom: {
                  enabled: true,
                  duration: 300
                }
              });
            */
            
            /* Event hooks */

            $("#sectNavHor > input").change(
                function (e) {
                    $("#sectNavHor > input").button("option", { icons: { primary: "ui-icon-bullet" } });
                    var button = $(this);
                    if (button.is(":checked")) {
                        button.button("option", { icons: { primary: "ui-icon-check" } }
                        );
                        FetchImagesList("first");
                    }
                });

            $("#imageNavFirst").click(function () { FetchImagesList("first"); });
            $("#imageNavPrevious").click(function () { FetchImagesList("previous"); });
            $("#imageNavNext").click(function () { FetchImagesList("next"); });
            $("#imageNavLast").click(function () { FetchImagesList("last"); });
            
            $(window).resize(function () { ResizeDiv(false); });
            
        	if (+$("#galleryBody").attr("data-groupings-type") > 0)
        	{
        		$("#sectNavHor > input:first-child").click();
        	}
        	else
        	{
        		FetchImagesList("first");
        	}
        }
    );

    function ResizeDiv(force) {
        var factor = ($(window).width() - 20) / 204;
        var proposedNumberOfImages = Math.floor(factor);
        if (numberOfImages != proposedNumberOfImages || force) {
            var newWidth = (proposedNumberOfImages * 204) + 20;
            $("#imagesPane").width(newWidth.toString() + "px");
            //$("#pageHeader").width(newWidth.toString() + "px");
            $("#pageNavigations").width(newWidth.toString() + "px");
            
            var newHeaderWidth = newWidth - 200;
            //$("#pageHeader").width(newHeaderWidth.toString() + "px");
            //$("#pageNavigations").width(newWidth.toString() + "px");
            
            console.info($(window).width() + " " + newWidth);
            numberOfImages = proposedNumberOfImages;
        }
    }
    
    function ImageNavUpdate(totalImages, imagesFirst, imagesLast, imagesFetchSize) {
        
    	//alert(totalImages + ' ' + imagesFirst + ' ' + imagesFetchSize);
    	
    	//Images have been requested.
    	if (totalImages < 0) {
            $("#imageNavTextTotal").text("loading images");
            ShowImageNav(false);
       		return;
        }
    	
    	if (totalImages == 0) {
            $("#imageNavTextTotal").text("No images");
            ShowImageNav(false);
       		return;
        }

        $("#imageNavTextTotal").text(totalImages + " images");

        if (totalImages <= imagesFetchSize) {
       		ShowImageNav(false);
       		return;
        }
        else
       	{
       		ShowImageNav(true);
       	}

        $("#imageNavTextCursor").text(imagesFirst + " to " + imagesLast);

        $("#imageNavFirst").button("enable");
        $("#imageNavPrevious").button("enable");
        $("#imageNavNext").button("enable");
        $("#imageNavLast").button("enable");
        
        if (imagesFirst == 0) {
            $("#imageNavFirst").button("disable");
            $("#imageNavPrevious").button("disable");
            return;
        }

        if ((imagesFirst + imagesFetchSize) >= totalImages) {
            $("#imageNavNext").button("disable");
            $("#imageNavLast").button("disable");
            return;
        }
    }

    function ShowImageNav(show)
    {
    	if (show)
    	{
	        $("#imageNavFirst").show();
	        $("#imageNavPrevious").show();
	        $("#imageNavTextCursor").show();
	        $("#imageNavNext").show();
	        $("#imageNavLast").show();
    	}
    	else
   		{
           $("#imageNavFirst").hide();
           $("#imageNavPrevious").hide();
           $("#imageNavTextCursor").hide();
           $("#imageNavNext").hide();
           $("#imageNavLast").hide();
   		}
    }
    
    function FetchImagesList(direction) {
    	
    	var fetchSize = +$("#galleryBody").attr("data-images-fetchsize");
    	var firstImage = +$("#imagesPane").attr("data-images-first");
    	var lastImage = +$("#imagesPane").attr("data-images-last");
    	var sectionImageCount = +$("#imagesPane").attr("data-section-image-count");
    	
    	//alert($('#sectNavHor > input[name=sectNavHor]:checked').attr("data-section-id"));
    	
    	var sectionId = -1;
    	if (+$("#galleryBody").attr("data-groupings-type") > 0)
    	{
    		sectionId = +$('#sectNavHor > input[name=sectNavHor]:checked').attr("data-section-id");
    	}
    	
    	//alert($("#galleryBody").attr("data-groupings-type"));
    	//alert(sectionId);
    	
    	//$("#imagesPane").attr("data-images-last"), $("#galleryBody").attr("data-images-fetchsize"));
    	
        switch (direction) {
            case "first":
            	RetrieveImageListFromServer(sectionId, 0, fetchSize);
                break;
            case "previous":
            	RetrieveImageListFromServer(sectionId, Math.max((firstImage - fetchSize),0), fetchSize);
                break;
            case "next":
            	RetrieveImageListFromServer(sectionId, (firstImage + fetchSize), fetchSize);
                break;
            case "last":
            	RetrieveImageListFromServer(sectionId, Math.floor(sectionImageCount / fetchSize) * fetchSize, fetchSize);
                break;
        }
    }

    function RetrieveImageListFromServer(sectionId, cursor, size) {

        var url = document.location.pathname + "/" + sectionId + "/" + cursor + "/" + size;
        /* Prepare form for asyncronous request */

        ImageNavUpdate(-1, 0, 0, 0);
        $("#imagesPaneContainer").empty();
        //TODO add loading images.

        //$.ajax({ url: url, type: "GET", dataType: "xml",success:function(xhr){ResponseSuccess(xhr);});
        //request.done(function (


        $.get(url, null, null, "html").done(function (data, status, jqXHR) { ResponseSuccess(data, status, jqXHR); }).fail(function (data, status, errorThrown) { ResponseFail(data, status, errorThrown); });
        //alert(response.responseText);

    }

    function ResponseSuccess(data, status, jqXHR) {
    	//alert(jqXHR.responseText);

        $("#imagesPaneContainer").html(data);

        var imageCount = 0;
        
    	if (+$("#galleryBody").attr("data-groupings-type") > 0)
    	{
    		imageCount = +$("#imagesPane").attr("data-section-image-count");
    	}
    	else
    	{
    		imageCount = +$("#galleryBody").attr("data-total-image-count");
    	}
        
        
        //var totalImages = $("#imagesPane").attr("data-total-images");
        ImageNavUpdate(imageCount, +$("#imagesPane").attr("data-images-first"), +$("#imagesPane").attr("data-images-last"), +$("#galleryBody").attr("data-images-fetchsize"));
        
        ResizeDiv(true);
    }

    function ResponseFail(data, status, errorThrown) {
        alert(errorThrown.toString());
    }
    </script>
    
    
    
</body>
</html>