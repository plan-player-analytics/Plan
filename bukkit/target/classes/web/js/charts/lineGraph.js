function lineChart(id, series) {
    Highcharts.stockChart(id, {
        rangeSelector: {
            selected: 2,
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
        yAxis: {
            softMax: 2,
            softMin: 0
        },
        title: {text: ''},
        legend: {
            enabled: true
        },
        series: series
    });
}