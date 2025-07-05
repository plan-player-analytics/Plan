// TODO 4114 change into CSS variables
export const getLightModeChartTheming = () => {
    return { // Defaults
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
}

export const getNightModeChartTheming = () => {
    return {
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
    };
}