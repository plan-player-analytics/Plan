$(function () {
    skinChanger();

    setSkin();

    setSkinListHeightAndScroll(true);
    $(window).resize(function () {
        setSkinListHeightAndScroll(false);
    });
});

//Skin changer
function skinChanger() {
    $('.right-sidebar .demo-choose-skin li').on('click', function () {
        var $body = $('body');
        var $this = $(this);

        var existTheme = $('.right-sidebar .demo-choose-skin li.active').data('theme');
        $('.right-sidebar .demo-choose-skin li').removeClass('active');
        $body.removeClass('theme-' + existTheme);
        $this.addClass('active');

        var theme = $this.data('theme');
        $body.addClass('theme-' + theme);
        localStorage.setItem("plan_skin", theme.toString());
    });
}

function setSkin() {
    var theme = localStorage.getItem("plan_skin");
    if (theme === null) {
        theme = '${defaultTheme}'
    }
    var body = document.getElementsByTagName('body')[0];

    var classes = body.className.split(' ');
    var themeClass;
    for (i = 0; i < classes.length; i++) {
        if (classes[i].startsWith('theme')) {
            themeClass = classes[i];
            break;
        }
    }

    body.classList.remove(themeClass);
    body.classList.add('theme-' + theme);
    localStorage.setItem("plan_skin", theme);
}

//Skin tab content set height and show scroll
function setSkinListHeightAndScroll(isFirstTime) {
    var height = $(window).height() - ($('.navbar').innerHeight() + $('.right-sidebar .nav-tabs').outerHeight());
    var $el = $('.demo-choose-skin');

    if (!isFirstTime) {
        $el.slimScroll({destroy: true}).height('auto');
        $el.parent().find('.slimScrollBar, .slimScrollRail').remove();
    }

    $el.slimscroll({
        height: height + 'px',
        color: 'rgba(0,0,0,0.5)',
        size: '6px',
        alwaysVisible: false,
        borderRadius: '0',
        railBorderRadius: '0'
    });
}