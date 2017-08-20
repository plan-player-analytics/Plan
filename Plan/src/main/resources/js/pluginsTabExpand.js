$(".plugins-header").click(function () {
	$header = $(this);                
	$content = $header.next();
	$(this).parent().siblings().children().next().slideUp(500);
	$content.slideToggle(500, function () {
		//execute this after slideToggle is done
	});
});