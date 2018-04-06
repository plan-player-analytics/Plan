function stackChart(id, categories, series, label) {
    Highcharts.chart(id, {
        chart: {
            type: 'area'
        },
        title: {
            text: ''
        },
        xAxis: {
            categories: categories,
            tickmarkPlacement: 'on',
            title: {
                enabled: false
            }
        },
        yAxis: {
            title: {
                text: label
            },
            labels: {
                formatter: function () {
                    return this.value;
                }
            },
            softMax: 2,
            softMin: 0
        },
        tooltip: {
            split: true,
            valueSuffix: ' ' + label
        },
        plotOptions: {
            area: {
                stacking: 'normal',
                lineWidth: 1
            }
        },
        series: series
    });
}