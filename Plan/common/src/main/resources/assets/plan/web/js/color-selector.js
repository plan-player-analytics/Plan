(function ($) {
    var bgElements = ['.sidebar', '.btn'];
    var textElements = [];

    var colors = ['plan',
        'red', 'pink', 'purple', 'deep-purple',
        'indigo', 'blue', 'light-blue', 'cyan',
        'teal', 'green', 'light-green', 'lime',
        'yellow', 'amber', 'orange', 'deep-orange',
        'brown', 'grey', 'blue-grey'];

    var selectedColor = window.localStorage.getItem('themeColor');
    var themeDefaultColor = 'plan';
    var currentColor = 'plan';

    if (selectedColor === null) {
        window.localStorage.setItem('themeColor', currentColor);
    }

    // Function for changing color
    function setColor(nextColor) {
        if (!nextColor || nextColor == currentColor) {
            return;
        }

        for (i in bgElements) {
            var element = bgElements[i];
            $(element + '.bg-' + currentColor + ":not(.color-chooser)")
                .removeClass('bg-' + currentColor)
                .addClass('bg-' + nextColor);
        }
        for (i in textElements) {
            var element = textElements[i];
            $(element + '.col-' + currentColor)
                .removeClass('col-' + currentColor)
                .addClass('col-' + nextColor);
        }
        if (nextColor != 'night') {
            window.localStorage.setItem('themeColor', nextColor);
        }
        currentColor = nextColor;
    }

    // Set the color changing function for all color change buttons
    function enableColorSetters() {
        function colorSetter(i) {
            return function () {
                setColor(colors[i]);
            }
        }

        for (i in colors) {
            var color = colors[i];
            var func = colorSetter(i);
            $('#choose-' + color).on('click', func);
            $('#choose-' + color).addClass('bg-' + color);
        }
    }

    enableColorSetters();

    function disableColorSetters() {
        for (i in colors) {
            var color = colors[i];
            $('#choose-' + color).addClass('disabled').unbind('click');
        }
    }

    // Change the color of the theme
    setColor(selectedColor ? selectedColor : themeDefaultColor);

    var nightMode = window.localStorage.getItem('nightMode') == 'true';

    function changeNightModeCSS() {
        if (nightMode) {
            // Background colors from dracula theme
            $('head').append('<style id="nightmode">' +
                '#content {background-color:#282a36;}' +
                '.card,.bg-white,.modal-content,.page-loader,hr {background-color:#44475a;border-color:#6272a4;}' +
                '.bg-white.collapse-inner {border 1px solid;}' +
                '.card-header {background-color:#44475a;border-color:#6272a4;}' +
                '#content,.col-black,.text-gray-800,.collapse-item,.modal-title,.modal-body,.page-loader,.close {color:#eee8d5 !important;}' +
                '.collapse-item:hover,.nav-link.active {background-color: #606270 !important;}' +
                '</style>');
            // Turn bright tables to dark
            $('.table').addClass('table-dark');
            // Sidebar is grey when in night mode
            disableColorSetters();
            setColor('night');
        } else {
            // Remove night mode style sheet
            $('#nightmode').remove();
            // Turn dark tables bright again
            $('.table').removeClass('table-dark');
            // Sidebar is colorful
            $('.color-chooser').removeClass('disabled');
            enableColorSetters();
            setColor(window.localStorage.getItem('themeColor'));
        }
    }

    changeNightModeCSS();

    function toggleNightMode() {
        if (nightMode) {
            nightMode = false;
        } else {
            nightMode = true;
        }
        window.localStorage.setItem('nightMode', nightMode);
        changeNightModeCSS();
    }

    $('#night-mode-toggle').on('click', toggleNightMode);

})(jQuery);