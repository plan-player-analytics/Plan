(function ($) {
    const bgElements = ['.sidebar', '.btn', 'body'];
    const textElements = ['a', 'button'];

    const colors = ['plan',
        'red', 'pink', 'purple', 'deep-purple',
        'indigo', 'blue', 'light-blue', 'cyan',
        'teal', 'green', 'light-green', 'lime',
        'yellow', 'amber', 'orange', 'deep-orange',
        'brown', 'grey', 'blue-grey'];

    const selectedColor = window.localStorage.getItem('themeColor');
    const themeDefaultColor = '${defaultTheme}';
    let currentColor = 'plan';

    // Function for changing color
    function setColor(nextColor) {
        if (selectedColor === null) {
            window.localStorage.setItem('themeColor', currentColor);
        }
        const bodyElement = document.querySelector('body');
        bodyElement.classList.remove(`theme-${currentColor}`);
        bodyElement.classList.add(`theme-${nextColor}`);

        if (!nextColor || nextColor == currentColor) {
            return;
        }

        bgElements.map(element => element + '.bg-' + currentColor + ":not(.color-chooser)")
            .forEach(selector => {
                document.querySelectorAll(selector).forEach(element => {
                    element.classList.remove(`bg-${currentColor}`);
                    element.classList.add(`bg-${nextColor}`);
                });
            });
        textElements.map(element => element + '.col-' + currentColor)
            .forEach(selector => {
                document.querySelectorAll(selector).forEach(element => {
                    element.classList.remove(`col-${currentColor}`);
                    element.classList.add(`col-${nextColor}`);
                });
            });
        if (nextColor != 'night') {
            window.localStorage.setItem('themeColor', nextColor);
        }
        currentColor = nextColor;
    }

    // Set the color changing function for all color change buttons
    function enableColorSetters() {
        for (const color of colors) {
            const selector = document.getElementById(`choose-${color}`);
            selector.removeAttribute('disabled');
            selector.classList.remove('disabled');
            selector.classList.add(`bg-${color}`);
            selector.addEventListener('click', () => setColor(color));
        }
    }

    enableColorSetters();

    function disableColorSetters() {
        for (const color of colors) {
            const selector = document.getElementById(`choose-${color}`);
            selector.classList.add('disabled');
            selector.setAttribute('disabled', 'true');
        }
    }

    // Change the color of the theme
    setColor(selectedColor ? selectedColor : themeDefaultColor);

    let nightMode = window.localStorage.getItem('nightMode') == 'true' || '${defaultTheme}' == 'night';

    const saturationReduction = 0.70;

    // From https://stackoverflow.com/a/3732187
    function withReducedSaturation(colorHex) {
        // To RGB
        let r = parseInt(colorHex.substr(1, 2), 16); // Grab the hex representation of red (chars 1-2) and convert to decimal (base 10).
        let g = parseInt(colorHex.substr(3, 2), 16);
        let b = parseInt(colorHex.substr(5, 2), 16);

        // To HSL
        r /= 255;
        g /= 255;
        b /= 255;
        const max = Math.max(r, g, b), min = Math.min(r, g, b);
        let h, s;
        const l = (max + min) / 2;

        if (max === min) {
            h = s = 0; // achromatic
        } else {
            const d = max - min;
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

    const red = withReducedSaturation('#F44336');
    const pink = withReducedSaturation('#E91E63');
    const purple = withReducedSaturation('#9C27B0');
    const deepPurple = withReducedSaturation('#673AB7');
    const indigo = withReducedSaturation('#3F51B5');
    const blue = withReducedSaturation('#2196F3');
    const lightBlue = withReducedSaturation('#03A9F4');
    const cyan = withReducedSaturation('#00BCD4');
    const teal = withReducedSaturation('#009688');
    const green = withReducedSaturation('#4CAF50');
    const lightGreen = withReducedSaturation('#8BC34A');
    const lime = withReducedSaturation('#CDDC39');
    const yellow = withReducedSaturation('#ffe821');
    const amber = withReducedSaturation('#FFC107');
    const warningColor = withReducedSaturation('#f6c23e');
    const orange = withReducedSaturation('#FF9800');
    const deepOrange = withReducedSaturation('#FF5722');
    const dangerColor = withReducedSaturation('#e74a3b');
    const brown = withReducedSaturation('#795548');
    const grey = withReducedSaturation('#9E9E9E');
    const blueGrey = withReducedSaturation('#607D8B');
    const black = withReducedSaturation('#555555');
    const planColor = withReducedSaturation('#368F17');
    const successColor = withReducedSaturation('#1cc88a');
    const nightModeColors = `.bg-red {background-color: ${red};color: #eee8d5;}` +
        `.bg-pink {background-color: ${pink};color: #eee8d5;}` +
        `.bg-purple {background-color: ${purple};color: #eee8d5;}` +
        `.bg-deep-purple {background-color: ${deepPurple};color: #eee8d5;}` +
        `.bg-indigo {background-color: ${indigo};color: #eee8d5;}` +
        `.bg-blue {background-color: ${blue};color: #eee8d5;}` +
        `.bg-light-blue {background-color: ${lightBlue};color: #eee8d5;}` +
        `.bg-cyan {background-color: ${cyan};color: #eee8d5;}` +
        `.bg-teal {background-color: ${teal};color: #eee8d5;}` +
        `.bg-green {background-color: ${green};color: #eee8d5;}` +
        `.bg-light-green {background-color: ${lightGreen};color: #eee8d5;}` +
        `.bg-lime {background-color: ${lime};color: #eee8d5;}` +
        `.bg-yellow {background-color: ${yellow};color: #eee8d5;}` +
        `.bg-amber {background-color: ${amber};color: #eee8d5;}` +
        `.bg-warning {background-color: ${warningColor};color: #eee8d5;}` +
        `.bg-orange {background-color: ${orange};color: #eee8d5;}` +
        `.bg-deep-orange {background-color: ${deepOrange};color: #eee8d5;}` +
        `.bg-danger {background-color: ${dangerColor};color: #eee8d5;}` +
        `.bg-brown {background-color: ${brown};color: #eee8d5;}` +
        `.bg-grey {background-color: ${grey};color: #eee8d5;}` +
        `.bg-blue-grey {background-color: ${blueGrey};color: #eee8d5;}` +
        `.bg-black {background-color: ${black};color: #eee8d5;}` +
        `.bg-plan,.page-item.active .page-link {background-color: ${planColor};color: #eee8d5;}` +
        `.bg-success {background-color: ${successColor};color: #eee8d5;}` +
        `.bg-night {background-color: #44475a;color: #eee8d5;}` +
        `.bg-red-outline {outline-color: ${red};border-color: ${red};}` +
        `.bg-pink-outline {outline-color: ${pink};border-color: ${pink};}` +
        `.bg-purple-outline {outline-color: ${purple};border-color: ${purple};}` +
        `.bg-deep-purple-outline {outline-color: ${deepPurple};border-color: ${deepPurple};}` +
        `.bg-indigo-outline {outline-color: ${indigo};border-color: ${indigo};}` +
        `.bg-blue-outline {outline-color: ${blue};border-color: ${blue};}` +
        `.bg-light-blue-outline {outline-color: ${lightBlue};border-color: ${lightBlue};}` +
        `.bg-cyan-outline {outline-color: ${cyan};border-color: ${cyan};}` +
        `.bg-teal-outline {outline-color: ${teal};border-color: ${teal};}` +
        `.bg-green-outline {outline-color: ${green};border-color: ${green};}` +
        `.bg-light-green-outline {outline-color: ${lightGreen};border-color: ${lightGreen};}` +
        `.bg-lime-outline {outline-color: ${lime};border-color: ${lime};}` +
        `.bg-yellow-outline {outline-color: ${yellow};border-color: ${yellow};}` +
        `.bg-amber-outline {outline-color: ${amber};border-color: ${amber};}` +
        `.bg-orange-outline {outline-color: ${orange};border-color: ${orange};}` +
        `.bg-deep-orange-outline {outline-color: ${deepOrange};border-color: ${deepOrange};}` +
        `.bg-brown-outline {outline-color: ${brown};border-color: ${brown};}` +
        `.bg-grey-outline {outline-color: ${grey};border-color: ${grey};}` +
        `.bg-blue-grey-outline {outline-color: ${blueGrey};border-color: ${blueGrey};}` +
        `.bg-black-outline {outline-color: ${black};border-color: ${black};}` +
        `.bg-plan-outline {outline-color: ${planColor};border-color: ${planColor};}` +
        `.col-red {color: ${red};}` +
        `.col-pink {color: ${pink};}` +
        `.col-purple {color: ${purple};}` +
        `.col-deep-purple {color: ${deepPurple};}` +
        `.col-indigo {color: ${indigo};}` +
        `.col-blue {color: ${blue};}` +
        `.col-light-blue {color: ${lightBlue};}` +
        `.col-cyan {color: ${cyan};}` +
        `.col-teal {color: ${teal};}` +
        `.col-green {color: ${green};}` +
        `.col-light-green {color: ${lightGreen};}` +
        `.col-lime {color: ${lime};}` +
        `.col-yellow {color: ${yellow};}` +
        `.col-amber {color: ${amber};}` +
        `.text-warning {color: ${warningColor};}` +
        `.col-orange {color: ${orange};}` +
        `.col-deep-orange {color: ${deepOrange};}` +
        `.text-danger {color: ${dangerColor};}` +
        `.col-brown {color: ${brown};}` +
        `.col-grey {color: ${grey};}` +
        `.col-blue-grey {color: ${blueGrey};}` +
        `.col-plan {color: ${planColor};}` +
        `.text-success {color: ${successColor};}`;

    function changeNightModeCSS() {
        if (nightMode) {
            // Background colors from dracula theme
            $('head').append('<style id="nightmode">' +
                '#content {background-color:#282a36;}' +
                '.btn {color: #eee8d5;}' +
                '.card,.bg-white,.modal-content,.page-loader,.nav-tabs .nav-link:hover,.nav-tabs,hr,form .btn, .btn-outline-secondary{background-color:#44475a!important;border-color:#6272a4!important;}' +
                '.bg-white.collapse-inner {border:1px solid;}' +
                '.card-header {background-color:#44475a;border-color:#6272a4;}' +
                '#content,.col-black,.text-gray-900,.text-gray-800,.collapse-item,.modal-title,.modal-body,.page-loader,.fc-title,.fc-time,pre,.table-dark,input::placeholder{color:#eee8d5 !important;}' +
                '.collapse-item:hover,.nav-link.active {background-color: #606270 !important;}' +
                '.nav-tabs .nav-link.active {background-color: #44475a !important;border-color:#6272a4 #6272a4 #44475a !important;}' +
                '.fc-today {background:#646e8c !important}' +
                '.fc-popover-body,.fc-popover-header{background-color: #44475a !important;color: #eee8d5 !important;}' +
                'select,input,.dataTables_paginate .page-item:not(.active) a,.input-group-text,.input-group-text > * {background-color:#44475a !important;border-color:#6272a4 !important;color: #eee8d5 !important;}' +
                nightModeColors +
                '</style>');
            // Turn bright tables to dark
            $('.table').addClass('table-dark');
            // Turn modal close buttons light
            document.querySelectorAll('button.btn-close').forEach(element => { element.classList.add('btn-close-white'); })
            // Sidebar is grey when in night mode
            disableColorSetters();
            setColor('night');
        } else {
            // Remove night mode style sheet
            $('#nightmode').remove();
            // Turn dark tables bright again
            $('.table').removeClass('table-dark');
            // Turn modal close buttons dark
            document.querySelectorAll('button.btn-close').forEach(element => { element.classList.remove('btn-close-white'); })
            // Sidebar is colorful
            enableColorSetters();
            setColor(window.localStorage.getItem('themeColor'));
        }
    }

    function changeHighChartsNightMode() {
        try {
            Highcharts.theme = nightMode ? {
                chart: {
                    backgroundColor: null,
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
                    backgroundColor: null,
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
            if ("Highcharts is not defined" === e.message || "updateGraphs is not defined" === e.message) {
                // Highcharts isn't loaded, can be ignored
            } else {
                console.error(e);
            }
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