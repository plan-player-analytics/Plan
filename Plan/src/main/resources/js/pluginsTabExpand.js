$(".plugins-header").click(function () {
    var $header = $(this);
    var $content = $header.next();
	$(this).parent().siblings().children().next().slideUp(500);
	$content.slideToggle(500, function () {
		//execute this after slideToggle is done
	});
});