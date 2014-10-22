var showNames = true;

/*
Body - #galleryBody
data-show-names="true" 
data-images-fetchsize="100" 
data-groupings-type="2" 

Section - Images - #ImagesPane{sectionId}

*/

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

        var totalImages = $("#galleryBody").attr("data-total-images");
        ImageNavUpdate($("#galleryBody").attr("data-total-images"), $("#galleryBody").attr("data-images-first"), $("#galleryBody").attr("data-images-last"), $("#galleryBody").attr("data-images-fetchsize"));

        /* Event hooks */

        $("#sectNavHor > input").change(
            function (e) {
                $("#sectNavHor > input").button("option", { icons: { primary: "ui-icon-bullet" } });
                var button = $(this);
                if (button.is(":checked")) {
                    button.button("option", { icons: { primary: "ui-icon-check" } });
                }
            });

        $("#imageNavFirst").click(function () { FetchImagesList("first") });
        $("#imageNavPrevious").click(function () { FetchImagesList("previous") });
        $("#imageNavNext").click(function () { FetchImagesList("next") });
        $("#imageNavLast").click(function () { FetchImagesList("last") });
    }
);

function ImageNavUpdate(totalImages, imagesFirst, imagesLast, imagesFetchSize) {
    if (totalImages < 0) {
        $("#imageNavTextTotal").text("loading images");
        $("#imageNav").hide();
        return;
    }

    $("#imageNavTextTotal").text(totalImages + " images");

    if (totalImages <= imagesFetchSize) {
        $("#imageNav").hide();
        return;
    }

    $("#imageNavTextTotal").text(imagesFirst + " to " + imagesLast);

    if (imagesFirst == 0) {
        $("#imageNavFirst").button("disable");
        $("#imageNavPrevious").button("disable");
        $("#imageNavNext").button("enable");
        $("#imageNavLast").button("enable");
        return;
    }

    if (imagesLast >= totalImages) {
        $("#imageNavFirst").button("enable");
        $("#imageNavPrevious").button("enable");
        $("#imageNavNext").button("disable");
        $("#imageNavLast").button("disable");
        return;
    }
}

function FetchImagesList(direction) {
    var queryString;

    switch (direction) {
        case "first":
            alert(direction);
            break;
        case "previous":
            alert(direction);
            break;
        case "next":
        	RetrieveImageListFromServer("notneeded", 0, 100);
            break;
        case "last":
            alert(direction);
            break;
    }
}

function RetrieveImageListFromServer(galleryName, cursor, size) {

    var url = document.location.pathname + "/0/" + cursor + "/" + size;
    /* Prepare form for asyncronous request */

    ImageNavUpdate(-1, 0, 0, 0);
    $("#ImagesPane").empty();
    //TODO add loading images.

    //$.ajax({ url: url, type: "GET", dataType: "xml",success:function(xhr){ResponseSuccess(xhr);});
    //request.done(function (


    $.get(url, null, null, "html").done(function (data, status, jqXHR) { ResponseSuccess(data, status, jqXHR); }).fail(function (data, status, errorThrown) { ResponseFail(data, status, errorThrown); });
    //alert(response.responseText);

}

function ResponseSuccess(data, status, jqXHR) {
    $("#ImagesPane").html(data);
}

function ResponseFail(data, status, errorThrown) {
    alert(errorThrown.toString());
}