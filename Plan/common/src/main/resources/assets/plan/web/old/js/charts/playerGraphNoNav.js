function playersChartNoNav(id, playersOnlineSeries) {
    Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 3,
            buttons: [{
                type: 'hour',
                count: 12,
                text: '12h'
            }, {
                type: 'hour',
                count: 24,
                text: '24h'
            }, {
                type: 'day',
                count: 7,
                text: '7d'
            }, {
                type: 'month',
                count: 1,
                text: '30d'
            }, {
                type: 'all',
                text: 'All'
            }]
        },
        navigator: {
            enabled: false
        },
        yAxis: {
            softMax: 2,
            softMin: 0
        },
        title: {text: ''},
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            }
        },
        series: [playersOnlineSeries]
    });
}