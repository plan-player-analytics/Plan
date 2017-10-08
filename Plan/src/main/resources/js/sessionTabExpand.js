$(".session-header").click(function () {
	$header = $(this);
	$content = $header.next();
	$(this).parent().siblings().children().next().slideUp(500);

    $header.html(function(i, origText) {
        $(".session-header").html(function(i, origText) {
            return origText.replace("fa-chevron-up", "fa-chevron-down")
        });
        if (origText.includes("fa-chevron-down")) {
            return origText.replace("fa-chevron-down", "fa-chevron-up")
        } else {
            return origText.replace("fa-chevron-up", "fa-chevron-down")
        }
    });
	$content.slideToggle(500);
});