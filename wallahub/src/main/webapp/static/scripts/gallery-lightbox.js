var numberOfImages = 0;

$(document).ready
(
    function () {

        ResizeDiv(true);

        $(window).resize(function () { ResizeDiv(false); });
    }
);

function ResizeDiv(force) {
	
	var thumbWidth = +($("#galleryBody").attr("data-thumbwidth"));
	var borderSize = 2*2;
	var windowPad = +20;
	var maximum = +1200;
    var factor = +Math.min(($(window).width() - windowPad),maximum) / (thumbWidth + borderSize);
    
    var proposedNumberOfImages = Math.floor(factor);
    if (numberOfImages != proposedNumberOfImages || force) {
        var newWidth = (proposedNumberOfImages * (thumbWidth + borderSize));
        //$("#links").width(newWidth.toString() + "px");
        //$("#pageNavigations").width(newWidth.toString() + "px");
        $("#galleryBody").width(newWidth.toString() + "px");
        
        //console.info($(window).width() + " " + newWidth + " " + proposedNumberOfImages);
        numberOfImages = proposedNumberOfImages;
    }
}