function healthGauge(id, healthData) {
    var gaugeOptions = {

        chart: {
            type: 'solidgauge'
        },

        title: null,

        pane: {
            center: ['50%', '85%'],
            size: '140%',
            startAngle: -90,
            endAngle: 90,
            background: {
                backgroundColor: (Highcharts.theme && Highcharts.theme.background2) || '#EEE',
                innerRadius: '60%',
                outerRadius: '100%',
                shape: 'arc'
            }
        },

        tooltip: {
            enabled: false
        },

        // the value axis
        yAxis: {
            stops: [
                [0.1, '#DF5353'], // red
                [0.5, '#DDDF0D'], // yellow
                [0.9, '#55BF3B'] // green
            ],
            lineWidth: 0,
            minorTickInterval: null,
            tickAmount: 2,
            title: {
                y: -70
            },
            labels: {
                y: 16
            }
        },

        plotOptions: {
            solidgauge: {
                dataLabels: {
                    y: 5,
                    borderWidth: 0,
                    useHTML: true
                }
            }
        }
    };

    var chartSpeed = Highcharts.chart(id, Highcharts.merge(gaugeOptions, {
        yAxis: {
            min: 0,
            max: 100,
            title: {
                text: 'Server Health'
            },
            visible: false
        },

        credits: {
            enabled: false
        },

        series: [{
            name: 'health',
            data: healthData,
            dataLabels: {
                formatter: function () {
                    return '<div style="text-align:center"><span style="font-size:25px;color:' +
                        ((Highcharts.theme && Highcharts.theme.contrastTextColor) || 'black') + '">' + (this.y).toFixed(2) + '</span><br/>' +
                        '<span style="font-size:12px;color:silver">' + getLabel(this.y) + '</span></div>';
                }
            }
        }]

    }));
}

function getLabel(index) {
    if (index >= 80) {
        return 'Very Healthy';
    }
    if (index >= 60) {
        return 'Healthy';
    }
    if (index >= 50) {
        return 'Good';
    }
    if (index >= 30) {
        return 'OK';
    }
    if (index >= 0) {
        return 'Poor';
    }
}