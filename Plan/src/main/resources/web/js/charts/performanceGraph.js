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
        yAxis: [{
            labels: {
                formatter: function () {
                    return this.value + ' P';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + ' TPS';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + '%';
                }
            }
        }, {
            labels: {
                formatter: function () {
                    return this.value + ' MB';
                }
            }
        }, {
            opposite: true,
            labels: {
                formatter: function () {
                    return this.value + ' E';
                }
            }
        }, {
            labels: {
                formatter: function () {
                    return this.value + ' C';
                }
            }
        }],
        legend: {
            enabled: true
        },
        series: [playersOnlineSeries, tpsSeries, cpuSeries, ramSeries, entitySeries, chunkSeries]
    });
}