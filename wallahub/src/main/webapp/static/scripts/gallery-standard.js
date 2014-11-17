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
        	  tLoading: 'Loading image #%curr%...',
              mainClass: 'mfp-img-mobile',
				gallery: {
					enabled: true,
					navigateByImgClick: true,
					preload: [0,1] // Will preload 0 - before current, and 1 after the current image
				},
              image: {
            		tError: '<a href="%url%">The image #%curr%</a> could not be loaded.',
        			titleSrc: function(item) 
        			{
        				var name = (item.el.attr('title')) ? item.el.attr('title') : '';
        				var fileMeta = (item.el.attr('data-file-meta')) ? item.el.attr('data-file-meta') : '';
        				var shotMeta = (item.el.attr('data-shot-meta')) ? item.el.attr('data-shot-meta') : '';
        				var meta = fileMeta + ((shotMeta.length > 0) ? ' - ' + shotMeta : '');
        				var desc = (item.el.attr('data-desc')) ? item.el.attr('data-desc') : '';
        				var output = '';
        				
        				if (meta.length > 0 && desc.length > 0 && name.length > 0)
        				{ output = name + ' - ' + desc + '<small>' + meta + '</small>'; }
        				else if (desc.length > 0 && name.length > 0)
        				{ output = name + '<small>' + desc + '</small>'; }
        				else if (meta.length > 0 && name.length > 0)
        				{ output = name + '<small>' + meta + '</small>'; }
        				else if (name.length > 0)
        				{ output = name; }
        				else if (desc.length > 0 && meta.length > 0)
        				{ output = desc + '<small>' + meta + '</small>'; }
        				else if (desc.length > 0)
        				{ output = desc; }
        				else if (meta.length > 0)
        				{ output = meta; }
        				
        				
        				return output;}
              }
                  
        	});
        
        /*
         * 
         * 
        mainClass: "mfp-no-margins mfp-with-zoom",
        fixedContentPos: true,
                          closeOnContentClick: true,
                  closeBtnInside: false,
                  
        verticalFit: true
        zoom: {
                    enabled: true,
                    duration: 300
                  }
        
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
	
	var thumbWidth = +($("#galleryBody").attr("data-thumbwidth"));
	var borderSize = 2*2;
	var windowPad = 20;
    var factor = ($(window).width() - windowPad) / (thumbWidth + borderSize);
    
    var proposedNumberOfImages = Math.floor(factor);
    if (numberOfImages != proposedNumberOfImages || force) {
        var newWidth = (proposedNumberOfImages * (thumbWidth + borderSize));
        $("#imagesPane").width(newWidth.toString() + "px");
        $("#pageNavigations").width(newWidth.toString() + "px");
        
        //var newHeaderWidth = newWidth - 200;
        //$("#pageHeader").width(newHeaderWidth.toString() + "px");
        //$("#pageNavigations").width(newWidth.toString() + "px");
        
        //console.info($(window).width() + " " + newWidth + " " + proposedNumberOfImages);
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