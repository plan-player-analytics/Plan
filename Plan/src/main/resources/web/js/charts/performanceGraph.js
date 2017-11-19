function performanceChart(id, playersOnlineSeries, tpsSeries, cpuSeries, ramSeries, entitySeries, chunkSeries) {
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
        title: {text: ''},
        legend: {
            enabled: true,
        },
        series: [playersOnlineSeries, tpsSeries, cpuSeries, ramSeries, entitySeries, chunkSeries]
    });
}