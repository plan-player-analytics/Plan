export const getLightModeChartTheming = () => {
    return { // Defaults
        chart: {
            backgroundColor: null,
            plotBorderColor: 'var(--color-text)'
        },
        title: {
            style: {color: 'var(--color-text)'}
        },
        subtitle: {
            style: {color: 'var(--color-text)'}
        },
        xAxis: {
            gridLineColor: 'var(--color-graphs-style-grid-line)',
            labels: {
                style: {color: 'var(--color-text)'}
            },
            lineColor: 'var(--color-graphs-style-grid-line)',
            minorGridLineColor: 'var(--color-graphs-style-minor-grid-line)',
            tickColor: 'var(--color-graphs-style-grid-line)',
            title: {
                style: {color: 'var(--color-text)'}
            }
        },
        yAxis: {
            gridLineColor: 'var(--color-graphs-style-grid-line)',
            labels: {
                style: {color: 'var(--color-text)'}
            },
            lineColor: 'var(--color-graphs-style-grid-line)',
            minorGridLineColor: 'var(--color-graphs-style-minor-grid-line)',
            tickColor: 'var(--color-graphs-style-grid-line)',
            tickWidth: 1,
            title: {
                style: {color: 'var(--color-text)'}
            }
        },
        tooltip: {
            backgroundColor: 'var(--color-graphs-style-tooltip-background)',
            style: {color: 'var(--color-text)'}
        },
        plotOptions: {
            series: {
                dataLabels: {color: undefined},
                marker: {lineColor: undefined},
                borderColor: 'var(--color-graphs-style-border)'
            }
        },
        legend: {
            itemStyle: {color: 'var(--color-text)'},
            itemHoverStyle: {color: 'var(--color-text)'},
            itemHiddenStyle: {color: 'color-mix(in srgb, var(--color-text), transparent 50%)'}
        },
        labels: {
            style: {color: 'var(--color-text)'}
        },
        drilldown: {
            activeAxisLabelStyle: {color: 'var(--color-text)'},
            activeDataLabelStyle: {color: 'var(--color-text)'}
        },
        navigation: {
            buttonOptions: {
                symbolStroke: 'var(--color-text)',
                theme: {fill: 'var(--color-text)'}
            }
        },
        // scroll charts
        rangeSelector: {
            buttonTheme: {
                fill: 'var(--color-graphs-style-selector-button-background)',
                stroke: 'var(--color-text)',
                style: {color: 'var(--color-text)'},
                states: {
                    hover: {
                        fill: 'var(--color-graphs-style-selector-button-hover)',
                        stroke: 'var(--color-text)',
                        style: {color: 'var(--color-text)'}
                    },
                    select: {
                        fill: 'var(--color-graphs-style-selector-button-selected)',
                        stroke: 'var(--color-text)',
                        style: {color: 'var(--color-text)'}
                    }
                }
            },
            inputBoxBorderColor: 'var(--color-graphs-style-selector-text-input-border)',
            inputStyle: {
                backgroundColor: 'var(--color-graphs-style-selector-text-input-background)',
                color: 'var(--color-text)'
            },
            labelStyle: {color: 'var(--color-text)'}
        },

        navigator: {
            handles: {
                backgroundColor: 'var(--color-graphs-style-selector-range-handle-background)',
                borderColor: 'var(--color-graphs-style-selector-range-handle-border)'
            },
            outlineColor: 'var(--color-graphs-style-selector-range-outline)',
            maskFill: 'color-mix(in srgb, var(--color-graphs-style-selector-range-selected-area), transparent 85%)',
            series: {lineColor: "var(--color-graphs-style-selector-range-series-line)"},
            xAxis: {gridLineColor: 'var(--color-graphs-style-grid-line)'}
        },

        scrollbar: {
            barBackgroundColor: 'var(--color-graphs-style-scrollbar-bar-background)',
            barBorderColor: 'var(--color-graphs-style-scrollbar-bar-background)',
            buttonArrowColor: 'var(--color-text)',
            buttonBackgroundColor: 'var(--color-graphs-style-scrollbar-button-background)',
            buttonBorderColor: 'var(--color-graphs-style-scrollbar-button-background)',
            rifleColor: 'var(--color-graphs-style-scrollbar-decoration)',
            trackBackgroundColor: 'var(--color-graphs-style-scrollbar-track-background)',
            trackBorderColor: 'var(--color-graphs-style-scrollbar-track-background)',
        }
        // mapNavigation: { 4114 TODO look at some point, only color and fill works
        //     buttonOptions: {
        //         style: {
        //             color: 'var(--color-text)'
        //         },
        //         theme: {
        //             fill: 'var(--color-graphs-style-selector-button-background)'
        //         }
        //     }
        // }
    };
}

export const getNightModeChartTheming = () => {
    return {
        chart: {
            backgroundColor: null,
            plotBorderColor: '#606063'
        },
        title: {
            style: {color: 'var(--color-night-text)'}
        },
        subtitle: {
            style: {color: 'var(--color-night-text)'}
        },
        xAxis: {
            gridLineColor: '#707073',
            labels: {
                style: {color: 'var(--color-night-text)'}
            },
            lineColor: '#707073',
            minorGridLineColor: '#505053',
            tickColor: '#707073',
            title: {
                style: {color: 'var(--color-night-text)'}
            }
        },
        yAxis: {
            gridLineColor: '#707073',
            labels: {
                style: {color: 'var(--color-night-text)'}
            },
            lineColor: '#707073',
            minorGridLineColor: '#505053',
            tickColor: '#707073',
            tickWidth: 1,
            title: {
                style: {color: 'var(--color-night-text)'}
            }
        },
        tooltip: {
            backgroundColor: '#44475a',
            style: {color: 'var(--color-night-text)'}
        },
        plotOptions: {
            series: {
                dataLabels: {color: '#B0B0B3'},
                marker: {lineColor: '#333'}
            }
        },
        legend: {
            itemStyle: {color: 'var(--color-night-text)'},
            itemHoverStyle: {color: 'var(--color-night-text)'},
            itemHiddenStyle: {color: '#606063'}
        },
        labels: {
            style: {color: 'var(--color-night-text)'}
        },
        drilldown: {
            activeAxisLabelStyle: {color: 'var(--color-night-text)'},
            activeDataLabelStyle: {color: 'var(--color-night-text)'}
        },
        navigation: {
            buttonOptions: {
                symbolStroke: 'var(--color-night-text)',
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
    };
}