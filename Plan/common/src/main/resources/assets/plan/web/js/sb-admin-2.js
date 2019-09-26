function openTab(i) {
    var x = document.getElementById("content");
    var navButtons = document.getElementsByClassName("nav-button");
    var max = navButtons.length;
    for (var j = 0; j < max; j++) {
        if (navButtons[j].classList.contains('active')) {
            navButtons[j].classList.remove('active');
        }
        if (j === i) {
            navButtons[j].classList.add('active');
        }
    }
    var percent = -100 / navButtons.length;
    slideIndex = i;
    if (slideIndex > max) {
        slideIndex = 0
    }
    if (slideIndex < 0) {
        slideIndex = max
    }
    window.scrollTo(0, 0);
    var value = slideIndex * percent;
    x.style.transition = "0.5s";
    x.style.transform = "translate3d(" + value + "%,0px,0)";
}

function openPage() {
    var params = (window.location.hash.substr(5)).split("&");

    if (!params.length) {
        openTab(0);
        return;
    }
    // window.sessionStorage.setItem("server_slide_index", slideIndex);

    var tabID = params[0];
    var button = $('.nav-button[href="#' + tabID + '"]');

    var tabs = document.getElementsByClassName("tab");
    for (var i = 0; i < tabs.length; i++) {
        if (tabs[i].id === tabID) openTab(i);
    }

    if (params.length <= 1) {
        return;
    }

    var graphTabID = params[1];
    $('a[href="#' + graphTabID + '"]').tab('show');
}

(function ($) {
    "use strict"; // Start of use strict

    var x = document.getElementById("content");
    // Prepare tabs for display
    var navButtons = document.getElementsByClassName("nav-button");
    var tabs = document.getElementsByClassName("tab");
    x.style.transform = "translate3d(0px,0px,0)";
    x.style.width = "" + navButtons.length * 100 + "%";
    for (var i = 0; i < navButtons.length; i++) {
        tabs[i].style.width = "" + 100 / navButtons.length + "%";
    }
    x.style.opacity = "1";

    window.addEventListener('hashchange', function (e) {
        openPage();
    });

    // Persistent Bootstrap tabs
    $('.nav-tabs a.nav-link').click(function (e) {
        var params = (window.location.hash).split("&");
        if (!params) return;
        window.location.hash = params[0] + '&' + e.target.href.split('#')[1];
    });

    var oldWidth = null;

    function reduceSidebar() {
        var newWidth = $(window).width();
        if (oldWidth && oldWidth === newWidth) {
            return;
        }

        var $sidebar = $('.sidebar');
        var closeModal = $('.sidebar-close-modal');
        if ($(window).width() < 1350) {
            if (!$sidebar.hasClass('hidden')) $sidebar.addClass('hidden');
            if (!closeModal.hasClass('hidden')) closeModal.addClass('hidden');

            $('.sidebar .collapse').collapse('hide');
        } else if ($(window).width() > 1400 && $sidebar.hasClass('hidden')) {
            $sidebar.removeClass('hidden');
            if (!closeModal.hasClass('hidden')) closeModal.addClass('hidden');
        }
        oldWidth = newWidth;
    }

    reduceSidebar();

    function toggleSidebar() {
        $('.sidebar').toggleClass('hidden');
        $('.sidebar .collapse').collapse('hide');

        var closeModal = $('.sidebar-close-modal');
        if ($(window).width() < 900) {
            closeModal.toggleClass('hidden');
        } else {
            if (!closeModal.hasClass('hidden')) closeModal.addClass('hidden');
        }
    }

    $('.sidebar-toggler,.sidebar-close-modal').on('click', toggleSidebar);

    // Close any open menu accordions when window is resized below 924px
    $(window).resize(reduceSidebar);

    // Prevent the content wrapper from scrolling when the fixed side navigation hovered over
    $('body.fixed-nav .sidebar').on('mousewheel DOMMouseScroll wheel', function (e) {
        if ($(window).width() > 924) {
            var e0 = e.originalEvent,
                delta = e0.wheelDelta || -e0.detail;
            this.scrollTop += (delta < 0 ? 1 : -1) * 30;
            e.preventDefault();
        }
    });

    // Scroll to top button appear
    $(document).on('scroll', function () {
        var scrollDistance = $(this).scrollTop();
        if (scrollDistance > 100) {
            $('.scroll-to-top').fadeIn();
        } else {
            $('.scroll-to-top').fadeOut();
        }
    });

    // Smooth scrolling using jQuery easing
    $(document).on('click', 'a.scroll-to-top', function (e) {
        var $anchor = $(this);
        $('html, body').stop().animate({
            scrollTop: ($($anchor.attr('href')).offset().top)
        }, 1000, 'easeInOutExpo');
        e.preventDefault();
    });

})(jQuery); // End of use strict
