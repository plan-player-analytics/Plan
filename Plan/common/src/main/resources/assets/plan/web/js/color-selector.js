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

    var saturationReduction = 0.70;

    // From https://stackoverflow.com/a/3732187
    function withReducedSaturation(colorHex) {
        // To RGB
        var r = parseInt(colorHex.substr(1, 2), 16); // Grab the hex representation of red (chars 1-2) and convert to decimal (base 10).
        var g = parseInt(colorHex.substr(3, 2), 16);
        var b = parseInt(colorHex.substr(5, 2), 16);

        // To HSL
        r /= 255;
        g /= 255;
        b /= 255;
        var max = Math.max(r, g, b), min = Math.min(r, g, b);
        var h, s, l = (max + min) / 2;

        if (max === min) {
            h = s = 0; // achromatic
        } else {
            var d = max - min;
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
            switch (max) {
                case r:
                    h = (g - b) / d + (g < b ? 6 : 0);
                    break;
                case g:
                    h = (b - r) / d + 2;
                    break;
                case b:
                    h = (r - g) / d + 4;
                    break;
            }
            h /= 6;
        }

        // To css property
        return 'hsl(' + h * 360 + ',' + s * 100 * saturationReduction + '%,' + l * 95 + '%)';
    }

    var nightModeColors = '.bg-red {background-color: ' + withReducedSaturation('#F44336') + ';color: #eee8d5;}' +
        '.bg-pink {background-color: ' + withReducedSaturation('#E91E63') + ';color: #eee8d5;}' +
        '.bg-purple {background-color: ' + withReducedSaturation('#9C27B0') + ';color: #eee8d5;}' +
        '.bg-deep-purple {background-color: ' + withReducedSaturation('#673AB7') + ';color: #eee8d5;}' +
        '.bg-indigo {background-color: ' + withReducedSaturation('#3F51B5') + ';color: #eee8d5;}' +
        '.bg-blue {background-color: ' + withReducedSaturation('#2196F3') + ';color: #eee8d5;}' +
        '.bg-light-blue {background-color: ' + withReducedSaturation('#03A9F4') + ';color: #eee8d5;}' +
        '.bg-cyan {background-color: ' + withReducedSaturation('#00BCD4') + ';color: #eee8d5;}' +
        '.bg-teal {background-color: ' + withReducedSaturation('#009688') + ';color: #eee8d5;}' +
        '.bg-green {background-color: ' + withReducedSaturation('#4CAF50') + ';color: #eee8d5;}' +
        '.bg-light-green {background-color: ' + withReducedSaturation('#8BC34A') + ';color: #eee8d5;}' +
        '.bg-lime {background-color: ' + withReducedSaturation('#CDDC39') + ';color: #eee8d5;}' +
        '.bg-yellow {background-color: ' + withReducedSaturation('#ffe821') + ';color: #eee8d5;}' +
        '.bg-amber {background-color: ' + withReducedSaturation('#FFC107') + ';color: #eee8d5;}' +
        '.badge-warning {background-color: ' + withReducedSaturation('#f6c23e') + ';color: #eee8d5;}' +
        '.bg-orange {background-color: ' + withReducedSaturation('#FF9800') + ';color: #eee8d5;}' +
        '.bg-deep-orange {background-color: ' + withReducedSaturation('#FF5722') + ';color: #eee8d5;}' +
        '.badge-danger {background-color: ' + withReducedSaturation('#e74a3b') + ';color: #eee8d5;}' +
        '.bg-brown {background-color: ' + withReducedSaturation('#795548') + ';color: #eee8d5;}' +
        '.bg-grey {background-color: ' + withReducedSaturation('#9E9E9E') + ';color: #eee8d5;}' +
        '.bg-blue-grey {background-color: ' + withReducedSaturation('#607D8B') + ';color: #eee8d5;}' +
        '.bg-black {background-color: ' + withReducedSaturation('#555555') + ';color: #eee8d5;}' +
        '.bg-plan {background-color: ' + withReducedSaturation('#368F17') + ';color: #eee8d5;}' +
        '.badge-success {background-color: ' + withReducedSaturation('#1cc88a') + ';color: #eee8d5;}' +
        '.bg-night {background-color: #44475a;color: #eee8d5;}' +
        '.col-red {color: ' + withReducedSaturation('#F44336') + ';}' +
        '.col-pink {color: ' + withReducedSaturation('#E91E63') + ';}' +
        '.col-purple {color: ' + withReducedSaturation('#9C27B0') + ';}' +
        '.col-deep-purple {color: ' + withReducedSaturation('#673AB7') + ';}' +
        '.col-indigo {color: ' + withReducedSaturation('#3F51B5') + ';}' +
        '.col-blue {color: ' + withReducedSaturation('#2196F3') + ';}' +
        '.col-light-blue {color: ' + withReducedSaturation('#03A9F4') + ';}' +
        '.col-cyan {color: ' + withReducedSaturation('#00BCD4') + ';}' +
        '.col-teal {color: ' + withReducedSaturation('#009688') + ';}' +
        '.col-green {color: ' + withReducedSaturation('#4CAF50') + ';}' +
        '.col-light-green {color: ' + withReducedSaturation('#8BC34A') + ';}' +
        '.col-lime {color: ' + withReducedSaturation('#CDDC39') + ';}' +
        '.col-yellow {color: ' + withReducedSaturation('#ffe821') + ';}' +
        '.col-amber {color: ' + withReducedSaturation('#FFC107') + ';}' +
        '.text-warning {color: ' + withReducedSaturation('#f6c23e') + ';}' +
        '.col-orange {color: ' + withReducedSaturation('#FF9800') + ';}' +
        '.col-deep-orange {color: ' + withReducedSaturation('#FF5722') + ';}' +
        '.text-danger {color: ' + withReducedSaturation('#e74a3b') + ';}' +
        '.col-brown {color: ' + withReducedSaturation('#795548') + ';}' +
        '.col-grey {color: ' + withReducedSaturation('#9E9E9E') + ';}' +
        '.col-blue-grey {color: ' + withReducedSaturation('#607D8B') + ';}' +
        '.col-plan {color: ' + withReducedSaturation('#368F17') + ';}' +
        '.text-success {color: ' + withReducedSaturation('#1cc88a') + ';}';

    function changeNightModeCSS() {
        if (nightMode) {
            // Background colors from dracula theme
            $('head').append('<style id="nightmode">' +
                '#content {background-color:#282a36;}' +
                '.card,.bg-white,.modal-content,.page-loader,.nav-tabs .nav-link:hover,.nav-tabs,hr {background-color:#44475a;border-color:#6272a4!important;}' +
                '.bg-white.collapse-inner {border:1px solid;}' +
                '.card-header {background-color:#44475a;border-color:#6272a4;}' +
                '#content,.col-black,.text-gray-800,.collapse-item,.modal-title,.modal-body,.page-loader,.close,.fc-title,.fc-time,pre,.table-dark {color:#eee8d5 !important;}' +
                '.collapse-item:hover,.nav-link.active {background-color: #606270 !important;}' +
                '.nav-tabs .nav-link.active {background-color: #44475a !important;border-color:#6272a4 #6272a4 #44475a !important;}' +
                '.fc-today {background:#646e8c !important}' +
                nightModeColors +
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

    function changeHighChartsNightMode() {
        try {
            Highcharts.theme = nightMode ? {
                chart: {
                    backgroundColor: '#44475a',
                    plotBorderColor: '#606063'
                },
                title: {
                    style: {color: '#eee8d5'}
                },
                subtitle: {
                    style: {color: '#eee8d5'}
                },
                xAxis: {
                    gridLineColor: '#707073',
                    labels: {
                        style: {color: '#eee8d5'}
                    },
                    lineColor: '#707073',
                    minorGridLineColor: '#505053',
                    tickColor: '#707073',
                    title: {
                        style: {color: '#eee8d5'}
                    }
                },
                yAxis: {
                    gridLineColor: '#707073',
                    labels: {
                        style: {color: '#eee8d5'}
                    },
                    lineColor: '#707073',
                    minorGridLineColor: '#505053',
                    tickColor: '#707073',
                    tickWidth: 1,
                    title: {
                        style: {color: '#eee8d5'}
                    }
                },
                tooltip: {
                    backgroundColor: '#44475a',
                    style: {color: '#eee8d5'}
                },
                plotOptions: {
                    series: {
                        dataLabels: {color: '#B0B0B3'},
                        marker: {lineColor: '#333'}
                    }
                },
                legend: {
                    itemStyle: {color: '#eee8d5'},
                    itemHoverStyle: {color: '#eee8d5'},
                    itemHiddenStyle: {color: '#606063'}
                },
                labels: {
                    style: {color: '#eee8d5'}
                },
                drilldown: {
                    activeAxisLabelStyle: {color: '#eee8d5'},
                    activeDataLabelStyle: {color: '#eee8d5'}
                },
                navigation: {
                    buttonOptions: {
                        symbolStroke: '#eee8d5',
                        theme: {fill: '#44475a'}
                    }
                },
                // scroll charts
                rangeSelector: {
                    buttonTheme: {
                        fill: '#505053',
                        stroke: '#646e8c',
                        style: {color: '#CCC'},
                        states: {
                            hover: {
                                fill: '#646e9d',
                                stroke: '#646e8c',
                                style: {color: 'white'}
                            },
                            select: {
                                fill: '#646e9d',
                                stroke: '#646e8c',
                                style: {color: 'white'}
                            }
                        }
                    },
                    inputBoxBorderColor: '#505053',
                    inputStyle: {
                        backgroundColor: '#333',
                        color: 'silver'
                    },
                    labelStyle: {color: 'silver'}
                },

                navigator: {
                    handles: {
                        backgroundColor: '#666',
                        borderColor: '#AAA'
                    },
                    outlineColor: '#CCC',
                    maskFill: 'rgba(255,255,255,0.1)',
                    series: {lineColor: '#A6C7ED'},
                    xAxis: {gridLineColor: '#505053'}
                },

                scrollbar: {
                    barBackgroundColor: '#808083',
                    barBorderColor: '#808083',
                    buttonArrowColor: '#CCC',
                    buttonBackgroundColor: '#606063',
                    buttonBorderColor: '#606063',
                    rifleColor: '#FFF',
                    trackBackgroundColor: '#404043',
                    trackBorderColor: '#404043'
                }
            } : { // Defaults
                chart: {
                    backgroundColor: '#fff',
                    plotBorderColor: '#cccccc'
                },
                title: {
                    style: {color: '#3E576F'}
                },
                subtitle: {
                    style: {color: '#3E576F'}
                },
                xAxis: {
                    gridLineColor: '#E6E6E6',
                    labels: {
                        style: {color: '#333333'}
                    },
                    lineColor: '#E6E6E6',
                    minorGridLineColor: '#505053',
                    tickColor: '#E6E6E6',
                    title: {
                        style: {color: '#333333'}
                    }
                },
                yAxis: {
                    gridLineColor: '#E6E6E6',
                    labels: {
                        style: {color: '#333333'}
                    },
                    lineColor: '#E6E6E6',
                    minorGridLineColor: '#505053',
                    tickColor: '#E6E6E6',
                    tickWidth: 1,
                    title: {
                        style: {color: '#333333'}
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(247,247,247,0.85)',
                    style: {color: '#333333'}
                },
                plotOptions: {
                    series: {
                        dataLabels: {color: undefined},
                        marker: {lineColor: undefined}
                    }
                },
                legend: {
                    itemStyle: {color: '#333333'},
                    itemHoverStyle: {color: '#000000'},
                    itemHiddenStyle: {color: '#cccccc'}
                },
                labels: {
                    style: {color: '#333333'}
                },
                drilldown: {
                    activeAxisLabelStyle: {color: '#333333'},
                    activeDataLabelStyle: {color: '#333333'}
                },
                navigation: {
                    buttonOptions: {
                        symbolStroke: '#333333',
                        theme: {fill: '#CCCCCC'}
                    }
                },
                // scroll charts
                rangeSelector: {
                    buttonTheme: {
                        fill: '#F7F7F7',
                        stroke: '#333',
                        style: {color: '#4B336A'},
                        states: {
                            hover: {
                                fill: '#E6EBF5',
                                stroke: '#333',
                                style: {color: 'black'}
                            },
                            select: {
                                fill: '#E6EBF5',
                                stroke: '#333',
                                style: {color: 'black'}
                            }
                        }
                    },
                    inputBoxBorderColor: '#CCCCCC',
                    inputStyle: {
                        backgroundColor: '#333',
                        color: '#666666'
                    },
                    labelStyle: {color: "#666666"}
                },

                navigator: {
                    handles: {
                        backgroundColor: '#f2f2f2',
                        borderColor: '#999999'
                    },
                    outlineColor: '#cccccc',
                    maskFill: 'rgba(102,133,194,0.3)',
                    series: {lineColor: "#3FA0FF"},
                    xAxis: {gridLineColor: '#e6e6e6'}
                },

                scrollbar: {
                    barBackgroundColor: '#cccccc',
                    barBorderColor: '#cccccc',
                    buttonArrowColor: '#333333',
                    buttonBackgroundColor: '#e6e6e6',
                    buttonBorderColor: '#cccccc',
                    rifleColor: '#333333',
                    trackBackgroundColor: '#f2f2f2',
                    trackBorderColor: '#f2f2f2'
                }
            };
            Highcharts.setOptions(Highcharts.theme);
            updateGraphs();
        } catch (e) {
        }
    }

    changeNightModeCSS();
    changeHighChartsNightMode();

    function toggleNightMode() {
        nightMode = !nightMode;
        setTimeout(function () {
            window.localStorage.setItem('nightMode', nightMode);
            changeNightModeCSS();
            changeHighChartsNightMode();
        }, 0);
    }

    $('#night-mode-toggle').on('click', toggleNightMode);

})(jQuery);